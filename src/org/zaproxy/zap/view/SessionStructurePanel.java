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
 * 
 * Note that this extension ane the other classes in this package are heavily 
 * based on the orriginal Paros ExtensionSpider! 
 */
package org.zaproxy.zap.view;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ParameterParser;
import org.zaproxy.zap.model.StandardParameterParser;
import org.zaproxy.zap.utils.ZapTextField;

public class SessionStructurePanel extends AbstractContextPropertiesPanel {

	private static final String PANEL_NAME = Constant.messages.getString("context.struct.title");
	private static final long serialVersionUID = -1;
	private Context context;

	private JPanel panelSession = null;
	private ZapTextField urlKvPairSeparators = null;
	private ZapTextField urlKeyValueSeparators = null;
	private ZapTextField postKeyValueSeparators = null;
	private ZapTextField postKvPairSeparators = null;
	
	private JTable tableStructuralParams = null;
	private JScrollPane jScrollPane = null;
	private SingleColumnTableModel model = null;


	public static String getPanelName(int contextId) {
		// Panel names hav to be unique, so prefix with the context id
		return contextId + ": " + PANEL_NAME;
	}

	public SessionStructurePanel(Context context) {
		super(context.getIndex());
		this.context = context;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setLayout(new CardLayout());
		this.setName(getPanelName(this.getContextIndex()));
		this.add(getPanelSession(), getPanelSession().getName());
	}

	/**
	 * This method initializes panelSession
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelSession() {
		if (panelSession == null) {
			/*
			+----------------+-----------------------------------------+
			| | + Contexts   | 1: Structure                            |
			| |  + Include   |                                         |
			| |  + Exclude   | URL Key value pair delimiters  [ &    ] |
			| |  + Structure | URL Key value delimiters       [ =    ] |
			| |              | POST Key value pair delimiters [ &    ] |
			| |              | POST Key value delimiters      [ =    ] |
			| |              | Structural Parameters:  +-------------+ |
			| |              |                         |             | |
			| |              |                         |             | |
			| |              |                         |             | |
			| |              |                         |             | |
			| |              |                         +-------------+ |
			 */
			panelSession = new JPanel();
			panelSession.setLayout(new GridBagLayout());
			panelSession.setName("SessionStructure");
			panelSession.setLayout(new GridBagLayout());

			panelSession.add(new JLabel(Constant.messages.getString("context.struct.label.url.kvpsep")),
					LayoutHelper.getGBC(0, 0, 1, 1.0D));
			panelSession.add(getUrlKvPairSeparators(), 
					LayoutHelper.getGBC(1, 0, 1, 1.0D, new Insets(2, 0, 2, 0)));
			panelSession.add(new JLabel(Constant.messages.getString("context.struct.label.url.kvsep")),
					LayoutHelper.getGBC(0, 1, 1, 1.0D));
			panelSession.add(getUrlKeyValueSeparators(),
					LayoutHelper.getGBC(1, 1, 1, 1.0D, new Insets(2, 0, 2, 0)));
			panelSession.add(new JLabel(Constant.messages.getString("context.struct.label.post.kvpsep")),
					LayoutHelper.getGBC(0, 2, 1, 1.0D));
			panelSession.add(getPostKvPairSeparators(),
					LayoutHelper.getGBC(1, 2, 1, 1.0D, new Insets(2, 0, 2, 0)));
			panelSession.add(new JLabel(Constant.messages.getString("context.struct.label.post.kvsep")),
					LayoutHelper.getGBC(0, 3, 1, 1.0D));
			panelSession.add(getPostKeyValueSeparators(), 
					LayoutHelper.getGBC(1, 3, 1, 1.0D, new Insets(2, 0, 2, 0)));

			panelSession.add(new JLabel(Constant.messages.getString("context.struct.label.struct")),
					LayoutHelper.getGBC(0, 4, 1, 1.0D));
			panelSession.add(getJScrollPane(), 
					LayoutHelper.getGBC(1, 4, 1, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(2, 0, 2, 0)));

			panelSession.add(new JLabel(), LayoutHelper.getGBC(0, 20, 1, 1.0D, 1.0D)); // Padding
			
		}
		return panelSession;
	}

	private ZapTextField getUrlKvPairSeparators() {
		if (urlKvPairSeparators == null) {
			urlKvPairSeparators = new ZapTextField();
		}
		return urlKvPairSeparators;
	}

	private ZapTextField getUrlKeyValueSeparators() {
		if (urlKeyValueSeparators == null) {
			urlKeyValueSeparators = new ZapTextField();
		}
		return urlKeyValueSeparators;
	}

	private ZapTextField getPostKeyValueSeparators() {
		if (postKeyValueSeparators == null) {
			postKeyValueSeparators = new ZapTextField();
		}
		return postKeyValueSeparators;
	}

	private ZapTextField getPostKvPairSeparators() {
		if (postKvPairSeparators == null) {
			postKvPairSeparators = new ZapTextField();
		}
		return postKvPairSeparators;
	}
	
	private JTable getTableStructualParams() {
		if (tableStructuralParams == null) {
			tableStructuralParams = new JTable();
			tableStructuralParams.setModel(getStructuralParamsModel());
			tableStructuralParams.setRowHeight(18);
			// Issue 954: Force the JTable cell to auto-save when the focus changes.
			// Example, edit cell, click OK for a panel dialog box, the data will get saved.
			tableStructuralParams.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		}
		return tableStructuralParams;
	}
	
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTableStructualParams());
			jScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
		}
		return jScrollPane;
	}

	private SingleColumnTableModel getStructuralParamsModel() {
		if (model == null) {
			model = new SingleColumnTableModel(Constant.messages.getString("context.struct.table.header.param"));
		}
		return model;
	}


	@Override
	public void initContextData(Session session, Context context) {
		ParameterParser urlParamParser = context.getUrlParamParser();
		ParameterParser formParamParser = context.getPostParamParser();
		if (urlParamParser instanceof StandardParameterParser) {
			StandardParameterParser urlStdParamParser = (StandardParameterParser) urlParamParser;
			this.getUrlKvPairSeparators().setText(urlStdParamParser.getKeyValuePairSeparators());
			this.getUrlKeyValueSeparators().setText(urlStdParamParser.getKeyValueSeparators());

			this.getStructuralParamsModel().setLines(urlStdParamParser.getStructuralParameters());
		}
		if (formParamParser instanceof StandardParameterParser) {
			StandardParameterParser formStdParamParser = (StandardParameterParser) formParamParser;
			this.getPostKvPairSeparators().setText(formStdParamParser.getKeyValuePairSeparators());
			this.getPostKeyValueSeparators().setText(formStdParamParser.getKeyValueSeparators());
		}
	}

	@Override
	public void validateContextData(Session session) throws Exception {
		if (this.urlKvPairSeparators.getText().length() == 0) {
			throw new IllegalArgumentException(Constant.messages.getString("context.struct.warning.stdparser.nokvpsep"));
		}
		if (this.urlKeyValueSeparators.getText().length() == 0) {
			throw new IllegalArgumentException(Constant.messages.getString("context.struct.warning.stdparser.nokvsep"));
		}
		// Dont allow any common characters
		for (char ch : this.urlKvPairSeparators.getText().toCharArray()) {
			if (this.urlKeyValueSeparators.getText().contains("" + ch)) {
				throw new IllegalArgumentException(Constant.messages.getString("context.struct.warning.stdparser.dup"));
			}
		}
		
		if (this.postKvPairSeparators.getText().length() == 0) {
			throw new IllegalArgumentException(Constant.messages.getString("context.struct.warning.stdparser.nokvpsep"));
		}
		if (this.postKeyValueSeparators.getText().length() == 0) {
			throw new IllegalArgumentException(Constant.messages.getString("context.struct.warning.stdparser.nokvsep"));
		}
		// Dont allow any common characters
		for (char ch : this.postKvPairSeparators.getText().toCharArray()) {
			if (this.postKeyValueSeparators.getText().contains("" + ch)) {
				throw new IllegalArgumentException(Constant.messages.getString("context.struct.warning.stdparser.dup"));
			}
		}
	}

	@Override
	public void saveContextData(Session session) throws Exception {
		ParameterParser urlParamParser = context.getUrlParamParser();
		ParameterParser formParamParser = context.getPostParamParser();

		if (urlParamParser instanceof StandardParameterParser) {
			StandardParameterParser urlStdParamParser = (StandardParameterParser) urlParamParser;
			urlStdParamParser.setKeyValuePairSeparators(this.getUrlKvPairSeparators().getText());
			urlStdParamParser.setKeyValueSeparators(this.getUrlKeyValueSeparators().getText());
			
			urlStdParamParser.setStructuralParameters(this.getStructuralParamsModel().getLines());

			context.setUrlParamParser(urlStdParamParser);
		}
		if (formParamParser instanceof StandardParameterParser) {
			StandardParameterParser formStdParamParser = (StandardParameterParser) formParamParser;
			formStdParamParser.setKeyValuePairSeparators(this.getPostKvPairSeparators().getText());
			formStdParamParser.setKeyValueSeparators(this.getPostKeyValueSeparators().getText());
			context.setPostParamParser(formStdParamParser);
		}
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.contexts";
	}

	public void setLoginURL(String string) {
		this.getUrlKvPairSeparators().setText(string);

	}

	public void setLoginPostData(String string) {
		this.getUrlKeyValueSeparators().setText(string);
	}

	public void setLogoutURL(String string) {
		this.getPostKeyValueSeparators().setText(string);

	}

	public void setLoggedInIndicationRegex(String authIndicationRegex) {
		this.getPostKvPairSeparators().setText(authIndicationRegex);
	}

	@Override
	public void saveTemporaryContextData(Context uiSharedContext) {
	}
}
