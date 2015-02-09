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
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.view.table.AbstractCustomColumnHistoryReferencesTableModel;
import org.zaproxy.zap.view.table.AbstractHistoryReferencesTableEntry;

public class SearchResultsTableModel extends
        AbstractCustomColumnHistoryReferencesTableModel<SearchResultsTableModel.SearchResultTableEntry> {

    private static final long serialVersionUID = 5732679524771190690L;

    private static final String MATCH_COLUMN_NAME = Constant.messages.getString("search.results.table.header.match");

    private List<SearchResultTableEntry> results = new ArrayList<>();

    public SearchResultsTableModel() {
        super(new Column[] { Column.HREF_ID, Column.METHOD, Column.URL, Column.CUSTOM });
    }

    public void addSearchResult(SearchResult sr) {
        SearchResultTableEntry previousResult = null;
        if (results.size() > 1) {
            previousResult = results.get(results.size() - 1);
        }
        results.add(createSearchResultTableEntry(sr, previousResult));
        fireTableRowsInserted(results.size() - 1, results.size() - 1);
    }

    private static SearchResultTableEntry createSearchResultTableEntry(SearchResult sr, SearchResultTableEntry previousResult) {
        HistoryReference hRef = sr.getMessage().getHistoryRef();
        Integer historyId = null;
        String uri = null;
        String stringFound = null;
        if (previousResult != null) {
            Integer previousId = previousResult.getHistoryId();
            if (previousId.intValue() == hRef.getHistoryId()) {
                historyId = previousId;
            }
            if (previousResult.getStringFound().equals(sr.getStringFound())) {
                stringFound = previousResult.getStringFound();
            }
            uri = sr.getMessage().getRequestHeader().getURI().toString();
            if (previousResult.getUri().equals(uri)) {
                uri = previousResult.getUri();
            }
        }

        if (historyId == null) {
            historyId = Integer.valueOf(hRef.getHistoryId());
        }
        if (stringFound == null) {
            stringFound = sr.getStringFound();
        }

        if (uri == null) {
            uri = sr.getMessage().getRequestHeader().getURI().toString();
        }

        return new SearchResultTableEntry(hRef, historyId, hRef.getMethod(), uri, stringFound, sr);
    }

    @Override
    public void addEntry(SearchResultTableEntry entry) {
    }

    @Override
    public void refreshEntryRow(int historyReferenceId) {
    }

    @Override
    public void removeEntry(int historyReferenceId) {
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

    public static class SearchResultTableEntry extends AbstractHistoryReferencesTableEntry {

        private static final int MAX_CHARS_FOUND_STRING = 150;

        private final Integer historyId;
        private final String method;
        private final String uri;
        private final String stringFound;

        private final SearchResult sr;

        public SearchResultTableEntry(
                HistoryReference historyReference,
                Integer historyId,
                String method,
                String uri,
                String stringFound,
                SearchResult sr) {
            super(historyReference);

            this.historyId = historyId;
            this.method = method;
            this.uri = uri;

            if (stringFound.length() > MAX_CHARS_FOUND_STRING) {
                this.stringFound = stringFound.substring(0, MAX_CHARS_FOUND_STRING) + "...";
            } else {
                this.stringFound = stringFound;
            }
            this.sr = new HistoryReferenceSearchResult(sr, stringFound);
        }

        @Override
        public Integer getHistoryId() {
            return historyId;
        }

        @Override
        public String getMethod() {
            return method;
        }

        @Override
        public String getUri() {
            return uri;
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
                super(null, searchMatch.getLocation(), searchMatch.getStart(), searchMatch.getEnd());
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
}
