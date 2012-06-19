// $ANTLR 2.7.7 (20060930): "data.g" -> "TagValueParser.java"$

package org.spdx.tag;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

public class TagValueParser extends antlr.LLkParser       implements TagValueParserTokenTypes
 {

	TagValueBehavior behavior = null;

	public void setBehavior(TagValueBehavior b) {
		behavior = b;
	}

protected TagValueParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public TagValueParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected TagValueParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public TagValueParser(TokenStream lexer) {
  this(lexer,1);
}

public TagValueParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

	public final void data() throws RecognitionException, TokenStreamException, Exception {
		
		Token  t = null;
		
			String v;
			behavior.enter();
		
		
		try {      // for error handling
			{
			int _cnt3=0;
			_loop3:
			do {
				if ((LA(1)==TAG)) {
					t = LT(1);
					match(TAG);
					v=value();
					behavior.buildDocument(t.getText(), v);
				}
				else {
					if ( _cnt3>=1 ) { break _loop3; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt3++;
			} while (true);
			}
			behavior.exit();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
	}
	
	public final String  value() throws RecognitionException, TokenStreamException {
		String v;
		
		Token  s = null;
		
			v=null;
			StringBuffer sbuf = new StringBuffer();
		
		
		try {      // for error handling
			{
			int _cnt6=0;
			_loop6:
			do {
				if ((LA(1)==VALUE)) {
					s = LT(1);
					match(VALUE);
					sbuf.append(s.getText());
				}
				else {
					if ( _cnt6>=1 ) { break _loop6; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt6++;
			} while (true);
			}
			v=sbuf.toString();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_1);
		}
		return v;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"TAG",
		"VALUE",
		"TAG_VALUE_TOKEN",
		"MULTI_LINE_VALUE",
		"LINE_COMMENT"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 18L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	
	}
