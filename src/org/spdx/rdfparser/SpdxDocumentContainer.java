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
package org.spdx.rdfparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.model.ExternalDocumentRef;
import org.spdx.rdfparser.model.RdfModelObject;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.rdfparser.model.SpdxElementFactory;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * This class contains the SPDX Document and provides some of the basic
 * RDF model support. This class also manages the SpdxRef, ExtractedLicenseInfos,
 * and LicenseRefs.
 * 
 * Separating the container aspects of the SpdxDocument into this separate
 * class allows for the SpdxDocument to follow the rdfparser.model pattern.
 * 
 * @author Gary O'Neall
 *
 */
public class SpdxDocumentContainer implements IModelContainer, SpdxRdfConstants {

	public static final String POINT_EIGHT_SPDX_VERSION = "SPDX-0.8";
	public static final String POINT_NINE_SPDX_VERSION = "SPDX-0.9";
	public static final String ONE_DOT_ZERO_SPDX_VERSION = "SPDX-1.0";
	public static final String ONE_DOT_ONE_SPDX_VERSION = "SPDX-1.1";
	public static final String ONE_DOT_TWO_SPDX_VERSION = "SPDX-1.2";
	public static final String TWO_POINT_ZERO_VERSION = "SPDX-2.0";
	public static final String CURRENT_SPDX_VERSION = TWO_POINT_ZERO_VERSION;
	
	public static final String CURRENT_IMPLEMENTATION_VERSION = "2.0.0";
	
	static HashSet<String> SUPPORTED_SPDX_VERSIONS = new HashSet<String>();	
	
	/**
	 * Keep track of all existing SPDX element references
	 */
	HashSet<String> spdxRefs = new HashSet<String>();
	
	/**
	 * Map of license ID to extracted license info
	 */
	HashMap<String, ExtractedLicenseInfo> licenseIdToExtractedLicense = 
			new HashMap<String, ExtractedLicenseInfo>();
	
	/**
	 * Map of external document ID's to external document references
	 */
	HashMap<String, ExternalDocumentRef> externalDocIdToRef = 
			new HashMap<String, ExternalDocumentRef>();
	/**
	 * Map of external document namespaces to external document references
	 */
	HashMap<String, ExternalDocumentRef> externalDocNamespaceToRef = 
			new HashMap<String, ExternalDocumentRef>();
	
	static {
		SUPPORTED_SPDX_VERSIONS.add(CURRENT_SPDX_VERSION);
		SUPPORTED_SPDX_VERSIONS.add(POINT_EIGHT_SPDX_VERSION);
		SUPPORTED_SPDX_VERSIONS.add(POINT_NINE_SPDX_VERSION);
		SUPPORTED_SPDX_VERSIONS.add(ONE_DOT_ZERO_SPDX_VERSION);
		SUPPORTED_SPDX_VERSIONS.add(ONE_DOT_ONE_SPDX_VERSION);
		SUPPORTED_SPDX_VERSIONS.add(ONE_DOT_TWO_SPDX_VERSION);
	}
	private Model model;
	private String documentNamespace;
	private Node documentNode;
	private SpdxDocument spdxDocument;
	
	/**
	 * Keeps tract of the next license reference number when generating the license ID's for
	 * non-standard licenses
	 */
	protected int nextLicenseRef = 1;
	/**
	 * Keeps track of the next SPDX element reference
	 */
	private int nextElementRef = 0;
	
	/**
	 * Construct an SpdxDocumentContainer from an existing model which
	 * already contain an SPDX Document
	 * @param model 
	 * @throws InvalidSPDXAnalysisException 
	 * 
	 */
	public SpdxDocumentContainer(Model model) throws InvalidSPDXAnalysisException {
		this.model = model;
		this.documentNode = getSpdxDocNode();
		if (this.documentNode == null) {
			throw(new InvalidSPDXAnalysisException("Invalid model - must contain an SPDX Document"));
		}
		if (!this.documentNode.isURI()) {
			throw(new InvalidSPDXAnalysisException("SPDX Documents must have a unique URI"));
		}
		String docUri = this.documentNode.getURI();
		this.documentNamespace = this.formDocNamespace(docUri);
		initializeExternalDocumentRefs();
		this.spdxDocument = new SpdxDocument(this, this.documentNode);
		initializeNextLicenseRef();
		initializeNextElementRef();
		if (this.spdxDocument.getDocumentDescribes() != null || 
				this.spdxDocument.getDocumentDescribes().length == 0) {
			upgradeDescribesToRelationship();
		}
	}
	
	/**
	 * @throws InvalidSPDXAnalysisException 
	 * 
	 */
	private void upgradeDescribesToRelationship() throws InvalidSPDXAnalysisException {
		Node p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_PACKAGE).asNode();
		Triple m = Triple.createMatch(this.documentNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		ArrayList<SpdxPackage> describedPackages = new ArrayList<SpdxPackage>();
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			describedPackages.add(new SpdxPackage(this, t.getObject()));
		}
		for (SpdxPackage pkg:describedPackages) {
			Relationship describes = new Relationship(pkg, 
					Relationship.RelationshipType.relationshipType_describes, "");
			this.getSpdxDocument().addRelationship(describes);
		}
	}

	/**
	 * Creates a new empty SPDX Document with the current SPDX document version.
	 * Note: Follow-up calls MUST be made to add the required properties for this
	 * to be a valid SPDX document
	 * @param uri URI for the SPDX Document
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SpdxDocumentContainer(String uri) throws InvalidSPDXAnalysisException {		
		this(uri, CURRENT_SPDX_VERSION);
	}
	
	/**
	 * Creates a new empty SPDX Document.
	 * Note: Follow-up calls MUST be made to add the required properties for this
	 * to be a valid SPDX document
	 * @param uri URI for the SPDX Document
	 * @param spdxVersion The version of SPDX analysis to create (impacts the data license for some versions)
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SpdxDocumentContainer(String uri, String spdxVersion) throws InvalidSPDXAnalysisException {
		this.model = ModelFactory.createDefaultModel();
		String v = verifySpdxVersion(spdxVersion);
		if (v != null) {
			throw(new InvalidSPDXAnalysisException("Invalid SPDX Version: "+v));
		}
		model.setNsPrefix("spdx", SPDX_NAMESPACE);
		model.setNsPrefix("doap", DOAP_NAMESPACE);
		model.setNsPrefix("rdfs", RDFS_NAMESPACE);
		model.setNsPrefix("rdf", RDF_NAMESPACE);
		this.documentNamespace = formDocNamespace(uri);
		model.setNsPrefix("", this.documentNamespace);
		// set the default namespace to the document namespace
		Resource spdxAnalysisType = model.createResource(SPDX_NAMESPACE+CLASS_SPDX_DOCUMENT);
		model.createResource(this.documentNamespace + SPDX_DOCUMENT_ID, spdxAnalysisType);
		this.addSpdxElementRef(SPDX_DOCUMENT_ID);
		// reset the next license number and next spdx element num
		this.nextElementRef = 1;
		this.nextLicenseRef = 1;
		this.documentNode = getSpdxDocNode();
		this.spdxDocument = new SpdxDocument(this, this.documentNode);
		// add the version
		this.spdxDocument.setSpecVersion(spdxVersion);
		// add the default data license
		if (!spdxVersion.equals(POINT_EIGHT_SPDX_VERSION) && !spdxVersion.equals(POINT_NINE_SPDX_VERSION)) { // added as a mandatory field in 1.0
			try {
				SpdxListedLicense dataLicense;
				if (spdxVersion.equals(ONE_DOT_ZERO_SPDX_VERSION)) 
					{ 
					dataLicense = (SpdxListedLicense)(LicenseInfoFactory.parseSPDXLicenseString(SPDX_DATA_LICENSE_ID_VERSION_1_0, this));
				} else {
					dataLicense = (SpdxListedLicense)(LicenseInfoFactory.parseSPDXLicenseString(SPDX_DATA_LICENSE_ID, this));				
				}
				spdxDocument.setDataLicense(dataLicense);
			} catch (InvalidLicenseStringException e) {
				throw new InvalidSPDXAnalysisException("Unable to create data license", e);
			}
		}
	}
	
	/**
	 * Form the document namespace URI from the SPDX document URI
	 * @param docUriString String form of the SPDX document URI
	 * @return
	 */
	private String formDocNamespace(String docUriString) {
		// just remove any fragments for the DOC URI
		int fragmentIndex = docUriString.indexOf('#');
		if (fragmentIndex <= 0) {
			return docUriString + "#";
		} else {
			return docUriString.substring(0, fragmentIndex) + "#";
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#getModel()
	 */
	@Override
	public Model getModel() {
		return model;
	}
	
	/**
	 * @return the spdx doc node from the model
	 */
	private Node getSpdxDocNode() {
		Node spdxDocNode = null;
		Node rdfTypePredicate = this.model.getProperty(RDF_NAMESPACE, RDF_PROP_TYPE).asNode();
		Node spdxDocObject = this.model.getProperty(SPDX_NAMESPACE, CLASS_SPDX_DOCUMENT).asNode();
		Triple m = Triple.createMatch(null, rdfTypePredicate, spdxDocObject);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	// find the document
		while (tripleIter.hasNext()) {
			Triple docTriple = tripleIter.next();
			spdxDocNode = docTriple.getSubject();
		}
		return spdxDocNode;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#getDocumentNamespace()
	 */
	@Override
	public String getDocumentNamespace() {
		return this.documentNamespace;
	}
	
	/**
	 * Initialize the next SPDX element reference used for creating new SPDX element URIs
	 */
	private void initializeNextElementRef() {
		int highestElementRef = 0;
		Triple m = Triple.createMatch(null, null, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	// find everything
		while (tripleIter.hasNext()) {
			// iterate through everything looking for matches to this SPDX document URI
			Triple trip = tripleIter.next();
			if (trip.getSubject().isURI()) {	// check the subject
				String subjectUri = trip.getSubject().getURI();
				if (subjectUri.startsWith(this.documentNamespace)) {
					String elementRef = subjectUri.substring(this.documentNamespace.length());
					this.spdxRefs.add(elementRef);
					if (SPDX_ELEMENT_REF_PATTERN.matcher(elementRef).matches()) {
						int elementRefNum = getElementRefNumber(elementRef);
						if (elementRefNum > highestElementRef) {
							highestElementRef = elementRefNum;
						}
					}
				}
			}
			if (trip.getObject().isURI()) {		// check the object
				String objectUri = trip.getObject().getURI();
				if (objectUri.startsWith(this.documentNamespace)) {
					String elementRef = objectUri.substring(this.documentNamespace.length());
					this.spdxRefs.add(elementRef);
					if (SPDX_ELEMENT_REF_PATTERN.matcher(elementRef).matches()) {
						int elementRefNum = getElementRefNumber(elementRef);
						if (elementRefNum > highestElementRef) {
							highestElementRef = elementRefNum;
						}
					}
				}
			}
		}
		
		this.nextElementRef = highestElementRef + 1;
	}

	/**
	 * Parses out the reference number for an SPDX element reference
	 * @param elementReference Element reference to parse
	 * @return element reference or -1 if the element reference is not valid
	 */
	public static int getElementRefNumber(String elementReference) {
		String numPart = elementReference.substring(SPDX_ELEMENT_REF_PRENUM.length());
		try {
			return Integer.parseInt(numPart);
		} catch(Exception ex) {
			return -1;
		}
	}
	
	/**
	 * Find an element within the container by the SPDX Identifier.  
	 * Returns null if the element does not exist in the container.
	 * @param id
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SpdxElement findElementById(String id) throws InvalidSPDXAnalysisException {
		if (SPDX_DOCUMENT_ID.equals(id)) {
			return this.spdxDocument;
		}
		String uri = null;
		if (id.contains(":")) {
			// external document reference
			String[] parts = id.split(":");
			if (parts.length != 2) {
				throw(new InvalidSPDXAnalysisException("Invalid SPDX ID: "+id+" - must be an SPDX element ID or SPDXDocument ID: SpdxElement ID"));
			}
			String nameSpace = this.externalDocumentIdToNamespace(parts[0]);
			if (nameSpace == null) {
				throw(new InvalidSPDXAnalysisException("No external document ref found for SPDX ID "+id));
			}
			uri = nameSpace + "#" + parts[1];
		} else if (!this.spdxElementRefExists(id)) {
			return null;
		} else {
			uri = this.getDocumentNamespace() + id;
		}
		Resource r = this.model.createResource(uri);
		return SpdxElementFactory.createElementFromModel(this, r.asNode());
	}


	
	public String verifySpdxVersion(String spdxVersion) {
		if (!spdxVersion.startsWith("SPDX-")) {
			return "Invalid spdx version - must start with 'SPDX-'";
		}
		Matcher docSpecVersionMatcher = SpdxRdfConstants.SPDX_VERSION_PATTERN.matcher(spdxVersion);
		if (!docSpecVersionMatcher.matches()) {
			return "Invalid spdx version format - must match 'SPDX-M.N'";
		}
		if (!SUPPORTED_SPDX_VERSIONS.contains(spdxVersion)) {
			return "Version "+spdxVersion+" is not supported by this version of the rdf parser";
		}
		return null;	// if we got here, there is no problem
	}

	/**
	 * @return
	 */
	public SpdxDocument getSpdxDocument() {
		return this.spdxDocument;
	}
	
	/**
	 * @return return the next available SPDX element reference.
	 */
	public String getNextSpdxElementRef() {
		int nextSpdxElementNum = this.getAndIncrementNextElementRef();
		String retval = formSpdxElementRef(nextSpdxElementNum);
		while (this.spdxElementRefExists(retval)) {
			nextSpdxElementNum = this.getAndIncrementNextLicenseRef();
			retval = formSpdxElementRef(nextSpdxElementNum);
		}
		this.spdxRefs.add(retval);
		return retval;
	}
	
	public static String formSpdxElementRef(int refNum) {
		return SPDX_ELEMENT_REF_PRENUM + String.valueOf(refNum);
	}
	
	/**
	 * @return
	 */
	synchronized int getAndIncrementNextElementRef() {
		int retval = this.nextElementRef;
		this.nextElementRef++;
		return retval;
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#SpdxElementRefExists(java.lang.String)
	 */
	@Override
	public boolean spdxElementRefExists(String elementRef) {
		return this.spdxRefs.contains(elementRef);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#addSpdxElementRef(java.lang.String)
	 */
	@Override
	public void addSpdxElementRef(String elementRef) throws InvalidSPDXAnalysisException {
		if (spdxElementRefExists(elementRef)) {
			throw(new InvalidSPDXAnalysisException("Duplicate SPDX element reference: "+elementRef));
		}
		this.spdxRefs.add(elementRef);
	}
	
	/**
	 * Initialize the next license reference and the cache of extracted license infos
	 * @throws InvalidSPDXAnalysisException
	 */
	public void initializeNextLicenseRef() throws InvalidSPDXAnalysisException {
		getExtractedLicenseInfosFromModel();	// this initializes the cache
		ExtractedLicenseInfo[] existingLicenses = getExtractedLicenseInfos();
		if (existingLicenses == null) {
			this.nextLicenseRef = 1;
			return;
		}
		int highestNonStdLicense = 0;
		for (int i = 0; i < existingLicenses.length; i++) {
			try {
				int idNum = getLicenseRefNum(existingLicenses[i].getLicenseId());
				if (idNum > highestNonStdLicense) {
					highestNonStdLicense = idNum;
				}
			} catch (NonNumericLicenseIdException ex) {
				// just continue
			}
		}	
		this.nextLicenseRef = highestNonStdLicense + 1;
	}

	/**
	 * @return next available license ID for an ExtractedLicenseInfo
	 */
	public synchronized String getNextLicenseRef() {
		int nextLicNum = this.getAndIncrementNextLicenseRef();
		String retval = formNonStandardLicenseID(nextLicNum);
		return retval;
	}
	
	/**
	 * Parses a license ID and return the integer representing the ID number (e.g. N in LicenseRef-N)
	 * Note that in SPDX 1.2, non-numeric license IDs are allowed. This method will throw a NonNumericException if
	 * a non numeric license ID passed as a licenseID parameter
	 * @param licenseID
	 * @return
	 * @throws NonNumericLicenseIdException If the non-standard license ID is not of the form LicenseRef-NN
	 */
	public int getLicenseRefNum(String licenseID) throws NonNumericLicenseIdException {
		Matcher matcher = LICENSE_ID_PATTERN_NUMERIC.matcher(licenseID);
		if (!matcher.matches()) {
			throw(new NonNumericLicenseIdException("Invalid license ID found in the non-standard licenses: '"+licenseID+"'"));
		}
		int numGroups = matcher.groupCount();
		if (numGroups != 1) {
			throw(new NonNumericLicenseIdException("Invalid license ID found in the non-standard licenses: '"+licenseID+"'"));
		}
		int idNum = Integer.decode(matcher.group(1));
		return idNum;
	}
	
	public static String formNonStandardLicenseID(int idNum) {
		return NON_STD_LICENSE_ID_PRENUM + String.valueOf(idNum);
	}
	
	synchronized int getAndIncrementNextLicenseRef() {
		int retval = this.nextLicenseRef;
		this.nextLicenseRef++;
		return retval;
	}
	
	/**
	 * Adds a new non-standard license containing the text provided.  Forms the license ID
	 * from the next License ID available
	 * @param licenseText
	 * @return the newly created NonStandardLicense
	 * @throws InvalidSPDXAnalysisException
	 */
	public ExtractedLicenseInfo addNewExtractedLicenseInfo(String licenseText) throws InvalidSPDXAnalysisException {
		String licenseID = getNextLicenseRef();
		ExtractedLicenseInfo retval = new ExtractedLicenseInfo(licenseID, licenseText);
		addNewExtractedLicenseInfo(retval);
		return retval;
	}

	
	/**
	 * Adds the license as a new ExtractedLicenseInfo
	 * @param license
	 * @throws InvalidSPDXAnalysisException
	 */
	public void addNewExtractedLicenseInfo(ExtractedLicenseInfo license) throws InvalidSPDXAnalysisException {
		if (extractedLicenseExists(license.getLicenseId())) {
			throw(new InvalidSPDXAnalysisException("Can not add license - ID "+license.getLicenseId()+" already exists."));
		}
		Property p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_EXTRACTED_LICENSES);
		Resource s = getResource(getSpdxDocNode());
		s.addProperty(p, license.createResource(this));		
		this.licenseIdToExtractedLicense.put(license.getLicenseId(), license);
	}
	
	/**
	 * @param id
	 * @return true if the license ID is already in the model as an extracted license info
	 * @throws InvalidSPDXAnalysisException 
	 */
	public boolean extractedLicenseExists(String id) throws InvalidSPDXAnalysisException {
		return this.licenseIdToExtractedLicense.containsKey(id);
	}
	
	/**
	 * @param id
	 * @return true if the license ID is already in the model as an extracted license info
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ExtractedLicenseInfo getExtractedLicense(String id) throws InvalidSPDXAnalysisException {
		return this.licenseIdToExtractedLicense.get(id);
	}
	
	/**
	 * Get Update the extrated license infos from the model and resynchronize it with
	 * the license cache.
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void getExtractedLicenseInfosFromModel() throws InvalidSPDXAnalysisException {
		AnyLicenseInfo[] extractedAnyLicenseInfo = this.spdxDocument.findAnyLicenseInfoPropertyValues(
				SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_SPDX_EXTRACTED_LICENSES);
		this.licenseIdToExtractedLicense.clear();
		
		for (int i = 0; i < extractedAnyLicenseInfo.length; i++) {
			if (!(extractedAnyLicenseInfo[i] instanceof ExtractedLicenseInfo)) {
				throw new InvalidSPDXAnalysisException("Invalid type for extracted license infos");
			}
			ExtractedLicenseInfo lic = (ExtractedLicenseInfo)extractedAnyLicenseInfo[i];
			this.licenseIdToExtractedLicense.put(lic.getLicenseId(), lic);
		}
	}

	/**
	 * @return
	 */
	public ExtractedLicenseInfo[] getExtractedLicenseInfos() {
		return this.licenseIdToExtractedLicense.values().toArray(
				new ExtractedLicenseInfo[this.licenseIdToExtractedLicense.values().size()]);
	}

	/**
	 * @param extractedLicenseInfos
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setExtractedLicenseInfos(
			ExtractedLicenseInfo[] extractedLicenseInfos) throws InvalidSPDXAnalysisException {
		this.spdxDocument.setPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_SPDX_EXTRACTED_LICENSES, extractedLicenseInfos);
		this.initializeNextLicenseRef();
	}

	/**
	 * @param license
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void addExtractedLicenseInfos(ExtractedLicenseInfo license) throws InvalidSPDXAnalysisException {
		if (license == null) {
			return;
		}
		try {
			int idNum = getLicenseRefNum(license.getLicenseId());
			if (idNum >= this.nextLicenseRef) {
				this.nextLicenseRef = idNum + 1;
			}
		} catch (NonNumericLicenseIdException ex) {
			// just continue
		}
		this.licenseIdToExtractedLicense.put(license.getLicenseId(), license);
		this.spdxDocument.addPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_SPDX_EXTRACTED_LICENSES, license);
	}
	
	private Resource getResource(Node node) throws InvalidSPDXAnalysisException {
		Resource s;
		if (node.isURI()) {
			s = model.createResource(node.getURI());
		} else if (node.isBlank()) {
			s = model.createResource(node.getBlankNodeId());
		} else {
			throw(new InvalidSPDXAnalysisException("Node can not be a literal"));
		}
		return s;
	}

	/**
	 * get all file references contained within the container
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SpdxFile[] getFileReferences() throws InvalidSPDXAnalysisException {
		ArrayList<SpdxFile> alFiles = new ArrayList<SpdxFile>();
		Node rdfTypeNode = model.getProperty(SpdxRdfConstants.RDF_NAMESPACE, 
				SpdxRdfConstants.RDF_PROP_TYPE).asNode();
		String fileTypeUri = SPDX_NAMESPACE + CLASS_SPDX_FILE;
		Node fileTypeNode = model.getResource(fileTypeUri).asNode();
		Triple m = Triple.createMatch(null, rdfTypeNode, fileTypeNode);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alFiles.add(new SpdxFile(this, t.getSubject()));
		}
		SpdxFile[] retval = new SpdxFile[alFiles.size()];
		return alFiles.toArray(retval);
	}

	private void initializeExternalDocumentRefs() throws InvalidSPDXAnalysisException {
		this.initializeExternalDocumentRefs(
				RdfModelObject.findExternalDocRefPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_SPDX_EXTERNAL_DOC_REF, this, documentNode));
	}
	
	private void initializeExternalDocumentRefs(ExternalDocumentRef[] externalDocumentRefs) throws InvalidSPDXAnalysisException {
		this.externalDocIdToRef.clear();
		this.externalDocNamespaceToRef.clear();
		if (externalDocumentRefs == null) {
			return;
		}
		for (int i = 0; i < externalDocumentRefs.length; i++) {
			this.externalDocIdToRef.put(externalDocumentRefs[i].getExternalDocumentId(), 
					externalDocumentRefs[i]);
			this.externalDocNamespaceToRef.put(externalDocumentRefs[i].getSpdxDocumentNamespace(), 
					externalDocumentRefs[i]);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#documentNamespaceToId(java.lang.String)
	 */
	@Override
	public String documentNamespaceToId(String externalNamespace) {
		ExternalDocumentRef ref = this.externalDocNamespaceToRef.get(externalNamespace);
		if (ref == null) {
			return null;
		} else {
			return ref.getExternalDocumentId();
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#externalDocumentIdToNamespace(java.lang.String)
	 */
	@Override
	public String externalDocumentIdToNamespace(String docId) {
		ExternalDocumentRef ref = this.externalDocIdToRef.get(docId);
		if (ref == null) {
			return null;
		} else {
			return ref.getSpdxDocumentNamespace();
		}
	}

	/**
	 * @return external document refs
	 */
	public ExternalDocumentRef[] getExternalDocumentRefs() {
		return this.externalDocIdToRef.values().toArray(
						new ExternalDocumentRef[this.externalDocIdToRef.values().size()]);
	}

	/**
	 * @param externalDocumentRefs
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setExternalDocumentRefs(
			ExternalDocumentRef[] externalDocumentRefs) throws InvalidSPDXAnalysisException {
		initializeExternalDocumentRefs(externalDocumentRefs);
		this.spdxDocument.setPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_SPDX_EXTERNAL_DOC_REF, externalDocumentRefs);
	}
	
	public List<SpdxPackage> findAllPackages() throws InvalidSPDXAnalysisException {
		Node rdfTypePredicate = model.getProperty(SpdxRdfConstants.RDF_NAMESPACE, 
				SpdxRdfConstants.RDF_PROP_TYPE).asNode();
		Node packageTypeObject = model.createResource(SPDX_NAMESPACE + CLASS_SPDX_PACKAGE).asNode();
		Triple m = Triple.createMatch(null, rdfTypePredicate, packageTypeObject);
		ArrayList<SpdxPackage> retval = new ArrayList<SpdxPackage>();
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			retval.add((SpdxPackage)SpdxElementFactory.createElementFromModel(this, tripleIter.next().getSubject()));
		}
		return retval;
	}
	
	public List<SpdxFile> findAllFiles() throws InvalidSPDXAnalysisException {
		Node rdfTypePredicate = model.getProperty(SpdxRdfConstants.RDF_NAMESPACE, 
				SpdxRdfConstants.RDF_PROP_TYPE).asNode();
		Node packageTypeObject = model.createResource(SPDX_NAMESPACE + CLASS_SPDX_FILE).asNode();
		Triple m = Triple.createMatch(null, rdfTypePredicate, packageTypeObject);
		ArrayList<SpdxFile> retval = new ArrayList<SpdxFile>();
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			retval.add((SpdxFile)SpdxElementFactory.createElementFromModel(this, tripleIter.next().getSubject()));
		}
		return retval;
	}

	/**
	 * Add an SPDX element directly to the model without connecting it to any properties
	 * @param element
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void addElement(SpdxElement element) throws InvalidSPDXAnalysisException {
		element.createResource(this, true);
	}

	/**
	 * Returns all elements in the contains
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public List<SpdxElement> findAllElements() throws InvalidSPDXAnalysisException {
		// NOTE: This needs to be updated for any new types
		List<SpdxElement> retval = new ArrayList<SpdxElement>();
		retval.add(this.spdxDocument);
		retval.addAll(this.findAllFiles());
		retval.addAll(this.findAllPackages());
		return retval;
	}
}
