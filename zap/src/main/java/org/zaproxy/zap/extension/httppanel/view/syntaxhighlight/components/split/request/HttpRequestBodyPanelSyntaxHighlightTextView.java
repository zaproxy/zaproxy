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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.split.request;

import java.awt.Component;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestBodyStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.AutoDetectSyntaxHttpPanelTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.ContentSplitter;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.util.CaretVisibilityEnforcerOnFocusGain;
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

@SuppressWarnings("serial")
public class HttpRequestBodyPanelSyntaxHighlightTextView extends HttpPanelSyntaxHighlightTextView
        implements SelectableContentHttpMessageContainer {

    public static final String NAME = "HttpRequestBodySyntaxTextView";

    private MessageLocationProducerFocusListenerAdapter focusListenerAdapter;
    private ContentSplitter contentSplitter;

    public HttpRequestBodyPanelSyntaxHighlightTextView(RequestBodyStringHttpPanelViewModel model) {
        super(model);

        getHttpPanelTextArea()
                .setComponentPopupMenu(
                        new CustomPopupMenu() {

                            private static final long serialVersionUID = -426000345249750052L;

                            @Override
                            public void show(Component invoker, int x, int y) {
                                if (!getHttpPanelTextArea().isFocusOwner()) {
                                    getHttpPanelTextArea().requestFocusInWindow();
                                }

                                View.getSingleton()
                                        .getPopupMenu()
                                        .show(
                                                HttpRequestBodyPanelSyntaxHighlightTextView.this,
                                                x,
                                                y);
                            }
                        });
    }

    @Override
    protected HttpRequestBodyPanelSyntaxHighlightTextArea getHttpPanelTextArea() {
        return (HttpRequestBodyPanelSyntaxHighlightTextArea) super.getHttpPanelTextArea();
    }

    @Override
    protected HttpPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
        contentSplitter = new ContentSplitter(getMainPanel());
        HttpPanelSyntaxHighlightTextArea textArea =
                new HttpRequestBodyPanelSyntaxHighlightTextArea(contentSplitter);
        contentSplitter.setTextArea(textArea);
        return textArea;
    }

    @Override
    protected void setModelData(String data) {
        super.setModelData(contentSplitter.process(data));
    }

    private static class HttpRequestBodyPanelSyntaxHighlightTextArea
            extends AutoDetectSyntaxHttpPanelTextArea {

        private static final long serialVersionUID = -2102275261139781996L;

        private static final String X_WWW_FORM_URLENCODED =
                Constant.messages.getString("http.panel.view.syntaxtext.syntax.xWwwFormUrlencoded");
        private static final String JAVASCRIPT =
                Constant.messages.getString("http.panel.view.syntaxtext.syntax.javascript");
        private static final String JSON =
                Constant.messages.getString("http.panel.view.syntaxtext.syntax.json");
        private static final String XML =
                Constant.messages.getString("http.panel.view.syntaxtext.syntax.xml");

        private static final String SYNTAX_STYLE_X_WWW_FORM = "application/x-www-form-urlencoded";

        private static RequestBodyTokenMakerFactory tokenMakerFactory = null;

        private final ContentSplitter contentSplitter;

        private CaretVisibilityEnforcerOnFocusGain caretVisibilityEnforcer;

        public HttpRequestBodyPanelSyntaxHighlightTextArea(ContentSplitter contentSplitter) {
            this.contentSplitter = contentSplitter;

            addSyntaxStyle(X_WWW_FORM_URLENCODED, SYNTAX_STYLE_X_WWW_FORM);
            addSyntaxStyle(JAVASCRIPT, SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
            addSyntaxStyle(JSON, SyntaxConstants.SYNTAX_STYLE_JSON);
            addSyntaxStyle(XML, SyntaxConstants.SYNTAX_STYLE_XML);

            caretVisibilityEnforcer = new CaretVisibilityEnforcerOnFocusGain(this);

            setCodeFoldingAllowed(true);
        }

        @Override
        public String getName() {
            return NAME;
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
            int start = getSelectionStart();
            int end = getSelectionEnd();
            if (start == end) {
                return new DefaultTextHttpMessageLocation(
                        HttpMessageLocation.Location.REQUEST_BODY, start);
            }
            return new DefaultTextHttpMessageLocation(
                    HttpMessageLocation.Location.REQUEST_BODY, start, end, getSelectedText());
        }

        protected MessageLocationHighlightsManager create() {
            return new TextMessageLocationHighlightsManager();
        }

        protected MessageLocationHighlight highlightImpl(
                TextHttpMessageLocation textLocation, TextMessageLocationHighlight textHighlight) {
            textHighlight.setHighlightReference(
                    highlight(textLocation.getStart(), textLocation.getEnd(), textHighlight));

            return textHighlight;
        }

        @Override
        public void search(Pattern p, List<SearchMatch> matches) {
            Matcher m = p.matcher(getText());
            while (m.find()) {
                matches.add(new SearchMatch(SearchMatch.Location.REQUEST_BODY, m.start(), m.end()));
            }
        }

        @Override
        public void highlight(SearchMatch sm) {
            if (!SearchMatch.Location.REQUEST_BODY.equals(sm.getLocation())) {
                return;
            }

            int len = getText().length();
            if (sm.getStart() > len || sm.getEnd() > len) {
                return;
            }

            int[] offsets = contentSplitter.highlightOffsets(sm.getStart(), sm.getEnd());
            highlight(offsets[0], offsets[1]);
        }

        @Override
        protected String detectSyntax(HttpMessage httpMessage) {
            String syntax = null;
            if (httpMessage != null) {
                String contentType =
                        httpMessage.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
                if (contentType != null && !contentType.isEmpty()) {
                    contentType = contentType.toLowerCase(Locale.ENGLISH);
                    final int pos = contentType.indexOf(';');
                    if (pos != -1) {
                        contentType = contentType.substring(0, pos).trim();
                    }
                    if (contentType.contains("javascript")) {
                        syntax = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
                    } else if (contentType.contains("json")) {
                        syntax = SyntaxConstants.SYNTAX_STYLE_JSON;
                    } else if (contentType.contains("xml")) {
                        syntax = SyntaxConstants.SYNTAX_STYLE_XML;
                    } else {
                        syntax = contentType;
                    }
                }
            }
            return syntax;
        }

        @Override
        protected synchronized CustomTokenMakerFactory getTokenMakerFactory() {
            if (tokenMakerFactory == null) {
                tokenMakerFactory = new RequestBodyTokenMakerFactory();
            }
            return tokenMakerFactory;
        }

        private static class RequestBodyTokenMakerFactory extends CustomTokenMakerFactory {

            public RequestBodyTokenMakerFactory() {
                String pkg = "org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.lexers.";

                putMapping(SYNTAX_STYLE_X_WWW_FORM, pkg + "WwwFormTokenMaker");

                pkg = "org.fife.ui.rsyntaxtextarea.modes.";
                putMapping(SYNTAX_STYLE_JAVASCRIPT, pkg + "JavaScriptTokenMaker");
                putMapping(SYNTAX_STYLE_JSON, pkg + "JsonTokenMaker");
                putMapping(SYNTAX_STYLE_XML, pkg + "XMLTokenMaker");
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
        return ((TextHttpMessageLocation) location).getLocation()
                == TextHttpMessageLocation.Location.REQUEST_BODY;
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
