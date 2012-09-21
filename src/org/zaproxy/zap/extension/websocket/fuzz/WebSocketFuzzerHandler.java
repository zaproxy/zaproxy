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
package org.zaproxy.zap.extension.websocket.fuzz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.fuzz.ExtensionFuzz;
import org.zaproxy.zap.extension.fuzz.FuzzableComponent;
import org.zaproxy.zap.extension.fuzz.FuzzerContentPanel;
import org.zaproxy.zap.extension.fuzz.FuzzerHandler;
import org.zaproxy.zap.extension.search.SearchResult;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;
import org.zaproxy.zap.extension.websocket.WebSocketProxy.State;
import org.zaproxy.zap.extension.websocket.db.TableWebSocket;
import org.zaproxy.zap.extension.websocket.ui.WebSocketPanel;

/**
 * Observes WebSockets to collect connected {@link WebSocketProxy} instances.
 */
public class WebSocketFuzzerHandler implements FuzzerHandler, WebSocketObserver {

    private WebSocketFuzzMessagesView fuzzView;
	private WebSocketFuzzMessagesViewModel viewModel;
	
	private Map<Integer, WebSocketProxy> wsProxies;
	private WebSocketFuzzDialog fuzzDialog;
	private TableWebSocket table;
    
    public WebSocketFuzzerHandler(TableWebSocket webSocketTable) {
        super();
        wsProxies = new HashMap<>();
        table = webSocketTable;
		viewModel = new WebSocketFuzzMessagesViewModel(table);
    }
    
    @Override
    public void showFuzzDialog(FuzzableComponent fuzzableComponent) {
        getDialog(fuzzableComponent).setVisible(true);
    }

    @Override
    public FuzzerContentPanel getFuzzerContentPanel() {
    	if (fuzzView == null) {
            fuzzView = new WebSocketFuzzMessagesView(viewModel, table);
            
            View view = View.getSingleton();
            fuzzView.setDisplayPanel(view.getRequestPanel(), view.getResponsePanel());
        }
        return fuzzView;
    }
    
    private WebSocketFuzzDialog getDialog(FuzzableComponent fuzzableComponent) {
        if (fuzzDialog == null) {
        	ExtensionFuzz ext = (ExtensionFuzz) Control.getSingleton().getExtensionLoader().getExtension(ExtensionFuzz.NAME);
        	fuzzDialog = new WebSocketFuzzDialog(ext, fuzzableComponent, wsProxies);
        } else {
        	// re-use dialog, such that the previous selection of the
        	// fuzzer & its category is not lost.
        	fuzzDialog.setFuzzableComponent(fuzzableComponent);
        }
        return fuzzDialog;
    }
    
    @Override
    public List<SearchResult> searchResults(Pattern pattern, boolean inverse) {
        return fuzzView.searchResults(pattern, inverse);
    }

	@Override
	public int getObservingOrder() {
		return WebSocketPanel.WEBSOCKET_OBSERVING_ORDER + 5;
	}

	@Override
	public boolean onMessageFrame(int channelId, WebSocketMessage message) {
		// update table
		viewModel.fireMessageArrived(message.getDTO());
		return true;
	}

	/**
	 * Keeps track of active WebSocket proxies.
	 */
	@Override
	public void onStateChange(State state, WebSocketProxy proxy) {
		if (proxy.isConnected()) {
			wsProxies.put(proxy.getChannelId(), proxy);
		} else {
			wsProxies.remove(proxy.getChannelId());
		}
	}
	
	public void pause() {
		((WebSocketFuzzMessagesView) getFuzzerContentPanel()).pause();
	}
	
	public void resume() {
		((WebSocketFuzzMessagesView) getFuzzerContentPanel()).resume();
	}
}
