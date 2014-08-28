/**
 * Copyright (c) 2014 Gang Ling.
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
package org.spdx.merge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.spdx.rdfparser.SPDXConjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXDisjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXNonStandardLicense;

/**
 * Application to build HashMaps to help SPDX documents merging. Currently, it helps mapping any SPDX licenses.
 * @author Gang Ling
 *
 */
/**
 * @author Gary
 *
 */
public class SpdxLicenseMapper {

	private HashMap<SPDXDocument, HashMap<SPDXLicenseInfo, SPDXLicenseInfo>> nonStdLicIdMap = 
			new HashMap<SPDXDocument, HashMap< SPDXLicenseInfo, SPDXLicenseInfo>>();
	
	/**
	 * 
	 * @param masterDoc
	 */
	public SpdxLicenseMapper(){
	}
	
	/**
	 * Creates a new non-standard license in the outputDoc and creates a mapping
	 * between the output document non-standard license and the subDoc non standard license
	 * This particular non-standard license is unique to the outputDoc document.
	 * The return variable subNonStdLicInfo is cloned from input non-standard license, but replace the license id.
	 * @param outputDoc
	 * @param subDoc
	 * @param subNonStdLicInfo
	 * @return
	 */
	public SPDXNonStandardLicense mappingNewNonStdLic(SPDXDocument outputDoc, SPDXDocument subDoc, SPDXNonStandardLicense subNonStdLicInfo){
		
		HashMap<SPDXLicenseInfo,SPDXLicenseInfo> interMap = new HashMap<SPDXLicenseInfo,SPDXLicenseInfo>();
		if(docInNonStdLicIdMap(subDoc)){
			interMap = nonStdLicIdMap.get(subDoc);
		}    
		String NewNonStdLicId = outputDoc.getNextLicenseRef();
		SPDXNonStandardLicense subCopy = (SPDXNonStandardLicense) subNonStdLicInfo.clone();
		subNonStdLicInfo.setId(NewNonStdLicId);
		interMap.put(subCopy, subNonStdLicInfo);

		nonStdLicIdMap.put(subDoc, interMap);

		return subNonStdLicInfo;
	}
	
	/**
	 * Maps a subDoc nonstandard license to an existing output document nonstandard license
	 * @param outputDoc
	 * @param subDoc
	 * @param subLicense
	 * @param outputDocLicense
	 */
	public void mappingExistingNonStdLic(SPDXDocument outputDoc, SPDXNonStandardLicense outputDocLicense, SPDXDocument subDoc, SPDXNonStandardLicense subLicense) {
		HashMap<SPDXLicenseInfo,SPDXLicenseInfo> interMap;
		if(docInNonStdLicIdMap(subDoc)){
			interMap = nonStdLicIdMap.get(subDoc);
		} else {
			interMap = new HashMap<SPDXLicenseInfo,SPDXLicenseInfo>();
		}
		interMap.put(outputDocLicense, subLicense);
		nonStdLicIdMap.put(subDoc, interMap);
	}
	
	/**
	 * a method gets a sub SPDX document and its file information. 
	 * Check the non-standard licenses in the file information. Replace the non-standard licenses if this particular license in the HashMap.
	 * @param spdxDoc
	 * @param subFileInfo
	 * @return
	 */
	public SPDXFile replaceNonStdLicInFile(SPDXDocument spdxDoc, SPDXFile subFileInfo){
			SPDXLicenseInfo[] subLicInfoInFile = subFileInfo.getSeenLicenses();
			HashMap<SPDXLicenseInfo, SPDXLicenseInfo> idMap = foundInterMap(spdxDoc);
			Set <SPDXLicenseInfo> keys = idMap.keySet();
			SPDXLicenseInfo[] orgNonStdLics = keys.toArray(new SPDXLicenseInfo[idMap.keySet().size()]);
			ArrayList <SPDXLicenseInfo> retval = new ArrayList<SPDXLicenseInfo>();
			for(int i = 0; i < subLicInfoInFile.length; i++){
				boolean foundLicId = false;
				for(int q = 0; q < orgNonStdLics.length; q++){
					//if the subfile's orgNonStdLic is found in the subLicInfo, 
					if(subLicInfoInFile[i].equals(orgNonStdLics[q])){
						foundLicId = true;
					}
					//then we replace the orgNonStdLic's id with new id from the internal map
					if(foundLicId){
						retval.add(idMap.get(orgNonStdLics[q]));
					}else{
						retval.add(subLicInfoInFile[i]);//if not, add this license to retval array directly. this license must be not non-standard license.
					}
				}
			}
			SPDXLicenseInfo[] mergedFileLics = new SPDXLicenseInfo[retval.size()];
			retval.toArray(mergedFileLics);
			subFileInfo.setSeenLicenses(mergedFileLics);
			SPDXLicenseInfo subConcludedLicInFile = subFileInfo.getConcludedLicenses();
			subFileInfo.setConcludedLicenses(mapLicenseInfo(spdxDoc,subConcludedLicInFile));
			retval.clear();
			return subFileInfo;
	}
	
	/**
	 * a method gets a sub SPDX document and one of its license (non-standard license). Replace the input license if it is found in the HashMap.
	 * And return the mapped license. If the input license doesn't in the HashMap, return the original input license. 
	 * @param spdxDoc
	 * @param license
	 * @return
	 */
	public SPDXLicenseInfo mapNonStdLicInMap(SPDXDocument spdxDoc, SPDXLicenseInfo license){
		HashMap<SPDXLicenseInfo, SPDXLicenseInfo> idMap = foundInterMap(spdxDoc);
		SPDXNonStandardLicense[] orgNonStdLics = idMap.keySet().toArray(new SPDXNonStandardLicense[idMap.keySet().size()]);
		for(int i = 0; i < orgNonStdLics.length; i++ ){
			boolean foundLic = false;
			if(license.equals(orgNonStdLics[i])){
				foundLic = true;
			}
			if(foundLic){
				license = idMap.get(orgNonStdLics[i]);
			}	
		}
		return license;
	}

	
	/**
	 * a method gets a sub SPDX document and licenses from declared licenses in document package. 
	 * Check the object type of input license. If the license is non-standard license, run the license through mapNonStdLicIndMap. 
	 * Otherwise, return the original input license. 
	 * @param spdxDoc
	 * @param license
	 * @return
	 */
	public SPDXLicenseInfo mapLicenseInfo(SPDXDocument spdxDoc, SPDXLicenseInfo license){
		if(license instanceof SPDXConjunctiveLicenseSet){
			SPDXLicenseInfo[] members = ((SPDXConjunctiveLicenseSet) license).getSPDXLicenseInfos();
			SPDXLicenseInfo[] mappedMembers = new SPDXLicenseInfo[members.length];
			for(int i = 0; i < members.length; i++){
				mappedMembers[i] = mapLicenseInfo(spdxDoc, members[i]);
			}
			return new SPDXConjunctiveLicenseSet(mappedMembers);
		}
		else if(license instanceof SPDXDisjunctiveLicenseSet){
			SPDXLicenseInfo[] members = ((SPDXDisjunctiveLicenseSet) license).getSPDXLicenseInfos();
			SPDXLicenseInfo[] mappedMembers = new SPDXLicenseInfo[members.length];
			for(int q = 0; q < members.length; q++ ){
				mappedMembers[q] = mapLicenseInfo(spdxDoc, members[q]);
			}
			return new SPDXDisjunctiveLicenseSet(mappedMembers);
		}else if(license instanceof SPDXNonStandardLicense){
			return license = mapNonStdLicInMap(spdxDoc,(SPDXNonStandardLicense)license);
		}
		return license;	
	}
	/**
	 * 
	 * @param spdxDoc
	 * @return foundDocMatch
	 */
	public boolean docInNonStdLicIdMap(SPDXDocument spdxDoc){
		boolean foundDocMatch = false;
		if(nonStdLicIdMap.containsKey(spdxDoc)){
			foundDocMatch = true;
		}
		return foundDocMatch;
	}
	
	/**
	 * 
	 * @param spdxDoc
	 * @return idMap
	 */
	public HashMap<SPDXLicenseInfo, SPDXLicenseInfo> foundInterMap(SPDXDocument spdxDoc){

		HashMap<SPDXLicenseInfo, SPDXLicenseInfo> idMap = 
				new HashMap<SPDXLicenseInfo, SPDXLicenseInfo>();
		
		idMap = this.nonStdLicIdMap.get(spdxDoc);
		return idMap;
		
	}
	
	/**
	 * 
	 * @return emptyMap
	 */
	public boolean isNonStdLicIdMapEmpty(){
		boolean emptyMap = false;
		if(nonStdLicIdMap.isEmpty()){
			emptyMap = true;
		}
		return emptyMap;
	}
	
	public void clearNonStdLicIdMap(){
		this.nonStdLicIdMap.clear();
	}
}
