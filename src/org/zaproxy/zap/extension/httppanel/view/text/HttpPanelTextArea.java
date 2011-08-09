package org.zaproxy.zap.extension.httppanel.view.text;

import java.awt.Color;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import org.apache.log4j.Logger;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.utils.ZapTextArea;

public class HttpPanelTextArea extends ZapTextArea {

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
		}
		
		return sm;
	}

	public void highlight(SearchMatch sm) {
		Highlighter hilite = this.getHighlighter();
		HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);
		try {
			hilite.removeAllHighlights();
			hilite.addHighlight(sm.getStart(), sm.getEnd(), painter);
			this.setCaretPosition(sm.getStart());
		} catch (BadLocationException e) {
			log.error(e.getMessage(), e);
		}
	}
	
}
