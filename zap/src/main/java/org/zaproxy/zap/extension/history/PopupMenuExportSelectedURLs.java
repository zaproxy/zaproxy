/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.extension.history;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.model.SiteNode;

/** @deprecated (2.12.0) see the exim add-on */
@Deprecated
public class PopupMenuExportSelectedURLs extends PopupMenuExportURLs {

    private static final long serialVersionUID = -4426560452505908380L;

    private static Logger LOG = LogManager.getLogger(PopupMenuExportSelectedURLs.class);

    public PopupMenuExportSelectedURLs(String menuItem, Extension extension) {
        super(menuItem, extension);
    }

    @Override
    protected void performAction() {
        File file = super.getOutputFile();
        if (file == null) {
            return;
        }

        JTree siteTree = extension.getView().getSiteTreePanel().getTreeSite();

        super.writeURLs(file, this.getOutputSet(siteTree.getSelectionPaths()));
    }

    private SortedSet<String> getOutputSet(TreePath[] startingPoints) {
        JTree siteTree = extension.getView().getSiteTreePanel().getTreeSite();
        ArrayList<TreePath> startingPts = new ArrayList<>();

        if (ArrayUtils.isEmpty(startingPoints)) {
            startingPts.add(new TreePath(siteTree.getModel().getRoot()));
        } else {
            startingPts.addAll(Arrays.asList(startingPoints));
        }

        SortedSet<String> outputSet = new TreeSet<>();
        for (TreePath aPath : startingPts) {
            Enumeration<?> en = (((SiteNode) aPath.getLastPathComponent()).preorderEnumeration());
            while (en.hasMoreElements()) {
                SiteNode node = (SiteNode) en.nextElement();
                if (node.isRoot()) {
                    continue;
                }
                outputSet.add(node.getHistoryReference().getURI().toString());
            }
        }
        return outputSet;
    }
}
