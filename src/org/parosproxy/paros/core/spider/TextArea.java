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

class TextArea extends Tag {
	private String	value	= null;
	private String	type	= null;
    
	private static final ParserTag parser = new ParserTag(Tag.TEXTAREA, ParserTag.CLOSING_TAG_YES);
	
	static TextArea[] getTextAreas(String doc) {
		Vector textareas  = new Vector();
		parser.parse(doc);
		while (parser.nextTag()) {
			String content	= parser.getContent();
			String attrs	= parser.getAttrs();
			TextArea textarea = new TextArea();
            textarea.buildAttrs(attrs);
            textarea.build(content);
			textareas.addElement(textarea);
		}

		TextArea[]	result	= new TextArea[textareas.size()];
		result = (TextArea[]) textareas.toArray(result);
		return result;
	}
    
    protected void build(String content) {
        value = content;
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
