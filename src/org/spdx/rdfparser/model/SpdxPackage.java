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

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary O'Neall
 *
 */
public class SpdxPackage extends SpdxItem {
	
	AnyLicenseInfo licenseDeclared;
	Checksum checksum;
	String description;
	String downloadLocation;
	String homepage;
	String originator;
	String packageFileName;
	SpdxPackageVerificationCode packageVerificationCode;	//TODO Move this to RdfModelObject
	String sourceInfo;
	String summary;
	String supplier;
	String versionInfo;
	

	/**
	 * @param name
	 * @param comment
	 * @param annotations
	 * @param relationships
	 * @param licenseConcluded
	 * @param licenseDeclared
	 * @param copyrightText
	 * @param licenseComment
	 */
	public SpdxPackage(String name, String comment, Annotation[] annotations,
			Relationship[] relationships, AnyLicenseInfo licenseConcluded,
			AnyLicenseInfo[] licenseInfosFromFiles, String copyrightText,
			String licenseComment) {
		super(name, comment, annotations, relationships, licenseConcluded,
				licenseInfosFromFiles, copyrightText, licenseComment);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxPackage(IModelContainer modelContainer, Node node)
			throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected Resource findDuplicateResource(IModelContainer modelContainer, String uri) throws InvalidSPDXAnalysisException {
		//TODO Implement
		// probably a package with the same verification code and
		// the same package name
		return null;
	}
	
	@Override
	protected void populateModel() throws InvalidSPDXAnalysisException {
		super.populateModel();
	}

	@Override
	protected String getNamePropertyName() {
		return SpdxRdfConstants.PROP_PROJECT_NAME;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_PACKAGE);
	}
	
	@Override
	public boolean equivalent(RdfModelObject o) {
		return false;
		//TODO Implement
	}
	
	@Override
	public SpdxPackage clone() {
		//TODO Implement
		return null;
	}
}
