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

import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenImpl;
import org.fife.ui.rsyntaxtextarea.modes.JsonTokenMaker;

/**
 * Token maker for JSON HTTP bodies.
 *
 * <p>Wraps {@link JsonTokenMaker} and post-processes its token list to additionally highlight
 * single-quoted string literals (non-standard but frequently seen in HTTP bodies).
 *
 * <p>{@link JsonTokenMaker} emits single-quoted strings character-by-character as {@code
 * Token.IDENTIFIER}. This class detects those runs and re-types them: as {@code Token.VARIABLE}
 * when the closing quote is followed by {@code :} (i.e. it is a key), or as {@code
 * Token.LITERAL_CHAR} when it is a value.
 */
public class HttpBodyJsonTokenMaker extends JsonTokenMaker {

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        Token first = super.getTokenList(text, initialTokenType, startOffset);
        retypeSingleQuotedStrings(text.array, first);
        return first;
    }

    /**
     * Walks the token list, finds runs of {@code IDENTIFIER} tokens that form a {@code '...'}
     * literal, and re-types them as key ({@code VARIABLE}) or value ({@code LITERAL_CHAR}).
     */
    private static void retypeSingleQuotedStrings(char[] buf, Token first) {
        Token t = first;
        while (t != null && t.isPaintable()) {
            if (t.getType() == Token.IDENTIFIER && charAt(buf, t.getTextOffset()) == '\'') {
                t = retypeSingleQuotedRun(t, buf);
            } else {
                t = t.getNextToken();
            }
        }
    }

    /**
     * Re-types a run of tokens from the opening {@code '} to the closing {@code '}, then decides
     * key vs value by checking whether the closing quote is followed by optional whitespace and
     * {@code :} in the buffer.
     *
     * @return the first token after the re-typed run
     */
    private static Token retypeSingleQuotedRun(Token start, char[] buf) {
        // Walk forward until we find the token that ends with the closing quote.
        Token t = start;
        Token last = null;
        while (t != null && t.isPaintable()) {
            int endIdx = t.getTextOffset() + t.length() - 1;
            boolean closingQuote = charAt(buf, endIdx) == '\'' && !(t == start && t.length() == 1);
            if (closingQuote) {
                last = t;
                t = t.getNextToken();
                break;
            }
            t = t.getNextToken();
        }

        if (last == null) {
            // Unclosed single-quoted string — treat as value.
            retypeRun(start, null, Token.LITERAL_CHAR);
            return t;
        }

        // Determine key vs value: scan buf after the closing quote for optional whitespace then ':'
        int afterQuote = last.getTextOffset() + last.length();
        int i = afterQuote;
        while (i < buf.length && (buf[i] == ' ' || buf[i] == '\t' || buf[i] == '\f')) i++;
        int tokenType = (i < buf.length && buf[i] == ':') ? Token.VARIABLE : Token.LITERAL_CHAR;

        retypeRun(start, last, tokenType);
        return t;
    }

    /** Sets {@code type} on every token from {@code start} up to and including {@code last}. */
    private static void retypeRun(Token start, Token last, int type) {
        Token t = start;
        while (t != null && t.isPaintable()) {
            ((TokenImpl) t).setType(type);
            if (t == last) break;
            t = t.getNextToken();
        }
    }

    private static char charAt(char[] buf, int idx) {
        return (idx >= 0 && idx < buf.length) ? buf[idx] : 0;
    }
}
