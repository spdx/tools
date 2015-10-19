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

import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXPackageInfo;

/**
 * Abstract PackageInfoSheet to manage cross-version implementations
 * @author Gary O'Neall
 *
 */
public abstract class PackageInfoSheet extends AbstractSheet {

	protected String version;

	public PackageInfoSheet(Workbook workbook, String sheetName, String version) {
		super(workbook, sheetName);
		this.version = version;
	}

	public abstract void add(SPDXPackageInfo pkgInfo);
	public abstract SPDXPackageInfo getPackageInfo(int rowNum) throws SpreadsheetException;

	public static String licensesToString(SPDXLicenseInfo[] licenses) {
		if (licenses == null || licenses.length == 0) {
			return "";
		} else if (licenses.length == 1) {
			return licenses[0].toString();
		} else {
			StringBuilder sb = new StringBuilder(licenses[0].toString());
			for (int i = 1; i < licenses.length; i++) {
				sb.append(", ");
				sb.append(licenses[i].toString());
			}
			return sb.toString();
		}
	}

	public static void create(Workbook wb, String sheetName) {
		PackageInfoSheetV09d3.create(wb, sheetName);
	}
}
