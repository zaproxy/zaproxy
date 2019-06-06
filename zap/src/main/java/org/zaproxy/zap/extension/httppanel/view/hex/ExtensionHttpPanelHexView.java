/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.httppanel.view.hex;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.httppanel.component.all.request.RequestAllComponent;
import org.zaproxy.zap.extension.httppanel.component.all.response.ResponseAllComponent;
import org.zaproxy.zap.extension.httppanel.component.split.request.RequestSplitComponent;
import org.zaproxy.zap.extension.httppanel.component.split.response.ResponseSplitComponent;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestBodyByteHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestByteHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestHeaderByteHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseBodyByteHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseByteHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseHeaderByteHttpPanelViewModel;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelViewFactory;

public class ExtensionHttpPanelHexView extends ExtensionAdaptor {

    public static final String NAME = "ExtensionHttpPanelHexView";

    public ExtensionHttpPanelHexView() {
        super(NAME);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("http.panel.view.hex.ext.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        if (getView() != null) {
            HttpPanelManager.getInstance()
                    .addRequestViewFactory(
                            RequestSplitComponent.NAME, new RequestSplitHeaderHexViewFactory());
            HttpPanelManager.getInstance()
                    .addRequestViewFactory(
                            RequestSplitComponent.NAME, new RequestSplitBodyHexViewFactory());

            HttpPanelManager.getInstance()
                    .addResponseViewFactory(
                            ResponseSplitComponent.NAME, new ResponseSplitHeaderHexViewFactory());
            HttpPanelManager.getInstance()
                    .addResponseViewFactory(
                            ResponseSplitComponent.NAME, new ResponseSplitBodyHexViewFactory());

            HttpPanelManager.getInstance()
                    .addRequestViewFactory(
                            RequestAllComponent.NAME, new RequestAllHexViewFactory());
            HttpPanelManager.getInstance()
                    .addResponseViewFactory(
                            ResponseAllComponent.NAME, new ResponseAllHexViewFactory());
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
            panelManager.removeRequestViewFactory(
                    RequestSplitComponent.NAME, RequestSplitHeaderHexViewFactory.NAME);
            panelManager.removeRequestViews(
                    RequestSplitComponent.NAME,
                    HttpPanelHexView.NAME,
                    RequestSplitComponent.ViewComponent.HEADER);
            panelManager.removeRequestViewFactory(
                    RequestSplitComponent.NAME, RequestSplitBodyHexViewFactory.NAME);
            panelManager.removeRequestViews(
                    RequestSplitComponent.NAME,
                    HttpPanelHexView.NAME,
                    RequestSplitComponent.ViewComponent.BODY);

            panelManager.removeResponseViewFactory(
                    ResponseSplitComponent.NAME, ResponseSplitHeaderHexViewFactory.NAME);
            panelManager.removeResponseViews(
                    ResponseSplitComponent.NAME,
                    HttpPanelHexView.NAME,
                    ResponseSplitComponent.ViewComponent.HEADER);
            panelManager.removeResponseViewFactory(
                    ResponseSplitComponent.NAME, ResponseSplitBodyHexViewFactory.NAME);
            panelManager.removeResponseViews(
                    ResponseSplitComponent.NAME,
                    HttpPanelHexView.NAME,
                    ResponseSplitComponent.ViewComponent.BODY);

            panelManager.removeRequestViewFactory(
                    RequestAllComponent.NAME, RequestAllHexViewFactory.NAME);
            panelManager.removeRequestViews(RequestAllComponent.NAME, HttpPanelHexView.NAME, null);
            panelManager.removeResponseViewFactory(
                    ResponseAllComponent.NAME, ResponseAllHexViewFactory.NAME);
            panelManager.removeResponseViews(
                    ResponseAllComponent.NAME, HttpPanelHexView.NAME, null);
        }
    }

    private static final class RequestSplitHeaderHexViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "RequestSplitHeaderHexViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new HttpPanelHexView(new RequestHeaderByteHttpPanelViewModel(), false);
        }

        @Override
        public Object getOptions() {
            return RequestSplitComponent.ViewComponent.HEADER;
        }
    }

    private static final class RequestSplitBodyHexViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "RequestSplitBodyHexViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new HttpPanelHexView(new RequestBodyByteHttpPanelViewModel(), false);
        }

        @Override
        public Object getOptions() {
            return RequestSplitComponent.ViewComponent.BODY;
        }
    }

    private static final class ResponseSplitHeaderHexViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "ResponseSplitHeaderHexViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new HttpPanelHexView(new ResponseHeaderByteHttpPanelViewModel(), false);
        }

        @Override
        public Object getOptions() {
            return ResponseSplitComponent.ViewComponent.HEADER;
        }
    }

    private static final class ResponseSplitBodyHexViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "ResponseSplitBodyHexViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new HttpPanelHexView(new ResponseBodyByteHttpPanelViewModel(), false);
        }

        @Override
        public Object getOptions() {
            return ResponseSplitComponent.ViewComponent.BODY;
        }
    }

    private static final class RequestAllHexViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "RequestAllHexViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new HttpPanelHexView(new RequestByteHttpPanelViewModel(), false);
        }

        @Override
        public Object getOptions() {
            return null;
        }
    }

    private static final class ResponseAllHexViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "ResponseAllHexViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new HttpPanelHexView(new ResponseByteHttpPanelViewModel(), false);
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

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }
}
