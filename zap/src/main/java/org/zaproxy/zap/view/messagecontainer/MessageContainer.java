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
package org.zaproxy.zap.view.messagecontainer;

import java.awt.Component;
import org.zaproxy.zap.extension.httppanel.Message;

/**
 * Base interface for GUI components that contain {@code Message}s, for example, {@code JList},
 * {@code JTable} and {@code JTextArea}.
 *
 * <p>Hides the implementation details and the GUI component used to show the messages, while
 * providing access to those messages and to some extent its state (for example, with {@code
 * SelectableMessagesContainer} to get the selected messages).
 *
 * <p>The containers can opt to expose only a snapshot of the container state, instead of giving
 * access to the current (always updated) state, depending on how they are accessed. For example,
 * when the container state is exposed to a pop up menu invocation (which is a short living action)
 * the container might return only the selection state at that point in time, so further queries to
 * the exposed selection state will, always, return the same values.
 *
 * <p>No assumptions should be made on the implementation types used to return the container state.
 * For example, if the {@code List}s are modifiable or not.
 *
 * <p><strong>Note:</strong> No hard {@code Reference} should be kept to the messages containers
 * longer than needed to do the actual work as it would prevent the unloading of the classes (when
 * the corresponding add-ons are uninstalled).
 *
 * @param <T> the type of message in this container
 * @see Message
 * @see MultipleMessagesContainer
 * @see SelectableMessagesContainer
 * @see SingleMessageContainer
 * @see java.lang.ref.Reference
 * @see java.lang.ref.SoftReference
 * @see java.lang.ref.WeakReference
 * @since 2.3.0
 */
public interface MessageContainer<T extends Message> {

    /**
     * Returns the {@code Class} of the container's message, for use as runtime type token.
     *
     * @return the {@code Class} of the container's message.
     */
    Class<T> getMessageClass();

    /**
     * Returns the name of the message container.
     *
     * <p>The name should be unique among other containers since it will be used to uniquely
     * identify them.
     *
     * @return the name of the container, never {@code null}.
     */
    String getName();

    /**
     * Returns the GUI component of the message container.
     *
     * <p>Actual type of the component depends on the implementation, example component types:
     * {@code JList}, {@code JTable}, {@code JTextArea}, ...
     *
     * @return the GUI component of the container, never {@code null}.
     */
    Component getComponent();

    /**
     * Tells whether or not the message container is empty, that is, doesn't contain any message.
     *
     * @return {@code true} if the container is empty, {@code false} otherwise.
     */
    boolean isEmpty();
}
