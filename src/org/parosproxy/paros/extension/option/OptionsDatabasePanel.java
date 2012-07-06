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

/**
 * The GUI database options panel.
 * <p>
 * It allows to change the following database options:
 * <ul>
 * <li>Compact - allows to compact the database on exit.</li>
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
	 * The check box used to select/deselect the compact option.
	 */
	private JCheckBox checkBoxCompactDatabase = null;
	
    public OptionsDatabasePanel() {
        super();
        setName(NAME);
        
        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        panel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new java.awt.Insets(2,2,2,2);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(COMPACT_DATABASE_LABEL), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(getCheckBoxCompactDatabase(), gbc);
        
        add(panel);
    }
    
    private JCheckBox getCheckBoxCompactDatabase() {
        if (checkBoxCompactDatabase == null) {
            checkBoxCompactDatabase = new JCheckBox();
        }
        return checkBoxCompactDatabase;
    }
    
    @Override
    public void initParam(Object obj) {
        final OptionsParam options = (OptionsParam) obj;
        final DatabaseParam param = options.getDatabaseParam();
        
        checkBoxCompactDatabase.setSelected(param.isCompactDatabase());
    }

    @Override
    public void validateParam(Object obj) throws Exception {
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        final OptionsParam options = (OptionsParam) obj;
        final DatabaseParam param = options.getDatabaseParam();
        
        param.setCompactDatabase(checkBoxCompactDatabase.isSelected());
    }
    
    @Override
    public String getHelpIndex() {
        // TODO Add the help page.
        return null; 
    }
}