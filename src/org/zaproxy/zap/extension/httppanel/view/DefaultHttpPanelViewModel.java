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
package org.zaproxy.zap.extension.httppanel.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.parosproxy.paros.network.HttpMessage;

public class DefaultHttpPanelViewModel implements HttpPanelViewModel {
	
	protected HttpMessage httpMessage;
	
	protected List<HttpPanelViewModelListener> listeners;
	
	public DefaultHttpPanelViewModel() {
		listeners = new ArrayList<HttpPanelViewModelListener>(2);
		httpMessage = null;
	}
	
	public void setHttpMessage(HttpMessage httpMessage) {
		this.httpMessage = httpMessage;
		
		fireDataChanged();
	}
	
	public HttpMessage getHttpMessage() {
		return httpMessage;
	}
	
	public void clear() {
		httpMessage = null;
		
		fireDataChanged();
	}
	
	public void addHttpPanelViewModelListener(HttpPanelViewModelListener l) {
		listeners.add(l);
	}
	
	public void removeHttpPanelViewModelListener(HttpPanelViewModelListener l) {
		listeners.remove(l);
	}
	
	protected void fireDataChanged() {
		notifyAllListeners(new HttpPanelViewModelEvent(this));
	}
	
	private void notifyAllListeners(HttpPanelViewModelEvent e) {
		Iterator<HttpPanelViewModelListener> it = listeners.iterator();
		while (it.hasNext()) {
			it.next().dataChanged(e);
		}
	}
}
