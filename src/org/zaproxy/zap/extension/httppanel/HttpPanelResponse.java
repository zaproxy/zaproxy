package org.zaproxy.zap.extension.httppanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.history.ManualRequestEditorDialog;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.HttpPanel;
import org.zaproxy.zap.view.HttpPanelView;

public class HttpPanelResponse extends HttpPanel {
	private static final long serialVersionUID = 1L;
	
	// ZAP: Added logger
    private static Log log = LogFactory.getLog(ManualRequestEditorDialog.class);

	public HttpPanelResponse() {
		super();
	}
	
	public HttpPanelResponse(boolean isEditable) {
		super(isEditable);
	}
	
	public HttpPanelResponse(boolean isEditable, Extension extension) {
		super(isEditable, extension);
	}

	public void getMessage(HttpMessage msg, boolean isRequest) {
		try {
				if (getTxtHeader().getText().length() == 0) {
					msg.getResponseHeader().clear();
					msg.getResponseBody().setBody("");
				} else {
					msg.getResponseHeader().setMessage(getHeaderFromJTextArea(getTxtHeader()));
					String txt = getTxtBody().getText();
					msg.getResponseBody().setBody(txt);
					msg.getResponseHeader().setContentLength(msg.getResponseBody().length());
				}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	protected void setDisplay(HttpMessage msg) {
		if (msg.getResponseHeader().isEmpty()) {
			getTxtHeader().setText("");
			getTxtHeader().setCaretPosition(0);

			getTxtBody().setText("");
			getTxtBody().setCaretPosition(0);

			getLblIcon().setIcon(null);
			return;
		}

		String header = replaceHeaderForJTextArea(msg.getResponseHeader().toString());
		String body = msg.getResponseBody().toString();

		getTxtHeader().setText(header);
		getTxtHeader().setCaretPosition(0);

		getTxtBody().setText(body);
		getTxtBody().setCaretPosition(0);

		getComboView().removeAllItems();
		getComboView().addItem(VIEW_RAW);

		pluggableView(msg);
		getComboView().setEnabled(true);

		if (msg.getResponseHeader().isImage()) {
			getComboView().addItem(VIEW_IMAGE);
			getLblIcon().setIcon(getImageIcon(msg));

		}

		if (msg.getResponseHeader().isImage()) {
			getComboView().setSelectedItem(VIEW_IMAGE);	        
		} else {
			getComboView().setSelectedItem(VIEW_RAW);	        	        
		}
	}

}
