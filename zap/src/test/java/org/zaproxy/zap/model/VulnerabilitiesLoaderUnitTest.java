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
package org.zaproxy.zap.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.testutils.TestUtils;

/** Unit test for {@link VulnerabilitiesLoader}. */
class VulnerabilitiesLoaderUnitTest extends TestUtils {

    private static final Path DIRECTORY =
            getResourcePath("/vulnerabilities/", VulnerabilitiesLoaderUnitTest.class);
    private static final Path DIRECTORY_INVALID =
            getResourcePath("/vulnerabilities/invalid", VulnerabilitiesLoaderUnitTest.class);
    private static final String FILE_NAME = "vulnerabilities-test";
    private static final String FILE_EXTENSION = ".xml";

    VulnerabilitiesLoader loader;

    @Test
    void shouldThrownExceptionIfDirectoryIsNull() {
        // Given
        Path directory = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new VulnerabilitiesLoader(directory, FILE_NAME, FILE_EXTENSION));
    }

    @Test
    void shouldThrownExceptionIfFileNameIsNull() {
        // Given
        String fileName = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new VulnerabilitiesLoader(DIRECTORY, fileName, FILE_EXTENSION));
    }

    @Test
    void shouldThrownExceptionIfFileNameIsEmpty() {
        // Given
        String fileName = "";
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new VulnerabilitiesLoader(DIRECTORY, fileName, FILE_EXTENSION));
    }

    @Test
    void shouldThrownExceptionIfFileExtensionIsNull() {
        // Given
        String fileExtension = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new VulnerabilitiesLoader(DIRECTORY, FILE_NAME, fileExtension));
    }

    @Test
    void shouldThrownExceptionIfFileExtensionIsEmpty() {
        // Given
        String fileExtension = "";
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new VulnerabilitiesLoader(DIRECTORY, FILE_NAME, fileExtension));
    }

    @Test
    void shouldReturnEmptyListIfVulnerabilitiesFileNotFound() {
        // Given
        loader = new VulnerabilitiesLoader(DIRECTORY, "FileNotFound", ".NoExtension");
        // When
        List<Vulnerability> vulnerabilities = loader.load(Locale.ROOT);
        // Then
        assertThat(vulnerabilities, is(empty()));
    }

    @Test
    void shouldReturnListWithVulnerabilitiesForDefaultLocale() {
        // Given
        loader = new VulnerabilitiesLoader(DIRECTORY, FILE_NAME, FILE_EXTENSION);
        // When
        List<Vulnerability> vulnerabilities = loader.load(Locale.ROOT);
        // Then
        assertThat(vulnerabilities.size(), is(equalTo(2)));

        Vulnerability wasc1 = vulnerabilities.get(0);
        assertThat(wasc1.getId(), is(equalTo("wasc_1")));
        assertThat(wasc1.getAlert(), is(equalTo("Locale default")));
        assertThat(wasc1.getDescription(), is(equalTo("Description default")));
        assertThat(wasc1.getSolution(), is(equalTo("Solution default")));
        assertThat(wasc1.getReferences().size(), is(equalTo(2)));
        assertThat(wasc1.getReferences().get(0), is(equalTo("Reference default 1")));
        assertThat(wasc1.getReferences().get(1), is(equalTo("Reference default 2")));

        Vulnerability wasc2 = vulnerabilities.get(1);
        assertThat(wasc2.getId(), is(equalTo("wasc_2")));
        assertThat(wasc2.getAlert(), is(equalTo("Alert 2")));
        assertThat(wasc2.getDescription(), is(equalTo("Description 2")));
        assertThat(wasc2.getSolution(), is(equalTo("Solution 2")));
        assertThat(wasc2.getReferences().size(), is(equalTo(1)));
        assertThat(wasc2.getReferences().get(0), is(equalTo("Reference 2")));
    }

    @Test
    void shouldLoadFileWithSameLanguageCountryWhenAvailable() {
        // Given
        Locale locale = new Locale.Builder().setLanguage("nl").setRegion("NL").build();
        loader = new VulnerabilitiesLoader(DIRECTORY, FILE_NAME, FILE_EXTENSION);
        // When
        List<Vulnerability> vulnerabilities = loader.load(locale);
        // Then
        assertThat(vulnerabilities, is(not(empty())));
        assertThat(vulnerabilities.get(0).getAlert(), is(equalTo("Locale nl_NL")));
    }

    @Test
    void shouldLoadDefaultFileEvenIfFileWithSameLanguageButDifferentCountryIsAvailable() {
        // Given
        Locale.setDefault(new Locale("nl", "XX"));
        Locale locale = new Locale.Builder().setLanguage("nl").setRegion("XX").build();
        loader = new VulnerabilitiesLoader(DIRECTORY, FILE_NAME, FILE_EXTENSION);
        // When
        List<Vulnerability> vulnerabilities = loader.load(locale);
        // Then
        assertThat(vulnerabilities, is(not(empty())));
        assertThat(vulnerabilities.get(0).getAlert(), is(equalTo("Locale default")));
    }

    @Test
    void shouldLoadFileWithOnlyLanguageMatchWhenLanguageCountryNotAvailable() {
        // Given
        Locale locale = new Locale.Builder().setLanguage("es").setRegion("AR").build();
        loader = new VulnerabilitiesLoader(DIRECTORY, FILE_NAME, FILE_EXTENSION);
        // When
        List<Vulnerability> vulnerabilities = loader.load(locale);
        // Then
        assertThat(vulnerabilities, is(not(empty())));
        assertThat(vulnerabilities.get(0).getAlert(), is(equalTo("Locale es")));
    }

    @Test
    void shouldReturnEmptyListIfFoundFileIsEmpty() {
        // Given
        loader = new VulnerabilitiesLoader(DIRECTORY_INVALID, FILE_NAME + "-empty", FILE_EXTENSION);
        // When
        List<Vulnerability> vulnerabilities = loader.load(Locale.ROOT);
        // Then
        assertThat(vulnerabilities, is(empty()));
    }

    @Test
    void shouldReturnEmptyListIfFoundFileIsNotValidXml() {
        // Given
        loader =
                new VulnerabilitiesLoader(
                        DIRECTORY_INVALID, FILE_NAME + "-invalid-xml", FILE_EXTENSION);
        // When
        List<Vulnerability> vulnerabilities = loader.load(Locale.ROOT);
        // Then
        assertThat(vulnerabilities, is(empty()));
    }
}
