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

import java.util.ArrayList;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Creator class for SPDX documents
 * @author Gary O'Neall
 *
 */
public class SPDXCreatorInformation {

	private String[] creators = null;
	private String comment = null;
	private String createdDate = null;
	private Node creatorNode = null;
	private Model model = null;
	private Resource creatorResource = null;

	/**
	 * @return the name
	 */
	public String[] getCreators() {
		return creators;
	}

	/**
	 * @param name the name to set
	 */
	public void setCreators(String[] creators) {
		this.creators = creators;
		if (this.creatorNode != null) {
			// delete any previous comments
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CREATION_CREATOR);
			model.removeAll(creatorResource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CREATION_CREATOR);
			for (int i = 0; i < creators.length; i++) {
				creatorResource.addProperty(p, creators[i]);
			}
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
			Property p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			model.removeAll(creatorResource, p, null);
			if (comment != null) {
				// add the property
				p = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
				creatorResource.addProperty(p, comment);
			}
		}
	}

	public SPDXCreatorInformation(String[] creators, String createdDate, String comment) {
		this.creators = creators;
		this.createdDate = createdDate;
		this.comment = comment;
	}

	public SPDXCreatorInformation(Model spdxModel, Node creatorNode) throws InvalidSPDXAnalysisException {
		this.model = spdxModel;
		this.creatorNode = creatorNode;
		if (creatorNode.isBlank()) {
			creatorResource = model.createResource(creatorNode.getBlankNodeId());
		} else if (creatorNode.isURI()) {
			creatorResource = model.createResource(creatorNode.getURI());
		} else {
			throw(new InvalidSPDXAnalysisException("Creator node can not be a literal"));
		}
		// creators
		Node p = spdxModel.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CREATION_CREATOR).asNode();
		Triple m = Triple.createMatch(creatorNode, p, null);
		ExtendedIterator<Triple> tripleIter = spdxModel.getGraph().find(m);
		ArrayList<String> alCreators = new ArrayList<String>();
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alCreators.add(t.getObject().toString(false));
		}
		this.creators = alCreators.toArray(new String[alCreators.size()]);
		// comment
		p = spdxModel.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT).asNode();
		m = Triple.createMatch(creatorNode, p, null);
		tripleIter = spdxModel.getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.comment = t.getObject().toString(false);
		}
		// created
		p = spdxModel.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CREATION_CREATED).asNode();
		m = Triple.createMatch(creatorNode, p, null);
		tripleIter = spdxModel.getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.createdDate = t.getObject().toString(false);
		}
	}

	public Resource createResource(Model model) {
		this.model = model;
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE +
				SpdxRdfConstants.CLASS_SPDX_CREATION_INFO);
		Resource r = model.createResource(type);
		if (creators != null && creators.length > 0) {
			Property nameProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE,
					SpdxRdfConstants.PROP_CREATION_CREATOR);
			for (int i = 0; i < creators.length; i++) {
				r.addProperty(nameProperty, this.creators[i]);
			}
		}
		if (this.comment != null) {
			Property commentProperty = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			r.addProperty(commentProperty, this.comment);
		}
		// creation date
		if (this.createdDate != null) {
			Property createdDateProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CREATION_CREATED);
			r.addProperty(createdDateProperty, this.createdDate);
		}
		this.creatorNode = r.asNode();
		this.creatorResource = r;
		return r;
	}

	public String getCreated() {
		return this.createdDate;
	}

	public void setCreated(String createdDate) {
		this.createdDate = createdDate;
		if (this.creatorNode != null) {
			// delete any previous comments
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CREATION_CREATED);
			model.removeAll(creatorResource, p, null);
			if (comment != null) {
				// add the property
				p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CREATION_CREATED);
				creatorResource.addProperty(p, this.createdDate);
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SPDXCreatorInformation)) {
			return false;
		}
		SPDXCreatorInformation compCreator = (SPDXCreatorInformation)o;
		if (compCreator.getCreated() == null) {
			if (this.getCreated() != null) {
				return false;
			}
		} else if (this.createdDate == null) {
			return false;
		}
			else if (!compCreator.getCreated().equals(this.createdDate)) {
		}
		if (compCreator.getComment() == null) {
			if (this.getComment() != null) {
				return false;
			}
		} else if (this.getComment() == null) {
			return false;
		}
			else if (!compCreator.getComment().equals(this.comment)) {
				return false;
		}
		String[] compNames = compCreator.getCreators();
		if (compNames == null) {
			if (this.creators != null) {
				return false;
			}
		} else if (this.creators == null) {
			return false;
		} else {
			if (compNames.length != this.creators.length) {
				return false;
			}
			for (int i = 0; i < compNames.length; i++) {
				boolean found = false;
				for (int j = 0; j < this.creators.length; j++) {
					if (compNames[i].equals(this.creators[j])) {
						found = true;
						break;
					}
				}
				if (!found) {
					return false;
				}
			}
		}
		return true;

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.creators != null && this.creators.length > 0) {
			sb.append(creators[0]);
			for (int i = 0; i < creators.length; i++) {
				sb.append(", ");
				sb.append(creators[i]);
			}
		}
		if (createdDate != null && !createdDate.isEmpty()) {
			sb.append("; Created on ");
			sb.append(createdDate);
		}
		if (comment != null && !comment.isEmpty()) {
			sb.append("; Comment: ");
			sb.append(comment);
		}
		return sb.toString();
	}

	/**
	 * @return
	 */
	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		String[] creators = this.getCreators();
		if (creators == null || creators.length == 0) {
			retval.add("Missing required creators");
		} else {
			for (int i = 0;i < creators.length; i++) {
				String verify = SpdxVerificationHelper.verifyCreator(creators[i]);
				if (verify != null) {
					retval.add(verify);
				}
			}
		}
		String creationDate = this.getCreated();
		if (creationDate == null || creationDate.isEmpty()) {
			retval.add("Missing required created date");
		} else {
			String verify = SpdxVerificationHelper.verifyDate(creationDate);
			if (verify != null) {
				retval.add(verify);
			}
		}
		@SuppressWarnings("unused")
		String createdComments = this.getComment();
		// anything to verify for comments?
		return retval;
	}
}
