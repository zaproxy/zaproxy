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
package org.zaproxy.zap.extension.keyboard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.KeyStroke;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.StandardFieldsDialog;

public class DialogEditShortcut extends StandardFieldsDialog {

	private static final String FIELD_ACTION = "keyboard.dialog.label.action"; 
	private static final String FIELD_KEY = "keyboard.dialog.label.key"; 
	private static final String FIELD_CONTROL = "keyboard.dialog.label.control"; 
	private static final String FIELD_ALT = "keyboard.dialog.label.alt"; 
	private static final String FIELD_SHIFT = "keyboard.dialog.label.shift"; 
	private static final String FIELD_INFO = "keyboard.dialog.label.info"; 

	private static final long serialVersionUID = 1L;

	private KeyboardShortcut shortcut;
	private KeyboardShortcutTableModel model;

	/**
	 * Constructs a modal {@code DialogEditShortcut}, with the given {@code Window} as its owner.
	 *
	 * @param owner the owner of the dialogue
	 * @since 2.5.0
	 */
	public DialogEditShortcut(Window owner) {
		super(owner, "keyboard.dialog.title", new Dimension(300, 200), true);
	}

	public DialogEditShortcut(Frame owner) {
		super(owner, "keyboard.dialog.title", new Dimension(300, 200));
	}

	public void init (KeyboardShortcut shortcut, KeyboardShortcutTableModel model) {
		this.shortcut = shortcut;
		this.model = model;

		this.removeAllFields();

		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkDuplicate();
			}};
		
		this.addReadOnlyField(FIELD_ACTION, shortcut.getName(), false);
		this.addComboField(FIELD_KEY, getKeyList(), getKey(shortcut.getKeyStroke()));
		this.addFieldListener(FIELD_KEY, listener);
		this.addCheckBoxField(FIELD_CONTROL, this.isModifier(shortcut.getKeyStroke(), InputEvent.CTRL_DOWN_MASK));
		this.addFieldListener(FIELD_CONTROL, listener);
		this.addCheckBoxField(FIELD_ALT, this.isModifier(shortcut.getKeyStroke(), InputEvent.ALT_DOWN_MASK));
		this.addFieldListener(FIELD_ALT, listener);
		this.addCheckBoxField(FIELD_SHIFT, this.isModifier(shortcut.getKeyStroke(), InputEvent.SHIFT_DOWN_MASK));
		this.addFieldListener(FIELD_SHIFT, listener);
		this.addReadOnlyField(FIELD_INFO, "", true);
		
		this.getField(FIELD_INFO).setForeground(Color.RED);
	}
	
	@Override
	public String getSaveButtonText() {
		// Not really saving, just setting here..
		return Constant.messages.getString("keyboard.dialog.button.save");
	}

	/**
	 * Checks to see if the chosen shortcut is already being used and if so shows a message warning the user
	 */
	private void checkDuplicate() {
		KeyboardShortcut ks = this.getDuplicate();
		if (ks != null) {
			this.setFieldValue(FIELD_INFO, 
					MessageFormat.format(Constant.messages.getString("keyboard.dialog.warning.dup"), ks.getName()));
		} else {
			this.setFieldValue(FIELD_INFO, "");
		}
	}
	
	private KeyboardShortcut getDuplicate() {
		KeyStroke chosenKs = this.getKeyStroke();
		if (chosenKs != null) {
			for (KeyboardShortcut ks : this.model.getElements()) {
				if ( ! ks.equals(this.shortcut)) {
					KeyStroke testKs = ks.getKeyStroke();
					if (testKs != null && 
							chosenKs.getKeyCode() == testKs.getKeyCode() &&
							chosenKs.getModifiers() == testKs.getModifiers()) {
						return ks;
					}
				}
			}
		}
		return null;
	}
	
	private List<String> getKeyList() {
		List<String> list = new ArrayList<String>();
		list.add("");	// Always start with a blank option - means no key set
		for(char c = 'A'; c <= 'Z'; c++) {
			list.add(String.valueOf(c));
		}
		// Numbers
		for(int i=0; i <= 9; i++) {
			list.add(String.valueOf(i));
		}
		// Non Alphnumeric keys
		list.add("-");
		list.add("=");
		list.add("[");
		list.add("]");
		list.add(";");
		list.add("'");
		list.add("#");
		list.add(",");
		list.add(".");
		list.add("/");
		// Function keys
		for (int i=1; i<= 12; i++) {
			list.add("F" + i);
		}
		// Arrow keys
		list.add(Constant.messages.getString("keyboard.key.up"));
		list.add(Constant.messages.getString("keyboard.key.down"));
		list.add(Constant.messages.getString("keyboard.key.left"));
		list.add(Constant.messages.getString("keyboard.key.right"));
		
		return list;
	}
	
	private String getKey(KeyStroke ks) {
		if (ks != null) {
			return KeyboardMapping.keyString(ks.getKeyCode());
		}
		return "";
	}
	
	private char selectedKey() {
		return KeyboardMapping.keyCode(this.getStringValue(FIELD_KEY));
	}
	
	private boolean isModifier(KeyStroke ks, int modifier) {
		if (ks != null) {
			return (ks.getModifiers() & modifier) != 0;
		}
		return false;
	}

	public KeyStroke getKeyStroke() {
		KeyStroke ks = null;
    	int keyCode = selectedKey();
		int modifiers = 0;
		
		if (keyCode != 0) {
			if (this.getBoolValue(FIELD_CONTROL)) {
				modifiers |= InputEvent.CTRL_DOWN_MASK;
			}
			if (this.getBoolValue(FIELD_ALT)) {
				modifiers |= InputEvent.ALT_DOWN_MASK;
			}
			if (this.getBoolValue(FIELD_SHIFT)) {
				modifiers |= InputEvent.SHIFT_DOWN_MASK;
			}
			ks = KeyStroke.getKeyStroke(keyCode, modifiers, false);
		}
    	return ks;
	}

	@Override
	public void save() {
		KeyboardShortcut ksDup = this.getDuplicate();
		if (ksDup != null) {
			// used for another menu item, so remove it from that
			ksDup.setKeyStroke(null);
		}
		KeyStroke ks = getKeyStroke();
    	shortcut.setKeyStroke(ks);
	}

	@Override
	public String validateFields() {
		// Nothing to do
		return null;
	}
	
}
