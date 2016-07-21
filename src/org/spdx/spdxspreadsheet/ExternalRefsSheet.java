/**
 * Copyright (c) 2016 Source Auditor Inc.
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

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.ExternalRef;
import org.spdx.rdfparser.referencetype.ReferenceType;

/**
 * Package external refs
 * @author Gary O'Neall
 *
 */
public class ExternalRefsSheet extends AbstractSheet {
	
	static final int PKG_ID_COL = 0;
	static final int REF_CATEGORY_COL = PKG_ID_COL + 1;
	static final int REF_TYPE_COL = REF_CATEGORY_COL + 1;
	static final int REF_LOCATOR_COL = REF_TYPE_COL + 1;
	static final int COMMENT_COL = REF_LOCATOR_COL + 1;

	static final int USER_DEFINED_COLS = COMMENT_COL + 1;
	static final int NUM_COLS = USER_DEFINED_COLS + 1;
	
	static final boolean[] REQUIRED = new boolean[] {true, true, true, true, false, false};
	static final String[] HEADER_TITLES = new String[] {"Package ID", "Category",
		"Type", "Locator", "Comment", "User Defined ..."};
	static final int[] COLUMN_WIDTHS = new int[] {25, 25, 40, 60, 40, 40};
	static final boolean[] LEFT_WRAP = new boolean[] {false, false, true, true, true, true};
	static final boolean[] CENTER_NOWRAP = new boolean[] {true, true, false, false, false, false};

	/**
	 * @param workbook
	 * @param sheetName
	 */
	public ExternalRefsSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		try {
			if (sheet == null) {
				return "Worksheet for External Refs does not exist";
			}
			Row firstRow = sheet.getRow(firstRowNum);
			for (int i = 0; i < NUM_COLS- 1; i++) { 	// Don't check the last (user defined) column
				Cell cell = firstRow.getCell(i+firstCellNum);
				if (cell == null || 
						cell.getStringCellValue() == null ||
						!cell.getStringCellValue().equals(HEADER_TITLES[i])) {
					return "Column "+HEADER_TITLES[i]+" missing for External Refs worksheet";
				}
			}
			// validate rows
			boolean done = false;
			int rowNum = getFirstDataRow();
			while (!done) {
				Row row = sheet.getRow(rowNum);
				if (row == null || row.getCell(firstCellNum) == null) {
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
			return "Error in verifying External Refs work sheet: "+ex.getMessage();
		}
	}
	
	/**
	 * @param row
	 * @return
	 */
	private String validateRow(Row row) {
		for (int i = 0; i < NUM_COLS; i++) {
			Cell cell = row.getCell(i);
			if (cell == null) {
				if (REQUIRED[i]) {
					return "Required cell "+HEADER_TITLES[i]+" missing for row "+String.valueOf(row.getRowNum());
				}
			}
		}
		return null;
	}

	/**
	 * @param wb
	 * @param externalRefsSheetName
	 */
	public static void create(Workbook wb, String externalRefsSheetName) {
		int sheetNum = wb.getSheetIndex(sheetName);
		if (sheetNum >= 0) {
			wb.removeSheetAt(sheetNum);
		}
		Sheet sheet = wb.createSheet(sheetName);
		CellStyle headerStyle = AbstractSheet.createHeaderStyle(wb);	
		CellStyle centerStyle = AbstractSheet.createCenterStyle(wb);
		CellStyle wrapStyle = AbstractSheet.createLeftWrapStyle(wb);
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
	}

	/**
	 * @param packageId Package ID for the package that contains this external ref
	 * @param externalRef
	 * @throws SpreadsheetException 
	 */
	public void add(String packageId, ExternalRef externalRef) throws SpreadsheetException {
		Row row = addRow();
		if (packageId != null) {
			row.createCell(PKG_ID_COL).setCellValue(packageId);
		}
		if (externalRef != null) {
			if (externalRef.getReferenceCategory() != null) {
				row.createCell(REF_CATEGORY_COL).setCellValue(externalRef.getReferenceCategory().getTag());
			}
			try {
				if (externalRef.getReferenceType() != null) {
					row.createCell(REF_TYPE_COL).setCellValue(refTypeToString(externalRef.getReferenceType()));
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpreadsheetException("Error getting external reference type: "+e.getMessage()));
			}
			if (externalRef.getReferenceLocator() != null) {
				row.createCell(REF_LOCATOR_COL).setCellValue(externalRef.getReferenceLocator());
			}
			if (externalRef.getComment() != null) {
				row.createCell(COMMENT_COL).setCellValue(externalRef.getComment());
			}
		}
	}

	/**
	 * Convert a reference type to the type used in 
	 * @param referenceType
	 * @return
	 */
	private Date refTypeToString(ReferenceType referenceType) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get all external references for a given package ID
	 * @param id
	 * @return
	 */
	public ExternalRef[] getExternalRefsForPkgid(String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
