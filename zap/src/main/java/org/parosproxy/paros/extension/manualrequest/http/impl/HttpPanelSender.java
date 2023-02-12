/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.parosproxy.paros.extension.manualrequest.http.impl;

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLException;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import org.apache.commons.httpclient.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.PersistentConnectionListener;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.HttpPanelViewModelUtils;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.network.HttpRedirectionValidator;
import org.zaproxy.zap.network.HttpRequestConfig;

/**
 * Knows how to send {@link HttpMessage} objects.
 *
 * @deprecated (2.12.0) Replaced by Requester add-on.
 */
@Deprecated
public class HttpPanelSender implements org.parosproxy.paros.extension.manualrequest.MessageSender {

    private static final Logger logger = LogManager.getLogger(HttpPanelSender.class);

    private final HttpPanelResponse responsePanel;
    private ExtensionHistory extension;
    private ExtensionAntiCSRF extAntiCSRF;

    private HttpSender delegate;

    private JToggleButton fixContentLength = null;
    private JToggleButton followRedirect = null;
    private JToggleButton useTrackingSessionState = null;
    private JToggleButton useCookies = null;
    private JToggleButton useCsrf = null;

    private List<PersistentConnectionListener> persistentConnectionListener = new ArrayList<>();

    public HttpPanelSender(HttpPanelRequest requestPanel, HttpPanelResponse responsePanel) {
        this.responsePanel = responsePanel;

        extAntiCSRF =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.class);

        delegate = new HttpSender(HttpSender.MANUAL_REQUEST_INITIATOR);
        requestPanel.addOptions(
                getButtonUseTrackingSessionState(), HttpPanel.OptionsLocation.AFTER_COMPONENTS);
        requestPanel.addOptions(getButtonUseCookies(), HttpPanel.OptionsLocation.AFTER_COMPONENTS);
        requestPanel.addOptions(
                getButtonFollowRedirects(), HttpPanel.OptionsLocation.AFTER_COMPONENTS);
        requestPanel.addOptions(
                getButtonFixContentLength(), HttpPanel.OptionsLocation.AFTER_COMPONENTS);
        if (extAntiCSRF != null) {
            requestPanel.addOptions(getButtonUseCsrf(), HttpPanel.OptionsLocation.AFTER_COMPONENTS);
        }

        updateButtonTrackingSessionState();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void handleSendMessage(Message aMessage) throws IllegalArgumentException, IOException {
        final HttpMessage httpMessage = (HttpMessage) aMessage;
        // Reset the user before sending (e.g. Forced User mode sets the user, if needed).
        httpMessage.setRequestingUser(null);

        if (getButtonFixContentLength().isSelected()) {
            HttpPanelViewModelUtils.updateRequestContentLength(httpMessage);
        }
        try {
            final ModeRedirectionValidator redirectionValidator = new ModeRedirectionValidator();
            boolean followRedirects = getButtonFollowRedirects().isSelected();

            if (extAntiCSRF != null && getButtonUseCsrf().isSelected()) {
                extAntiCSRF.regenerateAntiCsrfToken(httpMessage, delegate::sendAndReceive);
            }

            if (followRedirects) {
                delegate.sendAndReceive(
                        httpMessage,
                        HttpRequestConfig.builder()
                                .setRedirectionValidator(redirectionValidator)
                                .build());
            } else {
                delegate.sendAndReceive(httpMessage, false);
            }

            EventQueue.invokeAndWait(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (!httpMessage.getResponseHeader().isEmpty()) {
                                // Indicate UI new response arrived
                                responsePanel.updateContent();

                                if (!followRedirects) {
                                    persistAndShowMessage(httpMessage);
                                } else if (!redirectionValidator.isRequestValid()) {
                                    View.getSingleton()
                                            .showWarningDialog(
                                                    responsePanel,
                                                    Constant.messages.getString(
                                                            "manReq.outofscope.redirection.warning",
                                                            redirectionValidator
                                                                    .getInvalidRedirection()));
                                }
                            }
                        }
                    });

            Object userObject = httpMessage.getUserObject();
            if (userObject instanceof Socket) {
                closeSilently((Socket) userObject);
                return;
            }

            if (!(userObject instanceof org.zaproxy.zap.ZapGetMethod)) {
                return;
            }

            org.zaproxy.zap.ZapGetMethod method = (org.zaproxy.zap.ZapGetMethod) userObject;
            notifyPersistentConnectionListener(httpMessage, null, method);

        } catch (final HttpMalformedHeaderException mhe) {
            throw new IllegalArgumentException("Malformed header error.", mhe);

        } catch (final UnknownHostException uhe) {
            throw new IOException("Error forwarding to an Unknown host: " + uhe.getMessage(), uhe);

        } catch (final SSLException sslEx) {
            throw sslEx;
        } catch (final IOException ioe) {
            throw new IOException(
                    "IO error in sending request: " + ioe.getClass() + ": " + ioe.getMessage(),
                    ioe);

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void closeSilently(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignore) {
            // Nothing to do.
        }
    }

    private void persistAndShowMessage(HttpMessage httpMessage) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(() -> persistAndShowMessage(httpMessage));
            return;
        }

        try {
            Session session = Model.getSingleton().getSession();
            HistoryReference ref =
                    new HistoryReference(session, HistoryReference.TYPE_ZAP_USER, httpMessage);
            final ExtensionHistory extHistory = getHistoryExtension();
            if (extHistory != null) {
                extHistory.addHistory(ref);
            }
            SessionStructure.addPath(Model.getSingleton(), ref, httpMessage);
        } catch (HttpMalformedHeaderException | DatabaseException e) {
            logger.warn("Failed to persist message sent:", e);
        }
    }

    /**
     * Go thru each listener and offer him to take over the connection. The first observer that
     * returns true gets exclusive rights.
     *
     * @param httpMessage Contains HTTP request & response.
     * @param inSocket Encapsulates the TCP connection to the browser.
     * @param method Provides more power to process response.
     * @return Boolean to indicate if socket should be kept open.
     */
    private boolean notifyPersistentConnectionListener(
            HttpMessage httpMessage,
            Socket inSocket,
            @SuppressWarnings("deprecation") org.zaproxy.zap.ZapGetMethod method) {
        boolean keepSocketOpen = false;
        PersistentConnectionListener listener = null;
        synchronized (persistentConnectionListener) {
            for (int i = 0; i < persistentConnectionListener.size(); i++) {
                listener = persistentConnectionListener.get(i);
                try {
                    if (listener.onHandshakeResponse(httpMessage, inSocket, method)) {
                        // inform as long as one listener wishes to overtake the connection
                        keepSocketOpen = true;
                        break;
                    }
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }

        return keepSocketOpen;
    }

    protected ExtensionHistory getHistoryExtension() {
        if (extension == null) {
            extension =
                    Control.getSingleton()
                            .getExtensionLoader()
                            .getExtension(ExtensionHistory.class);
        }
        return extension;
    }

    @Override
    public void cleanup() {}

    private JToggleButton getButtonFollowRedirects() {
        if (followRedirect == null) {
            followRedirect =
                    new JToggleButton(
                            new ImageIcon(
                                    HttpPanelSender.class.getResource(
                                            "/resource/icon/16/118.png"))); // Arrow
            // turn
            // around
            // left
            followRedirect.setToolTipText(
                    Constant.messages.getString("manReq.checkBox.followRedirect"));
            followRedirect.setSelected(true);
        }
        return followRedirect;
    }

    private JToggleButton getButtonUseTrackingSessionState() {
        if (useTrackingSessionState == null) {
            useTrackingSessionState =
                    new JToggleButton(
                            new ImageIcon(
                                    HttpPanelSender.class.getResource(
                                            "/resource/icon/fugue/globe-green.png")));
            useTrackingSessionState.setToolTipText(
                    Constant.messages.getString("manReq.checkBox.useSession"));
            useTrackingSessionState.addItemListener(
                    e -> delegate.setUseGlobalState(e.getStateChange() == ItemEvent.SELECTED));
        }
        return useTrackingSessionState;
    }

    private JToggleButton getButtonUseCookies() {
        if (useCookies == null) {
            useCookies =
                    new JToggleButton(
                            new ImageIcon(
                                    HttpPanelSender.class.getResource(
                                            "/resource/icon/fugue/cookie.png")),
                            true);
            useCookies.setToolTipText(Constant.messages.getString("manReq.checkBox.useCookies"));
            useCookies.addItemListener(
                    e -> delegate.setUseCookies(e.getStateChange() == ItemEvent.SELECTED));
        }
        return useCookies;
    }

    private JToggleButton getButtonUseCsrf() {
        if (useCsrf == null) {
            useCsrf =
                    new JToggleButton(
                            new ImageIcon(
                                    HttpPanelSender.class.getResource(
                                            "/resource/icon/csrf-button.png")));
            useCsrf.setToolTipText(Constant.messages.getString("manReq.checkBox.useCSRF"));
        }
        return useCsrf;
    }

    private JToggleButton getButtonFixContentLength() {
        if (fixContentLength == null) {
            fixContentLength =
                    new JToggleButton(
                            new ImageIcon(
                                    HttpPanelSender.class.getResource(
                                            "/resource/icon/fugue/application-resize.png")),
                            true);
            fixContentLength.setToolTipText(
                    Constant.messages.getString("manReq.checkBox.fixLength"));
        }
        return fixContentLength;
    }

    public void addPersistentConnectionListener(PersistentConnectionListener listener) {
        persistentConnectionListener.add(listener);
    }

    public void removePersistentConnectionListener(PersistentConnectionListener listener) {
        persistentConnectionListener.remove(listener);
    }

    /**
     * A {@link HttpRedirectionValidator} that enforces the {@link Mode} when validating the {@code
     * URI} of redirections.
     *
     * @see #isRequestValid()
     */
    private class ModeRedirectionValidator implements HttpRedirectionValidator {

        private boolean isRequestValid;
        private URI invalidRedirection;

        public ModeRedirectionValidator() {
            isRequestValid = true;
        }

        @Override
        public void notifyMessageReceived(HttpMessage message) {
            persistAndShowMessage(message);
        }

        @Override
        public boolean isValid(URI redirection) {
            if (!isValidForCurrentMode(redirection)) {
                isRequestValid = false;
                invalidRedirection = redirection;
                return false;
            }
            return true;
        }

        private boolean isValidForCurrentMode(URI uri) {
            switch (Control.getSingleton().getMode()) {
                case safe:
                    return false;
                case protect:
                    return Model.getSingleton().getSession().isInScope(uri.toString());
                default:
                    return true;
            }
        }

        /**
         * Tells whether or not the request is valid, that is, all redirections were valid for the
         * current {@link Mode}.
         *
         * @return {@code true} is the request is valid, {@code false} otherwise.
         * @see #getInvalidRedirection()
         */
        public boolean isRequestValid() {
            return isRequestValid;
        }

        /**
         * Gets the invalid redirection, if any.
         *
         * @return the invalid redirection, {@code null} if there was none.
         * @see #isRequestValid()
         */
        public URI getInvalidRedirection() {
            return invalidRedirection;
        }
    }

    void updateButtonTrackingSessionState() {
        setButtonTrackingSessionStateEnabled(delegate.isGlobalStateEnabled());
    }

    public void setButtonTrackingSessionStateEnabled(boolean enabled) {
        getButtonUseTrackingSessionState().setEnabled(enabled);
        getButtonUseTrackingSessionState().setSelected(enabled);
        delegate.setUseGlobalState(enabled);
    }
}
