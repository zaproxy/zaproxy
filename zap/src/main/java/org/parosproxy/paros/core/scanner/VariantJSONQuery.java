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
package org.parosproxy.paros.core.scanner;

import org.apache.commons.lang.StringEscapeUtils;

/** @author andy */
public class VariantJSONQuery extends VariantAbstractRPCQuery {

    public static final String JSON_RPC_CONTENT_TYPE = "application/json";

    public static final int NAME_SEPARATOR = ':';
    public static final int VALUE_SEPARATOR = ',';
    public static final int BEGIN_ARRAY = '[';
    public static final int QUOTATION_MARK = '"';
    public static final int BEGIN_OBJECT = '{';
    public static final int END_OBJECT = '}';
    public static final int END_ARRAY = ']';
    public static final int BACKSLASH = '\\';

    private SimpleStringReader sr;
    private boolean scanNullValues;

    private static final String SHORT_NAME = "json";

    @Override
    public String getShortName() {
        return SHORT_NAME;
    }

    public VariantJSONQuery() {
        super(NameValuePair.TYPE_JSON);
    }

    /**
     * Sets whether or not to scan null values.
     *
     * <p>The null values are handled as if they were strings, that is, the payload injected is a
     * string.
     *
     * @param scan {@code true} if null values should be scanned, {@code false} otherwise.
     * @since 2.11.0
     * @see #isScanNullValues()
     */
    public void setScanNullValues(boolean scan) {
        scanNullValues = scan;
    }

    /**
     * Tells whether or not to scan null values.
     *
     * @return {@code true} if null values should be scanned, {@code false} otherwise.
     * @see #setScanNullValues(boolean)
     */
    public boolean isScanNullValues() {
        return scanNullValues;
    }

    @Override
    public boolean isValidContentType(String contentType) {
        return contentType.startsWith(JSON_RPC_CONTENT_TYPE);
    }

    @Override
    public void parseContent(String content) {
        if (content.isEmpty()) {
            return;
        }
        sr = new SimpleStringReader(content);
        parseObject();
    }

    @Override
    public String getEscapedValue(String value, boolean toQuote) {
        String result = StringEscapeUtils.escapeJava(value);
        return (toQuote) ? (char) QUOTATION_MARK + result + (char) QUOTATION_MARK : result;
    }

    @Override
    public String getUnescapedValue(String value) {
        return StringEscapeUtils.unescapeJava(value);
    }

    // --------------------------------------------------------------------

    private static final int STATE_READ_START_OBJECT = 0;
    private static final int STATE_READ_FIELD = 1;
    private static final int STATE_READ_VALUE = 2;
    private static final int STATE_READ_POST_VALUE = 3;

    private void parseObject() {
        int state = STATE_READ_START_OBJECT;
        boolean objectRead = false;
        boolean done = false;
        String field = null;
        int beginToken;
        int endToken;
        int chr;

        while (!done) {
            switch (state) {
                case STATE_READ_START_OBJECT:
                    chr = sr.skipWhitespaceRead();
                    if (chr == BEGIN_OBJECT) {
                        objectRead = true;

                        chr = sr.skipWhitespaceRead();
                        if (chr == END_OBJECT) { // empty object
                            return;
                        }

                        sr.unreadLastCharacter();
                        state = STATE_READ_FIELD;

                    } else if (chr == BEGIN_ARRAY) {
                        sr.unreadLastCharacter();
                        state = STATE_READ_VALUE;

                    } else {
                        throw new IllegalArgumentException(
                                "Input is invalid JSON; does not start with '{' or '[', c=" + chr);
                    }

                    break;

                case STATE_READ_FIELD:
                    chr = sr.skipWhitespaceRead();
                    if (chr == QUOTATION_MARK) {

                        beginToken = sr.getPosition();
                        readEscapedString();

                        endToken = sr.getPosition() - 1;
                        // Now we have the string object name
                        // we can do something here for value filtering...
                        field = getToken(beginToken, endToken);

                        chr = sr.skipWhitespaceRead();
                        if (chr != NAME_SEPARATOR) {
                            throw new IllegalArgumentException(
                                    "Expected ':' between string field and value at position "
                                            + sr.getPosition());
                        }

                        sr.skipWhitespaceRead();
                        sr.unreadLastCharacter();
                        state = STATE_READ_VALUE;

                    } else {
                        throw new IllegalArgumentException(
                                "Expected quote at position " + sr.getPosition());
                    }

                    break;

                case STATE_READ_VALUE:
                    if (field == null) {
                        // field is null when you have an untyped Object[], so we place
                        // the JsonArray on the @items field.
                        field = "@items";
                    }

                    parseValue(field);
                    state = STATE_READ_POST_VALUE;
                    break;

                case STATE_READ_POST_VALUE:
                    chr = sr.skipWhitespaceRead();
                    if (chr == -1 && objectRead) {
                        throw new IllegalArgumentException("EOF reached before closing '}'");
                    }

                    if (chr == END_OBJECT || chr == -1) {
                        done = true;

                    } else if (chr == VALUE_SEPARATOR) {
                        state = STATE_READ_FIELD;

                    } else {
                        throw new IllegalArgumentException(
                                "Object not ended with '}' or ']' at position " + sr.getPosition());
                    }
                    break;
            }
        }
    }

    private void parseValue(String fieldName) {
        int chr = sr.read();

        // Check if the value is a string
        if (chr == QUOTATION_MARK) {
            int beginToken = sr.getPosition();
            readEscapedString();

            // Now we have the string object value
            // Put everything inside the parameter array
            addParameter(fieldName, beginToken, sr.getPosition() - 1, false, true);

            // check if the value is a number
        } else if (Character.isDigit(chr) || chr == '-') {
            sr.unreadLastCharacter();

            int beginToken = sr.getPosition();
            do {
                chr = sr.read();
                if (chr == -1) {
                    throw new IllegalArgumentException("Reached EOF while reading number");
                }

            } while (Character.isDigit(chr)
                    || (chr == '.')
                    || (chr == 'e')
                    || (chr == 'E')
                    || (chr == '+')
                    || (chr == '-'));

            sr.unreadLastCharacter();
            // Now we have the int object value
            // Put everything inside the parameter array
            addParameter(fieldName, beginToken, sr.getPosition(), true, false);

        } else if (chr == BEGIN_OBJECT) {
            sr.unreadLastCharacter();
            parseObject();

        } else if (chr == BEGIN_ARRAY) {
            parseArray(fieldName);

        } else if (chr == END_ARRAY) { // [] empty array
            sr.unreadLastCharacter();

        } else if (chr == 't' || chr == 'T') {
            sr.unreadLastCharacter();
            parseToken("true");

        } else if (chr == 'f' || chr == 'F') {
            sr.unreadLastCharacter();
            parseToken("false");

        } else if (chr == 'n' || chr == 'N') {
            sr.unreadLastCharacter();
            if (scanNullValues) {
                int start = sr.getPosition();
                addParameter(fieldName, start, start + 4, true, null);
            }
            parseToken("null");

        } else if (chr == -1) {
            throw new IllegalArgumentException("EOF reached prematurely");

        } else {
            throw new IllegalArgumentException(
                    "Unknown value type '"
                            + chr
                            + "' for field '"
                            + fieldName
                            + "' at position "
                            + sr.getPosition());
        }
    }

    /** Read a JSON array */
    private void parseArray(String fieldName) {
        int chr;
        int idx = 0;
        while (true) {
            sr.skipWhitespaceRead();
            sr.unreadLastCharacter();
            parseValue(fieldName + "[" + (idx++) + "]");

            chr = sr.skipWhitespaceRead();
            if (chr == END_ARRAY) {
                break;
            }

            if (chr != VALUE_SEPARATOR) {
                throw new IllegalArgumentException(
                        "Expected ',' or ']' inside array at position " + sr.getPosition());
            }
        }
    }

    /**
     * Return the specified token from the reader. If it is not found, throw an IOException
     * indicating that. Converting to chr to (char) chr is acceptable because the 'tokens' allowed
     * in a JSON input stream (true, false, null) are all ASCII.
     */
    private void parseToken(String token) {
        int len = token.length();

        for (int i = 0; i < len; i++) {
            int chr = sr.read();
            if (chr == -1) {
                throw new IllegalArgumentException("EOF reached while reading token: " + token);
            }
            chr = Character.toLowerCase((char) chr);
            int loTokenChar = token.charAt(i);

            if (loTokenChar != chr) {
                throw new IllegalArgumentException(
                        "Expected token: " + token + " at position " + sr.getPosition());
            }
        }
    }

    private void readEscapedString() {
        int chr;
        while (true) {
            chr = sr.read();
            if (chr == BACKSLASH) {
                chr = sr.read();
            } else if (chr == QUOTATION_MARK) {
                break;
            }
            if (chr == -1) {
                throw new IllegalArgumentException("EOF reached while reading JSON field name");
            }
        }
    }

    protected class SimpleStringReader {
        private static final String WS = " \t\r\n";
        private String str;
        private int length;
        private int next = 0;

        /**
         * Creates a new string reader.
         *
         * @param s String providing the character stream.
         */
        public SimpleStringReader(String s) {
            this.str = s;
            this.length = s.length();
        }

        /**
         * Read until non-whitespace character and then return it. This saves extra read/pushback.
         *
         * @return int repesenting the next non-whitespace character in the stream.
         */
        public int skipWhitespaceRead() {
            int c = read();
            while (WS.indexOf(c) != -1) {
                c = read();
            }
            return c;
        }

        /**
         * Reads a single character.
         *
         * @return The character read, or -1 if the end of the stream has been reached
         */
        public int read() {
            if (next >= length) {
                return -1;
            }
            return str.charAt(next++);
        }

        public void unreadLastCharacter() {
            next--;
        }

        public int getPosition() {
            return next;
        }
    }
}
