/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * Copyright 2014 Jay Ball - Aspect Security
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
package org.zaproxy.zap.extension.globalexcludeurl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

/** TODO The GlobalExcludeURL functionality is currently alpha and subject to change.  */
public class GlobalExcludeURLParam extends AbstractParam {

    private static final Logger logger = Logger.getLogger(GlobalExcludeURLParam.class);

    private static final String GLOBAL_EXCLUDE_URL_BASE_KEY = "globalexcludeurl";
    
    private static final String ALL_TOKENS_KEY = GLOBAL_EXCLUDE_URL_BASE_KEY + ".url_list.url";
    
    private static final String TOKEN_NAME_KEY = "regex";
    private static final String TOKEN_ENABLED_KEY = "enabled";
    private static final String TOKEN_DESCRIPTION_KEY = "description";

    private static final String CONFIRM_REMOVE_TOKEN_KEY = GLOBAL_EXCLUDE_URL_BASE_KEY + ".confirmRemoveToken";
    
    // Remember, these are regexs, so escape properly \\ vs \
    // Also, http://regexpal.com/ for quick testing.
    // The file formats are common types, not inclusive of all types. Remember, more == slower;
    // complex == slower. Don't overload with every obscure image/audio/video format in existence.
    private static final String[] DEFAULT_TOKENS_NAMES = { 
        "^.*\\.(gif|jpe?g|png|ico|icns|bmp)$",
        "^.*\\.(mp[34]|mpe?g|m4[ap]|aac|avi|mov|wmv|og[gav])$",
        "^.*\\.(pdf|docx?|xlsx?|pptx?)$",
        "^.*\\.(css|js)$",
        "^.*\\.(sw[fa]|flv)$",
        "^https?://(safebrowsing-cache|sb-ssl|sb|safebrowsing\\.clients)\\.google\\.com",
        "^https?://([^/])*\\.?lastpass\\.com",
        "^https?://(.*addons|au[0-9])\\.mozilla\\.(org|net|com)",
        "^https?://([^/])*\\.?(getfoxyproxy\\.org|getfirebug\\.com|noscript\\.net)"
    };

    // XXX these must be in the same order as above - there are better ways to implement this.  
    // XXX This will crash if array lengths not equal.
    private static final String[] DEFAULT_TOKENS_DESCRIPTIONS = { 
        "Image (ends with .ext)",
        "Audio/Video (ends with .ext)",
        "PDF & MS Office (ends with .ext)",
        "Stylesheet, JavaScript (ends with .ext)",
        "Flash & related (ends with .ext)",
        "Google malware detector updates",
        "Lastpass manager",
        "Firefox browser updates",
        "Firefox extensions phoning home"
    };

    private List<GlobalExcludeURLParamToken> tokens = null;
    private List<String> enabledTokensNames = null;
    
    private boolean confirmRemoveToken = true;

    public GlobalExcludeURLParam() {
    }

    @Override
    protected void parse() {
        try {
            List<HierarchicalConfiguration> fields = ((HierarchicalConfiguration) getConfig()).configurationsAt(ALL_TOKENS_KEY);
            this.tokens = new ArrayList<>(fields.size());
            enabledTokensNames = new ArrayList<>(fields.size());
            List<String> tempTokensNames = new ArrayList<>(fields.size());
            for (HierarchicalConfiguration sub : fields) {
                String name = sub.getString(TOKEN_NAME_KEY, "");
                if (!"".equals(name) && !tempTokensNames.contains(name)) {
                    boolean enabled = sub.getBoolean(TOKEN_ENABLED_KEY, true);
                    String desc = sub.getString(TOKEN_DESCRIPTION_KEY, "");
                    this.tokens.add(new GlobalExcludeURLParamToken(name, desc, enabled));
                    tempTokensNames.add(name);
                    if (enabled) {
                        enabledTokensNames.add(name);
                    }
                }
            }
        } catch (ConversionException e) {
            logger.error("Error while loading Global Exclude URL tokens: " + e.getMessage(), e);
            this.tokens = new ArrayList<>(DEFAULT_TOKENS_NAMES.length);
            this.enabledTokensNames = new ArrayList<>(DEFAULT_TOKENS_NAMES.length);
        }
        
        if (this.tokens.size() == 0) {
            int i = 0;
            for (String tokenName : DEFAULT_TOKENS_NAMES) {
                String description = DEFAULT_TOKENS_DESCRIPTIONS[i];
                // By default, don't enable all of the DEFAULT_TOKENS_NAMES, let the user choose.
                this.tokens.add(new GlobalExcludeURLParamToken(tokenName, description, false));
                i++;
            }
        }

        try {
            this.confirmRemoveToken = getConfig().getBoolean(CONFIRM_REMOVE_TOKEN_KEY, true);
        } catch (ConversionException e) {
            logger.error("Error while loading the confirm remove token option: " + e.getMessage(), e);
        }
    }

    public List<GlobalExcludeURLParamToken> getTokens() {
        return tokens;
    }

    public void setTokens(List<GlobalExcludeURLParamToken> tokens) {
        this.tokens = new ArrayList<>(tokens);
        
        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_TOKENS_KEY);

        ArrayList<String> enabledTokens = new ArrayList<>(tokens.size());
        for (int i = 0, size = tokens.size(); i < size; ++i) {
            String elementBaseKey = ALL_TOKENS_KEY + "(" + i + ").";
            GlobalExcludeURLParamToken token = tokens.get(i);
            
            getConfig().setProperty(elementBaseKey + TOKEN_NAME_KEY, token.getName());
            getConfig().setProperty(elementBaseKey + TOKEN_DESCRIPTION_KEY, token.getDescription());
            getConfig().setProperty(elementBaseKey + TOKEN_ENABLED_KEY, Boolean.valueOf(token.isEnabled()));
            
            if (token.isEnabled()) {
                enabledTokens.add(token.getName());
            }
        }
        
        enabledTokens.trimToSize();
        this.enabledTokensNames = enabledTokens;
    }

    public void addToken(String name) {
        this.tokens.add(new GlobalExcludeURLParamToken(name));
        
        this.enabledTokensNames.add(name);
    }

    public void removeToken(String name) {
        this.tokens.remove(new GlobalExcludeURLParamToken(name));
        
        this.enabledTokensNames.remove(name);
    }

    public List<String> getTokensNames() {
        return enabledTokensNames;
    }
    
    public boolean isConfirmRemoveToken() {
        return this.confirmRemoveToken;
    }
    
    public void setConfirmRemoveToken(boolean confirmRemove) {
        this.confirmRemoveToken = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_TOKEN_KEY, Boolean.valueOf(confirmRemoveToken));
    }

}
