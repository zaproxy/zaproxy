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
// ZAP: 2013/07/01 Added string encoding according to the abstract superclass
// ZAP: 2013/08/21 Added decoding for correct parameter value manipulation

package org.parosproxy.paros.core.scanner;

/**
 * Simplified GWT RPC Variant only set to not-empty strings parameter...
 * It takes the RPC call as it has been retrieved, check only for 
 * java.util.String objects, and give back only the one that has a 
 * set value. If a string has been set to null (so it comes in the
 * request with the value 0 inside the index) it's discarded.
 * To manage null string we need to rebuild the overall payload
 * and currently there are some lack of information regarding the
 * real serialization model used by the GWT environment (e.g. how
 * Long are encoded, how can be serialized vectors, how validation
 * classes work, how are managed proxy objects and, finally, 
 * the use of encoded types. 
 * 
 * @author yhawke
 */
public class VariantGWTQuery extends VariantAbstractRPCQuery {

    public static final String GWT_RPC_CONTENT_TYPE = "text/x-gwt-rpc";
    
    public static final int RPC_SEPARATOR_CHAR = '|';
    public static final int FLAG_RPC_TOKEN_INCLUDED = 0x2;

    /**
     * 
     * @param contentType
     * @return 
     */
    @Override
    public boolean isValidContentType(String contentType) {
        return contentType.startsWith(GWT_RPC_CONTENT_TYPE);
    }

    /**
     * 
     * @param content 
     */
    @Override
    public void parseContent(String content) {
        GWTStringTokenizer st = new GWTStringTokenizer(content, RPC_SEPARATOR_CHAR);
        int version = Integer.parseInt(st.nextToken());
        int flags = Integer.parseInt(st.nextToken());
        // Read the type name table
        int columns = Integer.parseInt(st.nextToken());
        int[] stringTableIndices = new int[columns + 1];
        String[] stringTable = new String[columns];
        for (int i = 0; i < columns; i++) {
            stringTableIndices[i] = st.getPosition();
            stringTable[i] = st.nextToken();
        }
   
        // Now get the index of the last one
        stringTableIndices[columns] = st.getPosition();

        // Start read the first elements
        // inside the RPC stringTable
        // following the RPC index table
        // ---------------------------------------
        String moduleBaseUrl = stringTable[Integer.parseInt(st.nextToken()) - 1];
        String strongName = stringTable[Integer.parseInt(st.nextToken()) - 1];
        String rpcToken = null;

        // rpc request has an rpc/xsrf token
        if ((flags & FLAG_RPC_TOKEN_INCLUDED) == FLAG_RPC_TOKEN_INCLUDED) {
            // Read the RPC token
            rpcToken = stringTable[Integer.parseInt(st.nextToken()) - 1];
        }

        // Get service and method name
        // should be obfuscated but it's not important for
        // this request parser
        String serviceInterfaceName = stringTable[Integer.parseInt(st.nextToken()) - 1];
        String serviceMethodName = stringTable[Integer.parseInt(st.nextToken()) -1];

        // Get the number of parameters        
        int paramCount = Integer.parseInt(st.nextToken());
        // Get each parameter type
        String[] parameterTypes = new String[paramCount];
        for (int i = 0; i < parameterTypes.length; i++) {
            // --
            // Simpler method for parameter type retrieval
            // should be interpreted the hash value after the / char
            // see also encoded types manipulation how works
            // --
            parameterTypes[i] = stringTable[Integer.parseInt(st.nextToken()) - 1];
        }
        
        for (int i = 0; i < paramCount; i++) {
            String strIndex = st.nextToken();
            if (parameterTypes[i].startsWith("java.lang.String")) {
                int idx = Integer.parseInt(strIndex);
                if (idx > 0) {
                    addParameter(String.valueOf(i), stringTableIndices[idx - 1], stringTableIndices[idx] - 1, false, true);
                }
            }
        }        
    }
    
    /**
     * 
     * @param value
     * @param toQuote
     * @param escaped
     * @return 
     */
    @Override
    public String getEscapedValue(String value, boolean toQuote, boolean escaped) {
        return value;
    }

    @Override
    public String getUnescapedValue(String value) {
        return value;
    }
    
    /* Code available for complete GWT parsing -----
    private List<NameValuePair> params = new ArrayList();
    private RPCRequest request = null;

    @Override
    public void setMessage(HttpMessage msg) {
        // First check if it's a gwt rpc form data request
        // Otherwise give back an empty param list
        String contentType = msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
        if (contentType.startsWith(GWT_RPC_CONTENT_TYPE)) {
            request = RPCRequestHandler.decodeRequest(msg.getRequestBody().toString());
            
            for (RPCParameter p : request.getParameters()) {
                if (p.getTypeSignature().startsWith("java.lang.String")) {
                    params.add(new NameValuePair(String.valueOf(p.getPosition()), (String)p.getValue(), p.getPosition()));
                }
            }
        }
    }
    * 
    private String setParameter(HttpMessage msg, NameValuePair originalPair, String name, String value, boolean escaped) {        
        String query = RPCRequestHandler.encodeRequest(request, originalPair.getPosition(), value);
        msg.getRequestBody().setBody(query);
        return query;
    }
    */

    /**
     * This is a replacement for the standard StringTokenizer which handles
     * multiple delimiters differently. Given the input "A,B,,,E", a
     * StringTokenizer would return three tokens: "A", "B", and "E" (collapsing
     * the repeated ",,," into a single delimiter ","). A
     * NoncollapsingStringTokenizer instead returns five tokens: "A", "B", "",
     * "", "E". The repeated delimiters are taken to indicate empty fields, so
     * an empty string "" is returned where appropriate.
     */
    protected class GWTStringTokenizer {

        private String str;
        private int delim;
        private int currentPosition;

        public GWTStringTokenizer(String str, int delim) {
            this.str = str;
            this.delim = delim;
        }

        public String nextToken() {
            int nextDelimPosition = str.indexOf(delim, currentPosition);
            if (nextDelimPosition < 0) {
                nextDelimPosition = str.length();
            }

            String token = str.substring(currentPosition, nextDelimPosition);
            currentPosition = nextDelimPosition + 1;
            return token;
        }

        public boolean hasMoreTokens() {
            return (currentPosition < str.length());
        }
        
        public int getPosition() {
            return currentPosition;
        }
    }
}
