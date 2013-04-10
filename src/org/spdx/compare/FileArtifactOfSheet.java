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
import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.spdxspreadsheet.AbstractSheet;

/**
 * @author Source Auditor
 *
 */
public class FileArtifactOfSheet extends AbstractFileCompareSheet {

	private static final int FILE_ARTIFACTOF_COL_WIDTH = 60;
	/**
	 * @param workbook
	 * @param sheetName
	 */
	public FileArtifactOfSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}
	
	static void create(Workbook wb, String sheetName) {
		AbstractFileCompareSheet.create(wb, sheetName, FILE_ARTIFACTOF_COL_WIDTH);
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.compare.AbstractFileCompareSheet#getFileValue(org.spdx.rdfparser.SPDXFile)
	 */
	@Override
	String getFileValue(SPDXFile spdxFile) {
		DOAPProject[] projects = spdxFile.getArtifactOf();
		if (projects == null || projects.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int  i = 0; i < projects.length; i++) {
			if (sb.length() > 0) {
				sb.append("; ");
			}
			sb.append(projects[i].getName());
			if (projects[i].getHomePage() != null && !projects[i].getHomePage().isEmpty()) {
				sb.append("(");
				sb.append(projects[i].getHomePage());
				sb.append(")");
			}
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.spdx.compare.AbstractFileCompareSheet#valuesMatch(org.spdx.compare.SpdxComparer, org.spdx.rdfparser.SPDXFile, int, org.spdx.rdfparser.SPDXFile, int)
	 */
	@Override
	boolean valuesMatch(SpdxComparer comparer, SPDXFile fileA, int docIndexA,
			SPDXFile fileB, int docIndexB) throws SpdxCompareException {
		DOAPProject[] projectsA = fileA.getArtifactOf();
		DOAPProject[] projectsB = fileB.getArtifactOf();
		if (projectsA == null) {
			if (projectsB == null || projectsB.length == 0) {
				return true;
			} else {
				return false;
			}
		} 
		if (projectsB == null) {
			if (projectsA.length == 0) {
				return true;
			} else {
				return false;
			}
		}
		if (projectsA.length != projectsB.length) {
			return false;
		}
		for (int i = 0; i < projectsA.length; i++) {
			boolean found = false;
			for (int j = 0; j < projectsB.length; j++) {
				if (SpdxComparer.stringsEqual(projectsA[i].getName(), projectsB[j].getName()) &&
						SpdxComparer.stringsEqual(projectsA[i].getHomePage(), projectsB[j].getHomePage()) &&
						SpdxComparer.stringsEqual(projectsA[i].getProjectUri(), projectsB[j].getProjectUri())) {
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
