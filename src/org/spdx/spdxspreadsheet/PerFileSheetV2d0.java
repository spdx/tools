/**
 * Copyright (c) 2015 Source Auditor Inc.
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

import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.compare.CompareHelper;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.DoapProject;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxFile.FileType;

/**
 * @author Gary
 *
 */
public class PerFileSheetV2d0 extends PerFileSheet {

	PerFileSheetV2d0(Workbook workbook, String sheetName, String version) {
		super(workbook, sheetName, version);
	}
	static final int NUM_COLS = 17;
	static final int FILE_NAME_COL = 0;
	static final int ID_COL = FILE_NAME_COL + 1;
	static final int PACKAGE_ID_COL = ID_COL + 1;
	static final int FILE_TYPE_COL = PACKAGE_ID_COL + 1;
	static final int CHECKSUMS_COL = FILE_TYPE_COL + 1;
	static final int CONCLUDED_LIC_COL = CHECKSUMS_COL + 1;
	static final int LIC_INFO_IN_FILE_COL = CONCLUDED_LIC_COL + 1;
	static final int LIC_COMMENTS_COL = LIC_INFO_IN_FILE_COL + 1;
	static final int SEEN_COPYRIGHT_COL = LIC_COMMENTS_COL + 1;
	static final int NOTICE_TEXT_COL = SEEN_COPYRIGHT_COL + 1;
	static final int ARTIFACT_OF_PROJECT_COL = NOTICE_TEXT_COL + 1;
	static final int ARTIFACT_OF_HOMEPAGE_COL = ARTIFACT_OF_PROJECT_COL + 1;
	static final int ARTIFACT_OF_PROJECT_URL_COL = ARTIFACT_OF_HOMEPAGE_COL + 1;
	static final int CONTRIBUTORS_COL = ARTIFACT_OF_PROJECT_URL_COL + 1;
	static final int COMMENT_COL = CONTRIBUTORS_COL + 1;
	static final int FILE_DEPENDENCIES_COL = COMMENT_COL + 1;
	static final int USER_DEFINED_COL = FILE_DEPENDENCIES_COL + 1;
	
	static final boolean[] REQUIRED = new boolean[] {true, true, false, true, false, false, 
		false, false, false, false, false, false, false, false, false, false, false};
	static final String[] HEADER_TITLES = new String[] {"File Name", "SPDX Identifier",
		"Package Identifier", "File Type(s)",
		"File Checksum(s)", "License Concluded", "License Info in File", "License Comments",
		"File Copyright Text", "Notice Text", "Artifact of Project", "Artifact of Homepage", 
		"Artifact of URL", "Contributors", "File Comment", "File Dependencies", "User Defined Columns..."};
	static final int[] COLUMN_WIDTHS = new int[] {60, 25, 25, 30, 85, 50, 50, 60,
		70, 70, 35, 60, 60, 60, 60, 60, 60};
	static final boolean[] LEFT_WRAP = new boolean[] {true, false, false, true, true, 
		true, true, true, true, true, true, true, true, true, true, true, true};
	static final boolean[] CENTER_NOWRAP = new boolean[] {false, true, true, false, false, 
		false, false, false, false, false, false, false, false, false, false, false, false};
	
	/**
	 * Hashmap of the file name to SPDX file
	 */
	HashMap<String, SpdxFile> fileCache = new HashMap<String, SpdxFile>();
	
	@SuppressWarnings("deprecation")
	public void add(SpdxFile fileInfo, String pkgId) {
		Row row = addRow();
		if (fileInfo.getId() != null && !fileInfo.getId().isEmpty()) {
			row.createCell(ID_COL).setCellValue(fileInfo.getId());
		}
		if (pkgId != null && !pkgId.isEmpty()) {
			row.createCell(PACKAGE_ID_COL).setCellValue(pkgId);
		}
		if (fileInfo.getArtifactOf() != null && fileInfo.getArtifactOf().length > 0) {
			DoapProject[] projects = fileInfo.getArtifactOf();
			String[] projectNames = new String[projects.length];
			String[] projectHomePages = new String[projects.length];
			String[] projectUrls = new String[projects.length];
			for (int i = 0; i < projects.length; i++) {
				String projectName = projects[i].getName();
				if (projectName == null) {
					projectName = "";
				}
				projectNames[i] = projectName;
				String projectHomePage = projects[i].getHomePage();
				if (projectHomePage == null) {
					projectHomePage = "";
				}
				projectHomePages[i] = projectHomePage;
				String projectUrl = projects[i].getProjectUri();
				if (projectUrl == null) {
					projectUrl = "";
				}
				projectUrls[i] = projectUrl;
			}			
			row.createCell(ARTIFACT_OF_PROJECT_COL).setCellValue(stringsToCsv(projectNames));
			row.createCell(ARTIFACT_OF_HOMEPAGE_COL).setCellValue(stringsToCsv(projectHomePages));
			row.createCell(ARTIFACT_OF_PROJECT_URL_COL).setCellValue(stringsToCsv(projectUrls));
		}
		if (fileInfo.getLicenseConcluded() != null) {
			row.createCell(CONCLUDED_LIC_COL).setCellValue(fileInfo.getLicenseConcluded().toString());
		}
		row.createCell(FILE_NAME_COL).setCellValue(fileInfo.getName());
		if (fileInfo.getChecksums() != null && fileInfo.getChecksums().length > 0) {
			row.createCell(CHECKSUMS_COL).setCellValue(CompareHelper.checksumsToString(fileInfo.getChecksums()));
		}
		row.createCell(FILE_TYPE_COL).setCellValue(
				CompareHelper.fileTypesToString(fileInfo.getFileTypes()));
		if (fileInfo.getLicenseComments() != null && !fileInfo.getLicenseComments().isEmpty()) {
			row.createCell(LIC_COMMENTS_COL).setCellValue(fileInfo.getLicenseComments());
		}
		if (fileInfo.getCopyrightText() != null && !fileInfo.getCopyrightText().isEmpty()) {
			row.createCell(SEEN_COPYRIGHT_COL).setCellValue(fileInfo.getCopyrightText());
		}
		if (fileInfo.getLicenseInfoFromFiles() != null && fileInfo.getLicenseInfoFromFiles().length > 0) {
			row.createCell(LIC_INFO_IN_FILE_COL).setCellValue(PackageInfoSheet.licensesToString(fileInfo.getLicenseInfoFromFiles()));
		}
		if (fileInfo.getComment() != null && !fileInfo.getComment().isEmpty()) {
			row.createCell(COMMENT_COL).setCellValue(fileInfo.getComment());
		}
		if (fileInfo.getFileContributors() != null && fileInfo.getFileContributors().length > 0) {
			row.createCell(CONTRIBUTORS_COL).setCellValue(stringsToCsv(fileInfo.getFileContributors()));	
		}
		if (fileInfo.getFileDependencies() != null && fileInfo.getFileDependencies().length > 0) {
			SpdxFile[] fileDependencies = fileInfo.getFileDependencies();
			String[] fileDependencyNames = new String[fileDependencies.length];
			for (int i = 0; i < fileDependencies.length; i++) {
				fileDependencyNames[i] = fileDependencies[i].getName();
			}
			row.createCell(FILE_DEPENDENCIES_COL).setCellValue(stringsToCsv(fileDependencyNames));
		}
		if (fileInfo.getNoticeText() != null && !fileInfo.getNoticeText().isEmpty()) {
			row.createCell(NOTICE_TEXT_COL).setCellValue(fileInfo.getNoticeText());
		}
	}

	@SuppressWarnings("deprecation")
	public SpdxFile getFileInfo(int rowNum, SpdxDocumentContainer container) throws SpreadsheetException {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		String ver = validateRow(row);
		if (ver != null && !ver.isEmpty()) {
			throw(new SpreadsheetException(ver));
		}
		String name = row.getCell(FILE_NAME_COL).getStringCellValue();
		
		if (this.fileCache.containsKey(name)) {
			return this.fileCache.get(name);
		}
		String typeStr = row.getCell(FILE_TYPE_COL).getStringCellValue();
		FileType[] types;
		try {
			types = CompareHelper.parseFileTypeString(typeStr);
		} catch (InvalidSPDXAnalysisException e1) {
			throw(new SpreadsheetException("Error converting file types: "+e1.getMessage()));
		}
		Cell checksumsCell = row.getCell(CHECKSUMS_COL);
		Checksum[] checksums = new Checksum[0];
		if (checksumsCell != null) {
			try {
				checksums = CompareHelper.strToChecksums(checksumsCell.getStringCellValue());
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpreadsheetException("Error converting file checksums: "+e.getMessage()));
			}
		}
		AnyLicenseInfo fileLicenses;
		Cell assertedLicenseCell = row.getCell(CONCLUDED_LIC_COL);
		if (assertedLicenseCell != null && !assertedLicenseCell.getStringCellValue().isEmpty()) {
			fileLicenses = LicenseInfoFactory.parseSPDXLicenseString(assertedLicenseCell.getStringCellValue(), container);
		} else {
			fileLicenses = null;
		}
		AnyLicenseInfo[] seenLicenses;
		Cell seenLicenseCell = row.getCell(LIC_INFO_IN_FILE_COL);
		if (seenLicenseCell != null && !seenLicenseCell.getStringCellValue().isEmpty()) {
			String[] licenseStrings = seenLicenseCell.getStringCellValue().split(",");
			seenLicenses = new AnyLicenseInfo[licenseStrings.length];
			for (int i = 0; i < licenseStrings.length; i++) {
				seenLicenses[i] = LicenseInfoFactory.parseSPDXLicenseString(licenseStrings[i].trim(), container);
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
		
		//artifactOf
		String[] projectNames = new String[0];
		String[] projectHomePages = new String[0];
		String[] projectUrls = new String[0];
		Cell artifactOfNameCell = row.getCell(ARTIFACT_OF_PROJECT_COL);
		if (artifactOfNameCell != null && !artifactOfNameCell.getStringCellValue().isEmpty()) {
			projectNames = csvToStrings(artifactOfNameCell.getStringCellValue());
		}
		Cell artifactOfHomePageCell = row.getCell(ARTIFACT_OF_HOMEPAGE_COL);
		if (artifactOfHomePageCell != null && !artifactOfHomePageCell.getStringCellValue().isEmpty()) {
			projectHomePages = csvToStrings(artifactOfHomePageCell.getStringCellValue());
		}
		Cell artifactOfUrlCell = row.getCell(ARTIFACT_OF_PROJECT_URL_COL);
		if (artifactOfUrlCell != null && !artifactOfUrlCell.getStringCellValue().isEmpty()) {
			projectUrls = csvToStrings(artifactOfUrlCell.getStringCellValue());
		}
		int numProjects = projectNames.length;

		DoapProject[] projects = new DoapProject[numProjects];
		for (int i = 0; i < numProjects; i++) {
			String homePage = null;
			if (projectHomePages.length > i) {
				homePage = projectHomePages[i];
			}
			projects[i] = new DoapProject(projectNames[i], homePage);
			if (projectUrls.length > i && !projectUrls[i].isEmpty()) {
				try {
					projects[i].setProjectUri(projectUrls[i]);
				} catch (InvalidSPDXAnalysisException e) {
					throw new SpreadsheetException("Error setting the URI for the artifact of");
				}
			}			
		}
		
		SpdxFile[] fileDependencies = new SpdxFile[0];
		Cell fileDependencyCells = row.getCell(FILE_DEPENDENCIES_COL);
		if (fileDependencyCells != null && !fileDependencyCells.getStringCellValue().isEmpty()) {
			String[] fileDependencyNames = csvToStrings(fileDependencyCells.getStringCellValue());
			fileDependencies = new SpdxFile[fileDependencyNames.length];
			for (int i = 0; i < fileDependencyNames.length; i++) {
				fileDependencies[i] = findFileByName(fileDependencyNames[i].trim(), container);
			}
		}
		String[] contributors = new String[0];
		Cell contributorCell = row.getCell(CONTRIBUTORS_COL);
		if (contributorCell != null && !contributorCell.getStringCellValue().trim().isEmpty()) {
			contributors = csvToStrings(contributorCell.getStringCellValue().trim());
		}
		
		String noticeText = null;
		Cell noticeCell = row.getCell(NOTICE_TEXT_COL);
		if (noticeCell != null) {
			noticeText = noticeCell.getStringCellValue().trim();
		}
		
		String comment = null;
		Cell commentCell = row.getCell(COMMENT_COL);
		if (commentCell != null) {
			comment = commentCell.getStringCellValue();
		}

		SpdxFile retval;
		try {
			retval = new SpdxFile(name, comment, new Annotation[0], new Relationship[0], 
					fileLicenses, seenLicenses, copyright, licenseComments, types, 
					checksums, contributors, noticeText, projects);
			Cell idCell = row.getCell(ID_COL);
			if (idCell != null && idCell.getStringCellValue() != null && !idCell.getStringCellValue().isEmpty()) {
				retval.setId(idCell.getStringCellValue());
			}
			retval.setFileDependencies(fileDependencies);
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpreadsheetException("Error creating new SPDX file: "+e.getMessage()));
		}
		this.fileCache.put(name, retval);
		return retval;
	}

	/**
	 * Finds an SPDX file by name by searching through the rows for a matching file name
	 * @param fileName
	 * @return
	 * @throws SpreadsheetException 
	 */
	public SpdxFile findFileByName(String fileName, SpdxDocumentContainer container) throws SpreadsheetException {
		if (this.fileCache.containsKey(fileName)) {
			return this.fileCache.get(fileName);
		}
		for (int i = this.firstRowNum; i < this.lastRowNum+1; i++) {
			Cell fileNameCell = sheet.getRow(i).getCell(FILE_NAME_COL);
			if (fileNameCell.getStringCellValue().trim().equals(fileName)) {
				return getFileInfo(i, container);	//note: this will add the file to the cache
			}
		}
		throw(new SpreadsheetException("Could not find dependant file in the spreadsheet: "+fileName));
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
			for (int i = 0; i < NUM_COLS- 1; i++) { 	// Don't check the last (user defined) column
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
				if (i == CONCLUDED_LIC_COL) {
					try {
						LicenseInfoFactory.parseSPDXLicenseString(cell.getStringCellValue(), null);
					} catch (SpreadsheetException ex) {
						return "Invalid asserted license string in row "+String.valueOf(row.getRowNum()) +
								" details: "+ex.getMessage();
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

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.PerFileSheet#getPackageIds(int)
	 */
	@Override
	public String[] getPackageIds(int row) {
		Cell pkgIdCell = sheet.getRow(row).getCell(PACKAGE_ID_COL);
		if (pkgIdCell == null || pkgIdCell.getStringCellValue() == null ||
				pkgIdCell.getStringCellValue().isEmpty()) {
			return new String[0];
		}
		String[] parts = pkgIdCell.getStringCellValue().split(",");
		String[] retval = new String[parts.length];
		for (int i = 0; i < parts.length; i++) {
			retval[i] = parts[i].trim();
		}
		return retval;
	}
}
