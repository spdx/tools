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
import org.spdx.rdfparser.model.SpdxElement;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary O'Neall
 *
 */
public class LineCharPointer extends SinglePointer {
	
	private Integer lineNumber;

	/**
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public LineCharPointer(IModelContainer modelContainer, Node node)
			throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getMyPropertiesFromModel();
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		super.getPropertiesFromModel();
		getMyPropertiesFromModel();
	}
	
	/**
	 * Get the local properties just associated with this class
	 */
	private void getMyPropertiesFromModel() throws InvalidSPDXAnalysisException {
		this.lineNumber = findIntPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, 
				SpdxRdfConstants.PROP_POINTER_LINE_NUMBER);
	}

	/**
	 * @param reference
	 */
	public LineCharPointer(SpdxElement reference, int lineNumber) {
		super(reference);
		this.lineNumber = lineNumber;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	public String getUri(IModelContainer modelContainer)
			throws InvalidSPDXAnalysisException {
		return null;	// Use anon
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.RDF_POINTER_NAMESPACE + SpdxRdfConstants.CLASS_POINTER_LINE_CHAR_POINTER);
	}
	
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		super.populateModel();
		setPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, 
				SpdxRdfConstants.PROP_POINTER_LINE_NUMBER, this.lineNumber);
	}

	/**
	 * @return the lineNumber
	 */
	public Integer getLineNumber() {
		if (this.resource != null && this.refreshOnGet) {
			this.lineNumber = findIntPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, 
					SpdxRdfConstants.PROP_POINTER_LINE_NUMBER);
		}
		return lineNumber;
	}

	/**
	 * @param lineNumber the lineNumber to set
	 */
	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
		setPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE, 
				SpdxRdfConstants.PROP_POINTER_LINE_NUMBER, this.lineNumber);
	}
	
	@Override
	public boolean equivalent(IRdfModel o) {
		if (!super.equivalent(o)) {
			return false;
		}
		if (!(o instanceof LineCharPointer)) {
			return false;
		}
		LineCharPointer comp = (LineCharPointer)o;
		if (this.lineNumber == null) {
			return comp.getLineNumber() == null;
		}
		if (comp.getLineNumber() == null) {
			return false;
		}
		return this.lineNumber.equals(comp.getLineNumber());
	}

	@Override
	public List<String> verify() {
		List<String> retval = super.verify();
		if (this.lineNumber == null) {
			retval.add("Missing line number value");
		} else if (this.lineNumber < 0) {
			retval.add("Line number most not be negative for a line offset pointer: "+this.lineNumber.toString());
		}
		return retval;
	}
	
	@Override
	public LineCharPointer clone() {
		SpdxElement newReference = null;
		if (this.reference != null) {
			newReference = this.reference.clone();
		}
		return new LineCharPointer(newReference, this.lineNumber);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SinglePointer o) {
		if (o == null) {
			return 1;
		}
		int retval = compareReferences(o);
		if (retval != 0) {
			return retval;
		}
		if (!(o instanceof LineCharPointer)) {
			return 1;
		}
		Integer compLine = ((LineCharPointer)o).getLineNumber();
		if (this.lineNumber == null) {
			return -1;
		}
		return this.lineNumber.compareTo(compLine);
	}

	@Override
	public String toString() {
		if (this.lineNumber != null) {
			return "line number " + this.lineNumber.toString();
		} else {
			return "Unknown line number";
		}
	}
}
