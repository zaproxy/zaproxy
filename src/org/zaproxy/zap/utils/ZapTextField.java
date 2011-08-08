package org.zaproxy.zap.utils;

import javax.swing.JTextField;

public class ZapTextField extends JTextField {

	private static final long serialVersionUID = 1L;
	
	public ZapTextField() {
		UndoRedoHelper.getInstance().register(this);
	}
}
