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

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.javadoc.JavadocBlockTag;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

/**
 * A task that changes a JFlex's generated class to be a {@code RSyntaxTextArea}'s {@code
 * TokenMaker}.
 *
 * <p>Changes done to the generated class:
 *
 * <ul>
 *   <li>Add default constructor;
 *   <li>Remove methods {@code zzRefill} and {@code yyreset}, replaced with custom implementation;
 *   <li>Remove method {@code yyResetPosition}, not used;
 *   <li>Remove initialisation of {@code zzBuffer}, not needed;
 *   <li>Remove constant {@code ZZ_BUFFERSIZE}, not used;
 *   <li>Remove {@code throws IOException} from {@code yylex()}, not actually thrown;
 *   <li>Add {@code @SuppressWarnings("fallthrough")} annotation to {@code yylex()};
 *   <li>Add {@code @Override} annotation to {@code yybegin} and {@code yyclose}.
 * </ul>
 *
 * @see JFlexGenerator
 */
public class JFlexToRstaTokenMaker extends SourceTask {

    private final DirectoryProperty outputDirectory;

    public JFlexToRstaTokenMaker() {
        this.outputDirectory = getProject().getObjects().directoryProperty();

        include("**/*.java");
    }

    @OutputDirectory
    public DirectoryProperty getOutputDirectory() {
        return outputDirectory;
    }

    @TaskAction
    public void process() {
        Path outputDir = outputDirectory.getAsFile().get().toPath();
        getSource().visit(source -> processSource(source, outputDir));
    }

    private void processSource(FileVisitDetails source, Path outputDir) {
        if (source.isDirectory()) {
            return;
        }

        CompilationUnit compilationUnit = parseJavaSource(source.getFile());
        TypeDeclaration<?> type = compilationUnit.getType(0);

        type.addConstructor(Modifier.Keyword.PUBLIC);

        removeMethod(type, "zzRefill", "yyreset", "yyResetPosition");

        type.getFieldByName("zzBuffer").ifPresent(JFlexToRstaTokenMaker::removeInitialisation);
        type.getFieldByName("ZZ_BUFFERSIZE").ifPresent(Node::remove);

        type.getMethodsByName("yylex")
                .forEach(
                        method -> {
                            removeThrowsIoException(method);

                            method.addSingleMemberAnnotation(
                                    SuppressWarnings.class, "\"fallthrough\"");
                        });

        addOverrideAnnotation(type, "yybegin", "yyclose");

        Path outputFile = outputDir.resolve(source.getRelativePath().getPathString());
        try {
            Files.createDirectories(outputFile.getParent());
            Files.write(outputFile, compilationUnit.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new BuildException("Failed to save the processed source: " + e.getMessage(), e);
        }
    }

    private static CompilationUnit parseJavaSource(File file) {
        try {
            return StaticJavaParser.parse(file);
        } catch (FileNotFoundException e) {
            throw new BuildException("File not found: " + file, e);
        }
    }

    private static void addOverrideAnnotation(TypeDeclaration<?> type, String... names) {
        if (names == null || names.length == 0) {
            return;
        }

        Stream.of(names)
                .map(type::getMethodsByName)
                .flatMap(List::stream)
                .forEach(method -> method.addMarkerAnnotation(Override.class));
    }

    private static void removeInitialisation(FieldDeclaration field) {
        field.getVariables().forEach(variable -> variable.getInitializer().ifPresent(Node::remove));
    }

    private static void removeThrowsIoException(MethodDeclaration method) {
        method.getThrownExceptions().clear();
        method.getJavadoc()
                .ifPresent(
                        javadoc -> {
                            javadoc.getBlockTags().removeIf(JFlexToRstaTokenMaker::isException);
                            method.setJavadocComment(javadoc);
                        });
    }

    private static boolean isException(JavadocBlockTag blockTag) {
        return JavadocBlockTag.Type.EXCEPTION == blockTag.getType();
    }

    private static void removeMethod(TypeDeclaration<?> type, String... names) {
        if (names == null || names.length == 0) {
            return;
        }

        for (String name : names) {
            List<MethodDeclaration> methods = type.getMethodsByName(name);
            MethodDeclaration method = methods.get(methods.size() - 1);
            method.getJavadocComment().ifPresent(Node::remove);
            method.remove();
        }
    }
}
