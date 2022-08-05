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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.all.response;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.AutoDetectSyntaxHttpPanelTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.ContentSplitter;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.util.HttpTextViewUtils;
import org.zaproxy.zap.extension.search.SearchMatch;

@SuppressWarnings("serial")
public class HttpResponseAllPanelSyntaxHighlightTextView extends HttpPanelSyntaxHighlightTextView {

    private static final String CSS =
            Constant.messages.getString("http.panel.view.syntaxtext.syntax.css");
    private static final String HTML =
            Constant.messages.getString("http.panel.view.syntaxtext.syntax.html");
    private static final String JAVASCRIPT =
            Constant.messages.getString("http.panel.view.syntaxtext.syntax.javascript");
    private static final String JSON =
            Constant.messages.getString("http.panel.view.syntaxtext.syntax.json");
    private static final String XML =
            Constant.messages.getString("http.panel.view.syntaxtext.syntax.xml");

    private ContentSplitter contentSplitter;

    public HttpResponseAllPanelSyntaxHighlightTextView(ResponseStringHttpPanelViewModel model) {
        super(model);
    }

    @Override
    protected HttpPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
        contentSplitter = new ContentSplitter(getMainPanel());
        HttpPanelSyntaxHighlightTextArea textArea =
                new HttpResponseAllPanelSyntaxHighlightTextArea(contentSplitter);
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

    private static class HttpResponseAllPanelSyntaxHighlightTextArea
            extends AutoDetectSyntaxHttpPanelTextArea {

        private static final long serialVersionUID = 3665478428546560762L;

        private static final Logger log =
                LogManager.getLogger(HttpResponseAllPanelSyntaxHighlightTextArea.class);

        // private static final String HTTP_RESPONSE_HEADER_AND_BODY = "HTTP Response Header and
        // Body";

        // private static final String SYNTAX_STYLE_HTTP_RESPONSE_HEADER_AND_BODY =
        // "text/http-response-header-body";

        private final ContentSplitter contentSplitter;

        private static ResponseAllTokenMakerFactory tokenMakerFactory = null;

        public HttpResponseAllPanelSyntaxHighlightTextArea(ContentSplitter contentSplitter) {
            this.contentSplitter = contentSplitter;

            // addSyntaxStyle(HTTP_RESPONSE_HEADER_AND_BODY,
            // SYNTAX_STYLE_HTTP_RESPONSE_HEADER_AND_BODY);
            addSyntaxStyle(CSS, SyntaxConstants.SYNTAX_STYLE_CSS);
            addSyntaxStyle(HTML, SyntaxConstants.SYNTAX_STYLE_HTML);
            addSyntaxStyle(JAVASCRIPT, SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
            addSyntaxStyle(JSON, SyntaxConstants.SYNTAX_STYLE_JSON);
            addSyntaxStyle(XML, SyntaxConstants.SYNTAX_STYLE_XML);
        }

        @Override
        public void search(Pattern p, List<SearchMatch> matches) {
            String header = ((HttpMessage) getMessage()).getResponseHeader().toString();
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
                                ? SearchMatch.Location.RESPONSE_HEAD
                                : SearchMatch.Location.RESPONSE_BODY;
                matches.add(new SearchMatch(location, position[0], position[1]));
            }
        }

        @Override
        public void highlight(SearchMatch sm) {
            if (!(SearchMatch.Location.RESPONSE_HEAD.equals(sm.getLocation())
                    || SearchMatch.Location.RESPONSE_BODY.equals(sm.getLocation()))) {
                return;
            }

            int[] pos;
            if (SearchMatch.Location.RESPONSE_HEAD.equals(sm.getLocation())) {
                pos =
                        HttpTextViewUtils.getHeaderToViewPosition(
                                this,
                                sm.getMessage().getResponseHeader().toString(),
                                sm.getStart(),
                                sm.getEnd());
            } else {
                pos =
                        HttpTextViewUtils.getBodyToViewPosition(
                                this,
                                sm.getMessage().getResponseHeader().toString(),
                                sm.getStart(),
                                sm.getEnd());

                if (pos.length != 0) {
                    pos = contentSplitter.highlightOffsets(pos[0], pos[1]);
                }
            }

            if (pos.length == 0) {
                return;
            }

            highlight(pos[0], pos[1]);
        }

        @Override
        protected String detectSyntax(HttpMessage httpMessage) {
            String syntax = null;
            if (httpMessage != null) {
                String contentType =
                        httpMessage.getResponseHeader().getHeader(HttpHeader.CONTENT_TYPE);
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
                    } else if (contentType.contains("xhtml")) {
                        syntax = SyntaxConstants.SYNTAX_STYLE_HTML;
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
                tokenMakerFactory = new ResponseAllTokenMakerFactory();
            }
            return tokenMakerFactory;
        }

        private static class ResponseAllTokenMakerFactory extends CustomTokenMakerFactory {

            public ResponseAllTokenMakerFactory() {
                String pkg = "org.fife.ui.rsyntaxtextarea.modes.";

                putMapping(SYNTAX_STYLE_CSS, pkg + "CSSTokenMaker");
                putMapping(SYNTAX_STYLE_HTML, pkg + "HTMLTokenMaker");
                putMapping(SYNTAX_STYLE_JAVASCRIPT, pkg + "JavaScriptTokenMaker");
                putMapping(SYNTAX_STYLE_JSON, pkg + "JsonTokenMaker");
                putMapping(SYNTAX_STYLE_XML, pkg + "XMLTokenMaker");
            }
        }
    }
}
