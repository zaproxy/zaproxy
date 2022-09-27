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
package org.zaproxy.zap.extension.pscan;

import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The GUI panel for options of the passive scanner.
 *
 * <p>It allows to change the following options:
 *
 * <ul>
 *   <li>Scan only in scope - allows to set if the passive scan should be performed only on messages
 *       that are in scope.
 * </ul>
 *
 * @since 2.6.0
 */
class PassiveScannerOptionsPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;

    private final JCheckBox scanOnlyInScopeCheckBox;
    private final JCheckBox scanFuzzerMessagesCheckBox;
    private final ZapNumberSpinner passiveScanThreads;
    private final ZapNumberSpinner maxAlertsPerRule;
    private final ZapNumberSpinner maxBodySizeInBytes;
    private final JButton clearQueue;

    public PassiveScannerOptionsPanel(ExtensionPassiveScan extPassiveScan, I18N messages) {
        setName(messages.getString("pscan.options.main.name"));

        scanOnlyInScopeCheckBox =
                new JCheckBox(messages.getString("pscan.options.main.label.scanOnlyInScope"));
        scanFuzzerMessagesCheckBox =
                new JCheckBox(messages.getString("pscan.options.main.label.scanFuzzerMessages"));
        passiveScanThreads =
                new ZapNumberSpinner(1, PassiveScanParam.PASSIVE_SCAN_DEFAULT_THREADS, 50);
        maxAlertsPerRule = new ZapNumberSpinner();
        maxBodySizeInBytes = new ZapNumberSpinner();
        clearQueue = new JButton(messages.getString("pscan.options.main.label.clearQueue"));
        clearQueue.addActionListener(al -> extPassiveScan.clearQueue());

        this.setLayout(new GridBagLayout());

        int y = 0;
        this.add(scanOnlyInScopeCheckBox, LayoutHelper.getGBC(0, ++y, 2, 1.0));
        this.add(scanFuzzerMessagesCheckBox, LayoutHelper.getGBC(0, ++y, 2, 1.0));

        JLabel pscanThreadsLabel =
                new JLabel(messages.getString("pscan.options.main.label.threads"));
        pscanThreadsLabel.setLabelFor(passiveScanThreads);
        this.add(pscanThreadsLabel, LayoutHelper.getGBC(0, ++y, 1, 1.0));
        this.add(passiveScanThreads, LayoutHelper.getGBC(1, y, 1, 1.0));

        JLabel maxAlertsLabel =
                new JLabel(messages.getString("pscan.options.main.label.maxAlertsPerRule"));
        maxAlertsLabel.setLabelFor(maxAlertsPerRule);
        this.add(maxAlertsLabel, LayoutHelper.getGBC(0, ++y, 1, 1.0));
        this.add(maxAlertsPerRule, LayoutHelper.getGBC(1, y, 1, 1.0));

        JLabel maxBodySizeLabel =
                new JLabel(messages.getString("pscan.options.main.label.maxBodySizeInBytes"));
        maxBodySizeLabel.setLabelFor(maxBodySizeInBytes);
        this.add(maxBodySizeLabel, LayoutHelper.getGBC(0, ++y, 1, 1.0));
        this.add(maxBodySizeInBytes, LayoutHelper.getGBC(1, y, 1, 1.0));
        this.add(clearQueue, LayoutHelper.getGBC(1, ++y, 1, 0.5));
        this.add(
                new ZapHtmlLabel(messages.getString("pscan.options.main.footer.threadsApply")),
                LayoutHelper.getGBC(0, ++y, 2, 1.0));

        this.add(new JLabel(""), LayoutHelper.getGBC(0, ++y, 2, 1.0, 1.0));
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam optionsParam = (OptionsParam) obj;
        PassiveScanParam pscanOptions = optionsParam.getParamSet(PassiveScanParam.class);

        scanOnlyInScopeCheckBox.setSelected(pscanOptions.isScanOnlyInScope());
        scanFuzzerMessagesCheckBox.setSelected(pscanOptions.isScanFuzzerMessages());
        passiveScanThreads.setValue(pscanOptions.getPassiveScanThreads());
        maxAlertsPerRule.setValue(pscanOptions.getMaxAlertsPerRule());
        maxBodySizeInBytes.setValue(pscanOptions.getMaxBodySizeInBytesToScan());
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam optionsParam = (OptionsParam) obj;
        PassiveScanParam pscanOptions = optionsParam.getParamSet(PassiveScanParam.class);

        pscanOptions.setScanOnlyInScope(scanOnlyInScopeCheckBox.isSelected());
        pscanOptions.setScanFuzzerMessages(scanFuzzerMessagesCheckBox.isSelected());
        pscanOptions.setPassiveScanThreads(passiveScanThreads.getValue());
        pscanOptions.setMaxAlertsPerRule(maxAlertsPerRule.getValue());
        pscanOptions.setMaxBodySizeInBytesToScan(maxBodySizeInBytes.getValue());
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.pscan.main";
    }
}
