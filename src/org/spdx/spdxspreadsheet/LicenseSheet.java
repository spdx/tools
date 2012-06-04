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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXStandardLicense;

/**
 * Spreadsheet holding SPDX Licenses
 * The text column can contain either the text for the license or a hyperlink to a file containing the text.
 * The text file location is relative to the directory containing the spreadsheet.
 * @author Gary O'Neall
 *
 */
public class LicenseSheet extends AbstractSheet {
	
	static final Logger logger = Logger.getLogger(LicenseSheet.class.getName());
	static final int NUM_COLS = 7;
	static final int COL_NAME = 0;
	static final int COL_ID = COL_NAME + 1;
	static final int COL_SOURCE_URL = COL_ID + 1;
	static final int COL_NOTES = COL_SOURCE_URL + 1;
	static final int COL_OSI_APPROVED = COL_NOTES + 1;	
	static final int COL_STANDARD_LICENSE_HEADER = COL_OSI_APPROVED + 1;
	static final int COL_TEXT = COL_STANDARD_LICENSE_HEADER + 1;
//	static final int COL_TEMPLATE = COL_TEXT + 1;

	static final boolean[] REQUIRED = new boolean[] {true, true, false, false,
		false, false, true, false};
	static final String[] HEADER_TITLES = new String[] {"Full name of License", "License Identifier", "Source/url", "Notes", 
		"OSI Approved", "Standard License Header", "Text", "Template"};
	
	static final String TEXT_EXTENSION = ".txt";
	static final String ENCODING = "UTF-8";
	String workbookPath;
	
	public LicenseSheet(Workbook workbook, String sheetName, File workbookFile) {
		super(workbook, sheetName);
		workbookPath = workbookFile.getParent();
	}
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
	
	public void add(SPDXStandardLicense license) {
		Row row = addRow();
		Cell nameCell = row.createCell(COL_NAME);
		nameCell.setCellValue(license.getName());
		Cell idCell = row.createCell(COL_ID);
		idCell.setCellValue(license.getId());
		if (license.getSourceUrl() != null) {
			Cell sourceUrlCell = row.createCell(COL_SOURCE_URL);
			sourceUrlCell.setCellValue(license.getSourceUrl());
		}
		if (license.getNotes() != null) {
			Cell notesCell = row.createCell(COL_NOTES);
			notesCell.setCellValue(license.getNotes());
		}
		if (license.getStandardLicenseHeader() != null) {
			Cell standardLicenseHeaderCell = row.createCell(COL_STANDARD_LICENSE_HEADER);
			standardLicenseHeaderCell.setCellValue(license.getStandardLicenseHeader());
		}
		Cell textCell = row.createCell(COL_TEXT);
		setLicenseText(textCell, license.getText(), license.getId());
//		if (license.getTemplate() != null) {
//			Cell templateCell = row.createCell(COL_TEMPLATE);
//			templateCell.setCellValue(license.getTemplate());
//		}
		if (license.isOsiApproved()) {
			Cell osiApprovedCell = row.createCell(COL_OSI_APPROVED);
			osiApprovedCell.setCellValue("YES");
		}
	}
	
	/**
	 * Adds a license text to a text cell.  First attempts to create a file and
	 * a hyperlink to the file.  If that fails, will attempt to add the text
	 * directly to the file.
	 * 
	 * @param textCell
	 * @param text
	 */
	private void setLicenseText(Cell textCell, String text, String licenseId) {
		String licenseFileName = licenseId + TEXT_EXTENSION;
		File licenseTextFile = new File(this.workbookPath + File.separator + licenseFileName);
		try {
			if (!licenseTextFile.createNewFile()) {
				logger.warn("Unable to create license text file "+licenseTextFile.getName());
				textCell.setCellValue(text);
				return;
			}
		} catch (IOException e) {
			logger.warn("IO Error creating license text file: "+e.getMessage());
			textCell.setCellValue(text);
			return;
		}
		if (!licenseTextFile.canWrite()) {
			logger.warn("Can not write to text file "+licenseTextFile.getName());
			textCell.setCellValue(text);
			return;
		}
		try {
			Writer out = new OutputStreamWriter(new FileOutputStream(licenseTextFile), ENCODING);
			try {
				out.write(text);
				// add in the hyperlink
				textCell.setCellValue(licenseFileName);
				HSSFHyperlink hyperlink = new HSSFHyperlink(HSSFHyperlink.LINK_FILE);
				hyperlink.setAddress(licenseFileName);
				textCell.setHyperlink(hyperlink);
			} finally {
				out.close();
			}
		} catch (IOException e) {
			logger.warn("Unable to open text file for output: "+e.getMessage());
			textCell.setCellValue(text);
			return;
		}
	}
	
	/**
	 * Retrieve the text from a license text cell either through the hyperlink
	 * to a text file in a directory local to the spreadsheet or from the cell 
	 * itself if there is no hyperlink present
	 * @param textCell
	 * @return
	 */
	private String getLicenseText(Cell textCell) {
		String localFileName = null;
		if (textCell.getHyperlink() != null && textCell.getHyperlink().getAddress() != null) {
			localFileName = textCell.getHyperlink().getAddress();
		} else if (textCell.getStringCellValue() != null && textCell.getStringCellValue().toUpperCase().endsWith(".TXT")) {
			localFileName = textCell.getStringCellValue();
		}
		if (localFileName != null) {
			File licenseTextFile = new File(this.workbookPath + File.separator + localFileName);
			if (!licenseTextFile.exists()) {
				logger.warn("Can not find linked license text file "+licenseTextFile.getName());
				return("WARNING: Could not find license text file "+licenseTextFile.getName());
			}
			if (!licenseTextFile.canRead()) {
				logger.warn("Can not read linked license text file "+licenseTextFile.getName());
				return("WARNING: Could not read license text file "+licenseTextFile.getName());
			}
			try {
				InputStream in = new FileInputStream(licenseTextFile);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, ENCODING));
				try {
					StringBuilder sb = new StringBuilder();
					String line = null;
					String newLine = System.getProperty("line.separator");
					line = reader.readLine();
					if (line != null) {
						sb.append(line);
					}
					while ((line = reader.readLine()) != null) {
						sb.append(newLine);
						sb.append(line);
					}
					return sb.toString();
				} finally {
					reader.close();
				}
			} catch (IOException e) {
				logger.warn("Error reading linked license text file "+licenseTextFile.getName()+": "+e.getMessage());
				return("WARNING: Error reading license text file "+licenseTextFile.getName());
			}
		} else {	// no file name
			return textCell.getStringCellValue();
		}
	}
	
	/**
	 * Retrieve a license for a specific row in the sheet
	 * @param rowNum
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	public SPDXStandardLicense getLicense(int rowNum) throws InvalidSPDXAnalysisException {
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
		String sourceURL = null;
		Cell sourceURLCell = row.getCell(COL_SOURCE_URL);
		if (sourceURLCell != null) {
			try {
				sourceURL = sourceURLCell.getStringCellValue();
			} catch (Exception ex) {
				sourceURL = "Exception getting URL: "+ex.getMessage();
			}
		}
		String stdLicHeader = null;
		Cell stdLicHeaderCell = row.getCell(COL_STANDARD_LICENSE_HEADER);
		if (stdLicHeaderCell != null) {
			stdLicHeader = stdLicHeaderCell.getStringCellValue();
		}
		String template = null;
//		Cell templateCell = row.getCell(COL_TEMPLATE);
//		if (templateCell != null) {
//			template = templateCell.getStringCellValue();
//		}
		String text = null;
		Cell textCell = row.getCell(COL_TEXT);
		if (textCell != null) {
			text = getLicenseText(textCell);
		}
		boolean osiApproved = false;
		Cell osiApprovedCell = row.getCell(COL_OSI_APPROVED);
		if (osiApprovedCell != null) {
			String osiApprovedStr = osiApprovedCell.getStringCellValue();
			if (osiApprovedStr != null && !osiApprovedStr.isEmpty() && osiApprovedStr.toUpperCase().trim().charAt(0) == 'Y') {
				osiApproved = true;
			}
		}
		return new SPDXStandardLicense(name, id, text, sourceURL, notes, stdLicHeader, template, osiApproved);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		try {
			if (sheet == null) {
				return "Worksheet for SPDX Licenses does not exist";
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
			return "Error in verifying SPDX License work sheet: "+ex.getMessage();
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

}
