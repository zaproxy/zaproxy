/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.extension.forceduser;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.extension.authentication.AuthenticationAPI;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;

/** The API for managing the Forced User for a Context. */
public class ForcedUserAPI extends ApiImplementor {

    private static final Logger log = LogManager.getLogger(AuthenticationAPI.class);

    private static final String PREFIX = "forcedUser";

    private static final String VIEW_GET_FORCED_USER = "getForcedUser";
    private static final String VIEW_IS_FORCED_USER_MODE_ENABLED = "isForcedUserModeEnabled";

    private static final String ACTION_SET_FORCED_USER = "setForcedUser";
    private static final String ACTION_SET_FORCED_USER_MODE_ENABLED = "setForcedUserModeEnabled";

    private static final String PARAM_USER_ID = "userId";
    private static final String PARAM_CONTEXT_ID = "contextId";
    private static final String PARAM_MODE_ENABLED = "boolean";

    private ExtensionForcedUser extension;

    public ForcedUserAPI(ExtensionForcedUser extension) {
        super();
        this.extension = extension;

        this.addApiView(new ApiView(VIEW_IS_FORCED_USER_MODE_ENABLED));
        this.addApiView(new ApiView(VIEW_GET_FORCED_USER, new String[] {PARAM_CONTEXT_ID}));

        this.addApiAction(
                new ApiAction(
                        ACTION_SET_FORCED_USER, new String[] {PARAM_CONTEXT_ID, PARAM_USER_ID}));
        this.addApiAction(
                new ApiAction(
                        ACTION_SET_FORCED_USER_MODE_ENABLED, new String[] {PARAM_MODE_ENABLED}));
    }

    @Override
    public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
        log.debug("handleApiView {} {}", name, params);

        switch (name) {
            case VIEW_GET_FORCED_USER:
                Context context = ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID);
                User forcedUser = extension.getForcedUser(context.getId());
                if (forcedUser != null)
                    return new ApiResponseElement(
                            "forcedUserId", Integer.toString(forcedUser.getId()));
                else return new ApiResponseElement("forcedUserId", "");
            case VIEW_IS_FORCED_USER_MODE_ENABLED:
                return new ApiResponseElement(
                        "forcedModeEnabled", Boolean.toString(extension.isForcedUserModeEnabled()));
            default:
                throw new ApiException(Type.BAD_VIEW);
        }
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        log.debug("handleApiAction {} {}", name, params);
        Context context;
        switch (name) {
            case ACTION_SET_FORCED_USER:
                context = ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID);
                int userId = ApiUtils.getIntParam(params, PARAM_USER_ID);
                try {
                    extension.setForcedUser(context.getId(), userId);
                } catch (IllegalStateException ex) {
                    throw new ApiException(Type.USER_NOT_FOUND);
                }
                context.save();
                return ApiResponseElement.OK;
            case ACTION_SET_FORCED_USER_MODE_ENABLED:
                if (!params.containsKey(PARAM_MODE_ENABLED))
                    throw new ApiException(Type.MISSING_PARAMETER, PARAM_MODE_ENABLED);
                boolean newModeStatus;
                try {
                    newModeStatus = params.getBoolean(PARAM_MODE_ENABLED);
                } catch (JSONException ex) {
                    throw new ApiException(Type.ILLEGAL_PARAMETER, PARAM_MODE_ENABLED);
                }
                extension.setForcedUserModeEnabled(newModeStatus);
                return ApiResponseElement.OK;
            default:
                throw new ApiException(Type.BAD_ACTION);
        }
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
