/**
 * Copyright (c) 2015 Source Auditor Inc.
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
import org.spdx.rdfparser.model.Annotation;

/**
 * Sheet containing all annotations
 * @author Gary O'Neall
 *
 */
public class AnnotationsSheet extends AbstractSheet {

	/**
	 * @param workbook
	 * @param annotationsSheetName
	 */
	public AnnotationsSheet(Workbook workbook, String annotationsSheetName) {
		super(workbook, annotationsSheetName);
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
	 * @param annotationsSheetName
	 */
	public static void create(Workbook wb, String annotationsSheetName) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param annotation
	 */
	public void add(Annotation annotation) {
		// TODO Auto-generated method stub
		
	}

}
