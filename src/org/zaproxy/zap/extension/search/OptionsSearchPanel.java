/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.search;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.ZapNumberSpinner;

/**
 * The GUI search options panel.
 * <p>
 * It allows to change the following search options:
 * <ul>
 * <li>Maximum search results in GUI - allows to set the maximum number of results that should be shown in GUI results panel.</li>
 * </ul>
 * </p>
 */
public class OptionsSearchPanel extends AbstractParamPanel {

    private static final long serialVersionUID = -7541236934312940852L;

    /**
     * The name of the options panel.
     */
    private static final String NAME = Constant.messages.getString("search.optionspanel.name");

    /**
     * The label for the "Maximum search results in GUI" option.
     */
    private static final String MAX_SEARCH_RESULTS_GUI_LABEL = Constant.messages.getString("search.optionspanel.option.max.resutls.gui");

    /**
     * The number spinner that will contain the maximum number of results that should be shown in GUI.
     */
    private ZapNumberSpinner numberSpinnerMaxSearchResultsGUI = null;

    public OptionsSearchPanel() {
        super();
        setName(NAME);

        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new java.awt.Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(MAX_SEARCH_RESULTS_GUI_LABEL), gbc);
        gbc.gridx = 1;
        panel.add(getNumberSpinnerMaxSearchResultsGUI(), gbc);

        add(panel);
    }

    private ZapNumberSpinner getNumberSpinnerMaxSearchResultsGUI() {
        if (numberSpinnerMaxSearchResultsGUI == null) {
            numberSpinnerMaxSearchResultsGUI = new ZapNumberSpinner();
        }
        return numberSpinnerMaxSearchResultsGUI;
    }

    @Override
    public void initParam(Object obj) {
        final OptionsParam options = (OptionsParam) obj;
        final SearchParam param = options.getParamSet(SearchParam.class);

        numberSpinnerMaxSearchResultsGUI.setValue(Integer.valueOf(param.getMaximumSearchResultsGUI()));
    }

    @Override
    public void validateParam(Object obj) throws Exception {
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        final OptionsParam options = (OptionsParam) obj;
        final SearchParam param = options.getParamSet(SearchParam.class);

        param.setMaximumSearchResultsGUI(numberSpinnerMaxSearchResultsGUI.getValue().intValue());
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.search";
    }
}