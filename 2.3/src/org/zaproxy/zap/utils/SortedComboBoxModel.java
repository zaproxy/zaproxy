/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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

// Code ripped off from http://forums.sun.com/thread.jspa?threadID=520282

package org.zaproxy.zap.utils;

import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

public class SortedComboBoxModel<E extends Comparable<E>> extends DefaultComboBoxModel<E> {

	private static final long serialVersionUID = 1L;

	public SortedComboBoxModel() {
		super();
	}

	public SortedComboBoxModel(E[] items) {
		super( items );
	}

	public SortedComboBoxModel(Vector<E> items) {
		super( items );
	}

	@Override
	public void addElement(E element) {
		int size = getSize();

		if (size == 0) {
			super.addElement(element);
			return;
		}
		
		//  Determine where to insert element to keep list in sorted order

		int index = 0;
		for (; index < size; index++) {
			if (getElementAt(index).compareTo(element) > 0) {
				break;
			}
		}

		super.insertElementAt(element, index);
	}

	@Override
	public void insertElementAt(E element, int index) {
		addElement( element );
	}

	/**
	 * Notifies the listeners that the given {@code element} of the combo box model was changed.
	 * <p>
	 * The call to this method has no effect if the given {@code element} doesn't exist in the combo box model.
	 * </p>
	 * 
	 * @param element the element that was changed.
	 * @since 2.3.0
	 */
	public void elementChanged(E element) {
		int idx = getIndexOf(element);
		if (idx < 0) {
			return;
		}
		super.fireContentsChanged(this, idx, idx);
	}
}
