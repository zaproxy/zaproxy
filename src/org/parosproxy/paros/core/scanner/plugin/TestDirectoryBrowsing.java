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

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.core.scanner.AbstractAppPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;



/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestDirectoryBrowsing extends AbstractAppPlugin {


	private final static Pattern patternIIS			= Pattern.compile("Parent Directory", PATTERN_PARAM);
	private final static Pattern patternApache		= Pattern.compile("\\bDirectory Listing\\b.*(Tomcat|Apache)", PATTERN_PARAM);
	
	// general match for directory
	private final static Pattern patternGeneralDir1		= Pattern.compile("\\bDirectory\\b", PATTERN_PARAM);
	private final static Pattern patternGeneralDir2		= Pattern.compile("[\\s<]+IMG\\s*=", PATTERN_PARAM);
	private final static Pattern patternGeneralParent	= Pattern.compile("Parent directory", PATTERN_PARAM);


    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getId()
     */
    public int getId() {
        return 00001;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getName()
     */
    public String getName() {
        
        return "Directory browsing";
    }
    


    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDependency()
     */
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getSummary()
     */
    public String getDescription() {
        return "It is possible to view the directory listing.  Directory listing may reveal hidden scripts, include files , backup source files etc which be accessed to read sensitive information.";
    }
    
    public int getCategory() {
        return Category.SERVER;
    }

    public String getSolution() {
        return "Disable directory browsing.  If this is required, make sure the listed files does not induce risks.";
    }
    
    public String getReference() {
        String ref = "For IIS, turn off directory browsing.\r\n"
            + "For Apache, use the 'Options -Indexes' directive to disable indexes in directory or via .htaccess:\r\n"
            + ". http://httpd.apache.org/docs/mod/core.html#options\r\n"
            + ". http://alamo.satlug.org/pipermail/satlug/2002-February/000053.html\r\n"
            + ". or create a default index.html for each directory.";
        return ref;
    }
    
    public void init() {
    }
    

	private void checkIfDirectory(HttpMessage msg) throws URIException {

	    URI uri = msg.getRequestHeader().getURI();
	    uri.setQuery(null);
	    String sUri = uri.toString();
		if (!sUri.endsWith("/")) {
			sUri = sUri + "/";
		}
		msg.getRequestHeader().setURI(new URI(sUri, true));
	
	}

	public void scan() {
	    
	    boolean result = false;
	    HttpMessage msg = getNewMsg();
	    int reliability = Alert.WARNING;
	    
	    try {
            checkIfDirectory(msg);
            writeProgress(msg.getRequestHeader().getURI().toString());
    		sendAndReceive(msg);

    		if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
    			return;
    		}
    		
    		if (matchBodyPattern(msg, patternIIS, null)) {
    			result = true;
    		} else if (matchBodyPattern(msg, patternApache, null)) {
    			result = true;
    		} else if (matchBodyPattern(msg, patternGeneralParent, null)) {
    			result = true;
    			reliability = Alert.SUSPICIOUS;
    		} else if (matchBodyPattern(msg, patternGeneralDir1, null)) {
    			if (matchBodyPattern(msg, patternGeneralDir2, null)) {
    				result = true;
    				reliability = Alert.SUSPICIOUS;
    			}
    		}


        } catch (IOException e) {
        }
		
		if (result) {
            bingo(Alert.RISK_MEDIUM, reliability, msg.getRequestHeader().getURI().toString(), "", "", msg);
		}
	}
    
}
