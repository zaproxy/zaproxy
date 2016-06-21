/*
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
// ZAP: 2014/02/08 Used the same constants used in ScanParam Target settings
// ZAP: 2016/02/22 Add hashCode, equals and toString methods. Remove redundant instance variable initialisations.
package org.parosproxy.paros.core.scanner;

public class NameValuePair {

    // ZAP: Parameter type constants
    public static final int TYPE_URL_PATH = ScannerParam.TARGET_URLPATH;
    public static final int TYPE_QUERY_STRING = ScannerParam.TARGET_QUERYSTRING;
    public static final int TYPE_COOKIE = ScannerParam.TARGET_COOKIE;
    public static final int TYPE_HEADER = ScannerParam.TARGET_HTTPHEADERS;
    public static final int TYPE_POST_DATA = ScannerParam.TARGET_POSTDATA;
    public static final int TYPE_UNDEFINED = -1;
    
    private final int targetType;
    private String name;
    private String value;
    private int position;

    /**
     * @param name
     * @param value
     */
    public NameValuePair(int type, String name, String value, int position) {
        super();
        this.targetType = type;
        this.name = name;
        this.value = value;
        this.position = position;
    }

    /**
     * Returns the type
     *
     * @return
     */
    public int getType() {
        return targetType;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + position;
        result = prime * result + targetType;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NameValuePair other = (NameValuePair) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (position != other.position) {
            return false;
        }
        if (targetType != other.targetType) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder(75);
        strBuilder.append("[Position=").append(position);
        strBuilder.append(", Type=").append(targetType);
        if (name != null) {
            strBuilder.append(", Name=").append(name);
        }
        if (value != null) {
            strBuilder.append(", Value=").append(value);
        }
        strBuilder.append(']');
        return strBuilder.toString();
    }
}
