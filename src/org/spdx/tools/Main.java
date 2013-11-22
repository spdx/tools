/**
 * Copyright (c) Linux Foundation
 * Copyright (c) Roger Meier <r.meier@siemens.com>
 * SPDX-License-Identifier:	Apache-2.0
 */

package org.spdx.tools;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Dispatch to the individual tools
 */
public class Main {

	public static void main(String[] args) {

		if (args.length < 1) {
			usage();
			return;
		}

		String spdxTool = args[0];
		args = ArrayUtils.removeElement(args, args[0]);

		if (spdxTool.equalsIgnoreCase("SpdxViewer")) {
			SpdxViewer.main(args);
		} else if (spdxTool.equalsIgnoreCase("PrettyPrinter")) {
			PrettyPrinter.main(args);
		} else if (spdxTool.equalsIgnoreCase("TagToSpreadsheet")) {
			TagToSpreadsheet.main(args);
		} else if (spdxTool.equalsIgnoreCase("TagToRDF")) {
			TagToRDF.main(args);
		} else if (spdxTool.equalsIgnoreCase("RdfToTag")) {
			RdfToTag.main(args);
		} else if (spdxTool.equalsIgnoreCase("RdfToHtml")) {
			RdfToHtml.main(args);
		} else if (spdxTool.equalsIgnoreCase("RdfToSpreadsheet")) {
			RdfToSpreadsheet.main(args);
		} else if (spdxTool.equalsIgnoreCase("SpreadsheetToRDF")) {
			SpreadsheetToRDF.main(args);
		} else if (spdxTool.equalsIgnoreCase("SpreadsheetToTag")){
			SpreadsheetToTag.main(args);
		} else if (spdxTool.equalsIgnoreCase("CompareMultipleSpdxDocs")) {
			CompareMultpleSpdxDocs.main(args);
		} else if (spdxTool.equalsIgnoreCase("CompareSpdxDocs")) {
			CompareSpdxDocs.main(args);
		} else if (spdxTool.equalsIgnoreCase("LicenseRDFAGenerator")) {
			LicenseRDFAGenerator.main(args);
		} else if (spdxTool.equalsIgnoreCase("GenerateVerificationCode")) {
			GenerateVerificationCode.main(args);
		} else {
			usage();
		}

	}

	private static void usage() {
		System.out
				.println(""
						+ "Usage: java -jar spdx-tools-jar-with-dependencies.jar <function> <parameters> \n"
						+ "function                 parameter                         example \n"
						+ "------------------------------------------------------------------------------------------------------------------- \n"
						+ "TagToSpreadsheet         inputFile outputFile              Examples/SPDXTagExample.tag TagToSpreadsheet.xls \n"
						+ "TagToRDF                 inputFile outputFile              Examples/SPDXTagExample.tag TagToRDF.rdf \n"
						+ "RdfToTag                 inputFile outputFile              TestFiles/SPDXRdfExample.rdf  RdfToTag.tag \n"
						+ "RdfToHtml                inputFile outputFile              TestFiles/SPDXRdfExample.rdf  RdfToHtml.html \n"
						+ "RdfToSpreadsheet         inputFile outputFile              TestFiles/SPDXRdfExample.rdf RdfToSpreadsheet.xls \n"
						+ "SpreadsheetToRDF         inputFile outputFile              Examples/SPDXSpreadsheetExample.xls SpreadsheetToRDF.rdf \n"
						+ "SpreadsheetToTag         inputFile outputFile              Examples/SPDXSpreadsheetExample.xls SpreadsheetToTag.tag \n"
						+ "SPDXViewer               inputFile                         TestFiles/SPDXRdfExample.rdf \n"
						+ "PrettyPrinter            inputFile                         TestFiles/SPDXRdfExample.rdf \n"
						+ "CompareMultipleSpdxDocs  output.xls doc1 doc2 ... docN \n"
						+ "CompareSpdxDocs          doc1 doc2 [output] \n"
						+ "LicenseRDFAGenerator     licenseSpreadsheet.xls outputDirectory [version] [releasedate] \n"
						+ "GenerateVerificationCode sourceDirectory");
	}
}
