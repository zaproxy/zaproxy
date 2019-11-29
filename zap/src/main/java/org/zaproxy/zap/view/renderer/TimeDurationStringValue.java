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

import java.text.MessageFormat;
import java.text.NumberFormat;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.parosproxy.paros.Constant;

public class TimeDurationStringValue implements StringValue {

    private static final long serialVersionUID = -2071582450216282057L;

    private static final MessageFormat TIME_DURATION_WITH_UNIT_FORMAT =
            new MessageFormat(
                    Constant.messages.getString("generic.value.time.duration.value.unit"));

    // Use the same NumberFormat instance since the renderers are used in EDT only.
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    static {
        NUMBER_FORMAT.setMaximumFractionDigits(2);
    }

    private static final String UNIT_MILLISECONDS =
            Constant.messages.getString("generic.value.time.duration.unit.milliseconds");
    private static final String UNIT_SECONDS =
            Constant.messages.getString("generic.value.time.duration.unit.seconds");
    private static final String UNIT_MINUTES =
            Constant.messages.getString("generic.value.time.duration.unit.minutes");
    private static final String UNIT_HOURS =
            Constant.messages.getString("generic.value.time.duration.unit.hours");

    private static final int ONE_SECOND_IN_MS = 1000;
    private static final int ONE_MINUTE_IN_MS = 60 * ONE_SECOND_IN_MS;
    private static final int ONE_HOUR_IN_MS = 60 * ONE_MINUTE_IN_MS;

    public TimeDurationStringValue() {}

    @Override
    public String getString(Object value) {
        if (value instanceof Number) {
            double duration = ((Number) value).doubleValue();
            String unit;
            if (duration < ONE_SECOND_IN_MS) {
                unit = UNIT_MILLISECONDS;
            } else if (duration < ONE_MINUTE_IN_MS) {
                duration = duration / ONE_SECOND_IN_MS;
                unit = UNIT_SECONDS;
            } else if (duration < ONE_HOUR_IN_MS) {
                duration = duration / ONE_MINUTE_IN_MS;
                unit = UNIT_MINUTES;
            } else {
                duration = duration / ONE_HOUR_IN_MS;
                unit = UNIT_HOURS;
            }
            return TIME_DURATION_WITH_UNIT_FORMAT.format(
                    new Object[] {NUMBER_FORMAT.format(duration), unit});
        }
        return StringValues.TO_STRING.getString(value);
    }

    public static boolean isTargetClass(Class<?> clazz) {
        return Number.class.isAssignableFrom(clazz);
    }
}
