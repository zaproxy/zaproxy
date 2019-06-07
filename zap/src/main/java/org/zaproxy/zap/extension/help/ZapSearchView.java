/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.extension.help;

import java.awt.Component;
import java.util.Hashtable;
import java.util.Locale;
import javax.help.HelpModel;
import javax.help.HelpSet;
import javax.help.JHelpSearchNavigator;
import javax.help.SearchView;

/**
 * A {@code SearchView} that uses a {@link ZapBasicSearchNavigatorUI} for its {@code
 * JHelpSearchNavigator}.
 *
 * @since 2.5.0
 */
public class ZapSearchView extends SearchView {

    private static final long serialVersionUID = 1L;

    public ZapSearchView(HelpSet hs, String name, String label, Hashtable<?, ?> params) {
        super(hs, name, label, params);
    }

    public ZapSearchView(
            HelpSet hs, String name, String label, Locale locale, Hashtable<?, ?> params) {
        super(hs, name, label, locale, params);
    }

    @Override
    public Component createNavigator(HelpModel model) {
        return new JHelpSearchNavigator(this, model) {

            private static final long serialVersionUID = 1L;

            @Override
            public String getUIClassID() {
                return "ZapHelpSearchNavigatorUI";
            }
        };
    }
}
