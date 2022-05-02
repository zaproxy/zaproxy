/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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
package org.parosproxy.paros;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.core.config.Configurator;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link Constant}. */
class ConstantUnitTest {

    @TempDir static Path tempDir;
    private Path zapInstallDir;
    private Path zapHomeDir;

    @BeforeEach
    void before() throws Exception {
        Path parentDir = Files.createTempDirectory(tempDir, "zap-");
        zapInstallDir = Files.createDirectories(parentDir.resolve("install"));
        zapHomeDir = Files.createDirectories(parentDir.resolve("home"));
    }

    @AfterEach
    void after() throws Exception {
        Configurator.reconfigure(
                ConstantUnitTest.class.getResource("/log4j2-test.properties").toURI());
    }

    @Test
    void shouldInitialiseHomeDirFromInstallDir() throws IOException {
        // Given
        String configContents =
                String.format(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>%n"
                                + "<config>%n"
                                + "    <version>%s</version>%n"
                                + "</config>%n",
                        Constant.VERSION_TAG);
        installationFile("xml/config.xml", configContents);
        String log4jContents = "# Custom Config";
        installationFile("xml/log4j2.properties", log4jContents);
        Constant.setZapInstall(zapInstallDir.toString());
        Constant.setZapHome(zapHomeDir.toString());
        // When
        new Constant();
        // Then
        assertHomeFile("config.xml", configContents);
        assertHomeFile("log4j2.properties", log4jContents);
        assertHomeDirs();
        assertThat(Files.walk(zapHomeDir).count(), is(equalTo(7L)));
    }

    @Test
    void shouldInitialiseHomeDirFromBundledFiles() throws IOException {
        // Given
        Constant.setZapInstall(zapInstallDir.toString());
        Constant.setZapHome(zapHomeDir.toString());
        // When
        new Constant();
        // Then
        assertHomeFile("config.xml", defaultConfigContents());
        assertHomeFile("log4j2.properties", defaultContents("log4j2-home.properties"));
        assertHomeFile("zap.log", is(not(emptyString())));
        assertHomeDirs();
        assertThat(Files.walk(zapHomeDir).count(), is(equalTo(8L)));
    }

    @Test
    void shouldRestoreDefaultConfigFileIfOneInHomeIsMalformed() throws IOException {
        // Given
        String malformedConfig = "not a valid config";
        homeFile("config.xml", malformedConfig);
        Constant.setZapInstall(zapInstallDir.toString());
        Constant.setZapHome(zapHomeDir.toString());
        // When
        new Constant();
        // Then
        assertHomeFile("config.xml", defaultConfigContents());
        assertHomeFile(getNameBackupMalformedConfig(), malformedConfig);
    }

    @Test
    void shouldUseExistingLog4jConfiguration() throws IOException {
        // Given
        String log4jContents = "# Nothing, should not create default log file...";
        homeFile("log4j2.properties", log4jContents);
        Constant.setZapInstall(zapInstallDir.toString());
        Constant.setZapHome(zapHomeDir.toString());
        // When
        new Constant();
        // Then
        assertHomeFile("log4j2.properties", log4jContents);
        assertHomeFileNotExists("zap.log");
    }

    @Test
    void shouldBackupLegacyLog4jConfiguration() throws IOException {
        // Given
        Constant.setZapInstall(zapInstallDir.toString());
        Constant.setZapHome(zapHomeDir.toString());
        String log4jContents = "log4j.rootLogger...";
        homeFile("log4j.properties", log4jContents);
        // When
        new Constant();
        // Then
        assertHomeFileNotExists("log4j.properties");
        assertHomeFile("log4j.properties.bak", log4jContents);
    }

    @Test
    void shouldNotBackupLegacyLog4jConfigurationIfBackupExists() throws IOException {
        // Given
        Constant.setZapInstall(zapInstallDir.toString());
        Constant.setZapHome(zapHomeDir.toString());
        String log4jContents = "log4j.rootLogger...";
        homeFile("log4j.properties", log4jContents);
        String log4jContentsBackup = "backup";
        homeFile("log4j.properties.bak", log4jContentsBackup);
        // When
        new Constant();
        // Then
        assertHomeFile("log4j.properties", log4jContents);
        assertHomeFile("log4j.properties.bak", log4jContentsBackup);
    }

    private void assertHomeFile(String name, String contents) throws IOException {
        assertHomeFile(name, is(equalTo(contents)));
    }

    private void assertHomeFile(String name, Matcher<String> contentsMatcher) throws IOException {
        Path file = zapHomeDir.resolve(name);
        assertThat(Files.exists(file), is(true));
        assertThat(contents(file), contentsMatcher);
    }

    private void assertHomeFileNotExists(String name) throws IOException {
        assertThat(Files.exists(zapHomeDir.resolve(name)), is(false));
    }

    private void assertHomeDirs() {
        assertThat(Files.isDirectory(zapHomeDir.resolve("dirbuster")), is(true));
        assertThat(Files.isDirectory(zapHomeDir.resolve("fuzzers")), is(true));
        assertThat(Files.isDirectory(zapHomeDir.resolve("plugin")), is(true));
        assertThat(Files.isDirectory(zapHomeDir.resolve("session")), is(true));
    }

    private static String defaultConfigContents() throws IOException {
        try (InputStream is =
                Constant.class.getResourceAsStream("/org/zaproxy/zap/resources/config.xml")) {
            ZapXmlConfiguration configuration = new ZapXmlConfiguration(is);
            configuration.setProperty("version", Constant.VERSION_TAG);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            configuration.save(os);
            return os.toString(StandardCharsets.UTF_8.name());
        } catch (ConfigurationException e) {
            throw new IOException(e);
        }
    }

    private static String defaultContents(String name) throws IOException {
        try (InputStream is =
                Constant.class.getResourceAsStream("/org/zaproxy/zap/resources/" + name)) {
            if (is == null) {
                throw new IOException("File not found: " + name);
            }
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

    private void installationFile(String name, String contents) throws IOException {
        createFile(zapInstallDir.resolve(name), contents);
    }

    private static void createFile(Path file, String contents) throws IOException {
        Files.createDirectories(file.getParent());
        Files.write(file, contents.getBytes(StandardCharsets.UTF_8));
    }

    private void homeFile(String name, String contents) throws IOException {
        createFile(zapHomeDir.resolve(name), contents);
    }

    private static String contents(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private String getNameBackupMalformedConfig() throws IOException {
        try (Stream<Path> files = Files.list(zapHomeDir)) {
            return files.filter(
                            f -> {
                                String name = f.getFileName().toString();
                                return name.startsWith("config-") && name.endsWith(".xml.bak");
                            })
                    .findFirst()
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .orElse(null);
        }
    }

    @Test
    void shouldUpgradeFrom2_9_0() {
        // Given
        List<String> keyPrefixes = Arrays.asList("a.", "a.b.", "c.");
        ZapXmlConfiguration configuration = new ZapXmlConfiguration();
        for (String keyPrefix : keyPrefixes) {
            configuration.setProperty(keyPrefix + "markocurrences", "true");
        }
        String unrelatedKey = "a.markocurrences.y";
        configuration.setProperty(unrelatedKey, "abc");
        // When
        Constant.upgradeFrom2_9_0(configuration);
        // Then
        for (String keyPrefix : keyPrefixes) {
            assertThat(
                    keyPrefix,
                    configuration.containsKey(keyPrefix + "markocurrences"),
                    is(equalTo(false)));
            assertThat(
                    keyPrefix,
                    configuration.getProperty(keyPrefix + "markoccurrences"),
                    is(equalTo("true")));
        }
        assertThat(configuration.getProperty(unrelatedKey), is(equalTo("abc")));
    }
}
