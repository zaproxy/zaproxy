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
package org.zaproxy.zap.extension.fuzz.impl.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.extension.encoder.Encoder;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.fuzz.AbstractFuzzProcess;
import org.zaproxy.zap.extension.fuzz.FuzzResult;
import org.zaproxy.zap.extension.fuzz.FuzzResult.State;
import org.zaproxy.zap.extension.httppanel.view.FuzzableMessage;

public class HttpFuzzProcess extends AbstractFuzzProcess {

    private static final Logger logger = Logger.getLogger(HttpFuzzProcess.class);

    private HttpSender httpSender;
    
    private FuzzableMessage fuzzableHttpMessage;
    
    private Encoder encoder;
    private boolean urlEncode;
    
    private ExtensionAntiCSRF extAntiCSRF;
    private AntiCsrfToken acsrfToken;
    private boolean showTokenRequests;

    public HttpFuzzProcess(HttpSender httpSender, FuzzableMessage fuzzableHttpMessage, 
            ExtensionAntiCSRF extAntiCSRF, AntiCsrfToken acsrfToken, 
            boolean showTokenRequests, 
            boolean urlEncode, Encoder encoder) {
        this.httpSender = httpSender;
        this.fuzzableHttpMessage = fuzzableHttpMessage;
        this.acsrfToken = acsrfToken;
        this.showTokenRequests = showTokenRequests;
        this.urlEncode = urlEncode;
        this.extAntiCSRF = extAntiCSRF;
        this.encoder = encoder;
    }

    @Override
    public FuzzResult fuzz(String fuzz) {
        String tokenValue = null;
        HttpFuzzResult fuzzResult = new HttpFuzzResult();
        
        if (this.acsrfToken != null) {
            // This currently just supports a single token in one page
            // To support wizards etc need to loop back up the messages for previous tokens
            try {
                HttpMessage tokenMsg = this.acsrfToken.getMsg().cloneAll();
                httpSender.sendAndReceive(tokenMsg);

                // If we've got a token value here then the AntiCSRF extension must have been registered
                tokenValue = extAntiCSRF.getTokenValue(tokenMsg, acsrfToken.getName());

                if (showTokenRequests) {
                    List<HttpMessage> tokenRequests = new ArrayList<>();
                    tokenRequests.add(tokenMsg);
                    fuzzResult.setTokenRequestMessages(tokenRequests);
                }
            } catch (HttpException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        
        String fuzzString;
        if (urlEncode) {
            fuzzString = encoder.getURLEncode(fuzz);
        } else {
            fuzzString = fuzz;
        }

        HttpMessage msg;
        try {
            // Inject the payload
            msg = (HttpMessage) fuzzableHttpMessage.fuzz(fuzzString);
        } catch(Exception e) {
            msg = ((HttpMessage)fuzzableHttpMessage.getMessage()).cloneRequest();
            msg.setNote(fuzz);
            fuzzResult.setMessage(msg);

            fuzzResult.setState(State.ERROR);
            return fuzzResult;
        }
        
        msg.setNote(fuzz);
        
        if (tokenValue != null) {
            // Replace token value - only supported in the body right now
            String replaced = msg.getRequestBody().toString();
            replaced = replaced.replace(encoder.getURLEncode(acsrfToken.getValue()), encoder.getURLEncode(tokenValue));
            msg.setRequestBody(replaced);
        }
        // Correct the content length for the above changes
        msg.getRequestHeader().setContentLength(msg.getRequestBody().length());
        
        try {
            httpSender.sendAndReceive(msg);
            
            if (isFuzzStringReflected(msg, fuzz)) {
                fuzzResult.setState(State.REFLECTED);
            }
        } catch (HttpException e) {
            logger.error(e.getMessage(), e);
            fuzzResult.setState(State.ERROR);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            fuzzResult.setState(State.ERROR);
        }
        
        fuzzResult.setMessage(msg);
        
        return fuzzResult;
    }
    
    private boolean isFuzzStringReflected(HttpMessage msg, String fuzzString) {
        HttpMessage originalMessage = (HttpMessage)fuzzableHttpMessage.getMessage();

        final int pos = originalMessage.getResponseBody().toString().indexOf(fuzzString);
    
        return msg.getResponseBody().toString().indexOf(fuzzString, pos) != -1;
    }
    
}
