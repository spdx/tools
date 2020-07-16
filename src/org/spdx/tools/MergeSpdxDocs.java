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
package org.spdx.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.spdx.compare.SpdxCompareException;
import org.spdx.merge.SpdxFileInfoMerger;
import org.spdx.merge.SpdxLicenseInfoMerger;
import org.spdx.merge.SpdxLicenseMapper;
import org.spdx.merge.SpdxPackageInfoMerger;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

import org.apache.jena.rdf.model.Model;


/**
 * Commend line application to merge multiple SPDX documents into one single documents
 * Usage: MergeSpdxDocs doc1 doc2 doc3 ... [output]
 * where doc1 doc2 doc3 are SPDX documents either RDF/XML or tag/value format
 * And doc1 will be used as master document. The output SPDX document is built based on the master document.
 *
 * @author Gang Ling
 *
 */
public class MergeSpdxDocs {

	static final int MIN_ARGS = 2;
	static final int ERROR_STATUS =1;

	/**
	 *
	 * @param args (input SPDX documents; the last item in the args will be the output file name)
	 */
	public static void main(String[] args){
			if (args.length < MIN_ARGS){
					System.out.println("Insufficient arguments");
					usage();
					System.exit(ERROR_STATUS);
			}
			//check the output file name to avoid the miss input value
			File spdxRdfFile = new File(args[args.length-1]);
			if(spdxRdfFile.exists()){
				System.out.println("Output file "+args[args.length-1]+" already exist");
				System.exit(ERROR_STATUS);
			}

			//store inputed SPDX documents in the array "mergeDocs" for later parsing
			SpdxDocument[] mergeDocs = new SpdxDocument[args.length-1];

			String[] docNames = new String[args.length-1];
			@SuppressWarnings("unchecked")
			List<String>[] verficationError = new List[args.length-1];

			for(int i = 0; i < args.length-1; i++){
				try{
					List<String> warnings = new ArrayList<String>();
					mergeDocs[i] = CompareSpdxDocs.openRdfOrTagDoc(args[i], warnings);
					if (!warnings.isEmpty()) {
						System.out.println("Verification errors were found in "+args[i].trim()+":");
						if (!warnings.isEmpty()) {
							System.out.println("The following warnings and or verification errors were found:");
							for (String warning:warnings) {
								System.out.println("\t"+warning);
							}
						}
					}
					docNames[i] = CompareSpdxDocs.convertDocName(args[i]);
					verficationError[i] = mergeDocs[i].verify();
					if(verficationError[i] != null && verficationError[i].size() > 0){
						System.out.println("Warning: "+docNames[i]+" contains verfication errors.");
					}
				}catch(SpdxCompareException e){
					System.out.println("Error opening SPDX document "+args[i]+" : "+e.getMessage());
					System.exit(ERROR_STATUS);
				}
			}

			//separate master document and sub-documents
			SpdxDocument master = mergeDocs[0];
			SpdxDocument[] subDocs = new SpdxDocument[mergeDocs.length-1];
			for(int k = 0; k < subDocs.length; k++){
				subDocs[k] = mergeDocs[k+1];
			}

			FileOutputStream out;
			try{
				out = new FileOutputStream(spdxRdfFile);
			}catch(FileNotFoundException e){
				System.out.println("Could not write to the new SPDX RDF file "+args[args.length-1]);
				System.out.println("due to error "+e.getMessage());
				usage();
				return;
			}

			//create outputDoc, then clone master into outputDoc
			SpdxDocument outputDoc = null;
			String masterDocUri = master.getDocumentContainer().getDocumentNamespace();
			Model model = null;
			if(masterDocUri.endsWith("#")){
				masterDocUri = masterDocUri.substring(0,masterDocUri.length()-1);
			}
			String outputDocURI = masterDocUri + "-merged";
			try {
				SpdxDocumentContainer container = new SpdxDocumentContainer(outputDocURI);
				outputDoc = container.getSpdxDocument();
				model = container.getModel();
			} catch (InvalidSPDXAnalysisException e1) {
				System.out.print("Error creating SPDX Analysis: "+e1.getMessage());
				//System.exit(1); - Causes unit tests to block
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}

			//obtain package list from master document
			List <SpdxPackage> packageInfoResult = null;
			try {
				 packageInfoResult = master.getDocumentContainer().findAllPackages();
			} catch (InvalidSPDXAnalysisException e1) {
				System.out.println("Error obtaining master's packages: "+e1.getMessage());
			}

			ExtractedLicenseInfo[] licInfoResult = null;
			SpdxLicenseMapper licenseMapper = new SpdxLicenseMapper();

			try{
				SpdxLicenseInfoMerger nonStandardLicMerger = new SpdxLicenseInfoMerger(outputDoc, licenseMapper);
				//merge non-standard license information
				licInfoResult = nonStandardLicMerger.mergeNonStdLic(subDocs);
			}catch(InvalidSPDXAnalysisException e){
				System.out.println("Error merging documents' SPDX Non-standard License Information: "+e.getMessage());
				System.exit(ERROR_STATUS);
			}

			SpdxFile[] fileInfoResult = null;
			try{
				SpdxFileInfoMerger fileInfoMerger = new SpdxFileInfoMerger(master, licenseMapper);
				//merge file information
				fileInfoResult = fileInfoMerger.mergeFileInfo(subDocs);
			}catch(InvalidSPDXAnalysisException e){
				System.out.println("Error merging SPDX files' Information: "+e.getMessage());
				System.exit(ERROR_STATUS);
			}

			try{
				SpdxPackageInfoMerger packInfoMerger = new SpdxPackageInfoMerger(packageInfoResult, subDocs, licenseMapper);
				try {
					packInfoMerger.mergePackagesInfo(fileInfoResult);
				} catch (NoSuchAlgorithmException e) {
					System.out.println("Error merging packages' information: "+e.getMessage());
				} catch (InvalidLicenseStringException e) {
					System.out.println("Error on package's license string "+e.getMessage());
				}
			}catch(InvalidSPDXAnalysisException e){
				System.out.println("Error merging SPDX Non-standard License Information: "+e.getMessage());
				System.exit(ERROR_STATUS);
			}

			try{
				//set document review information as empty array
				SPDXReview[] reviewInfoResult = new SPDXReview[0];
				outputDoc.setReviewers(reviewInfoResult);
				//set document SPDX version
				outputDoc.setSpecVersion(master.getSpecVersion());
				//set document creator information
				outputDoc.setCreationInfo(master.getCreationInfo());
				//set document comment information
				outputDoc.setComment(master.getComment());
				//set document data license information
				outputDoc.setDataLicense(master.getDataLicense());
				//set extracted license information
				outputDoc.setExtractedLicenseInfos(licInfoResult);
				//set package's declared license information
//				outputDoc.getSpdxPackage().setLicenseDeclared(packageInfoResult.getLicenseDeclared());
				//set package's file information
//				outputDoc.getSpdxPackage().setFiles(fileInfoResult);
				//set package's license comments information
//				outputDoc.getSpdxPackage().setLicenseComments(packageInfoResult.getLicenseComments());
				//set package's verification code
//				outputDoc.getSpdxPackage().setPackageVerificationCode(packageInfoResult.getPackageVerificationCode());
			}catch(InvalidSPDXAnalysisException e){
				System.out.println("Error to set merged information into output document "+e.getMessage());
				//System.exit(ERROR_STATUS); - Causes unit tests to stop
				try {
					out.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return;
			}

			try{
				model.write(out, "RDF/XML-ABBREV");
			}catch(Exception e){
				System.out.println("Error writing to the output file "+e.getMessage());
			}
			finally{if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					System.out.println("Error closing RDF file: "+e.getMessage());
				}
			 }
			}

	}

    /**
     *
     */
    private static void usage(){
    		System.out.println("Usage: doc1 doc2 doc3...[output]");
    		System.out.println("where doc1, doc2, doc3... is a serial of vaild SPDX documents in RDF/XML format");
    		System.out.println("[output] is a vaild name for output document");
    		System.out.println("Note: the doc1 will be used as master document to build the finail output document ");
    }
}
