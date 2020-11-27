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
package org.zaproxy.zap.view.panelsearch.items;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.painter.RectanglePainter;
import org.zaproxy.zap.view.panelsearch.HighlightedComponent;
import org.zaproxy.zap.view.panelsearch.HighlighterUtils;
import org.zaproxy.zap.view.panelsearch.SearchQuery;

public class JxLabelSearch extends AbstractComponentSearch<JXLabel> {

    private static final String BACKGROUND_PAINTER = "BackgroundPainter";

    @Override
    protected boolean isSearchMatchingInternal(JXLabel component, SearchQuery query) {
        return query.match(component.getText());
    }

    @Override
    protected Object[] getComponentsInternal(JXLabel component) {
        return new Object[] {};
    }

    @Override
    protected HighlightedComponent highlightInternal(JXLabel component) {
        HighlightedComponent highlightedComponent = new HighlightedComponent(component);
        highlightedComponent.put(BACKGROUND_PAINTER, component.getBackgroundPainter());
        component.setBackgroundPainter(
                new RectanglePainter(
                        HighlighterUtils.getHighlightColor(),
                        HighlighterUtils.getHighlightColor()));
        return highlightedComponent;
    }

    @Override
    protected void undoHighlightInternal(
            HighlightedComponent highlightedComponent, JXLabel component) {
        component.setBackgroundPainter(highlightedComponent.get(BACKGROUND_PAINTER));
    }
}
