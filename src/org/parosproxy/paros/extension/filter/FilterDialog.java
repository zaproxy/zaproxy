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
package org.parosproxy.paros.extension.filter;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.List;

import org.parosproxy.paros.view.AbstractParamDialog;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FilterDialog extends AbstractParamDialog {

    private static final String[] ROOT = {};
	private AllFilterPanel allFilterPanel = null;
    public FilterDialog() {
        super();
        initialize();
        
    }
    
    public FilterDialog(Frame parent) throws HeadlessException {
        super(parent, true, "Filter", "Filters");
        initialize();
    }

    private void initialize() {
                this.setTitle("Filters");
                this.setSize(640, 480);
        addParamPanel(ROOT, getAllFilterPanel());
        getBtnCancel().setEnabled(false);
    }


	/**
	 * This method initializes allFilterPanel	
	 * 	
	 * @return com.proofsecure.paros.extension.filter.AllFilterPanel	
	 */    
	private AllFilterPanel getAllFilterPanel() {
		if (allFilterPanel == null) {
			allFilterPanel = new AllFilterPanel();
			allFilterPanel.setName("Filter");
		}
		return allFilterPanel;
	}
	
	void setAllFilters(List allFilters) {
	    getAllFilterPanel().getAllFilterTableModel().setTable(allFilters);
	}
   }  //  @jve:decl-index=0:visual-constraint="10,10"
