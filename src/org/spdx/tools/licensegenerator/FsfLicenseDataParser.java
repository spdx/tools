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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
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
	
	private static FsfLicenseDataParser fsfLicenseDataParser = null;
	private Map<String, Boolean> licenseIdToFsfFree;
	private boolean useOnlyLocalFile = false;
	private String licenseJsonUrl = DEFAULT_FSF_JSON_URL;
	
	private FsfLicenseDataParser() throws LicenseGeneratorException {
		licenseIdToFsfFree = Maps.newHashMap();
		useOnlyLocalFile = Boolean.parseBoolean(System.getProperty(PROP_USE_ONLY_LOCAL_FILE, "false"));
		licenseJsonUrl = System.getProperty(PROP_FSF_FREE_JSON_URL, DEFAULT_FSF_JSON_URL);
		Reader reader = null;
		try {
			if (!useOnlyLocalFile) {
				// First, try the URL
				try {
					URL url = new URL(licenseJsonUrl);
					reader = new BufferedReader(new InputStreamReader(url.openStream()));
				} catch (MalformedURLException e) {
					reader = null;
				} catch (IOException e) {
					reader = null;
				}
			}
			if (reader == null) {
				// try the file system
				try {
					reader = new BufferedReader(new FileReader(FSF_JSON_FILE_PATH));
				} catch (FileNotFoundException e) {
					reader = null;
				}
			}
			if (reader == null) {
				try {
					reader = new BufferedReader(new FileReader(FSF_JSON_CLASS_PATH));
				} catch (FileNotFoundException e) {
					throw new LicenseGeneratorException("Unable to open reader for the FSF API");
				}
			}
			JSONObject fsfLicenses = (JSONObject)JSONValue.parseWithException(reader);
			@SuppressWarnings("unchecked")
			Iterator<Entry<String, JSONObject>> iter = fsfLicenses.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, JSONObject> entry = iter.next();
				JSONObject fsfLicense = (JSONObject)entry.getValue();
				JSONObject identifiers = (JSONObject)fsfLicense.get("identifiers");
				if (identifiers != null) {
					String spdxId = (String)identifiers.get("spdx");
					if (spdxId != null) {
						Boolean fsfLibre = false;
						JSONArray tags = (JSONArray)fsfLicense.get("tags");
						if (tags != null) {
							for (Object tag:tags) {
								if ("libre".equals(tag)) {
									fsfLibre = true;
									break;
								}
							}
						}
						this.licenseIdToFsfFree.put(spdxId, fsfLibre);
					}
				}
			}
		} catch (IOException e) {
			throw new LicenseGeneratorException("IO error reading FSF license information");
		} catch (ParseException e) {
			throw new LicenseGeneratorException("Parsing error reading FSF license information");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new LicenseGeneratorException("Unable to close reader for the FSF API");
				}
			}
		}
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
