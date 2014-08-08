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

import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.utils.Pair;

public class HttpFuzzRequestRecord implements HttpFuzzRecord {
	enum State {
		SUCCESSFUL, REFLECTED, ERROR, ANTI_CRSF_TOKEN, CUSTOM
	}

	private static final ImageIcon ERROR_ICON = new ImageIcon(
			HttpFuzzTableModel.class.getResource("/resource/icon/16/150.png"));
	private static final ImageIcon REFLECTED_ICON = new ImageIcon(
			HttpFuzzTableModel.class.getResource("/resource/icon/16/099.png"));
	private static final ImageIcon ANTI_CSRF_ICON = new ImageIcon(
				HttpFuzzTableModel.class
						.getResource("/resource/icon/16/183.png"));
	private static final String STATE_ERROR_LABEL = Constant.messages
			.getString("fuzz.http.table.field.state.error");
	private static final String STATE_REFLECTED_LABEL = Constant.messages
			.getString("fuzz.http.table.field.state.reflected");
	private static final String STATE_ANTI_CSRF_TOKEN_REQUEST_LABEL = Constant.messages
			.getString("fuzz.http.table.field.state.antiCsrfTokenRequest");
	private static final String STATE_SUCCESSFUL_LABEL = Constant.messages
			.getString("fuzz.http.table.field.state.successful");

	private static final Logger logger = Logger
			.getLogger(HttpFuzzRequestRecord.class);
	private String name;
	private String custom;
	private State state;
	private ArrayList<String> payloads;
	private HistoryReference historyReference;
	private boolean incl;

	public HttpFuzzRequestRecord(String n, String custom, State s, ArrayList<String> pay,
			HistoryReference history) {
		this.name = n;
		this.custom = custom;
		this.state = s;
		this.payloads = pay;
		this.historyReference = history;
		this.incl = true;
	}

	public HistoryReference getHistory() {
		return historyReference;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getMethod() {
		HttpMessage msg = getHttpMessage(historyReference);
		if (msg != null) {
			return msg.getRequestHeader().getMethod();
		}
		return "";
	}

	@Override
	public String getURI() {
		HttpMessage msg = getHttpMessage(historyReference);
		if (msg != null) {
			return msg.getRequestHeader().getURI().toString();
		}
		return "";
	}

	@Override
	public int getRTT() {
		HttpMessage msg = getHttpMessage(historyReference);
		if (msg != null) {
			return msg.getTimeElapsedMillis();
		}
		return -1;
	}

	@Override
	public int getSize() {
		HttpMessage msg = getHttpMessage(historyReference);
		if (msg != null) {
			return msg.getResponseBody().length();
		}
		return -1;
	}

	@Override
	public int getState() {
		HttpMessage msg = getHttpMessage(historyReference);
		if (msg != null) {
			return msg.getResponseHeader().getStatusCode();
		}
		return -1;
	}

	@Override
	public String getReason() {
		HttpMessage msg = getHttpMessage(historyReference);
		if (msg != null) {
			return msg.getResponseHeader().getReasonPhrase();
		}
		return "";
	}

	@Override
	public Pair<String, ImageIcon> getResult() {
		String status;
		ImageIcon icon;

		switch (state) {
		case ERROR:
			status = STATE_ERROR_LABEL;
			icon = ERROR_ICON;
			break;
		case REFLECTED:
			status = STATE_REFLECTED_LABEL;
			icon = REFLECTED_ICON;
			break;
		case ANTI_CRSF_TOKEN:
			status = STATE_ANTI_CSRF_TOKEN_REQUEST_LABEL;
			icon = ANTI_CSRF_ICON;
			break;
		case CUSTOM:
			status = getCustom();
			icon = null;
			break;
		case SUCCESSFUL:
		default:
			status = STATE_SUCCESSFUL_LABEL;
			icon = null;
		}

		return new Pair<>(status, icon);
	}

	@Override
	public ArrayList<String> getPayloads() {
		return payloads;
	}

	@Override
	public Boolean isIncluded() {
		return incl;
	}

	@Override
	public void setIncluded(Boolean i) {
		incl = i;
	}

	private HttpMessage getHttpMessage(HistoryReference historyReference) {
		HttpMessage msg = null;

		try {
			msg = historyReference.getHttpMessage();
		} catch (HttpMalformedHeaderException | SQLException e) {
			logger.error(e.getMessage(), e);
		}

		return msg;
	}

	@Override
	public void setName(String s) {
		this.name = s;
	}
	@Override
	public String getCustom() {
		return custom;
	}

	public void setCustom(String custom) {
		this.custom = custom;
	}
}
