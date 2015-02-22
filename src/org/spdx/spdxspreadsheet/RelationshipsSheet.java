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
import org.spdx.rdfparser.model.Relationship;

/**
 * Sheet containing relationship data
 * @author Gary O'Neall
 *
 */
public class RelationshipsSheet extends AbstractSheet {

	/**
	 * @param workbook
	 * @param relationshipsSheetName
	 */
	public RelationshipsSheet(Workbook workbook, String relationshipsSheetName) {
		super(workbook, relationshipsSheetName);
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
	 * @param relationshipsSheetName
	 */
	public static void create(Workbook wb, String relationshipsSheetName) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param relationship
	 */
	public void add(Relationship relationship) {
		// TODO Auto-generated method stub
		
	}

}
