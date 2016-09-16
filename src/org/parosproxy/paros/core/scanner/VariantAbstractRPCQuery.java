/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package org.parosproxy.paros.core.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Abstract class for HTTP RPC request handling
 * 
 * @author andy
 */
public abstract class VariantAbstractRPCQuery implements Variant {
    
    private final Logger logger = Logger.getLogger(this.getClass());
    
    private final List<RPCParameter> listParam = new ArrayList<>();
    private final List<NameValuePair> params = new ArrayList<>();
    private String requestContent;

    @Override
    public void setMessage(HttpMessage msg) {
        // First check if it's a gwt rpc form data request
        // Otherwise give back an empty param list
        String contentType = msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
        if (contentType != null && isValidContentType(contentType)) {
            try {
                setRequestContent(msg.getRequestBody().toString());
            } catch (Exception e) {
                logger.warn("Failed to parse the request body: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 
     * @param name the name of the parameter
     * @param beginOffset the begin offset of the parameter value inside the RPC content body
     * @param endOffset the ending offset of the parameter value inside the RPC content body
     * @param toQuote the parameter need to be quoted when used
     * @param escaped the parameter value should be escaped so it has to be unescaped before
     */
    public void addParameter(String name, int beginOffset, int endOffset, boolean toQuote, boolean escaped) {
        RPCParameter param = new RPCParameter();
        String value = requestContent.substring(beginOffset, endOffset);
        param.setName(name);
        param.setValue((escaped) ? getUnescapedValue(value) : value);
        param.setBeginOffset(beginOffset);
        param.setEndOffset(endOffset);
        param.setToQuote(toQuote);
        listParam.add(param);
    }

    /**
     * 
     * @param name the name of the parameter
     * @param beginOffset the begin offset of the parameter value inside the RPC content body
     * @param endOffset the ending offset of the parameter value inside the RPC content body
     * @param toQuote the parameter need to be quoted when used
     * @param value the value that need to be set
     */
    public void addParameter(String name, int beginOffset, int endOffset, boolean toQuote, String value) {
        RPCParameter param = new RPCParameter();
        param.setName(name);
        param.setValue(value);
        param.setBeginOffset(beginOffset);
        param.setEndOffset(endOffset);
        param.setToQuote(toQuote);
        listParam.add(param);
    }    
    
    /**
     * 
     * @param beginOffset
     * @param endOffset
     * @return 
     */
    public String getToken(int beginOffset, int endOffset) {
        return requestContent.substring(beginOffset, endOffset);
    }

    /**
     * 
     * @return 
     */
    @Override
    public List<NameValuePair> getParamList() {
        return params;
    }

    /**
     * 
     * @param msg
     * @param originalPair
     * @param name
     * @param value
     * @return 
     */
    @Override
    public String setParameter(HttpMessage msg, NameValuePair originalPair, String name, String value) {
    	return this.setParameter(msg, originalPair, name, value, false);
    }

    /**
     * 
     * @param msg
     * @param originalPair
     * @param name
     * @param value
     * @return 
     */
    @Override
    public String setEscapedParameter(HttpMessage msg, NameValuePair originalPair, String name, String value) {
    	return this.setParameter(msg, originalPair, name, value, true);
    }

    /**
     * 
     * @param msg
     * @param originalPair
     * @param name
     * @param value
     * @param escaped
     * @return 
     */
    private String setParameter(HttpMessage msg, NameValuePair originalPair, String name, String value, boolean escaped) {
        RPCParameter param = listParam.get(originalPair.getPosition());
        StringBuilder sb = new StringBuilder();
        sb.append(requestContent.substring(0, param.getBeginOffset()));
        sb.append(escaped ? value : getEscapedValue(value, param.isToQuote()));
        sb.append(requestContent.substring(param.getEndOffset()));
        
        String query = sb.toString();
        msg.getRequestBody().setBody(query);
        return query;        
    }

    /**
     * 
     * @param requestContent 
     */
    protected void setRequestContent(String requestContent) {
        this.requestContent = requestContent;
        parseContent(requestContent);

        // Put each parameter in order by beginOffset
        // so that we can inject payloads
        // following the request order
        // --------------------------------------
        Collections.sort(listParam);

        for (int i = 0; i < listParam.size(); i++) {
            RPCParameter param = listParam.get(i);
            params.add(new NameValuePair(NameValuePair.TYPE_POST_DATA, param.getName(), param.getValue(), i));
        }         
    }
    
    /**
     * 
     * @return 
     */
    public String getReadableParametrizedQuery() {
       StringBuilder result = new StringBuilder();
       int begin = 0;
       int end;
       
       for (RPCParameter param : listParam) {
           end = param.getBeginOffset();
           result.append(requestContent.substring(begin, end));
           result.append("__INJECTABLE_PARAM__");
           begin = param.getEndOffset();
       }
       
       result.append(requestContent.substring(begin));
       
       return result.toString();
    }

    /**
     * 
     * @param contentType
     * @return 
     */
    public abstract boolean isValidContentType(String contentType);

    /**
     * 
     * @param content 
     */
    public abstract void parseContent(String content);

    /**
     * 
     * @param value
     * @param toQuote
     * @return 
     */
    public abstract String getEscapedValue(String value, boolean toQuote);

    /**
     * 
     * @param value
     * @return 
     */
    public abstract String getUnescapedValue(String value);
    
    /**
     * Inner support class
     */
    protected class RPCParameter implements Comparable<RPCParameter> {                
        private String name;
        private String value;
        private int beginOffset;
        private int endOffset;
        private boolean toQuote;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getBeginOffset() {
            return beginOffset;
        }

        public void setBeginOffset(int beginOffset) {
            this.beginOffset = beginOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public void setEndOffset(int endOffset) {
            this.endOffset = endOffset;
        }

        public boolean isToQuote() {
            return toQuote;
        }

        public void setToQuote(boolean toQuote) {
            this.toQuote = toQuote;
        }        

        @Override
        public int compareTo(RPCParameter t) {
            return this.beginOffset - t.beginOffset;
        }
    }
}
