/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.zaproxy.zap.extension.siteinfo;

import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.popup.MenuWeights;

/**
 * Right-click "Get Info" item for the Sites tree, as requested in
 * https://github.com/zaproxy/zaproxy/issues/3738. Shows a small modal dialog summarising the
 * subtree rooted at the selected node: number of nodes, the most recent history reference, and the
 * scanner / source that recorded it.
 */
public class PopupMenuSiteGetInfo extends ExtensionPopupMenuItem {

    private static final long serialVersionUID = 1L;

    public PopupMenuSiteGetInfo() {
        super(Constant.messages.getString("siteinfo.popup"));

        this.addActionListener(e -> showInfoDialog());
    }

    private void showInfoDialog() {
        JTree tree = View.getSingleton().getSiteTreePanel().getTreeSite();
        TreePath selection = tree.getSelectionPath();
        if (selection == null) {
            return;
        }
        SiteNode node = (SiteNode) selection.getLastPathComponent();
        SiteInfoCalculator.Stats stats = SiteInfoCalculator.compute(node);

        StringBuilder body = new StringBuilder();
        body.append(
                Constant.messages.getString("siteinfo.dialog.subtreeNodes", stats.getNodeCount()));
        body.append('\n');
        if (stats.hasNewestHistoryRef()) {
            body.append(
                    Constant.messages.getString(
                            "siteinfo.dialog.lastNodeAdded",
                            DateFormat.getDateTimeInstance()
                                    .format(new Date(stats.getNewestHistoryRefTimeMillis()))));
            body.append('\n');
            body.append(
                    Constant.messages.getString(
                            "siteinfo.dialog.howFound",
                            describeHistoryType(stats.getNewestHistoryRefType())));
        } else {
            body.append(Constant.messages.getString("siteinfo.dialog.noHistory"));
        }

        JOptionPane.showMessageDialog(
                View.getSingleton().getMainFrame(),
                body.toString(),
                Constant.messages.getString("siteinfo.dialog.title", node.getNodeName()),
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Map {@code HistoryReference.TYPE_*} to a short display string. Visible for testing. */
    static String describeHistoryType(int type) {
        switch (type) {
            case HistoryReference.TYPE_PROXIED:
                return Constant.messages.getString("siteinfo.type.proxied");
            case HistoryReference.TYPE_ZAP_USER:
                return Constant.messages.getString("siteinfo.type.user");
            case HistoryReference.TYPE_SPIDER:
                return Constant.messages.getString("siteinfo.type.spider");
            case HistoryReference.TYPE_SPIDER_AJAX:
                return Constant.messages.getString("siteinfo.type.spiderAjax");
            case HistoryReference.TYPE_SCANNER:
                return Constant.messages.getString("siteinfo.type.scanner");
            case HistoryReference.TYPE_FUZZER:
                return Constant.messages.getString("siteinfo.type.fuzzer");
            case HistoryReference.TYPE_BRUTE_FORCE:
                return Constant.messages.getString("siteinfo.type.bruteForce");
            case HistoryReference.TYPE_CALLBACK:
                return Constant.messages.getString("siteinfo.type.callback");
            case HistoryReference.TYPE_OAST:
                return Constant.messages.getString("siteinfo.type.oast");
            default:
                return Constant.messages.getString("siteinfo.type.other", type);
        }
    }

    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker instanceof JTree) {
            JTree tree = (JTree) invoker;
            if ("treeSite".equals(tree.getName())) {
                this.setEnabled(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getWeight() {
        return MenuWeights.MENU_SITE_GET_INFO_WEIGHT;
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}
