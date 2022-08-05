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
package org.zaproxy.zap.view;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.widgets.WritableFileChooser;

/**
 * A {@code MultipleOptionsTablePanel} to manage regular expressions.
 *
 * @since 2.5.0
 */
@SuppressWarnings("serial")
public class MultipleRegexesOptionsPanel extends AbstractMultipleOptionsBaseTablePanel<String> {

    private static final long serialVersionUID = 1041782873016590998L;

    private static final String REMOVE_DIALOG_TITLE =
            Constant.messages.getString("multiple.options.regexes.dialog.remove.regex.title");
    private static final String REMOVE_DIALOG_TEXT =
            Constant.messages.getString("multiple.options.regexes.dialog.remove.regex.text");

    private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL =
            Constant.messages.getString(
                    "multiple.options.regexes.dialog.remove.regex.button.confirm");
    private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL =
            Constant.messages.getString(
                    "multiple.options.regexes.dialog.remove.regex.button.cancel");

    private static final String REMOVE_DIALOG_CHECKBOX_LABEL =
            Constant.messages.getString(
                    "multiple.options.regexes.dialog.remove.regex.checkbox.label");

    private DialogAddRegex addDialog;
    private DialogModifyRegex modifyDialog;

    private final Dialog owner;

    public MultipleRegexesOptionsPanel(Dialog owner) {
        this(owner, new RegexesTableModel());
    }

    public MultipleRegexesOptionsPanel(Dialog owner, RegexesTableModel model) {
        super(model);

        this.owner = owner;
        getTable().setSortOrder(0, SortOrder.ASCENDING);
        JButton importButton =
                new JButton(
                        Constant.messages.getString(
                                "multiple.options.regexes.dialog.import.button"));
        importButton.addActionListener(e -> importButtonClicked());
        addButton(importButton);

        JButton exportButton =
                new JButton(
                        Constant.messages.getString(
                                "multiple.options.regexes.dialog.export.button"));
        exportButton.addActionListener(e -> exportRegexes());
        addButton(exportButton);
    }

    private void exportRegexes() {
        WritableFileChooser fc = new WritableFileChooser();
        if (fc.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fc.getSelectedFile();
        String lineSeparator = System.getProperty("line.separator", "\n");
        try (Writer writer = Files.newBufferedWriter(selectedFile.toPath())) {
            for (String regex : getRegexes()) {
                writer.append(regex).append(lineSeparator);
            }
        } catch (IOException e) {
            View.getSingleton()
                    .showWarningDialog(
                            Constant.messages.getString(
                                    "multiple.options.regexes.dialog.export.error",
                                    e.getLocalizedMessage()));
        }
    }

    /**
     * Checks if {@code trimmedRegex} is contained in {@code regexes} and if it is a valid regex
     * pattern. If {@code regexes} is null, then the duplication check is omitted
     *
     * @param trimmedRegex A regex as string. Must not contain any leading or trailing spaces (need
     *     to be trimmed)
     * @param regexes List of regex strings
     * @return True if {@code trimmedRegex} is not a duplicate and the pattern is valid.
     * @throws PatternSyntaxException If {@code trimmedRegex} is not a valid regex pattern
     */
    private static boolean validateRegexAndCheckForDuplicates(
            String trimmedRegex, List<String> regexes) throws PatternSyntaxException {
        if (trimmedRegex == null
                || trimmedRegex.isEmpty()
                || regexes != null && regexes.contains(trimmedRegex)) {
            return false;
        }
        Pattern.compile(trimmedRegex, Pattern.CASE_INSENSITIVE);
        return true;
    }

    private void importButtonClicked() {
        File file = selectImportFile();
        if (file != null) {
            List<String> regexes = new ArrayList<>(getRegexes());

            try (BufferedReader br =
                    Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    String trimmedLine = line.trim();
                    if (validateRegexAndCheckForDuplicates(trimmedLine, regexes)) {
                        regexes.add(trimmedLine);
                    }
                }
            } catch (IOException | PatternSyntaxException e) {
                View.getSingleton()
                        .showWarningDialog(
                                Constant.messages.getString(
                                        "multiple.options.regexes.dialog.import.error",
                                        e.getLocalizedMessage()));
                return;
            }
            setRegexes(regexes);
        }
    }

    private File selectImportFile() {
        JFileChooser chooser = new JFileChooser();

        int rc = chooser.showOpenDialog(this.owner);
        if (rc == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file == null || !file.exists()) {
                return null;
            }
            return file;
        }
        return null;
    }

    public void setRegexes(List<String> regexes) {
        ((RegexesTableModel) getMultipleOptionsModel()).setElements(regexes);
    }

    public List<String> getRegexes() {
        return ((RegexesTableModel) getMultipleOptionsModel()).getElements();
    }

    @Override
    public String showAddDialogue() {
        if (addDialog == null) {
            addDialog = new DialogAddRegex(owner);
            addDialog.pack();
        }

        addDialog.setRegexes(this.getRegexes());
        addDialog.setVisible(true);

        String regex = addDialog.getRegex();
        addDialog.clear();

        return regex;
    }

    @Override
    public String showModifyDialogue(String e) {
        if (modifyDialog == null) {
            modifyDialog = new DialogModifyRegex(owner);
            modifyDialog.pack();
        }

        modifyDialog.setRegexes(this.getRegexes());
        modifyDialog.setRegex(e);
        modifyDialog.setVisible(true);

        String regex = modifyDialog.getRegex();
        modifyDialog.clear();

        if (!regex.equals(e)) {
            return regex;
        }

        return null;
    }

    @Override
    public boolean showRemoveDialogue(String e) {
        JCheckBox removeWithoutConfirmationCheckBox = new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
        Object[] messages = {REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCheckBox};
        int option =
                JOptionPane.showOptionDialog(
                        View.getSingleton().getMainFrame(),
                        messages,
                        REMOVE_DIALOG_TITLE,
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[] {
                            REMOVE_DIALOG_CONFIRM_BUTTON_LABEL, REMOVE_DIALOG_CANCEL_BUTTON_LABEL
                        },
                        null);

        if (option == JOptionPane.OK_OPTION) {
            setRemoveWithoutConfirmation(removeWithoutConfirmationCheckBox.isSelected());
            return true;
        }
        return false;
    }

    protected static class DialogAddRegex extends AbstractFormDialog {

        private static final long serialVersionUID = 9172864521259395417L;

        private static final String DIALOG_TITLE =
                Constant.messages.getString("multiple.options.regexes.dialog.add.regex.title");

        private static final String CONFIRM_BUTTON_LABEL =
                Constant.messages.getString(
                        "multiple.options.regexes.dialog.add.regex.button.confirm");

        private static final String REGEX_FIELD_LABEL =
                Constant.messages.getString("multiple.options.regexes.dialog.regex.label");

        private static final String TITLE_INVALID_REGEX_DIALOG =
                Constant.messages.getString("multiple.options.regexes.dialog.regex.invalid.title");
        private static final String TEXT_INVALID_REGEX_DIALOG =
                Constant.messages.getString("multiple.options.regexes.dialog.regex.invalid.text");
        private static final String TEXT_ALREADY_EXIST_INVALID_REGEX_DIALOG =
                Constant.messages.getString(
                        "multiple.options.regexes.dialog.regex.invalid.alreadyExist.text");
        private static final String TITLE_ALREADY_EXIST_INVALID_REGEX_DIALOG =
                Constant.messages.getString(
                        "multiple.options.regexes.dialog.regex.invalid.alreadyExist.title");

        private ZapTextField regexTextField;

        protected String regex;

        private List<String> regexes;

        public DialogAddRegex(Dialog owner) {
            super(owner, DIALOG_TITLE);
        }

        protected DialogAddRegex(Dialog owner, String title) {
            super(owner, title);
        }

        @Override
        protected JPanel getFieldsPanel() {
            JPanel fieldsPanel = new JPanel();

            GroupLayout layout = new GroupLayout(fieldsPanel);
            fieldsPanel.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            JLabel regexLabel = new JLabel(REGEX_FIELD_LABEL);

            layout.setHorizontalGroup(
                    layout.createSequentialGroup()
                            .addComponent(regexLabel)
                            .addComponent(getRegexTextField()));

            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(regexLabel)
                            .addComponent(getRegexTextField()));

            return fieldsPanel;
        }

        @Override
        protected String getConfirmButtonLabel() {
            return CONFIRM_BUTTON_LABEL;
        }

        @Override
        protected void init() {
            getRegexTextField().setText("");
            regex = null;
        }

        @Override
        protected boolean validateFields() {
            String trimmedRegex = getRegexTextField().getText().trim();
            try {
                if (validateRegexAndCheckForDuplicates(trimmedRegex, regexes)) {
                    return true;
                }
                JOptionPane.showMessageDialog(
                        this,
                        TEXT_ALREADY_EXIST_INVALID_REGEX_DIALOG,
                        TITLE_ALREADY_EXIST_INVALID_REGEX_DIALOG,
                        JOptionPane.INFORMATION_MESSAGE);
                getRegexTextField().requestFocusInWindow();
                return false;

            } catch (PatternSyntaxException e) {
                JOptionPane.showMessageDialog(
                        this,
                        MessageFormat.format(TEXT_INVALID_REGEX_DIALOG, e.getLocalizedMessage()),
                        TITLE_INVALID_REGEX_DIALOG,
                        JOptionPane.INFORMATION_MESSAGE);
                getRegexTextField().requestFocusInWindow();
                return false;
            }
        }

        @Override
        protected void performAction() {
            regex = getRegexTextField().getText().trim();
        }

        @Override
        protected void clearFields() {
            getRegexTextField().setText("");
            getRegexTextField().discardAllEdits();
        }

        public String getRegex() {
            return regex;
        }

        protected ZapTextField getRegexTextField() {
            if (regexTextField == null) {
                regexTextField = new ZapTextField(30);
                regexTextField
                        .getDocument()
                        .addDocumentListener(
                                new DocumentListener() {

                                    @Override
                                    public void removeUpdate(DocumentEvent e) {
                                        checkAndEnableConfirmButton();
                                    }

                                    @Override
                                    public void insertUpdate(DocumentEvent e) {
                                        checkAndEnableConfirmButton();
                                    }

                                    @Override
                                    public void changedUpdate(DocumentEvent e) {
                                        checkAndEnableConfirmButton();
                                    }

                                    private void checkAndEnableConfirmButton() {
                                        setConfirmButtonEnabled(
                                                !getRegexTextField().getText().trim().isEmpty());
                                    }
                                });
            }

            return regexTextField;
        }

        public void clear() {
            this.regex = null;
        }

        public void setRegexes(List<String> regexes) {
            this.regexes = regexes;
        }
    }

    protected static class DialogModifyRegex extends DialogAddRegex {

        private static final long serialVersionUID = 3803499933691686617L;

        private static final String DIALOG_TITLE =
                Constant.messages.getString("multiple.options.regexes.dialog.modify.regex.title");

        private static final String CONFIRM_BUTTON_LABEL =
                Constant.messages.getString(
                        "multiple.options.regexes.dialog.modify.regex.button.confirm");

        protected DialogModifyRegex(Dialog owner) {
            super(owner, DIALOG_TITLE);
        }

        @Override
        protected String getConfirmButtonLabel() {
            return CONFIRM_BUTTON_LABEL;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }

        @Override
        protected void init() {
            getRegexTextField().setText(regex);
            getRegexTextField().discardAllEdits();
        }

        @Override
        protected boolean validateFields() {
            String trimmedRegex = getRegexTextField().getText().trim();
            return regex.equals(trimmedRegex) || super.validateFields();
        }
    }

    protected static class RegexesTableModel extends AbstractMultipleOptionsBaseTableModel<String> {

        private static final long serialVersionUID = -7644711449311289615L;

        private static final String[] COLUMN_NAMES = {
            Constant.messages.getString("multiple.options.regexes.table.header.regex")
        };

        private static final int COLUMN_COUNT = COLUMN_NAMES.length;

        private List<String> elements;

        public RegexesTableModel() {
            elements = new ArrayList<>();
        }

        @Override
        public String getColumnName(int col) {
            return COLUMN_NAMES[col];
        }

        @Override
        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public int getRowCount() {
            return elements.size();
        }

        @Override
        public String getValueAt(int rowIndex, int columnIndex) {
            return elements.get(rowIndex);
        }

        @Override
        public List<String> getElements() {
            return elements;
        }

        public void setElements(List<String> regexes) {
            this.elements = new ArrayList<>(regexes);
            fireTableDataChanged();
        }
    }
}
