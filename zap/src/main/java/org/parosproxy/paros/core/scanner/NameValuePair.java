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
// ZAP: 2016/02/22 Add hashCode, equals and toString methods. Remove redundant instance variable
// initialisations.
// ZAP: 2018/01/03 Added type constants for revised multipart/form-data handling
// ZAP: 2018/02/19 Added type constants for application/json handling
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/09/01 Added compareTo method.
// ZAP: 2020/09/22 Added type constant for GraphQL Inline Arguments handling.
package org.parosproxy.paros.core.scanner;

public class NameValuePair implements Comparable<NameValuePair> {

    // NOTE: After adding a new type update ScannerParamFilter.
    public static final int TYPE_URL_PATH = ScannerParam.TARGET_URLPATH;
    public static final int TYPE_QUERY_STRING = ScannerParam.TARGET_QUERYSTRING;
    public static final int TYPE_COOKIE = ScannerParam.TARGET_COOKIE;
    public static final int TYPE_HEADER = ScannerParam.TARGET_HTTPHEADERS;
    public static final int TYPE_POST_DATA = ScannerParam.TARGET_POSTDATA;
    /**
     * A "normal" (non-file) multipart/form-data parameter
     *
     * @since 2.8.0
     */
    public static final int TYPE_MULTIPART_DATA_PARAM = 33;
    /**
     * A file (content) multipart/form-data file parameter
     *
     * @since 2.8.0
     */
    public static final int TYPE_MULTIPART_DATA_FILE_PARAM = 34;
    /**
     * The filename portion of a multipart/form-data file parameter
     *
     * @since 2.8.0
     */
    public static final int TYPE_MULTIPART_DATA_FILE_NAME = 35;
    /**
     * The content-type portion of a multipart/form-data file parameter
     *
     * @since 2.8.0
     */
    public static final int TYPE_MULTIPART_DATA_FILE_CONTENTTYPE = 36;
    /**
     * The application/json content-type of a web application
     *
     * @since 2.8.0
     */
    public static final int TYPE_JSON = 37;
    /**
     * The inline arguments in a GraphQL query
     *
     * @since 2.10.0
     */
    public static final int TYPE_GRAPHQL_INLINE = 38;

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

    /** @return Returns the name. */
    public String getName() {
        return name;
    }

    /** @param name The name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return Returns the value. */
    public String getValue() {
        return value;
    }

    /** @param value The value to set. */
    public void setValue(String value) {
        this.value = value;
    }

    /** @return Returns the position. */
    public int getPosition() {
        return position;
    }

    /** @param position The position to set. */
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

    @Override
    public int compareTo(NameValuePair nvp) {
        if (nvp == null) {
            return -1;
        }
        if (nvp.targetType != targetType) {
            return nvp.targetType - targetType;
        }
        if (nvp.position != position) {
            return nvp.position - position;
        }
        int cmp;
        if (nvp.name != null && name != null) {
            cmp = nvp.name.compareTo(name);
            if (cmp != 0) {
                return cmp;
            }
        } else if (nvp.name == null || name == null) {
            // They can't both be null due to previous test
            return nvp.name != null ? -1 : 1;
        }
        if (nvp.value != null && value != null) {
            cmp = nvp.value.compareTo(value);
            if (cmp != 0) {
                return cmp;
            }
        } else if (nvp.value == null || value == null) {
            return nvp.value != null ? -1 : 1;
        }
        return 0;
    }
}
