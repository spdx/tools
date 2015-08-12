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
package org.spdx.rdfparser.license;

import java.util.List;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfModelHelper;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * The SimpleLicenseInfo class includes all resources that represent 
 * simple, atomic, licensing information.
 * 
 * @author Gary O'Neall
 *
 */
public abstract class SimpleLicensingInfo extends AnyLicenseInfo {
	protected String licenseId;
	protected String comment;
	protected String name;
	protected String[] seeAlso;
	

	/**
	 * @param modelContainer container which includes the license
	 * @param licenseInfoNode RDF Node that defines the SimpleLicensingInfo
	 * @throws InvalidSPDXAnalysisException 
	 */
	SimpleLicensingInfo(IModelContainer modelContainer, Node licenseInfoNode) throws InvalidSPDXAnalysisException {
		super(modelContainer, licenseInfoNode);
		// id
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID).asNode();
		Triple m = Triple.createMatch(licenseInfoNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.licenseId = t.getObject().toString(false);
		}
		// name
		this.name = null;
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME).asNode();
		m = Triple.createMatch(licenseInfoNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.name = t.getObject().toString(false);
		} else {
			// try the pre 1.1 name - for backwards compatibility
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME_VERSION_1).asNode();
			m = Triple.createMatch(licenseInfoNode, p, null);
			tripleIter = model.getGraph().find(m);	
			if (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.name = t.getObject().toString(false);
//TODO: Remove the commented out code below once we verify no compatibility issue exist
				//			} else {	
//				this.name = licenseId;	// No name hsa been found, default is the ID
			}
		}
		// SourceUrl/seeAlso
		List<String> alsourceUrls = Lists.newArrayList();
		p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO).asNode();
		m = Triple.createMatch(licenseInfoNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alsourceUrls.add(t.getObject().toString(false));
		}
		// The following is added for compatibility with earlier versions
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL_VERSION_1).asNode();
		m = Triple.createMatch(licenseInfoNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alsourceUrls.add(t.getObject().toString(false));
		}
		p = model.getProperty(SpdxRdfConstants.OWL_NAMESPACE, SpdxRdfConstants.PROP_OWL_SAME_AS).asNode();
		m = Triple.createMatch(licenseInfoNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alsourceUrls.add(t.getObject().toString(false));
		}
		this.seeAlso = alsourceUrls.toArray(new String[alsourceUrls.size()]);
		// comments
		p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT).asNode();
		m = Triple.createMatch(licenseInfoNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (!tripleIter.hasNext()) {
			// check the old property name for compatibility with pre-1.1 generated RDF files
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTES_VERSION_1).asNode();
			m = Triple.createMatch(licenseInfoNode, p, null);
			tripleIter = model.getGraph().find(m);	
		}
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.comment = t.getObject().toString(false);
		} else {
			this.comment = null;
		}
		
	}
	
	/**
	 * @param name License name
	 * @param id License ID
	 * @param comments Optional license comments
	 * @param sourceUrl Optional reference URL's
	 */
	SimpleLicensingInfo(String name, String id, String comments, String[] sourceUrl) {
		super();
		this.licenseId = id;
		this.name = name;
		this.comment = comments;
		this.seeAlso = sourceUrl;
	}
	/**
	 * @return the id
	 */
	public String getLicenseId() {
		return this.licenseId;
	}

	/**
	 * @param id the id to set
	 */
	public void setLicenseId(String id) {
		this.licenseId = id;
		if (licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID);
			model.removeAll(resource, p, null);
			// add the property
			if (id != null) {
				p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID);
				resource.addProperty(p, id);
			}
		}
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME);
			model.removeAll(resource, p, null);
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME_VERSION_1);
			model.removeAll(resource, p, null);
			// add the property
			if (name != null) {
				p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME);
				resource.addProperty(p, this.name);
			}
		}
	}
	/**
	 * @return the comments
	 */
	public String getComment() {
		return comment;
	}
	
	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			model.removeAll(resource, p, null);
			// Also delete any instances of the pre-1.1 property names
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTES_VERSION_1);
			model.removeAll(resource, p, null);
			// add the property
			if (comment != null) {
				p = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
				resource.addProperty(p, this.comment);
			}
		}
	}
	
	/**
	 * @return the urls which reference the same license information
	 */
	public String[] getSeeAlso() {
		return seeAlso;
	}
	/**
	 * @param seeAlsoUrl the urls which are references to the same license to set
	 */
	public void setSeeAlso(String[] seeAlsoUrl) {
		this.seeAlso = seeAlsoUrl;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			// the following is to fix any earlier versions using the old property name
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL_VERSION_1);
			model.removeAll(resource, p, null);
			p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
			model.removeAll(resource, p, null);
			// add the property
			if (seeAlsoUrl != null) {
				p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
				for (int i = 0; i < seeAlsoUrl.length; i++) {
					resource.addProperty(p, this.seeAlso[i]);
				}	
			}
		}
	}
	
	/**
	 * Create a basic SPDXLicense resource of a given type
	 * If a license with this ID already exists in the model, then that resource
	 * is returned.  For the case where a license ID already exists, the text in the resource will be updated
	 *  with the text of this license as long as the text is not null.
	 *  ID's are assumed to be unique.
	 * NOTE: the type must be a subclass of SPDXLicense
	 * @param model
	 * @param uri 
	 * @param typeURI
	 * @return
	 */
	protected Resource _createResource(Resource type, String uri) {
		Resource r = null;
		if (licenseId != null) {
			// check to see if it exists
			Property idProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_LICENSE_ID);
			Property typeProperty = this.model.getProperty(SpdxRdfConstants.RDF_NAMESPACE, 
					SpdxRdfConstants.RDF_PROP_TYPE);
			Triple m = Triple.createMatch(null, idProperty.asNode(), null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				if (t.getObject().toString(false).equals(this.licenseId)) {
					Triple typeMatch = Triple.createMatch(t.getSubject(), typeProperty.asNode(), type.asNode());
					ExtendedIterator<Triple> typeTripleIter = model.getGraph().find(typeMatch);
					if (typeTripleIter.hasNext()) {
						// found it
						if (t.getSubject().isURI()) {
							r = model.createResource(t.getSubject().getURI());
						} else if (t.getSubject().isBlank()) {
							r = model.createResource(t.getSubject().getBlankNodeId());
						}
					}
				}
			}
		}
		if (r == null) {
			// need to create it
			if (uri == null || uri.isEmpty()) {
				r = model.createResource(type);
			} else {
				r = model.createResource(uri, type);
			}
			if (licenseId != null) {
				Property idProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_LICENSE_ID);
				r.addProperty(idProperty, this.licenseId);
			}
			//name
			if (name != null) {
				Property namePropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_STD_LICENSE_NAME);
				r.addProperty(namePropery, this.name);
			}
			// comments
			if (this.comment != null) {
				Property notesPropery = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
				r.addProperty(notesPropery, this.comment);
			}
			//source URL
			if (this.seeAlso != null && this.seeAlso.length > 0) {
				Property sourceUrlPropery = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, 
						SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
				for (int i = 0; i < this.seeAlso.length; i++) {
					r.addProperty(sourceUrlPropery, this.seeAlso[i]);
				}
			}
		}
		return r;
	}
	
	@Override
	public int hashCode() {
		if (this.getLicenseId() == null) {
			return 0;
		} else {
			return this.getLicenseId().hashCode();
		}
	}
	
	@Override
	public boolean equals(Object comp) {
		if (comp == this) {
			return true;
		}
		if (!(comp instanceof License)) {
			return false;
		}
		License compl = (License)comp;
		return compl.getLicenseId().equals(this.getLicenseId());
	}

	/**
	 * @param type
	 * @return
	 */
	public Resource _createResource(Resource type) {
		return _createResource(type, null);
	}
	
	@Override
    public boolean equivalent(IRdfModel compare) {
		if (compare == this) {
			return true;
		}
		if (!(compare instanceof SimpleLicensingInfo)) {
			return false;
		}
		SimpleLicensingInfo sCompare = (SimpleLicensingInfo)compare;
        return Objects.equal(this.comment, sCompare.getComment()) &&
                Objects.equal(this.name, sCompare.getName()) &&
				RdfModelHelper.arraysEqual(this.seeAlso, sCompare.getSeeAlso());
	}
	
}
