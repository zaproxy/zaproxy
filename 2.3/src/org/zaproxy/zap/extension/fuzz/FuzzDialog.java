/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.extension.fuzz;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.owasp.jbrofuzz.core.Fuzzer;
import org.owasp.jbrofuzz.core.NoSuchFuzzerException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.view.View;

public abstract class FuzzDialog extends AbstractDialog {

    private static final long serialVersionUID = 3855005636913607013L;

    private static final int selectionFieldLength = 40;

    private static final Logger logger = Logger.getLogger(FuzzDialog.class);
    
	private JPanel jPanel = null;

	private DefaultComboBoxModel<String> fuzzerModel = null;
    private JComboBox<String> categoryField = null;

    private JList<String> fuzzersField = null;
    
    private JLabel fuzzDescriptionLabel = null;
    private JLabel fuzzTargetLabel = null;
	private JLabel selectionField = null;

    private JButton cancelButton = null;
    private JButton startButton = null;
    
    private ExtensionFuzz extension;
    
    /**
     * 
     * @param extension
     * @param fuzzTarget
     * @throws HeadlessException
     */
    public FuzzDialog(ExtensionFuzz extension, String fuzzTarget) throws HeadlessException {
        super(View.getSingleton().getMainFrame(), true);
        
        this.setTitle(Constant.messages.getString("fuzz.title"));
        
        this.extension = extension;
        setSelection(fuzzTarget);
    }
    
	/**
	 * This method initializes this
	 */
	protected void initialize() {
        this.setContentPane(getJTabbed());
        setDefaultCategory();
        
		this.setSize(500, 450);
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJTabbed() {
		if (jPanel == null) {
			jPanel = new JPanel(new GridBagLayout());
			
			int currentRow = 0;
			
			jPanel.add(getFuzzDescriptionLabel(), getGBC(0, currentRow, GridBagConstraints.REMAINDER, 1.0D));
			currentRow++;
			jPanel.add(getFuzzTargetLabel(), getGBC(0, currentRow, 1, 0.25D));
			jPanel.add(getSelectionField(), getGBC(1, currentRow, 3, 0.0D));
			currentRow++;
			
			currentRow = addCustomComponents(jPanel, currentRow);
			
			jPanel.add(new JLabel(Constant.messages.getString("fuzz.label.category")), getGBC(0, currentRow, 1, 0.25D));
			jPanel.add(getCategoryField(), getGBC(1, currentRow, 3, 0.75D));
			currentRow++;

			jPanel.add(new JLabel(Constant.messages.getString("fuzz.label.fuzzer")), getGBC(0, currentRow, 1, 0.25D));
			jPanel.add(new JScrollPane(getFuzzersField()), getGBC(1, currentRow, 3, 1.0D, 0.75D));
			currentRow++;
			
			jPanel.add(new JLabel(""), getGBC(1, currentRow, 1, 0.50));
			jPanel.add(getStartButton(), getGBC(2, currentRow, 1, 0.25));
			jPanel.add(getCancelButton(), getGBC(3, currentRow, 1, 0.25));
		}
		return jPanel;
	}
    
    protected abstract int addCustomComponents(JPanel panel, int row);

    protected GridBagConstraints getGBC(int x, int y, int width, double weightx) {
        return this.getGBC(x, y, width, weightx, 0.0);
    }
    
    protected GridBagConstraints getGBC(int x, int y, int width, double weightx, double weighty) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new java.awt.Insets(1,5,1,5);
        gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.gridwidth = width;
        return gbc;
    }

    private JButton getStartButton() {
        if (startButton == null) {
            startButton = new JButton();
            startButton.setEnabled(false);  // Only enabled when 1 or more fuzzers chosen
            startButton.setAction(getStartFuzzAction());
        }
        return startButton;
    }

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setAction(getCancelFuzzAction());
		}
		return cancelButton;
	}
	
	protected abstract FuzzProcessFactory getFuzzProcessFactory();
	
	protected StartFuzzAction getStartFuzzAction() {
	    return new StartFuzzAction();
	}
    
    protected CancelFuzzAction getCancelFuzzAction() {
        return new CancelFuzzAction();
    }
	
	private boolean isCustomCategory() {
		return Constant.messages.getString("fuzz.category.custom").equals(getCategoryField().getSelectedItem());
	}
	
	private boolean isJBroFuzzCategory() {
		return ((String)getCategoryField().getSelectedItem()).startsWith(ExtensionFuzz.JBROFUZZ_CATEGORY_PREFIX);
	}
	
	private JComboBox<String> getCategoryField() {
		if (categoryField == null) {
			categoryField = new JComboBox<>();

			// Add File based fuzzers (fuzzdb)
			for (String category : extension.getFileFuzzerCategories()) {
				categoryField.addItem(category);
			}
			
			// Add jbrofuzz fuzzers
			for (String category : extension.getJBroFuzzCategories()) {
				categoryField.addItem(category);
			}

			// Custom category
			categoryField.addItem(Constant.messages.getString("fuzz.category.custom"));
			
			categoryField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setFuzzerNames();
				}});
		}
		return categoryField;
	}
	
	private void setFuzzerNames () {
		fuzzerModel.removeAllElements();
		
		String category = (String) getCategoryField().getSelectedItem();
		if (category == null) {
			return;
		}
		
		if (isCustomCategory()) {
			List<String> fuzzers = extension.getCustomFileList();
			for (String fuzzer : fuzzers) {
				fuzzerModel.addElement(fuzzer);
			}
		} else if (isJBroFuzzCategory()) {
			for (String fuzzer : extension.getJBroFuzzFuzzerNames(category)) {
				fuzzerModel.addElement(fuzzer);
			}
		} else {
			List<String> fuzzers = extension.getFileFuzzerNames(category);
			for (String fuzzer : fuzzers) {
				fuzzerModel.addElement(fuzzer);
			}
		}
	}
	
	private JLabel getFuzzTargetLabel() {
		if (fuzzTargetLabel == null) {
			fuzzTargetLabel = new JLabel();
		}
		return fuzzTargetLabel;
	}
	
	private JLabel getFuzzDescriptionLabel() {
		if (fuzzDescriptionLabel == null) {
			fuzzDescriptionLabel = new JLabel();
		}
		return fuzzDescriptionLabel;
	}
	
	private JLabel getSelectionField() {
		if (selectionField == null) {
			selectionField = new JLabel();
		}
		return selectionField;
	}

	private JList<String> getFuzzersField() {
		if (fuzzersField == null) {
			fuzzerModel = new DefaultComboBoxModel<>();
			
			fuzzersField = new JList<>();
			fuzzersField.setModel(fuzzerModel);
			setFuzzerNames();
			fuzzersField.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
		            getStartButton().setEnabled(!fuzzersField.isSelectionEmpty());
				}});
		}
		return fuzzersField;
	}

	protected void setSelection(String fuzzTarget) {
        final int length = fuzzTarget.length();
        if (length == 0) {
            getSelectionField().setText("");
            getFuzzTargetLabel().setText("");
            getFuzzDescriptionLabel().setText(Constant.messages.getString("fuzz.label.insertFuzzStrings"));
        } else {
            getFuzzDescriptionLabel().setText("");
            getFuzzTargetLabel().setText(Constant.messages.getString("fuzz.label.selection"));
            if (length > selectionFieldLength) {
                getSelectionField().setText(fuzzTarget.substring(0, selectionFieldLength) + "...");
            } else {
                getSelectionField().setText(fuzzTarget);
            }
        }
	}
	
	private void setDefaultCategory() {
		this.getCategoryField().setSelectedItem(extension.getDefaultCategory());
	}
	
	
	protected class StartFuzzAction extends AbstractAction {

        private static final long serialVersionUID = -961522394390805325L;

        public StartFuzzAction() {
            super(Constant.messages.getString("fuzz.button.start"));
            setEnabled(false);
        }
        
        @Override
	    public void actionPerformed(ActionEvent e) {
            if (!fuzzersField.isSelectionEmpty()) {
                List<String> names = fuzzersField.getSelectedValuesList();
                int size = names.size();
                try {
                    Fuzzer [] fuzzers = null;
                    FileFuzzer [] fileFuzzers = null;
                    if (isCustomCategory()) {
                        fileFuzzers = new FileFuzzer[size];
                        for (int i=0; i < size; i++) {
                            fileFuzzers[i] = extension.getCustomFileFuzzer(names.get(i));
                        }
                    } else if (isJBroFuzzCategory()) {
                        fuzzers = new Fuzzer[size];
                        for (int i=0; i < size; i++) {
                            fuzzers[i] = extension.getJBroFuzzer(names.get(i));
                        }
                    } else {
                        fileFuzzers = new FileFuzzer[size];
                        final String category = (String)getCategoryField().getSelectedItem();
                        for (int i=0; i < size; i++) {
                            fileFuzzers[i] = extension.getFileFuzzer(category, names.get(i));
                        }
                    }
                    
                    extension.startFuzzers(fuzzers, fileFuzzers, getFuzzProcessFactory());
                    
                } catch (NoSuchFuzzerException ex) {
                    logger.error(ex.getMessage(), ex);
                }
                setVisible(false);
            }
	    }
	}
	
    protected class CancelFuzzAction extends AbstractAction {

        private static final long serialVersionUID = -6716179197963523133L;

        public CancelFuzzAction() {
            super(Constant.messages.getString("fuzz.button.cancel"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }
}
