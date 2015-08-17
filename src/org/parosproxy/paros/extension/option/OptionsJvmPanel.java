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

import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.vafer.jdeb.shaded.compress.io.FileUtils;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The JVM options panel.
 * <p>
 * These options are used by zap.sh and zap.bat when starting ZAP
 * </p>
 * 
 */
public class OptionsJvmPanel extends AbstractParamPanel {

    private static final long serialVersionUID = -7541236934312940852L;
    private static final File JVM_PROPERTIES_FILE = 
    		new File(System.getProperty("user.home"), ".ZAP_JVM.properties");
    
    /**
     * The name of the options panel.
     */
    private static final String NAME = Constant.messages.getString("jvm.options.title");
    
    
	/**
	 * The text field for the JVM options.
	 */
	private ZapTextField jvmOptionsField = null; 
	
    public OptionsJvmPanel() {
        super();
        setName(NAME);
        
        JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		JLabel jvmOptionsLabel = new JLabel(Constant.messages.getString("jvm.options.label.jvmoptions"));
		jvmOptionsLabel.setLabelFor(getJvmOptionsField());
		
		panel.add(jvmOptionsLabel, 
				LayoutHelper.getGBC(0, 0, 1, 1.0));
		panel.add(getJvmOptionsField(), 
				LayoutHelper.getGBC(1, 0, 1, 1.0));

		panel.add(new JLabel(Constant.messages.getString("jvm.options.warning.restart")), 
				LayoutHelper.getGBC(0, 1, 2, 1.0));

		panel.add(new JLabel(), 
				LayoutHelper.getGBC(0, 10, 1, 0.5D, 1.0D));	// Spacer
		
		this.add(panel);
    }
    
	private ZapTextField getJvmOptionsField() {
		if (jvmOptionsField == null) {
			jvmOptionsField = new ZapTextField();
		}
		return jvmOptionsField;
	}
	
    @Override
    public void initParam(Object obj) {
		try {
			/* JVM properties are unusual in that they are held
			 * in a separate file from the other options.
			 * This is for various reasons, including the fact they are used 
			 * by the scripts rather than the java code. 
			 */
			if (JVM_PROPERTIES_FILE.exists()) {
				List<String> lines;
				lines = FileUtils.readLines(JVM_PROPERTIES_FILE);
				if (lines.size() > 0) {
					getJvmOptionsField().setText(lines.get(0));
				}
			}
		} catch (IOException e) {
			// Ignore
		}
    }

    @Override
    public void validateParam(Object obj) throws Exception {
    }

    @Override
    public void saveParam(Object obj) throws Exception {
		try {
			String opts = getJvmOptionsField().getText();
			if (opts.length() == 0) {
				if (JVM_PROPERTIES_FILE.exists()) {
					// Delete the file so that the 'normal' defaults apply
					JVM_PROPERTIES_FILE.delete();
				}
			} else {
				// Replace the file contents, even if its just with whitespace
				FileUtils.writeStringToFile(JVM_PROPERTIES_FILE, opts, false);
			}
		} catch (IOException e) {
			View.getSingleton().showWarningDialog(this, 
					MessageFormat.format(
							Constant.messages.getString("jvm.options.error.writing"),
							JVM_PROPERTIES_FILE.getAbsolutePath(),
							e.getMessage()));
		}
    }
    
    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.jvm"; 
    }
}