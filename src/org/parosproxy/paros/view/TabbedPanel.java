/*
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 www.proofsecure.com
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
// ZAP: 2011/06/02 Warn the first time the user double clicks on a tab
// ZAP: 2012/04/23 Added @Override annotation to appropriate method.

package org.parosproxy.paros.view;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;

public class TabbedPanel extends JTabbedPane {

	private static final long serialVersionUID = 8927990541854169615L;

	private java.awt.Container originalParent = null;
    private java.awt.Container alternativeParent = null;
    private java.awt.Component backupChild = null;
    private Logger log = Logger.getLogger(this.getClass());
    
    /**
	 * This is the default constructor
	 */
	public TabbedPanel() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private  void initialize() {
		if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
			this.setSize(225, 145);
		}
		this.addMouseListener(new java.awt.event.MouseAdapter() { 

			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {    

			    if (e.getClickCount() >= 2) {
			        alternateParent();
			    }

			}
		});

	}
	
	public void setAlternativeParent(java.awt.Container alternativeParent) {
	    this.alternativeParent = alternativeParent;
	}
	
	private boolean isAlternative = true;
	
	private void alternateParent() {
	    if (alternativeParent == null) return;

		if (Model.getSingleton().getOptionsParam().getViewParam().getWarnOnTabDoubleClick()) {
			if (View.getSingleton().showConfirmDialog(Constant.messages.getString("tab.doubleClick.warning"))  
					!= JOptionPane.OK_OPTION) {
				// They cancelled the dialog
				return;
			}
			// Only ever warn once
			Model.getSingleton().getOptionsParam().getViewParam().setWarnOnTabDoubleClick(false);
			try {
				Model.getSingleton().getOptionsParam().getViewParam().getConfig().save();
			} catch (ConfigurationException e) {
				log.error(e.getMessage(), e);
			}
		}
	    
	    if (isAlternative) {
	        
	        originalParent = this.getParent();
	        originalParent.remove(this);
	        backupChild = alternativeParent.getComponent(0);
	        alternativeParent.remove(backupChild);
	        alternativeParent.add(this, "");
	    } else {
	        alternativeParent.remove(this);
	        alternativeParent.add(backupChild, "");
	        originalParent.add(this, "");
	    }
        originalParent.validate();
        alternativeParent.validate();
        this.validate();
	    isAlternative = !isAlternative;
	}
	

}  //  @jve:visual-info  decl-index=0 visual-constraint="10,10"
