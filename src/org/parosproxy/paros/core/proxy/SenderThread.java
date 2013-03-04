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
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method and removed
// unnecessary cast.
// ZAP: 2012/05/02 Changed the "listenerList" to List, changed the
// initialisation from Vector to ArrayList and set the initial capacity.
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods
package org.parosproxy.paros.core.proxy;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;

/**
 * @deprecated No longer used/needed. It will be removed in a future release.
 */
@Deprecated
public class SenderThread implements Runnable {
    
    private HttpMessage msg = null;
    private HttpSender httpSender = null;
    // ZAP: Changed to List.
    private List<SenderThreadListener> listenerList = null;
    
    public SenderThread(HttpSender httpSender, HttpMessage msg, SenderThreadListener listener) {
        this.httpSender = httpSender;
        this.msg = msg;
        // ZAP: Changed to ArrayList and added the initial capacity.
        listenerList = new ArrayList<>(1);
        listenerList.add(listener);
    }   
    
    public void start() {
        Thread t = new Thread(this);
        t.setDaemon(true);
    }
    
    @Override
    public void run() {
        Exception ex = null;
        try {
            getHttpSender().sendAndReceive(getHttpMessage());
            
        } catch (Exception e) {
            ex = e;
        }
        notifyListener(getHttpMessage(), ex);
    }

    private void notifyListener(HttpMessage msg, Exception ex) {
        for (int i=0; i<listenerList.size(); i++) {
            // ZAP: Removed unnecessary cast.
            SenderThreadListener listener = listenerList.get(i);
            listener.onMessageReceive(msg, ex);
        }
    }
    /**
     * @return Returns the msg.
     */
    public HttpMessage getHttpMessage() {
        return msg;
    }
    /**
     * @return Returns the sender.
     */
    public HttpSender getHttpSender() {
        return httpSender;
    }
}
