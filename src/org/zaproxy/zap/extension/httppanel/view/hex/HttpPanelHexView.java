/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.extension.httppanel.view.hex;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextArea.MessageType;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;

public class HttpPanelHexView implements HttpPanelView {

	private HttpPanelHexModel httpPanelHexModel = null;
    private static final String VIEW_HEX = Constant.messages.getString("http.panel.hexView");
	private JTable hexTableBody = null;
	private javax.swing.JScrollPane scrollHexTableBody = null;
	private boolean isEditable = false;
	private HttpPanelTextModelInterface model;
	
	public HttpPanelHexView(HttpPanelTextModelInterface model, MessageType messageType, boolean isEditable) {
		this.model = model;
		this.isEditable = isEditable;
	}
	
	@Override
	public String getName() {
		return "Hex";
	}

	@Override
	public JScrollPane getPane() {
		if (scrollHexTableBody == null) {
			scrollHexTableBody = new javax.swing.JScrollPane();
			scrollHexTableBody.setName(VIEW_HEX);
			scrollHexTableBody.setViewportView(getHexTableBody());
		}
		return scrollHexTableBody;
	}

	private JTable getHexTableBody() {
		if (hexTableBody == null) {
			hexTableBody = new JTable();
			hexTableBody.setName("");
			hexTableBody.setModel(getHttpPanelHexModel());

			hexTableBody.setGridColor(java.awt.Color.gray);
			hexTableBody.setIntercellSpacing(new java.awt.Dimension(1,1));
			hexTableBody.setRowHeight(18);
			
			hexTableBody.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			hexTableBody.getColumnModel().getColumn(0).setPreferredWidth(100);
			for (int i=1; i <= 17; i++) {
				hexTableBody.getColumnModel().getColumn(i).setPreferredWidth(30);
			}
			for (int i=17; i <= hexTableBody.getColumnModel().getColumnCount()-1; i++) {
				hexTableBody.getColumnModel().getColumn(i).setPreferredWidth(25);
			}
			
			hexTableBody.setCellSelectionEnabled(true);
			hexTableBody.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return hexTableBody;
	}

	private HttpPanelHexModel getHttpPanelHexModel() {
		if (httpPanelHexModel == null) {
			httpPanelHexModel = new HttpPanelHexModel();
		}
		return httpPanelHexModel;
	}

	@Override
	public boolean isEnabled(HttpMessage msg) {
		return true;
	}

	@Override
	public boolean hasChanged() {
		return getHttpPanelHexModel().hasChanged();
	}

	@Override
	public boolean isEditable() {
		return isEditable;
	}

	@Override
	public void setEditable(boolean editable) {
		this.isEditable  = editable;
		getHttpPanelHexModel().setEditable(editable);
	}

	@Override
	public void load() {
		getHttpPanelHexModel().setText( model.getData() );
	}

	@Override
	public void save() {
		model.setData(getHttpPanelHexModel().getText());
	}
}
