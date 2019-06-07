/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2012/04/25 Changed the type of the parameter "panel" of the method
// addSessionPanel.
// ZAP: 2016/04/08 Allow to add ContextPanelFactory
// ZAP: 2017/02/19 Allow to add components to the main tool bar.
// ZAP: 2018/10/05 Lazily initialise the lists and add JavaDoc.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
package org.parosproxy.paros.extension;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.extension.httppanel.DisplayedMessageChangedListener;
import org.zaproxy.zap.view.ContextPanelFactory;

/**
 * The object to add/hook components to the main UI components.
 *
 * <p>The components added through the hook are removed when the extension is unloaded.
 *
 * <p><strong>Note:</strong> This class is not thread-safe, the components should be added only
 * through the thread that {@link Extension#hook(ExtensionHook) hooks the extension}.
 *
 * @since 1.0.0
 */
public class ExtensionHookView {

    /**
     * The work panels added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addWorkPanel(AbstractPanel)
     * @see #getWorkPanel()
     */
    private List<AbstractPanel> workPanelList;
    /**
     * The status panels added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addWorkPanel(AbstractPanel)
     * @see #getWorkPanel()
     */
    private List<AbstractPanel> statusPanelList;
    /**
     * The work panels added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addWorkPanel(AbstractPanel)
     * @see #getWorkPanel()
     */
    private List<AbstractPanel> selectPanelList;
    /**
     * The session panels added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addSessionPanel(AbstractParamPanel)
     * @see #getSessionPanel()
     */
    private List<AbstractParamPanel> sessionPanelList;
    /**
     * The options panels added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addOptionPanel(AbstractParamPanel)
     * @see #getOptionsPanel()
     */
    private List<AbstractParamPanel> optionPanelList;

    /**
     * The {@link ContextPanelFactory}s added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addContextPanelFactory(ContextPanelFactory)
     * @see #getContextPanelFactories()
     */
    private List<ContextPanelFactory> contextPanelFactories;

    /**
     * The {@link Component}s added to this extension hook, to be later added to the main tool bar
     * panel.
     *
     * <p>Lazily initialised.
     *
     * @see #addMainToolBarComponent(Component)
     * @see #getMainToolBarComponents()
     */
    private List<Component> mainToolBarComponents;

    /**
     * The {@link DisplayedMessageChangedListener} added to this extension hook, to be later added
     * to the request panel changelisteners.
     *
     * <p>Lazily initialised.
     *
     * @see #addRequestPanelDisplayedMessageChangedListener(DisplayedMessageChangedListener)
     * @see #getRequestPanelDisplayedMessageChangedListeners()
     */
    private List<DisplayedMessageChangedListener> requestPanelDisplayedMessageChangedListener;

    /**
     * The {@link DisplayedMessageChangedListener} added to this extension hook, to be later added
     * to the response panel changelisteners.
     *
     * <p>Lazily initialised.
     *
     * @see #addResponsePanelDisplayedMessageChangedListener(DisplayedMessageChangedListener)
     * @see #getResponsePanelDisplayedMessageChangedListeners()
     */
    private List<DisplayedMessageChangedListener> responsePanelDisplayedMessageChangedListener;

    public ExtensionHookView() {}

    /**
     * Adds the given {@link AbstractPanel} to the view hook, to be later added to the {@link
     * org.parosproxy.paros.view.WorkbenchPanel WorkbenchPanel} as a {@link
     * org.parosproxy.paros.view.WorkbenchPanel.PanelType#WORK work} panel.
     *
     * @param panel the panel that will be added to the {@code WorkbenchPanel}.
     * @see org.parosproxy.paros.view.View#getWorkbench()
     */
    public void addWorkPanel(AbstractPanel panel) {
        if (workPanelList == null) {
            workPanelList = createList();
        }
        workPanelList.add(panel);
    }

    /**
     * Adds the given {@link AbstractPanel} to the view hook, to be later added to the {@link
     * org.parosproxy.paros.view.WorkbenchPanel WorkbenchPanel} as a {@link
     * org.parosproxy.paros.view.WorkbenchPanel.PanelType#SELECT select} panel.
     *
     * @param panel the panel that will be added to the {@code WorkbenchPanel}.
     * @see org.parosproxy.paros.view.View#getWorkbench()
     */
    public void addSelectPanel(AbstractPanel panel) {
        if (selectPanelList == null) {
            selectPanelList = createList();
        }
        selectPanelList.add(panel);
    }

    /**
     * Adds the given {@link AbstractPanel} to the view hook, to be later added to the {@link
     * org.parosproxy.paros.view.WorkbenchPanel WorkbenchPanel} as a {@link
     * org.parosproxy.paros.view.WorkbenchPanel.PanelType#STATUS status} panel.
     *
     * @param panel the panel that will be added to the {@code WorkbenchPanel}.
     * @see org.parosproxy.paros.view.View#getWorkbench()
     */
    public void addStatusPanel(AbstractPanel panel) {
        if (statusPanelList == null) {
            statusPanelList = createList();
        }
        statusPanelList.add(panel);
    }

    /**
     * Adds the given {@link AbstractParamPanel} to the view hook, to be later added to the {@link
     * org.parosproxy.paros.view.SessionDialog Session Properties dialogue}.
     *
     * @param panel the {@code AbstractParamPanel} that will be added to the Options dialogue.
     * @see org.parosproxy.paros.view.View#getSessionDialog()
     */
    public void addSessionPanel(AbstractParamPanel panel) {
        if (sessionPanelList == null) {
            sessionPanelList = createList();
        }
        sessionPanelList.add(panel);
    }

    /**
     * Adds the given {@link AbstractParamPanel} to the view hook, to be later added to the {@link
     * org.parosproxy.paros.view.OptionsDialog Options dialogue}.
     *
     * @param panel the {@code AbstractParamPanel} that will be added to the Options dialogue.
     * @see org.parosproxy.paros.view.View#getOptionsDialog(String)
     */
    public void addOptionPanel(AbstractParamPanel panel) {
        if (optionPanelList == null) {
            optionPanelList = createList();
        }
        optionPanelList.add(panel);
    }

    List<AbstractPanel> getWorkPanel() {
        return unmodifiableList(workPanelList);
    }

    List<AbstractPanel> getSelectPanel() {
        return unmodifiableList(selectPanelList);
    }

    List<AbstractPanel> getStatusPanel() {
        return unmodifiableList(statusPanelList);
    }

    List<AbstractParamPanel> getSessionPanel() {
        return unmodifiableList(sessionPanelList);
    }

    List<AbstractParamPanel> getOptionsPanel() {
        return unmodifiableList(optionPanelList);
    }

    /**
     * Adds the given {@link ContextPanelFactory} to the view hook, to be later added to the {@link
     * org.parosproxy.paros.view.View View}.
     *
     * @param contextPanelFactory the {@code ContextPanelFactory} that will be added to the {@code
     *     View}
     * @since 2.5.0
     */
    public void addContextPanelFactory(ContextPanelFactory contextPanelFactory) {
        if (contextPanelFactories == null) {
            contextPanelFactories = createList();
        }
        contextPanelFactories.add(contextPanelFactory);
    }

    /**
     * Gets the {@link ContextPanelFactory}s added to this hook.
     *
     * @return an unmodifiable {@code List} containing the added {@code ContextPanelFactory}s, never
     *     {@code null}.
     * @since 2.5.0
     */
    List<ContextPanelFactory> getContextPanelFactories() {
        return unmodifiableList(contextPanelFactories);
    }

    /**
     * Adds the given {@link Component} (usually {@link javax.swing.JButton JButton}, {@link
     * javax.swing.JToggleButton JToggleButton}, {@link javax.swing.JToolBar.Separator
     * JToolBar.Separator}) to the view hook, to be later added to the main tool bar panel.
     *
     * @param component the {@code component} that will be added to the main tool bar panel
     * @since 2.6.0
     * @see org.zaproxy.zap.view.MainToolbarPanel#addToolBarComponent(Component)
     * @see org.zaproxy.zap.view.MainToolbarPanel#removeToolBarComponent(Component)
     */
    public void addMainToolBarComponent(Component component) {
        if (mainToolBarComponents == null) {
            mainToolBarComponents = createList();
        }
        mainToolBarComponents.add(component);
    }

    /**
     * Gets the {@link Component}s added to this hook, for the main tool bar panel.
     *
     * @return an unmodifiable {@code List} containing the added {@code Component}s, never {@code
     *     null}.
     * @since 2.6.0
     */
    List<Component> getMainToolBarComponents() {
        return unmodifiableList(mainToolBarComponents);
    }

    /**
     * Creates an {@link ArrayList} with initial capacity of 1.
     *
     * <p>Majority of extensions just add one element.
     *
     * @return the {@code ArrayList}.
     */
    private static <T> List<T> createList() {
        return new ArrayList<>(1);
    }

    /**
     * Gets an unmodifiable list from the given list.
     *
     * @param list the list, might be {@code null}.
     * @return an unmodifiable list, never {@code null}.
     */
    private static <T> List<T> unmodifiableList(List<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Adds the given {@link DisplayedMessageChangedListener} to the view hook, to be later added to
     * the the {@link org.parosproxy.paros.view.View}s {@link
     * org.zaproxy.zap.extension.httppanel.HttpPanelRequest} ChangeListeners.
     *
     * @see
     *     org.zaproxy.zap.extension.httppanel.HttpPanelRequest#addDisplayedMessageChangedListener(DisplayedMessageChangedListener)
     * @param messageChangedListener the listener for the request panel.
     * @since 2.8.0
     */
    public void addRequestPanelDisplayedMessageChangedListener(
            DisplayedMessageChangedListener messageChangedListener) {
        if (requestPanelDisplayedMessageChangedListener == null) {
            requestPanelDisplayedMessageChangedListener = createList();
        }
        requestPanelDisplayedMessageChangedListener.add(messageChangedListener);
    }

    /**
     * Gets the {@link DisplayedMessageChangedListener}s added to this hook, for the RequestPanel.
     *
     * @return an unmodifiable {@code List} containing the added {@code
     *     DisplayedMessageChangedListener}s, never {@code null}.
     * @since 2.8.0
     */
    List<DisplayedMessageChangedListener> getRequestPanelDisplayedMessageChangedListeners() {
        return unmodifiableList(requestPanelDisplayedMessageChangedListener);
    }

    /**
     * Adds the given {@link DisplayedMessageChangedListener} to the view hook, to be later added to
     * the the {@link org.parosproxy.paros.view.View}s {@link
     * org.zaproxy.zap.extension.httppanel.HttpPanelResponse} ChangeListeners.
     *
     * @see
     *     org.zaproxy.zap.extension.httppanel.HttpPanelResponse#addDisplayedMessageChangedListener(DisplayedMessageChangedListener)
     * @param messageChangedListener the listener for the response panel.
     * @since 2.8.0
     */
    public void addResponsePanelDisplayedMessageChangedListener(
            DisplayedMessageChangedListener messageChangedListener) {
        if (responsePanelDisplayedMessageChangedListener == null) {
            responsePanelDisplayedMessageChangedListener = createList();
        }
        responsePanelDisplayedMessageChangedListener.add(messageChangedListener);
    }

    /**
     * Gets the {@link DisplayedMessageChangedListener}s added to this hook, for the ResponsePanel.
     *
     * @return an unmodifiable {@code List} containing the added {@code
     *     DisplayedMessageChangedListener}s, never {@code null}.
     * @since 2.8.0
     */
    List<DisplayedMessageChangedListener> getResponsePanelDisplayedMessageChangedListeners() {
        return unmodifiableList(responsePanelDisplayedMessageChangedListener);
    }
}
