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

import com.github.zafarkhaja.semver.Version;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.zaproxy.zap.tasks.internal.JapicmpExcludedData;
import org.zaproxy.zap.tasks.internal.ProjectProperties;
import org.zaproxy.zap.tasks.internal.Utils;

/** A task that prepares the next development iteration of ZAP. */
public abstract class PrepareNextDevIter extends DefaultTask {

    @Input
    public abstract Property<File> getPropertiesFile();

    @Input
    public abstract Property<String> getVersionProperty();

    @Input
    public abstract Property<String> getVersionBcProperty();

    @Input
    public abstract Property<File> getJapicmpExcludedDataFile();

    @TaskAction
    public void prepare() throws Exception {
        updatePropertiesFile();

        Path japicmpExcludedDataFile = getJapicmpExcludedDataFile().get().toPath();
        Utils.updateYaml(
                new JapicmpExcludedData(), japicmpExcludedDataFile, japicmpExcludedDataFile);
    }

    private void updatePropertiesFile() throws IOException {
        String versionProperty = getVersionProperty().get();
        ProjectProperties properties = new ProjectProperties(getPropertiesFile().get().toPath());
        String currentVersion = properties.getProperty(versionProperty);
        Version newVersion =
                Version.valueOf(currentVersion)
                        .incrementMinorVersion()
                        .setPreReleaseVersion("SNAPSHOT");
        properties.setProperty(versionProperty, newVersion.toString());
        properties.setProperty(getVersionBcProperty().get(), currentVersion);
        properties.store();
    }
}
