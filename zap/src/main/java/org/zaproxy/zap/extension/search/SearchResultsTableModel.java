/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.search;

import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.HistoryReferenceEventPublisher;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.eventBus.EventConsumer;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.utils.StringUIUtils;
import org.zaproxy.zap.view.table.AbstractCustomColumnHistoryReferencesTableModel;
import org.zaproxy.zap.view.table.AbstractHistoryReferencesTableEntry;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableEntry;

@SuppressWarnings("serial")
public class SearchResultsTableModel
        extends AbstractCustomColumnHistoryReferencesTableModel<
                SearchResultsTableModel.SearchResultTableEntry>
        implements EventConsumer {

    private static final long serialVersionUID = 5732679524771190690L;

    private static final String MATCH_COLUMN_NAME =
            Constant.messages.getString("search.results.table.header.match");

    private static final Column[] COLUMNS =
            new Column[] {
                Column.HREF_ID,
                Column.REQUEST_TIMESTAMP,
                Column.RESPONSE_TIMESTAMP,
                Column.METHOD,
                Column.URL,
                Column.STATUS_CODE,
                Column.STATUS_REASON,
                Column.RTT,
                Column.SIZE_REQUEST_HEADER,
                Column.SIZE_REQUEST_BODY,
                Column.SIZE_RESPONSE_HEADER,
                Column.SIZE_RESPONSE_BODY,
                Column.HIGHEST_ALERT,
                Column.NOTE,
                Column.TAGS,
                Column.CUSTOM
            };

    private List<SearchResultTableEntry> results = new ArrayList<>();

    public SearchResultsTableModel() {
        super(COLUMNS);
        ZAP.getEventBus()
                .registerConsumer(
                        this,
                        HistoryReferenceEventPublisher.getPublisher().getPublisherName(),
                        HistoryReferenceEventPublisher.EVENT_REMOVED,
                        HistoryReferenceEventPublisher.EVENT_TAG_ADDED,
                        HistoryReferenceEventPublisher.EVENT_TAG_REMOVED,
                        HistoryReferenceEventPublisher.EVENT_TAGS_SET);
    }

    public void addSearchResult(SearchResult sr) {
        SearchResultTableEntry previousResult = null;
        if (results.size() > 1) {
            previousResult = results.get(results.size() - 1);
        }
        results.add(createSearchResultTableEntry(sr, previousResult));
        fireTableRowsInserted(results.size() - 1, results.size() - 1);
    }

    private static SearchResultTableEntry createSearchResultTableEntry(
            SearchResult sr, SearchResultTableEntry previousResult) {
        HistoryReference hRef = sr.getMessage().getHistoryRef();
        String stringFound = null;
        if (previousResult != null) {
            if (previousResult.getStringFound().equals(sr.getStringFound())) {
                stringFound = previousResult.getStringFound();
            }
        }

        if (stringFound == null) {
            stringFound = sr.getStringFound();
        }

        return new SearchResultTableEntry(hRef, stringFound, sr);
    }

    @Override
    public void addEntry(SearchResultTableEntry entry) {}

    @Override
    public void refreshEntryRow(int historyReferenceId) {}

    @Override
    public void removeEntry(int historyReferenceId) {
        if (results.removeIf(r -> r.getHistoryId() == historyReferenceId)) {
            this.fireTableDataChanged();
        }
    }

    @Override
    public SearchResultTableEntry getEntry(int rowIndex) {
        return results.get(rowIndex);
    }

    @Override
    public SearchResultTableEntry getEntryWithHistoryId(int historyReferenceId) {
        return null;
    }

    @Override
    public int getEntryRowIndex(int historyReferenceId) {
        return -1;
    }

    @Override
    public void clear() {
        results = new ArrayList<>();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return results.size();
    }

    @Override
    protected Class<?> getColumnClass(Column column) {
        return AbstractHistoryReferencesTableEntry.getColumnClass(column);
    }

    @Override
    protected Object getPrototypeValue(Column column) {
        return AbstractHistoryReferencesTableEntry.getPrototypeValue(column);
    }

    @Override
    protected Object getCustomValueAt(SearchResultTableEntry entry, int columnIndex) {
        return entry.getStringFound();
    }

    @Override
    protected String getCustomColumnName(int columnIndex) {
        return MATCH_COLUMN_NAME;
    }

    @Override
    protected Class<String> getCustomColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    protected Object getCustomPrototypeValue(int columnIndex) {
        return "A match with some long text";
    }

    public static class SearchResultTableEntry extends DefaultHistoryReferencesTableEntry {

        private static final int MAX_CHARS_FOUND_STRING = 150;

        private final String stringFound;

        private final SearchResult sr;

        public SearchResultTableEntry(
                HistoryReference historyReference, String stringFound, SearchResult sr) {
            super(historyReference, COLUMNS);

            String temp;
            if (stringFound.length() > MAX_CHARS_FOUND_STRING) {
                temp = stringFound.substring(0, MAX_CHARS_FOUND_STRING) + "...";
            } else {
                temp = stringFound;
            }
            this.stringFound = StringUIUtils.replaceWithVisibleWhiteSpaceChars(temp);
            this.sr = new HistoryReferenceSearchResult(sr, stringFound);
        }

        public String getStringFound() {
            return stringFound;
        }

        public SearchResult getSearchResult() {
            return sr;
        }

        private class HistoryReferenceSearchResult extends SearchResult {

            private List<CachedSearchMatch> matches;
            private CachedSearchMatch lastMatch = null;

            public HistoryReferenceSearchResult(SearchResult sr, String stringFound) {
                super(null, sr.getType(), sr.getRegEx(), stringFound);

                matches = new ArrayList<>(1);
                matches.add(new CachedSearchMatch(sr.getFirstMatch(null, null)));
            }

            @Override
            public HttpMessage getMessage() {
                try {
                    return getHistoryReference().getHttpMessage();
                } catch (DatabaseException | HttpMalformedHeaderException e) {

                    return null;
                }
            }

            @Override
            public SearchMatch getFirstMatch(HttpPanel reqPanel, HttpPanel resPanel) {
                if (matches.size() > 0) {
                    lastMatch = matches.get(0);
                    return lastMatch;
                }
                return null;
            }

            @Override
            public SearchMatch getLastMatch(HttpPanel reqPanel, HttpPanel resPanel) {
                if (matches.size() > 0) {
                    lastMatch = matches.get(matches.size() - 1);
                    return lastMatch;
                }
                return null;
            }

            @Override
            public SearchMatch getNextMatch() {
                if (lastMatch != null) {
                    int i = matches.indexOf(lastMatch);
                    if (i >= 0 && i < matches.size() - 1) {
                        lastMatch = matches.get(i + 1);
                        return lastMatch;
                    }
                }
                return null;
            }

            @Override
            public SearchMatch getPrevMatch() {
                if (lastMatch != null) {
                    int i = matches.indexOf(lastMatch);
                    if (i >= 1) {
                        lastMatch = matches.get(i - 1);
                        return lastMatch;
                    }
                }
                return null;
            }
        }

        private class CachedSearchMatch extends SearchMatch {

            public CachedSearchMatch(SearchMatch searchMatch) {
                super(
                        null,
                        searchMatch.getLocation(),
                        searchMatch.getStart(),
                        searchMatch.getEnd());
            }

            @Override
            public HttpMessage getMessage() {
                try {
                    return getHistoryReference().getHttpMessage();
                } catch (DatabaseException | HttpMalformedHeaderException e) {

                    return null;
                }
            }
        }
    }

    @Override
    public void eventReceived(Event event) {
        String idStr =
                event.getParameters()
                        .get(HistoryReferenceEventPublisher.FIELD_HISTORY_REFERENCE_ID);
        int historyId = Integer.parseInt(idStr);
        switch (event.getEventType()) {
            case HistoryReferenceEventPublisher.EVENT_REMOVED:
                this.removeEntry(historyId);
                break;
            case HistoryReferenceEventPublisher.EVENT_TAG_ADDED:
            case HistoryReferenceEventPublisher.EVENT_TAG_REMOVED:
            case HistoryReferenceEventPublisher.EVENT_TAGS_SET:
                boolean rowsUpdated = false;
                for (int i = 0; i < results.size(); i++) {
                    SearchResultTableEntry entry = results.get(i);
                    if (entry.getHistoryId() == historyId) {
                        entry.refreshCachedValues();
                        rowsUpdated = true;
                    }
                }

                if (rowsUpdated) {
                    fireTableRowsUpdated(0, results.size() - 1);
                }
                break;
        }
    }
}
