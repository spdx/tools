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

import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.DuplicateExtractedLicenseIdException;
import org.spdx.rdfparser.license.LicenseInfoFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Contains and SPDXFile object and updates the RDF model.
 * 
 * This class is for compatibility with the 1.2 version of the SPDX library
 * and is no longer used for version 2.0 and above.
 * @author Gary O'Neall
 *
 */
@Deprecated
public class SPDXFile implements Comparable<SPDXFile>, Cloneable {
	
	static final Logger logger = LoggerFactory.getLogger(SPDXFile.class.getName());
	IModelContainer modelContainer;
	private Model model = null;
	private Resource resource = null;
	private String name;
	private AnyLicenseInfo concludedLicenses;
	private SPDXChecksum sha1;
	private String type;
	private AnyLicenseInfo[] seenLicenses;
	private String licenseComments;
	private String copyright;
	private DOAPProject[] artifactOf;
	private String comment = null;
	private SPDXFile[] fileDependencies;
	private String[] contributors;
	private String noticeText;
	
	public static final Map<String, String> FILE_TYPE_TO_RESOURCE = Maps.newHashMap();
	public static final Map<String, String> RESOURCE_TO_FILE_TYPE = Maps.newHashMap();

	static {
		FILE_TYPE_TO_RESOURCE.put(SpdxRdfConstants.FILE_TYPE_SOURCE, 
				SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.PROP_FILE_TYPE_SOURCE);
		RESOURCE_TO_FILE_TYPE.put(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.PROP_FILE_TYPE_SOURCE, 
				SpdxRdfConstants.FILE_TYPE_SOURCE);
		FILE_TYPE_TO_RESOURCE.put(SpdxRdfConstants.FILE_TYPE_BINARY, 
				SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.PROP_FILE_TYPE_BINARY);
		RESOURCE_TO_FILE_TYPE.put(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.PROP_FILE_TYPE_BINARY, 
				SpdxRdfConstants.FILE_TYPE_BINARY);
		FILE_TYPE_TO_RESOURCE.put(SpdxRdfConstants.FILE_TYPE_ARCHIVE, 
				SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.PROP_FILE_TYPE_ARCHIVE);
		RESOURCE_TO_FILE_TYPE.put(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.PROP_FILE_TYPE_ARCHIVE, 
				SpdxRdfConstants.FILE_TYPE_ARCHIVE);
		FILE_TYPE_TO_RESOURCE.put(SpdxRdfConstants.FILE_TYPE_OTHER, 
				SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.PROP_FILE_TYPE_OTHER);
		RESOURCE_TO_FILE_TYPE.put(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.PROP_FILE_TYPE_OTHER, 
				SpdxRdfConstants.FILE_TYPE_OTHER);
	};
	
	/**
	 * Construct an SPDX File form the fileNode
	 * @param fileNode RDF Graph node representing the SPDX File
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SPDXFile(IModelContainer modelContainer, Node fileNode) throws InvalidSPDXAnalysisException {
		this.modelContainer = modelContainer;
		this.model = modelContainer.getModel();
		this.resource = RdfParserHelper.convertToResource(model, fileNode);
		// name
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_NAME).asNode();
		Triple m = Triple.createMatch(fileNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.name = t.getObject().toString(false);
		}
		// checksum - sha1
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_CHECKSUM).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.sha1  = new SPDXChecksum(model, t.getObject());
		}
		// type
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_TYPE).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			if (t.getObject().isLiteral()) {
				// the following is for compatibility with previous versions of the tool which used literals for the file type
				this.type = t.getObject().toString(false);
			} else if (t.getObject().isURI()) {
				this.type = RESOURCE_TO_FILE_TYPE.get(t.getObject().getURI());
				if (this.type == null) {
					throw(new InvalidSPDXAnalysisException("Invalid URI for file type resource - must be one of the individual file types in http://spdx.org/rdf/terms"));
				}
			} else {
				throw(new InvalidSPDXAnalysisException("Invalid file type property - must be a URI type specified in http://spdx.org/rdf/terms"));
			}			
		}
		// concluded License
		List<AnyLicenseInfo> alLic = Lists.newArrayList();
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_CONCLUDED).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alLic.add(LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, t.getObject()));
		}
		if (alLic.size() > 1) {
			throw(new InvalidSPDXAnalysisException("Too many concluded licenses for file"));
		}
		if (alLic.size() == 0) {
			throw(new InvalidSPDXAnalysisException("Missing required concluded license"));
		}
		this.concludedLicenses = alLic.get(0);
		// seenLicenses
		alLic.clear();		
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_SEEN_LICENSE).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alLic.add(LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, t.getObject()));
		}
		this.seenLicenses = alLic.toArray(new AnyLicenseInfo[alLic.size()]);

		// fileDependencies
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_FILE_DEPENDENCY).asNode();
		m = Triple.createMatch(fileNode, p, null);
		List<SPDXFile> alDependencies = Lists.newArrayList();
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alDependencies.add(new SPDXFile(modelContainer, t.getObject()));
		}
		this.fileDependencies = alDependencies.toArray(new SPDXFile[alDependencies.size()]);
		
		//licenseComments
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_COMMENTS).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.licenseComments = t.getObject().toString(false);
		}
		//copyright
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_COPYRIGHT).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			if (t.getObject().isURI()) {
				// check for standard value types
				if (t.getObject().getURI().equals(SpdxRdfConstants.URI_VALUE_NOASSERTION)) {
					this.copyright = SpdxRdfConstants.NOASSERTION_VALUE;
				} else if (t.getObject().getURI().equals(SpdxRdfConstants.URI_VALUE_NONE)) {
					this.copyright = SpdxRdfConstants.NONE_VALUE;
				} else {
					this.copyright = t.getObject().toString(false);
				}
			} else {
				this.copyright = t.getObject().toString(false);
			}
		}
		//comment
		p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			this.comment = tripleIter.next().getObject().toString(false);
		}
		// contributors
		List<String> alContributors = Lists.newArrayList();
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_CONTRIBUTOR).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alContributors.add(t.getObject().toString(false));
		}
		this.contributors = alContributors.toArray(new String[alContributors.size()]);
		//artifactOf
		List<DOAPProject> alProjects = Lists.newArrayList();
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_ARTIFACTOF).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alProjects.add(new DOAPProject(model, t.getObject()));
		}
		this.artifactOf = alProjects.toArray(new DOAPProject[alProjects.size()]);
		// noticeText
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_NOTICE).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			this.noticeText = tripleIter.next().getObject().toString(false);
		}
	}
	
	/**
	 * Create a resource for this SPDX file
	 * @param uri URI for the file resource
	 * @param doc SPDX document containing the SPDX file
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	public Resource createResource(SPDXDocument doc, String uri) throws InvalidSPDXAnalysisException {
		this.modelContainer = doc;
		Model model = doc.getModel();
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_FILE);
		Resource retval = findFileResource(model, this);	// prevent duplicate files in the model
		if (retval == null) {
			retval = model.createResource(uri, type);
		}
		populateModel(doc, retval);
		this.resource = retval;
		return retval;
	}
	
	/**
	 * Populates a Jena RDF model with the information from this file declaration
	 * @param licenseResource
	 * @param model
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void populateModel(SPDXDocument doc, Resource fileResource) throws InvalidSPDXAnalysisException {
		Model model = doc.getModel();
		// name
		Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_NAME);
		fileResource.addProperty(p, this.getName());

		// sha1
		p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_CHECKSUM);
		model.removeAll(fileResource, p, null);
		if (this.sha1 != null) {
			Resource cksumResource = this.sha1.getResource();
			if (cksumResource == null) {
				cksumResource = this.sha1.createResource(model);
			}
			fileResource.addProperty(p, cksumResource);
		}
		// type
		p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_TYPE);
		model.removeAll(fileResource, p, null);
		Resource fileTypeResource = fileTypeStringToTypeResource(this.getType(), model);
		fileResource.addProperty(p, fileTypeResource);

		// concludedLicenses
		p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_CONCLUDED);
		model.removeAll(fileResource, p, null);
		if (this.concludedLicenses != null) {
			Resource lic = this.concludedLicenses.createResource(doc);
			fileResource.addProperty(p, lic);
		}

		// seenLicenses
		p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_SEEN_LICENSE);
		model.removeAll(fileResource, p, null);
		if (this.seenLicenses != null && this.seenLicenses.length > 0) {
			for (int i = 0; i < this.seenLicenses.length; i++) {
				Resource lic = this.seenLicenses[i].createResource(doc);
				fileResource.addProperty(p, lic);
			}
		}
		
		// fileDependencies
		p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_FILE_DEPENDENCY);
		model.removeAll(fileResource, p, null);
		if (this.fileDependencies != null && this.fileDependencies.length > 0) {
			for (int i = 0; i < this.fileDependencies.length; i++) {
				Resource dep = this.fileDependencies[i].getResource();
				if (dep == null) {
					// see if the file already exists in the model - prevents creating a duplicate file
					dep = findFileResource(model, this.fileDependencies[i]);
					if (dep == null) {	// need to add this file dependency to a resource
						dep = this.fileDependencies[i].createResource(doc, doc.getDocumentNamespace() + doc.getNextSpdxElementRef());
					}
				}
				fileResource.addProperty(p, dep);
			}
		}
		//licenseComments
		p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_COMMENTS);
		model.removeAll(fileResource, p, null);
		if (this.licenseComments != null) {
			fileResource.addProperty(p, this.getLicenseComments());
		}
		//copyright
		p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_COPYRIGHT);
		model.removeAll(fileResource, p, null);
		if (this.copyright != null) {
			if (copyright.equals(SpdxRdfConstants.NONE_VALUE)) {
				Resource r = model.createResource(SpdxRdfConstants.URI_VALUE_NONE);
				fileResource.addProperty(p, r);
			} else if (copyright.equals(SpdxRdfConstants.NOASSERTION_VALUE)) {
				Resource r = model.createResource(SpdxRdfConstants.URI_VALUE_NOASSERTION);
				fileResource.addProperty(p, r);
			} else {
				fileResource.addProperty(p, this.getCopyright());
			}
		}
		//comment
		p = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
		model.removeAll(fileResource, p, null);
		if (this.comment != null) {
			fileResource.addProperty(p, this.comment);
		}

		//artifactof
		p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_ARTIFACTOF);
		model.removeAll(fileResource, p, null);
		if (this.artifactOf != null) {
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_ARTIFACTOF);
			for (int i = 0; i < artifactOf.length; i++) {
				Resource projectResource = artifactOf[i].createResource(model);
				fileResource.addProperty(p, projectResource);
			}
		}		
		
		//contributors
		p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_CONTRIBUTOR);
		model.removeAll(fileResource, p, null);
		if (this.contributors != null) {
			for (int i = 0; i < this.contributors.length; i++) {
				fileResource.addProperty(p, this.contributors[i]);
			}
		}
		
		//noticeText
		p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_NOTICE);
		model.removeAll(fileResource, p, null);
		if (this.noticeText != null) {
			fileResource.addProperty(p, this.getNoticeText());
		}
		this.model = model;
		this.resource = fileResource;
	}
	
	/**
	 * Finds the resource for an existing file in the model
	 * @param spdxFile
	 * @return resource of an SPDX file with the same name and checksum.  Null if none found
	 * @throws InvalidSPDXAnalysisException 
	 */
	static protected Resource findFileResource(Model model, SPDXFile spdxFile) throws InvalidSPDXAnalysisException {
		// find any matching file names
		Node fileNameProperty = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_NAME).asNode();
		Triple fileNameMatch = Triple.createMatch(null, fileNameProperty, NodeFactory.createLiteral(spdxFile.getName()));
		
		ExtendedIterator<Triple> filenameMatchIter = model.getGraph().find(fileNameMatch);	
		if (filenameMatchIter.hasNext()) {
			Triple fileMatchTriple = filenameMatchIter.next();
			Node fileNode = fileMatchTriple.getSubject();
			// check the checksum
			Node checksumProperty = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_CHECKSUM).asNode();
			Triple checksumMatch = Triple.createMatch(fileNode, checksumProperty, null);
			ExtendedIterator<Triple> checksumMatchIterator = model.getGraph().find(checksumMatch);
			if (checksumMatchIterator.hasNext()) {
				Triple checksumMatchTriple = checksumMatchIterator.next();
				SPDXChecksum cksum = new SPDXChecksum(model, checksumMatchTriple.getObject());
				if (cksum.getValue().compareToIgnoreCase(spdxFile.sha1.getValue()) == 0) {
					return RdfParserHelper.convertToResource(model, fileNode);
				}
			}
		}
		// if we get to here, we did not find a match
		return null;
	}
	public SPDXFile(String name, String type, String sha1,
			AnyLicenseInfo concludedLicenses,
			AnyLicenseInfo[] seenLicenses, String licenseComments,
			String copyright, DOAPProject[] artifactOf, String comment,
			SPDXFile[] fileDependencies, String[] contributors, 
			String noticeText) {
		this.name = name;
		this.type = type;
		this.sha1 = new SPDXChecksum(SpdxRdfConstants.ALGORITHM_SHA1, sha1);
		this.concludedLicenses = concludedLicenses;
		this.seenLicenses = seenLicenses;
		this.licenseComments = licenseComments;
		this.copyright = copyright;
		this.artifactOf = artifactOf;
		this.comment = comment;
		if (fileDependencies == null) {
			this.fileDependencies = new SPDXFile[0];
		} else {
			this.fileDependencies = fileDependencies;
		}
		if (contributors == null) {
			this.contributors = new String[0];
		} else {
			this.contributors = contributors;
		}
		this.noticeText = noticeText;
	}
	
	public SPDXFile(String name, String type, String sha1,
			AnyLicenseInfo concludedLicenses,
			AnyLicenseInfo[] seenLicenses, String licenseComments,
			String copyright, DOAPProject[] artifactOf) {
		this(name, type, sha1, concludedLicenses, seenLicenses,
				licenseComments, copyright, artifactOf, null, null, null, null);
	}
	
	public SPDXFile(String name, String type, String sha1,
			AnyLicenseInfo concludedLicenses,
			AnyLicenseInfo[] seenLicenses, String licenseComments,
			String copyright, DOAPProject[] artifactOf, String comment) {
		this(name, type, sha1, concludedLicenses, seenLicenses,
				licenseComments, copyright, artifactOf, comment, null, null, null);
	}

	
	/**
	 * @return the seenLicenses
	 * @throws InvalidSPDXAnalysisException 
	 */
	public AnyLicenseInfo[] getSeenLicenses() {
		if (this.model != null && this.resource != null) {
			try {
				List<Node> alLicNode = Lists.newArrayList();	
				Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_SEEN_LICENSE).asNode();
				Triple m = Triple.createMatch(this.resource.asNode(), p, null);
				ExtendedIterator <Triple> tripleIter = model.getGraph().find(m);	
				while (tripleIter.hasNext()) {
					Triple t = tripleIter.next();
					alLicNode.add(t.getObject());
				}
				boolean seenLicensesMatch = (this.seenLicenses.length == alLicNode.size());
				int i = 0;
				while (seenLicensesMatch && i < this.seenLicenses.length) {
					Resource licResource = this.seenLicenses[i++].getResource();
					if (licResource == null) {
						seenLicensesMatch = false;
					} else {
						boolean found = false;
						for (int j = 0; j < alLicNode.size(); j++) {
							if (alLicNode.get(j).equals(licResource.asNode())) {
								found = true;
								break;
							}
						}
						if (!found) {
							seenLicensesMatch = false;
						}
					}
				}
				if (!seenLicensesMatch) {
					this.seenLicenses = new AnyLicenseInfo[alLicNode.size()];
					for (int k = 0; i < this.seenLicenses.length; k++) {
						this.seenLicenses[k] = LicenseInfoFactory.getLicenseInfoFromModel(
								modelContainer, alLicNode.get(k));
					}
				}
			} catch(InvalidSPDXAnalysisException e) {
				// just use the original exception
			}
		}
		return seenLicenses;
	}
	/**
	 * @param seenLicenses the seenLicenses to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setSeenLicenses(AnyLicenseInfo[] seenLicenses) throws InvalidSPDXAnalysisException {
		this.seenLicenses = seenLicenses;
		if (this.model != null && this.resource != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_SEEN_LICENSE);
			model.removeAll(this.resource, p, null);
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_SEEN_LICENSE);

			for (int i = 0; i < seenLicenses.length; i++) {
				Resource lic;
				try {
					lic = seenLicenses[i].createResource(this.modelContainer);
				} catch (DuplicateExtractedLicenseIdException e) {
					throw(new InvalidSPDXAnalysisException("Seen licenses contains a non-standard license ID "+
							" which already exists in the model with different license text:" + seenLicenses[i].toString()));
				}
				this.resource.addProperty(p, lic);
			}
		}
	}
	/**
	 * @return the licenseComments
	 */
	public String getLicenseComments() {
		if (this.model != null && this.resource != null) {
			Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_COMMENTS).asNode();
			Triple m = Triple.createMatch(this.resource.asNode(), p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.licenseComments = t.getObject().toString(false);
			}
		}
		return licenseComments;
	}
	/**
	 * @param licenseComments the licenseComments to set
	 */
	public void setLicenseComments(String licenseComments) {
		this.licenseComments = licenseComments;
		if (this.model != null && this.resource != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_COMMENTS);
			model.removeAll(this.resource, p, null);
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_COMMENTS);
			this.resource.addProperty(p, this.getLicenseComments());
		}	
	}
	
	/**
	 * @return the noticeText
	 */
	public String getNoticeText() {
		if (this.model != null && this.resource != null) {
			Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_NOTICE).asNode();
			Triple m = Triple.createMatch(this.resource.asNode(), p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				this.noticeText = tripleIter.next().getObject().toString(false);
			}
		}
		return this.noticeText;
	}
	/**
	 * @param noticeTexgt the noticeText to set
	 */
	public void setNoticeText(String noticeText) {
		this.noticeText = noticeText;
		if (this.model != null && this.resource != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_NOTICE);
			model.removeAll(this.resource, p, null);
			if (this.noticeText != null) {
				p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_NOTICE);
				this.resource.addProperty(p, this.getNoticeText());
			}
		}	
	}
	
	public String getComment() {
		if (this.model != null && this.resource != null) {
			Node p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT).asNode();
			Triple m = Triple.createMatch(this.resource.asNode(), p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				this.comment = tripleIter.next().getObject().toString(false);
			}
		}
		return this.comment;
	}
	
	public void setComment(String comment) {
	this.comment = comment;
		if (this.model != null && this.resource != null) {
			Property p = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			model.removeAll(this.resource, p, null);
			p = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			this.resource.addProperty(p, this.comment);
		}
	}
	/**
	 * @return the copyright
	 */
	public String getCopyright() {
		if (this.model != null && this.resource != null) {
			Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_COPYRIGHT).asNode();
			Triple m = Triple.createMatch(this.resource.asNode(), p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				if (t.getObject().isURI()) {
					// check for standard value types
					if (t.getObject().getURI().equals(SpdxRdfConstants.URI_VALUE_NOASSERTION)) {
						this.copyright = SpdxRdfConstants.NOASSERTION_VALUE;
					} else if (t.getObject().getURI().equals(SpdxRdfConstants.URI_VALUE_NONE)) {
						this.copyright = SpdxRdfConstants.NONE_VALUE;
					} else {
						this.copyright = t.getObject().toString(false);
					}
				} else {
					this.copyright = t.getObject().toString(false);
				}
			}
		}
		return copyright;
	}
	/**
	 * @param copyright the copyright to set
	 */
	public void setCopyright(String copyright) {
		this.copyright = copyright;
		if (this.model != null && this.resource != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_COPYRIGHT);
			model.removeAll(this.resource, p, null);
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_COPYRIGHT);
			if (copyright.equals(SpdxRdfConstants.NONE_VALUE)) {
				Resource r = model.createResource(SpdxRdfConstants.URI_VALUE_NONE);
				this.resource.addProperty(p, r);
			} else if (copyright.equals(SpdxRdfConstants.NOASSERTION_VALUE)) {
				Resource r = model.createResource(SpdxRdfConstants.URI_VALUE_NOASSERTION);
				this.resource.addProperty(p, r);
			} else {
				this.resource.addProperty(p, this.getCopyright());
			}
		}	
	}

	
	/**
	 * @return the name
	 */
	public String getName() {
		if (this.model != null && this.resource != null) {
			Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_NAME).asNode();
			Triple m = Triple.createMatch(this.resource.asNode(), p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.name = t.getObject().toString(false);
			}
		}
		return this.name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
		if (this.model != null && this.resource != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_NAME);
			model.removeAll(this.resource, p, null);
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_NAME);
			this.resource.addProperty(p, this.getName());
		}
	}
	/**
	 * @return the fileLicenses
	 * @throws InvalidSPDXAnalysisException 
	 */
	public AnyLicenseInfo getConcludedLicenses() {
		if (this.model != null && this.resource != null) {
			try {
				Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_CONCLUDED).asNode();
				Triple m = Triple.createMatch(this.resource.asNode(), p, null);
				ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
				Triple t = null;
				while (tripleIter.hasNext()) {
					t = tripleIter.next();
					if (this.concludedLicenses != null &&
							t.getObject().equals(this.concludedLicenses.getResource().asNode())) {
						break;
					}
				}
				if (this.concludedLicenses == null ||
						!t.getObject().equals(this.concludedLicenses.getResource().asNode())) {
					this.concludedLicenses = LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, t.getObject());
				}
			}	catch(InvalidSPDXAnalysisException e) {
				// just use the original property
			}
		}
		return this.concludedLicenses;
	}
	/**
	 * @param fileLicenses the fileLicenses to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setConcludedLicenses(AnyLicenseInfo fileLicenses) throws InvalidSPDXAnalysisException {
		this.concludedLicenses = fileLicenses;
		if (this.model != null && this.resource != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_CONCLUDED);
			model.removeAll(this.resource, p, null);
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_CONCLUDED);
			Resource lic = null;
			try {
				lic = fileLicenses.createResource(this.modelContainer);
			} catch (DuplicateExtractedLicenseIdException e) {
				throw(new InvalidSPDXAnalysisException("Concluded licenses contains a non-standard license with inconsistent text with " +
						"                                   an existing license: "+fileLicenses.toString()));
			}
			this.resource.addProperty(p, lic);
		}
	}
	/**
	 * @return the sha1
	 * @throws InvalidSPDXAnalysisException 
	 */
	public String getSha1()  {
		if (this.model != null && this.resource != null) {
			try {
				Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_CHECKSUM).asNode();
				Triple m = Triple.createMatch(this.resource.asNode(), p, null);
				ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
				Triple t = null;
				while (tripleIter.hasNext()) {
					t = tripleIter.next();
					if (this.sha1 != null && nodesEquals(this.sha1.getResource().asNode(), t.getObject())) {
						break;
					}
				}
				if (this.sha1 == null || !nodesEquals(this.sha1.getResource().asNode(), t.getObject())) {
					this.sha1 = new SPDXChecksum(model, t.getObject());
				}
			} catch (InvalidSPDXAnalysisException e) {
				// just use the original sha1
			}
		}
		return this.sha1.getValue();
	}
	
	private boolean nodesEquals(Node n1, Node n2) {
		return n1.equals(n2);
	}
	
	/**
	 * @param sha1 the sha1 to set
	 */
	public void setSha1(String sha1) {
		this.sha1 = new SPDXChecksum(SpdxRdfConstants.ALGORITHM_SHA1, sha1);
		if (this.model != null && this.resource != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_CHECKSUM);
			model.removeAll(this.resource, p, null);
			Resource cksumResource = this.sha1.createResource(model);
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_CHECKSUM);
			this.resource.addProperty(p, cksumResource);
		}
	}
	/**
	 * @return the type
	 * @throws InvalidSPDXAnalysisException 
	 */
	public String getType() {
		if (this.model != null && this.resource != null) {
			try {
				Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_TYPE).asNode();
				Triple m = Triple.createMatch(this.resource.asNode(), p, null);
				ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
				while (tripleIter.hasNext()) {
					Triple t = tripleIter.next();
					if (t.getObject().isLiteral()) {
						// the following is for compatibility with previous versions of the tool which used literals for the file type
						this.type = t.getObject().toString(false);
					} else if (t.getObject().isURI()) {
						this.type = RESOURCE_TO_FILE_TYPE.get(t.getObject().getURI());
						if (this.type == null) {
							throw(new InvalidSPDXAnalysisException("Invalid URI for file type resource - must be one of the individual file types in http://spdx.org/rdf/terms"));
						}
					} else {
						throw(new InvalidSPDXAnalysisException("Invalid file type property - must be a URI type specified in http://spdx.org/rdf/terms"));
					}			
				} 
			}catch(InvalidSPDXAnalysisException e) {
				// just use the original
			}
		}
		return this.type;
	}
	/**
	 * @param type the type to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setType(String type) throws InvalidSPDXAnalysisException {
		this.type = type;
		if (this.model != null && this.resource != null) {
			Resource typeResource = fileTypeStringToTypeResource(type, this.model);
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_TYPE);
			model.removeAll(this.resource, p, null);
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_TYPE);
			this.resource.addProperty(p, typeResource);
		}
	}

	/**
	 * Converts a string file type to an RDF resource
	 * @param fileType
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static Resource  fileTypeStringToTypeResource(String fileType, Model model) throws InvalidSPDXAnalysisException {
		String resourceUri = FILE_TYPE_TO_RESOURCE.get(fileType);
		if (resourceUri == null) {
			// not sure if we want to throw an exception here or just set to "Other"
			throw(new InvalidSPDXAnalysisException("Invalid file type: "+fileType));
			//resourceUri = SpdxRdfConstants.PROP_FILE_TYPE_OTHER;
			
		}
		Resource retval = model.createResource(resourceUri);
		return retval;
	}
	
	public static String fileTypeResourceToString(Resource fileTypeResource) throws InvalidSPDXAnalysisException {
		if (!fileTypeResource.isURIResource()) {
			throw(new InvalidSPDXAnalysisException("File type resource must be a URI."));
		}
		String retval = fileTypeResource.getURI();
		if (retval == null) {
			throw(new InvalidSPDXAnalysisException("Not a recognized file type for an SPDX document."));
		}
		return retval;
	}

	/**
	 * @return the artifactOf
	 * @throws InvalidSPDXAnalysisException 
	 */
	public DOAPProject[] getArtifactOf()  {
		if (this.model != null && this.resource != null) {
			try {
				List<Node> alProjectNodes = Lists.newArrayList();
				Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_ARTIFACTOF).asNode();
				Triple m = Triple.createMatch(this.resource.asNode(), p, null);
				ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
				while (tripleIter.hasNext()) {
					Triple t = tripleIter.next();
					alProjectNodes.add(t.getObject());					
				}
				List<DOAPProject> alProjects = Lists.newArrayList();
				boolean projectsDifferent = this.artifactOf.length != alProjectNodes.size();
				for (int i = 0; i < this.artifactOf.length; i++) {
					Resource projectResource = this.artifactOf[i].getResource();
					if (projectResource == null) {
						projectsDifferent = true;
					} else {
						boolean found = false;
						for (int j = 0; j < alProjectNodes.size(); j++) {
							if (alProjectNodes.get(j).equals(projectResource.asNode())) {
								found = true;
								alProjects.add(this.artifactOf[i]);
								alProjectNodes.remove(j);
								break;
							}
						}
						if (!found) {
							projectsDifferent = true;
						}
					}
				}
				if (projectsDifferent) {
					for(int i = 0; i < alProjectNodes.size(); i++) {
						alProjects.add(new DOAPProject(model, alProjectNodes.get(i)));
					}
					this.artifactOf = alProjects.toArray(new DOAPProject[alProjects.size()]);
				}
			} catch (InvalidSPDXAnalysisException e) {
				// just use the original artifactOf
			}
		}
		return artifactOf;
	}

	/**
	 * @param artifactOf the artifactOf to set
	 */
	public void setArtifactOf(DOAPProject[] artifactOf) {
		this.artifactOf = artifactOf;
		if (this.model != null && this.resource != null && this.name != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_ARTIFACTOF);
			model.removeAll(this.resource, p, null);
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_ARTIFACTOF);
			for (int i = 0; i < artifactOf.length; i++) {
				// we need to check on these if it already exists
				Resource projectResource = null;
				String uri = artifactOf[i].getProjectUri();
				if (uri != null && !uri.isEmpty() && !uri.equals(DOAPProject.UNKNOWN_URI)) {
					projectResource = model.createResource(uri);
				} else {
					projectResource = artifactOf[i].createResource(model);
				}
				this.resource.addProperty(p, projectResource);
			}
		}
	}

	/**
	 * @return
	 */
	public List<String> verify() {
		List<String> retval = Lists.newArrayList();
		// fileName
		String fileName = this.getName();
		if (fileName == null || fileName.isEmpty()) {
			retval.add("Missing required name for file");
			fileName = "UNKNOWN";
		}
		// fileType
		String fileType = this.getType();
		if (fileType == null || fileType.isEmpty()) {
			retval.add("Missing required file type");
		} else {
			String verifyFileType = SpdxVerificationHelper.verifyFileType(fileType);
			if (verifyFileType != null) {
				retval.add(verifyFileType + "; File - "+fileName);
			}
		}
		// copyrightText
		String copyrightText = this.getCopyright();
		if (copyrightText == null || copyrightText.isEmpty()) {
			retval.add("Missing required copyright text for file "+fileName);
		}
		// contributors - nothing to verify
		// license comments - invoked for processing
		getLicenseComments();
		// license concluded
		AnyLicenseInfo concludedLicense = this.getConcludedLicenses();
		if (concludedLicense == null) {
			retval.add("Missing required concluded license for file "+fileName);
		} else {
			retval.addAll(concludedLicense.verify());
		}
		// license info in files
		AnyLicenseInfo[] licenseInfosInFile = this.getSeenLicenses();
		if (licenseInfosInFile == null || licenseInfosInFile.length == 0) {
			retval.add("Missing required license information in file for file "+fileName);
		} else {
			for (int i = 0; i < licenseInfosInFile.length; i++) {
				retval.addAll(licenseInfosInFile[i].verify());
			}
		}
		// fileDependencies
		if (fileDependencies != null) {
			for (int i = 0; i < fileDependencies.length; i++) {
				List<String> verifyFileDependency = fileDependencies[i].verify();
				for (int j = 0; j < verifyFileDependency.size(); j++) {
					retval.add("Invalid file dependency for file named "+
							fileDependencies[i].getName()+": "+verifyFileDependency.get(j));
				}
			}
		}
		// checksum
		String checksum = this.getSha1();
		if (checksum == null || checksum.isEmpty()) {
			retval.add("Missing required checksum for file "+fileName);
		} else {
			String verify = SpdxVerificationHelper.verifyChecksumString(checksum);
			if (verify != null) {
				retval.add(verify + "; file "+fileName);
			}
		}
		// artifactOf
		DOAPProject[] projects = this.getArtifactOf();
		if (projects != null) {
			for (int i = 0;i < projects.length; i++) {
				retval.addAll(projects[i].verify());
			}
		}	
		// comment - verify there are not more than one
		if (this.model != null && this.resource != null) {
			Node p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT).asNode();
			Triple m = Triple.createMatch(this.resource.asNode(), p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			int count = 0;
			while (tripleIter.hasNext()) {
				tripleIter.next();
				count++;
			}
			if (count > 1) {
				retval.add("More than one file comment for file "+this.name);
			}
		}	
		// noticeText - verify there are not more than one
		if (this.model != null && this.resource != null) {
			Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_NOTICE).asNode();
			Triple m = Triple.createMatch(this.resource.asNode(), p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			int count = 0;
			while (tripleIter.hasNext()) {
				tripleIter.next();
				count++;
			}
			if (count > 1) {
				retval.add("More than one file notice for file "+this.name);
			}
		}
		return retval;
	}
	/**
	 * @return file dependencies
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SPDXFile[] getFileDependencies() {
		if (this.model != null && this.resource != null) {
			try {
				Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_FILE_DEPENDENCY).asNode();
				Triple m = Triple.createMatch(this.resource.asNode(), p, null);
				List<Node> alDependencyNodes = Lists.newArrayList();
				ExtendedIterator<Triple >tripleIter = model.getGraph().find(m);	
				while (tripleIter.hasNext()) {
					Triple t = tripleIter.next();
					alDependencyNodes.add(t.getObject());
				}
				boolean fileDependenciesMatch = (this.fileDependencies.length == alDependencyNodes.size());
				int i = 0;
				while (i < this.fileDependencies.length && fileDependenciesMatch) {
					Resource fileDependencyResource = this.fileDependencies[i++].getResource();					
					if (fileDependencyResource == null) {
						fileDependenciesMatch = false;
					} else {
						Node fileDependancyNode = fileDependencyResource.asNode();
						boolean found = false;
						for (int j = 0; j < alDependencyNodes.size(); j++) {
							if (alDependencyNodes.get(j).equals(fileDependancyNode)) {
								found = true;
								break;
							}
						}
						if (!found) {
							fileDependenciesMatch = false;
						}
					}
				}
				if (!fileDependenciesMatch) {
					this.fileDependencies = new SPDXFile[alDependencyNodes.size()];
					for (int k = 0; k < this.fileDependencies.length; k++) {
						this.fileDependencies[k] = new SPDXFile(modelContainer, alDependencyNodes.get(k));
					}
				}
			} catch (InvalidSPDXAnalysisException e) {
				// just the the original property
			}
		}
		return this.fileDependencies;
	}
	/**
	 * Set the file dependencies for this file
	 * @param fileDependencies
	 * @param doc SPDX Document containing the file dependencies
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setFileDependencies(SPDXFile[] fileDependencies, SPDXDocument doc) throws InvalidSPDXAnalysisException {
		if (fileDependencies == null) {
			this.fileDependencies = new SPDXFile[0];
		} else {
			this.fileDependencies = fileDependencies;
		}
		if (this.model != null && this.resource != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_FILE_DEPENDENCY);
			model.removeAll(this.resource, p, null);
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_FILE_DEPENDENCY);

			for (int i = 0; i < this.fileDependencies.length; i++) {
				Resource dep = this.fileDependencies[i].getResource();
				if (dep == null) {
					dep = this.fileDependencies[i].createResource(doc, doc.getDocumentNamespace() + doc.getNextSpdxElementRef());
				}
				this.resource.addProperty(p, dep);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SPDXFile)) {
			return false;
		}
		SPDXFile compareFile = (SPDXFile)o;
		if (this.model != null && this.resource != null) {
			Resource compareFileResource = compareFile.getResource();
			return resourcesEqual(this.resource, compareFileResource);
		} else {
			// no resource
			Resource compareFileResource = compareFile.getResource();
			if (compareFileResource != null) {
				return false;
			}
			return super.equals(o);
		}
	}
	
	@Override
	public int hashCode() {
		if (this.model != null && this.resource != null) {
			if (this.resource.isAnon()) {
				return this.resource.getId().hashCode();
			} else if (this.resource.isURIResource()) {
				return this.resource.getURI().hashCode();
			} else {
				return this.resource.hashCode();
			}
		} else {
			return super.hashCode();
		}
	}
	/**
	 * @param r1
	 * @param r2
	 * @return
	 */
	private boolean resourcesEqual(Resource r1,
			Resource r2) {
		if (r2 == null) {
			return false;
		}
		if (r1.isAnon()) {
			if (!r2.isAnon()) {
				return false;
			}
			return r1.getId().equals(r2.getId());
		} else {
			if (!r2.isURIResource()) {
				return false;
			}
			return r1.getURI().equals(r2.getURI());
		} 
	}
	/**
	 * @return the Jena resource if it exists in the model
	 */
	protected Resource getResource() {
		if (this.model == null) {
			return null;
		}
		return this.resource;
	}
	/**
	 * @return contributors to the file
	 */
	public String[] getContributors() {
		if (this.model != null && this.resource != null) {
			List<String> alContributors = Lists.newArrayList();
			Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_CONTRIBUTOR).asNode();
			Triple m = Triple.createMatch(this.resource.asNode(), p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alContributors.add(t.getObject().toString(false));
			}
			this.contributors = alContributors.toArray(new String[alContributors.size()]);
		}
		return this.contributors;
	}
	
	/**
	 * Set the contributors to the file
	 * @param contributors
	 */
	public void setContributors(String[] contributors) {
		if (contributors == null) {
			this.contributors = new String[0];
		} else {
			this.contributors = contributors;
		}
		if (this.model != null && this.resource != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_CONTRIBUTOR);
			model.removeAll(this.resource, p, null);
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_CONTRIBUTOR);

			for (int i = 0; i < this.contributors.length; i++) {			
				this.resource.addProperty(p, this.contributors[i]);
			}
		}
	}

    /**
     * This method is used for sorting a list of SPDX files
     * @param file SPDXFile that is compared
     * @return 
     */
    @Override
    public int compareTo(SPDXFile file) {
        return this.getName().compareTo(file.getName());        
    }
    
    /**
     * Create a deep copy or clone of this file including all properties.
     * The cloned SPDX file is created within the SPDX document using the
     * provided fileUri and the SPDXDocument model is populated with all of the
     * properties.
     * @param doc Document containing the Model used to create the copy of this SPDX file
     * @param fileUri
     * @return
     * @throws InvalidSPDXAnalysisException 
     */
    public SPDXFile clone(SPDXDocument doc, String fileUri) throws InvalidSPDXAnalysisException {
    	SPDXFile retval = this.clone();
    	retval.createResource(doc, fileUri);
    	return retval;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     * Creates a deep copy including clones of the files and artifactOfs referenced in the fileDependencies
     */
    @Override
    public SPDXFile clone() {
    	SPDXFile[] cloneFileDependencies;
    	if (this.fileDependencies == null) {
    		cloneFileDependencies = new SPDXFile[0];
    	} else {
    		cloneFileDependencies = new SPDXFile[this.fileDependencies.length];
    		for (int i = 0; i < cloneFileDependencies.length; i++) {
    			cloneFileDependencies[i] = this.fileDependencies[i].clone();
    		}
    	}
    	DOAPProject[] cloneArtifactOfs;
    	if (this.artifactOf == null) {
    		cloneArtifactOfs = new DOAPProject[0];
    	} else {
    		cloneArtifactOfs = new DOAPProject[this.artifactOf.length];
    		for (int i = 0; i < cloneArtifactOfs.length; i++) {
    			cloneArtifactOfs[i] = this.artifactOf[i].clone();
    		}
    	}
    	AnyLicenseInfo clonedConcludedLicense = concludedLicenses.clone();
    	AnyLicenseInfo[] clonedSeenLicenses;
    	if (this.seenLicenses == null) {
    		clonedSeenLicenses = new AnyLicenseInfo[0];
    	} else {
    		clonedSeenLicenses = new AnyLicenseInfo[seenLicenses.length];
    		for (int i = 0; i < clonedSeenLicenses.length; i++) {
    			if (seenLicenses[i] != null) {
    				clonedSeenLicenses[i] = seenLicenses[i].clone();
    			}
    		}
    	}
    	SPDXChecksum clonedChecksum = sha1.clone();
    	return new SPDXFile(this.getName(), this.getType(), 
    			clonedChecksum.getValue(), clonedConcludedLicense, 
    			clonedSeenLicenses, this.getLicenseComments(),
    			this.getCopyright(), cloneArtifactOfs, this.getComment(), 
    			cloneFileDependencies, this.getContributors(), this.getNoticeText());
    }

	/**
	 * Returns true if all of the properties of the compareToFile are 
	 * equivalent or equal to all properties of this SPDXFile.  Equivalent
	 * is different from equals in that the equals method will only return true
	 * if it is the same object.
	 * @param compareToFile
	 * @return
	 */
	public boolean equivalent(SPDXFile compareToFile) {
		if (!SpdxVerificationHelper.equivalentArray(this.artifactOf, compareToFile.getArtifactOf())) {
			return false;
		}
		if (!SpdxVerificationHelper.equalsWithNull(this.comment, compareToFile.getComment())) {
			return false;
		}
		if (!SpdxVerificationHelper.equalsWithNull(this.concludedLicenses, compareToFile.getConcludedLicenses())) {
			return false;
		}
		if (!SpdxVerificationHelper.equivalentArray(this.contributors, compareToFile.getContributors())) {
			return false;
		}
		if (!SpdxVerificationHelper.equalsWithNull(this.copyright, compareToFile.getCopyright())) {
			return false;
		}
		if (!equivalentFileDependencies(this.fileDependencies, compareToFile.getFileDependencies())) {
			return false;
		}
		if (!SpdxVerificationHelper.equalsWithNull(this.licenseComments, compareToFile.getLicenseComments())) {
			return false;
		}
		if (!SpdxVerificationHelper.equalsWithNull(this.name, compareToFile.getName())) {
			return false;
		}
		if (!SpdxVerificationHelper.equalsWithNull(this.noticeText, compareToFile.getNoticeText())) {
			return false;
		}
		if (!SpdxVerificationHelper.equivalentArray(this.seenLicenses, compareToFile.getSeenLicenses())) {
			return false;
		}
		if (sha1 == null) {
			if (compareToFile.getSha1() != null) {
				return false;
			}
		} else {
			if (compareToFile.getSha1() == null) {
				return false;
			}
			if (!SpdxVerificationHelper.equalsWithNull(this.sha1.getValue(), compareToFile.getSha1())) {
				return false;
			}
		}
		if (!SpdxVerificationHelper.equalsWithNull(this.type, compareToFile.getType())) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if two arrays of file dependencies are equivalent indpendent of order and considering nulls
	 * @param fileDependencies1
	 * @param fileDependencies2
	 * @return
	 */
	private boolean equivalentFileDependencies(SPDXFile[] dependencies1,
			SPDXFile[] dependencies2) {
		if (dependencies1 == null) {
			return (dependencies2 == null);
		}
		if (dependencies2 == null) {
			return false;
		}
		if (dependencies1.length != dependencies2.length) {
			return false;
		}
		for (int i = 0; i < dependencies1.length; i++) {
			boolean found = false;
			for (int j = 0; j < dependencies2.length; j++) {
				if (dependencies1[i].equivalent(dependencies2[j])) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
}
