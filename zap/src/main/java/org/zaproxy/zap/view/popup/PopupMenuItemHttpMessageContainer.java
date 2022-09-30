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
package org.zaproxy.zap.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.alert.AlertPanel;
import org.zaproxy.zap.extension.ascan.ActiveScanPanel;
import org.zaproxy.zap.extension.search.SearchPanel;
import org.zaproxy.zap.view.messagecontainer.MessageContainer;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.messagecontainer.http.SelectableHttpMessagesContainer;
import org.zaproxy.zap.view.messagecontainer.http.SingleHttpMessageContainer;

/**
 * A {@code ExtensionPopupMenuItemMessageContainer} that exposes the state of {@code
 * HttpMessageContainer}s.
 *
 * @since 2.3.0
 * @see ExtensionPopupMenuItemMessageContainer
 * @see HttpMessageContainer
 * @see #isEnableForMessageContainer(MessageContainer)
 */
@SuppressWarnings("serial")
public abstract class PopupMenuItemHttpMessageContainer
        extends ExtensionPopupMenuItemMessageContainer {

    private static final long serialVersionUID = -4769111731197641466L;

    private static final Logger logger =
            LogManager.getLogger(PopupMenuItemHttpMessageContainer.class);

    /** The invokers of the the pop up menu. */
    protected static enum Invoker {
        SITES_PANEL,
        HISTORY_PANEL,
        ALERTS_PANEL,
        ACTIVE_SCANNER_PANEL,
        SEARCH_PANEL,
        /**
         * The panel where spiders' HTTP messages are shown.
         *
         * @since 2.5.0
         */
        SPIDER_PANEL,
        FUZZER_PANEL,
        FORCED_BROWSE_PANEL,
        UNKNOWN
    }

    /** The current invoker of the menu, {@code null} if none or invalid. */
    private Invoker invoker;

    /** The invoker message container. Might be {@code null}. */
    private HttpMessageContainer httpMessageContainer;

    /** Flag that indicates that the menu item accepts multiple selections. */
    private final boolean multiSelect;

    /**
     * Constructs a {@code PopupMenuItemHttpMessageContainer} with the given label and with no
     * support for multiple selected messages (the menu item button will not be enabled when the
     * invoker has multiple selected messages).
     *
     * @param label the label of the menu
     * @see #isEnableForMessageContainer(MessageContainer)
     */
    public PopupMenuItemHttpMessageContainer(String label) {
        this(label, false);
    }

    /**
     * Constructs a {@code PopupMenuItemHttpMessageContainer} with the given label and whether or
     * not the menu item supports multiple selected messages (if {@code false} the menu item button
     * will not be enabled when the invoker has multiple selected messages).
     *
     * @param label the label of the menu
     * @param multiSelect {@code true} if the menu supports multiple selected messages, {@code
     *     false} otherwise.
     * @see #isEnableForMessageContainer(MessageContainer)
     */
    public PopupMenuItemHttpMessageContainer(String label, boolean multiSelect) {
        super(label);
        invoker = null;
        httpMessageContainer = null;

        this.multiSelect = multiSelect;

        addActionListener(new PerformActionsActionListener());
    }

    /**
     * Tells whether or not the menu item supports multiple selected messages. If {@code false} the
     * menu item button will not be enabled when the invoker has multiple selected messages.
     *
     * @return {@code true} if the menu item supports multiple selected messages, {@code false}
     *     otherwise.
     * @see #isButtonEnabledForNumberOfSelectedMessages(int)
     */
    protected boolean isMultiSelect() {
        return multiSelect;
    }

    /**
     * Returns the invoker of the pop up menu item.
     *
     * @return the invoker or {@code null} if has not been invoked or not a valid invoker.
     */
    protected Invoker getInvoker() {
        return invoker;
    }

    /**
     * To determine if the menu item is enable for the given message container following steps are
     * done:
     *
     * <ol>
     *   <li>Check if message container is {@code HttpMessageContainer}, if not returns immediately
     *       with {@code false};
     *   <li>Call the method {@code isEnable(HttpMessageContainer)}, if it doesn't return {@code
     *       true} the method returns immediately with {@code false};
     *   <li>Call the method {@code isEnableForInvoker(Invoker, HttpMessageContainer)}, if it
     *       doesn't return {@code true} the method returns immediately with {@code false}.
     * </ol>
     *
     * Otherwise the menu will be enable for the given message container.
     *
     * <p>To determine if menu item's button is enabled it is called the method {@code
     * isButtonEnabledForHttpMessageContainerState(HttpMessageContainer)} and used its the return
     * value.
     *
     * <p><strong>Note:</strong> If the menu item is declared as not safe ({@code isSafe()}) the
     * button will be disabled if in {@code Mode.Safe} or if in {@code Mode.Protected} and not all
     * the selected messages are in scope.
     *
     * <p><strong>Note:</strong> Override of this method should be done with extra care as it might
     * break all the expected functionality.
     *
     * @see #isEnable(HttpMessageContainer)
     * @see #isEnableForInvoker(Invoker, HttpMessageContainer)
     * @see #getInvoker(HttpMessageContainer)
     * @see #isButtonEnabledForHttpMessageContainerState(HttpMessageContainer)
     */
    @Override
    public boolean isEnableForMessageContainer(MessageContainer<?> messageContainer) {
        resetState();

        if (!(messageContainer instanceof HttpMessageContainer)) {
            return false;
        }

        HttpMessageContainer httpMessageContainer = (HttpMessageContainer) messageContainer;

        if (!isEnable(httpMessageContainer)) {
            return false;
        }

        invoker = getInvoker(httpMessageContainer);

        if (!isEnableForInvoker(invoker, httpMessageContainer)) {
            invoker = null;
            return false;
        }

        boolean enabled = isButtonEnabledForHttpMessageContainerState(httpMessageContainer);

        if (enabled && !isSafe()) {
            Mode mode = Control.getSingleton().getMode();
            if (mode.equals(Mode.protect)) {
                enabled = isSelectedMessagesInSessionScope(httpMessageContainer);
            } else if (mode.equals(Mode.safe)) {
                enabled = false;
            }
        }

        if (enabled) {
            this.httpMessageContainer = httpMessageContainer;
        }

        setEnabled(enabled);

        return true;
    }

    /**
     * Clears the reference to the invoker of this pop up menu item, if this was not the selected
     * menu.
     *
     * @since 2.4.0
     */
    @Override
    public void dismissed(ExtensionPopupMenuComponent selectedMenuComponent) {
        super.dismissed(selectedMenuComponent);

        if (this != selectedMenuComponent) {
            resetState();
        }
    }

    /**
     * Resets the state of this menu item by setting the {@code invoker} and {@code
     * httpMessageContainer} to {@code null}.
     *
     * @see #invoker
     * @see #httpMessageContainer
     */
    private void resetState() {
        invoker = null;
        httpMessageContainer = null;
    }

    /**
     * Tells whether or not the menu is enable for the given HTTP message container.
     *
     * <p>By default is enable for {@code SingleHttpMessageContainer}s and {@code
     * SelectableHttpMessagesContainer}s.
     *
     * <p>Normally overridden if other implementations of {@code HttpMessageContainer} are
     * supported. The methods {@code getSelectedMessages(HttpMessageContainer)} and {@code
     * getNumberOfSelectedMessages(HttpMessageContainer)} might need to be overridden accordingly to
     * the supported implementations.
     *
     * @param httpMessageContainer the message container that will be evaluated
     * @return {@code true} if the given message container is a {@code SingleHttpMessageContainer}
     *     or {@code SelectableHttpMessagesContainer}.
     * @see #getSelectedMessages(HttpMessageContainer)
     * @see #getNumberOfSelectedMessages(HttpMessageContainer)
     * @see SingleHttpMessageContainer
     * @see SelectableHttpMessagesContainer
     */
    protected boolean isEnable(HttpMessageContainer httpMessageContainer) {
        if (httpMessageContainer instanceof SelectableHttpMessagesContainer
                || httpMessageContainer instanceof SingleHttpMessageContainer) {
            return true;
        }

        return false;
    }

    /**
     * Returns the {@code Invoker} behind the message container.
     *
     * @param httpMessageContainer the message container that will be evaluated
     * @return the invoker or {@code Invoker#UNKNOWN} if the message container was not identified.
     * @see Invoker
     */
    @SuppressWarnings("deprecation")
    private static Invoker getInvoker(HttpMessageContainer httpMessageContainer) {
        Invoker invoker;
        switch (httpMessageContainer.getName()) {
                // TODO Allow to inject the HTTP fuzz component
            case "History Table":
                invoker = Invoker.HISTORY_PANEL;
                break;
            case "treeSite":
                invoker = Invoker.SITES_PANEL;
                break;
            case AlertPanel.ALERT_TREE_PANEL_NAME:
                invoker = Invoker.ALERTS_PANEL;
                break;
            case SearchPanel.HTTP_MESSAGE_CONTAINER_NAME:
                invoker = Invoker.SEARCH_PANEL;
                break;
            case org.zaproxy.zap.extension.spider.SpiderPanel.HTTP_MESSAGE_CONTAINER_NAME:
                invoker = Invoker.SPIDER_PANEL;
                break;
            case ActiveScanPanel.MESSAGE_CONTAINER_NAME:
                invoker = Invoker.ACTIVE_SCANNER_PANEL;
                break;
            case "ForcedBrowseMessageContainer":
                invoker = Invoker.FORCED_BROWSE_PANEL;
                break;
            case "fuzz.httpfuzzerResultsContentPanel":
                invoker = Invoker.FUZZER_PANEL;
                break;
            default:
                invoker = Invoker.UNKNOWN;
        }
        return invoker;
    }

    /**
     * Tells whether or not the menu item is enable for the given invoker (or optionally the given
     * message container).
     *
     * <p>By default, the menu item is enable for all invokers.
     *
     * <p>The message container can be used to identify {@code Invoker#UNKNOWN} invokers by using
     * its name or component. No hard reference should be kept to the message container.
     *
     * @param invoker the invoker
     * @param httpMessageContainer the message container of the invoker
     * @return {@code true} if the menu item is enable for the given invoker.
     */
    protected boolean isEnableForInvoker(
            Invoker invoker, HttpMessageContainer httpMessageContainer) {
        return true;
    }

    /**
     * Tells whether or not the button should be enabled for the state of the given message
     * container. Called from {@code isEnableForMessageContainer(MessageContainer)}.
     *
     * <p>By default, is only enabled if both methods {@code
     * isButtonEnabledForNumberOfSelectedMessages(HttpMessageContainer)} and {@code
     * isButtonEnabledForSelectedMessages(HttpMessageContainer)} (called only if the former method
     * returns {@code true}) return {@code true}.<br>
     *
     * <p>Not normally overridden.
     *
     * @param httpMessageContainer the message container that will be evaluated
     * @return {@code true} if the button should be enabled for the message container, {@code false}
     *     otherwise.
     * @see #isEnableForMessageContainer(MessageContainer)
     * @see #isButtonEnabledForNumberOfSelectedMessages(HttpMessageContainer)
     * @see #isButtonEnabledForSelectedMessages(HttpMessageContainer)
     */
    protected boolean isButtonEnabledForHttpMessageContainerState(
            HttpMessageContainer httpMessageContainer) {
        boolean enabled = isButtonEnabledForNumberOfSelectedMessages(httpMessageContainer);

        if (enabled) {
            enabled = isButtonEnabledForSelectedMessages(httpMessageContainer);
        }

        return enabled;
    }

    /**
     * Tells whether or not the button should be enabled for the number of selected messages of the
     * given message container.
     *
     * <p>Defaults to call the method {@code isButtonEnabledForNumberOfSelectedMessages(int)} with
     * the number of selected messages obtained by calling the method {@code
     * getNumberOfSelectedMessages(HttpMessageContainer)}, with the given message container as
     * parameter.
     *
     * <p>Normally overridden if other implementations of {@code HttpMessageContainer} are
     * supported.
     *
     * @param httpMessageContainer the container that will be evaluated
     * @return {@code true} if the button should be enabled for the number of selected message,
     *     {@code false} otherwise.
     * @see #isButtonEnabledForNumberOfSelectedMessages(int)
     * @see #getNumberOfSelectedMessages(HttpMessageContainer)
     */
    protected boolean isButtonEnabledForNumberOfSelectedMessages(
            HttpMessageContainer httpMessageContainer) {
        return isButtonEnabledForNumberOfSelectedMessages(
                getNumberOfSelectedMessages(httpMessageContainer));
    }

    /**
     * Tells whether or not the button should be enabled for the selected messages of the given
     * message container.
     *
     * <p>Defaults to call the method {@code isButtonEnabledForSelectedMessages(List)} with the
     * selected messages obtained by calling the method {@code
     * getSelectedMessages(HttpMessageContainer)}, with the given message container as parameter.
     *
     * <p>Normally overridden if other implementations of {@code HttpMessageContainer} are
     * supported.
     *
     * @param httpMessageContainer the container that will be evaluated
     * @return {@code true} if the button should be enabled for the selected messages, {@code false}
     *     otherwise.
     * @see #isButtonEnabledForSelectedMessages(List)
     * @see #getSelectedMessages(HttpMessageContainer)
     */
    protected boolean isButtonEnabledForSelectedMessages(
            HttpMessageContainer httpMessageContainer) {
        return isButtonEnabledForSelectedMessages(getSelectedMessages(httpMessageContainer));
    }

    /**
     * Returns the number of selected messages of the given message container.
     *
     * <p>By default it returns the number of selected messages from {@code
     * SelectableHttpMessagesContainer}s and for {@code SingleHttpMessageContainer}s returns 1 if it
     * contains a message, 0 otherwise.
     *
     * <p>Normally overridden if other implementations of {@code HttpMessageContainer} are
     * supported. Default are {@code SingleHttpMessageContainer} and {@code
     * SelectableHttpMessagesContainer}.
     *
     * @param httpMessageContainer the container that will be evaluated
     * @return the number of selected messages in the message container
     * @see SingleHttpMessageContainer
     * @see SelectableHttpMessagesContainer
     */
    protected int getNumberOfSelectedMessages(HttpMessageContainer httpMessageContainer) {
        if (httpMessageContainer instanceof SelectableHttpMessagesContainer) {
            return ((SelectableHttpMessagesContainer) httpMessageContainer)
                    .getNumberOfSelectedMessages();
        } else if (httpMessageContainer instanceof SingleHttpMessageContainer) {
            return ((SingleHttpMessageContainer) httpMessageContainer).isEmpty() ? 0 : 1;
        }

        return 0;
    }

    /**
     * Tells whether or not the button should be enabled for the given number of selected messages.
     *
     * <p>By default, it returns {@code false} when there are no messages selected or when {@code
     * isMultiSelect()} returns {@code true} and there is more than one message selected.
     *
     * <p>If overridden the method {@code isMultiSelect()} should be taken into account.
     *
     * <p>
     *
     * @param numberOfSelectedMessages the number of selected messages in the message container
     * @return {@code true} if the button should be enabled for the given number of selected
     *     messages, {@code false} otherwise.
     * @see #isMultiSelect()
     */
    protected boolean isButtonEnabledForNumberOfSelectedMessages(int numberOfSelectedMessages) {
        if (numberOfSelectedMessages == 0) {
            return false;
        } else if (numberOfSelectedMessages > 1 && !isMultiSelect()) {
            return false;
        }
        return true;
    }

    /**
     * Returns the selected messages of the given message container.
     *
     * <p>By default it returns the selected messages from {@code SelectableHttpMessagesContainer}s
     * and for {@code SingleHttpMessageContainer}s returns the contained message or empty {@code
     * List} if none.
     *
     * <p>Normally overridden if other implementations of {@code HttpMessageContainer} are
     * supported. Default are {@code SingleHttpMessageContainer} and {@code
     * SelectableHttpMessagesContainer}.
     *
     * @param httpMessageContainer the container that will be evaluated
     * @return a {@code List} containing the selected messages
     * @see #isButtonEnabledForSelectedMessages(List)
     * @see #isSelectedMessagesInSessionScope(HttpMessageContainer)
     * @see SingleHttpMessageContainer
     * @see SelectableHttpMessagesContainer
     */
    protected List<HttpMessage> getSelectedMessages(HttpMessageContainer httpMessageContainer) {
        if (httpMessageContainer instanceof SelectableHttpMessagesContainer) {
            return ((SelectableHttpMessagesContainer) httpMessageContainer).getSelectedMessages();
        } else if (httpMessageContainer instanceof SingleHttpMessageContainer) {
            SingleHttpMessageContainer singleMessageContainer =
                    (SingleHttpMessageContainer) httpMessageContainer;
            if (!singleMessageContainer.isEmpty()) {
                List<HttpMessage> selectedHttpMessages = new ArrayList<>(1);
                selectedHttpMessages.add(
                        (((SingleHttpMessageContainer) httpMessageContainer).getMessage()));
                return selectedHttpMessages;
            }
        }

        return Collections.emptyList();
    }

    /**
     * Tells whether or not the button should be enabled for the given selected messages.
     *
     * <p>By default, it returns {@code true} unless the method {@code
     * isButtonEnabledForSelectedHttpMessage(HttpMessage)} returns false for one of the selected
     * messages.
     *
     * @param httpMessages the selected messages in the message container
     * @return {@code true} if the button should be enabled for the given selected messages, {@code
     *     false} otherwise.
     * @see #isButtonEnabledForSelectedHttpMessage(HttpMessage)
     */
    protected boolean isButtonEnabledForSelectedMessages(List<HttpMessage> httpMessages) {
        for (HttpMessage httpMessage : httpMessages) {
            if (httpMessage != null && !isButtonEnabledForSelectedHttpMessage(httpMessage)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tells whether or not the button should be enabled for the given selected message.
     *
     * <p>By default, it returns {@code true}.
     *
     * @param httpMessage the selected message, never {@code null}
     * @return {@code true} if the button should be enabled for the given selected message, {@code
     *     false} otherwise.
     */
    protected boolean isButtonEnabledForSelectedHttpMessage(HttpMessage httpMessage) {
        return true;
    }

    /**
     * Tells whether or not the selected messages of the given message container are in scope.
     *
     * <p>By default, the selected messages are obtained by calling the method
     * getSelectedMessages(httpMessageContainer) with the given message container as parameter and
     * for each selected message is called the method {@code HttpMessage#isInScope()}.
     *
     * <p>Normally overridden if other implementations of {@code HttpMessageContainer} are
     * supported. Default are {@code SingleHttpMessageContainer} and {@code
     * SelectableHttpMessagesContainer}.
     *
     * @param httpMessageContainer the container that will be evaluated
     * @return {@code true} if all the selected messages are in scope, {@code false} otherwise.
     * @see #isEnableForMessageContainer(MessageContainer)
     * @see HttpMessage#isInScope()
     */
    protected boolean isSelectedMessagesInSessionScope(HttpMessageContainer httpMessageContainer) {
        for (HttpMessage httpMessage : getSelectedMessages(httpMessageContainer)) {
            if (httpMessage != null && !httpMessage.isInScope()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Performs the actions on the the given message container.
     *
     * <p>Defaults to call the method {@code performActions(List)} with the selected messages
     * obtained by calling the method {@code getSelectedMessages(HttpMessageContainer)}, with the
     * given message container as parameter.
     *
     * <p>Normally overridden if other implementations of {@code HttpMessageContainer} are supported
     * (or not the desired behaviour).
     *
     * @param httpMessageContainer the container with the selected messages
     * @see #performActions(List)
     * @see #getSelectedMessages(HttpMessageContainer)
     */
    protected void performActions(HttpMessageContainer httpMessageContainer) {
        performActions(getSelectedMessages(httpMessageContainer));
    }

    /**
     * Performs the actions on all the the given messages.
     *
     * <p>Defaults to call the method {@code performAction(HttpMessage)} for each message (with the
     * message as parameter).
     *
     * <p>Normally overridden if other implementations of {@code HttpMessageContainer} are supported
     * (or not the desired behaviour).
     *
     * @param httpMessages the messages that will be used to perform the actions
     * @see #performAction(HttpMessage)
     */
    protected void performActions(List<HttpMessage> httpMessages) {
        for (HttpMessage httpMessage : httpMessages) {
            if (httpMessage != null) {
                performAction(httpMessage);
            }
        }
    }

    /**
     * Performs an action on the given message.
     *
     * @param httpMessage the message, never {@code null}
     */
    protected abstract void performAction(HttpMessage httpMessage);

    /** The action listener responsible to call the required methods to perform the actions. */
    private class PerformActionsActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            logger.debug(
                    "actionPerformed {} {}",
                    invoker != null ? invoker.name() : "null invoker",
                    evt.getActionCommand());

            try {
                performActions(httpMessageContainer);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            resetState();
        }
    }
}
