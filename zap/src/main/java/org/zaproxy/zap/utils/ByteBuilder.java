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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ByteBuilder {

    private static final Logger logger = LogManager.getLogger(ByteBuilder.class);

    private byte[] array;
    private int size;

    public ByteBuilder() {
        this(10);
    }

    public ByteBuilder(int capacity) {
        array = new byte[capacity];
        size = 0;
    }

    public ByteBuilder(byte[] array) {
        this.array = Arrays.copyOf(array, array.length * 2);
        size = array.length;
    }

    public void ensureCapacity(int capacity) {
        if (capacity > this.array.length) {
            this.array = Arrays.copyOf(array, capacity);
        }
    }

    private void testAddition(int addition) {
        while (size + addition >= this.array.length) {
            this.array = Arrays.copyOf(array, this.array.length * 2);
        }
    }

    private void copyIntoArray(byte[] b) {
        for (int i = 0; i < b.length; i++) {
            array[size + i] = b[i];
        }
        size += b.length;
    }

    public int capacity() {
        return array.length;
    }

    public int size() {
        return size;
    }

    public void truncate(int size) {
        array = Arrays.copyOf(array, size);
    }

    public byte[] subSequence(int start, int end) {
        return Arrays.copyOfRange(array, start, end);
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(array, size);
    }

    @Override
    public String toString() {
        return new String(array, 0, size);
    }

    // all append() methods return this so we can chain calls together, of course :)

    public ByteBuilder append(byte value) {
        testAddition(1);
        this.array[size++] = value;
        return this;
    }

    public ByteBuilder append(byte[] value) {
        return this.append(value, 0, value.length);
    }

    public ByteBuilder append(byte[] value, final int offset, final int len) {
        if (offset + len > value.length) throw new ArrayIndexOutOfBoundsException();

        testAddition(len);
        for (int i = 0; i < len; i++) {
            this.array[size + (i)] = value[i + offset];
        }
        size += len;
        return this;
    }

    public ByteBuilder append(char value) {
        testAddition(1);
        this.array[size++] = (byte) value;
        return this;
    }

    public ByteBuilder append(char[] value) {
        return this.append(value, 0, value.length);
    }

    public ByteBuilder append(char[] value, final int offset, final int len) {
        if (offset + len > value.length) throw new ArrayIndexOutOfBoundsException();

        testAddition(len);
        for (int i = 0; i < len; i++) {
            this.array[size + (i)] = (byte) value[i + offset];
        }
        size += len;
        return this;
    }

    public ByteBuilder append(boolean value) {
        testAddition(1);
        this.array[size++] = (byte) ((value) ? 1 : 0);
        return this;
    }

    public ByteBuilder append(short value) {
        final int LEN = 2; // int are 4 bytes long
        testAddition(LEN);
        {
            byte[] b = new byte[LEN];
            for (int i = 0; i < LEN; i++) {
                int offset = (b.length - 1 - i) * 8;
                b[i] = (byte) ((value >>> offset) & 0xFF);
            }

            copyIntoArray(b);
        }
        return this;
    }

    public ByteBuilder append(int value) {
        final int LEN = 4; // int are 4 bytes long
        testAddition(LEN);
        {
            byte[] b = new byte[LEN];
            for (int i = 0; i < LEN; i++) {
                int offset = (b.length - 1 - i) * 8;
                b[i] = (byte) ((value >>> offset) & 0xFF);
            }

            copyIntoArray(b);
        }
        return this;
    }

    public ByteBuilder append(long value) {
        final int LEN = 8; // long are 8 bytes long
        testAddition(LEN);
        {
            byte[] b = new byte[LEN];
            for (int i = 0; i < LEN; i++) {
                int offset = (b.length - 1 - i) * 8;
                b[i] = (byte) ((value >>> offset) & 0xFF);
            }

            copyIntoArray(b);
        }
        return this;
    }

    public ByteBuilder append(float value) {
        final int LEN = 4; // float are 4 bytes long
        testAddition(LEN);
        int intval = Float.floatToRawIntBits(value);
        {
            byte[] b = new byte[LEN];
            for (int i = 0; i < LEN; i++) {
                int offset = (b.length - 1 - i) * 8;
                b[i] = (byte) ((intval >>> offset) & 0xFF);
            }

            copyIntoArray(b);
        }
        return this;
    }

    public ByteBuilder append(double value) {
        final int LEN = 8; // double are 8 bytes long
        testAddition(LEN);
        long intval = Double.doubleToRawLongBits(value);
        {
            byte[] b = new byte[LEN];
            for (int i = 0; i < LEN; i++) {
                int offset = (b.length - 1 - i) * 8;
                b[i] = (byte) ((intval >>> offset) & 0xFF);
            }

            copyIntoArray(b);
        }
        return this;
    }

    public ByteBuilder append(Object value) {
        try {
            // attempts to call a toByteStructure() method on the object, if it has it.
            Method bytem = value.getClass().getMethod("toByteStructure", (Class<?>[]) null);
            byte[] array = (byte[]) bytem.invoke(value);
            return this.append(array);
        } catch (SecurityException
                | IllegalArgumentException
                | IllegalAccessException
                | InvocationTargetException e) {
            // shouldn't happen but in case it does log it.
            logger.debug(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            // will happen, a lot
        }

        // if the above did not work, instead append the result of the toString() method.
        return this.append(value.toString());
    }

    public ByteBuilder append(String value) {
        return append(value, Charset.defaultCharset());
    }

    public ByteBuilder append(String value, Charset charset) {
        byte[] b = value.getBytes(charset);
        testAddition(b.length + 4);
        return this.append(b.length).append(b);
    }

    public ByteBuilder append(StringBuffer value) {
        return append(value, Charset.defaultCharset());
    }

    public ByteBuilder append(StringBuffer value, Charset charset) {
        byte[] b = value.toString().getBytes(charset);
        testAddition(b.length + 4);
        return this.append(b.length).append(b);
    }

    public ByteBuilder append(ByteBuilder value) {
        testAddition(value.size);
        return this.append(value.array, 0, value.size);
    }

    ///////// and now, appendSpecial() //////////

    public ByteBuilder appendSpecial(long value, int length, boolean preserveNegative) {
        testAddition(length);
        {
            byte[] b = new byte[length];
            for (int i = 0; i < length; i++) {
                int offset = (b.length - 1 - i) * 8;
                b[i] = (byte) ((value >>> offset) & 0xFF);
                if (preserveNegative && i == 0) {
                    if (value < 0) { // original value is negative
                        b[i] |= 0x80; // 1000 0000 - or'ed into the value to make it negative
                    } else { // original value is positive
                        b[i] &= 0x7F; // 0111 0000 - and'ed into the value to clear the negative bit
                    }
                }
            }

            copyIntoArray(b);
        }

        return this;
    }
}
