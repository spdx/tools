/**
 * Copyright (c) 2013 Source Auditor Inc.
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

/**
 * Sheet with results for file contributor comparison results
 * @author Gary O'Neall
 *
 */
public class FileContributorsSheet extends AbstractFileCompareSheet {
	
	private static final int FILE_CONTRIBUTOR_COL_WIDTH = 60;
	
	public FileContributorsSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}

	static void create(Workbook wb, String sheetName) {
		AbstractFileCompareSheet.create(wb, sheetName, FILE_CONTRIBUTOR_COL_WIDTH);
	}

	/* (non-Javadoc)
	 * @see org.spdx.compare.AbstractFileCompareSheet#valuesMatch(org.spdx.compare.SpdxComparer, org.spdx.rdfparser.SPDXFile, int, org.spdx.rdfparser.SPDXFile, int)
	 */
	@Override
	boolean valuesMatch(SpdxComparer comparer, SPDXFile fileA, int docIndexA,
			SPDXFile fileB, int docIndexB) throws SpdxCompareException {
		return SpdxComparer.stringArraysEqual(fileA.getContributors(), fileB.getContributors());
	}

	/* (non-Javadoc)
	 * @see org.spdx.compare.AbstractFileCompareSheet#getFileValue(org.spdx.rdfparser.SPDXFile)
	 */
	@Override
	String getFileValue(SPDXFile spdxFile) {
		StringBuilder sb = new StringBuilder();
		String[] contributors = spdxFile.getContributors();
		if (contributors != null && contributors.length > 0) {
			sb.append(contributors[0]);
			for (int i = 1; i < contributors.length; i++) {
				sb.append(", ");
				sb.append(contributors[i]);
			}
		}
		return sb.toString();
	}

}
