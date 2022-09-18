/*
 * Created on Jun 14, 2004
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2012/03/15 Changed to use byte[] instead of StringBuffer.
// ZAP: 2014/11/26 Issue: 1415 Fixed file uploads > 128k
// ZAP: 2016/05/18 Always use charset set when changing the HTTP body
// ZAP: 2016/10/18 Attempt to determine the charset when setting a String with unknown charset
// ZAP: 2017/02/01 Allow to set whether or not the charset should be determined.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2020/12/09 Add content encoding.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.network;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zaproxy.zap.network.HttpEncoding;

/**
 * Abstract a HTTP body in request or response messages.
 *
 * @since 1.0.0
 */
public abstract class HttpBody {

    private static final Logger log = LogManager.getLogger(HttpBody.class);

    /**
     * The name of the default charset ({@code ISO-8859-1}) used for {@code String} related
     * operations, for example, {@link #HttpBody(String)}, {@link #append(String)}, {@link
     * #setBody(String)}, or {@link #toString()}.
     *
     * @see #setCharset(String)
     */
    public static final String DEFAULT_CHARSET = StandardCharsets.ISO_8859_1.name();

    /**
     * The limit for the initial capacity, prevents allocating a bigger array when the
     * Content-Length is wrong.
     */
    public static final int LIMIT_INITIAL_CAPACITY = 128000;

    private byte[] body;
    private int pos;
    private byte[] bodyDecoded;
    private String cachedString;
    private Charset charset;
    private boolean determineCharset = true;
    private boolean contentEncodingErrors;

    private List<HttpEncoding> encodings = Collections.emptyList();

    /** Constructs a {@code HttpBody} with no contents (that is, zero length). */
    public HttpBody() {
        this(0);
    }

    /**
     * Constructs a {@code HttpBody} with the given initial capacity.
     *
     * <p>The initial capacity is limited to {@value #LIMIT_INITIAL_CAPACITY} to prevent allocating
     * big arrays.
     *
     * @param capacity the initial capacity
     */
    public HttpBody(int capacity) {
        body = new byte[Math.max(Math.min(capacity, LIMIT_INITIAL_CAPACITY), 0)];
    }

    /**
     * Constructs a {@code HttpBody} with the given {@code contents}.
     *
     * <p>If the given {@code contents} are {@code null} the {@code HttpBody} will have no content.
     *
     * @param contents the contents of the body, might be {@code null}
     * @since 2.5.0
     */
    public HttpBody(byte[] contents) {
        if (contents != null) {
            setBody(contents);
        } else {
            body = new byte[0];
        }
    }

    /**
     * Constructs a {@code HttpBody} with the given {@code contents}, using default charset for
     * {@code String} related operations.
     *
     * <p>If the given {@code contents} are {@code null} the {@code HttpBody} will have no content.
     *
     * <p><strong>Note:</strong> If the contents are not representable with the default charset it
     * might lead to data loss.
     *
     * @param contents the contents of the body, might be {@code null}
     * @see #DEFAULT_CHARSET
     * @see #HttpBody(byte[])
     */
    public HttpBody(String contents) {
        if (contents != null) {
            setBody(contents);
        } else {
            body = new byte[0];
        }
    }

    /**
     * Sets the given {@code contents} as the body.
     *
     * <p>If the {@code contents} are {@code null} the call to this method has no effect.
     *
     * @param contents the new contents of the body, might be {@code null}
     */
    public void setBody(byte[] contents) {
        if (contents == null) {
            return;
        }
        resetCachedValues();

        body = new byte[contents.length];
        System.arraycopy(contents, 0, body, 0, contents.length);

        pos = body.length;
    }

    /**
     * Sets the given {@code contents} as the body, using the current charset.
     *
     * <p>The given contents are encoded with the content encodings set, if any.
     *
     * <p>If the {@code contents} are {@code null} the call to this method has no effect.
     *
     * <p><strong>Note:</strong> Setting the contents with incorrect charset might lead to data
     * loss.
     *
     * @param contents the new contents of the body, might be {@code null}
     * @see #setCharset(String)
     * @see #getContentEncodings()
     */
    public void setBody(String contents) {
        if (contents == null) {
            return;
        }

        cachedString = null;
        if (charset == null && isDetermineCharset()) {
            // Attempt to determine the charset to avoid data loss.
            charset = determineCharset(contents);
        }

        bodyDecoded = contents.getBytes(getCharsetImpl());
        body = encode(bodyDecoded);

        pos = body.length;
    }

    protected byte[] encode(byte[] data) {
        if (encodings.isEmpty()) {
            return data;
        }

        byte[] value = data;
        try {
            byte[] decoded = value;
            for (HttpEncoding encoding : encodings) {
                decoded = encoding.encode(decoded);
            }
            contentEncodingErrors = false;
            return decoded;
        } catch (IOException e) {
            log.warn("An error occurred while encoding the body: {}", e.getMessage());
        }
        contentEncodingErrors = true;
        return value;
    }

    /**
     * Determines the {@code Charset} of the given {@code contents}, that are being set to the body.
     *
     * <p>An attempt to prevent data loss when {@link #setBody(String) new contents} are set without
     * a {@code Charset}.
     *
     * <p>By default returns {@code null}.
     *
     * @param contents the contents being set to the body
     * @return the {@code Charset}, or {@code null} if not known.
     * @since 2.6.0
     */
    protected Charset determineCharset(String contents) {
        return null;
    }

    /**
     * Sets whether or not the {@code Charset} of the response should be determined, when no {@code
     * Charset} is set.
     *
     * <p>An attempt to prevent data loss when {@link #setBody(String) new contents} are set without
     * a {@code Charset}.
     *
     * @param determine {@code true} if the {@code Charset} should be determined, {@code false}
     *     otherwise.
     * @since 2.6.0
     * @see #isDetermineCharset()
     * @see #setCharset(String)
     */
    public void setDetermineCharset(boolean determine) {
        determineCharset = determine;
    }

    /**
     * Tells whether or not the {@code Charset} of the response should be determined, when no {@code
     * Charset} is set.
     *
     * <p>An attempt to prevent data loss when {@link #setBody(String) new contents} are set without
     * a {@code Charset}.
     *
     * <p>By default returns {@code true}.
     *
     * @return {@code true} if the {@code Charset} should be determined, {@code false} otherwise.
     * @since 2.6.0
     * @see #setDetermineCharset(boolean)
     * @see #setCharset(String)
     */
    public boolean isDetermineCharset() {
        return determineCharset;
    }

    /**
     * Gets the {@code Charset} that should be used internally by the class for {@code String}
     * related operations.
     *
     * <p>If no {@code Charset} was set (that is, is {@code null}) it falls back to {@code
     * ISO-8859-1}, otherwise it returns the {@code Charset} set.
     *
     * @return the {@code Charset} to be used for {@code String} related operations, never {@code
     *     null}
     * @see #DEFAULT_CHARSET
     * @see #setCharset(String)
     */
    private Charset getCharsetImpl() {
        if (charset != null) {
            return charset;
        }
        return StandardCharsets.ISO_8859_1;
    }

    /**
     * Appends the given {@code contents} to the body, up to a certain length.
     *
     * <p>If the {@code contents} are {@code null} or the {@code length} negative or zero, the call
     * to this method has no effect.
     *
     * @param contents the contents to append, might be {@code null}
     * @param length the length of contents to append
     */
    public void append(byte[] contents, int length) {
        if (contents == null || length <= 0) {
            return;
        }

        int len = Math.min(contents.length, length);
        if (pos + len > body.length) {
            byte[] newBody = new byte[pos + len];
            System.arraycopy(body, 0, newBody, 0, pos);
            body = newBody;
        }
        System.arraycopy(contents, 0, body, pos, len);
        pos += len;

        resetCachedValues();
    }

    private void resetCachedValues() {
        cachedString = null;
        bodyDecoded = null;
        contentEncodingErrors = false;
    }

    /**
     * Appends the given {@code contents} to the body.
     *
     * <p>If the {@code contents} are {@code null} the call to this method has no effect.
     *
     * @param contents the contents to append, might be {@code null}
     */
    public void append(byte[] contents) {
        if (contents == null) {
            return;
        }
        append(contents, contents.length);
    }

    /**
     * Appends the given {@code contents} to the body, using the current charset.
     *
     * <p>The given contents are encoded with the content encodings set, if any.
     *
     * <p>If the {@code contents} are {@code null} the call to this method has no effect.
     *
     * <p><strong>Note:</strong> Setting the contents with incorrect charset might lead to data
     * loss.
     *
     * @param contents the contents to append, might be {@code null}
     * @see #setCharset(String)
     * @see #getContentEncodings()
     */
    public void append(String contents) {
        if (contents == null) {
            return;
        }

        byte[] decoded = decode();
        byte[] contentsBytes = contents.getBytes(getCharsetImpl());
        byte[] data = new byte[decoded.length + contentsBytes.length];
        System.arraycopy(decoded, 0, data, 0, decoded.length);
        System.arraycopy(contentsBytes, 0, data, decoded.length, contentsBytes.length);
        bodyDecoded = data;
        setBody(encode(bodyDecoded));
    }

    /**
     * Gets the {@code String} representation of the body, using the current charset.
     *
     * <p>The representation is returned decoded, using the content encodings set, if any.
     *
     * <p>The {@code String} representation contains only the contents set so far, that is,
     * increasing the length of the body manually (with {@link #HttpBody(int)} or {@link
     * #setLength(int)}) does not affect the string representation.
     *
     * @return the {@code String} representation of the body
     * @see #getCharset()
     * @see #getContentEncodings()
     */
    @Override
    public String toString() {
        if (cachedString != null) {
            return cachedString;
        }

        cachedString = createString(charset);
        return cachedString;
    }

    /**
     * Returns the {@code String} representation of the body.
     *
     * <p>Called when the cached string representation is no longer up-to-date.
     *
     * @param charset the current {@code Charset} set, {@code null} if none
     * @return the {@code String} representation of the body
     * @since 2.5.0
     * @see #getBytes()
     */
    protected String createString(Charset charset) {
        return new String(decode(), charset != null ? charset : getCharsetImpl());
    }

    /**
     * Gets the body decoded, if any content encodings are applied.
     *
     * @return the body decoded.
     * @see #getContentEncodings()
     */
    protected byte[] decode() {
        if (bodyDecoded != null) {
            return bodyDecoded;
        }

        bodyDecoded = decodeImpl();
        return bodyDecoded;
    }

    private byte[] decodeImpl() {
        byte[] value = pos != body.length ? Arrays.copyOf(body, pos) : body;
        try {
            byte[] decoded = value;
            for (HttpEncoding encoding : encodings) {
                decoded = encoding.decode(decoded);
            }
            contentEncodingErrors = false;
            return decoded;
        } catch (IOException e) {
            log.warn("An error occurred while decoding the body: {}", e.getMessage());
        }
        contentEncodingErrors = true;
        return value;
    }

    /**
     * Gets the actual (end) position of the contents in the byte array (different if the array was
     * expanded, either by setting the initial capacity or its length). Should be used when creating
     * the string representation of the body to only return the actual contents, set so far.
     *
     * @return the end position of the contents in the byte array
     * @since 2.5.0
     * @see #getBytes()
     * @see #createString(Charset)
     */
    protected final int getPos() {
        return pos;
    }

    /**
     * Gets the contents of the body as an array of bytes.
     *
     * <p>The returned array of bytes mustn't be modified. Is returned a reference instead of a copy
     * to avoid more memory allocations.
     *
     * @return a reference to the content of this body as {@code byte[]}.
     * @since 1.4.0
     */
    public byte[] getBytes() {
        return body;
    }

    /**
     * Gets the content of the body as bytes, without any encodings applied (if any).
     *
     * <p>The returned array of bytes mustn't be modified. Is returned a reference instead of a copy
     * to avoid more memory allocations.
     *
     * @return the content of the body.
     * @since TOOD add version
     * @see #getContentEncodings()
     * @see #hasContentEncodingErrors()
     * @see #toString()
     */
    public byte[] getContent() {
        if (encodings.isEmpty()) {
            return body;
        }
        return decode();
    }

    /**
     * Tells whether or not the content encodings set to the body have errors (e.g. content
     * malformed).
     *
     * @return {@code true} if there are errors while using the content encodings, {@code false}
     *     otherwise.
     * @since TOOD add version
     * @see #getContent()
     */
    public boolean hasContentEncodingErrors() {
        return contentEncodingErrors;
    }

    /**
     * Sets the content of the body as bytes, applying any content encodings of the body, if any.
     *
     * @since TOOD add version
     * @see #getContentEncodings()
     */
    public void setContent(byte[] content) {
        if (content == null) {
            return;
        }
        bodyDecoded = content;
        body = encode(bodyDecoded);
        pos = body.length;
        cachedString = null;
    }

    /**
     * Gets the current length of the body.
     *
     * @return the current length of the body.
     */
    public int length() {
        return body.length;
    }

    /**
     * Sets the current length of the body. If the current content is longer, the excessive data
     * will be truncated.
     *
     * @param length the new length to set.
     */
    public void setLength(int length) {
        if (length < 0 || body.length == length) {
            return;
        }

        int oldPos = pos;
        pos = Math.min(pos, length);

        byte[] newBody = new byte[length];
        System.arraycopy(body, 0, newBody, 0, pos);
        body = newBody;

        if (oldPos > pos) {
            resetCachedValues();
        }
    }

    /**
     * Gets the name of the charset used for {@code String} related operations.
     *
     * <p>If no charset was set it returns the default.
     *
     * @return the name of the charset, never {@code null}
     * @see #setCharset(String)
     * @see #DEFAULT_CHARSET
     */
    public String getCharset() {
        if (charset != null) {
            return charset.name();
        }
        return DEFAULT_CHARSET;
    }

    /**
     * Sets the charset used for {@code String} related operations, for example, {@link
     * #append(String)}, {@link #setBody(String)}, or {@link #toString()}.
     *
     * <p>The charset is reset if {@code null} or empty (that is, it will use default charset or the
     * charset determined internally by {@code HttpBody} implementations). The charset is ignored if
     * not valid (either the name is not valid or is unsupported).
     *
     * <p>
     *
     * @param charsetName the name of the charset to set
     * @see #getCharset()
     * @see #DEFAULT_CHARSET
     */
    public void setCharset(String charsetName) {
        if (StringUtils.isEmpty(charsetName)) {
            setCharsetImpl(null);
            return;
        }

        Charset newCharset = null;
        try {
            newCharset = Charset.forName(charsetName);
            if (newCharset != charset) {
                setCharsetImpl(newCharset);
            }
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            log.error("Failed to set charset: {}", charsetName, e);
        }
    }

    /**
     * Sets the charset to the given value and resets the cached string (that is, is set to {@code
     * null}).
     *
     * @param newCharset the new charset to set, might be {@code null}
     * @see #charset
     * @see #cachedString
     */
    private void setCharsetImpl(Charset newCharset) {
        this.charset = newCharset;
        this.cachedString = null;
    }

    /**
     * Sets the content encodings of the body.
     *
     * @param encodings the encodings
     * @throws NullPointerException if the given list or any of the encodings contained in the list
     *     are {@code null}.
     * @since 2.10.0
     */
    public void setContentEncodings(List<HttpEncoding> encodings) {
        Objects.requireNonNull(encodings);
        encodings.forEach(Objects::requireNonNull);

        this.encodings =
                encodings.isEmpty()
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(new ArrayList<>(encodings));
        resetCachedValues();
    }

    /**
     * Gets the content encodings of the body.
     *
     * @return the encodings, never {@code null}.
     * @since 2.10.0
     */
    public List<HttpEncoding> getContentEncodings() {
        return encodings;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + Arrays.hashCode(body);
        result = prime * result + Objects.hash(encodings);
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        HttpBody otherBody = (HttpBody) object;
        if (!Arrays.equals(body, otherBody.body)) {
            return false;
        }
        return Objects.equals(encodings, otherBody.encodings);
    }
}
