/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
package org.zaproxy.zap.extension.stdmenus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link PopupMenuItemIncludeSiteInContext}. */
class PopupMenuItemIncludeSiteInContextUnitTest {

    private PopupMenuItemIncludeSiteInContext popup;

    @BeforeAll
    static void setupAll() {
        Constant.messages = mock(I18N.class);
    }

    @AfterAll
    static void cleanup() {
        Constant.messages = null;
    }

    @BeforeEach
    void setup() {
        popup = new PopupMenuItemIncludeSiteInContext();
    }

    @Test
    void shouldTraverseParentsUntilHostNodeForRegex() throws DatabaseException {
        // Given
        SiteNode sitesNode = mock(SiteNode.class);
        SiteNode hostNodeNode = mock(SiteNode.class);
        // Make "root" to return early on regex creation.
        given(hostNodeNode.isRoot()).willReturn(true);
        given(hostNodeNode.getParent()).willReturn(sitesNode);
        SiteNode leafHostNode = mock(SiteNode.class);
        given(leafHostNode.getParent()).willReturn(hostNodeNode);
        // When
        String regex = popup.createRegex(leafHostNode);
        // Then
        assertThat(regex, is(equalTo(".*")));
    }
}
