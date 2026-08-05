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
package org.parosproxy.paros.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.network.HttpResponseBody;

/** Unit test for {@link HttpMessageCachedData} note-related behaviour. */
class HttpMessageCachedDataUnitTest {

    private static HttpMessageCachedData cachedDataWithNote(String note) {
        HttpRequestHeader reqHeader = mock(HttpRequestHeader.class);
        given(reqHeader.toString()).willReturn("");

        HttpRequestBody reqBody = mock(HttpRequestBody.class);
        given(reqBody.toString()).willReturn("");

        HttpResponseHeader resHeader = mock(HttpResponseHeader.class);
        given(resHeader.toString()).willReturn("");

        HttpResponseBody resBody = mock(HttpResponseBody.class);

        HttpMessage msg = mock(HttpMessage.class);
        given(msg.getRequestHeader()).willReturn(reqHeader);
        given(msg.getRequestBody()).willReturn(reqBody);
        given(msg.getResponseHeader()).willReturn(resHeader);
        given(msg.getResponseBody()).willReturn(resBody);
        given(msg.getNote()).willReturn(note);

        return new HttpMessageCachedData(msg);
    }

    @Test
    void hasNoteIsFalseWhenMessageNoteIsNull() {
        // Given / When
        HttpMessageCachedData data = cachedDataWithNote(null);
        // Then
        assertThat(data.hasNote(), is(false));
    }

    @Test
    void hasNoteIsFalseWhenMessageNoteIsEmpty() {
        // Given / When
        HttpMessageCachedData data = cachedDataWithNote("");
        // Then
        assertThat(data.hasNote(), is(false));
    }

    @Test
    void hasNoteIsTrueWhenMessageHasNote() {
        // Given / When
        HttpMessageCachedData data = cachedDataWithNote("my note");
        // Then
        assertThat(data.hasNote(), is(true));
    }

    @Test
    void setNoteFalseUpdatesFlag() {
        // Given
        HttpMessageCachedData data = cachedDataWithNote("existing");
        // When
        data.setNote(false);
        // Then
        assertThat(data.hasNote(), is(false));
    }

    @Test
    void setNoteTrueUpdatesFlag() {
        // Given
        HttpMessageCachedData data = cachedDataWithNote(null);
        // When
        data.setNote(true);
        // Then
        assertThat(data.hasNote(), is(true));
    }
}
