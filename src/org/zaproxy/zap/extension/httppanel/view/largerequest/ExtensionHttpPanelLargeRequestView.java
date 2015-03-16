/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.httppanel.view.largerequest;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.component.all.request.RequestAllComponent;
import org.zaproxy.zap.extension.httppanel.component.split.request.RequestSplitComponent;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelDefaultViewSelector;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelDefaultViewSelectorFactory;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelViewFactory;

public class ExtensionHttpPanelLargeRequestView extends ExtensionAdaptor {

    public static final String NAME = "ExtensionHttpPanelLargeRequestView";

    public ExtensionHttpPanelLargeRequestView() {
        super(NAME);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        if (getView() != null) {
            HttpPanelManager panelManager = HttpPanelManager.getInstance();
            panelManager.addRequestViewFactory(RequestSplitComponent.NAME, new RequestLargeRequestSplitViewFactory());
            panelManager.addRequestDefaultViewSelectorFactory(
                    RequestSplitComponent.NAME,
                    new LargeRequestDefaultSplitViewSelectorFactory());

            panelManager.addRequestViewFactory(RequestAllComponent.NAME, new RequestLargeRequestAllViewFactory());
            panelManager.addRequestDefaultViewSelectorFactory(
                    RequestAllComponent.NAME,
                    new LargeRequestDefaultAllViewSelectorFactory());
        }
    }

    @Override
    public boolean canUnload() {
        // Do not allow the unload until moved to an add-on.
        return false;
    }

    @Override
    public void unload() {
        if (getView() != null) {
            HttpPanelManager panelManager = HttpPanelManager.getInstance();
            panelManager.removeRequestViewFactory(RequestSplitComponent.NAME, RequestLargeRequestSplitViewFactory.NAME);
            panelManager.removeRequestViews(
                    RequestSplitComponent.NAME,
                    RequestLargeRequestSplitView.NAME,
                    RequestSplitComponent.ViewComponent.BODY);
            panelManager.removeRequestDefaultViewSelectorFactory(
                    RequestSplitComponent.NAME,
                    LargeRequestDefaultSplitViewSelectorFactory.NAME);
            panelManager.removeRequestDefaultViewSelectorFactoryAndDefaultViewSelectorsAdded(
                    RequestSplitComponent.NAME,
                    LargeRequestDefaultSplitViewSelector.NAME,
                    RequestSplitComponent.ViewComponent.BODY);

            panelManager.removeRequestViewFactory(RequestAllComponent.NAME, RequestLargeRequestAllViewFactory.NAME);
            panelManager.removeRequestViews(RequestAllComponent.NAME, RequestLargeRequestAllView.NAME, null);
            panelManager.removeRequestDefaultViewSelectorFactory(
                    RequestAllComponent.NAME,
                    LargeRequestDefaultAllViewSelectorFactory.NAME);
            panelManager.removeRequestDefaultViewSelectorFactoryAndDefaultViewSelectorsAdded(
                    RequestAllComponent.NAME,
                    LargeRequestDefaultAllViewSelector.NAME,
                    null);
        }
    }

    private static final class RequestLargeRequestSplitViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "RequestLargeRequestSplitViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new RequestLargeRequestSplitView();
        }

        @Override
        public Object getOptions() {
            return RequestSplitComponent.ViewComponent.BODY;
        }
    }

    private static final class LargeRequestDefaultSplitViewSelector implements HttpPanelDefaultViewSelector {

        public static final String NAME = "LargeRequestDefaultSplitViewSelector";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public boolean matchToDefaultView(Message aMessage) {
            return LargeRequestUtil.isLargeRequest(aMessage);
        }

        @Override
        public String getViewName() {
            return RequestLargeRequestSplitView.NAME;
        }

        @Override
        public int getOrder() {
            return 50;
        }
    }

    private static final class RequestLargeRequestAllViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "RequestLargeRequestAllViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new RequestLargeRequestAllView(new LargeRequestStringHttpPanelViewModel());
        }

        @Override
        public Object getOptions() {
            return null;
        }
    }

    private static final class LargeRequestDefaultAllViewSelector implements HttpPanelDefaultViewSelector {

        public static final String NAME = "LargeRequestDefaultAllViewSelector";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public boolean matchToDefaultView(Message aMessage) {
            return LargeRequestUtil.isLargeRequest(aMessage);
        }

        @Override
        public String getViewName() {
            return RequestLargeRequestAllView.NAME;
        }

        @Override
        public int getOrder() {
            return 50;
        }
    }

    private static final class LargeRequestDefaultSplitViewSelectorFactory implements HttpPanelDefaultViewSelectorFactory {

        private static HttpPanelDefaultViewSelector defaultViewSelector = null;

        private HttpPanelDefaultViewSelector getDefaultViewSelector() {
            if (defaultViewSelector == null) {
                createViewSelector();
            }
            return defaultViewSelector;
        }

        private synchronized void createViewSelector() {
            if (defaultViewSelector == null) {
                defaultViewSelector = new LargeRequestDefaultSplitViewSelector();
            }
        }

        public static final String NAME = "LargeRequestDefaultSplitViewSelectorFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelDefaultViewSelector getNewDefaultViewSelector() {
            return getDefaultViewSelector();
        }

        @Override
        public Object getOptions() {
            return RequestSplitComponent.ViewComponent.BODY;
        }
    }

    private static final class LargeRequestDefaultAllViewSelectorFactory implements HttpPanelDefaultViewSelectorFactory {

        private static HttpPanelDefaultViewSelector defaultViewSelector = null;

        private HttpPanelDefaultViewSelector getDefaultViewSelector() {
            if (defaultViewSelector == null) {
                createViewSelector();
            }
            return defaultViewSelector;
        }

        private synchronized void createViewSelector() {
            if (defaultViewSelector == null) {
                defaultViewSelector = new LargeRequestDefaultAllViewSelector();
            }
        }

        public static final String NAME = "LargeRequestDefaultAllViewSelectorFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelDefaultViewSelector getNewDefaultViewSelector() {
            return getDefaultViewSelector();
        }

        @Override
        public Object getOptions() {
            return null;
        }

    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

	/**
	 * No database tables used, so all supported
	 */
	@Override
	public boolean supportsDb(String type) {
		return true;
	}
}
