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
package org.zaproxy.zap.utils;

import javax.swing.JTextArea;
import javax.swing.text.Document;

import org.zaproxy.zap.utils.ZapTextComponentUndoManager.UndoManagerPolicy;

/**
 * {@code ZapTextArea} is a {@code JTextArea} with {@code UndoableEdit}s.
 * <p>
 * The default is to maintain a window of 100 undoable edits. When the limit is
 * reached older undoable edits start to be discarded when new ones are saved.
 * The limit can be changed with the method {@code setEditsLimit(int)}.
 * </p>
 * <p>
 * It is responsibility of the owner of the {@code ZapTextArea} to discard all
 * undoable edits when they are not needed.
 * </p>
 * <p>
 * If you do not need undoable edits consider using a {@code JTextArea} instead.
 * </p>
 * 
 * @since 1.3.0
 * @see #discardAllEdits()
 * @see #setUndoManagerPolicy
 * @see #setEditsLimit(int)
 * @see ZapTextComponentUndoManager
 */
public class ZapTextArea extends JTextArea {

	private static final long serialVersionUID = -5473367713363097247L;

	private ZapTextComponentUndoManager undoManager;

	/**
	 * Constructs a {@code ZapTextArea}, with a default {@code Document}, {@code null} text and zero rows/columns.
	 */
	public ZapTextArea() {
		this(null, null, 0, 0);
	}

	/**
	 * Constructs a {@code ZapTextArea}, with the given {@code Document}, {@code null} text and zero rows/columns.
	 * 
	 * @param doc the document of the text area
	 */
	public ZapTextArea(Document doc) {
		this(doc, null, 0, 0);
	}

	/**
	 * Constructs a {@code ZapTextArea}, with a default {@code Document}, the given {@code text} and zero rows/columns.
	 * 
	 * @param text the initial text of the text area
	 */
	public ZapTextArea(String text) {
		this(null, text, 0, 0);
	}

	/**
	 * Constructs a {@code ZapTextArea}, with a default {@code Document}, {@code null} {@code text} and the given number of rows
	 * and columns.
	 * 
	 * @param rows the number of rows of the text area
	 * @param columns the number of columns of the text area
	 */
	public ZapTextArea(int rows, int columns) {
		this(null, null, rows, columns);
	}

	/**
	 * Constructs a {@code ZapTextArea}, with a default {@code Document}, the given {@code text} and the given number of rows
	 * and columns.
	 * 
	 * @param text the initial text of the text area
	 * @param rows the number of rows of the text area
	 * @param columns the number of columns of the text area
	 */
	public ZapTextArea(String text, int rows, int columns) {
		this(null, text, rows, columns);
	}

	/**
	 * Constructs a {@code ZapTextArea}, with the given {@code Document}, {@code text} and number of rows and columns.
	 * 
	 * @param doc the document of the text area
	 * @param text the initial text of the text area
	 * @param rows the number of rows of the text area
	 * @param columns the number of columns of the text area
	 */
	public ZapTextArea(Document doc, String text, int rows, int columns) {
		super(doc, text, rows, columns);

		undoManager = new ZapTextComponentUndoManager(this);
	}

	/**
	 * Discards all undoable edits.
	 * 
	 * @see ZapTextComponentUndoManager#discardAllEdits()
	 */
	public void discardAllEdits() {
		undoManager.discardAllEdits();
	}

	/**
	 * Sets the maximum number of undoable edits this {@code ZapTextArea} can
	 * hold.
	 * 
	 * @param limit
	 *            the new limit
	 * 
	 * @see ZapTextComponentUndoManager#setLimit(int)
	 */
	public void setEditsLimit(int limit) {
		undoManager.setLimit(limit);
	}

	/**
	 * Sets the policy of the undoable edits of this {@code ZapTextArea}.
	 * 
	 * @param policy
	 *            the new policy
	 * 
	 * @throws NullPointerException
	 *             if policy is {@code null}
	 * 
	 * @see ZapTextComponentUndoManager#setUndoManagerPolicy(UndoManagerPolicy)
	 */
	public void setUndoManagerPolicy(UndoManagerPolicy policy) throws NullPointerException {
		undoManager.setUndoManagerPolicy(policy);
	}

}
