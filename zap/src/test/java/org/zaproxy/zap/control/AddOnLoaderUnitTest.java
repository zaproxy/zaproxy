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
package org.zaproxy.zap.control;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.control.AddOn.InstallationStatus;

/** Unit test for {@link AddOnLoader}. */
class AddOnLoaderUnitTest extends AddOnTestUtils {

    @BeforeEach
    void createZapHome() throws Exception {
        Constant.setZapHome(newTempDir("home").toAbsolutePath().toString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldCreateAddOnLoaderWithoutDirectories(File[] dirs) throws Exception {
        // Given dirs
        // When / Then
        AddOnLoader addOnLoader = assertDoesNotThrow(() -> new AddOnLoader(dirs));
        assertThat(addOnLoader.getIdsAddOnsWithRunningIssuesSinceLastRun(), is(empty()));
        assertThat(addOnLoader.getAddOnCollection().getAddOns(), is(empty()));
    }

    @Test
    void shouldCreateAddOnLoaderFromEmptyDirectory() throws Exception {
        // Given
        File[] emptyDirectory = {newTempDir().toFile()};
        // When
        AddOnLoader addOnLoader = new AddOnLoader(emptyDirectory);
        // Then
        assertThat(addOnLoader.getIdsAddOnsWithRunningIssuesSinceLastRun(), is(empty()));
        assertThat(addOnLoader.getAddOnCollection().getAddOns(), is(empty()));
    }

    @Test
    void shouldCreateAddOnLoaderFromDirectoryWithAddOnsWithNoIssues() throws Exception {
        // Given
        Path dir = newTempDir();
        createAddOnFile(dir, "addon1.zap");
        createAddOnFile(dir, "addon2.zap");
        File[] dirWithAddOns = {dir.toFile()};
        // When
        AddOnLoader addOnLoader = new AddOnLoader(dirWithAddOns);
        // Then
        assertThat(addOnLoader.getIdsAddOnsWithRunningIssuesSinceLastRun(), is(empty()));
        assertThat(addOnLoader.getAddOnCollection().getAddOns(), hasSize(2));
        assertThat(
                addOnLoader.getAddOnCollection().getAddOn("addon1").getInstallationStatus(),
                is(equalTo(InstallationStatus.INSTALLED)));
        assertThat(
                addOnLoader.getAddOnCollection().getAddOn("addon2").getInstallationStatus(),
                is(equalTo(InstallationStatus.INSTALLED)));
    }

    @Test
    void shouldCreateAddOnLoaderFromDirectoryWithAddOnsWithIssues() throws Exception {
        // Given
        Path dir = newTempDir();
        createAddOnFile(dir, "addon1.zap", this::manifestWithAddOnMissingDependency);
        createAddOnFile(dir, "addon2.zap");
        File[] dirWithAddOns = {dir.toFile()};
        // When
        AddOnLoader addOnLoader = new AddOnLoader(dirWithAddOns);
        // Then
        assertThat(addOnLoader.getIdsAddOnsWithRunningIssuesSinceLastRun(), is(empty()));
        assertThat(addOnLoader.getAddOnCollection().getAddOns(), hasSize(2));
        assertThat(
                addOnLoader.getAddOnCollection().getAddOn("addon1").getInstallationStatus(),
                is(equalTo(InstallationStatus.NOT_INSTALLED)));
        assertThat(
                addOnLoader.getAddOnCollection().getAddOn("addon2").getInstallationStatus(),
                is(equalTo(InstallationStatus.INSTALLED)));
    }

    @Test
    void shouldReportAddOnsWithRunningIssuesSinceLastRun() throws Exception {
        // Given
        Path dir = newTempDir();
        createAddOnFile(dir, "addon1.zap");
        createAddOnFile(dir, "addon2.zap");
        File[] dirWithAddOns = {dir.toFile()};
        // When
        // 1st run
        new AddOnLoader(dirWithAddOns);
        // Now with issues
        createAddOnFile(dir, "addon1.zap", this::manifestWithAddOnMissingDependency);
        // 2nd run
        AddOnLoader addOnLoader = new AddOnLoader(dirWithAddOns);
        // Then
        assertThat(addOnLoader.getIdsAddOnsWithRunningIssuesSinceLastRun(), contains("addon1"));
        assertThat(addOnLoader.getAddOnCollection().getAddOns(), hasSize(2));
        assertThat(
                addOnLoader.getAddOnCollection().getAddOn("addon1").getInstallationStatus(),
                is(equalTo(InstallationStatus.NOT_INSTALLED)));
        assertThat(
                addOnLoader.getAddOnCollection().getAddOn("addon2").getInstallationStatus(),
                is(equalTo(InstallationStatus.INSTALLED)));
    }

    @Test
    void shouldReportAddOnsWithExtensionsWithRunningIssuesSinceLastRun() throws Exception {
        // Given
        Path dir = newTempDir();
        createAddOnFile(dir, "addon1.zap", manifest -> manifestWithExtensions(manifest, "addon2"));
        createAddOnFile(dir, "addon2.zap");
        File[] dirWithAddOns = {dir.toFile()};
        // When
        // 1st run
        new AddOnLoader(dirWithAddOns);
        // Now with extension issues
        createAddOnFile(dir, "addon1.zap", manifest -> manifestWithExtensions(manifest, "addon3"));
        // 2nd run
        AddOnLoader addOnLoader = new AddOnLoader(dirWithAddOns);
        // Then
        assertThat(addOnLoader.getIdsAddOnsWithRunningIssuesSinceLastRun(), contains("addon1"));
        assertThat(addOnLoader.getAddOnCollection().getAddOns(), hasSize(2));
        assertThat(
                addOnLoader.getAddOnCollection().getAddOn("addon1").getInstallationStatus(),
                is(equalTo(InstallationStatus.INSTALLED)));
        assertThat(
                addOnLoader.getAddOnCollection().getAddOn("addon2").getInstallationStatus(),
                is(equalTo(InstallationStatus.INSTALLED)));
    }

    private void manifestWithAddOnMissingDependency(StringBuilder manifest) {
        manifest.append("<dependencies>")
                .append("<addons>")
                .append("<addon>")
                .append("<id>missingAddOn</id>")
                .append("</addon>")
                .append("</addons>")
                .append("</dependencies>");
    }

    private void manifestWithExtensions(StringBuilder manifest, String addOnIdExtDep) {
        manifest.append("<extensions>")
                .append("<extension>extension.no.issues</extension>")
                .append("<extension v=\"1\">")
                .append("<classname>")
                .append("extension.for.issues")
                .append("</classname>")
                .append("<dependencies>")
                .append("<addons>")
                .append("<addon>")
                .append("<id>")
                .append(addOnIdExtDep)
                .append("</id>")
                .append("</addon>")
                .append("</addons>")
                .append("</dependencies>")
                .append("</extension>")
                .append("</extensions>");
    }

    private Path createAddOnFile(Path dir, String fileName) {
        return createAddOnFile(dir, fileName, manifest -> {});
    }

    private Path createAddOnFile(
            Path dir, String fileName, Consumer<StringBuilder> manifestConsumer) {
        return createAddOnFile(
                dir,
                fileName,
                "release",
                "1.0.0",
                null,
                manifest -> {
                    manifest.append("<not-before-version>")
                            .append(Constant.PROGRAM_VERSION)
                            .append("</not-before-version>");
                    manifestConsumer.accept(manifest);
                },
                null);
    }
}
