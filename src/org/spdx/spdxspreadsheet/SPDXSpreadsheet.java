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
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * A spreadsheet containing information on an SPDX Document.
 * 
 * The spreadsheet contains 4 sheets:
 *   - Origins - Information about the origin of the SPDX Document (Version, createdby, ...)
 *   - Package Info - Information about the package itself
 *   - Non-standard licenses - text from any non-standard licenses found
 *   - Per File Info - Information about each file in the document
 *   - Reviewers - Information on any organizations who have reviewed the documents
 *   
 *   See notes below on version management
 * @author Gary O'Neall
 *
 */
public class SPDXSpreadsheet extends AbstractSpreadsheet {

	/*
	 * The following information relates to the version management for the SPDXSpreadsheet.
	 * Each sheet in the workbook implements a Factory method to instantiate the correct
	 * version using the static method <code>openVersion(Workbook wb, String sheetName, String versionNumber)</code>
	 * Each sheet also implements a method to create the latest version <code>create(Workbook wb, String sheetName)</code>
	 */
	public static final String CURRENT_VERSION = "2.1.0";
	public static final String VERSION_2_0_0 = "2.0.0";
	public static final String VERSION_1_2_0 = "1.2.0";
	public static final String VERSION_1_1_0 = "1.1.0";
	public static final String VERSION_0_9_4 = "0.9.4";
	public static final String VERSION_0_9_3 = "0.9.3";
	public static final String VERSION_0_9_2 = "0.9.2";
	public static final String VERSION_0_9_1 = "0.9.1";
	public static final String[] SUPPORTED_VERSIONS = new String[] {CURRENT_VERSION,VERSION_0_9_4, 
		VERSION_0_9_3, VERSION_0_9_2, VERSION_0_9_1, VERSION_2_0_0};
	public static final String UNKNOWN_VERSION = "UNKNOWN";
	
	private DocumentInfoSheet documentInfoSheet;
	static final String DOCUMENT_INFO_NAME = "Document Info";
	private PackageInfoSheet packageInfoSheet;
	static final String PACKAGE_INFO_SHEET_NAME = "Package Info";
	private NonStandardLicensesSheet nonStandardLicensesSheet;
	static final String NON_STANDARD_LICENSE_SHEET_NAME = "Extracted License Info";
	private PerFileSheet perFileSheet;
	static final String PER_FILE_SHEET_NAME = "Per File Info";
	private RelationshipsSheet relationshipsSheet;
	static final String RELATIONSHIPS_SHEET_NAME = "Relationships";
	private AnnotationsSheet annotationsSheet;
	static final String ANNOTATIONS_SHEET_NAME = "Annotations";
	private ReviewersSheet reviewersSheet;
	static final String REVIEWERS_SHEET_NAME = "Reviewers";
	private SnippetSheet snippetSheet;
	static final String SNIPPET_SHEET_NAME = "Snippets";
	private ExternalRefsSheet externalRefsSheet;
	static final String EXTERNAL_REFS_SHEET_NAME = "External Refs";
	private String version;
	
	
	/**
	 * Creates a new spreadsheet based on an existing file.  Handles all version compatibilities
	 * @param spreadsheetFile
	 * @param create
	 * @param readonly
	 * @throws SpreadsheetException
	 */
	public SPDXSpreadsheet(File spreadsheetFile, boolean create,
			boolean readonly) throws SpreadsheetException {
		super(spreadsheetFile, create, readonly);
		this.version = readVersion(this.workbook, DOCUMENT_INFO_NAME);	
		if (this.version.equals(UNKNOWN_VERSION)) {
			throw(new SpreadsheetException("The version for the SPDX spreadsheet could not be read."));
		}
		this.documentInfoSheet = DocumentInfoSheet.openVersion(this.workbook, DOCUMENT_INFO_NAME, this.version);
		String verifyMsg = documentInfoSheet.verify();
		if (verifyMsg != null) {
			logger.error(verifyMsg);
			throw(new SpreadsheetException(verifyMsg));
		}
		this.packageInfoSheet = PackageInfoSheet.openVersion(this.workbook, PACKAGE_INFO_SHEET_NAME, version);
		this.nonStandardLicensesSheet = NonStandardLicensesSheetV0d9d4.openVersion(this.workbook, NON_STANDARD_LICENSE_SHEET_NAME, version);
		this.perFileSheet = PerFileSheet.openVersion(this.workbook, PER_FILE_SHEET_NAME, version);
		this.relationshipsSheet = new RelationshipsSheet(this.workbook, RELATIONSHIPS_SHEET_NAME);
		this.annotationsSheet = new AnnotationsSheet(this.workbook, ANNOTATIONS_SHEET_NAME);
		this.reviewersSheet = new ReviewersSheet(this.workbook, REVIEWERS_SHEET_NAME, version);
		this.snippetSheet = new SnippetSheet(this.workbook, SNIPPET_SHEET_NAME);
		this.externalRefsSheet = new ExternalRefsSheet(this.workbook, EXTERNAL_REFS_SHEET_NAME);

		verifyMsg = verifyWorkbook();
		if (verifyMsg != null) {
			logger.error(verifyMsg);
			throw(new SpreadsheetException(verifyMsg));
		}
	}

	/**
	 * Determine the version of an existing workbook
	 * @param workbook
	 * @param originSheetName
	 * @return
	 */
	private String readVersion(Workbook workbook, String originSheetName) {
		Sheet sheet = workbook.getSheet(originSheetName);
		int firstRowNum = sheet.getFirstRowNum();
		Row dataRow = sheet.getRow(firstRowNum + DocumentInfoSheet.DATA_ROW_NUM);
		if (dataRow == null) {
			return UNKNOWN_VERSION;
		}
		Cell versionCell = dataRow.getCell(DocumentInfoSheet.SPREADSHEET_VERSION_COL);
		if (versionCell == null) {
			return UNKNOWN_VERSION;
		}
		return versionCell.getStringCellValue();
	}
	
	/**
	 * @param versionToCheck
	 * @return
	 */
	public static boolean verifyVersion(String versionToCheck) {
		boolean supported = false;
		String trVersion = versionToCheck.trim();
		for (int i = 0; i < SPDXSpreadsheet.SUPPORTED_VERSIONS.length; i++) {
			if (SPDXSpreadsheet.SUPPORTED_VERSIONS[i].equals(trVersion)) {
				supported = true;
				break;
			}
		}
		return supported;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSpreadsheet#create(java.io.File)
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
			DocumentInfoSheet.create(wb, DOCUMENT_INFO_NAME);
			PackageInfoSheet.create(wb, PACKAGE_INFO_SHEET_NAME);
			ExternalRefsSheet.create(wb, EXTERNAL_REFS_SHEET_NAME);
			NonStandardLicensesSheet.create(wb, NON_STANDARD_LICENSE_SHEET_NAME);
			PerFileSheet.create(wb, PER_FILE_SHEET_NAME);
			RelationshipsSheet.create(wb, RELATIONSHIPS_SHEET_NAME);
			AnnotationsSheet.create(wb, ANNOTATIONS_SHEET_NAME);
			SnippetSheet.create(wb, SNIPPET_SHEET_NAME);
			ReviewersSheet.create(wb, REVIEWERS_SHEET_NAME);
			wb.write(excelOut);
		} finally {
		    if(excelOut != null){
		        excelOut.close();
		    }
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSpreadsheet#clear()
	 */
	@Override
	public void clear() {
		this.documentInfoSheet.clear();
		this.packageInfoSheet.clear();
		this.nonStandardLicensesSheet.clear();
		this.perFileSheet.clear();
		this.relationshipsSheet.clear();
		this.annotationsSheet.clear();
		this.reviewersSheet.clear();
		this.snippetSheet.clear();
		this.externalRefsSheet.clear();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSpreadsheet#verifyWorkbook()
	 */
	@Override
	public String verifyWorkbook() {
		String retval = this.documentInfoSheet.verify();
		if (retval == null || retval.isEmpty()) {
			retval = this.packageInfoSheet.verify();
		}
		if (retval == null || retval.isEmpty()) {
			retval = this.nonStandardLicensesSheet.verify();
		}
		if (retval == null || retval.isEmpty()) {
			retval = this.perFileSheet.verify();
		}
		if (retval == null || retval.isEmpty()) {
			retval = this.reviewersSheet.verify();
		}
		if (retval == null || retval.isEmpty()) {
			retval = this.relationshipsSheet.verify();
		}
		if (retval == null || retval.isEmpty()) {
			retval = this.annotationsSheet.verify();
		}
		if (retval == null || retval.isEmpty()) {
			retval = this.snippetSheet.verify();
		}
		if (retval == null || retval.isEmpty()) {
			retval = this.externalRefsSheet.verify();
		}
		return retval;
	}

	/**
	 * @return the originsSheet
	 */
	public DocumentInfoSheet getOriginsSheet() {
		return documentInfoSheet;
	}

	/**
	 * @param originsSheet the originsSheet to set
	 */
	public void setOriginsSheet(DocumentInfoSheet originsSheet) {
		this.documentInfoSheet = originsSheet;
	}

	/**
	 * @return the packageInfoSheet
	 */
	public PackageInfoSheet getPackageInfoSheet() {
		return packageInfoSheet;
	}

	/**
	 * @param packageInfoSheet the packageInfoSheet to set
	 */
	public void setPackageInfoSheet(PackageInfoSheetV09d2 packageInfoSheet) {
		this.packageInfoSheet = packageInfoSheet;
	}

	/**
	 * @return the nonStandardLicensesSheet
	 */
	public NonStandardLicensesSheet getNonStandardLicensesSheet() {
		return nonStandardLicensesSheet;
	}

	/**
	 * @param nonStandardLicensesSheet the nonStandardLicensesSheet to set
	 */
	public void setNonStandardLicensesSheet(
			NonStandardLicensesSheetV0d9d4 nonStandardLicensesSheet) {
		this.nonStandardLicensesSheet = nonStandardLicensesSheet;
	}

	/**
	 * @return the perFileSheet
	 */
	public PerFileSheet getPerFileSheet() {
		return perFileSheet;
	}

	/**
	 * @param perFileSheet the perFileSheet to set
	 */
	public void setPerFileSheet(PerFileSheetV09d3 perFileSheet) {
		this.perFileSheet = perFileSheet;
	}

	/**
	 * @return the reviewersSheet
	 */
	public ReviewersSheet getReviewersSheet() {
		return reviewersSheet;
	}

	/**
	 * @param reviewersSheet the reviewersSheet to set
	 */
	public void setReviewersSheet(ReviewersSheet reviewersSheet) {
		this.reviewersSheet = reviewersSheet;
	}
	
	public RelationshipsSheet getRelationshipsSheet() {
		return relationshipsSheet;
	}

	public void setRelationshipsSheet(RelationshipsSheet relationshipsSheet) {
		this.relationshipsSheet = relationshipsSheet;
	}

	public AnnotationsSheet getAnnotationsSheet() {
		return annotationsSheet;
	}

	public void setAnnotationsSheet(AnnotationsSheet annotationsSheet) {
		this.annotationsSheet = annotationsSheet;
	}

	public void setPackageInfoSheet(PackageInfoSheet packageInfoSheet) {
		this.packageInfoSheet = packageInfoSheet;
	}

	public void setNonStandardLicensesSheet(
			NonStandardLicensesSheet nonStandardLicensesSheet) {
		this.nonStandardLicensesSheet = nonStandardLicensesSheet;
	}

	public void setPerFileSheet(PerFileSheet perFileSheet) {
		this.perFileSheet = perFileSheet;
	}
	
	/**
	 * @return the snippetSheet
	 */
	public SnippetSheet getSnippetSheet() {
		return snippetSheet;
	}

	/**
	 * @param snippetSheet the snippetSheet to set
	 */
	public void setSnippetSheet(SnippetSheet snippetSheet) {
		this.snippetSheet = snippetSheet;
	}
	
	/**
	 * @return the externalRefsSheet
	 */
	public ExternalRefsSheet getExternalRefsSheet() {
		return externalRefsSheet;
	}

	/**
	 * @param snippetSheet the snippetSheet to set
	 */
	public void setExternaRefsSheet(ExternalRefsSheet externalRefsSheet) {
		this.externalRefsSheet = externalRefsSheet;
	}

	/**
	 * Resize the height of all rows - will not exceed a maximum height
	 */
	public void resizeRow() {
		nonStandardLicensesSheet.resizeRows();
//		originsSheet.resizeRows(); - Can't resize the origins sheet since it uses blank cells
		packageInfoSheet.resizeRows();
		perFileSheet.resizeRows();
		relationshipsSheet.resizeRows();
		annotationsSheet.resizeRows();
		snippetSheet.resizeRows();
		externalRefsSheet.resizeRows();
//		reviewersSheet.resizeRows(); - Can't resize the review sheet since it uses blank cells
	}
}
