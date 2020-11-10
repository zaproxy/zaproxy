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
package org.parosproxy.paros.db;

public class RecordContext {

    public static final int TYPE_NAME = 1;
    public static final int TYPE_DESCRIPTION = 2;
    public static final int TYPE_INCLUDE = 3;
    public static final int TYPE_EXCLUDE = 4;
    public static final int TYPE_IN_SCOPE = 5;
    public static final int TYPE_INCLUDE_TECH = 6;
    public static final int TYPE_EXCLUDE_TECH = 7;

    public static final int TYPE_AUTH_LOGIN_URL = 101;
    public static final int TYPE_AUTH_LOGIN_POST_DATA = 102;
    public static final int TYPE_AUTH_LOGIN_INDICATOR = 103;
    public static final int TYPE_AUTH_LOGOUT_URL = 104;
    public static final int TYPE_AUTH_LOGOUT_POST_DATA = 105;
    public static final int TYPE_AUTH_LOGOUT_INDICATOR = 106;

    public static final int TYPE_AUTH_METHOD_TYPE = 200;
    public static final int TYPE_AUTH_METHOD_FIELD_1 = 201;
    public static final int TYPE_AUTH_METHOD_FIELD_2 = 202;
    public static final int TYPE_AUTH_METHOD_FIELD_3 = 203;
    public static final int TYPE_AUTH_METHOD_FIELD_4 = 204;
    public static final int TYPE_AUTH_METHOD_FIELD_5 = 205;
    public static final int TYPE_AUTH_METHOD_LOGGEDIN_INDICATOR = 206;
    public static final int TYPE_AUTH_METHOD_LOGGEDOUT_INDICATOR = 207;
    public static final int TYPE_AUTH_VERIF_STRATEGY = 208;
    public static final int TYPE_AUTH_POLL_URL = 209;
    public static final int TYPE_AUTH_POLL_DATA = 210;
    public static final int TYPE_AUTH_POLL_FREQ = 211;
    public static final int TYPE_AUTH_POLL_FREQ_UNITS = 212;
    public static final int TYPE_AUTH_POLL_HEADERS = 213;

    public static final int TYPE_SESSION_MANAGEMENT_TYPE = 220;
    public static final int TYPE_SESSION_MANAGEMENT_FIELD_1 = 221;
    public static final int TYPE_SESSION_MANAGEMENT_FIELD_2 = 222;
    public static final int TYPE_SESSION_MANAGEMENT_FIELD_3 = 223;
    public static final int TYPE_SESSION_MANAGEMENT_FIELD_4 = 224;
    public static final int TYPE_SESSION_MANAGEMENT_FIELD_5 = 225;

    public static final int TYPE_AUTHORIZATION_METHOD_TYPE = 230;
    public static final int TYPE_AUTHORIZATION_METHOD_FIELD_1 = 231;
    public static final int TYPE_AUTHORIZATION_METHOD_FIELD_2 = 232;
    public static final int TYPE_AUTHORIZATION_METHOD_FIELD_3 = 233;
    public static final int TYPE_AUTHORIZATION_METHOD_FIELD_4 = 234;

    public static final int TYPE_USER = 300;
    public static final int TYPE_FORCED_USER_ID = 310;

    public static final int TYPE_URL_PARSER_CLASSNAME = 400;
    public static final int TYPE_URL_PARSER_CONFIG = 401;
    public static final int TYPE_POST_PARSER_CLASSNAME = 402;
    public static final int TYPE_POST_PARSER_CONFIG = 403;
    public static final int TYPE_DATA_DRIVEN_NODES = 404;

    public static final int TYPE_ACCESS_CONTROL_RULE = 410;

    private long dataId = 0;
    private int contextId = 0;
    private int type = 0;
    private String data = "";

    public RecordContext(long dataId, int contextId, int type, String data) {
        super();
        this.contextId = contextId;
        this.dataId = dataId;
        this.type = type;
        this.data = data;
    }

    public int getContextId() {
        return contextId;
    }

    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    public long getDataId() {
        return dataId;
    }

    public void setDataId(long dataId) {
        this.dataId = dataId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
