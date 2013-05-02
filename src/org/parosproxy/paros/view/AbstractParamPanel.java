/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
package org.parosproxy.paros.view;

import javax.swing.JPanel;

import org.parosproxy.paros.model.Model;

public abstract class AbstractParamPanel extends JPanel {

	private static final long serialVersionUID = 3245127348676340802L;

	/**
	 * This is the default constructor
	 */
	public AbstractParamPanel() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 */
	private  void initialize() {
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(500, 375);
	    }
	}
	
	public abstract void initParam(Object obj);
	
	public abstract void validateParam(Object obj) throws Exception;
	
	public abstract void saveParam(Object obj) throws Exception;
	
	/**
	 * @return The help index key, as used in JavaHelp.
	 *         OR use return null, if no help key is available.
	 *         When a valid key is returned, there will be a help button displayed.
	 */
	public abstract String getHelpIndex();
}
