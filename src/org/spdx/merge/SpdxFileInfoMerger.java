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
import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXFile;

/**
 * Application to merge SPDX files information into one unique result. 
 * @author Gang Ling
 *
 */

public class SpdxFileInfoMerger{
	
	/**
	 * 
	 * @param mergeDocs
	 * @return mergeFileInfo
	 * @throws InvalidSPDXAnalysisException
	 */
	public ArrayList<SPDXFile> mergeFileInfo(SPDXDocument[] mergeDocs)throws InvalidSPDXAnalysisException{
			
	        //an array to store an deep copy of file information from master document.
			SPDXFile[] masterFileInfo = mergeDocs[0].getSpdxPackage().getFiles();
			
			//convert masterFileInfo array into an arrayList which will be returned to main class at end
			ArrayList<SPDXFile> fileInfoResult = new ArrayList<SPDXFile>(Arrays.asList(cloneFiles(masterFileInfo)));
			
			SpdxLicenseMapper mappingHelper = new SpdxLicenseMapper();
			
			for(int q = 1; q < mergeDocs.length; q++){
				//an array to store an deep copy of file information from current child document
				SPDXFile[] subFileInfo = cloneFiles(mergeDocs[q].getSpdxPackage().getFiles());
				
				for(int k = 0; k < fileInfoResult.size(); k++){
					boolean foundNameMatch = false;
					boolean foundSha1Match = false;
					
					//determine if any file name matched
					for(int p = 0; p < subFileInfo.length; p++){
						if(fileInfoResult.get(k).getName().equalsIgnoreCase(subFileInfo[p].getName())){
							foundNameMatch = true;
						}
						//determine if any checksum matched
						if(fileInfoResult.get(k).getSha1().equals(subFileInfo[p].getSha1())){
							foundSha1Match = true;
						}
						//if both name and checksum are not matched, then check the license Ids from child files 
						if(!foundNameMatch && !foundSha1Match){
							//check whether licIdMap has this particular child document  
							if(mappingHelper.docInNonStdLicIdMap(mergeDocs[q])){
								mappingHelper.checkNonStdLicId(mergeDocs[q], subFileInfo[p]);
								fileInfoResult.add(subFileInfo[p]);
							}else{
								fileInfoResult.add(subFileInfo[p]);
							}
						}else{
							//if both name and checksum are matched, then merge the DOAPProject information 
							boolean foundMasterDOAP = false;
							boolean foundChildDOAP = false;
						    if(checkDOAPProject(fileInfoResult.get(k))){
						    	foundMasterDOAP = true;
						    }
						    if(checkDOAPProject(subFileInfo[p])){
						    	foundChildDOAP = true;
						    }
						    if(foundMasterDOAP && foundChildDOAP){
						    	DOAPProject[] masterArtifactOf = cloneDOAPProject(fileInfoResult.get(k).getArtifactOf());
						    	DOAPProject[] subArtifactOfA = cloneDOAPProject(subFileInfo[p].getArtifactOf());
						    	DOAPProject[] mergedArtifactOf = mergeDOAPInfo(masterArtifactOf, subArtifactOfA);
						    	fileInfoResult.get(k).setArtifactOf(mergedArtifactOf);//assume the setArtifactOf() runs as over-write data
						    	
						    }
						    //if master doesn't have DOAPProject information but sub file has 
						    if(!foundMasterDOAP && foundChildDOAP){
						    	DOAPProject[] childArtifactOfB = cloneDOAPProject(subFileInfo[p].getArtifactOf());
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
	 * @param subArtifactOfA
	 * @return mergedArtfactOf
	 */
	public DOAPProject[] mergeDOAPInfo(DOAPProject[] MasterArtifactOf, DOAPProject[] subArtifactOfA){
		ArrayList<DOAPProject> retval = new ArrayList<DOAPProject>(Arrays.asList(MasterArtifactOf));
		for(int l = 0; l < subArtifactOfA.length; l++){
			retval.add(subArtifactOfA[l]);//assume add all DOAPProject include both artifactOf and Homepage
		}
		DOAPProject[] mergedArtifactOf = new DOAPProject[retval.size()];
		retval.toArray(mergedArtifactOf);
		retval.clear();
		return mergedArtifactOf;
	}
	
	/**
	 * 
	 * @param orgFilesArray
	 * @return clonedFilesArray
	 */
	public SPDXFile[] cloneFiles(SPDXFile[] orgFilesArray){
		SPDXFile[] clonedFilesArray = new SPDXFile[orgFilesArray.length];
		for(int h = 0; h < orgFilesArray.length; h++){
			clonedFilesArray[h] = orgFilesArray[h].clone();
		}
		return clonedFilesArray;
	}
	
	/**
	 * 
	 * @param orgProjectArray
	 * @return clonedProjectArray
	 */
	public DOAPProject[] cloneDOAPProject(DOAPProject[] orgProjectArray){
		DOAPProject[] clonedProjectArray = new DOAPProject[orgProjectArray.length];
		for(int j = 0; j < orgProjectArray.length; j++){
			clonedProjectArray[j] = orgProjectArray[j].clone();
		}
		return clonedProjectArray;		
	}
}
