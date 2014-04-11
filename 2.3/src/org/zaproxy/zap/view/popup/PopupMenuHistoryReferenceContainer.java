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

import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.view.messagecontainer.MessageContainer;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.messagecontainer.http.SelectableHistoryReferencesContainer;
import org.zaproxy.zap.view.messagecontainer.http.SingleHistoryReferenceContainer;

/**
 * A {@code PopupMenuHttpMessageContainer} that exposes the {@code HistoryReference}s of {@code HttpMessageContainer}s.
 * 
 * @since 2.3.0
 * @see PopupMenuHttpMessageContainer
 * @see HttpMessageContainer
 * @see #isEnableForMessageContainer(MessageContainer)
 */
public class PopupMenuHistoryReferenceContainer extends PopupMenuHttpMessageContainer {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a {@code PopupMenuHistoryReferenceContainer} with the given label and with no support for multiple selected
     * messages (the menu button will not be enabled when the invoker has multiple selected messages).
     * 
     * @param label the label of the menu
     * @see #setButtonStateOverriddenByChildren(boolean)
     * @see #setProcessExtensionPopupChildren(boolean)
     * @see #isEnableForMessageContainer(MessageContainer)
     */
    public PopupMenuHistoryReferenceContainer(String label) {
        this(label, false);
    }

    /**
     * Constructs a {@code PopupMenuHistoryReferenceContainer} with the given label and whether or not the menu supports
     * multiple selected messages (if {@code false} the menu button will not be enabled when the invoker has multiple selected
     * messages).
     * 
     * @param label the label of the menu
     * @param multiSelect {@code true} if the menu supports multiple selected messages, {@code false} otherwise.
     * @see #setButtonStateOverriddenByChildren(boolean)
     * @see #setProcessExtensionPopupChildren(boolean)
     * @see #isEnableForMessageContainer(MessageContainer)
     */
    public PopupMenuHistoryReferenceContainer(String label, boolean multiSelect) {
        super(label, multiSelect);
    }

    /**
     * Tells whether or not the menu is enable for the given HTTP message container.
     * <p>
     * By default is enable for {@code SingleHistoryReferenceContainer}s and {@code SelectableHistoryReferencesContainer}s.
     * </p>
     * <p>
     * Normally overridden if other implementations of {@code HttpMessageContainer} are supported. The methods
     * {@code getSelectedMessages(HttpMessageContainer)}, {@code getSelectedHistoryReferences(HttpMessageContainer)} and
     * {@code getNumberOfSelectedMessages(HttpMessageContainer)} might need to be overridden accordingly to the supported
     * implementations.
     * </p>
     * 
     * @param httpMessageContainer the message container that will be evaluated
     * @return {@code true} if the given message container is a {@code SingleHistoryReferenceContainer} or
     *         {@code SelectableHistoryReferencesContainer}.
     * @see #getSelectedMessages(HttpMessageContainer)
     * @see #getSelectedHistoryReferences(HttpMessageContainer)
     * @see #getNumberOfSelectedMessages(HttpMessageContainer)
     * @see SingleHistoryReferenceContainer
     * @see SelectableHistoryReferencesContainer
     */
    @Override
    protected boolean isEnable(HttpMessageContainer httpMessageContainer) {
        if (httpMessageContainer instanceof SelectableHistoryReferencesContainer
                || httpMessageContainer instanceof SingleHistoryReferenceContainer) {
            return true;
        }

        return false;
    }

    /**
     * Tells whether or not the button should be enabled for the selected messages of the given message container.
     * <p>
     * Defaults to call the method {@code isButtonEnabledForSelectedHistoryReferences(List)} with the selected messages obtained
     * by calling the method {@code getSelectedHistoryReferences(HttpMessageContainer)}, with the given message container as
     * parameter.
     * </p>
     * <p>
     * Normally overridden if other implementations of {@code HttpMessageContainer} are supported.
     * </p>
     * 
     * @param httpMessageContainer the container that will be evaluated
     * @return {@code true} if the button should be enabled for the selected messages, {@code false} otherwise.
     * @see #isButtonEnabledForSelectedHistoryReferences(List)
     * @see #getSelectedHistoryReferences(HttpMessageContainer)
     */
    @Override
    protected boolean isButtonEnabledForSelectedMessages(HttpMessageContainer httpMessageContainer) {
        return isButtonEnabledForSelectedHistoryReferences(getSelectedHistoryReferences(httpMessageContainer));
    }

    /**
     * Tells whether or not the selected messages of the given message container are in scope.
     * <p>
     * By default, the selected messages are obtained by calling the method getSelectedHistoryReferences(httpMessageContainer)
     * with the given message container as parameter and for each selected message is called the method
     * {@code Session#isInScope(HistoryReference)} with the message as parameter.
     * </p>
     * <p>
     * Normally overridden if other implementations of {@code HttpMessageContainer} are supported. Default are
     * {@code SingleHistoryReferenceContainer} and {@code SelectableHistoryReferencesContainer}.
     * </p>
     * 
     * @param httpMessageContainer the container that will be evaluated
     * @return {@code true} if all the selected messages are in scope, {@code false} otherwise.
     * @see #isEnableForMessageContainer(MessageContainer)
     * @see Session#isInScope(HistoryReference)
     */
    @Override
    protected boolean isSelectedMessagesInSessionScope(HttpMessageContainer httpMessageContainer) {
        final Session session = Model.getSingleton().getSession();
        for (HistoryReference historyReference : getSelectedHistoryReferences(httpMessageContainer)) {
            if (!session.isInScope(historyReference)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the selected messages of the given message container.
     * <p>
     * By default it returns the selected messages from {@code SelectableHistoryReferencesContainer}s and for
     * {@code SingleHistoryReferenceContainer}s returns the contained message or empty {@code List} if none.
     * </p>
     * <p>
     * Normally overridden if other implementations of {@code HttpMessageContainer} are supported. Default are
     * {@code SingleHistoryReferenceContainer} and {@code SelectableHistoryReferencesContainer}.
     * </p>
     * 
     * @param httpMessageContainer the container that will be evaluated
     * @return a {@code List} containing the selected messages
     * @see #isButtonEnabledForSelectedMessages(List)
     * @see #isSelectedMessagesInSessionScope(HttpMessageContainer)
     * @see SingleHistoryReferenceContainer
     * @see SelectableHistoryReferencesContainer
     */
    protected List<HistoryReference> getSelectedHistoryReferences(HttpMessageContainer httpMessageContainer) {
        if (httpMessageContainer instanceof SelectableHistoryReferencesContainer) {
            return ((SelectableHistoryReferencesContainer) httpMessageContainer).getSelectedHistoryReferences();
        } else if (httpMessageContainer instanceof SingleHistoryReferenceContainer) {
            SingleHistoryReferenceContainer singleContainer = (SingleHistoryReferenceContainer) httpMessageContainer;
            if (!singleContainer.isEmpty()) {
                List<HistoryReference> selectedHistoryReferences = new ArrayList<>(1);
                selectedHistoryReferences.add(singleContainer.getHistoryReference());
                return selectedHistoryReferences;
            }
        }

        return Collections.emptyList();
    }

    /**
     * Tells whether or not the button should be enabled for the given selected messages.
     * <p>
     * By default, it returns {@code true} unless the method {@code isButtonEnabledForHistoryReference(HistoryReference)}
     * returns false for one of the selected messages.
     * </p>
     * 
     * @param historyReferences the selected messages in the message container
     * @return {@code true} if the button should be enabled for the given selected messages, {@code false} otherwise.
     * @see #isButtonEnabledForHistoryReference(HistoryReference)
     */
    protected boolean isButtonEnabledForSelectedHistoryReferences(List<HistoryReference> historyReferences) {
        for (HistoryReference historyReference : historyReferences) {
            if (historyReference != null && !isButtonEnabledForHistoryReference(historyReference)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tells whether or not the button should be enabled for the given selected message.
     * <p>
     * By default, it returns {@code true} if it is not a temporary message.
     * 
     * @param historyReference the selected message, never {@code null}
     * @return {@code true} if the button should be enabled for the given selected message, {@code false} otherwise.
     * @see HistoryReference#TYPE_TEMPORARY
     */
    protected boolean isButtonEnabledForHistoryReference(HistoryReference historyReference) {
        return historyReference.getHistoryType() != HistoryReference.TYPE_TEMPORARY;
    }

}
