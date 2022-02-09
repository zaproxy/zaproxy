/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.extension.proxies;

import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONObject;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;

/** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
@Deprecated
public class ProxiesAPI extends ApiImplementor {

    private static final String PREFIX = "localProxies";

    private static final String VIEW_ADDITIONAL_PROXIES = "additionalProxies";
    private static final String ACTION_ADD_PROXY = "addAdditionalProxy";
    private static final String ACTION_REMOVE_PROXY = "removeAdditionalProxy";
    private static final String PARAM_ADDRESS = "address";
    private static final String PARAM_PORT = "port";
    private static final String PARAM_BEHIND_NAT = "behindNat";
    private static final String PARAM_DECODE_ZIP = "alwaysDecodeZip";
    private static final String PARAM_REM_UNSUPPORTED_ENC = "removeUnsupportedEncodings";

    private ExtensionProxies extension = null;

    public ProxiesAPI(ExtensionProxies ext) {
        this.extension = ext;
        this.addApiView(new ApiView(VIEW_ADDITIONAL_PROXIES));
        this.addApiAction(
                new ApiAction(
                        ACTION_ADD_PROXY,
                        new String[] {PARAM_ADDRESS, PARAM_PORT},
                        new String[] {
                            PARAM_BEHIND_NAT, PARAM_DECODE_ZIP, PARAM_REM_UNSUPPORTED_ENC
                        }));
        this.addApiAction(
                new ApiAction(ACTION_REMOVE_PROXY, new String[] {PARAM_ADDRESS, PARAM_PORT}));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
        if (VIEW_ADDITIONAL_PROXIES.equals(name)) {
            ApiResponseList response = new ApiResponseList(name);

            for (ProxiesParamProxy p : (this.extension.getAdditionalProxies())) {
                Map<String, String> map = new HashMap<>();
                map.put("address", p.getAddress());
                map.put("port", Integer.toString(p.getPort()));
                map.put("enabled", Boolean.toString(p.isEnabled()));
                map.put("behindNat", Boolean.toString(p.isBehindNat()));
                map.put("alwaysDecodeZip", Boolean.toString(p.isAlwaysDecodeGzip()));
                map.put(
                        "removeUnsupportedEncodings",
                        Boolean.toString(p.isRemoveUnsupportedEncodings()));
                response.addItem(new ApiResponseSet<>("proxy", map));
            }

            return response;
        } else {
            throw new ApiException(ApiException.Type.BAD_VIEW, name);
        }
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        if (ACTION_ADD_PROXY.equals(name)) {
            try {
                extension.addProxy(
                        new ProxiesParamProxy(
                                params.getString(PARAM_ADDRESS),
                                params.getInt(PARAM_PORT),
                                true,
                                false,
                                this.getParam(params, PARAM_REM_UNSUPPORTED_ENC, false),
                                this.getParam(params, PARAM_DECODE_ZIP, false),
                                this.getParam(params, PARAM_BEHIND_NAT, false)));
                return ApiResponseElement.OK;
            } catch (Exception e) {
                throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, e.getMessage());
            }
        } else if (ACTION_REMOVE_PROXY.equals(name)) {
            try {
                extension.removeProxy(params.getString(PARAM_ADDRESS), params.getInt(PARAM_PORT));
                return ApiResponseElement.OK;
            } catch (Exception e) {
                throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, e.getMessage());
            }
        } else {
            throw new ApiException(ApiException.Type.BAD_VIEW, name);
        }
    }
}
