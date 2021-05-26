/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.parosproxy.paros.model;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class FileCopierUnitTest {

    private FileCopier fileCopier;

    @TempDir Path tempFolder;

    @BeforeEach
    void setUp() throws Exception {
        fileCopier = new FileCopier();
    }

    @Test
    void shouldCopyFileViaPreJava7IO() throws Exception {
        // Given
        File source = Files.createTempFile(tempFolder, "", "").toFile();
        FileUtils.writeStringToFile(source, "Test", StandardCharsets.UTF_8);
        File target = newFile();
        // When
        fileCopier.copyLegacy(source, target);
        // Then
        assertTrue(FileUtils.contentEquals(source, target));
    }

    @Test
    void shouldCopyFileViaNIO() throws Exception {
        // Given
        File source = newFile();
        FileUtils.writeStringToFile(source, "Test", StandardCharsets.UTF_8);
        File target = newFile();
        // When
        fileCopier.copyNIO(source, target);
        // Then
        assertTrue(FileUtils.contentEquals(source, target));
    }

    @Test
    void shouldFallbackToPreJava7IOIfNIOFails() throws Exception {
        // Given
        FileCopier fileCopierStub = Mockito.spy(fileCopier);
        doThrow(IOException.class).when(fileCopierStub).copyNIO(any(File.class), any(File.class));
        File source = newFile();
        FileUtils.writeStringToFile(source, "Test", StandardCharsets.UTF_8);
        File target = newFile();
        // When
        fileCopierStub.copy(source, target);
        // Then
        assertTrue(FileUtils.contentEquals(source, target));
    }

    private File newFile() throws IOException {
        return Files.createTempFile(tempFolder, "", "").toFile();
    }
}
