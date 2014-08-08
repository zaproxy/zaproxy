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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.multiFuzz.FuzzComponent;
import org.zaproxy.zap.extension.multiFuzz.Util;

public class HttpFuzzComponent implements
		FuzzComponent<HttpMessage, HttpFuzzLocation, HttpFuzzGap> {
	
	private static final Logger logger = Logger
			.getLogger(HttpFuzzComponent.class);
	private JSplitPane bgSplit;
	private JTextArea headView;
	private JTextArea bodyView;
	boolean headerFocus = true;

	public HttpFuzzComponent() {
	    this(null);
	}

	public HttpFuzzComponent(HttpMessage msg) {
	    bgSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false);
	    bgSplit.setDividerLocation(0.5);
	    headView = new JTextArea();
	    bodyView = new JTextArea();
	    if (msg != null) {
	        setMessage(msg);
	    }
	    headView.setEditable(false);
	    bodyView.setEditable(false);
		bgSplit.setTopComponent(headView);
		bgSplit.setBottomComponent(bodyView);
		headView.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				bodyView.select(0, 0);
				bodyView.getCaret().setVisible(false);
				headView.getCaret().setVisible(true);
				headerFocus = true;
			}
		});
		bodyView.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				headView.select(0, 0);
				headView.getCaret().setVisible(false);
				bodyView.getCaret().setVisible(true);
				headerFocus = false;
			}
		});
		headView.setMinimumSize(new Dimension(50, 50));
		bodyView.setMinimumSize(new Dimension(50, 50));
	}
	
	@Override
	public HttpFuzzLocation selection() {
		int s = 0;
		int e = 0;
		if (headerFocus) {
			s = headView.getSelectionStart();
			e = headView.getSelectionEnd();
		} else {
			s = headView.getText().length() + 1 + bodyView.getSelectionStart();
			e = headView.getText().length() + 1 + bodyView.getSelectionEnd();
		}
		return new HttpFuzzLocation(s, e);
	}

	@Override
	public void highlight(ArrayList<HttpFuzzGap> allLocs) {
		removeHighlights();
		for (int i = 0; i < allLocs.size(); i++) {
			HttpFuzzLocation l = allLocs.get(i).getLocation();
			try {
				if (l.begin() < headView.getText().length()) {
					headView.getHighlighter().addHighlight(l.begin(), l.end(),
							new MyHighlightPainter(Util.getColor(i + 1)));
				} else {
					bodyView.getHighlighter().addHighlight(
							l.begin() - headView.getText().length() - 1,
							l.end() - headView.getText().length() - 1,
							new MyHighlightPainter(Util.getColor(i + 1)));
				}
			} catch (Exception e) {
				logger.debug(e.getMessage());
			}
		}
	}

	private void removeHighlights() {
		Highlighter.Highlight[] headhigh = headView.getHighlighter()
				.getHighlights();
		for (int i = 0; i < headhigh.length; i++) {
			if (headhigh[i].getPainter() instanceof MyHighlightPainter) {
				headView.getHighlighter().removeHighlight(headhigh[i]);
			}
		}
		Highlighter.Highlight[] bodyhigh = bodyView.getHighlighter()
				.getHighlights();
		for (int i = 0; i < bodyhigh.length; i++) {
			if (bodyhigh[i].getPainter() instanceof MyHighlightPainter) {
				bodyView.getHighlighter().removeHighlight(bodyhigh[i]);
			}
		}
	}

	@Override
	public Component messageView() {
		return bgSplit;
	}

	protected class MyHighlightPainter extends DefaultHighlightPainter {
		public MyHighlightPainter(Color color) {
			super(color);
		}
	}

	@Override
	public void markUp(HttpFuzzLocation gap) {
		int s = gap.begin();
		int e = gap.end();
		if (s > headView.getText().length()) {
			s -= headView.getText().length();
			e -= headView.getText().length();
			bodyView.requestFocus();
			bodyView.select(s, e);
			headerFocus = false;
		} else {
			headView.requestFocus();
			headView.select(s, e);
			headerFocus = true;
		}

	}

	@Override
	public void search(String text) {
		String txt = headView.getText().toLowerCase()
				+ bodyView.getText().toLowerCase();
		String findText = text.toLowerCase();
		int focus = 0;
		if (headerFocus) {
			focus = headView.getSelectionEnd();
		} else {
			focus = bodyView.getSelectionEnd();
		}
		int startPos = txt.indexOf(findText, focus);

		// Enable Wrap Search
		if (startPos <= 0) {
			focus = 0;
			startPos = txt.indexOf(findText, focus);
		}

		int length = findText.length();
		if (startPos > -1) {
			if(startPos > headView.getText().length()){
				startPos++;
			}
			this.markUp(new HttpFuzzLocation(startPos, startPos + length));
		} else {
			Toolkit.getDefaultToolkit().beep();
		}

	}
	@Override
	public void setMessage(HttpMessage msg) {
		headView.setText(msg.getRequestHeader().toString());
		bodyView.setText(msg.getRequestBody().toString());
	}
}
