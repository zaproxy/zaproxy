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

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;

public class ContextExcludePanel extends AbstractContextPropertiesPanel {

	private static final String PANEL_NAME = Constant.messages.getString("context.scope.exclude.title"); 
	private static final long serialVersionUID = -8337361808959321380L;
	
	private JPanel panelSession = null;
	private MultipleRegexesOptionsPanel regexesPanel;
	
    /**
     * Returns the name of the panel "Exclude from context" for the given {@code contextIndex}.
     * 
     * @param contextIndex the context index that will be used to create the name of the panel
     * @return the name of the panel "Exclude from context" for the given {@code contextIndex}
     * @since 2.2.0
     * @see Context#getIndex()
     */
	public static String getPanelName(int contextIndex) {
		// Panel names have to be unique, so precede with the context index
		return contextIndex + ": " + PANEL_NAME;
	}
	
    /**
     * @deprecated (2.2.0) Replaced by {@link #getPanelName(int)}. It will be removed in a future release.
     */
    @Deprecated
    @SuppressWarnings("javadoc")
    public static String getPanelName(Context context) {
        return getPanelName(context.getIndex());
    }

    public ContextExcludePanel(Context context) {
        super(context.getIndex());
 		initialize();
   }
    
	/**
	 * This method initializes this
	 */
	private void initialize() {
        regexesPanel = new MultipleRegexesOptionsPanel(View.getSingleton().getSessionDialog());

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
			panelSession.setLayout(new GridBagLayout());
			panelSession.setName("ExcludeFromScope");

			java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
	        java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

	        javax.swing.JLabel jLabel = new JLabel();

	        jLabel.setText(Constant.messages.getString("context.label.exclude"));
	        gridBagConstraints1.gridx = 0;
	        gridBagConstraints1.gridy = 0;
	        gridBagConstraints1.gridheight = 1;
	        gridBagConstraints1.insets = new java.awt.Insets(10,0,5,0);
	        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
	        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints1.weightx = 0.0D;

	        gridBagConstraints2.gridx = 0;
	        gridBagConstraints2.gridy = 1;
	        gridBagConstraints2.weightx = 1.0;
	        gridBagConstraints2.weighty = 1.0;
	        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
	        gridBagConstraints2.ipadx = 0;
	        gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
	        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
	        panelSession.add(jLabel, gridBagConstraints1);
	        panelSession.add(regexesPanel, gridBagConstraints2);
		}
		return panelSession;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.contexts";
	}

	@Override
	public void initContextData(Session session, Context uiContext) {
		regexesPanel.setRegexes(uiContext.getExcludeFromContextRegexs());
	}

	@Override
	public void validateContextData(Session session) throws Exception {
		// Nothing to do, the regular expressions are already validated when manually added and
		// regular expressions added programmatically are expected to be valid.
	}

	@Override
	public void saveContextData(Session session) throws Exception {
		Context context = session.getContext(getContextIndex());
		context.setExcludeFromContextRegexs(regexesPanel.getRegexes());
	}

	@Override
	public void saveTemporaryContextData(Context uiSharedContext) {
		uiSharedContext.setExcludeFromContextRegexs(regexesPanel.getRegexes());
	}

}
