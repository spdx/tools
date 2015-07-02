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
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.licenseTemplate.LicenseTemplateRuleException;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.SpdxListedLicense;

/**
 * Spreadsheet holding SPDX Licenses
 * The text column can contain either the text for the license or a hyperlink to a file containing the text.
 * The text file location is relative to the directory containing the spreadsheet.
 * @author Gary O'Neall
 *
 */
public class LicenseSheet extends AbstractSheet {
	
	static final Logger logger = Logger.getLogger(LicenseSheet.class.getName());
	static final int NUM_COLS = 9;
	static final int COL_NAME = 0;
	static final int COL_ID = COL_NAME + 1;
	static final int COL_SOURCE_URL = COL_ID + 1;
	static final int COL_NOTES = COL_SOURCE_URL + 1;
	static final int COL_OSI_APPROVED = COL_NOTES + 1;	
	static final int COL_STANDARD_LICENSE_HEADER = COL_OSI_APPROVED + 1;
	static final int COL_TEMPLATE = COL_STANDARD_LICENSE_HEADER + 1;
	static final int COL_VERSION = COL_TEMPLATE + 1;
	static final int COL_RELEASE_DATE = COL_VERSION + 1;

	static final boolean[] REQUIRED = new boolean[] {true, true, false, false,
		false, false, true, false, false, false};
	static final String[] HEADER_TITLES = new String[] {"Full name of License", "License Identifier", "Source/url", "Notes", 
		"OSI Approved", "Standard License Header", "Template", "License List Version", "License List Release Date"};
	
	static final String TEXT_EXTENSION = ".txt";
	static final String ENCODING = "UTF-8";
	String workbookPath;
	String version = null;
	String releaseDate = null;
	
	public LicenseSheet(Workbook workbook, String sheetName, File workbookFile) {
		super(workbook, sheetName);
		workbookPath = workbookFile.getParent();
		Row firstDataRow = sheet.getRow(firstRowNum + 1);
		if (firstDataRow != null) {
			// fill in versions
			Cell versionCell = firstDataRow.getCell(COL_VERSION);
			if (versionCell != null) {
				if (versionCell.getCellType() == Cell.CELL_TYPE_STRING) {
					version = versionCell.getStringCellValue();
				} else if (versionCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					version = String.valueOf(versionCell.getNumericCellValue());
				}
			}
			Cell releaseDateCell = firstDataRow.getCell(COL_RELEASE_DATE);
			if (releaseDateCell != null) {
				if (releaseDateCell.getCellType() == Cell.CELL_TYPE_STRING) {
					this.releaseDate = releaseDateCell.getStringCellValue();
				} else if (releaseDateCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
					this.releaseDate = dateFormat.format(releaseDateCell.getDateCellValue());
				}
			}
		}
	}
	/**
	 * Create a blank worksheet NOTE: Replaces / deletes existing sheet by the same name
	 */
	public static void create(Workbook wb, String sheetName, String version, String releaseDate) {
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
		Row firstDataRow = sheet.createRow(1);
		Cell versionCell = firstDataRow.createCell(COL_VERSION);
		versionCell.setCellValue(version);
		Cell releaseDateCell = firstDataRow.createCell(COL_RELEASE_DATE);
		releaseDateCell.setCellValue(releaseDate);
	}
	
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @return the releaseDate
	 */
	public String getReleaseDate() {
		return releaseDate;
	}
	public void add(SpdxListedLicense license) {
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
		setTemplateText(templateCell, license.getStandardLicenseTemplate(), license.getLicenseId(), workbookPath);
		if (license.isOsiApproved()) {
			Cell osiApprovedCell = row.createCell(COL_OSI_APPROVED);
			osiApprovedCell.setCellValue("YES");
		}
		if (row.getRowNum() == firstRowNum + 1) {
			// need to add version release date
			Cell versionCell = row.createCell(COL_VERSION);
			versionCell.setCellValue(this.version);
			Cell releaseDateCell = row.createCell(COL_RELEASE_DATE);
			releaseDateCell.setCellValue(this.releaseDate);
		}
	}
	
	/**
	 * Adds a license template to a text cell.  First attempts to create a file and
	 * a hyperlink to the file.  If that fails, will attempt to add the text
	 * directly to the file.
	 * 
	 * @param textCell
	 * @param text
	 */
	public static void setTemplateText(Cell textCell, String text, String licenseId, String textFilePath) {
		String licenseFileName = licenseId + TEXT_EXTENSION;
		File licenseTextFile = new File(textFilePath + File.separator + licenseFileName);
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
	public static String getLicenseTemplateText(Cell textCell, String textFilePath) {
		String localFileName = null;
		File licenseTemplateTextFile = null;
		Hyperlink cellHyperlink = textCell.getHyperlink();
		if (cellHyperlink != null && cellHyperlink.getAddress() != null) {
			localFileName = cellHyperlink.getAddress();
			licenseTemplateTextFile = new File(textFilePath + File.separator + localFileName);
			if (!licenseTemplateTextFile.exists()) {
				// try without the workbook path
				licenseTemplateTextFile = new File(localFileName);
			}
			if (!licenseTemplateTextFile.exists()) {
				licenseTemplateTextFile = null;
			}
		} 
		if (licenseTemplateTextFile == null && textCell.getStringCellValue() != null && textCell.getStringCellValue().toUpperCase().endsWith(".TXT")) {
			localFileName = textCell.getStringCellValue();
			licenseTemplateTextFile = new File(textFilePath + File.separator + localFileName);
		}
		if (localFileName != null) {
			if (!licenseTemplateTextFile.exists()) {
				logger.warn("Can not find linked license text file "+licenseTemplateTextFile.getName());
				return("WARNING: Could not find license text file "+licenseTemplateTextFile.getName());
			}
			if (!licenseTemplateTextFile.canRead()) {
				logger.warn("Can not read linked license text file "+licenseTemplateTextFile.getName());
				return("WARNING: Could not read license text file "+licenseTemplateTextFile.getName());
			}
			try {
				InputStream in = new FileInputStream(licenseTemplateTextFile);
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
				logger.warn("Error reading linked license template text file "+licenseTemplateTextFile.getName()+": "+e.getMessage());
				return("WARNING: Error reading license template text file "+licenseTemplateTextFile.getName());
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
			template = getLicenseTemplateText(templateCell, this.workbookPath);
			try {
				text = convertTemplateToText(template);
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
	 * Convert a template to a standard SPDX text
	 * @param template
	 * @return
	 * @throws LicenseTemplateRuleException 
	 */
	private String convertTemplateToText(String template) throws LicenseTemplateRuleException {
		return SpdxLicenseTemplateHelper.templateToText(template);
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
			Row firstDataRow = sheet.getRow(firstRowNum + 1);
			Cell versionCell = firstDataRow.getCell(COL_VERSION);
			if (versionCell == null) {
				return "No version";
			}
			
			Cell releaseDateCell = firstDataRow.getCell(COL_RELEASE_DATE);
			if (releaseDateCell == null) {
				return "No release date";
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
