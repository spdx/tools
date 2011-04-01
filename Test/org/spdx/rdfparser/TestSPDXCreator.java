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
public class TestSPDXCreator {
	String[] NAMES = new String[] {"Name1", "Name2", "Name3"};
	String[] COMMENTS = new String[] {"", "Value2", null};
	SPDXCreator[] TEST_CREATORS;
	Model model;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		TEST_CREATORS = new SPDXCreator[NAMES.length];
		for (int i = 0; i < NAMES.length; i++) {
			TEST_CREATORS[i] = new SPDXCreator(NAMES[i], COMMENTS[i]);
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
	 * Test method for {@link org.spdx.rdfparser.SPDXCreator#setName(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetName() throws InvalidSPDXAnalysisException {
		Resource[] creatorResources = new Resource[TEST_CREATORS.length];
		for (int i = 0; i < creatorResources.length; i++) {
			creatorResources[i] = TEST_CREATORS[i].createResource(model);
		}
		String[] newNames = new String[NAMES.length];
		for (int i = 0; i < newNames.length; i++) {
			newNames[i] = NAMES[i] + "-new";
		}
		for (int i = 0;i < creatorResources.length; i++) {
			SPDXCreator comp = new SPDXCreator(model, creatorResources[i].asNode());
			comp.setName(newNames[i]);
			assertEquals(newNames[i], comp.getName());
			assertEquals(TEST_CREATORS[i].getComment(), comp.getComment());
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXCreator#setComment(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetComment() throws InvalidSPDXAnalysisException {
		Resource[] creatorResources = new Resource[TEST_CREATORS.length];
		for (int i = 0; i < creatorResources.length; i++) {
			creatorResources[i] = TEST_CREATORS[i].createResource(model);
		}
		String[] newComments = new String[COMMENTS.length];
		newComments[0] = null;
		for (int i = 1; i < newComments.length; i++) {
			newComments[i] = "New comment "+String.valueOf(i);
		}
		for (int i = 0;i < creatorResources.length; i++) {
			SPDXCreator comp = new SPDXCreator(model, creatorResources[i].asNode());
			comp.setComment(newComments[i]);
			assertEquals(TEST_CREATORS[i].getName(), comp.getName());
			assertEquals(newComments[i], comp.getComment());
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXCreator#createResource(com.hp.hpl.jena.rdf.model.Model)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testCreateResource() throws InvalidSPDXAnalysisException {
		Resource[] creatorResources = new Resource[TEST_CREATORS.length];
		for (int i = 0; i < creatorResources.length; i++) {
			creatorResources[i] = TEST_CREATORS[i].createResource(model);
		}
		for (int i = 0;i < creatorResources.length; i++) {
			SPDXCreator comp = new SPDXCreator(model, creatorResources[i].asNode());
			assertEquals(TEST_CREATORS[i].getName(), comp.getName());
			assertEquals(TEST_CREATORS[i].getComment(), comp.getComment());
		}
	}
}
