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
public class HttpRequestTokenMaker extends AbstractHttpTokenMaker {

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

    /*
     * Delegated body types use a base-plus-offset encoding: the end-token type for a body line is
     * INTERNAL_BODY_X + delegateLastTokenType, where delegateLastTokenType <= Token.NULL (0).
     * The base must be spaced at least as far apart as the maximum number of internal states the
     * delegate lexer can produce (empirically ~100 for RSTA's JsonTokenMaker). A gap of 100 is
     * used here; use ≥200 for any new body type added in future.
     *
     * Adding a new body type requires:
     *   1. A new INTERNAL_BODY_X constant spaced ≥(delegate max states) below the previous one.
     *   2. A range guard in getTokenList() ordered from least (most negative) to greatest.
     *   3. A case in getClosestStandardTokenTypeForInternalType().
     */

    /**
     * JSON body line. Encodes the delegate's own last-token type: {@code INTERNAL_BODY_JSON +
     * jsonLastType}, where {@code jsonLastType <= 0}. This allows mid-string continuation
     * highlighting across lines.
     */
    static final int INTERNAL_BODY_JSON = -100;

    /** Delegate for header-line tokenisation. Created lazily. */
    private HttpRequestHeaderTokenMaker headerTokenMaker;

    /** Delegate for JSON body lines. Created lazily. */
    private HttpBodyJsonTokenMaker jsonTokenMaker;

    @Override
    protected int internalBodyPlain() {
        return INTERNAL_BODY_PLAIN;
    }

    @Override
    protected int translateSignal(int signal) {
        if (signal == HttpRequestHeaderTokenMaker.SIGNAL_BODY_PLAIN) {
            return INTERNAL_BODY_PLAIN;
        }
        if (signal == HttpRequestHeaderTokenMaker.SIGNAL_BODY_FORM) {
            return INTERNAL_BODY_FORM_KEY;
        }
        if (signal == HttpRequestHeaderTokenMaker.SIGNAL_BODY_JSON) {
            return INTERNAL_BODY_JSON;
        }
        return signal;
    }

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        resetTokenList();

        /* ---- JSON body ---- */
        if (initialTokenType <= INTERNAL_BODY_JSON) {
            if (jsonTokenMaker == null) {
                jsonTokenMaker = new HttpBodyJsonTokenMaker();
            }
            return tokenizeDelegate(
                    text, initialTokenType, startOffset, jsonTokenMaker, INTERNAL_BODY_JSON);
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
        if (headerTokenMaker == null) {
            headerTokenMaker = new HttpRequestHeaderTokenMaker();
        }
        return tokenizeHeader(text, initialTokenType, startOffset, headerTokenMaker);
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
}
