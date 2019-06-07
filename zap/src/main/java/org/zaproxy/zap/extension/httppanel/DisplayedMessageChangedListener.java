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
package org.zaproxy.zap.extension.httppanel;

/**
 * Provides the ability to track changes of the displayed Message inside of the {@link
 * org.zaproxy.zap.extension.httppanel.HttpPanel}.
 *
 * <p>It can be registered in the {@link org.parosproxy.paros.extension.ExtensionHookView} or
 * directly in a {@link org.zaproxy.zap.extension.httppanel.HttpPanel}
 *
 * @see
 *     org.zaproxy.zap.extension.httppanel.HttpPanel#addDisplayedMessageChangedListener(DisplayedMessageChangedListener)
 * @since 2.8.0
 */
public interface DisplayedMessageChangedListener {

    /**
     * Is called when the displayed message of the {@link
     * org.zaproxy.zap.extension.httppanel.HttpPanel} changes
     *
     * @param oldMessage can be {@code null} (i.e. when there was no message displayed in the panel)
     * @param newMessage can be {@code null} (i.e. when the panel is cleared)
     */
    void messageChanged(Message oldMessage, Message newMessage);
}
