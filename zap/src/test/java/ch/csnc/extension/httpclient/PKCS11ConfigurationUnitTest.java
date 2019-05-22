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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import org.junit.Test;

import ch.csnc.extension.httpclient.PKCS11Configuration.PCKS11ConfigurationBuilder;

/**
 * Unit test for {@link ch.csnc.extension.httpclient.PKCS11Configuration}
 */
public class PKCS11ConfigurationUnitTest {

    private static final String NAME = "Provider Name";
    private static final String LIBRARY = "path/to/library";

    private PKCS11Configuration configuration;
    private PCKS11ConfigurationBuilder configurationBuilder;

    private static PCKS11ConfigurationBuilder getConfigurationBuilderWithNameAndLibrarySet() {
        return PKCS11Configuration.builder().setName(NAME).setLibrary(LIBRARY);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenBuildingAConfigurationWithoutNameAndLibrary() {
        PKCS11Configuration.builder().build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenBuildingAConfigurationWithoutName() {
        PKCS11Configuration.builder().setLibrary(LIBRARY).build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenCreatingAConfigurationWithoutLibrary() {
        PKCS11Configuration.builder().setName(NAME).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingAnEmptyName() {
        PKCS11Configuration.builder().setName("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingANullName() {
        PKCS11Configuration.builder().setName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingAnEmptyLibrary() {
        PKCS11Configuration.builder().setLibrary("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingANullLibrary() {
        PKCS11Configuration.builder().setLibrary(null);
    }

    @Test
    public void shouldCreateConfigurationWithNonEmptyNameAndNonEmptyLibrary() {
        // Given
        String nonEmptyName = "ProviderName";
        String nonEmptyLibrary = "Library";
        // When / Then
        PKCS11Configuration.builder().setName(nonEmptyName).setLibrary(nonEmptyLibrary).build();
    }

    @Test
    public void shouldRetrieveNameSet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        String retrievedName = configuration.getName();
        // Then
        assertThat(retrievedName, is(equalTo(NAME)));
    }

    @Test
    public void shouldRetrieveLibrarySet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        String retrievedLibrary = configuration.getLibrary();
        // Then
        assertThat(retrievedLibrary, is(equalTo(LIBRARY)));
    }

    @Test
    public void shouldRetrieveDefaultSlotListIndexFromNewlyCreatedConfiguration() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        int retrievedSlotListIndex = configuration.getSlotListIndex();
        // Then
        assertThat(retrievedSlotListIndex, is(equalTo(0)));
    }

    @Test
    public void shouldRetrieveUndefinedSlotIdFromNewlyCreatedConfiguration() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        int retrievedSlotId = configuration.getSlotId();
        // Then
        assertThat(retrievedSlotId, is(equalTo(-1)));
    }

    @Test
    public void shouldRetrieveStringRepresentationFromNewlyCreatedConfiguration() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, containsString("name = \"" + NAME + "\""));
        assertThat(retrievedStringRepresentation, containsString("library = " + LIBRARY));
        assertThat(retrievedStringRepresentation, containsString("slotListIndex = 0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingNegativeSlotListIndex() {
        // Given
        configurationBuilder = getConfigurationBuilderWithNameAndLibrarySet();
        // When
        configurationBuilder.setSlotListIndex(-1);
        // Then = IllegalArgumentException.class
    }

    @Test
    public void shouldRetrieveDescriptionSet() {
        // Given
        String description = "Description of Provider X";
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setDescription(description).build();
        // When
        String retrievedDescription = configuration.getDescription();
        // Then
        assertThat(retrievedDescription, is(equalTo(description)));
    }

    @Test
    public void shouldRetrieveNullDescriptionSet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setDescription(null).build();
        // When
        String retrievedDescription = configuration.getDescription();
        // Then
        assertThat(retrievedDescription, is(nullValue()));
    }

    @Test
    public void shouldRetrieveEmptyDescriptionSet() {
        // Given
        String description = "";
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setDescription(description).build();
        // When
        String retrievedDescription = configuration.getDescription();
        // Then
        assertThat(retrievedDescription, is(equalTo(description)));
    }

    @Test
    public void shouldRetrieveUndefinedDescriptionFromNewlyCreatedConfiguration() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        String retrievedDescription = configuration.getDescription();
        // Then
        assertThat(retrievedDescription, is(nullValue()));
    }

    @Test
    public void shouldRetrieveStringRepresentationWithDescriptionSet() {
        // Given
        String description = "Description of Provider X";
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setDescription(description).build();
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, containsString("description = " + description));
    }

    @Test
    public void shouldRetrieveStringRepresentationWithOutDescriptionWhenEmptyDescriptionSet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setDescription("").build();
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, not(containsString("description = ")));
    }

    @Test
    public void shouldRetrieveStringRepresentationWithOutDescriptionWhenNullDescriptionSet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setDescription(null).build();
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, not(containsString("description = ")));
    }

    @Test
    public void shouldRetrieveSlotListIndexSet() {
        // Given
        int slotListIndexSet = 1;
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setSlotListIndex(slotListIndexSet).build();
        // When
        int retrievedSlotListIndexSet = configuration.getSlotListIndex();
        // Then
        assertThat(retrievedSlotListIndexSet, is(equalTo(slotListIndexSet)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingNegativeSlotId() {
        // Given
        configurationBuilder = getConfigurationBuilderWithNameAndLibrarySet();
        // When
        configurationBuilder.setSlotId(-1);
        // Then = IllegalArgumentException.class
    }

    @Test
    public void shouldRetrieveSlotIdSet() {
        // Given
        int slotIdSet = 1;
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setSlotId(slotIdSet).build();
        // When
        int retrievedSlotId = configuration.getSlotId();
        // Then
        assertThat(retrievedSlotId, is(equalTo(slotIdSet)));
    }

    @Test
    public void shouldRetrieveSlotIdAsUndefinedGivenASlotListIndexSet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setSlotListIndex(1).build();
        // When
        int retrievedSlotId = configuration.getSlotId();
        // Then
        assertThat(retrievedSlotId, is(equalTo(-1)));
    }

    @Test
    public void shouldRetrieveSlotListIndexAsUndefinedGivenASlotIdSet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setSlotId(1).build();
        // When
        int retrievedSlotListIndex = configuration.getSlotListIndex();
        // Then
        assertThat(retrievedSlotListIndex, is(equalTo(-1)));
    }

    @Test
    public void shouldRetrieveStringRepresentationWithAttributeSlotInsteadOfAttributeSlotListIndexGivenASlotIdSet() {
        // Given
        int slotId = 1;
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setSlotId(slotId).build();
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, containsString("slot = " + slotId));
        assertThat(retrievedStringRepresentation, not(containsString("slotListIndex")));
    }

    @Test
    public void shouldRetrieveStringRepresentationWithBackslashesPresentInNameEscapedWithBackslashes() {
        // Given
        String nameWithBackslash = "\\";
        configuration = PKCS11Configuration.builder().setName(nameWithBackslash).setLibrary(LIBRARY).build();
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, containsString("\\\\"));
    }

    @Test
    public void shouldRetrieveStringRepresentationWithQuotationMarksPresentInNameEscapedWithBackslashes() {
        // Given
        String nameWithQuotationMark = "\"";
        configuration = PKCS11Configuration.builder().setName(nameWithQuotationMark).setLibrary(LIBRARY).build();
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, containsString("\\\""));
    }

    @Test
    public void shouldRetrieveInputStream() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        InputStream retrievedInputStream = configuration.toInpuStream();
        // Then
        assertThat(retrievedInputStream, is(not(nullValue())));
    }

}
