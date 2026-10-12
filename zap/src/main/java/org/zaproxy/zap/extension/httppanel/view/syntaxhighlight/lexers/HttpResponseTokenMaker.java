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
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMaker;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;

/**
 * Token maker for a full HTTP response (status line + headers + body).
 *
 * <p>RSyntaxTextArea calls {@link #getTokenList} once per line. This class dispatches each line to
 * the appropriate delegate based on the {@code initialTokenType} carried from the previous line:
 *
 * <ul>
 *   <li>Status line and headers → {@link HttpResponseHeaderTokenMaker}
 *   <li>HTML body → {@code HTMLTokenMaker} (via installed {@link TokenMakerFactory})
 *   <li>JavaScript body → {@code JavaScriptTokenMaker} (via installed {@link TokenMakerFactory})
 *   <li>JSON body → {@code JsonTokenMaker} (via installed {@link TokenMakerFactory})
 *   <li>XML body → {@code XMLTokenMaker} (via installed {@link TokenMakerFactory})
 *   <li>Plain/unrecognised body → emitted as unstyled {@code IDENTIFIER} tokens
 * </ul>
 *
 * <p>Body highlighting uses an explicit allowlist: only content types that are positively
 * identified receive syntax colouring. All other bodies are rendered as plain text without passing
 * through any lexer. This is necessary because HTTP bodies may contain arbitrary bytes (e.g.
 * Latin-1-decoded binary) that include Unicode line terminators such as U+0085, which JFlex {@code
 * %unicode} mode cannot match.
 */
public class HttpResponseTokenMaker extends AbstractHttpTokenMaker {

    /* ------------------------------------------------------------------
     * Internal (negative) token types for body lines.
     * Header-section types are defined in HttpResponseHeaderTokenMaker.
     * ------------------------------------------------------------------ */

    /** Plain body line — JFlex is never invoked for these. */
    static final int INTERNAL_BODY_PLAIN = -30;

    /*
     * Delegated body types use a base-plus-offset encoding: the end-token type for a body line is
     * INTERNAL_BODY_X + delegateLastTokenType, where delegateLastTokenType <= Token.NULL (0).
     * Each base must therefore be spaced at least as far apart as the maximum number of internal
     * states the corresponding delegate lexer can produce (empirically ~100 for RSTA's built-in
     * lexers). A gap of 200 is used here to leave room for future delegate changes.
     *
     * The range comparisons in getTokenList() depend on these ranges being non-overlapping and
     * all being <= INTERNAL_BODY_PLAIN (-30). Adding a new body type requires:
     *   1. A new INTERNAL_BODY_X constant spaced ≥200 below the previous one.
     *   2. A range guard in getTokenList() ordered from least (most negative) to greatest.
     *   3. A case in getClosestStandardTokenTypeForInternalType().
     */

    /**
     * HTML body line. Encodes delegate's last-token type: {@code INTERNAL_BODY_HTML +
     * htmlLastType}, where {@code htmlLastType <= 0}.
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

    /** Delegate for HTML body lines. Resolved via installed factory, lazily. */
    private TokenMaker htmlTokenMaker;

    /** Delegate for JavaScript body lines. Resolved via installed factory, lazily. */
    private TokenMaker jsTokenMaker;

    /** Delegate for JSON body lines. Resolved via installed factory, lazily. */
    private TokenMaker jsonTokenMaker;

    /** Delegate for XML body lines. Resolved via installed factory, lazily. */
    private TokenMaker xmlTokenMaker;

    @Override
    protected int internalBodyPlain() {
        return INTERNAL_BODY_PLAIN;
    }

    @Override
    protected int translateSignal(int signal) {
        if (signal == HttpResponseHeaderTokenMaker.SIGNAL_BODY_PLAIN) {
            return INTERNAL_BODY_PLAIN;
        }
        if (signal == HttpResponseHeaderTokenMaker.SIGNAL_BODY_HTML) {
            return INTERNAL_BODY_HTML;
        }
        if (signal == HttpResponseHeaderTokenMaker.SIGNAL_BODY_JS) {
            return INTERNAL_BODY_JS;
        }
        if (signal == HttpResponseHeaderTokenMaker.SIGNAL_BODY_JSON) {
            return INTERNAL_BODY_JSON;
        }
        if (signal == HttpResponseHeaderTokenMaker.SIGNAL_BODY_XML) {
            return INTERNAL_BODY_XML;
        }
        return signal;
    }

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
        if (headerTokenMaker == null) {
            headerTokenMaker = new HttpResponseHeaderTokenMaker();
        }
        return tokenizeHeader(text, initialTokenType, startOffset, headerTokenMaker);
    }

    @Override
    public int getLastTokenTypeOnLine(Segment text, int initialTokenType) {
        // Short-circuit for body types that bypass or delegate without re-entry, so we never
        // call getTokenList unnecessarily (and never risk feeding binary body content to yylex()).
        if (initialTokenType <= INTERNAL_BODY_HTML) {
            return initialTokenType;
        }
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
    // Lazy delegate accessors — resolved via installed TokenMakerFactory
    // ------------------------------------------------------------------

    private TokenMaker getHtmlTokenMaker() {
        if (htmlTokenMaker == null) {
            htmlTokenMaker =
                    TokenMakerFactory.getDefaultInstance()
                            .getTokenMaker(SyntaxConstants.SYNTAX_STYLE_HTML);
        }
        return htmlTokenMaker;
    }

    private TokenMaker getJsTokenMaker() {
        if (jsTokenMaker == null) {
            jsTokenMaker =
                    TokenMakerFactory.getDefaultInstance()
                            .getTokenMaker(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        }
        return jsTokenMaker;
    }

    private TokenMaker getJsonTokenMaker() {
        if (jsonTokenMaker == null) {
            jsonTokenMaker =
                    TokenMakerFactory.getDefaultInstance()
                            .getTokenMaker(SyntaxConstants.SYNTAX_STYLE_JSON);
        }
        return jsonTokenMaker;
    }

    private TokenMaker getXmlTokenMaker() {
        if (xmlTokenMaker == null) {
            xmlTokenMaker =
                    TokenMakerFactory.getDefaultInstance()
                            .getTokenMaker(SyntaxConstants.SYNTAX_STYLE_XML);
        }
        return xmlTokenMaker;
    }
}
