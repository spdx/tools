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

public class GrddlSpecificTest extends ModelTestBase
    {
    public GrddlSpecificTest( String name )
        { super( name ); }
    
    public void testRDFa() {
    	Model m = createMemModel();
    	RDFReader r = m.getReader("GRDDL");
    	r.setProperty("grddl.rdfa", "true");
        r.read(m, "http://www.w3.org/2001/sw/grddl-wg/td/hCardFabien-RDFa.html");
        Model m2 = createMemModel();
        m2.read("http://www.w3.org/2001/sw/grddl-wg/td/hCardFabien-output.rdf",
        		"http://www.w3.org/2001/sw/grddl-wg/td/hCardFabien-RDFa.html",
        		"RDF/XML");
        assertIsoModels(m2, m);
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