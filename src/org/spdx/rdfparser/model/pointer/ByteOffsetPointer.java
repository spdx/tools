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

import com.google.common.base.Objects;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 *
 * @author Gary O'Neall
 *
 */
public class ByteOffsetPointer extends SinglePointer {

	private Integer offset;

	public ByteOffsetPointer(SpdxElement reference, int offset) {
		super(reference);
		this.offset = offset;
	}

	/**
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public ByteOffsetPointer(IModelContainer modelContainer, Node node)
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
		this.offset = findIntPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE,
				SpdxRdfConstants.PROP_POINTER_OFFSET);
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
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(org.apache.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.RDF_POINTER_NAMESPACE + SpdxRdfConstants.CLASS_POINTER_BYTE_OFFSET_POINTER);
	}

	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		super.populateModel();
		setPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE,
				SpdxRdfConstants.PROP_POINTER_OFFSET, this.offset);
	}

	@Override
	public boolean equivalent(IRdfModel o) {
		if (!super.equivalent(o)) {
			return false;
		}
		if (!(o instanceof ByteOffsetPointer)) {
			return false;
		}
		ByteOffsetPointer comp = (ByteOffsetPointer)o;
		return Objects.equal(this.getOffset(), comp.getOffset());
	}

	/**
	 * @return the offset
	 */
	public Integer getOffset() {
		if (this.resource != null && this.refreshOnGet) {
			this.offset = findIntPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE,
					SpdxRdfConstants.PROP_POINTER_OFFSET);
		}
		return offset;
	}

	/**
	 * @param offset the offset to set
	 */
	public void setOffset(Integer offset) {
		this.offset = offset;
		setPropertyValue(SpdxRdfConstants.RDF_POINTER_NAMESPACE,
				SpdxRdfConstants.PROP_POINTER_OFFSET, this.offset);
	}

	@Override
	public List<String> verify() {
		List<String> retval = super.verify();
		if (this.offset == null) {
			retval.add("Missing byte offset offset value");
		} else if (this.offset < 0) {
			retval.add("Offset most not be negative for a byte pointer: "+this.offset.toString());
		}
		return retval;
	}

	@Override
	public ByteOffsetPointer clone() {
		SpdxElement newReference = null;
		if (this.reference != null) {
			newReference = this.reference.clone();
		}
		return new ByteOffsetPointer(newReference, this.offset);
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
		if (!(o instanceof ByteOffsetPointer)) {
			return 1;
		}

		Integer compByteOffset = ((ByteOffsetPointer)o).getOffset();
		if (this.offset == null) {
			return -1;
		}
		return this.offset.compareTo(compByteOffset);
	}

	@Override
	public String toString() {
		if (this.offset != null) {
			return "byte offset " + this.offset.toString();
		} else {
			return "Unknown byte offset";
		}
	}
}
