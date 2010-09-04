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
package org.parosproxy.paros.core.scanner;

import java.util.Vector;
import java.util.regex.Pattern;

import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
abstract public class VariantAbstractQuery implements Variant {
    
	private static Pattern staticPatternParam = Pattern.compile("&", Pattern.CASE_INSENSITIVE);	

    private Vector listParam = new Vector();
    
    public VariantAbstractQuery() {
        
    }

    abstract protected void buildMessage(HttpMessage msg, String query);
    
    /**
     * Return encoded mutate of the value.  To be overriden by subclass.
     * @param msg
     * @param value
     * @return Encoded value
     */
    protected String getEncodedValue(HttpMessage msg, String value) {
       return value; 
    }
    
    protected void parse(String params) {
        
        if (params == null || params.equals("")) {
            return;
        }
        
		String[] keyValue = staticPatternParam.split(params);
		String key = null;
		String value = null;
		int pos = 0;
		for (int i=0; i<keyValue.length; i++) {
			key = null;
			value = null;
			pos = keyValue[i].indexOf('=');
			try {
				if (pos > 0) {
					key = keyValue[i].substring(0,pos);
					value = keyValue[i].substring(pos+1);
				} else {
				    key = keyValue[i];
				    value = null;
				    
				}
				listParam.add(new NameValuePair(key, value, i));

			} catch (Exception e) {
			}
		}
		
    }
    
    public Vector getParamList() {
        return listParam;
    }

    /**
     * If name and value = null, not to append entire paramter.
     */
    public String setParameter(HttpMessage msg, NameValuePair originalPair, String name, String value) {

        StringBuffer sb = new StringBuffer();
        NameValuePair pair = null;
        boolean isAppended = false;
        for (int i=0; i<getParamList().size(); i++) {
            pair = (NameValuePair) getParamList().get(i);
            if (i == originalPair.getPosition()) {
                String encodedValue = getEncodedValue(msg, value);
                isAppended = paramAppend(sb, name, encodedValue);

            } else {
                isAppended = paramAppend(sb, pair.getName(), pair.getValue());

            }

            if (isAppended && i<getParamList().size()-1) {
                sb.append('&');
            }
        }

        String query = sb.toString();
        buildMessage(msg, query);
        return query;
    }

    /**
     * Set the name value pair into the StringBuffer.  If both name and value is null,
     * not to append whole paramter.
     * @param sb
     * @param name Null = not to append parameter.
     * @param value null = not to append parameter value.
     * @return true = paretmer changed.
     */
    private boolean paramAppend(StringBuffer sb, String name, String value) {
        String param = name;
        boolean isEdited = false;
        if (name != null) {
            sb.append(name);
            isEdited = true;
        }
        if (value != null) {
            sb.append('=');
            sb.append(value);
            isEdited = true;
        }
        return isEdited;
    }

}
