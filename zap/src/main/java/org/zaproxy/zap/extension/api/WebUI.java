/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.extension.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.api.API.Format;
import org.zaproxy.zap.extension.api.API.RequestType;

public class WebUI {

    private API api;
    private boolean isDevTestNonce = false; // Manually change here to test nonces with the web UI
    private static final String PAC_FILE_API_PATH = "/OTHER/network/other/proxy.pac/";
    private static final String ROOT_CA_CERT_API_PATH = "/OTHER/network/other/rootCaCert/";

    public WebUI(API api) {
        this.api = api;
    }

    private ApiElement getElement(ApiImplementor impl, String name, RequestType reqType)
            throws ApiException {
        if (RequestType.action.equals(reqType) && name != null) {
            // Action form
            List<ApiAction> actionList = impl.getApiActions();
            ApiAction action = null;
            for (ApiAction act : actionList) {
                if (name.equals(act.getName())) {
                    action = act;
                    break;
                }
            }
            if (action == null) {
                throw new ApiException(ApiException.Type.BAD_ACTION);
            }
            return action;
        } else if (RequestType.other.equals(reqType) && name != null) {
            // Other form
            List<ApiOther> otherList = impl.getApiOthers();
            ApiOther other = null;
            for (ApiOther oth : otherList) {
                if (name.equals(oth.getName())) {
                    other = oth;
                    break;
                }
            }
            if (other == null) {
                throw new ApiException(ApiException.Type.BAD_OTHER);
            }
            return other;
        } else if (RequestType.view.equals(reqType) && name != null) {
            List<ApiView> viewList = impl.getApiViews();
            ApiView view = null;
            for (ApiView v : viewList) {
                if (name.equals(v.getName())) {
                    view = v;
                    break;
                }
            }
            if (view == null) {
                throw new ApiException(ApiException.Type.BAD_VIEW);
            }
            return view;
        } else if (RequestType.pconn.equals(reqType) && name != null) {
            List<ApiPersistentConnection> pconnList = impl.getApiPersistentConnections();
            ApiPersistentConnection pconn = null;
            for (ApiPersistentConnection pc : pconnList) {
                if (name.equals(pc.getName())) {
                    pconn = pc;
                    break;
                }
            }
            if (pconn == null) {
                throw new ApiException(ApiException.Type.BAD_PCONN);
            }
            return pconn;
        } else {
            throw new ApiException(ApiException.Type.BAD_TYPE);
        }
    }

    private void appendElements(
            StringBuilder sb, String component, String type, List<ApiElement> elementList) {
        Collections.sort(
                elementList,
                new Comparator<ApiElement>() {
                    @Override
                    public int compare(ApiElement ae1, ApiElement ae2) {
                        return ae1.getName().compareTo(ae2.getName());
                    }
                });

        sb.append("\n<table>\n");
        for (ApiElement element : elementList) {
            sb.append("<tr>");
            sb.append("<td>");
            sb.append("<a href=\"/");
            sb.append(Format.UI.name());
            sb.append('/');
            sb.append(component);
            sb.append('/');
            sb.append(type);
            sb.append('/');
            sb.append(element.getName());
            sb.append("/\">");
            sb.append(element.getName());
            if (!element.getParameters().isEmpty()) {
                sb.append(" (");
                for (ApiParameter parameter : element.getParameters()) {
                    sb.append(parameter.getName());
                    if (parameter.isRequired()) {
                        sb.append('*');
                    }
                    sb.append(' ');
                }
                sb.append(") ");
            }
            sb.append("</a>");
            sb.append("</td><td>");

            if (element.isDeprecated()) {
                sb.append(Constant.messages.getString("api.html.deprecated.endpoint"));
                sb.append("<br />");
                String text = element.getDeprecatedDescription();
                if (text != null && !text.isEmpty()) {
                    sb.append(text);
                    sb.append("<br />");
                }
            }

            String descTag = element.getDescriptionTag();
            if (Constant.messages.containsKey(descTag)) {
                sb.append(Constant.messages.getString(descTag));
            } else {
                // Uncomment to see what tags are missing via the UI
                // sb.append(descTag);
            }
            sb.append("</td>");

            sb.append("</tr>\n");
        }
        sb.append("</table>\n");
    }

    private void appendShortcuts(StringBuilder sb, String component, List<String> shortcutList) {
        Collections.sort(shortcutList);

        sb.append("\n<table>\n");
        for (String shortcut : shortcutList) {
            sb.append("<tr>");
            sb.append("<td>");
            sb.append("<a href=\"/");
            sb.append(shortcut);
            sb.append("/?");
            sb.append(API.API_NONCE_PARAM);
            sb.append("=");
            sb.append(api.getOneTimeNonce("/" + shortcut + "/"));
            sb.append("\">");
            sb.append(shortcut);
            sb.append("</a>");
            sb.append("</td><td>");

            sb.append("</td>");

            sb.append("</tr>\n");
        }
        sb.append("</table>\n");
    }

    public String handleRequest(
            String component, ApiImplementor impl, RequestType reqType, String name)
            throws ApiException {
        // Generate HTML UI
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<head>\n");
        sb.append("<title>");
        sb.append(Constant.messages.getString("api.html.title"));
        sb.append("</title>\n");
        /* The script version prevents the cache being used if ZAP has been updated in the same day */
        sb.append(
                "<script src=\"/script.js/?v="
                        + CoreAPI.API_SCRIPT_VERSION
                        + "&"
                        + API.API_NONCE_PARAM
                        + "="
                        + api.getOneTimeNonce("/script.js/")
                        + "\" type=\"text/javascript\"></script>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("<h1>");
        sb.append("<a href=\"/");
        sb.append(Format.UI.name());
        sb.append("/\">");
        sb.append(Constant.messages.getString("api.html.title"));
        sb.append("</a>");
        sb.append("</h1>\n");

        if (impl != null) {
            sb.append("<h2>");
            sb.append("<a href=\"/");
            sb.append(Format.UI.name());
            sb.append("/");
            sb.append(component);
            sb.append("/\">");
            sb.append(Constant.messages.getString("api.html.component"));
            sb.append(component);
            sb.append("</a>");
            sb.append("</h2>\n");

            if (name != null) {
                ApiElement element = this.getElement(impl, name, reqType);

                sb.append("<h3>");
                sb.append(Constant.messages.getString("api.html." + reqType.name()));
                sb.append(element.getName());
                sb.append("</h3>\n");
                String descTag = element.getDescriptionTag();
                if (Constant.messages.containsKey(descTag)) {
                    sb.append(Constant.messages.getString(descTag));
                }

                sb.append("\n<form id=\"zapform\" name=\"zapform\" action=\"override\">");
                sb.append("<table>\n");
                if (!RequestType.other.equals(reqType)) {
                    sb.append("<tr><td>");
                    sb.append(Constant.messages.getString("api.html.format"));
                    sb.append("</td><td>\n");
                    sb.append("<select id=\"zapapiformat\">\n");
                    sb.append("<option value=\"JSON\">JSON</option>\n");
                    if (getOptionsParamApi().isEnableJSONP()) {
                        sb.append("<option value=\"JSONP\">JSONP</option>\n");
                    } else {
                        sb.append("<option value=\"JSONP\" disabled>JSONP</option>\n");
                    }

                    sb.append("<option value=\"HTML\">HTML</option>\n");
                    sb.append("<option value=\"XML\">XML</option>\n");
                    sb.append("</select>\n");
                    sb.append("</td>");
                    sb.append("<td></td>");
                    sb.append("</tr>\n");
                }

                if (RequestType.action.equals(reqType)
                        || RequestType.other.equals(reqType)
                        || !getOptionsParamApi().isNoKeyForSafeOps()) {
                    String keyType = API.API_KEY_PARAM;
                    if (this.isDevTestNonce && RequestType.other.equals(reqType)) {
                        // We can use nonces as we know the return type
                        keyType = API.API_NONCE_PARAM;
                    }
                    if (!getOptionsParamApi().isDisableKey()) {
                        sb.append("<tr>");
                        sb.append("<td>");
                        sb.append(keyType);
                        sb.append("*</td>");
                        sb.append("<td>");
                        sb.append("<input id=\"");
                        sb.append(keyType);
                        sb.append("\" name=\"");
                        sb.append(keyType);
                        sb.append("\" value=\"");
                        if (this.isDevTestNonce && RequestType.other.equals(reqType)) {
                            sb.append(
                                    api.getOneTimeNonce(
                                            "/"
                                                    + reqType.name().toUpperCase(Locale.ROOT)
                                                    + "/"
                                                    + impl.getPrefix()
                                                    + "/"
                                                    + reqType.name()
                                                    + "/"
                                                    + element.getName()
                                                    + "/"));
                        } else {
                            if (getOptionsParamApi().isAutofillKey()) {
                                sb.append(getOptionsParamApi().getKey());
                            }
                        }
                        sb.append("\"/>");
                        sb.append("</td>");
                        sb.append("<td></td>");
                        sb.append("</tr>\n");
                    }
                    sb.append("<tr>");
                    sb.append("<td>");
                    sb.append(Constant.messages.getString("api.html.formMethod"));
                    sb.append("</td>");
                    sb.append("<td>");
                    sb.append("<select id=\"formMethod\">\n");
                    sb.append("<option value=\"GET\" selected>GET</option>\n");
                    sb.append("<option value=\"POST\">POST</option>\n");
                    sb.append("</select>\n");
                    sb.append("</td>");
                    sb.append("<td></td>");
                    sb.append("</tr>\n");
                }

                appendParams(sb, element.getParameters());

                sb.append("<tr>");
                sb.append("<td>");
                sb.append("</td>");
                sb.append("<td>");
                sb.append("<input id=\"button\" value=\"");
                sb.append(element.getName());
                sb.append(
                        "\" type=\"submit\" zap-component=\""
                                + component
                                + "\" zap-type=\""
                                + reqType
                                + "\" zap-name=\""
                                + name
                                + "\"/>\n");
                sb.append("</td>");
                sb.append("<td></td>");
                sb.append("</tr>\n");
                sb.append("</table>\n");
                sb.append("</form>\n");

            } else {
                if (Constant.messages.containsKey(impl.getDescriptionKey())) {
                    sb.append("<p>\n");
                    sb.append(Constant.messages.getString(impl.getDescriptionKey()));
                    sb.append("\n</p>\n");
                }

                List<ApiElement> elementList = new ArrayList<>();
                List<ApiView> viewList = impl.getApiViews();
                if (viewList != null && viewList.size() > 0) {
                    sb.append("<h3>");
                    sb.append(Constant.messages.getString("api.html.views"));
                    sb.append("</h3>\n");
                    elementList.addAll(viewList);
                    this.appendElements(sb, component, RequestType.view.name(), elementList);
                }

                List<ApiAction> actionList = impl.getApiActions();
                if (actionList != null && actionList.size() > 0) {
                    sb.append("<h3>");
                    sb.append(Constant.messages.getString("api.html.actions"));
                    sb.append("</h3>\n");
                    elementList = new ArrayList<>();
                    elementList.addAll(actionList);
                    this.appendElements(sb, component, RequestType.action.name(), elementList);
                }

                List<ApiOther> otherList = impl.getApiOthers();
                if (otherList != null && otherList.size() > 0) {
                    sb.append("<h3>");
                    sb.append(Constant.messages.getString("api.html.others"));
                    sb.append("</h3>\n");
                    elementList = new ArrayList<>();
                    elementList.addAll(otherList);
                    this.appendElements(sb, component, RequestType.other.name(), elementList);
                }

                List<ApiPersistentConnection> pconnList = impl.getApiPersistentConnections();
                if (pconnList != null && pconnList.size() > 0) {
                    sb.append("<h3>");
                    sb.append(Constant.messages.getString("api.html.pconns"));
                    sb.append("</h3>\n");
                    elementList = new ArrayList<>();
                    elementList.addAll(pconnList);
                    this.appendElements(sb, component, RequestType.pconn.name(), elementList);
                }

                if (getOptionsParamApi().isDisableKey()
                        || getOptionsParamApi().isAutofillKey()
                        || this.isDevTestNonce) {
                    // Only show shortcuts if they will work without the user having to add a
                    // key/nonce
                    List<String> shortcutList = impl.getApiShortcuts();
                    if (shortcutList != null && shortcutList.size() > 0) {
                        sb.append("<h3>");
                        sb.append(Constant.messages.getString("api.html.shortcuts"));
                        sb.append("</h3>\n");
                        elementList = new ArrayList<>();
                        elementList.addAll(otherList);
                        this.appendShortcuts(sb, component, shortcutList);
                    }
                }
            }

        } else {
            sb.append("<h3>");
            sb.append(Constant.messages.getString("api.html.components"));
            sb.append("</h3>\n");
            List<ApiImplementor> components = new ArrayList<>(api.getImplementors().values());
            Collections.sort(components, Comparator.comparing(ApiImplementor::getPrefix));

            sb.append("<table>\n");
            for (ApiImplementor cmp : components) {
                sb.append("<tr>");
                sb.append("<td>");
                sb.append("<a href=\"/");
                sb.append(Format.UI.name());
                sb.append('/');
                sb.append(cmp.getPrefix());
                sb.append("/\">");
                sb.append(cmp.getPrefix());
                sb.append("</a>");
                sb.append("</td>");
                sb.append("<td>");
                if (Constant.messages.containsKey(cmp.getDescriptionKey())) {
                    sb.append(Constant.messages.getString(cmp.getDescriptionKey()));
                }
                sb.append("</td>");
                sb.append("</tr>\n");
            }
            sb.append("</table>\n");
        }
        sb.append("</body>\n");

        return sb.toString();
    }

    private static void appendParams(StringBuilder sb, List<ApiParameter> params) {
        for (ApiParameter param : params) {
            sb.append("<tr>");
            sb.append("<td>");
            sb.append(param.getName());
            if (param.isRequired()) {
                sb.append('*');
            }
            sb.append("</td>");
            sb.append("<td>");
            sb.append("<input id=\"");
            sb.append(param.getName());
            sb.append("\" name=\"");
            sb.append(param.getName());
            sb.append("\"/>");
            sb.append("</td><td>");
            String descKey = param.getDescriptionKey();
            if (Constant.messages.containsKey(descKey)) {
                sb.append(Constant.messages.getString(descKey));
            }
            sb.append("</td>");
            sb.append("</tr>\n");
        }
    }

    public String handleRequest(URI uri, boolean apiEnabled) {
        // Right now just generate a basic home page
        StringBuilder sb = new StringBuilder();
        sb.append("<head>\n");
        sb.append("<title>");
        sb.append(Constant.messages.getString("api.html.title"));
        sb.append("</title>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append(Constant.messages.getString("api.home.topmsg"));
        sb.append(
                Constant.messages.getString(
                        "api.home.proxypac", getApiPathWithNonceParam(PAC_FILE_API_PATH)));
        sb.append(
                Constant.messages.getString(
                        "api.home.cacert", getApiPathWithNonceParam(ROOT_CA_CERT_API_PATH)));
        sb.append(Constant.messages.getString("api.home.links.header"));
        if (apiEnabled) {
            sb.append(Constant.messages.getString("api.home.links.api.enabled"));
        } else {
            sb.append(Constant.messages.getString("api.home.links.api.disabled"));
        }
        sb.append(Constant.messages.getString("api.home.links.online"));
        sb.append("</body>\n");

        return sb.toString();
    }

    private static String getApiPathWithNonceParam(String path) {
        return path + '?' + API.API_NONCE_PARAM + '=' + API.getInstance().getLongLivedNonce(path);
    }

    private OptionsParamApi getOptionsParamApi() {
        return Model.getSingleton().getOptionsParam().getApiParam();
    }
}
