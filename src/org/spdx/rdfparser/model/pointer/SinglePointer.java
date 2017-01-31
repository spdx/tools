/**
 * Copyright (c) 2016 Source Auditor Inc.
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
package org.spdx.rdfparser.model.pointer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;
import org.spdx.rdfparser.model.RdfModelObject;
import org.spdx.rdfparser.model.SpdxElement;

import org.apache.jena.graph.Node;

/**
 * A pointing method made up of a unique pointer. This is an abstract single pointer that provides the necessary framework, 
 * but it does not provide any kind of pointer, so more specific subclasses must be used.
 * See http://www.w3.org/2009/pointers and https://www.w3.org/WAI/ER/Pointers/WD-Pointers-in-RDF10-20110427
 * 
 * @author Gary O'Neall
 *
 */
public abstract class SinglePointer extends RdfModelObject implements Comparable<SinglePointer> {
	
	static final Logger logger = Logger.getLogger(SinglePointer.class);
	
	/**
	 * The document within which the pointer is applicable or meaningful.
	 */
	protected SpdxElement reference;
	
	/**
	 * Create a SinglePointer from the model
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public SinglePointer(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getPropertiesFromModel();
	}
	
	public SinglePointer(SpdxElement reference) {
		super();
		this.reference = reference;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public List<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		if (this.reference == null) {
			retval.add("Missing required reference field");
		} else {
			retval.addAll(this.reference.verify());
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#equivalent(org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean equivalent(IRdfModel compare) {
		if (!(compare instanceof SinglePointer)) {
			return false;
		}
		SpdxElement compFile;
		try {
			compFile = ((SinglePointer)compare).getReference();
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting reference from comparison",e);
			return false;
		}
		SpdxElement myReference;
		try {
			myReference = this.getReference();
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting my reference from comparison",e);
			return false;
		}
		if (myReference == null) {
			return compFile == null;
		}
		if (compFile == null) {
			return false;
		}
		return myReference.equivalent(compFile, false);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		this.reference = this.findElementPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, SpdxRdfConstants.PROP_POINTER_REFERENCE);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		this.setPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, 
				SpdxRdfConstants.PROP_POINTER_REFERENCE, this.reference);
	}

	/**
	 * @return the reference
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SpdxElement getReference() throws InvalidSPDXAnalysisException {
		if (this.resource != null && this.refreshOnGet) {
			this.reference = this.findElementPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, SpdxRdfConstants.PROP_POINTER_REFERENCE);
		}
		return reference;
	}

	/**
	 * @param reference the reference to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setReference(SpdxElement reference) throws InvalidSPDXAnalysisException {
		this.reference = reference;
		this.setPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, 
				SpdxRdfConstants.PROP_POINTER_REFERENCE, this.reference);
	}
	
	@Override
	public SinglePointer clone() {
		throw(new RuntimeException("Can not clone an abstract single pointer class"));
	}
	
	protected int compareReferences(SinglePointer o) {
		if (o == null) {
			return 1;
		}
		SpdxElement compRef = null;
		try {
			compRef = o.getReference();
			if (this.reference == null) {
				if (compRef == null) {
					return 0;
				} else {
					return -1;
				}
			} else if (compRef == null) {
				return 1;
			} else {
				String myName = this.reference.getName();
				if (myName != null) {
					return myName.compareTo(compRef.getName());
				} else {
					if (compRef.getName() == null) {
						return 0;
					} else {
						return -1;
					}
				}
			}
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting comparison reference element",e);
			return -1;
		}
	}
}
