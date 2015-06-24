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

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.spdxspreadsheet.AbstractSheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

/**
 * Worksheet containing verification errors
 * Columns are package names, rows are individual verification errors
 * @author Gary O'Neall
 *
 */
public class VerificationSheet extends AbstractSheet {

	private static final int COL_WIDTH = 40;

	/**
	 * @param workbook
	 * @param sheetName
	 */
	public VerificationSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		// Nothing to verify
		return null;
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
		for (int i = 0; i < MultiDocumentSpreadsheet.MAX_DOCUMENTS; i++) {
			sheet.setColumnWidth(i, COL_WIDTH*256);
			sheet.setDefaultColumnStyle(i, defaultStyle);
			Cell cell = row.createCell(i);
			cell.setCellStyle(headerStyle);
		}
	}

	/**
	 * Import verification errors
	 * @param verificationErrors Array of verification error message arraylists ordered by docname
	 * @param docNames Name of documents relating to the errors
	 * @throws SpreadsheetException 
	 */
	public void importVerificationErrors(
			List<String>[] verificationErrors, String[] docNames) throws SpreadsheetException {
		if (verificationErrors == null) {
			throw(new SpreadsheetException("Verification errors not specified on import (null value)."));
		}
		if (docNames == null) {
			throw(new SpreadsheetException("Document names errors not specified on import (null value)."));
		}
		if (verificationErrors.length != docNames.length) {
			throw(new SpreadsheetException("Number of verification errors does not equal the number of documents."));
		}
		if (docNames.length > MultiDocumentSpreadsheet.MAX_DOCUMENTS) {
			throw(new SpreadsheetException("Too many compare documents - must be less than "+String.valueOf(MultiDocumentSpreadsheet.MAX_DOCUMENTS+1)));
		}			
		Row header = sheet.getRow(0);
		int lastRowCreated = 0;
		for (int i = 0; i < docNames.length; i++) {
			Cell hCell = header.getCell(i);
			hCell.setCellValue(docNames[i]);
			List<String> errors = verificationErrors[i];
			if (errors != null) {
				for (int j = 0; j < errors.size(); j++) {
					Row errorRow;
					if (j+1 > lastRowCreated) {
						errorRow = sheet.createRow(j+1);
						lastRowCreated = j+1;
					} else {
						errorRow = sheet.getRow(j+1);
					}
					Cell errorCell = errorRow.createCell(i);
					errorCell.setCellValue(errors.get(j));
				}
			}
		}
	}

}
