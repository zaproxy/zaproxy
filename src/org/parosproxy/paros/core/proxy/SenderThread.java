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
package org.parosproxy.paros.core.proxy;

import java.util.Vector;

import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SenderThread implements Runnable {
    
    private HttpMessage msg = null;
    private HttpSender httpSender = null;
    private Vector listenerList = null;
    public SenderThread(HttpSender httpSender, HttpMessage msg, SenderThreadListener listener) {
        this.httpSender = httpSender;
        this.msg = msg;
        listenerList = new Vector(1);
        listenerList.add(listener);
    }   
    
    public void start() {
        Thread t = new Thread(this);
        t.setDaemon(true);
    }
    
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
            SenderThreadListener listener = (SenderThreadListener) listenerList.get(i);
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
