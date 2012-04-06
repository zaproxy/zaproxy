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


import org.apache.commons.httpclient.URI;

/**
 *	An html used for spider/collector
 */
public class Html {

    private String doc = "";
    private URI uri = null;

	/**
	 *	Constructor
	 *	@param	url
	 *			url of the html
	 *	@param	plainDoc
	 *			the plain text html. For pattern matching.
	 *			(This is required as HTMLDocument cannot return the plain text.
	 */
    public Html(URI uri, String doc) {
		this.uri = uri;
		this.doc = doc;
        
        Base[] bases = getBases();
        if (bases.length > 0) {
            if (bases[0].getHref().length() >0) {
                // base href exists
                try {
                	// ZAP: FindBugs fix - assign to this.url rather than url
                    this.uri = new URI(bases[0].getHref(), false);
                } catch (Exception e) {
                }
            }
        }
        
    }

    public URI getURI() {
		return uri;
    }

    public String toString() {
		return doc;
    }

	public Form[] getForms() {
		return Form.getForms(doc);
	}

	public A[] getAs() {
		return A.getAs(doc);
	}
	
	public Frame[] getFrames() {
		return Frame.getFrames(doc);
	}

	public Img[] getImgs() {
		return Img.getImgs(doc);
	}
	
	public Hyperlink[] getHyperlinks() {
	    return Hyperlink.getHyperlinks(doc);
	}
	
	public Meta[] getMetas() {
		return Meta.getMetas(doc);
	}
    
    private Base[] getBases() {
        return Base.getBases(doc);
    }

}

