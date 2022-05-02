/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.RelativePath;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;

/** A task that runs JFlex for given lexers. */
@CacheableTask
public class JFlexGenerator extends SourceTask {

    private final ConfigurableFileCollection classpath;
    private final DirectoryProperty outputDirectory;

    public JFlexGenerator() {
        this.outputDirectory = getProject().getObjects().directoryProperty();
        this.classpath = getProject().files();

        source("src/main/flex");
        include("**/*.flex");
    }

    @Override
    @PathSensitive(PathSensitivity.RELATIVE)
    public FileTree getSource() {
        return super.getSource();
    }

    @Classpath
    public ConfigurableFileCollection getClasspath() {
        return classpath;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDirectory() {
        return outputDirectory;
    }

    @TaskAction
    public void generate() {
        File baseOutputDir = outputDirectory.getAsFile().get();
        getSource().visit(source -> generate(source, baseOutputDir));
    }

    private void generate(FileVisitDetails source, File baseOutputDir) {
        if (source.isDirectory()) {
            return;
        }

        RelativePath relPath = source.getRelativePath();

        List<String> args = new ArrayList<>();
        args.add("--encoding");
        args.add("UTF-8");
        args.add("--nobak");
        args.add("--quiet");
        args.add("-d");
        args.add(getOutputDir(baseOutputDir, relPath));
        args.add(source.getFile().getAbsolutePath());

        ExecResult result =
                getProject()
                        .javaexec(
                                spec -> {
                                    spec.getMainClass().set("jflex.Main");
                                    spec.setClasspath(classpath)
                                            .args(args)
                                            .setStandardOutput(System.out)
                                            .setErrorOutput(System.err)
                                            .setWorkingDir(
                                                    getWorkingDir(source.getFile(), relPath));
                                });

        result.assertNormalExitValue();
    }

    private String getOutputDir(File baseOutputDir, RelativePath relPath) {
        return new File(baseOutputDir, relPath.getParent().getPathString()).toString();
    }

    private File getWorkingDir(File file, RelativePath relPath) {
        String sourcePath = file.getAbsolutePath();
        return new File(sourcePath.substring(0, sourcePath.length() - relPath.toString().length()));
    }
}
