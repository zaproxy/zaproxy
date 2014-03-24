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
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/06/25 Changed visibility of getFilterReplaceDialog() from private
// to protected to ease inheritance. Created processFilterReplaceDialog() method
// to ease extensibility and moved code from getFilterReplaceDialog() there.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
package org.parosproxy.paros.extension.filter;

import java.util.regex.Pattern;

import javax.swing.JOptionPane;


public abstract class FilterAbstractReplace extends FilterAdaptor {
    
    private FilterReplaceDialog filterReplaceDialog = null;
	private Pattern pattern = null;
	private String txtReplace = "";
	
    
	/**
	 * This method initializes filterReplaceDialog	
	 * 	
	 * @return org.parosproxy.paros.extension.filter.FilterReplaceDialog	
	 */    
	protected FilterReplaceDialog getFilterReplaceDialog() {
		if (filterReplaceDialog == null) {
			filterReplaceDialog = new FilterReplaceDialog(getView().getMainFrame(), true);
		}
		return filterReplaceDialog;
	}
	
	@Override
	public boolean isPropertyExists() {
	    return true;
	}
	
	@Override
	public void editProperty() {
	    FilterReplaceDialog dialog = getFilterReplaceDialog();
	    dialog.setView(getView());
	    int result = dialog.showDialog();
	    if (result == JOptionPane.CANCEL_OPTION) {
	        return;
	    }
	    
	    processFilterReplaceDialog(dialog);
	}
	
	/**
	 * Is called when the dialog is closed (except its exit code is
	 * {@link JOptionPane#CANCEL_OPTION}).
	 * 
	 * @param dialog
	 */
	protected void processFilterReplaceDialog(FilterReplaceDialog dialog) {
		// ZAP: Created new method for inheritance reasons to allow for better
		// re-usability and extensibility.
		
	    if (dialog.getTxtPattern().getText().equals("")) {
	        pattern = null;
			// disable filter when empty pattern is entered
			setEnabled(false);
	    } else {
	        pattern = Pattern.compile(dialog.getTxtPattern().getText(), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
			// enable filter when pattern is entered
			setEnabled(true);
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
