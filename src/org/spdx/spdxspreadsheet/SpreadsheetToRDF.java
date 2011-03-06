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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.spdx.rdfparser.InvalidSPDXDocException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicense;
import org.spdx.rdfparser.SPDXPackageInfo;
import org.spdx.rdfparser.SpreadsheetException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Converts a spreadsheet to an SPDX RDF Document
 * Usage: SpreadsheetToRDF spreadsheetfile.xls rdfxmlfile.rdf 
 * where spreadsheetfile.xls is a valid SPDX Spreadsheet and 
 * rdfxmlfile.rdf is the output SPDX RDF document file.
 * @author Gary O'Neall
 *
 */
public class SpreadsheetToRDF {

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
		File spdxSpreadsheetFile = new File(args[0]);
		if (!spdxSpreadsheetFile.exists()) {
			System.out.printf("Spreadsheet file %1$s does not exists.\n", args[0]);
			return;
		}
		File spdxRdfFile = new File(args[1]);
		if (spdxRdfFile.exists()) {
			System.out.printf("Error: File %1$s already exists - please specify a new file.\n", args[1]);
			return;
		}
	
		try {
			if (!spdxRdfFile.createNewFile()) {
				System.out.println("Could not create the new SPDX RDF file "+args[1]);
				usage();
				return;
			}
		} catch (IOException e1) {
			System.out.println("Could not create the new SPDX RDF file "+args[1]);
			System.out.println("due to error "+e1.getMessage());
			usage();
			return;
		}
		FileOutputStream out;
		try {
			out = new FileOutputStream(spdxRdfFile);
		} catch (FileNotFoundException e1) {
			System.out.println("Could not write to the new SPDX RDF file "+args[1]);
			System.out.println("due to error "+e1.getMessage());
			usage();
			return;
		}
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = null;
		try {
			doc = new SPDXDocument(model);
		} catch (InvalidSPDXDocException ex) {
			System.out.print("Error creating SPDX Document: "+ex.getMessage());
			return;
		}
		SPDXSpreadsheet ss = null;
		try {
			ss = new SPDXSpreadsheet(spdxSpreadsheetFile, false, true);
			copySpreadsheetToSPDXDoc(ss, doc);
			model.write(out);
		} catch (SpreadsheetException e) {
			System.out.println("Error creating or writing to spreadsheet: "+e.getMessage());
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
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					System.out.println("Error closing RDF file: "+e.getMessage());
				}
			}
		}
	}
	
	private static void copySpreadsheetToSPDXDoc(SPDXSpreadsheet ss,
			SPDXDocument doc) throws SpreadsheetException, InvalidSPDXDocException {
		doc.createSpdxDocument(ss.getPackageInfoSheet().getPackageInfo(1).getUrl()+"#SPDXDOC");		
		copyOrigins(ss.getOriginsSheet(), doc);
		doc.createSpdxPackage();
		copyPackageInfo(ss.getPackageInfoSheet(), doc.getSpdxPackage());
		copyNonStdLicenses(ss.getNonStandardLicensesSheet(), doc);
		copyPerFileInfo(ss.getPerFileSheet(), doc.getSpdxPackage());
		copyReviewerInfo(ss.getReviewersSheet(), doc);

	}

	private static void copyReviewerInfo(ReviewersSheet reviewersSheet,
			SPDXDocument doc) throws InvalidSPDXDocException {
		int numReviewers = reviewersSheet.getNumDataRows();
		int firstRow = reviewersSheet.getFirstDataRow();
		String[] reviewers = new String[numReviewers];
		for (int i = 0; i < reviewers.length; i++) {
			reviewers[i] = reviewersSheet.getReviewer(firstRow+i) + " " + reviewersSheet.getReviewerTimestampe(firstRow+i).toGMTString();
		}
		doc.setReviewers(reviewers);
	}

	private static void copyPerFileInfo(PerFileSheet perFileSheet,
			SPDXPackage spdxPackage) throws SpreadsheetException {
		int firstRow = perFileSheet.getFirstDataRow();
		SPDXFile[] files = new SPDXFile[perFileSheet.getNumDataRows()];
		for (int i = 0; i < files.length; i++) {
			files[i] = perFileSheet.getFileInfo(firstRow+i);
		}
		spdxPackage.setFiles(files);
	}

	private static void copyNonStdLicenses(
			NonStandardLicensesSheet nonStandardLicensesSheet, SPDXDocument doc) throws InvalidSPDXDocException {
		int numNonStdLicenses = nonStandardLicensesSheet.getNumDataRows();
		int firstRow = nonStandardLicensesSheet.getFirstDataRow();
		SPDXLicense[] nonStdLicenses = new SPDXLicense[numNonStdLicenses];
		for (int i = 0; i < nonStdLicenses.length; i++) {
			nonStdLicenses[i] = new SPDXLicense(nonStandardLicensesSheet.getIdentifier(firstRow+i), 
					nonStandardLicensesSheet.getIdentifier(firstRow+i), 
					nonStandardLicensesSheet.getExtractedText(firstRow+i), null, null, null, null);
		}
		doc.setNonStandardLicenses(nonStdLicenses);
	}

	private static void copyPackageInfo(PackageInfoSheet packageInfoSheet,
			SPDXPackage spdxPackage) throws SpreadsheetException, InvalidSPDXDocException {
		SPDXPackageInfo info = packageInfoSheet.getPackageInfo(packageInfoSheet.getFirstDataRow());
		if (info == null) {
			throw(new InvalidSPDXDocException("No package info in the spreadsheet"));
		}
		spdxPackage.setDeclaredCopyright(info.getDeclaredCopyright());
		spdxPackage.setDeclaredLicenses(info.getDeclaredLicenses());
		spdxPackage.setDeclaredName(info.getDeclaredName());
		spdxPackage.setDescription(info.getDescription());
		spdxPackage.setDetectedLicenses(info.getDetectedLicenses());
		spdxPackage.setFileChecksums(info.getFileChecksum());
		spdxPackage.setFileName(info.getFileName());
		spdxPackage.setSha1(info.getSha1());
		spdxPackage.setShortDescription(info.getShortDescription());
		spdxPackage.setSourceInfo(info.getSourceInfo());
		spdxPackage.setUrl(info.getUrl());
	}

	private static void copyOrigins(OriginsSheet originsSheet, SPDXDocument doc) throws InvalidSPDXDocException {
		doc.setCreated(originsSheet.getCreated().toGMTString());
		doc.setCreatedBy(originsSheet.getCreatedBy());
		doc.setSpdxVersion(originsSheet.getSPDXVersion());
		doc.setAuthorsComments(originsSheet.getSheet());
	}

	private static void usage() {
		System.out.println("Usage: SpreadsheetToRDF spreadsheetfile.xls rdfxmlfile.rdf \n"+
				"where spreadsheetfile.xls is a valid SPDX Spreadsheet and\n"+
				"rdfxmlfile.rdf is the output SPDX RDF document file.");
	}

}
