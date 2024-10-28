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
package org.parosproxy.paros.core.scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.core.scanner.Alert.Builder;
import org.parosproxy.paros.db.RecordAlert;

class AlertUnitTest {

    @Test
    void shouldHaveNoHistoryIdByDefault() {
        // Given / When
        Alert alert = new Alert(1);
        // Then
        assertThat(alert.getHistoryId(), is(equalTo(0)));
    }

    @Test
    void shouldHaveHistoryIdFromRecordAlert() {
        // Given
        RecordAlert recordAlert = mock(RecordAlert.class);
        int historyId = 42;
        given(recordAlert.getHistoryId()).willReturn(historyId);
        // When
        Alert alert = new Alert(recordAlert);
        // Then
        assertThat(alert.getHistoryId(), is(equalTo(historyId)));
    }

    @Test
    void shouldHaveNoSourceHistoryIdByDefault() {
        // Given / When
        Alert alert = new Alert(1);
        // Then
        assertThat(alert.getSourceHistoryId(), is(equalTo(0)));
    }

    @Test
    void shouldHaveSourceHistoryIdFromRecordAlert() {
        // Given
        RecordAlert recordAlert = mock(RecordAlert.class);
        int sourceHistoryId = 42;
        given(recordAlert.getSourceHistoryId()).willReturn(sourceHistoryId);
        // When
        Alert alert = new Alert(recordAlert);
        // Then
        assertThat(alert.getSourceHistoryId(), is(equalTo(sourceHistoryId)));
    }

    @Test
    void shouldDefaultAlertRefToPluginId() {
        // Given
        Alert alert = new Alert(1);
        // When / Then
        assertThat(alert.getAlertRef(), is(equalTo(Integer.toString(alert.getPluginId()))));
    }

    @Test
    void shouldSetValidAlertRef() {
        // Given
        Alert alert = new Alert(1);
        String alertRef = "1-1";
        // When
        alert.setAlertRef(alertRef);
        // Then
        assertThat(alert.getAlertRef(), is(equalTo(alertRef)));
    }

    @Test
    void shouldHaveEmptyAlertRefForManualAlerts() {
        // Given
        Alert alert = new Alert(-1);
        // When / Then
        assertThat(alert.getAlertRef(), is(equalTo("")));
    }

    @Test
    void shouldThrowExceptionOnInvalidAlertRef() {
        // Given
        Alert alert = new Alert(1);
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> alert.setAlertRef("2"));
    }

    @Test
    void shouldThrowExceptionOnTooLongAlertRef() {
        // Given
        Alert alert = new Alert(1);
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> alert.setAlertRef(new String(new char[256]).replace("\0", "1")));
    }

    @Test
    void shouldNotEqualIfAlertRefDifferent() {
        // Given
        Alert alertA = new Alert(1);
        Alert alertB = new Alert(1);
        // When
        alertA.setAlertRef("1-1");
        alertB.setAlertRef("1-2");
        boolean equals = alertA.equals(alertB);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    void shouldNotCompareIfAlertRefDifferent() {
        // Given
        Alert alertA = new Alert(1);
        Alert alertB = new Alert(1);
        // When
        alertA.setAlertRef("1-1");
        alertB.setAlertRef("1-2");
        int cmp = alertA.compareTo(alertB);
        // Then
        assertThat(cmp, is(equalTo(-1)));
    }

    @Test
    void shouldBuildAlertWithOneTag() {
        // Given
        Builder builder = new Alert.Builder();
        // When
        builder.addTag("Test");
        Alert alert = builder.build();
        int tagCount = alert.getTags().size();
        // Then
        assertThat(tagCount, is(equalTo(1)));
    }

    @Test
    void shouldBuildAlertWithTwoTagsWhenOneSetAndOneAdded() {
        // Given
        Builder builder = new Alert.Builder();
        // When
        Map<String, String> tags = new HashMap<>();
        tags.put("Test1", "Test");
        builder.setTags(tags);
        builder.addTag("Test2");
        Alert alert = builder.build();
        int tagCount = alert.getTags().size();
        // Then
        assertThat(tagCount, is(equalTo(2)));
    }

    @Test
    void shouldBuildAlertWithOneTagWhenOneSetAndOneAddedOneRemoved() {
        // Given
        Builder builder = new Alert.Builder();
        // When
        Map<String, String> tags = new HashMap<>();
        tags.put("Test1", "Test");
        builder.setTags(tags);
        builder.addTag("Test2");
        builder.removeTag("Test1");
        Alert alert = builder.build();
        int tagCount = alert.getTags().size();
        // Then
        assertThat(tagCount, is(equalTo(1)));
        assertThat(alert.getTags().get("Test2"), is(equalTo("")));
    }

    @Test
    void shouldBuildAlertWithNoTagsWhenOneRemoved() {
        // Given
        Builder builder = new Alert.Builder();
        // When
        builder.removeTag("Test");
        Alert alert = builder.build();
        int tagCount = alert.getTags().size();
        // Then
        assertThat(tagCount, is(equalTo(0)));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void shouldNotAddCweTagIfNotValidValue(int cweId) {
        // Given
        Builder builder = new Alert.Builder();
        builder.setCweId(cweId);
        Alert alert = builder.build();
        // When
        int tagCount = alert.getTags().size();
        // Then
        assertThat(tagCount, is(equalTo(0)));
    }

    @Test
    void shouldAddCweTagWhenPlausibleValue() {
        // Given
        int cwe = 618;
        String cweUrl = "https://cwe.mitre.org/data/definitions/618.html";
        String cweKey = "CWE-" + cwe;
        Builder builder = new Alert.Builder();
        // When
        builder.setCweId(cwe);
        Alert alert = builder.build();
        int tagCount = alert.getTags().size();
        // Then
        Map<String, String> tags = alert.getTags();
        assertThat(tagCount, is(equalTo(1)));
        assertThat(tags.containsKey(cweKey), is(equalTo(true)));
        assertThat(tags.get(cweKey), is(equalTo(cweUrl)));
    }

    @Test
    void shouldAddOneCweTagEvenIfCweIdSetTwice() {
        // Given
        int cwe = 618;
        int cwe2 = 619;
        String cweUrl = "https://cwe.mitre.org/data/definitions/619.html";
        String cweKey = "CWE-" + cwe2;
        Builder builder = new Alert.Builder();
        // When
        builder.setCweId(cwe);
        builder.setCweId(cwe2); // This one will carry thru
        Alert alert = builder.build();
        int tagCount = alert.getTags().size();
        // Then
        Map<String, String> tags = alert.getTags();
        assertThat(tagCount, is(equalTo(1)));
        assertThat(tags.containsKey(cweKey), is(equalTo(true)));
        assertThat(tags.get(cweKey), is(equalTo(cweUrl)));
    }

    @Test
    void shouldAddLastCweTagEvenIfCweIdSetTwiceAndBuildTwice() {
        // Given
        int cwe = 618;
        int cwe2 = 619;
        String cwe2Url = "https://cwe.mitre.org/data/definitions/619.html";
        String cwe2Key = "CWE-" + cwe2;
        Builder builder = new Alert.Builder();
        // When
        builder.setCweId(cwe);
        Alert alert = builder.build();
        builder.setCweId(cwe2); // This one will carry thru
        alert = builder.build();
        int tagCount = alert.getTags().size();
        // Then
        Map<String, String> tags = alert.getTags();
        assertThat(tagCount, is(equalTo(1)));
        System.out.println(tags);
        assertThat(tags.containsKey(cwe2Key), is(equalTo(true)));
        assertThat(tags.get(cwe2Key), is(equalTo(cwe2Url)));
    }
}
