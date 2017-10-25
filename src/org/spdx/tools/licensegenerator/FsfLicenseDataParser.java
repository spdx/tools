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
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.spdx.tools.LicenseGeneratorException;

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

	private static FsfLicenseDataParser fsfLicenseDataParser = null;
	private static Map<String, String> fsfID;

	private FsfLicenseDataParser() throws LicenseGeneratorException {
		fsfID = new HashMap<String, String>();
		URL url;
		try {
			url = new URL("https://wking.github.io/fsf-api/licenses.json");
		} catch (MalformedURLException e) {
			throw new LicenseGeneratorException("invalid FSF license-list URL", e);
		}
		try {
			Reader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			JSONObject index = (JSONObject)JSONValue.parseWithException(reader);
			Iterator iterator = index.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry entry = (Entry)iterator.next();
				JSONObject value = (JSONObject)entry.getValue();
				JSONObject identifiers = (JSONObject)value.get("identifiers");
				if (identifiers == null) {
					continue;
				}
				String spdx = (String)identifiers.get("spdx");
				if (spdx != null) {
					fsfID.put(spdx, (String)entry.getKey());
				}
			}
		} catch (IOException|ParseException e) {
			throw new LicenseGeneratorException("failure processing FSF license-list", e);
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
	public Boolean isSpdxLicenseFsfLibre(String spdxLicenseId) throws LicenseGeneratorException {
		String id = fsfID.get(spdxLicenseId);
		if (id == null) {
			return null;
		}
		URL url;
		try {
			url = new URL(String.format("https://wking.github.io/fsf-api/%s.json", id));
		} catch (MalformedURLException e) {
			throw new LicenseGeneratorException("invalid FSF license-list URL", e);
		}
		JSONArray tags;
		try {
			Reader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			JSONObject license = (JSONObject)JSONValue.parseWithException(reader);
			tags = (JSONArray)license.get("tags");
		} catch (IOException|ParseException e) {
			throw new LicenseGeneratorException(String.format("failure processing FSF metadata for %s", id), e);
		}
		if (tags == null) {
			return null;
		}
		return tags.contains("libre");
	}

}
