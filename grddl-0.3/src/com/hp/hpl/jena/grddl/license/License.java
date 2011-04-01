/*
 (c) Copyright 2007 Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: License.java 1137 2007-04-13 15:06:06Z jeremy_carroll $
 */
package com.hp.hpl.jena.grddl.license;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;

/**
 * The purpose of this class is to ensure
 * that a user of this software is aware of
 * the risks involved, and that they agree
 * to the BSD license terms, that under which the
 * authors of the software do not accept liability
 * for that risk.
 * 
 * @author Jeremy J. Carroll
 */
public class License {
	static private String ACCEPT = "The end-user understands the risks associated with running GRDDL software. The end-user agrees to the BSD license for the Jena GRDDL Reader.";
	

	static String software = "Jena GRRDL Reader, version 0.2";

	/**
	 * This method returns only after the user has accepted the license,
	 * aware of the risks.
	 * 
	 */
	static public void check() {
		// do nothing, all in static initializer
	}

	private File license;

	private Model model;

	private String user;

	private License() {
		if (checkSystemProperty())
			return;
		license = licenseFile();
		model = ModelFactory.createDefaultModel();
		user = System.getProperty("user.name");
		if (user.equals("root")) {
			System.err.println("Do not run this code as root.");
			System.exit(-1);
		}
		boolean ok = checkAlreadyAgreed() || checkDialog();
		if (!ok)
			System.exit(-1);
	}

	private File licenseFile() {
		File dir = HomeDir.jenaDir();
//		System.err.println(dir);
		return new File(dir, "license.rdf");
	}

	private static String getProperty(String p) {
		try {
			return System.getProperty(p);
		} catch (SecurityException e) {
			return null;
		}
	}

	private boolean checkDialog() {
		if (Contract.askUser()) {
			if (user != null) {
				model.createResource(Vocab.Agreement)
				     .addProperty(Vocab.licensee, user)
				     .addProperty(Vocab.software,software)
				     .addProperty(Vocab.licensor,
						"Hewlett-Packard Development Company, LP")
					 .addProperty(
						Vocab.agreementDate,
						model.createTypedLiteral(Calendar.getInstance()));

				try {
					FileOutputStream fos = new FileOutputStream(license);
					model.write(fos,"RDF/XML-ABBREV");
					fos.close();
				} catch (IOException e) {
					// ignore
				}
			}
			return true;
		}
		return false;
	}

	private boolean checkSystemProperty() {
		String accept = getProperty("jena.grddl.license");
		if (accept == null)
			return false;
		if (accept.equalsIgnoreCase(ACCEPT))
			return true;
		System.err
				.println("The value for 'jena.grddl.license' is inapprorpiate.");
		System.err.println("The only acceptable value is: \"" + ACCEPT + "\"");

		return false;
	}

	private static String queryString = 
		"PREFIX l: <http://jena.hpl.hp.com/2007/03/license#>"
		+ " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+ " SELECT ?date ?user ?software"
			+ " WHERE {"
			+ " ?a rdf:type l:Agreement  . "
			+ " ?a l:software ?software . "
			+ " ?a l:licensor \"Hewlett-Packard Development Company, LP\" ."
			+ " ?a l:licensee ?user . " 
			+ " _:a l:agreementDate ?date . " 
			+ " }";

	private boolean checkAlreadyAgreed() {
//		System.err.println(license);
		if (license.exists() && user != null && !user.equals("")) {
			try {
				model.read(new FileInputStream(license), null);

				QueryExecution qexec = QueryExecutionFactory.create(
						queryString, model);
				try {
					ResultSet results = qexec.execSelect();
					while (results.hasNext()) {
						QuerySolution soln = results.nextSolution();
						try {
							Literal u = soln.getLiteral("user");
//							System.err.println("user: "+u.getString());
							Literal s = soln.getLiteral("software");
//							System.err.println("software: "+s);
							Calendar d = ((XSDDateTime) soln.getLiteral("date")
									.getValue()).asCalendar();
//							System.err.println("date: "+d);
							
							if (u.getString().equals(user)
									&& s.getString().equals(software)
									&& d.before(Calendar.getInstance())) {
								return true;
							}
						} catch (JenaException e) {
							// ignore
						} catch (ClassCastException e) {
							System.err.println(e.getMessage());
						}
					}
				} finally {
					qexec.close();
				}

			} catch (FileNotFoundException e) {
			}
		}
		return false;
	}

	static public void main(String a[]) {
	}

	/* MUST BE LAST */
	static {
		new License();
	}
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP All rights
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