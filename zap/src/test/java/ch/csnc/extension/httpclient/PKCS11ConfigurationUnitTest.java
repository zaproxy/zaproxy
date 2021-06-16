/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package ch.csnc.extension.httpclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.csnc.extension.httpclient.PKCS11Configuration.PCKS11ConfigurationBuilder;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/** Unit test for {@link ch.csnc.extension.httpclient.PKCS11Configuration} */
class PKCS11ConfigurationUnitTest {

    private static final String NAME = "Provider Name";
    private static final String LIBRARY = "path/to/library";

    private PKCS11Configuration configuration;

    private static PCKS11ConfigurationBuilder getConfigurationBuilderWithNameAndLibrarySet() {
        return PKCS11Configuration.builder().setName(NAME).setLibrary(LIBRARY);
    }

    @Test
    void shouldThrowExceptionWhenBuildingAConfigurationWithoutNameAndLibrary() {
        // Given
        PCKS11ConfigurationBuilder builder = PKCS11Configuration.builder();
        // When
        IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
        // Then
        assertThat(e.getMessage(), containsString("name"));
    }

    @Test
    void shouldThrowExceptionWhenBuildingAConfigurationWithoutName() {
        // Given
        PCKS11ConfigurationBuilder builder = PKCS11Configuration.builder().setLibrary(LIBRARY);
        // When
        IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
        // Then
        assertThat(e.getMessage(), containsString("name"));
    }

    @Test
    void shouldThrowExceptionWhenCreatingAConfigurationWithoutLibrary() {
        // Given
        PCKS11ConfigurationBuilder builder = PKCS11Configuration.builder().setName(NAME);
        // When
        IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
        // Then
        assertThat(e.getMessage(), containsString("library"));
    }

    @Test
    void shouldThrowExceptionWhenSettingAnEmptyName() {
        // Given
        PCKS11ConfigurationBuilder builder = PKCS11Configuration.builder();
        String name = "";
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> builder.setName(name));
        // Then
        assertThat(e.getMessage(), containsString("name"));
    }

    @Test
    void shouldThrowExceptionWhenSettingANullName() {
        // Given
        PCKS11ConfigurationBuilder builder = PKCS11Configuration.builder();
        String name = null;
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> builder.setName(name));
        // Then
        assertThat(e.getMessage(), containsString("name"));
    }

    @Test
    void shouldThrowExceptionWhenSettingAnEmptyLibrary() {
        // Given
        PCKS11ConfigurationBuilder builder = PKCS11Configuration.builder();
        String library = "";
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> builder.setLibrary(library));
        // Then
        assertThat(e.getMessage(), containsString("library"));
    }

    @Test
    void shouldThrowExceptionWhenSettingANullLibrary() {
        // Given
        PCKS11ConfigurationBuilder builder = PKCS11Configuration.builder();
        String library = null;
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> builder.setLibrary(library));
        // Then
        assertThat(e.getMessage(), containsString("library"));
    }

    @Test
    void shouldCreateConfigurationWithNonEmptyNameAndNonEmptyLibrary() {
        // Given
        PCKS11ConfigurationBuilder builder = PKCS11Configuration.builder();
        String nonEmptyName = "ProviderName";
        String nonEmptyLibrary = "Library";
        // When / Then
        assertDoesNotThrow(() -> builder.setName(nonEmptyName).setLibrary(nonEmptyLibrary).build());
    }

    @Test
    void shouldRetrieveNameSet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        String retrievedName = configuration.getName();
        // Then
        assertThat(retrievedName, is(equalTo(NAME)));
    }

    @Test
    void shouldRetrieveLibrarySet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        String retrievedLibrary = configuration.getLibrary();
        // Then
        assertThat(retrievedLibrary, is(equalTo(LIBRARY)));
    }

    @Test
    void shouldRetrieveDefaultSlotListIndexFromNewlyCreatedConfiguration() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        int retrievedSlotListIndex = configuration.getSlotListIndex();
        // Then
        assertThat(retrievedSlotListIndex, is(equalTo(0)));
    }

    @Test
    void shouldRetrieveUndefinedSlotIdFromNewlyCreatedConfiguration() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        int retrievedSlotId = configuration.getSlotId();
        // Then
        assertThat(retrievedSlotId, is(equalTo(-1)));
    }

    @Test
    void shouldRetrieveStringRepresentationFromNewlyCreatedConfiguration() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, containsString("name = \"" + NAME + "\""));
        assertThat(retrievedStringRepresentation, containsString("library = " + LIBRARY));
        assertThat(retrievedStringRepresentation, containsString("slotListIndex = 0"));
    }

    @Test
    void shouldThrowExceptionWhenSettingNegativeSlotListIndex() {
        // Given
        PCKS11ConfigurationBuilder builder = getConfigurationBuilderWithNameAndLibrarySet();
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> builder.setSlotListIndex(-1));
        // Then
        assertThat(e.getMessage(), containsString("slotListIndex"));
    }

    @Test
    void shouldRetrieveDescriptionSet() {
        // Given
        String description = "Description of Provider X";
        configuration =
                getConfigurationBuilderWithNameAndLibrarySet().setDescription(description).build();
        // When
        String retrievedDescription = configuration.getDescription();
        // Then
        assertThat(retrievedDescription, is(equalTo(description)));
    }

    @Test
    void shouldRetrieveNullDescriptionSet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setDescription(null).build();
        // When
        String retrievedDescription = configuration.getDescription();
        // Then
        assertThat(retrievedDescription, is(nullValue()));
    }

    @Test
    void shouldRetrieveEmptyDescriptionSet() {
        // Given
        String description = "";
        configuration =
                getConfigurationBuilderWithNameAndLibrarySet().setDescription(description).build();
        // When
        String retrievedDescription = configuration.getDescription();
        // Then
        assertThat(retrievedDescription, is(equalTo(description)));
    }

    @Test
    void shouldRetrieveUndefinedDescriptionFromNewlyCreatedConfiguration() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        String retrievedDescription = configuration.getDescription();
        // Then
        assertThat(retrievedDescription, is(nullValue()));
    }

    @Test
    void shouldRetrieveStringRepresentationWithDescriptionSet() {
        // Given
        String description = "Description of Provider X";
        configuration =
                getConfigurationBuilderWithNameAndLibrarySet().setDescription(description).build();
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, containsString("description = " + description));
    }

    @Test
    void shouldRetrieveStringRepresentationWithOutDescriptionWhenEmptyDescriptionSet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setDescription("").build();
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, not(containsString("description = ")));
    }

    @Test
    void shouldRetrieveStringRepresentationWithOutDescriptionWhenNullDescriptionSet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setDescription(null).build();
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, not(containsString("description = ")));
    }

    @Test
    void shouldRetrieveSlotListIndexSet() {
        // Given
        int slotListIndexSet = 1;
        configuration =
                getConfigurationBuilderWithNameAndLibrarySet()
                        .setSlotListIndex(slotListIndexSet)
                        .build();
        // When
        int retrievedSlotListIndexSet = configuration.getSlotListIndex();
        // Then
        assertThat(retrievedSlotListIndexSet, is(equalTo(slotListIndexSet)));
    }

    @Test
    void shouldThrowExceptionWhenSettingNegativeSlotId() {
        // Given
        PCKS11ConfigurationBuilder builder = getConfigurationBuilderWithNameAndLibrarySet();
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> builder.setSlotId(-1));
        // Then
        assertThat(e.getMessage(), containsString("slotId"));
    }

    @Test
    void shouldRetrieveSlotIdSet() {
        // Given
        int slotIdSet = 1;
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setSlotId(slotIdSet).build();
        // When
        int retrievedSlotId = configuration.getSlotId();
        // Then
        assertThat(retrievedSlotId, is(equalTo(slotIdSet)));
    }

    @Test
    void shouldRetrieveSlotIdAsUndefinedGivenASlotListIndexSet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setSlotListIndex(1).build();
        // When
        int retrievedSlotId = configuration.getSlotId();
        // Then
        assertThat(retrievedSlotId, is(equalTo(-1)));
    }

    @Test
    void shouldRetrieveSlotListIndexAsUndefinedGivenASlotIdSet() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().setSlotId(1).build();
        // When
        int retrievedSlotListIndex = configuration.getSlotListIndex();
        // Then
        assertThat(retrievedSlotListIndex, is(equalTo(-1)));
    }

    @Test
    void
            shouldRetrieveStringRepresentationWithAttributeSlotInsteadOfAttributeSlotListIndexGivenASlotIdSet() {
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
    void shouldRetrieveStringRepresentationWithBackslashesPresentInNameEscapedWithBackslashes() {
        // Given
        String nameWithBackslash = "\\";
        configuration =
                PKCS11Configuration.builder()
                        .setName(nameWithBackslash)
                        .setLibrary(LIBRARY)
                        .build();
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, containsString("\\\\"));
    }

    @Test
    void shouldRetrieveStringRepresentationWithQuotationMarksPresentInNameEscapedWithBackslashes() {
        // Given
        String nameWithQuotationMark = "\"";
        configuration =
                PKCS11Configuration.builder()
                        .setName(nameWithQuotationMark)
                        .setLibrary(LIBRARY)
                        .build();
        // When
        String retrievedStringRepresentation = configuration.toString();
        // Then
        assertThat(retrievedStringRepresentation, containsString("\\\""));
    }

    @Test
    void shouldRetrieveInputStream() {
        // Given
        configuration = getConfigurationBuilderWithNameAndLibrarySet().build();
        // When
        InputStream retrievedInputStream = configuration.toInpuStream();
        // Then
        assertThat(retrievedInputStream, is(not(nullValue())));
    }
}
