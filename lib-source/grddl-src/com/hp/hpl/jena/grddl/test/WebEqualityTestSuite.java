/*
  (c) Copyright 2006 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: WebEqualityTestSuite.java 1137 2007-04-13 15:06:06Z jeremy_carroll $
*/
package com.hp.hpl.jena.grddl.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestSuite;

/**
 * WebEqualityTests.
 * Subclasses of this class connect to
 * directories of test cases.
 * Each test case is a GRDDL-input = RDF/XML output.
 * @author Jeremy J. Carroll
 */
abstract class WebEqualityTestSuite   {
	final private String directoryUrl;
	private List<String> inputs = new ArrayList<String>();
	private List<String> outputs = new ArrayList<String>();
	private List<String> options = new ArrayList<String>();
	private List<String> values = new ArrayList<String>();
	
	private List<String> horridDeath = new ArrayList<String>();
	
	void mustDieHorribly(String bad) {
		horridDeath.add(bad);
	}
	void add(String in, String out) {
		add(in,out,null,null);
	}
	void add(String in, String out, String opt, String val) {
		inputs.add(directoryUrl+in);
		outputs.add(directoryUrl+out);
		options.add(opt);
		values.add(val);
	}
	public TestSuite getSuite() {
		TestSuite s = new TestSuite(directoryUrl);
		Iterator<String> ii = inputs.iterator();
		Iterator<String> oi= outputs.iterator();
		Iterator<String> opti = options.iterator();
		Iterator<String> vi = values.iterator();
		while (ii.hasNext()) {
			s.addTest(new WebEqualityTest(directoryUrl,
					ii.next(),
					oi.next(),
					opti.next(),
					vi.next()));
		}
		ii = horridDeath.iterator();
		while (ii.hasNext()) {
			s.addTest(new HorridDeathTest(directoryUrl,(String)ii.next()));
		}
		return s;
	}
	protected void addLong(String in, String out) {
		inputs.add(in);
		outputs.add(out);
		
	}
	public WebEqualityTestSuite(String directory){
		directoryUrl = directory;
//		Log4JLogger lj = (Log4JLogger)GRDDL.logger;
//		Logger ljlogger = lj.getLogger();
//		ljlogger.setLevel(Level.TRACE);
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