/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.extension.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.parosproxy.paros.Constant;

/** Unit test for {@link LangImporter}. */
class LangImporterUnitTest {

    @Test
    void shouldIncludeMessagesFiles() {
        // Given
        String[] resourceFiles = {
            "Messages.properties",
            "Messages_en.properties",
            "Messages_en_GB.properties",
            "Messages_ar_SA.properties",
            "Messages_fil_PH.properties",
            "Messages_zh_CN.properties",
        };
        // When
        Pattern pattern = LangImporter.createIncludedFilesPattern();
        // Then
        for (String file : resourceFiles) {
            assertThat(file, pattern.matcher(file).matches(), is(equalTo(true)));
        }
    }

    @Test
    void shouldNotMatchOtherFilesThanMessagesFiles() {
        // Given
        String[] resourceFiles = {"OtherFile_ar_SA.properties", "messages.properties"};
        // When
        Pattern pattern = LangImporter.createIncludedFilesPattern();
        // Then
        for (String file : resourceFiles) {
            assertThat(file, pattern.matcher(file).matches(), is(equalTo(false)));
        }
    }

    @Test
    void shouldImportMessagesFileInsideInstallDirectory(@TempDir Path tempDir) throws Exception {
        // Given
        Path installDir = Files.createDirectory(tempDir.resolve("install"));
        Path langDir = Files.createDirectory(installDir.resolve("lang"));
        Path languagePack = createLanguagePack(tempDir, "lang/Messages_en_GB.properties", "safe");

        // When
        String result = importLanguagePack(installDir, languagePack);

        // Then
        assertThat(result, is(equalTo("options.lang.importer.dialog.message.success")));
        assertThat(Files.readString(langDir.resolve("Messages_en_GB.properties")), is("safe"));
    }

    @Test
    void shouldNotImportMessagesFileOutsideInstallDirectory(@TempDir Path tempDir)
            throws Exception {
        // Given
        Path installDir = Files.createDirectory(tempDir.resolve("install"));
        Files.createDirectory(installDir.resolve("lang"));
        Path outsideDir = Files.createDirectory(tempDir.resolve("outside"));
        Path outsideFile = Files.writeString(outsideDir.resolve("Messages.properties"), "original");
        Path languagePack =
                createLanguagePack(
                        tempDir, "lang/../../outside/Messages.properties", "overwritten");

        // When
        String result = importLanguagePack(installDir, languagePack);

        // Then
        assertThat(result, is(equalTo("options.lang.importer.dialog.message.error")));
        assertThat(Files.readString(outsideFile), is("original"));
    }

    @Test
    void shouldNotImportMessagesFileWithAbsolutePath(@TempDir Path tempDir) throws Exception {
        // Given
        Path installDir = Files.createDirectory(tempDir.resolve("install"));
        Path outsideFile = tempDir.resolve("Messages.properties");
        Path languagePack =
                createLanguagePack(tempDir, outsideFile.toString(), "outside installation");

        // When
        String result = importLanguagePack(installDir, languagePack);

        // Then
        assertThat(result, is(equalTo("options.lang.importer.dialog.message.error")));
        assertThat(Files.exists(outsideFile), is(false));
    }

    private static String importLanguagePack(Path installDir, Path languagePack) {
        String originalInstallDir = Constant.getZapInstall();
        Constant.setZapInstall(installDir.toString());
        try {
            return LangImporter.importLanguagePack(languagePack.toString());
        } finally {
            Constant.setZapInstall(originalInstallDir);
        }
    }

    private static Path createLanguagePack(Path directory, String entryName, String contents)
            throws IOException {
        Path languagePack = directory.resolve("language-pack.zaplang");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(languagePack))) {
            zip.putNextEntry(new ZipEntry(entryName));
            zip.write(contents.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return languagePack;
    }
}
