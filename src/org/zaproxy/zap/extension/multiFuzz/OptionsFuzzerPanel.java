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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.zaproxy.zap.extension.multiFuzz;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.filechooser.FileFilter;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.FileCopier;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.PositiveValuesSlider;

public class OptionsFuzzerPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private ExtensionFuzz extension = null;
	private JPanel panelPortScan = null;
	private JSlider sliderThreadsPerScan = null;
	private JComboBox<String> categoryField = null;
	private JButton addFileButton = null;

	public OptionsFuzzerPanel(ExtensionFuzz extension) {
		super();
		this.extension = extension;
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setLayout(new CardLayout());
		this.setName(Constant.messages.getString("fuzz.options.title"));
		this.setSize(314, 245);
		this.add(getPanelPortScan(), getPanelPortScan().getName());
	}

	/**
	 * This method initializes panelSpider
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelPortScan() {
		if (panelPortScan == null) {

			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5a = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5b = new GridBagConstraints();
			GridBagConstraints gridBagConstraintsX = new GridBagConstraints();

			panelPortScan = new JPanel();
			JLabel jLabel0 = new JLabel();
			JLabel jLabel1 = new JLabel();
			JLabel jLabel2 = new JLabel();
			JLabel jLabel3 = new JLabel();

			panelPortScan.setLayout(new GridBagLayout());
			panelPortScan.setSize(114, 132);
			panelPortScan.setName("");
			jLabel0.setText(Constant.messages
					.getString("fuzz.options.label.category"));
			jLabel1.setText(Constant.messages
					.getString("fuzz.options.label.threads"));
			jLabel2.setText("");
			jLabel3.setText(Constant.messages
					.getString("fuzz.options.label.addfile"));

			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.gridy = 0;
			gridBagConstraints10.ipadx = 0;
			gridBagConstraints10.ipady = 0;
			gridBagConstraints10.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints10.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints10.weightx = 0.5D;

			gridBagConstraints11.gridx = 1;
			gridBagConstraints11.gridy = 0;
			gridBagConstraints11.ipadx = 0;
			gridBagConstraints11.ipady = 0;
			gridBagConstraints11.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints11.weightx = 0.5D;

			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 2;
			gridBagConstraints3.ipadx = 0;
			gridBagConstraints3.ipady = 0;
			gridBagConstraints3.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints3.weightx = 1.0D;
			gridBagConstraints3.gridwidth = 2;

			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 3;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.ipadx = 0;
			gridBagConstraints4.ipady = 0;
			gridBagConstraints4.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints4.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints4.gridwidth = 2;

			gridBagConstraints5a.gridx = 0;
			gridBagConstraints5a.gridy = 4;
			gridBagConstraints5a.weightx = 1.0;
			gridBagConstraints5a.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5a.ipadx = 0;
			gridBagConstraints5a.ipady = 0;
			gridBagConstraints5a.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints5a.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints5a.gridwidth = 1;

			gridBagConstraints5b.gridx = 1;
			gridBagConstraints5b.gridy = 4;
			gridBagConstraints5b.weightx = 1.0;
			gridBagConstraints5b.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5b.ipadx = 0;
			gridBagConstraints5b.ipady = 0;
			gridBagConstraints5b.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints5b.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints5b.gridwidth = 1;

			gridBagConstraintsX.gridx = 0;
			gridBagConstraintsX.gridy = 10;
			gridBagConstraintsX.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraintsX.fill = GridBagConstraints.BOTH;
			gridBagConstraintsX.insets = new Insets(2, 2, 2, 2);
			gridBagConstraintsX.weightx = 1.0D;
			gridBagConstraintsX.weighty = 1.0D;
			gridBagConstraintsX.gridwidth = 2;

			panelPortScan.add(jLabel0, gridBagConstraints10);
			panelPortScan.add(getDefaultCategory(), gridBagConstraints11);
			panelPortScan.add(jLabel1, gridBagConstraints3);
			panelPortScan.add(getSliderThreadsPerScan(), gridBagConstraints4);
			panelPortScan.add(jLabel2, gridBagConstraintsX);

			panelPortScan.add(jLabel3, gridBagConstraints5a);
			panelPortScan.add(getAddFileButton(), gridBagConstraints5b);

		}
		return panelPortScan;
	}

	private JComboBox<String> getDefaultCategory() {
		if (categoryField == null) {
			categoryField = new JComboBox<>();

			addAvailableFuzzCategoriesToDefaultCategoryComboBox();
		}
		return categoryField;
	}

	private void addAvailableFuzzCategoriesToDefaultCategoryComboBox() {
		for (String category : extension.getFileFuzzerCategories()) {
			categoryField.addItem(category);
		}

		for (String category : extension.getJBroFuzzCategories()) {
			categoryField.addItem(category);
		}

		categoryField.addItem(Constant.messages
				.getString("fuzz.category.custom"));
	}

	void updateFuzzCategories() {
		categoryField.removeAllItems();

		addAvailableFuzzCategoriesToDefaultCategoryComboBox();
	}

	@Override
	public void initParam(Object obj) {
		OptionsParam options = (OptionsParam) obj;
		FuzzerParam param = (FuzzerParam) options
				.getParamSet(FuzzerParam.class);
		if (param != null) {
			getSliderThreadsPerScan().setValue(param.getThreadPerScan());
			this.getDefaultCategory().setSelectedItem(
					param.getDefaultCategory());
		}
	}

	@Override
	public void validateParam(Object obj) {
		// no validation needed
	}

	@Override
	public void saveParam(Object obj) throws Exception {
		OptionsParam options = (OptionsParam) obj;
		FuzzerParam param = (FuzzerParam) options
				.getParamSet(FuzzerParam.class);
		if (param == null) {
			param = new FuzzerParam();
			options.addParamSet(param);
		}
		param.setThreadPerScan(getSliderThreadsPerScan().getValue());
		param.setDefaultCategory((String) this.getDefaultCategory()
				.getSelectedItem());
	}

	/**
	 * This method initializes sliderThreadsPerHost
	 * 
	 * @return JSlider
	 */
	private JSlider getSliderThreadsPerScan() {
		if (sliderThreadsPerScan == null) {
			sliderThreadsPerScan = new PositiveValuesSlider(
					Constant.MAX_THREADS_PER_SCAN);
		}
		return sliderThreadsPerScan;
	}

	public int getThreadPerScan() {
		return this.sliderThreadsPerScan.getValue();
	}

	private JButton getAddFileButton() {
		if (addFileButton == null) {
			addFileButton = new JButton(
					Constant.messages.getString("fuzz.options.button.addfile"));
			addFileButton
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent e) {
							JFileChooser fcCommand = new JFileChooser();
							fcCommand.setFileFilter(new FileFilter() {
								@Override
								public String getDescription() {
									return Constant.messages
											.getString("fuzz.options.title");
								}

								@Override
								public boolean accept(File f) {
									return true;
								}
							});

							FuzzerParam params = (FuzzerParam) Model
									.getSingleton().getOptionsParam()
									.getParamSet(FuzzerParam.class);

							fcCommand.setCurrentDirectory(params
									.getLastSelectedDirectory());

							// Copy the file into the 'home' dirbuster directory
							int state = fcCommand.showOpenDialog(null);

							if (state == JFileChooser.APPROVE_OPTION) {
								final File selectedFile = fcCommand
										.getSelectedFile();
								final File newFile = new File(Constant
										.getInstance().FUZZER_CUSTOM_DIR
										+ File.separator
										+ selectedFile.getName());

								boolean copyFile = false;
								if (newFile.exists()) {
									copyFile = confirmOverwrite();
								} else if (!newFile.getParentFile().canWrite()) {
									View.getSingleton()
											.showWarningDialog(
													Constant.messages
															.getString("fuzz.add.dirperms.error")
															+ newFile
																	.getParentFile()
																	.getAbsolutePath());

								} else {
									copyFile = true;
								}

								if (copyFile) {
									copyFile(selectedFile, newFile);
								}

								params.setLastSelectedDirectory(fcCommand
										.getCurrentDirectory());
							}
						}

						private boolean confirmOverwrite() {
							int option = JOptionPane.showOptionDialog(
									View.getSingleton().getMainFrame(),
									Constant.messages
											.getString("fuzz.add.duplicate.error"),
									Constant.messages
											.getString("fuzz.add.duplicate.error.title"),
									JOptionPane.OK_CANCEL_OPTION,
									JOptionPane.QUESTION_MESSAGE,
									null,
									new String[] {
											Constant.messages
													.getString("fuzz.add.duplicate.error.button.confirm"),
											Constant.messages
													.getString("all.button.cancel") },
									null);

							return option == JOptionPane.OK_OPTION;
						}

						private void copyFile(final File source, final File dest) {
							final FileCopier copier = new FileCopier();
							try {
								copier.copy(source, dest);
								View.getSingleton().showMessageDialog(
										Constant.messages
												.getString("fuzz.add.ok"));
							} catch (IOException e) {
								View.getSingleton()
										.showWarningDialog(
												Constant.messages
														.getString("fuzz.add.fail.error")
														+ e.getMessage());
							}
						}
					});
		}
		return addFileButton;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.fuzz";
	}
}
