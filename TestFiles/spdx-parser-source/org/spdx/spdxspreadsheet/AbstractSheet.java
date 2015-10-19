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

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
/**
 * Abstract class representing a workbook sheet used in storing structured data
 * @author Gary O'Neall
 *
 */
public abstract class AbstractSheet {
	// Default style for cells
	static final String FONT_NAME = "Arial";
	protected static final short FONT_SIZE = (short)10*20;
	static final String CHECKBOX_FONT_NAME = "Wingdings 2";
	static final String CHECKBOX = "P";
	protected CellStyle checkboxStyle;
	protected CellStyle dateStyle;

	protected Sheet sheet;
	protected int lastRowNum;
	protected int firstCellNum;
	protected int firstRowNum;
	/**
	 * @param workbook
	 * @param sheetName
	 */
	public AbstractSheet(Workbook workbook, String sheetName) {
		sheet = workbook.getSheet(sheetName);
		if (sheet != null) {
			firstRowNum = sheet.getFirstRowNum();
			Row firstRow = sheet.getRow(firstRowNum);
			if (firstRow == null) {
				firstCellNum = 1;
			} else {
				firstCellNum = firstRow.getFirstCellNum();
			}
			findLastRow();
		} else {
			firstRowNum = 0;
			lastRowNum = 0;
			firstCellNum = 0;
		}
		createStyles(workbook);
	}

	/**
	 * create the styles in the workbook
	 */
	private void createStyles(Workbook wb) {
		// create the styles
		this.checkboxStyle = wb.createCellStyle();
		this.checkboxStyle.setAlignment(CellStyle.ALIGN_CENTER);
		this.checkboxStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		this.checkboxStyle.setBorderBottom(CellStyle.BORDER_THIN);
		this.checkboxStyle.setBorderLeft(CellStyle.BORDER_THIN);
		this.checkboxStyle.setBorderRight(CellStyle.BORDER_THIN);
		this.checkboxStyle.setBorderTop(CellStyle.BORDER_THIN);
		Font checkboxFont = wb.createFont();
		checkboxFont.setFontHeight(FONT_SIZE);
		checkboxFont.setFontName(CHECKBOX_FONT_NAME);
		this.checkboxStyle.setFont(checkboxFont);

		this.dateStyle = wb.createCellStyle();
		DataFormat df = wb.createDataFormat();
		this.dateStyle.setDataFormat(df.getFormat("m/d/yy h:mm"));
	}

	/**
	 *
	 */
	private void findLastRow() {
		boolean done = false;
		lastRowNum = firstRowNum + 1;
		try {
			while (!done) {
				Row row = sheet.getRow(lastRowNum);
				if (row == null || row.getCell(firstCellNum) == null ||
						row.getCell(firstCellNum).getStringCellValue() == null ||
						row.getCell(firstCellNum).getStringCellValue().isEmpty()) {
					lastRowNum--;
					done = true;
				} else {
					lastRowNum++;
				}
			}
		}
		catch (Exception ex) {
			// we just stop - stop counting rows at the first invalid row
		}
	}

	/**
	 * Add a new row to the end of the sheet
	 * @return new row
	 */
	protected Row addRow() {
		lastRowNum++;
		Row row = sheet.createRow(lastRowNum);
		return row;
	}

	/**
	 * Clears all data from the worksheet
	 */
	public void clear() {
		for (int i = lastRowNum; i > firstRowNum; i--) {
			Row row = sheet.getRow(i);
			sheet.removeRow(row);
		}
		lastRowNum = firstRowNum;
	}

	public int getFirstDataRow() {
		return this.firstRowNum + 1;
	}

	public int getNumDataRows() {
		return this.lastRowNum - (this.firstRowNum);
	}

	public Sheet getSheet() {
		return this.sheet;
	}

	public abstract String verify();

	public static CellStyle createHeaderStyle(Workbook wb) {
		CellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font headerFont = wb.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeight(FONT_SIZE);
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerStyle.setFont(headerFont);
		headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
		return headerStyle;
	}

	public static CellStyle createLeftWrapStyle(Workbook wb) {
		CellStyle wrapStyle = wb.createCellStyle();
		wrapStyle.setWrapText(true);
		wrapStyle.setAlignment(CellStyle.ALIGN_LEFT);
		return wrapStyle;
	}

	public static CellStyle createCenterStyle(Workbook wb) {
		CellStyle centerStyle = wb.createCellStyle();
		centerStyle.setWrapText(false);
		centerStyle.setAlignment(CellStyle.ALIGN_CENTER);
		return centerStyle;
	}
}
