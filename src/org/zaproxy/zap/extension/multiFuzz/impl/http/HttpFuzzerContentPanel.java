/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.awt.EventQueue;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.multiFuzz.FuzzResult;
import org.zaproxy.zap.extension.multiFuzz.FuzzerContentPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.search.ExtensionSearch;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.extension.search.SearchResult;
import org.zaproxy.zap.utils.Pair;

public class HttpFuzzerContentPanel implements FuzzerContentPanel {

	public static final String PANEL_NAME = "HttpFuzzerContentPanel";
	
    private static final Logger logger = Logger.getLogger(HttpFuzzerContentPanel.class);

    private JTable fuzzResultTable;
    private HttpFuzzTableModel resultsModel;

    private HttpPanel requestPanel;
    private HttpPanel responsePanel;

    /**
     * A list containing all the {@code HistoryReference} IDs that are added to
     * the instance variable {@code resultsModel}. Used to delete the
     * {@code HistoryReference}s from the database when no longer needed.
     */
    private List<Integer> historyReferencesToDelete = new ArrayList<>();

    private boolean showTokenRequests;

    enum State {
        SUCCESSFUL,
        REFLECTED,
        ERROR,
        ANTI_CRSF_TOKEN,
    }

    public HttpFuzzerContentPanel() {
        super();

        showTokenRequests = false;
    }

    public void setDisplayPanel(HttpPanel requestPanel, HttpPanel responsePanel) {
        this.requestPanel = requestPanel;
        this.responsePanel = responsePanel;
    }

    public JTable getFuzzResultTable() {
        if (fuzzResultTable == null) {
            fuzzResultTable = new JTable(resultsModel);
            fuzzResultTable.setDoubleBuffered(true);
            fuzzResultTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            fuzzResultTable.setName(PANEL_NAME);
            fuzzResultTable.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
            fuzzResultTable.setDefaultRenderer(Pair.class, new IconTableCellRenderer());

            int[] widths = {
                    10, 25, 550, 30, 85, 55, 40, 70
             };
            for (int i = 0, count = widths.length; i < count; i++) {
                TableColumn column = fuzzResultTable.getColumnModel().getColumn(i);
                column.setPreferredWidth(widths[i]);
            }

            fuzzResultTable.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        // Select list item on right click
                        JTable table = (JTable) e.getSource();
                        int row = table.rowAtPoint(e.getPoint());

                        if (! table.isRowSelected(row)) {
                            table.changeSelection(row, 0, false, false);
                        }
                        View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });

            fuzzResultTable.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {

                @Override
                public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        if (fuzzResultTable.getSelectedRowCount() == 0) {
                            return;
                        }

                        final int row = fuzzResultTable.getSelectedRow();
                        final HistoryReference historyReference = resultsModel.getHistoryReferenceAtRow(row);

                        try {
                            displayMessage(historyReference.getHttpMessage());
                        } catch (HttpMalformedHeaderException ex) {
                            logger.error(ex.getMessage(), ex);
                        } catch (SQLException ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }
                }
            });

            resetFuzzResultTable();
        }
        return fuzzResultTable;
    }

    private void resetFuzzResultTable() {
        if (historyReferencesToDelete.size() != 0) {
            try {
                Database.getSingleton().getTableHistory().delete(historyReferencesToDelete);
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
        resultsModel = new HttpFuzzTableModel();
        historyReferencesToDelete = new ArrayList<>();
        getFuzzResultTable().setModel(resultsModel);
    }

    private void addFuzzResult(final State state, final HttpMessage  msg) {

        if (EventQueue.isDispatchThread()) {
            addFuzzResultToView(state, msg);
            return;
        }
        try {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    addFuzzResultToView(state, msg);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void addFuzzResultToView(final State state, final HttpMessage msg) {
        try {
            HistoryReference historyReference = new HistoryReference(Model.getSingleton().getSession(), HistoryReference.TYPE_TEMPORARY, msg);
            this.historyReferencesToDelete.add(Integer.valueOf(historyReference.getHistoryId()));

            resultsModel.addHistoryReference(state, historyReference);
        } catch (HttpMalformedHeaderException e) {
            logger.error(e.getMessage(), e);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void displayMessage(HttpMessage msg) {
        try {
            if (msg.getRequestHeader().isEmpty()) {
                requestPanel.clearView(true);
            } else {
                requestPanel.setMessage(msg);
            }

            if (msg.getResponseHeader().isEmpty()) {
                responsePanel.clearView(false);
            } else {
                responsePanel.setMessage(msg, true);
            }

            // The fuzz payload is recorded in the note

            String note = msg.getNote();
            if (note != null && note.length() > 0) {
                int startIndex = msg.getResponseBody().toString().indexOf(note);
                if (startIndex >= 0) {
                    // Found the exact pattern - highlight it
                    SearchMatch sm = new SearchMatch(msg, SearchMatch.Location.RESPONSE_BODY, startIndex, startIndex + note.length());
                    responsePanel.setTabFocus();
                    responsePanel.requestFocus();
                    responsePanel.highlightBody(sm);
                }
            }


        } catch (Exception e) {
            logger.error("Failed to access message ", e);
        }
    }

    public List<SearchResult> searchResults(Pattern pattern, boolean inverse) {
        List<SearchResult> results = new ArrayList<>();

        if (resultsModel == null) {
            return results;
        }

        Iterator<Pair<State, HistoryReference>> it = resultsModel.getHistoryReferences().iterator();
        Matcher matcher;
        while (it.hasNext()) {
            HistoryReference historyReference = it.next().second;
            try {
                HttpMessage msg = historyReference.getHttpMessage();
                if (msg != null) {
                	if (msg.getRequestHeader() != null) {
	                    matcher = pattern.matcher(msg.getResponseHeader().toString());
	                    if (inverse) {
	                        if (! matcher.find()) {
	                            results.add(new SearchResult(msg, ExtensionSearch.Type.Fuzz, pattern.toString(), ""));
	                        }
	                    } else {
	                        while (matcher.find()) {
	                            results.add(
	                                    new SearchResult(ExtensionSearch.Type.Fuzz, pattern.toString(), matcher.group(),
	                                            new SearchMatch(msg, SearchMatch.Location.RESPONSE_HEAD, matcher.start(), matcher.end())));
	                        }
	                    }
                	}
                	if (msg.getRequestBody() != null) {
	                    matcher = pattern.matcher(msg.getResponseBody().toString());
	                    if (inverse) {
	                        if (! matcher.find()) {
	                            results.add(new SearchResult(msg, ExtensionSearch.Type.Fuzz, pattern.toString(), ""));
	                        }
	                    } else {
	                        while (matcher.find()) {
	                            results.add(
	                                    new SearchResult(ExtensionSearch.Type.Fuzz, pattern.toString(), matcher.group(),
	                                            new SearchMatch(msg, SearchMatch.Location.RESPONSE_BODY, matcher.start(), matcher.end())));
	                        }
	                    }
                	}
                }
            } catch (HttpMalformedHeaderException e) {
                logger.error(e.getMessage(), e);
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return results;
    }

    @Override
    public JComponent getComponent() {
        return getFuzzResultTable();
    }

    @Override
    public void addFuzzResult(FuzzResult fuzzResult) {
        HttpFuzzResult httpFuzzResult = (HttpFuzzResult)fuzzResult;
        if (showTokenRequests) {

            for (HttpMessage tokenMsg : httpFuzzResult.getTokenRequestMessages()) {
                addFuzzResult(State.ANTI_CRSF_TOKEN, tokenMsg);
            }
        }
        addFuzzResult(convertState(fuzzResult.getState()), (HttpMessage)fuzzResult.getMessage());
    }

    private State convertState(FuzzResult.State fuzzState) {
        State state;
        switch (fuzzState) {
        case REFLECTED:
            state = State.REFLECTED;
            break;
        case ERROR:
            state = State.ERROR;
            break;
        case SUCCESSFUL:
        default:
            state = State.SUCCESSFUL;
            break;
        }
        return state;
    }

    @Override
    public void clear() {
        resetFuzzResultTable();
    }

    protected void setShowTokenRequests(boolean showTokenRequests) {
        this.showTokenRequests = showTokenRequests;
    }

}
