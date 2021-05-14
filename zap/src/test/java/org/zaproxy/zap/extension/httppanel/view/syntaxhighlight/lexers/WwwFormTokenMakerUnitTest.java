/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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

/** Unit test for {@link WwwFormTokenMaker}. */
class WwwFormTokenMakerUnitTest {

    private WwwFormTokenMaker tokenMaker = new WwwFormTokenMaker();

    @Test
    void shouldReturnNullTokenWithEmptyText() {
        // Given
        Segment text = new Segment();
        // When
        Token tokenList = tokenMaker.getTokenList(text, 0, 0);
        // Then
        assertTokens(tokenList, token(TokenTypes.NULL));
    }

    @Test
    void shouldReturnTokensFromNameValuePairs() {
        // Given
        Segment text = segment("name=value&2x+4=1+2y");
        // When
        Token tokenList = tokenMaker.getTokenList(text, 0, 0);
        // Then
        assertTokens(
                tokenList,
                token(TokenTypes.RESERVED_WORD, "name"),
                token(TokenTypes.SEPARATOR, "="),
                token(TokenTypes.DATA_TYPE, "value"),
                token(TokenTypes.VARIABLE, "&"),
                token(TokenTypes.RESERVED_WORD, "2x+4"),
                token(TokenTypes.SEPARATOR, "="),
                token(TokenTypes.DATA_TYPE, "1"),
                token(TokenTypes.COMMENT_DOCUMENTATION, "+"),
                token(TokenTypes.DATA_TYPE, "2y"),
                token(TokenTypes.NULL));
    }

    @Test
    void shouldReturnTokensFromNameValuePairsWithEncodings() {
        // Given
        Segment text = segment("x=%C3%A3&%C3%A3=y");
        // When
        Token tokenList = tokenMaker.getTokenList(text, 0, 0);
        // Then
        assertTokens(
                tokenList,
                token(TokenTypes.RESERVED_WORD, "x"),
                token(TokenTypes.SEPARATOR, "="),
                token(TokenTypes.DATA_TYPE, "%C3%A3"),
                token(TokenTypes.VARIABLE, "&"),
                token(TokenTypes.RESERVED_WORD, "%C3%A3"),
                token(TokenTypes.SEPARATOR, "="),
                token(TokenTypes.DATA_TYPE, "y"),
                token(TokenTypes.NULL));
    }

    @Test
    void shouldReturnTokensEvenIfMalformedContent() {
        // Given
        Segment text = segment("+%a%=%b%++%&%");
        // When
        Token tokenList = tokenMaker.getTokenList(text, 0, 0);
        // Then
        assertTokens(
                tokenList,
                token(TokenTypes.IDENTIFIER, "+%"),
                token(TokenTypes.RESERVED_WORD, "a"),
                token(TokenTypes.IDENTIFIER, "%"),
                token(TokenTypes.SEPARATOR, "="),
                token(TokenTypes.IDENTIFIER, "%"),
                token(TokenTypes.DATA_TYPE, "b"),
                token(TokenTypes.IDENTIFIER, "%"),
                token(TokenTypes.COMMENT_DOCUMENTATION, "++"),
                token(TokenTypes.IDENTIFIER, "%"),
                token(TokenTypes.VARIABLE, "&"),
                token(TokenTypes.IDENTIFIER, "%"),
                token(TokenTypes.NULL));
    }

    private static void assertTokens(Token tokenList, TokenData... tokens) {
        Token currentToken = tokenList;
        int i = 1;
        for (TokenData token : tokens) {
            String tokenId = "Token " + (i++) + " " + currentToken;
            assertThat(tokenId, currentToken, is(notNullValue()));
            if (token.getText() != null) {
                assertThat(tokenId, currentToken.getLexeme(), is(equalTo(token.getText())));
            }
            assertThat(tokenId, currentToken.getType(), is(equalTo(token.getType())));

            currentToken = currentToken.getNextToken();
        }
        assertThat("Not all tokens were asserted.", currentToken, is(nullValue()));
    }

    private static Segment segment(String text) {
        char[] chars = text.toCharArray();
        return new Segment(chars, 0, chars.length);
    }

    private static TokenData token(int type) {
        return token(type, null);
    }

    private static TokenData token(int type, String text) {
        return new TokenData(type, text);
    }

    private static class TokenData {
        final int type;
        final String text;

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
