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
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;
import org.spdx.rdfparser.SPDXNoneLicense;
import org.spdx.rdfparser.SPDXPackageInfo;

/**
 * Sheet describing the package information for an SPDX Document
 * @author Gary O'Neall
 *
 */
public class PackageInfoSheet extends AbstractSheet {

	int NUM_COLS = 11;
	int NAME_COL = 0;
	int MACHINE_NAME_COL = NAME_COL+1;
	int URL_COL = MACHINE_NAME_COL + 1;
	int PACKAGE_SHA_COL = URL_COL + 1;
	int FILE_CHECKSUM_COL = PACKAGE_SHA_COL + 1;
	int SOURCE_INFO_COL = FILE_CHECKSUM_COL + 1;
	int DECLARED_LICENSE_COL = SOURCE_INFO_COL + 1;
	int SEEN_LICENSE_COL = DECLARED_LICENSE_COL + 1;
	int DECLARED_COPYRIGHT_COL = SEEN_LICENSE_COL + 1;
	int SHORT_DESC_COL = DECLARED_COPYRIGHT_COL + 1;
	int FULL_DESC_COL = SHORT_DESC_COL + 1;
	
	static final boolean[] REQUIRED = new boolean[] {true, true, true, 
		true, true, false, true, true, true, false, false};
	static final String[] HEADER_TITLES = new String[] {"DeclaredName",
		"MachineName", "Package URL", "Package SHA", "Files Checksum",
		"Source Info", "Declared License(s)", "Seen License(s)", 
		"Declared Copyright", "Short Desc.", "Full Desc."};
	static final int[] COLUMN_WIDTHS = new int[] {20, 20, 30, 15, 15, 30,
		40, 40, 40, 40, 40};

	/**
	 * @param workbook
	 * @param sheetName
	 */
	public PackageInfoSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		try {
			if (sheet == null) {
				return "Worksheet for SPDX Package Info does not exist";
			}
			Row firstRow = sheet.getRow(firstRowNum);
			for (int i = 0; i < NUM_COLS; i++) {
				Cell cell = firstRow.getCell(i+firstCellNum);
				if (cell == null || 
						cell.getStringCellValue() == null ||
						!cell.getStringCellValue().equals(HEADER_TITLES[i])) {
					return "Column "+HEADER_TITLES[i]+" missing for SPDX Package Info worksheet";
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
			return "Error in verifying SPDX Package Info work sheet: "+ex.getMessage();
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
				if (i == DECLARED_LICENSE_COL || i == SEEN_LICENSE_COL) {
					try {
						SPDXLicenseInfoFactory.parseSPDXLicenseString(cell.getStringCellValue());
					} catch(SpreadsheetException ex) {
						if (i == DECLARED_LICENSE_COL) {
							return "Invalid declared license in row "+String.valueOf(row.getRowNum())+" detail: "+ex.getMessage();
						} else {
							return "Invalid seen license in row "+String.valueOf(row.getRowNum())+" detail: "+ex.getMessage();
						}
					}
				}
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
		CellStyle headerStyle = AbstractSheet.createHeaderStyle(wb);		
		Row row = sheet.createRow(0);
		for (int i = 0; i < HEADER_TITLES.length; i++) {
			sheet.setColumnWidth(i, COLUMN_WIDTHS[i]*256);
			Cell cell = row.createCell(i);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(HEADER_TITLES[i]);
		}
	}
	
	public void add(SPDXPackageInfo pkgInfo) {
		Row row = addRow();
		Cell nameCell = row.createCell(NAME_COL);
		nameCell.setCellValue(pkgInfo.getDeclaredName());
		Cell copyrightCell = row.createCell(DECLARED_COPYRIGHT_COL);
		copyrightCell.setCellValue(pkgInfo.getDeclaredCopyright());
		Cell DeclaredLicenseCol = row.createCell(DECLARED_LICENSE_COL);
		DeclaredLicenseCol.setCellValue(pkgInfo.getDeclaredLicenses().toString());
		Cell fileChecksumCell = row.createCell(FILE_CHECKSUM_COL);
		fileChecksumCell.setCellValue(pkgInfo.getFileChecksum());
		if (pkgInfo.getDescription() != null) {
			Cell descCell = row.createCell(FULL_DESC_COL);
			descCell.setCellValue(pkgInfo.getDescription());
		}
		Cell fileNameCell = row.createCell(MACHINE_NAME_COL);
		fileNameCell.setCellValue(pkgInfo.getFileName());
		Cell pkgSha1 = row.createCell(PACKAGE_SHA_COL);
		pkgSha1.setCellValue(pkgInfo.getSha1());
		Cell detectedLicenseCell = row.createCell(SEEN_LICENSE_COL);
		detectedLicenseCell.setCellValue(pkgInfo.getDetectedLicenses().toString());
		if (pkgInfo.getShortDescription() != null) {
			Cell shortDescCell = row.createCell(SHORT_DESC_COL);
			shortDescCell.setCellValue(pkgInfo.getShortDescription());
		}
		if (pkgInfo.getSourceInfo() != null) {
			Cell sourceInfoCell = row.createCell(SOURCE_INFO_COL);
			sourceInfoCell.setCellValue(pkgInfo.getSourceInfo());
		}
		Cell urlCell = row.createCell(URL_COL);
		urlCell.setCellValue(pkgInfo.getUrl());
	}
	
	public SPDXPackageInfo getPackageInfo(int rowNum) throws SpreadsheetException {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		String error = validateRow(row);
		if (error != null && !error.isEmpty()) {
			throw(new SpreadsheetException(error));
		}
		String declaredName = row.getCell(NAME_COL).getStringCellValue();
		String machineName = row.getCell(MACHINE_NAME_COL).getStringCellValue();
		String sha1 = row.getCell(PACKAGE_SHA_COL).getStringCellValue();
		String sourceInfo;
		Cell sourceInfocol = row.getCell(SOURCE_INFO_COL);
		if (sourceInfocol != null) {
			sourceInfo = sourceInfocol.getStringCellValue();
		} else {
			sourceInfo = "";
		}
		SPDXLicenseInfo declaredLicenses = 
				SPDXLicenseInfoFactory.parseSPDXLicenseString(row.getCell(DECLARED_LICENSE_COL).getStringCellValue());
		SPDXLicenseInfo seenLicenses;
		Cell seenLicensesCell = row.getCell(SEEN_LICENSE_COL);
		if (seenLicensesCell != null && !seenLicensesCell.getStringCellValue().isEmpty()) {
			seenLicenses = SPDXLicenseInfoFactory.parseSPDXLicenseString(seenLicensesCell.getStringCellValue());
		} else {
			seenLicenses = new SPDXNoneLicense();
		}
		String declaredCopyright = row.getCell(DECLARED_COPYRIGHT_COL).getStringCellValue();
		Cell shortDescCell = row.getCell(SHORT_DESC_COL);
		String shortDesc;
		if (shortDescCell != null && !shortDescCell.getStringCellValue().isEmpty()) {
			shortDesc = shortDescCell.getStringCellValue();
		} else {
			shortDesc = "";
		}
		Cell descCell = row.getCell(FULL_DESC_COL);
		String description;
		if (descCell != null && !descCell.getStringCellValue().isEmpty()) {
			description = descCell.getStringCellValue();
		} else {
			description = "";
		}
		String url = row.getCell(URL_COL).getStringCellValue();
		String fileChecksums = row.getCell(FILE_CHECKSUM_COL).getStringCellValue();
		return new SPDXPackageInfo(declaredName, machineName, sha1, sourceInfo, 
				declaredLicenses, seenLicenses, declaredCopyright, shortDesc, 
				description, url, fileChecksums);
	}

	public static String licensesToString(SPDXLicenseInfo[] licenses) {
		if (licenses == null || licenses.length == 0) {
			return "";
		} else if (licenses.length == 1) {
			return licenses[0].toString();
		} else {
			StringBuilder sb = new StringBuilder("(");
			sb.append(licenses[0].toString());
			for (int i = 1; i < licenses.length; i++) {
				sb.append(" AND ");
				sb.append(licenses[i].toString());
			}
			sb.append(")");
			return sb.toString();
		}
	}
}
