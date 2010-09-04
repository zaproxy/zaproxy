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

    private static final String XSS4 = "alert('{" + Constant.getEyeCatcher() + "}');";
	
	private static final Pattern patternXSS4
	     = Pattern.compile("<SCRIPT>.*?alert\\('\\{" + Constant.getEyeCatcher() + "\\}'\\);.*?</SCRIPT>", Pattern.DOTALL);
	
    public int getId() {
        return 40005;
    }

    public String getName() {
        return "Cross site scripting in SCRIPT section";
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
    public int getCategory() {
        return Category.INJECTION;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Test#getSolution()
     */
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
    public void init() {

    }
    


    public void scan(HttpMessage msg, String param, String value) {
		
		setParameter(msg, param, XSS4);

		try {
            sendAndReceive(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }

		//	no need to have 200 response
		//	if (getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
		//	return;
		//}

        StringBuffer sb = new StringBuffer();
//        String result = msg.getResponseBody().toString();
//        System.out.println(result);
        if (matchBodyPattern(msg, patternXSS4, sb)) {
			bingo(Alert.RISK_MEDIUM, Alert.SUSPICIOUS, null, param + "=" + XSS4, null, msg);
			return;
		}
		

	}
	
    public boolean isVisible() {
        return Constant.isSP();
    }
}
