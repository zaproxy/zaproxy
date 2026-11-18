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
import static org.mockito.Mockito.verify;

import java.util.Collections;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.view.TableModelTestUtils;
import org.zaproxy.zap.view.TableModelTestUtils.TestTableModelListener;
import org.zaproxy.zap.view.table.HistoryReferencesTableModel.Column;

/** Unit test for ADV_NOTES editing behaviour in {@link HistoryTableModel}. */
class HistoryTableModelUnitTest {

    private HistoryTableModel model;

    @BeforeAll
    static void beforeAll() {
        Constant.messages = mock(I18N.class);
    }

    @AfterAll
    static void afterAll() {
        Constant.messages = null;
    }

    @BeforeEach
    void setUp() throws Exception {
        model = new HistoryTableModel();
        model.addHistoryReference(createHistoryReference(1, ""));
    }

    @Test
    void isCellEditableReturnsTrueOnlyForAdvNotesColumn() {
        // Given
        int advNotesCol = model.getColumnIndex(Column.ADV_NOTES);
        int otherCol = model.getColumnIndex(Column.URL);
        // Then
        assertThat(model.isCellEditable(0, advNotesCol), is(true));
        assertThat(model.isCellEditable(0, otherCol), is(false));
    }

    @Test
    void setValueAtCallsSetNoteOnHistoryReference() throws Exception {
        // Given
        HistoryReference href = createHistoryReference(2, "original");
        model.addHistoryReference(href);
        int row = model.getEntryRowIndex(2);
        int col = model.getColumnIndex(Column.ADV_NOTES);
        // When
        model.setValueAt("updated note", row, col);
        // Then
        verify(href).setNote("updated note");
    }

    @Test
    void setValueAtWithNullPassesEmptyStringToSetNote() throws Exception {
        // Given
        HistoryReference href = createHistoryReference(3, "original");
        model.addHistoryReference(href);
        int row = model.getEntryRowIndex(3);
        int col = model.getColumnIndex(Column.ADV_NOTES);
        // When
        model.setValueAt(null, row, col);
        // Then
        verify(href).setNote("");
    }

    @Test
    void setValueAtFiresTableCellUpdatedEvent() throws Exception {
        // Given
        HistoryReference href = createHistoryReference(4, "");
        model.addHistoryReference(href);
        int row = model.getEntryRowIndex(4);
        int col = model.getColumnIndex(Column.ADV_NOTES);
        TestTableModelListener listener = TableModelTestUtils.createTestTableModelListener();
        model.addTableModelListener(listener);
        // When
        model.setValueAt("new note", row, col);
        // Then
        assertThat(listener.isRowUpdated(row), is(true));
    }

    @Test
    void setValueAtIgnoresNonAdvNotesColumns() throws Exception {
        // Given
        HistoryReference href = createHistoryReference(5, "");
        model.addHistoryReference(href);
        int row = model.getEntryRowIndex(5);
        int col = model.getColumnIndex(Column.URL);
        TestTableModelListener listener = TableModelTestUtils.createTestTableModelListener();
        model.addTableModelListener(listener);
        // When
        model.setValueAt("ignored", row, col);
        // Then
        assertThat(listener.getNumberOfEvents(), is(0));
    }

    private static HistoryReference createHistoryReference(int id, String note) throws Exception {
        HistoryReference href = mock(HistoryReference.class);
        given(href.getHistoryId()).willReturn(id);
        given(href.getHistoryType()).willReturn(HistoryReference.TYPE_PROXIED);
        given(href.getMethod()).willReturn("GET");
        given(href.getStatusCode()).willReturn(200);
        given(href.getReason()).willReturn("OK");
        given(href.getHighestAlert()).willReturn(-1);
        given(href.hasNote()).willReturn(!note.isEmpty());
        given(href.getNote()).willReturn(note);
        given(href.getTags()).willReturn(Collections.emptyList());

        URI uri = mock(URI.class);
        given(uri.toString()).willReturn("http://example.com/");
        given(uri.getRawHost()).willReturn("example.com".toCharArray());
        given(uri.getEscapedPathQuery()).willReturn("/");
        given(href.getURI()).willReturn(uri);

        return href;
    }
}
