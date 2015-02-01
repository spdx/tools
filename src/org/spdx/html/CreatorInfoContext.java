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
package org.spdx.html;

import java.util.ArrayList;
import java.util.List;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.model.SpdxDocument;

/**
 * Mustache Context for converting an SPDX Document for use in the SpdxHTMLTemplate
 * The constants are used in the SpdxHTMLTemplate.html file in the resources directory.
 * 
 * Note that the Mustache variable names are the tag values in the SPDX specification
 * 
 * @author Gary O'Neall
 *
 */
public class CreatorInfoContext {
	
	private SpdxDocument doc;
	
	public CreatorInfoContext(SpdxDocument doc2) {
		this.doc = doc2;
	}
	
	public String created() {
		try {
			return doc.getCreationInfo().getCreated();
		} catch (InvalidSPDXAnalysisException e) {
			return "Error getting creator created date: "+e.getMessage();
		}
	}
	
	public String licenseListVersion() {
		String retval;
		try {
			retval = this.doc.getCreationInfo().getLicenseListVersion();
		} catch (InvalidSPDXAnalysisException e) {
			return "Error getting license list version: "+e.getMessage();
		};
		if (retval == null) {
			retval = "";
		}
		return retval;
	}
	
	public List<String> creator() {
		ArrayList<String> creators = new ArrayList<String>();
		try {
			SPDXCreatorInformation creatorInfo = doc.getCreationInfo();
			if (creatorInfo != null) {
				String[] creatorArray = creatorInfo.getCreators();
				for (int i = 0; i < creatorArray.length; i++) {
					creators.add(creatorArray[i]);
				}
			}
		} catch(InvalidSPDXAnalysisException ex) {
			creators.add("Error getting creators: "+ex.getMessage());
		}
		return creators;
	}
	
	public String comment() {
		try {
			if (doc.getCreationInfo() != null) {
				return doc.getCreationInfo().getComment();
			} else {
				return null;
			}
		} catch (InvalidSPDXAnalysisException e) {
			return "Error getting creator comment: "+e.getMessage();
		}
	}

}
