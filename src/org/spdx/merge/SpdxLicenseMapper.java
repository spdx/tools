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

import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXNonStandardLicense;

/**
 * Application to build HashMaps to help SPDX documents merging. Currently, it helps mapping SPDX non-standard licenses
 * @author Gang Ling
 *
 */
public class SpdxLicenseMapper {

	private HashMap<SPDXDocument, HashMap<SPDXNonStandardLicense, SPDXNonStandardLicense>> NonStdLicIdMap = 
			new HashMap<SPDXDocument, HashMap< SPDXNonStandardLicense, SPDXNonStandardLicense>>();
	
	private HashMap<SPDXNonStandardLicense, SPDXNonStandardLicense> NonStdLicIds = 
			new HashMap<SPDXNonStandardLicense, SPDXNonStandardLicense>();
	
	/**
	 * 
	 * @param mergeDocs
	 * @param subNonStdLicInfo
	 * @return subNonStdLicInfo
	 */
	public SPDXNonStandardLicense mappingNonStdLic(SPDXDocument[] mergeDocs, SPDXNonStandardLicense subNonStdLicInfo){
				
		String NewNonStdLicId = mergeDocs[0].getNextLicenseRef();
		SPDXNonStandardLicense subCopy = (SPDXNonStandardLicense) subNonStdLicInfo.clone();
		subNonStdLicInfo.setId(NewNonStdLicId);
		NonStdLicIds.put(subCopy, subNonStdLicInfo);		
		return subNonStdLicInfo;
	}
	
	/**
	 * 
	 * @param spdxDoc
	 */
	public void updatedMainMap(SPDXDocument spdxDoc){
		if(!NonStdLicIds.isEmpty()){
			NonStdLicIdMap.put(spdxDoc, NonStdLicIds);
		}
		NonStdLicIds.clear();
	}
	
	/**
	 * 
	 * @param childFileInfo
	 * @param licId
	 * @return 
	 */
	public SPDXFile checkNonStdLicId(SPDXDocument spdxDoc, SPDXFile subFileInfo){
			SPDXLicenseInfo[] subLicInfo = subFileInfo.getSeenLicenses();
			HashMap<SPDXNonStandardLicense, SPDXNonStandardLicense> idMap = foundNonStdLicIds(spdxDoc);
			SPDXNonStandardLicense[] orgNonStdLics = (SPDXNonStandardLicense[]) idMap.keySet().toArray();
			ArrayList <SPDXLicenseInfo> retval = new ArrayList<SPDXLicenseInfo>();
			for(int i = 0; i < subLicInfo.length; i++){
				boolean foundLicId = false;
				for(int q = 0; q < orgNonStdLics.length; q++){
					if(subLicInfo[i].equals(orgNonStdLics[q].getId())){
						foundLicId = true;
					}
					if(!foundLicId){
						retval.add(subLicInfo[i]);
					}else{
						retval.add(idMap.get(orgNonStdLics[q]));
					}
				}
			}
			SPDXLicenseInfo[] mergedFileLics = new SPDXLicenseInfo[retval.size()];
			retval.toArray(mergedFileLics);
			subFileInfo.setSeenLicenses(mergedFileLics);
			retval.clear();
			return subFileInfo;
	}
	
	/**
	 * 
	 * @param spdxDoc
	 * @return foundDocMatch
	 */
	public boolean docInNonStdLicIdMap(SPDXDocument spdxDoc){
		boolean foundDocMatch = false;
		if(NonStdLicIdMap.containsKey(spdxDoc)){
			foundDocMatch = true;
		}
		return foundDocMatch;
	}
	
	/**
	 * 
	 * @param spdxDoc
	 * @return idMap
	 */
	public HashMap<SPDXNonStandardLicense, SPDXNonStandardLicense> foundNonStdLicIds(SPDXDocument spdxDoc){

		HashMap<SPDXNonStandardLicense, SPDXNonStandardLicense> idMap = 
				new HashMap<SPDXNonStandardLicense, SPDXNonStandardLicense>();
		
		idMap = this.NonStdLicIdMap.get(spdxDoc);
		return idMap;
		
	}
	
	public void clearNonStdLicIdMap(){
		this.NonStdLicIdMap.clear();
	}
}
