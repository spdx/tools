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

import org.spdx.compare.SpdxCompareException;
import org.spdx.merge.SpdxFileInfoMerger;
import org.spdx.merge.SpdxLicenseInfoMerger;
import org.spdx.merge.SpdxPackageInfoMerger;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
			SPDXDocument[] mergeDocs = new SPDXDocument[args.length-1];

			String[] docNames = new String[args.length-1];
			@SuppressWarnings("unchecked")
			ArrayList<String>[] verficationError = new ArrayList[args.length-1];
			
			for(int i = 0; i < args.length-1; i++){
				try{
					mergeDocs[i] = CompareSpdxDocs.openRdfOrTagDoc(args[i]);
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
			SPDXDocument master = mergeDocs[0];
			SPDXDocument[] subDocs = new SPDXDocument[mergeDocs.length-1];
			for(int k = 0; k < subDocs.length; k++){
				subDocs[k] = mergeDocs[k+1];
			}
			
			//file output stream
			FileOutputStream out;
			try{
				out = new FileOutputStream(spdxRdfFile);
			}catch(FileNotFoundException e){
				System.out.println("Could not write to the new SPDX RDF file "+args[args.length-1]);
				System.out.println("due to error "+e.getMessage());
				usage();
				return;
			}
			Model model = ModelFactory.createDefaultModel();			
			SPDXDocument outputDoc = null;
			try {
				outputDoc = SPDXDocumentFactory.createSpdxDocument(model);
				
			} catch (InvalidSPDXAnalysisException e1) {
				System.out.print("Error creating merged SPDX Document: "+e1.getMessage());
				try {
					out.close();
					} catch (IOException e) {
						System.out.println("Warning - unable to close output file on error: "+e.getMessage());
					}
					return;
			}
			String outputDocURI = master.getDocumentNamespace()+"-merged";
			try {
				outputDoc.createSpdxAnalysis(outputDocURI);
			} catch (InvalidSPDXAnalysisException e1) {
				System.out.print("Error creating SPDX Analysis: "+e1.getMessage());
			}
			
			SPDXNonStandardLicense[] licInfoResult = null;
			try{
				SpdxLicenseInfoMerger NonStandardLicMerger = new SpdxLicenseInfoMerger(master);
				//merge non-standard license information
				licInfoResult = NonStandardLicMerger.mergeNonStdLic(subDocs);
			}catch(InvalidSPDXAnalysisException e){
				System.out.println("Error merging documents' SPDX Non-standard License Information: "+e.getMessage());
				System.exit(ERROR_STATUS);
			}
				
			SPDXFile[] fileInfoResult = null;
			try{
				SpdxFileInfoMerger fileInfoMerger = new SpdxFileInfoMerger(master);
				//merge file information 
				fileInfoResult = fileInfoMerger.mergeFileInfo(subDocs);
			}catch(InvalidSPDXAnalysisException e){
				System.out.println("Error merging SPDX files' Information: "+e.getMessage());
				System.exit(ERROR_STATUS);
			}
			
			SPDXPackage packageInfoResult = null;
			try {
				packageInfoResult = master.getSpdxPackage().clone(outputDoc, outputDocURI+"#package");
			} catch (InvalidSPDXAnalysisException e1) {
				System.out.println("Error cloning master's package information: "+e1.getMessage());
			}
			try{
				SpdxPackageInfoMerger packInfoMerger = new SpdxPackageInfoMerger(master, mergeDocs);
				try {
					packInfoMerger.mergePackageInfo(packageInfoResult, subDocs, fileInfoResult);
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
				outputDoc.setSpdxVersion(master.getSpdxVersion());
				//set document creator information
				outputDoc.setCreationInfo(master.getCreatorInfo());
				//set document comment information 
				outputDoc.setDocumentComment(master.getDocumentComment());
				//set document data license information
				outputDoc.setDataLicense(master.getDataLicense());
				//set extracted license information
				outputDoc.setExtractedLicenseInfos(licInfoResult);
				//set package's declared license information
				outputDoc.getSpdxPackage().setDeclaredLicense(packageInfoResult.getDeclaredLicense());
				//set package's file information
				outputDoc.getSpdxPackage().setFiles(fileInfoResult);
				//set package's license comments information 
				outputDoc.getSpdxPackage().setLicenseComment(packageInfoResult.getLicenseComment());
				//set package's verification code
				outputDoc.getSpdxPackage().setVerificationCode(packageInfoResult.getVerificationCode());
			}catch(InvalidSPDXAnalysisException e){
				System.out.println("Error to set merged information into output document "+e.getMessage());
				System.exit(ERROR_STATUS);
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
