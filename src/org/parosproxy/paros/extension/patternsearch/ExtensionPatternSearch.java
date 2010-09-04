/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2005 Chinotec Technologies Company
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
package org.parosproxy.paros.extension.patternsearch;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;

import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookMenu;
import org.parosproxy.paros.model.HistoryList;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionPatternSearch extends ExtensionAdaptor {

	private JMenuItem menuItemPatternSearch = null;
	private ExtensionHookMenu extensionMenu = null;
	private HistoryList historyList = null;
	
	private SearchDialog searchDialog = null;  //  @jve:decl-index=0:visual-constraint="169,99"
    /**
     * 
     */
    public ExtensionPatternSearch() {
        super();
 		initialize();
   }   

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionSearch");
			
	}
	/**
	 * This method initializes menuItemEncoder	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getMenuItemPatternSearch() {
		if (menuItemPatternSearch == null) {
			menuItemPatternSearch = new JMenuItem();
			menuItemPatternSearch.setText("Extract Pattern in Session...");
			menuItemPatternSearch.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

					getSearchDialog().setVisible(true);
					
				}
			});

		}
		return menuItemPatternSearch;
	}
	
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        extensionHook.getHookMenu().addEditMenuItem(getMenuItemPatternSearch());
	    }
	}
	/**
	 * This method initializes searchDialog1	
	 * 	
	 * @return org.parosproxy.paros.extension.patternsearch.SearchDialog	
	 */    
	private SearchDialog getSearchDialog() {
		if (searchDialog == null) {
			searchDialog = new SearchDialog(getView().getMainFrame(), false);
			searchDialog.setExt(this);
			searchDialog.setView(getView());
			searchDialog.setSize(640, 480);
		}
		return searchDialog;
	}
	
	public HistoryList getHistoryList() {
	    if (historyList == null) {
	        historyList = new HistoryList();
	    }
	    return historyList;
	}
	
	public String search(String filter, boolean isRequest){
	    String result="";
	    Session session = getModel().getSession();
        Pattern pattern = Pattern.compile(filter, Pattern.MULTILINE| Pattern.CASE_INSENSITIVE);
		Matcher matcher = null;
		
		getHistoryList();
	    synchronized (historyList) {
	        try {
	            List list = getModel().getDb().getTableHistory().getHistoryList(session.getSessionId(), HistoryReference.TYPE_MANUAL, filter,isRequest);
	            int last = list.size();
	            for (int index=0;index < last;index++){
	                int v = ((Integer)(list.get(index))).intValue();
	                try {
                        HttpMessage message = getModel().getDb().getTableHistory().read(v).getHttpMessage();

                        if (isRequest){
                            matcher = pattern.matcher(message.getRequestHeader().toString()+ message.getRequestBody().toString());    
                        }
                        else{
                            matcher = pattern.matcher(message.getResponseHeader().toString()+ message.getResponseBody().toString());    
                        }
                		while (matcher.find()) {
                		    if (result.indexOf(matcher.group(0))==-1)
                		            result += "\r\n" + matcher.group(0);
                		}                
                		
                    } catch (HttpMalformedHeaderException e1) {
                        e1.printStackTrace();
                    }	               
	            }	            
	        } catch (SQLException e) {}

	        getView().getOutputPanel().append(result);
	    }
	    return result;
	}
 }
