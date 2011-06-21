/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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

package org.zaproxy.zap.extension.lang;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.LocaleUtils;
import org.zaproxy.zap.utils.ZapTextField;

public class OptionsLangPanel extends AbstractParamPanel {

	
	private static final long serialVersionUID = 1L;
	private JPanel panelLang = null;
	private JLabel languageLabel = null;
	private JLabel importLabel = null;
	//private JLabel importNoticeLabel = null;
	private JButton selectionButton = null;
	private JButton importButton = null;
	private JComboBox localeSelect = null;
	private ZapTextField fileTextField = null; 
	
	private Map<String, String> localeMap = new HashMap<String, String>();
	
	
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
		    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
		    	panelLang.setSize(409, 268);
		    }
			
			languageLabel = new JLabel(Constant.messages.getString("options.lang.selector.label"));
			importLabel = new JLabel(Constant.messages.getString("options.lang.importer.label"));
			//importNoticeLabel = new JLabel(Constant.messages.getString("options.lang.importer.noticeLabel"));
			
			int rowId = 0;
			
			panelLang.add(languageLabel, getGridBackConstraints(rowId, 0, 0, 0));
			panelLang.add(getLocaleSelect(), getGridBackConstraints(rowId++, 1, 1, GridBagConstraints.REMAINDER));
			
			panelLang.add(importLabel, getGridBackConstraints(rowId++, 0, 0, GridBagConstraints.REMAINDER));
			
			panelLang.add(getFileTextField(), getGridBackConstraints(rowId, 0, 1, GridBagConstraints.RELATIVE));
			panelLang.add(getSelectionButton(), getGridBackConstraints(rowId++, 1, 0, 0));
			
			panelLang.add(new JLabel(""), getGridBackConstraints(rowId, 0, 0, GridBagConstraints.RELATIVE));
			panelLang.add(getImportButton(), getGridBackConstraints(rowId++, 1, 0, 0));
			
			//panelLang.add(importNoticeLabel, getGridBackConstraints(rowId++, 0, 0, GridBagConstraints.REMAINDER));
	        
		}
		return panelLang;
	}
	
	private GridBagConstraints getGridBackConstraints(int y, int x, double weight, int columnWidth) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = y;
        gbc.gridx = x;
        gbc.insets = new java.awt.Insets(0,0,0,0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = weight;
		if (columnWidth > 0) {
			gbc.gridwidth = columnWidth;
		}
		return gbc;
	}


	private ZapTextField getFileTextField() {
		if (fileTextField == null) {
			fileTextField = new ZapTextField();
		}
		return fileTextField;
	}	
	
	private JButton getSelectionButton() {
		if (selectionButton == null) {
			selectionButton = new JButton();
			selectionButton.setText(Constant.messages.getString("options.lang.importer.browse"));
			selectionButton.addActionListener(new ActionListener() {
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
			importButton.setText(Constant.messages.getString("options.lang.importer.button"));
			importButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					if (fileTextField.getText() != "") {
						LangImporter.importLanguagePack(fileTextField.getText());
						fileTextField.setText("");
					}
				}
			});
		}
		return importButton;
	}
	
	private JComboBox getLocaleSelect() {
		if (localeSelect == null) {
			localeSelect = new JComboBox();
		}
		return localeSelect;
	}
	
	private void browseButtonActionPerformed(ActionEvent evt) {
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Zap language file";
			}

			@Override
			public boolean accept(java.io.File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".zaplang");
			}
		});

		final int state = fc.showOpenDialog(null);

		if (state == JFileChooser.APPROVE_OPTION) {
			fileTextField.setText(fc.getSelectedFile().toString());
		}
	}
	
	
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    
	    localeSelect.removeAllItems();
		List <String> locales = LocaleUtils.getAvailableLocales();
		for (String locale : locales) {
			String desc = LocaleUtils.getLocalDisplayName(locale);
			localeSelect.addItem(desc);
			localeMap.put(desc, locale);
		}

	    String locale = LocaleUtils.getLocalDisplayName(options.getViewParam().getLocale());
	    localeSelect.setSelectedItem(locale);
	    
	}
	
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    String selectedLocale = (String) localeSelect.getSelectedItem();
	    String locale = localeMap.get(selectedLocale);
	    if (locale != null) {
		    options.getViewParam().setLocale(locale);
	    }
	}
	
	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.lang";
	}
}
