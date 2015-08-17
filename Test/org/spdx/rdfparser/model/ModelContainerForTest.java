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

import java.util.Map;
import java.util.Set;

import org.spdx.rdfparser.IModelContainer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Model container class used for testing
 * @author Gary
 *
 */
public class ModelContainerForTest implements IModelContainer {

	Model model;
	String namespace;
	int nextRef = 1;
	Set<String> elementRefs = Sets.newHashSet();
	Map<String, String> externalNamespaceToId = Maps.newHashMap();
	Map<String, String> externalIdToNamespace = Maps.newHashMap();
	/**
	 * 
	 */
	public ModelContainerForTest(Model model, String namespace) {
		this.model = model;
		this.namespace = namespace;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#getModel()
	 */
	@Override
	public Model getModel() {
		return this.model;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#getDocumentNamespace()
	 */
	@Override
	public String getDocumentNamespace() {
		return this.namespace;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#getNextSpdxElementRef()
	 */
	@Override
	public String getNextSpdxElementRef() {
		return "SpdxRef-"+String.valueOf(this.nextRef++);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#SpdxElementRefExists(java.lang.String)
	 */
	@Override
	public boolean spdxElementRefExists(String elementRef) {
		return elementRefs.contains(elementRef);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#addSpdxElementRef(java.lang.String)
	 */
	@Override
	public void addSpdxElementRef(String elementRef) {
		elementRefs.add(elementRef);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#documentNamespaceToId(java.lang.String)
	 */
	@Override
	public String documentNamespaceToId(String externalNamespace) {
		return this.externalNamespaceToId.get(externalNamespace);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#externalDocumentIdToNamespace(java.lang.String)
	 */
	@Override
	public String externalDocumentIdToNamespace(String docId) {
		return this.externalIdToNamespace.get(docId);
	}
	
	public void addExternalDocReference(String docId, String docNamespace) {
		this.externalIdToNamespace.put(docId, docNamespace);
		this.externalNamespaceToId.put(docNamespace, docId);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#createResource(com.hp.hpl.jena.rdf.model.Resource, java.lang.String, com.hp.hpl.jena.rdf.model.Resource, org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public Resource createResource(Resource duplicate, String uri,
			Resource type, IRdfModel modelObject) {
		// Always set multiple to true
		modelObject.setMultipleObjectsForSameNode();
		if (duplicate != null) {
			return duplicate;
		} else if (uri == null) {			
			return model.createResource(type);
		} else {
			return model.createResource(uri, type);
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#addCheckNodeObject(com.hp.hpl.jena.graph.Node, org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean addCheckNodeObject(Node node, IRdfModel rdfModelObject) {
		return true;
	}

}
