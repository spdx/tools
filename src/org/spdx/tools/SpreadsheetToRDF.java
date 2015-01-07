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
package org.spdx.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.SPDXPackageInfo;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.SpdxVerificationHelper;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;
import org.spdx.spdxspreadsheet.NonStandardLicensesSheet;
import org.spdx.spdxspreadsheet.OriginsSheet;
import org.spdx.spdxspreadsheet.PackageInfoSheet;
import org.spdx.spdxspreadsheet.PerFileSheet;
import org.spdx.spdxspreadsheet.ReviewersSheet;
import org.spdx.spdxspreadsheet.SPDXSpreadsheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Converts a spreadsheet to an SPDX RDF Analysis file
 * Usage: SpreadsheetToRDF spreadsheetfile.xls rdfxmlfile.rdf 
 * where spreadsheetfile.xls is a valid SPDX Spreadsheet and 
 * rdfxmlfile.rdf is the output SPDX RDF Analysis file.
 * @author Gary O'Neall
 *
 */
public class SpreadsheetToRDF {

	static final Logger logger = Logger.getLogger(SpreadsheetToRDF.class.getName());
	static final int MIN_ARGS = 2;
	static final int MAX_ARGS = 2;
	static DateFormat format = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
	
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
		SPDXDocument analysis = null;
		try {
			analysis = new SPDXDocument(model);
		} catch (InvalidSPDXAnalysisException ex) {
			System.out.print("Error creating SPDX Analysis: "+ex.getMessage());
			try {
				out.close();
			} catch (IOException e) {
				System.out.println("Warning - unable to close output file on error: "+e.getMessage());
			}
			return;
		}
		SPDXSpreadsheet ss = null;
		try {
			ss = new SPDXSpreadsheet(spdxSpreadsheetFile, false, true);
			copySpreadsheetToSPDXAnalysis(ss, analysis);
			ArrayList<String> verify = analysis.verify();
			if (verify.size() > 0) {
				System.out.println("Warning: The following verification errors were found in the resultant SPDX Document:");
				for (int i = 0; i < verify.size(); i++) {
					System.out.println("\t"+verify.get(i));
				}
			}
			model.write(out, "RDF/XML-ABBREV");
		} catch (SpreadsheetException e) {
			System.out.println("Error creating or writing to spreadsheet: "+e.getMessage());
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
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					System.out.println("Error closing RDF file: "+e.getMessage());
				}
			}
		}
	}
	
	public static void copySpreadsheetToSPDXAnalysis(SPDXSpreadsheet ss,
			SPDXDocument analysis) throws SpreadsheetException, InvalidSPDXAnalysisException {
		// need to create a unique URL
		// Use the download URL + "#SPDXANALYSIS"
		String pkgUrl = ss.getPackageInfoSheet().getPackageInfo(1).getUrl();
		if (!SpdxVerificationHelper.isValidUri(pkgUrl)) {
			// Since the download location is not valid, replace it with a spdx.org/tempspdxuri
			pkgUrl = "http://spdx.org/tempspdxuri";
		}
		analysis.createSpdxAnalysis(pkgUrl+"#SPDXANALYSIS");		
		copyOrigins(ss.getOriginsSheet(), analysis);
		analysis.createSpdxPackage();
		copyNonStdLicenses(ss.getNonStandardLicensesSheet(), analysis);
		// note - non std licenses must be added first so that the text is available
		copyPackageInfo(ss.getPackageInfoSheet(), analysis.getSpdxPackage());
		copyPerFileInfo(ss.getPerFileSheet(), analysis.getSpdxPackage());
		copyReviewerInfo(ss.getReviewersSheet(), analysis);

	}

	private static void copyReviewerInfo(ReviewersSheet reviewersSheet,
			SPDXDocument analysis) throws InvalidSPDXAnalysisException {
		int numReviewers = reviewersSheet.getNumDataRows();
		int firstRow = reviewersSheet.getFirstDataRow();
		SPDXReview[] reviewers = new SPDXReview[numReviewers];
		for (int i = 0; i < reviewers.length; i++) {
			reviewers[i] = new SPDXReview(reviewersSheet.getReviewer(firstRow+i), format.format(reviewersSheet.getReviewerTimestamp(firstRow+i)),
					reviewersSheet.getReviewerComment(firstRow + i));
		}
		analysis.setReviewers(reviewers);
	}

	private static void copyPerFileInfo(PerFileSheet perFileSheet,
			SPDXPackage spdxPackage) throws SpreadsheetException, InvalidSPDXAnalysisException {
		int firstRow = perFileSheet.getFirstDataRow();
		int numFiles = perFileSheet.getNumDataRows();
		for (int i = 0; i < numFiles; i++) {
			spdxPackage.addFile(perFileSheet.getFileInfo(firstRow+i));
		}
	}

	private static void copyNonStdLicenses(
			NonStandardLicensesSheet nonStandardLicensesSheet, SPDXDocument analysis) throws InvalidSPDXAnalysisException {
		int numNonStdLicenses = nonStandardLicensesSheet.getNumDataRows();
		int firstRow = nonStandardLicensesSheet.getFirstDataRow();
		ExtractedLicenseInfo[] nonStdLicenses = new ExtractedLicenseInfo[numNonStdLicenses];
		for (int i = 0; i < nonStdLicenses.length; i++) {
			nonStdLicenses[i] = new ExtractedLicenseInfo(nonStandardLicensesSheet.getIdentifier(firstRow+i), 
					nonStandardLicensesSheet.getExtractedText(firstRow+i),
					nonStandardLicensesSheet.getLicenseName(firstRow+i), 
					nonStandardLicensesSheet.getCrossRefUrls(firstRow+i),
					nonStandardLicensesSheet.getComment(firstRow+i));
		}
		analysis.setExtractedLicenseInfos(nonStdLicenses);
	}

	private static void copyPackageInfo(PackageInfoSheet packageInfoSheet,
			SPDXPackage spdxPackage) throws SpreadsheetException, InvalidSPDXAnalysisException {
		SPDXPackageInfo info = packageInfoSheet.getPackageInfo(packageInfoSheet.getFirstDataRow());
		if (info == null) {
			throw(new InvalidSPDXAnalysisException("No package info in the spreadsheet"));
		}
		spdxPackage.setDeclaredCopyright(info.getDeclaredCopyright());
		spdxPackage.setDeclaredLicense(info.getDeclaredLicenses());
		spdxPackage.setDeclaredName(info.getDeclaredName());
		spdxPackage.setDescription(info.getDescription());
		spdxPackage.setConcludedLicenses(info.getConcludedLicense());
		spdxPackage.setLicenseInfoFromFiles(info.getLicensesFromFiles());
		spdxPackage.setLicenseComment(info.getLicenseComments());
		spdxPackage.setVerificationCode(info.getPackageVerification());
		spdxPackage.setFileName(info.getFileName());
		spdxPackage.setSha1(info.getSha1());
		spdxPackage.setShortDescription(info.getShortDescription());
		spdxPackage.setSourceInfo(info.getSourceInfo());
		spdxPackage.setDownloadUrl(info.getUrl());
		if (info.getVersionInfo() != null && !info.getVersionInfo().isEmpty()) {
			spdxPackage.setVersionInfo(info.getVersionInfo());
		}
		if (info.getOriginator() != null && !info.getOriginator().isEmpty()) {
			spdxPackage.setOriginator(info.getOriginator());
		}
		if (info.getSupplier() != null && !info.getSupplier().isEmpty()) {
			spdxPackage.setSupplier(info.getSupplier());
		}
		if (info.getHomePage() != null && !info.getHomePage().isEmpty()) {
			spdxPackage.setHomePage(info.getHomePage());
		}
	}

	private static void copyOrigins(OriginsSheet originsSheet, SPDXDocument analysis) throws InvalidSPDXAnalysisException {
		Date createdDate = originsSheet.getCreated();
		String created  = format.format(createdDate);
		String[] createdBys = originsSheet.getCreatedBy();
		String creatorComment = originsSheet.getAuthorComments();
		String licenseListVersion = originsSheet.getLicenseListVersion();
		SPDXCreatorInformation creator = new SPDXCreatorInformation(createdBys, created, creatorComment, licenseListVersion);
		String specVersion = originsSheet.getSPDXVersion();
		analysis.setSpdxVersion(specVersion);
		String dataLicenseId = originsSheet.getDataLicense();
		if (dataLicenseId == null || dataLicenseId.isEmpty() || dataLicenseId.equals(RdfToSpreadsheet.NOT_SUPPORTED_STRING)) {
			if (specVersion.equals(SPDXDocument.ONE_DOT_ZERO_SPDX_VERSION)) {
				dataLicenseId = SpdxRdfConstants.SPDX_DATA_LICENSE_ID_VERSION_1_0;
			} else {
				dataLicenseId = SpdxRdfConstants.SPDX_DATA_LICENSE_ID;
			}
		}
		SpdxListedLicense dataLicense = null;
		try {
			dataLicense = (SpdxListedLicense)LicenseInfoFactory.parseSPDXLicenseString(dataLicenseId);
		} catch (Exception ex) {
			logger.warn("Unable to parse the provided standard license ID.  Using "+SpdxRdfConstants.SPDX_DATA_LICENSE_ID);
			try {
				dataLicense = (SpdxListedLicense)LicenseInfoFactory.parseSPDXLicenseString(SpdxRdfConstants.SPDX_DATA_LICENSE_ID);
			} catch (InvalidLicenseStringException e) {
				throw(new InvalidSPDXAnalysisException("Unable to get document license"));
			}
		}
		analysis.setDataLicense(dataLicense);
		analysis.setCreationInfo(creator);
		String docComment = originsSheet.getDocumentComment();
		if (docComment != null) {
			docComment.trim();
			if (!docComment.isEmpty()) {
				analysis.setDocumentComment(docComment);
			}
		}
	}

	private static void usage() {
		System.out.println("Usage: SpreadsheetToRDF spreadsheetfile.xls rdfxmlfile.rdf \n"+
				"where spreadsheetfile.xls is a valid SPDX Spreadsheet and\n"+
				"rdfxmlfile.rdf is the output SPDX RDF analysis file.");
	}

}
