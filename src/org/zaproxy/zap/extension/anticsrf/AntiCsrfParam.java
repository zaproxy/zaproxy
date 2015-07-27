/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.extension.anticsrf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.zaproxy.zap.extension.api.ZapApiIgnore;

public class AntiCsrfParam extends AbstractParam {

    private static final Logger logger = Logger.getLogger(AntiCsrfParam.class);

    private static final String ANTI_CSRF_BASE_KEY = "anticsrf";
    
    private static final String ALL_TOKENS_KEY = ANTI_CSRF_BASE_KEY + ".tokens.token";
    
    private static final String TOKEN_NAME_KEY = "name";
    private static final String TOKEN_ENABLED_KEY = "enabled";
    
    private static final String CONFIRM_REMOVE_TOKEN_KEY = ANTI_CSRF_BASE_KEY + ".confirmRemoveToken";
    
    private static final String[] DEFAULT_TOKENS_NAMES = { "anticsrf",
            "CSRFToken", "__RequestVerificationToken", "csrfmiddlewaretoken", "authenticity_token" };

    private List<AntiCsrfParamToken> tokens = null;
    private List<String> enabledTokensNames = null;
    
    private boolean confirmRemoveToken = true;

    public AntiCsrfParam() {
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
                    this.tokens.add(new AntiCsrfParamToken(name, enabled));
                    tempTokensNames.add(name);
                    if (enabled) {
                        enabledTokensNames.add(name);
                    }
                }
            }
        } catch (ConversionException e) {
            logger.error("Error while loading anti CSRF tokens: " + e.getMessage(), e);
            this.tokens = new ArrayList<>(DEFAULT_TOKENS_NAMES.length);
            this.enabledTokensNames = new ArrayList<>(DEFAULT_TOKENS_NAMES.length);
        }
        
        if (this.tokens.size() == 0) {
            for (String tokenName : DEFAULT_TOKENS_NAMES) {
                this.tokens.add(new AntiCsrfParamToken(tokenName));
                this.enabledTokensNames.add(tokenName);
            }
        }

        try {
            this.confirmRemoveToken = getConfig().getBoolean(CONFIRM_REMOVE_TOKEN_KEY, true);
        } catch (ConversionException e) {
            logger.error("Error while loading the confirm remove token option: " + e.getMessage(), e);
        }
    }

    @ZapApiIgnore
    public List<AntiCsrfParamToken> getTokens() {
        return tokens;
    }

    @ZapApiIgnore
    public void setTokens(List<AntiCsrfParamToken> tokens) {
        this.tokens = new ArrayList<>(tokens);
        
        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_TOKENS_KEY);

        ArrayList<String> enabledTokens = new ArrayList<>(tokens.size());
        for (int i = 0, size = tokens.size(); i < size; ++i) {
            String elementBaseKey = ALL_TOKENS_KEY + "(" + i + ").";
            AntiCsrfParamToken token = tokens.get(i);
            
            getConfig().setProperty(elementBaseKey + TOKEN_NAME_KEY, token.getName());
            getConfig().setProperty(elementBaseKey + TOKEN_ENABLED_KEY, Boolean.valueOf(token.isEnabled()));
            
            if (token.isEnabled()) {
                enabledTokens.add(token.getName());
            }
        }
        
        enabledTokens.trimToSize();
        this.enabledTokensNames = enabledTokens;
    }

    /**
     * Adds a new token with the given {@code name}, enabled by default.
     * <p>
     * The call to this method has no effect if the given {@code name} is null or empty, or a token with the given name already
     * exist.
     *
     * @param name the name of the token that will be added
     */
    public void addToken(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }

        for (Iterator<AntiCsrfParamToken> it = tokens.iterator(); it.hasNext();) {
            if (name.equals(it.next().getName())) {
                return;
            }
        }

        this.tokens.add(new AntiCsrfParamToken(name));
        
        this.enabledTokensNames.add(name);
    }

    /**
     * Removes the token with the given {@code name}.
     * <p>
     * The call to this method has no effect if the given {@code name} is null or empty, or a token with the given {@code name}
     * does not exist.
     *
     * @param name the name of the token that will be removed
     */
    public void removeToken(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }

        for (Iterator<AntiCsrfParamToken> it = tokens.iterator(); it.hasNext();) {
            AntiCsrfParamToken token = it.next();
            if (name.equals(token.getName())) {
                it.remove();
                if (token.isEnabled()) {
                    this.enabledTokensNames.remove(name);
                }
                break;
            }
        }
    }

    public List<String> getTokensNames() {
        return enabledTokensNames;
    }
    
    @ZapApiIgnore
    public boolean isConfirmRemoveToken() {
        return this.confirmRemoveToken;
    }
    
    @ZapApiIgnore
    public void setConfirmRemoveToken(boolean confirmRemove) {
        this.confirmRemoveToken = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_TOKEN_KEY, Boolean.valueOf(confirmRemoveToken));
    }

}
