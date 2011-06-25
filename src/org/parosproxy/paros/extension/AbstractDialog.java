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
package org.parosproxy.paros.extension;


import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JDialog;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;


/**
 * Abstract base class for all dialog box.
 */
abstract public class AbstractDialog extends JDialog {
 
	private static final long serialVersionUID = -3951504408180103696L;

	protected AbstractDialog thisDialog = null;
    
    /**
	 * @throws java.awt.HeadlessException
	 */
	public AbstractDialog() throws HeadlessException {
		super();
		initialize();
	}

	/**
	 * @param owner
	 * @param modal
	 * @throws java.awt.HeadlessException
	 */
	public AbstractDialog(Frame owner, boolean modal) throws HeadlessException {
		super(owner, modal);
		initialize();
	}


	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setVisible(false);
		this.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(300,200);
	    }
		this.setTitle(Constant.PROGRAM_NAME);
	}

	/**
	 * Centres this dialog on the main fame.
	 * This is needed, because when using multiple monitors.
	 * Additionally, it will shrink the size of the dialog to fit the screen.
	 */
	public void centreDialog() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		// shrink dialog to fit screen if necessary
		frameSize.height = Math.min(frameSize.height,screenSize.height);
		frameSize.width  = Math.min(frameSize.width, screenSize.width);
		// centres the dialog on main frame 
		final Rectangle mainrect = View.getSingleton().getMainFrame().getBounds();
		int x = mainrect.x + (mainrect.width - frameSize.width) / 2;
		int y = mainrect.y + (mainrect.height - frameSize.height) / 2;
		// finally set the new location
	    this.setLocation(x, y);
	}
	
	public void setVisible(boolean show) {
	    if (show) {
		    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
		    	centreDialog();
		    }
	    }
	    super.setVisible(show);
	}
}
