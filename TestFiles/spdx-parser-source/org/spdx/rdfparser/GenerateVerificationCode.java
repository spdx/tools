/**
 * Copyright (c) 2011 Source Auditor Inc.
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
package org.spdx.rdfparser;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Generates a verification code for a specific directory
 * @author Gary O'Neall
 *
 */
public class GenerateVerificationCode {

	/**
	 * Print an SPDX Verification code for a directory of files
	 * args[0] is the source directory containing the files
	 * args[1] is an optional regular expression of skipped files.  The expression is applied against a file path relative the the source directory supplied
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1 || args.length > 2) {
			error("Incorrect number of arguments.");
			System.exit(1);
		}
		File sourceDirectory = new File(args[0]);
		if (!sourceDirectory.exists()) {
			error("Source directory "+args[0]+" does not exist.");
			System.exit(1);
		}
		if (!sourceDirectory.isDirectory()) {
			error("File "+args[0]+" is not a directory.");
			System.exit(1);
		}
		String skippedRegex = null;
		File[] skippedFiles = new File[0];
		if (args.length > 1) {
			skippedRegex = args[1];
			skippedFiles = collectSkippedFiles(skippedRegex, sourceDirectory);
		}
		try {
			VerificationCodeGenerator vcg = new VerificationCodeGenerator(new JavaSha1ChecksumGenerator());
			SpdxPackageVerificationCode verificationCode = vcg.generatePackageVerificationCode(sourceDirectory, skippedFiles);
			printVerificationCode(verificationCode);
			System.exit(0);
		} catch (Exception ex) {
			error("Error creating verification code: "+ex.getMessage());
		}
	}

	/**
	 * Collect files to be skipped
	 * @param skippedRegex Regular Expression for file paths to be skipped
	 * @param dir Directory to scan for collecting skipped files
	 * @return
	 */
	private static File[] collectSkippedFiles(String skippedRegex, File dir) {
		Pattern skippedPattern = Pattern.compile(skippedRegex);
		ArrayList<File> skippedFiles = new ArrayList<File>();
		collectSkippedFiles(skippedPattern, skippedFiles, dir.getPath(), dir);
		File[] retval = new File[skippedFiles.size()];
		retval = skippedFiles.toArray(retval);
		return retval;
	}

	/**
	 * Internal method to recurse through the source directory collecting files to skip
	 * @param skippedPattern
	 * @param skippedFiles
	 * @param rootPath
	 * @param dir
	 * @return
	 */
	private static void collectSkippedFiles(Pattern skippedPattern,
			ArrayList<File> skippedFiles, String rootPath, File dir) {
		if (dir.isFile()) {
			String relativePath = dir.getPath().substring(rootPath.length()+1);
			if (skippedPattern.matcher(relativePath).matches()) {
				skippedFiles.add(dir);
			}
		} else if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (int i = 0; i < children.length; i++) {
				if (children[i].isFile()) {
					String relativePath = children[i].getPath().substring(rootPath.length()+1);
					if (skippedPattern.matcher(relativePath).matches()) {
						skippedFiles.add(children[i]);
					}
				} else if (children[i].isDirectory()) {
					collectSkippedFiles(skippedPattern, skippedFiles, rootPath, children[i]);
				}
			}
		}
	}

	/**
	 * @param verificationCode
	 */
	private static void printVerificationCode(
			SpdxPackageVerificationCode verificationCode) {
		System.out.println("Verification code value: "+verificationCode.getValue());
		String[] excludedFiles = verificationCode.getExcludedFileNames();
		if (excludedFiles != null && excludedFiles.length > 0) {
			System.out.println("Excluded files:");
			for (int i = 0; i < excludedFiles.length; i++) {
				System.out.println("\t"+excludedFiles[i]);
			}
		} else {
			System.out.println("No excluded files");
		}
	}

	/**
	 * @param string
	 */
	private static void error(String string) {
		System.out.println(string);
		usage();
	}

	/**
	 *
	 */
	private static void usage() {
		System.out.println("Usage: GenerateVerificationCode sourceDirectory");
		System.out.println("where sourceDirectory is the root of the archive file for which the verification code is generated");
	}

}
