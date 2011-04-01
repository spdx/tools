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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Creator class for SPDX documents
 * @author Gary O'Neall
 *
 */
public class SPDXCreator {
	
	private String name = null;
	private String comment = null;
	private Node creatorNode = null;
	private Model model = null;
	private Resource creatorResource = null;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
		if (this.creatorNode != null) {
			// delete any previous comments
			Property p = model.getProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_CREATOR_NAME);
			model.removeAll(creatorResource, p, null);
			// add the property
			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_CREATOR_NAME);
			creatorResource.addProperty(p, name);
		}
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
		if (this.creatorNode != null) {
			// delete any previous comments
			Property p = model.getProperty(SPDXAnalysis.RDFS_NAMESPACE, SPDXAnalysis.RDFS_PROP_COMMENT);
			model.removeAll(creatorResource, p, null);
			if (comment != null) {
				// add the property
				p = model.createProperty(SPDXAnalysis.RDFS_NAMESPACE, SPDXAnalysis.RDFS_PROP_COMMENT);
				creatorResource.addProperty(p, comment);
			}
		}
	}

	public SPDXCreator(String name, String comment) {
		this.name = name;
		this.comment = comment;
	}
	
	public SPDXCreator(Model spdxModel, Node creatorNode) throws InvalidSPDXAnalysisException {
		this.model = spdxModel;
		this.creatorNode = creatorNode;
		if (creatorNode.isBlank()) {
			creatorResource = model.createResource(creatorNode.getBlankNodeId());
		} else if (creatorNode.isURI()) {
			creatorResource = model.createResource(creatorNode.getURI());
		} else {
			throw(new InvalidSPDXAnalysisException("Creator node can not be a literal"));
		}
		// name
		Node p = spdxModel.getProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_CREATOR_NAME).asNode();
		Triple m = Triple.createMatch(creatorNode, p, null);
		ExtendedIterator<Triple> tripleIter = spdxModel.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.name = t.getObject().toString(false);
		}
		// comment
		p = spdxModel.getProperty(SPDXAnalysis.RDFS_NAMESPACE, SPDXAnalysis.RDFS_PROP_COMMENT).asNode();
		m = Triple.createMatch(creatorNode, p, null);
		tripleIter = spdxModel.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.comment = t.getObject().toString(false);
		}
	}
	
	public Resource createResource(Model model) {
		this.model = model;
		Resource type = model.createResource(SPDXAnalysis.SPDX_NAMESPACE +
				SPDXAnalysis.CLASS_SPDX_CREATOR);
		Resource r = model.createResource(type);
		if (name != null) {
			Property nameProperty = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, 
					SPDXAnalysis.PROP_CREATOR_NAME);
			r.addProperty(nameProperty, this.name);
		}
		if (this.comment != null) {
			Property commentProperty = model.createProperty(SPDXAnalysis.RDFS_NAMESPACE, SPDXAnalysis.RDFS_PROP_COMMENT);
			r.addProperty(commentProperty, this.comment);
		}
		this.creatorNode = r.asNode();
		this.creatorResource = r;
		return r;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SPDXCreator)) {
			return false;
		}
		SPDXCreator compCreator = (SPDXCreator)o;
		if (compCreator.getName().equals(this.name) && compCreator.getComment().equals(this.comment)) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		if (comment == null  && !comment.isEmpty()) {
			return this.name;
		} else {
			return this.name + "("+ this.comment + ")";
		}
	}
}
