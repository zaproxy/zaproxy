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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.ascan.ActiveScanPanel;
import org.zaproxy.zap.extension.search.SearchPanel;
import org.zaproxy.zap.view.messagecontainer.MessageContainer;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.messagecontainer.http.SelectableHttpMessagesContainer;
import org.zaproxy.zap.view.messagecontainer.http.SingleHttpMessageContainer;

/**
 * A {@code ExtensionPopupMenuMessageContainer} that exposes the state of {@code
 * HttpMessageContainer}s.
 *
 * @since 2.3.0
 * @see ExtensionPopupMenuMessageContainer
 * @see HttpMessageContainer
 * @see #isEnableForMessageContainer(MessageContainer)
 */
public class PopupMenuHttpMessageContainer extends ExtensionPopupMenuMessageContainer {

    private static final long serialVersionUID = -5266647403287261225L;

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

    /**
     * Flag that indicates that the menu button should be enabled if at least one child is enable
     * for the invoker.
     *
     * @see #processExtensionPopupChildren
     */
    private boolean buttonEnabledForEnableChildren;

    /**
     * Flag that indicates that the child pop up menus should be notified that the menu is being
     * invoked.
     */
    private boolean processExtensionPopupChildren;

    /** Flag that indicates that the menu accepts multiple selections. */
    private final boolean multiSelect;

    /**
     * Constructs a {@code PopupMenuHttpMessageContainer} with the given label and with no support
     * for multiple selected messages (the menu button will not be enabled when the invoker has
     * multiple selected messages).
     *
     * @param label the label of the menu
     * @see #setButtonStateOverriddenByChildren(boolean)
     * @see #setProcessExtensionPopupChildren(boolean)
     * @see #isEnableForMessageContainer(MessageContainer)
     */
    public PopupMenuHttpMessageContainer(String label) {
        this(label, false);
    }

    /**
     * Constructs a {@code PopupMenuHttpMessageContainer} with the given label and whether or not
     * the menu supports multiple selected messages (if {@code false} the menu button will not be
     * enabled when the invoker has multiple selected messages).
     *
     * @param label the label of the menu
     * @param multiSelect {@code true} if the menu supports multiple selected messages, {@code
     *     false} otherwise.
     * @see #setButtonStateOverriddenByChildren(boolean)
     * @see #setProcessExtensionPopupChildren(boolean)
     * @see #isEnableForMessageContainer(MessageContainer)
     */
    public PopupMenuHttpMessageContainer(String label, boolean multiSelect) {
        super(label);
        invoker = null;
        buttonEnabledForEnableChildren = true;
        processExtensionPopupChildren = true;

        this.multiSelect = multiSelect;
    }

    /**
     * Sets whether or not the menu button enabled state should be overridden by the enable state of
     * child menu items for the given invoker.
     *
     * <p>Default value is {@code true}.
     *
     * <p>The method {@code isProcessExtensionPopupChildren()} must return {@code true}, otherwise
     * this option will have no effect.
     *
     * <p><strong>Note:</strong> This option overrides the value returned by {@code
     * isButtonEnabledForHttpMessageContainerState(HttpMessageContainer)}.
     *
     * @param buttonEnabledForEnableChildren {@code true} if the button should be enabled, {@code
     *     false} otherwise.
     * @see #isButtonStateOverriddenByChildren()
     * @see #isProcessExtensionPopupChildren()
     * @see #isButtonEnabledForHttpMessageContainerState(HttpMessageContainer)
     * @see #isEnableForMessageContainer(MessageContainer)
     * @see ExtensionPopupMenuComponent#isEnableForMessageContainer(MessageContainer)
     */
    public final void setButtonStateOverriddenByChildren(boolean buttonEnabledForEnableChildren) {
        this.buttonEnabledForEnableChildren = buttonEnabledForEnableChildren;
    }

    /**
     * Tells whether or not the menu button enabled state should be overridden by the enable state
     * of child menu items for the given invoker.
     *
     * <p>The method {@code isProcessExtensionPopupChildren()} must return {@code true}, otherwise
     * this option will have no effect.
     *
     * @return {@code true} if the button will be enabled, {@code false} otherwise.
     * @see #setButtonStateOverriddenByChildren(boolean)
     * @see #isProcessExtensionPopupChildren()
     * @see ExtensionPopupMenuComponent#isEnableForMessageContainer(MessageContainer)
     */
    public final boolean isButtonStateOverriddenByChildren() {
        return buttonEnabledForEnableChildren;
    }

    /**
     * Sets whether or not the children should be notified and processed when the menu is invoked.
     *
     * <p>Default value is {@code true}.
     *
     * @param processExtensionPopupChildren {@code true} if the children should be notified and
     *     processed, {@code false} otherwise.
     * @see #isProcessExtensionPopupChildren()
     * @see #setButtonStateOverriddenByChildren(boolean)
     * @see #isEnableForMessageContainer(MessageContainer)
     * @see ExtensionPopupMenuComponent#isEnableForMessageContainer(MessageContainer)
     */
    public final void setProcessExtensionPopupChildren(boolean processExtensionPopupChildren) {
        this.processExtensionPopupChildren = processExtensionPopupChildren;
    }

    /**
     * Tells whether or not the children should be notified and processed when the menu is invoked.
     *
     * @return {@code true} if the children will be notified and processed, {@code false} otherwise.
     * @see #setProcessExtensionPopupChildren(boolean)
     */
    public final boolean isProcessExtensionPopupChildren() {
        return processExtensionPopupChildren;
    }

    /**
     * Tells whether or not the menu supports multiple selected messages. If {@code false} the menu
     * button will not be enabled when the invoker has multiple selected messages.
     *
     * @return {@code true} if the menu supports multiple selected messages, {@code false}
     *     otherwise.
     * @see #isButtonEnabledForNumberOfSelectedMessages(int)
     */
    public final boolean isMultiSelect() {
        return multiSelect;
    }

    /**
     * Returns the invoker of the pop up menu item.
     *
     * @return the invoker or {@code null} if has not been invoked or not a valid invoker.
     */
    protected final Invoker getInvoker() {
        return invoker;
    }

    /**
     * To determine if the menu is enable for the given message container following steps are done:
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
     * <p>To determine the menu's button enabled state the following steps are performed:
     *
     * <ol>
     *   <li>If {@code isProcessExtensionPopupChildren()} and {@code
     *       isButtonStateOverriddenByChildren()} return true, use the value returned from notifying
     *       and processing the child menus;
     *   <li>Otherwise call the method {@code
     *       isButtonEnabledForHttpMessageContainerState(HttpMessageContainer)} and use the returned
     *       value.
     * </ol>
     *
     * <strong>Note:</strong> If the menu is declared as not safe ({@code isSafe()}) the button will
     * be disabled if in {@code Mode.Safe} or if in {@code Mode.Protected} and not all the selected
     * messages are in scope.
     *
     * <p>Notifying and processing child menus
     *
     * <p>When the method {@code isProcessExtensionPopupChildren()} returns true, the method {@code
     * isEnableForComponent(Component)} is called on all child {@code ExtensionPopupMenuComponent}s.
     *
     * <p>All the child menus that implement {@code ExtensionPopupMenuComponent} will have the
     * methods {@code precedeWithSeparator()}, {@code succeedWithSeparator()}, {@code
     * getMenuIndex()} and {@code isSafe()} honoured, with the following caveats:
     *
     * <ul>
     *   <li>{@code precedeWithSeparator()} - the separator will only be added if there's already a
     *       menu component in the menu and if it is not a separator;
     *   <li>{@code succeedWithSeparator()} - the separator will be added always but removed if
     *       there's no item following it when the menu is ready to be shown;
     *   <li>{@code getMenuIndex()} - the menu index will be honoured only if the method {@code
     *       isOrderChildren()} returns {@code true};
     * </ul>
     *
     * The separators will be dynamically added and removed as needed when the pop up menu is shown.
     *
     * <p><strong>Note:</strong> Override of this method should be done with extra care as it might
     * break all the expected functionality.
     *
     * @see #isEnable(HttpMessageContainer)
     * @see #isEnableForInvoker(Invoker, HttpMessageContainer)
     * @see #getInvoker(HttpMessageContainer)
     * @see #isProcessExtensionPopupChildren()
     * @see #isButtonStateOverriddenByChildren()
     * @see #isButtonEnabledForHttpMessageContainerState(HttpMessageContainer)
     * @see #isSafe()
     * @see Mode
     */
    @Override
    public boolean isEnableForMessageContainer(MessageContainer<?> messageContainer) {
        invoker = null;

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

        boolean enabled = false;
        if (isProcessExtensionPopupChildren()) {
            boolean childrenEnable =
                    processExtensionPopupChildren(
                            PopupMenuUtils.getPopupMenuInvokerWrapper(httpMessageContainer));
            if (isButtonStateOverriddenByChildren()) {
                enabled = childrenEnable;
            }
        }

        if (!isProcessExtensionPopupChildren()
                || (isProcessExtensionPopupChildren() && !isButtonStateOverriddenByChildren())) {
            enabled = isButtonEnabledForHttpMessageContainerState(httpMessageContainer);
        }

        if (enabled && !isSafe()) {
            Mode mode = Control.getSingleton().getMode();
            if (mode.equals(Mode.protect)) {
                enabled = isSelectedMessagesInSessionScope(httpMessageContainer);
            } else if (mode.equals(Mode.safe)) {
                enabled = false;
            }
        }

        setEnabled(enabled);

        return true;
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
    protected static final Invoker getInvoker(HttpMessageContainer httpMessageContainer) {
        Invoker invoker;
        switch (httpMessageContainer.getName()) {
            case "History Table":
                invoker = Invoker.HISTORY_PANEL;
                break;
            case "treeSite":
                invoker = Invoker.SITES_PANEL;
                break;
            case "treeAlert":
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
            case "fuzz.httpfuzzerResultsContentPanel":
                invoker = Invoker.FUZZER_PANEL;
                break;
            case "ForcedBrowseMessageContainer":
                invoker = Invoker.FORCED_BROWSE_PANEL;
                break;
            default:
                invoker = Invoker.UNKNOWN;
        }
        return invoker;
    }

    /**
     * Tells whether or not the menu is enable for the given invoker (or optionally the given
     * message container).
     *
     * <p>By default, the menu is enable for all invokers.
     *
     * <p>The message container can be used to identify {@code Invoker#UNKNOWN} invokers by using
     * its name or component. No hard reference should be kept to the message container.
     *
     * @param invoker the invoker
     * @param httpMessageContainer the message container of the invoker
     * @return {@code true} if the menu is enable for the given invoker.
     */
    protected boolean isEnableForInvoker(
            Invoker invoker, HttpMessageContainer httpMessageContainer) {
        return true;
    }

    /**
     * Tells whether or not the button should be enabled for the state of the given message
     * container. Called from {@code isEnableForMessageContainer(MessageContainer)} when {@code
     * isButtonStateOverriddenByChildren()} returns {@code false}.
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
     * @see #isButtonStateOverriddenByChildren()
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
            SingleHttpMessageContainer singleContainer =
                    (SingleHttpMessageContainer) httpMessageContainer;
            if (!singleContainer.isEmpty()) {
                List<HttpMessage> selectedHttpMessages = new ArrayList<>(1);
                selectedHttpMessages.add(singleContainer.getMessage());
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
}
