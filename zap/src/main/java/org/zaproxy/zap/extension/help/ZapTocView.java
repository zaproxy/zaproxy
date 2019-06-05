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
package org.zaproxy.zap.extension.help;

import java.net.URL;
import java.util.Hashtable;
import java.util.Locale;
import javax.help.BadIDException;
import javax.help.HelpSet;
import javax.help.Map;
import javax.help.Map.ID;
import javax.help.TOCView;
import javax.help.TreeItem;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * <strong>NOTE:</strong> The name (and package) of the class must not be changed lightly! It will
 * break help's TOC merging at runtime. The name and package is hard coded in helpset files. (END
 * NOTE)
 *
 * <p>A {@code TOCView} that also reads the "tocid" attribute of the "tocitem" elements and creates
 * {@code ZapTocItem}s.
 *
 * @see TOCView
 * @see ZapTocItem
 * @see ZapTocMerger
 */
// Note: This class contains copied (verbatim) code from the base class TOCView.
public class ZapTocView extends TOCView {

    private static final long serialVersionUID = 8473218435547565830L;

    public ZapTocView(HelpSet hs, String name, String label, Hashtable<?, ?> params) {
        this(hs, name, label, hs.getLocale(), params);
    }

    public ZapTocView(
            HelpSet hs, String name, String label, Locale locale, Hashtable<?, ?> params) {
        super(hs, name, label, locale, params);
    }

    // Note: The implementation has been copied (verbatim) from the base method except for the use
    // of a custom TreeItemFactory.
    @Override
    public DefaultMutableTreeNode getDataAsTree() {
        HelpSet hs = getHelpSet();
        Hashtable<?, ?> params = getParameters();
        URL url;

        if (params == null || !params.containsKey("data")) {
            return new DefaultMutableTreeNode();
        }

        try {
            url = new URL(hs.getHelpSetURL(), (String) params.get("data"));
        } catch (Exception ex) {
            throw new Error("Trouble getting URL to TOC data; " + ex);
        }

        return parse(url, hs, hs.getLocale(), new TreeItemFactoryImpl(), this);
    }

    /**
     * A {@code DefaultTOCFactory} that reads the "tocid" attribute of the "tocitem" element and
     * creates {@code ZapTocItem}s (instead of {@code TOCItem}s).
     *
     * @see javax.help.TOCView.DefaultTOCFactory
     * @see ZapTocItem
     */
    private static class TreeItemFactoryImpl extends DefaultTOCFactory {

        // Note: The implementation has been copied (verbatim) from the base method except for the
        // read of tocid attribute and
        // creation of ZapTocItem.
        @Override
        public TreeItem createItem(
                String tagName,
                @SuppressWarnings("rawtypes") Hashtable atts,
                HelpSet hs,
                Locale locale) {
            if (tagName == null || !tagName.equals("tocitem")) {
                throw new IllegalArgumentException("tagName");
            }
            String id = null;
            String imageID = null;
            String text = null;
            String mergeType = null;
            String expand = null;
            String presentation = null;
            String presentationName = null;
            String tocid = null;

            if (atts != null) {
                id = (String) atts.get("target");
                imageID = (String) atts.get("image");
                text = (String) atts.get("text");
                mergeType = (String) atts.get("mergetype");
                expand = (String) atts.get("expand");
                presentation = (String) atts.get("presentationtype");
                presentationName = (String) atts.get("presentationname");
                tocid = (String) atts.get("tocid");
            }

            Map.ID mapID = null;
            Map.ID imageMapID = null;
            try {
                mapID = ID.create(id, hs);
            } catch (BadIDException bex1) {
            }
            try {
                imageMapID = ID.create(imageID, hs);
            } catch (BadIDException bex2) {
            }
            ZapTocItem item = new ZapTocItem(mapID, imageMapID, hs, locale, tocid);
            if (text != null) {
                item.setName(text);
            }

            if (mergeType != null) {
                item.setMergeType(mergeType);
            }

            if (expand != null) {
                if (expand.equals("true")) {
                    item.setExpansionType(TreeItem.EXPAND);
                } else if (expand.equals("false")) {
                    item.setExpansionType(TreeItem.COLLAPSE);
                }
            }

            if (presentation != null) {
                item.setPresentation(presentation);
            }

            if (presentationName != null) {
                item.setPresentationName(presentationName);
            }
            return item;
        }
    }
}
