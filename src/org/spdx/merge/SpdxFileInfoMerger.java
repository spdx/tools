/**
 * Copyright (c) 2014 Source Auditor Inc.
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
import java.util.Map;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXFile;

/**
 * @author gling
 *
 */
public class SpdxFileInfoMerger {

	ArrayList<SPDXDocument> spdxDocList = new ArrayList<SPDXDocument>();
	ArrayList<SPDXFile> alFiles = new ArrayList<SPDXFile>();
	SPDXFile[] tempfileArray;
	private String fileNameA;
	private String fileNameB;
	private String sha1A;
	private String sha1B;
	
	public ArrayList<SPDXFile> FileInfoMerge
		(HashMap<String,SPDXDocument> spdxDocMap)throws InvalidSPDXAnalysisException{
		    //get all documents and store into a list
			for (Map.Entry<String, SPDXDocument> entry : spdxDocMap.entrySet()){
				spdxDocList.add(entry.getValue());
			}
			//get all file level info. and store into a list
	        for (int i=0; i < spdxDocList.size(); i++){
	        	 tempfileArray = spdxDocList.get(i).getSpdxPackage().getFiles();
	        	 for (int j = 0; j < tempfileArray.length; j++){
	        		 alFiles.add(j,tempfileArray[j]);
	        	 }
	        }
	        //compare each file name and sha1 in the list, and remove the duplicated info
	        for (int i= alFiles.size(); i >= 0; i--){
	        		fileNameA = alFiles.get(i).getName();
	        		sha1A = alFiles.get(i).getSha1();
	        		for (int j = alFiles.size(); j >= 0; i--){
	        			if(i == j){
	        				continue;
	        			}
	        			else{
	        				fileNameB = alFiles.get(j).getName();
	        				sha1B = alFiles.get(j).getSha1();
	        			}
	        			if(fileNameA.equals(fileNameB)&& sha1A.equals(sha1B)){
	        					alFiles.remove(j); 				
	        			}
	        			else{
	        				fileNameA ="";
	        				fileNameB ="";
	        				sha1A ="";
	        				sha1B ="";
	        			}
	        	}
	        }
            
			return alFiles;		
	}
	
}

