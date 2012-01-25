/*
 (c) Copyright 2006 Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: WebEqualityTest.java 1137 2007-04-13 15:06:06Z jeremy_carroll $
 */
package com.hp.hpl.jena.grddl.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.TestCase;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;

/**
 * WebEqualityTest
 * 
 * @author Jeremy J. Carroll
 */
class WebEqualityTest extends TestCase {
	final private String directory;

	final private String input;

	final private String output;
	final private String option;
	final private String value;

	WebEqualityTest(String d, String i, String o, String opt, String val) {
		super(shorten(d, i) + " -> " + shorten(d, o));
		directory = d;
		input = i;
		output = o;
		option = opt;
		value = val;
	}

	private static String shorten(String d, String o) {
		if (o.startsWith(d))
			return o.substring(d.length());
		return o;
	}

	private String shorten(String a) {
		return shorten(directory, a);
	}

	public void runTest() throws IOException {
		long start = System.currentTimeMillis();
		try {
		Model m1 = ModelFactory.createDefaultModel();
		Model m2 = ModelFactory.createDefaultModel();
		if (option==null)
			m1.read(input, "GRDDL");
		else {
			RDFReader rdr = m1.getReader("GRDDL");
			rdr.setProperty(option, value);
			rdr.read(m1, input);
		}
			
		URLConnection conn = new URL(output).openConnection();
		conn
				.setRequestProperty(
						"accept",
						"application/rdf+xml, application/xml; q=0.8, text/xml; q=0.7, application/rss+xml; q=0.3, */*; q=0.2");
		String encoding = conn.getContentEncoding();
		if (encoding == null)
			m2.read(conn.getInputStream(), input);
		else
			m2.read(new InputStreamReader(conn.getInputStream(), encoding),
					input);

		if (!m1.isIsomorphicWith(m2)) {   
			if ( true || m1.size() == m2.size()) {
				System.err.println("GRDDL\n=====");
				m1.difference(m2).write(System.err, "N3-TRIPLES");
				System.err.println("RDF/XML\n=======");
				m2.difference(m1).write(System.err, "N3-TRIPLES");
			}

			String fn = shorten(input);
			if (fn.indexOf('/') == -1) {
				FileOutputStream fos = new FileOutputStream("C:/temp/" + fn
						+ ".rdf");
				Writer fw = new OutputStreamWriter(fos, "utf-8");
				fw.write("<!-- Base used for output: <" + input + "> -->\r\n");
				m1.write(fw, "RDF/XML-ABBREV", input);
				fw.close();
			}
			fail("output (" + m1.size() + " triples) != input (" + m2.size()
					+ " triples)");
		}
		}
		finally {
			System.err.println("Took: "+(System.currentTimeMillis()-start));
		}

	}
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */