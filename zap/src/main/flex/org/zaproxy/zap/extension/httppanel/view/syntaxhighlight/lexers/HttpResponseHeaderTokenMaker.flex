/*
 * This file is based on the flex files from RSyntaxTextArea.
 *
 * HttpResponseHeaderTokenMaker.flex - Tokenises the status line and headers of an
 * HTTP response.  Used as a delegate by HttpResponseTokenMaker.
 *
 * IMPORTANT: RSyntaxTextArea calls getTokenList() ONE LINE AT A TIME.
 * State across lines is communicated via the initialTokenType parameter and addEndToken().
 * All internal token types are negative so that WrappedSyntaxView.isPaintable() returns
 * false for them, preventing BadLocationException during mouse drag selection.
 *
 * Internal token type → meaning:
 *   INTERNAL_HEADER_NAME       → next line is a header name; body type not yet known
 *   INTERNAL_HEADER_NAME_HTML  → next line is a header name; HTML body detected
 *   INTERNAL_HEADER_NAME_JS    → next line is a header name; JavaScript body detected
 *   INTERNAL_HEADER_NAME_JSON  → next line is a header name; JSON body detected
 *   INTERNAL_HEADER_NAME_XML   → next line is a header name; XML body detected
 *   INTERNAL_HEADER_VALUE      → continuation of a header value; body type not yet known
 *   INTERNAL_HEADER_VALUE_HTML → continuation of a header value; HTML body detected
 *   INTERNAL_HEADER_VALUE_JS   → continuation of a header value; JavaScript body detected
 *   INTERNAL_HEADER_VALUE_JSON → continuation of a header value; JSON body detected
 *   INTERNAL_HEADER_VALUE_XML  → continuation of a header value; XML body detected
 *   INTERNAL_HEADER_VALUE_CT   → continuation of a Content-Type header value
 *
 * On the blank line that separates headers from body, the EOF action emits one of:
 *   SIGNAL_BODY_PLAIN → body should be rendered as plain text
 *   SIGNAL_BODY_HTML  → body should be rendered as HTML
 *   SIGNAL_BODY_JS    → body should be rendered as JavaScript
 *   SIGNAL_BODY_JSON  → body should be rendered as JSON
 *   SIGNAL_BODY_XML   → body should be rendered as XML
 */
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.lexers;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.AbstractJFlexTokenMaker;


%%

%public
%class HttpResponseHeaderTokenMaker
%extends AbstractJFlexTokenMaker
%unicode
%type Token

%{

    /* ------------------------------------------------------------------ */
    /* Internal (negative) token types — cross-line state                  */
    /* ------------------------------------------------------------------ */

    static final int INTERNAL_HEADER_NAME       = -1;
    static final int INTERNAL_HEADER_NAME_HTML  = -2;
    static final int INTERNAL_HEADER_NAME_JS    = -3;
    static final int INTERNAL_HEADER_NAME_JSON  = -4;
    static final int INTERNAL_HEADER_NAME_XML   = -5;
    static final int INTERNAL_HEADER_VALUE      = -6;
    static final int INTERNAL_HEADER_VALUE_HTML = -7;
    static final int INTERNAL_HEADER_VALUE_JS   = -8;
    static final int INTERNAL_HEADER_VALUE_JSON = -9;
    static final int INTERNAL_HEADER_VALUE_XML  = -10;
    static final int INTERNAL_HEADER_VALUE_CT   = -11;

    /* ------------------------------------------------------------------ */
    /* Signal tokens emitted on the blank header/body separator line.      */
    /* Consumed by HttpResponseTokenMaker to select the body token maker.  */
    /* ------------------------------------------------------------------ */

    static final int SIGNAL_BODY_PLAIN = -20;
    static final int SIGNAL_BODY_HTML  = -21;
    static final int SIGNAL_BODY_JS    = -22;
    static final int SIGNAL_BODY_JSON  = -23;
    static final int SIGNAL_BODY_XML   = -24;

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
        switch (bodySignal) {
            case SIGNAL_BODY_HTML:  return INTERNAL_HEADER_NAME_HTML;
            case SIGNAL_BODY_JS:    return INTERNAL_HEADER_NAME_JS;
            case SIGNAL_BODY_JSON:  return INTERNAL_HEADER_NAME_JSON;
            case SIGNAL_BODY_XML:   return INTERNAL_HEADER_NAME_XML;
            default:                return INTERNAL_HEADER_NAME;
        }
    }

    /** Returns the internal type for "still in header value" with the current body signal. */
    private int headerValueType() {
        switch (bodySignal) {
            case SIGNAL_BODY_HTML:  return INTERNAL_HEADER_VALUE_HTML;
            case SIGNAL_BODY_JS:    return INTERNAL_HEADER_VALUE_JS;
            case SIGNAL_BODY_JSON:  return INTERNAL_HEADER_VALUE_JSON;
            case SIGNAL_BODY_XML:   return INTERNAL_HEADER_VALUE_XML;
            default:                return INTERNAL_HEADER_VALUE;
        }
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
            case INTERNAL_HEADER_NAME_HTML:
                bodySignal = SIGNAL_BODY_HTML;
                yybegin(HEADER_NAME);
                break;
            case INTERNAL_HEADER_NAME_JS:
                bodySignal = SIGNAL_BODY_JS;
                yybegin(HEADER_NAME);
                break;
            case INTERNAL_HEADER_NAME_JSON:
                bodySignal = SIGNAL_BODY_JSON;
                yybegin(HEADER_NAME);
                break;
            case INTERNAL_HEADER_NAME_XML:
                bodySignal = SIGNAL_BODY_XML;
                yybegin(HEADER_NAME);
                break;
            case INTERNAL_HEADER_VALUE:
                yybegin(HEADER_VALUE);
                break;
            case INTERNAL_HEADER_VALUE_HTML:
                bodySignal = SIGNAL_BODY_HTML;
                yybegin(HEADER_VALUE);
                break;
            case INTERNAL_HEADER_VALUE_JS:
                bodySignal = SIGNAL_BODY_JS;
                yybegin(HEADER_VALUE);
                break;
            case INTERNAL_HEADER_VALUE_JSON:
                bodySignal = SIGNAL_BODY_JSON;
                yybegin(HEADER_VALUE);
                break;
            case INTERNAL_HEADER_VALUE_XML:
                bodySignal = SIGNAL_BODY_XML;
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

HttpVersion = "HTTP/" [0-9] ("." [0-9])?

StatusCode  = [0-9]{3}

HeaderName  = [a-zA-Z][a-zA-Z0-9\-]*

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
/* YYINITIAL: status line  HTTP/1.1 200 OK                            */
/* ================================================================== */

<YYINITIAL> {

    {HttpVersion}           { addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); }

    {StatusCode}            { addToken(Token.RESERVED_WORD); }

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
        if (value.regionMatches(true, 0, "application/json", 0, 16)) {
            bodySignal = SIGNAL_BODY_JSON;
        } else if (value.regionMatches(true, 0, "application/javascript", 0, 22)
                || value.regionMatches(true, 0, "text/javascript", 0, 15)) {
            bodySignal = SIGNAL_BODY_JS;
        } else if (value.regionMatches(true, 0, "text/html", 0, 9)
                || value.regionMatches(true, 0, "application/xhtml", 0, 17)) {
            bodySignal = SIGNAL_BODY_HTML;
        } else if (value.regionMatches(true, 0, "text/xml", 0, 8)
                || value.regionMatches(true, 0, "application/xml", 0, 15)) {
            bodySignal = SIGNAL_BODY_XML;
        }
    }

    <<EOF>> {
        addEndToken(headerNameType());
        return firstToken;
    }
}
