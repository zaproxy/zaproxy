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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.all.request;

import java.awt.Component;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.ContentSplitter;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.util.CaretVisibilityEnforcerOnFocusGain;
import org.zaproxy.zap.extension.httppanel.view.util.HttpTextViewUtils;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.model.DefaultTextHttpMessageLocation;
import org.zaproxy.zap.model.HttpMessageLocation;
import org.zaproxy.zap.model.MessageLocation;
import org.zaproxy.zap.model.TextHttpMessageLocation;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.messagecontainer.http.SelectableContentHttpMessageContainer;
import org.zaproxy.zap.view.messagelocation.MessageLocationHighlight;
import org.zaproxy.zap.view.messagelocation.MessageLocationHighlightsManager;
import org.zaproxy.zap.view.messagelocation.MessageLocationProducerFocusListener;
import org.zaproxy.zap.view.messagelocation.MessageLocationProducerFocusListenerAdapter;
import org.zaproxy.zap.view.messagelocation.TextMessageLocationHighlight;
import org.zaproxy.zap.view.messagelocation.TextMessageLocationHighlightsManager;

public class HttpRequestAllPanelSyntaxHighlightTextView extends HttpPanelSyntaxHighlightTextView
        implements SelectableContentHttpMessageContainer {

    public static final String NAME = "HttpRequestSyntaxTextView";

    private MessageLocationProducerFocusListenerAdapter focusListenerAdapter;
    private ContentSplitter contentSplitter;

    public HttpRequestAllPanelSyntaxHighlightTextView(RequestStringHttpPanelViewModel model) {
        super(model);

        getHttpPanelTextArea()
                .setComponentPopupMenu(
                        new CustomPopupMenu() {

                            private static final long serialVersionUID = 377256890518967680L;

                            @Override
                            public void show(Component invoker, int x, int y) {
                                if (!getHttpPanelTextArea().isFocusOwner()) {
                                    getHttpPanelTextArea().requestFocusInWindow();
                                }

                                View.getSingleton()
                                        .getPopupMenu()
                                        .show(
                                                HttpRequestAllPanelSyntaxHighlightTextView.this,
                                                x,
                                                y);
                            }
                        });
    }

    @Override
    protected HttpRequestAllPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
        contentSplitter = new ContentSplitter(getMainPanel());
        HttpRequestAllPanelSyntaxHighlightTextArea textArea =
                new HttpRequestAllPanelSyntaxHighlightTextArea(contentSplitter);
        contentSplitter.setTextArea(textArea);
        return textArea;
    }

    @Override
    protected void setModelData(String data) {
        if (data.isEmpty()) {
            super.setModelData(data);
            return;
        }

        int separator = data.indexOf("\n\n") + 2;
        String header = data.substring(0, separator);
        String body = contentSplitter.process(data.substring(separator));
        super.setModelData(header + body);
    }

    @Override
    protected HttpRequestAllPanelSyntaxHighlightTextArea getHttpPanelTextArea() {
        return (HttpRequestAllPanelSyntaxHighlightTextArea) super.getHttpPanelTextArea();
    }

    @SuppressWarnings("serial")
    protected static class HttpRequestAllPanelSyntaxHighlightTextArea
            extends HttpPanelSyntaxHighlightTextArea {

        private static final long serialVersionUID = 923466158533211593L;

        private static final Logger log =
                LogManager.getLogger(HttpRequestAllPanelSyntaxHighlightTextArea.class);

        // private static final String HTTP_REQUEST_HEADER_AND_BODY = "HTTP Request Header and
        // Body";

        // private static final String SYNTAX_STYLE_HTTP_REQUEST_HEADER_AND_BODY =
        // "text/http-request-header-body";

        private static RequestAllTokenMakerFactory tokenMakerFactory = null;

        private final ContentSplitter contentSplitter;

        private CaretVisibilityEnforcerOnFocusGain caretVisibilityEnforcer;

        public HttpRequestAllPanelSyntaxHighlightTextArea() {
            this(null);
        }

        public HttpRequestAllPanelSyntaxHighlightTextArea(ContentSplitter contentSplitter) {
            this.contentSplitter = contentSplitter;

            // addSyntaxStyle(HTTP_REQUEST_HEADER_AND_BODY,
            // SYNTAX_STYLE_HTTP_REQUEST_HEADER_AND_BODY);

            caretVisibilityEnforcer = new CaretVisibilityEnforcerOnFocusGain(this);
        }

        @Override
        public HttpMessage getMessage() {
            return (HttpMessage) super.getMessage();
        }

        @Override
        public void setMessage(Message aMessage) {
            super.setMessage(aMessage);

            caretVisibilityEnforcer.setEnforceVisibilityOnFocusGain(aMessage != null);
        }

        protected MessageLocation getSelection() {
            String header = getMessage().getRequestHeader().toString();

            int[] position =
                    HttpTextViewUtils.getViewToHeaderBodyPosition(
                            this, header, getSelectionStart(), getSelectionEnd());
            if (position.length == 0) {
                return new DefaultTextHttpMessageLocation(
                        HttpMessageLocation.Location.REQUEST_HEADER, 0);
            }

            int start = position[0];
            int end = position[1];
            HttpMessageLocation.Location location;

            String value;
            if (position.length == 2) {
                location = HttpMessageLocation.Location.REQUEST_HEADER;
                value = header.substring(start, end);
            } else {
                location = HttpMessageLocation.Location.REQUEST_BODY;
                value = getMessage().getRequestBody().toString().substring(start, end);
            }

            return new DefaultTextHttpMessageLocation(location, start, end, value);
        }

        protected MessageLocationHighlightsManager create() {
            return new TextMessageLocationHighlightsManager();
        }

        protected MessageLocationHighlight highlightImpl(
                TextHttpMessageLocation textLocation, TextMessageLocationHighlight textHighlight) {
            if (getMessage() == null) {
                return null;
            }

            int[] pos;
            if (TextHttpMessageLocation.Location.REQUEST_HEADER.equals(
                    textLocation.getLocation())) {
                pos =
                        HttpTextViewUtils.getHeaderToViewPosition(
                                this,
                                getMessage().getRequestHeader().toString(),
                                textLocation.getStart(),
                                textLocation.getEnd());
            } else {
                pos =
                        HttpTextViewUtils.getBodyToViewPosition(
                                this,
                                getMessage().getRequestHeader().toString(),
                                textLocation.getStart(),
                                textLocation.getEnd());

                if (pos.length != 0 && contentSplitter != null) {
                    pos = contentSplitter.highlightOffsets(pos[0], pos[1]);
                }
            }

            if (pos.length == 0) {
                return null;
            }

            textHighlight.setHighlightReference(highlight(pos[0], pos[1], textHighlight));

            return textHighlight;
        }

        @Override
        public void search(Pattern p, List<SearchMatch> matches) {
            String header = getMessage().getRequestHeader().toString();
            Matcher m = p.matcher(getText());
            while (m.find()) {

                int[] position =
                        HttpTextViewUtils.getViewToHeaderBodyPosition(
                                this, header, m.start(), m.end());
                if (position.length == 0) {
                    return;
                }

                SearchMatch.Location location =
                        position.length == 2
                                ? SearchMatch.Location.REQUEST_HEAD
                                : SearchMatch.Location.REQUEST_BODY;
                matches.add(new SearchMatch(location, position[0], position[1]));
            }
        }

        @Override
        public void highlight(SearchMatch sm) {
            if (!(SearchMatch.Location.REQUEST_HEAD.equals(sm.getLocation())
                    || SearchMatch.Location.REQUEST_BODY.equals(sm.getLocation()))) {
                return;
            }

            int[] pos;
            if (SearchMatch.Location.REQUEST_HEAD.equals(sm.getLocation())) {
                pos =
                        HttpTextViewUtils.getHeaderToViewPosition(
                                this,
                                sm.getMessage().getRequestHeader().toString(),
                                sm.getStart(),
                                sm.getEnd());
            } else {
                pos =
                        HttpTextViewUtils.getBodyToViewPosition(
                                this,
                                sm.getMessage().getRequestHeader().toString(),
                                sm.getStart(),
                                sm.getEnd());

                if (pos.length != 0 && contentSplitter != null) {
                    pos = contentSplitter.highlightOffsets(pos[0], pos[1]);
                }
            }

            if (pos.length == 0) {
                return;
            }

            highlight(pos[0], pos[1]);
        }

        @Override
        protected synchronized CustomTokenMakerFactory getTokenMakerFactory() {
            if (tokenMakerFactory == null) {
                tokenMakerFactory = new RequestAllTokenMakerFactory();
            }
            return tokenMakerFactory;
        }

        private static class RequestAllTokenMakerFactory extends CustomTokenMakerFactory {

            public RequestAllTokenMakerFactory() {
                // String pkg = "";

                // putMapping(SYNTAX_STYLE_HTTP_REQUEST_HEADER_AND_BODY, pkg +
                // "HttpRequestHeaderAndBodyTokenMaker");
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<HttpMessage> getMessageClass() {
        return HttpMessage.class;
    }

    @Override
    public Class<? extends MessageLocation> getMessageLocationClass() {
        return TextHttpMessageLocation.class;
    }

    @Override
    public MessageLocation getSelection() {
        return getHttpPanelTextArea().getSelection();
    }

    @Override
    public MessageLocationHighlightsManager create() {
        return getHttpPanelTextArea().create();
    }

    @Override
    public MessageLocationHighlight highlight(MessageLocation location) {
        if (!supports(location)) {
            return null;
        }
        TextHttpMessageLocation textLocation = (TextHttpMessageLocation) location;

        return getHttpPanelTextArea()
                .highlightImpl(
                        textLocation,
                        new TextMessageLocationHighlight(DisplayUtils.getHighlightColor()));
    }

    @Override
    public MessageLocationHighlight highlight(
            MessageLocation location, MessageLocationHighlight highlight) {
        if (!supports(location) || !(highlight instanceof TextMessageLocationHighlight)) {
            return null;
        }
        TextHttpMessageLocation textLocation = (TextHttpMessageLocation) location;
        TextMessageLocationHighlight textHighlight = (TextMessageLocationHighlight) highlight;

        return getHttpPanelTextArea().highlightImpl(textLocation, textHighlight);
    }

    @Override
    public void removeHighlight(
            MessageLocation location, MessageLocationHighlight highlightReference) {
        if (!(highlightReference instanceof TextMessageLocationHighlight)) {
            return;
        }
        getHttpPanelTextArea()
                .removeHighlight(
                        ((TextMessageLocationHighlight) highlightReference)
                                .getHighlightReference());
    }

    @Override
    public boolean supports(MessageLocation location) {
        if (!(location instanceof TextHttpMessageLocation)) {
            return false;
        }
        TextHttpMessageLocation.Location msgLocation =
                ((TextHttpMessageLocation) location).getLocation();
        return msgLocation == TextHttpMessageLocation.Location.REQUEST_HEADER
                || msgLocation == TextHttpMessageLocation.Location.REQUEST_BODY;
    }

    @Override
    public boolean supports(Class<? extends MessageLocation> classLocation) {
        return (TextHttpMessageLocation.class.isAssignableFrom(classLocation));
    }

    @Override
    public void addFocusListener(MessageLocationProducerFocusListener focusListener) {
        getFocusListenerAdapter().addFocusListener(focusListener);
    }

    @Override
    public void removeFocusListener(MessageLocationProducerFocusListener focusListener) {
        getFocusListenerAdapter().removeFocusListener(focusListener);

        if (!getFocusListenerAdapter().hasFocusListeners()) {
            getHttpPanelTextArea().removeFocusListener(focusListenerAdapter);
            focusListenerAdapter = null;
        }
    }

    @Override
    public HttpMessage getMessage() {
        return getHttpPanelTextArea().getMessage();
    }

    @Override
    public Component getComponent() {
        return getHttpPanelTextArea();
    }

    @Override
    public boolean isEmpty() {
        return getHttpPanelTextArea().getMessage() == null;
    }

    private MessageLocationProducerFocusListenerAdapter getFocusListenerAdapter() {
        if (focusListenerAdapter == null) {
            focusListenerAdapter = new MessageLocationProducerFocusListenerAdapter(this);
            getHttpPanelTextArea().addFocusListener(focusListenerAdapter);
        }
        return focusListenerAdapter;
    }
}
