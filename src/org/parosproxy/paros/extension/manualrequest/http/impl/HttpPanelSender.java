/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.parosproxy.paros.extension.manualrequest.http.impl;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.extension.manualrequest.MessageSender;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.PersistentConnectionListener;
import org.zaproxy.zap.ZapGetMethod;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.model.SessionStructure;

/**
 * Knows how to send {@link HttpMessage} objects.
 */
public class HttpPanelSender implements MessageSender {

    private static final Logger logger = Logger
            .getLogger(HttpPanelSender.class);

    private final HttpPanelResponse responsePanel;
    private ExtensionHistory extension;

    private HttpSender delegate;

    private JToggleButton followRedirect = null;
    private JToggleButton useTrackingSessionState = null;

    private List<PersistentConnectionListener> persistentConnectionListener = new ArrayList<>();

    public HttpPanelSender(HttpPanelRequest requestPanel,
            HttpPanelResponse responsePanel) {
        this.responsePanel = responsePanel;

        requestPanel.addOptions(getButtonUseTrackingSessionState(),
                HttpPanel.OptionsLocation.AFTER_COMPONENTS);
        requestPanel.addOptions(getButtonFollowRedirects(),
                HttpPanel.OptionsLocation.AFTER_COMPONENTS);

        final boolean isSessionTrackingEnabled = Model.getSingleton()
                .getOptionsParam().getConnectionParam().isHttpStateEnabled();
        getButtonUseTrackingSessionState().setEnabled(isSessionTrackingEnabled);
    }

    @Override
    public void handleSendMessage(Message aMessage) throws IllegalArgumentException, IOException {
        final HttpMessage httpMessage = (HttpMessage) aMessage;
        try {
            final ModeRedirectionValidator redirectionValidator = new ModeRedirectionValidator();
            if (getButtonFollowRedirects().isSelected()) {
                getDelegate().sendAndReceive(httpMessage, redirectionValidator);
            } else {
                getDelegate().sendAndReceive(httpMessage, false);
            }

            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    if (!httpMessage.getResponseHeader().isEmpty()) {
                        // Indicate UI new response arrived
                        responsePanel.updateContent();

                        try {
                            Session session = Model.getSingleton().getSession();
                            HistoryReference ref = new HistoryReference(session, HistoryReference.TYPE_ZAP_USER, httpMessage);
                            final ExtensionHistory extHistory = getHistoryExtension();
                            if (extHistory != null) {
                                extHistory.addHistory(ref);
                            }
                            SessionStructure.addPath(session, ref, httpMessage);
                        } catch (final Exception e) {
                            logger.error(e.getMessage(), e);
                        }

                        if (!redirectionValidator.isRequestValid()) {
                            View.getSingleton().showWarningDialog(
                                    Constant.messages.getString(
                                            "manReq.outofscope.redirection.warning",
                                            redirectionValidator.getInvalidRedirection()));
                        }
                    }
                }
            });

            ZapGetMethod method = (ZapGetMethod) httpMessage.getUserObject();
            notifyPersistentConnectionListener(httpMessage, null, method);

        } catch (final HttpMalformedHeaderException mhe) {
            throw new IllegalArgumentException("Malformed header error.", mhe);

        } catch (final UnknownHostException uhe) {
            throw new IOException("Error forwarding to an Unknown host: " + uhe.getMessage(), uhe);

        } catch (final IOException ioe) {
            throw new IOException("IO error in sending request: " + ioe.getClass() + ": " + ioe.getMessage(), ioe);

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Go thru each listener and offer him to take over the connection. The
     * first observer that returns true gets exclusive rights.
     *
     * @param httpMessage Contains HTTP request & response.
     * @param inSocket Encapsulates the TCP connection to the browser.
     * @param method Provides more power to process response.
     *
     * @return Boolean to indicate if socket should be kept open.
     */
    private boolean notifyPersistentConnectionListener(HttpMessage httpMessage, Socket inSocket, ZapGetMethod method) {
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
            extension = ((ExtensionHistory) Control.getSingleton()
                    .getExtensionLoader().getExtension(ExtensionHistory.NAME));
        }
        return extension;
    }

    @Override
    public void cleanup() {
        if (delegate != null) {
            delegate.shutdown();
            delegate = null;
        }

        final boolean isSessionTrackingEnabled = Model.getSingleton()
                .getOptionsParam().getConnectionParam().isHttpStateEnabled();
        getButtonUseTrackingSessionState().setEnabled(isSessionTrackingEnabled);
    }

    private HttpSender getDelegate() {
        if (delegate == null) {
            delegate = new HttpSender(Model.getSingleton().getOptionsParam()
                    .getConnectionParam(), getButtonUseTrackingSessionState()
                    .isSelected(), HttpSender.MANUAL_REQUEST_INITIATOR);
        }
        return delegate;
    }

    private JToggleButton getButtonFollowRedirects() {
        if (followRedirect == null) {
            followRedirect = new JToggleButton(new ImageIcon(
                    HttpPanelSender.class
                    .getResource("/resource/icon/16/118.png"))); // Arrow
            // turn
            // around
            // left
            followRedirect.setToolTipText(Constant.messages
                    .getString("manReq.checkBox.followRedirect"));
            followRedirect.setSelected(true);
        }
        return followRedirect;
    }

    private JToggleButton getButtonUseTrackingSessionState() {
        if (useTrackingSessionState == null) {
            useTrackingSessionState = new JToggleButton(new ImageIcon(
                    HttpPanelSender.class
                    .getResource("/resource/icon/fugue/cookie.png"))); // Cookie
            useTrackingSessionState.setToolTipText(Constant.messages
                    .getString("manReq.checkBox.useSession"));
        }
        return useTrackingSessionState;
    }

    public void addPersistentConnectionListener(PersistentConnectionListener listener) {
        persistentConnectionListener.add(listener);
    }

    public void removePersistentConnectionListener(PersistentConnectionListener listener) {
        persistentConnectionListener.remove(listener);
    }

    /**
     * A {@code RedirectionValidator} that enforces the {@link Mode} when validating the {@code URI} of redirections.
     *
     * @see #isRequestValid()
     */
    private static class ModeRedirectionValidator implements HttpSender.RedirectionValidator {

        private boolean isRequestValid;
        private URI invalidRedirection;

        public ModeRedirectionValidator() {
            isRequestValid = true;
        }

        @Override
        public void notifyMessageReceived(HttpMessage message) {
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
         * Tells whether or not the request is valid, that is, all redirections were valid for the current {@link Mode}.
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
}
