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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Factory for creating SPDXLicenseInfo objects from a Jena model
 * @author Gary O'Neall
 *
 */
public class LicenseInfoFactory {
	
	
	public static final String DEFAULT_LICENSE_LIST_VERSION = "2.0";
	static final Logger logger = Logger.getLogger(LicenseInfoFactory.class.getName());
	static final String LISTED_LICENSE_ID_URL = "http://spdx.org/licenses/";
	
	public static final String NOASSERTION_LICENSE_NAME = "NOASSERTION";
	public static final String NONE_LICENSE_NAME = "NONE";

	public static final String LISTED_LICENSE_URI_PREFIX = "http://spdx.org/licenses/";
	private static final String LISTED_LICENSE_RDF_LOCAL_DIR = "resources" + "/" + "stdlicenses";

	private static final String LISTED_LICENSE_RDF_LOCAL_FILENAME = LISTED_LICENSE_RDF_LOCAL_DIR + "/" + "index.html";
	private static final String LISTED_LICENSE_PROPERTIES_FILENAME = LISTED_LICENSE_RDF_LOCAL_DIR + "/" + "licenses.properties";
	
	private static Model listedLicenseModel = null;
	
	static final HashSet<String> LISTED_LICENSE_ID_SET = new HashSet<String>();
	
	static HashMap<String, SpdxListedLicense> LISTED_LICENSES = null;
    
	private static final Properties licenseProperties = loadLicenseProperties();
    private static final boolean onlyUseLocalLicenses = Boolean.parseBoolean(
            System.getProperty("SPDXParser.OnlyUseLocalLicenses", licenseProperties.getProperty("OnlyUseLocalLicenses", "false")));

    static String LICENSE_LIST_VERSION = DEFAULT_LICENSE_LIST_VERSION;
	static {
		loadListedLicenseIDs();		
	}
	
	/**
	 * Create the appropriate SPDXLicenseInfo from the model and node provided.
	 * The appropriate SPDXLicenseInfo subclass object will be chosen based on
	 * the class (rdf type) of the node.  If there is no rdf type, then the
	 * license ID is parsed to determine the type
	 * @param model
	 * @param node
	 * @return
	 */
	public static AnyLicenseInfo getLicenseInfoFromModel(Model model, Node node) throws InvalidSPDXAnalysisException {
		if (!node.isURI() && !node.isBlank()) {
			throw(new InvalidSPDXAnalysisException("Can not create a LicenseInfo from a literal node"));
		}
		AnyLicenseInfo retval = null;
		// check to see if it is a "simple" type of license (NONESEEN, NONE, NOTANALYZED, or SPDX_LISTED_LICENSE)
		if (node.isURI()) {
			retval = getLicenseInfoByUri(model, node);
		}
		if (retval == null) {	// try by type
			retval = getLicenseInfoByType(model, node);
		}
		if (retval == null) {	// try by ID
			retval = getLicenseInfoById(model, node);
		}
		if (retval == null) {	// OK, we give up
			logger.error("Could not determine the type for a license");
			throw(new InvalidSPDXAnalysisException("Could not determine the type for a license"));
		}
		return retval;
	}
	
	/**
	 * Obtains an SPDX license by a URI - could be a listed license or a predefined license type
	 * @param model
	 * @param node
	 * @return License Info for the license or NULL if no external listed license info could be found
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static AnyLicenseInfo getLicenseInfoByUri(Model model, Node node) throws InvalidSPDXAnalysisException {
		if (!node.isURI()) {
			return null;
		}
		if (node.getURI().equals(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.TERM_LICENSE_NONE)) {
			return new SpdxNoneLicense(model, node);
		} else if (node.getURI().equals(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.TERM_LICENSE_NOASSERTION)) {
			return new SpdxNoAssertionLicense(model, node);
		} else if (node.getURI().startsWith(LISTED_LICENSE_URI_PREFIX)) {
			// try to fetch the listed license from the model
			try {
				return getLicenseFromStdLicModel(node.getURI());
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
	 * @return SPDX listed license
	 * @throws InvalidSPDXAnalysisException
	 */
	public static SpdxListedLicense getListedLicenseById(String licenseId)throws InvalidSPDXAnalysisException {
		return getLicenseFromStdLicModel(LISTED_LICENSE_URI_PREFIX + licenseId);
	}
	
	/**
	 * @param uri
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected static SpdxListedLicense getLicenseFromStdLicModel(String uri) throws InvalidSPDXAnalysisException {
		URL licenseUrl = null;
		try {
			licenseUrl = new URL(uri);
		} catch (MalformedURLException e) {
			throw new InvalidSPDXAnalysisException("Invalid listed license URL: "+e.getMessage());
		}
		String[] pathParts = licenseUrl.getFile().split("/");
		String id = pathParts[pathParts.length-1];
		if (LISTED_LICENSES.containsKey(id)) {
			return LISTED_LICENSES.get(id);
		}
		String base = LISTED_LICENSE_ID_URL + id;
		Model licenseModel = getLicenseModel(uri, base);
		if (licenseModel == null) {
			throw(new InvalidSPDXAnalysisException("No listed license was found at "+uri));
		}
		Resource licResource = licenseModel.getResource(base);
		if (licResource == null || !licenseModel.containsResource(licenseModel.asRDFNode(licResource.asNode()))) {
			throw(new InvalidSPDXAnalysisException("No listed license was found at "+uri));
		}
		SpdxListedLicense retval = new SpdxListedLicense(licenseModel, licResource.asNode());
		LISTED_LICENSES.put(id, retval);
		return retval;
	}

	/**
	 * @param uri - URI of the actual resource
	 * @param base - base for any fragments present in the license model
	 * @return
	 * @throws NoListedLicenseRdfModel 
	 */
	private static Model getLicenseModel(String uri, String base) throws NoListedLicenseRdfModel {
		try {
			Class.forName("net.rootdev.javardfa.jena.RDFaReader");
		} catch(java.lang.ClassNotFoundException e) {
			throw(new NoListedLicenseRdfModel("Could not load the RDFa reader for licenses.  This could be caused by an installation problem - missing java-rdfa jar file"));
		}  
		Model retval = ModelFactory.createDefaultModel();
		InputStream in = null;
		try {
			try {
				if (!onlyUseLocalLicenses) {
				    in = FileManager.get().open(uri);
					try {
						retval.read(in, base, "HTML");
						Property p = retval.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID);
				    	if (retval.isEmpty() || !retval.contains(null, p)) {
					    	try {
								in.close();
							} catch (IOException e) {
								logger.warn("Error closing listed license input");
							}
					    	in = null;
				    	}
					} catch(Exception ex) {

						if (in != null) {
							in.close();
							in = null;
						}
					}
				}
			} catch(Exception ex) {
				in = null;
				logger.warn("Unable to open SPDX listed license model.  Using local file copy for SPDX listed licenses");
			}
			if (in == null) {
				// need to fetch from the local file system
				String id = uri.substring(LISTED_LICENSE_URI_PREFIX.length());
				String fileName = LISTED_LICENSE_RDF_LOCAL_DIR + "/" + id;
				in = LicenseInfoFactory.class.getResourceAsStream("/" + fileName);
				if (in == null) {
					throw(new NoListedLicenseRdfModel("SPDX listed license "+uri+" could not be read."));
				}
				try {
					retval.read(in, base, "HTML");
				} catch(Exception ex) {
					throw(new NoListedLicenseRdfModel("Error reading the spdx listed licenses: "+ex.getMessage(),ex));
				}
			}
			return retval;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.warn("Unable to close model input stream");
				}
			}
		}
	}
	
	private static Model getListedLicenseModel() throws InvalidSPDXAnalysisException {
		if (listedLicenseModel == null) {
			loadListedLicenseModel();
		}
		return listedLicenseModel;
	}

	/**
	 * Load an spdx listed license model from the index page
	 */
	private static void loadListedLicenseModel() throws InvalidSPDXAnalysisException {
		try {
			Class.forName("net.rootdev.javardfa.jena.RDFaReader");
		} catch(java.lang.ClassNotFoundException e) {
			logger.warn("Unable to load Java RDFa reader");
		}  

		Model myStdLicModel = ModelFactory.createDefaultModel();	// don't use the static model to remove any possible timing windows while we are creating
		String fileType = "HTML";
		String base = LISTED_LICENSE_URI_PREFIX+"index.html";
		InputStream licRdfInput;
		if (onlyUseLocalLicenses) {
		    licRdfInput = null;
		} else {
		    licRdfInput = FileManager.get().open(LISTED_LICENSE_URI_PREFIX+"index.html");
		    try {
		    	myStdLicModel.read(licRdfInput, base, fileType);
				Property p = myStdLicModel.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID);
		    	if (myStdLicModel.isEmpty() || !myStdLicModel.contains(null, p)) {
			    	try {
						licRdfInput.close();
					} catch (IOException e) {
						logger.warn("Error closing listed license input");
					}
			    	licRdfInput = null;
		    	}
		    } catch(Exception ex) {	    	
	    		logger.warn("Unable to access the SPDX listed licenses at http://www.spdx.org/licenses.  Using local file copy of SPDX listed licenses");
	    		if (licRdfInput != null) {
	    			try {
	    				licRdfInput.close();
	    			} catch (IOException e) {
	    				logger.warn("Error closing listed license input");
	    			}
	    			licRdfInput = null;	
	    		}
	    	}
	    }	
		try {
			if (licRdfInput == null) {
				// need to load a static copy
				base = "file://"+LISTED_LICENSE_RDF_LOCAL_FILENAME;
				licRdfInput = FileManager.get().open(LISTED_LICENSE_RDF_LOCAL_FILENAME);
				if ( licRdfInput == null ) {
					// try the class loader
					licRdfInput = LicenseInfoFactory.class.getResourceAsStream("/" + LISTED_LICENSE_RDF_LOCAL_FILENAME);
				}
				if (licRdfInput == null) {
					throw new NoListedLicenseRdfModel("Unable to open SPDX listed license from website or from local file");
				}
				try {
					myStdLicModel.read(licRdfInput, base, fileType);
				} catch(Exception ex) {
					throw new NoListedLicenseRdfModel("Unable to read the SPDX listed license model", ex);
				}
			}

			listedLicenseModel = myStdLicModel;	
		} finally {
			if (licRdfInput != null) {
				try {
					licRdfInput.close();
				} catch (IOException e) {
					logger.warn("Unable to close license RDF Input Stream");
				}
			}
		}
	}
	
	static void loadListedLicenseIDs() {
		LISTED_LICENSES = new HashMap<String, SpdxListedLicense>();
		try {
			Model stdLicenseModel = getListedLicenseModel();
			Node p = stdLicenseModel.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID).asNode();
			Triple m = Triple.createMatch(null, p, null);
			ExtendedIterator<Triple> tripleIter = stdLicenseModel.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				LISTED_LICENSE_ID_SET.add(t.getObject().toString(false));
			}
			p = stdLicenseModel.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_LIST_VERSION).asNode();
			m = Triple.createMatch(null, p, null);
			tripleIter = stdLicenseModel.getGraph().find(m);	
			if (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				LICENSE_LIST_VERSION = t.getObject().toString(false);
			}
		} catch (Exception ex) {
			logger.error("Error loading SPDX listed license ID's from model.");
		}
	}

	/**
	 * @param model
	 * @param node
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static AnyLicenseInfo getLicenseInfoById(Model model, Node node) throws InvalidSPDXAnalysisException {
		Node licenseIdPredicate = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID).asNode();
		Triple m = Triple.createMatch(node, licenseIdPredicate, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
		if (tripleIter.hasNext()) {
			Triple triple = tripleIter.next();
			String id = triple.getObject().toString(false);
			if (tripleIter.hasNext()) {
				throw(new InvalidSPDXAnalysisException("More than one ID associated with license "+id));
			}
			if (isSpdxListedLicenseID(id)) {
				return new SpdxListedLicense(model, node);
			} else if (id.startsWith(SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM)) {
				return new ExtractedLicenseInfo(model, node);
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
	 * @param model
	 * @param node
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static AnyLicenseInfo getLicenseInfoByType(Model model, Node node) throws InvalidSPDXAnalysisException {
		// find the subclass
		Node rdfTypePredicate = model.getProperty(SpdxRdfConstants.RDF_NAMESPACE, 
				SpdxRdfConstants.RDF_PROP_TYPE).asNode();
		Triple m = Triple.createMatch(node, rdfTypePredicate, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	// find the type(s)
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
				return new ConjunctiveLicenseSet(model, node);
			} else if (type.equals(SpdxRdfConstants.CLASS_SPDX_DISJUNCTIVE_LICENSE_SET)) {
				return new DisjunctiveLicenseSet(model, node);
			}else if (type.equals(SpdxRdfConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO)) {
				return new ExtractedLicenseInfo(model, node);
			}else if (type.equals(SpdxRdfConstants.CLASS_SPDX_LICENSE)) {
				return new SpdxListedLicense(model, node);
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
	 * @return an SPDXLicenseInfo created from the string
	 * @throws InvalidLicenseStringException 
	 */
	public static AnyLicenseInfo parseSPDXLicenseString(String licenseString) throws InvalidLicenseStringException {
		try {
			return LicenseExpressionParser.parseLicenseExpression(licenseString);
		} catch (LicenseParserException e) {
			throw new InvalidLicenseStringException(e.getMessage(),e);
		} catch (InvalidSPDXAnalysisException e) {
			throw new InvalidLicenseStringException("Unexpected SPDX error parsing license string");
		}
	}

	/**
	 * Parses a license set which consists of a list of LicenseInfo strings
	 * @param parseString
	 * @return
	 * @throws InvalidLicenseStringException 
	 */
	private static AnyLicenseInfo parseLicenseSet(String parseString) throws InvalidLicenseStringException {
		boolean isConjunctive = false;
		boolean isDisjunctive = false;
		ArrayList<AnyLicenseInfo> licenseInfoList = new ArrayList<AnyLicenseInfo>();
		int pos = 0;	// character position
		while (pos < parseString.length()) {
			// skip white space
			pos = skipWhiteSpace(parseString, pos);
			if (pos >= parseString.length()) {
				break;	// we are done
			}
			// collect the license information
			if (parseString.charAt(pos) == '(') {
				int startOfSet = pos + 1;
				pos = findEndOfSet(parseString, pos);
				if (pos > parseString.length() || parseString.charAt(pos) != ')') {
					throw(new InvalidLicenseStringException("Missing end ')'"));
				}
				licenseInfoList.add(parseLicenseSet(parseString.substring(startOfSet, pos)));
				pos++;
			} else {
				// a license ID
				int startOfID = pos;
				pos = skipNonWhiteSpace(parseString, pos);
				String licenseID = parseString.substring(startOfID, pos);
				if (licenseID.equals(NONE_LICENSE_NAME)) {
					licenseInfoList.add(new SpdxNoneLicense());
				} else if (licenseID.equals(NOASSERTION_LICENSE_NAME)) {
					licenseInfoList.add(new SpdxNoAssertionLicense());
				} 
				if (isSpdxListedLicenseID(licenseID)) {
					try {
						licenseInfoList.add(getListedLicenseById(licenseID));
                    } catch (InvalidSPDXAnalysisException e) {
                        throw new InvalidLicenseStringException(e.getMessage());
                    }
				} else {
					licenseInfoList.add(new ExtractedLicenseInfo(licenseID, null));
				}
			}
			if (pos >= parseString.length()) {
				break;	// done
			}
			// consume the AND or the OR
			// skip more whitespace
			pos = skipWhiteSpace(parseString, pos);
			if (parseString.charAt(pos) == 'A' || parseString.charAt(pos) == 'a') {
				// And
				if (pos + 4 >= parseString.length() || 
						!parseString.substring(pos, pos+4).toUpperCase().equals("AND ")) {
					throw(new InvalidLicenseStringException("Expecting an AND"));
				}
				isConjunctive = true;
				pos = pos + 4;
			} else if (parseString.charAt(pos) == 'O' || parseString.charAt(pos) == 'o') {
				// or
				if (pos + 3 >= parseString.length() || 
						!parseString.substring(pos, pos+3).toUpperCase().equals("OR ")) {
					throw(new InvalidLicenseStringException("Expecting an OR"));
				}
				isDisjunctive = true;
				pos = pos + 3;
			} else {
				throw(new InvalidLicenseStringException("Expecting an AND or an OR"));
			}
		}
		if (isConjunctive && isDisjunctive) {
			throw(new InvalidLicenseStringException("Can not have both AND's and OR's inside the same set of parenthesis"));
		}
		AnyLicenseInfo[] licenseInfos = new AnyLicenseInfo[licenseInfoList.size()];
		licenseInfos = licenseInfoList.toArray(licenseInfos);
		if (isConjunctive) {
			return new ConjunctiveLicenseSet(licenseInfos);
		} else if (isDisjunctive) {
			return new DisjunctiveLicenseSet(licenseInfos);
		} else {
			throw(new InvalidLicenseStringException("Missing AND or OR inside parenthesis"));
		}
	}

	/**
	 * @param parseString
	 * @return
	 * @throws InvalidLicenseStringException 
	 */
	private static int findEndOfSet(String parseString, int pos) throws InvalidLicenseStringException {
		if (parseString.charAt(pos) != '(') {
			throw(new InvalidLicenseStringException("Expecting '('"));
		}
		int retval = pos;
		retval++;
		while (retval < parseString.length() && parseString.charAt(retval) != ')') {
			if (parseString.charAt(retval) == '(') {
				retval = findEndOfSet(parseString, retval) + 1;
			} else {
				retval++;
			}
		}
		return retval;
	}

	/**
	 * @param parseString
	 * @param pos
	 * @return
	 */
	private static int skipWhiteSpace(String parseString, int pos) {
		int retval = pos;
		char c = parseString.charAt(retval);
		while (retval < parseString.length() &&
				(c == ' ' || c == '\t' || c == '\r' || c == '\n')) {
			retval++;
			if (retval < parseString.length()) {
				c = parseString.charAt(retval);
			}
		}
		return retval;
	}
	
	/**
	 * @param parseString
	 * @param pos
	 * @return
	 */
	private static int skipNonWhiteSpace(String parseString, int pos) {
		int retval = pos;
		char c = parseString.charAt(retval);
		while (retval < parseString.length() &&
				c != ' ' && c != '\t' && c != '\r' && c != '\n') {
			retval++;
			if (retval < parseString.length()) {
				c = parseString.charAt(retval);
			}
		}
		return retval;
	}

	/**
	 * @param licenseID
	 * @return true if the licenseID belongs to an SPDX listed license
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static boolean isSpdxListedLicenseID(String licenseID)  {
		return LISTED_LICENSE_ID_SET.contains(licenseID);
	}

	/**
	 * Tries to load properties from LISTED_LICENSE_PROPERTIES_FILENAME, ignoring errors
	 * encountered during the process (e.g., the properties file doesn't exist, etc.).
	 * 
	 * @return a (possibly empty) set of properties
	 */
	private static Properties loadLicenseProperties() {
        Properties licenseProperties = new Properties();
        InputStream in = null;
        try {
            in = LicenseInfoFactory.class.getResourceAsStream("/" + LISTED_LICENSE_PROPERTIES_FILENAME);
            licenseProperties.load(in);
        } catch (IOException e) {
            // Ignore it and fall through
        	logger.warn("IO Exception reading listed license properties file: "+e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                   logger.warn("Unable to close listed license properties file: "+e.getMessage());
                }
            }
        }
        return licenseProperties;
    }
	
	
	/**
	 * @return Array of all SPDX listed license IDs
	 */
	public static String[] getSpdxListedLicenseIds() {
		return LISTED_LICENSE_ID_SET.toArray(new String[LISTED_LICENSE_ID_SET.size()]);
	}
	
	/**
	 * @return Version of the license list being used by the SPDXLicenseInfoFactory
	 */
	public static String getLicenseListVersion() {
		return LICENSE_LIST_VERSION;
	}   
}
