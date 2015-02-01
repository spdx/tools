/**
 * Copyright (c) 2013 Source Auditor Inc.
 * Copyright (c) 2013 Black Duck Software Inc.
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
package org.spdx.compare;

import java.util.Arrays;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.spdxspreadsheet.AbstractSheet;

/**
 * Document level fields for comparison spreadsheet
 * Column1 is the document field name, column2 indicates if all docs are equal, 
 * columns3 through columnN are document specific field values
 * @author Gary O'Neall
 *
 */
public class PackageSheet extends AbstractSheet {
	private static final int COL_WIDTH = 60;
	protected static final int FIELD_COL = 0;
	protected static final int EQUALS_COL = 1;
	protected static final int FIRST_DOC_COL = 2;
	private static final int FIELD_COL_WIDTH = 20;
	private static final int EQUALS_COL_WIDTH = 6;
	protected static final String FIELD_HEADER_TEXT = "Package Property";
	protected static final String EQUALS_HEADER_TEXT = "Equals";
	protected static final String DESCRIPTION_FIELD_TEXT = "Description";
	protected static final String SUMMARY_FIELD_TEXT = "Summary";
	protected static final String COPYRIGHT_FIELD_TEXT = "Copyright";
	protected static final String LICENSE_COMMENT_FIELD_TEXT = "License Comment";
	protected static final String DECLARED_LICENSE_FIELD_TEXT = "Declared License";
	protected static final String LICENSE_INFOS_FROM_FILES_FIELD_TEXT = "License From Files";
	protected static final String CONCLUDED_LICENSE_FIELD_TEXT = "Concluded License";
	protected static final String SOURCEINFO_FIELD_TEXT = "Source Info";
	protected static final String CHECKSUM_FIELD_TEXT = "Checksum";
	protected static final String VERIFICATION_EXCLUDED_FIELD_TEXT = "Verification Excluded";
	protected static final String VERIFICATION_FIELD_TEXT = "Verification Value";
	protected static final String DOWNLOAD_FIELD_TEXT = "Dowload Location";
	protected static final String ORIGINATOR_FIELD_TEXT = "Originator";
	protected static final String SUPPLIER_FIELD_TEXT = "Supplier";
	protected static final String FILE_NAME_FIELD_TEXT = "File Name";
	protected static final String VERSION_FIELD_TEXT = "Version";
	protected static final String PACKAGE_NAME_FIELD_TEXT = "Package Name";
	protected static final String DIFFERENT_STRING = "Diff";
	protected static final String EQUAL_STRING = "Equal";
	protected static final String HOMEPAGE_FIELD_TEXT = "Home Page";
	

	/**
	 * @param workbook
	 * @param sheetName
	 */
	public PackageSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		// Nothing to verify
		return null;
	}

	/**
	 * @param wb
	 * @param sheetName
	 */
	public static void create(Workbook wb, String sheetName) {
		int sheetNum = wb.getSheetIndex(sheetName);
		if (sheetNum >= 0) {
			wb.removeSheetAt(sheetNum);
		}
		Sheet sheet = wb.createSheet(sheetName);
		CellStyle headerStyle = AbstractSheet.createHeaderStyle(wb);
		CellStyle defaultStyle = AbstractSheet.createLeftWrapStyle(wb);
		Row headerRow = sheet.createRow(0);
		sheet.setColumnWidth(FIELD_COL, FIELD_COL_WIDTH*256);
		sheet.setDefaultColumnStyle(FIELD_COL, defaultStyle);
		Cell fieldCell = headerRow.createCell(FIELD_COL);
		fieldCell.setCellStyle(headerStyle);
		fieldCell.setCellValue(FIELD_HEADER_TEXT);
		
		sheet.setColumnWidth(EQUALS_COL, EQUALS_COL_WIDTH * 256);
		sheet.setDefaultColumnStyle(EQUALS_COL, defaultStyle);
		Cell equalsCell = headerRow.createCell(EQUALS_COL);
		equalsCell.setCellStyle(headerStyle);
		equalsCell.setCellValue(EQUALS_HEADER_TEXT);
		
		for (int i = FIRST_DOC_COL; i < MultiDocumentSpreadsheet.MAX_DOCUMENTS+FIRST_DOC_COL; i++) {
			sheet.setColumnWidth(i, COL_WIDTH*256);
			sheet.setDefaultColumnStyle(i, defaultStyle);
			Cell cell = headerRow.createCell(i);
			cell.setCellStyle(headerStyle);
		}
	}

	/**
	 * @param comparer
	 * @param docNames 
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void importCompareResults(SpdxComparer comparer, String[] docNames) throws SpdxCompareException, InvalidSPDXAnalysisException {
		if (comparer.getNumSpdxDocs() != docNames.length) {
			throw(new SpdxCompareException("Number of document names does not match the number of SPDX documents"));
		}
		this.clear();
		Row header = sheet.getRow(0);
		SpdxPackage[][] allPackages;
		allPackages = new SpdxPackage[comparer.getNumSpdxDocs()][];
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			allPackages[i] = comparer.collectAllPackages(comparer.getSpdxDoc(i));
			Arrays.sort(allPackages[i]);
		}
		int[] pkgIndexes = new int[comparer.getNumSpdxDocs()];
		for (int i = 0; i < pkgIndexes.length; i++) {
			pkgIndexes[i] = 0;	//might as well be explicit about it
		}
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			Cell headerCell = header.getCell(FIRST_DOC_COL+i);
			headerCell.setCellValue(docNames[i]);
		}

		// iterate through all of the packages in sorted order, finding any which are the same
		while (!done(pkgIndexes, allPackages)) {
			// find the next sorted package out of all of the indexes
			// include all package rows which are the same
			SpdxPackage[] nextPackages = getNextPackage(pkgIndexes, allPackages);
			addPackageToSheet(nextPackages);	
		}
	}
	
	/**
	 * @param nextPackages
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void addPackageToSheet(SpdxPackage[] nextPackages) throws InvalidSPDXAnalysisException {
		MultiPackageComparer comparer = new MultiPackageComparer(nextPackages);
		Row packageNameRow = this.addRow();
		packageNameRow.createCell(FIELD_COL).setCellValue(PACKAGE_NAME_FIELD_TEXT);
		if (comparer.isPackageNamesEqual()) {
			setCellEqualValue(packageNameRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(packageNameRow.createCell(EQUALS_COL));
		}
		Row versionRow = this.addRow();
		versionRow.createCell(FIELD_COL).setCellValue(VERSION_FIELD_TEXT);
		if (comparer.isPackageVersionsEqual()) {
			setCellEqualValue(versionRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(versionRow.createCell(EQUALS_COL));
		}
		Row fileNameRow = this.addRow();
		fileNameRow.createCell(FIELD_COL).setCellValue(FILE_NAME_FIELD_TEXT);
		if (comparer.isPackageFileNamesEqual()) {
			setCellEqualValue(fileNameRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(fileNameRow.createCell(EQUALS_COL));
		}
		Row supplierRow = this.addRow();
		supplierRow.createCell(FIELD_COL).setCellValue(SUPPLIER_FIELD_TEXT);
		if (comparer.isPackageSuppliersEqual()) {
			setCellEqualValue(supplierRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(supplierRow.createCell(EQUALS_COL));
		}
		Row originatorRow = this.addRow();
		originatorRow.createCell(FIELD_COL).setCellValue(ORIGINATOR_FIELD_TEXT);
		if (comparer.isPackageOriginatorsEqual()) {
			setCellEqualValue(originatorRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(originatorRow.createCell(EQUALS_COL));
		}
		Row homePageRow = this.addRow();
		homePageRow.createCell(FIELD_COL).setCellValue(HOMEPAGE_FIELD_TEXT);
		if (comparer.ispackageHomePagesEqual()) {
			setCellEqualValue(homePageRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(homePageRow.createCell(EQUALS_COL));
		}
		Row downloadRow = this.addRow();
		downloadRow.createCell(FIELD_COL).setCellValue(DOWNLOAD_FIELD_TEXT);
		if (comparer.isPackageDownloadLocationsEqual()) {
			setCellEqualValue(downloadRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(downloadRow.createCell(EQUALS_COL));
		}
		Row verificationRow = this.addRow();
		verificationRow.createCell(FIELD_COL).setCellValue(VERIFICATION_FIELD_TEXT);
		if (comparer.isPackageVerificationCodesEqual()) {
			setCellEqualValue(verificationRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(verificationRow.createCell(EQUALS_COL));
		}
		Row verificationExcludedRow = this.addRow();
		verificationExcludedRow.createCell(FIELD_COL).setCellValue(VERIFICATION_EXCLUDED_FIELD_TEXT);
		if (comparer.isPackageVerificationCodesEqual()) {
			setCellEqualValue(verificationExcludedRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(verificationExcludedRow.createCell(EQUALS_COL));
		}
		Row checksumRow = this.addRow();
		checksumRow.createCell(FIELD_COL).setCellValue(CHECKSUM_FIELD_TEXT);
		if (comparer.isPackageChecksumsEqual()) {
			setCellEqualValue(checksumRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(checksumRow.createCell(EQUALS_COL));
		}
		Row sourceInfoRow = this.addRow();
		sourceInfoRow.createCell(FIELD_COL).setCellValue(SOURCEINFO_FIELD_TEXT);
		if (comparer.isSourceInformationEqual()) {
			setCellEqualValue(sourceInfoRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(sourceInfoRow.createCell(EQUALS_COL));
		}
		Row concludedLicenseRow = this.addRow();
		concludedLicenseRow.createCell(FIELD_COL).setCellValue(CONCLUDED_LICENSE_FIELD_TEXT);
		if (comparer.isPackageConcludedLicensesEqual()) {
			setCellEqualValue(concludedLicenseRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(concludedLicenseRow.createCell(EQUALS_COL));
		}
		Row licenseInfosFromFilesRow = this.addRow();
		licenseInfosFromFilesRow.createCell(FIELD_COL).setCellValue(LICENSE_INFOS_FROM_FILES_FIELD_TEXT);
		if (comparer.isPackageLicenseInfoFromFilesEqual()) {
			setCellEqualValue(licenseInfosFromFilesRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(licenseInfosFromFilesRow.createCell(EQUALS_COL));
		}
		Row declaredLicenseRow = this.addRow();
		declaredLicenseRow.createCell(FIELD_COL).setCellValue(DECLARED_LICENSE_FIELD_TEXT);
		if (comparer.isPackageDeclaredLicensesEqual()) {
			setCellEqualValue(declaredLicenseRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(declaredLicenseRow.createCell(EQUALS_COL));
		}
		Row licenseCommentRow = this.addRow();
		licenseCommentRow.createCell(FIELD_COL).setCellValue(LICENSE_COMMENT_FIELD_TEXT);
		if (comparer.isLicenseCommentsEqual()) {
			setCellEqualValue(licenseCommentRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(licenseCommentRow.createCell(EQUALS_COL));
		}
		Row copyrightRow = this.addRow();
		copyrightRow.createCell(FIELD_COL).setCellValue(COPYRIGHT_FIELD_TEXT);
		if (comparer.isCopyrightTextsEqual()) {
			setCellEqualValue(copyrightRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(copyrightRow.createCell(EQUALS_COL));
		}
		Row summaryRow = this.addRow();
		summaryRow.createCell(FIELD_COL).setCellValue(SUMMARY_FIELD_TEXT);
		if (comparer.isPackageSummariesEqual()) {
			setCellEqualValue(summaryRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(summaryRow.createCell(EQUALS_COL));
		}
		Row descriptionRow = this.addRow();
		descriptionRow.createCell(FIELD_COL).setCellValue(DESCRIPTION_FIELD_TEXT);
		if (comparer.isPackageDescriptionsEqual()) {
			setCellEqualValue(descriptionRow.createCell(EQUALS_COL));
		} else {
			setCellDifferentValue(descriptionRow.createCell(EQUALS_COL));
		}
		for (int i = 0; i < nextPackages.length; i++) {
			if (nextPackages[i] == null) {
				continue;
			}

			SpdxPackage pkg = nextPackages[i];
			packageNameRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getName());
			versionRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getVersionInfo());
			fileNameRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getPackageFileName());
			supplierRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getSupplier());
			originatorRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getOriginator());
			homePageRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getHomepage());
			downloadRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getDownloadLocation());
			verificationRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getPackageVerificationCode().getValue());
			verificationExcludedRow.createCell(FIRST_DOC_COL+i).setCellValue(exludeFilesToString(pkg.getPackageVerificationCode().getExcludedFileNames()));
// TODO - replace with checksum values			checksumRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getSha1());
			sourceInfoRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getSourceInfo());
			concludedLicenseRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getLicenseConcluded().toString());
			licenseInfosFromFilesRow.createCell(FIRST_DOC_COL+i).setCellValue(licenseInfosToString(pkg.getLicenseInfoFromFiles()));
			declaredLicenseRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getLicenseDeclared().toString());
			licenseCommentRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getLicenseComment());
			copyrightRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getCopyrightText());
			summaryRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getSummary());
			descriptionRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getDescription());
		}
	}

	/**
	 * Find the next package in sorted ord
	 * @param pkgIndexes
	 * @param allPackages
	 * @return All packages that have the same name and version, null if the package name/version does not match
	 */
	private SpdxPackage[] getNextPackage(int[] pkgIndexes,
			SpdxPackage[][] allPackages) {
		// Pass 1 - find the next package
		SpdxPackage candidate = null;
		for (int i = 0; i < pkgIndexes.length; i++) {
			if (pkgIndexes[i] < allPackages[i].length) {
				if (candidate == null || candidate.compareTo(allPackages[i][pkgIndexes[i]]) < 0) {
					candidate = allPackages[i][pkgIndexes[i]];
				}
			}
		}
		// pass 2 - collect the packages and increment the counters
		SpdxPackage[] retval = new SpdxPackage[pkgIndexes.length];
		for (int i = 0; i < pkgIndexes.length; i++) {
			if (pkgIndexes[i] < allPackages[i].length) {
				if (allPackages[i][pkgIndexes[i]].compareTo(candidate) == 0) {
					retval[i] = allPackages[i][pkgIndexes[i]];
					pkgIndexes[i]++;
				} else {
					retval[i] = null;
				}
			}
		}
		return retval;
	}

	/**
	 * return true if the indexes for all the packages are past the end of the arrays
	 * @param pkgIndexes
	 * @param allPackages
	 * @return
	 */
	private boolean done(int[] pkgIndexes, SpdxPackage[][] allPackages) {
		for (int i = 0; i < pkgIndexes.length; i++) {
			if (pkgIndexes[i] < allPackages[i].length) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param licenseInfoFromFiles
	 * @return
	 */
	protected String licenseInfosToString(AnyLicenseInfo[] licenseInfoFromFiles) {
		if (licenseInfoFromFiles == null || licenseInfoFromFiles.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(licenseInfoFromFiles[0].toString());
		for (int i = 1; i < licenseInfoFromFiles.length; i++) {
			sb.append(", ");
			sb.append(licenseInfoFromFiles[i].toString());
		}
		return sb.toString();
	}

	/**
	 * @param excludedFileNames
	 * @return
	 */
	protected String exludeFilesToString(String[] excludedFileNames) {
		if (excludedFileNames == null || excludedFileNames.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(excludedFileNames[0]);
		for (int i = 1; i < excludedFileNames.length; i++) {
			sb.append(", ");
			sb.append(excludedFileNames[i]);
		}
		return sb.toString();
	}

	/**
	 * @param cell
	 */
	private void setCellDifferentValue(Cell cell) {
		cell.setCellValue(DIFFERENT_STRING);
		cell.setCellStyle(yellowWrapped);
	}

	/**
	 * @param cell
	 */
	private void setCellEqualValue(Cell cell) {
		cell.setCellValue(EQUAL_STRING);
		cell.setCellStyle(greenWrapped);
	}

}
