/*
 * Created on May 17, 2004
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

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import org.parosproxy.paros.Constant;

public abstract class AbstractFrame extends JFrame {


	private static final long serialVersionUID = 7658476500356416014L;

	/**
	 * This is the default constructor
	 */
	public AbstractFrame() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		// ZAP: Rebrand
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/zap48x48.png")));
		
		this.setVisible(false);
		this.setTitle(Constant.PROGRAM_NAME);
		this.setSize(800, 600);
	    this.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
	    centerFrame();
	}
	/**
	 * Centre this frame.
	 *
	 */
	public void centerFrame() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
	    this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
	}
}  //  @jve:visual-info  decl-index=0 visual-constraint="31,17"
