/*
  (c) Copyright 2006 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: WGTests.java 2197 2007-09-18 15:24:15Z jeremy_carroll $
*/
package com.hp.hpl.jena.grddl.test;

import junit.framework.Test;

/**
 * WGTests
 * @author Jeremy J. Carroll
 */
public class WGTests extends WebEqualityTestSuite {

	static public Test suite() {
//		System.err.println("The following tests fail at time of build:");
//		System.err.println("  testlist2.html, sq1ns.xml, sq1.xml, loop.xml, xinclude1.html");
		return new WGTests().getSuite();
	}
	public WGTests() {
//		super("http://www.w3.org/2001/sw/grddl-wg/td/");
		super("http://www.w3.org/2001/sw/grddl-wg/td/lib-tests/");
//		super("http://www.w3.org/2001/sw/grddl-wg/td/lib-tests/aa/");
//		super("http://www.w3.org/2001/sw/grddl-wg/doc29/");
//        addXHTML("svg-in-html-b");
//        addXHTML("svg-in-html-b-xr");
//        addXHTML("svg-in-html-b-xr-xe");
//        addXHTML("svg-in-html-b-xe");
		//		add("robin-hcal-grddl.html","robin-hcal-grddl.rdf");
//		add("david-erdf.html","david-erdf.rdf");
//		add("janeschedule.html","janeschedule.rdf");
//		add("janefriends.html","janefriends.rdf");
//		add("hotel-data.html","hotel-data.rdf");
//		add("hl7-sample-grddl.xml","hl7-sample.rdf");
//		addXML("baseDetail");
		add("trix3","trix-output");
//        for (int i=1; i<11;i++)
////          if (i!=7)
//		   add("inline-rdf"+i,"embedded-rdf"+i+"-output");
//        add("inline-rdf7","empty");
//		add("xhtmlWithMoreThanOneProfile",
//		     "xhtmlWithTwoTransformations-output");
//		add("xmlWithoutBase","xmlWithoutBase-output");
		
//		add("base/grddlProfileWithBaseElement",
//				"grddlProfileWithBaseElement-output");
//		add("loopx","loopx-output2");
//		add("base/xmlWithBase","xmlWithBase-output");
//		add("xmlWithBase","xmlWithBase-output");
//		add("base/xhtmlWithBaseElement","xhtmlWithBaseElement-output");
//		add("xhtmlWithBaseElement","xhtmlWithBaseElement-output");
//		add("xhtmlWithoutBaseElement","xhtmlWithoutBaseElement-output");
//		addXHTML("xhtmlWithBaseElement");
//		addXHTML("xhtmlWithoutBaseElement");
		
//		add("xhtmlProfileBase2","one");
//		add("xmlWithBase","xmlWithBase-output");
		
//		add("hCardFabien-RDFa","hCardFabien-output");
//		addXHTML("hCardFabien");


//		add("xhtmlWithMoreThanOneGrddlTransformation",
//				"xhtmlWithTwoTransformations-output");
//		add("multipleRepresentations","multipleRepresentations.rdf");
//		add("multipleRepresentations","multipleRepresentationsSvg-output.rdf","header.accept","image/svg+xml");
//		add("xinclude1.html","xinclude1.rdf","http://apache.org/xml/features/xinclude","true");
//		add("xinclude1.html","noxinclude1.rdf");
//		add("conneg.html","conneg-de.rdf","header.accept-language","de");
//		add("conneg.html","conneg-en.rdf");
//		add("conneg.html","conneg.rdf","header.negotiate","*");
	
//		add("base/xhtmlWithoutBaseElement","xhtmlWithoutBaseElement-output.rdf");
//		add("testlist2.html","testlist2.rdf");
//		add("hcard.html","hcard-output.rdf");
//		addXML("xmlWithGrddlAttribute");
//		add("projects.xml","projects.rdf");
//		addXML("atom-grddl");
//		addXML("foo");
//		addXHTML("rdf_sem");
//		add("sq1ns.xml","sq1-output.rdf");
//		add("projects.rdf","projects.rdf");
//		add("xhtmlWithMoreThanOneProfile",
//		     "xhtmlWithTwoTransformations-output");
//		add("xhtmlWithMoreThanOneGrddlTransformation",
//				"xhtmlWithTwoTransformations-output");
//		add("base/grddlProfileWithBaseElement",
//				"grddlProfileWithBaseElement-output");
//		add("xhtmlProfileBase2","one");
//		add("base/xmlWithBase","xhtmlWithBaseElement-output");
//		add("xmlWithBase","xhtmlWithBaseElement-output");
//		add("xmlWithoutBase","xhtmlWithoutBaseElement-output");
//		add("hCardFabien-RDFa","hCardFabien-output");
////		addXHTML("xhtmlWithGrddlEnabledProfileAndInBodyTransform");
//		addXHTML("hCardFabien");
//		addXML("sq1");
//		addXML("sq2");
//		addXML("loop");
//		add("grddlonrdf-xmlmediatype.rdf","grddlonrdf-xmlmediatype-output3.rdf");
//		add("grddlonrdf.rdf","grddlonrdf-output3.rdf");
//		add("baseURI.html","baseURI.rdf");
//		addLong("http://lists.w3.org/Archives/Public/public-grddl-wg/2007Mar/att-0030/inline.html",
//				"http://lists.w3.org/Archives/Public/public-grddl-wg/2007Mar/att-0030/inline.rdf" );
////		add("litres.xml","litres.xml");
		
//		addXHTML("profile-with-spaces-in-rel");
//		addXHTML("profile-has-spaces-in-rel");
//		addXML("embedded-rdf1");
//		addXML("embedded-rdf2");
//		addXML("embedded-rdf3");
		
	}

	@SuppressWarnings("unused")
	private void addXML(String s) {
		add(s+".xml",s+"-output.rdf");
	}
	@SuppressWarnings("unused")
	private void addXHTML(String s) {
		add(s+".html",s+"-output.rdf");
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