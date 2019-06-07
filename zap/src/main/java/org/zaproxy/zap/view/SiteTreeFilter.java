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
package org.zaproxy.zap.view;

import org.parosproxy.paros.extension.history.HistoryFilter;
import org.parosproxy.paros.model.SiteNode;

public class SiteTreeFilter {

    private HistoryFilter historyFilter;
    private boolean inScope;

    public SiteTreeFilter(HistoryFilter historyFilter) {
        this.historyFilter = historyFilter;
    }

    public boolean isInScope() {
        return inScope;
    }

    public void setInScope(boolean inScope) {
        this.inScope = inScope;
    }

    public boolean matches(SiteNode node) {
        if (node.isRoot()) {
            return true;
        }

        if (inScope && !node.isIncludedInScope()) {
            return false;
        }

        if (historyFilter != null && node.getHistoryReference() != null) {
            return historyFilter.matches(node.getHistoryReference());
        }
        return false;
    }

    public String toShortString() {
        if (historyFilter != null) {
            return historyFilter.toShortString();
        }
        return "";
    }

    public String toLongString() {
        if (historyFilter != null) {
            return historyFilter.toLongString();
        }
        return "";
    }
}
