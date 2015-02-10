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
package org.zaproxy.zap;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.zaproxy.zap.Version;

/**
 * Unit test for {@link Version}.
 */
public class VersionUnitTest {

    private static final String VALID_VERSION = "1.0.0";

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfVersionIsNull() {
        // Given
        String version = null;
        // When
        new Version(version);
        // Then = Exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfVersionIsEmpty() {
        // Given
        String version = "";
        // When
        new Version(version);
        // Then = Exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAcceptVersionWithOnlyMajorVersion() {
        // Given
        String versionWithOnlyMajorNumber = "1";
        // When
        new Version(versionWithOnlyMajorNumber);
        // Then = Exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAcceptVersionWithOnlyMajorAndMinorVersion() {
        // Given
        String versionWithOnlyMajorAndMinorNumbers = "1.0";
        // When
        new Version(versionWithOnlyMajorAndMinorNumbers);
        // Then = Exception
    }

    @Test
    public void shouldAcceptVersionWithMajorMinorAndPatchNumbers() {
        // Given
        String versionWithOnlyMajorMinorAndPatchNumbers = "1.0.0";
        // When
        new Version(versionWithOnlyMajorMinorAndPatchNumbers);
        // Then = no exception
    }

    @Test
    public void shouldReturnTrueWhenEqualingToTheSameVersionNumber() {
        // Given
        Version version = new Version("1.0.0");
        Version differentVersion = new Version("1.0.0");
        // When
        boolean equals = version.equals(differentVersion);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    public void shouldReturnFalseWhenEqualingToDifferntVersionNumbers() {
        // Given
        Version version = new Version("1.0.0");
        Version differentVersion = new Version("2.0.0");
        // When
        boolean equals = version.equals(differentVersion);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    public void shouldReturnPositiveNumberWhenComparingToOlderPatchVersion() {
        // Given
        Version version = new Version("2.0.1");
        Version olderVersion = new Version("2.0.0");
        // When
        int comparisonResult = version.compareTo(olderVersion);
        // Then
        assertThat(comparisonResult, is(greaterThan(0)));
    }

    @Test
    public void shouldReturnPositiveNumberWhenComparingToOlderMinorVersion() {
        // Given
        Version version = new Version("2.1.0");
        Version olderVersion = new Version("2.0.0");
        // When
        int comparisonResult = version.compareTo(olderVersion);
        // Then
        assertThat(comparisonResult, is(greaterThan(0)));
    }

    @Test
    public void shouldReturnPositiveNumberWhenComparingToOlderMajorVersion() {
        // Given
        Version version = new Version("3.0.0");
        Version olderVersion = new Version("2.0.0");
        // When
        int comparisonResult = version.compareTo(olderVersion);
        // Then
        assertThat(comparisonResult, is(greaterThan(0)));
    }

    @Test
    public void shouldReturnZeroWhenComparingToEqualVersion() {
        // Given
        Version version = new Version("1.0.0");
        Version sameVersion = new Version("1.0.0");
        // When
        int comparisonResult = version.compareTo(sameVersion);
        // Then
        assertThat(comparisonResult, is(equalTo(0)));
    }

    @Test
    public void shouldReturnNegativeNumberWhenComparingToNewerPatchVersion() {
        // Given
        Version version = new Version("1.0.0");
        Version newerVersion = new Version("1.0.1");
        // When
        int comparisonResult = version.compareTo(newerVersion);
        // Then
        assertThat(comparisonResult, is(lessThan(0)));
    }

    @Test
    public void shouldReturnNegativeNumberWhenComparingToNewerMinorVersion() {
        // Given
        Version version = new Version("1.0.0");
        Version newerVersion = new Version("1.1.0");
        // When
        int comparisonResult = version.compareTo(newerVersion);
        // Then
        assertThat(comparisonResult, is(lessThan(0)));
    }

    @Test
    public void shouldReturnNegativeNumberWhenComparingToNewerMajorVersion() {
        // Given
        Version version = new Version("1.0.0");
        Version newerVersion = new Version("2.0.0");
        // When
        int comparisonResult = version.compareTo(newerVersion);
        // Then
        assertThat(comparisonResult, is(lessThan(0)));
    }

    @Test
    public void shouldMatchExactVersion() {
        // Given
        Version version = new Version("1.2.3");
        String rangeVersion = "1.2.3";
        // When
        boolean matchResult = version.matches(rangeVersion);
        // Then
        assertThat(matchResult, is(equalTo(true)));
    }

    @Test
    public void shouldMatchMinorWildcardedVersionWithEqualMajorVersion() {
        // Given
        Version version = new Version("1.0.0");
        String rangeVersion = "1.*";
        // When
        boolean matchResult = version.matches(rangeVersion);
        // Then
        assertThat(matchResult, is(equalTo(true)));
    }

    @Test
    public void shouldMatchMinorWildcardedVersionWithEqualMajorVersionAndDifferentMinorVersion() {
        // Given
        Version version = new Version("1.5.0");
        String rangeVersion = "1.*";
        // When
        boolean matchResult = version.matches(rangeVersion);
        // Then
        assertThat(matchResult, is(equalTo(true)));
    }

    @Test
    public void shouldMatchMinorWildcardedVersionWithEqualMajorVersionAndDifferentPatchVersion() {
        // Given
        Version version = new Version("1.0.4");
        String rangeVersion = "1.*";
        // When
        boolean matchResult = version.matches(rangeVersion);
        // Then
        assertThat(matchResult, is(equalTo(true)));
    }

    @Test
    public void shouldMatchMinorWildcardedVersionWithEqualMajorVersionAndDifferentMinorAndPatchVersion() {
        // Given
        Version version = new Version("1.9.3");
        String rangeVersion = "1.*";
        // When
        boolean matchResult = version.matches(rangeVersion);
        // Then
        assertThat(matchResult, is(equalTo(true)));
    }

    @Test
    public void shouldNotMatchMinorWildcardedVersionWithGreaterMajorVersion() {
        // Given
        Version version = new Version("2.0.0");
        String rangeVersion = "1.*";
        // When
        boolean matchResult = version.matches(rangeVersion);
        // Then
        assertThat(matchResult, is(equalTo(false)));
    }

    @Test
    public void shouldReturnVersionAsString() {
        // Given
        Version version = new Version("1.2.3");
        // When
        String string = version.toString();
        // Then
        assertThat(string, is(equalTo("1.2.3")));
    }
}
