/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The Zed Attack Proxy Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.utils;

import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class ZapPortNumberSpinner extends JSpinner {

	private static final long serialVersionUID = -3475273563365395482L;
	
	private static final int MIN_PORT = 1;
	private static final int MAX_PORT = 65535;

	private int defaultPortNumber;

	public ZapPortNumberSpinner(int defaultPortNumber) {
		if (defaultPortNumber < MIN_PORT || defaultPortNumber > MAX_PORT) {
			this.defaultPortNumber = MIN_PORT;
		} else {
			this.defaultPortNumber = defaultPortNumber;
		}
		setModel(new SpinnerNumberModel(this.defaultPortNumber, MIN_PORT, MAX_PORT, 1));
		((NumberEditor)getEditor()).getTextField().setFormatterFactory(new DefaultFormatterFactory(new PortNumberFormatter()));
	}

	public void changeToDefaultValue() {
		super.setValue(Integer.valueOf(defaultPortNumber));
	}
	
	@Override
	public void setValue(Object value) {
		if ((value == null) || !(value instanceof Number)) {
			return;
		}
		final int intValue = ((Number)value).intValue();
		if (intValue < MIN_PORT || intValue > MAX_PORT) {
			return;
		}
		
		super.setValue(Integer.valueOf(intValue));
	}

	@Override
	public Integer getValue() {
		return (Integer)super.getValue();
	}

	public void setEditable(boolean enabled) {
		super.setEnabled(enabled);
		((NumberEditor)getEditor()).getTextField().setEnabled(true);
		((NumberEditor)getEditor()).getTextField().setEditable(enabled);
	}
	
	private static class PortNumberFormatter extends NumberFormatter {

		private static final long serialVersionUID = 4888079030453662194L;

		public PortNumberFormatter() {
			setValueClass(Integer.class);

			setMinimum(Integer.valueOf(MIN_PORT));
			setMaximum(Integer.valueOf(MAX_PORT));
			setAllowsInvalid(false);

			setFormat(new PortNumberFormat());
		}

		@Override
		public Object stringToValue(String text) throws ParseException {
			Object o = null;

			try {
				o = super.stringToValue(text);
			} catch (ParseException e) {
				boolean throwException = true;
				if (e.getMessage().equals("Value not within min/max range")) {
					final int value = ((Number)getFormat().parseObject(text)).intValue();
					if (value < MIN_PORT) {
						o = Integer.valueOf(MIN_PORT);
						throwException = false;
					} else if (value > MAX_PORT) {
						o = Integer.valueOf(MAX_PORT);
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

	private static class PortNumberFormat extends Format {

		private static final long serialVersionUID = 7864449797301371031L;
		
		private final NumberFormat numberFormat;

		public PortNumberFormat() {
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
				return Integer.valueOf(MIN_PORT);
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
