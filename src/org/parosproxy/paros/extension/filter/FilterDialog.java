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
// ZAP: 2011/04/16 i18n
// ZAP: 2012/04/25 Added argument type to generic type.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2016/04/05 Issue 2458: Fix xlint warning messages 

package org.parosproxy.paros.extension.filter;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.AbstractParamDialog;


public class FilterDialog extends AbstractParamDialog {

	private static final long serialVersionUID = 1L;
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
        this.setTitle(Constant.messages.getString("filter.title.filters"));
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(640, 480);
	    }
	    this.setPreferredSize(new Dimension(640, 480));
        addParamPanel(ROOT, getAllFilterPanel(), true);
        getBtnCancel().setEnabled(false);
        this.pack();
    }


	/**
	 * This method initializes allFilterPanel	
	 * 	
	 * @return org.parosproxy.paros.extension.filter.AllFilterPanel	
	 */    
	private AllFilterPanel getAllFilterPanel() {
		if (allFilterPanel == null) {
			allFilterPanel = new AllFilterPanel();
			allFilterPanel.setName("Filter");
		}
		return allFilterPanel;
	}
	
    // ZAP: Added the type argument.
	void setAllFilters(List<Filter> allFilters) {
	    getAllFilterPanel().getAllFilterTableModel().setTable(allFilters);
	}
   }  //  @jve:decl-index=0:visual-constraint="10,10"
