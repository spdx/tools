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
package org.spdx.rdfparser.license;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

/**
 * Factory for creating SPDXLicenseInfo objects from a Jena model
 * @author Gary O'Neall
 *
 */
public class LicenseInfoFactory {
	
	static final Logger logger = LoggerFactory.getLogger(LicenseInfoFactory.class.getName());
	
	public static final String NOASSERTION_LICENSE_NAME = "NOASSERTION";
	public static final String NONE_LICENSE_NAME = "NONE";

	
	/**
	 * Create the appropriate SPDXLicenseInfo from the model and node provided.
	 * The appropriate SPDXLicenseInfo subclass object will be chosen based on
	 * the class (rdf type) of the node.  If there is no rdf type, then the
	 * license ID is parsed to determine the type
	 * @param modelContainer
	 * @param node
	 * @return
	 */
	public static AnyLicenseInfo getLicenseInfoFromModel(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		if (!node.isURI() && !node.isBlank()) {
			throw(new InvalidSPDXAnalysisException("Can not create a LicenseInfo from a literal node"));
		}
		AnyLicenseInfo retval = null;
		// check to see if it is a "simple" type of license (NONESEEN, NONE, NOTANALYZED, or SPDX_LISTED_LICENSE)
		if (node.isURI()) {
			retval = getLicenseInfoByUri(modelContainer, node);
		}
		if (retval == null) {	// try by type
			retval = getLicenseInfoByType(modelContainer, node);
		}
		if (retval == null) {	// try by ID
			retval = getLicenseInfoById(modelContainer, node);
		}
		if (retval == null) {	// OK, we give up
			logger.error("Could not determine the type for a license");
			throw(new InvalidSPDXAnalysisException("Could not determine the type for a license"));
		}
		return retval;
	}
	
	/**
	 * Obtains an SPDX license by a URI - could be a listed license or a predefined license type
	 * @param document
	 * @param node
	 * @return License Info for the license or NULL if no external listed license info could be found
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static AnyLicenseInfo getLicenseInfoByUri(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		if (!node.isURI()) {
			return null;
		}
		if (node.getURI().equals(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.TERM_LICENSE_NONE)) {
			return new SpdxNoneLicense(modelContainer, node);
		} else if (node.getURI().equals(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.TERM_LICENSE_NOASSERTION)) {
			return new SpdxNoAssertionLicense(modelContainer, node);
		} else if (node.getURI().startsWith(ListedLicenses.LISTED_LICENSE_ID_URL)) {
			// try to fetch the listed license from the model
			try {
				return ListedLicenses.getListedLicenses().getLicenseFromStdLicModel(modelContainer, node);
			} catch (Exception ex) {
				logger.warn("Unable to get license from SPDX listed license model for "+node.getURI());
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * @param licenseId SPDX Listed License ID
	 * @return SPDX listed license or null if the ID is not in the SPDX license list
	 * @throws InvalidSPDXAnalysisException
	 */
	public static SpdxListedLicense getListedLicenseById(String licenseId)throws InvalidSPDXAnalysisException {
		return ListedLicenses.getListedLicenses().getListedLicenseById(licenseId);
	}

	/**
	 * @param document
	 * @param node
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static AnyLicenseInfo getLicenseInfoById(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		Node licenseIdPredicate = modelContainer.getModel().getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID).asNode();
		Triple m = Triple.createMatch(node, licenseIdPredicate, null);
		ExtendedIterator<Triple> tripleIter = modelContainer.getModel().getGraph().find(m);
		if (tripleIter.hasNext()) {
			Triple triple = tripleIter.next();
			String id = triple.getObject().toString(false);
			if (tripleIter.hasNext()) {
				throw(new InvalidSPDXAnalysisException("More than one ID associated with license "+id));
			}
			if (isSpdxListedLicenseID(id)) {
				return new SpdxListedLicense(modelContainer, node);
			} else if (id.startsWith(SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM)) {
				return new ExtractedLicenseInfo(modelContainer, node);
			} else {
				// could not determine the type from the ID
				// could be a conjunctive or disjunctive license ID
				return null;
			}
		} else {
			throw(new InvalidSPDXAnalysisException("No ID associated with a license"));
		}
	}

	/**
	 * @param modelContainer
	 * @param node
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static AnyLicenseInfo getLicenseInfoByType(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		// find the subclass
		Node rdfTypePredicate = modelContainer.getModel().getProperty(SpdxRdfConstants.RDF_NAMESPACE, 
				SpdxRdfConstants.RDF_PROP_TYPE).asNode();
		Triple m = Triple.createMatch(node, rdfTypePredicate, null);
		ExtendedIterator<Triple> tripleIter = modelContainer.getModel().getGraph().find(m);	// find the type(s)
		if (tripleIter.hasNext()) {
			Triple triple = tripleIter.next();
			if (tripleIter.hasNext()) {
				throw(new InvalidSPDXAnalysisException("More than one type associated with a licenseInfo"));
			}
			Node typeNode = triple.getObject();
			if (!typeNode.isURI()) {
				throw(new InvalidSPDXAnalysisException("Invalid type for licenseInfo - not a URI"));
			}
			// need to parse the URI
			String typeUri = typeNode.getURI();
			if (!typeUri.startsWith(SpdxRdfConstants.SPDX_NAMESPACE)) {
				throw(new InvalidSPDXAnalysisException("Invalid type for licenseInfo - not an SPDX type"));
			}
			String type = typeUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
			if (type.equals(SpdxRdfConstants.CLASS_SPDX_CONJUNCTIVE_LICENSE_SET)) {
				return new ConjunctiveLicenseSet(modelContainer, node);
			} else if (type.equals(SpdxRdfConstants.CLASS_SPDX_DISJUNCTIVE_LICENSE_SET)) {
				return new DisjunctiveLicenseSet(modelContainer, node);
			} else if (type.equals(SpdxRdfConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO)) {
				return new ExtractedLicenseInfo(modelContainer, node);
			} else if (type.equals(SpdxRdfConstants.CLASS_SPDX_LICENSE)) {
				return new SpdxListedLicense(modelContainer, node);
			} else if (type.equals(SpdxRdfConstants.CLASS_OR_LATER_OPERATOR)) {
				return new OrLaterOperator(modelContainer, node);
			} else if (type.equals(SpdxRdfConstants.CLASS_WITH_EXCEPTION_OPERATOR)) {
				return new WithExceptionOperator(modelContainer, node);
			} else {
				throw(new InvalidSPDXAnalysisException("Invalid type for licenseInfo '"+type+"'"));
			}
		} else {
			return null;
		}
	}

	/**
	 * Parses a license string and converts it into a SPDXLicenseInfo object
	 * Syntax - A license set must start and end with a parenthesis "("
	 * 			A conjunctive license set will have and AND after the first
	 *				licenseInfo term
	 * 			A disjunctive license set will have an OR after the first 
	 *				licenseInfo term
	 *			If there is no And or Or, then it is converted to a simple
	 *				license type
	 *			A space or tab must be used between license ID's and the 
	 *				keywords AND and OR
	 *			A licenseID must NOT be "AND" or "OR"
	 * @param licenseString String conforming to the syntax
	 * @param container Container containing any extractedLicenseInfos - if any extractedLicenseInfos by ID already exist, they will be used.  If
	 * none exist for an ID, they will be added.  If null, a simple Java object will be created for the extractedLicenseInfo.
	 * @return an SPDXLicenseInfo created from the string
	 * @throws InvalidLicenseStringException 
	 */
	public static AnyLicenseInfo parseSPDXLicenseString(String licenseString, SpdxDocumentContainer container) throws InvalidLicenseStringException {
		try {
			return LicenseExpressionParser.parseLicenseExpression(licenseString, container);
		} catch (LicenseParserException e) {
			throw new InvalidLicenseStringException(e.getMessage(),e);
		} catch (InvalidSPDXAnalysisException e) {
			throw new InvalidLicenseStringException("Unexpected SPDX error parsing license string");
		}
	}

	/**
	 * Parses a license string and converts it into a SPDXLicenseInfo object
	 * Syntax - A license set must start and end with a parenthesis "("
	 * 			A conjunctive license set will have and AND after the first
	 *				licenseInfo term
	 * 			A disjunctive license set will have an OR after the first 
	 *				licenseInfo term
	 *			If there is no And or Or, then it is converted to a simple
	 *				license type
	 *			A space or tab must be used between license ID's and the 
	 *				keywords AND and OR
	 *			A licenseID must NOT be "AND" or "OR"
	 * @param licenseString String conforming to the syntax
	 * @return an SPDXLicenseInfo created from the string
	 * @throws InvalidLicenseStringException 
	 */
	public static AnyLicenseInfo parseSPDXLicenseString(String licenseString) throws InvalidLicenseStringException {
		return parseSPDXLicenseString(licenseString, null);
	}



	/**
	 * @param licenseID
	 * @return true if the licenseID belongs to an SPDX listed license
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static boolean isSpdxListedLicenseID(String licenseID)  {
		return ListedLicenses.getListedLicenses().isSpdxListedLicenseID(licenseID);
	}
	
	/**
	 * @return Array of all SPDX listed license IDs
	 */
	public static String[] getSpdxListedLicenseIds() {
		return ListedLicenses.getListedLicenses().getSpdxListedLicenseIds();
	}
	
	/**
	 * @return Version of the license list being used by the SPDXLicenseInfoFactory
	 */
	public static String getLicenseListVersion() {
		return ListedLicenses.getListedLicenses().getLicenseListVersion();
	}

	/**
	 * @param id exception ID
	 * @return true if the exception ID is a supported SPDX listed exception
	 */
	public static boolean isSPdxListedExceptionID(String id) {
		return ListedExceptions.getListedExceptions().isSpdxListedLExceptionID(id);
	}

	/**
	 * @param id
	 * @return the standard SPDX license exception or null if the ID is not in the SPDX license list
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static LicenseException getListedExceptionById(String id) throws InvalidSPDXAnalysisException {
		return ListedExceptions.getListedExceptions().getListedExceptionById(id);
	}
}
