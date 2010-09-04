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

class ParserTag {

	public static final int	CLOSING_TAG_YES			= 0,
							CLOSING_TAG_NO			= 1,
							CLOSING_TAG_OPTIONAL	= 2;

	private Pattern pattern = null;
	private Matcher matcher = null;
	private String	doc		= null,
			attrs	= null,
			content = null;
	private int closingTagType = CLOSING_TAG_YES;

	ParserTag(String tag, int closingTag) {
		String uTag = tag.toUpperCase();
		String tagPattern = null;
		this.closingTagType = closingTag;
		switch (closingTag) {
        
   		    case CLOSING_TAG_NO:
				tagPattern = "<" + uTag + "\\s+?([^>]+?)\\s*?>";
				break;
			case CLOSING_TAG_OPTIONAL:
				tagPattern = "<" + uTag + "\\s+?([^>]+?)\\s*?>([^<]*?)<";
				break;
			case CLOSING_TAG_YES:
			default:
				tagPattern = "<" + uTag + "\\s+?([^>]+?)\\s*?>(.*?)</" + uTag + "\\s*?>";
				break;

		}
		pattern = Pattern.compile(tagPattern, Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
	}

	void parse(String doc) {
		this.doc = doc;
		matcher = pattern.matcher(doc);
	}

	/**
	 *	Get next tag if exist.
	 *	@return	true - next tag exist
	 */
	boolean nextTag() {
		boolean isFound = false;
		attrs = "";
		content = "";
		if (matcher.find()) {
			attrs = matcher.group(1);
			try {
			    if (closingTagType == CLOSING_TAG_OPTIONAL || closingTagType == CLOSING_TAG_YES) {
			        content = matcher.group(2);
			    }
			} catch (Exception e) {
			    // CLOSE TAG NO - may not have group 2
			}
			isFound = true;
		}
		return isFound;
	}

	String getAttrs() {
		return attrs;
	}

	String getContent() {
		return content;
	}

}
