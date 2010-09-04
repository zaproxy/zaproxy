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

package org.parosproxy.paros.extension.trap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.parosproxy.paros.common.AbstractParam;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TrapParam extends AbstractParam {

	private static final String TRAP = "trap";

	private static final String INCLUSIVE_FILTER = "trap.inclusiveFilter";
	private static final String EXCLUSIVE_FILTER = "trap.exclusiveFilter";
	
	
	private String inclusiveFilter = "";
	private String exclusiveFilter = "";
	
	private Pattern patternInclusiveFilter = null;
	private Pattern patternExclusiveFilter = null;
	
    /**
     * @param rootElementName
     */
    public TrapParam() {

    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.common.FileXML#parse()
     */
    protected void parse() {
        

		setInclusiveFilter(getConfig().getString(INCLUSIVE_FILTER, ""));
		setExclusiveFilter(getConfig().getString(EXCLUSIVE_FILTER, ""));

    }
	
	private void parseInclusiveFilter(String inclusiveFilter) {
		patternInclusiveFilter = null;

		if (inclusiveFilter == null || inclusiveFilter.equals("")) {
			return;
		}
		
		inclusiveFilter = inclusiveFilter.replaceAll("\\.", "\\\\.");
		inclusiveFilter = inclusiveFilter.replaceAll("\\*",".*?").replaceAll("(;+$)|(^;+)", "");
		inclusiveFilter = "(" + inclusiveFilter.replaceAll(";+", "|") + ")$";
		patternInclusiveFilter = Pattern.compile(inclusiveFilter, Pattern.CASE_INSENSITIVE);
	}

	private void parseExclusiveFilter(String exclusiveFilter) {
		patternExclusiveFilter = null;

		if (exclusiveFilter == null || exclusiveFilter.equals("")) {
			return;
		}
		
		exclusiveFilter = exclusiveFilter.replaceAll("\\.", "\\\\.");
		exclusiveFilter = exclusiveFilter.replaceAll("\\*",".*?").replaceAll("(;+$)|(^;+)", "");
		exclusiveFilter = "(" + exclusiveFilter.replaceAll(";+", "|") + ")$";

		patternExclusiveFilter = Pattern.compile(exclusiveFilter, Pattern.CASE_INSENSITIVE);
	}

    /**
     * @return Returns the exclusiveFilter.
     */
    public String getExclusiveFilter() {
        return exclusiveFilter;
    }
    /**
     * @param exclusiveFilter The exclusiveFilter to set.
     */
    public void setExclusiveFilter(String exclusiveFilter) {
        this.exclusiveFilter = exclusiveFilter;
		parseExclusiveFilter(this.exclusiveFilter);
    	getConfig().setProperty(EXCLUSIVE_FILTER, this.exclusiveFilter);

    }
    /**
     * @return Returns the inclusiveFilter.
     */
    public String getInclusiveFilter() {
        return inclusiveFilter;
    }
    
    /**
     * @param inclusiveFilter The inclusiveFilter to set.
     */
    public void setInclusiveFilter(String inclusiveFilter) {
        this.inclusiveFilter = inclusiveFilter;
		parseInclusiveFilter(this.inclusiveFilter);
    	getConfig().setProperty(INCLUSIVE_FILTER, this.inclusiveFilter);

    }
    
    public boolean isInclude(String s) {
        boolean result = true;
        Matcher matcher = null;
        if (patternInclusiveFilter != null) {
            try {
                
                matcher = patternInclusiveFilter.matcher(s);
                if (matcher.find()) {
                    result = true;
                } else {
                    result = false;
                }
            } catch (Exception e) {}
        }
    	System.out.println(" : " + result);
        return result;
    }
    
    public boolean isExclude(String s) {
        boolean result = false;
        Matcher matcher = null;
        if (patternExclusiveFilter != null) {
            try {
                matcher = patternExclusiveFilter.matcher(s);
                if (matcher.find()) {
                    result = true;
                } else {
                    result = false;
                }
            } catch (Exception e) {
            }
        }
        return result;
    }

}
