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
package org.zaproxy.zap.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.parosproxy.paros.model.SiteNode;

/** Unit test for {@link StructuralSiteNode}. */
class StructuralSiteNodeUnitTest {

    @Test
    void shouldNotAllowToConstructWithNullNode() {
        // Given
        SiteNode node = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> new StructuralSiteNode(node));
    }

    @Test
    void shouldConstructWithNonNullNode() {
        // Given
        SiteNode node = mock(SiteNode.class);
        // When / Then
        assertDoesNotThrow(() -> new StructuralSiteNode(node));
    }
}
