/**
 * Copyright (c) 2013 Source Auditor Inc.
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.SimpleLicensingInfo;
import org.spdx.rdfparser.model.ExternalDocumentRef;
import org.spdx.rdfparser.model.SpdxDocument;

/**
 * @author Gary O'Neall
 *
 */
public class OriginsSheetV1d2 extends OriginsSheet {

	static final int NUM_COLS = 9;
	static final int SPDX_VERSION_COL = SPREADSHEET_VERSION_COL + 1;
	static final int CREATED_BY_COL = SPDX_VERSION_COL + 1;
	static final int CREATED_COL = CREATED_BY_COL + 1;
	static final int DATA_LICENSE_COL = CREATED_COL + 1;
	static final int LICENSE_LIST_VERSION_COL = DATA_LICENSE_COL + 1;
	static final int AUTHOR_COMMENTS_COL = LICENSE_LIST_VERSION_COL + 1;
	static final int DOCUMENT_COMMENT_COL = AUTHOR_COMMENTS_COL + 1;
	static final int USER_DEFINED_COL = DOCUMENT_COMMENT_COL + 1;
	
	static final boolean[] REQUIRED = new boolean[] {true, true, true, true, 
		true, false, false, false, false, false};

	static final String[] HEADER_TITLES = new String[] {"Spreadsheet Version",
		"SPDX Version", "Creator", "Created", "Data License", "License List Version",  
		"Creator Comment", "Document Comment", "Optional User Defined Columns..."};
	static final int[] COLUMN_WIDTHS = new int[] {20, 20, 30, 16, 40, 20, 70, 70, 70};
	static final boolean[] LEFT_WRAP = new boolean[] {false, false, true, false, true, true,
		true, true, true};
	static final boolean[] CENTER_NOWRAP = new boolean[] {true, true, false, true, false, false,
		false, false, false};
	
	public OriginsSheetV1d2(Workbook workbook, String sheetName, String version) {
		super(workbook, sheetName, version);
	}

	@Override
	public String verify() {
		try {
			if (sheet == null) {
				return "Worksheet for SPDX Origins does not exist";
			}
			// validate version
			version = getDataCellStringValue(SPREADSHEET_VERSION_COL);
			if (version == null) {
				return "Invalid origins spreadsheet - no spreadsheet version found";
			}

			if (!SPDXSpreadsheet.verifyVersion(version)) {
				return "Spreadsheet version "+version+" not supported.";
			}
			Row firstRow = sheet.getRow(firstRowNum);
			for (int i = 0; i < NUM_COLS-1; i++) {	// don't check the last col - which is the user defined column
				Cell cell = firstRow.getCell(i+firstCellNum);
				if (cell == null || 
						cell.getStringCellValue() == null ||
						!cell.getStringCellValue().equals(HEADER_TITLES[i])) {
					return "Column "+HEADER_TITLES[i]+" missing for SPDX Origins worksheet";
				}
			}
			// validate rows
			boolean done = false;
			int rowNum = firstRowNum + 1;
			while (!done) {
				Row row = sheet.getRow(rowNum);
				if (row == null || row.getCell(SPDX_VERSION_COL) == null) {
					done = true;
				} else {
					String error = validateRow(row);
					if (error != null) {
						return error;
					}
					rowNum++;
				}
			}
			return null;
		} catch (Exception ex) {
			return "Error in verifying SPDX Origins work sheet: "+ex.getMessage();
		}
	}

	private String validateRow(Row row) {
		for (int i = 0; i < NUM_COLS; i++) {
			Cell cell = row.getCell(i);
			if (cell == null) {
				if (REQUIRED[i]) {
					return "Required cell "+HEADER_TITLES[i]+" missing for row "+String.valueOf(row.getRowNum()+" in Origins Spreadsheet");
				}
			} else {
				if (i == CREATED_COL) {
					if (!(cell.getCellType() == Cell.CELL_TYPE_NUMERIC)) {
						return "Created column in origin spreadsheet is not of type Date";
					}
				}
//				if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
//					return "Invalid cell format for "+HEADER_TITLES[i]+" for forw "+String.valueOf(row.getRowNum());
//				}
			}
		}
		return null;
	}
	public static void create(Workbook wb, String sheetName) {
		int sheetNum = wb.getSheetIndex(sheetName);
		if (sheetNum >= 0) {
			wb.removeSheetAt(sheetNum);
		}
		
		CellStyle headerStyle = AbstractSheet.createHeaderStyle(wb);
		CellStyle centerStyle = AbstractSheet.createCenterStyle(wb);
		CellStyle wrapStyle = AbstractSheet.createLeftWrapStyle(wb);
		Sheet sheet = wb.createSheet(sheetName);
		Row row = sheet.createRow(0);
		for (int i = 0; i < HEADER_TITLES.length; i++) {
			sheet.setColumnWidth(i, COLUMN_WIDTHS[i]*256);
			if (LEFT_WRAP[i]) {
				sheet.setDefaultColumnStyle(i, wrapStyle);
			} else if (CENTER_NOWRAP[i]) {
				sheet.setDefaultColumnStyle(i, centerStyle);
			}
			Cell cell = row.createCell(i);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(HEADER_TITLES[i]);
		}
		Row dataRow = sheet.createRow(1);
		Cell ssVersionCell = dataRow.createCell(SPREADSHEET_VERSION_COL);
		ssVersionCell.setCellValue(SPDXSpreadsheet.CURRENT_VERSION);
	}
	

	
	public void setAuthorComments(String comments) {
		setDataCellStringValue(AUTHOR_COMMENTS_COL, comments);
	}
	
	public void setCreatedBy(String createdBy) {
		setDataCellStringValue(CREATED_BY_COL, createdBy);
	}
	
	public void setDataLicense(String dataLicense) {
		setDataCellStringValue(DATA_LICENSE_COL, dataLicense);
	}
	
	public void setSPDXVersion(String version) {
		setDataCellStringValue(SPDX_VERSION_COL, version);
	}
	
	public void setSpreadsheetVersion(String version) {
		setDataCellStringValue(SPREADSHEET_VERSION_COL, version);
	}
	
	public String getAuthorComments() {
		return getDataCellStringValue(AUTHOR_COMMENTS_COL);
	}
	
	public Date getCreated() {
		return getDataCellDateValue(CREATED_COL);
	}
	
	public String getDataLicense() {
		return getDataCellStringValue(DATA_LICENSE_COL);
	}
	
	public String getSPDXVersion() {
		return getDataCellStringValue(SPDX_VERSION_COL);
	}
	
	public String getSpreadsheetVersion() {
		return getDataCellStringValue(SPREADSHEET_VERSION_COL);
	}

	public void setCreatedBy(String[] createdBy) {
		if (createdBy == null || createdBy.length < 1) {
			setDataCellStringValue(CREATED_BY_COL, "");
			int i = firstRowNum + DATA_ROW_NUM + 1;
			Row nextRow = sheet.getRow(i);
			while (nextRow != null) {
				Cell createdByCell = nextRow.getCell(CREATED_BY_COL);
				if (createdByCell != null) {
					createdByCell.setCellValue("");
				}
				i++;
				nextRow = sheet.getRow(i);
			}
			return;
		}
		setDataCellStringValue(CREATED_BY_COL, createdBy[0]);
		for (int i = 1; i < createdBy.length; i++) {
			Row row = getDataRow(i);
			Cell cell = row.getCell(CREATED_BY_COL);
			if (cell == null) {
				cell = row.createCell(CREATED_BY_COL);
			}
			cell.setCellValue(createdBy[i]);
		}
		// delete any remaining rows
		for (int i = firstRowNum + DATA_ROW_NUM + createdBy.length; i <= this.lastRowNum; i++) {
			Row row = sheet.getRow(i);
			Cell cell = row.getCell(CREATED_BY_COL);
			if (cell != null) {
				row.removeCell(cell);
			}
		}
	}
	
	public String[] getCreatedBy() {
		// first count rows
		int numRows = 0;
		while (sheet.getRow(firstRowNum + DATA_ROW_NUM + numRows) != null &&
				sheet.getRow(firstRowNum + DATA_ROW_NUM + numRows).getCell(CREATED_BY_COL) != null &&
				!sheet.getRow(firstRowNum + DATA_ROW_NUM + numRows).getCell(CREATED_BY_COL).getStringCellValue().isEmpty()) {
			numRows ++;
		}
		String[] retval = new String[numRows];
		for (int i = 0; i < numRows; i++) {
			retval[i] = sheet.getRow(firstRowNum + DATA_ROW_NUM + i).getCell(CREATED_BY_COL).getStringCellValue();
		}
		return retval;
	}

	public void setCreated(Date created) {
		setDataCellDateValue(CREATED_COL, created);
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#getDocumentDomment()
	 */
	@Override
	public String getDocumentComment() {
		return getDataCellStringValue(DOCUMENT_COMMENT_COL);
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#setDocumentComment(java.lang.String)
	 */
	@Override
	public void setDocumentComment(String docComment) {
		setDataCellStringValue(DOCUMENT_COMMENT_COL, docComment);
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#getLicenseListVersion()
	 */
	@Override
	public String getLicenseListVersion() {
		return getDataCellStringValue(LICENSE_LIST_VERSION_COL);
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#setLicenseListVersion(java.lang.String)
	 */
	@Override
	public void setLicenseListVersion(String licenseVersion) {
		setDataCellStringValue(LICENSE_LIST_VERSION_COL, licenseVersion);
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return "";
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#addDocument(org.spdx.rdfparser.model.SpdxDocument)
	 */
	@Override
	public void addDocument(SpdxDocument doc) throws SpreadsheetException {
		// SPDX Version
		setSPDXVersion(doc.getSpecVersion());
		// Created by
		SPDXCreatorInformation creator;
		try {
			creator = doc.getCreationInfo();
		} catch (InvalidSPDXAnalysisException e1) {
			throw(new SpreadsheetException("Error getting the creation info: "+e1.getMessage()));
		}
		String[] createdBys = creator.getCreators();
		setCreatedBy(createdBys);
		// Data license
		AnyLicenseInfo dataLicense;
		try {
			dataLicense = doc.getDataLicense();
		} catch (InvalidSPDXAnalysisException e1) {
			throw(new SpreadsheetException("Error getting the data license info: "+e1.getMessage()));
		}
		if (dataLicense != null && (dataLicense instanceof SimpleLicensingInfo)) {
			setDataLicense(((SimpleLicensingInfo)dataLicense).getLicenseId());
		}
		// Author Comments
		String comments = creator.getComment();
		if (comments != null && !comments.isEmpty()) {
			setAuthorComments(comments);
		}
		String created = creator.getCreated();
		DateFormat dateFormat = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);	
		try {
			setCreated(dateFormat.parse(created));
		} catch (ParseException e) {
			throw(new SpreadsheetException("Invalid created date - unable to parse"));
		}
		// Document comments
		String docComment = doc.getComment();
		if (docComment != null) {
			setDocumentComment(docComment);
		}
		// License List Version
		String licenseListVersion;
		try {
			licenseListVersion = doc.getCreationInfo().getLicenseListVersion();
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpreadsheetException("Error getting the license list info: "+e.getMessage()));
		}
		if (licenseListVersion != null) {
			setLicenseListVersion(licenseListVersion);
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#getSpdxId()
	 */
	@Override
	public String getSpdxId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#setSpdxId(java.lang.String)
	 */
	@Override
	public void setSpdxId(String id) {
		// not supported in this version 
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#getDocumentName()
	 */
	@Override
	public String getDocumentName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#setDocumentName(java.lang.String)
	 */
	@Override
	public void setDocumentName(String documentName) {
		// not supported in this version 
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#getDocumentContents()
	 */
	@Override
	public String[] getDocumentContents() {
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#setDocumentContents(java.lang.String[])
	 */
	@Override
	public void setDocumentDescribes(String[] contents) {
		// Not supported in this version
		
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#getExternalDocumentRefs()
	 */
	@Override
	public ExternalDocumentRef[] getExternalDocumentRefs() {
		return new ExternalDocumentRef[0];
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#setExternalDocumentRefs(org.spdx.rdfparser.model.ExternalDocumentRef[])
	 */
	@Override
	public void setExternalDocumentRefs(
			ExternalDocumentRef[] externalDocumentRefs) {
		// not supported in this version
	}

}
