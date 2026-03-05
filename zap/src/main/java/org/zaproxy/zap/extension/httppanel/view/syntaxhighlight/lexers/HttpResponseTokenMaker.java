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
import org.fife.ui.rsyntaxtextarea.modes.CSSTokenMaker;
import org.fife.ui.rsyntaxtextarea.modes.HTMLTokenMaker;
import org.fife.ui.rsyntaxtextarea.modes.JavaScriptTokenMaker;
import org.fife.ui.rsyntaxtextarea.modes.JsonTokenMaker;
import org.fife.ui.rsyntaxtextarea.modes.XMLTokenMaker;

/**
 * Token maker for a full HTTP response (status line + headers + body).
 *
 * <p>RSyntaxTextArea calls {@link #getTokenList} once per line. This class dispatches each line to
 * the appropriate delegate based on the {@code initialTokenType} carried from the previous line:
 *
 * <ul>
 *   <li>Status line and headers → {@link HttpResponseHeaderTokenMaker}
 *   <li>HTML body → {@link HTMLTokenMaker}
 *   <li>JavaScript body → {@link JavaScriptTokenMaker}
 *   <li>JSON body → {@link HttpBodyJsonTokenMaker}
 *   <li>XML body → {@link XMLTokenMaker}
 *   <li>Plain/unrecognised body → emitted as unstyled {@code IDENTIFIER} tokens
 * </ul>
 *
 * <p>Body highlighting uses an explicit allowlist: only content types that are positively
 * identified receive syntax colouring. All other bodies are rendered as plain text without passing
 * through any lexer. This is necessary because HTTP bodies may contain arbitrary bytes (e.g.
 * Latin-1-decoded binary) that include Unicode line terminators such as U+0085, which JFlex {@code
 * %unicode} mode cannot match.
 */
public class HttpResponseTokenMaker extends TokenMakerBase {

    /* ------------------------------------------------------------------
     * Internal (negative) token types for body lines.
     * Header-section types are defined in HttpResponseHeaderTokenMaker.
     * ------------------------------------------------------------------ */

    /** Plain body line — JFlex is never invoked for these. */
    static final int INTERNAL_BODY_PLAIN = -30;

    /**
     * HTML body line. The value encodes the delegate's own last-token type: {@code
     * INTERNAL_BODY_HTML + htmlLastType}, where {@code htmlLastType <= 0}.
     */
    static final int INTERNAL_BODY_HTML = -200;

    /**
     * JavaScript body line. Encodes delegate's last-token type: {@code INTERNAL_BODY_JS +
     * jsLastType}.
     */
    static final int INTERNAL_BODY_JS = -400;

    /**
     * JSON body line. Encodes delegate's last-token type: {@code INTERNAL_BODY_JSON +
     * jsonLastType}.
     */
    static final int INTERNAL_BODY_JSON = -600;

    /**
     * XML body line. Encodes delegate's last-token type: {@code INTERNAL_BODY_XML + xmlLastType}.
     */
    static final int INTERNAL_BODY_XML = -800;

    /** Delegate for header-line tokenisation. Created lazily. */
    private HttpResponseHeaderTokenMaker headerTokenMaker;

    /** Delegate for HTML body lines. Created lazily. */
    private HTMLTokenMaker htmlTokenMaker;

    /** Delegate for JavaScript body lines. Created lazily. */
    private JavaScriptTokenMaker jsTokenMaker;

    /** Delegate for JSON body lines. Created lazily. */
    private HttpBodyJsonTokenMaker jsonTokenMaker;

    /** Delegate for XML body lines. Created lazily. */
    private XMLTokenMaker xmlTokenMaker;

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        resetTokenList();

        /* ---- HTML body ---- */
        if (initialTokenType <= INTERNAL_BODY_HTML && initialTokenType > INTERNAL_BODY_JS) {
            return tokenizeDelegate(
                    text, initialTokenType, startOffset, getHtmlTokenMaker(), INTERNAL_BODY_HTML);
        }

        /* ---- JavaScript body ---- */
        if (initialTokenType <= INTERNAL_BODY_JS && initialTokenType > INTERNAL_BODY_JSON) {
            return tokenizeDelegate(
                    text, initialTokenType, startOffset, getJsTokenMaker(), INTERNAL_BODY_JS);
        }

        /* ---- JSON body ---- */
        if (initialTokenType <= INTERNAL_BODY_JSON && initialTokenType > INTERNAL_BODY_XML) {
            return tokenizeDelegate(
                    text, initialTokenType, startOffset, getJsonTokenMaker(), INTERNAL_BODY_JSON);
        }

        /* ---- XML body ---- */
        if (initialTokenType <= INTERNAL_BODY_XML) {
            return tokenizeDelegate(
                    text, initialTokenType, startOffset, getXmlTokenMaker(), INTERNAL_BODY_XML);
        }

        /* ---- Plain body ---- */
        if (initialTokenType == INTERNAL_BODY_PLAIN) {
            return tokenizePlainBody(text, startOffset);
        }

        /* ---- Header section (status line + headers) ---- */
        return tokenizeHeader(text, initialTokenType, startOffset);
    }

    @Override
    public int getLastTokenTypeOnLine(Segment text, int initialTokenType) {
        if (initialTokenType == INTERNAL_BODY_PLAIN) {
            return INTERNAL_BODY_PLAIN;
        }
        return super.getLastTokenTypeOnLine(text, initialTokenType);
    }

    @Override
    public int getClosestStandardTokenTypeForInternalType(int type) {
        switch (type) {
            case HttpResponseHeaderTokenMaker.INTERNAL_HEADER_NAME:
            case HttpResponseHeaderTokenMaker.INTERNAL_HEADER_NAME_HTML:
            case HttpResponseHeaderTokenMaker.INTERNAL_HEADER_NAME_JS:
            case HttpResponseHeaderTokenMaker.INTERNAL_HEADER_NAME_JSON:
            case HttpResponseHeaderTokenMaker.INTERNAL_HEADER_NAME_XML:
                return Token.DATA_TYPE;
            case HttpResponseHeaderTokenMaker.INTERNAL_HEADER_VALUE:
            case HttpResponseHeaderTokenMaker.INTERNAL_HEADER_VALUE_HTML:
            case HttpResponseHeaderTokenMaker.INTERNAL_HEADER_VALUE_JS:
            case HttpResponseHeaderTokenMaker.INTERNAL_HEADER_VALUE_JSON:
            case HttpResponseHeaderTokenMaker.INTERNAL_HEADER_VALUE_XML:
            case HttpResponseHeaderTokenMaker.INTERNAL_HEADER_VALUE_CT:
                return Token.LITERAL_CHAR;
            case INTERNAL_BODY_PLAIN:
                return Token.IDENTIFIER;
            default:
                if (type <= INTERNAL_BODY_HTML) {
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
            headerTokenMaker = new HttpResponseHeaderTokenMaker();
        }
        Token src = headerTokenMaker.getTokenList(text, initialTokenType, startOffset);

        // Deep-copy the delegate's token list and translate body-signal end tokens.
        TokenImpl head = null;
        TokenImpl tail = null;
        for (Token t = src; t != null; t = t.getNextToken()) {
            TokenImpl copy = new TokenImpl(t);
            copy.setNextToken(null);

            if (t.getNextToken() == null) {
                int sig = t.getType();
                if (sig == HttpResponseHeaderTokenMaker.SIGNAL_BODY_PLAIN) {
                    copy.setType(INTERNAL_BODY_PLAIN);
                } else if (sig == HttpResponseHeaderTokenMaker.SIGNAL_BODY_HTML) {
                    copy.setType(INTERNAL_BODY_HTML);
                } else if (sig == HttpResponseHeaderTokenMaker.SIGNAL_BODY_JS) {
                    copy.setType(INTERNAL_BODY_JS);
                } else if (sig == HttpResponseHeaderTokenMaker.SIGNAL_BODY_JSON) {
                    copy.setType(INTERNAL_BODY_JSON);
                } else if (sig == HttpResponseHeaderTokenMaker.SIGNAL_BODY_XML) {
                    copy.setType(INTERNAL_BODY_XML);
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
            head = new TokenImpl();
            head.setType(INTERNAL_BODY_PLAIN);
        }
        return head;
    }

    // ------------------------------------------------------------------
    // Plain body
    // ------------------------------------------------------------------

    private Token tokenizePlainBody(Segment text, int startOffset) {
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
    // Delegated body tokenisation (HTML / JS / JSON / XML)
    // ------------------------------------------------------------------

    private Token tokenizeDelegate(
            Segment text,
            int initialTokenType,
            int startOffset,
            TokenMakerBase delegate,
            int bodyBase) {
        int delegateInitialType = Math.min(initialTokenType - bodyBase, Token.NULL);
        Token src = delegate.getTokenList(text, delegateInitialType, startOffset);

        // Deep-copy and encode the delegate's last token type back into our body base.
        TokenImpl head = null;
        TokenImpl tail = null;
        int delegateLastType = Token.NULL;
        for (Token t = src; t != null; t = t.getNextToken()) {
            TokenImpl copy = new TokenImpl(t);
            copy.setNextToken(null);
            if (head == null) {
                head = copy;
            } else {
                tail.setNextToken(copy);
            }
            tail = copy;
            delegateLastType = t.getType();
        }

        int encodedType = bodyBase + Math.min(delegateLastType, Token.NULL);
        if (tail != null) {
            tail.setType(encodedType);
        } else {
            head = new TokenImpl();
            head.setType(encodedType);
        }
        return head;
    }

    // ------------------------------------------------------------------
    // Lazy delegate accessors
    // ------------------------------------------------------------------

    private HTMLTokenMaker getHtmlTokenMaker() {
        if (htmlTokenMaker == null) {
            htmlTokenMaker = new HTMLTokenMaker();
        }
        return htmlTokenMaker;
    }

    private JavaScriptTokenMaker getJsTokenMaker() {
        if (jsTokenMaker == null) {
            jsTokenMaker = new JavaScriptTokenMaker();
        }
        return jsTokenMaker;
    }

    private HttpBodyJsonTokenMaker getJsonTokenMaker() {
        if (jsonTokenMaker == null) {
            jsonTokenMaker = new HttpBodyJsonTokenMaker();
        }
        return jsonTokenMaker;
    }

    private XMLTokenMaker getXmlTokenMaker() {
        if (xmlTokenMaker == null) {
            xmlTokenMaker = new XMLTokenMaker();
        }
        return xmlTokenMaker;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void addEndToken(Segment text, int startOffset, int type) {
        int endPos = text.offset + text.count;
        addToken(text.array, endPos, endPos - 1, type, startOffset + text.count);
    }

    @Override
    public void addToken(char[] array, int start, int end, int tokenType, int startOffset) {
        super.addToken(array, start, end, tokenType, startOffset);
    }
}
