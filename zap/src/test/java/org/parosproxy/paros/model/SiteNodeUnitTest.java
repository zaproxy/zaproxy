/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SiteNodeUnitTest {

    private SiteNode rootNode;
    private SiteMap siteMap;

    @BeforeEach
    void setup() throws Exception {
        rootNode = new SiteNode(null, -1, "Sites");
    }

    @Test
    void shouldReturnNameOfRootChildNodeWithScheme() {
        // Given
        SiteNode siteNode = new SiteNode(siteMap, 0, "http://example.com");
        siteNode.add(new SiteNode(siteMap, 0, "GET:path"));
        rootNode.add(siteNode);
        // When
        String name = siteNode.getName();
        // Then
        assertThat(name, is(equalTo("http://example.com")));
    }

    @Test
    void shouldReturnNameOfRootChildNodeWithSchemeEvenIfLeaf() {
        // Given
        SiteNode siteNode = new SiteNode(siteMap, 0, "http://example.com");
        rootNode.add(siteNode);
        // When
        String name = siteNode.getName();
        // Then
        assertThat(name, is(equalTo("http://example.com")));
    }

    @Test
    void shouldReturnNameOfLeafChildNodeWithoutMethod() {
        // Given
        SiteNode siteNode = new SiteNode(siteMap, 0, "http://example.com");
        SiteNode leafNode = new SiteNode(siteMap, 0, "GET:leaf");
        siteNode.add(leafNode);
        rootNode.add(siteNode);
        // When
        String name = leafNode.getName();
        // Then
        assertThat(name, is(equalTo("leaf")));
    }

    @Test
    void shouldReturnNameOfBranchChildNode() {
        // Given
        SiteNode siteNode = new SiteNode(siteMap, 0, "http://example.com");
        SiteNode branchNode = new SiteNode(siteMap, 0, "branch");
        SiteNode leafNode = new SiteNode(siteMap, 0, "GET:leaf");
        branchNode.add(leafNode);
        siteNode.add(branchNode);
        rootNode.add(siteNode);
        // When
        String name = branchNode.getName();
        // Then
        assertThat(name, is(equalTo("branch")));
    }
}
