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
// ZAP: 2011/04/16 i18n
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/05/02 Removed the method checkAutocomplete(HttpMessage) and a call
// to it. Now there is a passive scanner that checks for the AUTOCOMPLETE:
// org.zaproxy.zap.extension.pscan.scanner.PasswordAutocompleteScanner.
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order

package org.parosproxy.paros.extension.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;



public class FilterDetectMalciousContent extends FilterAdaptor {

    @Override
    public int getId() {
        return 90;
    }

    @Override
    public String getName() {
        return Constant.messages.getString("filter.malicious.name");        
    }

    @Override
    public void onHttpRequestSend(HttpMessage httpMessage) {

    }

    @Override
    public void onHttpResponseReceive(HttpMessage msg) {

	    if (msg.getResponseHeader().isImage()) {
	        checkImage(msg);
	    }

	    if (msg.getResponseHeader().isText()) {
	        checkText(msg);
	    }

    }
  
    private void checkImage(HttpMessage msg) {
        
    }
    
	private void checkText(HttpMessage msg) {

		// ZAP: Removed the method call checkAutocomplete(HttpMessage).
		// Now there is a passive scanner that checks for the AUTOCOMPLETE:
		// org.zaproxy.zap.extension.pscan.scanner.PasswordAutocompleteScanner.

		try {
			checkMaliciousCode(msg);
		} catch (Exception e) {
		}
	    
	}

	private void writeOutput(String hostPath, String msg, String match) {
	    getView().getOutputPanel().append("+ " + hostPath + "\r\n");
	    getView().getOutputPanel().append("- " + msg + "\r\n");
	    
		if (match != null) {
			getView().getOutputPanel().append("- " + match + "\r\n");
		}
		getView().getOutputPanel().append("\r\n");
		
	}
	
	// ZAP: Removed the method checkAutocomplete(HttpMessage).
	// Now there is a passive scanner that checks for the AUTOCOMPLETE:
	// org.zaproxy.zap.extension.pscan.scanner.PasswordAutocompleteScanner.

	private static final int DEFAULT = Pattern.MULTILINE | Pattern.CASE_INSENSITIVE;

	private static final Pattern[] patternBadHeaderList = {
			Pattern.compile("Content-type:\\s+application/hta", DEFAULT),
			Pattern.compile("Content-Disposition: attachment; filename=[^{}]+\\{[\\w\\d\\-]*\\}[^{}]+", DEFAULT),
			Pattern.compile("Location:\\s+URL:", DEFAULT)
	};

	private static final String[] patternBadHeaderDesc = {
		"Suspicious content-type header 'application/hta'",
		"MS IE Microsoft Internet Explorer CLSID File Extension Misrepresentation Vulnerability (http://www.securityfocus.com/bid/9510)",
		"Microsoft Internet Explorer URL Local Resource Access Weakness (http://www.securityfocus.com/bid/10472, http://secunia.com/advisories/11793"
	};
	
	private static final Pattern[] patternBadBodyList = {
		Pattern.compile("^.*file:javascript:eval.*$", DEFAULT),
		Pattern.compile("<[^>]*CLSID:11111111-1111-1111-1111-11111111111[^>]*>", DEFAULT),
		Pattern.compile("^.*?Scripting\\.FileSystemObject.*?$", DEFAULT),
		Pattern.compile("^.*?new\\s+ActiveXObject.*?$", DEFAULT),
		Pattern.compile("<OBJECT\\s+[^>]+>", DEFAULT),
		Pattern.compile("https?://[^\\s\"']+?@[^\\s\"']+?", DEFAULT),
		Pattern.compile("^.*?Microsoft\\.XMLHTTP.*?$", DEFAULT),		
		Pattern.compile("^.*?SaveToFile.*?$", DEFAULT),
		Pattern.compile("^.*?CreateObject\\(\\s*[\"']+Adodb.Stream[\"']\\s*\\)$", DEFAULT),
		Pattern.compile("^.*?execcommand.*?$", DEFAULT),
		Pattern.compile("(ms-its|ms-itss|mk:@MSITStore):mhtml:file://", DEFAULT),
		Pattern.compile("ms-its|ms-itss", DEFAULT),		// simplified one of the above
		Pattern.compile("<iframe[^>]+src=['\"]*shell:[^>]+>", DEFAULT),
		Pattern.compile("showModalDialog\\([^)]*\\).location\\s*?=\\s*?[\"']javascript:[\"']<SCRIPT", DEFAULT),
		Pattern.compile("^.*?execScript.*?$", DEFAULT)
	};

	private static final String[] patternBadBodyDesc = {
			"Suspcious use of javascript 'file:javascript:eval'.",
			"Suspicious ActiveX CLSID 11111111-1111-1111-1111-... being used.",
			"Attempt to access Scripting.FileSystemObject.",
			"Inline creation of ActiveX object.",
			"ActiveX object used.",
			"URL with '@' to obscure hyperlink.",
			"Suspicious use of ActiveX XMLHTTP object (http://www.securityfocus.com/bid/8577)",
			"Suspicious scripting attempt to access local file via SafeToFile.  MS IE Self Executing HTML Arbitrary Code Execution Vulnerability.  (http://www.securityfocus.com/bid/8984)",
			"MS IE ADODB.Stream Object File Installation Weakness.  (http://www.securityfocus.com/bid/10514)",
			"MS IE ExecCommand Cross-Domain Access Violation Vulnerability (http://www.securityfocus.com/bid/9015)",
			"MS IE MT-ITS Protocol Zone Bypass Vulnerability (http://www.securityfocus.com/bid/9658)",
			"MS IE MT-ITS Protocol Zone Bypass Vulnerability (http://www.securityfocus.com/bid/9658)",
			"MS IE Shell: IFrame Cross-Zone Scripting Vulnerability (http://www.securityfocus.com/bid/9628)",
			"Microsoft Internet Explorer Modal Dialog Zone Bypass Vulnerability (http://www.securityfocus.com/bid/10473)",
			"Suspicious use of IE ActiveX Control Cross-Site Scripting (http://secunia.com/advisories/13482/)",
	};
	
	private void checkMaliciousCode(HttpMessage msg) {
	    
	    Pattern bad = null;
		Matcher matcher = null;
		String txtHeader = msg.getRequestHeader().toString();
		String txtBody = msg.getResponseBody().toString();

		// check malicious header
		
		for (int i=0; i<patternBadHeaderList.length; i++){
			bad = patternBadHeaderList[i];
			matcher = bad.matcher(txtHeader);
			while (matcher.find()) {
				writeOutput(msg.getRequestHeader().getURI().toString(), patternBadHeaderDesc[i], matcher.group(0));
			}
		}
		
		// check malicous body 
		for (int i=0; i<patternBadBodyList.length; i++){
			bad = patternBadBodyList[i];
			matcher = bad.matcher(txtBody);
			while (matcher.find()) {
				writeOutput(msg.getRequestHeader().getURI().toString(), patternBadBodyDesc[i], matcher.group(0));				
			}
		}
	}

    

}
