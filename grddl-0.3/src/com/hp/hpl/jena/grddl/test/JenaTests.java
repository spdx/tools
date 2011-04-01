/*
  (c) Copyright 2006 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: JenaTests.java 1121 2007-04-11 15:02:58Z jeremy_carroll $
*/
package com.hp.hpl.jena.grddl.test;

import junit.framework.Test;

/**
 * WGTests
 * @author Jeremy J. Carroll
 */
public class JenaTests extends WebEqualityTestSuite {

	static public Test suite() {
		return new JenaTests().getSuite();
	}
	public JenaTests() {
		super("http://jena.sourceforge.net/test/grddl/");
//		addXHTML("warn");
		addXHTML("badhtml");
		addXHTML("xslt2");
		mustDieHorribly("security.html");
		mustDieHorribly("security2.html");
		mustDieHorribly("security3.html");
		mustDieHorribly("security4.html");
		mustDieHorribly("security5.html");
		mustDieHorribly("security6.html");
		addXHTML("permitted6");
		addXHTML("imports");
		addXHTML("includes");
		addXML("httpHeaders");
		
		
	}
	
//	private void addXML(String s) {
//		add(s+".xml",s+"-output.rdf");
//	}
	private void addXHTML(String s) {
		add(s+".html",s+".rdf");
	}

	private void addXML(String s) {
		add(s+".xml",s+".rdf");
	}
	

}


/*
    (c) Copyright 2006 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/