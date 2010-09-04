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

public class Select extends Tag {

	private static final ParserTag parser = new ParserTag(Tag.SELECT, ParserTag.CLOSING_TAG_YES);

	private Option[] option = null;

	static Select[] getSelects(String doc) {

		Vector selects = new Vector();
		parser.parse(doc);
		while (parser.nextTag()) {
			String content	= parser.getContent();
			String attrs	= parser.getAttrs();
			Select select = new Select();
			select.build(content);
			select.buildAttrs(attrs);
			selects.addElement(select);
		}

		Select[]	result	= new Select[selects.size()];
		result = (Select[]) selects.toArray(result);
		return result;
	}

	protected void build(String content) {
		option = Option.getOptions(content);
	}
	
	public Option[] getOption() {
	    return option;
	}

}
