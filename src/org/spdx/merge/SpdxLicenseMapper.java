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
 * Application to build HashMaps to help SPDX documents merging. Currently, it helps mapping any SPDX licenses.
 * @author Gang Ling
 *
 */
public class SpdxLicenseMapper {

	private HashMap<SPDXDocument, HashMap<SPDXLicenseInfo, SPDXLicenseInfo>> nonStdLicIdMap = 
			new HashMap<SPDXDocument, HashMap< SPDXLicenseInfo, SPDXLicenseInfo>>();
	
	private SPDXDocument master = null;
	
	/**
	 * 
	 * @param masterDoc
	 */
	public SpdxLicenseMapper(SPDXDocument masterDoc){
		this.master = masterDoc;
	}
	
	/**
	 * 
	 * @param mergeDocs
	 * @param subNonStdLicInfo
	 * @return subNonStdLicInfo
	 */
	public SPDXNonStandardLicense mappingNonStdLic(SPDXDocument subDoc, SPDXNonStandardLicense subNonStdLicInfo){
		
	    HashMap<SPDXLicenseInfo, SPDXLicenseInfo> interMap = nonStdLicIdMap.get(subDoc);
	    if(interMap.isEmpty()){
	    	interMap = new HashMap<SPDXLicenseInfo, SPDXLicenseInfo>();
	    }
	    
		String NewNonStdLicId = master.getNextLicenseRef();
		SPDXNonStandardLicense subCopy = (SPDXNonStandardLicense) subNonStdLicInfo.clone();
		subNonStdLicInfo.setId(NewNonStdLicId);
		interMap.put(subCopy, subNonStdLicInfo);
		nonStdLicIdMap.put(subDoc, interMap);

		return subNonStdLicInfo;
	}
	
	/**
	 * 
	 * @param childFileInfo
	 * @param licId
	 * @return 
	 */
	public SPDXFile checkNonStdLicId(SPDXDocument spdxDoc, SPDXFile subFileInfo){
			SPDXLicenseInfo[] subLicInfo = subFileInfo.getSeenLicenses();
			HashMap<SPDXLicenseInfo, SPDXLicenseInfo> idMap = foundNonStdLicIds(spdxDoc);
			SPDXNonStandardLicense[] orgNonStdLics = (SPDXNonStandardLicense[]) idMap.keySet().toArray();
			ArrayList <SPDXLicenseInfo> retval = new ArrayList<SPDXLicenseInfo>();
			for(int i = 0; i < subLicInfo.length; i++){
				boolean foundLicId = false;
				for(int q = 0; q < orgNonStdLics.length; q++){
					//if the subfile's orgNonStdLic is found in the subLicInfo, 
					if(subLicInfo[i].toString().equals(orgNonStdLics[q].getId())){
						foundLicId = true;
					}
					//then we replace the orgNonStdLic's id with new id from the internal map
					if(foundLicId){
						retval.add(idMap.get(orgNonStdLics[q]));
					}else{
						retval.add(subLicInfo[i]);//if not, add this license to retval array directly. this license must be not non-standard license.
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
	public HashMap<SPDXLicenseInfo, SPDXLicenseInfo> foundNonStdLicIds(SPDXDocument spdxDoc){

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
