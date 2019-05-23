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
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments


package org.parosproxy.paros.db;

public class RecordSession {

    private long sessionId = 0;
    private String sessionName = "";
    private java.sql.Date lastAccess = null;

    
    /**
     * @param sessionId
     * @param sessionName
     * @param lastAccess
     */
    public RecordSession(long sessionId, String sessionName, java.sql.Date lastAccess) {
        super();
        this.sessionId = sessionId;
        this.sessionName = sessionName;
        this.lastAccess = lastAccess;
    }
    /**
     * @return Returns the sessionName.
     */
    public String getSessionName() {
        return sessionName;
    }
    /**
     * @param sessionName The sessionName to set.
     */
    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }
    /**
     * @return Returns the lastAccess.
     */
    public java.sql.Date getLastAccess() {
        return lastAccess;
    }
    /**
     * @return Returns the sessionId.
     */
    public long getSessionId() {
        return sessionId;
    }
}
