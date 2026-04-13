/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

/**
 * A task that updates the third-party library table in LEGALNOTICE.md.
 *
 * <p>Reads library licenses from a YAML mapping file and replaces the table (from the table header
 * to end of file) in the legal notice file with a freshly generated one.
 */
public abstract class UpdateLegalNotice extends DefaultTask {

    private static final String LIBRARY_HEADER = "Library";
    private static final String LICENSE_HEADER = "License";

    /** The runtime classpath configuration to inspect for third-party libraries. */
    @InputFiles
    @PathSensitive(PathSensitivity.NAME_ONLY)
    public abstract Property<Configuration> getConfiguration();

    /**
     * The YAML file mapping Maven {@code groupId:artifactId} coordinates to their license strings.
     */
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getLicensesFile();

    /** The legal notice file to update. */
    @OutputFile
    public abstract RegularFileProperty getLegalNoticeFile();

    @TaskAction
    public void update() throws IOException {
        Set<TableRow> rows =
                createRows(
                        getConfiguration().get().getResolvedConfiguration().getResolvedArtifacts(),
                        getLicensesFile().get().getAsFile().toPath());

        int libWidth = Math.max(LIBRARY_HEADER.length(), 35);
        int licWidth = LICENSE_HEADER.length();
        for (TableRow row : rows) {
            libWidth = Math.max(libWidth, row.fileName().length());
            licWidth = Math.max(licWidth, row.licenseName().length());
        }

        Path legalNoticeFile = getLegalNoticeFile().get().getAsFile().toPath();

        StringBuilder fileContent = new StringBuilder();
        fileContent.append(readMainContent(legalNoticeFile));

        String rowPattern = "| %-" + libWidth + "s | %-" + licWidth + "s |\n";
        appendRow(fileContent, rowPattern, LIBRARY_HEADER, LICENSE_HEADER);
        appendSeparator(fileContent, libWidth, licWidth);
        for (TableRow row : rows) {
            appendRow(fileContent, rowPattern, row.fileName(), row.licenseName());
        }

        Files.writeString(legalNoticeFile, fileContent);
    }

    @SuppressWarnings("unchecked")
    private static Set<TableRow> createRows(Set<ResolvedArtifact> artifacts, Path licensesFile)
            throws IOException {
        Map<String, Object> yaml =
                new ObjectMapper(new YAMLFactory()).readValue(licensesFile.toFile(), Map.class);
        Map<String, String> licenseMap = (Map<String, String>) yaml.get("licenses");
        if (licenseMap == null) {
            throw new GradleException(
                    "Licenses YAML file is missing the 'licenses' key: " + licensesFile);
        }

        Set<TableRow> rows = new TreeSet<>(Comparator.comparing(TableRow::fileName));
        for (ResolvedArtifact artifact : artifacts) {
            ModuleComponentIdentifier id =
                    (ModuleComponentIdentifier) artifact.getId().getComponentIdentifier();
            String coordinates = id.getGroup() + ":" + id.getModule();
            String fileName = artifact.getFile().getName();
            String license = licenseMap.get(coordinates);
            if (license == null) {
                throw new GradleException(
                        "No license mapping found for '"
                                + coordinates
                                + "' (JAR: "
                                + fileName
                                + "). Add it to: "
                                + licensesFile.getFileName());
            }
            rows.add(new TableRow(fileName, license));
        }

        return rows;
    }

    private static String readMainContent(Path legalNoticeFile) throws IOException {
        String content = Files.readString(legalNoticeFile);
        int tableStart = content.indexOf("\n| " + LIBRARY_HEADER);
        if (tableStart == -1) {
            throw new GradleException(
                    "Could not find table header ('| "
                            + LIBRARY_HEADER
                            + "') in: "
                            + legalNoticeFile);
        }
        return content.substring(0, tableStart + 1);
    }

    private static void appendRow(
            StringBuilder fileContent, String rowPattern, String library, String license) {
        fileContent.append(String.format(rowPattern, library, license));
    }

    private static void appendSeparator(StringBuilder fileContent, int libWidth, int licWidth) {
        fileContent
                .append('|')
                .append("-".repeat(libWidth + 2))
                .append("|")
                .append("-".repeat(licWidth + 2))
                .append("|")
                .append('\n');
    }

    private record TableRow(String fileName, String licenseName) {}
}
