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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

class AddOnTestUtils extends WithConfigsTest {

    static final String DEFAULT_LIB_CONTENTS = "Default Lib Content";
    private static final byte[] DEFAULT_LIB_CONTENTS_BYTES =
            DEFAULT_LIB_CONTENTS.getBytes(StandardCharsets.UTF_8);

    protected Path createEmptyAddOnFile(String fileName) {
        try {
            Path file = newTempDir().resolve(fileName);
            new ZipOutputStream(Files.newOutputStream(file)).close();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Path createAddOnWithLibs(String... libs) {
        return createAddOnWithLibs(null, libs);
    }

    protected Path createAddOnWithLibs(Consumer<StringBuilder> manifestConsumer, String... libs) {
        return createAddOnFile(
                "addon.zap",
                "release",
                "1.0.0",
                manifest -> {
                    if (libs == null || libs.length == 0) {
                        return;
                    }

                    manifest.append("<libs>");
                    for (String lib : libs) {
                        manifest.append("<lib>").append(lib).append("</lib>");
                    }
                    manifest.append("</libs>");

                    if (manifestConsumer != null) {
                        manifestConsumer.accept(manifest);
                    }
                },
                addOnContents -> {
                    if (libs == null || libs.length == 0) {
                        return;
                    }
                    try {
                        for (String lib : libs) {
                            ZipEntry ze = new ZipEntry(lib);
                            addOnContents.putNextEntry(ze);
                            addOnContents.write(
                                    DEFAULT_LIB_CONTENTS_BYTES,
                                    0,
                                    DEFAULT_LIB_CONTENTS_BYTES.length);
                            addOnContents.closeEntry();
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    protected Path createAddOnFile(String fileName) {
        return createAddOnFile(fileName, "release", "1.0.0", (String) null);
    }

    protected Path createAddOnFile(String fileName, String status, String version) {
        return createAddOnFile(fileName, status, version, (String) null);
    }

    protected Path createAddOnFile(
            String fileName, String status, String version, String javaVersion) {
        return createAddOnFile(fileName, status, version, javaVersion, null, null);
    }

    protected Path createAddOnFile(
            String fileName,
            String status,
            String version,
            Consumer<StringBuilder> manifestConsumer) {
        return createAddOnFile(fileName, status, version, null, manifestConsumer, null);
    }

    protected Path createAddOnFile(
            String fileName,
            String status,
            String version,
            Consumer<StringBuilder> manifestConsumer,
            Consumer<ZipOutputStream> addOnConsumer) {
        return createAddOnFile(fileName, status, version, null, manifestConsumer, addOnConsumer);
    }

    protected Path createAddOnFile(
            String fileName,
            String status,
            String version,
            String javaVersion,
            Consumer<StringBuilder> manifestConsumer,
            Consumer<ZipOutputStream> addOnConsumer) {
        try {
            return createAddOnFile(
                    newTempDir(),
                    fileName,
                    status,
                    version,
                    javaVersion,
                    manifestConsumer,
                    addOnConsumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Path createAddOnFile(
            Path dir,
            String fileName,
            String status,
            String version,
            String javaVersion,
            Consumer<StringBuilder> manifestConsumer,
            Consumer<ZipOutputStream> addOnConsumer) {
        try {
            Path file = dir.resolve(fileName);
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(file))) {
                ZipEntry manifest = new ZipEntry(AddOn.MANIFEST_FILE_NAME);
                zos.putNextEntry(manifest);
                StringBuilder strBuilder = new StringBuilder(150);
                strBuilder.append("<zapaddon>");
                strBuilder.append("<version>").append(version).append("</version>");
                strBuilder.append("<status>").append(status).append("</status>");
                if (javaVersion != null && !javaVersion.isEmpty()) {
                    strBuilder.append("<dependencies>");
                    strBuilder.append("<javaversion>").append(javaVersion).append("</javaversion>");
                    strBuilder.append("</dependencies>");
                }
                if (manifestConsumer != null) {
                    manifestConsumer.accept(strBuilder);
                }
                strBuilder.append("</zapaddon>");
                byte[] bytes = strBuilder.toString().getBytes(StandardCharsets.UTF_8);
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
                if (addOnConsumer != null) {
                    addOnConsumer.accept(zos);
                }
            }
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Path addOnDataLibsDir(AddOn addOn) {
        return AddOnInstaller.getAddOnDataDir(addOn).resolve("libs");
    }

    protected static Path installLib(AddOn addOn, String name) throws IOException {
        return installLib(addOn, name, null);
    }

    protected static Path installLib(AddOn addOn, String name, String contents) throws IOException {
        Path addOnLibsDir = addOnDataLibsDir(addOn);
        return createFile(addOnLibsDir.resolve(name), contents);
    }

    protected static Path createFile(Path file) throws IOException {
        return createFile(file, null);
    }

    protected static Path createHomeFile(String name) throws IOException {
        return createFile(Paths.get(Constant.getZapHome(), name), "");
    }

    private static Path createFile(Path file, String contents) throws IOException {
        Files.createDirectories(file.getParent());
        String data = contents != null ? contents : DEFAULT_LIB_CONTENTS;
        Files.write(file, data.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    protected static AddOn createAddOn(String addOnId, ZapXmlConfiguration zapVersions)
            throws Exception {
        return new AddOn(
                addOnId, Paths.get("").toFile(), zapVersions.configurationAt("addon_" + addOnId));
    }

    protected static Path newTempDir() throws IOException {
        return newTempDir("");
    }

    protected static Path newTempDir(String prefix) throws IOException {
        return Files.createTempDirectory(tempDir, prefix);
    }
}
