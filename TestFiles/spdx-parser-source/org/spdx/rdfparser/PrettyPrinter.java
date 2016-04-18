/**
 * Copyright (c) 2010 Source Auditor Inc.
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
package org.spdx.rdfparser;import java.util.ArrayList;import org.spdx.rdfparser.SPDXDocument.SPDXPackage;/**
 * Simple pretty printer for SPDX RDF XML files.  Writes output to System.out.
 * Usage: PrettyPrinter SPDXRdfXMLFile > textFile
 * where SPDXRdfXMLFile is a valid SPDX RDF XML file
 *
 * @author Gary O'Neall
 * @version 0.1
 */
public class PrettyPrinter {
	static final int MIN_ARGS = 1;
	static final int MAX_ARGS = 1;
	/**
	 * Pretty Printer for an SPDX Document
	 * @param args Argument 0 is a the file path name of the SPDX RDF/XML file
	 */
	public static void main(String[] args) {
		if (args.length < MIN_ARGS) {
			System.err.println("Usage:\n PrettyPrinter file\nwhere file is the file path to a valid SPDX RDF XML file");
			return;
		}
		if (args.length > MAX_ARGS) {
			System.out.printf("Warning: Extra arguments will be ignored");
		}
		SPDXDocument doc = null;
		try {
			doc = SPDXDocumentFactory.creatSpdxDocument(args[0]);
		} catch(Exception ex) {
			System.out.print("Error creating SPDX Document: "+ex.getMessage());
			return;
		}
		try {			ArrayList<String> verify = doc.verify();			if (verify.size() > 0) {				System.out.println("This SPDX Document is not valid due to:");				for (int i = 0; i < verify.size(); i++) {					System.out.print("\t" + verify.get(i));				}			}
			prettyPrintDoc(doc);
		} catch (InvalidSPDXAnalysisException e) {
			System.out.print("Error pretty printing SPDX Document: "+e.getMessage());
			return;
		} catch (Exception e) {			System.out.print("Unexpected error displaying SPDX Document: "+e.getMessage());		}
	}

	/**
	 * @param doc
	 * @throws InvalidSPDXAnalysisException
	 */
	private static void prettyPrintDoc(SPDXDocument doc) throws InvalidSPDXAnalysisException {
		if (doc == null) {
			System.out.println("Warning: No document to print");
			return;
		}
		if (doc.getSpdxDocUri() != null) {
			System.out.printf("SPDX Document for %1s\n",doc.getSpdxDocUri());
		}		String spdxVersion = "";
		if (doc.getSpdxVersion() != null && doc.getCreatorInfo().getCreated() != null) {			spdxVersion = doc.getSpdxVersion();
			System.out.printf("Version: %1s\tCreated: %2s\n", spdxVersion, doc.getCreatorInfo().getCreated());
		}		if (!spdxVersion.equals(SPDXDocument.POINT_EIGHT_SPDX_VERSION) && !spdxVersion.equals(SPDXDocument.POINT_NINE_SPDX_VERSION)) {			SPDXStandardLicense dataLicense = doc.getDataLicense();			if (dataLicense != null) {				System.out.printf("Data License: %1s\n", dataLicense.getName());			}		}
		if (doc.getCreatorInfo().getCreators() != null && doc.getCreatorInfo().getCreators().length > 0) {
			System.out.println("Created by:");
			String[] creators = doc.getCreatorInfo().getCreators();
			for (int i = 0; i < creators.length; i++) {
				System.out.printf("\t%1s\n", creators[i]);
			}
		}		if (doc.getCreatorInfo().getCreated() != null && !doc.getCreatorInfo().getCreated().isEmpty()) {			System.out.printf("\t%1s\n", doc.getCreatorInfo().getCreated());		}
		if (doc.getCreatorInfo().getComment() != null && !doc.getCreatorInfo().getComment().isEmpty()) {
			System.out.println("Creator comment: "+doc.getCreatorInfo().getComment());
		}
		if (doc.getReviewers() != null && doc.getReviewers().length > 0) {
			System.out.println("Reviewed by:");
			SPDXReview[] reviewedBy = doc.getReviewers();
			for (int i = 0; i < reviewedBy.length; i++) {
				if (reviewedBy[i].getComment() != null && !reviewedBy[i].getComment().isEmpty()) {
					System.out.printf("\t%1s\t%2s\tComment:%3s\n",reviewedBy[i].getReviewer(),
							reviewedBy[i].getReviewDate(), reviewedBy[i].getComment());
				} else {
					System.out.printf("\t%1s\t%2s\n",reviewedBy[i].getReviewer(),
							reviewedBy[i].getReviewDate());
				}
			}
		}
		prettyPrintPackage(doc.getSpdxPackage());
		if (doc.getExtractedLicenseInfos() != null && doc.getExtractedLicenseInfos().length > 0) {
			SPDXNonStandardLicense[] nonStandardLic = doc.getExtractedLicenseInfos();
			System.out.println("Non-Standard Licenses:");
			for (int i = 0; i < nonStandardLic.length; i++) {
				prettyPrintLicense(nonStandardLic[i]);
			}
		}
	}

	/**
	 * @param license
	 */
	private static void prettyPrintLicense(SPDXNonStandardLicense license) {
		// id
		if (license.getId() != null && !license.getId().isEmpty()) {
			System.out.printf("\tLicense ID: %1s", license.getId());
		}
		if (license.getText() != null && !license.getText().isEmpty()) {
			System.out.printf("\tText: %1s", license.getText());
		}
		System.out.println();
	}

	/**
	 * @param spdxPackage
	 * @throws InvalidSPDXAnalysisException
	 */
	private static void prettyPrintPackage(SPDXPackage pkg) throws InvalidSPDXAnalysisException {
		// Declared name
		if (pkg.getDeclaredName() != null && !pkg.getDeclaredName().isEmpty()) {
			System.out.printf("Package Name: %1s\n", pkg.getDeclaredName());
		}
		// Short description
		if (pkg.getShortDescription() != null && !pkg.getShortDescription().isEmpty()) {
			System.out.println(pkg.getShortDescription());
		}
		// Source info
		if (pkg.getSourceInfo() != null && !pkg.getSourceInfo().isEmpty()) {
			System.out.printf("Additional Information: %1s\n", pkg.getSourceInfo());
		}
		// File name
		if (pkg.getFileName() != null && !pkg.getFileName().isEmpty()) {
			System.out.printf("File name: %1s\n", pkg.getFileName());
		}		// Supplier		if (pkg.getSupplier() != null && !pkg.getSupplier().isEmpty()) {			System.out.printf("Supplier: %1s\n", pkg.getSupplier());		}		// Originator		if (pkg.getOriginator() != null && !pkg.getOriginator().isEmpty()) {			System.out.printf("Originator: %1s\n", pkg.getOriginator());		}
		// sha1
		if (pkg.getSha1() != null && !pkg.getSha1().isEmpty()) {
			System.out.printf("SHA1: %1s\n",pkg.getSha1());
		}
		// package verification code
		if (pkg.getVerificationCode() != null && pkg.getVerificationCode().getValue() != null && !pkg.getVerificationCode().getValue().isEmpty()) {
			System.out.printf("Verification: %1s\n", pkg.getVerificationCode().getValue());
		}
		// Description
		if (pkg.getDescription() != null && !pkg.getDescription().isEmpty()) {
			System.out.printf("Description: %1s\n", pkg.getDescription());
		}
		// Declared copyright
		if (pkg.getDeclaredCopyright() != null && ! pkg.getDeclaredCopyright().isEmpty()) {
			System.out.printf("Declared Copyright: %1s\n", pkg.getDeclaredCopyright());
		}
		// Declared licenses			if (pkg.getDeclaredLicense() != null) {			System.out.println("License declared: "+pkg.getDeclaredLicense());		}		// concluded license		if (pkg.getConcludedLicenses() != null) {			System.out.println("License concluded: "+pkg.getConcludedLicenses());		}		// file licenses
		if (pkg.getLicenseInfoFromFiles() != null && pkg.getLicenseInfoFromFiles().length > 0) {
			SPDXLicenseInfo[] licenses = pkg.getLicenseInfoFromFiles();
			System.out.println("Licenses from files:");
			for (int i = 0; i < licenses.length; i++) {
				System.out.printf("\t%1s\n", licenses[i].toString());
			}
		}		if (pkg.getLicenseComment() != null && !pkg.getLicenseComment().isEmpty()) {			System.out.println("License comments: "+pkg.getLicenseComment());		}
		// Files
		if (pkg.getFiles() != null && pkg.getFiles().length > 0) {
			for (int i = 0; i < pkg.getFiles().length; i++) {
				prettyPrintFile(pkg.getFiles()[i]);
			}
		}
	}

	/**
	 * @param file
	 */
	private static void prettyPrintFile(SPDXFile file) {
		// name
		if (file.getName() != null && !file.getName().isEmpty()) {
			System.out.printf("File Name: %1s\n", file.getName());
		}
		// type
		if (file.getType() != null && !file.getType().isEmpty()) {
			System.out.printf("\tFile Type: %1s\n", file.getType());
		}
		// sha1
		if (file.getSha1() != null && !file.getSha1().isEmpty()) {
			System.out.printf("\tSHA1: %1s\n", file.getSha1());
		}
		// concluded license
		if (file.getConcludedLicenses() != null) {
			System.out.println("\tConcluded license: "+file.getConcludedLicenses().toString());
		}		// License info in file		if (file.getSeenLicenses() != null && file.getSeenLicenses().length > 0) {			System.out.print("\tLicense infos from file: ");			System.out.print(file.getSeenLicenses()[0].toString());			for (int i = 1; i < file.getSeenLicenses().length; i++) {				System.out.print(", "+file.getSeenLicenses()[i].toString());			}			System.out.println();		}		// license comments		if (file.getLicenseComments() != null && !file.getLicenseComments().isEmpty()) {			System.out.println("\tLicense comments: "+file.getLicenseComments());		}		// file copyright		if (file.getCopyright() != null && !file.getCopyright().isEmpty()) {			System.out.println("\tFile copyright: "+file.getCopyright());		}		// artifact of		if (file.getArtifactOf() != null && file.getArtifactOf().length > 0) {			for (int i = 0; i < file.getArtifactOf().length; i++) {				prettyPrintProject(file.getArtifactOf()[i]);			}		}
	}	/**	 * @param doapProject	 */	private static void prettyPrintProject(DOAPProject doapProject) {		// project name 		if (doapProject.getName() != null && !doapProject.getName().isEmpty()) {			System.out.println("\t\tArtifact of Project: "+doapProject.getName());		}		// project homepage		if (doapProject.getHomePage() != null && !doapProject.getHomePage().isEmpty()) {			System.out.println("\t\tArtifact of home page: "+doapProject.getHomePage());		}		// DOAP file url		if (doapProject.getProjectUri() != null && !doapProject.getProjectUri().isEmpty()) {			System.out.println("\t\tArtifact of DOAP URI: "+doapProject.getProjectUri());		}	}
}