/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.extension.stats;

import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapPortNumberSpinner;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

public class OptionsStatsPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private JPanel panelMisc;
    private JCheckBox inMemoryStatsEnabledField;
    private JCheckBox statsdStatsEnabledField;
    private ZapTextField statsdHostField;
    private ZapPortNumberSpinner statsdPortField;
    private ZapTextField statsdPrefixField;

    public OptionsStatsPanel() {
        super();
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("stats.options.title"));
        this.add(getPanelMisc(), getPanelMisc().getName());
    }

    /**
     * This method initializes panelMisc
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPanelMisc() {
        if (panelMisc == null) {
            panelMisc = new JPanel();
            panelMisc.setLayout(new GridBagLayout());
            panelMisc.add(getInMemoryStatsEnabledField(), LayoutHelper.getGBC(0, 0, 1, 0.5));

            panelMisc.add(getStatsdStatsEnabledField(), LayoutHelper.getGBC(0, 1, 1, 0.5));

            JPanel statsdPanel = new JPanel();
            statsdPanel.setLayout(new GridBagLayout());
            statsdPanel.setBorder(
                    BorderFactory.createTitledBorder(
                            null,
                            Constant.messages.getString("stats.options.statsd.panel"),
                            TitledBorder.DEFAULT_JUSTIFICATION,
                            TitledBorder.DEFAULT_POSITION,
                            FontUtils.getFont(FontUtils.Size.standard)));

            statsdPanel.add(
                    new JLabel(Constant.messages.getString("stats.options.label.statsd.host")),
                    LayoutHelper.getGBC(0, 1, 1, 0.5));
            statsdPanel.add(this.getStatsdHostField(), LayoutHelper.getGBC(1, 1, 1, 0.5));

            statsdPanel.add(
                    new JLabel(Constant.messages.getString("stats.options.label.statsd.port")),
                    LayoutHelper.getGBC(0, 2, 1, 0.5));
            statsdPanel.add(this.getStatsdPortField(), LayoutHelper.getGBC(1, 2, 1, 0.5));

            statsdPanel.add(
                    new JLabel(Constant.messages.getString("stats.options.label.statsd.prefix")),
                    LayoutHelper.getGBC(0, 3, 1, 0.5));
            statsdPanel.add(this.getStatsdPrefixField(), LayoutHelper.getGBC(1, 3, 1, 0.5));

            panelMisc.add(statsdPanel, LayoutHelper.getGBC(0, 2, 1, 1.0D));

            panelMisc.add(new JLabel(), LayoutHelper.getGBC(0, 10, 1, 0.5D, 1.0D)); // Spacer
        }
        return panelMisc;
    }

    private JCheckBox getInMemoryStatsEnabledField() {
        if (inMemoryStatsEnabledField == null) {
            inMemoryStatsEnabledField = new JCheckBox();
            inMemoryStatsEnabledField.setText(
                    Constant.messages.getString("stats.options.mem.enabled"));
            inMemoryStatsEnabledField.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            inMemoryStatsEnabledField.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        }
        return inMemoryStatsEnabledField;
    }

    private JCheckBox getStatsdStatsEnabledField() {
        if (statsdStatsEnabledField == null) {
            statsdStatsEnabledField = new JCheckBox();
            statsdStatsEnabledField.setText(
                    Constant.messages.getString("stats.options.statsd.enabled"));
            statsdStatsEnabledField.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            statsdStatsEnabledField.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
            statsdStatsEnabledField.addActionListener(
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            setStatsdFieldStates();
                        }
                    });
        }
        return statsdStatsEnabledField;
    }

    private void setStatsdFieldStates() {
        getStatsdHostField().setEnabled(statsdStatsEnabledField.isSelected());
        getStatsdPortField().setEnabled(statsdStatsEnabledField.isSelected());
        getStatsdPrefixField().setEnabled(statsdStatsEnabledField.isSelected());
    }

    private ZapTextField getStatsdHostField() {
        if (statsdHostField == null) {
            statsdHostField = new ZapTextField();
        }
        return statsdHostField;
    }

    private ZapPortNumberSpinner getStatsdPortField() {
        if (statsdPortField == null) {
            statsdPortField = new ZapPortNumberSpinner(0);
        }
        return statsdPortField;
    }

    private ZapTextField getStatsdPrefixField() {
        if (statsdPrefixField == null) {
            statsdPrefixField = new ZapTextField();
        }
        return statsdPrefixField;
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam optionsParam = (OptionsParam) obj;
        StatsParam statsParam = optionsParam.getParamSet(StatsParam.class);

        getInMemoryStatsEnabledField().setSelected(statsParam.isInMemoryEnabled());
        getStatsdStatsEnabledField().setSelected(statsParam.isStatsdEnabled());
        getStatsdHostField().setText(statsParam.getStatsdHost());
        getStatsdHostField().discardAllEdits();
        getStatsdPortField().setValue(statsParam.getStatsdPort());
        getStatsdPrefixField().setText(statsParam.getStatsdPrefix());
        getStatsdPrefixField().discardAllEdits();

        setStatsdFieldStates();
    }

    @Override
    public void validateParam(Object obj) throws Exception {
        if (getStatsdStatsEnabledField().isSelected()) {
            // Basic hostname validation
            try {
                InetAddress.getByName(getStatsdHostField().getText());
            } catch (Exception e) {
                throw new Exception(
                        Constant.messages.getString("stats.options.error.statsd.host.bad"), e);
            }
        }
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam optionsParam = (OptionsParam) obj;
        StatsParam statsParam = optionsParam.getParamSet(StatsParam.class);

        statsParam.setInMemoryEnabled(getInMemoryStatsEnabledField().isSelected());

        if (getStatsdStatsEnabledField().isSelected()) {
            statsParam.setStatsdHost(getStatsdHostField().getText());
            statsParam.setStatsdPort(getStatsdPortField().getValue());
            statsParam.setStatsdPrefix(getStatsdPrefixField().getText());
        } else {
            statsParam.setStatsdHost("");
        }
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.stats";
    }
}
