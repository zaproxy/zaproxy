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
// ZAP: 2012/07/02 Introduced new class. Moved code from class
// ManualRequestEditorDialog to here (HistoryList).
package org.parosproxy.paros.extension.manualrequest.http.impl;

import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JToggleButton;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.extension.manualrequest.MessageSender;
import org.parosproxy.paros.model.HistoryList;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.httppanel.Message;

/**
 * Knows how to send {@link HttpMessage} objects.
 */
public class HttpPanelSender implements MessageSender {

    private static final Logger logger = Logger.getLogger(HttpPanelSender.class);
    
    private final HttpSender delegate;
    private final HttpPanelResponse responsePanel;
    private final HistoryList historyList;
    private final JToggleButton followRedirects;
    
    public HttpPanelSender(HttpSender httpSender, HttpPanelResponse responsePanel, JToggleButton followRedirects) {
        this.delegate = httpSender;
        this.responsePanel = responsePanel;
        this.historyList = ((ExtensionHistory)Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME)).getHistoryList();
        this.followRedirects = followRedirects;
    }
    
    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.manualrequest.MessageSender#sendAndReceiveMessage()
     */
    @Override
    public void handleSendMessage(Message aMessage) throws Exception {
        final HttpMessage httpMessage = (HttpMessage)aMessage;
        try {
            httpMessage.getRequestHeader().setContentLength(httpMessage.getRequestBody().length());
            delegate.sendAndReceive(httpMessage, followRedirects.isSelected());

            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    if (!httpMessage.getResponseHeader().isEmpty()) {
                        // Indicate UI new response arrived
                        responsePanel.updateContent();

                        final int finalType = HistoryReference.TYPE_MANUAL;
                        final Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                addHistory(httpMessage, finalType);
                            }
                        });
                        t.start();
                    }
                }
            });
        } catch (final HttpMalformedHeaderException mhe) {
            throw new Exception("Malformed header error.");
        } catch (final IOException ioe) {
            throw new Exception("IO error in sending request.");
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void addHistory(HttpMessage msg, int type) {
        HistoryReference historyRef = null;
        try {
            historyRef = new HistoryReference(Model.getSingleton().getSession(), type, msg);
            synchronized (historyList) {
                if (type == HistoryReference.TYPE_MANUAL) {
                    addHistoryInEventQueue(historyRef);
                    historyList.notifyItemChanged(historyRef);
                }
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void addHistoryInEventQueue(final HistoryReference ref) {
        if (EventQueue.isDispatchThread()) {
            historyList.addElement(ref);
        } else {
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        historyList.addElement(ref);
                    }
                });
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
}
