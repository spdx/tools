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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;

/**
 * Sheet describing the per file information in an SPDX Document
 * @author Gary O'Neall
 *
 */
public class PerFileSheet extends AbstractSheet {

	static final int NUM_COLS = 10;
	static final int FILE_NAME_COL = 0;
	static final int FILE_TYPE_COL = FILE_NAME_COL + 1;
	static final int SHA1_COL = FILE_TYPE_COL + 1;
	static final int CONCLUDED_LIC_COL = SHA1_COL + 1;
	static final int LIC_INFO_IN_FILE_COL = CONCLUDED_LIC_COL + 1;
	static final int LIC_COMMENTS_COL = LIC_INFO_IN_FILE_COL + 1;
	static final int SEEN_COPYRIGHT_COL = LIC_COMMENTS_COL + 1;
	static final int ARTIFACT_OF_PROJECT_COL = SEEN_COPYRIGHT_COL + 1;
	static final int ARTIFACT_OF_HOMEPAGE_COL = ARTIFACT_OF_PROJECT_COL + 1;
	static final int ARTIFACT_OF_PROJECT_URL_COL = ARTIFACT_OF_HOMEPAGE_COL + 1;

	static final boolean[] REQUIRED = new boolean[] {true, true, false, false,
		false, false, false, false, false, false};
	static final String[] HEADER_TITLES = new String[] {"File Name", "File Type",
		"File Checksum", "License Concluded", "License Info in File", "License Comments",
		"File Copyright Text", "Artifact of Project", "Artifact of Homepage",
		"Artifact of URL"};
	static final int[] COLUMN_WIDTHS = new int[] {60, 10, 25, 30, 30, 40,
		40, 25, 60, 60};
	static final boolean[] LEFT_WRAP = new boolean[] {true, false, true,
		true, true, true, true, true, true, true};
	static final boolean[] CENTER_NOWRAP = new boolean[] {false, true, false,
		false, false, false, false, false, false, false};


	@SuppressWarnings("unused")
	private String version;

	/**
	 * @param workbook
	 * @param sheetName
	 * @param version
	 */
	public PerFileSheet(Workbook workbook, String sheetName, String version) {
		super(workbook, sheetName);
		this.version = version;
	}

	public void add(SPDXFile fileInfo) {
		Row row = addRow();
		if (fileInfo.getArtifactOf() != null && fileInfo.getArtifactOf().length > 0) {
			// currently, this is restricted to only 0 or zero
			DOAPProject project = fileInfo.getArtifactOf()[0];
			row.createCell(ARTIFACT_OF_PROJECT_COL).setCellValue(project.getName());
			row.createCell(ARTIFACT_OF_HOMEPAGE_COL).setCellValue(project.getHomePage());
			row.createCell(ARTIFACT_OF_PROJECT_URL_COL).setCellValue(project.getProjectUri());
		}
		if (fileInfo.getConcludedLicenses() != null) {
			row.createCell(CONCLUDED_LIC_COL).setCellValue(fileInfo.getConcludedLicenses().toString());
		}
		row.createCell(FILE_NAME_COL).setCellValue(fileInfo.getName());
		if (fileInfo.getSha1() != null && !fileInfo.getSha1().isEmpty()) {
			row.createCell(SHA1_COL).setCellValue(fileInfo.getSha1());
		}
		row.createCell(FILE_TYPE_COL).setCellValue(fileInfo.getType());
		if (fileInfo.getLicenseComments() != null && !fileInfo.getLicenseComments().isEmpty()) {
			row.createCell(LIC_COMMENTS_COL).setCellValue(fileInfo.getLicenseComments());
		}
		if (fileInfo.getCopyright() != null && !fileInfo.getCopyright().isEmpty()) {
			row.createCell(SEEN_COPYRIGHT_COL).setCellValue(fileInfo.getCopyright());
		}
		if (fileInfo.getSeenLicenses() != null && fileInfo.getSeenLicenses().length > 0) {
			row.createCell(LIC_INFO_IN_FILE_COL).setCellValue(PackageInfoSheetV09d2.licensesToString(fileInfo.getSeenLicenses()));
		}
	}

	public SPDXFile getFileInfo(int rowNum) throws SpreadsheetException {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		String ver = validateRow(row);
		if (ver != null && !ver.isEmpty()) {
			throw(new SpreadsheetException(ver));
		}
		String name = row.getCell(FILE_NAME_COL).getStringCellValue();
		String type = row.getCell(FILE_TYPE_COL).getStringCellValue();
		Cell sha1cell = row.getCell(SHA1_COL);
		String sha1;
		if (sha1cell != null) {
			sha1 = sha1cell.getStringCellValue();
		} else {
			sha1 = "";
		}
		SPDXLicenseInfo fileLicenses;
		Cell assertedLicenseCell = row.getCell(CONCLUDED_LIC_COL);
		if (assertedLicenseCell != null && !assertedLicenseCell.getStringCellValue().isEmpty()) {
			fileLicenses = SPDXLicenseInfoFactory.parseSPDXLicenseString(assertedLicenseCell.getStringCellValue());
		} else {
			fileLicenses = null;
		}
		SPDXLicenseInfo[] seenLicenses;
		Cell seenLicenseCell = row.getCell(LIC_INFO_IN_FILE_COL);
		if (seenLicenseCell != null && !seenLicenseCell.getStringCellValue().isEmpty()) {
			String[] licenseStrings = seenLicenseCell.getStringCellValue().split(",");
			seenLicenses = new SPDXLicenseInfo[licenseStrings.length];
			for (int i = 0; i < licenseStrings.length; i++) {
				seenLicenses[i] = SPDXLicenseInfoFactory.parseSPDXLicenseString(licenseStrings[i].trim());
			}
		} else {
			seenLicenses = null;
		}
		String licenseComments;
		Cell licCommentCell = row.getCell(LIC_COMMENTS_COL);
		if (licCommentCell != null) {
			licenseComments = licCommentCell.getStringCellValue();
		} else {
			licenseComments = "";
		}
		String copyright;
		Cell copyrightCell = row.getCell(SEEN_COPYRIGHT_COL);
		if (copyrightCell != null) {
			copyright = copyrightCell.getStringCellValue();
		} else {
			copyright = "";
		}
		DOAPProject[] artifactOf;
		Cell artifactOfCell = row.getCell(ARTIFACT_OF_PROJECT_COL);
		if (artifactOfCell != null && !artifactOfCell.getStringCellValue().isEmpty()) {
			String projectName = artifactOfCell.getStringCellValue();
			String homePage = "";
			Cell homePageCell = row.getCell(ARTIFACT_OF_HOMEPAGE_COL);
			if (homePageCell != null) {
				homePage = homePageCell.getStringCellValue();
			}
			Cell uriCell = row.getCell(ARTIFACT_OF_PROJECT_URL_COL);
			String uri = "";
			if (uriCell != null) {
				uri = uriCell.getStringCellValue();
			}
			DOAPProject project = new DOAPProject(projectName, homePage);
			if (uri != null && !uri.isEmpty()) {
				try {
					project.setUri(uri);
				} catch (InvalidSPDXAnalysisException e) {
					throw new SpreadsheetException("Error setting the URI for the artifact of");
				}
			}
			artifactOf = new DOAPProject[] {project};
		} else {
			artifactOf = new DOAPProject[0];
		}
		return new SPDXFile(name, type, sha1, fileLicenses,
				seenLicenses, licenseComments, copyright, artifactOf);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		try {
			if (sheet == null) {
				return "Worksheet for SPDX File does not exist";
			}
			Row firstRow = sheet.getRow(firstRowNum);
			for (int i = 0; i < NUM_COLS; i++) {
				Cell cell = firstRow.getCell(i+firstCellNum);
				if (cell == null ||
						cell.getStringCellValue() == null ||
						!cell.getStringCellValue().equals(HEADER_TITLES[i])) {
					return "Column "+HEADER_TITLES[i]+" missing for SPDX File worksheet";
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
			return "Error in verifying SPDX File work sheet: "+ex.getMessage();
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
				if (i == CONCLUDED_LIC_COL || i == LIC_INFO_IN_FILE_COL) {
					try {
						SPDXLicenseInfoFactory.parseSPDXLicenseString(cell.getStringCellValue());
					} catch (SpreadsheetException ex) {
						if (i == CONCLUDED_LIC_COL) {
							return "Invalid asserted license string in row "+String.valueOf(row.getRowNum()) +
									" details: "+ex.getMessage();
						} else {
							return "Invalid seen license string in row "+String.valueOf(row.getRowNum()) +
							" details: "+ex.getMessage();
						}
					}
				}
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
}
