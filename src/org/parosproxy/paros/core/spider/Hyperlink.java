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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hyperlink extends org.parosproxy.paros.core.spider.Tag {

    private static final Pattern patternURL = Pattern.compile("\\W(http://[^\\x00-\\x1f\"'\\s<>#]+)");
    
	private String	href	= "";

	/**
	 *	Get an array of "http://" URLs.
	 *	@param	doc
	 *			The html document to be parsed.
	 *	@return	array of "A"
	 */
	public static Hyperlink[] getHyperlinks(String doc) {

		Vector links = new Vector();
		Matcher matcher = patternURL.matcher(doc);
		while (matcher.find()) {
		    try {
		        String s = matcher.group(1);
		        Hyperlink hlink = new Hyperlink();
		        hlink.buildAttrs(s);
		        links.addElement(hlink);
		    } catch (Exception e) {}
		}

		Hyperlink[]	result	= new Hyperlink[links.size()];
		result = (Hyperlink[]) links.toArray(result);
		return result;
	}


	protected void buildAttrs(String href) {

		this.href	= href;
	}
	
    /**
     * @return Returns the href.
     */
    public String getLink() {
        return href;
    }
}