/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.HistoryReferenceTableModel.COLUMN;
import org.zaproxy.zap.view.messagecontainer.http.DefaultSelectableHistoryReferencesContainer;
import org.zaproxy.zap.view.messagecontainer.http.SelectableHistoryReferencesContainer;

/**
 * @deprecated (2.3.0) Superseded by {@link org.zaproxy.zap.view.table.HistoryReferencesTable}. It will be removed in a future
 *             release.
 */
@Deprecated
public class HistoryReferenceTable extends JTable {

	private static final long serialVersionUID = 1L;
	
	private HistoryReferenceTableModel model = null;
	
	private static final Logger logger = Logger.getLogger(HistoryReferenceTable.class);

	public HistoryReferenceTable(COLUMN[] columns) {
		this(columns, null);
	}
	
	public HistoryReferenceTable(COLUMN[] columns, int[] sizes) {
		this (new HistoryReferenceTableModel(columns));
		this.setColumnSizes(sizes);
	}
	
	public HistoryReferenceTable(HistoryReferenceTableModel model) {
		this.model = model;
		this.setModel(model);
		this.setName("GenericHistoryReferenceTable");
		
		this.setColumnSelectionAllowed(false);
		this.setCellSelectionEnabled(false);
		this.setRowSelectionAllowed(true);
		this.setAutoCreateRowSorter(true);

		// Issue 954: Force the JTable cell to auto-save when the focus changes.
		// Example, edit cell, click OK for a panel dialog box, the data will get saved.
		this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		
		this.setDoubleBuffered(true);
		this.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		// Add hack to force row selection on right click
		this.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mousePressed(java.awt.event.MouseEvent e) {
				showPopupMenuIfTriggered(e);
			}
			@Override
			public void mouseReleased(java.awt.event.MouseEvent e) {
				showPopupMenuIfTriggered(e);
			}
			private void showPopupMenuIfTriggered(java.awt.event.MouseEvent e) {
				if (e.isPopupTrigger()) {
					// Select table item
					int row = rowAtPoint(e.getPoint());
					if (row < 0 || ! getSelectionModel().isSelectedIndex(row)) {
						getSelectionModel().clearSelection();
						if (row >= 0) {
							getSelectionModel().setSelectionInterval(row, row);
						}
					}
					final int countSelectedRows = getSelectedRowCount();
					final List<HistoryReference> historyReferences = new ArrayList<>(countSelectedRows);
					if (countSelectedRows > 0) {
						for (int selectedRow : getSelectedRows()) {
							historyReferences.add(getHrefModel().getHistoryReference(convertRowIndexToModel(selectedRow)));
						}
					}
					SelectableHistoryReferencesContainer messageContainer = new DefaultSelectableHistoryReferencesContainer(
							HistoryReferenceTable.this.getName(),
							HistoryReferenceTable.this,
							Collections.<HistoryReference>emptyList(),
							historyReferences);
					View.getSingleton().getPopupMenu().show(messageContainer, e.getX(), e.getY());
				}
			}
		});
		this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
			    if (!e.getValueIsAdjusting()) {
	                HistoryReference hRef = getSelectedValue();
			        if (hRef == null) {
                        return;
                    }
			        
				    try {
				        displayMessage(hRef.getHttpMessage());
				    } catch (HttpMalformedHeaderException ex) {
				        logger.error(ex.getMessage(), ex);
                    } catch (DatabaseException ex) {
                        logger.error(ex.getMessage(), ex);
                    }
			    }
			}});

	}
	
	public void setColumnSizes(int[] sizes) {
		if (sizes != null) {
			if (sizes.length != this.getModel().getColumnCount()) {
				logger.error("Different number of colum and size elements: " + this.getModel().getColumnCount() + " != " + sizes.length);
			} else {
				for (int i=0; i < sizes.length; i++) {
					this.getColumnModel().getColumn(i).setPreferredWidth(sizes[i]);
				}
			}
		}
	}
	
    private void displayMessage(HttpMessage msg) {
    	if (msg == null) {
    		return;
    	}
    	if (msg.getRequestHeader() != null) {
    		logger.debug("displayMessage " + msg.getRequestHeader().getURI());
    	} else {
    		logger.debug("displayMessage null header");
    	}
    	
        if (msg.getRequestHeader() != null && msg.getRequestHeader().isEmpty()) {
            View.getSingleton().getRequestPanel().clearView(true);
        } else {
        	View.getSingleton().getRequestPanel().setMessage(msg);
        }
        
        if (msg.getResponseHeader() != null && msg.getResponseHeader().isEmpty()) {
        	View.getSingleton().getResponsePanel().clearView(false);
        } else {
        	View.getSingleton().getResponsePanel().setMessage(msg, true);
        }
    }

	public HistoryReference getSelectedValue() {
		if (this.getSelectedRow() >= 0) {
			return model.getHistoryReference(convertRowIndexToModel(this.getSelectedRow()));
		}
		return null;
	}
	
	public List<HistoryReference> getSelectedValues() {
		int [] rows = this.getSelectedRows();
		if (rows != null) {
			List<HistoryReference> hrefList = new ArrayList<>(rows.length);
			for (int row : rows) {
				hrefList.add(this.getHrefModel().getHistoryReference(convertRowIndexToModel(row)));
			}
			return hrefList;
		}
		
		return null;
	}
	
	public HistoryReferenceTableModel getHrefModel() {
		return this.model;
	}

}
