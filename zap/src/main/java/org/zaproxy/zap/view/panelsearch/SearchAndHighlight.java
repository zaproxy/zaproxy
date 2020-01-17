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
package org.zaproxy.zap.view.panelsearch;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import org.zaproxy.zap.view.panelsearch.items.ComponentHighlighter;
import org.zaproxy.zap.view.panelsearch.items.ComponentSearch;

public class SearchAndHighlight {

    private Search search;
    private Highlighter highlighter;
    private HighlighterResult currentHighlighterResult;
    private Component parent;

    public SearchAndHighlight(Component parent) {
        this.parent = parent;
        this.search = new Search(Search.DefaultComponentSearchItems);
        this.highlighter = new Highlighter(Highlighter.DefaultComponentHighlighterItems);
    }

    public void searchAndHighlight(String text) {
        clearHighlight();
        if (text != null && !text.isEmpty()) {
            InStringSearchQuery query = new InStringSearchQuery(text);
            ArrayList<FoundComponent> findings = search.searchFor(new Object[] {parent}, query);

            currentHighlighterResult = highlighter.highlight(findings);
            SwingUtilities.invokeLater(() -> parent.repaint());
        }
    }

    public void clearHighlight() {
        if (currentHighlighterResult != null) {
            highlighter.undoHighlight(currentHighlighterResult);
            currentHighlighterResult = null;
            SwingUtilities.invokeLater(() -> parent.repaint());
        }
    }

    public void registerComponentSearch(ComponentSearch componentSearch) {
        search.registerComponentSearch(componentSearch);
    }

    public void registerComponentSearch(ComponentSearchProvider componentSearchProvider) {
        search.registerComponentSearch(componentSearchProvider);
    }

    public void removeComponentSearch(ComponentSearch componentSearch) {
        search.removeComponentSearch(componentSearch);
    }

    public void removeComponentSearch(ComponentSearchProvider componentSearchProvider) {
        search.removeComponentSearch(componentSearchProvider);
    }

    public void registerComponentHighlighter(ComponentHighlighter componentHighlighter) {
        clearHighlight();
        highlighter.registerComponentHighlighter(componentHighlighter);
    }

    public void registerComponentHighlighter(
            ComponentHighlighterProvider componentHighlighterProvider) {
        clearHighlight();
        highlighter.registerComponentHighlighter(componentHighlighterProvider);
    }

    public void removeComponentHighlighter(ComponentHighlighter componentHighlighter) {
        clearHighlight();
        highlighter.removeComponentHighlighter(componentHighlighter);
    }

    public void removeComponentHighlighter(
            ComponentHighlighterProvider componentHighlighterProvider) {
        clearHighlight();
        highlighter.removeComponentHighlighter(componentHighlighterProvider);
    }
}
