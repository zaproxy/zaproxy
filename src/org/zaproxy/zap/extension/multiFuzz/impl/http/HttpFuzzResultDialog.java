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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTreeTable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.Align;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.multiFuzz.FuzzComponent;
import org.zaproxy.zap.extension.multiFuzz.FuzzResultDialog;
import org.zaproxy.zap.utils.Pair;

public class HttpFuzzResultDialog extends FuzzResultDialog {

	private static final long serialVersionUID = -235856563071555190L;
	
	private static final Logger logger = Logger
			.getLogger(HttpFuzzResultDialog.class);
	public static final String DIALOG_NAME = "HttpFuzzResultDialog";
	private JXTreeTable table;
	private HttpFuzzTableModel model;
	private HttpFuzzComponent comp;
	private JTabbedPane diagrams;
	private ChartPanel status;
	private ChartPanel result;
	private ChartPanel size;
	private ChartPanel rtt;
	private DefaultPieDataset stateSet = new DefaultPieDataset();
	private DefaultPieDataset resultSet = new DefaultPieDataset();
	private DefaultCategoryDataset sizeSet = new DefaultCategoryDataset();
	private DefaultCategoryDataset rttSet = new DefaultCategoryDataset();

	public HttpFuzzResultDialog(HttpFuzzTableModel m) {
		super();
		model = m;
		table = new JXTreeTable();
		table.setTreeTableModel(model);
		table.updateUI();
		updateValues();
		redrawDiagrams();
	}

	@Override
	public JXTreeTable getTable() {
		if (table == null) {
			if (model == null) {
				model = new HttpFuzzTableModel();
			}
			table = new JXTreeTable(model);
			table.setDoubleBuffered(true);
			table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			table.setName("HttpFuzzResultTable");
			table.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
			table.setDefaultRenderer(Pair.class, new IconTableCellRenderer());

			int[] widths = { 10, 25, 550, 30, 85, 55, 40, 70 };
			for (int i = 0, count = widths.length; i < count; i++) {
				TableColumn column = table.getColumnModel().getColumn(i);
				column.setPreferredWidth(widths[i]);
			}
			table.addMouseListener(new java.awt.event.MouseAdapter() {
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
					if (e.isPopupTrigger()) {
						if (e.isPopupTrigger()) {
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
				}

			});
			table.getSelectionModel().addListSelectionListener(
					new javax.swing.event.ListSelectionListener() {

						@Override
						public void valueChanged(
								javax.swing.event.ListSelectionEvent e) {
							if (!e.getValueIsAdjusting()) {
								if (table.getSelectedRowCount() == 0) {
									return;
								}
								final int row = table.getSelectedRow();
								if (getEntry(row) instanceof HttpFuzzRequestRecord) {
									final HistoryReference historyReference = ((HttpFuzzRequestRecord) getEntry(row))
											.getHistory();
									try {
										getMessageInspection().setMessage(
												historyReference
														.getHttpMessage());
									} catch (HttpMalformedHeaderException
											| SQLException ex) {
										logger.error(ex.getMessage(), ex);
									}
								}
								updateValues();
								redrawDiagrams();
							}
						}
					});
		}
		table.getTableHeader().addMouseListener(new MouseListener() {
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
				int index = table.columnAtPoint(e.getPoint());
				List<HttpFuzzRecord> list = model.getEntries();
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
				table.updateUI();
			}
		});
		table.setRootVisible(false);
		table.setVisible(true);
		return table;
	}

	public HttpFuzzRecord getEntry(int row) {
		int c = 0;
		for (int i = 0; i < model.getEntries().size(); i++) {
			if (c != row) {
				if (model.getEntries().get(i) instanceof HttpFuzzRequestRecord) {
					c++;
				} else if (model.getEntries().get(i) instanceof HttpFuzzRecordGroup) {
					c++;
					if (!getTable().isCollapsed(i)) {
						HttpFuzzRecordGroup g = (HttpFuzzRecordGroup) model
								.getEntries().get(i);
						if (row < c + g.getMembers().size()) {
							return g.getMembers().get(row - c);
						}
						c += g.getMembers().size();
					}
				}
			} else {
				return model.getEntries().get(i);
			}
		}
		return null;
	}

	@Override
	public FuzzComponent getMessageInspection() {
		if (comp == null) {
			int row = getTable().getSelectedRow();
			if (row == -1) {
				comp = new HttpFuzzComponent(	);
			} else {
				try {
					comp = new HttpFuzzComponent(
							((HttpFuzzRequestRecord) getEntry(row))
									.getHistory().getHttpMessage());
				} catch (HttpMalformedHeaderException | SQLException e) {
					logger.error(e.getMessage());
				}
			}
		}
		return comp;
	}

	@Override
	public JTabbedPane getDiagrams() {
		if (diagrams == null) {
			diagrams = new JTabbedPane();
			diagrams.add(
					Constant.messages.getString("fuzz.result.dia.results"),
					result);
			diagrams.add(Constant.messages.getString("fuzz.result.dia.states"),
					status);
			diagrams.add(Constant.messages.getString("fuzz.result.dia.size"),
					size);
			diagrams.add(Constant.messages.getString("fuzz.result.dia.rtt"),
					rtt);
		}
		return diagrams;
	}

	private void updateValues() {
		stateSet = new DefaultPieDataset();
		resultSet = new DefaultPieDataset();
		sizeSet = new DefaultCategoryDataset();
		rttSet = new DefaultCategoryDataset();
		HashMap<String, Integer> statesMap = new HashMap<>();
		HashMap<String, Integer> resultMap = new HashMap<>();
		for (HttpFuzzRecord r : model.getEntries()) {
			if (r.isIncluded() && r instanceof HttpFuzzRequestRecord) {
				if (statesMap.containsKey(r.getReason())) {
					statesMap.put(r.getReason(),
							statesMap.get(r.getReason()) + 1);
				} else {
					statesMap.put(r.getReason(), 1);
				}
				if (resultMap.containsKey(r.getResult().first)) {
					resultMap.put(r.getResult().first,
							resultMap.get(r.getResult().first) + 1);
				} else {
					resultMap.put(r.getResult().first, 1);
				}
				sizeSet.addValue(r.getSize(), "Row 1", r.getName());
				rttSet.addValue(r.getRTT(), "Row 1", r.getName());
			} else if (r.isIncluded() && r instanceof HttpFuzzRecordGroup) {
				updateValues(((HttpFuzzRecordGroup) r).getMembers(), statesMap,
						resultMap);
			}
		}
		for (String key : statesMap.keySet()) {
			stateSet.setValue(key, statesMap.get(key));
		}
		for (String key : resultMap.keySet()) {
			resultSet.setValue(key, resultMap.get(key));
		}
	}

	private void updateValues(ArrayList<HttpFuzzRecord> members,
			HashMap<String, Integer> statesMap,
			HashMap<String, Integer> resultMap) {
		for (HttpFuzzRecord r : members) {
			if (r.isIncluded() && r instanceof HttpFuzzRequestRecord) {
				if (statesMap.containsKey(r.getReason())) {
					statesMap.put(r.getReason(),
							statesMap.get(r.getReason()) + 1);
				} else {
					statesMap.put(r.getReason(), 1);
				}
				if (resultMap.containsKey(r.getResult().first)) {
					resultMap.put(r.getResult().first,
							resultMap.get(r.getResult().first) + 1);
				} else {
					resultMap.put(r.getResult().first, 1);
				}
				sizeSet.addValue(r.getSize(), "Row 1", r.getName());
				rttSet.addValue(r.getRTT(), "Row 1", r.getName());
			} else if (r.isIncluded() && r instanceof HttpFuzzRecordGroup) {
				updateValues(((HttpFuzzRecordGroup) r).getMembers(), statesMap,
						resultMap);
			}
		}

	}

	public void redrawDiagrams() {
		diagrams.removeAll();
		JFreeChart resultChart = ChartFactory.createPieChart(Constant.messages
				.getString("fuzz.result.dia.chart.results.header"), resultSet,
				true, true, false);

		resultChart.getPlot().setBackgroundImageAlignment(Align.TOP_RIGHT);
		result = new ChartPanel(resultChart);

		JFreeChart statusChart = ChartFactory.createPieChart(Constant.messages
				.getString("fuzz.result.dia.chart.states.header"), stateSet,
				true, true, false);
		statusChart.getPlot().setBackgroundImageAlignment(Align.TOP_RIGHT);
		status = new ChartPanel(statusChart);

		JFreeChart sizeChart = ChartFactory
				.createBarChart(Constant.messages
						.getString("fuzz.result.dia.chart.size.header"),
						Constant.messages
								.getString("fuzz.result.dia.chart.size.xaxis"),
						Constant.messages
								.getString("fuzz.result.dia.chart.size.yaxis"),
						sizeSet, PlotOrientation.VERTICAL, false, true, true);
		sizeChart.getPlot().setBackgroundImageAlignment(Align.TOP_RIGHT);
		sizeChart
				.getCategoryPlot()
				.getRenderer()
				.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
		size = new ChartPanel(sizeChart);

		JFreeChart rttChart = ChartFactory
				.createBarChart(Constant.messages
						.getString("fuzz.result.dia.chart.rtt.header"),
						Constant.messages
								.getString("fuzz.result.dia.chart.rtt.xaxis"),
						Constant.messages
								.getString("fuzz.result.dia.chart.rtt.yaxis"),
						rttSet, PlotOrientation.VERTICAL, false, true, true);
		rttChart.getPlot().setBackgroundImageAlignment(Align.TOP_RIGHT);
		rttChart.getCategoryPlot()
				.getRenderer()
				.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());

		rtt = new ChartPanel(rttChart);
		diagrams.add(Constant.messages.getString("fuzz.result.dia.results"),
				result);
		diagrams.add(Constant.messages.getString("fuzz.result.dia.states"),
				status);
		diagrams.add(Constant.messages.getString("fuzz.result.dia.size"), size);
		diagrams.add(Constant.messages.getString("fuzz.result.dia.rtt"), rtt);
	}

	class HttpFuzzRecordComparator implements Comparator<HttpFuzzRecord> {
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

}
