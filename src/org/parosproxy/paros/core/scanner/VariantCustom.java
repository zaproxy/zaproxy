/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development team
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
import javax.script.ScriptException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;

/**
 * Custom Variant based on an implemented Script
 * 
 * @author yhawke (2014)
 */
public class VariantCustom implements Variant {
    
    ExtensionScript extension = null;
    private ScriptWrapper wrapper = null;
    private VariantScript script = null; 
    private List<NameValuePair> params = new ArrayList<>();

    /**
     * Create a new Custom Variant using the specific script
     * @param wrapper the script wrapper that need to be set for this Variant
     * @param extension the general extension Script object
     */
    public VariantCustom(ScriptWrapper wrapper, ExtensionScript extension) {
        this.wrapper = wrapper;
        this.extension = extension;
        if (wrapper != null && extension != null && wrapper.isEnabled()) {
            try {
                this.script = wrapper.getInterface(VariantScript.class);

            } catch (ScriptException | IOException ex) {
                this.extension.setError(wrapper, ex);
                this.extension.setEnabled(wrapper, false);
                this.script = null;
            }
        }
    }

    /**
     * Set the current message that this Variant has to scan
     * @param msg the message object (remember Response is not set)
     */
    @Override
    public void setMessage(HttpMessage msg) {
	try {
            if (script != null) {
                script.parseParameters(msg, params);
            }
            
        } catch (ScriptException se) {
            extension.setError(wrapper, se);
            extension.setEnabled(wrapper, false);
        }
    }

    /**
     * Give back the list of retrieved parameters
     * @return the list of parsed parameters
     */
    @Override
    public List<NameValuePair> getParamList() {
        return params;
    }

    @Override
    public String setParameter(HttpMessage msg, NameValuePair originalPair, String param, String value) {
        return setParameter(msg, param, value, false);
    }

    @Override
    public String setEscapedParameter(HttpMessage msg, NameValuePair originalPair, String param, String value) {
        return setParameter(msg, param, value, true);
    }    

    /**
     * Inner method for correct scripting
     * @param msg the message that need to be modified
     * @param paramName the name of the parameter
     * @param value the value thta should be set for this parameter
     * @param escaped true if the parameter has been already escaped
     * @return the value set as parameter
     */
    private String setParameter(HttpMessage msg, String paramName, String value, boolean escaped) {
	try {
            if (script != null) {
                script.setParameter(msg, paramName, value, escaped);
            }
                        
        } catch (ScriptException se) {
            extension.setError(wrapper, se);
            extension.setEnabled(wrapper, false);
        }
        
        return value;
    }
}
