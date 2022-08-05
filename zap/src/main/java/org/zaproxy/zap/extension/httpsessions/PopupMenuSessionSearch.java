/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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
package org.zaproxy.zap.extension.httpsessions;

import java.awt.Component;
import java.util.regex.Pattern;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.zaproxy.zap.extension.search.ExtensionSearch;

@SuppressWarnings("serial")
public class PopupMenuSessionSearch extends ExtensionPopupMenuItem {

    private static final long serialVersionUID = 1L;

    private ExtensionSearch extSearch = null;
    private HttpSessionsPanel httpSessionsPanel = null;

    public PopupMenuSessionSearch(final HttpSessionsPanel httpSessionsPanel) {
        super(Constant.messages.getString("httpsessions.popup.find"));
        this.httpSessionsPanel = httpSessionsPanel;
        this.addActionListener(
                ae -> {
                    Pattern pattern =
                            Pattern.compile(
                                    httpSessionsPanel
                                            .getSelectedSession()
                                            .getTokenValuesString()
                                            .replaceAll(";", "|"));
                    search(pattern);
                });
    }

    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (HttpSessionsPanel.PANEL_NAME.equals(invoker.getName())) {
            this.setEnabled(httpSessionsPanel.getSelectedSession() != null);
            return true;
        }
        return false;
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    private ExtensionSearch getExtensionSearch() {
        if (extSearch == null) {
            extSearch =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionSearch.class);
        }
        return extSearch;
    }

    private void search(Pattern p) {
        getExtensionSearch().search(p.pattern(), ExtensionSearch.Type.All, true, false);
    }
}
