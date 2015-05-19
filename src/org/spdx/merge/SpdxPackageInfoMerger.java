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
import java.util.Arrays;
import java.util.List;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.JavaSha1ChecksumGenerator;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.VerificationCodeGenerator;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

/**
 * Application to merge package information from input SPDX documents and file information merging result.
 * @author Gang Ling
 *
 */
public class SpdxPackageInfoMerger {

		private SpdxPackage packageInfoResult = null;
		private SpdxDocument[] allDocs;
		
		private List<SpdxPackage> packagesResult = null;
		private SpdxDocument[] subDocs = null;
		
		/* 
		 * @param masterPackagesInfo
		 * @param subDocs
		 */
		public SpdxPackageInfoMerger(List<SpdxPackage> masterPackagesInfo, SpdxDocument[] subDocs){
			this.packagesResult = masterPackagesInfo;
			this.subDocs = subDocs;
		}
		
		/* A method to merge all packages' information from sub list documents into master document
		 * @param subDocs
		 * @param fileMergeResult
		 * @return packagesResult
		 * @throws InvalidSPDXAnalysisException
		 */
		public List <SpdxPackage> mergePackagesInfo(SpdxDocument[] subDocs, SpdxFile[] fileMergeResult)
				throws InvalidSPDXAnalysisException{
			
			List<SpdxPackage> retval = new ArrayList<SpdxPackage>(clonePackages(packagesResult));
			
			for(int i = 0; i < subDocs.length; i++){				
				List<SpdxPackage> subPackagesInfo = subDocs[i].getDocumentContainer().findAllPackages();
				SpdxPackage temp = null;
				for(int p = 0; p < subPackagesInfo.size(); p++){
					boolean foundNameMatch = false;
					boolean foundSha1Match = false;
	                temp = subPackagesInfo.get(p);
	                
	                for(int q = 0; q < retval.size(); q++){
	                	if(retval.get(q).getName().equalsIgnoreCase(temp.getName()))
	                		foundNameMatch = true;
	                	if(retval.get(q).getSha1().equals(temp.getSha1())){
	                		foundSha1Match = true;
	                		break;
	                	}
	                }
	                if(!foundNameMatch && !foundSha1Match){
	                	retval.add(temp);
	                }
	                
				}
				
			}
			return packagesResult;
		}

		
		/**
		 * 
		 * @param packagesArray
		 * @return clonedPackagesArray
		 */
		public List<SpdxPackage> clonePackages(List<SpdxPackage> packagesList){
			List<SpdxPackage> clonedPackagesList = new ArrayList<SpdxPackage>();
			for(int h = 0; h < packagesList.size(); h++){
				clonedPackagesList.add(packagesList.get(h).clone());
			}
			return clonedPackagesList;
		}
		
		@Deprecated
		/**
		 * 
		 * @param subDocs
		 * @param fileMergeResult
		 * @return
		 * @throws InvalidSPDXAnalysisException
		 * @throws NoSuchAlgorithmException
		 * @throws InvalidLicenseStringException
		 */
		public SpdxPackage mergePackageInfo(SpdxDocument[] subDocs, SpdxFile[] fileMergeResult) 
				throws InvalidSPDXAnalysisException, NoSuchAlgorithmException, InvalidLicenseStringException{
			
			String[] skippedFiles = collectSkippedFiles();
			VerificationCodeGenerator vg = new VerificationCodeGenerator(new JavaSha1ChecksumGenerator());
			SpdxPackageVerificationCode result = vg.generatePackageVerificationCode(fileMergeResult, skippedFiles);
			packageInfoResult.setPackageVerificationCode(result);
			
			AnyLicenseInfo[] licsInFile = collectLicsInFiles(fileMergeResult);
			packageInfoResult.setLicenseInfosFromFiles(licsInFile);
			
			AnyLicenseInfo declaredLicense = LicenseInfoFactory.parseSPDXLicenseString("NOASSERTION");
			packageInfoResult.setLicenseDeclared(declaredLicense);		
			
			String licComments = translateSubDelcaredLicsIntoComments(subDocs);
			packageInfoResult.setLicenseComments(licComments);
						
			return packageInfoResult;			
		}
		
		@Deprecated
		/**
		 * method to collect all skipped files from input SPDX documents
		 * @return
		 * @throws InvalidSPDXAnalysisException
		 */
		public String[] collectSkippedFiles() throws InvalidSPDXAnalysisException{
			ArrayList<String> excludedFileNamesList = new ArrayList<String>();
			for(int p = 0; p < allDocs.length; p++){
			  List<SpdxPackage> packageList = allDocs[p].getDocumentContainer().findAllPackages();
			  for(int h = 0; h < packageList.size(); h++){
				String[] retval = packageList.get(h).getPackageVerificationCode().getExcludedFileNames();
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
			}
			String[] excludedFileNamesArray = new String[excludedFileNamesList.size()];
			excludedFileNamesList.toArray(excludedFileNamesArray);
			excludedFileNamesList.clear();
			return excludedFileNamesArray;
		}
		@Deprecated
		/**
		 * method to collect all license information from file merging result
		 * @param fileMergeResult
		 * @return
		 */
		public AnyLicenseInfo[] collectLicsInFiles(SpdxFile[] fileMergeResult){
			ArrayList<AnyLicenseInfo> licsList = new ArrayList<AnyLicenseInfo>();
			for(int a = 0; a < fileMergeResult.length; a++){
				AnyLicenseInfo[] retval = fileMergeResult[a].getLicenseInfoFromFiles();
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
			AnyLicenseInfo[] licsInFile = new AnyLicenseInfo[licsList.size()];
			licsList.toArray(licsInFile);
			licsList.clear();
			return licsInFile;	
		}
		@Deprecated
		/**
		 * 
		 * @param subDocs
		 * @return
		 * @throws InvalidSPDXAnalysisException
		 */
		public String translateSubDelcaredLicsIntoComments(SpdxDocument[] subDocs) throws InvalidSPDXAnalysisException{
			SpdxLicenseMapper mapper = new SpdxLicenseMapper();
				if(!mapper.isNonStdLicIdMapEmpty()){
					StringBuilder buffer = new StringBuilder(packageInfoResult.getLicenseComments() 
							+ ". This package merged several packages and the sub-package contain the following licenses:");

					for(int k = 0; k < subDocs.length; k++){
						if(mapper.docInNonStdLicIdMap(subDocs[k])){
						 List<SpdxPackage> tempList = subDocs[k].getDocumentContainer().findAllPackages();
						  for(int h = 0; h < tempList.size(); h++){
							AnyLicenseInfo license = tempList.get(h).getLicenseDeclared();
							AnyLicenseInfo result = mapper.mapLicenseInfo(subDocs[k], license); 
							buffer.append(tempList.get(h).getPackageFileName());
							buffer.append(" (" + result.toString() + ") ");
						  }
						}else{
							List<SpdxPackage> tempList = subDocs[k].getDocumentContainer().findAllPackages();
							for(int q = 0; q < tempList.size(); q++){
							buffer.append(tempList.get(q).getPackageFileName());
							buffer.append(" (" + tempList.get(q).getLicenseDeclared().toString() + ") ");
						  }
						}
					}			
					return buffer.toString();
			}else{
				return packageInfoResult.getLicenseComments();
			}
		}
}
