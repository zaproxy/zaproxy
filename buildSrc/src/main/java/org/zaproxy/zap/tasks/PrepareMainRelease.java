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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

/** A task that prepares the main release of ZAP. */
public abstract class PrepareMainRelease extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getBuildFile();

    @Input
    public abstract Property<Pattern> getVersionPattern();

    @TaskAction
    public void prepare() throws Exception {
        Path updatedBuildFile = updateBuildFile();

        Files.copy(
                updatedBuildFile,
                getBuildFile().getAsFile().get().toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }

    private Path updateBuildFile() throws IOException {
        Path buildFilePath = getBuildFile().getAsFile().get().toPath();
        String contents = new String(Files.readAllBytes(buildFilePath), StandardCharsets.UTF_8);

        Matcher version = getVersionPattern().get().matcher(contents);
        if (!version.find()) {
            throw new BuildException("Version pattern not found.");
        }
        String currentVersion = version.group(1);
        String newVersion = removePreReleaseVersion(currentVersion);
        contents = replace(contents, version, newVersion);

        Path updatedBuildFile =
                getTemporaryDir().toPath().resolve("updated-" + buildFilePath.getFileName());
        Files.write(updatedBuildFile, contents.getBytes(StandardCharsets.UTF_8));

        return updatedBuildFile;
    }

    private static String replace(String value, Matcher matcher, String replacement) {
        return new StringBuilder()
                .append(value, 0, matcher.start(1))
                .append(replacement)
                .append(value, matcher.end(1), value.length())
                .toString();
    }

    private static String removePreReleaseVersion(String version) {
        int idx = version.indexOf("-SNAPSHOT");
        if (idx == -1) {
            throw new BuildException("The version does not contain -SNAPSHOT: " + version);
        }
        return version.substring(0, idx);
    }
}
