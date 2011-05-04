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

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Source Auditor
 *
 */
public class TestSpdxPackageVerificationCode {

	static final String[] VALUES = new String[] {"0123456789abcdef0123456789abcdef01234567",
			"c1ef456789abcdab0123456789abcdef01234567", "invalidvalue"};
	
	static final String[] VALUES2 = new String[] {"ab23456789abcdef0123456789abcdef01234567",
		"00ef456789abcdab0123456789abcdef01234567", "2invalidvalue2"};

	static final String[] [] SKIPPED_FILES = new String[][] {new String[] {"skipped1", "skipped2"},
			new String[0], new String[] {"oneSkippedFile"}};
	
	static final String[] [] SKIPPED_FILES2 = new String[][] {new String[] {},
		new String[] {"single/file"}, new String[] {"a/b/c", "d/e/f", "g/hi"}};
	
	SpdxPackageVerificationCode[] VERIFICATION_CODES;
	Model model;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		model = ModelFactory.createDefaultModel();
		VERIFICATION_CODES = new SpdxPackageVerificationCode[] {
					new SpdxPackageVerificationCode(VALUES[0], SKIPPED_FILES[0]),
					new SpdxPackageVerificationCode(VALUES[1], SKIPPED_FILES[1]),
					new SpdxPackageVerificationCode(VALUES[2], SKIPPED_FILES[2])
				};
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SpdxPackageVerificationCode#setExcludedFileNames(java.lang.String[])}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetExcludedFileNames() throws InvalidSPDXAnalysisException {
		Resource[] verificationResources = new Resource[VERIFICATION_CODES.length];
		for (int i  = 0; i < verificationResources.length; i++) {
			verificationResources[i] = VERIFICATION_CODES[i].createResource(model);
		}
		for (int i  = 0; i < verificationResources.length; i++) {
			SpdxPackageVerificationCode comp = new SpdxPackageVerificationCode(model, verificationResources[i].asNode());
			comp.setExcludedFileNames(SKIPPED_FILES2[i]);
			assertEquals(SKIPPED_FILES2[i].length, comp.getExcludedFileNames().length);
			compareArrays(SKIPPED_FILES2[i], comp.getExcludedFileNames());
			assertEquals(VALUES[i], comp.getValue());
		}
	}

	/**
	 * @param strings
	 * @param excludedFileNames
	 */
	private void compareArrays(String[] s1, String[] s2) {
		assertEquals(s1.length, s2.length);
		for (int i = 0; i < s1.length; i++) {
			boolean found = false;
			for (int j = 0; j < s2.length; j++) {
				if (s1[i].equals(s2[j])) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SpdxPackageVerificationCode#addExcludedFileName(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testAddExcludedFileName() throws InvalidSPDXAnalysisException {
		Resource[] verificationResources = new Resource[VERIFICATION_CODES.length];
		for (int i  = 0; i < verificationResources.length; i++) {
			verificationResources[i] = VERIFICATION_CODES[i].createResource(model);
		}
		for (int i  = 0; i < verificationResources.length; i++) {
			SpdxPackageVerificationCode comp = new SpdxPackageVerificationCode(model, verificationResources[i].asNode());
			comp.addExcludedFileName("File"+String.valueOf(i));
			String[] compNames = new String[SKIPPED_FILES[i].length+1];
			for (int j = 0; j < SKIPPED_FILES[i].length; j++) {
				compNames[j] = SKIPPED_FILES[i][j];
			}
			compNames[SKIPPED_FILES[i].length] = "File"+String.valueOf(i);
			assertEquals(compNames.length, comp.getExcludedFileNames().length);
			compareArrays(compNames, comp.getExcludedFileNames());
			assertEquals(VALUES[i], comp.getValue());
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SpdxPackageVerificationCode#setValue(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetValue() throws InvalidSPDXAnalysisException {
		Resource[] verificationResources = new Resource[VERIFICATION_CODES.length];
		for (int i  = 0; i < verificationResources.length; i++) {
			verificationResources[i] = VERIFICATION_CODES[i].createResource(model);
		}
		for (int i  = 0; i < verificationResources.length; i++) {
			SpdxPackageVerificationCode comp = new SpdxPackageVerificationCode(model, verificationResources[i].asNode());
			comp.setValue(VALUES2[i]);
			compareArrays(SKIPPED_FILES[i], comp.getExcludedFileNames());
			assertEquals(VALUES2[i], comp.getValue());
		}
	}
	
	@Test
	public void testVerify() {
		ArrayList<String> verify = VERIFICATION_CODES[0].verify();
		assertEquals(0, verify.size());
		verify = VERIFICATION_CODES[1].verify();
		assertEquals(0, verify.size());
		verify = VERIFICATION_CODES[2].verify();
		assertEquals(1, verify.size());
	}

}
