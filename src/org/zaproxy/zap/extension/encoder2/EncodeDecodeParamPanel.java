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
package org.zaproxy.zap.extension.encoder2;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.FontUtils;

public class EncodeDecodeParamPanel extends AbstractParamPanel {


	private static final long serialVersionUID = -6357927982804314157L;

	private static final String NAME = Constant.messages.getString("enc2.optionspanel.name");
	private static final String NAME_BASE64 = Constant.messages.getString("enc2.optionspanel.base64");
	private static final String CHARSET_LABEL = Constant.messages.getString("enc2.optionspanel.base64.charset");
	private static final String BREAK_LINES_LABEL = Constant.messages.getString("enc2.optionspanel.base64.breaklines");
	
	private static final String[] CHARSETS = {"ISO-8859-1", "US-ASCII", "UTF-8"};
	
	private JComboBox<String> comboBoxBase64Charset;
	
	private JCheckBox checkBoxBase64DoBreakLines;
	
	private JPanel base64Panel;
	
	public EncodeDecodeParamPanel() {
		super();
		setName(NAME);
		
		setLayout(new BorderLayout(0, 0));
		
		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(2, 2, 2, 2));
		
		panel.add(getBase64Panel(), BorderLayout.NORTH);
		
		add(panel);
	}
	
	private JPanel getBase64Panel() {
		if (base64Panel == null) {
			base64Panel = new JPanel();
			base64Panel.setLayout(new GridBagLayout());
			
			GridBagConstraints gbc = new GridBagConstraints();
			
			base64Panel.setBorder(BorderFactory.createTitledBorder(null, NAME_BASE64, 
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
					FontUtils.getFont(FontUtils.Size.standard), java.awt.Color.black));

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = new java.awt.Insets(2,2,2,2);
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 0.5D;
			base64Panel.add(new JLabel(CHARSET_LABEL), gbc);

			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.ipadx = 50;
			base64Panel.add(getComboBoxBase64Charset(), gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.ipadx = 0;
			base64Panel.add(new JLabel(BREAK_LINES_LABEL), gbc);

			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.ipadx = 50;
			base64Panel.add(getCheckBoxBoxBase64DoBreakLines(), gbc);
		}
		return base64Panel;
	}
	
	private JComboBox<String> getComboBoxBase64Charset() {
		if (comboBoxBase64Charset == null) {
			comboBoxBase64Charset = new JComboBox<>(new DefaultComboBoxModel<>(CHARSETS));
		}
		return comboBoxBase64Charset;
	}
	
	private JCheckBox getCheckBoxBoxBase64DoBreakLines() {
		if (checkBoxBase64DoBreakLines == null) {
			checkBoxBase64DoBreakLines = new JCheckBox();
		}
		return checkBoxBase64DoBreakLines;
	}
	
	@Override
	public void initParam(Object obj) {
		final OptionsParam options = (OptionsParam) obj;
		final EncodeDecodeParam param = options.getParamSet(EncodeDecodeParam.class);
		
		comboBoxBase64Charset.setSelectedItem(param.getBase64Charset());
		
		checkBoxBase64DoBreakLines.setSelected(param.isBase64DoBreakLines());
	}

	@Override
	public void validateParam(Object obj) throws Exception {
	}

	@Override
	public void saveParam(Object obj) throws Exception {
		final OptionsParam options = (OptionsParam) obj;
		final EncodeDecodeParam param = options.getParamSet(EncodeDecodeParam.class);
		
		param.setBase64Charset((String)comboBoxBase64Charset.getSelectedItem());
		
		param.setBase64DoBreakLines(checkBoxBase64DoBreakLines.isSelected());
	}
	
	@Override
	public String getHelpIndex() {
		return null; // TODO
	}
}
