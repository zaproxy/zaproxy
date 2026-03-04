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
import org.fife.ui.rsyntaxtextarea.TokenMakerBase;

/**
 * Token maker for a full HTTP request (request line + headers + body).
 *
 * <p>RSyntaxTextArea calls {@link #getTokenList} once per line. This class dispatches each line to
 * the appropriate delegate based on the {@code initialTokenType} carried from the previous line:
 *
 * <ul>
 *   <li>Request line and headers → {@link HttpRequestHeaderTokenMaker}
 *   <li>JSON body → {@link HttpBodyJsonTokenMaker}
 *   <li>Form-urlencoded body → form key/value rules (inline)
 *   <li>Plain/unrecognised body → emitted as unstyled {@code IDENTIFIER} tokens
 * </ul>
 *
 * <p>Body highlighting uses an explicit allowlist: only {@code application/json} and {@code
 * application/x-www-form-urlencoded} receive syntax colouring. All other bodies are rendered as
 * plain text without passing through any lexer. This is necessary because HTTP bodies may contain
 * arbitrary bytes (e.g. Latin-1-decoded binary) that include Unicode line terminators such as
 * U+0085, which JFlex {@code %unicode} mode cannot match.
 *
 * <p>To add highlighting for a new body type (e.g. XML), add a new {@code INTERNAL_BODY_*} constant
 * and a corresponding branch in {@link #getTokenList} delegating to its token maker.
 *
 * <p>Token colour mapping:
 *
 * <ul>
 *   <li>HTTP method (GET, POST …) → {@code RESERVED_WORD}
 *   <li>Request path → {@code FUNCTION}
 *   <li>HTTP version (HTTP/1.1 …) → {@code LITERAL_STRING_DOUBLE_QUOTE}
 *   <li>Header name → {@code DATA_TYPE}
 *   <li>Colon separator → {@code SEPARATOR}
 *   <li>Header value → {@code LITERAL_CHAR}
 *   <li>Plain body text → {@code IDENTIFIER} (unstyled)
 *   <li>Form key → {@code RESERVED_WORD}
 *   <li>Form {@code =} → {@code SEPARATOR}
 *   <li>Form value → {@code DATA_TYPE}
 *   <li>Form {@code &} → {@code VARIABLE}
 * </ul>
 */
public class HttpRequestTokenMaker extends TokenMakerBase {

    /* ------------------------------------------------------------------
     * Internal (negative) token types for body lines.
     * Header-section types are defined in HttpRequestHeaderTokenMaker.
     * ------------------------------------------------------------------ */

    /** Plain body line — JFlex is never invoked for these. */
    static final int INTERNAL_BODY_PLAIN = -20;

    /** Form-urlencoded body, at the start of a key. */
    static final int INTERNAL_BODY_FORM_KEY = -21;

    /** Form-urlencoded body, inside a value. */
    static final int INTERNAL_BODY_FORM_VALUE = -22;

    /**
     * JSON body line. The value encodes the delegate's own last-token type: {@code
     * INTERNAL_BODY_JSON + jsonLastType}, where {@code jsonLastType <= 0}. This allows mid-string
     * continuation highlighting across lines.
     */
    static final int INTERNAL_BODY_JSON = -100;

    /** Delegate for header-line tokenisation. Created lazily. */
    private HttpRequestHeaderTokenMaker headerTokenMaker;

    /** Delegate for JSON body lines. Created lazily. */
    private HttpBodyJsonTokenMaker jsonTokenMaker;

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        resetTokenList();

        /* ---- JSON body ---- */
        if (initialTokenType <= INTERNAL_BODY_JSON) {
            return tokenizeJsonBody(text, initialTokenType, startOffset);
        }

        /* ---- Plain body ---- */
        if (initialTokenType == INTERNAL_BODY_PLAIN) {
            return tokenizePlainBody(text, startOffset);
        }

        /* ---- Form body ---- */
        if (initialTokenType == INTERNAL_BODY_FORM_KEY
                || initialTokenType == INTERNAL_BODY_FORM_VALUE) {
            return tokenizeFormBody(text, initialTokenType, startOffset);
        }

        /* ---- Header section (request line + headers) ---- */
        return tokenizeHeader(text, initialTokenType, startOffset);
    }

    @Override
    public int getLastTokenTypeOnLine(Segment text, int initialTokenType) {
        // Short-circuit for body types that bypass the JFlex lexer, so we never
        // call getTokenList unnecessarily (and never risk feeding binary body
        // content to yylex()).
        if (initialTokenType <= INTERNAL_BODY_JSON) {
            return INTERNAL_BODY_JSON;
        }
        if (initialTokenType == INTERNAL_BODY_PLAIN) {
            return INTERNAL_BODY_PLAIN;
        }
        return super.getLastTokenTypeOnLine(text, initialTokenType);
    }

    @Override
    public int getClosestStandardTokenTypeForInternalType(int type) {
        switch (type) {
            case HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME:
            case HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME_FORM:
            case HttpRequestHeaderTokenMaker.INTERNAL_HEADER_NAME_JSON:
                return Token.DATA_TYPE;
            case HttpRequestHeaderTokenMaker.INTERNAL_HEADER_VALUE:
            case HttpRequestHeaderTokenMaker.INTERNAL_HEADER_VALUE_FORM:
            case HttpRequestHeaderTokenMaker.INTERNAL_HEADER_VALUE_JSON:
            case HttpRequestHeaderTokenMaker.INTERNAL_HEADER_VALUE_CT:
                return Token.LITERAL_CHAR;
            case INTERNAL_BODY_PLAIN:
                return Token.IDENTIFIER;
            case INTERNAL_BODY_FORM_KEY:
                return Token.RESERVED_WORD;
            case INTERNAL_BODY_FORM_VALUE:
                return Token.DATA_TYPE;
            default:
                if (type <= INTERNAL_BODY_JSON) {
                    return Token.IDENTIFIER;
                }
                return type;
        }
    }

    // ------------------------------------------------------------------
    // Header delegation
    // ------------------------------------------------------------------

    private Token tokenizeHeader(Segment text, int initialTokenType, int startOffset) {
        if (headerTokenMaker == null) {
            headerTokenMaker = new HttpRequestHeaderTokenMaker();
        }
        Token src = headerTokenMaker.getTokenList(text, initialTokenType, startOffset);

        // Deep-copy the delegate's token list so that RSyntaxDocument's cached
        // token list is not corrupted when the delegate's pool recycles tokens
        // on the next getTokenList call.
        // While copying, translate any body-signal end token to our own body
        // internal types so the next line's getTokenList enters the right branch.
        TokenImpl head = null;
        TokenImpl tail = null;
        for (Token t = src; t != null; t = t.getNextToken()) {
            TokenImpl copy = new TokenImpl(t);
            copy.setNextToken(null);

            // Translate body signal on the last (end) token
            if (t.getNextToken() == null) {
                int sig = t.getType();
                if (sig == HttpRequestHeaderTokenMaker.SIGNAL_BODY_PLAIN) {
                    copy.setType(INTERNAL_BODY_PLAIN);
                } else if (sig == HttpRequestHeaderTokenMaker.SIGNAL_BODY_FORM) {
                    copy.setType(INTERNAL_BODY_FORM_KEY);
                } else if (sig == HttpRequestHeaderTokenMaker.SIGNAL_BODY_JSON) {
                    copy.setType(INTERNAL_BODY_JSON);
                }
                // Otherwise it is a header-section internal type — keep as-is.
            }

            if (head == null) {
                head = copy;
            } else {
                tail.setNextToken(copy);
            }
            tail = copy;
        }

        if (head == null) {
            // Delegate returned null (shouldn't happen) — emit a safe end token.
            head = new TokenImpl();
            head.setType(INTERNAL_BODY_PLAIN);
        }
        return head;
    }

    // ------------------------------------------------------------------
    // Plain body
    // ------------------------------------------------------------------

    private Token tokenizePlainBody(Segment text, int startOffset) {
        // Emit the line as a plain IDENTIFIER token (visible but unstyled), then
        // an INTERNAL_BODY_PLAIN end-token to carry state to the next line.
        // The JFlex lexer is never called, so binary/Latin-1 content is safe.
        if (text.count > 0) {
            addToken(
                    text.array,
                    text.offset,
                    text.offset + text.count - 1,
                    Token.IDENTIFIER,
                    startOffset);
        }
        addEndToken(text, startOffset, INTERNAL_BODY_PLAIN);
        return firstToken;
    }

    // ------------------------------------------------------------------
    // Form-urlencoded body
    // ------------------------------------------------------------------

    private Token tokenizeFormBody(Segment text, int initialTokenType, int startOffset) {
        // Simple hand-rolled tokenizer: scan for '=' and '&' delimiters.
        // Keys: RESERVED_WORD, '=': SEPARATOR, values: DATA_TYPE, '&': VARIABLE.
        char[] array = text.array;
        int offset = text.offset;
        int end = offset + text.count;

        boolean inValue = (initialTokenType == INTERNAL_BODY_FORM_VALUE);
        int tokenStart = offset;
        int docOffset = startOffset;

        for (int i = offset; i < end; i++) {
            char c = array[i];
            if (!inValue && c == '=') {
                if (i > tokenStart) {
                    addToken(array, tokenStart, i - 1, Token.RESERVED_WORD, docOffset);
                    docOffset += i - tokenStart;
                    tokenStart = i;
                }
                addToken(array, i, i, Token.SEPARATOR, docOffset);
                docOffset++;
                tokenStart = i + 1;
                inValue = true;
            } else if (inValue && c == '&') {
                if (i > tokenStart) {
                    addToken(array, tokenStart, i - 1, Token.DATA_TYPE, docOffset);
                    docOffset += i - tokenStart;
                    tokenStart = i;
                }
                addToken(array, i, i, Token.VARIABLE, docOffset);
                docOffset++;
                tokenStart = i + 1;
                inValue = false;
            }
        }

        // Remaining content
        if (tokenStart < end) {
            addToken(
                    array,
                    tokenStart,
                    end - 1,
                    inValue ? Token.DATA_TYPE : Token.RESERVED_WORD,
                    docOffset);
        }

        addEndToken(text, startOffset, inValue ? INTERNAL_BODY_FORM_VALUE : INTERNAL_BODY_FORM_KEY);
        return firstToken;
    }

    // ------------------------------------------------------------------
    // JSON body
    // ------------------------------------------------------------------

    private Token tokenizeJsonBody(Segment text, int initialTokenType, int startOffset) {
        if (jsonTokenMaker == null) {
            jsonTokenMaker = new HttpBodyJsonTokenMaker();
        }
        int jsonInitialType = Math.min(initialTokenType - INTERNAL_BODY_JSON, Token.NULL);
        Token src = jsonTokenMaker.getTokenList(text, jsonInitialType, startOffset);

        // Deep-copy the delegate's token list so that RSyntaxDocument's cached
        // token list is not corrupted when the delegate's pool recycles tokens.
        TokenImpl head = null;
        TokenImpl tail = null;
        int jsonLastType = Token.NULL;
        for (Token t = src; t != null; t = t.getNextToken()) {
            TokenImpl copy = new TokenImpl(t);
            copy.setNextToken(null);
            if (head == null) {
                head = copy;
            } else {
                tail.setNextToken(copy);
            }
            tail = copy;
            jsonLastType = t.getType();
        }

        // Encode the delegate's last token type into INTERNAL_BODY_JSON so that
        // the next line's getTokenList knows both that it is a JSON body line and
        // what internal state the delegate left off in.
        int encodedType = INTERNAL_BODY_JSON + Math.min(jsonLastType, Token.NULL);
        if (tail != null) {
            tail.setType(encodedType);
        } else {
            head = new TokenImpl();
            head.setType(encodedType);
        }
        return head;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Emits a non-paintable end token with the given internal type. The token points past the end
     * of the segment (start > end), which is the convention used by JFlex-generated token makers
     * for addEndToken().
     */
    private void addEndToken(Segment text, int startOffset, int type) {
        int endPos = text.offset + text.count;
        addToken(text.array, endPos, endPos - 1, type, startOffset + text.count);
    }

    @Override
    public void addToken(char[] array, int start, int end, int tokenType, int startOffset) {
        super.addToken(array, start, end, tokenType, startOffset);
    }
}
