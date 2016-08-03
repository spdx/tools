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

import com.hp.hpl.jena.graph.Node;

/**
 * A pointing method made up of a pair of pointers that identify a well defined section within a document delimited by a begin and an end.
 * See http://www.w3.org/2009/pointers and https://www.w3.org/WAI/ER/Pointers/WD-Pointers-in-RDF10-20110427
 * This is an abstract class of pointers which must be subclassed
 * @author Gary O'Neall
 *
 */
public abstract class CompoundPointer extends RdfModelObject {
	
	static final Logger logger=Logger.getLogger(CompoundPointer.class);
	
	protected SinglePointer startPointer;
	
	/**
	 * Create a compoundpointer from an existing RDF node
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public CompoundPointer(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getPropertiesFromModel();
	}
	
	/**
	 * Create a compound pointer without an RDF model
	 * @param startPointer
	 */
	public CompoundPointer(SinglePointer startPointer) {
		this.startPointer = startPointer;
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		this.startPointer = findSinglePointerPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, SpdxRdfConstants.PROP_POINTER_START_POINTER);
	}

	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		setPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, SpdxRdfConstants.PROP_POINTER_START_POINTER,
				this.startPointer);
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public List<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		if (this.startPointer == null) {
			retval.add("Missing required start pointer");
		} else {
			retval.addAll(this.startPointer.verify());
		}
		return retval;
	}

	/**
	 * @return the startPointer
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SinglePointer getStartPointer() throws InvalidSPDXAnalysisException {
		if (model != null && this.refreshOnGet) {
			this.startPointer = findSinglePointerPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, SpdxRdfConstants.PROP_POINTER_START_POINTER);
		}
		return startPointer;
	}

	/**
	 * @param startPointer the startPointer to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setStartPointer(SinglePointer startPointer) throws InvalidSPDXAnalysisException {
		this.startPointer = startPointer;
		setPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, SpdxRdfConstants.PROP_POINTER_START_POINTER,
				this.startPointer);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#equivalent(org.spdx.rdfparser.model.RdfModelObject)
	 */
	@Override
	public boolean equivalent(IRdfModel o) {
		if (!(o instanceof CompoundPointer)) {
			return false;
		}
		CompoundPointer comp = (CompoundPointer)o;
		try {
			return equivalentConsideringNull(getStartPointer(), comp.getStartPointer());
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting the start pointer for the comparison",e);
			return false;
		}
	}
}
