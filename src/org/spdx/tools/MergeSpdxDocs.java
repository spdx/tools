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
package org.spdx.tools;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.spdx.merge.SpdxFileInfoMerger;
import org.spdx.merge.SpdxMergeException;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXFile;

/**
 * @author gling
 *
 */
public class MergeSpdxDocs {

	static final int MIN_ARGS = 2;
	static final int ERROR_STATUS =1;
	private static String spdxDocName;
	private static ArrayList<SPDXFile> alFiles;
	
	
	/**
	 * @param args
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static void main(String[] args) throws InvalidSPDXAnalysisException, SpdxMergeException {
			if (args.length < MIN_ARGS){
					System.out.println("Insufficient arguments");
					usage();
					System.exit(ERROR_STATUS);
			}
			
			HashMap <String, SPDXDocument> spdxDocMap = new HashMap<String, SPDXDocument>();
			SPDXDocument spdxDoc = null;
	
			try{
				for(int i=0; i < args.length; i++){
					try{
						spdxDoc = openRdfDoc(args[i].trim());
						spdxDocName = convertDocName(args[i]);
				       }catch (SpdxMergeException e){
				    	   System.out.println("Error opening "+ spdxDocName+":"+e.getMessage());
				    	   usage();
				    	   System.exit(ERROR_STATUS);
				}
			   spdxDocMap.put(spdxDocName,spdxDoc);
			}
			SpdxFileInfoMerger MergeFileInfo = new SpdxFileInfoMerger();
				alFiles = MergeFileInfo.FileInfoMerge(spdxDocMap);
		}
		finally{
				System.out.println("Program executed");
		}
	}
	
    protected static SPDXDocument openRdfDoc(String spdxDocFileName)throws SpdxMergeException{
			File spdxDocFile = new File(spdxDocFileName);
			if (!spdxDocFile.exists()){
					throw(new SpdxMergeException("SPDX File "+spdxDocFileName+" does not exist."));		
			}
			if (!spdxDocFile.canRead()){
					throw(new SpdxMergeException("SPDX File "+spdxDocFileName+" can not be read."));
			}
			SPDXDocument retval = null;
			try{
					retval = SPDXDocumentFactory.creatSpdxDocument(spdxDocFileName);
			}catch (IOException e){				
			}catch (InvalidSPDXAnalysisException e){				
			}catch (Exception e){
            }
			if(retval == null){
				throw(new SpdxMergeException ("File "+spdxDocFileName+" is not a recognized RDF/XML format"));
			}
			return retval;
	}
    
	/**
	 * Method has taken credit from CompareSpdxDocs.java
	 * Original author: Gary O'Neall
	 * Converts a file path or URL to a shorter document name
	 * @param docPath
	 * @return
	 */
    protected static String convertDocName(String docPath){
    		if (docPath.contains(File.separator)){
    			File docFile = new File(docPath);
    			return docFile.getName();
    		}
    		else {
    				try{
    					URI uri = new URI(docPath);
    					String path = uri.getPath();
    					return path;
    				}catch(URISyntaxException e){
    					return docPath;
    				}
    		}
    }
    
    private static void usage(){
    		System.out.println("Usage: doc1 doc2 ...");
    		System.out.println("where doc1, doc2,... is a serial of vaild SPDX documents");
    		System.out.println("in RDF/XML format");
    }
}
