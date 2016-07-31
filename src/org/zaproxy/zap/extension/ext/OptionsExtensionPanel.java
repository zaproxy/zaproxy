/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 ZAP development team
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
package org.zaproxy.zap.extension.ext;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.DesktopUtils;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.LayoutHelper;

public class OptionsExtensionPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JTable tableExt = null;
	private JScrollPane jScrollPane = null;
	private JPanel detailsPane = null;
	private JLabel extName = new JLabel();
	private JLabel extAuthor = new JLabel();
	private JLabel extURL = new JLabel();
	private JTextArea extDescription = new JTextArea(); 
	private OptionsExtensionTableModel extensionModel = null;
	private JScrollPane extDescScrollPane = null;
	private JButton urlLaunchButton = null;

    private static Logger log = Logger.getLogger(OptionsExtensionPanel.class);

    public OptionsExtensionPanel(ExtensionExtension ext) {
        super();
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();

        javax.swing.JLabel jLabel = new JLabel();

        this.setLayout(new GridBagLayout());
        this.setSize(409, 268);
        this.setName(Constant.messages.getString("options.ext.title"));
        jLabel.setText(Constant.messages.getString("options.ext.label.enable"));
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridheight = 1;
        gridBagConstraints1.weightx = 0.0;
        gridBagConstraints1.weighty = 0.0;
        gridBagConstraints1.insets = new Insets(0,0,5,0);
        gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 0.75;
        gridBagConstraints2.fill = GridBagConstraints.BOTH;
        gridBagConstraints2.ipadx = 0;
        gridBagConstraints2.insets = new Insets(0,0,0,0);
        gridBagConstraints2.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 2;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 0.25;
        gridBagConstraints3.fill = GridBagConstraints.BOTH;
        gridBagConstraints3.ipadx = 0;
        gridBagConstraints3.insets = new Insets(0,0,0,0);
        gridBagConstraints3.anchor = GridBagConstraints.NORTHWEST;
        this.add(jLabel, gridBagConstraints1);
        this.add(getJScrollPane(), gridBagConstraints2);
        this.add(getDetailsPane(), gridBagConstraints3);
			
	}

	@Override
    public void initParam(Object obj) {
	    OptionsParam optionsParam = (OptionsParam) obj;
		ExtensionParam extParam = optionsParam.getParamSet(ExtensionParam.class);

		Map<String, Boolean> extensionsState = new HashMap<>();
		for (ExtensionParam.ExtensionState extEntry : extParam.getExtensions()) {
			extensionsState.put(extEntry.getName(), extEntry.isEnabled());
		}
		List<Extension> exts = extensionModel.getExtensions();
		for (Extension ext : exts) {
			Boolean enabled = extensionsState.get(ext.getName());
			if (enabled == null) {
				enabled = Boolean.TRUE;
			}
			ext.setEnabled(enabled);
		}
    }


    @Override
    public void validateParam(Object obj) throws Exception {

    }


    @Override
    public void saveParam(Object obj) throws Exception {
	    OptionsParam optionsParam = (OptionsParam) obj;
		ExtensionParam extParam = optionsParam.getParamSet(ExtensionParam.class);

		List<ExtensionParam.ExtensionState> extensions = new ArrayList<>(extensionModel.getExtensions().size());
		for (Extension ext : extensionModel.getExtensions()) {
			extensions.add(new ExtensionParam.ExtensionState(ext.getName(), ext.isEnabled()));
		}
		extParam.setExtensions(extensions);
    }

	/**
	 * This method initializes tableAuth	
	 * 	
	 * @return javax.swing.JTable	
	 */    
	private JTable getTableExtension() {
		if (tableExt == null) {
			tableExt = new JTable();
			tableExt.setModel(getExtensionModel());
			tableExt.setRowHeight(DisplayUtils.getScaledSize(18));
			tableExt.getColumnModel().getColumn(0).setPreferredWidth(DisplayUtils.getScaledSize(70));
			tableExt.getColumnModel().getColumn(1).setPreferredWidth(DisplayUtils.getScaledSize(70));
			tableExt.getColumnModel().getColumn(2).setPreferredWidth(DisplayUtils.getScaledSize(120));
			tableExt.getColumnModel().getColumn(3).setPreferredWidth(DisplayUtils.getScaledSize(220));
			
			ListSelectionListener sl = new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent arg0) {
	        		if (tableExt.getSelectedRow() > -1) {
	        			Extension ext = ((OptionsExtensionTableModel)tableExt.getModel()).getExtension(
	        					tableExt.getSelectedRow());
	        			if (ext != null) {
	        				try {
								extName.setText(ext.getUIName());
								extDescription.setText(ext.getDescription());
								if (ext.getAuthor() != null) {
									extAuthor.setText(ext.getAuthor());
								} else {
									extAuthor.setText("");
								}
								if (ext.getURL() != null) {
									extURL.setText(ext.getURL().toString());
									getUrlLaunchButton().setEnabled(true);
								} else {
									extURL.setText("");
									getUrlLaunchButton().setEnabled(false);
								}
							} catch (Exception e) {
								// Just to be safe
								log.error(e.getMessage(), e);
							}
	        			}
	        		}
				}};
			
			tableExt.getSelectionModel().addListSelectionListener(sl);
			tableExt.getColumnModel().getSelectionModel().addListSelectionListener(sl);
			
		}
		return tableExt;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTableExtension());
			jScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
		}
		return jScrollPane;
	}
	
	private JPanel getDetailsPane() {
		if (detailsPane == null) {
			detailsPane = new JPanel();
			detailsPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
			detailsPane.setLayout(new GridBagLayout());
			detailsPane.add(new JLabel(Constant.messages.getString("options.ext.label.name")), LayoutHelper.getGBC(0, 1, 1, 0.25D));
			detailsPane.add(extName, LayoutHelper.getGBC(1, 1, 1, 0.75D));

			detailsPane.add(new JLabel(Constant.messages.getString("options.ext.label.author")), LayoutHelper.getGBC(0, 2, 1, 0.25D));
			detailsPane.add(extAuthor, LayoutHelper.getGBC(1, 2, 1, 0.75D));

			detailsPane.add(new JLabel(Constant.messages.getString("options.ext.label.url")), LayoutHelper.getGBC(0, 3, 1, 0.25D));
			if (DesktopUtils.canOpenUrlInBrowser()) {
				detailsPane.add(getUrlLaunchButton(), LayoutHelper.getGBC(1, 3, 1, 0.0D, 0.0D, GridBagConstraints.NONE));
			} else {
				detailsPane.add(extURL, LayoutHelper.getGBC(1, 3, 1, 0.5D));
			}

			detailsPane.add(getExtDescJScrollPane(), LayoutHelper.getGBC(0, 4, 2, 1.0D, 1.0D));

		}
		return detailsPane;
	}
	
	private JButton getUrlLaunchButton() {
		if (urlLaunchButton == null) {
			urlLaunchButton = new JButton(Constant.messages.getString("options.ext.button.openurl"));
			urlLaunchButton.setEnabled(false);
			urlLaunchButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (extURL.getText() != null) {
						DesktopUtils.openUrlInBrowser(extURL.getText());
					}
				}});
		}
		return urlLaunchButton;
	}

	private JScrollPane getExtDescJScrollPane() {
		if (extDescScrollPane == null) {
			extDescScrollPane = new JScrollPane();
			extDescScrollPane.setViewportView(extDescription);
			extDescription.setEditable(false);
			extDescription.setLineWrap(true);
		}
		return extDescScrollPane;
	}
		
	/**
	 * This method initializes authModel	
	 * 	
	 * @return org.parosproxy.paros.view.OptionsAuthenticationTableModel	
	 */    
	private OptionsExtensionTableModel getExtensionModel() {
		if (extensionModel == null) {
			extensionModel = new OptionsExtensionTableModel();
		}
		return extensionModel;
	}
	
	protected boolean enableExtension(String name, boolean enable) {
		Extension ext = this.getExtensionModel().getExtension(name);
		if (ext != null) {
			ext.setEnabled(enable);
			return true;
		}
		return false;
	}


	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.ext";
	}

}
