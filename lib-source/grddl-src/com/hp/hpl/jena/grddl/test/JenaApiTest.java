/*
 	(c) Copyright 2007 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: JenaApiTest.java 1137 2007-04-13 15:06:06Z jeremy_carroll $
*/

package com.hp.hpl.jena.grddl.test;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class JenaApiTest extends ModelTestBase
   implements RDFErrorHandler
    {
    public JenaApiTest( String name )
        { super( name ); }
    
    public void testArpOption() {
    	eCnt = 0;
    	Model m = createMemModel();
    	RDFReader r = m.getReader("GRDDL");
    	r.setErrorHandler(this);
    	r.setProperty("WARN_UNQUALIFIED_ATTRIBUTE", "EM_IGNORE");
        r.read(m, "http://jena.sourceforge.net/test/grddl/warn.html");
        assertEquals("Warning not suppressed",0,eCnt);
        Model m2 = createMemModel();
        m2.read("http://jena.sourceforge.net/test/grddl/warn.rdf",
        		"http://jena.sourceforge.net/test/grddl/warn.html",
        		"RDF/XML");
        assertIsoModels(m2, m);
    }
    public void testWarningGiven() {
    	eCnt = 0;
    	Model m = createMemModel();
    	RDFReader r = m.getReader("GRDDL");
    	r.setErrorHandler(this);
    	r.read( m, "http://jena.sourceforge.net/test/grddl/warn.html");
        assertEquals("Warning not given",1,eCnt);
        Model m2 = createMemModel();
        m2.read("http://jena.sourceforge.net/test/grddl/warn.rdf",
        		"http://jena.sourceforge.net/test/grddl/warn.html",
        		"RDF/XML");
        assertIsoModels(m2, m);
    }
    
    public void testInputStream() throws IOException {
    	InputStream is = new FileInputStream("test/basic.html");
    	Model m = createMemModel();
    	m.read(is,"file:test/basic.html","GRDDL");
    	Model m2 = createMemModel();
    	is = new FileInputStream("test/basic.rdf");
    	m2.read(is,"file:test/basic.html");
        assertIsoModels(m2, m);
    }
    public void testReader() throws IOException {
    	Reader r = new FileReader("test/basic.html");
    	Model m = createMemModel();
    	m.read(r,"file:test/basic.html","GRDDL");
    	Model m2 = createMemModel();
    	InputStream is = new FileInputStream("test/basic.rdf");
    	m2.read(is,"file:test/basic.html");
        assertIsoModels(m2, m);
    }
    private int eCnt = 0;
	public void error(Exception e) {
		eCnt++;
	}

	public void fatalError(Exception e) {
		eCnt++;
	}

	public void warning(Exception e) {
		eCnt++;
	}
    

    
    }

/*
 * (c) Copyright  2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/