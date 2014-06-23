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

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.utils.Pair;

public class HttpFuzzTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -8627752081308487147L;

	private static final Logger logger = Logger
			.getLogger(HttpFuzzTableModel.class);

	private static final String[] COLUMN_NAMES = {
			Constant.messages.getString("fuzz.http.table.header.method"),
			Constant.messages.getString("fuzz.http.table.header.uri"),
			Constant.messages.getString("fuzz.http.table.header.status"),
			Constant.messages.getString("fuzz.http.table.header.reason"),
			Constant.messages.getString("fuzz.http.table.header.rtt"),
			Constant.messages.getString("fuzz.http.table.header.size"),
			Constant.messages.getString("fuzz.http.table.header.state"),
			Constant.messages.getString("fuzz.http.table.header.fuzz") };

	private static final String STATE_ERROR_LABEL = Constant.messages
			.getString("fuzz.http.table.field.state.error");
	private static final String STATE_REFLECTED_LABEL = Constant.messages
			.getString("fuzz.http.table.field.state.reflected");
	private static final String STATE_ANTI_CSRF_TOKEN_REQUEST_LABEL = Constant.messages
			.getString("fuzz.http.table.field.state.antiCsrfTokenRequest");
	private static final String STATE_SUCCESSFUL_LABEL = Constant.messages
			.getString("fuzz.http.table.field.state.successful");

	private static final int COLUMN_COUNT = COLUMN_NAMES.length;

	private List<Pair<HttpFuzzerContentPanel.State, HistoryReference>> data = new LinkedList<>();

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		Pair<HttpFuzzerContentPanel.State, HistoryReference> result = data
				.get(row);
		HistoryReference historyReference = result.second;

		Object value = "";

		switch (column) {
		case 0:
			HttpMessage msg = getHttpMessage(historyReference);
			if (msg != null) {
				value = msg.getRequestHeader().getMethod();
			}
			break;
		case 1:
			msg = getHttpMessage(historyReference);
			if (msg != null) {
				value = msg.getRequestHeader().getURI().toString();
			}
			break;
		case 2:
			msg = getHttpMessage(historyReference);
			if (msg != null) {
				value = Integer.toString(msg.getResponseHeader()
						.getStatusCode());
			}
			break;
		case 3:
			msg = getHttpMessage(historyReference);
			if (msg != null) {
				value = msg.getResponseHeader().getReasonPhrase();
			}
			break;
		case 4:
			msg = getHttpMessage(historyReference);
			if (msg != null) {
				value = Integer.valueOf(msg.getTimeElapsedMillis());
			}
			break;
		case 5:
			msg = getHttpMessage(historyReference);
			if (msg != null) {
				value = Integer.valueOf(msg.getResponseBody().length());
			}
			break;
		case 6:
			HttpFuzzerContentPanel.State state = result.first;
			String status;
			ImageIcon icon;

			switch (state) {
			case ERROR:
				status = STATE_ERROR_LABEL;
				icon = new ImageIcon(
						HttpFuzzTableModel.class
								.getResource("/resource/icon/16/150.png"));
				break;
			case REFLECTED:
				status = STATE_REFLECTED_LABEL;
				icon = new ImageIcon(
						HttpFuzzTableModel.class
								.getResource("/resource/icon/16/099.png")); // Yellow
																			// fuzzy
																			// circle
				break;
			case ANTI_CRSF_TOKEN:
				status = STATE_ANTI_CSRF_TOKEN_REQUEST_LABEL;
				icon = new ImageIcon(
						HttpFuzzTableModel.class
								.getResource("/resource/icon/16/183.png"));
				break;
			case SUCCESSFUL:
			default:
				status = STATE_SUCCESSFUL_LABEL;
				icon = null;
			}

			value = new Pair<>(status, icon);

			break;
		case 7:
			msg = getHttpMessage(historyReference);
			if (msg != null) {
				value = msg.getNote();
			}
			break;
		default:
		}

		return value;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		Class<?> clazz;
		switch (columnIndex) {
		case 0:
		case 3:
		case 5:
			clazz = String.class;
			break;
		case 6:
			clazz = Pair.class;
			break;
		default:
			clazz = String.class;
		}
		return clazz;
	}

	private HttpMessage getHttpMessage(HistoryReference historyReference) {
		HttpMessage msg = null;

		try {
			msg = historyReference.getHttpMessage();
		} catch (HttpMalformedHeaderException e) {
			logger.error(e.getMessage(), e);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}

		return msg;
	}

	public HistoryReference getHistoryReferenceAtRow(int row) {
		return data.get(row).second;
	}

	public void addHistoryReference(HttpFuzzerContentPanel.State state,
			HistoryReference historyReference) {
		final int row = data.size();
		data.add(new Pair<>(state, historyReference));
		fireTableRowsInserted(row, row);
	}

	public List<Pair<HttpFuzzerContentPanel.State, HistoryReference>> getHistoryReferences() {
		return data;
	}

}
