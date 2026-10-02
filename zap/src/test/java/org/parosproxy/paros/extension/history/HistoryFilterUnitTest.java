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
package org.parosproxy.paros.extension.history;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.quality.Strictness;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.utils.I18N;

/** Unit test for note-filter behaviour in {@link HistoryFilter}. */
class HistoryFilterUnitTest {

    private HistoryFilter filter;

    @BeforeAll
    static void beforeAll() {
        I18N i18n = mock(I18N.class, withSettings().strictness(Strictness.LENIENT));
        given(i18n.getString("history.filter.notes.ignore")).willReturn("Ignore");
        given(i18n.getString("history.filter.notes.present")).willReturn("Present");
        given(i18n.getString("history.filter.notes.absent")).willReturn("Absent");
        Constant.messages = i18n;
    }

    @AfterAll
    static void afterAll() {
        Constant.messages = null;
    }

    @BeforeEach
    void setUp() {
        filter = new HistoryFilter();
    }

    @Test
    void noteFilterIgnoredWhenNotSet() throws Exception {
        // Given
        HistoryReference refWithNote = refWithNote(true);
        HistoryReference refWithoutNote = refWithNote(false);
        // Then
        assertThat(filter.matches(refWithNote), is(true));
        assertThat(filter.matches(refWithoutNote), is(true));
    }

    @Test
    void noteFilterIgnoreMatchesBoth() throws Exception {
        // Given
        filter.setNote(HistoryFilter.NOTES_IGNORE);
        // Then
        assertThat(filter.matches(refWithNote(true)), is(true));
        assertThat(filter.matches(refWithNote(false)), is(true));
    }

    @Test
    void noteFilterPresentMatchesOnlyRefsWithNote() throws Exception {
        // Given
        filter.setNote(HistoryFilter.NOTES_PRESENT);
        // Then
        assertThat(filter.matches(refWithNote(true)), is(true));
        assertThat(filter.matches(refWithNote(false)), is(false));
    }

    @Test
    void noteFilterAbsentMatchesOnlyRefsWithoutNote() throws Exception {
        // Given
        filter.setNote(HistoryFilter.NOTES_ABSENT);
        // Then
        assertThat(filter.matches(refWithNote(false)), is(true));
        assertThat(filter.matches(refWithNote(true)), is(false));
    }

    private static HistoryReference refWithNote(boolean hasNote) throws Exception {
        HistoryReference ref = mock(HistoryReference.class);
        given(ref.getNote()).willReturn(hasNote ? "a note" : "");
        given(ref.getURI()).willReturn(new URI("http://example.com/", true));
        return ref;
    }
}
