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
public class TestConjunctiveLicenseSet {
	
	String[] IDS = new String[] {"LicenseRef-id1", "LicenseRef-id2", "LicenseRef-id3", "LicenseRef-id4"};
	String[] TEXTS = new String[] {"text1", "text2", "text3", "text4"};
	SPDXNonStandardLicense[] NON_STD_LICENSES;
	Model model;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		NON_STD_LICENSES = new SPDXNonStandardLicense[IDS.length];
		for (int i = 0; i < IDS.length; i++) {
			NON_STD_LICENSES[i] = new SPDXNonStandardLicense(IDS[i], TEXTS[i]);
		}
		model = ModelFactory.createDefaultModel();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	@Test
	public void testCreateConjunctive() throws InvalidSPDXAnalysisException {
		SPDXConjunctiveLicenseSet cls = new SPDXConjunctiveLicenseSet(NON_STD_LICENSES);
		Resource clsResource = cls.createResource(model);
		SPDXConjunctiveLicenseSet cls2 = new SPDXConjunctiveLicenseSet(model, clsResource.asNode());
		assertTrue(cls.equals(cls2));
		ArrayList<String> verify = cls2.verify();
		assertEquals(0, verify.size());
		verify = cls.verify();
		assertEquals(0, verify.size());
	}
	
	@Test
	public void testCreateDisjunctive() throws InvalidSPDXAnalysisException {
		SPDXDisjunctiveLicenseSet cls = new SPDXDisjunctiveLicenseSet(NON_STD_LICENSES);
		Resource clsResource = cls.createResource(model);
		SPDXDisjunctiveLicenseSet cls2 = new SPDXDisjunctiveLicenseSet(model, clsResource.asNode());
		assertTrue(cls.equals(cls2));
		ArrayList<String> verify = cls2.verify();
		assertEquals(0, verify.size());
		verify = cls.verify();
		assertEquals(0, verify.size());
	}
	
	@Test
	public void testCloneConjunctive() throws InvalidSPDXAnalysisException {
		SPDXConjunctiveLicenseSet cls = new SPDXConjunctiveLicenseSet(NON_STD_LICENSES);
		@SuppressWarnings("unused")
		Resource clsResource = cls.createResource(model);
		SPDXConjunctiveLicenseSet cls2 = (SPDXConjunctiveLicenseSet)cls.clone();
		assertTrue(cls.equals(cls2));
		ArrayList<String> verify = cls2.verify();
		assertEquals(0, verify.size());
		verify = cls.verify();
		assertEquals(0, verify.size());
		assertTrue(cls2.getResource() == null);
	}
	
	@Test
	public void testCloneDisjunctive() throws InvalidSPDXAnalysisException {
		SPDXDisjunctiveLicenseSet cls = new SPDXDisjunctiveLicenseSet(NON_STD_LICENSES);
		@SuppressWarnings("unused")
		Resource clsResource = cls.createResource(model);
		SPDXDisjunctiveLicenseSet cls2 = (SPDXDisjunctiveLicenseSet)cls.clone();
		assertTrue(cls.equals(cls2));
		ArrayList<String> verify = cls2.verify();
		assertEquals(0, verify.size());
		verify = cls.verify();
		assertEquals(0, verify.size());
		assertTrue(cls2.getResource() == null);
	}
}
