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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Singleton class which holds the listed licenses
 * @author Gary O'Neall
 *
 */
public class ListedLicenses implements IModelContainer {
	
	public static final String DEFAULT_LICENSE_LIST_VERSION = "2.0";
	static final Logger logger = Logger.getLogger(ListedLicenses.class.getName());
	static final String LISTED_LICENSE_ID_URL = "http://spdx.org/licenses/";
	public static final String LISTED_LICENSE_URI_PREFIX = "http://spdx.org/licenses/";
	private static final String LISTED_LICENSE_RDF_LOCAL_DIR = "resources" + "/" + "stdlicenses";

	private static final String LISTED_LICENSE_RDF_LOCAL_FILENAME = LISTED_LICENSE_RDF_LOCAL_DIR + "/" + "index.html";
	private static final String LISTED_LICENSE_PROPERTIES_FILENAME = LISTED_LICENSE_RDF_LOCAL_DIR + "/" + "licenses.properties";
	
	private Model listedLicenseModel = null;
	
	HashSet<String> listdLicenseIds = new HashSet<String>();
	
	HashMap<String, SpdxListedLicense> listedLicenseCache = null;
    
	Properties licenseProperties;
    boolean onlyUseLocalLicenses;

    String licenseListVersion = DEFAULT_LICENSE_LIST_VERSION;

	static ListedLicenses listedLicenses = null;
	
	int nextId = 0;
	
	/**
	 * This constructor should only be called by the getListedLicenses method
	 */
	private ListedLicenses() {
		licenseProperties = loadLicenseProperties();
		onlyUseLocalLicenses = Boolean.parseBoolean(
	            System.getProperty("SPDXParser.OnlyUseLocalLicenses", licenseProperties.getProperty("OnlyUseLocalLicenses", "false")));
		loadListedLicenseIDs();
	}
	
	public static ListedLicenses getListedLicenses() {
		if (listedLicenses == null) {
			listedLicenses = new ListedLicenses();
		}
		return listedLicenses;
	}
	
	/**
	 * Resets all of the cached license information and reloads the license IDs
	 * NOTE: This method should be used with caution, it will negatively impact
	 * performance.
	 * @return
	 */
	public static ListedLicenses resetListedLicenses() {
		listedLicenses = new ListedLicenses();
		return listedLicenses;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#getModel()
	 */
	@Override
	public Model getModel() {
		return listedLicenseModel;
	}
	
	/**
	 * Get a listed license based on a URI.  The URI can be a file or a web resource.
	 * The license information is copied into the listedLicenseModel and the license
	 * is placed into the cache.
	 * @param uri
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected SpdxListedLicense getLicenseFromUri(String uri) throws InvalidSPDXAnalysisException {
		URL licenseUrl = null;
		try {
			licenseUrl = new URL(uri);
		} catch (MalformedURLException e) {
			throw new InvalidSPDXAnalysisException("Invalid listed license URL: "+e.getMessage());
		}
		String id = urlToId(licenseUrl);
		if (listedLicenseCache.containsKey(id)) {
			return listedLicenseCache.get(id);
		}
		String base = LISTED_LICENSE_ID_URL + id;
		final Model localLicenseModel = getLicenseModel(uri, base);
		if (localLicenseModel == null) {
			throw(new InvalidSPDXAnalysisException("No listed license was found at "+uri));
		}
		Resource licResource = localLicenseModel.getResource(base);
		if (licResource == null || !localLicenseModel.containsResource(localLicenseModel.asRDFNode(licResource.asNode()))) {
			throw(new InvalidSPDXAnalysisException("No listed license was found at "+uri));
		}
		final String localLicenseNamespace = this.getDocumentNamespace();
		IModelContainer localLicenseContainer = new IModelContainer() {

			@Override
			public Model getModel() {
				return localLicenseModel;
			}

			@Override
			public String getDocumentNamespace() {
				return localLicenseNamespace;
			}

			@Override
			public String getNextSpdxElementRef() {
				return null;	// This will not be used
			}
			
		};
		SpdxListedLicense retval;
		if (this.getModel().equals(localLicenseModel)) {
			retval = new SpdxListedLicense(localLicenseContainer, licResource.asNode());
		} else {	// we need to copy from the local model into this model
			SpdxListedLicense localLicense = new SpdxListedLicense(localLicenseContainer, licResource.asNode());
			retval = (SpdxListedLicense)localLicense.clone();
			retval.createResource(this);
		}
		listedLicenseCache.put(id, retval);
		return retval;
	}

	/**
	 * Converts a license URL to a license ID
	 * @param licenseUrl
	 * @return
	 */
	private String urlToId(URL licenseUrl) {
		String[] pathParts = licenseUrl.getFile().split("/");
		String id = pathParts[pathParts.length-1];
		return id;
	}

	/**
	 * @param uri - URI of the actual resource
	 * @param base - base for any fragments present in the license model
	 * @return
	 * @throws NoListedLicenseRdfModel 
	 */
	private Model getLicenseModel(String uri, String base) throws NoListedLicenseRdfModel {
		try {
			Class.forName("net.rootdev.javardfa.jena.RDFaReader");
		} catch(java.lang.ClassNotFoundException e) {
			throw(new NoListedLicenseRdfModel("Could not load the RDFa reader for licenses.  This could be caused by an installation problem - missing java-rdfa jar file"));
		}  
		Model retval = ModelFactory.createDefaultModel();
		InputStream in = null;
		try {
			try {
				if (!(onlyUseLocalLicenses && uri.startsWith(LISTED_LICENSE_URI_PREFIX))) {
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
	
	private Model getListedLicenseModel() throws InvalidSPDXAnalysisException {
		if (listedLicenseModel == null) {
			loadListedLicenseModel();
		}
		return listedLicenseModel;
	}

	/**
	 * Load an spdx listed license model from the index page
	 */
	private void loadListedLicenseModel() throws InvalidSPDXAnalysisException {
		try {
			Class.forName("net.rootdev.javardfa.jena.RDFaReader");
		} catch(java.lang.ClassNotFoundException e) {
			logger.warn("Unable to load Java RDFa reader");
		}  

		// Create the initial model from the index page which only contains
		// the license ID's
		// We will fill in the licenses into the cache on demand
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
	
	private void loadListedLicenseIDs() {
		listedLicenseCache = new HashMap<String, SpdxListedLicense>();	// clear the cache
		try {
			Model stdLicenseModel = getListedLicenseModel();
			Node p = stdLicenseModel.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID).asNode();
			Triple m = Triple.createMatch(null, p, null);
			ExtendedIterator<Triple> tripleIter = stdLicenseModel.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				listdLicenseIds.add(t.getObject().toString(false));
			}
			p = stdLicenseModel.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_LIST_VERSION).asNode();
			m = Triple.createMatch(null, p, null);
			tripleIter = stdLicenseModel.getGraph().find(m);	
			if (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				licenseListVersion = t.getObject().toString(false);
			}
		} catch (Exception ex) {
			logger.error("Error loading SPDX listed license ID's from model.");
		}
	}
	
	/**
	 * @param licenseID
	 * @return true if the licenseID belongs to an SPDX listed license
	 * @throws InvalidSPDXAnalysisException 
	 */
	public boolean isSpdxListedLicenseID(String licenseID)  {
		return listdLicenseIds.contains(licenseID);
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
	public String[] getSpdxListedLicenseIds() {
		return listdLicenseIds.toArray(new String[listdLicenseIds.size()]);
	}
	
	/**
	 * @return Version of the license list being used by the SPDXLicenseInfoFactory
	 */
	public String getLicenseListVersion() {
		return licenseListVersion;
	}   
	
	/**
	 * @param licenseId SPDX Listed License ID
	 * @return SPDX listed license
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxListedLicense getListedLicenseById(String licenseId)throws InvalidSPDXAnalysisException {
		return getLicenseFromUri(LISTED_LICENSE_URI_PREFIX + licenseId);
	}

	/**
	 * Get or create a standard license in the model container copying any
	 * relevant information from the standard model to the model in the modelContainer
	 * @param modelContainer
	 * @param uri
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public AnyLicenseInfo getLicenseFromStdLicModel(
			IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		SpdxListedLicense retval = new SpdxListedLicense(modelContainer, node);
		if (!this.equals(modelContainer)) {
			String licenseId = retval.getLicenseId();
			if (licenseId == null) {
				URL licenseUrl;
				try {
					licenseUrl = new URL(node.getURI());
				} catch (MalformedURLException e) {
					throw new InvalidSPDXAnalysisException("Invalid license URL");
				}
				licenseId = this.urlToId(licenseUrl);
			}
			try {
				SpdxListedLicense licenseFromModel = getListedLicenseById(licenseId);
				retval.copyFrom(licenseFromModel);	// update the local model from the standard model
			} catch(Exception ex) {
				// ignore any errors - just don't copy from the license model
			}
		}		
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#getDocumentNamespace()
	 */
	@Override
	public String getDocumentNamespace() {
		return LISTED_LICENSE_ID_URL;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#getNextSpdxElementRef()
	 */
	@Override
	public synchronized String getNextSpdxElementRef() {
		this.nextId++;
		return "SpdxLicenseGeneratedId-"+String.valueOf(this.nextId);
	}
}
