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

import java.util.HashSet;
import java.util.Map;

import org.spdx.rdfparser.IModelContainer;

import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Model container class used for testing
 * @author Gary
 *
 */
public class ModelContainerForTest implements IModelContainer {

	Model model;
	String namespace;
	int nextRef = 1;
	HashSet<String> elementRefs = new HashSet<String>();
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

}
