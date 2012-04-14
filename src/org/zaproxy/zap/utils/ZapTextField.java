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

import javax.swing.JTextField;
import javax.swing.text.Document;

import org.zaproxy.zap.utils.ZapTextComponentUndoManager.UndoManagerPolicy;

/**
 * <code>ZapTextField</code> is a <code>JTextField</code> with
 * <code>UndoableEdits</code>.
 * 
 * <p>
 * It is responsibility of the owner of the <code>ZapTextField</code> to discard
 * all undoable edits when they are not needed.
 * </p>
 * 
 * <p>
 * If you do not need undoable edits consider using a <code>JTextField</code>
 * instead.
 * </p>
 * 
 * @see #discardAllEdits
 * @see #setUndoManagerPolicy
 * @see ZapTextComponentUndoManager
 */
public class ZapTextField extends JTextField {

	private static final long serialVersionUID = 483350845803973996L;

	private ZapTextComponentUndoManager undoManager;

	/**
	 * @see JTextField#JTextField()
	 */
	public ZapTextField() {
		this(null, null, 0);
	}

	/**
	 * @see JTextField#JTextField(int)
	 */
	public ZapTextField(int columns) {
		this(null, null, columns);
	}

	/**
	 * @see JTextField#JTextField(String)
	 */
	public ZapTextField(String text) {
		this(null, text, 0);
	}

	/**
	 * @see JTextField#JTextField(String, int)
	 */
	public ZapTextField(String text, int columns) {
		this(null, text, columns);
	}

	/**
	 * @see JTextField#JTextField(Document, String, int)
	 */
	public ZapTextField(Document doc, String text, int columns) {
		super(doc, text, columns);

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
	 * Sets the maximum number of undoable edits this <code>ZapTextField</code>
	 * can hold.
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
	 * Sets the policy of the undoable edits of this <code>ZapTextField</code>.
	 * 
	 * @param policy
	 *            the new policy
	 * 
	 * @see ZapTextComponentUndoManager#setUndoManagerPolicy(UndoManagerPolicy)
	 */
	public void setUndoManagerPolicy(UndoManagerPolicy policy) {
		undoManager.setUndoManagerPolicy(policy);
	}
}
