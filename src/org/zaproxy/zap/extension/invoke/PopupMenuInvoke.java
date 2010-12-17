/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.invoke;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JTree;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenu;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;

public class PopupMenuInvoke extends ExtensionPopupMenu {

	private static final long serialVersionUID = 1L;
    private Component invoker = null;
    private String command = null;
    private String parameters = null;

    private Logger logger = Logger.getLogger(PopupMenuInvoke.class);

    /**
     * @param label
     */
    public PopupMenuInvoke(String label) {
        super(label);
        this.initialize();
    }

    @Override
    public boolean isSubMenu() {
    	return true;
    }
    
    @Override
    public String getParentMenuName() {
    	return Constant.messages.getString("invoke.site.popup");
    }

    @Override
    public int getParentMenuIndex() {
    	return 1;
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(this.getText());
        
        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {
        		String url = "";	// Full URL
        		String host = "";	// Just the server name, e.g. localhost
        		String port = "";	// the port
        		String site = "";	// e.g. http://localhost:8080/
        		
        		HistoryReference hr = null;
        		
        		if (invoker instanceof JTree && invoker.getName().equals("treeSite")) {
        			JTree treeSite = (JTree) invoker;
        			hr = ((SiteNode) treeSite.getLastSelectedPathComponent()).getHistoryReference();

        		} else if (invoker instanceof JList && invoker.getName().equals("ListLog")) {
                	JList histLog = (JList) invoker;
                	Object obj = histLog.getSelectedValue();
                	if (obj != null && obj instanceof HistoryReference) {
                		
            			hr = (HistoryReference) obj;
                	}
        		}
        		if (hr != null) {
        			try {
        				URI uri = hr.getHttpMessage().getRequestHeader().getURI();
        				url = uri.toString();
        				host = uri.getHost();
        				site = uri.getScheme() + "://" + uri.getHost();
        				if (uri.getPort() > 0) {
        					site = site + ":" + uri.getPort() + "/";
        					port = "" + uri.getPort();
        				} else {
        					if (uri.getScheme().equalsIgnoreCase("http")) {
        						port = "80";
        					} else if (uri.getScheme().equalsIgnoreCase("https")) {
        						port = "443";
        					}
        					site = site + "/";
        				}
						
					} catch (Exception e1) {
						logger.error(e1.getMessage(), e1);
					}
        		}
        		
        		if (command != null) {
        			List<String> cmd = new ArrayList<String>();
        			cmd.add(command);
        			if (parameters != null) {
            			// Replace all of the tags 
        				String params = parameters.replace("%url%", url)
    											.replace("%host%", host)
    											.replace("%port%", port)
    											.replace("%site%", site);
        				for (String p : params.split(" ")) {
        					cmd.add(p);
        				}
        			}
        			try {
        				logger.debug("Invoking: " + cmd.toString());
						new ProcessBuilder(cmd).start();
					} catch (IOException e1) {
						View.getSingleton().showWarningDialog(e1.getMessage());
						logger.error(e1.getMessage(), e1);
					}
        		}
        	}
        });
	}
	
    public boolean isEnableForComponent(Component invoker) {
        this.invoker = invoker;
		if (invoker instanceof JTree && invoker.getName().equals("treeSite")) {
			// The Sites tree
			JTree treeSite = (JTree) invoker;
		    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();
		    if (node != null && ! node.isRoot()) {
		    	// Enabled only for a non root node (which is just "Sites"
		        this.setEnabled(true);
		    } else {
		        this.setEnabled(false);
		    }
            return true;
        } else if (invoker instanceof JList && invoker.getName().equals("ListLog")) {
        	// The history log
        	JList histLog = (JList) invoker;
        	
        	Object obj = histLog.getSelectedValue();
        	if (obj != null && obj instanceof HistoryReference) {
        		// Enabled for all rows
		        this.setEnabled(true);
        	} else {
		        this.setEnabled(false);
        	}
            return true;
		}
        return false;
    }

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
    	
}
