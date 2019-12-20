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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.swing.JTextArea;
import org.junit.jupiter.api.Test;

/** Unit test for {@link HttpTextViewUtils}. */
public class HttpTextViewUtilsUnitTest {

    private static final String HEADER =
            "GET /path HTTP/1.1\r\nHost: example.com\r\nX-SomeHeader: x-some-value\r\n\r\n";
    private static final int HEADER_LENGTH = HEADER.length();
    private static final JTextArea VIEW = new JTextArea(HEADER.replace("\r\n", "\n"));
    private static final int VIEW_LENGTH = VIEW.getDocument().getLength();

    private static final String BODY = "a=b&c=d\r\nXYZ";
    private static final JTextArea VIEW_WITH_BODY = new JTextArea(VIEW.getText() + BODY);
    private static final int VIEW_WITH_BODY_LENGTH = VIEW_WITH_BODY.getDocument().getLength();

    @Test
    public void shouldNotAllowUndefinedHeaderWhenGettingHeaderToViewPosition() {
        // Given
        String undefinedHeader = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getHeaderToViewPosition(VIEW, undefinedHeader, 0, 0));
    }

    @Test
    public void shouldNotAllowUndefinedViewWhenGettingHeaderToViewPosition() {
        // Given
        JTextArea undefinedView = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getHeaderToViewPosition(undefinedView, HEADER, 0, 0));
    }

    @Test
    public void shouldNotAllowNegativeStartWhenGettingHeaderToViewPosition() {
        // Given
        int start = -1;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getHeaderToViewPosition(VIEW, HEADER, start, 0));
    }

    @Test
    public void
            shouldReturnInvalidPositionIfStartGreaterThanLengthWhenGettingHeaderToViewPosition() {
        // Given / When
        int pos[] =
                HttpTextViewUtils.getHeaderToViewPosition(
                        VIEW, HEADER, HEADER_LENGTH + 1, HEADER_LENGTH + 2);
        // Then
        assertThat(pos, is(equalTo(HttpTextViewUtils.INVALID_POSITION)));
    }

    @Test
    public void shouldNotAllowNegativeEndWhenGettingHeaderToViewPosition() {
        // Given
        int end = -1;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getHeaderToViewPosition(VIEW, HEADER, 0, end));
    }

    @Test
    public void shouldReturnInvalidPositionIfEndGreaterThanLengthWhenGettingHeaderToViewPosition() {
        // Given / When
        int[] pos = HttpTextViewUtils.getHeaderToViewPosition(VIEW, HEADER, 0, HEADER_LENGTH + 1);
        // Then
        assertThat(pos, is(equalTo(HttpTextViewUtils.INVALID_POSITION)));
    }

    @Test
    public void shouldNotAllowStartGreaterThanEndWhenGettingHeaderToViewPosition() {
        // Given
        int start = 2;
        int end = 1;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getHeaderToViewPosition(VIEW, HEADER, start, end));
    }

    @Test
    public void shouldNotOffsetPositionsIfOnFirstHeaderLineWhenGettingHeaderToViewPosition()
            throws Exception {
        // Given
        int start = 10;
        int end = 18;
        // When
        int[] pos = HttpTextViewUtils.getHeaderToViewPosition(VIEW, HEADER, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(10)));
        assertThat(pos[1], is(equalTo(18)));
        assertThat(VIEW.getText(pos[0], pos[1] - pos[0]), is(equalTo("HTTP/1.1")));
    }

    @Test
    public void shouldOffsetPositionNotOnFirstLineWhenGettingHeaderToViewPosition()
            throws Exception {
        // Given
        int start = 4;
        int end = 65;
        // When
        int[] pos = HttpTextViewUtils.getHeaderToViewPosition(VIEW, HEADER, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(4)));
        assertThat(pos[1], is(equalTo(63)));
        assertThat(
                VIEW.getText(pos[0], pos[1] - pos[0]),
                is(equalTo("/path HTTP/1.1\nHost: example.com\nX-SomeHeader: x-some-value")));
    }

    @Test
    public void shouldOffsetPositionsPerEachLineHeaderWhenGettingHeaderToViewPosition()
            throws Exception {
        // Given
        int start = 20;
        int end = 52;
        // When
        int[] pos = HttpTextViewUtils.getHeaderToViewPosition(VIEW, HEADER, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(19)));
        assertThat(pos[1], is(equalTo(50)));
        assertThat(
                VIEW.getText(pos[0], pos[1] - pos[0]),
                is(equalTo("Host: example.com\nX-SomeHeader:")));
    }

    @Test
    public void shouldOffsetLastLineHeaderWhenGettingHeaderToViewPosition() throws Exception {
        // Given
        int start = 0;
        int end = HEADER_LENGTH;
        // When
        int[] pos = HttpTextViewUtils.getHeaderToViewPosition(VIEW, HEADER, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(0)));
        assertThat(pos[1], is(equalTo(VIEW_LENGTH)));
        assertThat(
                VIEW.getText(pos[0], pos[1]),
                is(
                        equalTo(
                                "GET /path HTTP/1.1\nHost: example.com\nX-SomeHeader: x-some-value\n\n")));
    }

    @Test
    public void shouldOffsetTillLastLineHeaderWhenGettingHeaderToViewPosition() {
        // Given
        int start = HEADER_LENGTH;
        int end = HEADER_LENGTH;
        // When
        int[] pos = HttpTextViewUtils.getHeaderToViewPosition(VIEW, HEADER, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(VIEW_LENGTH)));
        assertThat(pos[1], is(equalTo(VIEW_LENGTH)));
    }

    @Test
    public void
            shouldReturnInvalidPositionIfOffsetStartIsGreaterThanViewLengthWhenGettingHeaderToViewPosition() {
        // Given
        JTextArea view = new JTextArea("ABC");
        int start = 5;
        int end = 6;
        // When
        int[] pos = HttpTextViewUtils.getHeaderToViewPosition(view, HEADER, start, end);
        // Then
        assertThat(pos, is(equalTo(HttpTextViewUtils.INVALID_POSITION)));
    }

    @Test
    public void
            shouldReturnInvalidPositionIfOffsetEndIsGreaterThanViewLengthWhenGettingHeaderToViewPosition() {
        // Given
        JTextArea view = new JTextArea("ABC");
        int start = 2;
        int end = 6;
        // When
        int[] pos = HttpTextViewUtils.getHeaderToViewPosition(view, HEADER, start, end);
        // Then
        assertThat(pos, is(equalTo(HttpTextViewUtils.INVALID_POSITION)));
    }

    @Test
    public void shouldNotAllowUndefinedViewWhenGettingViewToHeaderPosition() {
        // Given
        JTextArea undefinedView = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getViewToHeaderPosition(undefinedView, 0, 0));
    }

    @Test
    public void shouldNotAllowNegativeStartWhenGettingViewToHeaderPosition() {
        // Given
        int start = -1;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getViewToHeaderPosition(VIEW, start, 0));
    }

    @Test
    public void
            shouldReturnInvalidPositionIfStartGreaterThanLengthWhenGettingViewToHeaderPosition() {
        // Given / When
        int[] pos =
                HttpTextViewUtils.getViewToHeaderPosition(VIEW, VIEW_LENGTH + 1, VIEW_LENGTH + 2);
        // Then
        assertThat(pos, is(equalTo(HttpTextViewUtils.INVALID_POSITION)));
    }

    @Test
    public void shouldNotAllowNegativeEndWhenGettingViewToHeaderPosition() {
        // Given
        int end = -1;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getViewToHeaderPosition(VIEW, 0, end));
    }

    @Test
    public void shouldReturnInvalidPositionIfEndGreaterThanLengthWhenGettingViewToHeaderPosition() {
        // Given / When
        int[] pos = HttpTextViewUtils.getViewToHeaderPosition(VIEW, 0, VIEW_LENGTH + 1);
        // Then
        assertThat(pos, is(equalTo(HttpTextViewUtils.INVALID_POSITION)));
    }

    @Test
    public void shouldNotAllowStartGreaterThanEndWhenGettingViewToHeaderPosition() {
        // Given
        int start = 2;
        int end = 1;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getViewToHeaderPosition(VIEW, start, end));
    }

    @Test
    public void shouldNotOffsetPositionsIfOnFirstHeaderLineWhenGettingViewToHeaderPosition() {
        // Given
        int start = 10;
        int end = 18;
        // When
        int[] pos = HttpTextViewUtils.getViewToHeaderPosition(VIEW, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(10)));
        assertThat(pos[1], is(equalTo(18)));
        assertThat(HEADER.substring(pos[0], pos[1]), is(equalTo("HTTP/1.1")));
    }

    @Test
    public void shouldOffsetPositionNotOnFirstLineWhenGettingViewToHeaderPosition() {
        // Given
        int start = 4;
        int end = 63;
        // When
        int[] pos = HttpTextViewUtils.getViewToHeaderPosition(VIEW, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(4)));
        assertThat(pos[1], is(equalTo(65)));
        assertThat(
                HEADER.substring(pos[0], pos[1]),
                is(equalTo("/path HTTP/1.1\r\nHost: example.com\r\nX-SomeHeader: x-some-value")));
    }

    @Test
    public void shouldOffsetPositionsPerEachLineHeaderHeaderWhenGettingViewToHeaderPosition() {
        // Given
        int start = 19;
        int end = 50;
        // When
        int[] pos = HttpTextViewUtils.getViewToHeaderPosition(VIEW, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(20)));
        assertThat(pos[1], is(equalTo(52)));
        assertThat(
                HEADER.substring(pos[0], pos[1]),
                is(equalTo("Host: example.com\r\nX-SomeHeader:")));
    }

    @Test
    public void shouldOffsetLastLineHeaderWhenGettingViewToHeaderPosition() {
        // Given
        int start = 0;
        int end = VIEW_LENGTH;
        // When
        int[] pos = HttpTextViewUtils.getViewToHeaderPosition(VIEW, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(0)));
        assertThat(pos[1], is(equalTo(HEADER_LENGTH)));
        assertThat(
                HEADER.substring(pos[0], pos[1]),
                is(
                        equalTo(
                                "GET /path HTTP/1.1\r\nHost: example.com\r\nX-SomeHeader: x-some-value\r\n\r\n")));
    }

    @Test
    public void shouldOffsetTillLastLineHeaderWhenGettingViewToHeaderPosition() {
        // Given
        int start = VIEW_LENGTH;
        int end = VIEW_LENGTH;
        // When
        int[] pos = HttpTextViewUtils.getViewToHeaderPosition(VIEW, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(HEADER_LENGTH)));
        assertThat(pos[1], is(equalTo(HEADER_LENGTH)));
    }

    @Test
    public void shouldNotAllowUndefinedHeaderWhenGettingBodyToViewPosition() {
        // Given
        String undefinedHeader = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        HttpTextViewUtils.getBodyToViewPosition(
                                VIEW_WITH_BODY, undefinedHeader, 0, 0));
    }

    @Test
    public void shouldNotAllowUndefinedViewWhenGettingBodyToViewPosition() {
        // Given
        JTextArea undefinedView = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getBodyToViewPosition(undefinedView, HEADER, 0, 0));
    }

    @Test
    public void shouldNotAllowNegativeStartWhenGettingBodyToViewPosition() {
        // Given
        int start = -1;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getBodyToViewPosition(VIEW_WITH_BODY, HEADER, start, 0));
    }

    @Test
    public void shouldReturnInvalidPositionIfStartGreaterThanLengthWhenGettingBodyToViewPosition() {
        // Given / When
        int[] pos =
                HttpTextViewUtils.getBodyToViewPosition(
                        VIEW_WITH_BODY,
                        HEADER,
                        VIEW_WITH_BODY_LENGTH + 1,
                        VIEW_WITH_BODY_LENGTH + 2);
        // Then
        assertThat(pos, is(equalTo(HttpTextViewUtils.INVALID_POSITION)));
    }

    @Test
    public void shouldNotAllowNegativeEndWhenGettingBodyToViewPosition() {
        // Given
        int end = -1;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getBodyToViewPosition(VIEW_WITH_BODY, HEADER, 0, end));
    }

    @Test
    public void shouldReturnInvalidPositionIfEndGreaterThanLengthWhenGettingBodyToViewPosition() {
        // Given / When
        int[] pos =
                HttpTextViewUtils.getBodyToViewPosition(
                        VIEW_WITH_BODY, HEADER, 0, VIEW_WITH_BODY_LENGTH + 1);
        // Then
        assertThat(pos, is(equalTo(HttpTextViewUtils.INVALID_POSITION)));
    }

    @Test
    public void shouldNotAllowStartGreaterThanEndWhenGettingBodyToViewPosition() {
        // Given
        int start = 2;
        int end = 1;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getBodyToViewPosition(VIEW_WITH_BODY, HEADER, start, end));
    }

    @Test
    public void shouldOffsetPositionsPerEachLineHeaderWhenGettingBodyToViewPosition()
            throws Exception {
        // Given
        int start = 0;
        int end = 12;
        // When
        int[] pos = HttpTextViewUtils.getBodyToViewPosition(VIEW_WITH_BODY, HEADER, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(65)));
        assertThat(pos[1], is(equalTo(77)));
        assertThat(VIEW_WITH_BODY.getText(pos[0], pos[1] - pos[0]), is(equalTo(BODY)));
    }

    @Test
    public void shouldReturnInvalidPositionIfViewHasNoHBodyLengthWhenGettingBodyToViewPosition() {
        // Given
        JTextArea view = new JTextArea("AB");
        int start = 0;
        int end = 0;
        // When
        int[] pos = HttpTextViewUtils.getBodyToViewPosition(view, HEADER, start, end);
        // Then
        assertThat(pos, is(equalTo(HttpTextViewUtils.INVALID_POSITION)));
    }

    @Test
    public void
            shouldReturnInvalidPositionIfOffsetStartIsGreaterThanViewLengthWhenGettingBodyToViewPosition() {
        // Given
        int start = 1;
        int end = 2;
        // When
        int[] pos = HttpTextViewUtils.getBodyToViewPosition(VIEW, HEADER, start, end);
        // Then
        assertThat(pos, is(equalTo(HttpTextViewUtils.INVALID_POSITION)));
    }

    @Test
    public void
            shouldReturnInvalidPositionIfOffsetEndIsGreaterThanViewLengthWhenGettingBodyToViewPosition() {
        // Given
        int start = 0;
        int end = 1;
        // When
        int[] pos = HttpTextViewUtils.getBodyToViewPosition(VIEW, HEADER, start, end);
        // Then
        assertThat(pos, is(equalTo(HttpTextViewUtils.INVALID_POSITION)));
    }

    @Test
    public void shouldNotAllowUndefinedHeaderWhenGettingViewToHeaderBodyPosition() {
        // Given
        String undefinedHeader = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        HttpTextViewUtils.getViewToHeaderBodyPosition(
                                VIEW_WITH_BODY, undefinedHeader, 0, 0));
    }

    @Test
    public void shouldNotAllowUndefinedViewWhenGettingViewToHeaderBodyPosition() {
        // Given
        JTextArea undefinedView = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> HttpTextViewUtils.getViewToHeaderBodyPosition(undefinedView, HEADER, 0, 0));
    }

    @Test
    public void shouldNotAllowNegativeStartWhenGettingViewToHeaderBodyPosition() {
        // Given
        int start = -1;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        HttpTextViewUtils.getViewToHeaderBodyPosition(
                                VIEW_WITH_BODY, HEADER, start, 0));
    }

    @Test
    public void
            shouldReturnInvalidPositionIfStartGreaterThanLengthWhenGettingViewToHeaderBodyPosition() {
        // Given / When
        int[] pos =
                HttpTextViewUtils.getViewToHeaderBodyPosition(
                        VIEW_WITH_BODY,
                        HEADER,
                        VIEW_WITH_BODY_LENGTH + 1,
                        VIEW_WITH_BODY_LENGTH + 2);
        // Then
        assertThat(pos, is(equalTo(HttpTextViewUtils.INVALID_POSITION)));
    }

    @Test
    public void shouldNotAllowNegativeEndWhenGettingViewToHeaderBodyPosition() {
        // Given
        int end = -1;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        HttpTextViewUtils.getViewToHeaderBodyPosition(
                                VIEW_WITH_BODY, HEADER, 0, end));
    }

    @Test
    public void
            shouldReturnInvalidPositionIfEndGreaterThanLengthWhenGettingViewToHeaderBodyPosition() {
        // Given / When
        int[] pos =
                HttpTextViewUtils.getViewToHeaderBodyPosition(
                        VIEW_WITH_BODY, HEADER, 0, VIEW_WITH_BODY_LENGTH + 1);
        // Then
        assertThat(pos, is(equalTo(HttpTextViewUtils.INVALID_POSITION)));
    }

    @Test
    public void shouldNotAllowStartGreaterThanEndWhenGettingViewToHeaderBodyPosition() {
        // Given
        int start = 2;
        int end = 1;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        HttpTextViewUtils.getViewToHeaderBodyPosition(
                                VIEW_WITH_BODY, HEADER, start, end));
    }

    @Test
    public void shouldNotOffsetPositionsIfOnFirstHeaderLineWhenGettingViewToHeaderBodyPosition() {
        // Given
        int start = 10;
        int end = 18;
        // When
        int[] pos =
                HttpTextViewUtils.getViewToHeaderBodyPosition(VIEW_WITH_BODY, HEADER, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(10)));
        assertThat(pos[1], is(equalTo(18)));
        assertThat(HEADER.substring(pos[0], pos[1]), is(equalTo("HTTP/1.1")));
    }

    @Test
    public void shouldOffsetPositionNotOnFirstLineWhenGettingViewToHeaderBodyPosition() {
        // Given
        int start = 4;
        int end = 63;
        // When
        int[] pos =
                HttpTextViewUtils.getViewToHeaderBodyPosition(VIEW_WITH_BODY, HEADER, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(4)));
        assertThat(pos[1], is(equalTo(65)));
        assertThat(
                HEADER.substring(pos[0], pos[1]),
                is(equalTo("/path HTTP/1.1\r\nHost: example.com\r\nX-SomeHeader: x-some-value")));
    }

    @Test
    public void shouldOffsetPositionsPerEachLineHeaderHeaderWhenGettingViewToHeaderBodyPosition() {
        // Given
        int start = 19;
        int end = 50;
        // When
        int[] pos =
                HttpTextViewUtils.getViewToHeaderBodyPosition(VIEW_WITH_BODY, HEADER, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(20)));
        assertThat(pos[1], is(equalTo(52)));
        assertThat(
                HEADER.substring(pos[0], pos[1]),
                is(equalTo("Host: example.com\r\nX-SomeHeader:")));
    }

    @Test
    public void shouldOffsetLastLineHeaderWhenGettingViewToHeaderBodyPosition() {
        // Given
        int start = 0;
        int end = VIEW_LENGTH;
        // When
        int[] pos =
                HttpTextViewUtils.getViewToHeaderBodyPosition(VIEW_WITH_BODY, HEADER, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(0)));
        assertThat(pos[1], is(equalTo(HEADER_LENGTH)));
        assertThat(
                HEADER.substring(pos[0], pos[1]),
                is(
                        equalTo(
                                "GET /path HTTP/1.1\r\nHost: example.com\r\nX-SomeHeader: x-some-value\r\n\r\n")));
    }

    @Test
    public void
            shouldTruncateEndOffsetIfGreaterThanLastLineHeaderWhenGettingViewToHeaderBodyPosition() {
        // Given
        int start = 0;
        int end = VIEW_WITH_BODY_LENGTH;
        // When
        int[] pos =
                HttpTextViewUtils.getViewToHeaderBodyPosition(VIEW_WITH_BODY, HEADER, start, end);
        // Then
        assertThat(pos.length, is(equalTo(2)));
        assertThat(pos[0], is(equalTo(0)));
        assertThat(pos[1], is(equalTo(HEADER_LENGTH)));
        assertThat(
                HEADER.substring(pos[0], pos[1]),
                is(
                        equalTo(
                                "GET /path HTTP/1.1\r\nHost: example.com\r\nX-SomeHeader: x-some-value\r\n\r\n")));
    }

    @Test
    public void shouldOffsetToBodyAfterLastLineHeaderWhenGettingViewToHeaderBodyPosition() {
        // Given
        int start = VIEW_LENGTH;
        int end = VIEW_WITH_BODY_LENGTH;
        // When
        int[] pos =
                HttpTextViewUtils.getViewToHeaderBodyPosition(VIEW_WITH_BODY, HEADER, start, end);
        // Then
        assertThat(pos.length, is(equalTo(3)));
        assertThat(pos[0], is(equalTo(0)));
        assertThat(pos[1], is(equalTo(BODY.length())));
    }
}
