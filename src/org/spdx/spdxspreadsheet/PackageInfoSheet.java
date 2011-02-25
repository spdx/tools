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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.AbstractSheet;
import org.spdx.rdfparser.LicenseDeclaration;
import org.spdx.rdfparser.SPDXPackageInfo;
import org.spdx.rdfparser.SpreadsheetException;

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
						parseLicenseString(cell.getStringCellValue());
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
		Row row = sheet.createRow(0);
		for (int i = 0; i < HEADER_TITLES.length; i++) {
			Cell cell = row.createCell(i);
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
		DeclaredLicenseCol.setCellValue(licensesToString(pkgInfo.getDeclaredLicenses()));
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
		detectedLicenseCell.setCellValue(licensesToString(pkgInfo.getDetectedLicenses()));
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
		LicenseDeclaration[] declaredLicenses = parseLicenseString(row.getCell(DECLARED_LICENSE_COL).getStringCellValue());
		LicenseDeclaration[] seenLicenses;
		Cell seenLicensesCell = row.getCell(SEEN_LICENSE_COL);
		if (seenLicensesCell != null && !seenLicensesCell.getStringCellValue().isEmpty()) {
			seenLicenses = parseLicenseString(seenLicensesCell.getStringCellValue());
		} else {
			seenLicenses = new LicenseDeclaration[0];
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
	
	public static LicenseDeclaration[] parseLicenseString(String licenseString) throws SpreadsheetException {
		String[] conjunctiveLicenses = licenseString.split("\\Wand(\\W|\\()");
		LicenseDeclaration[] retval = new LicenseDeclaration[conjunctiveLicenses.length];
		for (int i = 0; i < conjunctiveLicenses.length; i++) {
			String[] disjunctiveLicenses = conjunctiveLicenses[i].split("\\Wor\\W");
			if (disjunctiveLicenses.length == 0) {
				// should not get here
				throw new SpreadsheetException("Invalid license declaration at the "+String.valueOf(i)+
						" license: "+licenseString);
			}
			String[] diLicenses = new String[disjunctiveLicenses.length-1];
			for (int j = 1; j < disjunctiveLicenses.length; j++) {
				diLicenses[j-1] = trimLicense(disjunctiveLicenses[j]);
			}
			retval[i] = new LicenseDeclaration(trimLicense(disjunctiveLicenses[0]), diLicenses);
		}
		return retval;
	}

	private static String trimLicense(String licenseString) {
		String retval = licenseString.trim();
		if (retval.charAt(0) == '(') {
			retval = retval.substring(1);
		}
		if (retval.endsWith(")")) {
			retval = retval.substring(0, retval.length()-1);
		}
		return retval;
	}

	public static String licensesToString(LicenseDeclaration[] declaredLicenses) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < declaredLicenses.length; i++) {
			if (sb.length() > 0) {
				sb.append(" and ");
			}
			String[] disjunctiveLicenses = declaredLicenses[i].getDisjunctiveLicenses();
			if (disjunctiveLicenses != null && disjunctiveLicenses.length > 0) {
				sb.append("(");
			}
			sb.append(declaredLicenses[i].getName());
			if (disjunctiveLicenses != null && disjunctiveLicenses.length > 0) {
				for (int j = 0; j < disjunctiveLicenses.length; j++) {
					sb.append(" or ");
					sb.append(disjunctiveLicenses[j]);
				}
				sb.append(")");
			}
		}
		return sb.toString();
	}
}
