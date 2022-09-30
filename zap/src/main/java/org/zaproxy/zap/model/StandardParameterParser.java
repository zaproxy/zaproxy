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
package org.zaproxy.zap.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HtmlParameter.Type;
import org.parosproxy.paros.network.HttpMessage;

public class StandardParameterParser implements ParameterParser {

    private static final String CONFIG_KV_PAIR_SEPARATORS = "kvps";
    private static final String CONFIG_KV_SEPARATORS = "kvs";
    private static final String CONFIG_STRUCTURAL_PARAMS = "struct";

    private static final String DEFAULT_KV_PAIR_SEPARATOR = "&";
    private static final String DEFAULT_KV_SEPARATOR = "=";

    private Context context;
    private Pattern keyValuePairSeparatorPattern;
    private Pattern keyValueSeparatorPattern;
    private String keyValuePairSeparators;
    private String keyValueSeparators;
    private List<String> structuralParameters = new ArrayList<>();

    private static Logger log = LogManager.getLogger(StandardParameterParser.class);

    public StandardParameterParser(String keyValuePairSeparators, String keyValueSeparators)
            throws PatternSyntaxException {
        super();
        this.setKeyValuePairSeparators(keyValuePairSeparators);
        this.setKeyValueSeparators(keyValueSeparators);
    }

    public StandardParameterParser() {
        this(DEFAULT_KV_PAIR_SEPARATOR, DEFAULT_KV_SEPARATOR);
    }

    private Pattern getKeyValuePairSeparatorPattern() {
        return this.keyValuePairSeparatorPattern;
    }

    private Pattern getKeyValueSeparatorPattern() {
        return this.keyValueSeparatorPattern;
    }

    @Override
    public void init(String config) {
        if (config == null || config.isEmpty()) {
            setKeyValuePairSeparators(DEFAULT_KV_PAIR_SEPARATOR);
            setKeyValueSeparators(DEFAULT_KV_SEPARATOR);
            structuralParameters.clear();
            return;
        }

        try {
            JSONObject json = JSONObject.fromObject(config);
            this.setKeyValuePairSeparators(json.getString(CONFIG_KV_PAIR_SEPARATORS));
            this.setKeyValueSeparators(json.getString(CONFIG_KV_SEPARATORS));
            JSONArray ja = json.getJSONArray(CONFIG_STRUCTURAL_PARAMS);
            for (Object obj : ja.toArray()) {
                this.structuralParameters.add(obj.toString());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String getConfig() {
        JSONObject json = new JSONObject();
        json.put(CONFIG_KV_PAIR_SEPARATORS, this.getKeyValuePairSeparators());
        json.put(CONFIG_KV_SEPARATORS, this.getKeyValueSeparators());

        JSONArray ja = new JSONArray();
        ja.addAll(this.structuralParameters);
        json.put(CONFIG_STRUCTURAL_PARAMS, ja);

        return json.toString();
    }

    /**
     * @deprecated 2.10.0 use #getParameters(String) This method will lose duplicated parameter
     *     names
     */
    @Deprecated
    @Override
    public Map<String, String> getParams(HttpMessage msg, HtmlParameter.Type type) {
        if (msg == null) {
            return new HashMap<>();
        }
        switch (type) {
            case form:
                return this.parse(msg.getRequestBody().toString());
            case url:
                return convertParametersList(
                        parseParameters(msg.getRequestHeader().getURI().getEscapedQuery()));
            default:
                throw new InvalidParameterException("Type not supported: " + type);
        }
    }

    /**
     * Converts the given {@code List} of parameters to a {@code Map}.
     *
     * <p>The names of parameters are used as keys (mapping to corresponding value) thus removing
     * any duplicated parameters. It is used an empty {@code String} for the mapping, if the
     * parameter has no value ({@code null}).
     *
     * @param parameters the {@code List} to be converted, must not be {@code null}
     * @return a {@code Map} containing the parameters
     */
    private static Map<String, String> convertParametersList(List<NameValuePair> parameters) {
        Map<String, String> map = new HashMap<>();
        for (NameValuePair parameter : parameters) {
            String value = parameter.getValue();
            if (value == null) {
                value = "";
            }
            map.put(parameter.getName(), value);
        }
        return map;
    }

    /**
     * @throws IllegalArgumentException if any of the parameters is {@code null} or if the given
     *     {@code type} is not {@link org.parosproxy.paros.network.HtmlParameter.Type#url url} or
     *     {@link org.parosproxy.paros.network.HtmlParameter.Type#form form}.
     */
    @Override
    public List<NameValuePair> getParameters(HttpMessage msg, Type type) {
        if (msg == null) {
            throw new IllegalArgumentException("Parameter msg must not be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Parameter type must not be null.");
        }

        switch (type) {
            case form:
                return parseParameters(msg.getRequestBody().toString());
            case url:
                String query = msg.getRequestHeader().getURI().getEscapedQuery();
                if (query == null) {
                    return new ArrayList<>(0);
                }
                return parseParameters(query);
            default:
                throw new IllegalArgumentException("The provided type is not supported: " + type);
        }
    }

    private void setKeyValueSeparatorPattern(Pattern keyValueSeparatorPattern) {
        this.keyValueSeparatorPattern = keyValueSeparatorPattern;
    }

    private void setKeyValuePairSeparatorPattern(Pattern keyValuePairSeparatorPattern) {
        this.keyValuePairSeparatorPattern = keyValuePairSeparatorPattern;
    }

    public String getKeyValuePairSeparators() {
        return keyValuePairSeparators;
    }

    public void setKeyValuePairSeparators(String keyValuePairSeparators)
            throws PatternSyntaxException {
        this.setKeyValuePairSeparatorPattern(Pattern.compile("[" + keyValuePairSeparators + "]"));
        this.keyValuePairSeparators = keyValuePairSeparators;
    }

    public String getKeyValueSeparators() {
        return keyValueSeparators;
    }

    public void setKeyValueSeparators(String keyValueSeparators) throws PatternSyntaxException {
        this.setKeyValueSeparatorPattern(Pattern.compile("[" + keyValueSeparators + "]"));
        this.keyValueSeparators = keyValueSeparators;
    }

    @Override
    public String getDefaultKeyValuePairSeparator() {
        if (this.keyValuePairSeparators != null && this.keyValuePairSeparators.length() > 0) {
            return this.keyValuePairSeparators.substring(0, 1);
        }
        return DEFAULT_KV_PAIR_SEPARATOR;
    }

    @Override
    public String getDefaultKeyValueSeparator() {
        if (this.keyValueSeparators != null && this.keyValueSeparators.length() > 0) {
            return this.keyValueSeparators.substring(0, 1);
        }
        return DEFAULT_KV_SEPARATOR;
    }

    public List<String> getStructuralParameters() {
        return Collections.unmodifiableList(structuralParameters);
    }

    public void setStructuralParameters(List<String> structuralParameters) {
        this.structuralParameters.clear();
        this.structuralParameters.addAll(structuralParameters);
    }

    /**
     * @deprecated 2.10.0 use #parseParameters(String) This method will lose duplicated parameter
     *     names
     */
    @Deprecated
    @Override
    public Map<String, String> parse(String paramStr) {
        Map<String, String> map = new HashMap<>();

        if (paramStr != null) {
            String[] keyValue = this.getKeyValuePairSeparatorPattern().split(paramStr);
            for (String s : keyValue) {
                try {
                    String[] keyEqValue = this.getKeyValueSeparatorPattern().split(s);
                    if (keyEqValue.length == 1) {
                        map.put(keyEqValue[0], "");
                    } else if (keyEqValue.length > 1) {
                        map.put(keyEqValue[0], keyEqValue[1]);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return map;
    }

    @Override
    public List<NameValuePair> parseParameters(String parameters) {
        return createParameters(
                parameters,
                (name, value) -> {
                    String decodedName = urlDecode(name);
                    String decodedValue = value != null ? urlDecode(value) : "";
                    return new DefaultNameValuePair(decodedName, decodedValue);
                });
    }

    private List<NameValuePair> createParameters(
            String parameters, BiFunction<String, String, NameValuePair> nameValuePairFactory) {
        if (parameters == null) {
            return new ArrayList<>(0);
        }

        List<NameValuePair> parametersList = new ArrayList<>();
        String[] pairs = getKeyValuePairSeparatorPattern().split(parameters);
        for (String pair : pairs) {
            String[] nameValuePair = getKeyValueSeparatorPattern().split(pair, 2);
            String name = nameValuePair[0];
            String value = nameValuePair.length == 1 ? null : nameValuePair[1];
            parametersList.add(nameValuePairFactory.apply(name, value));
        }
        return parametersList;
    }

    private static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (IllegalArgumentException e) {
            return value;
        } catch (UnsupportedEncodingException ignore) {
            // Shouldn't happen UTF-8 is a standard charset (see java.nio.charset.StandardCharsets)
        }
        return "";
    }

    @Override
    public List<NameValuePair> parseRawParameters(String parameters) {
        return createParameters(parameters, DefaultNameValuePair::new);
    }

    @Override
    public StandardParameterParser clone() {
        StandardParameterParser spp =
                new StandardParameterParser(
                        this.getKeyValuePairSeparators(), this.getKeyValueSeparators());
        spp.setStructuralParameters(this.getStructuralParameters());
        return spp;
    }

    @Override
    public List<String> getTreePath(URI uri) throws URIException {
        return this.getTreePath(uri, true);
    }

    private List<String> getTreePath(URI uri, boolean incStructParams) throws URIException {
        String path = uri.getPath();
        List<String> list = new ArrayList<>();
        if (path != null) {
            Context context = this.getContext();
            if (context != null) {
                String uriStr = uri.toString();
                boolean changed = false;
                for (StructuralNodeModifier ddn : context.getDataDrivenNodes()) {
                    Matcher m = ddn.getPattern().matcher(uriStr);
                    if (m.find()) {
                        if (m.groupCount() == 3) {
                            path =
                                    m.group(1)
                                            + SessionStructure.DATA_DRIVEN_NODE_PREFIX
                                            + ddn.getName()
                                            + SessionStructure.DATA_DRIVEN_NODE_POSTFIX
                                            + m.group(3);
                            if (!path.startsWith("/")) {
                                // Should always start with a slash;)
                                path = "/" + path;
                            }
                            changed = true;
                        } else if (m.groupCount() == 2) {
                            path =
                                    m.group(1)
                                            + SessionStructure.DATA_DRIVEN_NODE_PREFIX
                                            + ddn.getName()
                                            + SessionStructure.DATA_DRIVEN_NODE_POSTFIX;
                            if (!path.startsWith("/")) {
                                // Should always start with a slash;)
                                path = "/" + path;
                            }
                            changed = true;
                        }
                    }
                }
                if (changed) {
                    log.debug("Changed path from {} to {}", uri.getPath(), path);
                }
            }

            // Note: Start from the 2nd path element as the first on is always the empty string due
            // to the split
            String[] pathList = path.split("/");
            for (int i = 1; i < pathList.length; i++) {
                list.add(pathList[i]);
            }
            if (path.endsWith("/")) {
                list.add("/");
            }
        }
        if (incStructParams) {
            // Add any structural params (url param) in key order
            Map<String, String> urlParams =
                    convertParametersList(parseParameters(uri.getEscapedQuery()));
            List<String> keys = new ArrayList<>(urlParams.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                if (this.structuralParameters.contains(key)) {
                    list.add(urlParams.get(key));
                }
            }
        }

        return list;
    }

    @Override
    public List<String> getTreePath(HttpMessage msg) throws URIException {
        URI uri = msg.getRequestHeader().getURI();

        List<String> list = getTreePath(uri);

        // Add any structural params (form params) in key order
        List<NameValuePair> formParams = this.parseParameters(msg.getRequestBody().toString());
        formParams.stream()
                .map(NameValuePair::getName)
                .filter(structuralParameters::contains)
                .sorted()
                .forEach(list::add);
        return list;
    }

    @Override
    public String getAncestorPath(URI uri, int depth) throws URIException {
        // If the depth is 0, return an empty path
        String path = uri.getPath();
        if (depth == 0 || path == null) {
            return "";
        }
        List<String> pathList = getTreePath(uri, false);

        // Add the 'normal' (plus data driven) path elements
        // until we finish them or we reach the desired depth
        StringBuilder parentPath = new StringBuilder(path.length());
        for (int i = 0; i < pathList.size() && depth > 0; i++, depth--) {
            String element = pathList.get(i);
            parentPath.append('/');
            if (element.startsWith(SessionStructure.DATA_DRIVEN_NODE_PREFIX)) {
                // Its a data driven node - use the regex pattern instead
                parentPath.append(SessionStructure.DATA_DRIVEN_NODE_REGEX);
            } else {
                parentPath.append(element);
            }
        }
        // If we're done or we have no structural parameters, just return
        if (depth == 0 || structuralParameters.isEmpty()) {
            return parentPath.toString();
        }

        // Add the 'structural params' path elements
        boolean firstElement = true;
        Map<String, String> urlParams =
                convertParametersList(parseParameters(uri.getEscapedQuery()));
        for (Entry<String, String> param : urlParams.entrySet()) {
            if (this.structuralParameters.contains(param.getKey())) {
                if (firstElement) {
                    firstElement = false;
                    parentPath.append('?');
                } else {
                    parentPath.append(keyValuePairSeparators);
                }
                parentPath
                        .append(param.getKey())
                        .append(keyValueSeparators)
                        .append(param.getValue());
                if ((--depth) == 0) {
                    break;
                }
            }
        }
        return parentPath.toString();
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }
}
