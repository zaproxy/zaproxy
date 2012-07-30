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
package org.zaproxy.zap.extension.httppanel.view.text;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.view.HighlightSearchEntry;
import org.zaproxy.zap.view.HighlighterManager;

/* ZAP Text Area
 * Which enhanced functionality. Used to display HTTP Message request / response, or parts of it.
 */
public abstract class HttpPanelTextArea extends ZapTextArea implements Observer {

	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(HttpPanelTextArea.class);
	
	private Message message;
	
	public HttpPanelTextArea() {
		this.message = null;
		
		initHighlighter();
	}
	
	private void initHighlighter() {
		HighlighterManager highlighter = HighlighterManager.getInstance();
		
		highlighter.addObserver(this);
		
		if (message != null) {
			highlightAll();
		}
	}
	
	// Highlight all search strings from HighlightManager
	private void highlightAll() {
		HighlighterManager highlighter = HighlighterManager.getInstance();
		
		LinkedList<HighlightSearchEntry> highlights = highlighter.getHighlights();
		for (HighlightSearchEntry entry: highlights) {
			highlightEntryParser(entry);
		}
	}
	
	// Parse the TextArea data and search the HighlightEntry strings
	// Highlight all found strings
	private void highlightEntryParser(HighlightSearchEntry entry) {
		String text;
		int lastPos = 0;
		
		text = this.getText();
		
		Highlighter hilite = this.getHighlighter();
		HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(entry.getColor());
		
		while ( (lastPos = text.indexOf(entry.getToken(), lastPos)) > -1) {
			try {
				hilite.addHighlight(lastPos, lastPos + entry.getToken().length(), painter);
				lastPos += entry.getToken().length();
			} catch (BadLocationException e) {
				log.warn("Could not highlight entry", e);
			}
		}
	}
	
	// Apply highlights after a setText()
	@Override
	public void setText(String s) {
		super.setText(s);
		highlightAll();
	}
	
	protected void highlight(int start, int end) {
		Highlighter hilite = this.getHighlighter();
		HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);
		
		try {
			// DOBIN
			removeAllHighlights();
			hilite.addHighlight(start, end, painter);
			this.setCaretPosition(start);
		} catch (BadLocationException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private void removeAllHighlights() {
		Highlighter hilite = this.getHighlighter();
		hilite.removeAllHighlights();
	}
	
	// HighlighterManager called us
	// there is either
	// - a new highlight
	// - something other (added several, deleted, ...).
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 == null) {
			// Re-highlight everything
			removeAllHighlights();
			highlightAll();
		} else {
			// Add specific highlight
			HighlightSearchEntry token = (HighlightSearchEntry) arg1;
			highlightEntryParser(token);
		}
		this.invalidate();
	}
	
	public abstract void search(Pattern p, List<SearchMatch> matches);
	
	// highlight a specific SearchMatch in the editor
	public abstract void highlight(SearchMatch sm);
	
	public void setMessage(Message aMessage) {
		this.message = aMessage;
	}
	
	public Message getMessage() {
		return message;
	}

}
