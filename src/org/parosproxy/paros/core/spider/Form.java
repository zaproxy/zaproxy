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


public class Form extends Tag {

	public static final String	POST	= "POST",
								GET		= "GET";

	private static final ParserTag	parser	= new ParserTag(Tag.FORM, ParserTag.CLOSING_TAG_YES);

	private static final ParserAttr	parserAttrAction = new ParserAttr(Attr.ACTION);
	private static final ParserAttr	parserAttrMethod = new ParserAttr(Attr.METHOD);

	private String	action	= "",
					method	= "";
	
	private Select[]	select	= null;
	private Input[]		input	= null;
    private TextArea[]  textArea = null;

	public static Form[] getForms(String doc) {

		Vector forms = new Vector();
		parser.parse(doc);
		while (parser.nextTag()) {
			String content	= parser.getContent();
			String attrs	= parser.getAttrs();
			Form form = new Form();
			form.buildAttrs(attrs);
			form.build(content);
			forms.addElement(form);
		}

		Form[]	result	= new Form[forms.size()];
		result = (Form[]) forms.toArray(result);
		return result;
	}

	protected void build(String content) {
		select	= Select.getSelects(content);
		input	= Input.getInputs(content);
        textArea = TextArea.getTextAreas(content);
	}

	protected void buildAttrs(String attrs) {
		super.buildAttrs(attrs);
		action	= parserAttrAction.getValue(attrs);
		method	= parserAttrMethod.getValue(attrs);
	}
    /**
     * @return Returns the action.
     */
    public String getAction() {
        return action;
    }
    /**
     * @return Returns the input.
     */
    public Input[] getInput() {
        return input;
    }
    /**
     * @return Returns the method.
     */
    public String getMethod() {
        return method;
    }
    /**
     * @return Returns the select.
     */
    public Select[] getSelect() {
        return select;
    }
    
    public TextArea[] getTextArea() {
        return textArea;
    }
}











