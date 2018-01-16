/**
 * Copyright (c) 2017 Source Auditor Inc.
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
 */
package org.spdx.tools.licensegenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.spdx.tools.LicenseGeneratorException;

import com.google.common.collect.Maps;

/**
 * Singleton class which returns information maintained by the Free Software Foundation
 * 
 * The default behavior is to pull the FSF data from <code>https://wking.github.io/fsf-api/licenses-full.json</code>
 * 
 * If the URL is not accessible, the file resources/licenses-full.json in the same path as the .jar file will be used.
 * If the local file can not be found, then a properties file resources/licenses-full.json will be used.
 * 
 * There are two properties that can be used to control where the JSON file is loaded from:
 *   LocalFsfFreeJson - if set to true, then use the local file or, if the local file is not found, the resource files and don't access the file from the github.io page
 *   FsfFreeJsonUrl - the URL to pull the JSON file from.  If both LocalFsfFreeJson and FsfFreeJsonUrl are specified, then
 *   the LocalFsfFreeJson takes precedence and the local resource file will be used.
 * 
 * NOTE: This is currently using a non authoritative data source
 * 
 * TODO: Update the class to use an official FSF data source once available
 * @author Gary O'Neall
 *
 */
public class FsfLicenseDataParser {
	
	static final String PROP_USE_ONLY_LOCAL_FILE = "LocalFsfFreeJson";
	static final String PROP_FSF_FREE_JSON_URL = "FsfFreeJsonUrl";
	
	static final String DEFAULT_FSF_JSON_URL = "https://wking.github.io/fsf-api/licenses-full.json";
	static final String FSF_JSON_FILE_PATH = "resources" + File.separator + "licenses-full.json";
	static final String FSF_JSON_CLASS_PATH = "resources/licenses-full.json";

	static final String FSF_JSON_NAMESPACE = "http://tremily.us/fsf/schema/";
	static final String PROPERTY_TAGS = "license.jsonldtags";
	private static final String PROPERTY_SPDXID = "license.jsonldspdx";
	private static final String PROPERTY_IDENTIFIERS = "license.jsonldidentifiers";
		
	private static FsfLicenseDataParser fsfLicenseDataParser = null;
	private Map<String, Boolean> licenseIdToFsfFree;
	private boolean useOnlyLocalFile = false;
	private String licenseJsonUrl = DEFAULT_FSF_JSON_URL;
	
	private FsfLicenseDataParser() throws LicenseGeneratorException {
		licenseIdToFsfFree = Maps.newHashMap();
		useOnlyLocalFile = Boolean.parseBoolean(System.getProperty(PROP_USE_ONLY_LOCAL_FILE, "false"));
		licenseJsonUrl = System.getProperty(PROP_FSF_FREE_JSON_URL, DEFAULT_FSF_JSON_URL);
		InputStream input = null;
		ClassLoader oldContextCL = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			if (!useOnlyLocalFile) {
				// First, try the URL
				try {
					URL url = new URL(licenseJsonUrl);
					input = url.openStream();
				} catch (MalformedURLException e) {
					input = null;
				} catch (IOException e) {
					input = null;
				}
			}
			if (input == null) {
				// try the file system
				try {
					input = new FileInputStream(FSF_JSON_FILE_PATH);
				} catch (FileNotFoundException e) {
					input = null;
				}
			}
			if (input == null) {
				try {
					input = new FileInputStream(FSF_JSON_FILE_PATH);
				} catch (FileNotFoundException e) {
					throw new LicenseGeneratorException("Unable to open reader for the FSF API");
				}
			}

			Model model = ModelFactory.createDefaultModel();
			model.read(input, null, "JSON-LD");
			
			Node p = model.getProperty(FSF_JSON_NAMESPACE, PROPERTY_TAGS).asNode();
			Triple m = Triple.createMatch(null, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			StringWriter sw = new StringWriter();
			model.write(sw);
			String debug = sw.toString();
			
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				if (t.getObject().isLiteral()) {
					String objectVal = t.getObject().toString(false);
					if ("libre".equals(objectVal)) {
						Node subject = t.getSubject();
						List<String> spdxIds = findSpdxIds(subject, model);
						for (String spdxId:spdxIds) {
							this.licenseIdToFsfFree.put(spdxId,true);
						}
					}
				}
			}
		} catch(Exception ex) {
			throw new LicenseGeneratorException("Error parsing FSF license data");
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					throw new LicenseGeneratorException("Unable to close input for the FSF API");
				}
			}
			Thread.currentThread().setContextClassLoader(oldContextCL);
		}
	}
	
	/**
	 * @param subject Subject of the RDF triple which contains the SPDX ID's
	 * @param model
	 * @return all SPDX ID's associated with the subject
	 * @throws LicenseGeneratorException 
	 */
	private List<String> findSpdxIds(Node subject, Model model) throws LicenseGeneratorException {
		Node identifiersProp = model.getProperty(FSF_JSON_NAMESPACE, PROPERTY_IDENTIFIERS).asNode();
		Triple identifersMatch = Triple.createMatch(subject, identifiersProp, null);
		ExtendedIterator<Triple> identifiersIterator = model.getGraph().find(identifersMatch);
		List<String> retval = Lists.newArrayList();
		while (identifiersIterator.hasNext()) {
			Node identifiersObject = identifiersIterator.next().getObject();
			if (identifiersObject == null) {
				continue;
			}
			Node spdxIdProp = model.getProperty(FSF_JSON_NAMESPACE, PROPERTY_SPDXID).asNode();
			Triple spdxIdMatch = Triple.createMatch(identifiersObject, spdxIdProp, null);
			ExtendedIterator<Triple> spdxIdIterator = model.getGraph().find(spdxIdMatch);
			while (spdxIdIterator.hasNext()) {
				Node spdxIdObject = spdxIdIterator.next().getObject();
				if (spdxIdObject == null) {
					continue;
				}
				if (!spdxIdObject.isLiteral()) {
					throw new LicenseGeneratorException("SPDX ID is not a literal");
				}
				retval.add(spdxIdObject.toString(false));
			}
		}
		return retval;
	}

	public static synchronized FsfLicenseDataParser getFsfLicenseDataParser() throws LicenseGeneratorException {
		if (fsfLicenseDataParser == null) {
			fsfLicenseDataParser = new FsfLicenseDataParser();
		}
		return fsfLicenseDataParser;
	}

	/**
	 * Determines if an SPDX license is designated as FSF Free / Libre by FSF.  Reference https://www.gnu.org/licenses/license-list.en.html
	 * @param spdxLicenseId
	 * @return true if FSF describes the license as free / libre, false if FSF describes the license as not free / libre, null if FSF does not reference the license
	 */
	public Boolean isSpdxLicenseFsfLibre(String spdxLicenseId) {
		return this.licenseIdToFsfFree.get(spdxLicenseId);
	}

}
