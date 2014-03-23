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
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.model.HistoryReference;

/**
 * A generic customizable TableModel for use by any panel that displays HistoryReferences.
 * 
 * @deprecated (2.3.0) Superseded by {@link org.zaproxy.zap.view.table.HistoryReferencesTableModel}. It will be removed in a
 *             future release.
 */
@Deprecated
public class HistoryReferenceTableModel extends AbstractTableModel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(HistoryReferenceTableModel.class);

	/** The full set of allowed columns **/
	public static final int COLUMN_ = 0;
	
	public enum COLUMN  {HREF_ID, TYPE_FLAG, METHOD, URL, CODE, REASON, RTT, SIZE, SESSION_ID, ALERT_FLAG, TAGS, 
		CUSTOM_1, CUSTOM_2, CUSTOM_3}
	
	/** The column names. */
	private static final String[] COLUMN_NAMES = { 
			Constant.messages.getString("view.href.table.header.hrefid"),
			Constant.messages.getString("view.href.table.header.hreftype"),
			Constant.messages.getString("view.href.table.header.method"),
			Constant.messages.getString("view.href.table.header.url"),
			Constant.messages.getString("view.href.table.header.code"),
			Constant.messages.getString("view.href.table.header.reason"),
			Constant.messages.getString("view.href.table.header.rtt"),
			Constant.messages.getString("view.href.table.header.size.message"),
			Constant.messages.getString("view.href.table.header.sessionid"),
			Constant.messages.getString("view.href.table.header.highestalert"),
			Constant.messages.getString("view.href.table.header.tags",
			"", 	/* CUSTOM_1 */
			"", 	/* CUSTOM_2 */
			"")};	/* CUSTOM_3 */

	/** The list to be displayed. */
	private List<HistoryReference> hrefList;
	private COLUMN[] columns = null;
	
	/**
	 * Instantiates a new table model using the specified columns in the specified order.
	 */
	public HistoryReferenceTableModel(COLUMN[] columns) {
		super();
		this.columns = columns;
		this.hrefList = new ArrayList<>();
	}

	/**
	 * This should be overriden for any custom columns
	 */
	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[this.columns[column].ordinal()];
	}

	@Override
	public int getColumnCount() {
		return this.columns.length;
	}
	
	public COLUMN getColumn(int column) {
		return this.columns[column];
	}

	@Override
	public int getRowCount() {
		return hrefList.size();
	}

	/**
	 * This should be overriden for any custom columns
	 */
	@Override
	public Object getValueAt(int row, int col) {
		// Get the href and the required field
		HistoryReference href = hrefList.get(row);
		try {
			switch (this.columns[col]) {
			case HREF_ID:		return href.getHistoryId();
			case TYPE_FLAG:		return this.getHrefTypeIcon(href);
			case METHOD:		return href.getMethod();
			case URL:			return href.getURI().toString();
			case CODE:			return href.getStatusCode();
			case REASON:		return href.getReason();
			case RTT:			return href.getRtt();
			case SIZE:			return href.getResponseBodyLength();
			case SESSION_ID:	return href.getSessionId();
			case ALERT_FLAG:	return this.getHrefAlertIcon(href);
			case TAGS:			return listToCsv(href.getTags());
			default:			return null;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	public HistoryReference getHistoryReference(int row) {
		return hrefList.get(row);
	}

	private String listToCsv(List<String> list) {
		if (list == null || list.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String str : list) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(str);
		}
		return sb.toString();
	}
	
	private ImageIcon getHrefAlertIcon(HistoryReference href) {
    	switch (href.getHighestAlert()) {
    	case Alert.RISK_INFO:	return new ImageIcon(Constant.INFO_FLAG_IMAGE_URL);
    	case Alert.RISK_LOW:	return new ImageIcon(Constant.LOW_FLAG_IMAGE_URL);
    	case Alert.RISK_MEDIUM:	return new ImageIcon(Constant.MED_FLAG_IMAGE_URL);
    	case Alert.RISK_HIGH:	return new ImageIcon(Constant.HIGH_FLAG_IMAGE_URL);
    	default:				return null;
    	}
	}

	private ImageIcon getHrefTypeIcon(HistoryReference href) {
		/*
    	switch (href.getHistoryType()) {
    	case Alert.RISK_INFO:	return new ImageIcon(Constant.INFO_FLAG_IMAGE_URL);
    	case Alert.RISK_LOW:	return new ImageIcon(Constant.LOW_FLAG_IMAGE_URL);
    	case Alert.RISK_MEDIUM:	return new ImageIcon(Constant.MED_FLAG_IMAGE_URL);
    	case Alert.RISK_HIGH:	return new ImageIcon(Constant.HIGH_FLAG_IMAGE_URL);
    	default:				return null;
    	}
    	*/
		return null;
	}

	/**
	 * Removes all the elements. Method is synchronized internally.
	 */
	public void removeAllElements() {
		synchronized (hrefList) {
			hrefList.clear();
			fireTableDataChanged();
		}
	}

	/**
	 * Adds a href. Method is synchronized internally.
	 * 
	 * @param href the HistoryReference
	 */
	public void add(HistoryReference href) {
		synchronized (hrefList) {
			hrefList.add(href);
			fireTableRowsInserted(hrefList.size() - 1, hrefList.size() - 1);
		}
	}

	/**
	 * Removes the scan result for a particular uri and method. Method is synchronized internally.
	 * 
	 * @param uri the uri
	 * @param method the method
	 */
	public void remove(HistoryReference href) {
		synchronized (hrefList) {
			int index = hrefList.indexOf(href);
			if (index >= 0) {
				hrefList.remove(index);
				fireTableRowsDeleted(index, index);
			}
		}
	}

	/**
	 * Returns the type of column for given column index.
	 * This should be overriden for any custom columns
	 * 
	 * @param columnIndex the column index
	 * @return the column class
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (this.columns[columnIndex]) {
		case HREF_ID:		return Integer.class;
		case TYPE_FLAG:		return ImageIcon.class;
		case METHOD:		return String.class;
		case URL:			return String.class;
		case CODE:			return Integer.class;
		case REASON:		return String.class;
		case RTT:			return Integer.class;
		case SIZE:			return Integer.class;
		case SESSION_ID:	return Long.class;
		case ALERT_FLAG:	return ImageIcon.class;
		case TAGS:			return String.class;
		default:			return null;
		}
	}
}
