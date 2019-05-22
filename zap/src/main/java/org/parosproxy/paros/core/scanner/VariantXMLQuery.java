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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Variant specialized
 * 
 * @author andy
 */
public class VariantXMLQuery extends VariantAbstractRPCQuery {

    public static final String XML_CONTENT_TYPE = "text/xml";
    public static final String XML_APP_CONTENT_TYPE = "application/xml";
    public static final String SOAP2_CONTENT_TYPE = "application/soap+xml";
    
    // XML standard from W3C Consortium
    // ---------------------------------------------
    // STag ::= '<' Name (S Attribute)* S? '>'
    // NameStartChar ::= ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D] | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
    // NameChar ::= NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
    // Name ::= NameStartChar (NameChar)*
    // S ::= (0x20 0x09 0x0d 0x0a)+ in Java (\s)
    // Attribute ::= Name Eq AttValue
    // Eq ::= S? '=' S?
    // AttValue ::= '"' ([^<&"] | Reference)* '"' |  "'" ([^<&'] | Reference)* "'"
    // ----------------------------------------------
    private final static String attRegex = "(\\S+)\\s*=\\s*((?:\"[^\"\\&\\<]*\")|(?:'[^'\\&\\<]*'))";
    private final static String tagRegex = "\\<([\\_\\:A-Za-z][\\_\\:A-Za-z0-9\\-\\.]*)\\s*[^\\>]*\\>((?:\\<\\!\\[CDATA\\[(?:.(?<!\\]\\]>))*\\]\\]>)|(?:[^\\<\\&]*))\\<\\/[\\_\\:A-Za-z][\\_\\:A-Za-z0-9\\-\\.]*\\s*\\>";
    private Pattern attPattern = Pattern.compile(attRegex);
    private Pattern tagPattern = Pattern.compile(tagRegex);

    /**
     * 
     * @param contentType
     * @return 
     */
    @Override
    public boolean isValidContentType(String contentType) {
        // Could be generalized finding only the "xml" string
        // but should become too much extended
        return contentType.startsWith(XML_CONTENT_TYPE) || 
               contentType.startsWith(SOAP2_CONTENT_TYPE) || 
               contentType.startsWith(XML_APP_CONTENT_TYPE);
    }

    /**
     * 
     * @param value
     * @param toQuote
     * @return 
     */
    @Override
    public String getEscapedValue(String value, boolean toQuote) {
        return StringEscapeUtils.escapeXml(value);
    }
    
    /**
     * 
     * @param value
     * @return 
     */
    @Override
    public String getUnescapedValue(String value) {
        return StringEscapeUtils.unescapeXml(value);
    }

    /**
     * 
     * @param content 
     */
    @Override
    public void parseContent(String content) {
        Matcher matcher = attPattern.matcher(content);
        String value;
        int bidx;
        int eidx;
        
        while (matcher.find()) {
            bidx = matcher.start(2) + 1;
            eidx = matcher.end(2) - 1;
            addParameter(matcher.group(1), bidx, eidx, false, false);
        }
        
        matcher = tagPattern.matcher(content);
        while (matcher.find()) {
            // if it is a CDATA content dequeue
            // the trailer and the footer from the param string
            value = matcher.group(2);
            bidx = matcher.start(2);
            eidx = matcher.end(2);
                
            if (value.startsWith("<![CDATA[") && value.endsWith("]]>")) {
                value = value.substring(9, value.length() - 3); //<![CDATA[   //]]>
                
            } else {
                value = getUnescapedValue(value);
            }
            
            addParameter(matcher.group(1), bidx, eidx, false, value);
        }
    }
}
