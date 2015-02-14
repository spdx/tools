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

import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxFile.FileType;

/**
 * Sheet containing File Type
 * @author Gary O'Neall
 *
 */
public class FileTypeSheet extends AbstractFileCompareSheet {

	private static final int FILE_TYPE_COL_WIDTH = 20;

	/**
	 * @param workbook
	 * @param sheetName
	 */
	public FileTypeSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}

	static void create(Workbook wb, String sheetName) {
		AbstractFileCompareSheet.create(wb, sheetName, FILE_TYPE_COL_WIDTH);
	}

	/* (non-Javadoc)
	 * @see org.spdx.compare.AbstractFileCompareSheet#getFileValue(org.spdx.rdfparser.SpdxFile)
	 */
	@Override
	String getFileValue(SpdxFile spdxFile) {
		if (spdxFile.getFileTypes() == null ||spdxFile.getFileTypes().length == 0) {
			return "";
		} else {
			FileType[] fileTypes = spdxFile.getFileTypes();
			String[] sFileTypes = new String[fileTypes.length];
			for (int i = 0; i < fileTypes.length; i++) {
				sFileTypes[i] = SpdxFile.FILE_TYPE_TO_TAG.get(fileTypes[i]);
			}
			Arrays.sort(sFileTypes);
			StringBuilder sb = new StringBuilder(sFileTypes[0]);
			for (int i = 1; i < sFileTypes.length; i++) {
				sb.append(", ");
				sb.append(sFileTypes[i]);
			}
			return sb.toString();
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.compare.AbstractFileCompareSheet#valuesMatch(org.spdx.compare.SpdxComparer, org.spdx.rdfparser.SpdxFile, int, org.spdx.rdfparser.SpdxFile, int)
	 */
	@Override
	boolean valuesMatch(SpdxComparer comparer, SpdxFile fileA, int docIndexA,
			SpdxFile fileB, int docIndexB) throws SpdxCompareException {
		return SpdxComparer.stringsEqual(getFileValue(fileA), getFileValue(fileB));
	}

}
