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
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary O'Neall
 *
 */
public class TestAnnotation {

	static final String ANNOTATOR1 = "Person: Annotator1";
	static final String ANNOTATOR2 = "Person: Annotator2";
	static final String COMMENT1 = "Comment1";
	static final String COMMENT2 = "Comment2";
	String date;
	String oldDate;
	static Annotation.AnnotationType REVIEW_ANNOTATION = Annotation.AnnotationType.annotationType_review;
	static Annotation.AnnotationType OTHER_ANNOTATION = Annotation.AnnotationType.annotationType_other;
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
		DateFormat format = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
		date = format.format(new Date());
		oldDate = format.format(new Date(10101));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Annotation#Annotation(java.lang.String, org.spdx.rdfparser.model.Annotation.AnnotationType, java.lang.String, java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testAnnotationStringAnnotationTypeStringString() throws InvalidSPDXAnalysisException {
		Annotation a = new Annotation(ANNOTATOR1, OTHER_ANNOTATION, date, COMMENT1);
		assertEquals(ANNOTATOR1, a.getAnnotator());
		assertEquals(OTHER_ANNOTATION, a.getAnnotationType());
		assertEquals(date, a.getAnnotationDate());
		assertEquals(COMMENT1, a.getComment());
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = a.createResource(modelContainer);
		Annotation copy = new Annotation(modelContainer, r.asNode());
		assertEquals(ANNOTATOR1, copy.getAnnotator());
		assertEquals(OTHER_ANNOTATION, copy.getAnnotationType());
		assertEquals(date, copy.getAnnotationDate());
		assertEquals(COMMENT1, copy.getComment());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Annotation#verify()}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		Annotation a = new Annotation(ANNOTATOR1, OTHER_ANNOTATION, date, COMMENT1);
		assertEquals(0, a.verify().size());
		a.setAnnotationType(null);
		a.setAnnotator(null);
		a.setAnnotationDate(null);
		a.setComment(null);
		assertEquals(4, a.verify().size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Annotation#setAnnotationType(org.spdx.rdfparser.model.Annotation.AnnotationType)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSetAnnotationType() throws InvalidSPDXAnalysisException {
		Annotation a = new Annotation(ANNOTATOR1, OTHER_ANNOTATION, date, COMMENT1);
		assertEquals(ANNOTATOR1, a.getAnnotator());
		assertEquals(OTHER_ANNOTATION, a.getAnnotationType());
		assertEquals(date, a.getAnnotationDate());
		assertEquals(COMMENT1, a.getComment());
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = a.createResource(modelContainer);
		a.setAnnotationType(REVIEW_ANNOTATION);
		assertEquals(REVIEW_ANNOTATION, a.getAnnotationType());
		Annotation copy = new Annotation(modelContainer, r.asNode());
		assertEquals(REVIEW_ANNOTATION, copy.getAnnotationType());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Annotation#setAnnotator(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSetAnnotator() throws InvalidSPDXAnalysisException {
		Annotation a = new Annotation(ANNOTATOR1, OTHER_ANNOTATION, date, COMMENT1);
		assertEquals(ANNOTATOR1, a.getAnnotator());
		assertEquals(OTHER_ANNOTATION, a.getAnnotationType());
		assertEquals(date, a.getAnnotationDate());
		assertEquals(COMMENT1, a.getComment());
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = a.createResource(modelContainer);
		a.setAnnotator(ANNOTATOR2);
		assertEquals(ANNOTATOR2, a.getAnnotator());
		Annotation copy = new Annotation(modelContainer, r.asNode());
		assertEquals(ANNOTATOR2, copy.getAnnotator());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Annotation#setComment(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSetComment() throws InvalidSPDXAnalysisException {
		Annotation a = new Annotation(ANNOTATOR1, OTHER_ANNOTATION, date, COMMENT1);
		assertEquals(ANNOTATOR1, a.getAnnotator());
		assertEquals(OTHER_ANNOTATION, a.getAnnotationType());
		assertEquals(date, a.getAnnotationDate());
		assertEquals(COMMENT1, a.getComment());
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = a.createResource(modelContainer);
		a.setComment(COMMENT2);
		assertEquals(COMMENT2, a.getComment());
		Annotation copy = new Annotation(modelContainer, r.asNode());
		assertEquals(COMMENT2, copy.getComment());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Annotation#setAnnotationDate(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSetDate() throws InvalidSPDXAnalysisException {
		Annotation a = new Annotation(ANNOTATOR1, OTHER_ANNOTATION, date, COMMENT1);
		assertEquals(ANNOTATOR1, a.getAnnotator());
		assertEquals(OTHER_ANNOTATION, a.getAnnotationType());
		assertEquals(date, a.getAnnotationDate());
		assertEquals(COMMENT1, a.getComment());
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = a.createResource(modelContainer);
		a.setAnnotationDate(oldDate);
		assertEquals(oldDate, a.getAnnotationDate());
		Annotation copy = new Annotation(modelContainer, r.asNode());
		assertEquals(oldDate, copy.getAnnotationDate());
	}

	@Test
	public void testEquivalent() throws InvalidSPDXAnalysisException {
		Annotation a1 = new Annotation(ANNOTATOR1, OTHER_ANNOTATION, date, COMMENT1);
		assertTrue(a1.equivalent(a1));
		Annotation a2 = new Annotation(ANNOTATOR1, OTHER_ANNOTATION, date, COMMENT1);
		assertTrue(a1.equivalent(a2));
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		a1.createResource(modelContainer);
		assertTrue(a1.equivalent(a2));
		// annotator
		a2.setAnnotator(ANNOTATOR2);
		assertFalse(a1.equivalent(a2));
		a2.setAnnotator(ANNOTATOR1);
		assertTrue(a2.equivalent(a1));
		// annotationType
		a2.setAnnotationType(REVIEW_ANNOTATION);
		assertFalse(a1.equivalent(a2));
		a2.setAnnotationType(OTHER_ANNOTATION);
		assertTrue(a2.equivalent(a1));
		// comment
		a2.setComment(COMMENT2);
		assertFalse(a1.equivalent(a2));
		a2.setComment(COMMENT1);
		assertTrue(a2.equivalent(a1));
		// date
		a2.setAnnotationDate(oldDate);
		assertFalse(a1.equivalent(a2));
		a2.setAnnotationDate(date);
		assertTrue(a2.equivalent(a1));
	}

	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		Annotation a1 = new Annotation(ANNOTATOR1, OTHER_ANNOTATION, date, COMMENT1);
		final Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		a1.createResource(modelContainer);
		Annotation a2 = a1.clone();
		assertEquals(a1.getAnnotationType(), a2.getAnnotationType());
		assertEquals(a1.getAnnotator(), a2.getAnnotator());
		assertEquals(a1.getComment(), a2.getComment());
		assertEquals(a1.getAnnotationDate(), a2.getAnnotationDate());
		assertTrue(a2.model == null);
	}
}
