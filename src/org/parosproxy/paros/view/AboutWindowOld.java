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

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JWindow;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AboutWindowOld extends JWindow {

	private javax.swing.JPanel jContentPane = null;  //  @jve:decl-index=0:visual-constraint="19,20"

	private AboutPanel aboutPanel = null;
    /**
     * 
     */
    public AboutWindowOld() {
        super();
   
		initialize();
 }

    /**
     * @param arg0
     */
    public AboutWindowOld(Frame arg0) {
        super(arg0);
   
		initialize();
 }

    /**
     * @param arg0
     */
    public AboutWindowOld(GraphicsConfiguration arg0) {
        super(arg0);
   
		initialize();
 }

    /**
     * @param arg0
     */
    public AboutWindowOld(Window arg0) {
        super(arg0);
   
		initialize();
 }

    /**
     * @param arg0
     * @param arg1
     */
    public AboutWindowOld(Window arg0, GraphicsConfiguration arg1) {
        super(arg0, arg1);
		initialize();
 }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		this.setVisible(true);
		this.setSize(405, 317);
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new CardLayout());
			jContentPane.add(getAboutPanel(), getAboutPanel().getName());
		}
		return jContentPane;
	}
	/**
	 * This method initializes aboutPanel	
	 * 	
	 * @return com.proofsecure.paros.view.AboutPanel	
	 */    
	private AboutPanel getAboutPanel() {
		if (aboutPanel == null) {
			aboutPanel = new AboutPanel();
			aboutPanel.setName("aboutPanel");
		}
		return aboutPanel;
	}
	
	/**
	 * Centre this frame.
	 *
	 */
	private void centerFrame() {
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
	
	public void setVisible(boolean show) {
	    centerFrame();
	    super.setVisible(show);
	}
 }  //  @jve:decl-index=0:visual-constraint="39,10"
