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

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.JavaSha1ChecksumGenerator;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.VerificationCodeGenerator;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

/**
 * Application to merge package information from input SPDX documents and file information merging result.
 * @author Gang Ling
 *
 */
public class SpdxPackageInfoMerger {

		private SPDXPackage packageInfoResult = null;
		private SPDXDocument[] allDocs;
		public SpdxPackageInfoMerger(SPDXPackage packageInfo, SPDXDocument[] mergeDocs){
			this.packageInfoResult = packageInfo;
			this.allDocs = mergeDocs;
		}
		
		/**
		 * 
		 * @param subDocs
		 * @param fileMergeResult
		 * @return
		 * @throws InvalidSPDXAnalysisException
		 * @throws NoSuchAlgorithmException
		 * @throws InvalidLicenseStringException
		 */
		public SPDXPackage mergePackageInfo(SPDXDocument[] subDocs, SPDXFile[] fileMergeResult) 
				throws InvalidSPDXAnalysisException, NoSuchAlgorithmException, InvalidLicenseStringException{
			
			String[] skippedFiles = collectSkippedFiles();
			VerificationCodeGenerator vg = new VerificationCodeGenerator(new JavaSha1ChecksumGenerator());
			SpdxPackageVerificationCode result = vg.generatePackageVerificationCode(fileMergeResult, skippedFiles);
			packageInfoResult.setVerificationCode(result);
			
			SPDXLicenseInfo[] licsInFile = collectLicsInFiles(fileMergeResult);
			packageInfoResult.setLicenseInfoFromFiles(licsInFile);
			
			SPDXLicenseInfo declaredLicense = SPDXLicenseInfoFactory.parseSPDXLicenseString("NOASSERTION");
			packageInfoResult.setDeclaredLicense(declaredLicense);		
			
			String licComments = translateSubDelcaredLicsIntoComments(subDocs);
			packageInfoResult.setLicenseComment(licComments);
						
			return packageInfoResult;			
		}
		
		/**
		 * method to collect all skipped files from input SPDX documents
		 * @return
		 * @throws InvalidSPDXAnalysisException
		 */
		public String[] collectSkippedFiles() throws InvalidSPDXAnalysisException{
			ArrayList<String> excludedFileNamesList = new ArrayList<String>();
			for(int p = 0; p < allDocs.length; p++){
				String[] retval = allDocs[p].getSpdxPackage().getVerificationCode().getExcludedFileNames();
				
				if(excludedFileNamesList.size() == 0){
					for(int i = 0; i < retval.length; i++){
						excludedFileNamesList.add(i, retval[i]);
					}
				}else{
					for(int k = 0; k < retval.length; k++){
						boolean foundNameMatch = false;
						for(int q = 0; q < excludedFileNamesList.size(); q++){
							if(retval[k].equalsIgnoreCase(excludedFileNamesList.get(q))){
								foundNameMatch = true;
							}
						}
						if(!foundNameMatch){
							excludedFileNamesList.add(retval[k]);
						}
					}
				}
			}
			String[] excludedFileNamesArray = new String[excludedFileNamesList.size()];
			excludedFileNamesList.toArray(excludedFileNamesArray);
			excludedFileNamesList.clear();
			return excludedFileNamesArray;
		}
		
		/**
		 * method to collect all license information from file merging result
		 * @param fileMergeResult
		 * @return
		 */
		public SPDXLicenseInfo[] collectLicsInFiles(SPDXFile[] fileMergeResult){
			ArrayList<SPDXLicenseInfo> licsList = new ArrayList<SPDXLicenseInfo>();
			for(int a = 0; a < fileMergeResult.length; a++){
				SPDXLicenseInfo[] retval = fileMergeResult[a].getSeenLicenses();
				if(licsList.size() == 0){
					for(int b = 0; b < retval.length; b++){
						licsList.add(b, retval[b]);
					}
				}else{
					for(int c = 0; c < retval.length; c++){
						boolean foundLicMatch = false;
						for(int d = 0; d < licsList.size(); d++){
							if(retval[c].equals(licsList.get(d))){
								foundLicMatch = true;
								break;
							}
						}
						if(!foundLicMatch){
							licsList.add(retval[c]);
						}
					}
				}
			}
			SPDXLicenseInfo[] licsInFile = new SPDXLicenseInfo[licsList.size()];
			licsList.toArray(licsInFile);
			licsList.clear();
			return licsInFile;	
		}
		
		/**
		 * 
		 * @param subDocs
		 * @return
		 * @throws InvalidSPDXAnalysisException
		 */
		public String translateSubDelcaredLicsIntoComments(SPDXDocument[] subDocs) throws InvalidSPDXAnalysisException{
			SpdxLicenseMapper mapper = new SpdxLicenseMapper();
				if(!mapper.isNonStdLicIdMapEmpty()){
					StringBuilder buffer = new StringBuilder(packageInfoResult.getLicenseComment() 
							+ ". This package merged several packages and the sub-package contain the following licenses:");
			
					for(int k = 0; k < subDocs.length; k++){
						if(mapper.docInNonStdLicIdMap(subDocs[k])){
							SPDXLicenseInfo license = subDocs[k].getSpdxPackage().getDeclaredLicense();
							SPDXLicenseInfo result = mapper.mapLicenseInfo(subDocs[k], license); 
							buffer.append(subDocs[k].getSpdxPackage().getFileName());
							buffer.append(" (" + result.toString() + ") ");
						}else{				
							buffer.append(subDocs[k].getSpdxPackage().getFileName());
							buffer.append(" (" + subDocs[k].getSpdxPackage().getDeclaredLicense().toString() + ") ");
						}
					}			
					return buffer.toString();
			}else{
				return packageInfoResult.getLicenseComment();
			}
		}
}
