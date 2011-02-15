/**
 * Copyright (c) 2010 Source Auditor Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spdx.rdfparser;

import java.io.File;
import java.io.InputStream;

import org.spdx.rdfparser.SPDXDocument.LicenseDeclaration;
import org.spdx.rdfparser.SPDXDocument.SPDXFile;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

/**
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
	static final String spdxResourceURI = "http://spdx.org/ont/#SPDXDoc";
	static final String spdxDeclaredCopyrightURI = "http://spdx.org/ont#DeclaredCopyright";

	/**
	 * Pretty Printer for an SPDX Document
	 * @param args Argument 0 is a the file path name of the SPDX RDF/XML file
	 */
	public static void main(String[] args) {
		if (args.length < MIN_ARGS) {
			System.console().printf("Usage:\n PrettyPrinter file\nwhere file is the file path to a valid SPDX RDF XML file");
			return;
		}
		if (args.length > MAX_ARGS) {
			System.out.printf("Warning: Extra arguments will be ignored");
		}
		File spdxRdfFile = new File(args[0]);
		if (!spdxRdfFile.exists()) {
			System.out.printf("Error: File %1$s does not exist.", args[0]);
			return;
		}
		Model model = ModelFactory.createDefaultModel();
		InputStream spdxRdfInput = FileManager.get().open(args[0]);
		if (spdxRdfInput == null) {
			System.out.printf("Error: Can not open %1$s", args[0]);
			return;
		}
		model.read(spdxRdfInput, null);
		SPDXDocument doc = null;
		try {
			doc = new SPDXDocument(model);
		} catch (InvalidSPDXDocException ex) {
			System.out.print("Error creating SPDX Document: "+ex.getMessage());
			return;
		}
		prettyPrintDoc(doc);
	}

	/**
	 * @param doc
	 */
	private static void prettyPrintDoc(SPDXDocument doc) {
		if (doc == null) {
			System.out.println("Warning: No document to print");
			return;
		}
		if (doc.getName() != null) {
			System.out.printf("SPDX Document for %1s\n",doc.getName());
		}
		if (doc.getSpdxVersion() != null && doc.getCreated() != null) {
			System.out.printf("Version: %1s\tCreated: %2s\n", doc.getSpdxVersion(), doc.getCreated());
		}
		if (doc.getCreatedBy() != null && doc.getCreatedBy().length > 0) {
			System.out.println("Created by:");
			String[] createdBy = doc.getCreatedBy();
			for (int i = 0; i < createdBy.length; i++) {
				System.out.printf("\t%1s\n", createdBy[i]);
			}
		}
		if (doc.getReviewers() != null && doc.getReviewers().length > 0) {
			System.out.println("Reviewed by:");
			String[] reviewedBy = doc.getReviewers();
			for (int i = 0; i < reviewedBy.length; i++) {
				System.out.printf("\t%1s\n",reviewedBy[i]);
			}
		}
		prettyPrintPackage(doc.getSpdxPackage());
		if (doc.getNonStandardLicenses() != null && doc.getNonStandardLicenses().length > 0) {
			SPDXLicense[] nonStandardLic = doc.getNonStandardLicenses();
			System.out.println("Non-Standard Licenses:");
			for (int i = 0; i < nonStandardLic.length; i++) {
				prettyPrintLicense(nonStandardLic[i]);
			}
		}
	}

	/**
	 * @param license
	 */
	private static void prettyPrintLicense(SPDXLicense license) {
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
	 */
	private static void prettyPrintPackage(SPDXPackage pkg) {
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
		}
		// sha1
		if (pkg.getSha1() != null && !pkg.getSha1().isEmpty()) {
				System.out.printf("SHA1: %1s\n",pkg.getSha1());				
		}
		// Description
		if (pkg.getDescription() != null && !pkg.getDescription().isEmpty()) {
			System.out.printf("Description: %1s\n", pkg.getDescription());
		}
		// Declared copyright
		if (pkg.getDeclaredCopyright() != null && ! pkg.getDeclaredCopyright().isEmpty()) {
			System.out.printf("Declared Copyright: %1s\n", pkg.getDeclaredCopyright());
		}
		// Declared licenses
		if (pkg.getDeclaredLicenses() != null && pkg.getDeclaredLicenses().length > 0) {
			for (int i = 0; i < pkg.getDeclaredLicenses().length; i++) {
				prettyPrintDeclaredLicense(pkg.getDeclaredLicenses()[i]);
			}
		}
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
		// file licenses
		if (file.getFileLicenses() != null && file.getFileLicenses().length > 0) {
			for (int i = 0; i < file.getFileLicenses().length; i++) {
				prettyPrintDeclaredLicense(file.getFileLicenses()[i]);
			}
		}
	}

	/**
	 * @param licenseDeclaration
	 */
	private static void prettyPrintDeclaredLicense(
			LicenseDeclaration licenseDeclaration) {
		String[] disjunctive = licenseDeclaration.getDisjunctiveLicenses();
		if (licenseDeclaration.getName() == null || licenseDeclaration.getName().isEmpty()) {
			System.out.println("\tLicense - UNKNOWN");
			return;
		} else {
			System.out.println("\tLicense - "+licenseDeclaration.getName());
		}
		if (disjunctive != null && disjunctive.length > 0) {
			System.out.println("\tLicensed under a choice of:");
			System.out.printf("\t\t%1s\n", licenseDeclaration.getName());
			for (int i = 0; i < disjunctive.length; i++) {
				System.out.printf("\t\t%1s\n", disjunctive[i]);
			}
		}
	}
}
