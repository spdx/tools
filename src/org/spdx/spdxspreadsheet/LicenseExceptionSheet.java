/**
 * Copyright (c) 2014 Source Auditor Inc.
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

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.license.LicenseException;

/**
 * Sheet containing the License Exceptions
 * @author Gary O'Neall
 *
 */
public class LicenseExceptionSheet extends AbstractSheet {

	static final Logger logger = Logger.getLogger(LicenseSheet.class.getName());
	static final int NUM_COLS = 6;
	static final int COL_NAME = 0;
	static final int COL_ID = COL_NAME + 1;
	static final int COL_SOURCE_URL = COL_ID + 1;
	static final int COL_NOTES = COL_SOURCE_URL + 1;
	static final int COL_TEXT = COL_NOTES + 1;
	static final int COL_EXAMPLES = COL_TEXT + 1;
	
	static final boolean[] REQUIRED = new boolean[] {true, true, false, false,
		true, true};
	static final String[] HEADER_TITLES = new String[] {"Full name of Exception", 
		"Exception Identifier", "Source/url", "Notes", "Exception Text", "Example of use"};

	/**
	 * Create a blank worksheet NOTE: Replaces / deletes existing sheet by the same name
	 */
	public static void create(Workbook wb, String sheetName) {
		int sheetNum = wb.getSheetIndex(sheetName);
		if (sheetNum >= 0) {
			wb.removeSheetAt(sheetNum);
		}
		Sheet sheet = wb.createSheet(sheetName);
		Row row = sheet.createRow(0);
		for (int i = 0; i < HEADER_TITLES.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(HEADER_TITLES[i]);
		}
	}
	/**
	 * @param workbook
	 * @param sheetName
	 */
	public LicenseExceptionSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}
	
	public void add(LicenseException exception) {
		Row row = addRow();
		Cell nameCell = row.createCell(COL_NAME);
		nameCell.setCellValue(exception.getName());
		Cell idCell = row.createCell(COL_ID);
		idCell.setCellValue(exception.getLicenseExceptionId());
		if (exception.getSeeAlso() != null && exception.getSeeAlso().length > 0) {
			Cell sourceUrlCell = row.createCell(COL_SOURCE_URL);
			StringBuilder sb = new StringBuilder();
			sb.append(exception.getSeeAlso()[0]);
			for (int i = 1; i < exception.getSeeAlso().length; i++) {
				sb.append(' ');
				sb.append(exception.getSeeAlso()[i]);
			}
			sourceUrlCell.setCellValue(sb.toString());
		}

		Cell textCell = row.createCell(COL_TEXT);
		String text = exception.getLicenseExceptionText();
		if (text != null) {
			textCell.setCellValue(text);
		}
		String notes = exception.getComment();
		Cell notesCell = row.createCell(COL_NOTES);
		if (notes != null) {
			notesCell.setCellValue(notes);
		}
		String examples = exception.getExample();
		Cell examplesCell = row.createCell(COL_EXAMPLES);
		examplesCell.setCellValue(examples);
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		try {
			if (sheet == null) {
				return "Worksheet for SPDX License Exceptions does not exist";
			}
			Row firstRow = sheet.getRow(firstRowNum);
			for (int i = 0; i < NUM_COLS; i++) {
				Cell cell = firstRow.getCell(i+firstCellNum);
				if (cell == null || 
						cell.getStringCellValue() == null ||
						!cell.getStringCellValue().equals(HEADER_TITLES[i])) {
					return "Column "+HEADER_TITLES[i]+" missing for SPDX Licenses worksheet";
				}
			}
			// validate rows
			boolean done = false;
			int rowNum = firstRowNum + 1;
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
			return "Error in verifying SPDX License Exception work sheet: "+ex.getMessage();
		}
	}

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
	 * Get the license exception at the row rowNum
	 * @param rowNum
	 * @return
	 */
	public LicenseException getException(int rowNum) {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		String id = null;
		Cell idCell = row.getCell(COL_ID);
		if (idCell != null) {
			id = idCell.getStringCellValue();
		}
		String name = null;
		Cell nameCell = row.getCell(COL_NAME);
		if (nameCell != null) {
			name = nameCell.getStringCellValue();
		}
		String notes = null;
		Cell notesCell = row.getCell(COL_NOTES);
		if (notesCell != null) {
			notes = notesCell.getStringCellValue();
		}
		String[] sourceURL = null;
		Cell sourceURLCell = row.getCell(COL_SOURCE_URL);
		if (sourceURLCell != null) {
			try {
				String stSourceURL = sourceURLCell.getStringCellValue();
				sourceURL = stSourceURL.split("\\s");
				for (int i = 0; i < sourceURL.length; i++) {
					sourceURL[i] = sourceURL[i].trim();
				}
			} catch (Exception ex) {
				sourceURL = new String[] {"Exception getting URL: "+ex.getMessage()};
			}
		}
		String text = null;
		Cell textCell = row.getCell(COL_TEXT);
		if (textCell != null) {
			text = textCell.getStringCellValue();
		}
		String examples = null;
		Cell examplesCell = row.getCell(COL_EXAMPLES);
		if (examplesCell != null) {
			examples = examplesCell.getStringCellValue();
		}
		return new LicenseException(id, name, text, sourceURL, notes, examples);
	}

}
