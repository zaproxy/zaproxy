package org.zaproxy.zap.extension.httppanel.view.text;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import org.apache.log4j.Logger;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.highlighter.HighlightSearchEntry;
import org.zaproxy.zap.extension.highlighter.HighlighterManager;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.utils.ZapTextArea;

/* ZAP Text Area
 * Which enhanced functionality. Used to display HTTP Message request / response, or parts of it.
 */
public class HttpPanelTextArea extends ZapTextArea implements Observer {

	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(ManualRequestEditorDialog.class);
	
	private HttpMessage httpMessage;
	private MessageType messageType;
	
	public enum MessageType {
		Header,
		Body,
		Full
	};
	
	public HttpPanelTextArea(HttpMessage httpMessage, MessageType messageType) {
		this.httpMessage = httpMessage;
		this.messageType = messageType;
		initHighlighter();
	}
	
	private void initHighlighter() {
		HighlighterManager highlighter = HighlighterManager.getInstance();
		
		highlighter.addObserver(this);
		
		if (httpMessage != null) {
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
	
	// Return selected text
	public SearchMatch getTextSelection() {
		SearchMatch sm = null;
		
		if (messageType.equals(MessageType.Header)) {
			sm = new SearchMatch(
					httpMessage,
					SearchMatch.Location.REQUEST_HEAD, 
					getSelectionStart(),
					getSelectionEnd());
		} else if (messageType.equals(MessageType.Body)) {
			sm = new SearchMatch(
					httpMessage,
					SearchMatch.Location.REQUEST_BODY, 
					getSelectionStart(),
					getSelectionEnd());
		} else if (messageType.equals(MessageType.Full)) {
			int headerLen = httpMessage.getRequestHeader().toString().length();
			if (getSelectionStart() < headerLen) {
				sm = new SearchMatch(
					httpMessage,
					SearchMatch.Location.REQUEST_HEAD, 
					getSelectionStart(),
					getSelectionEnd());
			} else {
				sm = new SearchMatch(
					httpMessage,
					SearchMatch.Location.REQUEST_BODY, 
					getSelectionStart() - headerLen,
					getSelectionEnd() - headerLen);
			}
		} else {
			log.debug("MessageType unknown");
		}
		
		return sm;
	}
	
	@Override
	// Apply highlights after a setText()
	public void setText(String s) {
		super.setText(s);
		highlightAll();
	}

	// highlight a specific SearchMatch in the editor
	public void highlight(SearchMatch sm) {
		Highlighter hilite = this.getHighlighter();
		HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);
		
		int len = this.getText().length();
		if (sm.getStart() > len || sm.getEnd() > len) {
			return;
		}
		
		try {
			// DOBIN
			removeAllHighlights();
			hilite.addHighlight(sm.getStart(), sm.getEnd(), painter);
			this.setCaretPosition(sm.getStart());
		} catch (BadLocationException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private void removeAllHighlights() {
		Highlighter hilite = this.getHighlighter();
		HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);
		hilite.removeAllHighlights();
	}

	@Override
	// HighlighterManager called us
	// there is either
	// - a new highlight
	// - something other (added several, deleted, ...).
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
	
	public MessageType getMessageType() {
		return messageType;
	}
	
	public void setHttpMessage(HttpMessage httpMessage) {
		this.httpMessage = httpMessage;
	}
	
	public HttpMessage getHttpMessage() {
		return httpMessage;
	}
	
}
