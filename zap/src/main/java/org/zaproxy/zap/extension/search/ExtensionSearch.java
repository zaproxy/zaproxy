/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.extension.search;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.view.ZapMenuItem;

public class ExtensionSearch extends ExtensionAdaptor {

    private static final Logger LOGGER = LogManager.getLogger(ExtensionSearch.class);

    public static final String NAME = "ExtensionSearch2";

    public enum Type {
        All,
        URL,
        Request,
        Response,
        Header,
        Custom
    }

    private SearchParam searchParam;
    private OptionsSearchPanel optionsPanel;

    private SearchPanel searchPanel = null;
    private JMenu searchMenu = null;
    private ZapMenuItem menuItemSearch = null;
    private ZapMenuItem menuItemNext = null;
    private ZapMenuItem menuItemPrev = null;

    private SearchThread searchThread = null;
    private boolean searchJustInScope = false;

    private Map<String, HttpSearcher> customSearchers = new HashMap<>();

    public ExtensionSearch() {
        super(NAME);
        this.setOrder(20);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("search.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        extensionHook.addOptionsParamSet(getSearchParam());
        if (getView() != null) {
            extensionHook.addSessionListener(new ViewSessionChangedListener());
            extensionHook.getHookView().addOptionPanel(getOptionsPanel());
            extensionHook.getHookView().addStatusPanel(getSearchPanel());
            extensionHook.getHookMenu().addEditSubMenu(getSearchMenu());

            ExtensionHelp.enableHelpKey(getSearchPanel(), "ui.tabs.search");
        }
        extensionHook.addApiImplementor(new SearchAPI(this));
    }

    SearchParam getSearchParam() {
        if (searchParam == null) {
            searchParam = new SearchParam();
        }
        return searchParam;
    }

    private OptionsSearchPanel getOptionsPanel() {
        if (optionsPanel == null) {
            optionsPanel = new OptionsSearchPanel();
        }
        return optionsPanel;
    }

    private SearchPanel getSearchPanel() {
        if (searchPanel == null) {
            searchPanel = new SearchPanel(getView());
            searchPanel.setExtension(this);
        }
        return searchPanel;
    }

    public void addCustomHttpSearcher(HttpSearcher searcher) {
        if (searcher == null) {
            return;
        }
        final String searcherName = searcher.getName();
        if (customSearchers.containsKey(searcherName)) {
            LOGGER.warn("Attempting to add an HTTP searcher with the same name: {}", searcherName);
            return;
        }
        customSearchers.put(searcherName, searcher);

        if (getView() != null) {
            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            searchPanel.addCustomSearcher(searcherName);
                        }
                    });
        }
    }

    public void removeCustomHttpSearcher(HttpSearcher searcher) {
        final String searcherName = searcher.getName();

        if (customSearchers.remove(searcherName) != null && getView() != null) {
            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            searchPanel.removeCustomSearcher(searcherName);
                        }
                    });
        }
    }

    public void search(String filter, Type reqType) {
        this.search(filter, reqType, false, false);
    }

    public void search(String filter, Type reqType, boolean setToolbar, boolean inverse) {
        this.search(filter, reqType, null, setToolbar, inverse);
    }

    public void search(
            String filter,
            Type reqType,
            String customSearcherName,
            boolean setToolbar,
            boolean inverse) {
        this.searchPanel.resetSearchResults();
        this.search(
                filter,
                this.searchPanel,
                reqType,
                customSearcherName,
                setToolbar,
                inverse,
                null,
                -1,
                -1,
                true,
                getSearchParam().getMaximumSearchResultsGUI());
    }

    public void search(
            String filter,
            SearchListenner listenner,
            Type reqType,
            boolean setToolbar,
            boolean inverse) {
        this.search(filter, listenner, reqType, setToolbar, inverse, null, -1, -1);
    }

    public void search(
            String filter,
            SearchListenner listenner,
            Type reqType,
            boolean setToolbar,
            boolean inverse,
            String baseUrl,
            int start,
            int count) {
        search(filter, listenner, reqType, setToolbar, inverse, baseUrl, start, count, true);
    }

    public void search(
            String filter,
            SearchListenner listenner,
            Type reqType,
            boolean setToolbar,
            boolean inverse,
            String baseUrl,
            int start,
            int count,
            boolean searchAllOccurrences) {
        search(
                filter,
                listenner,
                reqType,
                setToolbar,
                inverse,
                baseUrl,
                start,
                count,
                searchAllOccurrences,
                -1);
    }

    public void search(
            String filter,
            SearchListenner listenner,
            Type reqType,
            boolean setToolbar,
            boolean inverse,
            String baseUrl,
            int start,
            int count,
            boolean searchAllOccurrences,
            int maxOccurrences) {
        search(
                filter,
                listenner,
                reqType,
                null,
                setToolbar,
                inverse,
                baseUrl,
                start,
                count,
                searchAllOccurrences,
                maxOccurrences);
    }

    public void search(
            String filter,
            SearchListenner listenner,
            Type reqType,
            String customSearcherName,
            boolean setToolbar,
            boolean inverse,
            String baseUrl,
            int start,
            int count,
            boolean searchAllOccurrences,
            int maxOccurrences) {
        if (setToolbar) {
            this.getSearchPanel().searchFocus();
            this.getSearchPanel().getRegExField().setText(filter);
            this.getSearchPanel().setSearchType(reqType);
        }

        synchronized (this) {
            if (searchThread != null && searchThread.isAlive()) {
                searchThread.stopSearch();

                while (searchThread.isAlive()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            }
            searchThread =
                    new SearchThread(
                            filter,
                            reqType,
                            customSearcherName,
                            listenner,
                            inverse,
                            searchJustInScope,
                            baseUrl,
                            start,
                            count,
                            searchAllOccurrences,
                            maxOccurrences);
            searchThread.setCustomSearchers(customSearchers);
            searchThread.start();
        }
    }

    private JMenu getSearchMenu() {
        if (searchMenu == null) {
            searchMenu = new JMenu(Constant.messages.getString("menu.edit.search"));
            searchMenu.add(getMenuItemSearch());
            searchMenu.add(getMenuItemNext());
            searchMenu.add(getMenuItemPrev());
        }
        return searchMenu;
    }

    private ZapMenuItem getMenuItemSearch() {
        if (menuItemSearch == null) {
            menuItemSearch =
                    new ZapMenuItem(
                            "menu.edit.search.item",
                            getView().getMenuShortcutKeyStroke(KeyEvent.VK_H, 0, false));

            menuItemSearch.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            searchPanel.searchFocus();
                        }
                    });
        }
        return menuItemSearch;
    }

    private ZapMenuItem getMenuItemNext() {
        if (menuItemNext == null) {
            menuItemNext =
                    new ZapMenuItem(
                            "menu.edit.search.next.item",
                            getView().getMenuShortcutKeyStroke(KeyEvent.VK_G, 0, false));

            menuItemNext.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            searchPanel.highlightNextResult();
                        }
                    });
        }
        return menuItemNext;
    }

    private ZapMenuItem getMenuItemPrev() {
        if (menuItemPrev == null) {
            menuItemPrev =
                    new ZapMenuItem(
                            "menu.edit.search.previous.item",
                            getView()
                                    .getMenuShortcutKeyStroke(
                                            KeyEvent.VK_G, KeyEvent.SHIFT_DOWN_MASK, false));

            menuItemPrev.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            searchPanel.highlightPrevResult();
                        }
                    });
        }
        return menuItemPrev;
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("search.desc");
    }

    public void setSearchJustInScope(boolean searchJustInScope) {
        this.searchJustInScope = searchJustInScope;
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    /** A {@code SessionChangedListener} for view/UI related functionalities. */
    private class ViewSessionChangedListener implements SessionChangedListener {

        @Override
        public void sessionAboutToChange(Session session) {
            // Nothing to do.
        }

        @Override
        public void sessionScopeChanged(Session session) {
            // Nothing to do.
        }

        @Override
        public void sessionChanged(final Session session) {
            if (EventQueue.isDispatchThread()) {
                getSearchPanel().resetSearchResults();
                return;
            }

            try {
                EventQueue.invokeAndWait(
                        new Runnable() {

                            @Override
                            public void run() {
                                sessionChanged(session);
                            }
                        });
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        @Override
        public void sessionModeChanged(Mode mode) {
            // Nothing to do.
        }
    }
}
