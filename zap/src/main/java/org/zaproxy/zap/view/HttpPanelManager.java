/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.component.HttpPanelComponentInterface;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelDefaultViewSelector;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.utils.ThreadUtils;

public class HttpPanelManager {

    private static HttpPanelManager instance = null;

    private HttpPanelManagement requestPanels;
    private HttpPanelManagement responsePanels;

    private HttpPanelManager() {
        requestPanels = new HttpPanelManagement();
        responsePanels = new HttpPanelManagement();
    }

    public static HttpPanelManager getInstance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    private static synchronized void createInstance() {
        if (instance == null) {
            instance = new HttpPanelManager();
        }
    }

    public void addRequestPanel(HttpPanel panel) {
        ThreadUtils.invokeAndWaitHandled(() -> requestPanels.addPanel(panel));
    }

    public void addRequestComponentFactory(HttpPanelComponentFactory componentFactory) {
        ThreadUtils.invokeAndWaitHandled(() -> requestPanels.addComponentFactory(componentFactory));
    }

    public void addRequestViewFactory(String componentName, HttpPanelViewFactory viewFactory) {
        ThreadUtils.invokeAndWaitHandled(
                () -> requestPanels.addViewFactory(componentName, viewFactory));
    }

    public void addRequestDefaultViewSelectorFactory(
            String componentName, HttpPanelDefaultViewSelectorFactory defaultViewSelectorFactory) {
        ThreadUtils.invokeAndWaitHandled(
                () ->
                        requestPanels.addDefaultViewSelectorFactory(
                                componentName, defaultViewSelectorFactory));
    }

    public void addResponsePanel(HttpPanel panel) {
        ThreadUtils.invokeAndWaitHandled(() -> responsePanels.addPanel(panel));
    }

    public void addResponseComponentFactory(HttpPanelComponentFactory componentFactory) {
        ThreadUtils.invokeAndWaitHandled(
                () -> responsePanels.addComponentFactory(componentFactory));
    }

    public void addResponseViewFactory(String componentName, HttpPanelViewFactory viewFactory) {
        ThreadUtils.invokeAndWaitHandled(
                () -> responsePanels.addViewFactory(componentName, viewFactory));
    }

    public void addResponseDefaultViewSelectorFactory(
            String componentName, HttpPanelDefaultViewSelectorFactory defaultViewSelectorFactory) {
        ThreadUtils.invokeAndWaitHandled(
                () ->
                        responsePanels.addDefaultViewSelectorFactory(
                                componentName, defaultViewSelectorFactory));
    }

    public void removeRequestPanel(HttpPanel panel) {
        ThreadUtils.invokeAndWaitHandled(() -> requestPanels.removePanel(panel));
    }

    public void removeRequestComponentFactory(String componentFactoryName) {
        ThreadUtils.invokeAndWaitHandled(
                () -> requestPanels.removeComponentFactory(componentFactoryName));
    }

    public void removeRequestComponents(String componentName) {
        ThreadUtils.invokeAndWaitHandled(() -> requestPanels.removeComponents(componentName));
    }

    public void removeRequestViewFactory(String componentName, String viewFactoryName) {
        ThreadUtils.invokeAndWaitHandled(
                () -> requestPanels.removeViewFactory(componentName, viewFactoryName));
    }

    public void removeRequestViews(String componentName, String viewName, Object options) {
        ThreadUtils.invokeAndWaitHandled(
                () -> requestPanels.removeViews(componentName, viewName, options));
    }

    public void removeRequestDefaultViewSelectorFactory(
            String componentName, String defaultViewSelectorFactoryName) {
        ThreadUtils.invokeAndWaitHandled(
                () ->
                        requestPanels.removeDefaultViewSelectorFactory(
                                componentName, defaultViewSelectorFactoryName));
    }

    public void removeRequestDefaultViewSelectors(
            String componentName, String defaultViewSelectorName, Object options) {
        ThreadUtils.invokeAndWaitHandled(
                () ->
                        requestPanels.removeDefaultViewSelectors(
                                componentName, defaultViewSelectorName, options));
    }

    /**
     * @deprecated (2.7.0) Use {@link #removeRequestDefaultViewSelectors(String, String, Object)}
     *     instead
     */
    @Deprecated
    public void removeRequestDefaultViewSelectorFactoryAndDefaultViewSelectorsAdded(
            String componentName, String defaultViewSelectorName, Object options) {
        ThreadUtils.invokeAndWaitHandled(
                () ->
                        requestPanels.removeDefaultViewSelectors(
                                componentName, defaultViewSelectorName, options));
    }

    public void removeResponsePanel(HttpPanel panel) {
        ThreadUtils.invokeAndWaitHandled(() -> responsePanels.removePanel(panel));
    }

    public void removeResponseComponentFactory(String componentFactoryName) {
        ThreadUtils.invokeAndWaitHandled(
                () -> responsePanels.removeComponentFactory(componentFactoryName));
    }

    public void removeResponseComponents(String componentName) {
        ThreadUtils.invokeAndWaitHandled(() -> responsePanels.removeComponents(componentName));
    }

    public void removeResponseViewFactory(String componentName, String viewFactoryName) {
        ThreadUtils.invokeAndWaitHandled(
                () -> responsePanels.removeViewFactory(componentName, viewFactoryName));
    }

    public void removeResponseViews(String componentName, String viewName, Object options) {
        ThreadUtils.invokeAndWaitHandled(
                () -> responsePanels.removeViews(componentName, viewName, options));
    }

    public void removeResponseDefaultViewSelectorFactory(
            String componentName, String defaultViewSelectorFactoryName) {
        ThreadUtils.invokeAndWaitHandled(
                () ->
                        responsePanels.removeDefaultViewSelectorFactory(
                                componentName, defaultViewSelectorFactoryName));
    }

    public void removeResponseDefaultViewSelectors(
            String componentName, String defaultViewSelectorName, Object options) {
        ThreadUtils.invokeAndWaitHandled(
                () ->
                        responsePanels.removeDefaultViewSelectors(
                                componentName, defaultViewSelectorName, options));
    }

    private static final class HttpPanelManagement {

        private List<HttpPanel> panels;
        private Map<String, HttpPanelComponentFactory> components;
        private Map<String, Map<String, HttpPanelViewFactory>> views;
        private Map<String, Map<String, HttpPanelDefaultViewSelectorFactory>> defaultViews;

        public HttpPanelManagement() {
            panels = new ArrayList<>();
            components = new HashMap<>();
            views = new HashMap<>();
            defaultViews = new HashMap<>();
        }

        public void addPanel(HttpPanel panel) {
            this.panels.add(panel);

            FileConfiguration fileConfiguration =
                    Model.getSingleton().getOptionsParam().getConfig();

            for (HttpPanelComponentFactory componentFactory : components.values()) {
                panel.addComponent(componentFactory.getNewComponent(), fileConfiguration);
            }

            for (Map.Entry<String, Map<String, HttpPanelViewFactory>> componentViews :
                    views.entrySet()) {
                for (HttpPanelViewFactory viewFactory : componentViews.getValue().values()) {
                    panel.addView(
                            componentViews.getKey(),
                            viewFactory.getNewView(),
                            viewFactory.getOptions(),
                            fileConfiguration);
                }
            }

            for (Map.Entry<String, Map<String, HttpPanelDefaultViewSelectorFactory>>
                    componentDefaultViews : defaultViews.entrySet()) {
                for (HttpPanelDefaultViewSelectorFactory viewFactory :
                        componentDefaultViews.getValue().values()) {
                    panel.addDefaultViewSelector(
                            componentDefaultViews.getKey(),
                            viewFactory.getNewDefaultViewSelector(),
                            viewFactory.getOptions());
                }
            }
        }

        public void removePanel(HttpPanel panel) {
            this.panels.remove(panel);
        }

        public void addComponentFactory(HttpPanelComponentFactory componentFactory) {
            if (components.containsKey(componentFactory.getName())) {
                return;
            }
            this.components.put(componentFactory.getName(), componentFactory);

            FileConfiguration fileConfiguration =
                    Model.getSingleton().getOptionsParam().getConfig();

            for (HttpPanel panel : panels) {
                panel.addComponent(componentFactory.getNewComponent(), fileConfiguration);

                final String componentName = componentFactory.getComponentName();

                Map<String, HttpPanelViewFactory> componentViews = views.get(componentName);
                if (componentViews != null) {
                    for (HttpPanelViewFactory viewFactory : componentViews.values()) {
                        panel.addView(
                                componentName,
                                viewFactory.getNewView(),
                                viewFactory.getOptions(),
                                fileConfiguration);
                    }
                }

                Map<String, HttpPanelDefaultViewSelectorFactory> defaultViewsComp =
                        defaultViews.get(componentName);
                if (defaultViewsComp != null) {
                    for (HttpPanelDefaultViewSelectorFactory defaultViewSelector :
                            defaultViewsComp.values()) {
                        panel.addDefaultViewSelector(
                                componentName,
                                defaultViewSelector.getNewDefaultViewSelector(),
                                defaultViewSelector.getOptions());
                    }
                }
            }
        }

        public void removeComponentFactory(String componentFactoryName) {
            components.remove(componentFactoryName);
        }

        public void removeComponents(String componentName) {
            for (HttpPanel panel : panels) {
                panel.removeComponent(componentName);
            }
        }

        public void addViewFactory(String componentName, HttpPanelViewFactory viewFactory) {
            Map<String, HttpPanelViewFactory> componentViews = this.views.get(componentName);
            if (componentViews == null) {
                componentViews = new HashMap<>();
                this.views.put(componentName, componentViews);
            } else if (views.containsKey(viewFactory.getName())) {
                return;
            }

            componentViews.put(viewFactory.getName(), viewFactory);

            FileConfiguration fileConfiguration =
                    Model.getSingleton().getOptionsParam().getConfig();

            for (HttpPanel panel : panels) {
                panel.addView(
                        componentName,
                        viewFactory.getNewView(),
                        viewFactory.getOptions(),
                        fileConfiguration);
            }
        }

        public void removeViewFactory(String componentName, String viewFactoryName) {
            Map<String, HttpPanelViewFactory> componentViews = this.views.get(componentName);
            if (componentViews == null) {
                return;
            }

            HttpPanelViewFactory viewFactory = componentViews.get(viewFactoryName);
            if (viewFactory == null) {
                return;
            }

            componentViews.remove(viewFactoryName);

            if (componentViews.isEmpty()) {
                this.views.put(componentName, null);
            }
        }

        public void removeViews(String componentName, String viewName, Object options) {
            for (HttpPanel panel : panels) {
                panel.removeView(componentName, viewName, options);
            }
        }

        public void addDefaultViewSelectorFactory(
                String componentName,
                HttpPanelDefaultViewSelectorFactory defaultViewSelectorFactory) {
            Map<String, HttpPanelDefaultViewSelectorFactory> componentDefaultViews =
                    this.defaultViews.get(componentName);
            if (componentDefaultViews == null) {
                componentDefaultViews = new HashMap<>();
                this.defaultViews.put(componentName, componentDefaultViews);
            } else if (views.containsKey(defaultViewSelectorFactory.getName())) {
                return;
            }

            componentDefaultViews.put(
                    defaultViewSelectorFactory.getName(), defaultViewSelectorFactory);

            for (HttpPanel panel : panels) {
                panel.addDefaultViewSelector(
                        componentName,
                        defaultViewSelectorFactory.getNewDefaultViewSelector(),
                        defaultViewSelectorFactory.getOptions());
            }
        }

        public void removeDefaultViewSelectorFactory(String componentName, String viewFactoryName) {
            Map<String, HttpPanelDefaultViewSelectorFactory> componentDefaultViews =
                    this.defaultViews.get(componentName);
            if (componentDefaultViews == null) {
                return;
            }

            HttpPanelDefaultViewSelectorFactory viewFactory =
                    componentDefaultViews.get(viewFactoryName);
            if (viewFactory == null) {
                return;
            }

            componentDefaultViews.remove(viewFactoryName);

            if (componentDefaultViews.isEmpty()) {
                this.defaultViews.put(componentName, null);
            }
        }

        public void removeDefaultViewSelectors(
                String componentName, String defaultViewSelectorName, Object options) {
            for (HttpPanel panel : panels) {
                panel.removeDefaultViewSelector(componentName, defaultViewSelectorName, options);
            }
        }
    }

    public interface HttpPanelComponentFactory {
        String getName();

        String getComponentName();

        HttpPanelComponentInterface getNewComponent();
    }

    public interface HttpPanelViewFactory {
        String getName();

        HttpPanelView getNewView();

        Object getOptions();
    }

    public interface HttpPanelDefaultViewSelectorFactory {
        String getName();

        HttpPanelDefaultViewSelector getNewDefaultViewSelector();

        Object getOptions();
    }
}
