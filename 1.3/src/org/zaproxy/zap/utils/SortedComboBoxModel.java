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

public class SortedComboBoxModel extends DefaultComboBoxModel {

	private static final long serialVersionUID = 1L;

	public SortedComboBoxModel() {
		super();
	}

	public SortedComboBoxModel(Object[] items) {
		super( items );
	}

	@SuppressWarnings("unchecked")
	public SortedComboBoxModel(Vector items) {
		super( items );
	}

	@SuppressWarnings("unchecked")
	public void addElement(Object element) {
		int index = 0;
		int size = getSize();

		//  Determine where to insert element to keep list in sorted order

		for (index = 0; index < size; index++)
		{
			Comparable c = (Comparable)getElementAt( index );

			if (c.compareTo(element) > 0)
				break;
		}

		super.insertElementAt(element, index);
	}

	public void insertElementAt(Object element, int index) {
		addElement( element );
	}
}
