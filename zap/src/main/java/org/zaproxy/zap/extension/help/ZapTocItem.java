/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.help;

import java.util.Locale;

import javax.help.HelpSet;
import javax.help.Map.ID;
import javax.help.TOCItem;

/**
 * A {@code TOCItem} that has the value of "tocid" attribute of a "tocitem" element.
 * 
 * @see TOCItem
 * @see ZapTocMerger
 * @see ZapTocView
 */
public class ZapTocItem extends TOCItem {

    private static final long serialVersionUID = -1267310421450082382L;

    private final String tocId;

    public ZapTocItem() {
        super(null, null, null);

        this.tocId = null;
    }

    public ZapTocItem(ID id, ID imageID, Locale locale, String tocId) {
        super(id, imageID, locale);

        this.tocId = tocId;
    }

    public ZapTocItem(ID id, ID imageID, HelpSet hs, Locale locale, String tocId) {
        super(id, imageID, hs, locale);

        this.tocId = tocId;
    }

    public String getTocId() {
        return tocId;
    }
}
