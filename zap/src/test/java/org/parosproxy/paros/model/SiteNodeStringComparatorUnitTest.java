/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2023 The ZAP Development Team
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
package org.parosproxy.paros.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit test for {@link SiteNodeStringComparator}. */
class SiteNodeStringComparatorUnitTest {

    private SiteNodeStringComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new SiteNodeStringComparator();
    }

    @Test
    void shouldHaveSameOrderIfSameMethodAndName() {
        // Given
        SiteNode sn1 = createSiteNode("GET", "path");
        SiteNode sn2 = createSiteNode("GET", "path");
        // When
        int result = comparator.compare(sn1, sn2);
        // Then
        assertThat(result, is(equalTo(0)));
    }

    @Test
    void shouldDifferentiateByCaseInMethod() {
        // Given
        SiteNode sn1 = createSiteNode("GET", "path");
        SiteNode sn2 = createSiteNode("get", "path");
        // When
        int result = comparator.compare(sn1, sn2);
        // Then
        assertThat(result, is(lessThan(0)));
    }

    @Test
    void shouldDifferentiateByCaseInName() {
        // Given
        SiteNode sn1 = createSiteNode("GET", "PATH");
        SiteNode sn2 = createSiteNode("GET", "path");
        // When
        int result = comparator.compare(sn1, sn2);
        // Then
        assertThat(result, is(lessThan(0)));
    }

    private static SiteNode createSiteNode(String method, String path) {
        return new SiteNode(null, -1, method + ":" + path);
    }
}
