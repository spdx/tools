/**
 * Copyright (c) 2013 Source Auditor Inc.
 * Copyright (c) 2013 Black Duck Software Inc.
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.spdx.compare.SpdxCompareException;
import org.spdx.compare.SpdxComparer;
import org.spdx.compare.SpdxComparer.SPDXReviewDifference;
import org.spdx.compare.SpdxFileDifference;
import org.spdx.compare.SpdxLicenseDifference;
import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxPackageVerificationCode;

/**
 * Command line application to compare two SPDX documents
 * Usage: CompareSpdxDoc doc1 doc2 [output]
 * where doc1 and doc2 are two SPDX documents in either RDF/XML  or tag/value format
 * and [output] is an optional SPDX document
 * 
 * @author Gary O'Neall
 *
 */
public class CompareSpdxDocs {
	
	static final int MIN_ARGS = 2;
	static final int MAX_ARGS = 3;
	static final int ERROR_STATUS = 1;
	//TODO: Change implementation to use a properties file for the standard strings OR use Mustache

	/**
	 * @param args CompareFile1, CompareFile2, optional output file
	 */
	public static void main(String[] args) {
		if (args.length < MIN_ARGS) {
			System.out.println("Insufficient arguments");
			usage();
			System.exit(ERROR_STATUS);
		}
		if (args.length > MAX_ARGS) {
			System.out.println("Too many arguments specified");
			usage();
			System.exit(ERROR_STATUS);
		}
		SPDXDocument spdxDoc1 = null;
		try {
			spdxDoc1 = openRdfOrTagDoc(args[0].trim());
		} catch (SpdxCompareException e) {
			System.out.println("Error opening "+args[0].trim()+":"+e.getMessage());
			usage();
			System.exit(ERROR_STATUS);
		}
		SPDXDocument spdxDoc2 = null;
		try {
			spdxDoc2 = openRdfOrTagDoc(args[1].trim());
		} catch (SpdxCompareException e) {
			System.out.println("Error opening "+args[1].trim()+":"+e.getMessage());
			usage();
			System.exit(ERROR_STATUS);
		}

		PrintStream output = null;
		if (args.length > 2) {
			File outFile = new File(args[2].trim());
			if (outFile.exists()) {
				System.out.println("Output file "+args[2].trim()+" already exists.");
				System.exit(ERROR_STATUS);
			}
			try {
				if (!outFile.createNewFile()) {
					System.out.println("Can not create output file "+args[2].trim());
					System.exit(ERROR_STATUS);
				}
			} catch (IOException e) {
				System.out.println("Error creating output file "+args[2].trim());
				System.exit(ERROR_STATUS);
			}
			try {
				output = new PrintStream(outFile);
			} catch (FileNotFoundException e) {
				System.out.println("Error opening output file "+args[2].trim()+" for printing ("+e.getMessage());
				System.exit(ERROR_STATUS);
			}
		} else {
			output = System.out;
		}
		try {
			ArrayList<String> doc1VerificationErrors = spdxDoc1.verify();
			if (doc1VerificationErrors.size() > 0) {
				output.println("Warning - The SPDX document "+args[0].trim()+" contains the following verification errors:");
				printList(doc1VerificationErrors, output);
			}
			ArrayList<String> doc2VerificationErrors = spdxDoc2.verify();
			if (doc2VerificationErrors.size() > 0) {
				output.println("Warning - The SPDX document "+args[1].trim()+" contains the following verification errors:");
				printList(doc2VerificationErrors, output);
			}
			try {
				SpdxComparer comparer = new SpdxComparer();
				comparer.compare(spdxDoc1, spdxDoc2);
				// create some more readable document names for the compare results
				String docName1 = convertDocName(args[0]);
				String docName2 = convertDocName(args[1]);
				if (docName1.equals(docName2)) {
					docName1 = args[0];
					docName2 = args[1];
				}
				printCompareResults(comparer, docName1, docName2, output);
			} catch (SpdxCompareException e) {
				output.println("Error in comparing SPDX documents: "+e.getMessage());
				System.exit(ERROR_STATUS);
			} catch (InvalidSPDXAnalysisException e) {
				output.println("SPDX Analysis Error in comparing SPDX documents: "+e.getMessage());
				System.exit(ERROR_STATUS);
			}
		} finally {
			if (output != System.out) {
				output.close();
			}
		}
		System.exit(0);
	}
	
	/**
	 * Converts a file path or URL to a shorter document name
	 * @param docPath
	 * @return
	 */
	protected static String convertDocName(String docPath) {
		if (docPath.contains(File.separator)) {
			File docFile = new File(docPath);
			return docFile.getName();
		} else {
			try {
				URI uri = new URI(docPath);
				String path = uri.getPath();
				return path;
			} catch (URISyntaxException e) {
				return docPath;
			}
		}
	}

	/**
	 * Prints an array list of strings
	 * @param list
	 * @param output
	 */
	private static void printList(ArrayList<String> list,
			PrintStream output) {
		for(int i = 0;i < list.size(); i++) {
			output.println(list.get(i));
		}
	}

	/**
	 * @param comparer
	 * @param output
	 * @throws InvalidSPDXAnalysisException 
	 * @throws SpdxCompareException 
	 */
	private static void printCompareResults(SpdxComparer comparer, String doc1Name, 
			String doc2Name, PrintStream output) throws SpdxCompareException, InvalidSPDXAnalysisException {
		output.println("Comparing SPDX Documents: "+doc1Name+
				" and "+doc2Name);
		if (!comparer.isDifferenceFound()) {
			output.println("Both SPDX documents match - no differences found.");
			return;
		}
		// spdx version
		if (!comparer.isSpdxVersionEqual()) {
			output.println("SPDX versions differ.");
			output.println("\t"+doc1Name+":"+comparer.getSpdxDoc(0).getSpdxVersion());
			output.println("\t"+doc2Name+":"+comparer.getSpdxDoc(1).getSpdxVersion());
		}
		// data license
		if (!comparer.isDataLicenseEqual()) {
			output.println("SPDX data license differ.");
			output.println("\t"+doc1Name+":"+comparer.getSpdxDoc(0).getDataLicense().toString());
			output.println("\t"+doc2Name+":"+comparer.getSpdxDoc(1).getDataLicense().toString());
		}
		// document comment
		if (!comparer.isDocumentCommentsEqual()) {
			output.println("SPDX document comments differ.");
			if (comparer.getSpdxDoc(0).getDocumentComment() == null) {
				output.println("\t"+doc1Name+": [No comment]");
			} else {
				output.println("\t"+doc1Name+":"+comparer.getSpdxDoc(0).getDocumentComment());
			}
			if (comparer.getSpdxDoc(1).getDocumentComment() == null) {
				output.println("\t"+doc2Name+": [No comment]");
			} else {
				output.println("\t"+doc2Name+":"+comparer.getSpdxDoc(1).getDocumentComment());
			}
		}
		// creator information
		printCreatorCompareResults(comparer, doc1Name, doc2Name, output);
		// package
		printPackageCompareResults(comparer, doc1Name, doc2Name, output);
		// file
		printFileCompareResults(comparer, doc1Name, doc2Name, output);
		// other license information
		printExtractedLicenseCompareResults(comparer, doc1Name, doc2Name, output);
		// reviewer
		printReviewerCompareResults(comparer, doc1Name, doc2Name, output);		
	}

	/**
	 * @param comparer
	 * @param doc1Name
	 * @param doc2Name
	 * @param output
	 * @throws SpdxCompareException 
	 */
	private static void printFileCompareResults(SpdxComparer comparer,
			String doc1Name, String doc2Name, PrintStream output) throws SpdxCompareException {
		if (!comparer.isfilesEquals()) {
			SPDXFile[] inDoc1notInDoc2 = comparer.getUniqueFiles(0, 1);
			if (inDoc1notInDoc2 != null && inDoc1notInDoc2.length > 0) {
				output.println("The following license(s) are in "+doc1Name+
						" but not in "+doc2Name);
				for (int i = 0; i < inDoc1notInDoc2.length; i++) {
					printSpdxFile(inDoc1notInDoc2[i], output);
				}
			}
			SPDXFile[] inDoc2notInDoc1 = comparer.getUniqueFiles(1, 0);
			if (inDoc2notInDoc1 != null && inDoc2notInDoc1.length > 0) {
				output.println("The following license(s) are in "+doc2Name+
						" but not in "+doc1Name);
				for (int i = 0; i < inDoc2notInDoc1.length; i++) {
					printSpdxFile(inDoc2notInDoc1[i], output);
				}
			}
			SpdxFileDifference[] fileDifferences = comparer.getFileDifferences(0, 1);
			if (fileDifferences != null && fileDifferences.length > 0) {
				for (int i = 0; i < fileDifferences.length; i++) {
					printFileDifference(fileDifferences[i], output, doc1Name, doc2Name);
				}
			}
		}
	}

	/**
	 * Displays the file difference information
	 * @param spdxFileDifference
	 * @param output
	 * @param doc1Name
	 * @param doc2Name
	 */
	private static void printFileDifference(
			SpdxFileDifference spdxFileDifference, PrintStream output,
			String doc1Name, String doc2Name) {
		if (!spdxFileDifference.isConcludedLicenseEquals()) {
			output.println("File concluded license differ for file "+spdxFileDifference.getFileName()+":");
			output.println("\t"+doc1Name+":"+spdxFileDifference.getConcludedLicenseA());
			output.println("\t"+doc2Name+":"+spdxFileDifference.getConcludedLicenseB());
		}
		if (!spdxFileDifference.isCopyrightsEqual()) {
			output.println("File copyrights differ for file "+spdxFileDifference.getFileName()+":");
			output.println("\t"+doc1Name+":"+spdxFileDifference.getCopyrightA());
			output.println("\t"+doc2Name+":"+spdxFileDifference.getCopyrightB());
		}
		if (!spdxFileDifference.isCommentsEqual()) {
			output.println("File comments differ for file "+spdxFileDifference.getFileName()+":");
			output.println("\t"+doc1Name+":"+spdxFileDifference.getCommentA());
			output.println("\t"+doc2Name+":"+spdxFileDifference.getCommentB());
		}
		if (!spdxFileDifference.isChecksumsEqual()) {
			output.println("File checksums differ for file "+spdxFileDifference.getFileName()+":");
			output.println("\t"+doc1Name+":"+spdxFileDifference.getSha1A());
			output.println("\t"+doc2Name+":"+spdxFileDifference.getSha1B());			
		}
		if (!spdxFileDifference.isTypeEqual()) {
			output.println("File types differ for file "+spdxFileDifference.getFileName()+":");
			output.println("\t"+doc1Name+":"+spdxFileDifference.getFileTypeA());
			output.println("\t"+doc2Name+":"+spdxFileDifference.getFileTypeB());
		}
		if (!spdxFileDifference.isContributorsEqual()) {
			output.println("File contributors differ for file "+spdxFileDifference.getFileName()+":");
			output.println("\t"+doc1Name+":"+spdxFileDifference.getContributorsAAsString());
			output.println("\t"+doc2Name+":"+spdxFileDifference.getContributorsBAsString());
		}		
		if (!spdxFileDifference.isFileDependenciesEqual()) {
			output.println("File contributors differ for file "+spdxFileDifference.getFileName()+":");
			output.println("\t"+doc1Name+"dependencies:"+spdxFileDifference.getFileDependenciesAAsString());
			output.println("\t"+doc2Name+":"+spdxFileDifference.getFileDependenciesBAsString());
		}	
		if (!spdxFileDifference.isSeenLicensesEqual()) {
			if (spdxFileDifference.getUniqueSeenLicensesA() != null && 
					spdxFileDifference.getUniqueSeenLicensesA().length > 0) {
				output.println("The following license information was only found in "+
						doc1Name+" for file "+spdxFileDifference.getFileName());
				for (int i = 0; i < spdxFileDifference.getUniqueSeenLicensesA().length; i++) {
					output.println("\t"+spdxFileDifference.getUniqueSeenLicensesA()[i].toString());
				}
			}
			if (spdxFileDifference.getUniqueSeenLicensesB() != null && 
					spdxFileDifference.getUniqueSeenLicensesB().length > 0) {
				output.println("The following license information was only found in "+
						doc2Name+" for file "+spdxFileDifference.getFileName());
				for (int i = 0; i < spdxFileDifference.getUniqueSeenLicensesB().length; i++) {
					output.println("\t"+spdxFileDifference.getUniqueSeenLicensesB()[i].toString());
				}
			}
		}
		if (!spdxFileDifference.isArtifactOfsEquals()) {
			if (spdxFileDifference.getUniqueArtifactOfA() != null && 
					spdxFileDifference.getUniqueArtifactOfA().length > 0) {
				output.println("The following artifactOf information was only found in "+
						doc1Name+" for file "+spdxFileDifference.getFileName());
				for (int i = 0; i < spdxFileDifference.getUniqueArtifactOfA().length; i++) {
					printDoapProject(spdxFileDifference.getUniqueArtifactOfA()[i], output);
				}
			}
			if (spdxFileDifference.getUniqueArtifactOfB() != null && 
					spdxFileDifference.getUniqueArtifactOfB().length > 0) {
				output.println("The following artifactOf information was only found in "+
						doc2Name+" for file "+spdxFileDifference.getFileName());
				for (int i = 0; i < spdxFileDifference.getUniqueArtifactOfB().length; i++) {
					printDoapProject(spdxFileDifference.getUniqueArtifactOfB()[i], output);
				}
			}
		}
	}

	/**
	 * Prints DOAP Project information prceeded by tabs
	 * @param doapProject
	 * @param output
	 */
	private static void printDoapProject(DOAPProject doapProject,
			PrintStream output) {
		if (doapProject.getName() != null && !doapProject.getName().isEmpty()) {
			output.println("\tProject Name: "+doapProject.getName());
		}
		if (doapProject.getHomePage() != null && !doapProject.getHomePage().isEmpty()) {
			output.println("\tProject Home Page: "+doapProject.getName());
		}
		if (doapProject.getProjectUri() != null && !doapProject.getProjectUri().isEmpty()) {
			output.println("\tProject URI: "+doapProject.getProjectUri());
		}
	}

	/**
	 * Print the SPDX file information with preceeding tabs
	 * @param spdxFile
	 * @param output
	 */
	private static void printSpdxFile(SPDXFile spdxFile, PrintStream output) {
		output.println("\t"+spdxFile.getName()+", checksum:"+spdxFile.getSha1());
	}

	/**
	 * @param comparer
	 * @param output
	 */
	private static void printReviewerCompareResults(SpdxComparer comparer,
			String doc1Name, String doc2Name, PrintStream output) throws SpdxCompareException, InvalidSPDXAnalysisException {
		if (!comparer.isReviewersEqual()) {
			SPDXReview[] inDoc1notInDoc2 = comparer.getUniqueReviewers(0, 1);
			if (inDoc1notInDoc2.length > 0) {
				output.println("The following Reviewer(s) are in "+doc1Name+
						" but not in "+doc2Name);
				for (int i = 0; i < inDoc1notInDoc2.length; i++) {
					printReviewer(inDoc1notInDoc2[i], output);
				}
			}
			SPDXReview[] inDoc2notInDoc1 = comparer.getUniqueReviewers(1, 0);
			if (inDoc2notInDoc1.length > 0) {
				output.println("The following Reviewer(s) are in "+doc2Name+
						" but not in "+doc1Name);
				for (int i = 0; i < inDoc2notInDoc1.length; i++) {
					printReviewer(inDoc2notInDoc1[i], output);
				}
			}  
			SPDXReviewDifference[] differentReviewerInfo = comparer.getReviewerDifferences(0, 1); 
			for (int i = 0; i < differentReviewerInfo.length; i++) {
				// Review date
				if (!differentReviewerInfo[i].isDateEqual()) {
					output.println("Date is different for the SPDX Review by "+
							differentReviewerInfo[i].getReviewer()+":");
					output.println("\t"+doc1Name+":"+differentReviewerInfo[i].getDate(0));
					output.println("\t"+doc2Name+":"+differentReviewerInfo[i].getDate(1));
				}
				// Comment
				if (!differentReviewerInfo[i].isCommentEqual()) {
					output.println("Comment is different for the SPDX Review by "+
							differentReviewerInfo[i].getReviewer()+":");
					output.println("\t"+doc1Name+":"+differentReviewerInfo[i].getComment(0));
					output.println("\t"+doc2Name+":"+differentReviewerInfo[i].getComment(1));
				}
			}
		}
	}

	/**
	 * Prints an SPDX review to an output stream with preceeding tabs
	 * @param spdxReview
	 * @param output
	 */
	private static void printReviewer(SPDXReview spdxReview, PrintStream output) {
		output.println("\tReviewer:\t"+spdxReview.getReviewer());
		output.println("\tReview Date:\t"+spdxReview.getReviewDate());
		if (!spdxReview.getComment().trim().isEmpty()) {
			output.println("\tReview Comment:\t"+spdxReview.getComment());
		}
	}

	/**
	 * @param comparer
	 * @param output
	 * @throws SpdxCompareException 
	 */
	private static void printExtractedLicenseCompareResults(
			SpdxComparer comparer, String doc1Name, String doc2Name, PrintStream output) throws SpdxCompareException {
		if (!comparer.isExtractedLicensingInfosEqual()) {
			SPDXNonStandardLicense[] inDoc1notInDoc2 = comparer.getUniqueExtractedLicenses(0, 1);
			if (inDoc1notInDoc2 != null && inDoc1notInDoc2.length > 0) {
				output.println("The following extracted licensing infos were only found in "+doc1Name);
				for (int i = 0; i < inDoc1notInDoc2.length; i++) {
					printSpdxLicenseInfo(inDoc1notInDoc2[i], output);
				}
			}
			SPDXNonStandardLicense[] inDoc2notInDoc1 = comparer.getUniqueExtractedLicenses(1, 0);
			if (inDoc2notInDoc1 != null && inDoc2notInDoc1.length > 0) {
				output.println("The following extracted licensing infos were only found in "+doc2Name);
				for (int i = 0; i < inDoc2notInDoc1.length; i++) {
					printSpdxLicenseInfo(inDoc2notInDoc1[i], output);
				}
			}
			SpdxLicenseDifference[] differentLicenses = comparer.getExtractedLicenseDifferences(0, 1);
			for (int i = 0; i < differentLicenses.length; i++) {
				if (!differentLicenses[i].isCommentsEqual()) {
					output.println("The comments differ for the extracted license:");
					
					if (differentLicenses[i].getCommentA() == null || differentLicenses[i].getCommentA().length() == 0) {
						output.println("\tId "+differentLicenses[i].getIdA()+" in "+doc1Name+": [no comment]");
					} else {
						output.print("\tId "+differentLicenses[i].getIdA()+" in "+doc1Name+": ");
						output.println("\""+differentLicenses[i].getCommentA()+"\"");
					}
					
					if (differentLicenses[i].getCommentB() == null || differentLicenses[i].getCommentB().length() == 0) {
						output.println("\tId "+differentLicenses[i].getIdB()+" in "+doc2Name+": [no comment]");
					} else {
						output.print("\tId "+differentLicenses[i].getIdB()+" in "+doc2Name+": ");
						output.println("\""+differentLicenses[i].getCommentB()+"\"");
					}
				}
				if (!differentLicenses[i].isLicenseNamesEqual()) {
					output.println("The license names differ for the extracted license:");
					if (differentLicenses[i].getLicenseNameA() == null || differentLicenses[i].getLicenseNameA().length() == 0) {
						output.println("\tId "+differentLicenses[i].getIdA()+" in "+doc1Name+": [no license name]");
					} else {
						output.println("\tId "+differentLicenses[i].getIdA()+" in "+doc1Name+" \""+
								differentLicenses[i].getLicenseNameA()+"\"");
					}
					if (differentLicenses[i].getLicenseNameB() == null || differentLicenses[i].getLicenseNameB().length() == 0) {
						output.println("\tId "+differentLicenses[i].getIdB()+" in "+doc2Name+": [no license name]");
					} else {
						output.println("\tId "+differentLicenses[i].getIdB()+" in "+doc2Name+" \""+
								differentLicenses[i].getLicenseNameB()+"\"");
					}
				}
				if (!differentLicenses[i].isSourceUrlsEqual()) {
					output.println("The source URL's differ for the extracted license:");
					String[] sourceUrlsA = differentLicenses[i].getSourceUrlsA();
					if (sourceUrlsA == null || sourceUrlsA.length == 0) {
						output.println("\tId "+differentLicenses[i].getIdA()+" in "+doc1Name+": [no source URL]");
					} else {
						output.println("\tId "+differentLicenses[i].getIdA()+" in "+doc1Name+" source URLs:");
						for(int j = 0; j < sourceUrlsA.length; j++) {
							output.println("\t\t"+sourceUrlsA[j]);
						}
					}
					String[] sourceUrlsB = differentLicenses[i].getSourceUrlsB();
					if (sourceUrlsB == null || sourceUrlsB.length == 0) {
						output.println("\tId "+differentLicenses[i].getIdB()+" in "+doc2Name+": [no source URL]");
					} else {
						output.println("\tId "+differentLicenses[i].getIdB()+" in "+doc2Name+" source URLs:");
						for(int j = 0; j < sourceUrlsB.length; j++) {
							output.println("\t\t"+sourceUrlsB[j]);
						}
					}
				}
			}
		}
	}

	/**
	 * Prints the nonstandard license information to the output preceeded by tabs
	 * @param nonStdLicense
	 */
	private static void printSpdxLicenseInfo(
			SPDXNonStandardLicense nonStdLicense, PrintStream output) {
		output.println("\tID: "+nonStdLicense.getId());
		if (nonStdLicense.getComment() != null && !nonStdLicense.getComment().trim().isEmpty()) {
			output.println("\tComment: "+nonStdLicense.getComment());
		}
		if (nonStdLicense.getLicenseName() != null && !nonStdLicense.getLicenseName().trim().isEmpty()) {
			output.println("\tLicense Name: "+nonStdLicense.getLicenseName());
		}
		if (nonStdLicense.getSourceUrls() != null && nonStdLicense.getSourceUrls().length > 0) {
			output.println("\tSource URLs:");
			for (int i = 0; i < nonStdLicense.getSourceUrls().length; i++) {
				output.println("\t\t"+nonStdLicense.getSourceUrls()[i]);
			}
		}
		output.println("\tText: \""+nonStdLicense.getText()+"\"");
	}

	/**
	 * @param comparer
	 * @param output
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static void printPackageCompareResults(SpdxComparer comparer,
			String doc1Name, String doc2Name,
			PrintStream output) throws SpdxCompareException, InvalidSPDXAnalysisException {
		if (!comparer.isPackageEqual()) {
			// package name
			if (!comparer.isPackageNamesEqual()) {
				output.println("Package names differ.");
				output.print("\t"+doc1Name+": ");
				if (comparer.getSpdxDoc(0).getSpdxPackage().getDeclaredName() != null) {
					output.println(comparer.getSpdxDoc(0).getSpdxPackage().getDeclaredName());
				} else {
					output.println("[none]");
				}
				output.print("\t"+doc2Name+": ");
				if (comparer.getSpdxDoc(1).getSpdxPackage().getDeclaredName() != null) {
					output.println(comparer.getSpdxDoc(1).getSpdxPackage().getDeclaredName());
				} else {
					output.println("[none]");
				}
			}
			// package version
			if (!comparer.isPackageVersionsEqual()) {
				output.println("Package versions differ.");
				output.print("\t"+doc1Name+": ");
				if (comparer.getSpdxDoc(0).getSpdxPackage().getVersionInfo() != null) {
					output.println(comparer.getSpdxDoc(0).getSpdxPackage().getVersionInfo());
				} else {
					output.println("[none]");
				}
				output.print("\t"+doc2Name+": ");
				if (comparer.getSpdxDoc(1).getSpdxPackage().getVersionInfo() != null) {
					output.println(comparer.getSpdxDoc(1).getSpdxPackage().getVersionInfo());
				} else {
					output.println("[none]");
				}
			}
			// package file name
			if (!comparer.isPackageFileNamesEqual()) {
				output.println("Package file names differ.");
				output.println("\t"+doc1Name+": "+comparer.getSpdxDoc(0).getSpdxPackage().getFileName());
				output.println("\t"+doc2Name+": "+comparer.getSpdxDoc(1).getSpdxPackage().getFileName());
			}
			// package supplier
			if (!comparer.isPackageSuppliersEqual()) {
				output.println("Package supplier information differ.");
				String s1 = comparer.getSpdxDoc(0).getSpdxPackage().getSupplier();
				if (s1 == null || s1.trim().isEmpty()) {
					output.println("\t"+doc1Name+": [no supplier information]");
				} else {
					output.println("\t"+doc1Name+": "+s1);
				}
				String s2 = comparer.getSpdxDoc(1).getSpdxPackage().getSupplier();
				if (s2 == null || s2.trim().isEmpty()) {
					output.println("\t"+doc2Name+": [no supplier information]");
				} else {
					output.println("\t"+doc2Name+": "+s2);
				}
			}
			// package originator
			if (!comparer.isPackageOriginatorsEqual()) {
				output.println("Package originator information differ.");
				String s1 = comparer.getSpdxDoc(0).getSpdxPackage().getOriginator();
				if (s1 == null || s1.trim().isEmpty()) {
					output.println("\t"+doc1Name+": [no originator information]");
				} else {
					output.println("\t"+doc1Name+": "+s1);
				}
				String s2 = comparer.getSpdxDoc(1).getSpdxPackage().getOriginator();
				if (s2 == null || s2.trim().isEmpty()) {
					output.println("\t"+doc2Name+": [no originator information]");
				} else {
					output.println("\t"+doc2Name+": "+s2);
				}
			}
			// package download location
			if (!comparer.isPackageDownloadLocationsEqual()) {
				output.println("Package download locations differ.");
				output.println("\t"+doc1Name+": "+comparer.getSpdxDoc(0).getSpdxPackage().getDownloadUrl());
				output.println("\t"+doc2Name+": "+comparer.getSpdxDoc(1).getSpdxPackage().getDownloadUrl());
			}
			// package home page
			if (!comparer.ispackageHomePagesEqual()) {
				output.println("Package home page differ.");
				if (comparer.getSpdxDoc(0).getSpdxPackage().getHomePage() == null) {
					output.println("\t"+doc1Name+": [No home page information]");
				} else {
					output.println("\t"+doc1Name+": "+comparer.getSpdxDoc(0).getSpdxPackage().getHomePage());
				}
				if (comparer.getSpdxDoc(1).getSpdxPackage().getHomePage() == null) {
					output.println("\t"+doc2Name+": [No home page information]");
				} else {
					output.println("\t"+doc2Name+": "+comparer.getSpdxDoc(1).getSpdxPackage().getHomePage());
				}
			}
			// package verification code
			if (!comparer.isPackageVerificationCodesEqual()) {
				output.println("Package verification codees differ.");
				printVerificationCode(comparer.getSpdxDoc(0).getSpdxPackage().getVerificationCode(), doc1Name, output);
				printVerificationCode(comparer.getSpdxDoc(1).getSpdxPackage().getVerificationCode(), doc2Name, output);
			}
			// package checksum
			if (!comparer.isPackageChecksumsEqual()) {
				output.println("Package checksums differ.");
				output.println("\t"+doc1Name+": "+comparer.getSpdxDoc(0).getSpdxPackage().getSha1());
				output.println("\t"+doc2Name+": "+comparer.getSpdxDoc(1).getSpdxPackage().getSha1());
			}
			// source information
			if (!comparer.isSourceInformationEqual()) {
				output.println("Package source information differ.");
				String s1 = comparer.getSpdxDoc(0).getSpdxPackage().getSourceInfo();
				if (s1 == null || s1.trim().isEmpty()) {
					output.println("\t"+doc1Name+": [no source information]");
				} else {
					output.println("\t"+doc1Name+": "+s1);
				}
				String s2 = comparer.getSpdxDoc(1).getSpdxPackage().getSourceInfo();
				if (s2 == null || s2.trim().isEmpty()) {
					output.println("\t"+doc2Name+": [no source information]");
				} else {
					output.println("\t"+doc2Name+": "+s2);
				}
			}
			// declared license
			if (!comparer.isPackageDeclaredLicensesEqual()) {
				output.println("Declared licenses differ.");
				output.println("\t"+doc1Name+": "+comparer.getSpdxDoc(0).getSpdxPackage().getDeclaredLicense().toString());
				output.println("\t"+doc2Name+": "+comparer.getSpdxDoc(1).getSpdxPackage().getDeclaredLicense().toString());
			}
			// concluded license
			if (!comparer.isPackageConcludedLicensesEqual()) {
				output.println("Concluded licenses differ.");
				output.println("\t"+doc1Name+": "+comparer.getSpdxDoc(0).getSpdxPackage().getConcludedLicenses().toString());
				output.println("\t"+doc2Name+": "+comparer.getSpdxDoc(1).getSpdxPackage().getConcludedLicenses().toString());
			}
			// all license information from files
			if (!comparer.isPackageLicenseInfoFromFilesEqual()) {
				output.println("License information from files differ.");
				SPDXLicenseInfo[] fromFiles1 = comparer.getSpdxDoc(0).getSpdxPackage().getLicenseInfoFromFiles();
				StringBuilder sb = new StringBuilder();
				if (fromFiles1 != null && fromFiles1.length > 0) {
					sb.append(fromFiles1[0]);
				}
				for (int i = 1; i < fromFiles1.length; i++) {
					sb.append(", ");
					sb.append(fromFiles1[i]);
				}
				output.println("\t"+doc1Name+": "+sb.toString());
				sb = new StringBuilder();
				SPDXLicenseInfo[] fromFiles2 = comparer.getSpdxDoc(1).getSpdxPackage().getLicenseInfoFromFiles();
				if (fromFiles2 != null && fromFiles2.length > 0) {
					sb.append(fromFiles2[0]);
				}
				for (int i = 1; i < fromFiles2.length; i++) {
					sb.append(", ");
					sb.append(fromFiles2[i]);
				}
				output.println("\t"+doc2Name+": "+sb.toString());
			}
			// comments on license
			if (!comparer.isLicenseCommentsEqual()) {
				output.println("Package license comments differ.");
				String s1 = comparer.getSpdxDoc(0).getSpdxPackage().getLicenseComment();
				if (s1 == null || s1.trim().isEmpty()) {
					output.println("\t"+doc1Name+": [no license comments]");
				} else {
					output.println("\t"+doc1Name+": "+s1);
				}
				String s2 = comparer.getSpdxDoc(1).getSpdxPackage().getLicenseComment();
				if (s2 == null || s2.trim().isEmpty()) {
					output.println("\t"+doc2Name+": [no license comments]");
				} else {
					output.println("\t"+doc2Name+": "+s2);
				}
			}
			// copyright text
			if (!comparer.isCopyrightTextsEqual()) {
				output.println("Copyright texts differ.");
				output.println("\t"+doc1Name+": "+comparer.getSpdxDoc(0).getSpdxPackage().getDeclaredCopyright());
				output.println("\t"+doc2Name+": "+comparer.getSpdxDoc(1).getSpdxPackage().getDeclaredCopyright());
			}
			// package summary description
			if (!comparer.isPackageSummariesEqual()) {
				output.println("Package summaries differ.");
				String s1 = comparer.getSpdxDoc(0).getSpdxPackage().getShortDescription();
				if (s1 == null || s1.trim().isEmpty()) {
					output.println("\t"+doc1Name+": [no package summary]");
				} else {
					output.println("\t"+doc1Name+": "+s1);
				}
				String s2 = comparer.getSpdxDoc(1).getSpdxPackage().getShortDescription();
				if (s2 == null || s2.trim().isEmpty()) {
					output.println("\t"+doc2Name+": [no package summary]");
				} else {
					output.println("\t"+doc2Name+": "+s2);
				}
			}
			// package detailed description
			if (!comparer.isPackageDescriptionsEqual()) {
				output.println("Package detailed descriptions differ.");
				String s1 = comparer.getSpdxDoc(0).getSpdxPackage().getDescription();
				if (s1 == null || s1.trim().isEmpty()) {
					output.println("\t"+doc1Name+": [no package detailed description]");
				} else {
					output.println("\t"+doc1Name+": "+s1);
				}
				String s2 = comparer.getSpdxDoc(1).getSpdxPackage().getDescription();
				if (s2 == null || s2.trim().isEmpty()) {
					output.println("\t"+doc2Name+": [no package detailed description]");
				} else {
					output.println("\t"+doc2Name+": "+s2);
				}
			}
		}
	}

	/**
	 * Prints a verification code to output
	 * @param verificationCode
	 * @param doc1Name
	 */
	private static void printVerificationCode(
			SpdxPackageVerificationCode verificationCode, String docName, PrintStream output) {
		output.println("Verification code value for "+docName+ ": "+verificationCode.getValue());
		if (verificationCode.getExcludedFileNames() != null && 
				verificationCode.getExcludedFileNames().length > 0) {
			output.println("The following files were excluded from the verification code for "+docName+":");
			for (int i = 0; i < verificationCode.getExcludedFileNames().length; i++) {
				output.println("\t"+verificationCode.getExcludedFileNames()[i]);
			}
		}
	}

	/**
	 * @param comparer
	 * @param doc1Name
	 * @param doc2Name
	 * @param output
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static void printCreatorCompareResults(SpdxComparer comparer,
			String doc1Name, String doc2Name, PrintStream output) throws SpdxCompareException, InvalidSPDXAnalysisException {
		// creator
		if (!comparer.isCreatorInformationEqual()) {
			// creators
			String[] inDoc1NotDoc2 = comparer.getUniqueCreators(0, 1);
			if (inDoc1NotDoc2 != null && inDoc1NotDoc2.length > 0) {
				output.println("The following creators are in "+doc1Name+" and not in "+doc2Name);
				for (int i = 0; i < inDoc1NotDoc2.length; i++) {
					output.println("\t"+inDoc1NotDoc2[i]);
				}
			}
			String[] inDoc2NotDoc1 = comparer.getUniqueCreators(1, 0);
			if (inDoc2NotDoc1 != null && inDoc2NotDoc1.length > 0) {
				output.println("The following creators are in "+doc2Name+" and not in "+doc1Name);
				for (int i = 0; i < inDoc2NotDoc1.length; i++) {
					output.println("\t"+inDoc2NotDoc1[i]);
				}
			}
			SPDXCreatorInformation doc1CreatorInfo = comparer.getSpdxDoc(0).getCreatorInfo();
			SPDXCreatorInformation doc2CreatorInfo = comparer.getSpdxDoc(1).getCreatorInfo();
			// created
			if (!SpdxComparer.stringsEqual(doc1CreatorInfo.getCreated(), doc2CreatorInfo.getCreated())) {
				output.println("Creator creation dates differ.");
				if (doc1CreatorInfo.getCreated() == null) {
					output.println("\t"+doc1Name+": No creation date");
				} else {
					output.println("\t"+doc1Name+": "+doc1CreatorInfo.getCreated());
				}
				if (doc2CreatorInfo.getCreated() == null) {
					output.println("\t"+doc2Name+": No creation date");
				} else {
					output.println("\t"+doc2Name+": "+doc2CreatorInfo.getCreated());
				}
			}
			// creator comments
			if (!SpdxComparer.stringsEqual(doc1CreatorInfo.getComment(),doc2CreatorInfo.getComment())) {
				output.println("Creator comments differ.");
				if (doc1CreatorInfo.getComment() == null) {
					output.println("\t"+doc1Name+": [No comment]");
				} else {
					output.println("\t"+doc1Name+": "+doc1CreatorInfo.getComment());
				}
				if (doc2CreatorInfo.getComment() == null) {
					output.println("\t"+doc2Name+": [No comment]");
				} else {
					output.println("\t"+doc2Name+": "+doc2CreatorInfo.getComment());
				}
			}
			// license list version
			if (!SpdxComparer.stringsEqual(doc1CreatorInfo.getLicenseListVersion(), doc2CreatorInfo.getLicenseListVersion())) {
				output.println("License list versions differ.");
				if (doc1CreatorInfo.getLicenseListVersion() == null) {
					output.println("\t"+doc1Name+": [No license list version]");
				} else {
					output.println("\t"+doc1Name+": "+doc1CreatorInfo.getLicenseListVersion());
				}
				if (doc2CreatorInfo.getLicenseListVersion() == null) {
					output.println("\t"+doc2Name+": [No license list version]");
				} else {
					output.println("\t"+doc2Name+": "+doc2CreatorInfo.getLicenseListVersion());
				}
			}
		}
	}

	/**
	 * @param spdxDocFileName File name of either an RDF or Tag formated SPDX file
	 * @return
	 */
	protected static SPDXDocument openRdfOrTagDoc(String spdxDocFileName) throws SpdxCompareException {
		File spdxDocFile = new File(spdxDocFileName);
		if (!spdxDocFile.exists()) {
			throw(new SpdxCompareException("SPDX File "+spdxDocFileName+" does not exist."));
		}
		if (!spdxDocFile.canRead()) {
			throw(new SpdxCompareException("SPDX File "+spdxDocFileName+" can not be read."));
		}
		// try to open the file as an XML file first
		SPDXDocument retval = null;
		try {
			retval = SPDXDocumentFactory.creatSpdxDocument(spdxDocFileName);
		} catch (IOException e) {
			// ignore - assume this is a tag value file
		} catch (InvalidSPDXAnalysisException e) {
			// ignore - assume this is a tag value file
		} catch (Exception e) {
			// ignore this as well
		}
		if (retval == null) {
			File tempRdfFile = null;
			try {
				tempRdfFile = File.createTempFile("SPDXTempFile", ".spdx");
			} catch (IOException e) {
				throw(new SpdxCompareException("Unable to create temporary file for tag/value to rdf conversion",e));
			}
			try {
				convertTagValueToRdf(spdxDocFile, tempRdfFile);
				retval = SPDXDocumentFactory.creatSpdxDocument(tempRdfFile.getPath());
			} catch (SpdxCompareException e) {
				throw(new SpdxCompareException("File "+spdxDocFileName+" is not a recognized RDF/XML or tag/value format: "+e.getMessage()));
			} catch (IOException e) {
				throw(new SpdxCompareException("IO Error reading the converted tag/value file "+spdxDocFileName,e));
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("Invalid tag/value file "+spdxDocFileName,e));
			} finally {
				if (tempRdfFile != null) {
					tempRdfFile.delete();
				}
			}
		}
		if (retval == null) {
			throw(new SpdxCompareException("File "+spdxDocFileName+" is not a recognized RDF/XML or tag/value format"));
		}
		return retval;
	}

	/**
	 * Converts a tag/value filt to an rdfFile
	 * @param tagValueFile Input file in tag/value format
	 * @param tempRdfFile File to output the generated RDF file (must already exist - file is overwritten)
	 */
	private static void convertTagValueToRdf(File tagValueFile, File rdfFile) throws SpdxCompareException {
		if (!rdfFile.canWrite()) {
			throw(new SpdxCompareException("Can not write to output file"));
		}
		FileOutputStream out = null;
		FileInputStream in = null;
		try {
			out = new FileOutputStream(rdfFile);
			in = new FileInputStream(tagValueFile);
			TagToRDF.convertTagFileToRdf(in, out);
		} catch (Exception e) {
			throw(new SpdxCompareException("Error converting tag/value to RDF/XML format: "+e.getMessage(),e));
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					System.out.println("Error closing RDF file: " + e.getMessage());
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					System.out.println("Error closing Tag/Value file: " + e.getMessage());
				}
			}
		}
	}

	private static void usage() {		
		System.out.println("Usage: CompareSpdxDoc doc1 doc2 [output]");
		System.out.println("where doc1 and doc2 are file names of valid SPDX documents ");
		System.out.println("in either tag/value or RDF/XML format");
		System.out.println("and [output] is an optional output text file name");		
	}

}
