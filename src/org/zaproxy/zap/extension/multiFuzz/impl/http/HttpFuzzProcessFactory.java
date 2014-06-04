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
package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.util.HashMap;

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.multiFuzz.FuzzProcessFactory;

public class HttpFuzzProcessFactory implements FuzzProcessFactory<HttpFuzzProcess, HttpPayload, HttpFuzzLocation> {

    private HttpSender httpSender;
    private HttpMessage msg;
    private boolean showTokenRequests;
    private AntiCsrfToken acsrfToken;
    private ExtensionAntiCSRF extAntiCSRF; 
    
    
    public HttpFuzzProcessFactory(HttpMessage fuzzableMessage, AntiCsrfToken acsrfToken, boolean showTokenRequests, boolean followRedirects) {
        
        msg = fuzzableMessage;
        this.acsrfToken = acsrfToken;
        this.showTokenRequests = showTokenRequests;
        extAntiCSRF = (ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);
        
        httpSender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(), true, HttpSender.FUZZER_INITIATOR);
        httpSender.setFollowRedirect(followRedirects);
    }
    

	@Override
	public HttpFuzzProcess getFuzzProcess(HashMap<HttpFuzzLocation, HttpPayload> subs) {
		HttpFuzzProcess fuzzProcess = new HttpFuzzProcess(httpSender, msg, extAntiCSRF, acsrfToken, showTokenRequests);
		fuzzProcess.setPayload(subs);
        return fuzzProcess;
	}
	
	public HttpMessage getMessage(){
		return this.msg;
	}
	public HttpSender getSender(){
		return this.httpSender;
	}
	public AntiCsrfToken getToken(){
		return this.acsrfToken;
	}
}
