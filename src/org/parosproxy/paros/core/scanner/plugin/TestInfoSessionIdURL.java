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

import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.core.scanner.AbstractAppPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestInfoSessionIdURL extends AbstractAppPlugin {

	/*
	private static Pattern staticSessionCookieNamePHP = Pattern("PHPSESSID", PATTERN.PARAM);
	private 
	
	ASP = ASPSESSIONIDxxxxx=xxxxxx
	PHP = PHPSESSID
	Cole fusion = CFID, CFTOKEN	(firmed, checked with Macromedia)
	Java (tomcat, jrun, websphere, sunone, weblogic )= JSESSIONID=xxxxx	
	
	*/
	
	private static Pattern staticSessionIDPHP1 = Pattern.compile("(PHPSESSION)=\\w+", PATTERN_PARAM);
	private static Pattern staticSessionIDPHP2 = Pattern.compile("(PHPSESSID)=\\w+", PATTERN_PARAM);
	private static Pattern staticSessionIDJava = Pattern.compile("(JSESSIONID)=\\w+", PATTERN_PARAM);
	private static Pattern staticSessionIDASP = Pattern.compile("(ASPSESSIONID)=\\w+", PATTERN_PARAM);
	private static Pattern staticSessionIDColdFusion = Pattern.compile("(CFTOKEN)=\\w+", PATTERN_PARAM);
	private static Pattern staticSessionIDJW = Pattern.compile("(JWSESSIONID)=\\w+", PATTERN_PARAM);
	private static Pattern staticSessionIDWebLogic = Pattern.compile("(WebLogicSession)=\\w+", PATTERN_PARAM);
	private static Pattern staticSessionIDApache = Pattern.compile("(SESSIONID)=\\w+", PATTERN_PARAM);
	
	private static Pattern[] staticSessionIDList =
		{staticSessionIDPHP1, staticSessionIDPHP2, staticSessionIDJava, staticSessionIDColdFusion,
			staticSessionIDASP, staticSessionIDJW, staticSessionIDWebLogic, staticSessionIDApache};

    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getId()
     */
    public int getId() {
        return 00004;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getName()
     */
    public String getName() {
        return "Session ID in URL rewrite";
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
        return "URL rewrite is used to track user session ID.  The session ID may be disclosed in referer header.  Besides, the session ID can be stored in browser history or server logs.";
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
        return "For secure content, put session ID in cookie.  To be even more secure consider to use a combination of cookie and URL rewrite.";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getReference()
     */
    public String getReference() {
        return "http://seclists.org/lists/webappsec/2002/Oct-Dec/0111.html";
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
        HttpMessage base = getBaseMsg();
        
        String uri = base.getRequestHeader().getURI().toString();
        Matcher matcher = null;
		String sessionIdValue = null;
		String sessionIdName = null;
		for (int i=0; i<staticSessionIDList.length; i++) {
			matcher = staticSessionIDList[i].matcher(uri);
			if (matcher.find()) {
				sessionIdValue = matcher.group(0);
				sessionIdName = matcher.group(1);
				String kb = getKb().getString("sessionId/nameValue");

				if (kb == null || !kb.equals(sessionIdValue)) {
				    getKb().add("sessionId/nameValue", sessionIdValue);
					bingo(Alert.RISK_LOW, Alert.WARNING, null, "", sessionIdValue, base);
				}
				kb = getKb().getString("sessionId/name");
				getKb().add("sessionId/name", sessionIdName);
				try {
                    checkSessionIDExposure(base);
                } catch (URIException e) {
                }
				break;
			}
		}
		
	}
    
	private static final String paramHostHttp = "http://([\\w\\.\\-_]+)";
	private static final String paramHostHttps = "https://([\\w\\.\\-_]+)";
	private static final Pattern[] staticLinkCheck = {
		Pattern.compile("src\\s*=\\s*\"?" + paramHostHttp, PATTERN_PARAM),
		Pattern.compile("href\\s*=\\s*\"?" + paramHostHttp, PATTERN_PARAM),
		Pattern.compile("src\\s*=\\s*\"?" + paramHostHttps, PATTERN_PARAM),
		Pattern.compile("href\\s*=\\s*\"?" + paramHostHttps, PATTERN_PARAM),
		
	};

	private static final String alertReferer = "Referer expose session ID";
	private static final String descReferer = "Hyperlink to other host name is found.  As session ID URL rewrite is used, it may be disclosed in referer header to external host.";
	private static final String solutionReferer = "This is a risk if the session ID is sensitive and the hyperlink refer to an external host.  For secure content, put session ID in secured session cookie.";

	private void checkSessionIDExposure(HttpMessage msg) throws URIException {

		String body = msg.getResponseBody().toString();
		int risk = (msg.getRequestHeader().getSecure()) ? Alert.RISK_MEDIUM : Alert.RISK_INFO;
		String linkHostName = null;
		Matcher matcher = null;
		
		for (int i=0; i<staticLinkCheck.length; i++) {
			matcher = staticLinkCheck[i].matcher(body);
		
			while (matcher.find()) {
				linkHostName = matcher.group(1);
				String host = msg.getRequestHeader().getURI().getHost();
				if (host.compareToIgnoreCase(linkHostName) != 0) {
					bingo(risk, Alert.WARNING, alertReferer, descReferer, null, null, linkHostName, solutionReferer, msg);
				}
			}
		}
	}


}
