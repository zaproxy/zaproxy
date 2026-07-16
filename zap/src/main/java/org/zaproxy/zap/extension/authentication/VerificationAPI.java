/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.zaproxy.zap.extension.authentication;

import java.io.IOException;
import java.util.regex.Pattern;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthCheckingStrategy;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthPollFrequencyUnits;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseConversionUtils;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;

/**
 * The API for manipulating the {@link org.zaproxy.zap.authentication.VerificationMethod
 * VerificationMethod} for {@link Context Contexts}.
 */
public class VerificationAPI extends ApiImplementor {

    private static final Logger LOGGER = LogManager.getLogger(VerificationAPI.class);

    private static final String PREFIX = "verification";

    private static final String VIEW_GET_LOGGED_IN_INDICATOR = "getLoggedInIndicator";
    private static final String VIEW_GET_LOGGED_OUT_INDICATOR = "getLoggedOutIndicator";

    private static final String ACTION_SET_LOGGED_IN_INDICATOR = "setLoggedInIndicator";
    private static final String ACTION_SET_LOGGED_OUT_INDICATOR = "setLoggedOutIndicator";
    private static final String ACTION_SET_VERIFICATION_METHOD = "setVerificationMethod";
    private static final String ACTION_POLL_AS_USER = "pollAsUser";

    public static final String PARAM_CONTEXT_ID = "contextId";
    public static final String PARAM_USER_ID = "userId";
    private static final String PARAM_LOGGED_IN_INDICATOR = "loggedInIndicatorRegex";
    private static final String PARAM_LOGGED_OUT_INDICATOR = "loggedOutIndicatorRegex";
    private static final String PARAM_CHECKING_STRATEGY = "checkingStrategy";
    private static final String PARAM_POLL_URL = "pollUrl";
    private static final String PARAM_POLL_DATA = "pollData";
    private static final String PARAM_POLL_HEADERS = "pollHeaders";
    private static final String PARAM_POLL_FREQ = "pollFrequency";
    private static final String PARAM_POLL_FREQ_UNITS = "pollFrequencyUnits";

    public VerificationAPI() {
        super();

        this.addApiView(new ApiView(VIEW_GET_LOGGED_IN_INDICATOR, new String[] {PARAM_CONTEXT_ID}));
        this.addApiView(
                new ApiView(VIEW_GET_LOGGED_OUT_INDICATOR, new String[] {PARAM_CONTEXT_ID}));

        this.addApiAction(
                new ApiAction(
                        ACTION_SET_LOGGED_IN_INDICATOR,
                        new String[] {PARAM_CONTEXT_ID, PARAM_LOGGED_IN_INDICATOR}));
        this.addApiAction(
                new ApiAction(
                        ACTION_SET_LOGGED_OUT_INDICATOR,
                        new String[] {PARAM_CONTEXT_ID, PARAM_LOGGED_OUT_INDICATOR}));
        this.addApiAction(
                new ApiAction(
                        ACTION_SET_VERIFICATION_METHOD,
                        new String[] {PARAM_CONTEXT_ID, PARAM_CHECKING_STRATEGY},
                        new String[] {
                            PARAM_POLL_URL,
                            PARAM_POLL_DATA,
                            PARAM_POLL_HEADERS,
                            PARAM_POLL_FREQ,
                            PARAM_POLL_FREQ_UNITS
                        }));
        this.addApiAction(
                new ApiAction(ACTION_POLL_AS_USER, new String[] {PARAM_CONTEXT_ID, PARAM_USER_ID}));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
        LOGGER.debug("handleApiView {} {}", name, params);

        switch (name) {
            case VIEW_GET_LOGGED_IN_INDICATOR:
                Pattern loggedInPattern =
                        getContext(params).getVerificationMethod().getLoggedInIndicatorPattern();
                return new ApiResponseElement(
                        "logged_in_regex",
                        loggedInPattern != null ? loggedInPattern.toString() : "");
            case VIEW_GET_LOGGED_OUT_INDICATOR:
                Pattern loggedOutPattern =
                        getContext(params).getVerificationMethod().getLoggedOutIndicatorPattern();
                return new ApiResponseElement(
                        "logged_out_regex",
                        loggedOutPattern != null ? loggedOutPattern.toString() : "");
            default:
                throw new ApiException(ApiException.Type.BAD_VIEW);
        }
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        LOGGER.debug("handleApiAction {} {}", name, params);

        Context context;
        switch (name) {
            case ACTION_SET_LOGGED_IN_INDICATOR:
                String loggedInIndicator = params.getString(PARAM_LOGGED_IN_INDICATOR);
                if (loggedInIndicator == null || loggedInIndicator.isEmpty())
                    throw new ApiException(Type.MISSING_PARAMETER, PARAM_LOGGED_IN_INDICATOR);
                context = getContext(params);
                context.getVerificationMethod().setLoggedInIndicatorPattern(loggedInIndicator);
                context.save();
                return ApiResponseElement.OK;

            case ACTION_SET_LOGGED_OUT_INDICATOR:
                String loggedOutIndicator = params.getString(PARAM_LOGGED_OUT_INDICATOR);
                if (loggedOutIndicator == null || loggedOutIndicator.isEmpty())
                    throw new ApiException(Type.MISSING_PARAMETER, PARAM_LOGGED_OUT_INDICATOR);
                context = getContext(params);
                context.getVerificationMethod().setLoggedOutIndicatorPattern(loggedOutIndicator);
                context.save();
                return ApiResponseElement.OK;

            case ACTION_SET_VERIFICATION_METHOD:
                context = getContext(params);
                AuthCheckingStrategy strategy;
                try {
                    strategy =
                            AuthCheckingStrategy.valueOf(params.getString(PARAM_CHECKING_STRATEGY));
                } catch (Exception e) {
                    throw new ApiException(
                            ApiException.Type.ILLEGAL_PARAMETER, PARAM_CHECKING_STRATEGY);
                }
                if (AuthCheckingStrategy.POLL_URL.equals(strategy)) {
                    AuthPollFrequencyUnits units;
                    try {
                        units =
                                AuthPollFrequencyUnits.valueOf(
                                        params.getString(PARAM_POLL_FREQ_UNITS));
                    } catch (Exception e) {
                        throw new ApiException(
                                ApiException.Type.ILLEGAL_PARAMETER, PARAM_POLL_FREQ_UNITS);
                    }
                    String pollUrl = params.getString(PARAM_POLL_URL);
                    if (pollUrl == null || pollUrl.isEmpty()) {
                        throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_POLL_URL);
                    }
                    try {
                        new URI(pollUrl, true);
                    } catch (Exception e) {
                        throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_POLL_URL);
                    }
                    int freq;
                    try {
                        freq = params.getInt(PARAM_POLL_FREQ);
                    } catch (Exception e) {
                        throw new ApiException(
                                ApiException.Type.ILLEGAL_PARAMETER, PARAM_POLL_FREQ);
                    }
                    if (freq <= 0) {
                        throw new ApiException(
                                ApiException.Type.ILLEGAL_PARAMETER, PARAM_POLL_FREQ);
                    }
                    String pollData = params.getString(PARAM_POLL_DATA);
                    String pollHeaders = params.getString(PARAM_POLL_HEADERS);
                    context.getVerificationMethod().setPollUrl(pollUrl);
                    context.getVerificationMethod().setPollData(pollData);
                    context.getVerificationMethod().setPollHeaders(pollHeaders);
                    context.getVerificationMethod().setPollFrequency(freq);
                    context.getVerificationMethod().setPollFrequencyUnits(units);
                }
                context.getVerificationMethod().setAuthCheckingStrategy(strategy);
                Model.getSingleton().getSession().saveContext(context);
                return ApiResponseElement.OK;

            case ACTION_POLL_AS_USER:
                User user = getUser(params);
                try {
                    HttpMessage msg = user.getContext().getVerificationMethod().pollAsUser(user);
                    int href = -1;
                    if (msg.getHistoryRef() != null) {
                        href = msg.getHistoryRef().getHistoryId();
                    }
                    ApiResponseSet<String> responseSet =
                            ApiResponseConversionUtils.httpMessageToSet(href, msg);
                    responseSet.put(
                            "pollSuccessful",
                            Boolean.toString(
                                    user.getContext()
                                            .getVerificationMethod()
                                            .evaluateAuthRequest(
                                                    msg, user.getAuthenticationState())));
                    return responseSet;
                } catch (IllegalArgumentException e) {
                    throw new ApiException(Type.ILLEGAL_PARAMETER, PARAM_CONTEXT_ID);
                } catch (IOException e) {
                    throw new ApiException(Type.INTERNAL_ERROR, e);
                }

            default:
                throw new ApiException(Type.BAD_ACTION);
        }
    }

    private Context getContext(JSONObject params) throws ApiException {
        return ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID);
    }

    private User getUser(JSONObject params) throws ApiException {
        int contextId = ApiUtils.getIntParam(params, PARAM_CONTEXT_ID);
        int userId = ApiUtils.getIntParam(params, PARAM_USER_ID);
        ExtensionUserManagement usersExtension =
                Control.getSingleton()
                        .getExtensionLoader()
                        .getExtension(ExtensionUserManagement.class);
        if (usersExtension == null) {
            throw new ApiException(Type.DOES_NOT_EXIST, PARAM_USER_ID);
        }
        User user = usersExtension.getContextUserAuthManager(contextId).getUserById(userId);
        if (user == null) throw new ApiException(Type.USER_NOT_FOUND, PARAM_USER_ID);
        return user;
    }
}
