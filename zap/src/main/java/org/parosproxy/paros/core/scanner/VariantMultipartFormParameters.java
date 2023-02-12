/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.core.scanner.InputVector;

/**
 * Variant used for "multipart/form-data" POST request handling. Takes all parameters passed inside
 * the form-data structure and sets them for injection.
 */
public class VariantMultipartFormParameters implements Variant {

    private static final Logger LOGGER = LogManager.getLogger(VariantMultipartFormParameters.class);
    private static final Pattern FIELD_NAME_PATTERN =
            Pattern.compile(
                    "\\s*content-disposition\\s*:.*\\s+name\\s*\\=?\\s*\\\"?(?<name>.[^;\\\"\\n]*)\\\"?\\;?.*",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern FIELD_VALUE_PATTERN = Pattern.compile("[\\r\\n]{2}(?<value>.*)");
    private static final Pattern FILENAME_PART_PATTERN =
            Pattern.compile(
                    "\\s*content-disposition\\s*:.*filename\\s*\\=?\\s*\\\"?(?<filename>.[^;\"\\n]*)\\\"?\\;?.*",
                    Pattern.CASE_INSENSITIVE);
    // http://fiddle.re/etxbnd (Click Java, set case insensitive, and hit "test")
    private static final Pattern CONTENTTYPE_PART_PATTERN =
            Pattern.compile(
                    "\\s*content-disposition.*content-type\\s*:\\s*\\s*\\\"?(?<contenttype>.[^;\"\\r\\n]*)\\\"?\\;?.*",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // http://www.regexplanet.com/share/index.html?share=yyyyyythear (Click Java, set case
    // insensitive & DOTALL, and hit "test")

    private List<NameValuePair> params = Collections.emptyList();
    private List<MultipartFormParameter> multiPartParams = new ArrayList<>();

    private static final String SHORT_NAME = "multipart";

    @Override
    public String getShortName() {
        return SHORT_NAME;
    }

    @Override
    public void setMessage(HttpMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException("Parameter message must not be null.");
        }

        String contentType = msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/form-data")) {
            return;
        }

        ArrayList<NameValuePair> extractedParameters = new ArrayList<>();
        int position = 0;
        int offset = 0;
        String boundary = getBoundary(contentType) + HttpHeader.CRLF;
        for (String part : msg.getRequestBody().toString().split(boundary)) {
            if (!StringUtils.isBlank(part)) {
                String partHeaderLine =
                        part.substring(0, part.indexOf(HttpHeader.CRLF + HttpHeader.CRLF));
                boolean isFileParam = partHeaderLine.contains("filename=");
                part = boundary + part;
                Matcher nameMatcher = FIELD_NAME_PATTERN.matcher(partHeaderLine);
                Matcher valueMatcher = FIELD_VALUE_PATTERN.matcher(part);
                nameMatcher.find();
                valueMatcher.find();
                if (StringUtils.isBlank(valueMatcher.group("value"))) {
                    valueMatcher.find();
                    // Need to skip one find for some reason...
                    // https://regex101.com/r/4ig6Wk/1
                    // http://fiddle.re/23cudd (Click Java, hit "test")
                }

                String name = nameMatcher.group("name");
                // Value doesn't include boundary, headerline, or double CRLF
                String value =
                        part.replaceAll(
                                Pattern.quote(boundary + partHeaderLine)
                                        + HttpHeader.CRLF
                                        + HttpHeader.CRLF,
                                "");
                value =
                        value.replaceAll(
                                HttpHeader.CRLF
                                        + "("
                                        + Pattern.quote(getBoundary(contentType))
                                        + "--"
                                        + HttpHeader.CRLF
                                        + ")?$",
                                ""); // Strip final boundary
                if (isFileParam) {
                    position += 2;
                    extractedParameters.add(
                            new NameValuePair(
                                    NameValuePair.TYPE_MULTIPART_DATA_FILE_PARAM,
                                    name,
                                    value,
                                    position));
                } else {
                    extractedParameters.add(
                            new NameValuePair(
                                    NameValuePair.TYPE_MULTIPART_DATA_PARAM,
                                    name,
                                    value,
                                    position));
                }
                int start =
                        offset
                                + part.indexOf(HttpHeader.CRLF + HttpHeader.CRLF)
                                + 4; // 4 for two CRLFs
                int end = start + value.length();
                LOGGER.debug(
                        "Name: {} O: {} S: {} E: {} Pos: {}", name, offset, start, end, position);
                multiPartParams.add(
                        new MultipartFormParameter(
                                name,
                                valueMatcher.group("value"),
                                start,
                                end,
                                position,
                                MultipartFormParameter.Type.GENERAL));
                LOGGER.debug("Name: {} value: {}", name, valueMatcher.group("value"));
                if (isFileParam) {
                    position -= 2;
                    // Extract the filename
                    Matcher fnValueMatcher = FILENAME_PART_PATTERN.matcher(part);
                    fnValueMatcher.find();
                    String fnValue = fnValueMatcher.group("filename");
                    extractedParameters.add(
                            extractedParameters.size() - 1,
                            new NameValuePair(
                                    NameValuePair.TYPE_MULTIPART_DATA_FILE_NAME,
                                    name,
                                    fnValue,
                                    position));
                    int fnStart = offset + part.indexOf(fnValue);
                    int fnEnd = fnStart + fnValue.length();
                    LOGGER.debug(
                            "Name: {} O: {} S: {} E: {} Pos: {}",
                            name,
                            offset,
                            fnStart,
                            fnEnd,
                            position);
                    multiPartParams.add(
                            multiPartParams.size() - 1,
                            new MultipartFormParameter(
                                    name,
                                    fnValue,
                                    fnStart,
                                    fnEnd,
                                    position,
                                    MultipartFormParameter.Type.FILE_NAME));
                    // Extract the content-type
                    Matcher ctValueMatcher = CONTENTTYPE_PART_PATTERN.matcher(part);
                    ctValueMatcher.find();
                    String ctValue = ctValueMatcher.group("contenttype");
                    extractedParameters.add(
                            extractedParameters.size() - 1,
                            new NameValuePair(
                                    NameValuePair.TYPE_MULTIPART_DATA_FILE_CONTENTTYPE,
                                    name,
                                    ctValue,
                                    ++position));
                    int ctStart = offset + part.indexOf(ctValue);
                    int ctEnd = ctStart + ctValue.length();
                    LOGGER.debug(
                            "Name: {} O: {} S: {} E: {} Pos: {}",
                            name,
                            offset,
                            ctStart,
                            ctEnd,
                            position);
                    multiPartParams.add(
                            multiPartParams.size() - 1,
                            new MultipartFormParameter(
                                    name,
                                    ctValue,
                                    ctStart,
                                    ctEnd,
                                    position,
                                    MultipartFormParameter.Type.FILE_CONTENT_TYPE));
                }
            }
            position++;
            offset = offset + part.length();
        }
        params = Collections.unmodifiableList(extractedParameters);
    }

    @Override
    public List<NameValuePair> getParamList() {
        return params;
    }

    @Override
    public String setParameter(
            HttpMessage msg, NameValuePair originalPair, String name, String value) {
        return setParameter(
                msg,
                Collections.singletonList(originalPair.getPosition()),
                Collections.singletonList(value));
    }

    @Override
    public String setEscapedParameter(
            HttpMessage msg, NameValuePair originalPair, String name, String value) {
        return setParameter(
                msg,
                Collections.singletonList(originalPair.getPosition()),
                Collections.singletonList(value));
    }

    @Override
    public void setParameters(HttpMessage msg, List<InputVector> inputVectors) {
        this.setParameter(
                msg,
                inputVectors.stream().map(InputVector::getPosition).collect(Collectors.toList()),
                inputVectors.stream().map(InputVector::getValue).collect(Collectors.toList()));
    }

    private String setParameter(
            HttpMessage msg, List<Integer> nameValuePairPositions, List<String> values) {
        StringBuilder newBodyBuilder = new StringBuilder(msg.getRequestBody().toString());
        int offset = 0;
        for (int index = 0; index < nameValuePairPositions.size(); index++) {
            int originalPosition = nameValuePairPositions.get(index);
            String value = values.get(index);
            int idx = originalPosition - 1;

            MultipartFormParameter mpPart = this.multiPartParams.get(idx);
            LOGGER.debug(
                    "i: {} pos: {} S: {} E: {} O: {}",
                    idx,
                    originalPosition,
                    mpPart.getStart(),
                    mpPart.getEnd(),
                    offset);
            newBodyBuilder.replace(mpPart.getStart() + offset, mpPart.getEnd() + offset, value);
            offset = offset + value.length() - mpPart.getEnd() + mpPart.getStart();
        }
        String newBody = newBodyBuilder.toString();
        msg.getRequestBody().setBody(newBody);
        return newBody;
    }

    private String getBoundary(String contentTypeHeader) {
        int index = contentTypeHeader.lastIndexOf("boundary=");
        if (index == -1) {
            return null;
        }
        String boundary = contentTypeHeader.substring(index + 9); // "boundary=" is 9
        if (boundary.charAt(0) == '"') {
            index = boundary.lastIndexOf('"');
            boundary = boundary.substring(1, index);
        }
        // The real token is always preceded by an extra "--"
        boundary = "--" + boundary;

        return boundary;
    }
}
