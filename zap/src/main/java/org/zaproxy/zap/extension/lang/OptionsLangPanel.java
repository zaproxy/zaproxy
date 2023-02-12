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
package org.zaproxy.zap.extension.lang;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.LocaleUtils;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.ViewLocale;

@SuppressWarnings("serial")
public class OptionsLangPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private JPanel panelLang = null;
    private JLabel languageLabel = null;
    private JLabel importLabel = null;
    private JLabel restartLabel = null;
    private JButton selectionButton = null;
    private JButton importButton = null;
    private JComboBox<ViewLocale> localeSelect = null;
    private ZapTextField fileTextField = null;
    private Document fileTextFieldDoc = null;
    private JCheckBox useSystemsLocaleFormatCheckbox;

    public OptionsLangPanel() {
        super();
        initialize();
    }

    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("options.lang.title"));
        this.add(getPanelLang(), getPanelLang().getName());
    }

    private JPanel getPanelLang() {
        if (panelLang == null) {
            panelLang = new JPanel();
            panelLang.setName(Constant.messages.getString("options.lang.title"));
            panelLang.setLayout(new GridBagLayout());
            if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption()
                    == 0) {
                panelLang.setSize(409, 268);
            }

            languageLabel = new JLabel(Constant.messages.getString("options.lang.selector.label"));
            importLabel = new JLabel(Constant.messages.getString("options.lang.importer.label"));
            restartLabel =
                    new ZapHtmlLabel(Constant.messages.getString("options.lang.label.restart"));

            panelLang.add(languageLabel, getGridBagConstraints(0, 0, 0.5, 0, 0, 0, 0));
            panelLang.add(getLocaleSelect(), getGridBagConstraints(1, 0, 0.5, 0, 0, 0, 0));

            panelLang.add(
                    getUseSystemsLocaleFormatCheckbox(),
                    getGridBagConstraints(0, 1, 0.5, 0, 0, 0, 0));

            panelLang.add(importLabel, getGridBagConstraints(0, 2, 1.0, 0, 2, 0, 0));

            panelLang.add(getFileTextField(), getGridBagConstraints(0, 3, 1.0, 0, 2, 0, 0));

            JPanel buttons = new JPanel();
            buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
            buttons.add(Box.createHorizontalGlue());
            buttons.add(getImportButton());
            buttons.add(getSelectionButton());

            panelLang.add(
                    buttons,
                    getGridBagConstraints(0, 4, 0.5, 0, 2, 0, GridBagConstraints.NORTHEAST));

            panelLang.add(restartLabel, getGridBagConstraints(0, 5, 1.0, 0, 2, 0, 0));

            panelLang.add(
                    new JLabel(""),
                    getGridBagConstraints(0, 6, 1.0, 1.0, 2, GridBagConstraints.BOTH, 0));
        }
        return panelLang;
    }

    private GridBagConstraints getGridBagConstraints(
            int x, int y, double weightx, double weighty, int columnWidth, int fill, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.insets = new java.awt.Insets(2, 2, 2, 2);
        gbc.gridwidth = (columnWidth > 0) ? columnWidth : 1;
        gbc.fill = (fill > 0) ? fill : GridBagConstraints.HORIZONTAL;
        gbc.anchor = (anchor > 0) ? anchor : GridBagConstraints.NORTHWEST;

        return gbc;
    }

    private ZapTextField getFileTextField() {
        if (fileTextField == null) {
            fileTextField = new ZapTextField();
            fileTextFieldDoc = fileTextField.getDocument();

            fileTextFieldDoc.addDocumentListener(
                    new DocumentListener() {
                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            updated(e);
                        }

                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            updated(e);
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            updated(e);
                        }

                        private void updated(DocumentEvent e) {
                            try {
                                String inputString =
                                        e.getDocument().getText(0, e.getDocument().getLength());
                                importButton.setEnabled(inputString.endsWith(".zaplang"));
                            } catch (BadLocationException e1) {
                                // logger.error(e1.getMessage());
                            }
                        }
                    });
        }
        return fileTextField;
    }

    private JButton getSelectionButton() {
        if (selectionButton == null) {
            selectionButton = new JButton();
            selectionButton.setText(Constant.messages.getString("options.lang.importer.browse"));
            selectionButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            browseButtonActionPerformed(evt);
                        }
                    });
        }
        return selectionButton;
    }

    private JButton getImportButton() {
        if (importButton == null) {
            importButton = new JButton();
            importButton.setEnabled(false);
            importButton.setText(Constant.messages.getString("options.lang.importer.button"));
            importButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            if (!fileTextField.getText().equals("")) {
                                LangImporter.importLanguagePack(fileTextField.getText());
                                fileTextField.setText("");
                                fileTextField.discardAllEdits();
                                loadLocales();
                            }
                        }
                    });
        }
        return importButton;
    }

    private JComboBox<ViewLocale> getLocaleSelect() {
        if (localeSelect == null) {
            localeSelect = new JComboBox<>();
            loadLocales();
        }
        return localeSelect;
    }

    private JCheckBox getUseSystemsLocaleFormatCheckbox() {
        if (useSystemsLocaleFormatCheckbox == null) {
            String i18nPrefix = "options.lang.usesystemslocaleformat.";
            useSystemsLocaleFormatCheckbox =
                    new JCheckBox(
                            Constant.messages.getString(
                                    i18nPrefix + "label", Constant.getSystemsLocale().toString()));
            useSystemsLocaleFormatCheckbox.setToolTipText(
                    Constant.messages.getString(i18nPrefix + "tooltip"));
        }
        return useSystemsLocaleFormatCheckbox;
    }

    private void browseButtonActionPerformed(ActionEvent evt) {
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(
                new FileNameExtensionFilter(
                        Constant.messages.getString("options.lang.file.chooser.description"),
                        "zaplang"));

        final int state = fc.showOpenDialog(null);

        if (state == JFileChooser.APPROVE_OPTION) {
            fileTextField.setText(fc.getSelectedFile().toString());
            fileTextField.discardAllEdits();
        }
    }

    private void loadLocales() {
        localeSelect.removeAllItems();
        for (ViewLocale locale : LocaleUtils.getAvailableViewLocales()) {
            localeSelect.addItem(locale);
        }
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam options = (OptionsParam) obj;
        OptionsParamView viewParam = options.getViewParam();
        ViewLocale locale = LocaleUtils.getViewLocale(viewParam.getLocale());
        localeSelect.setSelectedItem(locale);

        useSystemsLocaleFormatCheckbox.setSelected(viewParam.isUseSystemsLocaleForFormat());
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam options = (OptionsParam) obj;
        OptionsParamView viewParam = options.getViewParam();
        ViewLocale selectedLocale = (ViewLocale) localeSelect.getSelectedItem();
        if (selectedLocale != null) {
            viewParam.setLocale(selectedLocale.getLocale());
        }
        viewParam.setUseSystemsLocaleForFormat(getUseSystemsLocaleFormatCheckbox().isSelected());
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.language";
    }
}
