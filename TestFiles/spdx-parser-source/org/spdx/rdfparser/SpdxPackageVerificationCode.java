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
 * Contains an SPDX Package Verification Code, currently consisting
 * of a value and list of excluded files.
 *
 * @author Gary O'Neall
 *
 */
public class SpdxPackageVerificationCode {

	private String value;
	private ArrayList<String> excludedFileNames = new ArrayList<String>();
	private Model model;
	private Node verificationCodeNode;
	private Resource verificationCodeResource;


	public SpdxPackageVerificationCode(String value, String[] excludedFileNames) {
		this.value = value;
		for (int i = 0; i < excludedFileNames.length; i++) {
			this.excludedFileNames.add(excludedFileNames[i]);
		}
		this.model = null;
		this.verificationCodeNode = null;
		this.verificationCodeResource = null;
	}

	public SpdxPackageVerificationCode(Model model, Node verificationCodeNode) throws InvalidSPDXAnalysisException {
		this.model = model;
		this.verificationCodeNode = verificationCodeNode;
		if (verificationCodeNode.isBlank()) {
			verificationCodeResource = model.createResource(verificationCodeNode.getBlankNodeId());
		} else if (verificationCodeNode.isURI()) {
			verificationCodeResource = model.createResource(verificationCodeNode.getURI());
		} else {
			throw(new InvalidSPDXAnalysisException("Verification code node can not be a literal"));
		}
		// excluded filenames
		// Algorithm
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_VERIFICATIONCODE_IGNORED_FILES).asNode();
		Triple m = Triple.createMatch(verificationCodeNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.excludedFileNames.add(t.getObject().toString(false));
		}

		// value
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_VERIFICATIONCODE_VALUE).asNode();
		m = Triple.createMatch(verificationCodeNode, p, null);
		tripleIter = model.getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.value = t.getObject().toString(false);
		}
	}

	/**
	 * Creates a resource from this SPDX Verification Code
	 * @param model
	 * @return
	 */
	public Resource createResource(Model model) {
		this.model = model;
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE +
				SpdxRdfConstants.CLASS_SPDX_VERIFICATIONCODE);
		Resource r = model.createResource(type);
		if (this.excludedFileNames.size() > 0) {
			Property excludedFileProp = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE,
					SpdxRdfConstants.PROP_VERIFICATIONCODE_IGNORED_FILES);
			for (int i = 0; i < this.excludedFileNames.size(); i++) {
				r.addProperty(excludedFileProp, this.excludedFileNames.get(i));
			}
		}
		if (this.value != null  && !this.value.isEmpty()) {
			Property valueProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_VERIFICATIONCODE_VALUE);
			r.addProperty(valueProperty, this.value);
		}
		this.verificationCodeNode = r.asNode();
		this.verificationCodeResource = r;
		return r;
	}

	public String[] getExcludedFileNames() {
		String[] retval = this.excludedFileNames.toArray(new String[excludedFileNames.size()]);
		return retval;
	}

	public void setExcludedFileNames(String[] excludedFileNames) {
		this.excludedFileNames.clear();
		if (this.verificationCodeNode != null && this.model != null) {
			// clear old list
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_VERIFICATIONCODE_IGNORED_FILES);
			model.removeAll(this.verificationCodeResource, p, null);
		}
		for (int i = 0; i < excludedFileNames.length; i++) {
			addExcludedFileName(excludedFileNames[i]);
		}
	}

	public void addExcludedFileName(String excludedFileName) {
		this.excludedFileNames.add(excludedFileName);
		if (this.verificationCodeNode != null && this.model != null) {
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_VERIFICATIONCODE_IGNORED_FILES);
			this.verificationCodeResource.addProperty(p, excludedFileName);
		}
	}

	public String getValue() {
		return this.value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
		if (this.model != null && this.verificationCodeNode != null) {
			// delete any previous value
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_VERIFICATIONCODE_VALUE);
			model.removeAll(verificationCodeResource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_VERIFICATIONCODE_VALUE);
			verificationCodeResource.addProperty(p, value);
		}
	}

	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		String value = this.getValue();
		if (value == null || value.isEmpty()) {
			retval.add("Missing required verification code value");
		} else {
			String verify = SpdxVerificationHelper.verifyChecksumString(value);
			if (verify != null) {
				retval.add(verify);
			}
		}
		return retval;
	}

}
