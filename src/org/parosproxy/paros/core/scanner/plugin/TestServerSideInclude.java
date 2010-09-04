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

import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;



/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestServerSideInclude extends AbstractAppParamPlugin {

//  private static final String SSI_UNIX = "<!--#EXEC%20cmd=\"ls%20/\"-->";
//	private static final String SSI_UNIX2 = "\">" +SSI_UNIX + "<";
//	private static final String SSI_WIN = "<!--#EXEC%20cmd=\"dir%20\\\"-->";
//	private static final String SSI_WIN2 = "\">" +SSI_WIN + "<";

    private static final String SSI_UNIX = "<!--#EXEC cmd=\"ls /\"-->";
    private static final String SSI_UNIX2 = "\">" +SSI_UNIX + "<";
    private static final String SSI_WIN = "<!--#EXEC cmd=\"dir \\\"-->";
    private static final String SSI_WIN2 = "\">" +SSI_WIN + "<";

	
	private static Pattern patternSSIUnix = Pattern.compile("\\broot\\b.*\\busr\\b", PATTERN_PARAM);
	private static Pattern patternSSIWin = Pattern.compile("\\bprogram files\\b.*\\b(WINDOWS|WINNT)\\b", PATTERN_PARAM);

    public int getId() {
        return 40002;
    }

    public String getName() {
        return "Server side include";
    }


    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDependency()
     */
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDescription()
     */
    public String getDescription() {
        String msg = "Certain parameters may cause Server Side Include commands to be executed.  This may allow database connection or arbitrary code to be executed.";
        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getCategory()
     */
    public int getCategory() {
        return Category.INJECTION;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getSolution()
     */
    public String getSolution() {
        String msg = "Do not trust client side input and enforece tight check in the server side.  Disable server side include." + CRLF
            + ". Refer to manual to disable Sever Side Include." + CRLF
            + ". Use least privilege to run your web server or application server." + CRLF
            + "For Apache, disable the following:" + CRLF
            + "Options Indexes FollowSymLinks Includes" + CRLF
            + "AddType application/x-httpd-cgi .cgi" + CRLF
            + "AddType text/x-server-parsed-html .html" + CRLF;
        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getReference()
     */
    public String getReference() {
        String msg = "http://www.carleton.ca/~dmcfet/html/ssi.html";
        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.AbstractTest#init()
     */
    public void init() {

    }
    


    public void scan(HttpMessage msg, String param, String value) {
        
		String result = null;

		try {
			setParameter(msg, param, SSI_UNIX);
            sendAndReceive(msg);
    		result = msg.getResponseBody().toString();
    		if (matchBodyPattern(msg, patternSSIUnix, null)) {    		    
    			bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, null, msg);
    			return;
    		}

        } catch (Exception e) {
        }	

		try {
		    msg = getNewMsg();
			setParameter(msg, param, SSI_UNIX2);
            sendAndReceive(msg);
    		result = msg.getResponseBody().toString();
    		if (matchBodyPattern(msg, patternSSIUnix, null)) {    		    
    			bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, null, msg);
    			return;
    		}

        } catch (Exception e) {
        }	


		try {
		    msg = getNewMsg();
			setParameter(msg, param, SSI_WIN);
            sendAndReceive(msg);
    		result = msg.getResponseBody().toString();
    		if (matchBodyPattern(msg, patternSSIWin, null)) {    		    
    			bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, null, msg);
    			return;
    		}

        } catch (Exception e) {
        }	

		try {
		    msg = getNewMsg();
			setParameter(msg, param, SSI_WIN2);
            sendAndReceive(msg);
    		result = msg.getResponseBody().toString();
    		if (matchBodyPattern(msg, patternSSIWin, null)) {    		    
    			bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, null, msg);
    			return;
    		}

        } catch (Exception e) {
        }	

	}

        
}
