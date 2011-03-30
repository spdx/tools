/*
  (c) Copyright 2007 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: WDSingleTest.java 1393 2007-05-25 12:21:58Z jeremy_carroll $
*/
package com.hp.hpl.jena.grddl.test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * WDSingleTest
 * @author Jeremy J. Carroll
 */
public class WDSingleTest {
    static final int MAX = 10;
	final Resource input;
	final Resource outputs[] = new Resource[MAX];
	final Resource tests[] = new Resource[MAX];
	int outCount = 0;
	
	public WDSingleTest(Resource in) {
		input = in;
	}

	public void addOutput(Resource out, Resource test) {
		outputs[outCount] = out;
		tests[outCount] = test;
		outCount++;
	}

	public void exec(Model earl) {
		try {
            String prop = null;
            String val = null;
			String base = input.getURI();
			System.err.println("exec("+base+")");
			String last = base.substring(base.lastIndexOf('/')+1);
			
			if (last.startsWith("xinclude")) {
				prop = "http://apache.org/xml/features/xinclude";
				val =	"true";
			}
			if (last.startsWith("multipleRepresentations")) {
				prop = "header.accept";
				val = "image/svg+xml";
			}
			if (last.startsWith("conneg.html")) {
				prop = "header.accept-language";
				val = "de";
			}
//        System.err.print("+");
//        System.err.flush();
		Model grddl[] = new Model[prop==null?2:3];
		for (int i=0;i<grddl.length;i++) {
		  grddl[i] = ModelFactory.createDefaultModel();
		  RDFReader rdr = grddl[i].getReader("GRDDL");
		  switch (i) {
		  case 1:
			  rdr.setProperty("header.negotiate", "*");
			  break;
		  case 2:
			  rdr.setProperty(prop, val);
			  break;
		  }
          rdr.read(grddl[i], base);
		}
		int matched = 0, used = 0;
		for (int i=0; i<outCount;i++) {
			Model out = ModelFactory.createDefaultModel();
			out.read(outputs[i].getURI(),base,"RDF/XML");
			for (int j=0; j<grddl.length;j++)
			 if (grddl[j].isIsomorphicWith(out)) {
				 matched |= (1<<i);
				 used |= (1<<j);
			 }
		}
		String info = null;
		if (used +1 != (1<<grddl.length) )
			info = "Other GRDDL results found";
		Resource badResult = matched==0?EARL.fail:EARL.notApplicable;
		for (int i=0; i<outCount;i++)
			earl(earl,i,((matched>>i)&1)==1?EARL.pass:badResult,info);
		if (info != null || matched+1 != (1<<outCount)) {
			System.err.println("Reading: "+base);
			if (info!=null) {
				System.err.print("Unused:");
				if ((used&1)==0)
					System.err.print(" normal");
				if ((used&2)==0)
					System.err.print(" tcn");
				if (prop!=null && (used&4)==0) 
					System.err.print(" "+prop+"=\""+val+"\"");
				System.err.println();
			}
			if ( matched+1 != (1<<outCount) ) {
				System.err.print(matched==0?"Failed:":"N/A: ");
				for (int i=0;i<outCount; i++) {
					if ((matched&(1<<i))==0) {
						System.err.print(" #"+tests[i].getLocalName());
					}
				}
				System.err.println();
			}
				
		}
		}
		catch (RuntimeException e) {
			String exceptionDetail = "Exception: "+e.toString()+ ": "+ e.getMessage();
			System.err.println(exceptionDetail);
			exceptionEarl(earl,exceptionDetail);
		}
	}

	private void exceptionEarl(Model earl, String exceptionDetail) {
		for (int i=0;i<outCount;i++)
			earl(earl, i, EARL.fail, exceptionDetail);
	}
	private Resource testResult(Model earl, Resource pass) {
		return earl.createResource(EARL.TestResult)
		.addProperty(EARL.outcome,
				pass
		  );
	}

	private Resource earl(Model earl, int i, Resource pass, String info) {
		Resource rslt = testResult(earl, pass);
		earlAssertion(earl, 
				pass==EARL.notApplicable?EARL.heuristic:EARL.automatic, 
						rslt, i);
		if (info != null)
			rslt.addProperty(EARL.info, info);
		return rslt;
	}

	private Resource earlAssertion(Model earl, Resource mode, Resource rslt, int i) {
		return earl.createResource(EARL.Assertion).
		   addProperty(EARL.test,tests[i])
		   .addProperty(EARL.mode,
						mode 
				   )
		   .addProperty(
				EARL.result,
				rslt
//				    .addProperty(DC.date,date)
		   );
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