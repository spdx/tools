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
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SPDXPackageInfo;
import org.spdx.rdfparser.SPDXStandardLicense;
import org.spdx.rdfparser.SpdxRdfConstants;

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
	public static final String NOT_SUPPORTED_STRING = "This field is not yet supported by SPDX";
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
		SPDXDocument doc = null;
		try {
			doc = SPDXDocumentFactory.creatSpdxDocument(args[0]);
		} catch (InvalidSPDXAnalysisException ex) {
			System.out.print("Error creating SPDX Document: "+ex.getMessage());
			return;
		} catch (IOException e) {
			System.out.print("Unable to open file :"+args[0]+", "+e.getMessage());
		}
		SPDXSpreadsheet ss = null;
		try {
			ss = new SPDXSpreadsheet(spdxSpreadsheetFile, true, false);
			copyRdfXmlToSpreadsheet(doc, ss);
			ArrayList<String> verify = doc.verify();
			if (verify.size() > 0) {
				System.out.println("Warning: The following verifications failed for the resultant SPDX RDF file:");
				for (int i = 0; i < verify.size(); i++) {
					System.out.println("\t"+verify.get(i));
				}
			}
		} catch (SpreadsheetException e) {
			System.out.println("Error opening or writing to spreadsheet: "+e.getMessage());
		} catch (InvalidSPDXAnalysisException e) {
			System.out.println("Error translating the RDF file: "+e.getMessage());
		} catch (Exception ex) {
			System.out.println("Unexpected error translating the RDF to spreadsheet: "+ex.getMessage());
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

	public static void copyRdfXmlToSpreadsheet(SPDXDocument doc,
			SPDXSpreadsheet ss) throws InvalidSPDXAnalysisException {
		if (doc == null) {
			System.out.println("Warning: No document to copy");
			return;
		}
		copyOrigins(doc, ss.getOriginsSheet());
		copyPackageInfo(doc.getSpdxPackage(), ss.getPackageInfoSheet());
		copyNonStdLicenses(doc.getExtractedLicenseInfos(), ss.getNonStandardLicensesSheet());
		copyPerFileInfo(doc.getSpdxPackage().getFiles(), ss.getPerFileSheet());
		copyReviewerInfo(doc.getReviewers(), ss.getReviewersSheet());
	}

	private static void copyReviewerInfo(SPDXReview[] reviewers,
			ReviewersSheet reviewersSheet) throws InvalidSPDXAnalysisException {
		DateFormat dateFormat = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
		for (int i = 0; i < reviewers.length; i++) {
			String reviewerName = reviewers[i].getReviewer();
			Date reviewDate = null;
			String dateString = reviewers[i].getReviewDate();
			if (dateString != null && !dateString.isEmpty()) {
				try {
					dateString = dateString.trim();
					reviewDate = dateFormat.parse(dateString);
				} catch (Exception ex) {
					throw(new InvalidSPDXAnalysisException("Invalid reviewer date format for reviewer "+reviewers[i]));
				}
			}
			reviewersSheet.addReviewer(reviewerName, reviewDate, reviewers[i].getComment());
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
		SPDXStandardLicense dataLicense = doc.getDataLicense();
		if (dataLicense != null) {
			originsSheet.setDataLicense(dataLicense.getId());
		}
		// Author Comments
		String comments = creator.getComment();
		if (comments != null && !comments.isEmpty()) {
			originsSheet.setAuthorComments(comments);
		}
		String created = creator.getCreated();
		DateFormat dateFormat = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
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
}
