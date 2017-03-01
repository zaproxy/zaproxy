/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.ascan;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;

/**
 * General Abstract class for Challenge/Response Active Plugin management
 * 
 * @author yhawke (2014)
 */
public abstract class ChallengeCallbackAPI extends ApiImplementor {

    // This are the result contents that should be returned by the API
    private static final String API_RESPONSE_KO = "ko";
    private static final String API_RESPONSE_OK = "ok";
    
    // The default expiration time for each callback (in millisecs)
    private static final long CALLBACK_EXPIRE_TIME = 2 * 60 * 1000;
    
    // Internal logger
    private static final Logger logger = Logger.getLogger(ChallengeCallbackAPI.class);
    
    // The registered callbacks for this API
    // Use a synchronized collection 
    private final Map<String, RegisteredCallback> regCallbacks 
            = Collections.synchronizedMap(new TreeMap<String, RegisteredCallback>());

    /**
     * Default contructor
     */
    public ChallengeCallbackAPI() {
         addApiShortcut(getPrefix());
   }

    /**
     * Implements this to give back the specific shortcut
     * @return the shortcut path to call the API
     */
    @Override
    public abstract String getPrefix();

    /**
     * Expire callbacks cleaning method. When called it remove from
     * the received callbacks list all the sent challenge which haven't received
     * any answer till now according to an expiring constraint. Currently
     * the cleaning is done for every new inserting and every received callback,
     * but it can be done also with a scheduled cleaning thread if the number
     * of items is memory and time consuming...
     * Maybe to be understood in the future.
     */
    public void cleanExpiredCallbacks() {
        long now = System.currentTimeMillis();
        
        // Cuncurrency could be possible for multiple instantiations
        synchronized(regCallbacks) {    
            Iterator<Map.Entry<String, RegisteredCallback>> it = regCallbacks.entrySet().iterator();
            Map.Entry<String, RegisteredCallback> entry;
            
            while (it.hasNext()) {
                entry = it.next();
                if (now - entry.getValue().getTimestamp() > CALLBACK_EXPIRE_TIME) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Gets the ZAP API URL to a challenge endpoint.
     *
     * @param challenge the last segment of the path for the challenge endpoint
     * @return a ZAP API URL to access the the challenge endpoint 
     */
    public String getCallbackUrl(String challenge) {
        String callbackUrl = "http://" 
                + Model.getSingleton().getOptionsParam().getProxyParam().getProxyIp() + ":" 
                + Model.getSingleton().getOptionsParam().getProxyParam().getProxyPort() + "/" 
                + getPrefix() + "/" 
                + challenge;
        
        /* Key is not currently used for shorcuts... very interesting
        String key = Model.getSingleton().getOptionsParam().getApiParam().getKey();
        if (key != null && key.length() > 0) {
        callbackUrl += "?" + API.API_KEY_PARAM + "=" + key;
        }
         */
        return callbackUrl;
    }

    /**
     * Handles the given message, which might contain a challenge request.
     * 
     * @param msg the HTTP message of the ZAP API request
     * @return the HTTP message containing the response to the challenge
     * @throws ApiException if an error occurred while handling the challenge request
     */
    @Override
    public HttpMessage handleShortcut(HttpMessage msg) throws ApiException {
        // We've to look at the name and verify if the challenge has
        // been registered by one of the executed plugins 
        // ----------------
        // http://<zap_IP>/json/xxe/other/NGFrteu568sgToo100
        // ----------------
        try {
            String path = msg.getRequestHeader().getURI().getPath();            
            String challenge = path.substring(path.indexOf(getPrefix()) + getPrefix().length() + 1);
            if (challenge.charAt(challenge.length() - 1) == '/') {
                challenge = challenge.substring(0, challenge.length() - 1);
            }
            
            RegisteredCallback rcback = regCallbacks.get(challenge);
            String response;
            
            if (rcback != null) {
                rcback.getPlugin().notifyCallback(challenge, rcback.getAttackMessage());
                response = API_RESPONSE_OK;
                
                // OK we consumed it so it's time to clean
                regCallbacks.remove(challenge);
                
            } else {
                response = API_RESPONSE_KO;
                
                // Maybe we've a lot of dirty entries
                cleanExpiredCallbacks();
            }
            
            // Build the response
            msg.setResponseHeader(API.getDefaultResponseHeader("text/html", response.length()));
            msg.getResponseHeader().setHeader("Access-Control-Allow-Origin", "*");
            
            msg.setResponseBody(response);
            
        } catch (URIException | HttpMalformedHeaderException e) {
            logger.warn(e.getMessage(), e);
        }
        
        return msg;
    }

    /**
     * Registers a new ZAP API challenge.
     * 
     * @param challenge the challenge
     * @param plugin the plugin that will be notified if the challenge is requested
     * @param attack the message that contains the attack that reproduces the issue
     */
    public void registerCallback(String challenge, ChallengeCallbackPlugin plugin, HttpMessage attack) {
        // Maybe we'va a lot of dirty entries
        cleanExpiredCallbacks();
        
        // Already synchronized (no need for a monitor)
        regCallbacks.put(challenge, new RegisteredCallback(plugin, attack));
    }
    
    private static class RegisteredCallback {
        private final ChallengeCallbackPlugin plugin;
        private HistoryReference hRef;
        private long timeStamp;
        
        public RegisteredCallback(ChallengeCallbackPlugin plugin, HttpMessage msg) {
            this.plugin = plugin;
            this.timeStamp = System.currentTimeMillis();
            
            try {
                // Generate an HistoryReference object
                this.hRef = new HistoryReference(Model.getSingleton().getSession(), 
                        HistoryReference.TYPE_TEMPORARY, 
                        msg);
                
            } catch (DatabaseException | HttpMalformedHeaderException ex) { }
        }
        
        public ChallengeCallbackPlugin getPlugin() {
            return plugin;
        }
        
        public HttpMessage getAttackMessage() {
            try {
                if (hRef != null) {
                    return hRef.getHttpMessage();
                }
                
            } catch (DatabaseException | HttpMalformedHeaderException ex) { }
            
            return null;
        }
        
        public long getTimestamp() {
            return timeStamp;
        }
    }    
}
