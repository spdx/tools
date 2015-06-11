//
// Copyright (c) 2011 Source Auditor Inc.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
//

// 
// @author Rana Rahal, Protecode Inc.
//

header 
{
package org.spdx.tag;
}

class TagValueParser extends Parser;

{
	TagValueBehavior behavior = null;

	public void setBehavior(TagValueBehavior b) {
		behavior = b;
	}
}

data throws Exception
{
	String v;
	behavior.enter();
}
	:	(	t:TAG v=value//s=string
			{behavior.buildDocument(t.getText(), v);}
		)+
		{behavior.exit();}
	;

value returns [String v]
{
	v=null;
	StringBuffer sbuf = new StringBuffer();
}
	:	(	s:VALUE
			{sbuf.append(s.getText());}
		)+
		{v=sbuf.toString();}
	;

class TagValueLexer extends Lexer;
options {
	filter=LINE_COMMENT;
	charVocabulary='\3'..'\377';
}

// looking for tag: value where tag: starts at beginning of line and value can span multiple lines
TAG_VALUE_TOKEN
	:  { getColumn()==1 }? ( TAG ':' ) => TAG ':'' '         {$setType(TAG);}
       |   VALUE  {$setType(VALUE);}
	;   
	
protected
TAG	:	('a'..'z'|'A'..'Z'|'0'..'9')+
	;
	
protected
VALUE : MULTI_LINE_VALUE (LINE_COMMENT!)
	  ;

protected
MULTI_LINE_VALUE
   : ( '\n'          {newline();}
		|      '\r' '\n'     {newline();}
		|      .
		)
	;
		
// Single-line comment
// TODO: Figure out a grammar which will not generate an error when there is a "#" not in the first column
//       and preserve the "#" in the token
protected
LINE_COMMENT 
	: ({ getColumn()==1 }? "#" 
		( ~('\n'|'\r') )*
        ( '\n'|'\r'('\n')? )?
	    { $setType(Token.SKIP); newline(); } ) ?
	;
	