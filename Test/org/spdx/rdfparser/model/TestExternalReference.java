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
package org.spdx.rdfparser.model;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.ExternalRef.ReferenceCategory;
import org.spdx.rdfparser.referencetype.ListedReferenceTypes;
import org.spdx.rdfparser.referencetype.ReferenceType;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Test cases for ExternalReference
 * @author Gary O'Neall
 *
 */
public class TestExternalReference {
	private static final String[] REFERENCE_LOCATORS = new String[] {
		"org.apache.tomcat:tomcat:9.0.0.M4", "Microsoft.AspNet.MVC/5.0.0",
		"cpe:2.3:o:canonical:ubuntu_linux:10.04::lts:*:*:*:*:*"
	};

	private static final String[] COMMENTS = new String[] {
		"comment one", "comment two", ""
	};

	private static final String[] REFERENCE_TYPE_NAMES = new String[] {
		"maven-central", "nuget", "cpe23Type"
	};

	ReferenceCategory[] REFERENCE_CATEGORIES = {ReferenceCategory.referenceCategory_packageManager,
			ReferenceCategory.referenceCategory_packageManager,
			ReferenceCategory.referenceCategory_security
	};

	ExternalRef[] TEST_REFERENCES;
	Model model;
	IModelContainer modelContainer;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.model = ModelFactory.createDefaultModel();
		modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		TEST_REFERENCES = new ExternalRef[REFERENCE_CATEGORIES.length];
		for (int i = 0; i < REFERENCE_CATEGORIES.length; i++) {
			TEST_REFERENCES[i] = new ExternalRef(REFERENCE_CATEGORIES[i], 
					new ReferenceType(new URI(SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX + REFERENCE_TYPE_NAMES[i]), null, null, null),
					REFERENCE_LOCATORS[i], COMMENTS[i]);
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalRef#getPropertiesFromModel()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetPropertiesFromModel() throws InvalidSPDXAnalysisException {
		Resource r = this.TEST_REFERENCES[0].createResource(modelContainer);
		ExternalRef externalRef = new ExternalRef(modelContainer, r.asNode());
		assertEquals(COMMENTS[0], externalRef.getComment());
		assertEquals(REFERENCE_CATEGORIES[0], externalRef.getReferenceCategory());
		assertEquals(REFERENCE_LOCATORS[0], externalRef.getReferenceLocator());
		assertEquals(REFERENCE_TYPE_NAMES[0], 
				ListedReferenceTypes.getListedReferenceTypes().getListedReferenceName(externalRef.getReferenceType().getReferenceTypeUri()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalRef#findDuplicateResource(org.spdx.rdfparser.IModelContainer, java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testFindDuplicateResource() throws InvalidSPDXAnalysisException {
		Resource r = this.TEST_REFERENCES[0].createResource(modelContainer);
		Node referenceLocatorProperty = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_REFERENCE_LOCATOR).asNode();
		Triple referenceLocatorMatch = Triple.createMatch(null, referenceLocatorProperty, null);
		ExtendedIterator<Triple> referenceMatchIter = model.getGraph().find(referenceLocatorMatch);	
		int numExternalRefs = 0;
		while (referenceMatchIter.hasNext()) {
			referenceMatchIter.next();
			numExternalRefs++;
		}
		assertEquals(1, numExternalRefs);
		ExternalRef clonedExtRef = this.TEST_REFERENCES[0].clone();
		Resource r2 = clonedExtRef.createResource(modelContainer);
		assertEquals(r, r2);
		referenceLocatorProperty = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_REFERENCE_LOCATOR).asNode();
		referenceLocatorMatch = Triple.createMatch(null, referenceLocatorProperty, null);
		referenceMatchIter = model.getGraph().find(referenceLocatorMatch);	
		numExternalRefs = 0;
		while (referenceMatchIter.hasNext()) {
			referenceMatchIter.next();
			numExternalRefs++;
		}
		assertEquals(1, numExternalRefs);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalRef#getUri(org.spdx.rdfparser.IModelContainer)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetUri() throws InvalidSPDXAnalysisException {
		this.TEST_REFERENCES[0].createResource(modelContainer);
		assertTrue(this.TEST_REFERENCES[0].getUri(modelContainer) == null);	// uses anon. nodes
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalRef#getType(com.hp.hpl.jena.rdf.model.Model)}.
	 */
	@Test
	public void testGetType() throws InvalidSPDXAnalysisException {
		this.TEST_REFERENCES[0].createResource(modelContainer);
		assertEquals(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_EXTERNAL_REFERENCE,
				this.TEST_REFERENCES[0].getType(model).getURI());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalRef#verify()}.
	 * @throws URISyntaxException 
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException, URISyntaxException {
		for (int i = 0; i < TEST_REFERENCES.length; i++) {
			assertEquals(0, TEST_REFERENCES[i].verify().size());
		}
		ExternalRef noCategory = new ExternalRef(null, 
				new ReferenceType(new URI(SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX + REFERENCE_TYPE_NAMES[0]), null, null, null),
				REFERENCE_LOCATORS[0], COMMENTS[0]);
		assertEquals(1, noCategory.verify().size());
		ExternalRef noReferenceType = new ExternalRef(REFERENCE_CATEGORIES[0], 
				null,
				REFERENCE_LOCATORS[0], COMMENTS[0]);
		assertEquals(1, noReferenceType.verify().size());
		ExternalRef nonListedReferenceType = new ExternalRef(REFERENCE_CATEGORIES[0], 
				new ReferenceType(new URI(SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX + "NotListed"), null, null, null),
				REFERENCE_LOCATORS[0], COMMENTS[0]);
		assertEquals(1, nonListedReferenceType.verify().size());
		ExternalRef noRferenceLocator = new ExternalRef(REFERENCE_CATEGORIES[0], 
				new ReferenceType(new URI(SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX + REFERENCE_TYPE_NAMES[0]), null, null, null),
				null, COMMENTS[0]);
		assertEquals(1, noRferenceLocator.verify().size());
		ExternalRef noComment = new ExternalRef(REFERENCE_CATEGORIES[0], 
				new ReferenceType(new URI(SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX + REFERENCE_TYPE_NAMES[0]), null, null, null),
				REFERENCE_LOCATORS[0], null);
		assertEquals(0, noComment.verify().size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalRef#equivalent(org.spdx.rdfparser.model.IRdfModel)}.
	 */
	@Test
	public void testEquivalent() throws InvalidSPDXAnalysisException {
		ExternalRef clone = TEST_REFERENCES[1].clone();
		assertTrue(clone.equivalent(TEST_REFERENCES[1]));
		assertFalse(TEST_REFERENCES[0].equivalent(TEST_REFERENCES[1]));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalRef#compareTo(org.spdx.rdfparser.model.ExternalRef)}.
	 */
	@Test
	public void testCompareTo() throws InvalidSPDXAnalysisException {
		ExternalRef clone = TEST_REFERENCES[2].clone();
		assertEquals(0, clone.compareTo(TEST_REFERENCES[2]));
		assertTrue(TEST_REFERENCES[0].compareTo(TEST_REFERENCES[1]) < 0);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalRef#clone()}.
	 */
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		ExternalRef clone = TEST_REFERENCES[1].clone();
		assertEquals(clone.getComment(), TEST_REFERENCES[1].getComment());
		assertEquals(clone.getReferenceCategory(), TEST_REFERENCES[1].getReferenceCategory());
		assertEquals(clone.getReferenceLocator(), TEST_REFERENCES[1].getReferenceLocator());
		assertEquals(clone.getReferenceType(), TEST_REFERENCES[1].getReferenceType());
		TEST_REFERENCES[1].createResource(modelContainer);
		assertEquals(clone.getComment(), TEST_REFERENCES[1].getComment());
		assertEquals(clone.getReferenceCategory(), TEST_REFERENCES[1].getReferenceCategory());
		assertEquals(clone.getReferenceLocator(), TEST_REFERENCES[1].getReferenceLocator());
		assertEquals(clone.getReferenceType(), TEST_REFERENCES[1].getReferenceType());
		clone.createResource(modelContainer);
		assertEquals(clone.getComment(), TEST_REFERENCES[1].getComment());
		assertEquals(clone.getReferenceCategory(), TEST_REFERENCES[1].getReferenceCategory());
		assertEquals(clone.getReferenceLocator(), TEST_REFERENCES[1].getReferenceLocator());
		assertEquals(clone.getReferenceType(), TEST_REFERENCES[1].getReferenceType());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalRef#setReferenceCategory(org.spdx.rdfparser.model.ExternalRef.ReferenceCategory)}.
	 */
	@Test
	public void testSetReferenceCategory() throws InvalidSPDXAnalysisException {
		Resource[] externalRefResources = new Resource[TEST_REFERENCES.length];
		for (int i = 0; i < TEST_REFERENCES.length; i++) {
			externalRefResources[i] = TEST_REFERENCES[i].createResource(modelContainer);
		}
		ReferenceCategory[] changedCategories = new ReferenceCategory[] {
			REFERENCE_CATEGORIES[1], REFERENCE_CATEGORIES[2], REFERENCE_CATEGORIES[0]
		};
		for (int i = 0; i < TEST_REFERENCES.length; i++) {
			ExternalRef comp = new ExternalRef(modelContainer, externalRefResources[i].asNode());
			assertEquals(REFERENCE_CATEGORIES[i], comp.getReferenceCategory());
			assertEquals(REFERENCE_CATEGORIES[i], TEST_REFERENCES[i].getReferenceCategory());
			TEST_REFERENCES[i].setReferenceCategory(changedCategories[i]);
			assertEquals(changedCategories[i], comp.getReferenceCategory());
			assertEquals(changedCategories[i], TEST_REFERENCES[i].getReferenceCategory());
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalRef#setReferenceType(java.lang.String)}.
	 * @throws URISyntaxException 
	 */
	@Test
	public void testSetReferenceType() throws InvalidSPDXAnalysisException, URISyntaxException {
		Resource[] externalRefResources = new Resource[TEST_REFERENCES.length];
		for (int i = 0; i < TEST_REFERENCES.length; i++) {
			externalRefResources[i] = TEST_REFERENCES[i].createResource(modelContainer);
		}
		ReferenceType[] changedTypes = new ReferenceType[] {
			new ReferenceType(new URI(SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX + REFERENCE_TYPE_NAMES[1]), null, null, null),
			new ReferenceType(new URI(SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX + REFERENCE_TYPE_NAMES[2]), null, null, null),
			new ReferenceType(new URI(SpdxRdfConstants.SPDX_LISTED_REFERENCE_TYPES_PREFIX + REFERENCE_TYPE_NAMES[0]), null, null, null)
		};
		for (int i = 0; i < TEST_REFERENCES.length; i++) {
			ExternalRef comp = new ExternalRef(modelContainer, externalRefResources[i].asNode());
			assertEquals(REFERENCE_TYPE_NAMES[i], ListedReferenceTypes.getListedReferenceTypes().getListedReferenceName(comp.getReferenceType().getReferenceTypeUri()));
			assertEquals(REFERENCE_TYPE_NAMES[i], ListedReferenceTypes.getListedReferenceTypes().getListedReferenceName(TEST_REFERENCES[i].getReferenceType().getReferenceTypeUri()));
			TEST_REFERENCES[i].setReferenceType(changedTypes[i]);
			assertEquals(changedTypes[i].getReferenceTypeUri(), comp.getReferenceType().getReferenceTypeUri());
			assertEquals(changedTypes[i].getReferenceTypeUri(), TEST_REFERENCES[i].getReferenceType().getReferenceTypeUri());
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalRef#setReferenceLocator(java.lang.String)}.
	 */
	@Test
	public void testSetReferenceLocator() throws InvalidSPDXAnalysisException {
		Resource[] externalRefResources = new Resource[TEST_REFERENCES.length];
		for (int i = 0; i < TEST_REFERENCES.length; i++) {
			externalRefResources[i] = TEST_REFERENCES[i].createResource(modelContainer);
		}
		String[] changedLocators = new String[] {
			"changed1", "changed2", "changed3"
		};
		for (int i = 0; i < TEST_REFERENCES.length; i++) {
			ExternalRef comp = new ExternalRef(modelContainer, externalRefResources[i].asNode());
			assertEquals(REFERENCE_LOCATORS[i], comp.getReferenceLocator());
			assertEquals(REFERENCE_LOCATORS[i], TEST_REFERENCES[i].getReferenceLocator());
			TEST_REFERENCES[i].setReferenceLocator(changedLocators[i]);
			assertEquals(changedLocators[i], comp.getReferenceLocator());
			assertEquals(changedLocators[i], TEST_REFERENCES[i].getReferenceLocator());
		}
	}
	
	@Test
	public void testSetComment() throws InvalidSPDXAnalysisException {
		Resource[] externalRefResources = new Resource[TEST_REFERENCES.length];
		for (int i = 0; i < TEST_REFERENCES.length; i++) {
			externalRefResources[i] = TEST_REFERENCES[i].createResource(modelContainer);
		}
		String[] changedComments = new String[] {
			"changed1", "changed2", "changed3"
		};
		for (int i = 0; i < TEST_REFERENCES.length; i++) {
			ExternalRef comp = new ExternalRef(modelContainer, externalRefResources[i].asNode());
			assertEquals(COMMENTS[i], comp.getComment());
			assertEquals(COMMENTS[i], TEST_REFERENCES[i].getComment());
			TEST_REFERENCES[i].setComment(changedComments[i]);
			assertEquals(changedComments[i], comp.getComment());
			assertEquals(changedComments[i], TEST_REFERENCES[i].getComment());
		}
	}

}
