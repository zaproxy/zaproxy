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


class Tag {

	public static final String FORM 	= "FORM";
	public static final String INPUT	= "INPUT";
	public static final String SELECT 	= "SELECT";
	public static final String OPTION 	= "OPTION";
	public static final String A		= "A";
    public static final String TEXTAREA = "TEXTAREA";

	protected static final ParserAttr parserAttrName	= new ParserAttr(Attr.NAME);
	protected static final ParserAttr parserAttrValue	= new ParserAttr(Attr.VALUE);

	private String	name = "";

	Tag() {
	}

	Tag(String name) {
		this.name	= name;
	}

	protected void build(String content) {
	}

	protected void buildAttrs(String attrs) {
		name = parserAttrName.getValue(attrs);
	}

	public String getName() {
	    return name;	    
	}
}