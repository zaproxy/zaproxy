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
import org.fife.ui.rsyntaxtextarea.TokenMaker;
import org.fife.ui.rsyntaxtextarea.TokenMakerBase;

/**
 * Shared machinery for HTTP request and response token makers.
 *
 * <p>Provides:
 *
 * <ul>
 *   <li>{@link #tokenizeHeader} — deep-copies the header delegate's token list and translates
 *       body-signal end tokens to body internal types via the subclass-supplied {@link
 *       #translateSignal} hook.
 *   <li>{@link #tokenizePlainBody} — emits the line as an unstyled {@code IDENTIFIER} token
 *       followed by a plain-body end token; the JFlex lexer is never called.
 *   <li>{@link #tokenizeDelegate} — deep-copies a body delegate's token list and encodes the
 *       delegate's last token type into the caller-supplied body base constant.
 *   <li>{@link #addEndToken} — emits a non-paintable end token past the end of the segment,
 *       matching the convention used by JFlex-generated token makers.
 * </ul>
 */
abstract class AbstractHttpTokenMaker extends TokenMakerBase {

    /**
     * Returns the internal plain-body constant for this token maker. The value is used by {@link
     * #tokenizePlainBody} as the end-token type and by subclasses in {@link
     * #getLastTokenTypeOnLine}.
     */
    protected abstract int internalBodyPlain();

    /**
     * Translates a body-signal token type emitted by the header delegate into the token maker's own
     * internal body constant. Returns the signal unchanged if it is not a body-signal (i.e. it is a
     * header-section internal type that should be kept as-is).
     */
    protected abstract int translateSignal(int signal);

    // ------------------------------------------------------------------
    // Header delegation
    // ------------------------------------------------------------------

    /**
     * Tokenises one line belonging to the header section by delegating to {@code headerDelegate}.
     *
     * <p>The delegate's token list is deep-copied so that RSyntaxDocument's cached token list is
     * not corrupted when the delegate's pool recycles tokens on the next call. During the copy, the
     * last (end) token is passed through {@link #translateSignal} to map body-signal types to this
     * token maker's own internal body constants.
     */
    protected Token tokenizeHeader(
            Segment text, int initialTokenType, int startOffset, TokenMakerBase headerDelegate) {
        Token src = headerDelegate.getTokenList(text, initialTokenType, startOffset);

        TokenImpl head = null;
        TokenImpl tail = null;
        for (Token t = src; t != null; t = t.getNextToken()) {
            TokenImpl copy = new TokenImpl(t);
            copy.setNextToken(null);

            if (t.getNextToken() == null) {
                copy.setType(translateSignal(t.getType()));
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
            head.setType(internalBodyPlain());
        }
        return head;
    }

    // ------------------------------------------------------------------
    // Plain body
    // ------------------------------------------------------------------

    /**
     * Tokenises one plain-body line. The line is emitted as a single {@code IDENTIFIER} token
     * (visible but unstyled). The JFlex lexer is never called, so binary / Latin-1 content
     * containing Unicode line terminators (e.g. U+0085) is handled safely.
     */
    protected Token tokenizePlainBody(Segment text, int startOffset) {
        if (text.count > 0) {
            addToken(
                    text.array,
                    text.offset,
                    text.offset + text.count - 1,
                    Token.IDENTIFIER,
                    startOffset);
        }
        addEndToken(text, startOffset, internalBodyPlain());
        return firstToken;
    }

    // ------------------------------------------------------------------
    // Delegated body tokenisation
    // ------------------------------------------------------------------

    /**
     * Tokenises one body line by delegating to {@code delegate}.
     *
     * <p>The delegate receives {@code initialTokenType - bodyBase} as its own initial token type,
     * clamped to {@code Token.NULL} (0). Its token list is deep-copied and the last (end) token's
     * type is set to {@code bodyBase + delegateLastTokenType} so that the next line's {@code
     * getTokenList} call knows both the body type and the delegate's internal state.
     *
     * @param bodyBase the {@code INTERNAL_BODY_X} constant for this body type
     */
    protected Token tokenizeDelegate(
            Segment text,
            int initialTokenType,
            int startOffset,
            TokenMaker delegate,
            int bodyBase) {
        int delegateInitialType = Math.min(initialTokenType - bodyBase, Token.NULL);
        Token src = delegate.getTokenList(text, delegateInitialType, startOffset);

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
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Emits a non-paintable end token with the given internal type. The token's start index is one
     * past the end of the segment (start &gt; end), matching the convention used by JFlex-generated
     * token makers for {@code addEndToken()}.
     */
    protected void addEndToken(Segment text, int startOffset, int type) {
        int endPos = text.offset + text.count;
        addToken(text.array, endPos, endPos - 1, type, startOffset + text.count);
    }
}
