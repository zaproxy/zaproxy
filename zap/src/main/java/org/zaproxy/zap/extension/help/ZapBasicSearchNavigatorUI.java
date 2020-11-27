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

import java.util.Arrays;
import java.util.Enumeration;
import javax.help.HelpSet;
import javax.help.JHelpSearchNavigator;
import javax.help.NavigatorView;
import javax.help.plaf.basic.BasicSearchNavigatorUI;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A {@code BasicSearchNavigatorUI} that keeps merging views even if one of them is invalid.
 *
 * @since 2.5.0
 */
public class ZapBasicSearchNavigatorUI extends BasicSearchNavigatorUI {

    private static final Logger LOGGER = LogManager.getLogger(ZapBasicSearchNavigatorUI.class);

    public ZapBasicSearchNavigatorUI(JHelpSearchNavigator b) {
        super(b);
    }

    @Override
    protected void addSubHelpSets(HelpSet hs) {
        for (Enumeration<?> e = hs.getHelpSets(); e.hasMoreElements(); ) {
            HelpSet ehs = (HelpSet) e.nextElement();
            // merge views
            Arrays.stream(ehs.getNavigatorViews()).forEach(view -> mergeSearchView(view, ehs));
            addSubHelpSets(ehs);
        }
    }

    private void mergeSearchView(NavigatorView view, HelpSet ehs) {
        try {
            if (searchnav.canMerge(view)) {
                searchnav.merge(view);
            }
        } catch (IllegalArgumentException ex) {
            StringBuilder logMessage = new StringBuilder(150);
            logMessage.append("Failed to merge Search view [").append(view.getName()).append("] ");
            logMessage.append("from HelpSet [").append(ehs.getTitle()).append("]: ");
            logMessage.append(ex.getMessage());
            LOGGER.warn(logMessage.toString());
        }
    }

    public static ComponentUI createUI(JComponent x) {
        return new ZapBasicSearchNavigatorUI((JHelpSearchNavigator) x);
    }
}
