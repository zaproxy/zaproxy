/*
 * This file is based on the flex files from RSyntaxTextArea.
 *
 * HttpRequestHeaderTokenMaker.flex - Tokenises the request line and headers of an
 * HTTP request.  Used as a delegate by HttpRequestTokenMaker.
 *
 * IMPORTANT: RSyntaxTextArea calls getTokenList() ONE LINE AT A TIME.
 * State across lines is communicated via the initialTokenType parameter and addEndToken().
 * All internal token types are negative so that WrappedSyntaxView.isPaintable() returns
 * false for them, preventing BadLocationException during mouse drag selection.
 *
 * Internal token type → meaning:
 *   INTERNAL_HEADER_NAME       → next line is a header name; body type not yet known
 *   INTERNAL_HEADER_NAME_FORM  → next line is a header name; form body detected
 *   INTERNAL_HEADER_NAME_JSON  → next line is a header name; JSON body detected
 *   INTERNAL_HEADER_VALUE      → continuation of a header value; body type not yet known
 *   INTERNAL_HEADER_VALUE_FORM → continuation of a header value; form body detected
 *   INTERNAL_HEADER_VALUE_JSON → continuation of a header value; JSON body detected
 *   INTERNAL_HEADER_VALUE_CT   → continuation of a Content-Type header value
 *
 * On the blank line that separates headers from body, the EOF action emits one of:
 *   SIGNAL_BODY_PLAIN → body should be rendered as plain text
 *   SIGNAL_BODY_FORM  → body should be rendered as form-urlencoded
 *   SIGNAL_BODY_JSON  → body should be rendered as JSON
 */
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.lexers;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.AbstractJFlexTokenMaker;


%%

%public
%class HttpRequestHeaderTokenMaker
%extends AbstractJFlexTokenMaker
%unicode
%type Token

%{

    /* ------------------------------------------------------------------ */
    /* Internal (negative) token types — cross-line state                  */
    /* ------------------------------------------------------------------ */

    static final int INTERNAL_HEADER_NAME       = -1;
    static final int INTERNAL_HEADER_NAME_FORM  = -2;
    static final int INTERNAL_HEADER_NAME_JSON  = -3;
    static final int INTERNAL_HEADER_VALUE      = -4;
    static final int INTERNAL_HEADER_VALUE_FORM = -5;
    static final int INTERNAL_HEADER_VALUE_JSON = -6;
    static final int INTERNAL_HEADER_VALUE_CT   = -7;

    /* ------------------------------------------------------------------ */
    /* Signal tokens emitted on the blank header/body separator line.      */
    /* Consumed by HttpRequestTokenMaker to select the body token maker.   */
    /* ------------------------------------------------------------------ */

    static final int SIGNAL_BODY_PLAIN = -10;
    static final int SIGNAL_BODY_FORM  = -11;
    static final int SIGNAL_BODY_JSON  = -12;

    /** Body type detected from Content-Type; carried as instance state only
     *  within a single yylex() call (reset at the top of getTokenList). */
    private int bodySignal = SIGNAL_BODY_PLAIN;

    private void addEndToken(int tokenType) {
        addToken(zzMarkedPos, zzMarkedPos, tokenType);
    }

    private void addToken(int tokenType) {
        addToken(zzStartRead, zzMarkedPos - 1, tokenType);
    }

    private void addToken(int start, int end, int tokenType) {
        int so = start + offsetShift;
        addToken(zzBuffer, start, end, tokenType, so);
    }

    public void addToken(char[] array, int start, int end, int tokenType, int startOffset) {
        super.addToken(array, start, end, tokenType, startOffset);
        zzStartRead = zzMarkedPos;
    }

    /** Returns the internal type for "still in header name" with the current body signal. */
    private int headerNameType() {
        if (bodySignal == SIGNAL_BODY_FORM) return INTERNAL_HEADER_NAME_FORM;
        if (bodySignal == SIGNAL_BODY_JSON) return INTERNAL_HEADER_NAME_JSON;
        return INTERNAL_HEADER_NAME;
    }

    /** Returns the internal type for "still in header value" with the current body signal. */
    private int headerValueType() {
        if (bodySignal == SIGNAL_BODY_FORM) return INTERNAL_HEADER_VALUE_FORM;
        if (bodySignal == SIGNAL_BODY_JSON) return INTERNAL_HEADER_VALUE_JSON;
        return INTERNAL_HEADER_VALUE;
    }

    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        resetTokenList();
        this.offsetShift = -text.offset + startOffset;
        bodySignal = SIGNAL_BODY_PLAIN;

        s = text;
        yyreset(zzReader);

        switch (initialTokenType) {
            case INTERNAL_HEADER_NAME:
                yybegin(HEADER_NAME);
                break;
            case INTERNAL_HEADER_NAME_FORM:
                bodySignal = SIGNAL_BODY_FORM;
                yybegin(HEADER_NAME);
                break;
            case INTERNAL_HEADER_NAME_JSON:
                bodySignal = SIGNAL_BODY_JSON;
                yybegin(HEADER_NAME);
                break;
            case INTERNAL_HEADER_VALUE:
                yybegin(HEADER_VALUE);
                break;
            case INTERNAL_HEADER_VALUE_FORM:
                bodySignal = SIGNAL_BODY_FORM;
                yybegin(HEADER_VALUE);
                break;
            case INTERNAL_HEADER_VALUE_JSON:
                bodySignal = SIGNAL_BODY_JSON;
                yybegin(HEADER_VALUE);
                break;
            case INTERNAL_HEADER_VALUE_CT:
                yybegin(HEADER_VALUE_CONTENT_TYPE);
                break;
            default:
                yybegin(YYINITIAL);
                break;
        }
        return yylex();
    }

    private boolean zzRefill() {
        return zzCurrentPos >= s.offset + s.count;
    }

    public final void yyreset(java.io.Reader reader) {
        zzBuffer = s.array;
        zzStartRead = s.offset;
        zzEndRead = zzStartRead + s.count;
        zzCurrentPos = zzMarkedPos = s.offset;
        zzLexicalState = YYINITIAL;
        zzReader = reader;
        zzAtBOL  = true;
        zzAtEOF  = false;
        zzEOFDone = false;
        zzFinalHighSurrogate = zzCurrentPos;
    }

%}

/* ------------------------------------------------------------------ */
/* Character classes                                                    */
/* ------------------------------------------------------------------ */

Whitespace  = [ \t]+

HttpMethod  = "GET" | "POST" | "PUT" | "DELETE" | "HEAD" | "OPTIONS"
            | "PATCH" | "TRACE" | "CONNECT"

HttpVersion = "HTTP/" [0-9] ("." [0-9])?

HeaderName  = [a-zA-Z][a-zA-Z0-9\-]*

ToEOL       = [^\r\n]+

/* Header value text: must not start with whitespace so that {Whitespace} can match first */
HeaderValueText = [^ \t\r\n][^\r\n]*

/* ------------------------------------------------------------------ */
/* Lexical states                                                        */
/* ------------------------------------------------------------------ */

%state HEADER_NAME
%state HEADER_NAME_CONTENT_TYPE
%state HEADER_VALUE
%state HEADER_VALUE_CONTENT_TYPE

%%

/* ================================================================== */
/* YYINITIAL: request line  GET /path HTTP/1.1                        */
/* ================================================================== */

<YYINITIAL> {

    {HttpMethod} / [ \t]    { addToken(Token.RESERVED_WORD); }

    {HttpVersion}           { addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); }

    {Whitespace}            { addToken(Token.WHITESPACE); }

    [^ \t\r\n]+             { addToken(Token.FUNCTION); }

    <<EOF>> {
        addEndToken(INTERNAL_HEADER_NAME);
        return firstToken;
    }
}

/* ================================================================== */
/* HEADER_NAME: start of a header line, up to ":"                     */
/* ================================================================== */

<HEADER_NAME> {

    /* Match header name; switch to content-type variant if it is Content-Type */
    {HeaderName}            {
        String name = new String(zzBuffer, zzStartRead, zzMarkedPos - zzStartRead);
        addToken(Token.DATA_TYPE);
        if (name.equalsIgnoreCase("Content-Type")) {
            yybegin(HEADER_NAME_CONTENT_TYPE);
        }
    }

    ":"                     {
        addToken(Token.SEPARATOR);
        yybegin(HEADER_VALUE);
    }

    {Whitespace}            { addToken(Token.WHITESPACE); }

    [^\r\n:]+               { addToken(Token.DATA_TYPE); }

    <<EOF>> {
        if (zzStartRead == zzEndRead) {
            addEndToken(bodySignal);
        } else {
            addEndToken(headerNameType());
        }
        return firstToken;
    }
}

/* ================================================================== */
/* HEADER_NAME_CONTENT_TYPE: after "Content-Type", wait for ":"       */
/* ================================================================== */

<HEADER_NAME_CONTENT_TYPE> {

    ":"                     {
        addToken(Token.SEPARATOR);
        yybegin(HEADER_VALUE_CONTENT_TYPE);
    }

    {Whitespace}            { addToken(Token.WHITESPACE); }

    [^\r\n]                 { addToken(Token.DATA_TYPE); }

    <<EOF>> {
        addEndToken(headerNameType());
        return firstToken;
    }
}

/* ================================================================== */
/* HEADER_VALUE: generic header value (non-Content-Type)              */
/* ================================================================== */

<HEADER_VALUE> {

    {Whitespace}            { addToken(Token.WHITESPACE); }

    {HeaderValueText}       { addToken(Token.LITERAL_CHAR); }

    <<EOF>> {
        addEndToken(headerNameType());
        return firstToken;
    }
}

/* ================================================================== */
/* HEADER_VALUE_CONTENT_TYPE: value of the Content-Type header        */
/* Detects body type via Java string comparison                        */
/* ================================================================== */

<HEADER_VALUE_CONTENT_TYPE> {

    {Whitespace}            { addToken(Token.WHITESPACE); }

    {HeaderValueText}       {
        String value = new String(zzBuffer, zzStartRead, zzMarkedPos - zzStartRead);
        addToken(Token.LITERAL_CHAR);
        if (value.regionMatches(true, 0, "application/x-www-form-urlencoded", 0, 33)) {
            bodySignal = SIGNAL_BODY_FORM;
        } else if (value.regionMatches(true, 0, "application/json", 0, 16)) {
            bodySignal = SIGNAL_BODY_JSON;
        }
    }

    <<EOF>> {
        addEndToken(headerNameType());
        return firstToken;
    }
}
