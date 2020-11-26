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
package org.zaproxy.zap.extension.httppanel.component.all.response;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextArea;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextView;
import org.zaproxy.zap.extension.httppanel.view.util.HttpTextViewUtils;
import org.zaproxy.zap.extension.search.SearchMatch;

public class HttpResponseAllPanelTextView extends HttpPanelTextView {

    private static final Logger log = LogManager.getLogger(HttpResponseAllPanelTextView.class);

    public HttpResponseAllPanelTextView(ResponseStringHttpPanelViewModel model) {
        super(model);
    }

    @Override
    protected HttpPanelTextArea createHttpPanelTextArea() {
        return new HttpResponseAllPanelTextArea();
    }

    protected static class HttpResponseAllPanelTextArea extends HttpPanelTextArea {

        private static final long serialVersionUID = 2539870692549575745L;

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
            }

            if (pos.length == 0) {
                return;
            }

            highlight(pos[0], pos[1]);
        }
    }
}
