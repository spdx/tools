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

import java.util.List;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A compound pointer pointing out parts of a document by means of a range delimited by a pair of single pointers that define the start point and the end point.
 * See http://www.w3.org/2009/pointers and https://www.w3.org/WAI/ER/Pointers/WD-Pointers-in-RDF10-20110427
 * @author Gary O'Neall
 *
 */
public class StartEndPointer extends CompoundPointer implements Comparable<StartEndPointer> {

	/**
	 * Reference to the pointer that defines the end point for a range.
	 */
	private SinglePointer endPointer;
	
	/**
	 * Create a StartEndPointer from an existing RDF node
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public StartEndPointer(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getPropertiesFromModel();
	}
	
	public StartEndPointer(SinglePointer startPointer, SinglePointer endPointer) {
		super(startPointer);
		this.endPointer = endPointer;
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public List<String> verify() {
		List<String> retval = super.verify();
		if (this.endPointer == null) {
			retval.add("Missing required end pointer");
		} else {
			retval.addAll(this.endPointer.verify());
			if (this.startPointer != null && this.startPointer instanceof ByteOffsetPointer && !(this.endPointer instanceof ByteOffsetPointer)) {
				retval.add("Inconsistent start and end pointer types");
			}
			if (this.startPointer != null && this.startPointer instanceof LineCharPointer && !(this.endPointer instanceof LineCharPointer)) {
				retval.add("Inconsistent start and end pointer types");
			}
			if (this.startPointer != null && this.startPointer.compareTo(endPointer) > 0) {
				retval.add("End pointer is less than start pointer");
			}
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#equivalent(org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean equivalent(IRdfModel compare) {
		if (!(compare instanceof StartEndPointer)) {
			return false;
		}
		if (!super.equivalent(compare)) {
			return false;
		}
		SinglePointer compEndPointer;
		try {
			compEndPointer = ((StartEndPointer)compare).getEndPointer();
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting the equivalend end pointer",e);
			return false;
		}
		if (this.endPointer == null) {
			return compEndPointer == null;
		}
		if (compEndPointer == null) {
			return false;
		}
		return this.endPointer.equivalent(compEndPointer);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		super.getPropertiesFromModel();
		this.endPointer = findSinglePointerPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, SpdxRdfConstants.PROP_POINTER_END_POINTER);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	public String getUri(IModelContainer modelContainer)
			throws InvalidSPDXAnalysisException {
		return null;	// create anon nodes
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.RDF_POINTER_NAMESPACE + SpdxRdfConstants.CLASS_POINTER_START_END_POINTER);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		super.populateModel();
		setPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, SpdxRdfConstants.PROP_POINTER_END_POINTER,
				this.endPointer);
	}
	
	/**
	 * @return the endPointer
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SinglePointer getEndPointer() throws InvalidSPDXAnalysisException {
		if (model != null && this.refreshOnGet) {
			this.endPointer = findSinglePointerPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, SpdxRdfConstants.PROP_POINTER_END_POINTER);
		}
		return endPointer;
	}

	/**
	 * @param endPointer the endPointer to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setEndPointer(SinglePointer endPointer) throws InvalidSPDXAnalysisException {
		this.endPointer = endPointer;
		setPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, SpdxRdfConstants.PROP_POINTER_END_POINTER,
				this.endPointer);
	}
	
	@Override
	public StartEndPointer clone() {
		SinglePointer newStartPointer = null;
		SinglePointer newEndPointer = null;
		if (this.startPointer != null) {
			newStartPointer = this.startPointer.clone();
		}
		if (this.endPointer != null) {
			newEndPointer = this.endPointer.clone();
		}
		return new StartEndPointer(newStartPointer, newEndPointer);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StartEndPointer o) {
		if (o == null) {
			return 1;
		}
		try {
			if (this.startPointer == null) {
				if (o.getStartPointer() == null) {
					return 0;
				} else {
					return -1;
				}
			}
			if (o.getStartPointer() == null) {
				return 1;
			} else {
				return startPointer.compareTo(o.getStartPointer());
			}
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting comparison for start end pointer",e);
			return -1;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("From: ");
		if (this.startPointer != null) {
			sb.append(this.startPointer.toString());
		} else {
			sb.append("[UNKNOWN]");
		}
		sb.append(" To: ");
		if (this.endPointer != null) {
			sb.append(this.endPointer.toString());
		} else {
			sb.append("[UNKNOWN]");
		}
		return sb.toString();
	}
}
