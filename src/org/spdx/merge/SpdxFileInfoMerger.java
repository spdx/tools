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
import java.util.Arrays;
import java.util.HashMap;

import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXNonStandardLicense;

/**
 * Application to merge SPDX files information into one unique result. 
 * @author Gang Ling
 *
 */

public class SpdxFileInfoMerger{
	
	/**
	 * 
	 * @param mergeDocs
	 * @param licIdMap
	 * @return mergeFileInfo
	 * @throws InvalidSPDXAnalysisException
	 */
	public ArrayList<SPDXFile> mergeFileInfo(SPDXDocument[] mergeDocs,
		            HashMap<SPDXDocument,HashMap<SPDXNonStandardLicense,SPDXNonStandardLicense>> licIdMap)throws InvalidSPDXAnalysisException{
			
	        //an array to store an deep copy of file information from master document.
			SPDXFile[] masterFileInfo = mergeDocs[0].getSpdxPackage().getFiles().clone();
			
			//convert masterFileInfo array into an arrayList which will be returned to main class at end
			ArrayList<SPDXFile> fileInfoResult = new ArrayList<SPDXFile>(Arrays.asList(masterFileInfo));
			
			for(int q = 1; q < mergeDocs.length; q++){
				//an array to store an deep copy of file information from current child document
				SPDXFile[] childFileInfo = mergeDocs[q].getSpdxPackage().getFiles().clone();
				for(int k = 0; k < fileInfoResult.size(); k++){
					boolean foundNameMatch = false;
					boolean foundSha1Match = false;
					//determine if any file name matched
					for(int p = 0; p < childFileInfo.length; p++){
						if(fileInfoResult.get(k).getName().equalsIgnoreCase(childFileInfo[p].getName())){
							foundNameMatch = true;
						}
						//determine if any checksum matched
						if(fileInfoResult.get(k).getSha1().equals(childFileInfo[p].getSha1())){
							foundSha1Match = true;
						}
						//if both name and checksum are not matched, then check the license Ids from child files 
						if(!foundNameMatch && !foundSha1Match){
							//check whether licIdMap has this particular child document  
							if(licIdMap.containsKey(mergeDocs[q])){
								checkNonStandardLicId(childFileInfo[p],licIdMap.get(mergeDocs[q]));
								fileInfoResult.add(childFileInfo[p]);
							}else{
								fileInfoResult.add(childFileInfo[p]);
							}
						}else{
							//if both name and checksum are matched, then merge the DOAPProject information 
							boolean foundMasterDOAP = false;
							boolean foundChildDOAP = false;
						    if(checkDOAPProject(fileInfoResult.get(k))){
						    	foundMasterDOAP = true;
						    }
						    if(checkDOAPProject(childFileInfo[p])){
						    	foundChildDOAP = true;
						    }
						    if(foundMasterDOAP && foundChildDOAP){
						    	DOAPProject[] masterArtifactOf = fileInfoResult.get(k).getArtifactOf();
						    	DOAPProject[] childArtifactOfA = childFileInfo[p].getArtifactOf();
						    	DOAPProject[] mergedArtifactOf = mergeDOAPInfo(masterArtifactOf, childArtifactOfA);
						    	fileInfoResult.get(k).setArtifactOf(mergedArtifactOf);//assume the setArtifactOf() runs as over-write data
						    	
						    }
						    //if master doesn't have DOAPProject information but child file has 
						    if(!foundMasterDOAP && foundChildDOAP){
						    	DOAPProject[] childArtifactOfB = childFileInfo[p].getArtifactOf();
						    	fileInfoResult.get(k).setArtifactOf(childArtifactOfB);//assume add artifact and Homepage at same time
						    }
						}
					}
				}
			}				
		return fileInfoResult;
	}
	
	/**
	 * 
	 * @param childFileInfo
	 * @param licId
	 * @return 
	 */
	public void checkNonStandardLicId(SPDXFile childFileInfo, HashMap<SPDXNonStandardLicense,SPDXNonStandardLicense> licId){
			SPDXLicenseInfo[] childLicenseInfo = childFileInfo.getSeenLicenses();
			SPDXNonStandardLicense[] orgLics = (SPDXNonStandardLicense[]) licId.keySet().toArray();
			ArrayList <SPDXLicenseInfo> retval = new ArrayList<SPDXLicenseInfo>();
			for(int i = 0; i < childLicenseInfo.length; i++){
				boolean foundLicId = false;
				for(int q = 0; q < orgLics.length; q++){
					if(childLicenseInfo[i].equals(orgLics[q].getId())){
						foundLicId = true;
					}
					if(!foundLicId){
						retval.add(childLicenseInfo[i]);
					}else{
						retval.add(licId.get(orgLics[q]));
					}
				}
			}
			SPDXLicenseInfo[] mergedFileLics = new SPDXLicenseInfo[retval.size()];
			retval.toArray(mergedFileLics);
			childFileInfo.setSeenLicenses(mergedFileLics);
			retval.clear();
	}
	
	/**
	 * 
	 * @param spdxFile
	 * @return foundDOAPProject
	 */
	public boolean checkDOAPProject (SPDXFile spdxFile){
		boolean foundDOAPProject = false;
		if(spdxFile.getArtifactOf() != null && spdxFile.getArtifactOf().length > 0){
			foundDOAPProject = true;
		}
		return foundDOAPProject;
	}
	
	/**
	 * 
	 * @param MasterArtifactOf
	 * @param childArtifactOfA
	 * @return mergedArtfactOf
	 */
	public DOAPProject[] mergeDOAPInfo(DOAPProject[] MasterArtifactOf, DOAPProject[] childArtifactOfA){
		ArrayList<DOAPProject> retval = new ArrayList<DOAPProject>(Arrays.asList(MasterArtifactOf));
		for(int l = 0; l < childArtifactOfA.length; l++){
			retval.add(childArtifactOfA[l]);//assume add all DOAPProject include both artifactOf and Homepage
		}
		DOAPProject[] mergedArtifactOf = new DOAPProject[retval.size()];
		retval.toArray(mergedArtifactOf);
		retval.clear();
		return mergedArtifactOf;
	}
}

