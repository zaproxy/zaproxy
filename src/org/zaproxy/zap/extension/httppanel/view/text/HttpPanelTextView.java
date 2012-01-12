package org.zaproxy.zap.extension.httppanel.view.text;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.fife.ui.rtextarea.RTextScrollPane;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanelView;
import org.zaproxy.zap.extension.search.SearchMatch;

public abstract class HttpPanelTextView implements HttpPanelView {

	private HttpPanelTextArea httpPanelTextArea;
	private JScrollPane scrollPane;
	private JPanel mainPanel;
	
	private HttpPanelTextModelInterface model;
	
	private boolean isEditable = false;
	
	public HttpPanelTextView(HttpPanelTextModelInterface model, boolean isEditable) {
		this.model = model;
		this.isEditable = isEditable;
		init();
	}
	
	private void init() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		httpPanelTextArea = createHttpPanelTextArea(model.getHttpMessage());
		httpPanelTextArea.setEditable(isEditable);
		httpPanelTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseReleased(java.awt.event.MouseEvent e) {
				// right mouse button action
				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 || e.isPopupTrigger()) { 
					if (!httpPanelTextArea.isFocusOwner()) {
						httpPanelTextArea.requestFocusInWindow();
					}

					View.getSingleton().getPopupMenu().show(httpPanelTextArea, e.getX(), e.getY());
				}
			}
		});
		
		scrollPane = new RTextScrollPane(httpPanelTextArea, false);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		mainPanel.add(scrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Classes that what to extend the functionalities of a HttpPanelTextArea
	 * should override this method and return the appropriate extended HttpPanelTextArea
	 * 
	 * @return a HttpPanelTextArea
	 */
	protected abstract HttpPanelTextArea createHttpPanelTextArea(HttpMessage httpMessage);
	
	public void setHttpMessage(HttpMessage httpMessage) {
		httpPanelTextArea.setHttpMessage(httpMessage);
	}
	
	public void highlight(SearchMatch sm) {
		httpPanelTextArea.highlight(sm);
	}
	
	public void load() {
		httpPanelTextArea.setText(model.getData());
		httpPanelTextArea.setCaretPosition(0);
		httpPanelTextArea.discardAllEdits();
	}
	
	public void save() {
		model.setData(httpPanelTextArea.getText());
	}

	@Override
	public String getConfigName() {
		return "Text";
	}
	
	@Override
	public String getName() {
		return Constant.messages.getString("request.panel.view.text");
	}

	@Override
	public boolean isEnabled(HttpMessage msg) {
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
