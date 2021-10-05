/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class ZapNumberSpinner extends JSpinner {

    private static final long serialVersionUID = -3475273563365395482L;

    private int minValue;
    private int maxValue;
    private int defaultValue;

    public ZapNumberSpinner() {
        this(0, 0, Integer.MAX_VALUE);
    }

    public ZapNumberSpinner(int minValue, int defaultValue, int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        if (!isValidValue(defaultValue)) {
            this.defaultValue = minValue;
        } else {
            this.defaultValue = defaultValue;
        }
        setModel(new SpinnerNumberModel(this.defaultValue, minValue, maxValue, 1));
        JFormattedTextField textField = ((NumberEditor) getEditor()).getTextField();
        textField.setFormatterFactory(
                new DefaultFormatterFactory(new ZapNumberFormatter(minValue, maxValue)));
        textField.addFocusListener(
                new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        SwingUtilities.invokeLater(textField::selectAll);
                    }
                });
    }

    private boolean isValidValue(int value) {
        if (value < minValue || value > maxValue) {
            return false;
        }
        return true;
    }

    public void changeToDefaultValue() {
        super.setValue(defaultValue);
    }

    @Override
    public void setValue(Object value) {
        if (!(value instanceof Number)) {
            return;
        }
        setValue(((Number) value).intValue());
    }

    public void setValue(int value) {
        if (!isValidValue(value)) {
            return;
        }

        super.setValue(value);
    }

    @Override
    public Integer getValue() {
        return (Integer) super.getValue();
    }

    public void setEditable(boolean enabled) {
        super.setEnabled(enabled);
        ((NumberEditor) getEditor()).getTextField().setEnabled(true);
        ((NumberEditor) getEditor()).getTextField().setEditable(enabled);
    }

    private static class ZapNumberFormatter extends NumberFormatter {

        private static final long serialVersionUID = 4888079030453662194L;
        private int minValue;
        private int maxValue;

        public ZapNumberFormatter(int minValue, int maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            setValueClass(Integer.class);

            setMinimum(minValue);
            setMaximum(maxValue);
            setAllowsInvalid(false);

            setFormat(new ZapNumberFormat(minValue));
        }

        @Override
        public Object stringToValue(String text) throws ParseException {
            Object o = null;

            try {
                o = super.stringToValue(text);
            } catch (ParseException e) {
                boolean throwException = true;
                if (e.getMessage().equals("Value not within min/max range")) {
                    final int value = ((Number) getFormat().parseObject(text)).intValue();
                    if (value < minValue) {
                        o = minValue;
                        throwException = false;
                    } else if (value > maxValue) {
                        o = maxValue;
                        throwException = false;
                    }
                }

                if (throwException) {
                    throw e;
                }
            }

            return o;
        }
    }

    private static class ZapNumberFormat extends Format {

        private static final long serialVersionUID = 7864449797301371031L;

        private final NumberFormat numberFormat;
        private int minValue;

        public ZapNumberFormat(int minValue) {
            this.minValue = minValue;
            this.numberFormat = NumberFormat.getIntegerInstance();
            this.numberFormat.setGroupingUsed(false);
        }

        @Override
        public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
            return numberFormat.formatToCharacterIterator(obj);
        }

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return numberFormat.format(obj, toAppendTo, pos);
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            if (source.isEmpty()) {
                pos.setIndex(1);
                return minValue;
            }

            Object val = numberFormat.parseObject(source, pos);

            if (pos.getIndex() != source.length()) {
                pos.setErrorIndex(pos.getIndex());
                pos.setIndex(0);
            }

            return val;
        }
    }
}
