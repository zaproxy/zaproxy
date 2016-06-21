/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.network;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.parosproxy.paros.core.scanner.Plugin;

/**
 * Class with helper/utility methods to help testing classes involving {@code HttpBody} class and its implementations.
 *
 * @see Plugin
 */
public class HttpBodyTestUtils {

    protected static final int LIMIT_INITIAL_CAPACITY = 128000;

    protected static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    protected static final String DEFAULT_CHARSET_NAME = DEFAULT_CHARSET.name();

    protected static final String UTF_8_NAME = StandardCharsets.UTF_8.name();

    protected static final String BODY_1_STRING = "[Body1 A B C ぁ]";

    protected static final byte[] BODY_1_BYTES_DEFAULT_CHARSET = BODY_1_STRING.getBytes(DEFAULT_CHARSET);
    protected static final String BODY_1_STRING_DEFAULT_CHARSET = new String(BODY_1_BYTES_DEFAULT_CHARSET, DEFAULT_CHARSET);

    protected static final byte[] BODY_1_BYTES_UTF_8 = BODY_1_STRING.getBytes(StandardCharsets.UTF_8);
    protected static final String BODY_1_STRING_UTF_8 = new String(BODY_1_BYTES_UTF_8, StandardCharsets.UTF_8);

    protected static final String BODY_2_STRING = "[Body2 X Y Z ぁ]";

    protected static final byte[] BODY_2_BYTES_DEFAULT_CHARSET = BODY_2_STRING.getBytes(DEFAULT_CHARSET);
    protected static final String BODY_2_STRING_DEFAULT_CHARSET = new String(BODY_2_BYTES_DEFAULT_CHARSET, DEFAULT_CHARSET);

    protected static final byte[] BODY_2_BYTES_UTF_8 = BODY_2_STRING.getBytes(StandardCharsets.UTF_8);
    protected static final String BODY_2_STRING_UTF_8 = new String(BODY_2_BYTES_UTF_8, StandardCharsets.UTF_8);

    protected static final byte[] BODY_1_AND_2_BYTES_DEFAULT_CHARSET = concatenate(
            BODY_1_BYTES_DEFAULT_CHARSET,
            BODY_2_BYTES_DEFAULT_CHARSET);
    protected static final String BODY_1_AND_2_STRING_DEFAULT_CHARSET = BODY_1_STRING_DEFAULT_CHARSET
            + BODY_2_STRING_DEFAULT_CHARSET;
    protected static final byte[] BODY_1_AND_2_BYTES_UTF_8 = concatenate(BODY_1_BYTES_UTF_8, BODY_2_BYTES_UTF_8);
    protected static final String BODY_1_AND_2_STRING_UTF_8 = BODY_1_STRING_UTF_8 + BODY_2_STRING_UTF_8;

    @BeforeClass
    public static void suppressLogging() {
        Logger.getRootLogger().addAppender(new NullAppender());
    }

    protected static byte[] concatenate(byte[] array, byte[] array2) {
        int newlen = array.length + array2.length;
        byte[] newArray = new byte[newlen];
        System.arraycopy(array, 0, newArray, 0, array.length);
        System.arraycopy(array2, 0, newArray, array.length, array2.length);
        return newArray;
    }

    protected static Matcher<byte[]> allZeroBytes() {
        return new BaseMatcher<byte[]>() {

            @Override
            public boolean matches(Object actualValue) {
                byte[] bytes = (byte[]) actualValue;
                for (byte data : bytes) {
                    if (data != 0) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("all zero bytes");
            }

            public void describeMismatch(Object item, Description description) {
                description.appendText("has at least one non-zero byte ").appendValue(item);
            }
        };
    }
}
