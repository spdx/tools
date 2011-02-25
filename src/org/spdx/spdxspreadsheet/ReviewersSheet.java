/**
 * Copyright (c) 2011 Source Auditor Inc.
* Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.spdx.spdxspreadsheet;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.AbstractSheet;

/**
 * Sheet describing any reviewers for an SPDX Document
 * @author Gary O'Neall
 *
 */
public class ReviewersSheet extends AbstractSheet {

	static final int NUM_COLS = 2;
	static final int REVIEWER_COL = 0;
	static final int TIMESTAMP_COL = REVIEWER_COL + 1;
	static final String[] HEADER_TITLES = new String[] {"Reviewer", "Timestamp"};
	
	public ReviewersSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}

	@Override
	public String verify() {
		try {
			if (sheet == null) {
				return "Worksheet for SPDX Reviewers does not exist";
			}
			Row firstRow = sheet.getRow(firstRowNum);
			for (int i = 0; i < NUM_COLS; i++) {
				Cell cell = firstRow.getCell(i+firstCellNum);
				if (cell == null || 
						cell.getStringCellValue() == null ||
						!cell.getStringCellValue().equals(HEADER_TITLES[i])) {
					return "Column "+HEADER_TITLES[i]+" missing for SPDX Reviewers worksheet";
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
			return "Error in verifying SPDX Reviewers worksheet: "+ex.getMessage();
		}
	}

	private String validateRow(Row row) {
		for (int i = 0; i < NUM_COLS; i++) {
			Cell cell = row.getCell(i);
			if (cell == null) {
				return "Required cell "+HEADER_TITLES[i]+" missing for row "+String.valueOf(row.getRowNum());
			} else {
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
		Sheet sheet = wb.createSheet(sheetName);
		Row row = sheet.createRow(0);
		for (int i = 0; i < HEADER_TITLES.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(HEADER_TITLES[i]);
		}
	}
	
	public void addReviewer(String reviewer, Date timeStamp) {
		Row row = addRow();
		row.createCell(REVIEWER_COL).setCellValue(reviewer);
		row.createCell(TIMESTAMP_COL).setCellValue(timeStamp);
	}
	
	public String getReviewer(int rowNum) {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		Cell reviewer = row.getCell(REVIEWER_COL);
		if (reviewer == null) {
			return null;
		}
		return reviewer.getStringCellValue();
	}
	
	public Date getReviewerTimestampe(int rowNum) {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		Cell tsCell = row.getCell(TIMESTAMP_COL);
		if (tsCell == null) {
			return null;
		}
		return tsCell.getDateCellValue();
	}
}