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

import javax.swing.JMenuItem;

import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.network.HttpMessage;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionFilter extends ExtensionAdaptor implements ProxyListener, Runnable {

	private JMenuItem menuToolsFilter = null;
	private FilterFactory filterFactory = new FilterFactory();
	private boolean isStop = false;
	
    /**
     * 
     */
    public ExtensionFilter() {
        super();
    }


    
    public void init() {
        filterFactory.loadAllFilter();
        Filter filter = null;
        for (int i=0; i<filterFactory.getAllFilter().size(); i++) {
            filter = (Filter) filterFactory.getAllFilter().get(i);
            try {
                filter.init();
            } catch (Exception e) {}
        }
        
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
    }

    public void initView(ViewDelegate view) {
        super.initView(view);
        Filter filter = null;
        for (int i=0; i<filterFactory.getAllFilter().size(); i++) {
            filter = (Filter) filterFactory.getAllFilter().get(i);
            try {
                filter.initView(view);
            } catch (Exception e) {}
        }
    }
    
	/**
	 * This method initializes menuToolsFilter	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getMenuToolsFilter() {
		if (menuToolsFilter == null) {
			menuToolsFilter = new JMenuItem();
			menuToolsFilter.setText("Filter...");
			menuToolsFilter.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

					FilterDialog dialog = new FilterDialog(getView().getMainFrame());
				    dialog.setAllFilters(filterFactory.getAllFilter());
				    dialog.showDialog(false);
				}
			});

		}
		return menuToolsFilter;
	}

	
	public void hook(ExtensionHook extensionHook) {
	    if (getView() != null) {
	        extensionHook.getHookMenu().addToolsMenuItem(getMenuToolsFilter());
	    }
	    extensionHook.addProxyListener(this);
	}



    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.proxy.ProxyListener#onHttpRequestSend(com.proofsecure.paros.network.HttpMessage)
     */
    public void onHttpRequestSend(HttpMessage httpMessage) {
        Filter filter = null;
        for (int i=0; i<filterFactory.getAllFilter().size(); i++) {
            filter = (Filter) filterFactory.getAllFilter().get(i);
            try {
                if (filter.isEnabled()) {
                    filter.onHttpRequestSend(httpMessage);
                }
            } catch (Exception e) {}
        }
        
    }



    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.proxy.ProxyListener#onHttpResponseReceive(com.proofsecure.paros.network.HttpMessage)
     */
    public void onHttpResponseReceive(HttpMessage httpMessage) {
        Filter filter = null;
        for (int i=0; i<filterFactory.getAllFilter().size(); i++) {
            filter = (Filter) filterFactory.getAllFilter().get(i);
            try {
                if (filter.isEnabled()) {

                    filter.onHttpResponseReceive(httpMessage);
                }
            } catch (Exception e) {}
        }
        
    }

    /**
     * Destroy every filter during extension destroy.
     */
    public void destroy() {
        isStop = true;
        Filter filter = null;
        for (int i=0; i<filterFactory.getAllFilter().size(); i++) {
            filter = (Filter) filterFactory.getAllFilter().get(i);
            try {
                filter.destroy();
            } catch (Exception e) {}
        }
        
        
    }

    public void run() {
        Filter filter = null;
        
        while (!isStop) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
            }
            for (int i=0; i<filterFactory.getAllFilter().size(); i++) {
                filter = (Filter) filterFactory.getAllFilter().get(i);
                try {
                    if (filter.isEnabled()) {
                        filter.timer();
                    }
                } catch (Exception e) {}
            }
        }
        
    }
    
  }
