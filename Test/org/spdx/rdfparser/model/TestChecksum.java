/**
 * Copyright (c) 2015 Source Auditor Inc.
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
package org.spdx.rdfparser.model;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary
 *
 */
public class TestChecksum {
	static final ChecksumAlgorithm[] ALGORITHMS = new ChecksumAlgorithm[] {
			ChecksumAlgorithm.checksumAlgorithm_md5, ChecksumAlgorithm.checksumAlgorithm_sha1,
			ChecksumAlgorithm.checksumAlgorithm_sha256};
	static final String SHA1_VALUE1 = "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12";
	static final String SHA1_VALUE2 = "2222e1c67a2d28fced849ee1bb76e7391b93eb12";
	static final String SHA256_VALUE1 = "CA978112CA1BBDCAFAC231B39A23DC4DA786EFF8147C4E72B9807785AFEE48BB";
	static final String SHA256_VALUE2 = "F7846F55CF23E14EEBEAB5B4E1550CAD5B509E3348FBC4EFA3A1413D393CB650";
	static final String MD5_VALUE1 = "9e107d9d372bb6826bd81d3542a419d6";
	static final String MD5_VALUE2 = "d41d8cd98f00b204e9800998ecf8427e";
	String[] VALUES = new String[] {MD5_VALUE1, SHA1_VALUE1, SHA256_VALUE1};
	Checksum[] TEST_CHECKSUMS;

	Model model;
	IModelContainer modelContainer;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.model = ModelFactory.createDefaultModel();
		modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		TEST_CHECKSUMS = new Checksum[ALGORITHMS.length];
		for (int i = 0; i < ALGORITHMS.length; i++) {
			TEST_CHECKSUMS[i] = new Checksum(ALGORITHMS[i], VALUES[i]);
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Checksum#getType(com.hp.hpl.jena.rdf.model.Model)}.
	 */
	@Test
	public void testGetType() {
		
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Checksum#populateModel()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testPopulateModel() throws InvalidSPDXAnalysisException {
		Resource r = this.TEST_CHECKSUMS[0].createResource(modelContainer);
		Checksum checksum = new Checksum(modelContainer, r.asNode());
		assertEquals(TEST_CHECKSUMS[0].getAlgorithm(), checksum.getAlgorithm());
		assertEquals(TEST_CHECKSUMS[0].getValue(), checksum.getValue());
		TEST_CHECKSUMS[0].setValue(MD5_VALUE2);
		assertEquals(MD5_VALUE2, checksum.getValue());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Checksum#equivalent(org.spdx.rdfparser.model.RdfModelObject)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testEquivalent() throws InvalidSPDXAnalysisException {
		Checksum c1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha256, SHA256_VALUE1);
		c1.createResource(modelContainer);
		Checksum c2 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha256, SHA256_VALUE1);
		assertTrue(c1.equivalent(c2));
		c2.setAlgorithm(ChecksumAlgorithm.checksumAlgorithm_sha1);
		assertFalse(c1.equals(c2));
		c2.setAlgorithm(ChecksumAlgorithm.checksumAlgorithm_sha256);
		assertTrue(c1.equivalent(c2));
		c2.setValue(SHA256_VALUE2);
		assertFalse(c1.equals(c2));
		c2.setValue(SHA256_VALUE1);
		assertTrue(c1.equivalent(c2));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Checksum#findSpdxChecksum(com.hp.hpl.jena.rdf.model.Model, org.spdx.rdfparser.model.Checksum)}.
	 */
	@Test
	public void testFindSpdxChecksum() throws InvalidSPDXAnalysisException {
		Resource[] checksumResources = new Resource[TEST_CHECKSUMS.length-1];
		for (int i = 0; i < checksumResources.length; i++) {
			checksumResources[i] = TEST_CHECKSUMS[i].createResource(modelContainer);
		}
		for (int i = 0;i < checksumResources.length; i++) {
			assertEquals(checksumResources[i], Checksum.findSpdxChecksum(model, TEST_CHECKSUMS[i]));
		}
		Resource r = Checksum.findSpdxChecksum(model, TEST_CHECKSUMS[TEST_CHECKSUMS.length-1]);
		assertTrue(r == null);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Checksum#Checksum(org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm, java.lang.String)}.
	 */
	@Test
	public void testChecksumChecksumAlgorithmString() {
		Checksum c1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_md5, 
				MD5_VALUE1);
		assertEquals(ChecksumAlgorithm.checksumAlgorithm_md5, c1.getAlgorithm());
		assertEquals(MD5_VALUE1, c1.getValue());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Checksum#setAlgorithm(org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm)}.
	 */
	@Test
	public void testSetAlgorithm() throws InvalidSPDXAnalysisException {
		Resource[] checksumResources = new Resource[TEST_CHECKSUMS.length];
		for (int i = 0; i < checksumResources.length; i++) {
			checksumResources[i] = TEST_CHECKSUMS[i].createResource(modelContainer);
		}
		ChecksumAlgorithm[] newAlgorithms = new ChecksumAlgorithm[] {
				TEST_CHECKSUMS[2].algorithm, TEST_CHECKSUMS[0].algorithm, TEST_CHECKSUMS[1].algorithm
		};
		for (int i = 0;i < checksumResources.length; i++) {
			Checksum comp = new Checksum(modelContainer, checksumResources[i].asNode());
			comp.setAlgorithm(newAlgorithms[i]);
			assertEquals(newAlgorithms[i], comp.getAlgorithm());
			assertEquals(TEST_CHECKSUMS[i].getValue(), comp.getValue());
		}
	}


	/**
	 * Test method for {@link org.spdx.rdfparser.model.Checksum#verify()}.
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		Checksum checksum = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, "0123456789abcdef0123456789abcdef01234567");
		ArrayList<String> verify = checksum.verify();
		assertEquals(0, verify.size());
		Resource chcksumResource = checksum.createResource(modelContainer);
		Checksum comp = new Checksum(modelContainer, chcksumResource.asNode());
		verify = comp.verify();
		assertEquals(0, verify.size());
		checksum.setValue("BadValue");
		assertEquals(1, checksum.verify().size());
		checksum.setAlgorithm(null);
		assertEquals(2, checksum.verify().size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Checksum#clone()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		Checksum[] clones = new Checksum[TEST_CHECKSUMS.length];
		for (int i = 0; i < TEST_CHECKSUMS.length; i++) {
			TEST_CHECKSUMS[i].createResource(modelContainer);
			String oldValue = TEST_CHECKSUMS[i].getValue();
			clones[i] = TEST_CHECKSUMS[i].clone();
			assertEquals(TEST_CHECKSUMS[i].getAlgorithm(), clones[i].getAlgorithm());
			assertEquals(TEST_CHECKSUMS[i].getValue(), clones[i].getValue());
			TEST_CHECKSUMS[i].setValue(MD5_VALUE2);
			assertEquals(TEST_CHECKSUMS[i].getValue(), MD5_VALUE2);
			assertEquals(oldValue, clones[i].getValue());
		}
	}

}
