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

package org.parosproxy.paros.core.spider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ParserAttr {

    Pattern pattern = null;

	ParserAttr(String attr) {
		// get attribute within:
		//	1.	double quote if exist
		//	2.	bounded by spaces if double quote not exist
//		String pattern = "(?i)\\s*?" + attr.toUpperCase() + "\\s*?=\\s*(\"*)([^\"]*)\\1(?:\\Z|\\s+)";
		String attrPattern = "\\s*?" + attr.toUpperCase() + "\\s*?=\\s*([\"']{0,1})([^\"']*?)\\1(\\Z|\\s+)";
		
		pattern = Pattern.compile(attrPattern, Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
	}

	String getValue(String doc) {

		String result = "";
		Matcher matcher = pattern.matcher(doc);
		if (matcher.find()) {
			result = matcher.group(2);
		}
		return result.trim();
	}
}