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

class Input extends Tag {
	private String	value	= null;
	private String	type	= null;

	public static final String	TEXT		= "TEXT",
								HIDDEN		= "HIDDEN",
								SUBMIT		= "SUBMIT",
								CHECKBOX	= "CHECKBOX",
								RADIO		= "RADIO",
								RESET		= "RESET",
								PASSWORD	= "PASSWORD";

	private static final ParserTag parser = new ParserTag(Tag.INPUT, ParserTag.CLOSING_TAG_NO);
	private static final ParserAttr	parserAttrType = new ParserAttr(Attr.TYPE);

	static Input[] getInputs (String doc) {
		Vector inputs = new Vector();
		parser.parse(doc);
		while (parser.nextTag()) {
			String content	= parser.getContent();
			String attrs	= parser.getAttrs();
			Input input = new Input();
			input.buildAttrs(attrs);
			input.build(content);
			inputs.addElement(input);
		}

		Input[]	result	= new Input[inputs.size()];
		result = (Input[]) inputs.toArray(result);
		return result;
	}

	protected void buildAttrs(String attrs) {
		super.buildAttrs(attrs);
		value	= parserAttrValue.getValue(attrs);
		type 	= parserAttrType.getValue(attrs);
	}
    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }
}
