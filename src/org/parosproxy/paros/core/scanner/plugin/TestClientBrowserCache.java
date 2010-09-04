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
package org.parosproxy.paros.core.scanner.plugin;

import java.util.regex.Pattern;

import org.parosproxy.paros.core.scanner.AbstractAppPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;



/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestClientBrowserCache extends AbstractAppPlugin {

    
    public final static Pattern patternNoCache	= Pattern.compile("\\QNo-cache\\E|\\QNo-store\\E", PATTERN_PARAM);

	// <meta http-equiv="Pragma" content="no-cache">
	// <meta http-equiv="Cache-Control" content="no-cache">
	public final static Pattern patternHtmlNoCache = Pattern.compile("<META[^>]+(Pragma|\\QCache-Control\\E)[^>]+(\\QNo-cache\\E|\\QNo-store\\E)[^>]*>", PATTERN_PARAM);

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getId()
     */
    public int getId() {
        return 10002;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getName()
     */
    public String getName() {
        return "Secure page browser cache";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getDependency()
     */
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getDescription()
     */
    public String getDescription() {
        return "Secure page can be cached in browser.  Cache control is not set in HTTP header nor HTML header.  Sensitive content can be recovered from browser storage.";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getCategory()
     */
    public int getCategory() {
        return Category.BROWSER;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getSolution()
     */
    public String getSolution() {
        String msg = "The best way is to set HTTP header with: 'Pragma: No-cache' and 'Cache-control: No-cache'." + CRLF
            + "Alternatively, this can be set in the HTML header by:" + CRLF
            + "<META HTTP-EQUIV='Pragma' CONTENT='no-cache'>" + CRLF
            + "<META HTTP-EQUIV='Cache-Control' CONTENT='no-cache'>" + CRLF
            + "but some browsers may have problem using this method.";
        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getReference()
     */
    public String getReference() {
        String msg = ". How to prevent caching in Internet Explorer - http://support.microsoft.com/default.aspx?kbid=234067" + CRLF
            + ". Pragma: No-cache Tag May Not Prevent Page from Being Cached - http://support.microsoft.com/default.aspx?kbid=222064";
        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.AbstractPlugin#init()
     */
    public void init() {

    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#scan()
     */
    public void scan() {

        HttpMessage msg = getBaseMsg();
		boolean result = false;
		
		if (!msg.getRequestHeader().getSecure()) {
		    // no need to if non-secure page;
		    return;
		} else if (msg.getRequestHeader().isImage()) {
		    // does not bother if image is cached
		    return;
		} else if (msg.getResponseBody().length() == 0) {
		    return;
		}
		
		if (!matchHeaderPattern(msg, HttpHeader.CACHE_CONTROL, patternNoCache)
		        && !matchHeaderPattern(msg, HttpHeader.PRAGMA, patternNoCache)
		        && !matchBodyPattern(msg, patternHtmlNoCache, null)) {
		    
		    result = true;
		}
		
		if (result) {
		    bingo(Alert.RISK_MEDIUM, Alert.WARNING, null, null, "", msg);
		}

    }

}
