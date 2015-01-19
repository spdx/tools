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
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
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
		@Override
		public boolean equals(Object o) {
			return o == this;
		}
		/* (non-Javadoc)
		 * @see org.spdx.rdfparser.model.RdfModelObject#hashCode()
		 */
		@Override
		public int hashCode() {
			return 0;
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
		IModelContainer modelContainer = new IModelContainer() {

			@Override
			public Model getModel() {
				return model;
			}

			@Override
			public String getDocumentNamespace() {
				return "http://testnamespace.com";
			}
			
		};
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
		IModelContainer modelContainer = new IModelContainer() {

			@Override
			public Model getModel() {
				return model;
			}

			@Override
			public String getDocumentNamespace() {
				return "http://testnamespace.com";
			}
			
		};
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
		IModelContainer modelContainer = new IModelContainer() {

			@Override
			public Model getModel() {
				return model;
			}

			@Override
			public String getDocumentNamespace() {
				return "http://testnamespace.com";
			}
			
		};
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
		IModelContainer modelContainer = new IModelContainer() {

			@Override
			public Model getModel() {
				return model;
			}

			@Override
			public String getDocumentNamespace() {
				return "http://testnamespace.com";
			}
			
		};
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
		IModelContainer modelContainer = new IModelContainer() {

			@Override
			public Model getModel() {
				return model;
			}

			@Override
			public String getDocumentNamespace() {
				return "http://testnamespace.com";
			}
			
		};
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
		IModelContainer modelContainer = new IModelContainer() {

			@Override
			public Model getModel() {
				return model;
			}

			@Override
			public String getDocumentNamespace() {
				return "http://testnamespace.com";
			}
			
		};
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
	public void testFindSetRelationshipPropertyValues() throws InvalidSPDXAnalysisException {
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new IModelContainer() {

			@Override
			public Model getModel() {
				return model;
			}

			@Override
			public String getDocumentNamespace() {
				return "http://testnamespace.com";
			}
			
		};
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
		IModelContainer modelContainer = new IModelContainer() {

			@Override
			public Model getModel() {
				return model;
			}

			@Override
			public String getDocumentNamespace() {
				return "http://testnamespace.com";
			}
			
		};
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
}
