package org.zaproxy.zap.extension.httppanel;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JScrollPane;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.extension.AbstractPanel;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.utils.ZapTextArea;

public class HttpPanelTextUi extends AbstractPanel {

	private static final long serialVersionUID = 1L;

	private ZapTextArea ZapTextArea;
	
    private static Log log = LogFactory.getLog(HttpPanelTextUi.class);
	
	public HttpPanelTextUi() {
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		
		ZapTextArea = new ZapTextArea();
		ZapTextArea.setLineWrap(true);
		ZapTextArea.setWrapStyleWord(true);
		
		JScrollPane scrollPane = new JScrollPane(ZapTextArea);
		
		this.add(scrollPane, gridBagConstraints);
		//this.add(ZapTextArea, gridBagConstraints);
	}
	
	public void setData(String data) {
		ZapTextArea.setText(data);
		ZapTextArea.setCaretPosition(0);
	}

	public String getData() {
		return ZapTextArea.getText();
	}

	public void highlight(SearchMatch sm) {
		Highlighter hilite = ZapTextArea.getHighlighter();
	    HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);
	    try {
			hilite.removeAllHighlights();
			hilite.addHighlight(sm.getStart(), sm.getEnd(), painter);
			ZapTextArea.setCaretPosition(sm.getStart());
	    } catch (BadLocationException e) {
			log.error(e.getMessage(), e);
		}
	}

}