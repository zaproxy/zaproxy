/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.extension.history;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.extension.history.HistoryFilter;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpStatusCode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.ZapLabel;
import org.zaproxy.zap.view.LayoutHelper;

@SuppressWarnings("serial")
public class HistoryFilterPlusDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private JPanel jPanel = null;
    private JButton btnApply = null;
    private JButton btnCancel = null;
    private JPanel jPanel1 = null;
    private int exitResult = JOptionPane.CANCEL_OPTION;
    private HistoryFilter filter = new HistoryFilter();

    private JButton btnReset = null;
    private JPanel jPanel2 = null;

    private JList<String> methodList = null;
    private JList<Integer> codeList = null;
    private JList<String> riskList = null;
    private JList<String> confidenceList = null;
    private JList<String> tagList = null;
    private JTextArea regexInc = null;
    private JList<String> regexSamples = null;
    private JTextArea regexExc = null;
    private JButton btnRegxInc = null;
    private JButton btnRegxExc = null;

    private DefaultListModel<String> tagModel = null;

    private JScrollPane methodScroller = null;
    private JScrollPane codeScroller = null;
    private JScrollPane tagScroller = null;
    private JScrollPane riskScroller = null;
    private JScrollPane confidenceScroller = null;
    private JComboBox<String> notesComboBox = null;
    private JScrollPane urlRegxIncScroller = null;
    private JScrollPane urlRegxSamples = null;
    private JScrollPane urlRegxExcScroller = null;

    /**
     * +----------------------------------------------------------------------+ | Methods Codes Tags
     * Alerts Inc URL Regexes | | +----------+ +-----+ +-----------+ +---------------+
     * +-------------+ | | | OPTIONS | | 100 | | | | Informational | | | | | | | | | | | | Low | | |
     * | | | | | | | | | Medium | | | | | | | | | | | | High | | | | | | | | | | | +---------------+
     * +-------------+ | | | | | | | | +---------------+ Exc URL Regexes | | | | | | | | | False
     * Positive| +-------------+ | | | | | | | | | Low | | | | | | | | | | | | Medium | | | | | | |
     * | | | | | High | | | | | | | | | | | | Confirmed | | | | | +----------+ +-----+ +-----------+
     * +---------------+ +-------------+ | | Notes [Ignore [v]] Images [Include [v]] | | [ Cancel ]
     * [Clear ] [Apply ] | +----------------------------------------------------------------------+
     */

    /**
     * @throws HeadlessException
     */
    public HistoryFilterPlusDialog() throws HeadlessException {
        super();
        initialize();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     */
    public HistoryFilterPlusDialog(Frame arg0, boolean arg1) throws HeadlessException {
        super(arg0, arg1);
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setContentPane(getJPanel());
        this.setVisible(false);
        this.setTitle(Constant.messages.getString("history.filter.title"));

        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
            this.setSize(600, 300);
        }

        centreDialog();
        this.getRootPane().setDefaultButton(btnApply);
        this.setMinimumSize(new Dimension(1000, 600));
        this.pack();
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints11 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints6 = new GridBagConstraints();

            ZapLabel descLabel = new ZapLabel();
            descLabel.setLineWrap(true);
            descLabel.setWrapStyleWord(true);
            descLabel.setText(Constant.messages.getString("history.filter.label.desc"));

            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());

            gridBagConstraints6.gridwidth = 3;
            gridBagConstraints6.gridx = 0;
            gridBagConstraints6.gridy = 3;
            gridBagConstraints6.insets = new java.awt.Insets(5, 2, 5, 2);
            gridBagConstraints6.ipadx = 3;
            gridBagConstraints6.ipady = 3;
            gridBagConstraints11.gridx = 0;
            gridBagConstraints11.gridy = 0;
            gridBagConstraints11.insets = new java.awt.Insets(5, 10, 5, 10);
            gridBagConstraints11.weightx = 1.0D;
            gridBagConstraints11.gridwidth = 3;
            gridBagConstraints11.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints11.ipadx = 3;
            gridBagConstraints11.ipady = 3;
            gridBagConstraints12.gridx = 0;
            gridBagConstraints12.weighty = 1.0D;
            gridBagConstraints12.gridwidth = 3;
            gridBagConstraints12.gridy = 2;
            gridBagConstraints12.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints12.insets = new java.awt.Insets(2, 10, 2, 10);
            gridBagConstraints12.ipadx = 0;
            gridBagConstraints12.ipady = 1;
            jPanel.add(descLabel, gridBagConstraints11);
            jPanel.add(getJPanel2(), gridBagConstraints12);
            jPanel.add(getJPanel1(), gridBagConstraints6);
        }
        return jPanel;
    }

    /**
     * This method initializes btnApply
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnApply() {
        if (btnApply == null) {
            btnApply = new JButton();
            btnApply.setText(Constant.messages.getString("history.filter.button.apply"));
            btnApply.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {

                            try {
                                filter.setMethods(methodList.getSelectedValuesList());
                                filter.setCodes(codeList.getSelectedValuesList());
                                filter.setTags(tagList.getSelectedValuesList());
                                filter.setRisks(riskList.getSelectedValuesList());
                                filter.setReliabilities(confidenceList.getSelectedValuesList());
                                filter.setNote(notesComboBox.getSelectedItem());
                                filter.setUrlIncPatternList(strToRegexList(regexInc.getText()));
                                filter.setUrlExcPatternList(strToRegexList(regexExc.getText()));
                                exitResult = JOptionPane.OK_OPTION;
                                HistoryFilterPlusDialog.this.dispose();
                            } catch (PatternSyntaxException e1) {
                                // Invalid regex
                                View.getSingleton()
                                        .showWarningDialog(
                                                Constant.messages.getString(
                                                        "history.filter.badregex.warning",
                                                        e1.getMessage()));
                            }
                        }
                    });
        }
        return btnApply;
    }

    private List<Pattern> strToRegexList(String str) throws PatternSyntaxException {
        List<Pattern> list = new ArrayList<>();
        for (String s : str.split("\n")) {
            if (s.length() > 0) {
                list.add(Pattern.compile(s));
            }
        }
        return list;
    }

    /**
     * This method initializes btnCancel
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton();
            btnCancel.setText(Constant.messages.getString("all.button.cancel"));
            btnCancel.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {

                            exitResult = JOptionPane.CANCEL_OPTION;
                            HistoryFilterPlusDialog.this.dispose();
                        }
                    });
        }
        return btnCancel;
    }

    /**
     * This method initializes jPanel1
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            jPanel1 = new JPanel();
            jPanel1.add(getBtnCancel(), null);
            jPanel1.add(getBtnReset(), null);
            jPanel1.add(getBtnApply(), null);
        }
        return jPanel1;
    }

    public int showDialog() {
        this.setVisible(true);
        return exitResult;
    }

    /**
     * This method initializes btnReset
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnReset() {
        if (btnReset == null) {
            btnReset = new JButton();
            btnReset.setText(Constant.messages.getString("history.filter.button.clear"));
            btnReset.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {

                            exitResult = JOptionPane.NO_OPTION;
                            // Unset everything
                            methodList.setSelectedIndices(new int[0]);
                            codeList.setSelectedIndices(new int[0]);
                            tagList.setSelectedIndices(new int[0]);
                            riskList.setSelectedIndices(new int[0]);
                            confidenceList.setSelectedIndices(new int[0]);
                            notesComboBox.setSelectedItem(HistoryFilter.NOTES_IGNORE);
                            regexInc.setText("");
                            regexExc.setText("");
                            filter.reset();
                        }
                    });
        }
        return btnReset;
    }

    private Insets stdInset() {
        return new Insets(5, 5, 1, 5);
    }

    /**
     * This method initializes jPanel2
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (jPanel2 == null) {
            jPanel2 = new JPanel();
            jPanel2.setLayout(new GridBagLayout());

            GridBagConstraints gbc00 = LayoutHelper.getGBC(0, 0, 1, 1.0, stdInset());
            GridBagConstraints gbc01 = LayoutHelper.getGBC(1, 0, 1, 1.0, stdInset());
            GridBagConstraints gbc02 = LayoutHelper.getGBC(2, 0, 1, 1.0, stdInset());
            GridBagConstraints gbc03 = LayoutHelper.getGBC(3, 0, 1, 1.0, stdInset());
            GridBagConstraints gbc04 = LayoutHelper.getGBC(4, 0, 1, 1.0, stdInset());
            GridBagConstraints gbc06 = LayoutHelper.getGBC(6, 0, 1, 1.0, stdInset());

            GridBagConstraints gbc10 =
                    LayoutHelper.getGBC(
                            0,
                            1,
                            1,
                            3,
                            1.0,
                            1.0,
                            GridBagConstraints.BOTH,
                            GridBagConstraints.NORTHWEST,
                            stdInset());
            GridBagConstraints gbc11 =
                    LayoutHelper.getGBC(
                            1,
                            1,
                            1,
                            3,
                            1.0,
                            1.0,
                            GridBagConstraints.BOTH,
                            GridBagConstraints.NORTHWEST,
                            stdInset());
            GridBagConstraints gbc12 =
                    LayoutHelper.getGBC(
                            2,
                            1,
                            1,
                            3,
                            1.0,
                            1.0,
                            GridBagConstraints.BOTH,
                            GridBagConstraints.NORTHWEST,
                            stdInset());
            GridBagConstraints gbc13 =
                    LayoutHelper.getGBC(
                            3,
                            1,
                            1,
                            2,
                            1.0,
                            1.0,
                            GridBagConstraints.BOTH,
                            GridBagConstraints.NORTHWEST,
                            stdInset());
            GridBagConstraints gbc14 =
                    LayoutHelper.getGBC(
                            4,
                            1,
                            1,
                            1,
                            1.0,
                            1.0,
                            GridBagConstraints.BOTH,
                            GridBagConstraints.NORTHWEST,
                            stdInset());

            GridBagConstraints gbc15 =
                    LayoutHelper.getGBC(
                            5,
                            1,
                            1,
                            1,
                            1.0,
                            1.0,
                            GridBagConstraints.BOTH,
                            GridBagConstraints.NORTHWEST,
                            stdInset());

            GridBagConstraints gbc16 =
                    LayoutHelper.getGBC(
                            6,
                            1,
                            1,
                            3,
                            1.0,
                            1.0,
                            GridBagConstraints.BOTH,
                            GridBagConstraints.NORTHWEST,
                            stdInset());

            GridBagConstraints gbc24 =
                    LayoutHelper.getGBC(
                            4,
                            2,
                            1,
                            1,
                            0.0,
                            0.0,
                            GridBagConstraints.NONE,
                            GridBagConstraints.NORTHWEST,
                            stdInset());

            GridBagConstraints gbc33 =
                    LayoutHelper.getGBC(
                            3,
                            3,
                            1,
                            1,
                            1.0,
                            1.0,
                            GridBagConstraints.BOTH,
                            GridBagConstraints.NORTHWEST,
                            stdInset());

            GridBagConstraints gbc34 =
                    LayoutHelper.getGBC(
                            4,
                            3,
                            1,
                            1,
                            1.0,
                            1.0,
                            GridBagConstraints.BOTH,
                            GridBagConstraints.NORTHWEST,
                            stdInset());

            GridBagConstraints gbc35 =
                    LayoutHelper.getGBC(
                            5,
                            3,
                            1,
                            1,
                            1.0,
                            1.0,
                            GridBagConstraints.BOTH,
                            GridBagConstraints.NORTHWEST,
                            stdInset());

            jPanel2.add(
                    new JLabel(Constant.messages.getString("history.filter.label.methods")), gbc00);
            jPanel2.add(
                    new JLabel(Constant.messages.getString("history.filter.label.codes")), gbc01);
            jPanel2.add(
                    new JLabel(Constant.messages.getString("history.filter.label.tags")), gbc02);
            jPanel2.add(
                    new JLabel(Constant.messages.getString("history.filter.label.alerts")), gbc03);
            jPanel2.add(
                    new JLabel(Constant.messages.getString("history.filter.label.urlincregex")),
                    gbc04);
            jPanel2.add(new JLabel("Sample regexps:"), gbc06);

            jPanel2.add(getMethodScroller(), gbc10);
            jPanel2.add(getCodeScroller(), gbc11);
            jPanel2.add(getTagScroller(), gbc12);
            jPanel2.add(getRiskScroller(), gbc13);
            jPanel2.add(getUrlRegxIncScroller(), gbc14);
            jPanel2.add(getBtnAddRegxToInc(), gbc15);
            jPanel2.add(getUrlRegxSamples(), gbc16);

            jPanel2.add(
                    new JLabel(Constant.messages.getString("history.filter.label.urlexcregex")),
                    gbc24);

            jPanel2.add(getConfidenceScroller(), gbc33);
            jPanel2.add(getUrlRegxExcScroller(), gbc34);
            jPanel2.add(getBtnAddRegxToExc(), gbc35);

            JPanel jPanel3 = new JPanel();
            jPanel3.setLayout(new BoxLayout(jPanel3, BoxLayout.X_AXIS));
            jPanel3.add(new JLabel(Constant.messages.getString("history.filter.label.notes")));
            jPanel3.add(getNotesComboBox());
        }
        return jPanel2;
    }

    private JScrollPane getMethodScroller() {
        if (methodScroller == null) {
            methodList = new JList<>(HttpRequestHeader.METHODS);
            methodList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            methodList.setLayoutOrientation(JList.VERTICAL);
            methodList.setVisibleRowCount(HttpRequestHeader.METHODS.length);
            methodScroller = new JScrollPane(methodList);
        }
        return methodScroller;
    }

    private JScrollPane getCodeScroller() {
        if (codeScroller == null) {
            Vector<Integer> codeInts = new Vector<>(HttpStatusCode.CODES.length);
            for (int i : HttpStatusCode.CODES) {
                codeInts.add(i);
            }
            codeList = new JList<>(codeInts);
            codeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            codeList.setLayoutOrientation(JList.VERTICAL);
            codeScroller = new JScrollPane(codeList);
        }
        return codeScroller;
    }

    private JScrollPane getRiskScroller() {
        if (riskScroller == null) {
            riskList = new JList<>(Alert.MSG_RISK);
            riskList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            riskList.setLayoutOrientation(JList.VERTICAL);
            riskList.setVisibleRowCount(Alert.MSG_RISK.length);
            riskScroller = new JScrollPane(riskList);
        }
        return riskScroller;
    }

    private JScrollPane getConfidenceScroller() {
        if (confidenceScroller == null) {
            confidenceList = new JList<>(Alert.MSG_CONFIDENCE);
            confidenceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            confidenceList.setLayoutOrientation(JList.VERTICAL);
            confidenceList.setVisibleRowCount(Alert.MSG_CONFIDENCE.length);
            confidenceScroller = new JScrollPane(confidenceList);
        }
        return confidenceScroller;
    }

    private JScrollPane getUrlRegxSamples() {
        if (urlRegxSamples == null) {
            // TODO save the list in better place
            final String[] URL_REGX_INC_LIST = {
                ".*\\.(js|css|woff2|png|jpeg|jpg|gif|svg)$", ".*\\.(html|asp|jsp|php)$"
            };

            regexSamples = new JList<>(URL_REGX_INC_LIST);
            regexSamples.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            regexSamples.setLayoutOrientation(JList.VERTICAL);
            regexSamples.setVisibleRowCount(URL_REGX_INC_LIST.length);
            urlRegxSamples = new JScrollPane(regexSamples);
        }
        return urlRegxSamples;
    }

    private JButton getBtnAddRegxToInc() {
        if (btnRegxInc == null) {
            btnRegxInc = new JButton();
            btnRegxInc.setText("<");
            btnRegxInc.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {

                            exitResult = JOptionPane.NO_OPTION;

                            @SuppressWarnings("deprecation")
                            List<Object> regexVals =
                                    Arrays.asList(regexSamples.getSelectedValues());

                            String curText = regexInc.getText();
                            for (Object r : regexVals) {
                                curText =
                                        curText.isEmpty()
                                                ? r.toString()
                                                : curText + "\n" + r.toString();
                            }

                            regexInc.setText(curText);
                        }
                    });
        }
        return btnRegxInc;
    }

    private JButton getBtnAddRegxToExc() {
        if (btnRegxExc == null) {
            btnRegxExc = new JButton();
            btnRegxExc.setText("<");
            btnRegxExc.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {

                            exitResult = JOptionPane.NO_OPTION;

                            @SuppressWarnings("deprecation")
                            List<Object> regexVals =
                                    Arrays.asList(regexSamples.getSelectedValues());

                            String curText = regexExc.getText();
                            for (Object r : regexVals) {
                                curText =
                                        curText.isEmpty()
                                                ? r.toString()
                                                : curText + "\n" + r.toString();
                            }

                            regexExc.setText(curText);
                        }
                    });
        }
        return btnRegxExc;
    }

    private JScrollPane getUrlRegxIncScroller() {
        if (urlRegxIncScroller == null) {
            regexInc = new JTextArea();
            regexInc.setRows(4);
            urlRegxIncScroller = new JScrollPane(regexInc);
        }
        return urlRegxIncScroller;
    }

    private JScrollPane getUrlRegxExcScroller() {
        if (urlRegxExcScroller == null) {
            regexExc = new JTextArea();
            regexExc.setRows(4);
            urlRegxExcScroller = new JScrollPane(regexExc);
        }
        return urlRegxExcScroller;
    }

    private DefaultListModel<String> getTagModel() {
        if (tagModel == null) {
            tagModel = new DefaultListModel<>();
        }
        return tagModel;
    }

    private JScrollPane getTagScroller() {
        if (tagScroller == null) {
            tagList = new JList<>(getTagModel());
            tagList.setPrototypeCellValue("Tags are short...");
            tagScroller = new JScrollPane(tagList);
            tagScroller.setHorizontalScrollBarPolicy(
                    javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            tagScroller.setVerticalScrollBarPolicy(
                    javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        }
        return tagScroller;
    }

    private JComboBox<String> getNotesComboBox() {
        if (notesComboBox == null) {
            notesComboBox = new JComboBox<>(HistoryFilter.NOTES_OPTIONS);
        }
        return notesComboBox;
    }

    public void setAllTags(List<String> allTags) {
        List<String> selected = tagList.getSelectedValuesList();
        int[] inds = new int[allTags.size()];
        Arrays.fill(inds, -1);

        getTagModel().clear();
        int i = 0;
        for (String tag : allTags) {
            getTagModel().addElement(tag);
        }
        for (Object sel : selected) {
            if (getTagModel().contains(sel)) {
                inds[i] = getTagModel().indexOf(sel);
            }
            i++;
        }
        tagList.setSelectedIndices(inds);
    }

    public HistoryFilter getFilter() {
        return this.filter;
    }
}
