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
// ZAP: 2013/07/03 Improved encapsulation for quoting and content type checking
// ZAP: 2013/07/10 Added some features and method encapsulation
// ZAP: 2013/07/21 Added XML parameters ordering on tag position inside the overall content

package org.parosproxy.paros.core.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Abstract class for HTTP RPC request handling
 * 
 * @author andy
 */
public abstract class VariantAbstractRPCQuery implements Variant {
    
    private List<RPCParameter> listParam = new ArrayList();
    private List<NameValuePair> params = new ArrayList();
    private String requestContent;

    @Override
    public void setMessage(HttpMessage msg) {
        // First check if it's a gwt rpc form data request
        // Otherwise give back an empty param list
        String contentType = msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
        if (contentType != null && isValidContentType(contentType)) {
            setRequestContent(msg.getRequestBody().toString());
        }
    }

    /**
     * 
     * @param name
     * @param beginOffset
     * @param endOffset
     * @param quote
     */
    public void addParameter(String name, int beginOffset, int endOffset, boolean toQuote) {
        RPCParameter param = new RPCParameter();
        param.setName(name);
        param.setValue(requestContent.substring(beginOffset, endOffset));
        param.setBeginOffset(beginOffset);
        param.setEndOffset(endOffset);
        param.setToQuote(toQuote);
        listParam.add(param);
    }

    /**
     * 
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
     * @param param
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
        sb.append(encodeParameter(value, param.isToQuote(), escaped));
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
            params.add(new NameValuePair(param.getName(), param.getValue(), i));
        }         
    }
    
    /**
     * 
     * @return 
     */
    protected String getReadableParametrizedQuery() {
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
     * @param escaped
     * @return 
     */
    public abstract String encodeParameter(String value, boolean toQuote, boolean escaped);
    
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
