/*
* Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright the ZAP Development Team
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
package org.zaproxy.zap.extension.ascan;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.SingleColumnTableModel;
import org.zaproxy.zap.view.StandardFieldsDialog;

public class PolicyManagerDialog extends StandardFieldsDialog {

    private static final long serialVersionUID = 1L;

    private JButton addButton = null;
    private JButton modifyButton = null;
    private JButton removeButton = null;
    private JButton importButton = null;
    private JButton exportButton = null;

    private JTable paramsTable = null;
    private SingleColumnTableModel paramsModel = null;

    private ExtensionActiveScan extension;

    private static final Logger logger = Logger.getLogger(PolicyManagerDialog.class);

    public PolicyManagerDialog(Frame owner) {
        super(owner, "ascan.policymgr.title", new Dimension(512, 400));
    }

    public void init(ExtensionActiveScan extension) {
        this.extension = extension;

        this.removeAllFields();

        this.getParamsModel().setLines(extension.getPolicyManager().getAllPolicyNames());

        List<JButton> buttons = new ArrayList<>();
        buttons.add(getAddButton());
        buttons.add(getModifyButton());
        buttons.add(getRemoveButton());
        buttons.add(getImportButton());
        buttons.add(getExportButton());

        this.addTableField(this.getParamsTable(), buttons);

    }

    /**
     * Only need one close button
     */
    @Override
    public boolean hasCancelSaveButtons() {
        return false;
    }
    
    @Override
	public String getHelpIndex() {
		return "ui.dialogs.scanpolicymgr";
	}


    private JButton getAddButton() {
        if (this.addButton == null) {
            this.addButton = new JButton(Constant.messages.getString("ascan.policymgr.button.add"));
            this.addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        extension.showPolicyDialog(PolicyManagerDialog.this);
                    } catch (ConfigurationException e1) {
                        logger.error(e1.getMessage(), e1);
                    }
                }
            });
        }
        return this.addButton;
    }

    private JButton getModifyButton() {
        if (this.modifyButton == null) {
            this.modifyButton = new JButton(Constant.messages.getString("ascan.policymgr.button.modify"));
            this.modifyButton.setEnabled(false);
            this.modifyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String name = (String) getParamsModel().getValueAt(getParamsTable().getSelectedRow(), 0);
                    if (name != null) {
                        try {
                            extension.showPolicyDialog(PolicyManagerDialog.this, name);
                        } catch (ConfigurationException e1) {
                            logger.error(e1.getMessage(), e1);
                        }
                    }
                }
            });
        }
        return this.modifyButton;
    }

    private JButton getRemoveButton() {
        if (this.removeButton == null) {
            this.removeButton = new JButton(Constant.messages.getString("ascan.policymgr.button.remove"));
            this.removeButton.setEnabled(false);
            this.removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String name = (String) getParamsModel().getValueAt(getParamsTable().getSelectedRow(), 0);
                    if (name != null) {
                        if (View.getSingleton().showConfirmDialog(PolicyManagerDialog.this,
                                Constant.messages.getString("ascan.policymgr.warn.delete"))
                                == JOptionPane.OK_OPTION) {
                            extension.getPolicyManager().deletePolicy(name);
                            policyNamesChanged();
                        }
                    }
                }
            });
        }
        return this.removeButton;
    }

    private JButton getImportButton() {
        if (this.importButton == null) {
            this.importButton = new JButton(Constant.messages.getString("ascan.policymgr.button.import"));
            this.importButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Default to ZAP home dir - we dont want to import/export to the policy dir
                    JFileChooser chooser = new JFileChooser(Constant.getZapHome());
                    chooser.setFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            if (file.isDirectory()) {
                                return true;
                            } else if (file.isFile() && file.getName().endsWith(".policy")) {
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public String getDescription() {
                            return Constant.messages.getString("file.format.zap.policy");
                        }
                    });
                    File file = null;
                    int rc = chooser.showOpenDialog(View.getSingleton().getMainFrame());
                    if (rc == JFileChooser.APPROVE_OPTION) {
                        file = chooser.getSelectedFile();
                        if (file == null) {
                            return;
                        }
                        try {
                            extension.getPolicyManager().importPolicy(file);
                            policyNamesChanged();
                        } catch (ConfigurationException | IOException e1) {
                            logger.error(e1.getMessage(), e1);
                            View.getSingleton().showWarningDialog(Constant.messages.getString("ascan.policy.load.error"));
                        }
                    }
                }
            });
        }
        return this.importButton;
    }

    private JButton getExportButton() {
        if (this.exportButton == null) {
            this.exportButton = new JButton(Constant.messages.getString("ascan.policymgr.button.export"));
            this.exportButton.setEnabled(false);
            this.exportButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String name = (String) getParamsModel().getValueAt(getParamsTable().getSelectedRow(), 0);
                    if (name != null) {
                        JFileChooser chooser = new JFileChooser(Constant.getPoliciesDir());
                        File file = new File(Constant.getZapHome(), name + PolicyManager.POLICY_EXTENSION);
                        chooser.setSelectedFile(file);

                        chooser.setFileFilter(new FileFilter() {
                            @Override
                            public boolean accept(File file) {
                                if (file.isDirectory()) {
                                    return true;
                                } else if (file.isFile() && file.getName().endsWith(".policy")) {
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public String getDescription() {
                                return Constant.messages.getString("file.format.zap.policy");
                            }
                        });
                        int rc = chooser.showSaveDialog(View.getSingleton().getMainFrame());
                        if (rc == JFileChooser.APPROVE_OPTION) {
                            file = chooser.getSelectedFile();
                            if (file == null) {
                                return;
                            }
                            try {
                                ScanPolicy policy = extension.getPolicyManager().getPolicy(name);
                                if (policy != null) {
                                    extension.getPolicyManager().exportPolicy(policy, file);
                                }
                            } catch (ConfigurationException e1) {
                                logger.error(e1.getMessage(), e1);
                                View.getSingleton().showWarningDialog(Constant.messages.getString("ascan.policy.load.error"));
                            }
                        }
                    }
                }
            });
        }
        return this.exportButton;
    }

    @Override
    public void save() {
    }

    @Override
    public String validateFields() {
        return null;
    }

    private SingleColumnTableModel getParamsModel() {
        if (paramsModel == null) {
            paramsModel = new SingleColumnTableModel(Constant.messages.getString("ascan.policymgr.table.policy"));
            paramsModel.setEditable(false);
        }
        return paramsModel;
    }

    private JTable getParamsTable() {
        if (paramsTable == null) {
            paramsTable = new JTable();
            paramsTable.setModel(getParamsModel());
            paramsTable.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getClickCount() >= 2) {
                        int row = paramsTable.rowAtPoint(e.getPoint());
                        if (row >= 0) {
                            String name = (String) getParamsModel().getValueAt(row, 0);
                            if (name != null) {
                                try {
                                    extension.showPolicyDialog(PolicyManagerDialog.this, name);
                                } catch (ConfigurationException e1) {
                                    logger.error(e1.getMessage(), e1);
                                }
                            }
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
            paramsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (getParamsTable().getSelectedRowCount() == 0) {
                        getModifyButton().setEnabled(false);
                        getRemoveButton().setEnabled(false);
                        getExportButton().setEnabled(false);
                    } else if (getParamsTable().getSelectedRowCount() == 1) {
                        getModifyButton().setEnabled(true);
                        // Dont let the last policy be removed
                        getRemoveButton().setEnabled(getParamsModel().getRowCount() > 1);
                        getExportButton().setEnabled(true);
                    } else {
                        getModifyButton().setEnabled(false);
                        getRemoveButton().setEnabled(false);
                        getExportButton().setEnabled(false);
                    }
                }
            });
        }
        return paramsTable;
    }

    protected void policyNamesChanged() {
        this.getParamsModel().setLines(extension.getPolicyManager().getAllPolicyNames());
    }

}
