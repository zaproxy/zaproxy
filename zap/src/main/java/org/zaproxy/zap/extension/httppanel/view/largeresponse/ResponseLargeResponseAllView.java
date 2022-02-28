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
package org.zaproxy.zap.extension.httppanel.view.largeresponse;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.component.all.response.HttpResponseAllPanelTextView;

/** @deprecated (2.12.0) No longer in use. */
@Deprecated
public class ResponseLargeResponseAllView extends HttpResponseAllPanelTextView {

    public static final String NAME = "ResponseLargeResponseAllView";

    public static final String CAPTION_NAME =
            Constant.messages.getString("http.panel.view.largeresponse.name");

    public ResponseLargeResponseAllView(LargeResponseStringHttpPanelViewModel model) {
        super(model);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getCaptionName() {
        return CAPTION_NAME;
    }

    @Override
    public int getPosition() {
        return 1;
    }

    @Override
    public boolean isEnabled(Message message) {
        return LargeResponseUtil.isLargeResponse(message);
    }

    @Override
    public boolean hasChanged() {
        return false;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(boolean editable) {}
}
