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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;

/**
 *
 * @author andy
 */
public class MultipartFormParser {
    
    public static final String WWW_MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String DEFAULT_ENCODING = "ISO-8859-1";

    private String boundary = null;
    private String encoding = DEFAULT_ENCODING;
    private BufferedReader br = null;

    /**
     * Cannot create a class without initialization!!!
     */
    private MultipartFormParser() {}
        
    /**
     * 
     * @param msg 
     */
    public MultipartFormParser(HttpMessage msg) throws IOException {
        // First check if it's a multipart form data request
        // Otherwise give back an empty param list
        String contentType = msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
        String line;

        if (contentType != null && contentType.startsWith(WWW_MULTIPART_FORM_DATA)) {
            // OK now it's time to parse the Multipart Request
            // Get the token string; it's included in the content type.
            // Should look something like "------------------------12012133613061"
            boundary = extractBoundary(contentType);

            if (boundary != null) {
                br = new BufferedReader(new StringReader(msg.getRequestBody().toString()));
                // Read until we hit the token
                // Some clients send a preamble (per RFC 2046), so ignore that
                // Thanks to Ben Johnson, ben.johnson@merrillcorp.com, for pointing out
                // the need for preamble support.
                do {
                    line = br.readLine();
                    if (line == null) {
                        throw new IOException("Corrupt form data: premature ending");
                    }
                    // See if this line is the token, and if so break
                    if (line.startsWith(boundary)) {
                        break;  // success
                    }
                    
                } while (true);
                
            } else {
                throw new IOException("No boundary defined in the Content-type header");
            }

        } else {
            throw new IOException("The request is not a " + WWW_MULTIPART_FORM_DATA + " content");
        }
    }

    /**
     * 
     * @return 
     */
    public String getBoundary() {
        return boundary;
    }
        
    public MultipartParam getNextParam() throws IOException {
        MultipartParam param;
        String line;

        // Read the headers; they look like this (not all may be present):
        // Content-Disposition: form-data; name="field1"; filename="file1.txt"
        // Content-Type: type/subtype
        // Content-Transfer-Encoding: binary
        line = br.readLine();
        if (line == null) {
            // No parts left, we're done
            return null;

        } else if (line.length() == 0) {
            // IE4 on Mac sends an empty line at the end; treat that as the end.
            // Thanks to Daniel Lemire and Henri Tourigny for this fix.
            return null;
        }

        param = new MultipartParam();

        // Read the following header lines we hit an empty line
        // A line starting with whitespace is considered a continuation;
        // that requires a little special logic.  Thanks to Nic Ferrier for
        // identifying a good fix.
        while (line != null && line.length() > 0) {
            String nextLine = null;
            boolean getNextLine = true;
            while (getNextLine) {
                nextLine = br.readLine();
                if ((nextLine != null) && (nextLine.startsWith(" ") || nextLine.startsWith("\t"))) {
                    line = line + nextLine;

                } else {
                    getNextLine = false;
                }
            }

            // Add the line to the header list
            param.addHeader(line);
            line = nextLine;
        }

        // If we got a null above, it's the end
        if (line == null) {
            return null;
        }

        // Now, finally, we read the content (end after reading the token)
        line = br.readLine();
        StringBuilder value = new StringBuilder();
        boolean isNotFirst = false;
        
        while (!line.startsWith(boundary)) {
            if (isNotFirst) {
                value.append(HttpHeader.CRLF);
                
            } else {
                isNotFirst = true;            
            }
            
            value.append(line);            
            line = br.readLine();
        }

        param.setContent(value.toString());
        return param;
    }
    
    /**
     * Extracts and returns the token token from a line.
     *
     * @return the token token.
     */
    private String extractBoundary(String line) {
        // Use lastIndexOf() because IE 4.01 on Win98 has been known to send the
        // "token=" string multiple times.  Thanks to David Wall for this fix.
        int index = line.lastIndexOf("boundary=");
        if (index == -1) {
            return null;
        }
        String token = line.substring(index + 9);  // 9 for "token="
        if (token.charAt(0) == '"') {
            // The token is enclosed in quotes, strip them
            index = token.lastIndexOf('"');
            token = token.substring(1, index);
        }

        // The real token is always preceeded by an extra "--"
        token = "--" + token;

        return token;
    }
}
