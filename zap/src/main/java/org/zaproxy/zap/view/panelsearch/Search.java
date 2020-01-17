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
import org.zaproxy.zap.view.panelsearch.items.ButtonSearch;
import org.zaproxy.zap.view.panelsearch.items.ComboBoxElementSearch;
import org.zaproxy.zap.view.panelsearch.items.ComboBoxSearch;
import org.zaproxy.zap.view.panelsearch.items.ComponentSearch;
import org.zaproxy.zap.view.panelsearch.items.ContainerSearch;
import org.zaproxy.zap.view.panelsearch.items.JComponentSearch;
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
import org.zaproxy.zap.view.panelsearch.items.TreeSearch;

public class Search {

    public static final List<ComponentSearch> DefaultComponentSearchItems =
            Arrays.asList(
                    new TreeSearch(),
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
                    new TitledBorderSearch(),
                    new JComponentSearch(),
                    new ContainerSearch() // Must be the last item, because it fits all!
                    );

    private final List<ComponentSearch> componentSearchItems;

    public Search(List<ComponentSearch> componentSearchItems) {
        if (componentSearchItems == null)
            throw new IllegalArgumentException("componentSearchItems is null");
        this.componentSearchItems = new ArrayList<>(componentSearchItems);
    }

    public ArrayList<FoundComponent> searchFor(Object component, SearchQuery query) {
        return searchFor(new Object[] {component}, query);
    }

    public ArrayList<FoundComponent> searchFor(Object[] components, SearchQuery query) {
        ArrayList<FoundComponent> foundComponents = new ArrayList<>();

        for (Object component : components) {

            for (ComponentSearch componentSearchItem : componentSearchItems) {

                if (componentSearchItem.isResponsible(component)) {
                    if (componentSearchItem.isSearchMatching(component, query)) {
                        foundComponents.add(new FoundComponent(component));
                    }

                    Object[] childComponents = componentSearchItem.getComponents(component);
                    ArrayList<FoundComponent> foundChildComponents =
                            searchFor(childComponents, query);

                    for (FoundComponent foundChildComponent : foundChildComponents) {
                        foundChildComponent.addParent(component);
                        foundComponents.add(foundChildComponent);
                    }

                    break;
                }
            }
        }
        return foundComponents;
    }

    public void registerComponentSearch(ComponentSearch componentSearch) {
        componentSearchItems.add(0, componentSearch);
    }

    public void registerComponentSearch(ComponentSearchProvider componentSearchProvider) {
        List<ComponentSearch> componentSearches = componentSearchProvider.getComponentSearch();
        if (componentSearches != null) {
            for (ComponentSearch componentSearch : componentSearches) {
                registerComponentSearch(componentSearch);
            }
        }
    }

    public void removeComponentSearch(ComponentSearch componentSearch) {
        componentSearchItems.remove(componentSearch);
    }

    public void removeComponentSearch(ComponentSearchProvider componentSearchProvider) {
        List<ComponentSearch> componentSearches = componentSearchProvider.getComponentSearch();
        if (componentSearches != null) {
            for (ComponentSearch componentSearch : componentSearches) {
                removeComponentSearch(componentSearch);
            }
        }
    }
}
