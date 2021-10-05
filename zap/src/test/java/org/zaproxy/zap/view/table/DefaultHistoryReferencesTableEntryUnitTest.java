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
package org.zaproxy.zap.view.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.view.table.HistoryReferencesTableModel.Column;

/** Unit test for {@link DefaultHistoryReferencesTableEntry}. */
class DefaultHistoryReferencesTableEntryUnitTest {

    @BeforeAll
    static void beforeAll() {
        Constant.messages = mock(I18N.class);
    }

    @AfterAll
    static void afterAll() {
        Constant.messages = null;
    }

    @Test
    void shouldHaveHostNameForUriWithJustAuthority() throws Exception {
        // Given
        HistoryReference historyReference = mock(HistoryReference.class);
        given(historyReference.getMethod()).willReturn("CONNECT");
        given(historyReference.getURI()).willReturn(URI.fromAuthority("example.com:443"));
        Column[] columns = {Column.HOSTNAME};
        // When
        DefaultHistoryReferencesTableEntry entry =
                new DefaultHistoryReferencesTableEntry(historyReference, columns);
        // Then
        assertThat(entry.getHostName(), is(equalTo("example.com")));
    }

    @Test
    void shouldHaveHostNameForUriWithHost() {
        // Given
        HistoryReference historyReference = mock(HistoryReference.class);
        given(historyReference.getURI()).willReturn(createUri("https://example.com/"));
        Column[] columns = {Column.HOSTNAME};
        // When
        DefaultHistoryReferencesTableEntry entry =
                new DefaultHistoryReferencesTableEntry(historyReference, columns);
        // Then
        assertThat(entry.getHostName(), is(equalTo("example.com")));
    }

    private static URI createUri(String uri) {
        try {
            return new URI(uri, true);
        } catch (URIException | NullPointerException e) {
            throw new RuntimeException("Failed to create URI from: " + uri, e);
        }
    }
}
