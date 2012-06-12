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
import org.spdx.rdfparser.SPDXFile;

/**
 * Abstract class for PerFileSheet.  Specific version implementations are implemented
 * as subclasses.
 * 
 * @author Gary O'Neall
 *
 */
public abstract class PerFileSheet extends AbstractSheet {
	
	protected String version;
	
	public PerFileSheet(Workbook workbook, String sheetName, String version) {
		super(workbook, sheetName);
		this.version = version;
	}

	/**
	 * Open a specific version of the PerFileSheet
	 * @param workbook
	 * @param perFileSheetName
	 * @param version spreadsheet version
	 * @return
	 */
	public static PerFileSheet openVersion(Workbook workbook,
			String perFileSheetName, String version) {
		if (version.compareToIgnoreCase(SPDXSpreadsheet.VERSION_0_9_4) <= 0) {
			return new PerFileSheetV09d3(workbook, perFileSheetName, version);
		} else {
			return new PerFileSheetV1d1(workbook, perFileSheetName, version);
		}
	}
	
	/**
	 * Add information about a specific SPDX file to the PerFileSheet
	 * @param fileInfo
	 */
	public abstract void add(SPDXFile fileInfo);

	/**
	 * Get the file information for a row in the PerFileSheet
	 * @param rowNum
	 * @return
	 */
	public abstract SPDXFile getFileInfo(int rowNum) throws SpreadsheetException;

	/**	
	 * Create a blank worksheet NOTE: Replaces / deletes existing sheet by the same name
	 * @param wb
	 * @param perFileSheetName
	 */
	public static void create(Workbook wb, String perFileSheetName) {
		//NOTE: This needs to be udpated the the most current version
		PerFileSheetV1d1.create(wb, perFileSheetName);
	}

}
