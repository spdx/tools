/**
 * Copyright (c) 2015 Source Auditor Inc.
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
import java.util.Arrays;
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
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.ExternalDocumentRef;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxItem;
import org.spdx.tag.BuildDocument;
import org.spdx.tag.InvalidSpdxTagFileException;

/**
 * @author Gary
 *
 */
public class OriginsSheetV2d0 extends DocumentInfoSheet {
	
	static final int NUM_COLS = 14;
	static final int SPDX_VERSION_COL = SPREADSHEET_VERSION_COL + 1;
	static final int DATA_LICENSE_COL = SPDX_VERSION_COL + 1;
	static final int SPDX_ID_COL = DATA_LICENSE_COL + 1;
	static final int LICENSE_LIST_VERSION_COL = SPDX_ID_COL + 1;
	static final int DOCUMENT_NAME_COL = LICENSE_LIST_VERSION_COL + 1;
	static final int NAMESPACE_COL = DOCUMENT_NAME_COL + 1;
	static final int DOCUMENT_DESCRIBES_COL = NAMESPACE_COL + 1;
	static final int EXTERNAL_DOC_REFS_COL = DOCUMENT_DESCRIBES_COL + 1;
	static final int DOCUMENT_COMMENT_COL = EXTERNAL_DOC_REFS_COL + 1;	
	static final int CREATED_BY_COL = DOCUMENT_COMMENT_COL + 1;
	static final int CREATED_COL = CREATED_BY_COL + 1;	
	static final int AUTHOR_COMMENTS_COL = CREATED_COL + 1;
	static final int USER_DEFINED_COL = AUTHOR_COMMENTS_COL + 1;
	
	static final boolean[] REQUIRED = new boolean[] {true, true, true, true, 
		false, true, true, true, false, false, true, true, false, false};

	static final String[] HEADER_TITLES = new String[] {"Spreadsheet Version",
		"SPDX Version", "Data License", "SPDX Identifier", "License List Version",
		"Document Name", "Document Namespace", "Document Contents",
		"External Document References", "Document Comment", "Creator", "Created",   
		"Creator Comment", "Optional User Defined Columns..."};
	static final int[] COLUMN_WIDTHS = new int[] {20, 16, 20, 20, 16, 40, 80,
		50, 140, 70, 60, 20, 70, 60};
	static final boolean[] LEFT_WRAP = new boolean[] {false, false, false, false, 
		false, true, true, true, true, true, true, false, true, true};
	static final boolean[] CENTER_NOWRAP = new boolean[] {true, true, true, true, 
		true, false, false, false, false, false, false, true, false, false};
	
	public OriginsSheetV2d0(Workbook workbook, String sheetName, String version) {
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
		return getDataCellStringValue(NAMESPACE_COL);
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
		setSpdxId(doc.getId());
		setDocumentName(doc.getName());
		try {
			setNamespace(doc.getDocumentNamespace());
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpreadsheetException("Error getting the document namespace: "+e.getMessage()));
		}
		SpdxItem[] contents = null;
		try {
			contents = doc.getDocumentDescribes();
		} catch (InvalidSPDXAnalysisException e1) {
			throw(new SpreadsheetException("Error getting the document describes: "+e1.getMessage()));
		}
		String[] contentIds = new String[contents.length];
		for (int i = 0; i < contents.length; i++) {
			contentIds[i] = contents[i].getId();
		}
		Arrays.sort(contentIds);
		setDocumentDescribes(contentIds);
		try {
			setExternalDocumentRefs(doc.getExternalDocumentRefs());
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpreadsheetException("Error getting the external document references: "+e.getMessage()));
		}
	}


	/**
	 * @param namespace
	 */
	private void setNamespace(String namespace) {
		setDataCellStringValue(NAMESPACE_COL, namespace);
	}


	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#getSpdxId()
	 */
	@Override
	public String getSpdxId() {
		return getDataCellStringValue(SPDX_ID_COL);
	}


	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#setSpdxId(java.lang.String)
	 */
	@Override
	public void setSpdxId(String id) {
		setDataCellStringValue(SPDX_ID_COL, id);
	}


	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#getDocumentName()
	 */
	@Override
	public String getDocumentName() {
		return getDataCellStringValue(DOCUMENT_NAME_COL);
	}


	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#setDocumentName(java.lang.String)
	 */
	@Override
	public void setDocumentName(String documentName) {
		setDataCellStringValue(DOCUMENT_NAME_COL, documentName);
	}


	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#getDocumentContents()
	 */
	@Override
	public String[] getDocumentContents() {
		// first count rows
		int numRows = 0;
		while (sheet.getRow(firstRowNum + DATA_ROW_NUM + numRows) != null &&
				sheet.getRow(firstRowNum + DATA_ROW_NUM + numRows).getCell(DOCUMENT_DESCRIBES_COL) != null &&
				!sheet.getRow(firstRowNum + DATA_ROW_NUM + numRows).getCell(DOCUMENT_DESCRIBES_COL).getStringCellValue().isEmpty()) {
			numRows ++;
		}
		String[] retval = new String[numRows];
		for (int i = 0; i < numRows; i++) {
			retval[i] = sheet.getRow(firstRowNum + DATA_ROW_NUM + i).getCell(DOCUMENT_DESCRIBES_COL).getStringCellValue();
		}
		return retval;
	}


	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#setDocumentContents(java.lang.String[])
	 */
	@Override
	public void setDocumentDescribes(String[] contents) {
		if (contents == null || contents.length < 1) {
			setDataCellStringValue(DOCUMENT_DESCRIBES_COL, "");
			int i = firstRowNum + DATA_ROW_NUM + 1;
			Row nextRow = sheet.getRow(i);
			while (nextRow != null) {
				Cell documentDescribesCell = nextRow.getCell(DOCUMENT_DESCRIBES_COL);
				if (documentDescribesCell != null) {
					documentDescribesCell.setCellValue("");
				}
				i++;
				nextRow = sheet.getRow(i);
			}
			return;
		}
		setDataCellStringValue(DOCUMENT_DESCRIBES_COL, contents[0]);
		for (int i = 1; i < contents.length; i++) {
			Row row = getDataRow(i);
			Cell cell = row.getCell(DOCUMENT_DESCRIBES_COL);
			if (cell == null) {
				cell = row.createCell(DOCUMENT_DESCRIBES_COL);
			}
			cell.setCellValue(contents[i]);
		}
		// delete any remaining rows
		for (int i = firstRowNum + DATA_ROW_NUM + contents.length; i <= this.lastRowNum; i++) {
			Row row = sheet.getRow(i);
			Cell cell = row.getCell(DOCUMENT_DESCRIBES_COL);
			if (cell != null) {
				row.removeCell(cell);
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#getExternalDocumentRefs()
	 */
	@Override
	public ExternalDocumentRef[] getExternalDocumentRefs() throws SpreadsheetException {
		int numRows = 0;
		while (sheet.getRow(firstRowNum + DATA_ROW_NUM + numRows) != null &&
				sheet.getRow(firstRowNum + DATA_ROW_NUM + numRows).getCell(EXTERNAL_DOC_REFS_COL) != null &&
				!sheet.getRow(firstRowNum + DATA_ROW_NUM + numRows).getCell(EXTERNAL_DOC_REFS_COL).getStringCellValue().isEmpty()) {
			numRows ++;
		}
		ExternalDocumentRef[] retval = new ExternalDocumentRef[numRows];
		for (int i = 0; i < numRows; i++) {
			try {
				retval[i] = BuildDocument.parseExternalDocumentRef(sheet.getRow(
						firstRowNum + DATA_ROW_NUM + i).
						getCell(EXTERNAL_DOC_REFS_COL).getStringCellValue());
			} catch (InvalidSpdxTagFileException e) {
				throw(new SpreadsheetException("Invalid external document reference string: "+sheet.getRow(
						firstRowNum + DATA_ROW_NUM + i).
						getCell(EXTERNAL_DOC_REFS_COL).getStringCellValue()));
			}
		}
		return retval;
	}


	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.OriginsSheet#setExternalDocumentRefs(org.spdx.rdfparser.model.ExternalDocumentRef[])
	 */
	@Override
	public void setExternalDocumentRefs(ExternalDocumentRef[] externalDocumentRefs) throws SpreadsheetException {
		if (externalDocumentRefs == null || externalDocumentRefs.length < 1) {
			setDataCellStringValue(EXTERNAL_DOC_REFS_COL, "");
			int i = firstRowNum + DATA_ROW_NUM + 1;
			Row nextRow = sheet.getRow(i);
			while (nextRow != null) {
				Cell externalDocRefsCell = nextRow.getCell(EXTERNAL_DOC_REFS_COL);
				if (externalDocRefsCell != null) {
					externalDocRefsCell.setCellValue("");
				}
				i++;
				nextRow = sheet.getRow(i);
			}
			return;
		}
		try {
			setDataCellStringValue(EXTERNAL_DOC_REFS_COL, 
					externalDocRefToStr(externalDocumentRefs[0]));
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpreadsheetException("Error getting external document reference",e));
		}
		for (int i = 1; i < externalDocumentRefs.length; i++) {
			Row row = getDataRow(i);
			Cell cell = row.getCell(EXTERNAL_DOC_REFS_COL);
			if (cell == null) {
				cell = row.createCell(EXTERNAL_DOC_REFS_COL);
			}
			try {
				cell.setCellValue(externalDocRefToStr(externalDocumentRefs[i]));
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpreadsheetException("Error getting external document reference",e));
			}
		}
		// delete any remaining rows
		for (int i = firstRowNum + DATA_ROW_NUM + externalDocumentRefs.length; i <= this.lastRowNum; i++) {
			Row row = sheet.getRow(i);
			Cell cell = row.getCell(EXTERNAL_DOC_REFS_COL);
			if (cell != null) {
				row.removeCell(cell);
			}
		}
	}


	/**
	 * @param externalDocumentRef
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private String externalDocRefToStr(ExternalDocumentRef externalDocumentRef) throws InvalidSPDXAnalysisException {
		if (externalDocumentRef == null) {
			return "";
		}
		return externalDocumentRef.getExternalDocumentId() +
				" " + externalDocumentRef.getSpdxDocumentNamespace() + 
				" " + Checksum.CHECKSUM_ALGORITHM_TO_TAG.get(externalDocumentRef.getChecksum().getAlgorithm()) +
				" " + externalDocumentRef.getChecksum().getValue();
	}
}
