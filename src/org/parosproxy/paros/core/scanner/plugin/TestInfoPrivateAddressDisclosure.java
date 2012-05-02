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
// ZAP: 2012/03/15 Changed the method scan to use the class StringBuilder 
// instead of String.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.

package org.parosproxy.paros.core.scanner.plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.parosproxy.paros.core.scanner.AbstractAppPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;

public class TestInfoPrivateAddressDisclosure extends AbstractAppPlugin {

	private static final String REGULAR_IP_OCTET = "\\b(25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})";
	private static final String REGULAR_PORTS = "\\b(6553[0-5]|65[0-5][0-2][0-9]|6[0-4][0-9]{4}|[0-5]?[0-9]{0,4})";
    
	// Private IP's including localhost
	public static final Pattern patternPrivateIP = Pattern.compile(
			"(10\\.(" + REGULAR_IP_OCTET + "\\.){2}" + REGULAR_IP_OCTET + "|" +
			"172\\." + "\\b(3[01]|2[0-9]|1[6-9])\\." + REGULAR_IP_OCTET + "\\." + REGULAR_IP_OCTET + "|" +
			"192\\.168\\." + REGULAR_IP_OCTET + "\\." + REGULAR_IP_OCTET + ")"
			+ "(\\:"+ REGULAR_PORTS +")?"
			, PATTERN_PARAM);
	
	
			/*"(10\\." +
			"\\b((([0-1]?[0-9]?|2[0-4])[0-9])|25[0-5])\\." +
			"\\b((([0-1]?[0-9]?|2[0-4])[0-9])|25[0-5])\\." +
			"\\b((([0-1]?[0-9]?|2[0-4])[0-9])|25[0-5])" +
			"|" +
			"172\\." +
			"\\b(1[6-9]|2[0-9]|3[01])\\." +
			"\\b((([0-1]?[0-9]?|2[0-4])[0-9])|25[0-5])\\." +
			"\\b((([0-1]?[0-9]?|2[0-4])[0-9])|25[0-5])" +
			"|" +
			"192\\.168\\." +
			"\\b((([0-1]?[0-9]?|2[0-4])[0-9])|25[0-5])\\." +
			"\\b((([0-1]?[0-9]?|2[0-4])[0-9])|25[0-5]))"
			, PATTERN_PARAM);
			*/
	
	//"(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|172\\.\\d{2,2}\\.\\d{1,3}\\.\\d{1,3}|192\\.168\\.\\d{1,3}\\.\\d{1,3})", PATTERN_PARAM);
			

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.parosproxy.paros.core.scanner.Plugin#getId()
	 */
    @Override
    public int getId() {
        return 00002;
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#getName()
     */
    @Override
    public String getName() {
        return "Private IP disclosure";
    }


    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#getDependency()
     */
    @Override
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#getDescription()
     */
    @Override
    public String getDescription() {
        return "A private IP such as 10.x.x.x, 172.x.x.x, 192.168.x.x has been found in the HTTP response body.  " +
        		"This information might be helpful for further attacks targeting internal systems.";        
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#getCategory()
     */
    @Override
    public int getCategory() {
        return Category.INFO_GATHER;
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#getSolution()
     */
    @Override
    public String getSolution() {
        return "Remove the private IP address from the HTTP response body.  For comments, use JSP/ASP comment instead " +
        		"of HTML/JavaScript comment which can be seen by client browsers.";
        
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#getReference()
     */
    @Override
    public String getReference() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.AbstractPlugin#init()
     */
    @Override
    public void init() {

    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#scan()
     */
    @Override
    public void scan() {
        
        HttpMessage msg = getBaseMsg();
		String txtBody = msg.getResponseBody().toString();
		Matcher matcher = patternPrivateIP.matcher(txtBody);
		StringBuilder sbTxtFound = new StringBuilder();
		
		while (matcher.find()) {
			sbTxtFound.append(matcher.group()).append("\n");
		}
		
		if (sbTxtFound.length() != 0) {
			bingo(Alert.RISK_LOW, Alert.WARNING, null, "", "", sbTxtFound.toString(), msg);
		}
		
    }

}
