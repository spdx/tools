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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.SpdxVerificationHelper;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.ExternalDocumentRef;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.rdfparser.model.SpdxSnippet;
import org.spdx.spdxspreadsheet.AnnotationsSheet;
import org.spdx.spdxspreadsheet.DocumentInfoSheet;
import org.spdx.spdxspreadsheet.ExternalRefsSheet;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;
import org.spdx.spdxspreadsheet.NonStandardLicensesSheet;
import org.spdx.spdxspreadsheet.PackageInfoSheet;
import org.spdx.spdxspreadsheet.PerFileSheet;
import org.spdx.spdxspreadsheet.RelationshipsSheet;
import org.spdx.spdxspreadsheet.ReviewersSheet;
import org.spdx.spdxspreadsheet.SPDXSpreadsheet;
import org.spdx.spdxspreadsheet.SnippetSheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

import com.google.common.collect.Maps;

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
	
	private static final ThreadLocal<DateFormat> format = new ThreadLocal<DateFormat>(){
	    @Override
	    protected DateFormat initialValue() {
	        return new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
	    }
	  };
	
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

		SPDXSpreadsheet ss = null;
		try {
			ss = new SPDXSpreadsheet(spdxSpreadsheetFile, false, true);
			SpdxDocument analysis = copySpreadsheetToSPDXAnalysis(ss);
			List<String> verify = analysis.verify();
			if (verify.size() > 0) {
				System.out.println("Warning: The following verification errors were found in the resultant SPDX Document:");
				for (int i = 0; i < verify.size(); i++) {
					System.out.println("\t"+verify.get(i));
				}
			}
			analysis.getDocumentContainer().getModel().write(out, "RDF/XML-ABBREV");
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
	
	public static SpdxDocument copySpreadsheetToSPDXAnalysis(SPDXSpreadsheet ss) throws SpreadsheetException, InvalidSPDXAnalysisException {
		String pkgUrl = ss.getOriginsSheet().getNamespace() + "#" + SpdxRdfConstants.SPDX_DOCUMENT_ID;
		if (!SpdxVerificationHelper.isValidUri(pkgUrl)) {
			// need to create a unique URL
			// Use the download URL + "#SPDXANALYSIS"
			logger.warn("Missing or invalid document namespace.  Using download location URL for the document namespace");
			SpdxPackage[] pkgs = ss.getPackageInfoSheet().getPackages(null);
			if (pkgs.length > 0) {
				pkgUrl = pkgs[0].getDownloadLocation();
			}
		}
		if (!SpdxVerificationHelper.isValidUri(pkgUrl)) {
			// Since the download location is not valid, replace it with a spdx.org/tempspdxuri
			logger.warn("Missing or invalid download location.  Using temporary namespace http://spdx.org/tempspdxuri");
			pkgUrl = "http://spdx.org/tempspdxuri";
		}
		SpdxDocumentContainer container = new SpdxDocumentContainer(pkgUrl);
		SpdxDocument analysis = container.getSpdxDocument();
		copyOrigins(ss.getOriginsSheet(), analysis);
		copyNonStdLicenses(ss.getNonStandardLicensesSheet(), analysis);
		// note - non std licenses must be added first so that the text is available
		Map<String, SpdxPackage> pkgIdToPackage = copyPackageInfo(ss.getPackageInfoSheet(), ss.getExternalRefsSheet(), analysis);
		// note - packages need to be added before the files so that the files can be added to the packages
		Map<String, SpdxFile> fileIdToFile = copyPerFileInfo(ss.getPerFileSheet(), analysis, pkgIdToPackage);
		// note - files need to be added before snippets
		copyPerSnippetInfo(ss.getSnippetSheet(), analysis, fileIdToFile);
		copyAnnotationInfo(ss.getAnnotationsSheet(), analysis);
		copyRelationshipInfo(ss.getRelationshipsSheet(), analysis);
		copyReviewerInfo(ss.getReviewersSheet(), analysis);
		return analysis;
	}

	/**
	 * Copy snippet information from the spreadsheet to the analysis document
	 * @param snippetSheet
	 * @param analysis
	 * @param fileIdToFile
	 * @throws InvalidSPDXAnalysisException 
	 * @throws SpreadsheetException 
	 */
	private static void copyPerSnippetInfo(SnippetSheet snippetSheet,
			SpdxDocument analysis, Map<String, SpdxFile> fileIdToFile) throws InvalidSPDXAnalysisException, SpreadsheetException {
		int i = snippetSheet.getFirstDataRow();
		SpdxSnippet snippet = snippetSheet.getSnippet(i, analysis.getDocumentContainer());
		while (snippet != null) {
			analysis.getDocumentContainer().addElement(snippet);
			i = i + 1;
			snippet = snippetSheet.getSnippet(i, analysis.getDocumentContainer());
		}
	}

	/**
	 * @param relationshipsSheet
	 * @param analysis
	 * @throws SpreadsheetException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static void copyRelationshipInfo(
			RelationshipsSheet relationshipsSheet, SpdxDocument analysis) throws SpreadsheetException, InvalidSPDXAnalysisException {
		int i = relationshipsSheet.getFirstDataRow();
		Relationship relationship = relationshipsSheet.getRelationship(i, analysis.getDocumentContainer());
		String id = relationshipsSheet.getElmementId(i);
		while (relationship != null && id != null) {
			SpdxElement element = analysis.getDocumentContainer().findElementById(id);
			element.addRelationship(relationship);
			i = i + 1;
			relationship = relationshipsSheet.getRelationship(i, analysis.getDocumentContainer());
			id = relationshipsSheet.getElmementId(i);
		}
	}

	/**
	 * @param annotationsSheet
	 * @param analysis
	 * @throws InvalidSPDXAnalysisException 
	 * @throws SpreadsheetException 
	 */
	private static void copyAnnotationInfo(AnnotationsSheet annotationsSheet,
			SpdxDocument analysis) throws InvalidSPDXAnalysisException, SpreadsheetException {
		int i = annotationsSheet.getFirstDataRow();
		Annotation annotation = annotationsSheet.getAnnotation(i);
		String id = annotationsSheet.getElmementId(i);
		while (annotation != null && id != null) {
			SpdxElement element = analysis.getDocumentContainer().findElementById(id);
			element.addAnnotation(annotation);
			i = i + 1;
			annotation = annotationsSheet.getAnnotation(i);
			id = annotationsSheet.getElmementId(i);
		}
		
	}

	@SuppressWarnings("deprecation")
	private static void copyReviewerInfo(ReviewersSheet reviewersSheet,
			SpdxDocument analysis) throws InvalidSPDXAnalysisException {
		int numReviewers = reviewersSheet.getNumDataRows();
		int firstRow = reviewersSheet.getFirstDataRow();
		SPDXReview[] reviewers = new SPDXReview[numReviewers];
		for (int i = 0; i < reviewers.length; i++) {
			reviewers[i] = new SPDXReview(reviewersSheet.getReviewer(firstRow+i), format.get().format(reviewersSheet.getReviewerTimestamp(firstRow+i)),
					reviewersSheet.getReviewerComment(firstRow + i));
		}
		analysis.setReviewers(reviewers);
	}

	private static Map<String, SpdxFile> copyPerFileInfo(PerFileSheet perFileSheet,
			SpdxDocument analysis, Map<String, SpdxPackage> pkgIdToPackage) throws SpreadsheetException, InvalidSPDXAnalysisException {
		int firstRow = perFileSheet.getFirstDataRow();
		int numFiles = perFileSheet.getNumDataRows();
		Map<String, SpdxFile> retval = Maps.newHashMap();
		for (int i = 0; i < numFiles; i++) {
			SpdxFile file = perFileSheet.getFileInfo(firstRow+i, analysis.getDocumentContainer());
			retval.put(file.getId(), file);
			String[] pkgIds = perFileSheet.getPackageIds(firstRow+i);
			boolean fileAdded = false;
			for (int j = 0;j < pkgIds.length; j++) {
				SpdxPackage pkg = pkgIdToPackage.get(pkgIds[j]);
				if (pkg != null) {
					pkg.addFile(file);
					fileAdded = true;
				} else {
					logger.warn("Can not add file "+file.getName()+" to package "+pkgIds[j]);
				}
			}
			if (!fileAdded) {
				analysis.getDocumentContainer().addElement(file);
			}
		}
		return retval;
	}

	private static void copyNonStdLicenses(
			NonStandardLicensesSheet nonStandardLicensesSheet, SpdxDocument analysis) throws InvalidSPDXAnalysisException {
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

	private static Map<String, SpdxPackage> copyPackageInfo(PackageInfoSheet packageInfoSheet,
			ExternalRefsSheet externalRefsSheet, SpdxDocument analysis) throws SpreadsheetException, InvalidSPDXAnalysisException {
		SpdxPackage[] packages = packageInfoSheet.getPackages(analysis.getDocumentContainer());
		Map<String, SpdxPackage> pkgIdToPackage = Maps.newHashMap();
		for (int i = 0; i < packages.length; i++) {
			packages[i].setExternalRefs(externalRefsSheet.getExternalRefsForPkgid(
					packages[i].getId(), analysis.getDocumentContainer()));
			pkgIdToPackage.put(packages[i].getId(), packages[i]);
			analysis.getDocumentContainer().addElement(packages[i]);
		}
		return pkgIdToPackage;
	}

	private static void copyOrigins(DocumentInfoSheet originsSheet, SpdxDocument analysis) throws InvalidSPDXAnalysisException, SpreadsheetException {
		Date createdDate = originsSheet.getCreated();
		String created  = format.get().format(createdDate);
		String[] createdBys = originsSheet.getCreatedBy();
		String creatorComment = originsSheet.getAuthorComments();
		String licenseListVersion = originsSheet.getLicenseListVersion();
		SPDXCreatorInformation creator = new SPDXCreatorInformation(createdBys, created, creatorComment, licenseListVersion);
		String specVersion = originsSheet.getSPDXVersion();
		analysis.setSpecVersion(specVersion);
		String dataLicenseId = originsSheet.getDataLicense();
		if (dataLicenseId == null || dataLicenseId.isEmpty() || dataLicenseId.equals(RdfToSpreadsheet.NOT_SUPPORTED_STRING)) {
			if (specVersion.equals(SpdxDocumentContainer.ONE_DOT_ZERO_SPDX_VERSION)) {
				dataLicenseId = SpdxRdfConstants.SPDX_DATA_LICENSE_ID_VERSION_1_0;
			} else {
				dataLicenseId = SpdxRdfConstants.SPDX_DATA_LICENSE_ID;
			}
		}
		SpdxListedLicense dataLicense = null;
		try {
			dataLicense = (SpdxListedLicense)LicenseInfoFactory.parseSPDXLicenseString(dataLicenseId, analysis.getDocumentContainer());
		} catch (Exception ex) {
			logger.warn("Unable to parse the provided standard license ID.  Using "+SpdxRdfConstants.SPDX_DATA_LICENSE_ID);
			try {
				dataLicense = (SpdxListedLicense)LicenseInfoFactory.parseSPDXLicenseString(SpdxRdfConstants.SPDX_DATA_LICENSE_ID, analysis.getDocumentContainer());
			} catch (InvalidLicenseStringException e) {
				throw(new InvalidSPDXAnalysisException("Unable to get document license"));
			}
		}
		analysis.setDataLicense(dataLicense);
		analysis.setCreationInfo(creator);
		String docComment = originsSheet.getDocumentComment();
		if (docComment != null) {
		    docComment = docComment.trim();
			if (!docComment.isEmpty()) {
				analysis.setComment(docComment);
			}
		}
		String docName = originsSheet.getDocumentName();
		if (docName != null) {
			analysis.setName(docName);
		}
		ExternalDocumentRef[] externalRefs = originsSheet.getExternalDocumentRefs();
		if (externalRefs != null) {
			analysis.setExternalDocumentRefs(externalRefs);
		}
	}

	private static void usage() {
		System.out.println("Usage: SpreadsheetToRDF spreadsheetfile.xls rdfxmlfile.rdf \n"+
				"where spreadsheetfile.xls is a valid SPDX Spreadsheet and\n"+
				"rdfxmlfile.rdf is the output SPDX RDF analysis file.");
	}

}
