/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.view;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;

public class SessionExcludeFromProxyPanel extends AbstractParamPanel {

    public static final String PANEL_NAME =
            Constant.messages.getString("session.proxy.exclude.title");
    private static final long serialVersionUID = -8337361808959321380L;

    private JPanel panelSession = null;
    private MultipleRegexesOptionsPanel regexesPanel;

    public SessionExcludeFromProxyPanel() {
        super();
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(PANEL_NAME);
        regexesPanel = new MultipleRegexesOptionsPanel(View.getSingleton().getSessionDialog());
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
            panelSession.setName("Ignoreproxy");

            java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();

            javax.swing.JLabel jLabel = new JLabel();

            jLabel.setText(Constant.messages.getString("session.proxy.label.ignore"));
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.gridheight = 1;
            gridBagConstraints1.insets = new java.awt.Insets(10, 0, 5, 0);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.weightx = 0.0D;

            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 1;
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.weighty = 1.0;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints2.ipadx = 0;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;

            JLabel noteLabel = new JLabel();
            noteLabel.setText(
                    Constant.messages.getString("options.globalexcludeurl.seeglobalconfig"));

            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.gridy = 2;
            gridBagConstraints3.gridheight = 1;
            gridBagConstraints3.weightx = 1.0;
            gridBagConstraints3.weighty = 0.0;
            gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints3.ipadx = 0;
            gridBagConstraints3.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.SOUTH;

            panelSession.add(jLabel, gridBagConstraints1);
            panelSession.add(regexesPanel, gridBagConstraints2);
            panelSession.add(noteLabel, gridBagConstraints3);
        }
        return panelSession;
    }

    @Override
    public void initParam(Object obj) {
        Session session = (Session) obj;
        regexesPanel.setRegexes(session.getExcludeFromProxyRegexs());
        regexesPanel.setRemoveWithoutConfirmation(
                !Model.getSingleton()
                        .getOptionsParam()
                        .getViewParam()
                        .isConfirmRemoveProxyExcludeRegex());
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        Session session = (Session) obj;
        session.setExcludeFromProxyRegexs(regexesPanel.getRegexes());
        Model.getSingleton()
                .getOptionsParam()
                .getViewParam()
                .setConfirmRemoveProxyExcludeRegex(!regexesPanel.isRemoveWithoutConfirmation());
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.sessprop";
    }
}
