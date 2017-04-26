/**
 * 
 */
package org.spdx.tools;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.ListedLicenses;
import org.spdx.rdfparser.model.IRdfModel;

/**
 * Container class for license information used by the LicenseRDFaGenerator
 * 
 * @author Gary O'Neall
 *
 */
public class LicenseContainer implements IModelContainer, SpdxRdfConstants {
			
			Model localLicenseModel;
			
			public LicenseContainer() {
				localLicenseModel = ModelFactory.createDefaultModel();
				localLicenseModel.setNsPrefix("spdx", SPDX_NAMESPACE);
				localLicenseModel.setNsPrefix("doap", DOAP_NAMESPACE);
				localLicenseModel.setNsPrefix("rdfs", RDFS_NAMESPACE);
				localLicenseModel.setNsPrefix("rdf", RDF_NAMESPACE);
			}

			@Override
			public Model getModel() {
				return localLicenseModel;
			}

			@Override
			public String getDocumentNamespace() {
				return ListedLicenses.LISTED_LICENSE_URI_PREFIX;
			}

			@Override
			public String getNextSpdxElementRef() {
				return null;	// This will not be used
			}

			@Override
			public boolean spdxElementRefExists(String elementRef) {
				return false;	// This will not be used
			}

			@Override
			public void addSpdxElementRef(String elementRef) {
				// This will not be used
			}

			@Override
			public String documentNamespaceToId(String externalNamespace) {
				// Listed licenses do not support external documents
				return null;
			}

			@Override
			public String externalDocumentIdToNamespace(String docId) {
				//  Listed licenses do not support external documents
				return null;
			}

			@Override
			public Resource createResource(Resource duplicate, String uri,
					Resource type, IRdfModel modelObject) {
				if (duplicate != null) {
					return duplicate;
				} else if (uri == null) {			
					return localLicenseModel.createResource(type);
				} else {
					return localLicenseModel.createResource(uri, type);
				}
			}

			@Override
			public boolean addCheckNodeObject(Node node,
					IRdfModel rdfModelObject) {
				// Not implemented
				return true;
			}	
}
