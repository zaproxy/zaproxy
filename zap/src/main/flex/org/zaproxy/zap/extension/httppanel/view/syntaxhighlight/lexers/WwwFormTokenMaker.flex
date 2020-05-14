/*
 * This file is based on the flex files from RSyntaxTextArea.
 *
 * WwwFormTokenMaker.java - Generates tokens for HTTP request body syntax highlighting.
 * Specifically to "application/x-www-form-urlencoded" body.
 */
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.lexers;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.AbstractJFlexTokenMaker;


/**
 * A parser of HTTP request body {@code application/x-www-form-urlencoded}.
 *
 * @see <a href="http://www.w3.org/TR/html401/interact/forms.html#form-content-type">Form content type</a>
 */
%%

%public
%class WwwFormTokenMaker
%extends AbstractJFlexTokenMaker
%unicode
%type Token


%{

	/**
	 * Adds the token specified to the current linked list of tokens as an
	 * "end token;" that is, at <code>zzMarkedPos</code>.
	 *
	 * @param tokenType The token's type.
	 */
	private void addEndToken(int tokenType) {
		addToken(zzMarkedPos,zzMarkedPos, tokenType);
	}

	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 */
	private void addToken(int tokenType) {
		addToken(zzStartRead, zzMarkedPos-1, tokenType);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 */
	private void addToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param array The character array.
	 * @param start The starting offset in the array.
	 * @param end The ending offset in the array.
	 * @param tokenType The token's type.
	 * @param startOffset The offset in the document at which this token
	 *                    occurs.
	 */
	public void addToken(char[] array, int start, int end, int tokenType, int startOffset) {
		super.addToken(array, start,end, tokenType, startOffset);
		zzStartRead = zzMarkedPos;
	}


	/**
	 * Returns the first token in the linked list of tokens generated
	 * from <code>text</code>.  This method must be implemented by
	 * subclasses so they can correctly implement syntax highlighting.
	 *
	 * @param text The text from which to get tokens.
	 * @param initialTokenType The token type we should start with.
	 * @param startOffset The offset into the document at which
	 *        <code>text</code> starts.
	 * @return The first <code>Token</code> in a linked list representing
	 *         the syntax highlighted text.
	 */
	public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
		resetTokenList();
		this.offsetShift = -text.offset + startOffset;

		// Start off in the proper state.
		s = text;
		yyreset(zzReader);
		yybegin(YYINITIAL);
		return yylex();
	}


	/**
	 * Refills the input buffer.
	 *
	 * @return      <code>true</code> if EOF was reached, otherwise
	 *              <code>false</code>.
	 */
	private boolean zzRefill() {
		return zzCurrentPos>=s.offset+s.count;
	}


	/**
	 * Resets the scanner to read from a new input stream.
	 * Does not close the old reader.
	 *
	 * All internal variables are reset, the old input stream 
	 * <b>cannot</b> be reused (internal buffer is discarded and lost).
	 * Lexical state is set to <tt>YY_INITIAL</tt>.
	 *
	 * @param reader   the new input stream 
	 */
	public final void yyreset(java.io.Reader reader) {
		// 's' has been updated.
		zzBuffer = s.array;
		/*
		 * We replaced the line below with the two below it because zzRefill
		 * no longer "refills" the buffer (since the way we do it, it's always
		 * "full" the first time through, since it points to the segment's
		 * array).  So, we assign zzEndRead here.
		 */
		//zzStartRead = zzEndRead = s.offset;
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

	int errorPos = -1;

	private void handleInvalidToken() {
		if (errorPos != -1) {
			int currentZzStartRead = zzStartRead;
			addToken(errorPos, zzStartRead - 1, Token.IDENTIFIER);
			zzStartRead = currentZzStartRead;
			errorPos = -1;
		}
	}

	private void startInvalidToken() {
		if (errorPos == -1) {
			errorPos = zzMarkedPos - 1;
		}
	}

%}

name = ({uchar}+{uchar_value}*)
separator = ("=")
pair_separator = ("&")

space = ("+")+
hex = ([0-9A-Fa-f])
escape = ("%" {hex} {hex})

uchar = ([a-zA-Z0-9\-_.*] | {escape})
uchar_value = ({uchar} | {space})

%state VALUE

%%

<YYINITIAL> {
	{name} { handleInvalidToken(); addToken(Token.RESERVED_WORD); }
	{separator} { handleInvalidToken(); addToken(Token.SEPARATOR); yybegin(VALUE); }
	[^=] { startInvalidToken(); }
}

<VALUE> {
	{uchar}+ { handleInvalidToken(); addToken(Token.DATA_TYPE); }
	{space} { handleInvalidToken(); addToken(Token.COMMENT_DOCUMENTATION); }
	{pair_separator} { handleInvalidToken(); addToken(Token.VARIABLE); yybegin(YYINITIAL); }
	[^&] { startInvalidToken(); }
}

<YYINITIAL, VALUE> {
	<<EOF>> { handleInvalidToken(); addNullToken(); return firstToken; }
}

