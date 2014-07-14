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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.spdx.compare.SpdxCompareException;
import org.spdx.merge.SpdxFileInfoMerger;
import org.spdx.merge.SpdxMergeException;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXFile;

/**
 * Commend line application to merge multiple SPDX documents into one single documents
 * Usage: MergeSpdxDocs doc1 doc2 doc3 ... [output]
 * where doc1 doc2 doc3 are SPDX documents either RDF/XML or tag/value format
 * 
 * @author Gang Ling
 *
 */
public class MergeSpdxDocs {

	static final int MIN_ARGS = 2;
	static final int ERROR_STATUS =1;	
	
	/**
	 * @param args (input SPDX documents; the last item in the args will be the output file name)
	 * @throws InvalidSPDXAnalysisException 
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws InvalidSPDXAnalysisException, SpdxMergeException {
			if (args.length < MIN_ARGS){
					System.out.println("Insufficient arguments");
					usage();
					System.exit(ERROR_STATUS);
			}
			//check the output file name to avoid the miss input value
			File outFile = new File(args[args.length-1]);
			if(outFile.exists()){
				System.out.println("Output file "+args[args.length-1]+" already exist");
				System.exit(ERROR_STATUS);
			}
			
			//store inputed SPDX documents in the array "mergeDocs" for later parsing 
			SPDXDocument[] mergeDocs = new SPDXDocument[args.length-1];

			String[] docNames = new String[args.length-1];
			@SuppressWarnings("unchecked")
			ArrayList<String>[] verficationError = new ArrayList[args.length-1];
			
			//call methods from CompareSpdxDocs class
			CompareSpdxDocs compareUtility = new CompareSpdxDocs ();
			for(int i = 0; i < args.length-1; i++){
				try{
					mergeDocs[i] = compareUtility.openRdfOrTagDoc(args[i]);
					docNames[i] = compareUtility.convertDocName(args[i]);
					verficationError[i] = mergeDocs[i].verify();
					if(verficationError[i] != null && verficationError[i].size() > 0){
						System.out.println("Warning: "+docNames[i]+" contains verfication errors.");
					}			
				}catch(SpdxCompareException e){
					System.out.println("Error opening SPDX document "+args[i]+" : "+e.getMessage());
					System.exit(ERROR_STATUS);
				}
			}
			
			
	}
			

    
    private static void usage(){
    		System.out.println("Usage: doc1 doc2 doc3...[output]");
    		System.out.println("where doc1, doc2, doc3... is a serial of vaild SPDX documents in RDF/XML format");
    		System.out.println("[output] is a vaild name for output document");
    }
}
