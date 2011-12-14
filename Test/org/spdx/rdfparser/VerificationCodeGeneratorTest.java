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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Source Auditor
 *
 */
public class VerificationCodeGeneratorTest {

	static final String SOURCE_PATH = "TestFiles"+File.separator+"spdx-parser-source";
	static final String [] SKIPPED_FILE_NAMES = new String[] {
		"TestFiles"+File.separator+"spdx-parser-source"+File.separator+"org"+File.separator+"spdx"+File.separator+"rdfparser"+File.separator+"DOAPProject.java",
		"TestFiles"+File.separator+"spdx-parser-source"+File.separator+"org"+File.separator+"spdx"+File.separator+"rdfparser"+File.separator+"SPDXFile.java"
	};
	private static final Object SHA1_RESULT = "559f9215216045864ca5785d1892a00106cf0f6a";
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.VerificationCodeGenerator#generatePackageVerificationCode(java.io.File, java.io.File[])}.
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 */
	@Test
	public void testGeneratePackageVerificationCodeFileFileArray() throws NoSuchAlgorithmException, IOException {
		VerificationCodeGenerator vg = new VerificationCodeGenerator(new JavaSha1ChecksumGenerator());
		File sourceDirectory = new File(SOURCE_PATH);
		File[] skippedFiles = new File[SKIPPED_FILE_NAMES.length];
		for (int i = 0; i < skippedFiles.length; i++) {
			skippedFiles[i] = new File(SKIPPED_FILE_NAMES[i]);
		}
		SpdxPackageVerificationCode vc = vg.generatePackageVerificationCode(sourceDirectory, skippedFiles);
		assertEquals(SHA1_RESULT, vc.getValue());
		compareFileNameArrays(SKIPPED_FILE_NAMES, vc.getExcludedFileNames());		
	}

	/**
	 * @param skippedFileNames
	 * @param excludedFileNames
	 */
	private void compareFileNameArrays(String[] skippedFileNames,
			String[] excludedFileNames) {
		assertEquals(skippedFileNames.length, excludedFileNames.length);
		for (int i = 0; i < skippedFileNames.length; i++) {
			boolean found = false;
			String skippedFile = VerificationCodeGenerator.normalizeFilePath(skippedFileNames[i].substring(SOURCE_PATH.length()+1));
			for (int j = 0; j < excludedFileNames.length; j++) {
				if (excludedFileNames[j].equals(skippedFile)) {
					found = true;
					break;
				}
			}
			if (!found) {
				fail(skippedFile + " not found");
			}
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.VerificationCodeGenerator#normalizeFilePath(java.lang.String)}.
	 */
	@Test
	public void testNormalizeFilePath() {
		String s1 = "simple/test.c";
		String ns1 = "./simple/test.c";
		String s2 = "name";
		String ns2 = "./name";
		String s3 = "dos\\file\\name.c";
		String ns3 = "./dos/file/name.c";
		String s4 = "\\leading\\slash";
		String ns4 = "./leading/slash";
		String s5 = "test/./dot/./slash";
		String ns5 = "./test/dot/slash";
		String s6 = "test/parent/../directory/name";
		String ns6 = "./test/directory/name";
		assertEquals(ns1, VerificationCodeGenerator.normalizeFilePath(s1));
		assertEquals(ns2, VerificationCodeGenerator.normalizeFilePath(s2));
		assertEquals(ns3, VerificationCodeGenerator.normalizeFilePath(s3));
		assertEquals(ns4, VerificationCodeGenerator.normalizeFilePath(s4));
		assertEquals(ns5, VerificationCodeGenerator.normalizeFilePath(s5));
		assertEquals(ns6, VerificationCodeGenerator.normalizeFilePath(s6));
	}

}
