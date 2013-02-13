/*
*
* Paros and its related class files.
* 
* Paros is an HTTP/HTTPS proxy for assessing web application security.
* Copyright (C) 2003-2006 Chinotec Technologies Company
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
// ZAP: 2012/03/15 Changed the RecordHistory constructor to receive a byte[]
//      instead of String in the parameters reqBody and resBody.


package org.parosproxy.paros.db;

import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RecordHistory {
    
    private int historyId = 0;
    private long sessionId = 0;
	private int historyType = HistoryReference.TYPE_MANUAL;
	private HttpMessage httpMessage = null;
	
	public RecordHistory() {
	    httpMessage = new HttpMessage();	
		
	}

	// ZAP: Added note to RecordHistory constructor
	public RecordHistory(int historyId, int historyType, long sessionId, long timeSentMillis, int timeElapsedMillis, String reqHeader, byte[] reqBody, String resHeader, byte[] resBody, String tag, String note) throws HttpMalformedHeaderException {
		setHistoryId(historyId);
		setHistoryType(historyType);
        setSessionId(sessionId);
		httpMessage = new HttpMessage(reqHeader, reqBody, resHeader, resBody);
		httpMessage.setTimeSentMillis(timeSentMillis);
		httpMessage.setTimeElapsedMillis(timeElapsedMillis);
        // ZAP: Support for multiple tags
        //httpMessage.setTag(tag);
		if (tag != null && tag.length() > 0) {
			// TODO decide how to handle these
			//httpMessage.addTag(tag);
		}
        httpMessage.setNote(note);
	}
	
	/**
	 * @return Returns the id.
	 */
	public int getHistoryId() {
		return historyId;
	}
    /**
     * @return Returns the historyType.
     */
    public int getHistoryType() {
        return historyType;
    }
	
	public HttpMessage getHttpMessage() {
		return httpMessage;
	}
	/**
	 * @param historyId The id to set.
	 */
	public void setHistoryId(int historyId) {
		this.historyId = historyId;
	}
    /**
     * @param historyType The historyType to set.
     */
    public void setHistoryType(int historyType) {
        this.historyType = historyType;
    }

    /**
     * @return Returns the sessionId.
     */
    public long getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId The sessionId to set.
     */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

}
