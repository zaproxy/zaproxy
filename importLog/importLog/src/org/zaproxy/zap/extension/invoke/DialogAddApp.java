/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.invoke;

import java.awt.Dialog;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractFormDialog;

class DialogAddApp extends AbstractFormDialog {

    private static final long serialVersionUID = 4629430215148620470L;

    private static final String DIALOG_TITLE = Constant.messages.getString("invoke.options.dialog.app.add.title");

    private static final String CONFIRM_BUTTON_LABEL = Constant.messages.getString("invoke.options.dialog.app.add.button.confirm");

    private static final String DISPLAY_NAME_FIELD_LABEL = Constant.messages.getString("invoke.options.dialog.app.field.label.name");
    private static final String FULL_COMMAND_FIELD_LABEL = Constant.messages.getString("invoke.options.dialog.app.field.label.command");
    private static final String FULL_COMMAND_BUTTON_LABEL = Constant.messages.getString("invoke.options.dialog.app.button.label.command");
    private static final String WORKING_DIR_FIELD_LABEL = Constant.messages.getString("invoke.options.dialog.app.field.label.workingDir");
    private static final String WORKING_DIR_BUTTON_LABEL = Constant.messages.getString("invoke.options.dialog.app.button.label.workingDir");
    private static final String PARAMETERS_FIELD_LABEL = Constant.messages.getString("invoke.options.dialog.app.field.label.parameters");
    private static final String CAPTURE_OUTPUT_FIELD_LABEL = Constant.messages.getString("invoke.options.dialog.app.field.label.output");
    private static final String OUTPUT_TO_NOTE_FIELD_LABEL = Constant.messages.getString("invoke.options.dialog.app.field.label.note");
    private static final String ENABLED_FIELD_LABEL = Constant.messages.getString("invoke.options.dialog.app.field.label.enabled");
    
    private static final String APP_FILE_DESCRIPTION = Constant.messages.getString("invoke.options.dialog.app.chooseCommand.file.description");
    
    private static final String TITLE_DISPLAY_NAME_REPEATED_DIALOG = Constant.messages.getString("invoke.options.dialog.app.warning.name.repeated.title");
    private static final String TEXT_DISPLAY_NAME_REPEATED_DIALOG = Constant.messages.getString("invoke.options.dialog.app.warning.name.repeated.text");

    private ZapTextField displayNameTextField;
    private ZapTextField fullCommandTextField;
    private ZapTextField workingDirTextField;
    private ZapTextField parametersTextField;
    private JCheckBox captureOutputCheckBox;
    private JCheckBox outputToNoteCheckBox;
    private JCheckBox enabledCheckBox;
    
    private JButton chooseAppButton;
    private JButton chooseDirButton;

    protected InvokableApp app;
    private List<InvokableApp> apps;
    
    private ConfirmButtonValidatorDocListener confirmButtonValidatorDocListener;

    public DialogAddApp(Dialog owner) {
        super(owner, DIALOG_TITLE);
    }

    protected DialogAddApp(Dialog owner, String title) {
        super(owner, title);
    }

    @Override
    protected JPanel getFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        
        GroupLayout layout = new GroupLayout(fieldsPanel);
        fieldsPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        JLabel displayNameLabel = new JLabel(DISPLAY_NAME_FIELD_LABEL);
        JLabel fullCommandLabel = new JLabel(FULL_COMMAND_FIELD_LABEL);
        JLabel workingDirLabel = new JLabel(WORKING_DIR_FIELD_LABEL);
        JLabel parametersLabel = new JLabel(PARAMETERS_FIELD_LABEL);
        JLabel captureOutputLabel = new JLabel(CAPTURE_OUTPUT_FIELD_LABEL);
        JLabel outputToNoteLabel = new JLabel(OUTPUT_TO_NOTE_FIELD_LABEL);
        JLabel enabledLabel = new JLabel(ENABLED_FIELD_LABEL);
        
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(displayNameLabel)
                .addComponent(fullCommandLabel)
                .addComponent(workingDirLabel)
                .addComponent(parametersLabel)
                .addComponent(captureOutputLabel)
                .addComponent(enabledLabel))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(getDisplayNameTextField())
                .addGroup(layout.createSequentialGroup()
                    .addComponent(getFullCommandTextField())
                    .addComponent(getChooseAppButton()))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(getWorkingDirTextField())
                    .addComponent(getChooseDirButton()))
                .addComponent(getParametersTextField())
                .addGroup(layout.createSequentialGroup()
                    .addComponent(getCaptureOutputCheckBox())
                    .addComponent(outputToNoteLabel)
                    .addComponent(getOutputToNoteCheckBox()))
                .addComponent(getEnabledCheckBox()))
        );
        
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(displayNameLabel)
                .addComponent(getDisplayNameTextField()))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(fullCommandLabel)
                .addComponent(getFullCommandTextField())
                .addComponent(getChooseAppButton()))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(workingDirLabel)
                .addComponent(getWorkingDirTextField())
                .addComponent(getChooseDirButton()))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(parametersLabel)
                .addComponent(getParametersTextField()))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(captureOutputLabel)
                .addComponent(getCaptureOutputCheckBox())
                .addComponent(outputToNoteLabel)
                .addComponent(getOutputToNoteCheckBox()))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(enabledLabel)
                .addComponent(getEnabledCheckBox()))
        );
        
        return fieldsPanel;
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }

    @Override
    protected void init() {
        getDisplayNameTextField().setText("");
        getFullCommandTextField().setText("");
        getWorkingDirTextField().setText("");
        getParametersTextField().setText("");
        getCaptureOutputCheckBox().setSelected(false);
        getOutputToNoteCheckBox().setSelected(false);
        getOutputToNoteCheckBox().setEnabled(false);
        getEnabledCheckBox().setSelected(true);
        app = null;
    }

    @Override
    protected boolean validateFields() {
        String displayName = getDisplayNameTextField().getText();
        for (InvokableApp t : apps) {
            if (displayName.equals(t.getDisplayName())) {
                JOptionPane.showMessageDialog(this, TEXT_DISPLAY_NAME_REPEATED_DIALOG,
                        TITLE_DISPLAY_NAME_REPEATED_DIALOG,
                        JOptionPane.INFORMATION_MESSAGE);
                getDisplayNameTextField().requestFocusInWindow();
                return false;
            }
        }

        return true;
    }

    @Override
    protected void performAction() {
        app = new InvokableApp();
        app.setDisplayName(getDisplayNameTextField().getText());
        app.setFullCommand(getFullCommandTextField().getText());
        String workingDir = getWorkingDirTextField().getText();
        if (workingDir != null) {
            File dir = new File(workingDir);
            if (dir.exists() && dir.isDirectory()) {
                app.setWorkingDirectory(dir);
            }
        }
        app.setParameters(getParametersTextField().getText());
        app.setCaptureOutput(getCaptureOutputCheckBox().isSelected());
        app.setOutputNote(getOutputToNoteCheckBox().isSelected());
        app.setEnabled(getEnabledCheckBox().isSelected());
    }

    @Override
    protected void clearFields() {
        getDisplayNameTextField().setText("");
        getDisplayNameTextField().discardAllEdits();
        
        getFullCommandTextField().setText("");
        getFullCommandTextField().discardAllEdits();
        
        getWorkingDirTextField().setText("");
        getWorkingDirTextField().discardAllEdits();
        
        getParametersTextField().setText("");
        getParametersTextField().discardAllEdits();
    }

    public InvokableApp getApp() {
        return app;
    }

    protected ZapTextField getDisplayNameTextField() {
        if (displayNameTextField == null) {
            displayNameTextField = new ZapTextField(25);
            displayNameTextField.getDocument().addDocumentListener(getConfirmButtonValidatorDocListener());
        }
        
        return displayNameTextField;
    }

    protected ZapTextField getFullCommandTextField() {
        if (fullCommandTextField == null) {
            fullCommandTextField = new ZapTextField(20);
            fullCommandTextField.setEditable(false);
            fullCommandTextField.getDocument().addDocumentListener(getConfirmButtonValidatorDocListener());
        }

        return fullCommandTextField;
    }
    
    protected JButton getChooseAppButton() {
        if (chooseAppButton == null) {
            chooseAppButton = new JButton(FULL_COMMAND_BUTTON_LABEL); 
            chooseAppButton.addActionListener(new java.awt.event.ActionListener() {
                
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JFileChooser fcCommand = new JFileChooser();
                    fcCommand.setFileFilter(new FileFilter() {
                        
                        @Override
                        public String getDescription() {
                            return APP_FILE_DESCRIPTION;
                        }
                        
                        @Override
                        public boolean accept(File f) {
                            return f.isDirectory() || f.canExecute();
                        }
                    });
                    
                    String command = getFullCommandTextField().getText();
                    if (command.length() > 0) {
                        // If there's an existing file select containing
                        // directory
                        File f = new File(command);
                        fcCommand.setCurrentDirectory(f.getParentFile());
                    }
                    
                    int state = fcCommand.showOpenDialog(null);
                    
                    if (state == JFileChooser.APPROVE_OPTION) {
                        getFullCommandTextField().setText(fcCommand.getSelectedFile().toString());
                    }
                }
            });
        }
        
        return chooseAppButton;
    }

    protected ZapTextField getWorkingDirTextField() {
        if (workingDirTextField == null) {
            workingDirTextField = new ZapTextField(20);
            workingDirTextField.setEditable(false);
        }

        return workingDirTextField;
    }

    protected JButton getChooseDirButton() {
        if (chooseDirButton == null) {
            chooseDirButton = new JButton(WORKING_DIR_BUTTON_LABEL); 
            chooseDirButton.addActionListener(new java.awt.event.ActionListener() {
                
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JFileChooser fcDirectory = new JFileChooser();
                    fcDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    // disable the "All files" option.
                    fcDirectory.setAcceptAllFileFilterUsed(false);
                    
                    String workingDir = getWorkingDirTextField().getText();
                    if (workingDir.length() > 0) {
                        // If there's an existing directory then select it
                        File f = new File(workingDir);
                        fcDirectory.setCurrentDirectory(f);
                    }
                    
                    int state = fcDirectory.showOpenDialog(null);
                    
                    if (state == JFileChooser.APPROVE_OPTION) {
                        getWorkingDirTextField().setText(fcDirectory.getSelectedFile().toString());
                    }
                }
            });
        }
        
        return chooseDirButton;
    }

    protected ZapTextField getParametersTextField() {
        if (parametersTextField == null) {
            parametersTextField = new ZapTextField(20);
            parametersTextField.getDocument().addDocumentListener(getConfirmButtonValidatorDocListener());
        }

        return parametersTextField;
    }

    protected JCheckBox getCaptureOutputCheckBox() {
        if (captureOutputCheckBox == null) {
            captureOutputCheckBox = new JCheckBox();
            captureOutputCheckBox.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        getOutputToNoteCheckBox().setEnabled(true);
                    } else {
                        getOutputToNoteCheckBox().setSelected(false);
                        getOutputToNoteCheckBox().setEnabled(false);
                    }
                }
            });
        }

        return captureOutputCheckBox;
    }

    protected JCheckBox getOutputToNoteCheckBox() {
        if (outputToNoteCheckBox == null) {
            outputToNoteCheckBox = new JCheckBox();
        }

        return outputToNoteCheckBox;
    }

    protected JCheckBox getEnabledCheckBox() {
        if (enabledCheckBox == null) {
            enabledCheckBox = new JCheckBox();
        }

        return enabledCheckBox;
    }

    public void setApps(List<InvokableApp> apps) {
        this.apps = apps;
    }

    public void clear() {
        this.apps = null;
        this.app = null;
    }

    private ConfirmButtonValidatorDocListener getConfirmButtonValidatorDocListener() {
        if (confirmButtonValidatorDocListener == null) {
            confirmButtonValidatorDocListener = new ConfirmButtonValidatorDocListener(); 
        }
        
        return confirmButtonValidatorDocListener;
    }
    
    private class ConfirmButtonValidatorDocListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            checkAndEnableConfirmButton();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            checkAndEnableConfirmButton();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            checkAndEnableConfirmButton();
        }

        private void checkAndEnableConfirmButton() {
            boolean enabled = (getDisplayNameTextField().getDocument().getLength() > 0)
                    && (getFullCommandTextField().getDocument().getLength() > 0)
                    && (getParametersTextField().getDocument().getLength() > 0);
            setConfirmButtonEnabled(enabled);
        }
    }
}
