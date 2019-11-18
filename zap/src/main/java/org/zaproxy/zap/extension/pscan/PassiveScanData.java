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
import java.util.List;
import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
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
 * @since TODO add version
 */
public final class PassiveScanData {

    private static final Logger LOGGER = Logger.getLogger(PassiveScanData.class);

    private final HttpMessage message;
    private final Context context;

    private final TechSet techSet;

    private List<User> userList = null;

    PassiveScanData(HttpMessage msg) {
        this.message = msg;
        this.context = getContext(message);

        if (getContext() == null) {
            this.userList = Collections.emptyList();
            this.techSet = TechSet.AllTech;
        } else {
            this.techSet = getContext().getTechSet();
        }
    }

    private static Context getContext(HttpMessage message) {
        List<Context> contextList =
                Model.getSingleton()
                        .getSession()
                        .getContextsForUrl(message.getRequestHeader().getURI().toString());
        if (contextList.isEmpty()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "No Context found for: " + message.getRequestHeader().getURI().toString());
            }
            return null;
        }
        return contextList.get(0);
    }

    /**
     * Returns a list of {@Code User}'s for the {@code HttpMessage} being passively scanned. The
     * list returned is based on the first {@code Context} matched.
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
        return Control.getSingleton()
                .getExtensionLoader()
                .getExtension(ExtensionUserManagement.class);
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
}
