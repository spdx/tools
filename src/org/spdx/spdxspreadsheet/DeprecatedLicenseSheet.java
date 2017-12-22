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

import java.io.File;
import java.text.SimpleDateFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.licenseTemplate.LicenseTemplateRuleException;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.SpdxListedLicense;
/**
 * Sheet holding information about deprecated licenses
 * @author Gary O'Neall
 *
 */
public class DeprecatedLicenseSheet extends AbstractSheet {

	static final Logger logger = LoggerFactory.getLogger(LicenseSheet.class.getName());
	static final int NUM_COLS = 8;
	static final int COL_NAME = 0;
	static final int COL_ID = COL_NAME + 1;
	static final int COL_SOURCE_URL = COL_ID + 1;
	static final int COL_NOTES = COL_SOURCE_URL + 1;
	static final int COL_OSI_APPROVED = COL_NOTES + 1;	
	static final int COL_STANDARD_LICENSE_HEADER = COL_OSI_APPROVED + 1;
	static final int COL_TEMPLATE = COL_STANDARD_LICENSE_HEADER + 1;
	static final int COL_DEPRECATED_VERSION = COL_TEMPLATE + 1;
	
	static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");

	static final boolean[] REQUIRED = new boolean[] {true, true, false, false,
		false, false, true, false, true};
	static final String[] HEADER_TITLES = new String[] {"Full name of License", "License Identifier", "Source/url", "Notes on Deprecation", 
		"OSI Approved", "Standard License Header", "Template", "Deprecated as of:"};
	
	static final String TEXT_EXTENSION = ".txt";
	static final String ENCODING = "UTF-8";
	String workbookPath;
	
	public DeprecatedLicenseSheet(Workbook workbook, String sheetName, File workbookFile) {
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
	
	/**
	 * Add a new row to the spreadsheet
	 * @param license SPDX standard license
	 * @param deprecatedVersion Version of the license list this license was first deprecated
	 */
	public void add(SpdxListedLicense license, String deprecatedVersion) {
		Row row = addRow();
		Cell nameCell = row.createCell(COL_NAME);
		nameCell.setCellValue(license.getName());
		Cell idCell = row.createCell(COL_ID);
		idCell.setCellValue(license.getLicenseId());
		if (license.getSeeAlso() != null && license.getSeeAlso().length > 0) {
			Cell sourceUrlCell = row.createCell(COL_SOURCE_URL);
			StringBuilder sb = new StringBuilder();
			sb.append(license.getSeeAlso()[0]);
			for (int i = 1; i < license.getSeeAlso().length; i++) {
				sb.append(' ');
				sb.append(license.getSeeAlso()[i]);
			}
			sourceUrlCell.setCellValue(sb.toString());
		}
		if (license.getComment() != null) {
			Cell notesCell = row.createCell(COL_NOTES);
			notesCell.setCellValue(license.getComment());
		}
		if (license.getStandardLicenseHeader() != null) {
			Cell standardLicenseHeaderCell = row.createCell(COL_STANDARD_LICENSE_HEADER);
			standardLicenseHeaderCell.setCellValue(license.getStandardLicenseHeader());
		}
		Cell templateCell = row.createCell(COL_TEMPLATE);
		String templateText = license.getStandardLicenseTemplate();
		if (templateText == null || templateText.trim().isEmpty()) {
			templateText = license.getLicenseText();
		}
		LicenseSheet.setTemplateText(templateCell, templateText, license.getLicenseId(), workbookPath, workbook);
		if (license.isOsiApproved()) {
			Cell osiApprovedCell = row.createCell(COL_OSI_APPROVED);
			osiApprovedCell.setCellValue("YES");
		}
		Cell deprecatedVersionCell = row.createCell(COL_DEPRECATED_VERSION);
		deprecatedVersionCell.setCellValue(deprecatedVersion);
	}
	
	/**
	 * Retrieve a license for a specific row in the sheet
	 * @param rowNum
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxListedLicense getLicense(int rowNum) throws InvalidSPDXAnalysisException {
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
		String stdLicHeader = null;
		Cell stdLicHeaderCell = row.getCell(COL_STANDARD_LICENSE_HEADER);
		if (stdLicHeaderCell != null) {
			stdLicHeader = stdLicHeaderCell.getStringCellValue();
		}
		String template = null;
		String text = null;
		Cell templateCell = row.getCell(COL_TEMPLATE);
		if (templateCell != null) {
			template = LicenseSheet.getLicenseTemplateText(templateCell, this.workbookPath);
			try {
				text = SpdxLicenseTemplateHelper.templateToText(template);
			} catch (LicenseTemplateRuleException e) {
				throw(new InvalidSPDXAnalysisException("Invalid template for "+id+": "+e.getMessage(),e));
			}
		}
		boolean osiApproved = false;
		Cell osiApprovedCell = row.getCell(COL_OSI_APPROVED);
		if (osiApprovedCell != null) {
			String osiApprovedStr = osiApprovedCell.getStringCellValue();
			if (osiApprovedStr != null && !osiApprovedStr.isEmpty() && osiApprovedStr.toUpperCase().trim().charAt(0) == 'Y') {
				osiApproved = true;
			}
		}
		return new SpdxListedLicense(name, id, text, sourceURL, notes, stdLicHeader, template, osiApproved);
	}
	
	/**
	 * Get the version the license was deprecated in (deprecated from)
	 * @param rowNum
	 * @return
	 */
	public String getDeprecatedVersion(int rowNum) {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		Cell deprecatedVersionCell = row.getCell(COL_DEPRECATED_VERSION);
		if (deprecatedVersionCell == null) {
			return null;
		}
		return deprecatedVersionCell.getStringCellValue();
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		try {
			if (sheet == null) {
				return "Worksheet for Deprecated Licenses does not exist";
			}
			Row firstRow = sheet.getRow(firstRowNum);
			for (int i = 0; i < NUM_COLS; i++) {
				Cell cell = firstRow.getCell(i+firstCellNum);
				if (cell == null || 
						cell.getStringCellValue() == null ||
						!cell.getStringCellValue().equals(HEADER_TITLES[i])) {
					return "Column "+HEADER_TITLES[i]+" missing for Deprecated Licenses worksheet";
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
			return "Error in verifying Deprecated License work sheet: "+ex.getMessage();
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

}
