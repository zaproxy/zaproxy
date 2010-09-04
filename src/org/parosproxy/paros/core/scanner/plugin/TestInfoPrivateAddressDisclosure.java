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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.parosproxy.paros.core.scanner.AbstractAppPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestInfoPrivateAddressDisclosure extends AbstractAppPlugin {

    
	// check for private IP list
	public static final Pattern patternPrivateIP = Pattern.compile("(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|172\\.\\d{2,2}\\.\\d{1,3}\\.\\d{1,3}|192\\.168\\.\\d{1,3}\\.\\d{1,3})", PATTERN_PARAM);

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getId()
     */
    public int getId() {
        return 00003;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getName()
     */
    public String getName() {
        return "Private IP disclosure";
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
        return "Private IP such as 10.x.x.x, 172.x.x.x, 192.168.x.x is found in the HTTP response body.  This can be used in exploits on internal system.";        
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getCategory()
     */
    public int getCategory() {
        return Category.INFO_GATHER;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getSolution()
     */
    public String getSolution() {
        return "Remove the private IP address from the HTTP response body.  For comments, use jsp/asp comment instead of HTML/javascript comment which can be seen by client browsers.";
        
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getReference()
     */
    public String getReference() {
        return null;
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
		String txtBody = msg.getResponseBody().toString();
		String txtFound = null;
		Matcher matcher = patternPrivateIP.matcher(txtBody);
		while (matcher.find()) {
			txtFound = matcher.group();
			if (txtFound != null) {
				bingo(Alert.RISK_LOW, Alert.WARNING, null, null, txtFound, msg);
			}
		}	
    }

}
