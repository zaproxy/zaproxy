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
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.common.AbstractParam;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SpiderParam extends AbstractParam {

	private static final String SPIDER = "spider";

	private static final String SPIDER_MAX_DEPTH = "spider.maxDepth";
	private static final String SPIDER_THREAD = "spider.thread";
	private static final String SPIDER_SCOPE = "spider.scope";
	private static final String SPIDER_POST_FORM = "spider.postform";
    private static final String SPIDER_SKIP_URL = "spider.skipurl";
	
	private int maxDepth = 5;
	private int thread = 2;
	private String scope = "";
	private int postForm = 0;
	private String skipURL = "";
    private Pattern patternSkipURL = null;
	private Pattern patternScope = null;
	
    /**
     * @param rootElementName
     */
    public SpiderParam() {
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.common.FileXML#parse()
     */
    protected void parse() {
        
		try {
			setThread(getConfig().getInt(SPIDER_THREAD, 2));
		} catch (Exception e) {}
		try {
			setMaxDepth(getConfig().getInt(SPIDER_MAX_DEPTH, 5));
		} catch (Exception e) {}
        try {
            setPostForm(getConfig().getInt(SPIDER_POST_FORM, 0) != 0);
        } catch (Exception e) {}
		setScope(getConfig().getString(SPIDER_SCOPE, ""));
        setSkipURL(getConfig().getString(SPIDER_SKIP_URL,""));

    }

    /**
     * @return Returns the maxDepth.
     */
    public int getMaxDepth() {
        return maxDepth;
    }
    /**
     * @param maxDepth The maxDepth to set.
     */
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
		getConfig().setProperty(SPIDER_MAX_DEPTH, Integer.toString(this.maxDepth));

    }
    /**
     * @return Returns the scope.
     */
    public String getScope() {
        return scope;
    }
    /**
     * @param scope The scope to set.
     */
    public void setScope(String scope) {
        this.scope = scope;
		getConfig().setProperty(SPIDER_SCOPE, this.scope);
		parseScope(this.scope);
    }
    
    /**
     * @return Returns the thread.
     */
    public int getThread() {
        return thread;
    }
    /**
     * @param thread The thread to set.
     */
    public void setThread(int thread) {
        this.thread = thread;
		getConfig().setProperty(SPIDER_THREAD, Integer.toString(this.thread));

    }
	
	/**
	Check if given host name need to send using proxy.
	@param	hostName	host name to be checked.
	@return	true = need to send via proxy.
	*/
	public boolean isInScope(String hostName) {
		if (patternScope == null || hostName == null) {
		    return false;
		}
		
		return patternScope.matcher(hostName).find();
	}
	
	/**
	Parse the proxy chain skip text string and build the regex pattern.
	*/
	private void parseScope(String scope) {
		patternScope = null;

		if (scope == null || scope.equals("")) {
			return;
		}
		
		scope = scope.replaceAll("\\.", "\\\\.");
		scope = scope.replaceAll("\\*",".*?").replaceAll("(;+$)|(^;+)", "");
		scope = "(" + scope.replaceAll(";+", "|") + ")$";
		patternScope = Pattern.compile(scope, Pattern.CASE_INSENSITIVE);
	}
    
    public boolean isPostForm() {
        return (postForm != 0);
    }
    
    public void setPostForm(boolean postForm) {
        if (postForm) {
            this.postForm = 1;
        } else {
            this.postForm = 0;
        }
        getConfig().setProperty(SPIDER_POST_FORM, Integer.toString(this.postForm));

    }
    
    public void setSkipURL(String skipURL) {
        this.skipURL = skipURL;
        getConfig().setProperty(SPIDER_SKIP_URL, this.skipURL);
        parseSkipURL(this.skipURL);
        
    }
    
    public boolean isSkipURL(URI uri) {

        if (patternSkipURL == null || uri == null) {
            return false;
        }
        String sURI = uri.toString();
        return patternSkipURL.matcher(sURI).find();
        
    }
    
    private void parseSkipURL(String skipURL) {
        patternSkipURL = null;
        
        if (skipURL == null || skipURL.equals("")) {
            return;
        }
        
        skipURL = skipURL.replaceAll("\\.", "\\\\.");
        skipURL = skipURL.replaceAll("\\*",".*?").replaceAll("(\\s+$)|(^\\s+)", "");
        skipURL = "\\A(" + skipURL.replaceAll("\\s+", "|") + ")";
        patternSkipURL = Pattern.compile(skipURL, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
    }
    
    public String getSkipURL() {
        return skipURL;
    }
}
