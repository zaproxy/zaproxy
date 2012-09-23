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
// ZAP: 2012/01/02 Separate param and attack
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/08/01 Removed the "(non-Javadoc)" comments.
package org.parosproxy.paros.core.scanner.plugin;

import java.util.regex.Pattern;

import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;


public class TestServerSideInclude extends AbstractAppParamPlugin {


    private static final String SSI_UNIX = "<!--#EXEC cmd=\"ls /\"-->";
    private static final String SSI_UNIX2 = "\">" +SSI_UNIX + "<";
    private static final String SSI_WIN = "<!--#EXEC cmd=\"dir \\\"-->";
    private static final String SSI_WIN2 = "\">" +SSI_WIN + "<";

	
	private static Pattern patternSSIUnix = Pattern.compile("\\broot\\b.*\\busr\\b", PATTERN_PARAM);
	private static Pattern patternSSIWin = Pattern.compile("\\bprogram files\\b.*\\b(WINDOWS|WINNT)\\b", PATTERN_PARAM);

    @Override
    public int getId() {
        return 40009;
    }

    @Override
    public String getName() {
        return "Server side include";
    }


    @Override
    public String[] getDependency() {
        return null;
    }

    @Override
    public String getDescription() {
        String msg = "Certain parameters may cause Server Side Include commands to be executed.  This may allow database connection or arbitrary code to be executed.";
        return msg;
    }

    @Override
    public int getCategory() {
        return Category.INJECTION;
    }

    @Override
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

    @Override
    public String getReference() {
        String msg = "http://www.carleton.ca/~dmcfet/html/ssi.html";
        return msg;
    }

    @Override
    public void init() {

    }
    


    @Override
    public void scan(HttpMessage msg, String param, String value) {
        
		//String result = null;

		try {
			setParameter(msg, param, SSI_UNIX);
            sendAndReceive(msg);
    		//result = msg.getResponseBody().toString();
    		if (matchBodyPattern(msg, patternSSIUnix, null)) {    		    
    			bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, SSI_UNIX, null, msg);
    			return;
    		}

        } catch (Exception e) {
        }	

		try {
		    msg = getNewMsg();
			setParameter(msg, param, SSI_UNIX2);
            sendAndReceive(msg);
    		//result = msg.getResponseBody().toString();
    		if (matchBodyPattern(msg, patternSSIUnix, null)) {    		    
    			bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, SSI_UNIX2, null, msg);
    			return;
    		}

        } catch (Exception e) {
        }	


		try {
		    msg = getNewMsg();
			setParameter(msg, param, SSI_WIN);
            sendAndReceive(msg);
    		//result = msg.getResponseBody().toString();
    		if (matchBodyPattern(msg, patternSSIWin, null)) {    		    
    			bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, SSI_WIN, null, msg);
    			return;
    		}

        } catch (Exception e) {
        }	

		try {
		    msg = getNewMsg();
			setParameter(msg, param, SSI_WIN2);
            sendAndReceive(msg);
    		//result = msg.getResponseBody().toString();
    		if (matchBodyPattern(msg, patternSSIWin, null)) {    		    
    			bingo(Alert.RISK_HIGH, Alert.WARNING, null, param, SSI_WIN2, null, msg);
    			return;
    		}

        } catch (Exception e) {
        }	

	}

	@Override
	public int getRisk() {
		return Alert.RISK_HIGH;
	}
       
}
