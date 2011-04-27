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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
public class TestSPDXReview {

	String[] REVIEWER = new String[] {"Person: Alg1", "Person: Alg2", "Person: Alg3"};
	DateFormat format = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
	String[] REVIEWDATE = new String[] {format.format(new Date()), format.format(new Date()),
			format.format(new Date())};
	String[] COMMENTS = new String[] {"", "comment1", null};
	SPDXReview[] TEST_REVIEWS;
	Model model;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		TEST_REVIEWS = new SPDXReview[REVIEWER.length];
		for (int i = 0; i < REVIEWER.length; i++) {
			TEST_REVIEWS[i] = new SPDXReview(REVIEWER[i], REVIEWDATE[i], COMMENTS[i]);
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
	 * Test method for {@link org.spdx.rdfparser.SPDXReview#createResource(com.hp.hpl.jena.rdf.model.Model)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testCreateResource() throws InvalidSPDXAnalysisException {
		Resource[] reviewerResources = new Resource[TEST_REVIEWS.length];
		for (int i = 0; i < reviewerResources.length; i++) {
			reviewerResources[i] = TEST_REVIEWS[i].createResource(model);
		}
		for (int i = 0;i < reviewerResources.length; i++) {
			SPDXReview comp = new SPDXReview(model, reviewerResources[i].asNode());
			assertEquals(TEST_REVIEWS[i].getReviewer(), comp.getReviewer());
			assertEquals(TEST_REVIEWS[i].getReviewDate(), comp.getReviewDate());
			assertEquals(TEST_REVIEWS[i].getComment(), comp.getComment());
			ArrayList<String> verify = comp.verify();
			assertEquals(0, verify.size());
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXReview#setReviewer(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetReviewer() throws InvalidSPDXAnalysisException {
		Resource[] reviewerResources = new Resource[TEST_REVIEWS.length];
		for (int i = 0; i < reviewerResources.length; i++) {
			reviewerResources[i] = TEST_REVIEWS[i].createResource(model);
		}
		String[] newReviewers = new String[REVIEWER.length];
		for (int i = 0; i < newReviewers.length; i++) {
			newReviewers[i] = REVIEWER[i] + "-New";
		}
		for (int i = 0;i < reviewerResources.length; i++) {
			SPDXReview comp = new SPDXReview(model, reviewerResources[i].asNode());
			comp.setReviewer(newReviewers[i]);
			assertEquals(newReviewers[i], comp.getReviewer());
			assertEquals(TEST_REVIEWS[i].getReviewDate(), comp.getReviewDate());
			assertEquals(TEST_REVIEWS[i].getComment(), comp.getComment());
			ArrayList<String> verify = comp.verify();
			assertEquals(0, verify.size());
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXReview#setReviewDate(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetReviewDate() throws InvalidSPDXAnalysisException {
		Resource[] reviewerResources = new Resource[TEST_REVIEWS.length];
		for (int i = 0; i < reviewerResources.length; i++) {
			reviewerResources[i] = TEST_REVIEWS[i].createResource(model);
		}
		String[] newReviewDates = new String[REVIEWDATE.length];
		for (int i = 0; i < newReviewDates.length; i++) {
			newReviewDates[i] = REVIEWDATE[i] + "-NEW";
		}
		for (int i = 0;i < reviewerResources.length; i++) {
			SPDXReview comp = new SPDXReview(model, reviewerResources[i].asNode());
			comp.setReviewDate(newReviewDates[i]);
			assertEquals(TEST_REVIEWS[i].getReviewer(), comp.getReviewer());
			assertEquals(newReviewDates[i], comp.getReviewDate());
			assertEquals(TEST_REVIEWS[i].getComment(), comp.getComment());
			ArrayList<String> verify = comp.verify();
			assertEquals(0, verify.size());
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXReview#setComment(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetComment() throws InvalidSPDXAnalysisException {
		Resource[] reviewerResources = new Resource[TEST_REVIEWS.length];
		for (int i = 0; i < reviewerResources.length; i++) {
			reviewerResources[i] = TEST_REVIEWS[i].createResource(model);
		}
		String[] newComments = new String[COMMENTS.length];
		newComments[0] = null;
		for (int i = 1; i < newComments.length; i++) {
			newComments[i] = "Comment "+String.valueOf(i);
		}
		for (int i = 0;i < reviewerResources.length; i++) {
			SPDXReview comp = new SPDXReview(model, reviewerResources[i].asNode());
			comp.setComment(newComments[i]);
			assertEquals(TEST_REVIEWS[i].getReviewer(), comp.getReviewer());
			assertEquals(TEST_REVIEWS[i].getReviewDate(), comp.getReviewDate());
			assertEquals(newComments[i], comp.getComment());
			ArrayList<String> verify = comp.verify();
			assertEquals(0, verify.size());
		}
	}

}
