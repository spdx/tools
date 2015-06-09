/**
 * Copyright (c) 2012 Source Auditor Inc.
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

import org.apache.poi.ss.usermodel.Workbook;

/**
 * Abstract sheet for NonSTandardLicenses.  Specific versions are implemented as subclasses.
 * @author Gary O'Neall
 *
 */
public abstract class NonStandardLicensesSheet extends AbstractSheet {

	protected String version;
	/**
	 * @param workbook
	 * @param sheetName
	 */
	public NonStandardLicensesSheet(Workbook workbook, String sheetName, String version) {
		super(workbook, sheetName);
		this.version = version;
	}
	
	/**
	 * Retrieve the license identifier for a specific row in the spreadsheet
	 * @param rowNum
	 * @return
	 */
	public abstract String getIdentifier(int rowNum);
	
	/**
	 * Retrieve the extracted text for a specific row in the spreadsheet
	 * @param rowNum
	 * @return
	 */
	public abstract String getExtractedText(int rowNum);
	
	/**
	 * Add a new row to the NonStandardLicenses sheet
	 * @param identifier License ID
	 * @param extractedText Extracted license text
	 * @param optional license name
	 * @param crossRefUrls optional cross reference URL's
	 * @param optional comment
	 */
	public abstract void add(String identifier, String extractedText, String licenseName,
			String[] crossRefUrls, String comment);

	/**
	 * Open an existing NonStandardLicenseSheet
	 * @param workbook
	 * @param nonStandardLicenseSheetName
	 * @param version Spreadsheet version
	 * @return
	 */
	public static NonStandardLicensesSheet openVersion(Workbook workbook,
			String nonStandardLicenseSheetName, String version) {
		if (version.compareToIgnoreCase(SPDXSpreadsheet.VERSION_0_9_4) <= 0) {
			return new NonStandardLicensesSheetV0d9d4(workbook, nonStandardLicenseSheetName, version);
		} else {
			return new NonStandardLicensesSheetV1d1(workbook, nonStandardLicenseSheetName, version);
		}
	}

	/**
	 * Create a blank worksheet NOTE: Replaces / deletes existing sheet by the same name
	 * @param wb
	 * @param nonStandardLicenseSheetName
	 */
	public static void create(Workbook wb, String nonStandardLicenseSheetName) {
		//NOTE: This needs to be updated to the current version
		NonStandardLicensesSheetV1d1.create(wb, nonStandardLicenseSheetName);
	}

	/**
	 * @return
	 */
	public abstract String getLicenseName(int rowNum);

	/**
	 * @return
	 */
	public abstract String[] getCrossRefUrls(int rowNum);

	/**
	 * @return
	 */
	public abstract String getComment(int rowNum);

}
