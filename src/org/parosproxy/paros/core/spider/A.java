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

import java.util.Vector;

public class A extends org.parosproxy.paros.core.spider.Tag {

	// parser for "A" tag
	private static final ParserTag	parser	= new ParserTag("A", ParserTag.CLOSING_TAG_YES);

	// parser for "href" attribute in tag
	private static final ParserAttr	parserAttrHref = new ParserAttr(Attr.HREF);

	private String	href	= "";

	/**
	 *	Get an array of "A" tags.
	 *	@param	doc
	 *			The html document to be parsed.
	 *	@return	array of "A"
	 */
	public static A[] getAs(String doc) {

		Vector as = new Vector();
		parser.parse(doc);
		while (parser.nextTag()) {
			String content	= parser.getContent();
			String attrs	= parser.getAttrs();
			A a = new A();
			a.buildAttrs(attrs);
			a.build(content);
			as.addElement(a);
		}

		A[]	result	= new A[as.size()];
		result = (A[]) as.toArray(result);
		return result;
	}


	protected void buildAttrs(String attrs) {
		super.buildAttrs(attrs);

		href	= parserAttrHref.getValue(attrs);
	}
    /**
     * @return Returns the href.
     */
    public String getHref() {
        return href;
    }
}