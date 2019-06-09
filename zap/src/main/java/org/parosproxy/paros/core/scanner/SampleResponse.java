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
// ZAP: 2014/03/23 Issue 1021: OutOutOfMemoryError while running the active scanner
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative
// implementations
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
package org.parosproxy.paros.core.scanner;

import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

class SampleResponse {

    static final int ERROR_PAGE_RFC = 0;
    static final int ERROR_PAGE_NON_RFC = 1;
    static final int ERROR_PAGE_REDIRECT = 2;
    static final int ERROR_PAGE_STATIC = 3;
    static final int ERROR_PAGE_DYNAMIC_BUT_DETERMINISTIC = 4;
    static final int ERROR_PAGE_UNDETERMINISTIC = 5;

    private HistoryReference historyReference = null;
    private int errorPageType = ERROR_PAGE_RFC;

    SampleResponse(HttpMessage message, int errorPageType)
            throws HttpMalformedHeaderException, DatabaseException {
        this.historyReference = createHistoryReference(message);
        this.errorPageType = errorPageType;
    }
    /** @return Returns the message. */
    public HttpMessage getMessage() throws HttpMalformedHeaderException, DatabaseException {
        return historyReference.getHttpMessage();
    }
    /** @param message The message to set. */
    public void setMessage(HttpMessage message)
            throws HttpMalformedHeaderException, DatabaseException {
        this.historyReference = createHistoryReference(message);
    }
    /** @return Returns the errorPageType. */
    public int getErrorPageType() {
        return errorPageType;
    }
    /** @param errorPageType The errorPageType to set. */
    public void setErrorPageType(int errorPageType) {
        this.errorPageType = errorPageType;
    }

    private static HistoryReference createHistoryReference(HttpMessage message)
            throws HttpMalformedHeaderException, DatabaseException {
        return new HistoryReference(
                Model.getSingleton().getSession(), HistoryReference.TYPE_TEMPORARY, message);
    }
}
