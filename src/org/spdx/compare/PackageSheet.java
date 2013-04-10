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

import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.spdxspreadsheet.AbstractSheet;

/**
 * Document level fields for comparison spreadsheet
 * Column1 is the document field name, column2 indicates if all docs are equal, 
 * columns3 through columnN are document specific field values
 * @author Gary O'Neall
 *
 */
public class PackageSheet extends AbstractSheet {

	/**
	 * @param workbook
	 * @param sheetName
	 */
	public PackageSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param wb
	 * @param packageSheetName
	 */
	public static void create(Workbook wb, String packageSheetName) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param comparer
	 * @param docNames 
	 */
	public void importCompareResults(SpdxComparer comparer, String[] docNames) throws SpdxCompareException {
		// TODO Auto-generated method stub
		
	}

}
