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

package org.parosproxy.paros.core.spider;

import java.sql.SQLException;

import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class QueueItem {
    private int depth = 0;
    private HistoryReference ref = null;
    
    /**
     * 
     */
    QueueItem() {
        super();
    }
    
    
    QueueItem(Session session, int historyType, HttpMessage msg) throws HttpMalformedHeaderException, SQLException {
        ref = new HistoryReference(session, historyType, msg);
    }
    
    void setDepth(int depth) {
        this.depth = depth;
    }
    int getDepth() {
        return depth;
    }
    
    HttpMessage getMessage() {
        HttpMessage msg = null;
        try {
            msg = ref.getHttpMessage();
        } catch (Exception e) {}
        
        return msg;
    }
    
    HistoryReference getHistoryReference() {
        return ref;
    }
}
