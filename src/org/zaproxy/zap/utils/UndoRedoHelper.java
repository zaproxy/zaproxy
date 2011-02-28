package org.zaproxy.zap.utils;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.parosproxy.paros.Constant;

public class UndoRedoHelper {

	private static UndoRedoHelper instance;
	private UndoManager undo = null;
	
	private UndoRedoHelper() {
		undo = new UndoManager();
	}
	
	public static UndoRedoHelper getInstance() {
		
		if(instance == null) {
			instance = new UndoRedoHelper();
		}
		
		return instance;
		
	}
	
	@SuppressWarnings("serial")
	public void register(JTextArea ta) {
		// Listen for undo and redo events on the textArea
		ta.getDocument().addUndoableEditListener(
				new UndoableEditListener() {
					public void undoableEditHappened(UndoableEditEvent evt) {
						undo.addEdit(evt.getEdit());
					}
				});

		// Create undo action and add it to the textArea
		ta.getActionMap().put("Undo", new AbstractAction("Undo") {
			public void actionPerformed(ActionEvent evt) {
				try {
					if (undo.canUndo()) {
						undo.undo();
					}
				} catch (CannotUndoException e) {
				}
			}
		});

		// Create redo action and add it to the textArea
		ta.getActionMap().put("Redo", new AbstractAction("Redo") {
			public void actionPerformed(ActionEvent evt) {
				try {
					if (undo.canRedo()) {
						undo.redo();
					}
				} catch (CannotRedoException e) {
				}
			}
		});

		// Add key-bindings
		ta.getInputMap().put(KeyStroke.getKeyStroke(Constant.ACCELERATOR_UNDO), "Undo");
		ta.getInputMap().put(KeyStroke.getKeyStroke(Constant.ACCELERATOR_REDO), "Redo");
	}
	
}
