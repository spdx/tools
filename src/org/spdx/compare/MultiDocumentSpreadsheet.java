/**
 * Copyright (c) 2013 Source Auditor Inc.
 * Copyright (c) 2013 Black Duck Software Inc.
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
package org.spdx.compare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.spdxspreadsheet.AbstractSpreadsheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

/**
 * Spreadsheet holding the results of a comparison from multiple SPDX documents
 * Each sheet contains the comparison result results with the columns representing the SPDX documents
 * and the rows representing the SPDX fields.
 * 
 * The sheets include:
 *   - document: Document level fields Created, Data License, Document Comment, created date, creator comment
 *   - creator: Creators
 *   - package: Package level fields name, version, filename, supplier, ...
 *   - extracted license info: Extracted license text and identifiers
 *   - file checksums: file checksums
 *   - file concluded: license concluded for each file
 *   - file licenseInfo: license information from each file
 *   - file license comments: license comments from each file
 *   - file artifactOfs: artifact of for all files
 *   - reviewers: review information
 *   - verification: List of any verification errors
 *   
 * @author Gary O'Neall
 *
 */
public class MultiDocumentSpreadsheet extends AbstractSpreadsheet {
	
	class SpdxFileComparator implements Comparator<SPDXFile> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(SPDXFile arg0, SPDXFile arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}	
	}
	
	private SpdxFileComparator fileComparator = new SpdxFileComparator();

	static Logger logger = Logger.getLogger(MultiDocumentSpreadsheet.class);
	private static final String DOCUMENT_SHEET_NAME = "Document";
	private DocumentSheet documentSheet;
	private static final String CREATOR_SHEET_NAME = "Creator";
	private CreatorSheet creatorSheet;
	private static final String PACKAGE_SHEET_NAME = "Package";
	private PackageSheet packageSheet;
	private static final String EXTRACTED_LICENSE_SHEET_NAME = "Extracted Licenses";
	private ExtractedLicenseSheet extractedLicenseSheet;
	private static final String FILE_CHECKSUM_SHEET_NAME = "File Checksum";
	private FileChecksumSheet fileChecksumSheet;
	private static final String FILE_CONCLUDED_SHEET_NAME = "File Concluded";
	private FileConcludedSheet fileConcludedSheet;
	private static final String FILE_FOUND_SHEET_NAME = "File Found Licenses";
	private FileLicenseInfoSheet fileLicenseInfoSheet;
	private static final String FILE_LICENSE_COMMENT_SHEET_NAME = "File License Comment";
	private FileLicenseCommentsSheet fileLicenseCommentsSheet;
	private static final String FILE_COMMENT_SHEET_NAME = "File Comment";
	private FileCommentSheet fileCommentSheet;
	private static final String FILE_ARTIFACT_OF_SHEET_NAME = "File ArtifactOf";
	private FileArtifactOfSheet fileArtifactOfSheet;
	private static final String REVIEWER_SHEET_NAME = "Reviewers";
	private ReviewerSheet reviewerSheet;
	private static final String VERIFICATION_SHEET_NAME = "Verification Errors";
	public static final int MAX_DOCUMENTS = 25;
	private VerificationSheet verificationSheet;
	
	/**
	 * @param spreadsheetFile
	 * @param create
	 * @param readonly
	 * @throws SpreadsheetException
	 */
	public MultiDocumentSpreadsheet(File spreadsheetFile, boolean create,
			boolean readonly) throws SpreadsheetException {
		super(spreadsheetFile, create, readonly);
		documentSheet = new DocumentSheet(this.workbook, DOCUMENT_SHEET_NAME);
		creatorSheet = new CreatorSheet(this.workbook, CREATOR_SHEET_NAME);
		packageSheet = new PackageSheet(this.workbook, PACKAGE_SHEET_NAME);
		extractedLicenseSheet = new ExtractedLicenseSheet(this.workbook, EXTRACTED_LICENSE_SHEET_NAME);
		fileChecksumSheet = new FileChecksumSheet(this.workbook, FILE_CHECKSUM_SHEET_NAME);
		fileConcludedSheet = new FileConcludedSheet(this.workbook, FILE_CONCLUDED_SHEET_NAME);
		fileLicenseInfoSheet = new FileLicenseInfoSheet(this.workbook, FILE_FOUND_SHEET_NAME);
		fileCommentSheet = new FileCommentSheet(this.workbook, FILE_COMMENT_SHEET_NAME);
		fileLicenseCommentsSheet = new FileLicenseCommentsSheet(this.workbook, FILE_LICENSE_COMMENT_SHEET_NAME);
		fileArtifactOfSheet = new FileArtifactOfSheet(this.workbook, FILE_ARTIFACT_OF_SHEET_NAME);
		reviewerSheet = new ReviewerSheet(this.workbook, REVIEWER_SHEET_NAME);	
		verificationSheet = new VerificationSheet(this.workbook, VERIFICATION_SHEET_NAME);	
		String verify = this.verifyWorkbook();
		if (verify != null && !verify.isEmpty()) {
			logger.error("Invalid workbook: "+verify);
			throw(new SpreadsheetException("Invalid workbook: "+verify));
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSpreadsheet#create(java.io.File)
	 */
	@Override
	public void create(File spreadsheetFile) throws IOException,
			SpreadsheetException {
		if (!spreadsheetFile.createNewFile()) {
			logger.error("Unable to create "+spreadsheetFile.getName());
			throw(new SpreadsheetException("Unable to create "+spreadsheetFile.getName()));
		}
		FileOutputStream excelOut = null;
		try {
			excelOut = new FileOutputStream(spreadsheetFile);
			Workbook wb = new HSSFWorkbook();
			DocumentSheet.create(wb, DOCUMENT_SHEET_NAME);
			CreatorSheet.create(wb, CREATOR_SHEET_NAME);
			PackageSheet.create(wb, PACKAGE_SHEET_NAME);
			ExtractedLicenseSheet.create(wb, EXTRACTED_LICENSE_SHEET_NAME);
			FileChecksumSheet.create(wb, FILE_CHECKSUM_SHEET_NAME);
			FileConcludedSheet.create(wb, FILE_CONCLUDED_SHEET_NAME);
			FileLicenseInfoSheet.create(wb, FILE_FOUND_SHEET_NAME);
			FileCommentSheet.create(wb, FILE_COMMENT_SHEET_NAME);
			FileLicenseCommentsSheet.create(wb, FILE_LICENSE_COMMENT_SHEET_NAME);
			FileArtifactOfSheet.create(wb, FILE_ARTIFACT_OF_SHEET_NAME);
			ReviewerSheet.create(wb, REVIEWER_SHEET_NAME);	
			VerificationSheet.create(wb, VERIFICATION_SHEET_NAME);
			wb.write(excelOut);
		} finally {
			excelOut.close();
		}
	}
	
	public void importCompareResults(SpdxComparer comparer, String[] docNames) throws SpdxCompareException, InvalidSPDXAnalysisException {
		if (docNames == null) {
			throw(new SpdxCompareException("Doc names can not be null"));
		}
		if (comparer == null) {
			throw(new SpdxCompareException("Comparer names can not be null"));
		}
		if (docNames.length != comparer.getNumSpdxDocs()) {
			throw(new SpdxCompareException("Number of document names does not match the number of documents compared"));
		}
		SPDXFile[][] files = new SPDXFile[comparer.getNumSpdxDocs()][];
		for (int i = 0; i < files.length; i++) {
			SPDXFile[] docFiles = comparer.getSpdxDoc(i).getSpdxPackage().getFiles();
			Arrays.sort(docFiles, fileComparator);
			files[i] = docFiles;
		}
		documentSheet.importCompareResults(comparer, docNames);
		creatorSheet.importCompareResults(comparer, docNames);
		packageSheet.importCompareResults(comparer, docNames);
		extractedLicenseSheet.importCompareResults(comparer, docNames);
		fileChecksumSheet.importCompareResults(comparer, files, docNames);
		fileConcludedSheet.importCompareResults(comparer, files, docNames);
		fileLicenseInfoSheet.importCompareResults(comparer, files, docNames);
		fileCommentSheet.importCompareResults(comparer, files, docNames);
		fileLicenseCommentsSheet.importCompareResults(comparer, files, docNames);
		fileArtifactOfSheet.importCompareResults(comparer, files, docNames);
		reviewerSheet.importCompareResults(comparer, docNames);

	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSpreadsheet#clear()
	 */
	@Override
	public void clear() {
		documentSheet.clear();
		creatorSheet.clear();
		packageSheet.clear();
		extractedLicenseSheet.clear();
		fileChecksumSheet.clear();
		fileConcludedSheet.clear();
		fileLicenseInfoSheet.clear();
		fileLicenseCommentsSheet.clear();
		fileArtifactOfSheet.clear();
		reviewerSheet.clear();	
		verificationSheet.clear();
		fileCommentSheet.clear();
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSpreadsheet#verifyWorkbook()
	 */
	@Override
	public String verifyWorkbook() {
		StringBuilder sb = new StringBuilder();
		String sheetVerify = documentSheet.verify();
		if (sheetVerify != null && !sheetVerify.isEmpty()) {
			sb.append(sheetVerify);
		}
		sheetVerify = creatorSheet.verify();
		if (sheetVerify != null && !sheetVerify.isEmpty()) {
			sb.append("; ");
			sb.append(sheetVerify);
		}
		sheetVerify = packageSheet.verify();
		if (sheetVerify != null && !sheetVerify.isEmpty()) {
			sb.append("; ");
			sb.append(sheetVerify);
		}
		sheetVerify = extractedLicenseSheet.verify();
		if (sheetVerify != null && !sheetVerify.isEmpty()) {
			sb.append("; ");
			sb.append(sheetVerify);
		}
		sheetVerify = fileChecksumSheet.verify();
		if (sheetVerify != null && !sheetVerify.isEmpty()) {
			sb.append("; ");
			sb.append(sheetVerify);
		}
		sheetVerify = fileConcludedSheet.verify();
		if (sheetVerify != null && !sheetVerify.isEmpty()) {
			sb.append("; ");
			sb.append(sheetVerify);
		}
		sheetVerify = fileLicenseInfoSheet.verify();
		if (sheetVerify != null && !sheetVerify.isEmpty()) {
			sb.append("; ");
			sb.append(sheetVerify);
		}
		sheetVerify = fileCommentSheet.verify();
		if (sheetVerify != null && !sheetVerify.isEmpty()) {
			sb.append("; ");
			sb.append(sheetVerify);
		}
		sheetVerify = fileLicenseCommentsSheet.verify();
		if (sheetVerify != null && !sheetVerify.isEmpty()) {
			sb.append("; ");
			sb.append(sheetVerify);
		}
		sheetVerify = fileArtifactOfSheet.verify();
		if (sheetVerify != null && !sheetVerify.isEmpty()) {
			sb.append("; ");
			sb.append(sheetVerify);
		}
		sheetVerify = reviewerSheet.verify();	
		if (sheetVerify != null && !sheetVerify.isEmpty()) {
			sb.append("; ");
			sb.append(sheetVerify);
		}
		verificationSheet.verify();	
		if (sheetVerify != null && !sheetVerify.isEmpty()) {
			sb.append("; ");
			sb.append(sheetVerify);
		}
		if (sb.length() > 0) {
			return sb.toString();
		} else {
			return null;
		}
	}

	/**
	 * @param verificationErrors
	 * @param docNames
	 * @throws SpreadsheetException 
	 */
	public void importVerificationErrors(
			ArrayList<String>[] verificationErrors, String[] docNames) throws SpreadsheetException {
		this.verificationSheet.importVerificationErrors(verificationErrors, docNames);
	}

}
