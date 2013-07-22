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

import java.util.List;
import java.util.regex.Pattern;

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.fuzz.ExtensionFuzz;
import org.zaproxy.zap.extension.fuzz.FuzzableComponent;
import org.zaproxy.zap.extension.fuzz.FuzzerContentPanel;
import org.zaproxy.zap.extension.fuzz.FuzzerHandler;
import org.zaproxy.zap.extension.search.SearchResult;

public class HttpFuzzerHandler implements FuzzerHandler {

    private HttpFuzzerContentPanel fuzzerPanel;
    private boolean showTokenRequests;
    
    public HttpFuzzerHandler() {
        super();
        
        showTokenRequests = false;
    }
    
    void setShowTokenRequests(boolean showTokenRequests) {
        this.showTokenRequests = showTokenRequests;
    }
    
    @Override
    public void showFuzzDialog(FuzzableComponent fuzzableComponent) {
        showTokenRequests = false;
        getDialog(fuzzableComponent).setVisible(true);
    }

    @Override
    public FuzzerContentPanel getFuzzerContentPanel() {
        return getContentPanel();
    }
    
    private HttpFuzzDialog getDialog(FuzzableComponent fuzzableComponent) {
        ExtensionFuzz ext = (ExtensionFuzz) Control.getSingleton().getExtensionLoader().getExtension(ExtensionFuzz.NAME);
        return new HttpFuzzDialog(this, ext, fuzzableComponent);
    }
    
    private FuzzerContentPanel getContentPanel() {
        if (fuzzerPanel == null) {
            fuzzerPanel = new HttpFuzzerContentPanel();
            fuzzerPanel.setDisplayPanel(View.getSingleton().getRequestPanel(), View.getSingleton().getResponsePanel());
        }
        fuzzerPanel.setShowTokenRequests(showTokenRequests);
        return fuzzerPanel;
    }
    
    @Override
    public List<SearchResult> searchResults(Pattern pattern, boolean inverse) {
        return fuzzerPanel.searchResults(pattern, inverse);
    }

}
