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
// ZAP: 2012/03/15 Changed the methods (the private) setParameter and paramAppend to 
// use the class StringBuilder instead of StringBuffer. Removed unnecessary 
// castings in the method setParameter. Made a change in the method parse.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2013/07/02 Changed Vector to ArrayList because obsolete and faster
// ZAP: 2013/08/21 Added a new encoding/decoding model for a correct parameter value interpretation
// ZAP: 2014/01/06 Issue 965: Support 'single page' apps and 'non standard' parameter separators

package org.parosproxy.paros.core.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.ParameterParser;

public abstract class VariantAbstractQuery implements Variant {

    private List<NameValuePair> listParam = new ArrayList<>();

    public VariantAbstractQuery() {
    }

    /**
     * Build the message content setting the query string
     * according to the Variant specific implementation
     * 
     * @param msg the message object we need to modify
     * @param query the query string we need to set inside the message
     */
    protected abstract void buildMessage(HttpMessage msg, String query);

    /**
     * Return escaped mutate of the value. To be overridden by subclass.
     *
     * @param msg
     * @param value
     * @return the escaped value
     */
    protected abstract String getEscapedValue(HttpMessage msg, String value);

    /**
     * Return unescaped mutate of the value. To be overridden by subclass.
     * 
     * @param value
     * @return the unescaped value
     */
    protected abstract String getUnescapedValue(String value);

    /**
     * 
     * @param params 
     */
    protected void setParams(String type, Map<String, String> params) {
    	int i = 0;
	    for (Entry<String, String> param : params.entrySet()) {
            listParam.add(new NameValuePair(type, param.getKey(), getUnescapedValue(param.getValue()), i));
	    	i++;
	    }
    }

    /**
     *
     * @return
     */
    @Override
    public List<NameValuePair> getParamList() {
        return listParam;
    }

    /**
     * If name and value = null, not to append entire parameter.
     */
    @Override
    public String setParameter(HttpMessage msg, NameValuePair originalPair, String name, String value) {
        return this.setParameter(msg, originalPair, name, value, false);
    }

    @Override
    public String setEscapedParameter(HttpMessage msg, NameValuePair originalPair, String name, String value) {
        return this.setParameter(msg, originalPair, name, value, true);
    }

    private String setParameter(HttpMessage msg, NameValuePair originalPair, String name, String value, boolean escaped) {
    	// We need the correct parameter parser to use the right separators
    	ParameterParser parser;
    	if (NameValuePair.TYPE_FORM.equals(originalPair.getType())) {
    		parser = Model.getSingleton().getSession().getFormParamParser(msg.getRequestHeader().getURI().toString());
    	} else {
    		parser = Model.getSingleton().getSession().getUrlParamParser(msg.getRequestHeader().getURI().toString());
    	}
    	
        StringBuilder sb = new StringBuilder();
        String encodedValue = (escaped) ? value : getEscapedValue(msg, value);
        NameValuePair pair;
        boolean isAppended;
        
        for (int i = 0; i < getParamList().size(); i++) {
            pair = getParamList().get(i);
            if (i == originalPair.getPosition()) {
                isAppended = paramAppend(sb, name, encodedValue, parser);

            } else {
                isAppended = paramAppend(sb, pair.getName(), getEscapedValue(msg, pair.getValue()), parser);
            }

            if (isAppended && i < getParamList().size() - 1) {
                sb.append(parser.getDefaultKeyValuePairSeparator());
            }
        }

        String query = sb.toString();
        buildMessage(msg, query);
        return query;
    }

    /**
     * Set the name value pair into the StringBuilder. If both name and value is
     * null, not to append whole parameter.
     *
     * @param sb
     * @param name Null = not to append parameter.
     * @param value null = not to append parameter value.
     * @return true = parameter changed.
     */
    private boolean paramAppend(StringBuilder sb, String name, String value, ParameterParser parser) {
        boolean isEdited = false;
        
        if (name != null) {
            sb.append(name);
            isEdited = true;
        }
        
        if (value != null) {
            sb.append(parser.getDefaultKeyValueSeparator());
            sb.append(value);
            isEdited = true;
        }
        
        return isEdited;
    }
}
