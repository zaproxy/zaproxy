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
package org.zaproxy.zap.control;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;

/** Unit test for {@link AddOnInstaller}. */
class AddOnInstallerUnitTest extends AddOnTestUtils {

    @BeforeEach
    void createZapHome() throws Exception {
        Constant.setZapHome(newTempDir("home").toAbsolutePath().toString());
    }

    @Test
    void shouldReturnAddOnDataDir() throws Exception {
        // Given
        AddOn addOn = new AddOn(createAddOnFile("addOnId.zap"));
        // When
        Path addOnDataDir = AddOnInstaller.getAddOnDataDir(addOn);
        // Then
        assertThat(
                Paths.get(Constant.getZapHome()).relativize(addOnDataDir),
                is(equalTo(Paths.get("addOnData/addOnId/1.0.0"))));
    }

    @Test
    void shouldReturnAddOnLibsDir() throws Exception {
        // Given
        AddOn addOn = new AddOn(createAddOnFile("addOnId.zap"));
        // When
        Path addOnLibsDir = AddOnInstaller.getAddOnLibsDir(addOn);
        // Then
        assertThat(
                Paths.get(Constant.getZapHome()).relativize(addOnLibsDir),
                is(equalTo(Paths.get("addOnData/addOnId/1.0.0/libs"))));
    }

    @Test
    void shouldDeleteLegacyAddOnLibsDir() throws Exception {
        // Given
        AddOn addOnA = new AddOn(createAddOnFile("addOnA.zap"));
        Path addOnALibsDir = Paths.get(Constant.getZapHome(), "addOnData/addOnA/libs");
        createFile(addOnALibsDir.resolve("a_a.jar"));
        createFile(addOnALibsDir.resolve("a_b.jar"));
        AddOn addOnB = new AddOn(createAddOnFile("addOnB.zap"));
        Path addOnBLibsDir = Paths.get(Constant.getZapHome(), "addOnData/addOnB/libs");
        createFile(addOnBLibsDir.resolve("b_a.jar"));
        createFile(addOnBLibsDir.resolve("b_b.jar"));
        List<AddOn> addOns = List.of(addOnA, addOnB);
        // When
        AddOnInstaller.deleteLegacyAddOnLibsDir(addOns);
        // Then
        assertThat(Files.notExists(addOnALibsDir), is(equalTo(true)));
        assertThat(Files.notExists(addOnBLibsDir), is(equalTo(true)));
    }

    @Test
    void shouldNotInstallAddOnLibsIfNone() throws Exception {
        // Given
        AddOn addOn = new AddOn(createAddOnFile("addon.zap"));
        // When
        boolean successfully = AddOnInstaller.installAddOnLibs(addOn);
        // Then
        assertThat(successfully, is(equalTo(true)));
        assertThat(Files.notExists(AddOnInstaller.getAddOnDataDir(addOn)), is(equalTo(true)));
    }

    @Test
    void shouldInstallAddOnLibs() throws Exception {
        // Given
        AddOn addOn = new AddOn(createAddOnWithLibs("lib1", "lib2"));
        // When
        boolean successfully = AddOnInstaller.installAddOnLibs(addOn);
        // Then
        assertThat(successfully, is(equalTo(true)));
        assertInstalledLibs(addOn, "lib1", "lib2");
    }

    @Test
    void shouldInstallAddOnLibsOverwritingExisting() throws Exception {
        // Given
        AddOn addOn = new AddOn(createAddOnWithLibs("lib1", "lib2"));
        Path lib2 = installLib(addOn, "lib2", "FileContents");
        // When
        boolean successfully = AddOnInstaller.installAddOnLibs(addOn);
        // Then
        assertThat(successfully, is(equalTo(true)));
        assertThat(contents(lib2), is(equalTo(DEFAULT_LIB_CONTENTS)));
    }

    @Test
    void shouldInstallMissingAddOnLibs() throws Exception {
        // Given
        AddOn addOn = new AddOn(createAddOnWithLibs("lib1", "lib2"));
        installLib(addOn, "lib2");
        // When
        boolean successfully = AddOnInstaller.installMissingAddOnLibs(addOn);
        // Then
        assertThat(successfully, is(equalTo(true)));
        assertInstalledLibs(addOn, "lib1", "lib2");
    }

    @Test
    void shouldInstallMissingAddOnLibsNotOverwritingExisting() throws Exception {
        // Given
        AddOn addOn = new AddOn(createAddOnWithLibs("lib1", "lib2"));
        Path lib2 = installLib(addOn, "lib2", "FileContents");
        // When
        boolean successfully = AddOnInstaller.installMissingAddOnLibs(addOn);
        // Then
        assertThat(successfully, is(equalTo(true)));
        assertInstalledLibs(addOn, "lib1", "lib2");
        assertThat(contents(lib2), is(equalTo("FileContents")));
    }

    @Test
    void shouldUninstallAddOnLibsAndRemoveDataDirIfEmpty() throws Exception {
        // Given
        AddOn addOn = new AddOn(createAddOnWithLibs("lib1", "lib2"));
        installLib(addOn, "lib1");
        installLib(addOn, "lib2");
        // When
        boolean successfully = AddOnInstaller.uninstallAddOnLibs(addOn);
        // Then
        assertThat(successfully, is(equalTo(true)));
        assertThat(Files.notExists(AddOnInstaller.getAddOnDataDir(addOn)), is(equalTo(true)));
    }

    @Test
    void shouldUninstallAddOnLibsAndKeepDataDirIfNotEmpty() throws Exception {
        // Given
        AddOn addOn = new AddOn(createAddOnWithLibs("lib1", "lib2"));
        Path customFile = createFile(AddOnInstaller.getAddOnDataDir(addOn).resolve("customFile"));
        // When
        boolean successfully = AddOnInstaller.uninstallAddOnLibs(addOn);
        // Then
        assertThat(successfully, is(equalTo(true)));
        assertThat(Files.notExists(addOnDataLibsDir(addOn)), is(equalTo(true)));
        assertThat(Files.exists(customFile), is(equalTo(true)));
    }

    @Test
    void shouldUninstallAllAddOnLibsEvenIfSomeNotDeclared() throws Exception {
        // Given
        AddOn addOn = new AddOn(createAddOnWithLibs("lib1", "lib2"));
        installLib(addOn, "lib1");
        installLib(addOn, "lib2");
        installLib(addOn, "libNotDeclared");
        // When
        boolean successfully = AddOnInstaller.uninstallAddOnLibs(addOn);
        // Then
        assertThat(successfully, is(equalTo(true)));
        assertThat(Files.notExists(AddOnInstaller.getAddOnDataDir(addOn)), is(equalTo(true)));
    }

    @Test
    void shouldNotUninstallAddOnLibsIfNoneDeclared() throws Exception {
        // Given
        AddOn addOn = new AddOn(createAddOnFile("addon.zap"));
        installLib(addOn, "lib1");
        installLib(addOn, "lib2");
        // When
        boolean successfully = AddOnInstaller.uninstallAddOnLibs(addOn);
        // Then
        assertThat(successfully, is(equalTo(true)));
        assertInstalledLibs(addOn, "lib1", "lib2");
    }

    private static void assertInstalledLibs(AddOn addOn, String... fileNames) throws IOException {
        Path addOnLibsDir = addOnDataLibsDir(addOn);

        try (Stream<Path> files = Files.list(addOnLibsDir)) {
            assertThat(
                    files.map(Path::getFileName).map(Path::toString).collect(Collectors.toList()),
                    containsInAnyOrder(fileNames));
        }
    }

    private static String contents(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }
}
