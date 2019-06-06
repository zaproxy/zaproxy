/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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

import java.awt.event.KeyEvent;

public class FilteredZapTextField extends ZapTextField {

    private static final long serialVersionUID = 1L;

    private String validChars;

    public FilteredZapTextField(String validChars) {
        super();
        this.validChars = validChars;
    }

    @Override
    public void processKeyEvent(KeyEvent ev) {

        char c = ev.getKeyChar();

        //		if((Character.isLetter(c) && ! ev.isAltDown())
        //				|| validChars.indexOf(c) < 0) {
        //		if (ev.getKeyCode() == KeyEvent.VK_DELETE) {
        if (Character.isISOControl(c)) {
            // Fall through
        } else if (validChars.indexOf(c) < 0) {
            ev.consume();
            return;
        }
        super.processKeyEvent(ev);
    }
}
