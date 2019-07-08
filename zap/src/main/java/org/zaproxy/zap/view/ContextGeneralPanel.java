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
import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.IllegalContextNameException;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.utils.ZapTextField;

public class ContextGeneralPanel extends AbstractContextPropertiesPanel {

    private static final long serialVersionUID = -8337361808959321380L;

    private JPanel panelContext = null;
    private ZapTextField txtName = null;
    private ZapTextArea txtDescription = null;
    private JCheckBox chkInScope = null;

    public static String getPanelName(Context ctx) {
        return getPanelName(ctx.getId(), ctx.getName());
    }

    public static String getPanelName(int index, String name) {
        return index + ":" + name;
    }

    /**
     * Constructs a {@code ContextGeneralPanel} for the given context.
     *
     * @param name the name of the panel
     * @param contextId the context id
     */
    public ContextGeneralPanel(String name, int contextId) {
        super(contextId);
        this.setName(name);

        this.setLayout(new CardLayout());
        this.add(getPanelSession(), this.getName() + "gen");
    }

    @Override
    public void setName(String name) {
        if (name == null) {
            super.setName(name);
            return;
        }
        if (name.startsWith(this.getContextId() + ":")) {
            name = name.substring(name.indexOf(":") + 1);
        }
        super.setName(getPanelName(this.getContextId(), name));
    }

    /**
     * This method initializes panelSession
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPanelSession() {
        if (panelContext == null) {
            panelContext = new JPanel();
            panelContext.setLayout(new GridBagLayout());

            if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption()
                    == 0) {
                panelContext.setSize(180, 101);
            }

            panelContext.add(
                    new JLabel(Constant.messages.getString("context.label.name")),
                    LayoutHelper.getGBC(0, 0, 1, 1.0D));
            panelContext.add(getTxtName(), LayoutHelper.getGBC(0, 1, 1, 1.0D));
            panelContext.add(getChkInScope(), LayoutHelper.getGBC(0, 2, 2, 1.0D));
            panelContext.add(
                    new JLabel(Constant.messages.getString("context.label.desc")),
                    LayoutHelper.getGBC(0, 3, 1, 1.0D));
            panelContext.add(getTxtDescription(), LayoutHelper.getGBC(0, 4, 1, 1.0D, 1.0D));
        }
        return panelContext;
    }

    /**
     * This method initializes txtSessionName
     *
     * @return org.zaproxy.zap.utils.ZapTextField
     */
    private ZapTextField getTxtName() {
        if (txtName == null) {
            txtName = new ZapTextField();
        }
        return txtName;
    }

    private JCheckBox getChkInScope() {
        if (chkInScope == null) {
            chkInScope = new JCheckBox();
            chkInScope.setText(Constant.messages.getString("context.inscope.label"));
        }
        return chkInScope;
    }

    /**
     * This method initializes txtDescription
     *
     * @return org.zaproxy.zap.utils.ZapTextArea
     */
    private ZapTextArea getTxtDescription() {
        if (txtDescription == null) {
            txtDescription = new ZapTextArea();
            txtDescription.setBorder(
                    javax.swing.BorderFactory.createBevelBorder(
                            javax.swing.border.BevelBorder.LOWERED));
            txtDescription.setLineWrap(true);
        }
        return txtDescription;
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.contexts";
    }

    @Override
    public void initContextData(Session session, Context uiSharedContext) {
        getTxtName().setText(uiSharedContext.getName());
        getTxtName().discardAllEdits();
        getTxtDescription().setText(uiSharedContext.getDescription());
        getTxtDescription().discardAllEdits();
        getChkInScope().setSelected(uiSharedContext.isInScope());

        if (uiSharedContext.getName().equals(Integer.toString(uiSharedContext.getId()))
                && uiSharedContext.getIncludeInContextRegexs().size() == 1) {
            // Default to the host name in the first and only regex
            String firstRegex = uiSharedContext.getIncludeInContextRegexs().get(0);
            int startIndex = firstRegex.indexOf("://");
            if (startIndex > 0) {
                String hostPlus = firstRegex.substring(startIndex + 3);
                int endIndex = hostPlus.indexOf("\\");
                if (endIndex > 0) {
                    // By default regexes end in \E
                    hostPlus = hostPlus.substring(0, endIndex);
                }
                endIndex = hostPlus.indexOf("/");
                if (endIndex > 0) {
                    hostPlus = hostPlus.substring(0, endIndex);
                }
                getTxtName().setText(hostPlus);
            }
        }
    }

    @Override
    public void validateContextData(Session session) throws Exception {
        String name = getTxtName().getText();
        if (name == null || name.isEmpty()) {
            throw new IllegalContextNameException(
                    IllegalContextNameException.Reason.EMPTY_NAME,
                    Constant.messages.getString("context.error.name.empty"));
        }

        if (!this.getName().equals(getPanelName(this.getContextId(), name))
                && session.getContext(name) != null) {
            throw new IllegalContextNameException(
                    IllegalContextNameException.Reason.DUPLICATED_NAME,
                    Constant.messages.getString("context.error.name.duplicated"));
        }
    }

    @Override
    public void saveContextData(Session session) {
        Context context = session.getContext(this.getContextId());
        saveDataInContext(context);
        String name = getTxtName().getText();
        if (!this.getName().equals(getPanelName(this.getContextId(), name))
                && View.isInitialised()) {
            View.getSingleton().renameContext(context);
        }
    }

    @Override
    public void saveTemporaryContextData(Context uiSharedContext) {
        saveDataInContext(uiSharedContext);
    }

    private void saveDataInContext(Context context) {
        context.setName(getTxtName().getText());
        context.setDescription(getTxtDescription().getText());
        context.setInScope(getChkInScope().isSelected());
    }
}
