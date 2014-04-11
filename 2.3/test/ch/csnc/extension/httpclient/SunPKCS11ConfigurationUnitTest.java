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
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package ch.csnc.extension.httpclient;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link ch.csnc.extension.httpclient.SunPKCS11Configuration}
 */
public class SunPKCS11ConfigurationUnitTest {

    private static final String NAME = "Provider Name";
    private static final String LIBRARY = "path/to/library";

    private SunPKCS11Configuration configuration;

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCreatingAConfigurationWithoutName() {
        configuration = new SunPKCS11Configuration(null, LIBRARY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCreatingAConfigurationWithEmptyName() {
        configuration = new SunPKCS11Configuration("", LIBRARY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCreatingAConfigurationWithoutLibrary() {
        configuration = new SunPKCS11Configuration(NAME, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCreatingAConfigurationWithEmptyLibrary() {
        configuration = new SunPKCS11Configuration(NAME, "");
    }

    @Test
    public void shouldCreateConfigurationWithNonEmptyNameAndNonEmptyLibrary() {
        // Given
        String nonEmptyName = "ProviderName";
        String nonEmptyLibrary = "Library";
        // When
        configuration = new SunPKCS11Configuration(nonEmptyName, nonEmptyLibrary);
        // Then
        assertThat(configuration.getName(), is(equalTo(nonEmptyName)));
        assertThat(configuration.getLibrary(), is(equalTo(nonEmptyLibrary)));
    }

    @Test
    public void shouldRetrieveDefaultSlotListIndexFromNewlyCreatedConfiguration() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        // When
        int retrievedSlotListIndex = configuration.getSlotListIndex();
        // Then
        assertThat(retrievedSlotListIndex, is(equalTo(0)));
    }

    @Test
    public void shouldRetrieveUndefinedSlotIdFromNewlyCreatedConfiguration() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        // When
        int retrievedSlotId = configuration.getSlotId();
        // Then
        assertThat(retrievedSlotId, is(equalTo(-1)));
    }

    @Test
    public void shouldRetrieveStringRepresentationFromNewlyCreatedConfiguration() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, containsString("name = \"" + NAME + "\""));
        assertThat(retrievedStringRepresentation, containsString("library = " + LIBRARY));
        assertThat(retrievedStringRepresentation, containsString("slotListIndex = 0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingANullName() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        // When
        configuration.setName(null);
        // Then = IllegalArgumentException.class
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingAnEmptyName() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        // When
        configuration.setName("");
        // Then = IllegalArgumentException.class
    }

    @Test
    public void shouldRetrieveNameSet() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        String nameSet = "another name";
        // When
        configuration.setName(nameSet);
        // Then
        assertThat(configuration.getName(), is(equalTo(nameSet)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingANullLibrary() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        // When
        configuration.setLibrary(null);
        // Then = IllegalArgumentException.class
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingAnEmptyLibrary() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        // When
        configuration.setLibrary("");
        // Then = IllegalArgumentException.class
    }

    @Test
    public void shouldRetrieveLibrarySet() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        String librarySet = "/path/to/another/library";
        // When
        configuration.setLibrary(librarySet);
        // Then
        assertThat(configuration.getLibrary(), is(equalTo(librarySet)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingNegativeSlotListIndex() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        // When
        configuration.setSlotListIndex(-1);
        // Then = IllegalArgumentException.class
    }

    @Test
    public void shouldRetrieveSlotListIndexSet() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        int slotListIndexSet = 1;
        // When
        configuration.setSlotListIndex(slotListIndexSet);
        // Then
        assertThat(configuration.getSlotListIndex(), is(equalTo(slotListIndexSet)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingNegativeSlotId() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        // When
        configuration.setSlotId(-1);
        // Then = IllegalArgumentException.class
    }

    @Test
    public void shouldRetrieveSlotIdSet() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        int slotIdSet = 1;
        // When
        configuration.setSlotId(slotIdSet);
        // Then
        assertThat(configuration.getSlotId(), is(equalTo(slotIdSet)));
    }

    @Test
    public void shouldRetrieveSlotIdAsUndefinedGivenASlotListIndexSet() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        configuration.setSlotListIndex(1);
        // When
        int retrievedSlotId = configuration.getSlotId();
        // Then
        assertThat(retrievedSlotId, is(equalTo(-1)));
    }

    @Test
    public void shouldRetrieveSlotListIndexAsUndefinedGivenASlotIdSet() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        configuration.setSlotId(1);
        // When
        int retrievedSlotListIndex = configuration.getSlotListIndex();
        // Then
        assertThat(retrievedSlotListIndex, is(equalTo(-1)));
    }

    @Test
    public void shouldRetrieveStringRepresentationWithAttributeSlotInsteadOfAttributeSlotListIndexGivenASlotIdSet() {
        // Given
        configuration = new SunPKCS11Configuration(NAME, LIBRARY);
        configuration.setSlotId(1);
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, containsString("slot = 1"));
        assertThat(retrievedStringRepresentation, not(containsString("slotListIndex")));
    }

    @Test
    public void shouldRetrieveStringRepresentationWithBackslashesPresentInNameEscapedWithBackslashes() {
        // Given
        String nameWithBackslash = "\\";
        configuration = new SunPKCS11Configuration(nameWithBackslash, LIBRARY);
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, containsString("\\\\"));
    }

    @Test
    public void shouldRetrieveStringRepresentationWithQuotationMarksPresentInNameEscapedWithBackslashes() {
        // Given
        String nameWithQuotationMark = "\"";
        configuration = new SunPKCS11Configuration(nameWithQuotationMark, LIBRARY);
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, containsString("\\\""));
    }

}
