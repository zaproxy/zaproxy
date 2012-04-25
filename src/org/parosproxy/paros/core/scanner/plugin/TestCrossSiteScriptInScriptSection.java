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
// ZAP: 2011/08/02 No longer switched on -sp flag and fixed regex
// ZAP: 2011/08/17 Check with double quotes as well as single 
// ZAP: 2011/11/30 Depreciated
// ZAP: 2012/01/02 Separate param and attack
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.

package org.parosproxy.paros.core.scanner.plugin;

import java.util.regex.Pattern;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;



/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestCrossSiteScriptInScriptSection extends AbstractAppParamPlugin {

	// ZAP Depreciated by org.zaproxy.zap.scanner.plugin.TestCrossSiteScriptV2
	@Override
	public boolean isDepreciated() {
		return true;
	}

    private static final String XSS_SCRIPT_1 = "alert('" + Constant.getEyeCatcher() + "');";
    private static final String XSS_SCRIPT_2 = "alert(\"" + Constant.getEyeCatcher() + "\");";
	
	private static final Pattern PATTERN_XSS_SCRIPT_1
	     = Pattern.compile("<SCRIPT.*alert\\('" + Constant.getEyeCatcher() + "'\\);.*</SCRIPT>", 
	    		Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_XSS_SCRIPT_2
    	= Pattern.compile("<SCRIPT.*alert\\(\"" + Constant.getEyeCatcher() + "\"\\);.*</SCRIPT>", 
    			Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
	
    @Override
    public int getId() {
        return 40001;
    }

    @Override
    public String getName() {
        return "Cross site scripting in SCRIPT section";
    }


    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDependency()
     */
    @Override
    public String[] getDependency() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getDescription()
     */
    @Override
    public String getDescription() {
        String msg = "Cross-site scripting or HTML injection is possible within javascript <SCRIPT> and </SCRIPT> section.\r\n"
            + "Malicious javacript can be injected into the browser even if the server filtered certain specail characters such as double quotes and <>.\r\n"
            + "It will appear to be genuine content from the original site.  "
            + "These scripts can be used to execute arbitrary code or steal customer sensitive information such as user password or cookies.\r\n"
            + "Very often this is in the form of a hyperlink with the injected script embeded in the query strings.  However, XSS is possible via FORM POST data, cookies, "
            + "user data sent from another user or shared data retrieved from database.\r\n"
            + "Currently this check does not verify XSS from cookie or database.  They should be checked manually if the application retrieve database records from another user's input.";
	
        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getCategory()
     */
    @Override
    public int getCategory() {
        return Category.INJECTION;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getSolution()
     */
    @Override
    public String getSolution() {
        String msg = "You should check manually if this is exactly cross-site script in this case."
            + "Do not embed dynamic content within <SCRIPT></SCRIPT> sections.  "
            + "Do not trust client side input even if there is client side validation.  Sanitize potentially danger characters in the server side.  Very often filtering the <, >, \" characters prevented injected script to be executed in most cases.  "
            + "However, sometimes other danger meta-characters such as ' , (, ), /, &, ; etc are also needed.\r\n"
            + "In addition (or if these characters are needed), HTML encode meta-characters in the response.  For example, encode < as &lt;\r\n";
        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getReference()
     */
    @Override
    public String getReference() {
        String msg = "<ul><li>The OWASP guide at http://www.owasp.org/documentation/guide</li>"
            + "<li>http://www.technicalinfo.net/papers/CSS.html</li>"
            + "<li>http://www.cgisecurity.org/articles/xss-faq.shtml</li>"
            + "<li>http://www.cert.org/tech_tips/malicious_code_FAQ.html</li>"
            + "<li>http://sandsprite.com/Sleuth/papers/RealWorld_XSS_1.html</li></ul>";

        return msg;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.AbstractTest#init()
     */
    @Override
    public void init() {

    }
    


    @Override
    public void scan(HttpMessage msg, String param, String value) {
		
		setParameter(msg, param, XSS_SCRIPT_1);

		try {
            sendAndReceive(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (matchBodyPattern(msg, PATTERN_XSS_SCRIPT_1, null)) {
        	// ZAP: Changed XSS level to HIGH
			bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, param, XSS_SCRIPT_1, null, msg);
			return;
		}
		
		setParameter(msg, param, XSS_SCRIPT_2);

		try {
            sendAndReceive(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (matchBodyPattern(msg, PATTERN_XSS_SCRIPT_2, null)) {
        	// ZAP: Changed XSS level to HIGH
			bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, param, XSS_SCRIPT_2, null, msg);
			return;
		}

	}
}
