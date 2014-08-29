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
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.SPDXFile;

/**
 * Application to merge SPDX files information into one unique result. 
 * @author Gang Ling
 *
 */

public class SpdxFileInfoMerger{
	
	private SPDXPackage packageInfo = null;
	private SpdxLicenseMapper mapper = null;
	
	/**
	 * 
	 * @param packageInfoResult
	 */
	public SpdxFileInfoMerger(SPDXPackage packageInfoResult, SpdxLicenseMapper mapper){
		this.packageInfo = packageInfoResult;
		this.mapper = mapper;
	}

	/**
	 * 
	 * @param subDocs
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	public SPDXFile[] mergeFileInfo(SPDXDocument[] subDocs)throws InvalidSPDXAnalysisException{
			
	        //an array to store an deep copy of file information from master document.
			SPDXFile[] masterFileInfo = packageInfo.getFiles();
			
			//convert masterFileInfo array into an arrayList which will be returned to main class at end
			ArrayList<SPDXFile> retval = new ArrayList<SPDXFile>(Arrays.asList(cloneFiles(masterFileInfo)));
			
			for(int q = 0; q < subDocs.length; q++){
				//an array to store an deep copy of file information from current child document
				SPDXFile[] subFileInfo = cloneFiles(subDocs[q].getSpdxPackage().getFiles());
				
				for(int k = 0; k < subFileInfo.length; k++){
					boolean foundNameMatch = false;
					boolean foundSha1Match = false;
					SPDXFile temp = null;
					
					//determine if any file name matched
					for(int p = 0; p < retval.size() ; p++){
						temp = retval.get(p);
						if(subFileInfo[k].getName().equalsIgnoreCase(retval.get(p).getName())){
							foundNameMatch = true;
						}
						//determine if any checksum matched
						if(subFileInfo[k].getSha1().equals(retval.get(p).getSha1())){
							foundSha1Match = true;
							break;
						}
					}
						//if both name and checksum are not matched, then check the license Ids from child files 
						if(!foundNameMatch && !foundSha1Match){
							//check whether licIdMap has this particular child document  
							if(mapper.docInNonStdLicIdMap(subDocs[q])){
								mapper.replaceNonStdLicInFile(subDocs[q], subFileInfo[k]);
								retval.add(subFileInfo[k]);
							}else{
								retval.add(subFileInfo[k]);
							}
						}else{
							//if both name and checksum are matched, then merge the DOAPProject information
							//still need to figure out how to solve the issue if license and other information is not exactly the same
							boolean foundMasterDOAP = false;
							boolean foundChildDOAP = false;
						    if(checkDOAPProject(temp)){
						    	foundMasterDOAP = true;
						    	break;
						    }
						    if(checkDOAPProject(subFileInfo[k])){
						    	foundChildDOAP = true;
						    	break;
						    }
						    if(foundMasterDOAP && foundChildDOAP){
						    	DOAPProject[] masterArtifactOf = cloneDOAPProject(temp.getArtifactOf());
						    	DOAPProject[] subArtifactOfA = cloneDOAPProject(subFileInfo[k].getArtifactOf());
						    	DOAPProject[] mergedArtifactOf = mergeDOAPInfo(masterArtifactOf, subArtifactOfA);
						    	temp.setArtifactOf(mergedArtifactOf);//assume the setArtifactOf() runs as over-write data
						    	
						    }
						    //if master doesn't have DOAPProject information but sub file has 
						    if(!foundMasterDOAP && foundChildDOAP){
						    	DOAPProject[] childArtifactOfB = cloneDOAPProject(subFileInfo[k].getArtifactOf());
						    	temp.setArtifactOf(childArtifactOfB);//assume add artifact and Homepage at same time
						    }
						}
					}			
			}
		SPDXFile[] fileMergeResult = new SPDXFile[retval.size()];
		retval.toArray(fileMergeResult);
		retval.clear();
		return fileMergeResult;
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
	 * @param masterArtifactOf
	 * @param subArtifactOf
	 * @return
	 */
	public DOAPProject[] mergeDOAPInfo(DOAPProject[] masterArtifactOf, DOAPProject[] subArtifactOf){
		ArrayList<DOAPProject> retval = new ArrayList<DOAPProject>(Arrays.asList(masterArtifactOf));
		
		for(int l = 0; l < subArtifactOf.length; l++){
			boolean foundMatch = false;
			for(int u = 0; u < masterArtifactOf.length; u++){
				if(subArtifactOf[l].equals(masterArtifactOf[u])){
					foundMatch = true;
					break;
				}
			}
			if(!foundMatch){
				retval.add(subArtifactOf[l]);//assume add all DOAPProject include both artifactOf and Homepage
			}
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
