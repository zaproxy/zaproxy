/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2023 The ZAP Development Team
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
import java.util.List;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

public abstract class CreateDmg extends DefaultTask {

    @Input
    public abstract Property<File> getWorkingDir();

    @Input
    public abstract Property<String> getVolname();

    @OutputFile
    public abstract RegularFileProperty getDmg();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @TaskAction
    void create() throws IOException {
        File dmgFile = getDmg().getAsFile().get();
        Files.createDirectories(dmgFile.getParentFile().toPath());

        File workingDir = getWorkingDir().get();

        getExecOperations()
                .exec(
                        spec -> {
                            spec.executable("hdiutil").workingDir(workingDir);
                            spec.args(
                                    List.of(
                                            "create",
                                            "-format",
                                            "UDBZ",
                                            "-fs",
                                            "HFS+",
                                            "-srcfolder",
                                            workingDir.toString(),
                                            "-volname",
                                            getVolname().get(),
                                            dmgFile.toString()));
                        });
    }
}
