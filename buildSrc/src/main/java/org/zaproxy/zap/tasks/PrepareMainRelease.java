/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.zaproxy.zap.tasks.internal.ProjectProperties;

/** A task that prepares the main release of ZAP. */
public abstract class PrepareMainRelease extends DefaultTask {

    @Input
    public abstract Property<File> getPropertiesFile();

    @Input
    public abstract Property<String> getOldVersionProperty();

    @Input
    public abstract Property<String> getVersionProperty();

    @Input
    public abstract Property<File> getSecurityFile();

    @Input
    public abstract Property<File> getSnapcraftFile();

    @TaskAction
    public void prepare() throws Exception {
        ProjectProperties properties = new ProjectProperties(getPropertiesFile().get().toPath());
        String newVersion = updatePropertiesFile(properties);
        String oldVersion = properties.getProperty(getOldVersionProperty().get());
        replaceVersion(getSecurityFile().get().toPath(), oldVersion, newVersion);
        replaceVersion(getSnapcraftFile().get().toPath(), oldVersion, newVersion);
    }

    private String updatePropertiesFile(ProjectProperties properties) throws IOException {
        String versionProperty = getVersionProperty().get();
        String newVersion = removePreReleaseVersion(properties.getProperty(versionProperty));
        properties.setProperty(getVersionProperty().get(), newVersion);
        properties.store();
        return newVersion;
    }

    private void replaceVersion(Path path, String oldVersion, String newVersion)
            throws IOException {
        String content = Files.readString(path);
        Files.writeString(path, content.replace(oldVersion, newVersion));
    }

    private static String removePreReleaseVersion(String version) {
        int idx = version.indexOf("-SNAPSHOT");
        if (idx == -1) {
            throw new BuildException("The version does not contain -SNAPSHOT: " + version);
        }
        return version.substring(0, idx);
    }
}
