/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 ZAP development team
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
package org.zaproxy.zap.extension.brk;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;

/**
 * The GUI breakpoints options panel.
 * <p>
 * It allows to change the following breakpoints options:
 * <ul>
 * <li>Confirm drop message - asks for confirmation when a trapped message is dropped.</li>
 * </ul>
 * </p>
 * 
 * @see org.zaproxy.zap.extension.brk.BreakPanelToolbarFactory#getBtnDrop()
 */
public class BreakpointsOptionsPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 7483614036849207715L;

    private JCheckBox checkBoxConfirmDropMessage = null;

    public BreakpointsOptionsPanel() {
        super();
        setName(Constant.messages.getString("brk.optionspanel.name"));

        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));

        GridBagConstraints gbc = new GridBagConstraints();

        panel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new java.awt.Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(getCheckBoxConfirmDropMessage(), gbc);

        add(panel);
    }

    private JCheckBox getCheckBoxConfirmDropMessage() {
        if (checkBoxConfirmDropMessage == null) {
            checkBoxConfirmDropMessage = new JCheckBox(
                    Constant.messages.getString("brk.optionspanel.option.confirmDropMessage.label"));
        }
        return checkBoxConfirmDropMessage;
    }

    @Override
    public void initParam(Object obj) {
        final OptionsParam options = (OptionsParam) obj;
        final BreakpointsParam param = (BreakpointsParam) options.getParamSet(BreakpointsParam.class);

        checkBoxConfirmDropMessage.setSelected(param.isConfirmDropMessage());
    }

    @Override
    public void validateParam(Object obj) throws Exception {
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        final OptionsParam options = (OptionsParam) obj;
        final BreakpointsParam param = (BreakpointsParam) options.getParamSet(BreakpointsParam.class);

        param.setConfirmDropMessage(checkBoxConfirmDropMessage.isSelected());
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.breakpoints";
    }

}
