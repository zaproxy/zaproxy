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
package org.parosproxy.paros.view;

import java.awt.Frame;
import java.awt.HeadlessException;

import org.parosproxy.paros.Constant;

public class OptionsDialog extends AbstractParamDialog {

	private static final long serialVersionUID = -4374132178769109917L;

    public OptionsDialog() {
        super();
 		initialize();
   }
    /**
     * @param parent
     * @param modal
     * @param title
     * @throws HeadlessException
     */
    public OptionsDialog(Frame parent, boolean modal, String title)
            throws HeadlessException {
        super(parent, modal, title, Constant.messages.getString("options.dialog.rootName"));
        initialize();
    }
	/**
	 * This method initializes this
	 */
	private void initialize() {

		// ZAP: Increase width and height of options dialog
	    //if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(750, 584);
	    //}
	}
	
	
}
