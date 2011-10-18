package org.zaproxy.zap.extension.httppanel.view.text;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextArea.MessageType;
import org.zaproxy.zap.extension.search.SearchMatch;

public class HttpPanelTextView implements HttpPanelView {

	private HttpPanelTextArea httpPanelTextArea;
	private JScrollPane scrollPane;
	private JPanel mainPanel;
	
	private MessageType messageType;
	private HttpPanelTextModelInterface model;
	
	private boolean isEditable = false;
	
	public HttpPanelTextView(HttpPanelTextModelInterface model, MessageType messageType, boolean isEditable) {
		this.messageType = messageType;
		this.model = model;
		this.isEditable = isEditable;
		init();
	}
	
	private void init() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		httpPanelTextArea = new HttpPanelTextArea(model.getHttpMessage(), messageType);
		httpPanelTextArea.setLineWrap(true);
		httpPanelTextArea.setEditable(isEditable);
        httpPanelTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
        	
        	public void mousePressed(java.awt.event.MouseEvent e) {
				mouseClicked(e);
			}
				
			public void mouseReleased(java.awt.event.MouseEvent e) {
				mouseClicked(e);
			}
			
			public void mouseClicked(java.awt.event.MouseEvent e) {
				// right mouse button action
				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 || e.isPopupTrigger()) {
					View.getSingleton().getPopupMenu().show(httpPanelTextArea, e.getX(), e.getY());
				}
			}
        });
		
		scrollPane = new JScrollPane(httpPanelTextArea);
		mainPanel.add(scrollPane, BorderLayout.CENTER);
	}
	
	public void setHttpMessage(HttpMessage httpMessage) {
		httpPanelTextArea.setHttpMessage(httpMessage);
	}
	
	public void highlight(SearchMatch sm) {
		httpPanelTextArea.highlight(sm);
	}
	
	
	public void load() {
		httpPanelTextArea.setText(model.getData());
		httpPanelTextArea.setCaretPosition(0);
	}
	
	public void save() {
		model.setData(httpPanelTextArea.getText());
	}
	
	
	@Override
	public String getName() {
		return Constant.messages.getString("request.panel.view.text");
	}

	@Override
	public boolean isEnabled(HttpMessage msg) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean hasChanged() {
		return false;
	}
	
	@Override
	public JComponent getPane() {
		return mainPanel;
	}

	@Override
	public boolean isEditable() {
		return httpPanelTextArea.isEditable();
	}

	@Override
	public void setEditable(boolean editable) {
		httpPanelTextArea.setEditable(editable);
	}
	
}
