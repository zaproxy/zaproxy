/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development Team
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

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.model.Context;

public class ContextTechnologyPanel extends AbstractContextPropertiesPanel {

	private static final String PANEL_NAME = Constant.messages.getString("context.technology.title");
	private static final long serialVersionUID = -8337361808959321380L;

	private JPanel panelSession = null;
	private TechnologyTreePanel techPanel;

	public static String getPanelName(int contextId) {
		// Panel names have to be unique, so precede with the context id
		return contextId + ": " + PANEL_NAME;
	}

	public ContextTechnologyPanel(Context context) {
		super(context.getIndex());
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setLayout(new CardLayout());
		this.setName(getPanelName(getContextIndex()));
		this.add(getPanelSession(), getPanelSession().getName());
	}

	/**
	 * This method initializes panelSession
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelSession() {
		if (panelSession == null) {

			panelSession = new JPanel();
			panelSession.setLayout(new BorderLayout());
			panelSession.setName("SessionTech");
			panelSession.add(getTechTree(), BorderLayout.CENTER);
		}
		return panelSession;
	}

	private TechnologyTreePanel getTechTree() {
		if (techPanel == null) {
			techPanel = new TechnologyTreePanel(Constant.messages.getString("context.technology.tree.root"));
		}
		return techPanel;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.contexts";
	}

	@Override
	public void initContextData(Session session, Context uiContext) {
		// Init model from context
		getTechTree().setTechSet(uiContext.getTechSet());
	}

	@Override
	public void validateContextData(Session session) throws Exception {
		// Nothing to validate

	}

	@Override
	public void saveContextData(Session session) throws Exception {

		session.getContext(getContextIndex()).setTechSet(getTechTree().getTechSet());

	}

	@Override
	public void saveTemporaryContextData(Context uiSharedContext) {
		uiSharedContext.setTechSet(getTechTree().getTechSet());
	}

}
