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
import java.nio.charset.Charset;

import org.spdx.compare.LicenseCompareHelper;
import org.spdx.compare.SpdxCompareException;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;

import com.google.common.io.Files;

/**
 * Tool to compare a license text to standard licenses.  Lists all standard
 * license ID's that are equivalent using the SPDX Legal team's license matching
 * guidelines (http://spdx.org/spdx-license-list/matching-guidelines)
 * @author Gary O'Neall
 *
 */
public class MatchingStandardLicenses {

	/**
	 * This class should not be instantiated.  Call the main method to invoke.
	 */
	private MatchingStandardLicenses() {
		
	}

	static int MIN_ARGS = 1;
	static int MAX_ARGS = 1;
	static final int ERROR_STATUS = 1;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < MIN_ARGS || args.length > MAX_ARGS) {
			System.out.println("Invalid arguments");
			usage();
			System.exit(ERROR_STATUS);
		}
		File textFile = new File(args[0]);
		
		if (!textFile.exists()) {
			System.out.println("Text file "+textFile.getName()+" does not exist");
			usage();
			System.exit(ERROR_STATUS);
		}		
		
		String licenseText = null;
		try {
			licenseText = readAll(textFile);
		} catch (IOException e) {
			System.out.println("Error reading file: "+e.getMessage());
			System.exit(ERROR_STATUS);
		}
		
		String[] matchingLicenseIds = null;
		try {
			matchingLicenseIds = LicenseCompareHelper.matchingStandardLicenseIds(licenseText);
		} catch (InvalidSPDXAnalysisException e) {
			System.out.println("Error reading standard licenses: "+e.getMessage());
			System.exit(ERROR_STATUS);
		} catch (SpdxCompareException e) {
			System.out.println("Error comparing licenses: "+e.getMessage());
			System.exit(ERROR_STATUS);
		}
		
		if (matchingLicenseIds == null || matchingLicenseIds.length == 0) {
			System.out.println("No standard licenses matched.");
		} else {
			StringBuilder sb = new StringBuilder("The following license id(s) match: ");
			sb.append(matchingLicenseIds[0]);
			for (int i = 1; i < matchingLicenseIds.length; i++) {
				sb.append(", ");
				sb.append(matchingLicenseIds[i]);
			}
			System.out.println(sb.toString());
		}
		System.exit(0);
	}

	/**
	 * @param textFile
	 * @return
	 * @throws IOException 
	 */
	private static String readAll(File textFile) throws IOException {
		return Files.toString(textFile, Charset.defaultCharset());
	}

	private static void usage() {
		System.out.println("Usage:");
		System.out.println("MatchingStandardLicenses textfile.txt");
		System.out.println("   textfile.txt is a text file containing the license text to compare.");
	}
}
