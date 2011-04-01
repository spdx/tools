/*
  (c) Copyright 2007 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: WDTests.java 2221 2007-09-21 11:15:36Z jeremy_carroll $
*/
package com.hp.hpl.jena.grddl.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * WDTests
 * @author Jeremy J. Carroll
 */
public class WDTests {
	static final String DIR = "http://www.w3.org/TR/";
	static final String TC = DIR + "grddl-tests";
	static final String TestSchema =
	"http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#";
	static final Model m = ModelFactory.createDefaultModel();
	static final Resource TestNS = m.createResource(TestSchema);
	static final Property inputDoc = m.createProperty(TestSchema,"inputDocument");
	static final Property outputDoc = m.createProperty(TestSchema,"outputDocument");
//	static String doc2="http://www.w3.org/TR/grddl-tests";
	
	static String doc="http://www.w3.org/TR/grddl-tests";
	final private Model manifest;
	final private Model earl = ModelFactory.createDefaultModel();
	
	final private List<WDSingleTest> tests = new ArrayList<WDSingleTest>();
	
	public WDTests(Model m) {
		manifest = m;
		
//		 Create a new query
		String queryString = 
			"PREFIX t: <http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#> " +
			"SELECT ?in ?out ?test " +
			"WHERE {" +
			"      ?test t:inputDocument ?in . " +
			"      ?test t:outputDocument ?out . " +
			"      } " +
			"ORDER BY ?in";

		Query query = QueryFactory.create(queryString);

//		 Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, manifest);
		ResultSet results = qe.execSelect();
		
		WDSingleTest one = null;
		Resource last = null;
		while (results.hasNext()) {
			QuerySolution s = results.nextSolution();
			Resource in = s.getResource("in");
			Resource out = s.getResource("out");
			Resource test = s.getResource("test");
			
			Property approval = m.createProperty(
					TestSchema, "approval");
			if (!test.hasProperty(approval))
				System.out.println("#"+test.getLocalName());
			if (!in.equals(last)) {
				one = new WDSingleTest(in);
				tests.add(one);
				last = in;
			}
		    one.addOutput(out,test);
			
		}
		qe.close();

	}
	static String removeThese[] = {
	"#db-3a",
	"#db-3b",
	"#db-3c",
	"#db-3d",
	"#db-3e",
	"#db-3f",
//	"#svg-in-html-1",
//	"#svg-in-html-3",
//	"#svg-in-html-4",
//	"#svg-in-html-7",
//	"#svg-in-html-8",
//	"#svg-in-html-2",
//	"#svg-in-html-5",
//	"#svg-in-html-6",
//	"#lang-html-1",
//	"#lang-xml-1",
//	"#lang-html-2",
//	"#lang-xml-2",
//	"#lang-html-3",
//	"#lang-xml-3",
//	"#lang-html-4",
//	"#lang-xml-4",
//	"#multi-profile-1",
//	"#with-multi-profile-1",
//	"#with-multi-profile-2",
//	"#with-multi-profile-3",
//	"#with-multi-profile-4"
};
	static public void main(String args[]) {
		if (args.length>0) {
			doc = args[0];
		}
		
		
		
		load(doc);
//		load(DIR+"pendinglist.html");
//		load(DIR+"grddl-tests.html");
		for (int i=0;i<removeThese.length;i++)
		removeTest(removeThese[i]);
		// from editor's draft:
//		removeTest("#primer-hotel-data");
//		removeTest("#httpHeaders");
		// from pending:
//		removeTest("#hcard-rdfa1");
//		removeTest("#hcard-rdfa2");
//		changeOutput("#xmlbase4","xmlWithoutBase-output");
//		changeOutput("#xmlbase2","xmlWithoutBase-output");
//		changeOutput("#xmlbase3","xmlWithBase-output");
//		changeOutput("#xmlbase1","xmlWithBase-output");
		
		
		WDTests wd = new WDTests(m);
		wd.exec();
		wd.print();
	}
	private static void changeOutput(String tc, String out) {
		Resource test = m.createResource(TC+tc);
		m.removeAll(test, outputDoc, null);
		test.addProperty(outputDoc, m.createResource(DIR+out));
	}
	private static void removeTest(String string) {
		m.removeAll(m.createResource(TC+string), null, null);
	}
	private static void load(String string) {
		m.read(string,"GRDDL");
	}
	private void print() {
		earl.write(System.out,"RDF/XML-ABBREV");
	}
	private void exec() {
		Resource system = earl.createResource(EARL.Software);
		system.addProperty(RDFS.label,"Jena");
		system.addProperty(DC.title, "Jena GRDDL Reader");
		system.addProperty(DCTerms.hasVersion,"$Id: WDTests.java 2221 2007-09-21 11:15:36Z jeremy_carroll $");
		system.addProperty(FOAF.homepage, earl.createResource("http://jena.sourceforge.net/grddl/"));
        Resource assertor =	earl.createResource(EARL.CompoundAssertor);
        assertor.addProperty(EARL.mainAssertor,
    		   system
    		   );
        assertor.addProperty(EARL.helpAssertor,
    		   earl.createResource(EARL.SingleAssertor)
    		     .addProperty(RDF.type, FOAF.Person)
    		     .addProperty(FOAF.name, "Jeremy J. Carroll")
    		     .addProperty(FOAF.mbox,earl.createResource("mailto:jeremy.carroll@hp.com"))
        );
        Iterator<WDSingleTest> it = tests.iterator();
        while (it.hasNext())
        	it.next().exec(earl);
        ResIterator rit = earl.listSubjectsWithProperty(RDF.type, EARL.Assertion);
		while (rit.hasNext())
			rit.nextResource().
			   addProperty(EARL.assertedBy,assertor)
			   .addProperty(EARL.subject, system);
	}
}


/*
    (c) Copyright 2007 Hewlett-Packard Development Company, LP
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