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
 * NOTE: This is currently using a non authoritative data source
 * 
 * TODO: Update the class to use an official FSF data source once available
 * @author Gary O'Neall
 *
 */
public class FsfLicenseDataParser {
	
	static final String FSF_JSON_URL = "";	//TODO: Once FSF implements an API, replace this with the actual URL
	static final String FSF_JSON_FILE_PATH = "resources" + File.separator + "fsf-licenses.json";
	static final String FSF_JSON_CLASS_PATH = "resources/fsf-licenses.json";
	
	private static FsfLicenseDataParser fsfLicenseDataParser = null;
	private Map<String, Boolean> licenseIdToFsfFree;
	
	private FsfLicenseDataParser() throws LicenseGeneratorException {
		licenseIdToFsfFree = Maps.newHashMap();
		Reader reader = null;
		try {
			// First, try the URL
			try {
				URL url = new URL(FSF_JSON_URL);
				reader = new BufferedReader(new InputStreamReader(url.openStream()));
			} catch (MalformedURLException e) {
				reader = null;
			} catch (IOException e) {
				reader = null;
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
	 * @return
	 */
	public boolean isSpdxLicenseFsfLibre(String spdxLicenseId) {
		Boolean retval = this.licenseIdToFsfFree.get(spdxLicenseId);
		if (retval == null) {
			return false;
		} else {
			return retval;
		}
	}

}
