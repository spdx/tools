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

import java.util.List;

import org.spdx.compare.LicenseCompareHelper;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.model.SpdxDocument;

import com.google.common.collect.Lists;

/**
 * Application to merge SPDX documents' non-standard license information and return the results to the merge main class
 * The non-standard license information from the output SPDX document will add to the result arraylist directly.
 * The non-standard license information from the sub SPDX document will be compared with license information in the result arraylist.
 * Any new license information will add to the result arraylist with replacing the license ID. 
 *  
 * @author Gang Ling
 *
 */
public class SpdxLicenseInfoMerger {
	
	private SpdxDocument output = null;
	private SpdxLicenseMapper mapper = null;
	
	public SpdxLicenseInfoMerger(SpdxDocument outputDoc, SpdxLicenseMapper mapper){
		this.output = outputDoc;
		this.mapper = mapper;
	}
    
	/**
	 * 
	 * @param subDocs
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	public ExtractedLicenseInfo[] mergeNonStdLic(SpdxDocument[] subDocs) throws InvalidSPDXAnalysisException{
		
		//an array to hold the non-standard license info from outputDoc
		ExtractedLicenseInfo[] masterNonStdLicInfo = output.getExtractedLicenseInfos();
		
		//an arrayList to hold the final result 
		List<ExtractedLicenseInfo> retval = Lists.newArrayList(masterNonStdLicInfo);
		
		//read each child SPDX document
		for(int i = 0; i < subDocs.length; i++){
			
			//an array to hold non-standard license info from current child SPDX document and clone the data
			ExtractedLicenseInfo[] subNonStdLicInfo = cloneNonStdLic(subDocs[i].getExtractedLicenseInfos());
												
			//compare non-standard license information
			ExtractedLicenseInfo foundLicense = null;
	        for(int k = 0; k < subNonStdLicInfo.length; k++){
	        	boolean foundTextMatch = false;
	        	for(int p = 0; p < retval.size(); p++){
	        		if(LicenseCompareHelper.isLicenseTextEquivalent(subNonStdLicInfo[k].getExtractedText(), retval.get(p).getExtractedText())){
	        			foundTextMatch = true;
	        			foundLicense = retval.get(p);
	        			break;
	           		}
	        	}
	        	if (foundTextMatch) {
	        		mapper.mappingExistingNonStdLic(output, foundLicense, subDocs[i], subNonStdLicInfo[k]);
	        	}
	        	if(!foundTextMatch){
	        		retval.add((mapper.mappingNewNonStdLic(output, subDocs[i], subNonStdLicInfo[k])));	    
	        	}
	        }

	   }
		ExtractedLicenseInfo[] nonStdLicMergeResult = new ExtractedLicenseInfo[retval.size()];
		retval.toArray(nonStdLicMergeResult);
		retval.clear();
		return nonStdLicMergeResult;
	}
	
	/**
	 * 
	 * @param orgNonStdLicArray
	 * @return clonedNonStdLicArray
	 */
	public ExtractedLicenseInfo[] cloneNonStdLic(ExtractedLicenseInfo[] orgNonStdLicArray){
		ExtractedLicenseInfo[] clonedNonStdLicArray = new ExtractedLicenseInfo[orgNonStdLicArray.length];
		for(int q = 0; q < orgNonStdLicArray.length; q++){
			clonedNonStdLicArray[q] = (ExtractedLicenseInfo) orgNonStdLicArray[q].clone();
		}
		return clonedNonStdLicArray;
	}
}
