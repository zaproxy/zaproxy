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
package org.zaproxy.zap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit test for {@link org.zaproxy.zap.Version}. */
class VersionUnitTest {

    @Test
    void shouldThrowExceptionIfVersionIsNull() {
        // Given
        String version = null;
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> new Version(version));
        // Then
        assertThat(e.getMessage(), containsString("null"));
    }

    @Test
    void shouldThrowExceptionIfVersionIsEmpty() {
        // Given
        String version = "";
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> new Version(version));
        // Then
        assertThat(e.getMessage(), containsString("empty"));
    }

    @Test
    void shouldNotAcceptVersionWithOnlyMajorVersion() {
        // Given
        String versionWithOnlyMajorNumber = "1";
        // When
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Version(versionWithOnlyMajorNumber));
        // Then
        assertThat(e.getMessage(), containsString("is not valid"));
    }

    @Test
    void shouldNotAcceptVersionWithOnlyMajorAndMinorVersion() {
        // Given
        String versionWithOnlyMajorAndMinorNumbers = "1.0";
        // When
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Version(versionWithOnlyMajorAndMinorNumbers));
        // Then
        assertThat(e.getMessage(), containsString("is not valid"));
    }

    @Test
    void shouldAcceptVersionWithMajorMinorAndPatchNumbers() {
        // Given
        String versionWithOnlyMajorMinorAndPatchNumbers = "1.0.0";
        // When / Then
        assertDoesNotThrow(() -> new Version(versionWithOnlyMajorMinorAndPatchNumbers));
    }

    @Test
    void shouldAcceptVersionWithPreReleaseIdentifier() {
        // Given
        String versionWithPreReleaseIdentifiers = "1.2.3-SNAPSHOT";
        // When
        assertDoesNotThrow(() -> new Version(versionWithPreReleaseIdentifiers));
    }

    @Test
    void shouldReturnTrueWhenEqualingToTheSameVersionNumber() {
        // Given
        Version version = new Version("1.0.0");
        Version differentVersion = new Version("1.0.0");
        // When
        boolean equals = version.equals(differentVersion);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    void shouldReturnFalseWhenEqualingToDifferentVersionNumbers() {
        // Given
        Version version = new Version("1.0.0");
        Version differentVersion = new Version("2.0.0");
        // When
        boolean equals = version.equals(differentVersion);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    void shouldReturnPositiveNumberWhenComparingToOlderPatchVersion() {
        // Given
        Version version = new Version("2.0.1");
        Version olderVersion = new Version("2.0.0");
        // When
        int comparisonResult = version.compareTo(olderVersion);
        // Then
        assertThat(comparisonResult, is(greaterThan(0)));
    }

    @Test
    void shouldReturnPositiveNumberWhenComparingToOlderMinorVersion() {
        // Given
        Version version = new Version("2.1.0");
        Version olderVersion = new Version("2.0.0");
        // When
        int comparisonResult = version.compareTo(olderVersion);
        // Then
        assertThat(comparisonResult, is(greaterThan(0)));
    }

    @Test
    void shouldReturnPositiveNumberWhenComparingToOlderMajorVersion() {
        // Given
        Version version = new Version("3.0.0");
        Version olderVersion = new Version("2.0.0");
        // When
        int comparisonResult = version.compareTo(olderVersion);
        // Then
        assertThat(comparisonResult, is(greaterThan(0)));
    }

    @Test
    void shouldReturnZeroWhenComparingToEqualVersion() {
        // Given
        Version version = new Version("1.0.0");
        Version sameVersion = new Version("1.0.0");
        // When
        int comparisonResult = version.compareTo(sameVersion);
        // Then
        assertThat(comparisonResult, is(equalTo(0)));
    }

    @Test
    void shouldReturnNegativeNumberWhenComparingToNewerPatchVersion() {
        // Given
        Version version = new Version("1.0.0");
        Version newerVersion = new Version("1.0.1");
        // When
        int comparisonResult = version.compareTo(newerVersion);
        // Then
        assertThat(comparisonResult, is(lessThan(0)));
    }

    @Test
    void shouldReturnNegativeNumberWhenComparingToNewerMinorVersion() {
        // Given
        Version version = new Version("1.0.0");
        Version newerVersion = new Version("1.1.0");
        // When
        int comparisonResult = version.compareTo(newerVersion);
        // Then
        assertThat(comparisonResult, is(lessThan(0)));
    }

    @Test
    void shouldReturnNegativeNumberWhenComparingToNewerMajorVersion() {
        // Given
        Version version = new Version("1.0.0");
        Version newerVersion = new Version("2.0.0");
        // When
        int comparisonResult = version.compareTo(newerVersion);
        // Then
        assertThat(comparisonResult, is(lessThan(0)));
    }

    @Test
    void shouldMatchExactVersion() {
        // Given
        Version version = new Version("1.2.3");
        String rangeVersion = "1.2.3";
        // When
        boolean matchResult = version.matches(rangeVersion);
        // Then
        assertThat(matchResult, is(equalTo(true)));
    }

    @Test
    void shouldMatchAllVersionsWithWildcardRange() {
        // Given
        List<Version> versions =
                Arrays.asList(new Version("0.0.1"), new Version("0.2.0"), new Version("3.0.0"));
        String rangeVersion = "*";
        for (Version version : versions) {
            // When
            boolean matchResult = version.matches(rangeVersion);
            // Then
            assertThat(matchResult, is(equalTo(true)));
        }
    }

    @Test
    void shouldMatchMinorWildcardedVersionWithEqualMajorVersion() {
        // Given
        Version version = new Version("1.0.0");
        String rangeVersion = "1.*";
        // When
        boolean matchResult = version.matches(rangeVersion);
        // Then
        assertThat(matchResult, is(equalTo(true)));
    }

    @Test
    void shouldMatchMinorWildcardedVersionWithEqualMajorVersionAndDifferentMinorVersion() {
        // Given
        Version version = new Version("1.5.0");
        String rangeVersion = "1.*";
        // When
        boolean matchResult = version.matches(rangeVersion);
        // Then
        assertThat(matchResult, is(equalTo(true)));
    }

    @Test
    void shouldMatchMinorWildcardedVersionWithEqualMajorVersionAndDifferentPatchVersion() {
        // Given
        Version version = new Version("1.0.4");
        String rangeVersion = "1.*";
        // When
        boolean matchResult = version.matches(rangeVersion);
        // Then
        assertThat(matchResult, is(equalTo(true)));
    }

    @Test
    void shouldMatchMinorWildcardedVersionWithEqualMajorVersionAndDifferentMinorAndPatchVersion() {
        // Given
        Version version = new Version("1.9.3");
        String rangeVersion = "1.*";
        // When
        boolean matchResult = version.matches(rangeVersion);
        // Then
        assertThat(matchResult, is(equalTo(true)));
    }

    @Test
    void shouldNotMatchMinorWildcardedVersionWithGreaterMajorVersion() {
        // Given
        Version version = new Version("2.0.0");
        String rangeVersion = "1.*";
        // When
        boolean matchResult = version.matches(rangeVersion);
        // Then
        assertThat(matchResult, is(equalTo(false)));
    }

    @Test
    void shouldReturnVersionAsString() {
        // Given
        Version version = new Version("1.2.3");
        // When
        String string = version.toString();
        // Then
        assertThat(string, is(equalTo("1.2.3")));
    }
}
