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
	
	int NUM_COLS = 7;
	int DOCUMENT_NAME_COL = 0;
	int SPDX_VERSION_COL = DOCUMENT_NAME_COL + 1;
	int DATA_LICENSE_COL = SPDX_VERSION_COL + 1;
	int DOCUMENT_COMMENT_COL = DATA_LICENSE_COL + 1;
	int CREATION_DATE_COL = DOCUMENT_COMMENT_COL + 1;
	int CREATOR_COMMENT_COL = CREATION_DATE_COL + 1;
	int LICENSE_LIST_VERSION_COL = CREATOR_COMMENT_COL + 1;

	
	static final boolean[] REQUIRED = new boolean[] {true, true, true, false, true, false, false};
	static final String[] HEADER_TITLES = new String[] {"Document Name", "SPDX Version", 
		"Data License", "Document Comment", "Creation Date", "Creator Comment", "Lic. List. Ver."};
	
	static final int[] COLUMN_WIDTHS = new int[] {35, 15, 15, 60, 22, 60, 22};
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
			addRow();
		}
		importDocumentNames(docNames);
		importSpdxVersion(comparer);
		importDataLicense(comparer);
		importDocumentComments(comparer);
		importCreationDate(comparer);
		importCreatorComment(comparer);
		importLicenseListVersions(comparer);
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importLicenseListVersions(SpdxComparer comparer) throws SpdxCompareException, InvalidSPDXAnalysisException {
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(LICENSE_LIST_VERSION_COL);
		if (comparer.isLicenseListVersionEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(LICENSE_LIST_VERSION_COL);
			if (comparer.getSpdxDoc(i).getCreatorInfo().getLicenseListVersion() != null) {
				cell.setCellValue(comparer.getSpdxDoc(i).getCreatorInfo().getLicenseListVersion());
			}
		}
	}

	/**
	 * @param comparer
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void importSpdxVersion(SpdxComparer comparer) throws SpdxCompareException, InvalidSPDXAnalysisException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(SPDX_VERSION_COL);
		if (comparer.isSpdxVersionEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(SPDX_VERSION_COL);
			if (comparer.getSpdxDoc(i).getSpdxVersion() != null) {
				cell.setCellValue(comparer.getSpdxDoc(i).getSpdxVersion());
			}
		}
	}

	/**
	 * @param comparer
	 */
	private void importDataLicense(SpdxComparer comparer) throws SpdxCompareException, InvalidSPDXAnalysisException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(DATA_LICENSE_COL);
		if (comparer.isDataLicenseEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(DATA_LICENSE_COL);
			if (comparer.getSpdxDoc(i).getDataLicense() != null) {
				cell.setCellValue(comparer.getSpdxDoc(i).getDataLicense().toString());
			}
		}
	}

	/**
	 * @param comparer
	 */
	private void importDocumentComments(SpdxComparer comparer) throws SpdxCompareException, InvalidSPDXAnalysisException {
		// comparison row
		Cell cell = sheet.getRow(getFirstDataRow()).createCell(DOCUMENT_COMMENT_COL);
		if (comparer.isDocumentCommentsEqual()) {
			setCellEqualValue(cell);
		} else {
			setCellDifferentValue(cell);
		}
		// data rows
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			cell = sheet.getRow(getFirstDataRow()+i+1).createCell(DOCUMENT_COMMENT_COL);
			if (comparer.getSpdxDoc(i).getDocumentComment() != null) {
				cell.setCellValue(comparer.getSpdxDoc(i).getDocumentComment());
			}
		}
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
	 * @param cell
	 */
	private void setCellDifferentValue(Cell cell) {
		cell.setCellValue(DIFFERENT_STRING);
		cell.setCellStyle(yellowWrapped);
	}

	/**
	 * @param cell
	 */
	private void setCellEqualValue(Cell cell) {
		cell.setCellValue(EQUAL_STRING);
		cell.setCellStyle(greenWrapped);
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
