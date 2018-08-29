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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.text.DateFormatter;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXDatePicker;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.extension.history.HistoryFilter;
import org.parosproxy.paros.extension.history.LogPanel;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpStatusCode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.ZapLabel;
import org.zaproxy.zap.view.LayoutHelper;

public class HistoryFilterPlusDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private static ImageIcon ICON_ADD_HISTORY_FILTER =
            new ImageIcon(LogPanel.class.getResource("/resource/icon/16/funnel--plus.png"));
    private JPanel jPanel = null;
    private JButton btnApply = null;
    private JButton btnCancel = null;
    private JButton btnSaveAsHistoryFilter = null;
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
    private JTextArea regexExc = null;

    private DefaultListModel<String> tagModel = null;

    private JScrollPane methodScroller = null;
    private JScrollPane codeScroller = null;
    private JScrollPane tagScroller = null;
    private JScrollPane riskScroller = null;
    private JScrollPane confidenceScroller = null;
    private JComboBox<String> notesComboBox = null;
    private JScrollPane urlRegxIncScroller = null;
    private JScrollPane urlRegxExcScroller = null;
    private ExtensionHistory extensionHistory;
    private JXDatePicker startDatePicker = null;
    private JSpinner startTimeSpinner = null;
    private JCheckBox startDateTimeCheckbox = null;
    private SpinnerDateModel startTimeModel = null;

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
     * @param frame
     * @param modal
     * @throws HeadlessException
     */
    public HistoryFilterPlusDialog(Frame frame, boolean modal, ExtensionHistory extensionHistory)
            throws HeadlessException {
        super(frame, modal);
        this.extensionHistory = extensionHistory;
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
                                updateFilterFromDialog(filter);
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

    private JButton getBtnSaveAsHistoryFilter() {
        if (btnSaveAsHistoryFilter == null) {
            btnSaveAsHistoryFilter = new JButton();

            btnSaveAsHistoryFilter.setIcon(ICON_ADD_HISTORY_FILTER);
            btnSaveAsHistoryFilter.setToolTipText(
                    Constant.messages.getString("history.filter.save.button"));
            DisplayUtils.scaleIcon(btnSaveAsHistoryFilter);

            btnSaveAsHistoryFilter.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {

                            String name =
                                    (String)
                                            JOptionPane.showInputDialog(
                                                    HistoryFilterPlusDialog.this,
                                                    Constant.messages.getString(
                                                            "history.filter.save.dialog.name"),
                                                    Constant.messages.getString(
                                                            "history.filter.save.dialog.title"),
                                                    JOptionPane.PLAIN_MESSAGE,
                                                    ICON_ADD_HISTORY_FILTER,
                                                    null,
                                                    "");

                            if (StringUtils.isBlank(name)) {
                                JOptionPane.showMessageDialog(
                                        HistoryFilterPlusDialog.this,
                                        Constant.messages.getString(
                                                "history.filter.save.dialog.nameempty"),
                                        Constant.messages.getString(
                                                "history.filter.save.dialog.title"),
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            if (extensionHistory.getFilterNames().contains(name)) {
                                JOptionPane.showMessageDialog(
                                        HistoryFilterPlusDialog.this,
                                        Constant.messages.getString(
                                                "history.filter.save.dialog.duplicatedname"),
                                        Constant.messages.getString(
                                                "history.filter.save.dialog.title"),
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            HistoryFilter newFilter = getNewFilterFromDialog();
                            newFilter.setName(name);
                            newFilter.setEnabled(true);

                            extensionHistory.addFilter(newFilter);
                        }
                    });
        }
        return btnSaveAsHistoryFilter;
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
            if (extensionHistory != null) {
                jPanel1.add(getBtnSaveAsHistoryFilter(), null);
            }
        }
        return jPanel1;
    }

    public int showDialog() {
        this.setVisible(true);
        return exitResult;
    }

    public void reset() {
        filter = new HistoryFilter();
        updateDialogFromFilter(filter);
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
                            reset();
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
                            0.0,
                            0.0,
                            GridBagConstraints.BOTH,
                            GridBagConstraints.NORTHWEST,
                            stdInset());

            GridBagConstraints gbc30 = LayoutHelper.getGBC(0, 4, 2, 1.0, stdInset());

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

            jPanel2.add(getMethodScroller(), gbc10);
            jPanel2.add(getCodeScroller(), gbc11);
            jPanel2.add(getTagScroller(), gbc12);
            jPanel2.add(getRiskScroller(), gbc13);
            jPanel2.add(getUrlRegxIncScroller(), gbc14);

            jPanel2.add(
                    new JLabel(Constant.messages.getString("history.filter.label.urlexcregex")),
                    gbc24);

            jPanel2.add(getConfidenceScroller(), gbc33);
            jPanel2.add(getUrlRegxExcScroller(), gbc34);
            getUrlRegxExcScroller();

            JPanel jPanel3 = new JPanel();
            jPanel3.setLayout(new BoxLayout(jPanel3, BoxLayout.X_AXIS));
            jPanel3.add(new JLabel(Constant.messages.getString("history.filter.label.notes")));
            jPanel3.add(getNotesComboBox());

            jPanel3.add(new JLabel(Constant.messages.getString("history.filter.label.startfrom")));
            jPanel3.add(getStartDateTimeCheckbox());
            jPanel3.add(getStartDatePicker());
            jPanel3.add(getStartTimeSpinner());
            jPanel2.add(jPanel3, gbc30);
        }
        return jPanel2;
    }

    private JCheckBox getStartDateTimeCheckbox() {
        if (startDateTimeCheckbox == null) {
            startDateTimeCheckbox = new JCheckBox();
        }
        return startDateTimeCheckbox;
    }

    private JSpinner getStartTimeSpinner() {
        if (startTimeSpinner == null) {
            SpinnerDateModel model = getStartTimeModel();
            model.setValue(new Date());
            JSpinner spinner = new JSpinner(model);
            spinner.getEditor();
            JSpinner.DateEditor editor = (JSpinner.DateEditor) spinner.getEditor();
            SimpleDateFormat format = editor.getFormat();
            format.applyPattern("HH:mm:ss");
            DateFormatter formatter = (DateFormatter) editor.getTextField().getFormatter();
            formatter.setAllowsInvalid(false);
            formatter.setOverwriteMode(true);
            startTimeSpinner = spinner;
        }
        return startTimeSpinner;
    }

    private SpinnerDateModel getStartTimeModel() {
        if (startTimeModel == null) {
            startTimeModel = new SpinnerDateModel();
        }
        return startTimeModel;
    }

    private JXDatePicker getStartDatePicker() {
        if (startDatePicker == null) {
            startDatePicker = new JXDatePicker(new Date());
        }
        return startDatePicker;
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

    private JScrollPane getUrlRegxIncScroller() {
        if (urlRegxIncScroller == null) {
            urlRegxIncScroller = new JScrollPane(getRegexIncArea());
        }
        return urlRegxIncScroller;
    }

    private JTextArea getRegexIncArea() {
        if (regexInc == null) {
            regexInc = new JTextArea();
            regexInc.setRows(4);
        }
        return regexInc;
    }

    private JScrollPane getUrlRegxExcScroller() {
        if (urlRegxExcScroller == null) {
            urlRegxExcScroller = new JScrollPane(getRegexExcTextArea());
        }
        return urlRegxExcScroller;
    }

    private JTextArea getRegexExcTextArea() {
        if (regexExc == null) {
            regexExc = new JTextArea();
            regexExc.setRows(5);
        }
        return regexExc;
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

    public HistoryFilter getNewFilterFromDialog() {
        HistoryFilter newHistoryFilter = new HistoryFilter();
        updateFilterFromDialog(newHistoryFilter);
        return newHistoryFilter;
    }

    private void updateFilterFromDialog(HistoryFilter filter) {

        String note = null;
        if (notesComboBox.getSelectedItem() != null) {
            note = notesComboBox.getSelectedItem().toString();
        }

        filter.setMethods(methodList.getSelectedValuesList());
        filter.setCodes(codeList.getSelectedValuesList());
        filter.setTags(tagList.getSelectedValuesList());
        filter.setRisks(riskList.getSelectedValuesList());
        filter.setReliabilities(confidenceList.getSelectedValuesList());
        filter.setNote(note);
        filter.setUrlIncPatternList(strToRegexList(regexInc.getText()));
        filter.setUrlExcPatternList(strToRegexList(regexExc.getText()));
        filter.setStartTimeSentInMs(getStartDateTime());
    }

    public void setFilter(HistoryFilter filter) {
        updateDialogFromFilter(filter);
        this.filter = getNewFilterFromDialog();
    }

    private void updateDialogFromFilter(HistoryFilter filter) {
        notesComboBox.setSelectedItem(filter.getNote());
        getRegexIncArea().setText(convertPatternToString(filter.getUrlIncPatternList()));
        getRegexExcTextArea().setText(convertPatternToString(filter.getUrlExcPatternList()));
        select(this.methodList, filter.getMethodList());
        select(this.confidenceList, filter.getConfidenceList());
        select(this.riskList, filter.getRiskList());
        select(this.tagList, filter.getTagList());
        select(this.codeList, filter.getCodeList());
        setStartDateTime(filter.getStartTimeSentInMs());
    }

    private void setStartDateTime(Optional<Long> dateTimeInMs) {
        getStartDateTimeCheckbox().setSelected(dateTimeInMs.isPresent());
        Date date = new Date();
        if (dateTimeInMs.isPresent()) {
            date = new Date(dateTimeInMs.get());
        }

        getStartDatePicker().setDate(date);
        getStartTimeModel().setValue(date);
    }

    private Optional<Long> getStartDateTime() {
        if (!getStartDateTimeCheckbox().isSelected()) {
            return Optional.empty();
        }

        Date date = getStartDatePicker().getDate();
        Date time = getStartTimeModel().getDate();

        Calendar calDate = Calendar.getInstance();
        calDate.setTime(date);

        Calendar calTime = Calendar.getInstance();
        calTime.setTime(time);

        calDate.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
        calDate.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
        calDate.set(Calendar.SECOND, calTime.get(Calendar.SECOND));
        calDate.set(Calendar.MILLISECOND, 0);

        return Optional.of(calDate.getTime().getTime());
    }

    private <T> void select(JList<T> list, List<T> values) {
        int[] selected = getIndicesOf(list, values);
        list.setSelectedIndices(selected);
    }

    private <T> int[] getIndicesOf(JList<T> list, List<T> values) {
        return values.stream().mapToInt(m -> getIndexOf(list, m)).filter(i -> i >= 0).toArray();
    }

    private <T> int getIndexOf(JList<T> list, T value) {
        for (int i = 0; i < list.getModel().getSize(); i++) {
            T valueAtIndex = list.getModel().getElementAt(i);
            if (ObjectUtils.equals(value, valueAtIndex)) {
                return i;
            }
        }
        return -1;
    }

    private String convertPatternToString(List<Pattern> urlExcPatternList) {
        StringBuilder regEx = new StringBuilder();
        for (Pattern pattern : urlExcPatternList) {
            regEx.append(pattern.pattern());
            regEx.append("\n");
        }
        return regEx.toString();
    }
}
