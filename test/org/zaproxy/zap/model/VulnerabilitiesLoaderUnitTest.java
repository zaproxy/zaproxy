package org.zaproxy.zap.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

/**
 * Unit test for {@link VulnerabilitiesLoader}.
 */
public class VulnerabilitiesLoaderUnitTest {

    private static final Path DIRECTORY = Paths.get("test/resources/vulnerabilities/");
    private static final Path DIRECTORY_INVALID = Paths.get("test/resources/vulnerabilities/invalid");
    private static final String FILE_NAME = "vulnerabilities-test";
    private static final String FILE_EXTENSION = ".xml";

    public VulnerabilitiesLoader loader;

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrownExceptionIfDirectoryIsNull() {
        // Given
        Path directory = null;
        // When
        loader = new VulnerabilitiesLoader(directory, FILE_NAME, FILE_EXTENSION);
        // Then = Exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrownExceptionIfFileNameIsNull() {
        // Given
        String fileName = null;
        // When
        loader = new VulnerabilitiesLoader(DIRECTORY, fileName, FILE_EXTENSION);
        // Then = Exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrownExceptionIfFileNameIsEmpty() {
        // Given
        String fileName = "";
        // When
        loader = new VulnerabilitiesLoader(DIRECTORY, fileName, FILE_EXTENSION);
        // Then = Exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrownExceptionIfFileExtensionIsNull() {
        // Given
        String fileExtension = null;
        // When
        loader = new VulnerabilitiesLoader(DIRECTORY, FILE_NAME, fileExtension);
        // Then = Exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrownExceptionIfFileExtensionIsEmpty() {
        // Given
        String fileExtension = "";
        // When
        loader = new VulnerabilitiesLoader(DIRECTORY, FILE_NAME, fileExtension);
        // Then = Exception
    }

    @Test
    public void shouldReturnEmptyListIfVulnerabilitiesFileNotFound() {
        // Given
        loader = new VulnerabilitiesLoader(DIRECTORY, "FileNotFound", ".NoExtension");
        // When
        List<Vulnerability> vulnerabilities = loader.load(Locale.ROOT);
        // Then
        assertThat(vulnerabilities, is(empty()));
    }

    @Test
    public void shouldReturnListWithVulnerabilitiesForDefaultLocale() {
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
    public void shouldLoadFileWithSameLanguageCountryWhenAvailable() {
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
    public void shouldLoadDefaultFileEvenIfFileWithSameLanguageButDifferentCountryIsAvailable() {
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
    public void shouldLoadFileWithOnlyLanguageMatchWhenLanguageCountryNotAvailable() {
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
    public void shouldReturnEmptyListIfFoundFileIsEmpty() {
        // Given
        loader = new VulnerabilitiesLoader(DIRECTORY_INVALID, FILE_NAME + "-empty", FILE_EXTENSION);
        // When
        List<Vulnerability> vulnerabilities = loader.load(Locale.ROOT);
        // Then
        assertThat(vulnerabilities, is(empty()));
    }

    @Test
    public void shouldReturnEmptyListIfFoundFileIsNotValidXml() {
        // Given
        loader = new VulnerabilitiesLoader(DIRECTORY_INVALID, FILE_NAME + "-invalid-xml", FILE_EXTENSION);
        // When
        List<Vulnerability> vulnerabilities = loader.load(Locale.ROOT);
        // Then
        assertThat(vulnerabilities, is(empty()));
    }
}
