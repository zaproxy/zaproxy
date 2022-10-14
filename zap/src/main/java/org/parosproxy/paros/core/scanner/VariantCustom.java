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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;
import org.zaproxy.zap.model.SessionStructure;

/**
 * Custom Variant based on an implemented Script
 *
 * @author yhawke (2014)
 */
public class VariantCustom implements Variant {

    ExtensionScript extension = null;
    private ScriptWrapper wrapper = null;
    private VariantScript script = null;
    private final List<NameValuePair> params = new ArrayList<>();
    private NameValuePair currentParam;

    // base64 strings are similar, except they can contain + and /, and end
    // with 0 - 2 '=' signs. They are also a multiple of 4 bytes.
    private static final Pattern BASE64_PATTERN = Pattern.compile("^[a-zA-Z0-9/+]+={0,2}$");
    private static final String SHORT_NAME = "script";

    @Override
    public String getShortName() {
        return SHORT_NAME;
    }

    /**
     * Create a new Custom Variant using the specific script
     *
     * @param wrapper the script wrapper that need to be set for this Variant
     * @param extension the general extension Script object
     */
    public VariantCustom(ScriptWrapper wrapper, ExtensionScript extension) {
        this.wrapper = wrapper;
        this.extension = extension;
        if (wrapper != null && extension != null && wrapper.isEnabled()) {
            try {
                this.script = extension.getInterface(wrapper, VariantScript.class);

                if (script == null) {
                    extension.handleFailedScriptInterface(
                            wrapper,
                            Constant.messages.getString(
                                    "variant.scripts.interface.variant.error", wrapper.getName()));
                }
            } catch (Exception ex) {
                // Catch Exception instead of ScriptException and IOException because script engine
                // implementations
                // might throw other exceptions on script errors (e.g.
                // jdk.nashorn.internal.runtime.ECMAException)
                this.extension.handleScriptException(wrapper, ex);
            }
        }
    }

    /**
     * Constructs a {@code VariantCustom} with the given values.
     *
     * @param wrapper the script wrapper.
     * @param script the script.
     * @param extension the script extension.
     */
    public VariantCustom(ScriptWrapper wrapper, VariantScript script, ExtensionScript extension) {
        this.wrapper = wrapper;
        this.script = script;
        this.extension = extension;
    }

    /**
     * Set the current message that this Variant has to scan
     *
     * @param msg the message object (remember Response is not set)
     */
    @Override
    public void setMessage(HttpMessage msg) {
        try {
            if (script != null) {
                script.parseParameters(this, msg);
            }

        } catch (Exception e) {
            // Catch Exception instead of ScriptException because script engine implementations
            // might
            // throw other exceptions on script errors (e.g.
            // jdk.nashorn.internal.runtime.ECMAException)
            extension.handleScriptException(wrapper, e);
        }
    }

    /**
     * Give back the list of retrieved parameters
     *
     * @return the list of parsed parameters
     */
    @Override
    public List<NameValuePair> getParamList() {
        return params;
    }

    /**
     * Support method to get back the name of the n-th parameter
     *
     * @param index the index of the requested parameter
     * @return the parameter name if exists
     */
    public String getParamName(int index) {
        return (index < params.size()) ? params.get(index).getName() : null;
    }

    /**
     * Support method to get back the value of the n-th parameter
     *
     * @param index the index of the requested parameter
     * @return the parameter value if exists
     */
    public String getParamValue(int index) {
        return (index < params.size()) ? params.get(index).getValue() : null;
    }

    /**
     * Get the number of parameters currently available for this variant
     *
     * @return
     */
    public int getParamNumber() {
        return params.size();
    }

    /**
     * Gets the current parameter being tested.
     *
     * @return the parameter being tested, or {@code null} if none.
     * @since 2.8.0
     */
    public NameValuePair getCurrentParam() {
        return currentParam;
    }

    /**
     * Support method to add a new param to this custom variant
     *
     * @param name the param name
     * @param value the value of this parameter
     * @param type the type of this parameter
     */
    public void addParam(String name, String value, int type) {
        // Current size usually is equal to the position
        params.add(new NameValuePair(type, name, value, params.size()));
    }

    /**
     * Support method to add a new QueryString param to this custom variant
     *
     * @param name the param name
     * @param value the value of this parameter
     */
    public void addParamQuery(String name, String value) {
        addParam(name, value, NameValuePair.TYPE_QUERY_STRING);
    }

    /**
     * Support method to add a new PostData param to this custom variant
     *
     * @param name the param name
     * @param value the value of this parameter
     */
    public void addParamPost(String name, String value) {
        addParam(name, value, NameValuePair.TYPE_POST_DATA);
    }

    /**
     * Support method to add a new Header param to this custom variant
     *
     * @param name the param name
     * @param value the value of this parameter
     */
    public void addParamHeader(String name, String value) {
        addParam(name, value, NameValuePair.TYPE_HEADER);
    }

    /**
     * Support method to encode a string to Base64
     *
     * @param value the value that need to be encoded
     * @return the encoded string
     */
    public String encodeBase64(String value) {
        return Base64.encodeBase64String(value.getBytes());
    }

    /**
     * Support method to decode a Base64 string
     *
     * @param value the value that need to be decoded
     * @return the decoded string
     */
    public String decodeBase64(String value) {
        return new String(Base64.decodeBase64(value));
    }

    /**
     * Support method to verify if the content is a Base64 string
     *
     * @param value the value that need to be checked
     * @return true if the value is a Base64 string
     */
    public boolean isBase64(String value) {
        return (BASE64_PATTERN.matcher(value).matches() && ((value.length() % 4) == 0));
    }

    @Override
    public String setParameter(
            HttpMessage msg, NameValuePair originalPair, String param, String value) {
        return setParameter(msg, originalPair, param, value, false);
    }

    @Override
    public String setEscapedParameter(
            HttpMessage msg, NameValuePair originalPair, String param, String value) {
        return setParameter(msg, originalPair, param, value, true);
    }

    /**
     * The standard name given to nodes in the sites tree for the given parameters
     *
     * @param nodeName the name of the node, typically the last element of the path
     * @param msg the message
     * @param params the url and post parameters for the given message
     * @return the name to be used in the Sites Map
     */
    public String getStandardLeafName(
            String nodeName, HttpMessage msg, List<NameValuePair> params) {
        return SessionStructure.getLeafName(nodeName, msg, params);
    }

    /**
     * Inner method for correct scripting
     *
     * @param msg the message that need to be modified
     * @param originalPair the original {@code NameValuePair} being tested.
     * @param paramName the name of the parameter
     * @param value the value that should be set for this parameter
     * @param escaped true if the parameter has been already escaped
     * @return the value set as parameter
     */
    private String setParameter(
            HttpMessage msg,
            NameValuePair originalPair,
            String paramName,
            String value,
            boolean escaped) {
        try {
            if (script != null) {
                currentParam = originalPair;
                script.setParameter(this, msg, paramName, value, escaped);
            }

        } catch (Exception e) {
            // Catch Exception instead of ScriptException because script engine implementations
            // might
            // throw other exceptions on script errors (e.g.
            // jdk.nashorn.internal.runtime.ECMAException)
            extension.handleScriptException(wrapper, e);
        } finally {
            currentParam = null;
        }

        return value;
    }

    @Override
    public String getLeafName(String nodeName, HttpMessage msg) {
        if (script != null) {
            try {
                return this.script.getLeafName(this, nodeName, msg);
            } catch (Exception e) {
                extension.handleScriptException(wrapper, e);
            }
        }
        return null;
    }

    @Override
    public List<String> getTreePath(HttpMessage msg) {
        if (script != null) {
            try {
                return this.script.getTreePath(this, msg);
            } catch (Exception e) {
                extension.handleScriptException(wrapper, e);
            }
        }
        return null;
    }
}
