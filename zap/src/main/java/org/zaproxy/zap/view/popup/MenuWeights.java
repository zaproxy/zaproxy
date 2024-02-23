/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
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
package org.zaproxy.zap.view.popup;

/**
 * This class is duplicated in the commonlib add-on so that it can be used prior to a full release.
 * It defines the "weights" applied to menu items - the higher the weight the higher it appears in
 * the menu. Weights are not expected to be consecutive. The ZAP code automatically adds horizontal
 * lines inbetween '100' boundaries. There will be one line between 110 and 220 and still just one
 * line between 220 and 550.
 */
public class MenuWeights {

    public static final int MENU_DEFAULT_WEIGHT = 1000;

    // Main Sites / History etc menus
    public static final int MENU_ATTACK_WEIGHT = 29090;
    public static final int MENU_BREAK_WEIGHT = 29080;
    public static final int MENU_DELETE_WEIGHT = 29070;
    public static final int MENU_EXCLUDE_WEIGHT = 29060;
    public static final int MENU_FIND_WEIGHT = 29050;
    public static final int MENU_ENCODE_WEIGHT = 29040;
    public static final int MENU_RUN_APP_WEIGHT = 29030;
    public static final int MENU_NEW_ALERT_WEIGHT = 29020;
    public static final int MENU_ALERTS_NODE_WEIGHT = 29010;
    // ---
    public static final int MENU_INC_CONTEXT_WEIGHT = 27090;
    public static final int MENU_INC_SITE_CONTEXT_WEIGHT = 27080;
    public static final int MENU_EXC_CONTEXT_WEIGHT = 27070;
    public static final int MENU_FLAG_CONTEXT_WEIGHT = 27060;
    public static final int MENU_INC_CHANNEL_CONTEXT_WEIGHT = 27050;
    public static final int MENU_EXC_CHANNEL_CONTEXT_WEIGHT = 27040;
    // ---
    public static final int MENU_OPEN_REQUEST_WEIGHT = 25060;
    public static final int MENU_OPEN_REQUESTER_WEIGHT = 25050;
    public static final int MENU_OPEN_BROWSER_WEIGHT = 25040;
    public static final int MENU_OPEN_SYS_BROWSER_WEIGHT = 25030;
    // ---
    public static final int MENU_SITES_SHOW_WEIGHT = 23060;
    public static final int MENU_HISTORY_SHOW_WEIGHT = 23060;
    public static final int MENU_HISTORY_TAGS_WEIGHT = 23040;
    public static final int MENU_HISTORY_JUMP_WEIGHT = 23020;
    // ---
    public static final int MENU_SCRIPT_INVOKE_WEIGHT = 21060;
    public static final int MENU_SCRIPT_ZEST_ADD_WEIGHT = 21040;
    public static final int MENU_SCRIPT_ZEST_RECORD_WEIGHT = 21020;
    // ---
    // Section typically only used in work window
    public static final int MENU_UNDO_WEIGHT = 19060;
    public static final int MENU_REDO_WEIGHT = 19040;
    // ---
    // Section typically only used in work window
    public static final int MENU_EDIT_CUT_WEIGHT = 17080;
    public static final int MENU_EDIT_COPY_WEIGHT = 17060;
    public static final int MENU_EDIT_PASTE_WEIGHT = 17040;
    public static final int MENU_EDIT_DELETE_WEIGHT = 17020;
    // ---
    // Section typically only used in work window
    public static final int MENU_SECECT_ALL_WEIGHT = 15050;
    // ---
    public static final int MENU_COPY_URLS_WEIGHT = 13080;
    public static final int MENU_SAVE_URLS_WEIGHT = 13070;
    public static final int MENU_SAVE_ALL_URLS_WEIGHT = 13060;
    public static final int MENU_SAVE_RAW_WEIGHT = 13050;
    public static final int MENU_SAVE_XML_WEIGHT = 13040;
    public static final int MENU_SAVE_HAR_WEIGHT = 13030;
    // ---
    public static final int MENU_COMPARE_REQ_WEIGHT = 11060;
    public static final int MENU_COMPARE_RESP_WEIGHT = 11040;
    // ---
    // Anything under 1000 will appear in the lowest section
    public static final int MENU_SYNTAX_WEIGHT = 60;
    public static final int MENU_VIEW_WEIGHT = 40;
    public static final int MENU_SITE_REFRESH_WEIGHT = 20;

    // Contexts menu
    public static final int MENU_CONTEXT_ACTIVE_WEIGHT = 11680;
    public static final int MENU_CONTEXT_AJAX_WEIGHT = 11660;
    public static final int MENU_CONTEXT_SPIDER_WEIGHT = 11640;
    // ---
    public static final int MENU_CONTEXT_EXPORT_WEIGHT = 11460;
    public static final int MENU_CONTEXT_EXPORT_URLS_WEIGHT = 11440;
    // ---
    public static final int MENU_CONTEXT_SCOPE_WEIGHT = 11260;
    public static final int MENU_CONTEXT_DELETE_WEIGHT = 11240;

    // Attack menus
    public static final int MENU_ATTACK_ACTIVE_WEIGHT = 1190;
    public static final int MENU_ATTACK_AJAX_WEIGHT = 1180;
    public static final int MENU_ATTACK_CLIENT_WEIGHT = 1170;
    public static final int MENU_ATTACK_SPIDER_WEIGHT = 1160;
    public static final int MENU_ATTACK_FUZZ_WEIGHT = 1150;
    public static final int MENU_ATTACK_DIGGER_WEIGHT = 1140;

    // Exclude From menus
    public static final int MENU_EXCLUDE_PROXY_WEIGHT = 1180;
    public static final int MENU_EXCLUDE_SPIDER_WEIGHT = 1160;
    public static final int MENU_EXCLUDE_SCANNER_WEIGHT = 1140;
}
