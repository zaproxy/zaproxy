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

public class Option extends Tag {

	private static final ParserTag parser = new ParserTag(Tag.OPTION, ParserTag.CLOSING_TAG_OPTIONAL);

	private String value = "";

	static Option[] getOptions (String doc) {
		Vector options = new Vector();
		parser.parse(doc);
		while (parser.nextTag()) {
			String content	= parser.getContent();
			String attrs	= parser.getAttrs();
			Option option = new Option();
			option.buildAttrs(attrs);
			option.build(content);
			options.addElement(option);
		}

		Option[]	result	= new Option[options.size()];
		result = (Option[]) options.toArray(result);
		return result;
	}

	protected void buildAttrs(String attrs) {
		// option does not have name attribute
		//	super.buildAttrs(attrs);
		value = parserAttrValue.getValue(attrs);

	}

	protected void build(String content) {
		// if there is no value attribute, the value is the content
		if (value.trim().length() == 0) {
			value = content;
		}
	}
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }
}