/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.lexers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.junit.jupiter.api.Test;

/** Unit test for {@link HttpRequestTokenMaker}. */
class HttpRequestTokenMakerUnitTest {

    private final HttpRequestTokenMaker tokenMaker = new HttpRequestTokenMaker();

    // -----------------------------------------------------------------------
    // Request line
    // -----------------------------------------------------------------------

    @Test
    void shouldTokeniseGetRequestLine() {
        Token tokens = tokenMaker.getTokenList(segment("GET /path HTTP/1.1"), 0, 0);
        assertTokens(
                tokens,
                token(TokenTypes.RESERVED_WORD, "GET"),
                token(TokenTypes.WHITESPACE, " "),
                token(TokenTypes.FUNCTION, "/path"),
                token(TokenTypes.WHITESPACE, " "),
                token(TokenTypes.LITERAL_STRING_DOUBLE_QUOTE, "HTTP/1.1"),
                token(HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME));
    }

    @Test
    void shouldTokenisePostRequestLine() {
        Token tokens = tokenMaker.getTokenList(segment("POST /api/data HTTP/2"), 0, 0);
        assertTokens(
                tokens,
                token(TokenTypes.RESERVED_WORD, "POST"),
                token(TokenTypes.WHITESPACE, " "),
                token(TokenTypes.FUNCTION, "/api/data"),
                token(TokenTypes.WHITESPACE, " "),
                token(TokenTypes.LITERAL_STRING_DOUBLE_QUOTE, "HTTP/2"),
                token(HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME));
    }

    // -----------------------------------------------------------------------
    // Headers
    // -----------------------------------------------------------------------

    @Test
    void shouldTokeniseGenericHeader() {
        Token tokens =
                tokenMaker.getTokenList(
                        segment("Host: example.com"),
                        HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME,
                        0);
        assertTokens(
                tokens,
                token(TokenTypes.DATA_TYPE, "Host"),
                token(TokenTypes.SEPARATOR, ":"),
                token(TokenTypes.WHITESPACE, " "),
                token(TokenTypes.LITERAL_CHAR, "example.com"),
                token(HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME));
    }

    @Test
    void shouldTokeniseContentTypeFormHeader() {
        Token tokens =
                tokenMaker.getTokenList(
                        segment("Content-Type: application/x-www-form-urlencoded"),
                        HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME,
                        0);
        assertTokens(
                tokens,
                token(TokenTypes.DATA_TYPE, "Content-Type"),
                token(TokenTypes.SEPARATOR, ":"),
                token(TokenTypes.WHITESPACE, " "),
                token(TokenTypes.LITERAL_CHAR, "application/x-www-form-urlencoded"),
                token(HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME_FORM));
    }

    @Test
    void shouldTokeniseContentTypeJsonHeader() {
        Token tokens =
                tokenMaker.getTokenList(
                        segment("Content-Type: application/json"),
                        HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME,
                        0);
        assertTokens(
                tokens,
                token(TokenTypes.DATA_TYPE, "Content-Type"),
                token(TokenTypes.SEPARATOR, ":"),
                token(TokenTypes.WHITESPACE, " "),
                token(TokenTypes.LITERAL_CHAR, "application/json"),
                token(HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME_JSON));
    }

    @Test
    void shouldTokeniseContentTypeJsonHeaderCaseInsensitive() {
        Token tokens =
                tokenMaker.getTokenList(
                        segment("content-type: application/json"),
                        HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME,
                        0);
        assertTokens(
                tokens,
                token(TokenTypes.DATA_TYPE, "content-type"),
                token(TokenTypes.SEPARATOR, ":"),
                token(TokenTypes.WHITESPACE, " "),
                token(TokenTypes.LITERAL_CHAR, "application/json"),
                token(HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME_JSON));
    }

    @Test
    void shouldTokeniseContentTypeWithCharsetSuffix() {
        Token tokens =
                tokenMaker.getTokenList(
                        segment("Content-Type: application/json; charset=utf-8"),
                        HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME,
                        0);
        assertTokens(
                tokens,
                token(TokenTypes.DATA_TYPE, "Content-Type"),
                token(TokenTypes.SEPARATOR, ":"),
                token(TokenTypes.WHITESPACE, " "),
                token(TokenTypes.LITERAL_CHAR, "application/json; charset=utf-8"),
                token(HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME_JSON));
    }

    @Test
    void shouldSignalPlainBodyOnBlankLineWithNoSpecialContentType() {
        // Blank line = header/body separator when body type is plain
        Token tokens =
                tokenMaker.getTokenList(
                        segment(""), HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME, 0);
        assertTokens(tokens, token(HttpRequestTokenMaker.INTERNAL_BODY_PLAIN));
    }

    @Test
    void shouldSignalFormBodyOnBlankLineAfterFormContentType() {
        Token tokens =
                tokenMaker.getTokenList(
                        segment(""), HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME_FORM, 0);
        assertTokens(tokens, token(HttpRequestTokenMaker.INTERNAL_BODY_FORM));
    }

    @Test
    void shouldSignalJsonBodyOnBlankLineAfterJsonContentType() {
        Token tokens =
                tokenMaker.getTokenList(
                        segment(""), HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME_JSON, 0);
        assertTokens(tokens, token(HttpRequestTokenMaker.INTERNAL_BODY_JSON));
    }

    // -----------------------------------------------------------------------
    // Plain body
    // -----------------------------------------------------------------------

    @Test
    void shouldTokenisePlainBodyLineAsIdentifier() {
        Token tokens =
                tokenMaker.getTokenList(
                        segment("some plain text body"),
                        HttpRequestTokenMaker.INTERNAL_BODY_PLAIN,
                        0);
        assertTokens(
                tokens,
                token(TokenTypes.IDENTIFIER, "some plain text body"),
                token(HttpRequestTokenMaker.INTERNAL_BODY_PLAIN));
    }

    @Test
    void shouldTokeniseEmptyPlainBodyLineWithoutContentToken() {
        Token tokens =
                tokenMaker.getTokenList(segment(""), HttpRequestTokenMaker.INTERNAL_BODY_PLAIN, 0);
        assertTokens(tokens, token(HttpRequestTokenMaker.INTERNAL_BODY_PLAIN));
    }

    @Test
    void shouldNotCrashOnBodyLineWithUnicodeLindTerminatorNel() {
        // U+0085 (NEL) — raw byte 0x85 decoded as Latin-1; previously caused ZZ_NO_MATCH
        String line = "binary\u0085content";
        Token tokens =
                tokenMaker.getTokenList(
                        segment(line), HttpRequestTokenMaker.INTERNAL_BODY_PLAIN, 0);
        assertTokens(
                tokens,
                token(TokenTypes.IDENTIFIER, line),
                token(HttpRequestTokenMaker.INTERNAL_BODY_PLAIN));
    }

    @Test
    void shouldNotCrashOnBodyLineWithLineSeparator() {
        // U+2028 (LS)
        String line = "data\u2028more";
        Token tokens =
                tokenMaker.getTokenList(
                        segment(line), HttpRequestTokenMaker.INTERNAL_BODY_PLAIN, 0);
        assertTokens(
                tokens,
                token(TokenTypes.IDENTIFIER, line),
                token(HttpRequestTokenMaker.INTERNAL_BODY_PLAIN));
    }

    @Test
    void shouldNotCrashOnBodyLineWithParagraphSeparator() {
        // U+2029 (PS)
        String line = "data\u2029more";
        Token tokens =
                tokenMaker.getTokenList(
                        segment(line), HttpRequestTokenMaker.INTERNAL_BODY_PLAIN, 0);
        assertTokens(
                tokens,
                token(TokenTypes.IDENTIFIER, line),
                token(HttpRequestTokenMaker.INTERNAL_BODY_PLAIN));
    }

    // -----------------------------------------------------------------------
    // Form-urlencoded body
    // -----------------------------------------------------------------------

    @Test
    void shouldTokeniseFormBodyKeyValue() {
        Token tokens =
                tokenMaker.getTokenList(
                        segment("key=value"), HttpRequestTokenMaker.INTERNAL_BODY_FORM, 0);
        assertTokens(
                tokens,
                token(TokenTypes.RESERVED_WORD, "key"),
                token(TokenTypes.SEPARATOR, "="),
                token(TokenTypes.DATA_TYPE, "value"),
                token(HttpRequestTokenMaker.INTERNAL_BODY_FORM));
    }

    @Test
    void shouldTokeniseFormBodyMultiplePairs() {
        Token tokens =
                tokenMaker.getTokenList(
                        segment("a=1&b=2"), HttpRequestTokenMaker.INTERNAL_BODY_FORM, 0);
        assertTokens(
                tokens,
                token(TokenTypes.RESERVED_WORD, "a"),
                token(TokenTypes.SEPARATOR, "="),
                token(TokenTypes.DATA_TYPE, "1"),
                token(TokenTypes.VARIABLE, "&"),
                token(TokenTypes.RESERVED_WORD, "b"),
                token(TokenTypes.SEPARATOR, "="),
                token(TokenTypes.DATA_TYPE, "2"),
                token(HttpRequestTokenMaker.INTERNAL_BODY_FORM));
    }

    // -----------------------------------------------------------------------
    // JSON body
    // -----------------------------------------------------------------------

    @Test
    void shouldTokeniseJsonBodyObject() {
        Token tokens =
                tokenMaker.getTokenList(
                        segment("{\"key\": \"value\"}"),
                        HttpRequestTokenMaker.INTERNAL_BODY_JSON,
                        0);
        // Verify the chain is non-null and ends with an INTERNAL_BODY_JSON end token
        assertThat(tokens, is(notNullValue()));
        Token last = tokens;
        while (last.getNextToken() != null) {
            last = last.getNextToken();
        }
        assertThat(last.getType() <= HttpRequestTokenMaker.INTERNAL_BODY_JSON, is(true));
    }

    @Test
    void shouldTokeniseJsonBodyNumber() {
        Token tokens =
                tokenMaker.getTokenList(segment("42"), HttpRequestTokenMaker.INTERNAL_BODY_JSON, 0);
        assertThat(tokens, is(notNullValue()));
        // First token should be a number
        assertThat(tokens.getType(), is(equalTo(TokenTypes.LITERAL_NUMBER_DECIMAL_INT)));
    }

    @Test
    void shouldTokeniseJsonBodyBoolean() {
        Token tokens =
                tokenMaker.getTokenList(
                        segment("true"), HttpRequestTokenMaker.INTERNAL_BODY_JSON, 0);
        assertThat(tokens, is(notNullValue()));
        assertThat(tokens.getType(), is(equalTo(TokenTypes.LITERAL_BOOLEAN)));
    }

    // -----------------------------------------------------------------------
    // Cross-line state continuity
    // -----------------------------------------------------------------------

    @Test
    void shouldCarryPlainBodyStateAcrossLines() {
        // First body line
        Token line1 =
                tokenMaker.getTokenList(
                        segment("line one"), HttpRequestTokenMaker.INTERNAL_BODY_PLAIN, 0);
        Token endToken1 = lastToken(line1);
        assertThat(endToken1.getType(), is(equalTo(HttpRequestTokenMaker.INTERNAL_BODY_PLAIN)));

        // Second body line uses state from first line's end token
        Token line2 = tokenMaker.getTokenList(segment("line two"), endToken1.getType(), 0);
        Token endToken2 = lastToken(line2);
        assertThat(endToken2.getType(), is(equalTo(HttpRequestTokenMaker.INTERNAL_BODY_PLAIN)));
    }

    @Test
    void shouldCarryFormBodyStateAcrossLines() {
        Token line1 =
                tokenMaker.getTokenList(
                        segment("key=val"), HttpRequestTokenMaker.INTERNAL_BODY_FORM, 0);
        int nextState = lastToken(line1).getType();
        assertThat(nextState <= HttpRequestTokenMaker.INTERNAL_BODY_FORM, is(true));

        Token line2 = tokenMaker.getTokenList(segment("k2=v2"), nextState, 0);
        assertThat(
                lastToken(line2).getType() <= HttpRequestTokenMaker.INTERNAL_BODY_FORM, is(true));
    }

    @Test
    void shouldCarryJsonBodyStateAcrossLines() {
        Token line1 =
                tokenMaker.getTokenList(segment("{"), HttpRequestTokenMaker.INTERNAL_BODY_JSON, 0);
        int nextState = lastToken(line1).getType();
        assertThat(nextState <= HttpRequestTokenMaker.INTERNAL_BODY_JSON, is(true));

        // Next line should also be tokenised as JSON
        Token line2 = tokenMaker.getTokenList(segment("\"key\": 1"), nextState, 0);
        assertThat(
                lastToken(line2).getType() <= HttpRequestTokenMaker.INTERNAL_BODY_JSON, is(true));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static Token lastToken(Token first) {
        Token t = first;
        while (t.getNextToken() != null) {
            t = t.getNextToken();
        }
        return t;
    }

    private static void assertTokens(Token tokenList, TokenData... expected) {
        Token current = tokenList;
        int i = 1;
        for (TokenData exp : expected) {
            String id = "Token " + i++ + " (got: " + current + ")";
            assertThat(id, current, is(notNullValue()));
            assertThat(id, current.getType(), is(equalTo(exp.getType())));
            if (exp.getText() != null) {
                assertThat(id, current.getLexeme(), is(equalTo(exp.getText())));
            }
            current = current.getNextToken();
        }
        assertThat("Extra tokens remain after assertions", current, is(nullValue()));
    }

    private static Segment segment(String text) {
        char[] chars = text.toCharArray();
        return new Segment(chars, 0, chars.length);
    }

    private static TokenData token(int type) {
        return new TokenData(type, null);
    }

    private static TokenData token(int type, String text) {
        return new TokenData(type, text);
    }

    private static class TokenData {
        private final int type;
        private final String text;

        TokenData(int type, String text) {
            this.type = type;
            this.text = text;
        }

        int getType() {
            return type;
        }

        String getText() {
            return text;
        }
    }
}
