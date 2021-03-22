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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

/**
 * Singleton class which holds the listed licenses
 * @author Gary O'Neall
 *
 */
public class ListedLicenses implements IModelContainer {

	public static final String DEFAULT_LICENSE_LIST_VERSION = "3.5";
	static final Logger logger = LoggerFactory.getLogger(ListedLicenses.class.getName());
	static final String LISTED_LICENSE_ID_URL = "http://spdx.org/licenses/";
	public static final String LISTED_LICENSE_URI_PREFIX = "https://spdx.org/licenses/";
	static final String LISTED_LICENSE_RDF_LOCAL_DIR = "resources" + "/" + "stdlicenses";
	private static final String LICENSE_TOC_FILENAME = "licenses.json";

	private static final String LISTED_LICENSE_PROPERTIES_FILENAME = LISTED_LICENSE_RDF_LOCAL_DIR + "/" + "licenses.properties";

	private Model listedLicenseModel = null;

	/**
	 * Map of lowercase listed license ID to proper cased listed license ID
	 */
	Map<String, String> listdLicenseIds = null;

	Map<String, SpdxListedLicense> listedLicenseCache = null;
	Map<IModelContainer, Map<Node, SpdxListedLicense>> listedLicenseNodeCache = Maps.newHashMap();



	Properties licenseProperties;
    boolean onlyUseLocalLicenses;
    File localLicensesDir = null;

    String licenseListVersion = DEFAULT_LICENSE_LIST_VERSION;

	private static volatile ListedLicenses listedLicenses = null;

	//Lock to ensure thread-safety of all modifications.
	//Since modifications should be extremely rare, a single lock for both listed licenses and the model
	//should be sufficient.
	private static final ReadWriteLock listedLicenseModificationLock = new ReentrantReadWriteLock();
	private static final String JSONLD_URL_SUFFIX = ".jsonld";

	int nextId = 0;

	/**
	 * This constructor should only be called by the getListedLicenses method
	 */
	private ListedLicenses() {
		licenseProperties = loadLicenseProperties();
		// System properties have precedence over environment variables which have precedence over the license properties file
		String stOnlyLicenses = System.getenv("SPDXParser.OnlyUseLocalLicenses");
		if (Objects.isNull(stOnlyLicenses) || stOnlyLicenses.isEmpty()) {
			onlyUseLocalLicenses = Boolean.parseBoolean(
		            System.getProperty("SPDXParser.OnlyUseLocalLicenses", licenseProperties.getProperty("OnlyUseLocalLicenses", "false")));
		} else {
			onlyUseLocalLicenses = Boolean.parseBoolean(stOnlyLicenses);
		}
		
		if (onlyUseLocalLicenses) {
			try {
				String localLicensesDirStr = System.getenv("SPDXParser.LocalLicensesDir");
				if (Objects.isNull(localLicensesDirStr) || localLicensesDirStr.isEmpty()) {
					localLicensesDirStr = System.getProperty("SPDXParser.LocalLicensesDir", 
							licenseProperties.getProperty("SPDXParser.LocalLicensesDir"));
				}
				if (Objects.isNull(localLicensesDirStr)) {
					localLicensesDir = null;
				} else {
					localLicensesDir = new File(localLicensesDirStr);
					if (!localLicensesDir.isDirectory()) {
						logger.error("Local license directory "+localLicensesDirStr+" is not a directory");
						localLicensesDir = null;
					}
				}
			} catch (Exception ex) {
				logger.error("Error getting local licenses directory",ex);
				localLicensesDir = null;
			}
		}
		loadListedLicenseIDs();
	}

    public static ListedLicenses getListedLicenses() {
        if (listedLicenses == null) {
            listedLicenseModificationLock.writeLock().lock();
            try {
                if (listedLicenses == null) {
                    listedLicenses = new ListedLicenses();
                }
            } finally {
                listedLicenseModificationLock.writeLock().unlock();
            }
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
        listedLicenseModificationLock.writeLock().lock();
        try {
            listedLicenses = new ListedLicenses();
            return listedLicenses;
        } finally {
            listedLicenseModificationLock.writeLock().unlock();
        }
    }

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#getModel()
	 */
	@Override
	public Model getModel() {
		listedLicenseModificationLock.writeLock().lock();
		try {
			if (listedLicenseModel == null) {
				listedLicenseModel = ModelFactory.createDefaultModel();
			}
		} finally {
            listedLicenseModificationLock.writeLock().unlock();
        }
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
            throw new InvalidSPDXAnalysisException("Invalid listed license URL: " + e.getMessage());
        }
        String id = urlToId(licenseUrl);
        //We will not enforce that the cache miss and the subsequent caching of the retrieved license be atomic.
        listedLicenseModificationLock.readLock().lock();
        try {
            if (listedLicenseCache.containsKey(id)) {
                return listedLicenseCache.get(id);
            }
        } finally {
            listedLicenseModificationLock.readLock().unlock();
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
				// Not implemented
				return null;
			}

			@Override
			public boolean addCheckNodeObject(Node node,
					IRdfModel rdfModelObject) {
				// Not implemented
				return true;
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
        listedLicenseModificationLock.writeLock().lock();
        try {
            listedLicenseCache.put(id, retval);
        } finally {
            listedLicenseModificationLock.writeLock().unlock();
        }
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
		if (id.endsWith(JSONLD_URL_SUFFIX)) {
			id = id.substring(0, id.length() - JSONLD_URL_SUFFIX.length());
		}
		return id;
	}

	/**
	 * @param uri - URI of the actual resource
	 * @param base - base for any fragments present in the license model
	 * @return
	 * @throws NoListedLicenseRdfModel
	 */
	private Model getLicenseModel(String uri, String base) throws NoListedLicenseRdfModel {
		Model retval = ModelFactory.createDefaultModel();
		InputStream in = null;
		try {
			if (onlyUseLocalLicenses && Objects.nonNull(this.localLicensesDir)) {
				// Fetch from the local file system
				String fileName = this.localLicensesDir.getAbsolutePath() + File.separator + uri.substring(LISTED_LICENSE_URI_PREFIX.length());
				try {
					in = new FileInputStream(new File(fileName));
				} catch (FileNotFoundException e) {
					throw(new NoListedLicenseRdfModel("SPDX listed license file "+fileName+" does not exist."));
				}
				try (InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"))){
					retval.read(in, base, "JSON-LD");
					return retval;
				} catch(Exception ex) {
					throw(new NoListedLicenseRdfModel("Error reading the spdx listed licenses: "+ex.getMessage(),ex));
				}
			}
			try {
				if (in == null && !onlyUseLocalLicenses) {
					//Accessing the old HTTP urls produces 301.
					String actualUrl = StringUtils.replaceOnce(uri, "http://", "https://");
					actualUrl = getNestedURL(actualUrl);
					in = FileManager.get().open(actualUrl);
					try {
						retval.read(in, base, "JSON-LD");
						Property p = retval.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID);
						if (!retval.contains(null, p)) {
						    Property typeProperty = retval.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	                        Resource licenseType = retval.getResource(SpdxRdfConstants.SPDX_NAMESPACE + "ListedLicense");
	                        Triple m = Triple.createMatch(null, typeProperty.asNode(), licenseType.asNode());
	                        ExtendedIterator<Triple> tripleIter = retval.getGraph().find(m);
	                        if (tripleIter.hasNext()) {
	                            Resource licenseIdResource = retval.createResource(tripleIter.next().getSubject().getURI());
	                            String id = licenseIdResource.getURI().substring("http://spdx.org/licenses/".length());
	                            ((Resource)licenseIdResource).addProperty(p, id);
	                        }
						}
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
				// need to fetch from the class
				String fileName = LISTED_LICENSE_RDF_LOCAL_DIR + "/" + uri.substring(LISTED_LICENSE_URI_PREFIX.length());
				in = LicenseInfoFactory.class.getResourceAsStream("/" + fileName);
				if (in == null) {
					throw(new NoListedLicenseRdfModel("SPDX listed license "+uri+" could not be read."));
				}
				try (InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"))){
					retval.read(in, base, "JSON-LD");
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

	public static String getNestedURL(String stringUrl) throws MalformedURLException {
		URL url = new URL(stringUrl);
		String protocol = url.getProtocol();
		if (protocol.startsWith("http")) {
			try {
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setInstanceFollowRedirects(false);
				con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
				con.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
				con.addRequestProperty("Referer", "https://www.google.com/");
				con.connect();
				int resultCode = con.getResponseCode();
				if (resultCode == HttpURLConnection.HTTP_SEE_OTHER
						|| resultCode == HttpURLConnection.HTTP_MOVED_PERM
						|| resultCode == HttpURLConnection.HTTP_MOVED_TEMP) {
					String Location = con.getHeaderField("Location");
					if (Location.startsWith("/")) {
						Location = url.getProtocol() + "://" + url.getHost() + Location;
					}
					return getNestedURL(Location);
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return url.toString();
	}


    /**
     * Load the listed license IDs from the website or local file cache
     */
    private void loadListedLicenseIDs() {
        listedLicenseModificationLock.writeLock().lock();
        try {
            listedLicenseCache = Maps.newHashMap(); // clear the cache
            listdLicenseIds = Maps.newHashMap(); //Clear the listed license IDs to avoid stale licenses.
            //TODO: Can the keys of listedLicenseCache be used instead of this set?
            //NOTE: This includes deprecated licenses - should this be changed to only return non-deprecated licenses?
            InputStream tocStream = null;
            BufferedReader reader = null;
            try {
            	if (this.onlyUseLocalLicenses) {
            		if (Objects.nonNull(localLicensesDir)) {
            			try {
                			// Fetch from the file system
            				tocStream = new FileInputStream(new File(localLicensesDir.getAbsolutePath() + File.separator + LICENSE_TOC_FILENAME));
            			} catch (IOException e) {
            				logger.error("Json TOC local file missing or invalid - using installed licenses");
            			}
            		}
        		} else {
                	try {
						URL tocUrl = new URL(LISTED_LICENSE_URI_PREFIX + LICENSE_TOC_FILENAME);
						tocStream = tocUrl.openStream();
					} catch (MalformedURLException e) {
						logger.error("Json TOC URL invalid, using local TOC file");
						tocStream = null;
					} catch (IOException e) {
						logger.error("I/O error opening Json TOC URL, using local TOC file");
						tocStream = null;
					}
        		}
                if (tocStream == null) {
                	// fetch from class loader
                	String fileName = LISTED_LICENSE_RDF_LOCAL_DIR + "/" + LICENSE_TOC_FILENAME;
                	tocStream = LicenseInfoFactory.class.getResourceAsStream("/" + fileName);
                }
                if (tocStream == null) {
                	logger.error("Unable to load license ID's from JSON TOC file");
                }
                reader = new BufferedReader(new InputStreamReader(tocStream));
                StringBuilder tocJsonStr = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null) {
                	tocJsonStr.append(line);
                }
                Gson gson = new Gson();
                LicenseJsonTOC jsonToc = gson.fromJson(tocJsonStr.toString(), LicenseJsonTOC.class);
                listdLicenseIds = jsonToc.getLicenseIds();
                this.licenseListVersion = jsonToc.getLicenseListVersion();

            } catch (IOException e) {
				logger.error("I/O error reading JSON TOC file");
			} finally {
            	if (reader != null) {
            		try {
						reader.close();
					} catch (IOException e) {
						logger.warn("Unable to close JSON TOC reader");
					}
            	} else if (tocStream != null) {
            		try {
						tocStream.close();
					} catch (IOException e) {
						logger.warn("Unable to close JSON TOC input stream");
					}
            	}
			}
        } finally {
            listedLicenseModificationLock.writeLock().unlock();
        }
    }

	/**
	 * @param licenseID
	 * @return true if the licenseID belongs to an SPDX listed license
	 * @throws InvalidSPDXAnalysisException
	 */
    public boolean isSpdxListedLicenseID(String licenseID) {
        try {
            listedLicenseModificationLock.readLock().lock();
            return listdLicenseIds.containsKey(licenseID.toLowerCase());
        } finally {
            listedLicenseModificationLock.readLock().unlock();
        }
    }

	/**
	 * Tries to load properties from LISTED_LICENSE_PROPERTIES_FILENAME, ignoring errors
	 * encountered during the process (e.g., the properties file doesn't exist, etc.).
	 *
	 * @return a (possibly empty) set of properties
	 */
    private static Properties loadLicenseProperties() {
        listedLicenseModificationLock.writeLock().lock();
        try {
            Properties licenseProperties = new Properties();
            InputStream in = null;
            try {
                in = LicenseInfoFactory.class.getResourceAsStream("/" + LISTED_LICENSE_PROPERTIES_FILENAME);
                if (in != null) {
                    licenseProperties.load(in);
                }
            } catch (IOException e) {
                // Ignore it and fall through
                logger.warn("IO Exception reading listed license properties file: " + e.getMessage());
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        logger.warn("Unable to close listed license properties file: " + e.getMessage());
                    }
                }
            }
            return licenseProperties;
        } finally {
            listedLicenseModificationLock.writeLock().unlock();
        }
    }

	/**
	 * @return Array of all SPDX listed license IDs
	 */
    public String[] getSpdxListedLicenseIds() {
        listedLicenseModificationLock.readLock().lock();
        try {
            return listdLicenseIds.values().toArray(new String[listdLicenseIds.size()]);
        } finally {
			listedLicenseModificationLock.readLock().unlock();
        }
    }

	/**
	 * @return The version of the loaded license list in the form M.N, where M is the major release and N is the minor release.
	 * If no license list is loaded, returns {@Link DEFAULT_LICENSE_LIST_VERSION}.
	 */
	public String getLicenseListVersion() {
		return licenseListVersion;
	}

	/**
	 * @param licenseId SPDX Listed License ID
	 * @return SPDX listed license or null if the ID is not in the SPDX license list
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxListedLicense getListedLicenseById(String licenseId)throws InvalidSPDXAnalysisException {
		SpdxListedLicense retval = getLicenseFromUri(LISTED_LICENSE_URI_PREFIX + listdLicenseIds.get(licenseId.toLowerCase()) + JSONLD_URL_SUFFIX);
		if (retval != null) {
			retval = (SpdxListedLicense)retval.clone();	// We need to clone the license to remove the references to the model in the cache
		}
		return retval;
	}

	/**
	 * Get or create a standard license in the model container copying any
	 * relevant information from the standard model to the model in the modelContainer
	 * @param modelContainer
	 * @param node
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	public AnyLicenseInfo getLicenseFromStdLicModel(
			IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		Map<Node, SpdxListedLicense> modelNodeCache = this.listedLicenseNodeCache.get(modelContainer);
		if (modelNodeCache == null) {
			modelNodeCache = Maps.newHashMap();
			this.listedLicenseNodeCache.put(modelContainer, modelNodeCache);
		}
		if (modelNodeCache.containsKey(node)) {
			return modelNodeCache.get(node);
		}
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
		modelNodeCache.put(node, retval);
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

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#SpdxElementRefExists(java.lang.String)
	 */
	@Override
	public boolean spdxElementRefExists(String elementRef) {
		return(listdLicenseIds.values().contains(elementRef));
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#addSpdxElementRef(java.lang.String)
	 */
	@Override
	public void addSpdxElementRef(String elementRef) {
		listdLicenseIds.put(elementRef.toLowerCase(),elementRef);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#documentNamespaceToId(java.lang.String)
	 */
	@Override
	public String documentNamespaceToId(String externalNamespace) {
		// Listed licenses do not support external documents
		return null;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#externalDocumentIdToNamespace(java.lang.String)
	 */
	@Override
	public String externalDocumentIdToNamespace(String docId) {
		// Listed licenses do not support external documents
		return null;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#createResource(org.apache.jena.rdf.model.Resource, java.lang.String, org.apache.jena.rdf.model.Resource, org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public Resource createResource(Resource duplicate, String uri,
			Resource type, IRdfModel modelObject) {
		if (duplicate != null) {
			return duplicate;
		} else if (uri == null) {
			return listedLicenseModel.createResource(getType(listedLicenseModel));
		} else {
			return listedLicenseModel.createResource(uri, getType(listedLicenseModel));
		}
	}

	/**
	 * @param model
	 * @return
	 */
	private Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_LICENSE);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#addCheckNodeObject(org.apache.jena.graph.Node, org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean addCheckNodeObject(Node node, IRdfModel rdfModelObject) {
		// TODO Refactor and implement
		return true;
	}
}