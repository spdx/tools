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
package org.spdx.rdfparser;
import org.apache.poi.ss.usermodel.CellStyle;
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
	static final short FONT_SIZE = (short)10*20;
	static final String CHECKBOX_FONT_NAME = "Wingdings 2";
	static final String CHECKBOX = "P";
	protected CellStyle checkboxStyle;
	
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
}
