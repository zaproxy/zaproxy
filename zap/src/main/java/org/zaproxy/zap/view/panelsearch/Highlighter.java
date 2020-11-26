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
package org.zaproxy.zap.view.panelsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zaproxy.zap.view.panelsearch.items.ButtonSearch;
import org.zaproxy.zap.view.panelsearch.items.ComboBoxElementSearch;
import org.zaproxy.zap.view.panelsearch.items.ComboBoxSearch;
import org.zaproxy.zap.view.panelsearch.items.ComponentHighlighter;
import org.zaproxy.zap.view.panelsearch.items.JxLabelSearch;
import org.zaproxy.zap.view.panelsearch.items.LabelSearch;
import org.zaproxy.zap.view.panelsearch.items.SpinnerSearch;
import org.zaproxy.zap.view.panelsearch.items.TabbedPaneElementSearch;
import org.zaproxy.zap.view.panelsearch.items.TabbedPaneSearch;
import org.zaproxy.zap.view.panelsearch.items.TableCellElementSearch;
import org.zaproxy.zap.view.panelsearch.items.TableSearch;
import org.zaproxy.zap.view.panelsearch.items.TextFieldSearch;
import org.zaproxy.zap.view.panelsearch.items.TitledBorderSearch;
import org.zaproxy.zap.view.panelsearch.items.TreeNodeElementSearch;

public class Highlighter {

    private Logger LOGGER = LogManager.getLogger(Highlighter.class);

    public static final List<ComponentHighlighter> DefaultComponentHighlighterItems =
            Arrays.asList(
                    new ButtonSearch(),
                    new TreeNodeElementSearch(),
                    new JxLabelSearch(),
                    new LabelSearch(),
                    new SpinnerSearch(),
                    new TextFieldSearch(),
                    new ComboBoxSearch(),
                    new ComboBoxElementSearch(),
                    new TableSearch(),
                    new TableCellElementSearch(),
                    new TabbedPaneSearch(),
                    new TabbedPaneElementSearch(),
                    new TitledBorderSearch());

    private final List<ComponentHighlighter> componentHighlighterItems;

    public Highlighter(List<ComponentHighlighter> componentHighlighterItems) {
        if (componentHighlighterItems == null)
            throw new IllegalArgumentException("componentHighlighterItems is null");
        this.componentHighlighterItems = new ArrayList<>(componentHighlighterItems);
    }

    public HighlighterResult highlight(ArrayList<FoundComponent> foundComponents) {
        ArrayList<HighlightedComponent> highlightedComponents = new ArrayList<>();
        ArrayList<HighlightedComponent> highlightedParentComponents = new ArrayList<>();

        for (FoundComponent foundComponent : foundComponents) {
            highlightedComponents.addAll(
                    highlightComponents(
                            Arrays.asList(foundComponent.getComponent()),
                            (h, c) -> h.highlight(c)));
            highlightedParentComponents.addAll(
                    highlightComponents(
                            foundComponent.getParents(), (h, c) -> h.highlightAsParent(c)));
        }

        return new HighlighterResult(highlightedComponents, highlightedParentComponents);
    }

    private ArrayList<HighlightedComponent> highlightComponents(
            List<Object> components,
            BiFunction<ComponentHighlighter, Object, HighlightedComponent> highlightAction) {
        ArrayList<HighlightedComponent> highlightedComponents = new ArrayList<>();
        for (Object component : components) {
            HighlightedComponent highlightedComponent =
                    highlightComponent(component, highlightAction);
            if (highlightedComponent != null) {
                highlightedComponents.add(highlightedComponent);
            }
        }
        return highlightedComponents;
    }

    private HighlightedComponent highlightComponent(
            Object component,
            BiFunction<ComponentHighlighter, Object, HighlightedComponent> highlightAction) {
        for (ComponentHighlighter componentHighlighter : componentHighlighterItems) {
            HighlightedComponent highlightedComponent =
                    highlightAction.apply(componentHighlighter, component);
            if (highlightedComponent != null) {
                return highlightedComponent;
            }
        }
        LOGGER.debug("Nothing to highlight here!");
        return null;
    }

    public void undoHighlight(HighlighterResult highlighterResult) {
        for (HighlightedComponent highlightedComponent :
                highlighterResult.getHighlightedComponents()) {
            for (ComponentHighlighter componentHighlighter : componentHighlighterItems) {
                boolean hasUndoSomething = componentHighlighter.undoHighlight(highlightedComponent);
                if (hasUndoSomething) {
                    break;
                }
            }
        }

        for (HighlightedComponent highlightedParentComponent :
                highlighterResult.getHighlightedParentComponents()) {
            for (ComponentHighlighter componentHighlighter : componentHighlighterItems) {
                boolean hasUndoSomething =
                        componentHighlighter.undoHighlightAsParent(highlightedParentComponent);
                if (hasUndoSomething) {
                    break;
                }
            }
        }
    }

    public void registerComponentHighlighter(ComponentHighlighter componentHighlighter) {
        componentHighlighterItems.add(0, componentHighlighter);
    }

    public void registerComponentHighlighter(
            ComponentHighlighterProvider componentHighlighterProvider) {
        List<ComponentHighlighter> componentHighlighters =
                componentHighlighterProvider.getComponentHighlighter();
        if (componentHighlighters != null) {
            for (ComponentHighlighter componentHighlighter : componentHighlighters) {
                registerComponentHighlighter(componentHighlighter);
            }
        }
    }

    public void removeComponentHighlighter(ComponentHighlighter componentHighlighter) {
        componentHighlighterItems.remove(componentHighlighter);
    }

    public void removeComponentHighlighter(
            ComponentHighlighterProvider componentHighlighterProvider) {
        List<ComponentHighlighter> componentHighlighters =
                componentHighlighterProvider.getComponentHighlighter();
        if (componentHighlighters != null) {
            for (ComponentHighlighter componentHighlighter : componentHighlighters) {
                removeComponentHighlighter(componentHighlighter);
            }
        }
    }
}
