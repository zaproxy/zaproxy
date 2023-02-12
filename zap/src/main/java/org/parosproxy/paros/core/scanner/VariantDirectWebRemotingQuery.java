/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.parosproxy.paros.core.scanner;

import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Variant to allow scanning of Direct Web Remoting (DWR) parameters
 *
 * @author 70pointer@gmail.com
 */
public class VariantDirectWebRemotingQuery extends VariantAbstractRPCQuery {

    public static final String DWR_CONTENT_TYPE = "text/plain";

    // parameter names to not scan
    private static final Pattern patternIgnoreScriptName =
            Pattern.compile("c[0-9]+-scriptName", Pattern.CASE_INSENSITIVE);
    private static final Pattern patternIgnoreMethodName =
            Pattern.compile("c[0-9]+-methodName", Pattern.CASE_INSENSITIVE);

    // parameter values to not scan
    private static final Pattern patternIgnoreArray =
            Pattern.compile("Array:\\[.*\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern patternIgnoreObject =
            Pattern.compile("Object_Object:\\{.*\\}", Pattern.CASE_INSENSITIVE);

    // strongly typed parameter values
    private static final Pattern patternNumberValue =
            Pattern.compile("number:.+", Pattern.CASE_INSENSITIVE);
    private static final Pattern patternStringValue =
            Pattern.compile("string:.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern patternBooleanValue =
            Pattern.compile("boolean:.+", Pattern.CASE_INSENSITIVE);
    private static final Pattern patternNullValue =
            Pattern.compile("null:null", Pattern.CASE_INSENSITIVE);

    private static final String SHORT_NAME = "dwr";

    @Override
    public String getShortName() {
        return SHORT_NAME;
    }

    /**
     * @param contentType
     * @return
     */
    @Override
    public boolean isValidContentType(String contentType) {
        return contentType.startsWith(DWR_CONTENT_TYPE);
    }

    @Override
    public String getEscapedValue(String value, boolean toQuote) {
        return StringEscapeUtils.escapeJava(value);
    }

    @Override
    public String getUnescapedValue(String value) {
        return StringEscapeUtils.unescapeJava(value);
    }

    @Override
    public void parseContent(String content) {
        // System.out.println("Getting parameters from ["+content + "]");
        int offie = content.indexOf("\n");
        int accumulatedOffset = 0;
        while (offie > 0) {
            String paramString = content.substring(0, offie);
            String paramDetails[] = paramString.split("=", 2);
            // do not interpret the DWR script or method as parameters to be scanned
            // take care to handle the case where a single POST request contains multiple DWR calls
            // (c0, c1, c2, etc.)
            if ((!patternIgnoreScriptName.matcher(paramDetails[0]).matches())
                    && (!patternIgnoreMethodName.matcher(paramDetails[0]).matches())
                    && paramDetails.length == 2) {
                if (paramDetails[1] == null) paramDetails[1] = "";
                // if the parameter value has one of the following patterns, ignore it.
                // Array:[<<some possible stuff>>]
                // Object_Object:{<<some possible stuff>>}
                if (!patternIgnoreArray.matcher(paramDetails[1]).matches()
                        && !patternIgnoreObject.matcher(paramDetails[1]).matches()) {

                    int valueOffset = 0;
                    String paramValue = paramDetails[1];
                    // if the parameter value has one of the following formats, then adjust the
                    // value offset, so that the "type" of the value is not corrupted.
                    // number:<<some number>>
                    // string:<<some string>>
                    // boolean:<<true or false>>
                    // null:null

                    if (patternNumberValue.matcher(paramDetails[1]).matches()
                            || patternStringValue.matcher(paramDetails[1]).matches()
                            || patternBooleanValue.matcher(paramDetails[1]).matches()
                            || patternNullValue.matcher(paramDetails[1]).matches()) {
                        // the value has a type built in.
                        valueOffset = paramDetails[1].indexOf(":") + 1;
                        paramValue = paramDetails[1].substring(valueOffset);
                    }

                    int beginOffset =
                            accumulatedOffset + paramDetails[0].length() + 1 + valueOffset;
                    int endOffset = accumulatedOffset + offie;
                    // System.out.println("Adding parameter ["+paramDetails[0]+"]=["+paramValue+"],
                    // value offset1:" + beginOffset+ ", value offset2:" + endOffset);
                    addParameter(paramDetails[0], beginOffset, endOffset, false, paramValue);
                }
            }
            accumulatedOffset += (1 + offie); // cater for the \n separator as well.
            content = content.substring(offie + 1);
            offie = content.indexOf("\n");
        }
    }
}
