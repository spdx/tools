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
	
	static final String ANNOTATOR1 = "Annotator1";
	static final String ANNOTATOR2 = "Annotator2";
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
		assertEquals(date, a.getDate());
		assertEquals(COMMENT1, a.getComment());
		Model model = ModelFactory.createDefaultModel();
		Resource r = a.createResource(model, null);
		Annotation copy = new Annotation(model, r.asNode());
		assertEquals(ANNOTATOR1, copy.getAnnotator());
		assertEquals(OTHER_ANNOTATION, copy.getAnnotationType());
		assertEquals(date, copy.getDate());
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
		a.setDate(null);
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
		assertEquals(date, a.getDate());
		assertEquals(COMMENT1, a.getComment());
		Model model = ModelFactory.createDefaultModel();
		Resource r = a.createResource(model, null);
		a.setAnnotationType(REVIEW_ANNOTATION);
		assertEquals(REVIEW_ANNOTATION, a.getAnnotationType());
		Annotation copy = new Annotation(model, r.asNode());
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
		assertEquals(date, a.getDate());
		assertEquals(COMMENT1, a.getComment());
		Model model = ModelFactory.createDefaultModel();
		Resource r = a.createResource(model, null);
		a.setAnnotator(ANNOTATOR2);
		assertEquals(ANNOTATOR2, a.getAnnotator());
		Annotation copy = new Annotation(model, r.asNode());
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
		assertEquals(date, a.getDate());
		assertEquals(COMMENT1, a.getComment());
		Model model = ModelFactory.createDefaultModel();
		Resource r = a.createResource(model, null);
		a.setComment(COMMENT2);
		assertEquals(COMMENT2, a.getComment());
		Annotation copy = new Annotation(model, r.asNode());
		assertEquals(COMMENT2, copy.getComment());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Annotation#setDate(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetDate() throws InvalidSPDXAnalysisException {
		Annotation a = new Annotation(ANNOTATOR1, OTHER_ANNOTATION, date, COMMENT1);
		assertEquals(ANNOTATOR1, a.getAnnotator());
		assertEquals(OTHER_ANNOTATION, a.getAnnotationType());
		assertEquals(date, a.getDate());
		assertEquals(COMMENT1, a.getComment());
		Model model = ModelFactory.createDefaultModel();
		Resource r = a.createResource(model, null);
		a.setDate(oldDate);
		assertEquals(oldDate, a.getDate());
		Annotation copy = new Annotation(model, r.asNode());
		assertEquals(oldDate, copy.getDate());
	}
}
