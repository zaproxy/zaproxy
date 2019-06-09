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
package org.zaproxy.zap.extension.httppanel.view.paramtable;

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

public class ExtensionHttpPanelRequestFormTableView extends ExtensionAdaptor {

    public static final String NAME = "ExtensionHttpPanelRequestFormTableView";

    public ExtensionHttpPanelRequestFormTableView() {
        super(NAME);

        setOrder(105);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("http.panel.view.formtable.ext.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        if (getView() != null) {
            HttpPanelManager.getInstance()
                    .addRequestViewFactory(
                            RequestSplitComponent.NAME, new HttpPanelFormParamTableViewFactory());
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
                    RequestSplitComponent.NAME, HttpPanelFormParamTableViewFactory.NAME);
            panelManager.removeRequestViews(
                    RequestSplitComponent.NAME,
                    HttpPanelFormParamTableView.NAME,
                    RequestSplitComponent.ViewComponent.BODY);
        }
    }

    private static final class HttpPanelFormParamTableViewFactory implements HttpPanelViewFactory {

        public static final String NAME = "HttpPanelFormParamTableViewFactory";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public HttpPanelView getNewView() {
            return new HttpPanelFormParamTableView(
                    new DefaultHttpPanelViewModel(), new HttpPanelFormParamTableModel());
        }

        @Override
        public Object getOptions() {
            return RequestSplitComponent.ViewComponent.BODY;
        }
    }

    private static class HttpPanelFormParamTableView extends HttpPanelParamTableView {

        public HttpPanelFormParamTableView(
                HttpPanelViewModel model, HttpPanelParamTableModel tableModel) {
            super(model, tableModel);
        }

        @Override
        public String getTargetViewName() {
            return "";
        }

        @Override
        public JComboBox<HtmlParameter.Type> getComboBoxTypes() {
            JComboBox<HtmlParameter.Type> comboBoxTypes = new JComboBox<>();

            comboBoxTypes.addItem(HtmlParameter.Type.form);

            return comboBoxTypes;
        }
    }

    private static class HttpPanelFormParamTableModel extends HttpPanelParamTableModel {

        private static final long serialVersionUID = -251253954319359635L;

        @Override
        protected void loadAllParams() {
            allParams.addAll(httpMessage.getFormParams());
        }

        @Override
        public void saveAllParams() {
            TreeSet<HtmlParameter> form = new TreeSet<>();

            Iterator<HtmlParameter> it = allParams.iterator();
            while (it.hasNext()) {
                HtmlParameter htmlParameter = it.next();
                if (!htmlParameter.getName().isEmpty()) {
                    switch (htmlParameter.getType()) {
                        case url:
                            break;
                        case cookie:
                            break;
                        case form:
                            form.add(htmlParameter);
                            break;
                        default:
                            break;
                    }
                }
            }

            httpMessage.setFormParams(form);
            httpMessage.getRequestHeader().setContentLength(httpMessage.getRequestBody().length());
        }

        @Override
        protected HtmlParameter getDefaultHtmlParameter() {
            return new HtmlParameter(HtmlParameter.Type.form, "", "");
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
