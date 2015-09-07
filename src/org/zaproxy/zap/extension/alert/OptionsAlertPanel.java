/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP Development Team
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
package org.zaproxy.zap.extension.alert;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The GUI report options panel.
 * <p>
 * It allows to change the following report options:
 * <ul>
 * <li>The number of maximum instances of each vulnerability included in a report.</li>
 * </ul>
 * </p>
 */
public class OptionsAlertPanel extends AbstractParamPanel {

    private static final long serialVersionUID = -7541236934312940852L;

    /**
     * The name of the options panel.
     */
    private static final String NAME = Constant.messages.getString("alert.optionspanel.name");

    /**
     * The number spinner that will contain the maximum number of instances to include in a report.
     */
    private ZapNumberSpinner maxInstances = null;
    private JCheckBox mergeRelatedIssues = null;

    public OptionsAlertPanel() {
        super();
        setName(NAME);

        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));

        panel.add(getMergeRelatedIssues(), 
        		LayoutHelper.getGBC(0, 0, 1, 1.0, new Insets(2,2,2,2)));

        JLabel maxInstancesLabel = new JLabel(
        		Constant.messages.getString("alert.optionspanel.label.maxinstances"));
        maxInstancesLabel.setLabelFor(getMaxInstances());
        panel.add(maxInstancesLabel, 
        		LayoutHelper.getGBC(0, 1, 1, 1.0, new Insets(2,2,2,2)));
        panel.add(getMaxInstances(), 
        		LayoutHelper.getGBC(1, 1, 1, 1.0, new Insets(2,2,2,2)));

        add(panel);
    }

    private JCheckBox getMergeRelatedIssues() {
    	if (mergeRelatedIssues == null) {
    		mergeRelatedIssues = new JCheckBox();
    		mergeRelatedIssues.setText(
    				Constant.messages.getString("alert.optionspanel.label.mergerelated"));
    		mergeRelatedIssues.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent arg0) {
			       	getMaxInstances().setEditable(mergeRelatedIssues.isSelected());
				}});
    	}
    	return mergeRelatedIssues;
    }
    private ZapNumberSpinner getMaxInstances() {
        if (maxInstances == null) {
            maxInstances = new ZapNumberSpinner();
        }
        return maxInstances;
    }

    @Override
    public void initParam(Object obj) {
        final OptionsParam options = (OptionsParam) obj;
        final AlertParam param = options.getParamSet(AlertParam.class);

        getMaxInstances().setValue(Integer.valueOf(param.getMaximumInstances()));
        getMergeRelatedIssues().setSelected(param.isMergeRelatedIssues());
       	getMaxInstances().setEditable(param.isMergeRelatedIssues());
    }

    @Override
    public void validateParam(Object obj) throws Exception {
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        final OptionsParam options = (OptionsParam) obj;
        final AlertParam param = options.getParamSet(AlertParam.class);

        param.setMaximumInstances(getMaxInstances().getValue().intValue());
        param.setMergeRelatedIssues(getMergeRelatedIssues().isSelected());
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.alert";
    }
}