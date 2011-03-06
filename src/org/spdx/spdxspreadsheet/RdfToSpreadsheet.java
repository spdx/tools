/**
 * Copyright (c) 2011 Source Auditor Inc.
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
package org.spdx.spdxspreadsheet;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.spdx.rdfparser.InvalidSPDXDocException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicense;
import org.spdx.rdfparser.SPDXPackageInfo;
import org.spdx.rdfparser.SpreadsheetException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

/**
 * Translates an RDF XML file to a SPDX Spreadsheet format
 * Usage: RdfToSpreadsheet rdfxmlfile.rdf spreadsheetfile.xls
 * where rdfxmlfile.rdf is a valid SPDX RDF XML file and spreadsheetfile.xls is 
 * the output SPDX spreadsheeet file.
 * @author Gary O'Neall
 *
 */
public class RdfToSpreadsheet {

	static final int MIN_ARGS = 2;
	static final int MAX_ARGS = 2;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < MIN_ARGS) {
			usage();
			return;
		}
		if (args.length > MAX_ARGS) {
			usage();
			return;
		}
		File spdxRdfFile = new File(args[0]);
		if (!spdxRdfFile.exists()) {
			System.out.printf("Error: File %1$s does not exist.\n", args[0]);
			return;
		}
		File spdxSpreadsheetFile = new File(args[1]);
		if (spdxSpreadsheetFile.exists()) {
			System.out.println("Spreadsheet file already exists - please specify a new file.");
			return;
		}
		Model model = ModelFactory.createDefaultModel();
		InputStream spdxRdfInput = FileManager.get().open(args[0]);
		if (spdxRdfInput == null) {
			System.out.printf("Error: Can not open %1$s", args[0]);
			return;
		}
		model.read(spdxRdfInput, null);
		SPDXDocument doc = null;
		try {
			doc = new SPDXDocument(model);
		} catch (InvalidSPDXDocException ex) {
			System.out.print("Error creating SPDX Document: "+ex.getMessage());
			return;
		}
		SPDXSpreadsheet ss = null;
		try {
			ss = new SPDXSpreadsheet(spdxSpreadsheetFile, true, false);
			copyRdfXmlToSpreadsheet(doc, ss);
		} catch (SpreadsheetException e) {
			System.out.println("Error opening or writing to spreadsheet: "+e.getMessage());
		} catch (InvalidSPDXDocException e) {
			System.out.println("Error translating the RDF file: "+e.getMessage());

		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (SpreadsheetException e) {
					System.out.println("Error closing spreadsheet: "+e.getMessage());
				}
			}
		}
	}

	private static void copyRdfXmlToSpreadsheet(SPDXDocument doc,
			SPDXSpreadsheet ss) throws InvalidSPDXDocException {
		if (doc == null) {
			System.out.println("Warning: No document to copy");
			return;
		}
		copyOrigins(doc, ss.getOriginsSheet());
		copyPackageInfo(doc.getSpdxPackage(), ss.getPackageInfoSheet());
		copyNonStdLicenses(doc.getNonStandardLicenses(), ss.getNonStandardLicensesSheet());
		copyPerFileInfo(doc.getSpdxPackage().getFiles(), ss.getPerFileSheet());
		copyReviewerInfo(doc.getReviewers(), ss.getReviewersSheet());
	}

	private static void copyReviewerInfo(String[] reviewers,
			ReviewersSheet reviewersSheet) {
		for (int i = 0; i < reviewers.length; i++) {
			reviewersSheet.addReviewer(reviewers[i], Calendar.getInstance().getTime());
		}
		//TODO: Replace the reviewers time with the actual time
	}

	private static void copyPerFileInfo(SPDXFile[] files,
			PerFileSheet perFileSheet) {
		for (int i = 0; i < files.length; i++) {
			perFileSheet.add(files[i]);
		}
	}

	private static void copyNonStdLicenses(SPDXLicense[] nonStandardLicenses,
			NonStandardLicensesSheet nonStandardLicensesSheet) {
		for(int i = 0; i < nonStandardLicenses.length; i++) {
			nonStandardLicensesSheet.add(nonStandardLicenses[i].getId(), nonStandardLicenses[i].getText());
		}
	}

	private static void copyPackageInfo(SPDXPackage spdxPackage,
			PackageInfoSheet packageInfoSheet) throws InvalidSPDXDocException {
		SPDXPackageInfo pkgInfo = spdxPackage.getPackageInfo();
		packageInfoSheet.add(pkgInfo);
	}

	private static void copyOrigins(SPDXDocument doc, OriginsSheet originsSheet) throws InvalidSPDXDocException {
		// SPDX Version
		originsSheet.setSPDXVersion(doc.getSpdxVersion());
		// Created by
		originsSheet.setCreatedBy(doc.getCreatedBy());
		// Data license
		originsSheet.setDataLicense("This field is not yet supported by SPDX");
		// Author Comments
		originsSheet.setAuthorComments("This field is not yet supported by SPDX");
		String created = doc.getCreated();
		if (created.endsWith("GMT")) {
			created = created.substring(0, created.length()-4);
		}
		DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");	//TODO: implment the correct
		try {
			originsSheet.setCreated(dateFormat.parse(created));
		} catch (ParseException e) {
			throw(new InvalidSPDXDocException("Invalid created date - unable to parse"));
		}
		
	}

	private static void usage() {
		System.out.println("Usage: RdfToSpreadsheet rdfxmlfile.rdf spreadsheetfile.xls\n"+
				"where rdfxmlfile.rdf is a valid SPDX RDF XML file and spreadsheetfile.xls is\n"+
				"the output SPDX spreadsheeet file.");
	}

}
