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

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit test for {@link HirshbergMatcher}
 */
public class HirshbergMatcherUnitTest {

    private static final String EMPTY_STRING = "";
    private static final String NON_EMPTY_STRING = "Non Empty String";

    @Test(expected = NullPointerException.class)
    public void shouldFailToGetLCSIfStringAIsNull() {
        // Given
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        matcher.getLCS(null, NON_EMPTY_STRING);
        // Then = NullPointerException.class
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailToGetLCSIfStringBIsNull() {
        // Given
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        matcher.getLCS(NON_EMPTY_STRING, null);
        // Then = NullPointerException.class
    }

    @Test
    public void shouldReturnEmptyStringWhenGettingLCSIfStringAIsEmpty() {
        // Given
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        String lcs = matcher.getLCS(EMPTY_STRING, NON_EMPTY_STRING);
        // Then
        assertThat(lcs, is(equalTo(EMPTY_STRING)));
    }

    @Test
    public void shouldReturnEmptyStringWhenGettingLCSIfStringBIsEmpty() {
        // Given
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        String lcs = matcher.getLCS(NON_EMPTY_STRING, EMPTY_STRING);
        // Then
        assertThat(lcs, is(equalTo(EMPTY_STRING)));
    }

    @Test
    public void shouldReturnEmptyStringWhenGettingLCSOfEmptyStrings() {
        // Given
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        String lcs = matcher.getLCS(EMPTY_STRING, EMPTY_STRING);
        // Then
        assertThat(lcs, is(equalTo(EMPTY_STRING)));
    }

    @Test
    public void shouldReturnLCSWhenGettingLCSOfCommonStrings() {
        // Given
        String stringA = "GTCGTTCGGAATGCCGTTGCTCTGTAAA";
        String stringB = "ACCGGTCGAGTGCGCGGAAGCCGGCCGAA";
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        String lcs = matcher.getLCS(stringA, stringB);
        // Then
        assertThat(lcs, is(equalTo("GTCGTCGGAAGCCGGCCGAA")));
    }

    @Test
    public void shouldReturnSameLCSWhenSwitchingPositionsOfCommonStrings() {
        // Given
        String stringA = "human";
        String stringB = "chimpanzee";
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        String lcs1 = matcher.getLCS(stringA, stringB);
        String lcs2 = matcher.getLCS(stringB, stringA);
        // Then
        assertThat(lcs1, is(equalTo(lcs2)));
        assertThat(lcs2, is(equalTo("hman")));
    }

    @Test
    public void shouldReturnLCSEqualToStringsIfStringsAreEqual() {
        // Given
        String stringA = "ABC";
        String stringB = stringA;
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        String lcs = matcher.getLCS(stringA, stringB);
        // Then
        assertThat(lcs, is(equalTo(stringB)));
    }

    @Test
    public void shouldReturnEmptyStringWhenGettingLCSOfUncommonStrings() {
        // Given
        String stringA = "man";
        String stringB = "pig";
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        String lcs = matcher.getLCS(stringA, stringB);
        // Then
        assertThat(lcs, is(equalTo(EMPTY_STRING)));
    }

    @Test
    public void shouldReturnMinMatchRatioIfStringAIsNull() {
        // Given
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        double ratio = matcher.getMatchRatio(null, NON_EMPTY_STRING);
        // Then
        assertThat(ratio, is(equalTo(HirshbergMatcher.MIN_RATIO)));
    }

    @Test
    public void shouldReturnMinMatchRatioIfStringBIsNull() {
        // Given
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        double ratio = matcher.getMatchRatio(NON_EMPTY_STRING, null);
        // Then
        assertThat(ratio, is(equalTo(HirshbergMatcher.MIN_RATIO)));
    }

    @Test
    public void shouldReturnMaxMatchRatioIfBothStringsAreNull() {
        // Given
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        double ratio = matcher.getMatchRatio(null, null);
        // Then
        assertThat(ratio, is(equalTo(HirshbergMatcher.MAX_RATIO)));
    }

    @Test
    public void shouldReturnMinMatchRatioIfStringAIsEmpty() {
        // Given
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        double ratio = matcher.getMatchRatio(EMPTY_STRING, NON_EMPTY_STRING);
        // Then
        assertThat(ratio, is(equalTo(HirshbergMatcher.MIN_RATIO)));
    }

    @Test
    public void shouldReturnMinMatchRatioIfStringBIsEmpty() {
        // Given
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        double ratio = matcher.getMatchRatio(NON_EMPTY_STRING, EMPTY_STRING);
        // Then
        assertThat(ratio, is(equalTo(HirshbergMatcher.MIN_RATIO)));
    }

    @Test
    public void shouldReturnMaxMatchRatioIfBothStringsAreEmpty() {
        // Given
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        double ratio = matcher.getMatchRatio(EMPTY_STRING, EMPTY_STRING);
        // Then
        assertThat(ratio, is(equalTo(HirshbergMatcher.MAX_RATIO)));
    }

    @Test
    public void shouldReturnCorrectMatchRatioForCommonStrings() {
        // Given
        String stringA = "capital";
        String stringB = "apple";
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        double ratio = matcher.getMatchRatio(stringA, stringB);
        // Then
        assertThat(ratio, is(closeTo(0.4285, 0.001)));
    }

    @Test
    public void shouldReturnMaxMatchRatioForEqualStrings() {
        // Given
        String stringA = "ca";
        String stringB = stringA;
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        double ratio = matcher.getMatchRatio(stringA, stringB);
        // Then
        assertThat(ratio, is(equalTo(HirshbergMatcher.MAX_RATIO)));
    }

    @Test
    public void shouldReturnZeroMatchRatioForUncommonStrings() {
        // Given
        String stringA = "ABC";
        String stringB = "XYZ";
        HirshbergMatcher matcher = new HirshbergMatcher();
        // When
        double ratio = matcher.getMatchRatio(stringA, stringB);
        // Then
        assertThat(ratio, is(equalTo(0.0)));
    }
}
