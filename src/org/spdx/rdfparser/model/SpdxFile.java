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

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfParserHelper;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.SimpleLicensingInfo;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * A File represents a named sequence of information 
 * that is contained in a software package.
 * @author Gary O'Neall
 *
 */
public class SpdxFile extends SpdxItem implements Comparable<SpdxFile> {
	
	static final Logger logger = Logger.getLogger(SpdxFile.class.getName());
	
	enum FileType {fileType_application, fileType_archive,
		fileType_audio, fileType_binary, fileType_documentation,
		fileType_image, fileType_other, fileType_source, fileType_spdx,
		fileType_text, fileType_video};
	FileType fileType;
	Checksum checksum;
	String[] fileContributors;
	String noticeText;
	String id;
	DoapProject[] artifactOf;
	SpdxFile[] fileDependencies;
	

	/**
	 * @param id SPDX Identifier for the file.  Must be unique within the modelContainer
	 * @param name fileName
	 * @param comment Comment on the file
	 * @param annotations annotations for the file
	 * @param relationships Relationships to this file
	 * @param licenseConcluded
	 * @param licenseInfoInFile
	 * @param copyrightText
	 * @param licenseComment
	 */
	public SpdxFile(String id, String name, String comment, Annotation[] annotations,
			Relationship[] relationships, AnyLicenseInfo licenseConcluded,
			SimpleLicensingInfo[] licenseInfoInFile, String copyrightText,
			String licenseComment, FileType fileType, Checksum checksum,
			String[] fileContributors, String noticeText, DoapProject[] artifactOf) {
		super(name, comment, annotations, relationships, 
				licenseConcluded, convertDeclaredLicense(licenseInfoInFile),
				copyrightText, licenseComment);
		this.id = id;
		this.fileType = fileType;
		this.checksum = checksum;
		this.fileContributors = fileContributors;
		if (this.fileContributors == null) {
			this.fileContributors = new String[0];
		}
		this.noticeText = noticeText;
		this.fileDependencies = new SpdxFile[0];
		this.artifactOf = artifactOf;
		if (this.artifactOf == null) {
			this.artifactOf = new DoapProject[0];
		}
	}
	
	static AnyLicenseInfo convertDeclaredLicense(SimpleLicensingInfo[] licenseInfoInFile) {
		if (licenseInfoInFile == null) {
			return null;
		} else if (licenseInfoInFile.length == 0) {
			return null;
		} else if (licenseInfoInFile.length == 1) {
			return licenseInfoInFile[0];
		} else {
			return new DisjunctiveLicenseSet(licenseInfoInFile);
		}
	}
	
	static SimpleLicensingInfo[] convertLicenseInfoFromFile(AnyLicenseInfo declaredLicense) throws InvalidSPDXAnalysisException {
		if (declaredLicense == null) {
			return new SimpleLicensingInfo[0];
		}
		if (declaredLicense instanceof SimpleLicensingInfo) {
			return new SimpleLicensingInfo[] {(SimpleLicensingInfo)declaredLicense};
		} else if (declaredLicense instanceof DisjunctiveLicenseSet) {
			AnyLicenseInfo[] members = ((DisjunctiveLicenseSet)declaredLicense).getMembers();
			SimpleLicensingInfo[] retval = new SimpleLicensingInfo[members.length];
			for (int i = 0; i < members.length; i++) {
				if (!(members[i] instanceof SimpleLicensingInfo)) {
					throw (new InvalidSPDXAnalysisException("Can not convert a complex license to license infos from file: "+declaredLicense.toString()));
				}
				retval[i] = (SimpleLicensingInfo)members[i];
			}
			return retval;
		} else {
			throw (new InvalidSPDXAnalysisException("Can not convert a complex license to license infos from file: "+declaredLicense.toString()));
		}
	}

	/**
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxFile(IModelContainer modelContainer, Node node)
			throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		String fileTypeUri = findUriPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_FILE_TYPE);
		if (fileTypeUri != null && !fileTypeUri.isEmpty()) {
			try {
				String fileTypeS = fileTypeUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
				this.fileType = FileType.valueOf(fileTypeS);
			} catch (Exception ex) {
				throw(new InvalidSPDXAnalysisException("Invalid file type: "+fileTypeUri));
			}
		}
		this.checksum = findChecksumPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_FILE_CHECKSUM);
		this.fileContributors = findMultiplePropertyValues(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_FILE_CONTRIBUTOR);
		this.noticeText = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_FILE_NOTICE);
		// ID
		if (this.resource.isURIResource()) {
			if (this.resource.getURI().startsWith(modelContainer.getDocumentNamespace())) {
				this.id = this.resource.getURI().substring(modelContainer.getDocumentNamespace().length());
			}
		}
		// File dependencies
		SpdxElement[] fileDependencyElements = findMultipleElementPropertyValues(
				SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_FILE_DEPENDENCY);
		int count = 0;
		if (fileDependencyElements != null) {
			for (int i = 0; i < fileDependencyElements.length; i++) {
				if (fileDependencyElements[i] instanceof SpdxFile) {
					count++;
				}
			}
		}
		if (count > 0) {
			this.fileDependencies = new SpdxFile[count];
			int j = 0;
			for (int i = 0; i < fileDependencyElements.length; i++) {
				if (fileDependencyElements[i] instanceof SpdxFile) {
					this.fileDependencies[j++] = (SpdxFile)fileDependencyElements[i];
				}
			}
		}
		// ArtifactOfs
		this.artifactOf = findMultipleDoapPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_FILE_ARTIFACTOF);
	}

	/**
	 * Finds the resource for an existing file in the model
	 * @param spdxFile
	 * @return resource of an SPDX file with the same name and checksum.  Null if none found
	 * @throws InvalidSPDXAnalysisException 
	 */
	static protected Resource findFileResource(IModelContainer modelContainer, SpdxFile spdxFile) throws InvalidSPDXAnalysisException {
		// find any matching file names
		Model model = modelContainer.getModel();
		Node fileNameProperty = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_NAME).asNode();
		Triple fileNameMatch = Triple.createMatch(null, fileNameProperty, Node.createLiteral(spdxFile.getName()));
		
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
				Checksum cksum = new Checksum(modelContainer, checksumMatchTriple.getObject());
				if (cksum.getValue().compareToIgnoreCase(spdxFile.getChecksum().getValue()) == 0) {
					return RdfParserHelper.convertToResource(model, fileNode);
				}
			}
		}
		// if we get to here, we did not find a match
		return null;
	}
	
	@Override
	protected Resource findDuplicateResource(IModelContainer modelContainer, String uri) throws InvalidSPDXAnalysisException {
		// see if we want to change what is considered a duplicate
		// currently, a file is considered a duplicate if the checksum and filename
		// are the same.
		return findFileResource(modelContainer, this);
	}


	@Override
	protected void populateModel() throws InvalidSPDXAnalysisException {
		super.populateModel();
		if (this.fileType == null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_FILE_TYPE);
		} else {
			setPropertyUriValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_FILE_TYPE, 
				SpdxRdfConstants.SPDX_NAMESPACE + this.fileType.toString());
		}
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_FILE_CHECKSUM, this.checksum);		
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
			SpdxRdfConstants.PROP_FILE_CONTRIBUTOR, this.fileContributors);
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
			SpdxRdfConstants.PROP_FILE_NOTICE, noticeText);
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
			SpdxRdfConstants.PROP_FILE_ARTIFACTOF, artifactOf);
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
			SpdxRdfConstants.PROP_FILE_FILE_DEPENDENCY, fileDependencies);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	String getUri(IModelContainer modelContainer) {
		if (this.id != null && !this.id.isEmpty()) {
			return modelContainer.getDocumentNamespace() + this.id;
		} else {
			return null;
		}
	}
	
	@Override
	protected String getLicenseDeclaredPropertyName() {
		return SpdxRdfConstants.PROP_FILE_SEEN_LICENSE;
	}
	
	@Override
	protected String getNamePropertyName() {
		return SpdxRdfConstants.PROP_FILE_NAME;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_FILE);
	}
	
	/**
	 * @return the fileType
	 */
	public FileType getFileType() {
		return fileType;
	}

	/**
	 * @param fileType the fileType to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setFileType(FileType fileType) throws InvalidSPDXAnalysisException {
		this.fileType = fileType;
		if (fileType == null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_FILE_TYPE);
		} else {
			setPropertyUriValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_FILE_TYPE, 
					SpdxRdfConstants.SPDX_NAMESPACE + this.fileType.toString());
		}
	}

	/**
	 * @return the checksum
	 */
	public Checksum getChecksum() {
		return checksum;
	}

	/**
	 * @param checksum the checksum to set
	 */
	public void setChecksum(Checksum checksum) {
		this.checksum = checksum;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_FILE_CHECKSUM, this.checksum);
	}

	/**
	 * @return the fileContributors
	 */
	public String[] getFileContributors() {
		return fileContributors;
	}

	/**
	 * @param fileContributors the fileContributors to set
	 */
	public void setFileContributors(String[] fileContributors) {	
		if (fileContributors == null) {
			this.fileContributors = new String[0];
		} else {
			this.fileContributors = fileContributors;
		}
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_FILE_CONTRIBUTOR, fileContributors);
	}

	/**
	 * @return the noticeText
	 */
	public String getNoticeText() {
		return noticeText;
	}

	/**
	 * @param noticeText the noticeText to set
	 */
	public void setNoticeText(String noticeText) {
		this.noticeText = noticeText;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_FILE_NOTICE, noticeText);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setId(String id) throws InvalidSPDXAnalysisException {
		if (this.model != null) {
			throw(new InvalidSPDXAnalysisException("Can not set a file ID for a file already in an RDF Model. You must create a new SPDX File with this ID."));
		}
		this.id = id;
	}
	
		/**
	 * @return the artifactOf
	 */
	public DoapProject[] getArtifactOf() {
		return artifactOf;
	}

	/**
	 * @param artifactOf the artifactOf to set
	 */
	public void setArtifactOf(DoapProject[] artifactOf) {
		if (artifactOf == null) {
			this.artifactOf = new DoapProject[0];
		} else {
			this.artifactOf = artifactOf;
		}
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_FILE_ARTIFACTOF, this.artifactOf);
	}

	/**
	 * This method should no longer be used.  The Relationship property should be used in its place.
	 * @return the fileDependencies
	 */
	@Deprecated
	public SpdxFile[] getFileDependencies() {
		return fileDependencies;
	}

	/**
	 * This method should no longer be used.  The Relationship property should be used in its place.
	 * @param fileDependencies the fileDependencies to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Deprecated
	public void setFileDependencies(SpdxFile[] fileDependencies) throws InvalidSPDXAnalysisException {
		if (fileDependencies == null) {
			this.fileDependencies = new SpdxFile[0];
		} else {
			this.fileDependencies = fileDependencies;
		}		
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_FILE_FILE_DEPENDENCY, this.fileDependencies);
	}

	@Override
	public boolean equivalent(RdfModelObject o) {
		if (!(o instanceof SpdxFile)) {
			return false;
		}
		SpdxFile comp = (SpdxFile)o;
		if (!super.equivalent(comp)) {
			return false;
		}
		// compare based on properties
		return (equalsConsideringNull(this.id, comp.getId()) &&
				equivalentConsideringNull(this.checksum, comp.getChecksum()) &&
				equalsConsideringNull(this.fileType, comp.getFileType()) &&
				arraysEqual(this.fileContributors, comp.getFileContributors()) &&
				arraysEquivalent(this.artifactOf, comp.getArtifactOf()) &&
				arraysEquivalent(this.fileDependencies, comp.getFileDependencies()) &&
				equalsConsideringNull(this.noticeText, comp.getNoticeText()));
	}
	
	protected Checksum cloneChecksum() {
		if (checksum == null) {
			return null;
		}
		return checksum.clone();
	}
	
	protected DoapProject[] cloneArtifactOf() {
		if (this.artifactOf == null) {
			return null;
		}
		DoapProject[] retval = new DoapProject[this.artifactOf.length];
		for (int i = 0; i < this.artifactOf.length; i++) {
			retval[i] = artifactOf[i].clone();
		}
		return retval;
	}
	
	public SpdxFile[] cloneFileDependencies() {
		if (this.fileDependencies == null) {
			return null;
		}
		SpdxFile[] retval = new SpdxFile[this.fileDependencies.length];
		for (int i = 0; i < this.fileDependencies.length; i++) {
			retval[i] = this.fileDependencies[i].clone();
		}
		return retval;
	}
	
	@Override public SpdxFile clone() {
		//TODO Determine if we should clone the ID - Currently we are setting this to null
		SpdxFile retval;
		SimpleLicensingInfo[] licenseInfosFromFiles;
		try {
			licenseInfosFromFiles = convertLicenseInfoFromFile(licenseDeclared);
			retval = new SpdxFile(null, name, comment, cloneAnnotations(),
					cloneRelationships(), cloneLicenseConcluded(),
					licenseInfosFromFiles, copyrightText,
					licenseComment, fileType, cloneChecksum(),
					fileContributors, noticeText, cloneArtifactOf());
		} catch (InvalidSPDXAnalysisException e) {
			// workaround for a declared license which is not compatible
			retval = new SpdxFile(null, name, comment, cloneAnnotations(),
					cloneRelationships(), cloneLicenseConcluded(),
					null, copyrightText,
					licenseComment, fileType, cloneChecksum(),
					fileContributors, noticeText, cloneArtifactOf());
			try {
				retval.setLicenseDeclared(this.licenseDeclared.clone());
			} catch (InvalidSPDXAnalysisException e1) {
				logger.error("Error setting declared license on clone",e1);
			}
		}
		if (this.fileDependencies != null) {
			try {
				retval.setFileDependencies(fileDependencies);
			} catch (InvalidSPDXAnalysisException e1) {
				logger.warn("Error setting file dependencies on clone", e1);
			}
		}
		return retval;
	}
	
	@Override
	public ArrayList<String> verify() {
		ArrayList<String> retval = super.verify();
		String fileName = this.getName();
		if (fileName == null) {
			fileName = "UNKNOWN";
		}
		if (fileType == null) {
			retval.add("Missing required file type for file "+fileName);
		}
		if (copyrightText == null || copyrightText.isEmpty()) {
			retval.add("Missing required copyright text for file "+fileName);
		}
		if (licenseConcluded == null) {
			retval.add("Missing required concluded license for file "+fileName);
		} else {
			retval.addAll(licenseConcluded.verify());
		}
		if (licenseDeclared == null) {
			retval.add("Missing required license info in filee for file "+fileName);
		} else {
			retval.addAll(licenseDeclared.verify());
		}
		if (checksum == null) {
			retval.add("Missing required checksum for file "+fileName);
		} else {
			retval.addAll(checksum.verify());
		}
		DoapProject[] projects = this.getArtifactOf();
		if (projects != null) {
			for (int i = 0;i < projects.length; i++) {
				retval.addAll(projects[i].verify());
			}
		}	
		// fileDependencies
		if (fileDependencies != null) {
			for (int i = 0; i < fileDependencies.length; i++) {
				ArrayList<String> verifyFileDependency = fileDependencies[i].verify();
				for (int j = 0; j < verifyFileDependency.size(); j++) {
					retval.add("Invalid file dependency for file named "+
							fileDependencies[i].getName()+": "+verifyFileDependency.get(j));
				}
			}
		}
		return retval;
	}
	
    /**
     * This method is used for sorting a list of SPDX files
     * @param file SPDXFile that is compared
     * @return 
     */
    @Override
    public int compareTo(SpdxFile file) {
        return this.getName().compareTo(file.getName());        
    }

}
