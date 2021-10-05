/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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
package org.zaproxy.zap.extension.pscan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.extension.pscan.scanner.RegexAutoTagScanner;

/** Unit test for {@link PassiveScannerList}. */
class PassiveScannerListUnitTest {

    private PassiveScannerList psl;

    @BeforeEach
    void setUp() throws Exception {
        psl = new PassiveScannerList();
    }

    @Test
    void shouldHaveNoScannersByDefault() {
        assertThat(psl.list(), is(empty()));
    }

    @Test
    void shouldAddPassiveScanner() {
        // Given
        PassiveScanner scanner = mock(PassiveScanner.class);
        // When
        boolean scannerAdded = psl.add(scanner);
        // Then
        assertThat(psl.list(), contains(scanner));
        assertThat(scannerAdded, is(equalTo(true)));
    }

    @Test
    void shouldIgnorePassiveScannerWithSameName() {
        // Given
        PassiveScanner scanner1 = mock(PassiveScanner.class);
        when(scanner1.getName()).thenReturn("PassiveScanner 1");
        PassiveScanner otherScannerWithSameName = mock(PassiveScanner.class);
        when(otherScannerWithSameName.getName()).thenReturn("PassiveScanner 1");
        // When
        psl.add(scanner1);
        boolean otherScannerAdded = psl.add(otherScannerWithSameName);
        // Then
        assertThat(psl.list(), contains(scanner1));
        assertThat(otherScannerAdded, is(equalTo(false)));
    }

    @Test
    void shouldRemovePassiveScanner() {
        // Given
        PassiveScanner scanner1 = mock(PassiveScanner.class);
        psl.add(scanner1);
        PassiveScanner scanner2 = mock(TestPassiveScanner.class);
        when(scanner2.getName()).thenReturn("TestPassiveScanner");
        psl.add(scanner2);
        // When
        PassiveScanner removedScanner = psl.removeScanner(scanner2.getClass().getName());
        // Then
        assertThat(psl.list(), contains(scanner1));
        assertThat(removedScanner, is(sameInstance(scanner2)));
    }

    @Test
    void shouldNotRemovePassiveScannerNotAdded() {
        // Given
        PassiveScanner scanner = mock(PassiveScanner.class);
        // When
        PassiveScanner removedScanner = psl.removeScanner(scanner.getClass().getName());
        // Then
        assertThat(removedScanner, is(nullValue()));
    }

    @Test
    void shouldSetAutoTagScanners() {
        // Given
        List<RegexAutoTagScanner> scanners = new ArrayList<>();
        RegexAutoTagScanner scanner1 = mock(RegexAutoTagScanner.class);
        when(scanner1.getName()).thenReturn("RegexAutoTagScanner 1");
        scanners.add(scanner1);
        RegexAutoTagScanner scanner2 = mock(RegexAutoTagScanner.class);
        when(scanner2.getName()).thenReturn("RegexAutoTagScanner 2");
        scanners.add(scanner2);
        // When
        psl.setAutoTagScanners(scanners);
        // Then
        assertThat(psl.list(), contains(scanner1, scanner2));
    }

    @Test
    void shouldRemovePreviousAutoTagScannersButNotPassiveScanners() {
        // Given
        RegexAutoTagScanner scanner1 = mock(RegexAutoTagScanner.class);
        when(scanner1.getName()).thenReturn("RegexAutoTagScanner 1");
        psl.add(scanner1);
        PassiveScanner scanner2 = mock(PassiveScanner.class);
        when(scanner2.getName()).thenReturn("PassiveScanner 1");
        psl.add(scanner2);
        List<RegexAutoTagScanner> scanners = new ArrayList<>();
        RegexAutoTagScanner scanner3 = mock(RegexAutoTagScanner.class);
        when(scanner3.getName()).thenReturn("RegexAutoTagScanner 2");
        scanners.add(scanner3);
        // When
        psl.setAutoTagScanners(scanners);
        // Then
        assertThat(psl.list(), contains(scanner2, scanner3));
    }

    @Test
    void shouldIgnoreAutoTagScannerWithSameName() {
        // Given
        List<RegexAutoTagScanner> scanners = new ArrayList<>();
        RegexAutoTagScanner scanner1 = mock(RegexAutoTagScanner.class);
        when(scanner1.getName()).thenReturn("RegexAutoTagScanner 1");
        scanners.add(scanner1);
        RegexAutoTagScanner otherScannerWithSameName = mock(RegexAutoTagScanner.class);
        when(otherScannerWithSameName.getName()).thenReturn("RegexAutoTagScanner 1");
        scanners.add(otherScannerWithSameName);
        // When
        psl.setAutoTagScanners(scanners);
        // Then
        assertThat(psl.list(), contains(scanner1));
    }

    @Test
    void shouldAllowToChangeListWhileIterating() {
        // Given
        PassiveScanner scanner1 = mock(PassiveScanner.class);
        psl.add(scanner1);
        RegexAutoTagScanner scanner2 = mock(RegexAutoTagScanner.class);
        when(scanner2.getName()).thenReturn("RegexAutoTagScanner");
        psl.add(scanner2);
        // When / Then
        assertDoesNotThrow(
                () ->
                        psl.list()
                                .forEach(
                                        e -> {
                                            psl.removeScanner(e.getClass().getName());
                                            psl.add(e);
                                        }));
        assertThat(psl.list(), contains(scanner1, scanner2));
    }

    @Test
    void shouldAllowToChangeListWhileIteratingAfterSettingAutoTagScanners() {
        // Given
        PassiveScanner scanner1 = mock(PassiveScanner.class);
        psl.add(scanner1);
        RegexAutoTagScanner scanner2 = mock(RegexAutoTagScanner.class);
        when(scanner2.getName()).thenReturn("RegexAutoTagScanner");
        List<RegexAutoTagScanner> autoTagScanners = new ArrayList<>();
        autoTagScanners.add(scanner2);
        psl.setAutoTagScanners(autoTagScanners);
        // When / Then
        assertDoesNotThrow(
                () ->
                        psl.list()
                                .forEach(
                                        e -> {
                                            psl.removeScanner(e.getClass().getName());
                                            psl.add(e);
                                        }));
        assertThat(psl.list(), contains(scanner1, scanner2));
    }

    /** An interface to mock {@code PassiveScanner}s with different class name. */
    private static interface TestPassiveScanner extends PassiveScanner {
        // Nothing to do.
    }
}
