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


import java.util.List;
import java.util.regex.Matcher;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * This is an SPDX element which is in an external document.
 * 
 * The only fields required to be valid are the id and externalDocumentId.
 * 
 * @author Gary O'Neall
 *
 */
public class ExternalSpdxElement extends SpdxElement {
	
	
	/**
	 * @param id SPDX ID used for referencing this external element.  Format ExternalSPDXRef:SPDXID
	 * @throws InvalidSPDXAnalysisException 
	 * 
	 */
	public ExternalSpdxElement(String id) throws InvalidSPDXAnalysisException {
		super(null, null, null, null);
		if (!SpdxRdfConstants.EXTERNAL_ELEMENT_REF_PATTERN.matcher(id).matches()) {
			throw(new InvalidSPDXAnalysisException("Invalid id format for an external document reference.  Must be of the form ExternalSPDXRef:SPDXID"));
		}
		this.setId(id);
	}
	
	@Override
	public void setId(String id) throws InvalidSPDXAnalysisException {
		if (id != null && !SpdxRdfConstants.EXTERNAL_ELEMENT_REF_PATTERN.matcher(id).matches()) {
			throw(new InvalidSPDXAnalysisException("Invalid id format for an external document reference.  Must be of the form ExternalSPDXRef:SPDXID"));
		}
		super.setId(id);
	}
	
	/**
	 * @return external document ID for the external reference
	 * @throws InvalidSPDXAnalysisException
	 */
	public String getExternalDocumentId() throws InvalidSPDXAnalysisException {
		Matcher matcher = SpdxRdfConstants.EXTERNAL_ELEMENT_REF_PATTERN.matcher(this.getId());
		if (!matcher.matches()) {
			throw(new InvalidSPDXAnalysisException("Invalid id format for an external document reference.  Must be of the form ExternalSPDXRef:SPDXID"));
		}
		return matcher.group(1);
	}
	
	/**
	 * @return element ID used in the external document
	 * @throws InvalidSPDXAnalysisException
	 */
	public String getExternalElementId() throws InvalidSPDXAnalysisException {
		Matcher matcher = SpdxRdfConstants.EXTERNAL_ELEMENT_REF_PATTERN.matcher(this.getId());
		if (!matcher.matches()) {
			throw(new InvalidSPDXAnalysisException("Invalid id format for an external document reference.  Must be of the form ExternalSPDXRef:SPDXID"));
		}
		return matcher.group(2);
	}
	
	@Override
    public String getUri(IModelContainer modelContainer) throws InvalidSPDXAnalysisException {
		Matcher matcher = SpdxRdfConstants.EXTERNAL_ELEMENT_REF_PATTERN.matcher(this.getId());
		if (!matcher.matches()) {
			throw(new InvalidSPDXAnalysisException("Invalid id format for an external document reference.  Must be of the form ExternalSPDXRef:SPDXID"));
		}
		String externalDocumentUri = modelContainer.externalDocumentIdToNamespace(matcher.group(1));
		if (externalDocumentUri == null) {
			throw(new InvalidSPDXAnalysisException("No external document reference exists for document ID "+matcher.group(1)));
		}
		return externalDocumentUri + "#" + matcher.group(2);
	}
	
	@Override
	public boolean equivalent(IRdfModel o) {
		return this.equivalent(o, true);
	}
	
	@Override
	public boolean equivalent(IRdfModel o, boolean testRelationships) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof ExternalSpdxElement)) {
			return false;
		}
		ExternalSpdxElement comp = (ExternalSpdxElement)o;
		if (!super.equivalent(comp, testRelationships)) {
			return false;
		}
        return (Objects.equal(this.getId(), comp.getId()));
	}
	
	@Override
	public ExternalSpdxElement clone() {
		try {
			return new ExternalSpdxElement(this.getId());
		} catch (InvalidSPDXAnalysisException e) {
			return null;
		}
	}
	
	@Override
	public List<String> verify() {
		// we don't want to call super.verify since we really don't require those fields
		List<String> retval = Lists.newArrayList();
		String id = this.getId();
		if (id == null) {
			retval.add("Missing required ID for external SPDX element");
		} else {
			if (!SpdxRdfConstants.EXTERNAL_ELEMENT_REF_PATTERN.matcher(id).matches()) {				
				retval.add("Invalid id format for an external document reference.  Must be of the form ExternalSPDXRef:SPDXID");
			}
		}
		return retval;
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		// Do nothing
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		// Do nothing
	}
}
