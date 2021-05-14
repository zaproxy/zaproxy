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
package org.zaproxy.zap.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Unit test for {@link LocaleUtils}. */
class LocaleUtilsUnitTest {

    private static final ResourceBundle.Control HELPER_CONTROL =
            ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);

    private static final Locale LOCALE_SPAIN = new Locale("es", "ES");

    private static final String FILE_NAME = "FileName";
    private static final String FILE_EXTENSION = ".extension";

    @Test
    void shouldThrowExceptionWhenGettingResourceFilesRegexWithNullFileName() {
        // Given
        String nullFileName = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> LocaleUtils.createResourceFilesRegex(nullFileName, FILE_EXTENSION));
    }

    @Test
    void shouldThrowExceptionWhenGettingResourceFilesRegexWithNullFileExtension() {
        // Given
        String nullFileExtension = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> LocaleUtils.createResourceFilesRegex(FILE_NAME, nullFileExtension));
    }

    @Test
    void shouldReturnValidRegexWhenGettingResourceFilesRegexWithNonNullFileNameAndFileExtension() {
        // Given
        String regex = LocaleUtils.createResourceFilesRegex(FILE_NAME, FILE_EXTENSION);
        // When / Then = valid regex
        assertDoesNotThrow(() -> Pattern.compile(regex));
    }

    @Test
    void shouldAcceptFileNameWithSpecialRegexCharsWhenGettingResourceFilesRegex() {
        // Given
        String fileNameWithSpecialRegexChars = "?]|*-)(^[:.";
        // When / Then = valid regex
        assertDoesNotThrow(
                () ->
                        Pattern.compile(
                                LocaleUtils.createResourceFilesRegex(
                                        fileNameWithSpecialRegexChars, FILE_EXTENSION)));
    }

    @Test
    void shouldAcceptFileExtensionWithSpecialRegexCharsWhenGettingResourceFilesRegex() {
        // Given
        String fileExtensionWithSpecialRegexChars = "?]|*-)(^[:.";
        // When / Then = valid regex
        assertDoesNotThrow(
                () ->
                        Pattern.compile(
                                LocaleUtils.createResourceFilesRegex(
                                        FILE_NAME, fileExtensionWithSpecialRegexChars)));
    }

    @Test
    void shouldProduceSameRegexForCreateResourceFilesRegexAsCreateResourceFilesPattern() {
        // Given
        String regex = LocaleUtils.createResourceFilesRegex(FILE_NAME, FILE_EXTENSION);
        Pattern pattern = LocaleUtils.createResourceFilesPattern(FILE_NAME, FILE_EXTENSION);
        // When
        String patternRegex = pattern.toString();
        // Then
        assertThat(regex, is(equalTo(patternRegex)));
    }

    @Test
    void shouldMatchValidResourceFilesWithCreatedResourceFilesPattern() {
        // Given
        String[] resourceFiles = {
            "FileName.extension",
            "FileName_en.extension",
            "FileName_en_GB.extension",
            "FileName_ar_SA.extension",
            "FileName_fil_PH.extension",
            "FileName_zh_CN.extension"
        };
        // When
        Pattern pattern = LocaleUtils.createResourceFilesPattern(FILE_NAME, FILE_EXTENSION);
        // Then
        for (String file : resourceFiles) {
            assertThat(file, pattern.matcher(file).matches(), is(equalTo(true)));
        }
    }

    @Test
    void shouldNotMatchInvalidResourceFilesWithCreatedResourceFilesPattern() {
        // Given
        String[] resourceFiles = {
            "Vulnerabilities.xml",
            "Vulnerabilities_en.xml",
            "Vulnerabilities_en_GB.xml",
            "OtherFile_ar_SA.properties",
            "fileName.ext"
        };
        // When
        Pattern pattern = LocaleUtils.createResourceFilesPattern(FILE_NAME, FILE_EXTENSION);
        // Then
        for (String file : resourceFiles) {
            assertThat(file, pattern.matcher(file).matches(), is(equalTo(false)));
        }
    }

    @Test
    void shouldMatchValidMessagesPropertiesFilesWithCreateMessagesPropertiesFilePattern() {
        // Given
        String[] resourceFiles = {
            "Messages.properties",
            "Messages_en.properties",
            "Messages_en_GB.properties",
            "Messages_ar_SA.properties",
            "Messages_fil_PH.properties",
            "Messages_zh_CN.properties"
        };
        // When
        Pattern pattern = LocaleUtils.createMessagesPropertiesFilePattern();
        // Then
        for (String file : resourceFiles) {
            assertThat(file, pattern.matcher(file).matches(), is(equalTo(true)));
        }
    }

    @Test
    void shouldNotMatchInvalidMessagesPropertiesFilesWithCreateMessagesPropertiesFilePattern() {
        // Given
        String[] resourceFiles = {
            "Vulnerabilities.xml",
            "Vulnerabilities_en.xml",
            "Vulnerabilities_en_GB.xml",
            "OtherFile_ar_SA.properties",
            "messages.properties"
        };
        // When
        Pattern pattern = LocaleUtils.createMessagesPropertiesFilePattern();
        // Then
        for (String file : resourceFiles) {
            assertThat(file, pattern.matcher(file).matches(), is(equalTo(false)));
        }
    }

    @Test
    void shouldAvailableLocalesBeNonEmpty() {
        // Given
        List<String> locales = LocaleUtils.getAvailableLocales();

        // When/Then
        assertThat(locales, is(not(empty())));
    }

    @Test
    void shouldHaveEnglishAsFirstAvailableLocale() {
        // Given
        List<String> locales = LocaleUtils.getAvailableLocales();

        // When
        String firstAvailableLocale = locales.get(0);

        // Then
        assertThat(firstAvailableLocale, is(equalTo("en_GB")));
    }

    @Test
    void shouldFindResourcesWithDefaultControl() {
        Locale defaultLocale = Locale.getDefault();
        try {
            // Given
            Locale.setDefault(Locale.FRANCE);
            List<String> resources = new ArrayList<>();
            // When
            LocaleUtils.findResource(
                    "org.example.file",
                    "ext",
                    LOCALE_SPAIN,
                    r -> {
                        resources.add(r);
                        return null;
                    });
            // Then
            assertThat(
                    resources,
                    contains(
                            "org/example/file_es_ES.ext",
                            "org/example/file_es.ext",
                            "org/example/file.ext",
                            "org/example/file_fr_FR.ext",
                            "org/example/file_fr.ext"));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    void shouldFindResourcesWithDefaultControlAndLocaleTokenReplaced() {
        Locale defaultLocale = Locale.getDefault();
        try {
            // Given
            Locale.setDefault(Locale.FRANCE);
            List<String> resources = new ArrayList<>();
            // When
            LocaleUtils.findResource(
                    "org.example.dir%LC%.file",
                    "ext",
                    "%LC%",
                    LOCALE_SPAIN,
                    r -> {
                        resources.add(r);
                        return null;
                    });
            // Then
            assertThat(
                    resources,
                    contains(
                            "org/example/dir_es_ES/file_es_ES.ext",
                            "org/example/dir_es/file_es.ext",
                            "org/example/dir/file.ext",
                            "org/example/dir_fr_FR/file_fr_FR.ext",
                            "org/example/dir_fr/file_fr.ext"));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    void shouldFindResourcesWithGivenControlWithoutFallbackLocale() {
        // Given
        ResourceBundle.Control control = mockResourceBundleControl();
        List<String> resources = new ArrayList<>();
        // When
        LocaleUtils.findResource(
                control,
                "org.example.file",
                "ext",
                LOCALE_SPAIN,
                r -> {
                    resources.add(r);
                    return null;
                });
        // Then
        assertThat(
                resources,
                contains(
                        "org/example/file_es_ES.ext",
                        "org/example/file_es.ext",
                        "org/example/file.ext"));
    }

    @Test
    void shouldFindResourcesWithGivenControlAndFallbackLocale() {
        // Given
        ResourceBundle.Control control = mockResourceBundleControl();
        given(control.getFallbackLocale(anyString(), any())).willReturn(Locale.FRANCE);
        List<String> resources = new ArrayList<>();
        // When
        LocaleUtils.findResource(
                control,
                "org.example.file",
                "ext",
                LOCALE_SPAIN,
                r -> {
                    resources.add(r);
                    return null;
                });
        // Then
        assertThat(
                resources,
                contains(
                        "org/example/file_es_ES.ext",
                        "org/example/file_es.ext",
                        "org/example/file.ext",
                        "org/example/file_fr_FR.ext",
                        "org/example/file_fr.ext"));
    }

    @Test
    void shouldFindResourcesWithGivenControlAndLocaleTokenReplaced() {
        // Given
        ResourceBundle.Control control = mockResourceBundleControl();
        List<String> resources = new ArrayList<>();
        // When
        LocaleUtils.findResource(
                control,
                "org.example.dir%LC%.file",
                "ext",
                "%LC%",
                LOCALE_SPAIN,
                r -> {
                    resources.add(r);
                    return null;
                });
        // Then
        assertThat(
                resources,
                contains(
                        "org/example/dir_es_ES/file_es_ES.ext",
                        "org/example/dir_es/file_es.ext",
                        "org/example/dir/file.ext"));
    }

    private static ResourceBundle.Control mockResourceBundleControl() {
        ResourceBundle.Control control = mock(ResourceBundle.Control.class);
        when(control.getCandidateLocales(anyString(), any()))
                .thenAnswer(
                        invocation -> {
                            Object[] args = invocation.getArguments();
                            return HELPER_CONTROL.getCandidateLocales(
                                    (String) args[0], (Locale) args[1]);
                        });
        when(control.toBundleName(anyString(), any()))
                .thenAnswer(
                        invocation -> {
                            Object[] args = invocation.getArguments();
                            return HELPER_CONTROL.toBundleName((String) args[0], (Locale) args[1]);
                        });
        return control;
    }
}
