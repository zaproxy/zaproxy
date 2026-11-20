/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.utils.I18N;

class PopupExcludeMenusUnitTest {

    private Model model;
    private Session session;
    private SiteNode siteNode;

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
        model = mock(Model.class);
        session = mock(Session.class);
        given(model.getSession()).willReturn(session);
        Model.setSingletonForTesting(model);
        siteNode = mock(SiteNode.class);
    }

    @Test
    void shouldEnableScanMenuWhenNotExcluded() {
        // Given
        given(session.isExcludedFromScan(siteNode)).willReturn(false);
        PopupExcludeFromScanMenu menu = new PopupExcludeFromScanMenu();
        // When / Then
        assertThat(menu.isButtonEnabledForSiteNode(siteNode), is(true));
    }

    @Test
    void shouldDisableScanMenuWhenExcluded() {
        // Given
        given(session.isExcludedFromScan(siteNode)).willReturn(true);
        PopupExcludeFromScanMenu menu = new PopupExcludeFromScanMenu();
        // When / Then
        assertThat(menu.isButtonEnabledForSiteNode(siteNode), is(false));
    }

    @Test
    void shouldEnableSpiderMenuWhenNotExcluded() {
        // Given
        given(session.isExcludedFromSpider(siteNode)).willReturn(false);
        PopupExcludeFromSpiderMenu menu = new PopupExcludeFromSpiderMenu();
        // When / Then
        assertThat(menu.isButtonEnabledForSiteNode(siteNode), is(true));
    }

    @Test
    void shouldDisableSpiderMenuWhenExcluded() {
        // Given
        given(session.isExcludedFromSpider(siteNode)).willReturn(true);
        PopupExcludeFromSpiderMenu menu = new PopupExcludeFromSpiderMenu();
        // When / Then
        assertThat(menu.isButtonEnabledForSiteNode(siteNode), is(false));
    }

    @Test
    void shouldEnableProxyMenuWhenNotExcluded() {
        // Given
        given(session.isExcludedFromProxy(siteNode)).willReturn(false);
        PopupExcludeFromProxyMenu menu = new PopupExcludeFromProxyMenu();
        // When / Then
        assertThat(menu.isButtonEnabledForSiteNode(siteNode), is(true));
    }

    @Test
    void shouldDisableProxyMenuWhenExcluded() {
        // Given
        given(session.isExcludedFromProxy(siteNode)).willReturn(true);
        PopupExcludeFromProxyMenu menu = new PopupExcludeFromProxyMenu();
        // When / Then
        assertThat(menu.isButtonEnabledForSiteNode(siteNode), is(false));
    }
}
