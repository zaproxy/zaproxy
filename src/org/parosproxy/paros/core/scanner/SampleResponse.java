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
package org.parosproxy.paros.core.scanner;

import org.parosproxy.paros.network.HttpMessage;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
class SampleResponse {

    static final int	ERROR_PAGE_RFC							= 0;
	static final int	ERROR_PAGE_NON_RFC						= 1;
	static final int	ERROR_PAGE_REDIRECT						= 2;
	static final int	ERROR_PAGE_STATIC						= 3;
	static final int	ERROR_PAGE_DYNAMIC_BUT_DETERMINISTIC	= 4;
	static final int	ERROR_PAGE_UNDETERMINISTIC				= 5;

	private HttpMessage message = null;
	private int	errorPageType = ERROR_PAGE_RFC;
	
	SampleResponse(HttpMessage message, int errorPageType) {
	    this.message = message;
	    this.errorPageType = errorPageType;
	    
	}
    /**
     * @return Returns the message.
     */
    public HttpMessage getMessage() {
        return message;
    }
    /**
     * @param message The message to set.
     */
    public void setMessage(HttpMessage message) {
        this.message = message;
    }
    /**
     * @return Returns the errorPageType.
     */
    public int getErrorPageType() {
        return errorPageType;
    }
    /**
     * @param errorPageType The errorPageType to set.
     */
    public void setErrorPageType(int errorPageType) {
        this.errorPageType = errorPageType;
    }
}
