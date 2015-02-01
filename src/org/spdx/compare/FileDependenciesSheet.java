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

import java.util.Arrays;

import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.model.SpdxFile;

/**
 * Sheet for file dependency comaparison results
 * @author Gary O'Neall
 *
 */
public class FileDependenciesSheet extends AbstractFileCompareSheet {
	
	private static final int FILE_DEPENDENCIES_COL_WIDTH = 60;
	
	/**
	 * @param workbook
	 * @param sheetName
	 */
	public FileDependenciesSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}
	
	static void create(Workbook wb, String sheetName) {
		AbstractFileCompareSheet.create(wb, sheetName, FILE_DEPENDENCIES_COL_WIDTH);
	}

	/* (non-Javadoc)
	 * @see org.spdx.compare.AbstractFileCompareSheet#valuesMatch(org.spdx.compare.SpdxComparer, org.spdx.rdfparser.SpdxFile, int, org.spdx.rdfparser.SpdxFile, int)
	 */
	@Override
	boolean valuesMatch(SpdxComparer comparer, SpdxFile fileA, int docIndexA,
			SpdxFile fileB, int docIndexB) throws SpdxCompareException {
		@SuppressWarnings("deprecation")
		String[] dependencyFileNamesA = SpdxFileComparer.filesToFileNames(fileA.getFileDependencies());
		@SuppressWarnings("deprecation")
		String[] dependencyFileNamesB = SpdxFileComparer.filesToFileNames(fileB.getFileDependencies());
		return SpdxComparer.stringArraysEqual(dependencyFileNamesA, dependencyFileNamesB);
	}

	/* (non-Javadoc)
	 * @see org.spdx.compare.AbstractFileCompareSheet#getFileValue(org.spdx.rdfparser.SpdxFile)
	 */
	@Override
	String getFileValue(SpdxFile spdxFile) {
		@SuppressWarnings("deprecation")
		String[] dependencyFileNames = SpdxFileComparer.filesToFileNames(spdxFile.getFileDependencies());
		StringBuilder sb = new StringBuilder();
		if (dependencyFileNames != null && dependencyFileNames.length > 0) {
			Arrays.sort(dependencyFileNames);
			sb.append(dependencyFileNames[0]);
			for (int i = 1; i < dependencyFileNames.length; i++) {
				sb.append(", ");
				sb.append(dependencyFileNames[i]);
			}
		}
		return sb.toString();
	}

}
