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
import org.spdx.rdfparser.SPDXFile;
import org.spdx.spdxspreadsheet.AbstractSheet;

/**
 * @author Source Auditor
 *
 */
public class FileConcludedSheet extends AbstractFileCompareSheet {

	private static final int LICENSE_COL_WIDTH = 60;

	/**
	 * @param workbook
	 * @param sheetName
	 */
	public FileConcludedSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}

	static void create(Workbook wb, String sheetName) {
		AbstractFileCompareSheet.create(wb, sheetName, LICENSE_COL_WIDTH);
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.compare.AbstractFileCompareSheet#getFileValue(org.spdx.rdfparser.SPDXFile)
	 */
	@Override
	String getFileValue(SPDXFile spdxFile) {
		return spdxFile.getConcludedLicenses().toString();
	}

	/* (non-Javadoc)
	 * @see org.spdx.compare.AbstractFileCompareSheet#valuesMatch(org.spdx.compare.SpdxComparer, org.spdx.rdfparser.SPDXFile, int, org.spdx.rdfparser.SPDXFile, int)
	 */
	@Override
	boolean valuesMatch(SpdxComparer comparer, SPDXFile fileA, int docIndexA,
			SPDXFile fileB, int docIndexB) throws SpdxCompareException {
		return comparer.compareLicense(docIndexA, fileA.getConcludedLicenses(), docIndexB, fileB.getConcludedLicenses());
	}
}
