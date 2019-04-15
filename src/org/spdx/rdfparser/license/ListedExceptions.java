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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

/**
 * Singleton class which holds the listed exceptions
 * @author Gary O'Neall
 *
 */
public class ListedExceptions implements IModelContainer {
	
	static final Logger logger = LoggerFactory.getLogger(ListedExceptions.class.getName());
	static final String LISTED_LICENSE_ID_URL = "http://spdx.org/licenses/";
	private static final String EXCEPTION_TOC_FILENAME = "exceptions.json";
	
	private Model listedExceptionModel = null;
	
	Set<String> listdExceptionIds = null;
	
	Map<String, ListedLicenseException> listedExceptionCache = null;
	Map<IModelContainer, Map<Node, ListedLicenseException>> listedExceptionNodeCache = Maps.newHashMap();

    boolean onlyUseLocalLicenses;

    String licenseListVersion = ListedLicenses.DEFAULT_LICENSE_LIST_VERSION;

	private static volatile ListedExceptions listedExceptions = null;
	
	//Lock to ensure thread-safety of all modifications.
	//Since modifications should be extremely rare, a single lock for both listed licenses and the model
	//should be sufficient.
	private static final ReadWriteLock listedExceptionModificationLock = new ReentrantReadWriteLock();
	private static final String JSONLD_URL_SUFFIX = ".jsonld";
	
	int nextId = 0;
	
	/**
	 * This constructor should only be called by the getListedExeptions method
	 */
	private ListedExceptions() {
		onlyUseLocalLicenses = ListedLicenses.getListedLicenses().onlyUseLocalLicenses;
		loadListedExceptionIDs();
	}
	
    public static ListedExceptions getListedExceptions() {
        if (listedExceptions == null) {
            listedExceptionModificationLock.writeLock().lock();
            try {
                if (listedExceptions == null) {
                    listedExceptions = new ListedExceptions();
                }
            } finally {
                listedExceptionModificationLock.writeLock().unlock();
            }
        }
        return listedExceptions;
    }
	
	/**
	 * Resets all of the cached exception information and reloads the exception IDs
	 * NOTE: This method should be used with caution, it will negatively impact
	 * performance.
	 * @return
	 */
    public static ListedExceptions resetListedExceptions() {
        listedExceptionModificationLock.writeLock().lock();
        try {
            listedExceptions = new ListedExceptions();
            return listedExceptions;
        } finally {
            listedExceptionModificationLock.writeLock().unlock();
        }
    }

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#getModel()
	 */
	@Override
	public Model getModel() {
		listedExceptionModificationLock.writeLock().lock();
		try {
			if (listedExceptionModel == null) {
				listedExceptionModel = ModelFactory.createDefaultModel();
			}
		} finally {
            listedExceptionModificationLock.writeLock().unlock();
        }
		return listedExceptionModel;
	}
	
	/**
	 * Get a listed exception based on a URI.  The URI can be a file or a web resource.
	 * The exception information is copied into the listedExceptionModel and the exception
	 * is placed into the cache.
	 * @param uri
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
    protected ListedLicenseException getExceptionFromUri(String uri) throws InvalidSPDXAnalysisException {
        URL exceptionUrl = null;
        try {
            exceptionUrl = new URL(uri);
        } catch (MalformedURLException e) {
            throw new InvalidSPDXAnalysisException("Invalid listed exception URL: " + e.getMessage());
        }
        String id = urlToId(exceptionUrl);
        //We will not enforce that the cache miss and the subsequent caching of the retrieved license be atomic.
        listedExceptionModificationLock.readLock().lock();
        try {
            if (listedExceptionCache.containsKey(id)) {
                return listedExceptionCache.get(id);
            }
        } finally {
            listedExceptionModificationLock.readLock().unlock();
        }
		String base = LISTED_LICENSE_ID_URL + id;
		final Model localExceptionModel = getExceptionModel(uri, base);
		if (localExceptionModel == null) {
			throw(new InvalidSPDXAnalysisException("No listed exception was found at "+uri));
		}
		Resource exceptionResource = localExceptionModel.getResource(base);
		if (exceptionResource == null || !localExceptionModel.containsResource(localExceptionModel.asRDFNode(exceptionResource.asNode()))) {
			throw(new InvalidSPDXAnalysisException("No listed exception was found at "+uri));
		}
		final String localExceptionNamespace = this.getDocumentNamespace();
		IModelContainer localExceptionContainer = new IModelContainer() {

			@Override
			public Model getModel() {
				return localExceptionModel;
			}

			@Override
			public String getDocumentNamespace() {
				return localExceptionNamespace;
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
		ListedLicenseException retval;
		if (this.getModel().equals(localExceptionModel)) {
			retval = new ListedLicenseException(localExceptionContainer, exceptionResource.asNode());
		} else {	// we need to copy from the local model into this model
			ListedLicenseException localException = new ListedLicenseException(localExceptionContainer, exceptionResource.asNode());
			retval = (ListedLicenseException)localException.clone();
			retval.createResource(this);
        }
        listedExceptionModificationLock.writeLock().lock();
        try {
            listedExceptionCache.put(id, retval);
        } finally {
            listedExceptionModificationLock.writeLock().unlock();
        }
		return retval;
	}

	/**
	 * Converts an exception URL to a license ID
	 * @param exceptionUrl
	 * @return
	 */
	private String urlToId(URL exceptionUrl) {
		String[] pathParts = exceptionUrl.getFile().split("/");
		String id = pathParts[pathParts.length-1];
		if (id.endsWith(JSONLD_URL_SUFFIX)) {
			id = id.substring(0, id.length() - JSONLD_URL_SUFFIX.length());
		}
		return id;
	}

	/**
	 * @param uri - URI of the actual resource
	 * @param base - base for any fragments present in the exception model
	 * @return
	 * @throws NoListedLicenseRdfModel 
	 */
	private Model getExceptionModel(String uri, String base) throws NoListedLicenseRdfModel {
		Model retval = ModelFactory.createDefaultModel();
		InputStream in = null;
		try {
			try {
				if (!(onlyUseLocalLicenses && uri.startsWith(ListedLicenses.LISTED_LICENSE_URI_PREFIX))) {
					//Accessing the old HTTP urls produces 301.
					String actualUrl = StringUtils.replaceOnce(uri, "http://", "https://");
					in = FileManager.get().open(actualUrl);
					try {
						retval.read(in, base, "JSON-LD");
						Property p = retval.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_EXCEPTION_ID);
				    	if (retval.isEmpty() || !retval.contains(null, p)) {
					    	try {
								in.close();
							} catch (IOException e) {
								logger.warn("Error closing listed exception input");
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
				logger.warn("Unable to open SPDX listed exception model.  Using local file copy for SPDX listed exceptions");
			}
			if (in == null) {
				// need to fetch from the class
				String fileName = ListedLicenses.LISTED_LICENSE_RDF_LOCAL_DIR + "/" + uri.substring(ListedLicenses.LISTED_LICENSE_URI_PREFIX.length());
				in = LicenseInfoFactory.class.getResourceAsStream("/" + fileName);
				if (in == null) {
					throw(new NoListedLicenseRdfModel("SPDX listed exception "+uri+" could not be read."));
				}
				try (InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"))){
					retval.read(in, base, "JSON-LD");
				} catch(Exception ex) {
					throw(new NoListedLicenseRdfModel("Error reading the spdx listed exception: "+ex.getMessage(),ex));
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
	
    /**
     * Load the listed exception IDs from the website or local file cache
     */
    private void loadListedExceptionIDs() {
        listedExceptionModificationLock.writeLock().lock();
        try {
            listedExceptionCache = Maps.newHashMap(); // clear the cache
            listdExceptionIds = Sets.newHashSet(); //Clear the listed license IDs to avoid stale licenses.
            InputStream tocStream = null;
            BufferedReader reader = null;
            try {
                if (!this.onlyUseLocalLicenses) {
                	try {
						URL tocUrl = new URL(ListedLicenses.LISTED_LICENSE_URI_PREFIX + EXCEPTION_TOC_FILENAME);
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
                	String fileName = ListedLicenses.LISTED_LICENSE_RDF_LOCAL_DIR + "/" + EXCEPTION_TOC_FILENAME;
                	tocStream = LicenseInfoFactory.class.getResourceAsStream("/" + fileName);
                }
                if (tocStream == null) {
                	logger.error("Unable to load exception ID's from JSON TOC file");
                }
                reader = new BufferedReader(new InputStreamReader(tocStream));
                StringBuilder tocJsonStr = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null) {
                	tocJsonStr.append(line);
                }
                Gson gson = new Gson();
                ExceptionJsonTOC jsonToc = gson.fromJson(tocJsonStr.toString(), ExceptionJsonTOC.class);
                listdExceptionIds = jsonToc.getExceptionIds();
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
            listedExceptionModificationLock.writeLock().unlock();
        }
    }
	
	
	/**
	 * @return Array of all SPDX listed exception IDs
	 */
    public String[] getSpdxListedExceptionIds() {
        listedExceptionModificationLock.readLock().lock();
        try {
            return listdExceptionIds.toArray(new String[listdExceptionIds.size()]);
        } finally {
			listedExceptionModificationLock.readLock().unlock();
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
	 * Get or create a standard exception in the model container copying any
	 * relevant information from the standard model to the model in the modelContainer
	 * @param modelContainer
	 * @param node
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ListedLicenseException getLicenseFromStdLicModel(
			IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		Map<Node, ListedLicenseException> modelNodeCache = this.listedExceptionNodeCache.get(modelContainer);
		if (modelNodeCache == null) {
			modelNodeCache = Maps.newHashMap();
			this.listedExceptionNodeCache.put(modelContainer, modelNodeCache);
		}
		if (modelNodeCache.containsKey(node)) {
			return modelNodeCache.get(node);
		}
		ListedLicenseException retval = new ListedLicenseException(modelContainer, node);
		if (!this.equals(modelContainer)) {
			String exceptionId = retval.getLicenseExceptionId();
			if (exceptionId == null) {
				URL exceptionUrl;
				try {
					exceptionUrl = new URL(node.getURI());
				} catch (MalformedURLException e) {
					throw new InvalidSPDXAnalysisException("Invalid exception URL");
				}
				exceptionId = this.urlToId(exceptionUrl);
			}
			try {
				ListedLicenseException licenseFromModel = getListedExceptionById(exceptionId);
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
		return(listdExceptionIds.contains(elementRef));
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#addSpdxElementRef(java.lang.String)
	 */
	@Override
	public void addSpdxElementRef(String elementRef) {
		listdExceptionIds.add(elementRef);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#documentNamespaceToId(java.lang.String)
	 */
	@Override
	public String documentNamespaceToId(String externalNamespace) {
		// Listed exceptions do not support external documents
		return null;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#externalDocumentIdToNamespace(java.lang.String)
	 */
	@Override
	public String externalDocumentIdToNamespace(String docId) {
		// Listed exceptions do not support external documents
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
			return listedExceptionModel.createResource(getType(listedExceptionModel));
		} else {
			return listedExceptionModel.createResource(uri, getType(listedExceptionModel));
		}
	}

	/**
	 * @param model
	 * @return
	 */
	private Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_LICENSE_EXCEPTION);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IModelContainer#addCheckNodeObject(org.apache.jena.graph.Node, org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean addCheckNodeObject(Node node, IRdfModel rdfModelObject) {
		// TODO Refactor and implement
		return true;
	}

	/**
	 * @param id exception ID
	 * @return true if the exception ID is a supported SPDX listed exception
	 */
	public boolean isSpdxListedLExceptionID(String id) {
        try {
            listedExceptionModificationLock.readLock().lock();
            return listdExceptionIds.contains(id);
        } finally {
            listedExceptionModificationLock.readLock().unlock();
        }
	}

	/**
	 * @param id
	 * @return the standard SPDX license exception or null if the ID is not in the SPDX license list
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ListedLicenseException getListedExceptionById(String id) throws InvalidSPDXAnalysisException {
		ListedLicenseException retval = getExceptionFromUri(ListedLicenses.LISTED_LICENSE_URI_PREFIX + id + JSONLD_URL_SUFFIX);
		if (retval != null) {
			retval = (ListedLicenseException)retval.clone();	// We need to clone the license to remove the references to the model in the cache
		}
		return retval;
	}
}