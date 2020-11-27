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
package org.zaproxy.zap.extension.httppanel.view.util;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HttpHeader;

/**
 * Utility methods related to text views of HTTP messages.
 *
 * @since 2.6.0
 */
public final class HttpTextViewUtils {

    /**
     * Position returned when the calculated offsets are greater than the length of the view (e.g.
     * contents of the view do not match the header).
     */
    public static final int[] INVALID_POSITION = {};

    private static final Logger LOGGER = LogManager.getLogger(HttpTextViewUtils.class);

    private HttpTextViewUtils() {}

    /**
     * Gets the given {@code start} and {@code end} header positions offset to the given {@code
     * view}.
     *
     * <p>The {@code view} is expected to replace the header line endings {@code \r\n} to {@code \n}
     * (e.g. so there's no invisible newline ({@code \r}) characters when editing), as such the
     * positions of the {@code header} need to be offset to match the ones in the {@code view}.
     *
     * @param view the view that contains the contents of the header
     * @param header the header shown in the view
     * @param start the start position
     * @param end the end position
     * @return the positions offset for the {@code view}, or {@link #INVALID_POSITION} if the {@code
     *     start}, {@code end} or offset positions are greater than {@code view}'s length.
     * @throws IllegalArgumentException if any of the conditions is true:
     *     <ul>
     *       <li>the {@code view} is {@code null} or it has no {@link JTextArea#getDocument()
     *           Document};
     *       <li>the {@code header} is {@code null};
     *       <li>the {@code start} position is negative or greater than the length of the {@code
     *           header};
     *       <li>the {@code end} position is negative or greater than the length of the {@code
     *           header};
     *       <li>the {@code start} position is greater than the {@code end} position.
     *     </ul>
     *
     * @see #getViewToHeaderPosition(JTextArea, int, int)
     * @see #getBodyToViewPosition(JTextArea, String, int, int)
     */
    public static int[] getHeaderToViewPosition(JTextArea view, String header, int start, int end) {
        validateView(view);
        validateHeader(header);
        validateStartEnd(start, end);

        if (!isValidStartEndForLength(start, end, header.length())) {
            return INVALID_POSITION;
        }

        int excessChars = 0;

        int pos = 0;
        while ((pos = header.indexOf(HttpHeader.CRLF, pos)) != -1 && pos < start) {
            pos += 2;
            ++excessChars;
        }

        int len = view.getDocument().getLength();
        int finalStartPos = start - excessChars;
        if (finalStartPos > len) {
            return INVALID_POSITION;
        }

        if (pos != -1) {
            while ((pos = header.indexOf(HttpHeader.CRLF, pos)) != -1 && pos < end) {
                pos += 2;
                ++excessChars;
            }
        }

        int finalEndPos = end - excessChars;
        if (finalEndPos > len) {
            return INVALID_POSITION;
        }

        return new int[] {finalStartPos, finalEndPos};
    }

    /**
     * Validates that the given {@code view} is not {@code null} and has a {@code Document}.
     *
     * @param view the view to be validated
     * @throws IllegalArgumentException if the {@code view} is {@code null} or it has no {@link
     *     JTextArea#getDocument() Document}.
     */
    private static void validateView(JTextArea view) {
        if (view == null || view.getDocument() == null) {
            throw new IllegalArgumentException(
                    "Parameter view must not be null and must have a Document.");
        }
    }

    /**
     * Validates that the given {@code header} is not {@code null}.
     *
     * @param header the header to be validated
     * @throws IllegalArgumentException if the {@code header} is {@code null}.
     */
    private static void validateHeader(String header) {
        if (header == null) {
            throw new IllegalArgumentException("Parameter header must not be null.");
        }
    }

    /**
     * Validates the given {@code start} and {@code end} positions.
     *
     * @param start the start position to be validated
     * @param end the end position to be validated
     * @throws IllegalArgumentException if any of the conditions is true:
     *     <ul>
     *       <li>the {@code start} position is negative;
     *       <li>the {@code end} position is negative;
     *       <li>the {@code start} position is greater than the {@code end} position.
     *     </ul>
     */
    private static void validateStartEnd(int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("Parameter start must not be negative.");
        }
        if (end < 0) {
            throw new IllegalArgumentException("Parameter end must not be negative.");
        }
        if (start > end) {
            throw new IllegalArgumentException("Parameter start must not be greater than end.");
        }
    }

    /**
     * Tells whether or not the given start and end are valid for the given length, that is both
     * start and end are lower that the length.
     *
     * @param start the start position to be validated
     * @param end the end position to be validated
     * @param length the length of the contents
     * @return {@code true} if the start and end positions are lower than the length, {@code false}
     *     otherwise.
     */
    private static boolean isValidStartEndForLength(int start, int end, int length) {
        if (start > length || end > length) {
            return false;
        }
        return true;
    }

    /**
     * Gets the given {@code start} and {@code end} body positions offset to the given {@code view}.
     *
     * <p>The {@code view} is expected to replace the header line endings {@code \r\n} to {@code \n}
     * (e.g. so there's no invisible newline ({@code \r}) characters when editing), as such the
     * positions of the body (shown after the header) need to be offset to match the ones in the
     * {@code view}.
     *
     * @param view the view that contains the contents of the header and the body
     * @param header the header shown in the view
     * @param start the start position
     * @param end the end position
     * @return the positions offset for the {@code view}, or {@link #INVALID_POSITION} if the {@code
     *     start} and {@code end} positions are greater than the length of the body or the {@code
     *     view}.
     * @throws IllegalArgumentException if any of the conditions is true:
     *     <ul>
     *       <li>the {@code view} is {@code null} or it has no {@link JTextArea#getDocument()
     *           Document};
     *       <li>the {@code header} is {@code null};
     *       <li>the {@code start} position is negative;
     *       <li>the {@code end} position is negative;
     *       <li>the {@code start} position is greater than the {@code end} position.
     *     </ul>
     *
     * @see #getHeaderToViewPosition(JTextArea, String, int, int)
     * @see #getViewToHeaderBodyPosition(JTextArea, String, int, int)
     */
    public static int[] getBodyToViewPosition(JTextArea view, String header, int start, int end) {
        validateView(view);
        validateHeader(header);
        validateStartEnd(start, end);

        if (!isValidStartEndForLength(start, end, view.getDocument().getLength())) {
            return INVALID_POSITION;
        }

        int excessChars = 0;

        int pos = 0;
        while ((pos = header.indexOf(HttpHeader.CRLF, pos)) != -1) {
            pos += 2;
            ++excessChars;
        }

        int len = view.getDocument().getLength();
        int bodyLen = len - header.length() + excessChars;
        if (bodyLen < 0 || start > bodyLen || end > bodyLen) {
            return INVALID_POSITION;
        }

        int finalStartPos = start + header.length() - excessChars;
        int finalEndPos = end + header.length() - excessChars;
        return new int[] {finalStartPos, finalEndPos};
    }

    /**
     * Gets the given {@code start} and {@code end} view positions offset to a header.
     *
     * <p>The {@code view} is expected to replace the header line endings {@code \r\n} to {@code \n}
     * (e.g. so there's no invisible newline ({@code \r}) characters when editing), as such the
     * positions of the {@code view} need to be offset to match the ones in the header.
     *
     * @param view the view that contains the contents of a header
     * @param start the start position
     * @param end the end position
     * @return the positions offset for the header, or {@link #INVALID_POSITION} if the {@code
     *     start} or {@code end} is greater than the length of the {@code view}
     * @throws IllegalArgumentException if any of the conditions is true:
     *     <ul>
     *       <li>the {@code view} is {@code null} or it has no {@link JTextArea#getDocument()
     *           Document};
     *       <li>the {@code start} position is negative;
     *       <li>the {@code end} position is negative;
     *       <li>the {@code start} position is greater than the {@code end} position.
     *     </ul>
     *
     * @see #getHeaderToViewPosition(JTextArea, String, int, int)
     * @see #getViewToHeaderBodyPosition(JTextArea, String, int, int)
     */
    public static int[] getViewToHeaderPosition(JTextArea view, int start, int end) {
        validateView(view);
        validateStartEnd(start, end);

        if (!isValidStartEndForLength(start, end, view.getDocument().getLength())) {
            return INVALID_POSITION;
        }

        return getViewToHeaderPositionImpl(view, start, end);
    }

    /**
     * Gets the given {@code start} and {@code end} view positions offset to a header.
     *
     * <p>The {@code view} is expected to replace the header line endings {@code \r\n} to {@code \n}
     * (e.g. so there's no invisible newline ({@code \r}) characters when editing), as such the
     * positions of the {@code view} need to be offset to match the ones in the header.
     *
     * <p><strong>Note:</strong> The {@code view} and {@code start} and {@code end} positions should
     * be validated before calling this method.
     *
     * @param view the view that contains the contents of a header
     * @param start the start position
     * @param end the end position
     * @return the positions offset for the header
     * @see #validateView(JTextArea)
     * @see #validateStartEnd(int, int)
     * @see #isValidStartEndForLength(int, int, int)
     */
    private static int[] getViewToHeaderPositionImpl(JTextArea view, int start, int end) {
        int finalStartPos = start;
        try {
            finalStartPos += view.getLineOfOffset(finalStartPos);
        } catch (BadLocationException e) {
            // Shouldn't happen, position was already validated.
            LOGGER.error(e.getMessage(), e);
            return INVALID_POSITION;
        }

        int finalEndPos = end;
        try {
            finalEndPos += view.getLineOfOffset(finalEndPos);
        } catch (BadLocationException e) {
            // Shouldn't happen, position was already validated.
            LOGGER.error(e.getMessage(), e);
            return INVALID_POSITION;
        }
        return new int[] {finalStartPos, finalEndPos};
    }

    /**
     * Gets the given {@code start} and {@code end} view positions offset to, or after, the given
     * {@code header}.
     *
     * <p>The {@code view} is expected to replace the header line endings {@code \r\n} to {@code \n}
     * (e.g. so there's no invisible newline ({@code \r}) characters when editing), as such the
     * positions of the {@code view} need to be offset to match the ones in, or after, the {@code
     * header}.
     *
     * @param view the view that contains the contents of the header and body
     * @param header the header shown in the view
     * @param start the start position
     * @param end the end position
     * @return the positions offset for the header or, 3 positions, for after the body (the third
     *     position is just to indicate that it's the body, the value is meaningless), or {@link
     *     #INVALID_POSITION} if the {@code start} or {@code end} is greater than the length of the
     *     {@code view}
     * @throws IllegalArgumentException if any of the conditions is true:
     *     <ul>
     *       <li>the {@code view} is {@code null} or it has no {@link JTextArea#getDocument()
     *           Document};
     *       <li>the {@code start} position is negative;
     *       <li>the {@code end} position is negative;
     *       <li>the {@code start} position is greater than the {@code end} position.
     *     </ul>
     */
    public static int[] getViewToHeaderBodyPosition(
            JTextArea view, String header, int start, int end) {
        validateView(view);
        validateHeader(header);
        validateStartEnd(start, end);

        if (!isValidStartEndForLength(start, end, view.getDocument().getLength())) {
            return INVALID_POSITION;
        }

        int excessChars = 0;
        int pos = 0;
        while ((pos = header.indexOf("\r\n", pos)) != -1) {
            pos += 2;
            ++excessChars;
        }

        if (start + excessChars < header.length()) {
            int[] position = getViewToHeaderPositionImpl(view, start, end);
            if (position[1] > header.length()) {
                position[1] = header.length();
            }
            return position;
        }

        int finalStartPos = start + excessChars - header.length();
        int finalEndPos = end + excessChars - header.length();
        return new int[] {finalStartPos, finalEndPos, 0};
    }
}
