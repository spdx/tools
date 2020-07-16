/**
 * Copyright (c) 2018 Source Auditor Inc.
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
 */
package org.spdx.tools;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the example files
 * @author Gary O'Neall
 *
 */
public class TestExamples {

	private static final String EXAMPLES_DIRECTORY = "Examples";

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

	@Test
	public void test() throws Exception {
		String tempDir = System.getProperty("java.io.tmpdir");
		File[] exampleFiles = new File(EXAMPLES_DIRECTORY).listFiles();
		for (File exampleFile:exampleFiles) {
			if (exampleFile.getName().endsWith(".xls")) {
				File tempRdf = null;
				try {
					tempRdf = new File(tempDir + File.separator + exampleFile.getName() + ".rdf");
					if (tempRdf.exists()) {
						if (!tempRdf.delete()) {
							fail("Could not delete temporary file "+tempRdf.getAbsolutePath());
						}
					}
					SpreadsheetToRDF.onlineFunction(new String[] {exampleFile.getPath(),tempRdf.getPath()});
					List<String> warnings = Verify.verifyRDFFile(tempRdf.getPath());
					assertNoWarnings(exampleFile, warnings);
				} finally {
					if (tempRdf != null) {
						tempRdf.delete();
					}
				}
			} else {
				List<String> warnings = Verify.verify(exampleFile.getPath());
				assertNoWarnings(exampleFile, warnings);
			}
		}
	}

	private void assertNoWarnings(File file, List<String> warnings) {
		if (warnings.size() == 0) {
			return;
		}
		StringBuilder msg = new StringBuilder("Example file ");
		msg.append(file.getName());
		msg.append(" is not valid due to the following:");
		for (String warning:warnings) {
			msg.append("\n\t");
			msg.append(warning);
		}
		fail(msg.toString());
	}

}
