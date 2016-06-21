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

import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.view.LayoutHelper;

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
    private JCheckBox checkBoxAlwaysOnTop = null;
    private JCheckBox checkBoxInScopeOnly = null;
    private JComboBox<String> buttonMode = null;

    public BreakpointsOptionsPanel() {
        super();
        setName(Constant.messages.getString("brk.optionspanel.name"));

        this.setLayout(new CardLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));

        panel.add(getCheckBoxConfirmDropMessage(), LayoutHelper.getGBC(0, 0, 2, 1.0, new Insets(2, 2, 2, 2)));
        panel.add(getCheckBoxInScopeOnly(), LayoutHelper.getGBC(0, 1, 2, 1.0, new Insets(2, 2, 2, 2)));
        panel.add(getCheckBoxAlwaysOnTop(), LayoutHelper.getGBC(0, 2, 2, 1.0, new Insets(2, 2, 2, 2)));
        
        JLabel modeLabel = new JLabel(Constant.messages.getString("brk.optionspanel.option.breakmode.label"));
        modeLabel.setLabelFor(getButtonMode());
        panel.add(modeLabel, LayoutHelper.getGBC(0, 3, 1, 0.5));
        panel.add(getButtonMode(), LayoutHelper.getGBC(1, 3, 1, 0.5));
		panel.add(new JLabel(), LayoutHelper.getGBC(0, 10, 1, 0.5D, 1.0D));	// Spacer

        add(panel);
    }

    private JCheckBox getCheckBoxConfirmDropMessage() {
        if (checkBoxConfirmDropMessage == null) {
            checkBoxConfirmDropMessage = new JCheckBox(
                    Constant.messages.getString("brk.optionspanel.option.confirmDropMessage.label"));
        }
        return checkBoxConfirmDropMessage;
    }
    
    private JCheckBox getCheckBoxAlwaysOnTop() {
    	if (checkBoxAlwaysOnTop == null) {
    		checkBoxAlwaysOnTop = new JCheckBox(
    				Constant.messages.getString("brk.optionspanel.option.alwaysOnTop.label"));
    	}
    	return checkBoxAlwaysOnTop;
    }

    private JCheckBox getCheckBoxInScopeOnly() {
    	if (checkBoxInScopeOnly == null) {
    		checkBoxInScopeOnly = new JCheckBox(
    				Constant.messages.getString("brk.optionspanel.option.inScopeOnly.label"));
    	}
    	return checkBoxInScopeOnly;
    }

    private JComboBox<String> getButtonMode() {
    	if (buttonMode == null) {
    		buttonMode = new JComboBox<String>();
    		buttonMode.addItem(Constant.messages.getString("brk.optionspanel.option.breakmode.simple.label"));
    		buttonMode.addItem(Constant.messages.getString("brk.optionspanel.option.breakmode.dual.label"));
    	}
    	return buttonMode;
    }

    @Override
    public void initParam(Object obj) {
        final OptionsParam options = (OptionsParam) obj;
        final BreakpointsParam param = options.getParamSet(BreakpointsParam.class);

        getCheckBoxConfirmDropMessage().setSelected(param.isConfirmDropMessage());
        // Note param.alwaysOnTop will be null if the user hasnt specified a preference yet
        getCheckBoxAlwaysOnTop().setSelected(param.getAlwaysOnTop() != Boolean.FALSE);
        getCheckBoxInScopeOnly().setSelected(param.isInScopeOnly());
        getButtonMode().setSelectedIndex(param.getButtonMode()-1);
    }

    @Override
    public void validateParam(Object obj) throws Exception {
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        final OptionsParam options = (OptionsParam) obj;
        final BreakpointsParam param = options.getParamSet(BreakpointsParam.class);

        param.setConfirmDropMessage(getCheckBoxConfirmDropMessage().isSelected());
        if (param.getAlwaysOnTop() != null || ! getCheckBoxAlwaysOnTop().isSelected()) {
        	// Dont set the option if its not already set, unless the user has changed it
        	// This is so that the warning message will still be shown the first time a breakpoint is hit
        	param.setAlwaysOnTop(getCheckBoxAlwaysOnTop().isSelected());
        }
        param.setInScopeOnly(getCheckBoxInScopeOnly().isSelected());
        param.setButtonMode(this.getButtonMode().getSelectedIndex()+1);
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.breakpoints";
    }

}
