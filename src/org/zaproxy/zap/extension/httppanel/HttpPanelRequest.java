package org.zaproxy.zap.extension.httppanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.history.ManualRequestEditorDialog;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.view.HttpPanel;
import org.zaproxy.zap.httputils.RequestUtils;

/*
 *
 */

public class HttpPanelRequest extends HttpPanel {
	private static final long serialVersionUID = 1L;
	
	// ZAP: Added logger
    private static Log log = LogFactory.getLog(ManualRequestEditorDialog.class);
	
	private JComboBox comboChangeMethod = null;
	private JPanel panelCommand = null;
	private JLabel jLabel = null;  //  @jve:decl-index=0:
	
	
	public HttpPanelRequest() {
		super();
		init();
	}
	
	public HttpPanelRequest(boolean isEditable) {
		super(isEditable);
	}
		
	public HttpPanelRequest(boolean isEditable, Extension extension) {
		super(isEditable, extension);
		init();
	}
		
	private void init() {
		getPanelOption().add(getPanelCommand(), "");
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelCommand() {
		if (panelCommand == null) {
			panelCommand = new JPanel();
			GridBagConstraints gridBagConstraints0 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1b = new GridBagConstraints();
			jLabel = new JLabel();
			panelCommand.setLayout(new GridBagLayout());
			jLabel.setText("");
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints0.gridx = 1;
			gridBagConstraints0.gridy = 0;
			gridBagConstraints0.ipadx = 0;
			gridBagConstraints0.ipady = 0;
			gridBagConstraints0.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints0.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints0.weightx = 1.0D;
			gridBagConstraints1b.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints1b.gridx = 2;
			gridBagConstraints1b.gridy = 0;
			gridBagConstraints1b.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints2.gridx = 3;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints3.gridx = 4;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHEAST;
			gridBagConstraints3.insets = new java.awt.Insets(2,2,2,2);
			panelCommand.add(jLabel, gridBagConstraints0);
			panelCommand.add(getComboChangeMethod(), gridBagConstraints1);
//			panelCommand.add(getBtnSend(), gridBagConstraints3);
		}
		return panelCommand;
	}
	

    private JComboBox getComboChangeMethod() {
    	if (comboChangeMethod == null) {
    		comboChangeMethod = new JComboBox();
    		comboChangeMethod.setEditable(false);
    		comboChangeMethod.addItem(Constant.messages.getString("manReq.pullDown.method"));
    		comboChangeMethod.addItem(HttpRequestHeader.CONNECT);
    		comboChangeMethod.addItem(HttpRequestHeader.DELETE);
    		comboChangeMethod.addItem(HttpRequestHeader.GET);
    		comboChangeMethod.addItem(HttpRequestHeader.HEAD);
    		comboChangeMethod.addItem(HttpRequestHeader.OPTIONS);
    		comboChangeMethod.addItem(HttpRequestHeader.POST);
    		comboChangeMethod.addItem(HttpRequestHeader.PUT);
    		comboChangeMethod.addItem(HttpRequestHeader.TRACE);
    		comboChangeMethod.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (comboChangeMethod.getSelectedIndex() > 0) {
						changeMethod((String) comboChangeMethod.getSelectedItem());
						comboChangeMethod.setSelectedIndex(0);
					}
				}});
    	}
    	
    	return this.comboChangeMethod;
    }
    
    private void changeMethod(String method) {
    	try {
    		HttpRequestHeader hrh =	RequestUtils.changeMethod(method, getTxtHeader().getText(), getTxtBody().getText());
			setMessage(hrh.toString(), getTxtBody().getText(), true);
		} catch (URIException e) {
			// Ignore?
			log.error(e.getMessage(), e);
		} catch (HttpMalformedHeaderException e) {
			log.error(e.getMessage(), e);
		}
    }
    
    
	public void getMessage(HttpMessage msg, boolean isRequest) {
		try {
				if (getTxtHeader().getText().length() == 0) {
					msg.getRequestHeader().clear();
					msg.getRequestBody().setBody("");
				} else {
					msg.getRequestHeader().setMessage(getHeaderFromJTextArea(getTxtHeader()));
					msg.getRequestBody().setBody(getTxtBody().getText());
					msg.getRequestHeader().setContentLength(msg.getRequestBody().length());
				}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
        
	protected void setDisplay(HttpMessage msg) {
		String header = replaceHeaderForJTextArea(msg.getRequestHeader().toString());
		String body = msg.getRequestBody().toString();

		getHttpPanelTabularModel().setText(msg.getRequestBody().toString());

		getTxtHeader().setText(header);
		getTxtHeader().setCaretPosition(0);

		getTxtBody().setText(body);
		getTxtBody().setCaretPosition(0);

		getComboView().addItem(VIEW_TABULAR);

		pluggableView(msg);
		getComboView().setEnabled(true);
	}
	
}
