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
package org.zaproxy.zap.utils;

import java.awt.Desktop;
import java.net.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DesktopUtils {

    private static enum BrowserInvoker {
        desktop,
        none
    }

    private static BrowserInvoker invoker =
            Desktop.isDesktopSupported() ? BrowserInvoker.desktop : BrowserInvoker.none;

    private static Logger log = LogManager.getLogger(DesktopUtils.class);

    public static boolean openUrlInBrowser(URI uri) {

        try {
            if (invoker == BrowserInvoker.desktop) {
                Desktop.getDesktop().browse(uri);
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            invoker = BrowserInvoker.none;
        }
        return false;
    }

    public static boolean openUrlInBrowser(String uri) {
        try {
            return openUrlInBrowser(new URI(uri));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            invoker = BrowserInvoker.none;
        }
        return false;
    }

    public static boolean openUrlInBrowser(org.apache.commons.httpclient.URI uri) {
        try {
            return openUrlInBrowser(uri.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            invoker = BrowserInvoker.none;
        }
        return false;
    }

    public static boolean canOpenUrlInBrowser() {
        return invoker == BrowserInvoker.desktop;
    }
}
