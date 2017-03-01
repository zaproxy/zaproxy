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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andy
 */
public class MultipartParam {

    private final List<String> headers = new ArrayList<>();
    private String name;
    private String fileName;
    private String content;

    /**
     * 
     * @param headerline 
     * @throws java.io.IOException 
     */
    public void addHeader(String headerline) throws IOException {
        headers.add(headerline);
        
        if (headerline.toLowerCase().startsWith("content-disposition:")) {
            // Parse the content-disposition line
            extractDispositionInfo(headerline);
        }
    }

    /**
     * 
     * @return 
     */
    public String getContent() {
        return content;
    }
    
    /**
     * 
     * @param value 
     */
    public void setContent(String value) {
        this.content = value;
    }

    /**
     * 
     * @return 
     */
    public List<String> getHeaders() {
        return headers;
    }

    /**
     * 
     * @return 
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @return 
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 
     * @return 
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (String header : headers) {
            result.append(header);
            result.append("\r\n");
        }
        
        result.append("\r\n");
        result.append(content);
        result.append("\r\n");

        return result.toString();
    }

    /**
      * Extracts and returns disposition info from a line, as a
      * <code>String<code>
      * array with elements: disposition, name, filename.
      *
      * @exception IOException if the line is malformatted.
      */
    private void extractDispositionInfo(String line) throws IOException {
        // Convert the line to a lowercase string without the ending \r\n
        // Keep the original line for error messages and for variable names.
        String origline = line;
        line = origline.toLowerCase();

        // Get the content disposition, should be "form-data"
        int start = line.indexOf("content-disposition: ");
        int end = line.indexOf(";");
        if (start == -1 || end == -1) {
            throw new IOException("Content disposition corrupt: " + origline);
        }
        String disposition = line.substring(start + 21, end).trim();
        if (!disposition.equals("form-data")) {
            throw new IOException("Invalid content disposition: " + disposition);
        }

        // Get the field name
        start = line.indexOf("name=\"", end);  // start at last semicolon
        end = line.indexOf("\"", start + 7);   // skip name=\"
        int startOffset = 6;
        if (start == -1 || end == -1) {
            // Some browsers like lynx don't surround with ""
            // Thanks to Deon van der Merwe, dvdm@truteq.co.za, for noticing
            start = line.indexOf("name=", end);
            end = line.indexOf(";", start + 6);
            if (start == -1) {
                throw new IOException("Content disposition corrupt: " + origline);

            } else if (end == -1) {
                end = line.length();
            }

            startOffset = 5;  // without quotes we have one fewer char to skip
        }

        name = origline.substring(start + startOffset, end);

        // Get the filename, if given
        String filename = null;
        String origname = null;
        start = line.indexOf("filename=\"", end + 2);  // start after name
        end = line.indexOf("\"", start + 10);          // skip filename=\"
        if (start != -1 && end != -1) {                // note the !=
            filename = origline.substring(start + 10, end);
            origname = filename;
            // The filename may contain a full path.  Cut to just the filename.
            int slash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
            if (slash > -1) {
                filename = filename.substring(slash + 1);  // past last slash
            }
        }

        fileName = filename;
    }

    /**
     * Extracts and returns the content type from a line, or null if the line
     * was empty.
     *
     * @return content type, or null if line was empty.
     * @exception IOException if the line is malformatted.
     */
    private static String extractContentType(String line) throws IOException {
        // Convert the line to a lowercase string
        line = line.toLowerCase();

        // Get the content type, if any
        // Note that Opera at least puts extra info after the type, so handle
        // that.  For example:  Content-Type: text/plain; name="foo"
        // Thanks to Leon Poyyayil, leon.poyyayil@trivadis.com, for noticing this.
        int end = line.indexOf(";");
        if (end == -1) {
            end = line.length();
        }

        return line.substring(13, end).trim();  // "content-type:" is 13
    }
}
