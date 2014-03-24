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

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.encoder.Encoder;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.fuzz.FuzzProcess;
import org.zaproxy.zap.extension.fuzz.FuzzProcessFactory;
import org.zaproxy.zap.extension.httppanel.view.FuzzableMessage;

public class HttpFuzzProcessFactory implements FuzzProcessFactory {

    private HttpSender httpSender;

    private FuzzableMessage fuzzableHttpMessage;

    private boolean showTokenRequests;
    private AntiCsrfToken acsrfToken;
    private ExtensionAntiCSRF extAntiCSRF; 
    
    private Encoder encoder;
    private boolean urlEncode;
    
    public HttpFuzzProcessFactory(FuzzableMessage fuzzableMessage, AntiCsrfToken acsrfToken, boolean showTokenRequests, boolean followRedirects, boolean urlEncode) {
        this.urlEncode = urlEncode;
        if (urlEncode) {
            this.encoder = new Encoder();
        }
        
        fuzzableHttpMessage = fuzzableMessage;
        
        this.acsrfToken = acsrfToken;
        this.showTokenRequests = showTokenRequests;
        extAntiCSRF = (ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);
        
        httpSender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(), true, HttpSender.FUZZER_INITIATOR);
        httpSender.setFollowRedirect(followRedirects);
    }
    
    @Override
    public FuzzProcess getFuzzProcess(String fuzz) {
        FuzzProcess fuzzProcess = new HttpFuzzProcess(httpSender, fuzzableHttpMessage, extAntiCSRF, acsrfToken, showTokenRequests, urlEncode, encoder);
        fuzzProcess.setFuzz(fuzz);
        
        return fuzzProcess;
    }

}
