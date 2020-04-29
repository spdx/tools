/**
 * Copyright (c) 2020 Source Auditor Inc.
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
 */
package org.spdx.rdfparser.model;

import java.util.List;

import org.apache.jena.graph.Node;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;

import com.google.common.collect.Lists;

/**
 * Type of SpdxElement which is a constant unmodifiable element
 * 
 * @author Gary O'Neall
 *
 */
public abstract class SpdxConstantElement extends SpdxElement {

	public SpdxConstantElement(IModelContainer container, Node elementNode)
			throws InvalidSPDXAnalysisException {
		super(container, elementNode);
	}
	
	public SpdxConstantElement(String name, String comment) {
		super(name, comment, new Annotation[0], new Relationship[0]);
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		// no properties to get
	}
	
	@Override 
	public void setName(String name) {
		throw new RuntimeException("Can not set name for constant element types");
	}
	
	@Override
	public void setComment(String comment) {
		throw new RuntimeException("Can not set comment for constant element types");
	}
	
	@Override
	public void setAnnotations(Annotation[] annotations) {
		throw new RuntimeException("Can not set annotations for constant element types");
	}
	
	@Override
	public void addAnnotation(Annotation annotation) {
		throw new RuntimeException("Can not add annotations for constant element types");
	}
	
	@Override
	public void setRelationships(Relationship[] relationships) {
		throw new RuntimeException("Can not set relationships for constant element types");
	}
	
	@Override
	public void addRelationship(Relationship relationship) {
		throw new RuntimeException("Can not add relationships for constant element types");
	}
	
	@Override
	public List<String> verify() {
		return Lists.newArrayList();
	}
	
	@Override
	public boolean equivalent(IRdfModel compare) {
		return this.equals(compare);
	}
	
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		// Nothing to populate
		
	}
	
	@Override
	public void setId(String id) {
		throw new RuntimeException("Can not set ID for constant element types");
	}
}
