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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.spdxspreadsheet.AbstractSheet;

/**
 * Sheet to hold compare information at the docment level:
 * 		Created, Data License, Document Comment
 * The first row summarizes which fields are different, the subsequent rows are the
 * specific date from each result
 * @author Gary O'Neall
 *
 */
public class DocumentSheet extends AbstractSheet {
	
	int NUM_COLS = 20;
	int DOCUMENT_NAME_COL = 0;
	int NAME_COL = DOCUMENT_NAME_COL+1;
	int VERSION_COL = NAME_COL+1;
	int MACHINE_NAME_COL = VERSION_COL+1;
	int SUPPLIER_COL = MACHINE_NAME_COL + 1;
	int ORIGINATOR_COL = SUPPLIER_COL + 1;
	int URL_COL = ORIGINATOR_COL + 1;
	int PACKAGE_SHA_COL = URL_COL + 1;
	int FILE_VERIFICATION_VALUE_COL = PACKAGE_SHA_COL + 1;
	int VERIFICATION_EXCLUDED_FILES_COL = FILE_VERIFICATION_VALUE_COL + 1;
	int SOURCE_INFO_COL = VERIFICATION_EXCLUDED_FILES_COL + 1;
	int DECLARED_LICENSE_COL = SOURCE_INFO_COL + 1;
	int CONCLUDED_LICENSE_COL = DECLARED_LICENSE_COL + 1;
	int LICENSE_INFO_IN_FILES_COL = CONCLUDED_LICENSE_COL + 1;
	int LICENSE_COMMENT_COL = LICENSE_INFO_IN_FILES_COL + 1;
	int DECLARED_COPYRIGHT_COL = LICENSE_COMMENT_COL + 1;
	int SHORT_DESC_COL = DECLARED_COPYRIGHT_COL + 1;
	int FULL_DESC_COL = SHORT_DESC_COL + 1;
	int CREATION_DATE_COL = FULL_DESC_COL + 1;
	int CREATOR_COMMENT_COL = CREATION_DATE_COL + 1;

	
	static final boolean[] REQUIRED = new boolean[] {true, true, false, true, false, false, true, 
		true, true, true, false, true, true, true, false, true, false, false};
	static final String[] HEADER_TITLES = new String[] {"Document Name", "Package Name", "Package Version", 
		"Package FileName", "Package Supplier", "Package Originator", "Package Download Location", "Package Checksum", "Package Verification Code",
		"Verification Code Excluded Files", "Source Info", "License Declared", "License Concluded", "License Info From Files", 
		"License Comments", "Package Copyright Text", "Summary", "Description", "Creation Date", "Creator Comment"};
	
	static final int[] COLUMN_WIDTHS = new int[] {25, 30, 17, 30, 30, 30, 50, 25, 25, 40, 30,
		40, 40, 90, 50, 50, 50, 80, 50, 50};
	private static final String DIFFERENT_STRING = "Diff";
	private static final String EQUAL_STRING = "Equals";


	/**
	 * @param workbook
	 * @param sheetName
	 */
	public DocumentSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		try {
			if (sheet == null) {
				return "Worksheet for SPDX Package Info does not exist";
			}
			Row firstRow = sheet.getRow(firstRowNum);
			for (int i = 0; i < NUM_COLS; i++) {
				Cell cell = firstRow.getCell(i+firstCellNum);
				if (cell == null || 
						cell.getStringCellValue() == null ||
						!cell.getStringCellValue().equals(HEADER_TITLES[i])) {
					return "Column "+HEADER_TITLES[i]+" missing for SPDX Package Info worksheet";
				}
			}
			return null;
		} catch (Exception ex) {
			return "Error in verifying SPDX Package Info work sheet: "+ex.getMessage();
		}
	}

	/**
	 * @param wb
	 * @param sheetName
	 */
	public static void create(Workbook wb, String sheetName) {
		int sheetNum = wb.getSheetIndex(sheetName);
		if (sheetNum >= 0) {
			wb.removeSheetAt(sheetNum);
		}
		Sheet sheet = wb.createSheet(sheetName);
		CellStyle headerStyle = AbstractSheet.createHeaderStyle(wb);
		CellStyle defaultStyle = AbstractSheet.createLeftWrapStyle(wb);
		Row row = sheet.createRow(0);
		for (int i = 0; i < HEADER_TITLES.length; i++) {
			sheet.setColumnWidth(i, COLUMN_WIDTHS[i]*256);
			sheet.setDefaultColumnStyle(i, defaultStyle);
			Cell cell = row.createCell(i);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(HEADER_TITLES[i]);
		}
	}

	/**
	 * Import comapare results from a comparison
	 * @param comparer Comparer which compared the documents
	 * @param docNames Document names - order must be the same as the documents provided
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void importCompareResults(SpdxComparer comparer, String[] docNames)  throws SpdxCompareException, InvalidSPDXAnalysisException {
		if (comparer.getNumSpdxDocs() != docNames.length) {
			throw(new SpdxCompareException("Number of document names does not match the number of SPDX documents"));
		}
		this.clear();
		// create the rows
		for (int i = 0; i < docNames.length+1; i++) {
			sheet.createRow(i+getFirstDataRow());
		}
		importDocumentNames(docNames);
		importPkgNames(comparer);
		importPkgVersion(comparer);
		importPkgFileNames(comparer);
		importSuppliers(comparer);
		importOriginators(comparer);
		importUrls(comparer);
		importPkgSha1s(comparer);
		importVerificationValues(comparer);
		importExcludedFiles(comparer);
		importSourceInfo(comparer);
		importDeclaredLicense(comparer);
		importConcludedLicense(comparer);
		importLicenseInfoFromFiles(comparer);
		importLicenseComments(comparer);
		importDeclaredCopyrights(comparer);
		importShortDescs(comparer);
		importFullDescs(comparer);
		importCreationDate(comparer);
		importCreatorComment(comparer);
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importCreatorComment(SpdxComparer comparer) throws InvalidSPDXAnalysisException, SpdxCompareException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(CREATOR_COMMENT_COL);
		if (comparer.isCreatorInformationEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(CREATOR_COMMENT_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getCreatorInfo().getComment());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importCreationDate(SpdxComparer comparer) throws InvalidSPDXAnalysisException, SpdxCompareException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(CREATION_DATE_COL);
		if (comparer.isCreatorInformationEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(CREATION_DATE_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getCreatorInfo().getCreated());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importFullDescs(SpdxComparer comparer) throws InvalidSPDXAnalysisException, SpdxCompareException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(FULL_DESC_COL);
		if (comparer.isPackageDescriptionsEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(FULL_DESC_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getDescription());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importShortDescs(SpdxComparer comparer) throws InvalidSPDXAnalysisException, SpdxCompareException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(SHORT_DESC_COL);
		if (comparer.isPackageSummariesEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(SHORT_DESC_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getShortDescription());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importDeclaredCopyrights(SpdxComparer comparer) throws InvalidSPDXAnalysisException, SpdxCompareException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(DECLARED_COPYRIGHT_COL);
		if (comparer.isCopyrightTextsEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(DECLARED_COPYRIGHT_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getDeclaredCopyright());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importLicenseInfoFromFiles(SpdxComparer comparer) throws InvalidSPDXAnalysisException, SpdxCompareException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(LICENSE_INFO_IN_FILES_COL);
		if (comparer.isExtractedLicensingInfosEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(LICENSE_INFO_IN_FILES_COL);
			SPDXLicenseInfo[] licFromFile = comparer.getSpdxDoc(i).getSpdxPackage().getLicenseInfoFromFiles();
			sb.setLength(0);
			if (licFromFile.length > 0) {
				sb.append(licFromFile[0].toString());
			}
			for (int j = 1; j < licFromFile.length; j++) {
				sb.append("; ");
				sb.append(licFromFile[j]);
			}
			cell.setCellValue(sb.toString());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importLicenseComments(SpdxComparer comparer) throws InvalidSPDXAnalysisException, SpdxCompareException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(LICENSE_COMMENT_COL);
		if (comparer.isLicenseCommentsEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(LICENSE_COMMENT_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getLicenseComment());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importConcludedLicense(SpdxComparer comparer) throws InvalidSPDXAnalysisException, SpdxCompareException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(CONCLUDED_LICENSE_COL);
		if (comparer.isPackageConcludedLicensesEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(CONCLUDED_LICENSE_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getConcludedLicenses().toString());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importDeclaredLicense(SpdxComparer comparer) throws InvalidSPDXAnalysisException, SpdxCompareException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(DECLARED_LICENSE_COL);
		if (comparer.isPackageDeclaredLicensesEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(DECLARED_LICENSE_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getDeclaredLicense().toString());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importExcludedFiles(SpdxComparer comparer) throws SpdxCompareException, InvalidSPDXAnalysisException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(VERIFICATION_EXCLUDED_FILES_COL);
		if (comparer.isPackageVerificationCodesEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		StringBuilder sb = new StringBuilder();
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(VERIFICATION_EXCLUDED_FILES_COL);
			String[] skippedFiles = comparer.getSpdxDoc(i).getSpdxPackage().getVerificationCode().getExcludedFileNames();
			sb.setLength(0);
			if (skippedFiles.length > 0) {
				sb.append(skippedFiles[0]);
			}
			for (int j = 1; j < skippedFiles.length; j++) {
				sb.append(", ");
				sb.append(skippedFiles[j]);
			}
			cell.setCellValue(sb.toString());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importSourceInfo(SpdxComparer comparer) throws SpdxCompareException, InvalidSPDXAnalysisException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(SOURCE_INFO_COL);
		if (comparer.isSourceInformationEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(SOURCE_INFO_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getSourceInfo());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importVerificationValues(SpdxComparer comparer) throws InvalidSPDXAnalysisException, SpdxCompareException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(FILE_VERIFICATION_VALUE_COL);
		if (comparer.isPackageVerificationCodesEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(FILE_VERIFICATION_VALUE_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getVerificationCode().getValue());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importPkgSha1s(SpdxComparer comparer) throws SpdxCompareException, InvalidSPDXAnalysisException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(PACKAGE_SHA_COL);
		if (comparer.isPackageChecksumsEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(PACKAGE_SHA_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getSha1());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importUrls(SpdxComparer comparer) throws SpdxCompareException, InvalidSPDXAnalysisException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(URL_COL);
		if (comparer.isPackageDownloadLocationsEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(URL_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getDownloadUrl());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importOriginators(SpdxComparer comparer) throws SpdxCompareException, InvalidSPDXAnalysisException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(ORIGINATOR_COL);
		if (comparer.isPackageOriginatorsEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(ORIGINATOR_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getOriginator());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importSuppliers(SpdxComparer comparer) throws SpdxCompareException, InvalidSPDXAnalysisException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(SUPPLIER_COL);
		if (comparer.isPackageSuppliersEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(SUPPLIER_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getSupplier());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importPkgFileNames(SpdxComparer comparer) throws SpdxCompareException, InvalidSPDXAnalysisException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(MACHINE_NAME_COL);
		if (comparer.isPackageFileNamesEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(MACHINE_NAME_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getFileName());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importPkgVersion(SpdxComparer comparer) throws SpdxCompareException, InvalidSPDXAnalysisException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(VERSION_COL);
		if (comparer.isPackageVersionsEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(VERSION_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getVersionInfo());
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importPkgNames(SpdxComparer comparer) throws InvalidSPDXAnalysisException, SpdxCompareException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(NAME_COL);
		if (comparer.isPackageNamesEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(NAME_COL);
			cell.setCellValue(comparer.getSpdxDoc(i).getSpdxPackage().getDeclaredName());
		}
	}

	/**
	 * @param cell
	 */
	private void setCellDifferentValue(Cell cell) {
		cell.setCellValue(DIFFERENT_STRING);
	}

	/**
	 * @param cell
	 */
	private void setCellEqualValue(Cell cell) {
		cell.setCellValue(EQUAL_STRING);
	}

	/**
	 * @param docNames
	 */
	private void importDocumentNames(String[] docNames) {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(DOCUMENT_NAME_COL);
		cell.setCellValue("Compare Results");
		// data rows
		for (int i = 0; i < docNames.length; i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(DOCUMENT_NAME_COL);
			cell.setCellValue(docNames[i]);
		}
	}

}
