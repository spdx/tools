/**
 * Copyright (c) 2016 Source Auditor Inc.
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
package org.spdx.rdfparser.referencetype;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

/**
 * @author Gary
 *
 */
public class TestReferenceType {
	
	static final String REFERENCE_TYPE_TEST_URL1 = SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX + "cpe22Type";
	static final String REFERENCE_TYPE_TEST_URL2 = SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX + "maven";

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
	 * Test method for {@link org.spdx.rdfparser.referencetype.ReferenceType#setReferenceTypeUri(java.net.URI)}.
	 * @throws URISyntaxException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetReferenceTypeUri() throws InvalidSPDXAnalysisException, URISyntaxException {
		URI uri1 = new URI(REFERENCE_TYPE_TEST_URL1);
		ReferenceType ref = new ReferenceType(uri1, null, null, null);
		assertEquals(uri1, ref.getReferenceTypeUri());
		URI uri2 = new URI(REFERENCE_TYPE_TEST_URL2);
		ref.setReferenceTypeUri(uri2);
		assertEquals(uri2, ref.getReferenceTypeUri());
	}
	
	@Test
	public void testCompare() throws InvalidSPDXAnalysisException, URISyntaxException {
		URI uri1 = new URI(REFERENCE_TYPE_TEST_URL1);
		URI uri2 = new URI(REFERENCE_TYPE_TEST_URL2);
		ReferenceType ref1 = new ReferenceType(uri1, null, null, null);
		ReferenceType ref1_1 = new ReferenceType(uri1, null, null, null);
		ReferenceType ref2 = new ReferenceType(uri2, null, null, null);
		assertEquals(0, ref1.compareTo(ref1_1));
		assertTrue(ref1.compareTo(ref2) < 0);
		assertTrue(ref2.compareTo(ref1) > 0);
	}
	
	@Test
	public void testEquivalent() throws InvalidSPDXAnalysisException, URISyntaxException {
		URI uri1 = new URI(REFERENCE_TYPE_TEST_URL1);
		URI uri2 = new URI(REFERENCE_TYPE_TEST_URL2);
		ReferenceType ref1 = new ReferenceType(uri1, null, null, null);
		ReferenceType ref1_1 = new ReferenceType(uri1, null, null, null);
		ReferenceType ref2 = new ReferenceType(uri2, null, null, null);
		assertTrue(ref1.equivalent(ref1_1));
		assertFalse(ref2.equivalent(ref1));
	}

}
