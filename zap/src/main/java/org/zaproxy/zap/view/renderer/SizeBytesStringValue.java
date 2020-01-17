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

/**
 * A {@link StringValue} for byte values.
 *
 * @since 2.4.0
 */
public class SizeBytesStringValue implements StringValue {

    private static final long serialVersionUID = 8021369832769317695L;

    private static final MessageFormat TIME_DURATION_WITH_UNIT_FORMAT =
            new MessageFormat(Constant.messages.getString("generic.value.size.bytes.value.unit"));

    // Use the same NumberFormat instance since the renderers are used in EDT only.
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    static {
        NUMBER_FORMAT.setMaximumFractionDigits(2);
    }

    private static final String UNIT_BYTES =
            Constant.messages.getString("generic.value.size.bytes.unit.bytes");
    private static final String UNIT_KBYTES =
            Constant.messages.getString("generic.value.size.bytes.unit.kibytes");
    private static final String UNIT_MBYTES =
            Constant.messages.getString("generic.value.size.bytes.unit.mibytes");
    private static final String UNIT_GBYTES =
            Constant.messages.getString("generic.value.size.bytes.unit.gibytes");

    private static final int ONE_KB_IN_BYTES = 1024;
    private static final int ONE_MB_IN_BYTES = 1024 * ONE_KB_IN_BYTES;
    private static final int ONE_GB_IN_BYTES = 1024 * ONE_MB_IN_BYTES;

    private boolean useJustBytesUnit = true;

    public SizeBytesStringValue() {}

    public SizeBytesStringValue(boolean shouldUseJustBytesUnit) {
        setUseJustBytesUnit(shouldUseJustBytesUnit);
    }

    /**
     * Tells whether or not the conversion to {@code String} should use just bytes, that is, it
     * should not use bigger units (e.g. KiB, MiB).
     *
     * <p>Default is {@code true}.
     *
     * @return {@code true} if it should use just bytes, {@code false} otherwise.
     * @since 2.6.0
     */
    public boolean isUseJustBytesUnit() {
        return useJustBytesUnit;
    }

    /**
     * Sets whether or not the conversion to {@code String} should use just bytes, that is, it
     * should not do use bigger units (e.g. KiB, MiB).
     *
     * @param useJustBytesUnit {@code true} if it should use just bytes, {@code false} otherwise.
     * @since 2.6.0
     */
    public void setUseJustBytesUnit(boolean useJustBytesUnit) {
        this.useJustBytesUnit = useJustBytesUnit;
    }

    @Override
    public String getString(Object value) {
        if (value instanceof Number) {
            double size = ((Number) value).doubleValue();
            String unit = UNIT_BYTES;
            if (!isUseJustBytesUnit() && size >= ONE_KB_IN_BYTES) {
                if (size < ONE_MB_IN_BYTES) {
                    size = size / ONE_KB_IN_BYTES;
                    unit = UNIT_KBYTES;
                } else if (size < ONE_GB_IN_BYTES) {
                    size = size / ONE_MB_IN_BYTES;
                    unit = UNIT_MBYTES;
                } else {
                    size = size / ONE_GB_IN_BYTES;
                    unit = UNIT_GBYTES;
                }
            }
            return TIME_DURATION_WITH_UNIT_FORMAT.format(
                    new Object[] {NUMBER_FORMAT.format(size), unit});
        }
        return StringValues.TO_STRING.getString(value);
    }

    public static boolean isTargetClass(Class<?> clazz) {
        return Number.class.isAssignableFrom(clazz);
    }
}
