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
import java.util.List;

import org.spdx.rdfparser.model.DoapProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;

/**
 * Application to merge SPDX files information into one unique result. 
 * @author Gang Ling
 *
 */

public class SpdxFileInfoMerger{
	
	private SpdxDocument master = null;
	private SpdxLicenseMapper mapper = null;
	
	/**
	 * 
	 * @param master
	 */
	public SpdxFileInfoMerger(SpdxDocument master, SpdxLicenseMapper mapper){
		this.master = master;
		this.mapper = mapper;
	}

	/**
	 * 
	 * @param subDocs
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxFile[] mergeFileInfo(SpdxDocument[] subDocs)throws InvalidSPDXAnalysisException{
			
	        //an array to store an deep copy of file information from master document.
			List <SpdxFile> masterFileInfo = master.getDocumentContainer().findAllFiles();
			
			//convert masterFileInfo array into an arrayList which will be returned to main class at end
			List<SpdxFile> retval = new ArrayList<SpdxFile>((cloneFiles(masterFileInfo)));
			
			for(int q = 0; q < subDocs.length; q++){
				//an array to store an deep copy of file information from current child document
				SpdxFile[] subFileInfo = cloneList(subDocs[q].getDocumentContainer().findAllFiles());
				
				for(int k = 0; k < subFileInfo.length; k++){
					boolean foundNameMatch = false;
					boolean foundSha1Match = false;
					SpdxFile temp = null;
					
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
							//if both name and checksum are matched, then merge the DoapProject information
							//still need to figure out how to solve the issue if license and other information is not exactly the same
							boolean foundMasterDOAP = false;
							boolean foundChildDOAP = false;
						    if(checkDoapProject(temp)){
						    	foundMasterDOAP = true;
						    	break;
						    }
						    if(checkDoapProject(subFileInfo[k])){
						    	foundChildDOAP = true;
						    	break;
						    }
						    if(foundMasterDOAP && foundChildDOAP){
						    	DoapProject[] masterArtifactOf = cloneDoapProject(temp.getArtifactOf());
						    	DoapProject[] subArtifactOfA = cloneDoapProject(subFileInfo[k].getArtifactOf());
						    	DoapProject[] mergedArtifactOf = mergeDOAPInfo(masterArtifactOf, subArtifactOfA);
						    	temp.setArtifactOf(mergedArtifactOf);//assume the setArtifactOf() runs as over-write data
						    	
						    }
						    //if master doesn't have DoapProject information but sub file has 
						    if(!foundMasterDOAP && foundChildDOAP){
						    	DoapProject[] childArtifactOfB = cloneDoapProject(subFileInfo[k].getArtifactOf());
						    	temp.setArtifactOf(childArtifactOfB);//assume add artifact and Homepage at same time
						    }
						}
					}			
			}
		SpdxFile[] fileMergeResult = new SpdxFile[retval.size()];
		retval.toArray(fileMergeResult);
		retval.clear();
		return fileMergeResult;
	}
		
	/**
	 * 
	 * @param spdxFile
	 * @return foundDoapProject
	 */
	public boolean checkDoapProject (SpdxFile spdxFile){
		boolean foundDoapProject = false;
		if(spdxFile.getArtifactOf() != null && spdxFile.getArtifactOf().length > 0){
			foundDoapProject = true;
		}
		return foundDoapProject;
	}
	
	/**
	 * 
	 * @param masterArtifactOf
	 * @param subArtifactOf
	 * @return
	 */
	public DoapProject[] mergeDOAPInfo(DoapProject[] masterArtifactOf, DoapProject[] subArtifactOf){
		ArrayList<DoapProject> retval = new ArrayList<DoapProject>(Arrays.asList(masterArtifactOf));
		
		for(int l = 0; l < subArtifactOf.length; l++){
			boolean foundMatch = false;
			for(int u = 0; u < masterArtifactOf.length; u++){
				if(subArtifactOf[l].equals(masterArtifactOf[u])){
					foundMatch = true;
					break;
				}
			}
			if(!foundMatch){
				retval.add(subArtifactOf[l]);//assume add all DoapProject include both artifactOf and Homepage
			}
		}
		DoapProject[] mergedArtifactOf = new DoapProject[retval.size()];
		retval.toArray(mergedArtifactOf);
		retval.clear();
		return mergedArtifactOf;
	}
	
	/**
	 * 
	 * @param filesArray
	 * @return clonedFilesArray
	 */
	public List <SpdxFile> cloneFiles(List <SpdxFile> filesArray){
		List <SpdxFile> clonedFilesArray = new ArrayList <SpdxFile>();
		for(int h = 0; h < filesArray.size(); h++){
			clonedFilesArray.add(filesArray.get(h).clone());
		}
		return clonedFilesArray;
	}

	/**
	 * 
	 * @param filesList
	 * @return clonedFilesArray
	 */
	public SpdxFile[] cloneList(List<SpdxFile> filesList){
		List<SpdxFile> clonedFilesList = new ArrayList<SpdxFile>();
		for(int h = 0; h < filesList.size(); h++){
			clonedFilesList.add(filesList.get(h));
		}
		SpdxFile[] clonedFilesArray = new SpdxFile[clonedFilesList.size()];
		clonedFilesList.toArray(clonedFilesArray);
		return clonedFilesArray;	
	}
	/**
	 * 
	 * @param orgProjectArray
	 * @return clonedProjectArray
	 */
	public DoapProject[] cloneDoapProject(DoapProject[] orgProjectArray){
		DoapProject[] clonedProjectArray = new DoapProject[orgProjectArray.length];
		for(int j = 0; j < orgProjectArray.length; j++){
			clonedProjectArray[j] = orgProjectArray[j].clone();
		}
		return clonedProjectArray;		
	}
}
