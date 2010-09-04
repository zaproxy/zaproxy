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
package org.parosproxy.paros.control;

import javax.swing.JOptionPane;

import org.apache.commons.configuration.ConfigurationException;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.OptionsDialog;
import org.parosproxy.paros.view.View;



/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MenuToolsControl {
	
	private View view = null;
	private Model model = null;
	private Control control = null;
	
	public MenuToolsControl() {
	    // use implicit MVC if not given
	    view = View.getSingleton();
	    model = Model.getSingleton();
	    control = Control.getSingleton();
	}
	
	public MenuToolsControl(Model model, View view, Control control) {
	    // best use explicit class contructor
	    this.model = model;
	    this.view = view;
	    this.control = control;
	}
	
	public void options() {
	    OptionsDialog dialog = view.getOptionsDialog("Options");
	    dialog.initParam(model.getOptionsParam());
		int result = dialog.showDialog(false);
		if (result == JOptionPane.OK_OPTION) {
		    try {
                model.getOptionsParam().getConfig().save();
            } catch (ConfigurationException e) {
                e.printStackTrace();
                view.showWarningDialog("Error saving options.");
                return;
            }
		    control.getProxy().stopServer();
		    control.getProxy().startServer();
		}
	}
}
