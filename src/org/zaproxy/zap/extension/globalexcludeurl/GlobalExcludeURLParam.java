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
import org.zaproxy.zap.extension.api.ZapApiIgnore;

/** TODO The GlobalExcludeURL functionality is currently alpha and subject to change.  */
public class GlobalExcludeURLParam extends AbstractParam {

    private static final Logger logger = Logger.getLogger(GlobalExcludeURLParam.class);

    private static final String GLOBAL_EXCLUDE_URL_BASE_KEY = "globalexcludeurl";
    
    private static final String ALL_TOKENS_KEY = GLOBAL_EXCLUDE_URL_BASE_KEY + ".url_list.url";
    
    private static final String TOKEN_REGEX_KEY = "regex";
    private static final String TOKEN_DESCRIPTION_KEY = "description";
    private static final String TOKEN_ENABLED_KEY = "enabled";

    private static final String CONFIRM_REMOVE_TOKEN_KEY = GLOBAL_EXCLUDE_URL_BASE_KEY + ".confirmRemoveToken";
    
    private static ArrayList<GlobalExcludeURLParamToken> defaultList = new ArrayList<GlobalExcludeURLParamToken>();
    
    /** Fills in the list of default regexs to ignore.  In a future version, this could be read from a
     * system-wide default HierarchicalConfiguration xml config file
     * instead or even a HierarchicalConfiguration string directly embedded in this file. */
    
    private void setDefaultList() {
        // Remember, these are regexs, so escape properly \\ vs \
        // Also, http://regexpal.com/ for quick testing.
        // The file formats are common types, not inclusive of all types. Remember, more == slower;
        // complex == slower. Don't overload with every obscure image/audio/video format in existence.
    	
    	/* At some point in the future, this could be read from some a config file and
    	 * parsed.  Thus, we just make it as arrays of strings and assume there
    	 * is some level of parsing at some point.  Since it is rarely accessed (like
    	 * once per boot, it need not be optimized).  */
    	final String defaultListArray[][] = {
			{
	    		"^.*\\.(gif|jpe?g|png|ico|icns|bmp)$",
	    	    "Extension - Image (ends with .extension)",
	            "false"
	  	    }, {
				"^.*\\.(mp[34]|mpe?g|m4[ap]|aac|avi|mov|wmv|og[gav])$",
				"Extension - Audio/Video (ends with .extension)",
				"false"
			}, {
				"^.*\\.(pdf|docx?|xlsx?|pptx?)$",
				"Extension - PDF & Office (ends with .extension)",
				"false"
			}, {
				"^.*\\.(css|js)$",
				"Extension - Stylesheet, JavaScript (ends with .extension)",
				"false"
			}, {
				"^.*\\.(sw[fa]|flv)$",
				"Extension - Flash & related (ends with .extension)",
				"false"
			}, {
				"^[^\\?]*\\.(gif|jpe?g|png|ico|icns|bmp)\\?.*$",
				"ExtParam - Image (extension plus ?params=values)",
				"false"
			}, {
				"^[^\\?]*\\.(mp[34]|mpe?g|m4[ap]|aac|avi|mov|wmv|og[gav])\\?.*$",
				"ExtParam - Audio/Video (extension plus ?params=values)",
				"false"
			}, {
				"^[^\\?]*\\.(pdf|docx?|xlsx?|pptx?)\\?.*$",
				"ExtParam - PDF & Office (extension plus ?params=values)",
				"false"
			}, {
				"^[^\\?]*\\.(css|js)\\?.*$",
				"ExtParam - Stylesheet, JavaScript (extension plus ?params=values)",
				"false"
			}, {
				"^[^\\?]*\\.(sw[fa]|flv)\\?.*$",
				"ExtParam - Flash & related (extension plus ?params=values)",
				"false"
			}, {
				"^[^\\?]*/(WebResource|ScriptResource)\\.axd\\?d=.*$",
				"ExtParam - .NET adx resources (SR/WR.adx?d=)",
				"false"
			}, {
				"^https?://api\\.bing\\.com/qsml\\.aspx?query=.*$",
				"Site - Bing API queries",
				"false"
			}, {
				"^https?://(safebrowsing-cache|sb-ssl|sb|safebrowsing\\.clients)\\.google\\.com",
				"Site - Google malware detector updates",
				"false"
			}, {
				"^https?://([^/])*\\.?lastpass\\.com",
				"Site - Lastpass manager",
				"false"
			}, {
				"^https?://(.*addons|au[0-9])\\.mozilla\\.(org|net|com)",
				"Site - Mozilla Firefox browser updates",
				"false"
			}, {
				"^https?://([^/])*\\.?(getfoxyproxy\\.org|getfirebug\\.com|noscript\\.net)",
				"Site - Mozilla Firefox extensions phoning home",
				"false"
			}, {
				// some of this from http://serverfault.com/questions/332003/what-urls-must-be-in-ies-trusted-sites-list-to-allow-windows-update
				"^https?://(.*update\\.microsoft|.*\\.windowsupdate)\\.com/.*$",
				"Site - Microsoft Windows updates",
				"false"
			}, {
				"^https?://clients2\\.google\\.com/service/update2/crx.*$",
				"Site - Google Chrome extension updates",
				"false"
			}
    	};
    	
    	for (String row[] : defaultListArray) {
    		boolean b = row[2].equalsIgnoreCase("true") ? true : false;
        	defaultList.add( new GlobalExcludeURLParamToken( row[0], row[1], b));
    	}
    }
 
    private List<GlobalExcludeURLParamToken> tokens = null;
    private List<String> enabledTokensNames = null;
    
    private boolean confirmRemoveToken = true;

    public GlobalExcludeURLParam() {
    	super();
    	setDefaultList();
    }

    @Override
    protected void parse() {
        try {
            List<HierarchicalConfiguration> fields = ((HierarchicalConfiguration) getConfig()).configurationsAt(ALL_TOKENS_KEY);
            this.tokens = new ArrayList<>(fields.size());
            enabledTokensNames = new ArrayList<>(fields.size());
            List<String> tempTokensNames = new ArrayList<>(fields.size());
            for (HierarchicalConfiguration sub : fields) {
                String regex = sub.getString(TOKEN_REGEX_KEY, "");
                if (!"".equals(regex) && !tempTokensNames.contains(regex)) {
                    boolean enabled = sub.getBoolean(TOKEN_ENABLED_KEY, true);
                    String desc = sub.getString(TOKEN_DESCRIPTION_KEY, "");
                    this.tokens.add(new GlobalExcludeURLParamToken(regex, desc, enabled));
                    tempTokensNames.add(regex);
                    if (enabled) {
                        enabledTokensNames.add(regex);
                    }
                }
            }
        } catch (ConversionException e) {
            logger.error("Error while loading Global Exclude URL tokens: " + e.getMessage(), e);
            this.tokens = new ArrayList<>(defaultList.size());
            this.enabledTokensNames = new ArrayList<>(defaultList.size());
        }
        
        if (this.tokens.size() == 0) {
            for (GlobalExcludeURLParamToken geu : defaultList) {
                this.tokens.add(new GlobalExcludeURLParamToken(geu));
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
            
            getConfig().setProperty(elementBaseKey + TOKEN_REGEX_KEY, token.getRegex());
            getConfig().setProperty(elementBaseKey + TOKEN_DESCRIPTION_KEY, token.getDescription());
            getConfig().setProperty(elementBaseKey + TOKEN_ENABLED_KEY, Boolean.valueOf(token.isEnabled()));
            
            if (token.isEnabled()) {
                enabledTokens.add(token.getRegex());
            }
        }
        
        enabledTokens.trimToSize();
        this.enabledTokensNames = enabledTokens;
    }

    public void addToken(String regex) {
        this.tokens.add(new GlobalExcludeURLParamToken(regex));
        
        this.enabledTokensNames.add(regex);
    }

    public void removeToken(String regex) {
        this.tokens.remove(new GlobalExcludeURLParamToken(regex));
        
        this.enabledTokensNames.remove(regex);
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
