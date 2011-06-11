package org.zaproxy.zap.utils;

import javax.swing.JTextArea;
import org.zaproxy.zap.utils.UndoRedoHelper;

public class ZapTextArea extends JTextArea {

	private static final long serialVersionUID = 1L;
	
	public ZapTextArea() {
		// TODO Philipp
		UndoRedoHelper.getInstance().register(this);
	}

	public ZapTextArea(int i, int j) {
		super(i, j);
		UndoRedoHelper.getInstance().register(this);
	}

	public ZapTextArea(String s) {
		super(s);
		UndoRedoHelper.getInstance().register(this);
	}
}
