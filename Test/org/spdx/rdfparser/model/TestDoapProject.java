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

import java.util.ArrayList;

import org.junit.After;

import org.junit.Before;

import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author Gary O'Neall
 *
 */
public class TestDoapProject {

	String[] NAMES = new String[] {"Name1", "name2", "Name3"};
	String[] HOMEPAGES = new String[] {null, "http://this.is.valid/also", ""};
	DoapProject[] TEST_PROJECTS;
	Model model;
	IModelContainer modelContainer;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		TEST_PROJECTS = new DoapProject[NAMES.length];
		for (int i = 0; i < NAMES.length; i++) {
			TEST_PROJECTS[i] = new DoapProject(NAMES[i], HOMEPAGES[i]);
		}
		model = ModelFactory.createDefaultModel();
		modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
	}
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.DoapProject#setName(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetName() throws InvalidSPDXAnalysisException {
		Resource[] projectResources = new Resource[TEST_PROJECTS.length];
		for (int i = 0; i < projectResources.length; i++) {
			projectResources[i] = TEST_PROJECTS[i].createResource(modelContainer);
		}
		String[] newNames = new String[NAMES.length];
		for (int i = 0; i < newNames.length; i++) {
			newNames[i] = NAMES[i] + "New";
		}
		for (int i = 0;i < projectResources.length; i++) {
			DoapProject comp = new DoapProject(modelContainer, projectResources[i].asNode());
			comp.setName(newNames[i]);
			assertEquals(newNames[i], comp.getName());
			assertEquals(TEST_PROJECTS[i].getHomePage(), comp.getHomePage());
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.DoapProject#setHomePage(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetHomePage() throws InvalidSPDXAnalysisException {
		Resource[] projectResources = new Resource[TEST_PROJECTS.length];
		for (int i = 0; i < projectResources.length; i++) {
			projectResources[i] = TEST_PROJECTS[i].createResource(modelContainer);
		}
		String[] newHomePages = new String[HOMEPAGES.length];
		newHomePages[0] = null;
		for (int i = 1; i < newHomePages.length; i++) {
			newHomePages[i] = "New home page" + String.valueOf(i);
		}
		for (int i = 0;i < projectResources.length; i++) {
			DoapProject comp = new DoapProject(modelContainer, projectResources[i].asNode());
			comp.setHomePage(newHomePages[i]);
			assertEquals(TEST_PROJECTS[i].getName(), comp.getName());
			assertEquals(newHomePages[i], comp.getHomePage());
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.DoapProject#getProjectUri()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetProjectUri() throws InvalidSPDXAnalysisException {
		Resource[] projectResources = new Resource[TEST_PROJECTS.length];
		Resource[] uriProjectResources = new Resource[TEST_PROJECTS.length];
		String[] uris = new String[TEST_PROJECTS.length];
		for (int i = 0; i < uris.length; i++) {
			uris[i] = "http://www.usefulinc.org/somethg/"+TEST_PROJECTS[i].getName();
		}
		for (int i = 0; i < projectResources.length; i++) {
			projectResources[i] = TEST_PROJECTS[i].createResource(modelContainer);
			uriProjectResources[i] = model.createResource(uris[i]);
			copyProperties(projectResources[i], uriProjectResources[i]);
			DoapProject comp = new DoapProject(modelContainer, uriProjectResources[i].asNode());
			assertEquals(TEST_PROJECTS[i].getName(), comp.getName());
			assertEquals(TEST_PROJECTS[i].getHomePage(), comp.getHomePage());
			assertEquals(uris[i], comp.getProjectUri());
		}
	}

	/**
	 * @param resource
	 * @param resource2
	 */
	private void copyProperties(Resource resource, Resource resource2) {
		Triple m = Triple.createMatch(resource.asNode(), null, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			Property p = model.createProperty(t.getPredicate().getURI());
			Node value = t.getObject();
			if (value instanceof RDFNode) {
				RDFNode valuen = (RDFNode)value;
				resource2.addProperty(p, valuen);
			} else {
				resource2.addProperty(p, value.toString(false));
			}
		}
	}
	/**
	 * Test method for {@link org.spdx.rdfparser.DoapProject#createResource(com.hp.hpl.jena.rdf.model.Model)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testCreateResource() throws InvalidSPDXAnalysisException {
		Resource[] projectResources = new Resource[TEST_PROJECTS.length];
		for (int i = 0; i < projectResources.length; i++) {
			projectResources[i] = TEST_PROJECTS[i].createResource(modelContainer);
		}
		for (int i = 0;i < projectResources.length; i++) {
			DoapProject comp = new DoapProject(modelContainer, projectResources[i].asNode());
			assertEquals(TEST_PROJECTS[i].getName(), comp.getName());
			assertEquals(TEST_PROJECTS[i].getHomePage(), comp.getHomePage());
		}
	}
	
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		ArrayList<String> verify;
		for (int i = 0; i < TEST_PROJECTS.length; i++) {
			verify = TEST_PROJECTS[i].verify();
			assertEquals(0, verify.size());
		}
		Resource[] projectResources = new Resource[TEST_PROJECTS.length];
		for (int i = 0; i < projectResources.length; i++) {
			projectResources[i] = TEST_PROJECTS[i].createResource(modelContainer);
		}
		for (int i = 0;i < projectResources.length; i++) {
			DoapProject comp = new DoapProject(modelContainer, projectResources[i].asNode());
			verify = comp.verify();
			assertEquals(0, verify.size());
		}
		
	}
	
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		Resource[] projectResources = new Resource[TEST_PROJECTS.length];
		for (int i = 0; i < projectResources.length; i++) {
			projectResources[i] = TEST_PROJECTS[i].createResource(modelContainer);
		}
		for (int i = 0; i < TEST_PROJECTS.length; i++) {
			DoapProject clone = TEST_PROJECTS[i].clone();
			if (clone.resource != null) {
				fail("Cloned project has a resource");
			}
			if (TEST_PROJECTS[i].resource == null) {
				fail("original project does not contain a resource");
			}
			assertEquals(TEST_PROJECTS[i].getHomePage(), clone.getHomePage());
			assertEquals(TEST_PROJECTS[i].getName(), clone.getName());
			assertEquals(TEST_PROJECTS[i].getProjectUri(), clone.getProjectUri());
		}
	}

}
