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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;
import org.spdx.rdfparser.model.Relationship.RelationshipType;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author Gary
 *
 */
public class TestRdfModelObject {
	static final String TEST_VERIFY = "test verify";
	static final String TEST_TYPE = "TestTypeClass";
	static final String TEST_NAMESPACE = "http://TestNamespace/rdf#";
	static final String TEST_PROPNAME1 = "property1";
	static final String TEST_PROPNAME2 = "property2";
	static final String TEST_PROPVALUE1 = "value1";
	static final String TEST_PROPVALUE2 = "value2";
	static final String TEST_PROPVALUE3 = "value3";
	static final String TEST_PROPVALUE4 = "value4";
	static final String STANDARD_LICENSE_ID1 = "Apache-1.0";
	
	class EmptyRdfModelObject extends RdfModelObject {

		String uri = null;
		
		public EmptyRdfModelObject(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
			super(modelContainer, node);
		}
		/**
		 * 
		 */
		public EmptyRdfModelObject() {
			super();
		}
		/* (non-Javadoc)
		 * @see org.spdx.rdfparser.model.IRdfModel#verify()
		 */
		@Override
		public ArrayList<String> verify() {
			ArrayList<String> retval = new ArrayList<String>();
			retval.add(TEST_VERIFY);
			return retval;
		}

		/* (non-Javadoc)
		 * @see org.spdx.rdfparser.model.RdfModelObject#getType()
		 */
		@Override
		Resource getType(Model model) {
			return model.createResource(TEST_NAMESPACE + TEST_TYPE);
		}

		/* (non-Javadoc)
		 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
		 */
		@Override
		void populateModel() {
			// Just populate one of the properties
			this.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, TEST_PROPVALUE1);
		}
		
		public String findSinglePropertyValue(String namespace, String propertyName) {
			return super.findSinglePropertyValue(namespace, propertyName);
		}
		
		public String[] findMultiplePropertyValues(String namespace,String propertyName) {
			return super.findMultiplePropertyValues(namespace, propertyName);
		}
		
		public void setPropertyValue(String nameSpace, String propertyName,
				String[] values) {
			super.setPropertyValue(nameSpace, propertyName, values);
		}
		
		public void setPropertyValue(String nameSpace, String propertyName,
				String value) {
			super.setPropertyValue(nameSpace, propertyName, value);
		}
		/* (non-Javadoc)
		 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
		 */
		@Override
		String getUri(IModelContainer modelContainer) {
			return uri;
		}
		/**
		 * @param uRI
		 */
		public void setUri(String uri) {
			this.uri = uri;
		}
		/* (non-Javadoc)
		 * @see org.spdx.rdfparser.model.RdfModelObject#equals(java.lang.Object)
		 */

		/* (non-Javadoc)
		 * @see org.spdx.rdfparser.model.RdfModelObject#equivalent(org.spdx.rdfparser.model.RdfModelObject)
		 */
		@Override
		public boolean equivalent(IRdfModel compare) {

			return false;
		}
		/* (non-Javadoc)
		 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
		 */
		@Override
		void getPropertiesFromModel() {
			
		}
		
	}

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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.RdfModelObject#RdfModelObject(com.hp.hpl.jena.rdf.model.Model, com.hp.hpl.jena.graph.Node)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testRdfModelObjectModelNode() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Property p = model.createProperty(TEST_NAMESPACE, TEST_PROPNAME2);
		Resource r = model.createResource();
		r.addProperty(p, TEST_PROPVALUE1);
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		String result = empty.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME2);
		assertEquals(TEST_PROPVALUE1, result);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.RdfModelObject#createResource(com.hp.hpl.jena.rdf.model.Model, java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testCreateResource() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		EmptyRdfModelObject empty = new EmptyRdfModelObject();
		// Anon.
		String URI = "http://a/uri#r";
		empty.setUri(URI);
		Resource r = empty.createResource(modelContainer);
		assertTrue(r.isURIResource());
		Node p = model.getProperty(TEST_NAMESPACE, TEST_PROPNAME1).asNode();
		Triple m = Triple.createMatch(r.asNode(), p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		assertTrue(tripleIter.hasNext());
		Triple t = tripleIter.next();
		assertEquals(TEST_PROPVALUE1,t.getObject().toString(false));
		assertFalse(tripleIter.hasNext());
		// Anon
		empty.setUri(null);
		Resource anon = empty.createResource(modelContainer);
		assertFalse(anon.isURIResource());
		p = model.getProperty(TEST_NAMESPACE, TEST_PROPNAME1).asNode();
		m = Triple.createMatch(anon.asNode(), p, null);
		tripleIter = model.getGraph().find(m);	
		assertTrue(tripleIter.hasNext());
		t = tripleIter.next();
		assertEquals(TEST_PROPVALUE1,t.getObject().toString(false));
		assertFalse(tripleIter.hasNext());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.RdfModelObject#findSinglePropertyValue(java.lang.String, java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetFindSinglePropertyValue() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		String result = empty.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertTrue(result == null);
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, TEST_PROPVALUE1);
		result = empty.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(result, TEST_PROPVALUE1);
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, TEST_PROPVALUE2);
		result = empty.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(result, TEST_PROPVALUE2);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.RdfModelObject#findMultiplePropertyValues(java.lang.String, java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetFindMultipePropertyValues() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		String[] result = empty.findMultiplePropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(0, result.length);
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, new String[] {TEST_PROPVALUE1, TEST_PROPVALUE2});
		result = empty.findMultiplePropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(2, result.length);
		if (result[0].equals(TEST_PROPVALUE1)) {
			assertEquals(TEST_PROPVALUE2, result[1]);
		} else if (result[0].equals(TEST_PROPVALUE2)) {
			assertEquals(TEST_PROPVALUE1, result[1]);
		} else {
			fail("Wrong values");
		}
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, new String[] {TEST_PROPVALUE3, TEST_PROPVALUE4});
		result = empty.findMultiplePropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(2, result.length);
		if (result[0].equals(TEST_PROPVALUE3)) {
			assertEquals(TEST_PROPVALUE4, result[1]);
		} else if (result[0].equals(TEST_PROPVALUE4)) {
			assertEquals(TEST_PROPVALUE3, result[1]);
		} else {
			fail("Wrong values");
		}
	}
	
	@Test
	public void testFindSetAnnotationsPropertyValue() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		Annotation[] result = empty.findAnnotationPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(0, result.length);
		String annotator1 = "Annotator 1";
		AnnotationType annType1 = AnnotationType.annotationType_other;
		DateFormat format = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
		String annDate1 = format.format(new Date());
		String annComment1 = "Annotation Comment 1";
		Annotation an1 = new Annotation(annotator1, annType1, annDate1, annComment1);
		String annotator2 = "Annotator 2";
		AnnotationType annType2 = AnnotationType.annotationType_review;
		String annDate2 = format.format(new Date(10101));
		String annComment2 = "Annotation Comment 2";
		Annotation an2 = new Annotation(annotator2, annType2, annDate2, annComment2);
		empty.setPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1, new Annotation[] {an1});
		result = empty.findAnnotationPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(1, result.length);
		assertEquals(an1, result[0]);
		empty.setPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1, new Annotation[] {an1, an2});
		result = empty.findAnnotationPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(2, result.length);
		if (result[0].equals(an1)) {
			assertEquals(result[1], an2);
		} else {
			assertEquals(result[0], an2);
		}
		empty.setPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1, new Annotation[] {});
		result = empty.findAnnotationPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(0, result.length);
	}
	
	@Test
	public void testFindSetElementsPropertyValue() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		SpdxElement result = empty.findElementPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertTrue(result == null);
		String elementName1 = "element name 1";
		String elementComment1 = "element comment 1";
		SpdxElement element1 = new SpdxElement(elementName1, elementComment1, null, null);
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, element1);
		result = empty.findElementPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(element1, result);
		String elementName2 = "element name 2";
		String elementComment2 = "element comment 2";
		SpdxElement element2 = new SpdxElement(elementName2, elementComment2, null, null);
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, element2);
		result = empty.findElementPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(element2, result);
	}
	
	@Test
	public void testAddElementsPropertyValue() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		SpdxElement[] result = empty.findMultipleElementPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(0, result.length);
		String elementName1 = "element name 1";
		String elementComment1 = "element comment 1";
		SpdxElement element1 = new SpdxElement(elementName1, elementComment1, null, null);
		empty.addPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, element1);
		result = empty.findMultipleElementPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(1, result.length);
		assertEquals(element1, result[0]);
		String elementName2 = "element name 2";
		String elementComment2 = "element comment 2";
		SpdxElement element2 = new SpdxElement(elementName2, elementComment2, null, null);
		empty.addPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, element2);
		result = empty.findMultipleElementPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(2, result.length);
		assertTrue(UnitTestHelper.isArraysEquivalent(new SpdxElement[] {element1,  element2}, result));
	}

	@Test
	public void testFindSetRelationshipPropertyValues() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		Relationship[] result = empty.findRelationshipPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(0, result.length);
		String elementName1 = "element name 1";
		String elementComment1 = "element comment 1";
		SpdxElement element1 = new SpdxElement(elementName1, elementComment1, null, null);
		RelationshipType relType1 = RelationshipType.relationshipType_buildToolOf;
		String relComment1 = "Relationship Comment 1";
		Relationship relationship1 = new Relationship(element1, relType1, relComment1);
		empty.setPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1, new Relationship[] {relationship1});
		result = empty.findRelationshipPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(1, result.length);
		assertEquals(relationship1, result[0]);
		String elementName2 = "element name 2";
		String elementComment2 = "element comment 2";
		SpdxElement element2 = new SpdxElement(elementName2, elementComment2, null, null);
		RelationshipType relType2 = RelationshipType.relationshipType_documentation;
		String relComment2 = "Relationship Comment 2";
		Relationship relationship2 = new Relationship(element2, relType2, relComment2);
		empty.setPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1, new Relationship[] {relationship1, relationship2});
		result = empty.findRelationshipPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(2, result.length);
		if (result[0].equals(relationship1)) {
			assertEquals(relationship2, result[1]);
		} else {
			assertEquals(relationship2, result[0]);
		}
		empty.setPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1, new Relationship[] {});
		result = empty.findRelationshipPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(0, result.length);
	}
	
	@Test
	public void testFindSetAnyLicenseInfos() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		AnyLicenseInfo result = empty.findAnyLicenseInfoPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertTrue(result == null);
		SpdxListedLicense lic1 = LicenseInfoFactory.getListedLicenseById(STANDARD_LICENSE_ID1);
		String licId2 = "LicRef-2";
		String licenseText2 = "License text 2";
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(licId2, licenseText2);
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, lic1);
		result = empty.findAnyLicenseInfoPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(lic1, result);
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, lic2);
		result = empty.findAnyLicenseInfoPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(lic2, result);
	}
	
	@Test
	public void testAddAnyLicenseInfos() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		AnyLicenseInfo[] result = empty.findAnyLicenseInfoPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(0, result.length);
		SpdxListedLicense lic1 = LicenseInfoFactory.getListedLicenseById(STANDARD_LICENSE_ID1);
		String licId2 = "LicRef-2";
		String licenseText2 = "License text 2";
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(licId2, licenseText2);
		empty.addPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, lic1);
		result = empty.findAnyLicenseInfoPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(1, result.length);
		assertEquals(lic1, result[0]);
		empty.addPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, lic2);
		result = empty.findAnyLicenseInfoPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(2, result.length);
		assertTrue(UnitTestHelper.isArraysEqual(new AnyLicenseInfo[] {lic1, lic2},
				result));
	}
	
	@Test
	public void testFindSetAnyLicenseInfosMultiple() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		AnyLicenseInfo[] result = empty.findAnyLicenseInfoPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(0, result.length);
		SpdxListedLicense lic1 = LicenseInfoFactory.getListedLicenseById(STANDARD_LICENSE_ID1);
		String licId2 = "LicRef-2";
		String licenseText2 = "License text 2";
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(licId2, licenseText2);
		AnyLicenseInfo[] licenses = new AnyLicenseInfo[] {lic1, lic2};
		empty.setPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1, licenses);
		result = empty.findAnyLicenseInfoPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(2, result.length);
		if (result[0].equals(lic1)) {
			assertEquals(lic2, result[1]);
		} else {
			assertEquals(lic1, result[1]);
		}
	}
	
	@Test
	public void testSpecialValues() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		// None
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, SpdxRdfConstants.NONE_VALUE);
		String result = empty.findUriPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(SpdxRdfConstants.URI_VALUE_NONE, result);
		result = empty.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(SpdxRdfConstants.NONE_VALUE, result);
		// NoAssertion
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME2, SpdxRdfConstants.NOASSERTION_VALUE);
		result = empty.findUriPropertyValue(TEST_NAMESPACE, TEST_PROPNAME2);
		assertEquals(SpdxRdfConstants.URI_VALUE_NOASSERTION, result);
		result = empty.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME2);
		assertEquals(SpdxRdfConstants.NOASSERTION_VALUE, result);
	}
	
	@Test
	public void testFindSetPropertyUriValue() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		String uri = "http://this.is.a#uri";
		empty.setPropertyUriValue(TEST_NAMESPACE, TEST_PROPNAME1, uri);
		String result = empty.findUriPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(uri, result);
	}
	
	@Test
	public void testFindSetPropertyUriValues() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		String uri1 = "http://this.is.a#uri";
		String uri2 = "http://this.is.a#uri2";
		String[] uris = new String[] {uri1, uri2};
		empty.setPropertyUriValues(TEST_NAMESPACE, TEST_PROPNAME1, uris);
		String[] result = empty.findUriPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertTrue(UnitTestHelper.isArraysEqual(uris, result));
	}
	
	@Test
	public void testAddSetPropertyUriValues() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		String uri1 = "http://this.is.a#uri";
		String uri2 = "http://this.is.a#uri2";
		String[] uris = new String[] {uri1, uri2};
		String[] result = empty.findUriPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(0, result.length);
		empty.addPropertyUriValue(TEST_NAMESPACE, TEST_PROPNAME1, uri1);
		result = empty.findUriPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(1, result.length);
		assertEquals(uri1, result[0]);
		empty.addPropertyUriValue(TEST_NAMESPACE, TEST_PROPNAME1, uri2);
		result = empty.findUriPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertTrue(UnitTestHelper.isArraysEqual(uris, result));
	}
	
	@Test
	public void testFindSetPropertyDaopValue() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		DoapProject p1 = new DoapProject("Name1", "http://home.page/one");
		DoapProject p2 = new DoapProject("Name2", "http://home.page/two");
		p2.createResource(modelContainer);
		DoapProject[] projects = new DoapProject[] {p1, p2};
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, projects);
		DoapProject[] result = empty.findMultipleDoapPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertTrue(UnitTestHelper.isArraysEqual(projects, result));
	}
	
	@Test
	public void testFindSetPropertyChecksumValue() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		Checksum c1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
				"1123456789abcdef0123456789abcdef01234567");
		Checksum c2 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_md5, 
				"2123456789abcdef0123456789abcdef01234567");
		c2.createResource(modelContainer);
		Checksum result = empty.findChecksumPropertyValue(TEST_NAMESPACE, TEST_PROPNAME2);
		assertTrue(result == null);
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME2, c1);
		result = empty.findChecksumPropertyValue(TEST_NAMESPACE, TEST_PROPNAME2);
		assertEquals(c1, result);
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME2, c2);
		result = empty.findChecksumPropertyValue(TEST_NAMESPACE, TEST_PROPNAME2);
		assertEquals(c2, result);
	}
	
	@Test
	public void testAddPropertyChecksumValue() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		Checksum c1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
				"1123456789abcdef0123456789abcdef01234567");
		Checksum c2 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_md5, 
				"2123456789abcdef0123456789abcdef01234567");
		c2.createResource(modelContainer);
		Checksum[] result = empty.findMultipleChecksumPropertyValues(TEST_NAMESPACE, TEST_PROPNAME2);
		assertEquals(0, result.length);
		empty.addPropertyValue(TEST_NAMESPACE, TEST_PROPNAME2, c1);
		result = empty.findMultipleChecksumPropertyValues(TEST_NAMESPACE, TEST_PROPNAME2);
		assertEquals(1, result.length);
		assertEquals(c1, result[0]);
		empty.addPropertyValue(TEST_NAMESPACE, TEST_PROPNAME2, c2);
		result = empty.findMultipleChecksumPropertyValues(TEST_NAMESPACE, TEST_PROPNAME2);
		assertEquals(2, result.length);
		assertTrue(UnitTestHelper.isArraysEquivalent(new Checksum[] {c1, c2}, result));
	}
	
	@Test
	public void testDuplicate() throws InvalidSPDXAnalysisException {
		// Same URI node
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		EmptyRdfModelObject empty = new EmptyRdfModelObject();
		String uri = "http://a.uri.this/that#mine";
		empty.setUri(uri);
		Resource r = empty.createResource(modelContainer);
		assertTrue(r.isURIResource());
		assertEquals(uri, r.getURI());
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME2, TEST_PROPVALUE2);
		String result = empty.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME2);
		assertEquals(TEST_PROPVALUE2, result);
		EmptyRdfModelObject empty2 = new EmptyRdfModelObject();
		Resource r2 = empty2.createResource(modelContainer);
		assertFalse(r2.isURIResource());
		result = empty2.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME2);
		assertTrue(result == null);
		EmptyRdfModelObject empty3 = new EmptyRdfModelObject();
		empty3.setUri(uri);	// this should cause the resource to reference the same
		Resource r3 = empty3.createResource(modelContainer);
		assertTrue(r3.isURIResource());
		assertEquals(uri, r3.getURI());
		result = empty3.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME2);
		assertEquals(TEST_PROPVALUE2, result);
		assertEquals(r, r3);	
		EmptyRdfModelObject empty4 = new EmptyRdfModelObject();
		String uri2 = "http://another.uri.this/that#mine";
		empty4.setUri(uri2);
		Resource r4 = empty4.createResource(modelContainer);
		assertTrue(r4.isURIResource());
		assertEquals(uri2, r4.getURI());
		result = empty4.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME2);
		assertTrue(result == null);

	}
	
	@Test public void testEquals() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		EmptyRdfModelObject empty = new EmptyRdfModelObject();
		String uri = "http://a.uri.this/that#mine";
		empty.setUri(uri);
		empty.createResource(modelContainer);
		EmptyRdfModelObject empty2 = new EmptyRdfModelObject();
		Resource r2 = empty2.createResource(modelContainer);
		assertFalse(empty.equals(empty2));
		EmptyRdfModelObject empty3 = new EmptyRdfModelObject();
		assertFalse(empty.equals(empty3));
		empty3.setUri(uri);
		empty3.createResource(modelContainer);
		assertTrue(empty.equals(empty3));
		EmptyRdfModelObject empty4 = new EmptyRdfModelObject(modelContainer, r2.asNode());
		assertTrue(empty2.equals(empty4));
	}
	
	@Test 
	public void testHashcode() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");

		EmptyRdfModelObject empty = new EmptyRdfModelObject();
		String uri = "http://a.uri#mine";
		empty.setUri(uri);
		empty.createResource(modelContainer);
		EmptyRdfModelObject empty2 = new EmptyRdfModelObject();
		Resource r2 = empty2.createResource(modelContainer);
		assertFalse(empty.hashCode() == empty2.hashCode());
		EmptyRdfModelObject empty3 = new EmptyRdfModelObject();
		assertFalse(empty.hashCode() == empty3.hashCode());
		empty3.setUri(uri);
		empty3.createResource(modelContainer);
		assertTrue(empty.hashCode() == empty3.hashCode());
		EmptyRdfModelObject empty4 = new EmptyRdfModelObject(modelContainer, r2.asNode());
		assertTrue(empty2.hashCode() == empty4.hashCode());
	}
	
	@Test
	public void testFindSetSpdxCreatorInfo() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		SPDXCreatorInformation creatorInfo = new SPDXCreatorInformation(
				new String[] {"PERSON: me", "TOOL: TEst"}, "2011-03-13T00:00:00Z", 
				"Comment", "listVersion");
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, creatorInfo);
		SPDXCreatorInformation result = empty.findCreationInfoPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(creatorInfo, result);
	}
	
	@Test
	public void testFindSetSpdxReviewers() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		SPDXReview rev1 = new SPDXReview("Person: Gary", "2011-03-13T00:00:00Z", "Comment1");
		SPDXReview rev2 = new SPDXReview("Tool: test", "2011-03-13T00:00:00Z", "Comment2");
		SPDXReview rev3 = new SPDXReview("Organization: SPDX", "2011-03-13T00:00:00Z", "Comment2");
		SPDXReview[] reviewers = new SPDXReview[] {rev1, rev2, rev3};
		empty.setPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1, reviewers);
		SPDXReview[] result = empty.findReviewPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertTrue(UnitTestHelper.isArraysEqual(reviewers, result));
	}	
	@Test
	public void testFindSetExternalDocReferences() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());
		Checksum c1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
				"1123456789abcdef0123456789abcdef01234567");
		Checksum c2 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
				"2123456789abcdef0123456789abcdef01234567");
		Checksum c3 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
				"3333456789abcdef0123456789abcdef01234567");
		String docUri1 = "http://spdx.org/docs/my/doc1";
		String docUri2 = "http://spdx.org/docs/my/doc2";
		String docUri3 = "http://spdx.org/docs/my/doc3";
		String docId1 = "DocumentRef-1";
		String docId2 = "DocumentRef-2";
		String docId3 = "DocumentRef-3";
		ExternalDocumentRef ref1 = new ExternalDocumentRef(docUri1, c1, docId1);
		ExternalDocumentRef ref2 = new ExternalDocumentRef(docUri2, c2, docId2);
		ExternalDocumentRef ref3 = new ExternalDocumentRef(docUri3, c3, docId3);
		ExternalDocumentRef[] refs = new ExternalDocumentRef[] {ref1, ref2, ref3};
		empty.setPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1, refs);
		ExternalDocumentRef[] result = empty.findExternalDocRefPropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertTrue(UnitTestHelper.isArraysEqual(refs, result));
	}
	
	@Test
	public void testFindSetVerifiationCode() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(modelContainer, r.asNode());

		SpdxPackageVerificationCode ver = new SpdxPackageVerificationCode("2123456789abcdef0123456789abcdef01234567", new String[] {"skippedfiles1", "skippedfile2"});
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, ver);
		SpdxPackageVerificationCode result = empty.findVerificationCodePropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(ver.getValue(), result.getValue());
		assertTrue(UnitTestHelper.isArraysEqual(ver.getExcludedFileNames(), 
				result.getExcludedFileNames()));
	}
}
