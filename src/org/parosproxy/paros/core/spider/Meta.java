/*
*
* Paros and its related class files.
* 
* Paros is an HTTP/HTTPS proxy for assessing web application security.
* Copyright (C) 2005 Chinotec Technologies Company
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

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Meta extends org.parosproxy.paros.core.spider.Tag {

	// parser for "Meta" tag
	private static final ParserTag	parser	= new ParserTag("META", ParserTag.CLOSING_TAG_OPTIONAL);

	// parser for "CONTENT" attribute in tag
	private static final ParserAttr	parserAttrContent = new ParserAttr(Attr.CONTENT);

	private static final Pattern urlPattern = Pattern.compile("url\\s*=\\s*([^;]+)", Pattern.CASE_INSENSITIVE);
	private String	url = "";

	/**
	 *	Get an array of "A" tags.
	 *	@param	doc
	 *			The html document to be parsed.
	 *	@return	array of "A"
	 */
	public static Meta[] getMetas(String doc) {

		Vector metas = new Vector();
		parser.parse(doc);
		while (parser.nextTag()) {
			String content	= parser.getContent();
			String attrs	= parser.getAttrs();
			Meta m = new Meta();
			m.buildAttrs(attrs);
			m.build(content);
			metas.addElement(m);
		}

		Meta[]	result	= new Meta[metas.size()];
		result = (Meta[]) metas.toArray(result);
		return result;
	}


	protected void buildAttrs(String attrs) {
	    super.buildAttrs(attrs);
	    
	    try {
	        String s = parserAttrContent.getValue(attrs);
	        Matcher matcher = urlPattern.matcher(s);
	        if (matcher.find()) {
	            url = matcher.group(1);
	        }
	    } catch (Exception e) {}
	}
    /**
     * @return Returns the href.
     */
    public String getURL() {
        return url;
    }
}