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

import java.util.regex.Pattern;

import javax.swing.JOptionPane;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
abstract public class FilterAbstractReplace extends FilterAdaptor {
    
    private FilterReplaceDialog filterReplaceDialog = null;
	private Pattern pattern = null;
	private String txtReplace = "";
	
    
	/**
	 * This method initializes filterReplaceDialog	
	 * 	
	 * @return com.proofsecure.paros.extension.filter.FilterReplaceDialog	
	 */    
	private FilterReplaceDialog getFilterReplaceDialog() {
		if (filterReplaceDialog == null) {
			filterReplaceDialog = new FilterReplaceDialog(getView().getMainFrame(), true);
		}
		return filterReplaceDialog;
	}
	
	public boolean isPropertyExists() {
	    return true;
	}
	
	public void editProperty() {
	    FilterReplaceDialog dialog = getFilterReplaceDialog();
	    dialog.setView(getView());
	    int result = dialog.showDialog();
	    if (result == JOptionPane.CANCEL_OPTION) {
	        return;
	    }
	    
	    if (dialog.getTxtPattern().getText().equals("")) {
	        pattern = null;
	    } else {
	        pattern = Pattern.compile(dialog.getTxtPattern().getText(), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
	    }
	    
	    txtReplace = dialog.getTxtReplaceWith().getText();
	    
	}
	
	protected Pattern getPattern() {
	    return pattern;
	}
	
	protected String getReplaceText() {
	    return txtReplace;
	}
	
	
}
