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
 * @author Gary O'Neall
 *
 */
public class TestListedReferenceTypes {
	
	static final String[] LISTED_REFERENCE_TYPE_NAMES = new String[] {
		"cpe22Type","cpe23Type","maven-central","npm","nuget","bower","debian"
	};

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
		ListedReferenceTypes.resetListedReferenceTypes();
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.referencetype.ListedReferenceTypes#isListedReferenceType(java.net.URI)}.
	 * @throws URISyntaxException 
	 */
	@Test
	public void testIsListedReferenceType() throws URISyntaxException {
		for (String refName:LISTED_REFERENCE_TYPE_NAMES) {
			URI uri = new URI(SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX + refName);
			assertTrue(ListedReferenceTypes.getListedReferenceTypes().isListedReferenceType(uri));
		}
		URI wrongNamespace = new URI("http://wrong/"+LISTED_REFERENCE_TYPE_NAMES[0]);
		assertFalse(ListedReferenceTypes.getListedReferenceTypes().isListedReferenceType(wrongNamespace));
		URI notValidName = new URI(SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX+"wrong");
		assertFalse(ListedReferenceTypes.getListedReferenceTypes().isListedReferenceType(notValidName));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.referencetype.ListedReferenceTypes#getListedReferenceUri(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetListedReferenceUri() throws InvalidSPDXAnalysisException {
		URI result = ListedReferenceTypes.getListedReferenceTypes().getListedReferenceUri(LISTED_REFERENCE_TYPE_NAMES[0]);
		assertEquals(SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX + LISTED_REFERENCE_TYPE_NAMES[0], result.toString());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.referencetype.ListedReferenceTypes#getListedReferenceName(java.net.URI)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetListedReferenceName() throws URISyntaxException, InvalidSPDXAnalysisException {
		for (String refName:LISTED_REFERENCE_TYPE_NAMES) {
			URI uri = new URI(SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX + refName);
			assertEquals(refName, ListedReferenceTypes.getListedReferenceTypes().getListedReferenceName(uri));
		}
	}

}
