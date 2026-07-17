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
package org.zaproxy.zap.extension.keyboard;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.KeyStroke;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.text.StringEscapeUtils;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiOther;

public class KeyboardAPI extends ApiImplementor {

    private static final String PREFIX = "keyboard";

    private static final String OTHER_CHEETSHEET_ACTION_ORDER = "cheatsheetActionOrder";
    private static final String OTHER_CHEETSHEET_KEY_ORDER = "cheatsheetKeyOrder";

    private static final String PARAM_INC_UNSET = "incUnset";

    // style-src needs 'unsafe-inline' for the cheatsheet's embedded <style> block.
    private static final String CHEATSHEET_CONTENT_SECURITY_POLICY =
            "default-src 'none'; style-src 'unsafe-inline'";

    private static final String CHEATSHEET_CSS =
            "body { font-family: sans-serif; }\n"
                    + "table { border-collapse: collapse; }\n"
                    + "th, td { padding: 4px 12px; text-align: left; vertical-align: top; }\n"
                    + "th { border-bottom: 1px solid #ccc; }\n"
                    + "kbd { font-family: system-ui, \"Apple Symbols\", \"Segoe UI Symbol\", sans-serif; padding: .1em .4em; border: 1px solid #ccc; border-radius: 3px; background: #f7f7f7; }\n";

    private ExtensionKeyboard extension;

    public KeyboardAPI(ExtensionKeyboard extension) {
        this.extension = extension;
        this.addApiOthers(
                new ApiOther(OTHER_CHEETSHEET_ACTION_ORDER, null, new String[] {PARAM_INC_UNSET}));
        this.addApiOthers(
                new ApiOther(OTHER_CHEETSHEET_KEY_ORDER, null, new String[] {PARAM_INC_UNSET}));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    public URI getCheatSheetActionURI() throws URIException, NullPointerException {
        return getCheatsheetURI(OTHER_CHEETSHEET_ACTION_ORDER);
    }

    public URI getCheatSheetKeyURI() throws URIException, NullPointerException {
        return getCheatsheetURI(OTHER_CHEETSHEET_KEY_ORDER);
    }

    private static URI getCheatsheetURI(String endpointName) throws URIException {
        API api = API.getInstance();
        String apiPath =
                API.Format.OTHER.name()
                        + "/"
                        + PREFIX
                        + "/"
                        + API.RequestType.other.name()
                        + "/"
                        + endpointName
                        + "/";
        String url =
                api.getBaseURL(false)
                        + apiPath
                        + "?"
                        + API.API_NONCE_PARAM
                        + "="
                        + api.getLongLivedNonce("/" + apiPath);
        return new URI(url, true);
    }

    @Override
    public HttpMessage handleApiOther(HttpMessage msg, String name, JSONObject params)
            throws ApiException {
        if (OTHER_CHEETSHEET_ACTION_ORDER.equals(name) || OTHER_CHEETSHEET_KEY_ORDER.equals(name)) {

            List<KeyboardShortcut> shortcutList = this.extension.getShortcuts();
            List<KeyboardShortcut> shortcuts =
                    new ArrayList<>(shortcutList != null ? shortcutList : List.of());

            if (OTHER_CHEETSHEET_KEY_ORDER.equals(name)) {
                shortcuts.sort(
                        Comparator.comparing(
                                KeyboardShortcut::getKeyStroke, KeyStrokeDisplay::compare));
            } else {
                shortcuts.sort(Comparator.comparing(KeyboardShortcut::getName));
            }

            String subtitleKey =
                    OTHER_CHEETSHEET_KEY_ORDER.equals(name)
                            ? "keyboard.api.cheatsheet.subtitle.key"
                            : "keyboard.api.cheatsheet.subtitle.action";
            boolean incUnset = this.getParam(params, PARAM_INC_UNSET, false);
            byte[] responseBody =
                    buildCheatsheetResponse(shortcuts, subtitleKey, incUnset)
                            .getBytes(StandardCharsets.UTF_8);
            msg.setResponseBody(responseBody);
            try {
                msg.setResponseHeader(
                        API.getDefaultResponseHeader(
                                "text/html; charset=UTF-8", responseBody.length));
                msg.getResponseHeader()
                        .setHeader("Content-Security-Policy", CHEATSHEET_CONTENT_SECURITY_POLICY);
            } catch (HttpMalformedHeaderException e) {
                throw new ApiException(ApiException.Type.INTERNAL_ERROR, name, e);
            }

            return msg;

        } else {
            throw new ApiException(ApiException.Type.BAD_OTHER, name);
        }
    }

    private static String buildCheatsheetResponse(
            List<KeyboardShortcut> shortcuts, String subtitleKey, boolean incUnset) {
        StringBuilder sb = new StringBuilder();
        String escapedTitle = escapeMessage("keyboard.api.cheatsheet.title");

        sb.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n<title>")
                .append(escapedTitle)
                .append("</title>\n<style>\n")
                .append(CHEATSHEET_CSS)
                .append("</style>\n</head>\n<body>\n<h1>")
                .append(escapedTitle)
                .append("</h1>\n<h2>")
                .append(escapeMessage(subtitleKey))
                .append("</h2>\n<table>\n<tr><th>")
                .append(escapeMessage("keyboard.api.cheatsheet.table.header.action"))
                .append("</th><th>")
                .append(escapeMessage("keyboard.api.cheatsheet.table.header.keys"))
                .append("</th><th>")
                .append(escapeMessage("keyboard.api.cheatsheet.table.header.symbols"))
                .append("</th></tr>\n");

        for (KeyboardShortcut shortcut : shortcuts) {
            KeyStroke keyStroke = shortcut.getKeyStroke();
            if (incUnset || (keyStroke != null && keyStroke.getKeyCode() != 0)) {
                sb.append("<tr><td>")
                        .append(StringEscapeUtils.escapeHtml4(shortcut.getName()))
                        .append("</td><td>")
                        .append(KeyStrokeDisplay.formatHtmlNames(keyStroke))
                        .append("</td><td>")
                        .append(KeyStrokeDisplay.formatHtmlSymbols(keyStroke))
                        .append("</td></tr>\n");
            }
        }

        sb.append("</table>\n<br>\n")
                .append(escapeMessage("keyboard.api.cheatsheet.generatedBy"))
                .append("\n</body>\n</html>\n");
        return sb.toString();
    }

    private static String escapeMessage(String key) {
        return StringEscapeUtils.escapeHtml4(Constant.messages.getString(key));
    }
}
