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
// ZAP: 2012/04/23 Removed unnecessary cast.
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods
package org.parosproxy.paros.model;

import java.util.Vector;

import org.parosproxy.paros.network.HttpMessage;

/**
 * @deprecated No longer used/needed. It will be removed in a future release.
 */
@Deprecated
public class HttpMessageList {

    private Vector<HttpMessage> list = new Vector<>();
    public synchronized boolean add(HttpMessage msg) {
        return list.add(msg);
    }
    
    public synchronized int size() {
        return list.size();
    }
    
    public synchronized HttpMessage get(int i) {
        
        HttpMessage msg = null;
        try {
            msg = list.get(i);
        } catch (Exception e) {}
        return msg;
    }
    
    public synchronized boolean isEmpty() {
        return list.isEmpty();
    }
    
    
}
