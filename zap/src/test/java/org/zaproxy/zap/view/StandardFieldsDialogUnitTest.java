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
package org.zaproxy.zap.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.model.StructuralSiteNode;
import org.zaproxy.zap.model.Target;

/** Unit test for {@link StandardFieldsDialog}. */
class StandardFieldsDialogUnitTest {

    private static String getTargetText(Target target) throws Exception {
        Method method = StandardFieldsDialog.class.getDeclaredMethod("getTargetText", Target.class);
        method.setAccessible(true);
        return (String) method.invoke(null, target);
    }

    @Test
    void shouldUseSiteNodeMessageByDefault() throws Exception {
        // Given
        SiteNode node = mockLeafNode("https://example.com/path");
        Target target = new Target(new StructuralSiteNode(node));
        // When
        String text = getTargetText(target);
        // Then
        assertThat(text, is("https://example.com/path"));
    }

    @Test
    void shouldUseSelectedMessageInsteadOfSiteNodeMessage() throws Exception {
        // Given the site node holds one message but a different one was selected
        SiteNode node = mockLeafNode("https://example.com/?aa=cc");
        HistoryReference selected = mockHistoryReference("https://example.com/?aa=bb");
        Target target = new Target(new StructuralSiteNode(node, selected));
        // When
        String text = getTargetText(target);
        // Then
        assertThat(text, is("https://example.com/?aa=bb"));
    }

    private static SiteNode mockLeafNode(String uri) throws Exception {
        HistoryReference href = mockHistoryReference(uri);
        SiteNode node = mock(SiteNode.class);
        given(node.isLeaf()).willReturn(true);
        given(node.getHistoryReference()).willReturn(href);
        return node;
    }

    private static HistoryReference mockHistoryReference(String uri) throws Exception {
        HistoryReference href = mock(HistoryReference.class);
        given(href.getURI()).willReturn(new URI(uri, true));
        return href;
    }
}
