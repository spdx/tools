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

import java.util.ArrayList;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A Relationship represents a relationship between two SpdxElements.
 * @author Gary O'Neall
 *
 */
public class Relationship extends RdfModelObject {
	
	public enum RelationshipType {
		 relationshipType_ancestorOf, relationshipType_buildToolOf,
		 relationshipType_containedBy, relationshipType_contains,
		 relationshipType_copyOf, relationshipType_dataFile,
		 relationshipType_descendantOf, relationshipType_distributionArtifact,
		 relationshipType_documentation, relationshipType_dynamicLink,
		 relationshipType_expandedFromArchive, relationshipType_fileAdded,
		 relationshipType_fileDeleted, relationshipType_fileModified,
		 relationshipType_generatedFrom, relationshipType_generates,
		 relationshipType_metafileOf, relationshipType_optionalComponentOf,
		 relationshipType_other, relationshipType_packageOf,
		 relationshipType_patchApplied, relationshipType_patchFor,
		 relationshipType_spdxAmendment, relationshipType_staticLink,
		 relationshipType_testcaseOf
	}

	private RelationshipType relationshipType;
	private String comment;
	private SpdxElement relatedSpdxElement;
	/**
	 * @param model
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public Relationship(Model model, Node node)
			throws InvalidSPDXAnalysisException {
		super(model, node);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	public Relationship() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public ArrayList<String> verify() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	Resource getType(Model model) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	void populateModel() {
		// TODO Auto-generated method stub

	}

}
