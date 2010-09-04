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

public class Frame extends org.parosproxy.paros.core.spider.Tag {

	// parser for "A" tag
	private static final ParserTag	parser	= new ParserTag("FRAME", ParserTag.CLOSING_TAG_YES);

	// parser for "href" attribute in tag
	private static final ParserAttr	parserAttrSrc = new ParserAttr(Attr.SRC);

	private String	src	= "";

	/**
	 *	Get an array of "FRAME" tags.
	 *	@param	doc
	 *			The html document to be parsed.
	 *	@return	array of "A"
	 */
	public static Frame[] getFrames(String doc) {

		Vector frames = new Vector();
		parser.parse(doc);
		while (parser.nextTag()) {
			String content	= parser.getContent();
			String attrs	= parser.getAttrs();
			Frame f = new Frame();
			f.buildAttrs(attrs);
			f.build(content);
			frames.add(f);
		}

		Frame[]	result	= new Frame[frames.size()];
		result = (Frame[]) frames.toArray(result);
		return result;
	}


	protected void buildAttrs(String attrs) {
		super.buildAttrs(attrs);

		src	= parserAttrSrc.getValue(attrs);
	}
    /**
     * @return Returns the href.
     */
    public String getSrc() {
        return src;
    }
}