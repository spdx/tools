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

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;

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

	private HashMap<SpdxDocument, HashMap<AnyLicenseInfo, AnyLicenseInfo>> nonStdLicIdMap = 
			new HashMap<SpdxDocument, HashMap< AnyLicenseInfo, AnyLicenseInfo>>();
	
	/**
	 * 
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
	public ExtractedLicenseInfo mappingNewNonStdLic(SpdxDocument outputDoc, SpdxDocument subDoc, ExtractedLicenseInfo subNonStdLicInfo){
		
		HashMap<AnyLicenseInfo,AnyLicenseInfo> interMap = new HashMap<AnyLicenseInfo,AnyLicenseInfo>();
		if(docInNonStdLicIdMap(subDoc)){
			interMap = nonStdLicIdMap.get(subDoc);
		}    
		String NewNonStdLicId = outputDoc.getDocumentContainer().getNextLicenseRef();
		ExtractedLicenseInfo subCopy = (ExtractedLicenseInfo) subNonStdLicInfo.clone();
		subNonStdLicInfo.setLicenseId(NewNonStdLicId);
		interMap.put(subCopy, subNonStdLicInfo);

		nonStdLicIdMap.put(subDoc, interMap);

		return subNonStdLicInfo;
	}
	
	/**
	 * Maps a subDoc nonstandard license to an existing output document nonstandard license
	 * @param output
	 * @param subDocs
	 * @param subLicense
	 * @param outputDocLicense
	 */
	public void mappingExistingNonStdLic(SpdxDocument output, ExtractedLicenseInfo outputDocLicense, SpdxDocument subDocs, ExtractedLicenseInfo subLicense) {
		HashMap<AnyLicenseInfo,AnyLicenseInfo> interMap;
		if(docInNonStdLicIdMap(subDocs)){
			interMap = nonStdLicIdMap.get(subDocs);
		} else {
			interMap = new HashMap<AnyLicenseInfo,AnyLicenseInfo>();
		}
		interMap.put(outputDocLicense, subLicense);
		nonStdLicIdMap.put(subDocs, interMap);
	}
	
	/**
	 * a method gets a sub SPDX document and its file information. 
	 * Check the non-standard licenses in the file information. Replace the non-standard licenses if this particular license in the HashMap.
	 * @param spdxDoc
	 * @param subFileInfo
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SpdxFile replaceNonStdLicInFile(SpdxDocument spdxDoc, SpdxFile subFileInfo) throws InvalidSPDXAnalysisException{
			AnyLicenseInfo[] subLicInfoInFile = subFileInfo.getLicenseInfoFromFiles();
			HashMap<AnyLicenseInfo, AnyLicenseInfo> idMap = foundInterMap(spdxDoc);
			Set <AnyLicenseInfo> keys = idMap.keySet();
			AnyLicenseInfo[] orgNonStdLics = keys.toArray(new AnyLicenseInfo[idMap.keySet().size()]);
			ArrayList <AnyLicenseInfo> retval = new ArrayList<AnyLicenseInfo>();
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
			AnyLicenseInfo[] mergedFileLics = new AnyLicenseInfo[retval.size()];
			retval.toArray(mergedFileLics);
			subFileInfo.setLicenseInfosFromFiles(mergedFileLics);
			AnyLicenseInfo subConcludedLicInFile = subFileInfo.getLicenseConcluded();
			subFileInfo.setLicenseConcluded(mapLicenseInfo(spdxDoc,subConcludedLicInFile));
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
	public AnyLicenseInfo mapNonStdLicInMap(SpdxDocument spdxDoc, AnyLicenseInfo license){
		HashMap<AnyLicenseInfo, AnyLicenseInfo> idMap = foundInterMap(spdxDoc);
		ExtractedLicenseInfo[] orgNonStdLics = idMap.keySet().toArray(new ExtractedLicenseInfo[idMap.keySet().size()]);
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
	public AnyLicenseInfo mapLicenseInfo(SpdxDocument spdxDoc, AnyLicenseInfo license){
		if(license instanceof ConjunctiveLicenseSet){
			AnyLicenseInfo[] members = ((ConjunctiveLicenseSet) license).getMembers();
			AnyLicenseInfo[] mappedMembers = new AnyLicenseInfo[members.length];
			for(int i = 0; i < members.length; i++){
				mappedMembers[i] = mapLicenseInfo(spdxDoc, members[i]);
			}
			return new ConjunctiveLicenseSet(mappedMembers);
		}
		else if(license instanceof DisjunctiveLicenseSet){
			AnyLicenseInfo[] members = ((DisjunctiveLicenseSet) license).getMembers();
			AnyLicenseInfo[] mappedMembers = new AnyLicenseInfo[members.length];
			for(int q = 0; q < members.length; q++ ){
				mappedMembers[q] = mapLicenseInfo(spdxDoc, members[q]);
			}
			return new DisjunctiveLicenseSet(mappedMembers);
		}else if(license instanceof ExtractedLicenseInfo){
			return license = mapNonStdLicInMap(spdxDoc,(ExtractedLicenseInfo)license);
		}
		return license;	
	}
	/**
	 * 
	 * @param spdxDoc
	 * @return foundDocMatch
	 */
	public boolean docInNonStdLicIdMap(SpdxDocument spdxDoc){
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
	public HashMap<AnyLicenseInfo, AnyLicenseInfo> foundInterMap(SpdxDocument spdxDoc){

		HashMap<AnyLicenseInfo, AnyLicenseInfo> idMap = 
				new HashMap<AnyLicenseInfo, AnyLicenseInfo>();
		
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
