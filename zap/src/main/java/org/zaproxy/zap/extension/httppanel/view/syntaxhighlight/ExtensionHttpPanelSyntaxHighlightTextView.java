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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.httppanel.component.all.request.RequestAllComponent;
import org.zaproxy.zap.extension.httppanel.component.all.response.ResponseAllComponent;
import org.zaproxy.zap.extension.httppanel.component.split.request.RequestSplitComponent;
import org.zaproxy.zap.extension.httppanel.component.split.response.ResponseSplitComponent;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestBodyStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestHeaderStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseBodyStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseHeaderStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.all.request.HttpRequestAllPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.all.response.HttpResponseAllPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.split.request.HttpRequestBodyPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.split.request.HttpRequestHeaderPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.split.response.HttpResponseBodyPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.split.response.HttpResponseHeaderPanelSyntaxHighlightTextView;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelViewFactory;

public class ExtensionHttpPanelSyntaxHighlightTextView extends ExtensionAdaptor {

    public static final String NAME = "ExtensionHttpPanelSyntaxHighlightTextView";

    public ExtensionHttpPanelSyntaxHighlightTextView() {
        super(NAME);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("http.panel.view.syntaxhighlighter.ext.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        if (getView() != null) {
            HttpPanelManager panelManager = HttpPanelManager.getInstance();
            panelManager.addRequestViewFactory(
                    RequestSplitComponent.NAME, new RequestSplitHeaderViewFactory());
            panelManager.addRequestViewFactory(
                    RequestSplitComponent.NAME, new RequestSplitBodyViewFactory());

            panelManager.addResponseViewFactory(
                    ResponseSplitComponent.NAME, new ResponseSplitHeaderViewFactory());
            panelManager.addResponseViewFactory(
                    ResponseSplitComponent.NAME, new ResponseSplitBodyViewFactory());

            panelManager.addRequestViewFactory(
                    RequestAllComponent.NAME, new RequestAllViewFactory());
            panelManager.addResponseViewFactory(
                    ResponseAllComponent.NAME, new ResponseAllViewFactory());
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
                    RequestSplitComponent.NAME, RequestSplitHeaderViewFactory.NAME);
            panelManager.removeRequestViews(
                    RequestSplitComponent.NAME,
                    HttpRequestHeaderPanelSyntaxHighlightTextView.NAME,
                    RequestSplitComponent.ViewComponent.HEADER);
            panelManager.removeRequestViewFactory(
                    RequestSplitComponent.NAME, RequestSplitBodyViewFactory.NAME);
            panelManager.removeRequestViews(
                    RequestSplitComponent.NAME,
                    HttpRequestBodyPanelSyntaxHighlightTextView.NAME,
                    RequestSplitComponent.ViewComponent.BODY);

            panelManager.removeResponseViewFactory(
                    ResponseSplitComponent.NAME, ResponseSplitHeaderViewFactory.NAME);
            panelManager.removeResponseViews(
                    ResponseSplitComponent.NAME,
                    HttpResponseHeaderPanelSyntaxHighlightTextView.NAME,
                    ResponseSplitComponent.ViewComponent.HEADER);
            panelManager.removeResponseViewFactory(
                    ResponseSplitComponent.NAME, ResponseSplitBodyViewFactory.NAME);
            panelManager.removeResponseViews(
                    ResponseSplitComponent.NAME,
                    HttpResponseBodyPanelSyntaxHighlightTextView.NAME,
                    ResponseSplitComponent.ViewComponent.BODY);

            panelManager.removeRequestViewFactory(
                    RequestAllComponent.NAME, RequestAllViewFactory.NAME);
            panelManager.removeRequestViews(
                    RequestAllComponent.NAME,
                    HttpRequestAllPanelSyntaxHighlightTextView.NAME,
                    null);
            panelManager.removeResponseViewFactory(
                    ResponseAllComponent.NAME, ResponseAllViewFactory.NAME);
            panelManager.removeResponseViews(
                    ResponseAllComponent.NAME,
                    HttpResponseAllPanelSyntaxHighlightTextView.NAME,
                    null);
        }
    }

    private static final class RequestSplitHeaderViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "RequestSplitHeaderViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new HttpRequestHeaderPanelSyntaxHighlightTextView(
                    new RequestHeaderStringHttpPanelViewModel());
        }

        @Override
        public Object getOptions() {
            return RequestSplitComponent.ViewComponent.HEADER;
        }
    }

    private static final class RequestSplitBodyViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "RequestSplitBodyViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new HttpRequestBodyPanelSyntaxHighlightTextView(
                    new RequestBodyStringHttpPanelViewModel());
        }

        @Override
        public Object getOptions() {
            return RequestSplitComponent.ViewComponent.BODY;
        }
    }

    private static final class ResponseSplitHeaderViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "ResponseSplitHeaderViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new HttpResponseHeaderPanelSyntaxHighlightTextView(
                    new ResponseHeaderStringHttpPanelViewModel());
        }

        @Override
        public Object getOptions() {
            return ResponseSplitComponent.ViewComponent.HEADER;
        }
    }

    private static final class ResponseSplitBodyViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "ResponseSplitBodyViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new HttpResponseBodyPanelSyntaxHighlightTextView(
                    new ResponseBodyStringHttpPanelViewModel());
        }

        @Override
        public Object getOptions() {
            return ResponseSplitComponent.ViewComponent.BODY;
        }
    }

    private static final class RequestAllViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "RequestAllViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new HttpRequestAllPanelSyntaxHighlightTextView(
                    new RequestStringHttpPanelViewModel());
        }

        @Override
        public Object getOptions() {
            return null;
        }
    }

    private static final class ResponseAllViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "ResponseAllViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new HttpResponseAllPanelSyntaxHighlightTextView(
                    new ResponseStringHttpPanelViewModel());
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
