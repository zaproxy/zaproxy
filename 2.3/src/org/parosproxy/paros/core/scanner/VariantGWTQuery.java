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
     * Escape a GWT string according to the client implementation found on
     * com.google.gwt.user.client.rpc.impl.ClientSerializationStreamWriter
     * http://www.gwtproject.org/
     * 
     * @param value the value that need to be escaped
     * @param toQuote
     * @return 
     */
    @Override
    public String getEscapedValue(String value, boolean toQuote) {
        // Escape special characters
        StringBuilder buf = new StringBuilder(value.length());
        int idx = 0;
        int ch;

        while (idx < value.length()) {
            ch = value.charAt(idx++);
            if (ch == 0) {
                buf.append("\\0");

            } else if (ch == 92) { // backslash
                buf.append("\\\\");

            } else if (ch == 124) { // vertical bar
                // 124 = "|" = AbstractSerializationStream.RPC_SEPARATOR_CHAR
                buf.append("\\!");

            } else if ((ch >= 0xD800) && (ch < 0xFFFF)) {
                buf.append(String.format("\\u%04x", ch));

            } else {
                buf.append((char) ch);
            }
        }

        return buf.toString();
    }

    /**
     * Unescape a GWT serialized string according to the server implementation found on
     * com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader
     * http://www.gwtproject.org/
     * 
     * @param value the value that need to be deserialized
     * @return the deserialized value
     */
    @Override
    public String getUnescapedValue(String value) {
        // Change quoted characters back
        if (value.indexOf('\\') < 0) {
            return value;
            
        } else {
            StringBuilder buf = new StringBuilder(value.length());
            int idx = 0;
            char ch;

            while (idx < value.length()) {
                ch = value.charAt(idx++);
                if (ch == '\\') {
                    if (idx == value.length()) {
                        //Unmatched backslash, skip the backslash
                        break;
                    }

                    ch = value.charAt(idx++);
                    switch (ch) {
                        case '0':
                            buf.append('\u0000');
                            break;

                        case '!':
                            buf.append((char)RPC_SEPARATOR_CHAR);
                            break;

                        case '\\':
                            buf.append(ch);
                            break;

                        case 'u':
                            try {
                                if (idx + 4 < value.length()) {
                                    ch = (char)Integer.parseInt(value.substring(idx, idx + 4), 16);
                                    buf.append(ch);
                                    
                                } else {
                                    //Invalid Unicode hex number
                                    //skip the sequence
                                }

                            } catch (NumberFormatException ex) {
                                //Invalid Unicode escape sequence
                                //skip the sequence
                            }
    
                            idx += 4;
                            break;

                        default:
                            //Unexpected escape character
                            //skip the sequence
                    }
                } else {
                    buf.append(ch);
                }
            }

            return buf.toString();
        }
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
