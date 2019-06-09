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
package org.zaproxy.zap.view.popup;

import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.view.messagecontainer.MessageContainer;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;

/**
 * A {@code PopupMenuHistoryReferenceContainer} that exposes the {@code SiteNode}s of {@code
 * HttpMessageContainer}s.
 *
 * @since 2.3.0
 * @see PopupMenuHistoryReferenceContainer
 * @see HttpMessageContainer
 * @see #isEnableForMessageContainer(MessageContainer)
 */
public class PopupMenuSiteNodeContainer extends PopupMenuHistoryReferenceContainer {

    private static final long serialVersionUID = -7761982452884675034L;

    /**
     * Constructs a {@code PopupMenuItemSiteNodeContainer} with the given label and with no support
     * for multiple selected messages (the menu item button will not be enabled when the invoker has
     * multiple selected messages).
     *
     * @param label the label of the menu
     * @see #isEnableForMessageContainer(MessageContainer)
     */
    public PopupMenuSiteNodeContainer(String label) {
        super(label);
    }

    /**
     * Constructs a {@code PopupMenuItemSiteNodeContainer} with the given label and whether or not
     * the menu item supports multiple selected messages (if {@code false} the menu item button will
     * not be enabled when the invoker has multiple selected messages).
     *
     * @param label the label of the menu
     * @param multiSelect {@code true} if the menu supports multiple selected messages, {@code
     *     false} otherwise.
     * @see #isEnableForMessageContainer(MessageContainer)
     */
    public PopupMenuSiteNodeContainer(String label, boolean multiSelect) {
        super(label, multiSelect);
    }

    /**
     * Tells whether or not the button should be enabled for the given selected message.
     *
     * <p>Calls the method {@code isButtonEnabledForSiteNode(SitNode)} passing as parameter the site
     * node extracted from the given {@code historyReference}.
     *
     * @param historyReference the selected message, never {@code null}
     * @return {@code true} if the button should be enabled for the given selected message, {@code
     *     false} otherwise.
     */
    @Override
    protected boolean isButtonEnabledForHistoryReference(HistoryReference historyReference) {
        final SiteNode siteNode = getSiteNode(historyReference);
        if (siteNode != null && !isButtonEnabledForSiteNode(siteNode)) {
            return false;
        }
        return true;
    }

    /**
     * Convenience method that extracts a {@code SiteNode} from the given {@code historyReference}.
     * If no {@code SiteNode} is found {@code null} is returned.
     *
     * @param historyReference the history reference
     * @return the {@code SiteNode} or {@code null} if not found
     * @see #isButtonEnabledForHistoryReference(HistoryReference)
     */
    protected static SiteNode getSiteNode(HistoryReference historyReference) {
        SiteNode sn = historyReference.getSiteNode();
        if (sn == null) {
            sn =
                    Model.getSingleton()
                            .getSession()
                            .getSiteTree()
                            .getSiteNode(historyReference.getHistoryId());
        }
        return sn;
    }

    /**
     * Tells whether or not the button should be enabled for the given selected message.
     *
     * <p>By default, it returns {@code true}.
     *
     * @param siteNode the site node, never {@code null}
     * @return {@code true} if the button should be enabled for the given selected message, {@code
     *     false} otherwise.
     */
    protected boolean isButtonEnabledForSiteNode(SiteNode siteNode) {
        return true;
    }
}
