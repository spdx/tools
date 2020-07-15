/**
 * Copyright (c) 2011 Source Auditor Inc.
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Sheet containing the text of any non-standard licenses found in an SPDX document
 * Implementat for version 1.1
 * @author Gary O'Neall
 *
 */
public class NonStandardLicensesSheetV1d1 extends NonStandardLicensesSheet {

	static final int NUM_COLS = 6;
	static final int IDENTIFIER_COL = 0;
	static final int EXTRACTED_TEXT_COL = IDENTIFIER_COL + 1;
	static final int LICENSE_NAME_COL = EXTRACTED_TEXT_COL + 1;
	static final int CROSS_REF_URL_COL = LICENSE_NAME_COL + 1;
	static final int COMMENT_COL = CROSS_REF_URL_COL + 1;
	static final int USER_DEFINED_COL = COMMENT_COL + 1;

	static boolean[] REQUIRED = new boolean[] {true, true, false, false, false, false};
	static final String[] HEADER_TITLES = new String[] {"Identifier", "Extracted Text",
		"License Name", "Cross Reference URLs", "Comment", "User Defined Columns..."};
	static final int[] COLUMN_WIDTHS = new int[] {15, 120, 50, 80, 80, 50};
	static final boolean[] LEFT_WRAP = new boolean[] {false, false, false, true, true, true};
	static final boolean[] CENTER_NOWRAP = new boolean[] {true, false, true, false, false, false};
	private static final int MAX_CELL_CONTENT_SIZE = 32700;


	/**
	 * @param workbook
	 * @param sheetName
	 * @param version
	 */
	public NonStandardLicensesSheetV1d1(Workbook workbook, String sheetName, String version) {
		super(workbook, sheetName, version);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		try {
			if (sheet == null) {
				return "Worksheet for non-standard Licenses does not exist";
			}
			Row firstRow = sheet.getRow(firstRowNum);
			for (int i = 0; i < NUM_COLS-1; i++) {	// Don't check the user defined column which is always last
				Cell cell = firstRow.getCell(i+firstCellNum);
				if (cell == null ||
						cell.getStringCellValue() == null ||
						!cell.getStringCellValue().equals(HEADER_TITLES[i])) {
					return "Column "+HEADER_TITLES[i]+" missing for non-standard Licenses worksheet";
				}
			}
			// validate rows
			boolean done = false;
			int rowNum = firstRowNum + 1;
			while (!done) {
				Row row = sheet.getRow(rowNum);
				if (row == null || row.getCell(firstCellNum) == null ||
						row.getCell(firstCellNum).getStringCellValue() == null ||
						row.getCell(firstCellNum).getStringCellValue().trim().isEmpty()) {
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
			return "Error in verifying non-standard License work sheet: "+ex.getMessage();
		}
	}

	private String validateRow(Row row) {
		for (int i = 0; i < NUM_COLS; i++) {
			Cell cell = row.getCell(i);
			if (cell == null) {
				if (REQUIRED[i]) {
					return "Required cell "+HEADER_TITLES[i]+" missing for row "+String.valueOf(row.getRowNum());
				}
			} else {
//				if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
//					return "Invalid cell format for "+HEADER_TITLES[i]+" for forw "+String.valueOf(row.getRowNum());
//				}
			}
		}
		return null;
	}

	/*
	 * Create a blank worksheet NOTE: Replaces / deletes existing sheet by the same name
	 */
	public static void create(Workbook wb, String sheetName) {
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

	public void add(String identifier, String extractedTextIn, String licenseName,
			String[] crossRefUrls, String comment) {
		Row row = addRow();
		Cell idCell = row.createCell(IDENTIFIER_COL);
		idCell.setCellValue(identifier);
		Cell extractedTextCell = row.createCell(EXTRACTED_TEXT_COL);
		String extractedText = extractedTextIn;
		if (extractedText == null) {
			extractedText = "";
		}
		if (extractedText.length() > MAX_CELL_CONTENT_SIZE) {
			extractedText = "[WARNING: TRUNCATED]" + extractedText.substring(0, MAX_CELL_CONTENT_SIZE -20);
		}
		extractedTextCell.setCellValue(extractedText);
		if (licenseName != null && !licenseName.isEmpty()) {
			Cell licenseNameCell = row.createCell(LICENSE_NAME_COL);
			licenseNameCell.setCellValue(licenseName);
		}
		if (crossRefUrls != null && crossRefUrls.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(crossRefUrls[0]);
			for (int i = 1; i < crossRefUrls.length; i++) {
				sb.append(", ");
				sb.append(crossRefUrls[i]);
			}
			Cell crossRefCell = row.createCell(CROSS_REF_URL_COL);
			crossRefCell.setCellValue(sb.toString());
		}
		if (comment != null && !comment.isEmpty()) {
			Cell commentCell = row.createCell(COMMENT_COL);
			commentCell.setCellValue(comment);
		}
	}


	public String getIdentifier(int rowNum) {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		Cell idCell = row.getCell(IDENTIFIER_COL);
		if (idCell == null) {
			return null;
		}
		return idCell.getStringCellValue();
	}

	public String getExtractedText(int rowNum) {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		Cell extractedTextCell = row.getCell(EXTRACTED_TEXT_COL);
		if (extractedTextCell == null) {
			return null;
		}
		return extractedTextCell.getStringCellValue();
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.NonStandardLicensesSheet#getLicenseName()
	 */
	@Override
	public String getLicenseName(int rowNum) {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		Cell licenseNameCell = row.getCell(LICENSE_NAME_COL);
		if (licenseNameCell == null) {
			return null;
		}
		return licenseNameCell.getStringCellValue();
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.NonStandardLicensesSheet#getCrossRefUrls()
	 */
	@Override
	public String[] getCrossRefUrls(int rowNum) {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		Cell crossRefUrlsCell = row.getCell(CROSS_REF_URL_COL);
		if (crossRefUrlsCell == null) {
			return null;
		}
		String val = crossRefUrlsCell.getStringCellValue().trim();
		if (val.isEmpty()) {
			return null;
		}
		String[] retval = val.split(",");
		for (int i = 0; i < retval.length; i++) {
			retval[i] = retval[i].trim();
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.NonStandardLicensesSheet#getComment()
	 */
	@Override
	public String getComment(int rowNum) {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		Cell commentCell = row.getCell(COMMENT_COL);
		if (commentCell == null) {
			return null;
		}
		return commentCell.getStringCellValue();
	}
}
