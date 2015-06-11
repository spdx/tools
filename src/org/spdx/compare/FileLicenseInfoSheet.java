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
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.license.AnyLicenseInfo;

/**
 * Sheet of the comparison results for the file seen licenses
 * @author Gary O'Neall
 *
 */
public class FileLicenseInfoSheet extends AbstractFileCompareSheet {

	private static final int LICENSE_COL_WIDTH = 60;
	
	/**
	 * @param workbook
	 * @param sheetName
	 */
	public FileLicenseInfoSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}
	static void create(Workbook wb, String sheetName) {
		AbstractFileCompareSheet.create(wb, sheetName, LICENSE_COL_WIDTH);
	}

	/* (non-Javadoc)
	 * @see org.spdx.compare.AbstractFileCompareSheet#getFileValue(org.spdx.rdfparser.SpdxFile)
	 */
	@Override
	String getFileValue(SpdxFile spdxFile) {
		if (spdxFile.getLicenseInfoFromFiles() == null || spdxFile.getLicenseInfoFromFiles().length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(spdxFile.getLicenseInfoFromFiles()[0].toString());
		for (int i = 1; i < spdxFile.getLicenseInfoFromFiles().length; i++) {
			sb.append(", ");
			sb.append(spdxFile.getLicenseInfoFromFiles()[i].toString());
		}
		return sb.toString();
	}
	/* (non-Javadoc)
	 * @see org.spdx.compare.AbstractFileCompareSheet#valuesMatch(org.spdx.rdfparser.SpdxFile, int, org.spdx.rdfparser.SpdxFile, int)
	 */
	@Override
	boolean valuesMatch(SpdxComparer comparer, SpdxFile fileA, int docIndexA, SpdxFile fileB,
			int docIndexB) throws SpdxCompareException {
		AnyLicenseInfo[] licenseInfosA = fileA.getLicenseInfoFromFiles();
		AnyLicenseInfo[] licenseInfosB = fileB.getLicenseInfoFromFiles();
		if (licenseInfosA.length != licenseInfosB.length) {
			return false;
		}
		for (int i = 0; i < licenseInfosA.length; i++) {
			boolean found = false;
			for (int j = 0; j < licenseInfosB.length; j++) {
				if (comparer.compareLicense(docIndexA, licenseInfosA[i], docIndexB, licenseInfosB[j])) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
}
