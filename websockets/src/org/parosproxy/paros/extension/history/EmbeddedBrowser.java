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
// ZAP: Disabled the platform specific browser
/*
package org.parosproxy.paros.extension.history;

import org.jdesktop.jdic.browser.WebBrowser;
import org.jdesktop.jdic.browser.WebBrowserEvent;
import org.jdesktop.jdic.browser.WebBrowserListener;
import org.parosproxy.paros.control.Control;

public class EmbeddedBrowser extends WebBrowser implements WebBrowserListener {

    PopupMenuEmbeddedBrowser menu = null;
    
	public EmbeddedBrowser() {
		super();
		initialize();
	}

    void setPopupMenuEmbeddedBrowser(PopupMenuEmbeddedBrowser menu) {
        this.menu = menu;
    }
    
    private void initialize() {
        addWebBrowserListener(this);
        
    }

    public void downloadStarted(WebBrowserEvent event) {
//        System.out.println("download started");

    }

    public void downloadCompleted(WebBrowserEvent event) {
//        System.out.println("download completed");
        
    }

    public void downloadProgress(WebBrowserEvent event) {
    }

    public void downloadError(WebBrowserEvent event) {
//        Control.getSingleton().getProxy().setEnableCacheProcessing(false);
        
    }

    public void documentCompleted(WebBrowserEvent event) {
//        System.out.println("doc completed");
        Control.getSingleton().getProxy().setEnableCacheProcessing(false);
    }

    public void titleChange(WebBrowserEvent event) {
        
    }

    public void statusTextChange(WebBrowserEvent event) {
        
    }

}
*/