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
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/07/02 Changed API to public because future extensible Variant model
// ZAP: 2014/01/06 Issue 965: Support 'single page' apps and 'non standard' parameter separators

package org.parosproxy.paros.core.scanner;

public class NameValuePair {
	
	public static final String TYPE_COOKIE = "cookie";
	public static final String TYPE_FORM = "form";
	public static final String TYPE_HEADER = "header";
	public static final String TYPE_MULTIPART_FORM = "multipartform";
	public static final String TYPE_ODATA_FILTER = "odatafilter";
	public static final String TYPE_ODATA_ID = "odataid";
	public static final String TYPE_RPC = "rpc";
	public static final String TYPE_URL = "url";
	
	private String type = null;
    private String name = null;
    private String value = null;
    private int position = 0;
    
    NameValuePair() {
        
    }
    
    /**
     * @param name
     * @param value
     */
    public NameValuePair(String type, String name, String value, int position) {
        super();
        this.type = type;
        this.name = name;
        this.value = value;
        this.position = position;
    }
    /**
     * Returns the type
     * @return
     */
    public String getType() {
		return type;
	}

	/**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }
    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    
    /**
     * @return Returns the position.
     */
    public int getPosition() {
        return position;
    }
    /**
     * @param position The position to set.
     */
    public void setPosition(int position) {
        this.position = position;
    }
}
