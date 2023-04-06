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
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.zaproxy.zap.control.AddOn.InstallationStatus;

/** Unit test for {@link AddOnLoader}. */
class AddOnLoaderUnitTest extends AddOnTestUtils {

    @BeforeEach
    void createZapHome() throws Exception {
        Constant.setZapHome(newTempDir("home").toAbsolutePath().toString());
        Constant.setZapInstall(newTempDir("install").toAbsolutePath().toString());
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
    void shouldLoadExtensionsFromAddOn() throws Exception {
        // Given
        Path dir = newTempDir();
        String extensionA = "org.zaproxy.a.ExtensionA";
        String extensionB = "org.zaproxy.a.ExtensionB";
        createAddOnWithExtensions(dir, "addOnA", List.of(extensionA, extensionB));
        AddOnLoader addOnLoader = new AddOnLoader(new File[] {dir.toFile()});
        // When
        List<Extension> extensions = sorted(addOnLoader.getExtensions());
        // Then
        assertThat(extensions, hasSize(2));
        assertExtensionCanonicalName(extensions.get(0), extensionA);
        assertExtensionCanonicalName(extensions.get(1), extensionB);
    }

    private static void assertExtensionCanonicalName(Extension extension, String extensionA) {
        assertThat(extension.getClass().getCanonicalName(), is(equalTo(extensionA)));
    }

    @Test
    void shouldLoadExtensionsFromAddOns() throws Exception {
        // Given
        Path dir = newTempDir();
        String extensionA = "org.zaproxy.a.ExtensionA";
        createAddOnWithExtensions(dir, "addOnA", List.of(extensionA));
        String extensionB = "org.zaproxy.b.ExtensionB";
        createAddOnWithExtensions(dir, "addOnB", List.of(extensionB));
        AddOnLoader addOnLoader = new AddOnLoader(new File[] {dir.toFile()});
        // When
        List<Extension> extensions = sorted(addOnLoader.getExtensions());
        // Then
        assertThat(extensions, hasSize(2));
        assertExtensionCanonicalName(extensions.get(0), extensionA);
        assertExtensionCanonicalName(extensions.get(1), extensionB);
    }

    @Test
    void shouldLoadOptionalExtensionsFromAddOns() throws Exception {
        // Given
        Path dir = newTempDir();
        String addOnA = "addOnA";
        String extensionA = "org.zaproxy.a.ExtensionA";
        createAddOnWithExtensions(dir, addOnA, List.of(extensionA));
        String extensionB = "org.zaproxy.b.ExtensionB";
        String extensionOptional = "org.zaproxy.b.optional.ExtensionOptional";
        createAddOnWithExtensions(
                dir,
                "addOnB",
                List.of(extensionB, extensionOptional),
                (manifest, classname) -> {
                    if (extensionOptional.equals(classname)) {
                        optionalExtension(manifest, classname, addOnA);
                        return true;
                    }
                    return false;
                });
        AddOnLoader addOnLoader = new AddOnLoader(new File[] {dir.toFile()});
        // When
        List<Extension> extensions = sorted(addOnLoader.getExtensions());
        // Then
        assertThat(extensions, hasSize(3));
        assertExtensionCanonicalName(extensions.get(2), extensionOptional);
    }

    @Test
    void shouldRemoveOptionalExtensionsWhenDependencyAddOnUninstalled() throws Exception {
        // Given
        Path dir = newTempDir();
        String addOnIdA = "addOnA";
        String extensionA = "org.zaproxy.a.ExtensionA";
        createAddOnWithExtensions(dir, addOnIdA, List.of(extensionA));
        String addOnIdB = "addOnB";
        String extensionB = "org.zaproxy.b.ExtensionB";
        String extensionOptionalA = "org.zaproxy.b.optionala.ExtensionOptionalA";
        String extensionOptionalB = "org.zaproxy.b.optionalb.ExtensionOptionalB";
        createAddOnWithExtensions(
                dir,
                addOnIdB,
                List.of(extensionB, extensionOptionalA, extensionOptionalB),
                (manifest, classname) -> {
                    if (extensionOptionalA.equals(classname)
                            || extensionOptionalB.equals(classname)) {
                        optionalExtension(manifest, classname, addOnIdA);
                        return true;
                    }
                    return false;
                });
        AddOnLoader addOnLoader = new AddOnLoader(new File[] {dir.toFile()});
        addOnLoader.getExtensions();
        AddOn addOnA = getAddOn(addOnLoader, addOnIdA);
        AddOn addOnB = getAddOn(addOnLoader, addOnIdB);
        List<Extension> addOnBExtensions = sorted(addOnB.getLoadedExtensions());
        // When
        addOnLoader.removeAddOn(addOnA, false, AddOnLoader.NULL_CALLBACK);
        // Then
        assertExtensionRemoved(addOnBExtensions.get(1));
        assertExtensionRemoved(addOnBExtensions.get(2));
        List<Extension> extensions = addOnLoader.getExtensions();
        assertThat(extensions, hasSize(1));
        assertExtensionCanonicalName(extensions.get(0), extensionB);
    }

    @Test
    void shouldRemoveOptionalExtensionsWhenTransitiveDependencyAddOnUninstalled() throws Exception {
        // Given
        Path dir = newTempDir();
        String addOnIdA = "addOnA";
        String extensionA = "org.zaproxy.a.ExtensionA";
        createAddOnWithExtensions(dir, addOnIdA, List.of(extensionA));
        String addOnIdB = "addOnB";
        String extensionB = "org.zaproxy.b.ExtensionB";
        createAddOnWithExtensions(
                dir,
                addOnIdB,
                List.of(extensionB),
                (manifest, classname) -> {
                    if ("".equals(classname)) {
                        appendAddOnDependencies(manifest, addOnIdA);
                    }
                    return false;
                });
        String addOnIdC = "addOnC";
        String extensionC = "org.zaproxy.c.ExtensionC";
        String extensionCOptional = "org.zaproxy.c.optionala.ExtensionCOptional";
        createAddOnWithExtensions(
                dir,
                addOnIdC,
                List.of(extensionC, extensionCOptional),
                (manifest, classname) -> {
                    if (extensionCOptional.equals(classname)) {
                        optionalExtension(manifest, classname, addOnIdB);
                        return true;
                    }
                    return false;
                });
        AddOnLoader addOnLoader = new AddOnLoader(new File[] {dir.toFile()});
        addOnLoader.getExtensions();
        AddOn addOnA = getAddOn(addOnLoader, addOnIdA);
        AddOn addOnC = getAddOn(addOnLoader, addOnIdC);
        List<Extension> addOnCExtensions = sorted(addOnC.getLoadedExtensions());
        // When
        addOnLoader.removeAddOn(addOnA, false, AddOnLoader.NULL_CALLBACK);
        // Then
        assertExtensionRemoved(addOnCExtensions.get(1));
        List<Extension> extensions = addOnLoader.getExtensions();
        assertThat(extensions, hasSize(1));
        assertExtensionCanonicalName(extensions.get(0), extensionC);
    }

    @Test
    void shouldCreateAddOnLoaderFromDirectoryWithAddOnsWithIssues() throws Exception {
        // Given
        Path dir = newTempDir();
        createAddOnFile(
                dir, "addon1.zap", manifest -> appendAddOnDependencies(manifest, "missingId"));
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
        createAddOnFile(
                dir, "addon1.zap", manifest -> appendAddOnDependencies(manifest, "missingId"));
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
        createAddOnFile(
                dir, "addon1.zap", manifest -> appendExtensionWithIssues(manifest, "addon2"));
        createAddOnFile(dir, "addon2.zap");
        File[] dirWithAddOns = {dir.toFile()};
        // When
        // 1st run
        new AddOnLoader(dirWithAddOns);
        // Now with extension issues
        createAddOnFile(
                dir, "addon1.zap", manifest -> appendExtensionWithIssues(manifest, "addon3"));
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

    @Test
    void shouldRemoveAddOnWhenNotInInstallationPluginDir() throws Exception {
        // Given
        Path dir = newTempDir();
        String addOnId = "id";
        createAddOnFile(dir, addOnId + ".zap");
        AddOnLoader addOnLoader = new AddOnLoader(new File[] {dir.toFile()});
        AddOn addOn = getAddOn(addOnLoader, addOnId);
        // When
        addOnLoader.removeAddOn(addOn, false, AddOnLoader.NULL_CALLBACK);
        // Then
        assertThat(Files.exists(addOn.getFile().toPath()), is(equalTo(false)));
        assertThat(addOnLoader.getBlockList(), not(contains("id")));
    }

    @Test
    void shouldKeepAndBlockAddOnInInstallationPluginDir() throws Exception {
        // Given
        Path dir = Paths.get(Constant.getZapInstall(), Constant.FOLDER_PLUGIN);
        String addOnId = "id";
        createAddOnFile(dir, addOnId + ".zap");
        AddOnLoader addOnLoader = new AddOnLoader(new File[] {dir.toFile()});
        AddOn addOn = getAddOn(addOnLoader, addOnId);
        // When
        addOnLoader.removeAddOn(addOn, false, AddOnLoader.NULL_CALLBACK);
        // Then
        assertThat(Files.exists(addOn.getFile().toPath()), is(equalTo(true)));
        assertThat(addOnLoader.getBlockList(), contains("id"));
    }

    @Test
    void shouldKeepButNotBlockMandatoryAddOnInInstallationPluginDir() throws Exception {
        // Given
        Path dir = Paths.get(Constant.getZapInstall(), Constant.FOLDER_PLUGIN);
        String addOnId = "id";
        createAddOnFile(dir, addOnId + ".zap");
        AddOnLoader addOnLoader = new AddOnLoader(new File[] {dir.toFile()});
        AddOn addOn = getAddOn(addOnLoader, addOnId);
        addOn.setMandatory(true);
        // When
        addOnLoader.removeAddOn(addOn, false, AddOnLoader.NULL_CALLBACK);
        // Then
        assertThat(Files.exists(addOn.getFile().toPath()), is(equalTo(true)));
        assertThat(addOnLoader.getBlockList(), not(contains("id")));
    }

    private void assertExtensionRemoved(Extension extension) {
        verify(extensionLoader).removeExtension(extension);
    }

    private static void appendAddOnDependencies(StringBuilder manifest, String addOnId) {
        manifest.append("<dependencies>")
                .append("<addons>")
                .append("<addon>")
                .append("<id>")
                .append(addOnId)
                .append("</id>")
                .append("</addon>")
                .append("</addons>")
                .append("</dependencies>");
    }

    private static void appendExtensionWithIssues(StringBuilder manifest, String addOnId) {
        manifest.append("<extensions>").append("<extension>extension.no.issues</extension>");
        optionalExtension(manifest, "extension.for.issues", addOnId);
        manifest.append("</extensions>");
    }

    private static void optionalExtension(
            StringBuilder manifest, String extension, String addOnId) {
        manifest.append("<extension v=\"1\">")
                .append("<classname>")
                .append(extension)
                .append("</classname>");
        appendAddOnDependencies(manifest, addOnId);
        manifest.append("</extension>");
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

    private Path createAddOnWithExtensions(Path dir, String addOnId, List<String> extensionNames)
            throws IOException {
        return createAddOnWithExtensions(dir, addOnId, extensionNames, null);
    }

    private Path createAddOnWithExtensions(
            Path dir,
            String addOnId,
            List<String> extensionNames,
            BiPredicate<StringBuilder, String> manifestConsumer)
            throws IOException {
        return createAddOnWithExtensions(dir, addOnId, extensionNames, manifestConsumer, null);
    }

    private Path createAddOnWithExtensions(
            Path dir,
            String addOnId,
            List<String> extensionNames,
            BiPredicate<StringBuilder, String> manifestConsumer,
            BiFunction<DynamicType.Builder<?>, String, DynamicType.Builder<?>> extensionImpl)
            throws IOException {
        Path addOn =
                createAddOnFile(
                        dir,
                        addOnId + ".zap",
                        manifest -> {
                            manifest.append("<extensions>");
                            for (String extensionName : extensionNames) {
                                boolean consumed = false;
                                if (manifestConsumer != null) {
                                    consumed = manifestConsumer.test(manifest, extensionName);
                                }

                                if (!consumed) {
                                    manifest.append("<extension>")
                                            .append(extensionName)
                                            .append("</extension>");
                                }
                            }
                            manifest.append("</extensions>");

                            if (manifestConsumer != null) {
                                manifestConsumer.test(manifest, "");
                            }
                        });
        for (String extensionName : extensionNames) {
            DynamicType.Builder<?> builder =
                    new ByteBuddy().subclass(ExtensionAdaptor.class).name(extensionName);
            if (extensionImpl != null) {
                builder = extensionImpl.apply(builder, extensionName);
            }
            builder.method(ElementMatchers.named("canUnload"))
                    .intercept(FixedValue.value(true))
                    .make()
                    .inject(addOn.toFile());
        }
        return addOn;
    }

    private static List<Extension> sorted(List<Extension> list) {
        List<Extension> extensions = new ArrayList<>(list);
        Collections.sort(
                extensions,
                (a, b) ->
                        a.getClass().getCanonicalName().compareTo(b.getClass().getCanonicalName()));
        return extensions;
    }

    private static AddOn getAddOn(AddOnLoader addOnLoader, String id) {
        return addOnLoader.getAddOnCollection().getAddOn(id);
    }
}
