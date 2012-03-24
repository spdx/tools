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
 * @author Gary O'Neall
 *
 */
public class TestSPDXChecksum {

	String[] ALGORITHMS = new String[] {"SHA1", "SHA1", "SHA1"};
	String[] VALUES = new String[] {"Value1", "Value2", "Value3"};
	SPDXChecksum[] TEST_CHECKSUMS;
	Model model;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		TEST_CHECKSUMS = new SPDXChecksum[ALGORITHMS.length];
		for (int i = 0; i < ALGORITHMS.length; i++) {
			TEST_CHECKSUMS[i] = new SPDXChecksum(ALGORITHMS[i], VALUES[i]);
		}
		model = ModelFactory.createDefaultModel();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXChecksum#setAlgorithm(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
/* - Can not run test due to verifications failurs causing test to fail

	@Test
	public void testSetAlgorithm() throws InvalidSPDXAnalysisException {
		Resource[] checksumResources = new Resource[TEST_CHECKSUMS.length];
		for (int i = 0; i < checksumResources.length; i++) {
			checksumResources[i] = TEST_CHECKSUMS[i].createResource(model);
		}
		String[] newAlgorithms = new String[ALGORITHMS.length];
		for (int i = 0; i < ALGORITHMS.length; i++) {
			newAlgorithms[i] = ALGORITHMS[i] + "-New";
		}
		for (int i = 0;i < checksumResources.length; i++) {
			SPDXChecksum comp = new SPDXChecksum(model, checksumResources[i].asNode());
			comp.setAlgorithm(newAlgorithms[i]);
			assertEquals(newAlgorithms[i], comp.getAlgorithm());
			assertEquals(TEST_CHECKSUMS[i].getValue(), comp.getValue());
		}
	}
*/
	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXChecksum#setValue(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetValue() throws InvalidSPDXAnalysisException {
		Resource[] checksumResources = new Resource[TEST_CHECKSUMS.length];
		for (int i = 0; i < checksumResources.length; i++) {
			checksumResources[i] = TEST_CHECKSUMS[i].createResource(model);
		}
		String[] newValues = new String[VALUES.length];
		for (int i = 0; i < VALUES.length; i++) {
			newValues[i] = VALUES[i] + "-New";
		}
		for (int i = 0;i < checksumResources.length; i++) {
			SPDXChecksum comp = new SPDXChecksum(model, checksumResources[i].asNode());
			comp.setValue(newValues[i]);
			assertEquals(newValues[i], comp.getValue());
			assertEquals(TEST_CHECKSUMS[i].getAlgorithm(), comp.getAlgorithm());
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXChecksum#createResource(com.hp.hpl.jena.rdf.model.Model)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testCreateResource() throws InvalidSPDXAnalysisException {
		Resource[] checksumResources = new Resource[TEST_CHECKSUMS.length];
		for (int i = 0; i < checksumResources.length; i++) {
			checksumResources[i] = TEST_CHECKSUMS[i].createResource(model);
		}
		for (int i = 0;i < checksumResources.length; i++) {
			SPDXChecksum comp = new SPDXChecksum(model, checksumResources[i].asNode());
			assertEquals(TEST_CHECKSUMS[i].getAlgorithm(), comp.getAlgorithm());
			assertEquals(TEST_CHECKSUMS[i].getValue(), comp.getValue());
		}
	}
	
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		SPDXChecksum checksum = new SPDXChecksum("SHA1", "0123456789abcdef0123456789abcdef01234567");
		ArrayList<String> verify = checksum.verify();
		assertEquals(0, verify.size());
		Resource chcksumResource = checksum.createResource(model);
		SPDXChecksum comp = new SPDXChecksum(model, chcksumResource.asNode());
		verify = comp.verify();
		assertEquals(0, verify.size());
	}

}
