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
// ZAP: 2014/03/27 Issue 1072: Allow the request and response body sizes to be user-specifiable as far as possible

package org.parosproxy.paros.extension.option;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.ZapSizeNumberSpinner;

/**
 * The GUI database options panel.
 * <p>
 * It allows to change the following database options:
 * <ul>
 * <li>Compact - allows the database to be compacted on exit.</li>
 * <li>Request Body Size - the size of the request body in the 'History' database table.</li>
 * <li>Response Body Size - the size of the response body in the 'History' database table.</li>
 * </ul>
 * </p>
 * 
 * @see org.parosproxy.paros.db.Database#close(boolean)
 */
public class OptionsDatabasePanel extends AbstractParamPanel {

    private static final long serialVersionUID = -7541236934312940852L;
    
    /**
     * The name of the options panel.
     */
    private static final String NAME = Constant.messages.getString("database.optionspanel.name");
    
    /**
     * The label for the compact option.
     */
    private static final String COMPACT_DATABASE_LABEL = Constant.messages.getString("database.optionspanel.option.compact.label");
    
    /**
     * The label for the request body size.
     */
    private static final String REQUEST_BODY_SIZE_DATABASE_LABEL = Constant.messages.getString("database.optionspanel.option.request.body.size.label");

    /**
     * The label for the response body size.
     */
    private static final String RESPONSE_BODY_SIZE_DATABASE_LABEL = Constant.messages.getString("database.optionspanel.option.response.body.size.label");

    
	/**
	 * The check box used to select/deselect the compact option.
	 */
	private JCheckBox checkBoxCompactDatabase = null;
	
	/**
	 * The spinner to select the size of the request body in the History table
	 */
	private ZapSizeNumberSpinner spinnerRequestBodySize = null;
	
	/**
	 * The spinner to select the size of the response body in the History table
	 */
	private ZapSizeNumberSpinner spinnerResponseBodySize = null;
	
    public OptionsDatabasePanel() {
        super();
        setName(NAME);
        
        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        
        java.awt.GridBagConstraints gridBagConstraintsLabelRequestBodySize = new GridBagConstraints();
        gridBagConstraintsLabelRequestBodySize.gridx = 0;
        gridBagConstraintsLabelRequestBodySize.gridy = 1;
        gridBagConstraintsLabelRequestBodySize.insets = new java.awt.Insets(2,2,2,2);
        gridBagConstraintsLabelRequestBodySize.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraintsLabelRequestBodySize.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraintsLabelRequestBodySize.weightx = 0.5D;
        
        java.awt.GridBagConstraints gridBagConstraintsRequestBodySize = new GridBagConstraints();
        gridBagConstraintsRequestBodySize.gridx = 1;
        gridBagConstraintsRequestBodySize.gridy = 1;
        gridBagConstraintsRequestBodySize.weightx = 0.5D;
        gridBagConstraintsRequestBodySize.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraintsRequestBodySize.insets = new java.awt.Insets(2,2,2,2);
        gridBagConstraintsRequestBodySize.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraintsRequestBodySize.ipadx = 50;

        java.awt.GridBagConstraints gridBagConstraintsLabelResponseBodySize = new GridBagConstraints();
        gridBagConstraintsLabelResponseBodySize.gridx = 0;
        gridBagConstraintsLabelResponseBodySize.gridy = 2;
        gridBagConstraintsLabelResponseBodySize.insets = new java.awt.Insets(2,2,2,2);
        gridBagConstraintsLabelResponseBodySize.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraintsLabelResponseBodySize.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraintsLabelResponseBodySize.weightx = 0.5D;
        
        java.awt.GridBagConstraints gridBagConstraintsResponseBodySize = new GridBagConstraints();
        gridBagConstraintsResponseBodySize.gridx = 1;
        gridBagConstraintsResponseBodySize.gridy = 2;
        gridBagConstraintsResponseBodySize.weightx = 0.5D;
        gridBagConstraintsResponseBodySize.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraintsResponseBodySize.insets = new java.awt.Insets(2,2,2,2);
        gridBagConstraintsResponseBodySize.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraintsResponseBodySize.ipadx = 50;

        
        javax.swing.JLabel jLabelRequestBodySize = new JLabel();
        javax.swing.JLabel jLabelResponseBodySize = new JLabel();
        jLabelRequestBodySize.setText(REQUEST_BODY_SIZE_DATABASE_LABEL);
        jLabelResponseBodySize.setText(RESPONSE_BODY_SIZE_DATABASE_LABEL);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        panel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new java.awt.Insets(2,2,2,2);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(getCheckBoxCompactDatabase(), gbc);
        panel.add(jLabelRequestBodySize, gridBagConstraintsLabelRequestBodySize);
        panel.add(getRequestBodySize(), gridBagConstraintsRequestBodySize);
        panel.add(jLabelResponseBodySize, gridBagConstraintsLabelResponseBodySize);
        panel.add(getResponseBodySize(), gridBagConstraintsResponseBodySize);
        add(panel);
    }
    
    private JCheckBox getCheckBoxCompactDatabase() {
        if (checkBoxCompactDatabase == null) {
            checkBoxCompactDatabase = new JCheckBox(COMPACT_DATABASE_LABEL);
        }
        return checkBoxCompactDatabase;
    }
    
    private ZapSizeNumberSpinner getRequestBodySize() {
        if (spinnerRequestBodySize == null) {
        	spinnerRequestBodySize = new ZapSizeNumberSpinner(16777216);
        }
        return spinnerRequestBodySize;
    }
    
    private ZapSizeNumberSpinner getResponseBodySize() {
        if (spinnerResponseBodySize == null) {
        	spinnerResponseBodySize = new ZapSizeNumberSpinner(16777216);
        }
        return spinnerResponseBodySize;
    }
    
    @Override
    public void initParam(Object obj) {
        final OptionsParam options = (OptionsParam) obj;
        final DatabaseParam param = options.getDatabaseParam();
        
        checkBoxCompactDatabase.setSelected(param.isCompactDatabase());
        spinnerRequestBodySize.setValue(param.getRequestBodySize());
        spinnerResponseBodySize.setValue(param.getResponseBodySize());
    }

    @Override
    public void validateParam(Object obj) throws Exception {
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        final OptionsParam options = (OptionsParam) obj;
        final DatabaseParam param = options.getDatabaseParam();
        
        param.setCompactDatabase(checkBoxCompactDatabase.isSelected());
        param.setRequestBodySize(spinnerRequestBodySize.getValue());
        param.setResponseBodySize(spinnerResponseBodySize.getValue());
    }
    
    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.database"; 
    }
}