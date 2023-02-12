/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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
package org.zaproxy.zap.extension.pscan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;
import org.zaproxy.zap.extension.custompages.CustomPage;
import org.zaproxy.zap.extension.custompages.ExtensionCustomPages;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.users.User;

/**
 * A utility class to simplify providing {@code Context} data to passive scan rules. Details will be
 * based on the first {@code Context} matched (if any).
 *
 * @see PassiveScanThread
 * @see PluginPassiveScanner
 * @since 2.9.0
 */
public class PassiveScanData {

    private static final Logger LOGGER = LogManager.getLogger(PassiveScanData.class);

    private static ExtensionUserManagement extUserMgmt = null;

    private final HttpMessage message;
    private final Context context;
    private final TechSet techSet;

    private List<User> userList = null;
    private Map<CustomPage.Type, Boolean> customPageMap;

    PassiveScanData(HttpMessage msg) {
        this.message = msg;
        this.context = getContext(message);

        if (getContext() == null) {
            this.userList = Collections.emptyList();
            this.techSet = TechSet.getAllTech();
        } else {
            this.techSet = getContext().getTechSet();
        }
    }

    public HttpMessage getMessage() {
        return message;
    }

    private static Context getContext(HttpMessage message) {
        List<Context> contextList =
                Model.getSingleton()
                        .getSession()
                        .getContextsForUrl(message.getRequestHeader().getURI().toString());
        if (contextList.isEmpty()) {
            LOGGER.debug("No Context found for: {}", message.getRequestHeader().getURI());
            return null;
        }
        return contextList.get(0);
    }

    /**
     * Returns an unmodifiable list of {@code User}s for the {@code HttpMessage} being passively
     * scanned. The list returned is based on the first {@code Context} matched.
     *
     * @return A list of users if some are available, an empty list otherwise.
     */
    public List<User> getUsers() {
        if (userList != null) {
            return userList;
        }
        if (getExtensionUserManagement() == null) {
            userList = Collections.emptyList();
            return userList;
        }
        userList =
                Collections.unmodifiableList(
                        new ArrayList<>(
                                getExtensionUserManagement()
                                        .getContextUserAuthManager(getContext().getId())
                                        .getUsers()));
        return userList;
    }

    private static ExtensionUserManagement getExtensionUserManagement() {
        return extUserMgmt != null
                ? extUserMgmt
                : Control.getSingleton()
                        .getExtensionLoader()
                        .getExtension(ExtensionUserManagement.class);
    }

    /**
     * Sets the {@code ExtensionUserManagement} being used, to facilitate testing.
     *
     * @param extUserMgmt the extension to the set.
     */
    static void setExtUserMgmt(ExtensionUserManagement extUserMgmt) {
        PassiveScanData.extUserMgmt = extUserMgmt;
    }

    /**
     * Returns a boolean indicating whether or not the {@code HttpMessage} being passively scanned
     * is currently associated with a {@code Context}.
     *
     * @return true if there is an associated context, false if not.
     */
    public boolean hasContext() {
        return context != null;
    }

    /**
     * Returns the {@code Context} associated with the message being passively scanned.
     *
     * @return the {@code Context} if the message has been matched to a Context, {@code null}
     *     otherwise.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Returns the {@code TechSet} associated with the Context of the message being passively
     * scanned.
     *
     * @return the {@code TechSet} if the message has been matched to a Context, {@code
     *     TechSet.AllTech} otherwise.
     */
    public TechSet getTechSet() {
        return techSet;
    }

    /**
     * Tells whether or not the message matches the specific {@code CustomPage.Type}
     *
     * @param msg the message that will be checked
     * @param cpType the custom page type to be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     */
    private boolean isCustomPage(HttpMessage msg, CustomPage.Type cpType) {
        if (context == null) {
            return false;
        }
        if (customPageMap == null) {
            customPageMap = new HashMap<>();
        }
        return customPageMap.computeIfAbsent(
                cpType, type -> context.isCustomPageWithFallback(msg, type));
    }

    /**
     * Tells whether or not the message matches {@code CustomPage.Type.OK_200} definitions. Falls
     * back to simply checking the response status code for "200 - Ok". Checks if the message
     * matches {@code CustomPage.Type.ERROR_500} or {@code CusotmPage.Type.NOTFOUND_404} first, in
     * case the user is trying to override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    public boolean isPage200(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.NOTFOUND_404)
                || isCustomPage(msg, CustomPage.Type.ERROR_500)) {
            return false;
        }
        if (isCustomPage(msg, CustomPage.Type.OK_200)) {
            return true;
        }
        return msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK;
    }

    /**
     * Tells whether or not the message matches {@code CustomPage.Type.ERROR_500} definitions. Falls
     * back to simply checking the response status code for "500 - Internal Server Error". Checks if
     * the message matches {@code CustomPage.Type.OK_200} or {@code CustomPage.Type.NOTFOUND_404}
     * first, in case the user is trying to override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    public boolean isPage500(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.OK_200)
                || isCustomPage(msg, CustomPage.Type.NOTFOUND_404)) {
            return false;
        }
        if (isCustomPage(msg, CustomPage.Type.ERROR_500)) {
            return true;
        }
        return msg.getResponseHeader().getStatusCode() == HttpStatusCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * Tells whether or not the message matches {@code CustomPage.Type.NOTFOUND_404} definitions.
     * Falls back to simply checking the response status code for "404 - Not Found". Checks if the
     * message matches {@code CustomPage.Type.OK_200} or {@code CustomPage.Type.ERROR_500} first, in
     * case the user is trying to override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    public boolean isPage404(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.OK_200)
                || isCustomPage(msg, CustomPage.Type.ERROR_500)) {
            return false;
        }
        if (isCustomPage(msg, CustomPage.Type.NOTFOUND_404)) {
            return true;
        }
        return msg.getResponseHeader().getStatusCode() == HttpStatusCode.NOT_FOUND;
    }

    /**
     * Tells whether or not the message matches {@code CustomPage.Type.OTHER} definitions.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    public boolean isPageOther(HttpMessage msg) {
        return isCustomPage(msg, CustomPage.Type.OTHER);
    }

    /**
     * Tells whether or not the message matches {@code CustomPage.Type.AUTH_4XX} definitions. Falls
     * back to simply checking the response status code for "401 - Unauthorized" or "403 -
     * Forbidden". Checks if the message matches {@code CustomPage.Type.OK_200} first, in case the
     * user is trying to override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.12.0
     */
    public boolean isPageAuthIssue(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.OK_200)) {
            return false;
        }
        if (isCustomPage(msg, CustomPage.Type.AUTH_4XX)) {
            return true;
        }
        return ExtensionCustomPages.AUTH_HTTP_STATUS_CODES.contains(
                msg.getResponseHeader().getStatusCode());
    }

    /**
     * Tells whether or not the response has a status code between 200 and 299 (inclusive), or
     * {@code CustomPage.Type.OK_200}. Checks if the message matches {@code
     * CustomPage.Type.NOTFOUND_404} or {@code CusotmPage.Type.ERROR_500} first, in case the user is
     * trying to override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    public boolean isSuccess(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.NOTFOUND_404)
                || isCustomPage(msg, CustomPage.Type.ERROR_500)) {
            return false;
        }
        if (isCustomPage(msg, CustomPage.Type.OK_200)) {
            return true;
        }
        return HttpStatusCode.isSuccess(msg.getResponseHeader().getStatusCode());
    }

    /**
     * Tells whether or not the response has a status code between 400 and 499 (inclusive), or
     * {@code CustomPage.Type.NOTFOUND_404}. Checks if the message matches {@code
     * CustomPage.Type.OK_200} or {@code CusotmPage.Type.ERROR_500} first, in case the user is
     * trying to override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    public boolean isClientError(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.OK_200)
                || isCustomPage(msg, CustomPage.Type.ERROR_500)) {
            return false;
        }
        if (isCustomPage(msg, CustomPage.Type.NOTFOUND_404)) {
            return true;
        }
        return HttpStatusCode.isClientError(msg.getResponseHeader().getStatusCode());
    }

    /**
     * Tells whether or not the response has a status code between 500 and 599 (inclusive), or
     * {@code CustomPage.Type.EROOR_500}. Checks if the message matches {@code
     * CustomPage.Type.OK_200} or {@code CustomPage.Type.NOTFOUND_404} first, in case the user is
     * trying to override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    public boolean isServerError(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.OK_200)
                || isCustomPage(msg, CustomPage.Type.NOTFOUND_404)) {
            return false;
        }
        if (isCustomPage(msg, CustomPage.Type.ERROR_500)) {
            return true;
        }
        return HttpStatusCode.isServerError(msg.getResponseHeader().getStatusCode());
    }
}
