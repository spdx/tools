/**
 * Copyright (c) 2011 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.spdx.spdxspreadsheet;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SPDXStandardLicense;
import org.spdx.rdfparser.SPDXPackageInfo;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import net.rootdev.javardfa.jena.RDFaReader;
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
	static Pattern datePattern = Pattern.compile(".. ... \\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d GMT$");
	
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
		
        try {
            Class.forName("net.rootdev.javardfa.jena.RDFaReader");
        } catch(java.lang.ClassNotFoundException e) {}  // do nothing

		model.read(spdxRdfInput, "http://example.com//", fileType(args[0]));
		SPDXDocument doc = null;
		try {
			doc = new SPDXDocument(model);
		} catch (InvalidSPDXAnalysisException ex) {
			System.out.print("Error creating SPDX Document: "+ex.getMessage());
			return;
		}
		SPDXSpreadsheet ss = null;
		try {
			ss = new SPDXSpreadsheet(spdxSpreadsheetFile, true, false);
			copyRdfXmlToSpreadsheet(doc, ss);
		} catch (SpreadsheetException e) {
			System.out.println("Error opening or writing to spreadsheet: "+e.getMessage());
		} catch (InvalidSPDXAnalysisException e) {
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
			SPDXSpreadsheet ss) throws InvalidSPDXAnalysisException {
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

	private static void copyReviewerInfo(SPDXReview[] reviewers,
			ReviewersSheet reviewersSheet) throws InvalidSPDXAnalysisException {
		DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");	//TODO: implement the correct
		for (int i = 0; i < reviewers.length; i++) {
			String reviewerName = reviewers[i].getReviewer();
			Date reviewDate = null;
			String dateString = reviewers[i].getReviewDate();
			if (dateString != null && !dateString.isEmpty()) {
				try {
					if (dateString.endsWith("GMT")) {
						dateString = dateString.substring(0, dateString.length()-3);
					}
					dateString = dateString.trim();
					reviewDate = dateFormat.parse(dateString);
				} catch (Exception ex) {
					throw(new InvalidSPDXAnalysisException("Invalid reviewer date format for reviewer "+reviewers[i]));
				}
			}
			reviewersSheet.addReviewer(reviewerName, reviewDate);
		}
	}

	private static void copyPerFileInfo(SPDXFile[] files,
			PerFileSheet perFileSheet) {
		for (int i = 0; i < files.length; i++) {
			perFileSheet.add(files[i]);
		}
	}

	private static void copyNonStdLicenses(SPDXNonStandardLicense[] nonStandardLicenses,
			NonStandardLicensesSheet nonStandardLicensesSheet) {
		for(int i = 0; i < nonStandardLicenses.length; i++) {
			nonStandardLicensesSheet.add(nonStandardLicenses[i].getId(), nonStandardLicenses[i].getText());
		}
	}

	private static void copyPackageInfo(SPDXPackage spdxPackage,
			PackageInfoSheet packageInfoSheet) throws InvalidSPDXAnalysisException {
		SPDXPackageInfo pkgInfo = spdxPackage.getPackageInfo();
		packageInfoSheet.add(pkgInfo);
	}

	private static void copyOrigins(SPDXDocument doc, OriginsSheet originsSheet) throws InvalidSPDXAnalysisException {
		// SPDX Version
		originsSheet.setSPDXVersion(doc.getSpdxVersion());
		// Created by
		SPDXCreatorInformation creator = doc.getCreatorInfo();
		String[] createdBys = creator.getCreators();
		originsSheet.setCreatedBy(createdBys);
		// Data license
		originsSheet.setDataLicense("This field is not yet supported by SPDX");
		// Author Comments
		String comments = creator.getComment();
		if (comments != null && !comments.isEmpty()) {
			originsSheet.setAuthorComments(comments);
		}
		String created = creator.getCreated();
		DateFormat dateFormat = new SimpleDateFormat(SPDXDocument.SPDX_DATE_FORMAT);	
		try {
			originsSheet.setCreated(dateFormat.parse(created));
		} catch (ParseException e) {
			throw(new InvalidSPDXAnalysisException("Invalid created date - unable to parse"));
		}
		
	}

	private static void usage() {
		System.out.println("Usage: RdfToSpreadsheet rdfxmlfile.rdf spreadsheetfile.xls\n"+
				"where rdfxmlfile.rdf is a valid SPDX RDF XML file and spreadsheetfile.xls is\n"+
				"the output SPDX spreadsheeet file.");
	}
	
    private static String fileType(String path) {
        if (Pattern.matches("(?i:.*\\.x?html?$)", path))
            return "HTML";
        else
            return "RDF/XML";
    }
}
