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
package org.zaproxy.zap.view.renderer;

import java.text.DateFormat;
import java.util.Date;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;

public class DateFormatStringValue implements StringValue {

    private static final long serialVersionUID = 1143489366351658047L;

    private static final DateFormat DATE_TIME_FORMAT =
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

    public DateFormatStringValue() {}

    /** This method is not threadsafe and should therefore only be called from the EDT */
    @Override
    public String getString(Object value) {
        try {
            return DATE_TIME_FORMAT.format(value);
        } catch (IllegalArgumentException ignore) {
            // There's not much that can be done.
        }
        return StringValues.TO_STRING.getString(value);
    }

    public static boolean isTargetClass(Class<?> clazz) {
        if (clazz == Date.class || Number.class.isAssignableFrom(clazz)) {
            return true;
        }
        return false;
    }
}
