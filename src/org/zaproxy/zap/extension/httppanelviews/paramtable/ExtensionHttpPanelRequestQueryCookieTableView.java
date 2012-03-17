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
package org.zaproxy.zap.extension.httppanelviews.paramtable;

import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JComboBox;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.network.HtmlParameter;
import org.zaproxy.zap.extension.httppanel.component.split.request.RequestSplitComponent;
import org.zaproxy.zap.extension.httppanel.view.DefaultHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModel;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelViewFactory;

public class ExtensionHttpPanelRequestQueryCookieTableView extends ExtensionAdaptor {

	public static final String NAME = "ExtensionHttpPanelRequestQueryCookieTableView";
	
	public ExtensionHttpPanelRequestQueryCookieTableView() {
		super(NAME);
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
		
		HttpPanelManager.getInstance().addRequestView(RequestSplitComponent.NAME, new RequestSplitHeaderViewFactory());
	}
	
	private static final class RequestSplitHeaderViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new HttpPanelQueryCookieParamTableView(new DefaultHttpPanelViewModel(), new HttpPanelQueryCookieParamTableModel());
		}

		@Override
		public Object getOptions() {
			return RequestSplitComponent.ViewComponent.HEADER;
		}
	}

	private static class HttpPanelQueryCookieParamTableView extends HttpPanelParamTableView {

		public HttpPanelQueryCookieParamTableView(HttpPanelViewModel model, HttpPanelParamTableModel tableModel) {
			super(model, tableModel);
		}

		@Override
		public JComboBox getComboBoxTypes() {
			JComboBox comboBoxTypes = new JComboBox();
			
			comboBoxTypes.addItem(HtmlParameter.Type.url);
			comboBoxTypes.addItem(HtmlParameter.Type.cookie);
			
			return comboBoxTypes;
		}
	}
	
	private static class HttpPanelQueryCookieParamTableModel extends HttpPanelParamTableModel {

		private static final long serialVersionUID = 869819957109403800L;

		@Override
		protected void loadAllParams() {
			allParams.addAll(httpMessage.getUrlParams());
			allParams.addAll(httpMessage.getRequestHeader().getCookieParams());
		}

		@Override
		public void saveAllParams() {
			TreeSet<HtmlParameter> get = new TreeSet<HtmlParameter>();
			TreeSet<HtmlParameter> cookies = new TreeSet<HtmlParameter>();
			
			Iterator<HtmlParameter> it = allParams.iterator();
			while (it.hasNext()) {
				HtmlParameter htmlParameter = it.next();
				if(!htmlParameter.getName().isEmpty()) {
					switch (htmlParameter.getType()) {
					case url:
						get.add(htmlParameter);
						break;
					case cookie:
						cookies.add(htmlParameter);
						break;
					case form:
						break;
					default:
						break;
					}
				}
			}
			
			httpMessage.setGetParams(get);
			httpMessage.setCookieParams(cookies);
		}

		@Override
		protected HtmlParameter getDefaultHtmlParameter() {
			return new HtmlParameter(HtmlParameter.Type.url, "", "");
		}
	}
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}
}
