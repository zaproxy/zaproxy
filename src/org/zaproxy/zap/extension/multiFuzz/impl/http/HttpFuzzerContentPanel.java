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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTreeTable;
import org.parosproxy.paros.Constant;
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

	private static final Logger logger = Logger
			.getLogger(HttpFuzzerContentPanel.class);
	private static final String STATE_ERROR_LABEL = Constant.messages
			.getString("fuzz.http.table.field.state.error");
	private static final String STATE_REFLECTED_LABEL = Constant.messages
			.getString("fuzz.http.table.field.state.reflected");
	private static final String STATE_ANTI_CSRF_TOKEN_REQUEST_LABEL = Constant.messages
			.getString("fuzz.http.table.field.state.antiCsrfTokenRequest");
	private static final String STATE_SUCCESSFUL_LABEL = Constant.messages
			.getString("fuzz.http.table.field.state.successful");
	private JXTreeTable fuzzResultTable;
	private HttpFuzzTableModel resultsModel;

	private HttpPanel requestPanel;
	private HttpPanel responsePanel;

	private HttpResultGroupingPopupFuzzMenu grouping;
	private HttpResultRenamePopupFuzzMenu rename;
	private HttpResultAllIncludePopupFuzzMenu allIn;

	public HttpFuzzerContentPanel() {
		super();
		View.getSingleton().getPopupList().add(getResultRename());
		View.getSingleton().getPopupList().add(getResultGrouping());
		View.getSingleton().getPopupList().add(getAllInclude());
	}

	private JMenuItem getAllInclude() {
		if (allIn == null) {
			allIn = new HttpResultAllIncludePopupFuzzMenu();
			allIn.addActionListener(new ActionListener() {
				boolean flag = false;

				@Override
				public void actionPerformed(ActionEvent click) {
					for (HttpFuzzRecord r : getResultsModel().getEntries()) {
						getResultsModel().setValueAt(flag, r, 9);
					}
					flag = !flag;
					if (flag) {
						allIn.setText(Constant.messages
								.getString("fuzz.result.allinclude"));
					} else {
						allIn.setText(Constant.messages
								.getString("fuzz.result.allexclude"));
					}
				}
			});
		}
		return allIn;
	}

	private HttpResultGroupingPopupFuzzMenu getResultGrouping() {
		if (grouping == null) {
			grouping = new HttpResultGroupingPopupFuzzMenu(this);
		}
		return grouping;
	}

	public HttpFuzzTableModel getResultsModel() {
		if (resultsModel == null) {
			resultsModel = new HttpFuzzTableModel();
		}
		return this.resultsModel;
	}

	private HttpResultRenamePopupFuzzMenu getResultRename() {
		if (rename == null) {
			rename = new HttpResultRenamePopupFuzzMenu();
			rename.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent click) {
					String name = JOptionPane.showInputDialog(Constant.messages
							.getString("fuzz.result.rename.new"));
					getResultsModel().setValueAt(name,
							getEntry(rename.getLastEntry()), 0);
				}
			});
		}
		return rename;
	}

	public void setDisplayPanel(HttpPanel requestPanel, HttpPanel responsePanel) {
		this.requestPanel = requestPanel;
		this.responsePanel = responsePanel;
	}

	public JXTreeTable getFuzzResultTable() {
		if (fuzzResultTable == null) {
			resetFuzzResultTable();
			fuzzResultTable = new JXTreeTable(getResultsModel());
			fuzzResultTable.setName("HttpFuzzResultTable");
			fuzzResultTable.setDoubleBuffered(true);
			fuzzResultTable
					.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			fuzzResultTable.setFont(new java.awt.Font("Default",
					java.awt.Font.PLAIN, 12));
			fuzzResultTable.setDefaultRenderer(Pair.class,
					new IconTableCellRenderer());

			int[] widths = { 10, 25, 550, 30, 85, 55, 40, 70 };
			for (int i = 0, count = widths.length; i < count; i++) {
				TableColumn column = fuzzResultTable.getColumnModel()
						.getColumn(i);
				column.setPreferredWidth(widths[i]);
			}
			fuzzResultTable.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					showPopupMenuIfTriggered(e);
				}

				@Override
				public void mouseReleased(java.awt.event.MouseEvent e) {
					showPopupMenuIfTriggered(e);
				}

				private void showPopupMenuIfTriggered(
						java.awt.event.MouseEvent e) {
					if (e.isPopupTrigger()
							&& SwingUtilities.isRightMouseButton(e)) {
						// Select list item on right click
						JTable table = (JTable) e.getSource();
						int row = table.rowAtPoint(e.getPoint());

						if (!table.isRowSelected(row)) {
							table.changeSelection(row, 0, false, false);
						}
						View.getSingleton().getPopupMenu()
								.show(e.getComponent(), e.getX(), e.getY());
					}
				}

			});

			fuzzResultTable.getSelectionModel().addListSelectionListener(
					new javax.swing.event.ListSelectionListener() {

						@Override
						public void valueChanged(
								javax.swing.event.ListSelectionEvent e) {
							if (!e.getValueIsAdjusting()) {
								if (fuzzResultTable.getSelectedRowCount() == 0) {
									return;
								}
								final int row = fuzzResultTable
										.getSelectedRow();
								if (getEntry(row) instanceof HttpFuzzRequestRecord) {
									final HistoryReference historyReference = ((HttpFuzzRequestRecord) getEntry(row))
											.getHistory();
									try {
										displayMessage(historyReference
												.getHttpMessage());
									} catch (HttpMalformedHeaderException
											| SQLException ex) {
										logger.error(ex.getMessage(), ex);
									}
								}
							}
						}
					});
			fuzzResultTable.getTableHeader().addMouseListener(
					new MouseListener() {
						int sortedOn = -1;

						@Override
						public void mouseReleased(MouseEvent arg0) {
						}

						@Override
						public void mousePressed(MouseEvent arg0) {
						}

						@Override
						public void mouseExited(MouseEvent arg0) {
						}

						@Override
						public void mouseEntered(MouseEvent arg0) {
						}

						@Override
						public void mouseClicked(MouseEvent e) {
							int index = fuzzResultTable.columnAtPoint(e
									.getPoint());
							List<HttpFuzzRecord> list = getResultsModel()
									.getEntries();
							if (list.size() == 0) {
								return;
							}
							HttpFuzzRecordComparator comp = new HttpFuzzRecordComparator();
							comp.setFeature(index);
							if (index == sortedOn) {
								Collections.sort(list, comp);
								Collections.reverse(list);
								sortedOn = -1;
							} else {
								Collections.sort(list, comp);
								sortedOn = index;
							}
							fuzzResultTable.updateUI();
						}
					});
			fuzzResultTable.setRootVisible(false);
			fuzzResultTable.setVisible(true);
		}
		return fuzzResultTable;
	}

	public HttpFuzzRecord getEntry(int row) {
		int c = 0;
		for (int i = 0; i < getResultsModel().getEntries().size(); i++) {
			if (c != row) {
				if (getResultsModel().getEntries().get(i) instanceof HttpFuzzRequestRecord) {
					c++;
				} else if (getResultsModel().getEntries().get(i) instanceof HttpFuzzRecordGroup) {
					c++;
					if (!getFuzzResultTable().isCollapsed(i)) {
						HttpFuzzRecordGroup g = (HttpFuzzRecordGroup) getResultsModel()
								.getEntries().get(i);
						if (row < c + g.getMembers().size()) {
							return g.getMembers().get(row - c);
						}
						c += g.getMembers().size();
					}
				}
			} else {
				return getResultsModel().getEntries().get(i);
			}
		}
		return null;
	}

	public List<HttpFuzzRecord> getEntries(int[] indices) {
		LinkedList<HttpFuzzRecord> res = new LinkedList<>();
		for (int i : indices) {
			res.add(getEntry(i));
		}
		return res;
	}

	private void resetFuzzResultTable() {
		resultsModel.removeAll();
	}

	private void addFuzzResult(final String name, final String custom,
			final HttpFuzzRequestRecord.State state,
			final ArrayList<String> pay, final HttpMessage msg) {

		if (EventQueue.isDispatchThread()) {
			addFuzzResultToView(name, custom, state, pay, msg);
			return;
		}
		try {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					addFuzzResultToView(name, custom, state, pay, msg);
				}
			});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void addFuzzResultToView(final String name, final String custom,
			final HttpFuzzRequestRecord.State state,
			final ArrayList<String> pay, final HttpMessage msg) {
		try {
			HistoryReference historyReference = new HistoryReference(Model
					.getSingleton().getSession(),
					HistoryReference.TYPE_TEMPORARY, msg);

			resultsModel.addFuzzRecord(new HttpFuzzRequestRecord(name, custom,
					state, pay, historyReference));
			fuzzResultTable.updateUI();
			fuzzResultTable.repaint();
		} catch (HttpMalformedHeaderException | SQLException e) {
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
					SearchMatch sm = new SearchMatch(msg,
							SearchMatch.Location.RESPONSE_BODY, startIndex,
							startIndex + note.length());
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

		Iterator<HttpFuzzRequestRecord> it = resultsModel
				.getHistoryReferences().iterator();
		Matcher matcher;
		while (it.hasNext()) {
			HistoryReference historyReference = it.next().getHistory();
			try {
				HttpMessage msg = historyReference.getHttpMessage();
				if (msg != null) {
					if (msg.getRequestHeader() != null) {
						matcher = pattern.matcher(msg.getResponseHeader()
								.toString());
						if (inverse) {
							if (!matcher.find()) {
								results.add(new SearchResult(msg,
										ExtensionSearch.Type.Fuzz, pattern
												.toString(), ""));
							}
						} else {
							while (matcher.find()) {
								results.add(new SearchResult(
										ExtensionSearch.Type.Fuzz,
										pattern.toString(),
										matcher.group(),
										new SearchMatch(
												msg,
												SearchMatch.Location.RESPONSE_HEAD,
												matcher.start(), matcher.end())));
							}
						}
					}
					if (msg.getRequestBody() != null) {
						matcher = pattern.matcher(msg.getResponseBody()
								.toString());
						if (inverse) {
							if (!matcher.find()) {
								results.add(new SearchResult(msg,
										ExtensionSearch.Type.Fuzz, pattern
												.toString(), ""));
							}
						} else {
							while (matcher.find()) {
								results.add(new SearchResult(
										ExtensionSearch.Type.Fuzz,
										pattern.toString(),
										matcher.group(),
										new SearchMatch(
												msg,
												SearchMatch.Location.RESPONSE_BODY,
												matcher.start(), matcher.end())));
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
	public void addFuzzResult(FuzzResult<?, ?> fuzzResult) {
		HttpFuzzResult httpFuzzResult = (HttpFuzzResult) fuzzResult;
		if (httpFuzzResult.getTokenRequestMessages() != null) {
			for (int i = 0; i < httpFuzzResult.getTokenRequestMessages().size(); i++) {
				addFuzzResult(fuzzResult.getName() + "." + i,
						fuzzResult.getCustom(),
						HttpFuzzRequestRecord.State.ANTI_CRSF_TOKEN,
						new ArrayList<String>(), httpFuzzResult
								.getTokenRequestMessages().get(i));
			}
		}
		addFuzzResult(fuzzResult.getName(), fuzzResult.getCustom(),
				convertState(fuzzResult.getState()), fuzzResult.getPayloads(),
				(HttpMessage) fuzzResult.getMessage());
	}

	private HttpFuzzRequestRecord.State convertState(String fuzzState) {
		HttpFuzzRequestRecord.State state;
		switch (fuzzState) {
		case HttpFuzzResult.STATE_ANTICSRF:
			state = HttpFuzzRequestRecord.State.ANTI_CRSF_TOKEN;
			break;
		case HttpFuzzResult.STATE_REFLECTED:
			state = HttpFuzzRequestRecord.State.REFLECTED;
			break;
		case FuzzResult.STATE_ERROR:
			state = HttpFuzzRequestRecord.State.ERROR;
			break;
		case FuzzResult.STATE_CUSTOM:
			state = HttpFuzzRequestRecord.State.CUSTOM;
			break;
		case FuzzResult.STATE_SUCCESSFUL:
		default:
			state = HttpFuzzRequestRecord.State.SUCCESSFUL;
			break;
		}
		return state;
	}

	@Override
	public void clear() {
		resetFuzzResultTable();
	}

	@Override
	public void showDiagrams() {
		HttpFuzzResultDialog dia = new HttpFuzzResultDialog(getResultsModel());
		dia.setVisible(true);
	}

	private static class HttpFuzzRecordComparator implements
			Comparator<HttpFuzzRecord> {
		private int feature = 0;

		public void setFeature(int f) {
			feature = f;
		}

		@Override
		public int compare(HttpFuzzRecord fst, HttpFuzzRecord snd) {
			switch (feature) {
			case 0:
				return fst.getName().compareTo(snd.getName());
			case 1:
				return fst.getMethod().compareTo(snd.getMethod());
			case 2:
				return fst.getURI().compareTo(snd.getURI());
			case 3:
				return fst.getRTT() - snd.getRTT();
			case 4:
				return fst.getSize() - snd.getSize();
			case 5:
				return fst.getState() - snd.getState();
			case 6:
				return fst.getReason().compareTo(snd.getReason());
			case 7:
				return fst.getResult().first.compareTo(snd.getResult().first);
			case 8:
				return 0;
			case 9:
				return fst.isIncluded().compareTo(snd.isIncluded());
			default:
				return 0;
			}
		}
	}

	@Override
	public void saveRecords(File f) {
		try (CSVPrinter printer = new CSVPrinter(new FileWriter(f), CSVFormat.DEFAULT);){
			printer.print(Constant.messages
					.getString("fuzz.http.csv.head.name"));
			printer.print(Constant.messages
					.getString("fuzz.http.csv.head.custom"));
			printer.print(Constant.messages
					.getString("fuzz.http.csv.head.result"));
			printer.print(Constant.messages
					.getString("fuzz.http.csv.head.payloadSize"));
			printer.print(Constant.messages
					.getString("fuzz.http.csv.head.payload"));
			printer.print(Constant.messages
					.getString("fuzz.http.csv.head.reqHead"));
			printer.print(Constant.messages
					.getString("fuzz.http.csv.head.reqBody"));
			printer.print(Constant.messages
					.getString("fuzz.http.csv.head.respHead"));
			printer.print(Constant.messages
					.getString("fuzz.http.csv.head.respBody"));
			printer.print(Constant.messages
					.getString("fuzz.http.csv.head.respTime"));
			printer.println();
			for (HttpFuzzRecord r : getResultsModel().getEntries()) {
				if (r instanceof HttpFuzzRequestRecord) {
					printer.print(r.getName());
					printer.print(r.getCustom());
					printer.print(r.getResult().first);
					printer.print(r.getPayloads().size());
					printer.print(r.getPayloads());
					HttpMessage m = ((HttpFuzzRequestRecord) r).getHistory()
							.getHttpMessage();
					printer.print(m.getRequestHeader().toString());
					printer.print(m.getRequestBody().toString());
					printer.print(m.getResponseHeader().toString());
					printer.print(m.getResponseBody().toString());
					printer.print(m.getTimeElapsedMillis());
					printer.println();
				}
			}
			printer.flush();
		} catch (IOException | SQLException e) {
			logger.debug(e.getMessage());
			JOptionPane.showMessageDialog(View.getSingleton().getMainFrame(), Constant.messages
					.getString("fuzz.http.csv.writeError"));
		}
	}

	@Override
	public void loadRecords(File f) {
		try (CSVParser parser = new CSVParser(new FileReader(f), CSVFormat.DEFAULT);){
			boolean header = true;
			for (CSVRecord rec : parser) {
				if (!header) {
					String name = rec.get(0);
					String custom = rec.get(1);
					HttpFuzzRequestRecord.State s;
					if (rec.get(2).equals(STATE_SUCCESSFUL_LABEL)) {
						s = HttpFuzzRequestRecord.State.SUCCESSFUL;
					} else if (rec.get(2).equals(STATE_REFLECTED_LABEL)) {
						s = HttpFuzzRequestRecord.State.REFLECTED;
					} else if (rec.get(2).equals(
							STATE_ANTI_CSRF_TOKEN_REQUEST_LABEL)) {
						s = HttpFuzzRequestRecord.State.ANTI_CRSF_TOKEN;
					} else if (rec.get(2).equals(STATE_ERROR_LABEL)) {
						s = HttpFuzzRequestRecord.State.ERROR;
					} else {
						s = HttpFuzzRequestRecord.State.CUSTOM;
					}

					int l = Integer.parseInt(rec.get(3));
					ArrayList<String> pay = new ArrayList<>();
					if (l == 0) {
						l++;
					} else {
						for (int i = 4; i < l + 4; i++) {
							pay.add(rec.get(i).substring(1,
									rec.get(i).length() - 1));
						}
					}
					HttpMessage m = new HttpMessage();
					m.setRequestHeader(rec.get(l + 4));
					m.setRequestBody(rec.get(l + 5));
					m.setResponseHeader(rec.get(l + 6));
					m.setResponseBody(rec.get(l + 7));
					m.setTimeElapsedMillis(Integer.parseInt(rec.get(l + 8)));
					addFuzzResult(name, custom, s, pay, m);
				} else {
					header = false;
				}
			}
		} catch (IOException e) {
			logger.debug(e.getMessage());
			JOptionPane.showMessageDialog(View.getSingleton().getMainFrame(), Constant.messages
					.getString("fuzz.http.csv.readError"));
		}
	}
}
