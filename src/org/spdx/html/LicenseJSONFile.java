/**
 * Copyright (c) 2016 Source Auditor Inc.
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
package org.spdx.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.SpdxListedLicense;

/**
 * This class holds a JSON details file for a specific license
 * @author Gary O'Neall
 *
 */
public class LicenseJSONFile {

	private boolean deprecated;
	private SpdxListedLicense license;

	/**
	 * Create a license JSON file with no license initialized
	 */
	public LicenseJSONFile() {
	}

	/**
	 * @param license
	 * @param deprecated True if the license ID is deprecated
	 */
	public void setLicense(SpdxListedLicense license, boolean deprecated) {
		this.license = license;
		this.deprecated = deprecated;
	}

	/**
	 * @param deprecated True if the license ID is deprecated
	 */
	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}

	/**
	 * @param jsonFile File to write JSON data to
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public void writeToFile(File jsonFile) throws IOException {
		OutputStreamWriter writer = null;
		if (!jsonFile.exists()) {
			if (!jsonFile.createNewFile()) {
				throw(new IOException("Can not create new file "+jsonFile.getName()));
			}
		}
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(SpdxRdfConstants.PROP_LICENSE_ID, license.getLicenseId());
			jsonObject.put(SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED, license.isOsiApproved());
			jsonObject.put(SpdxRdfConstants.PROP_STD_LICENSE_NAME, license.getName());
			String[] seeAlsos = license.getSeeAlso();
			if (seeAlsos != null && seeAlsos.length > 0) {
				JSONArray seeAlsoArray = new JSONArray();
				for (String seeAlso:seeAlsos) {
					seeAlsoArray.add(seeAlso);
				}
				jsonObject.put(SpdxRdfConstants.RDFS_PROP_SEE_ALSO, seeAlsoArray);
			}
			jsonObject.put(SpdxRdfConstants.PROP_LIC_COMMENTS, license.getComment());
			jsonObject.put(SpdxRdfConstants.PROP_LICENSE_TEXT, license.getLicenseText().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n"));
			jsonObject.put(SpdxRdfConstants.PROP_STD_LICENSE_NOTICE, license.getStandardLicenseHeader().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n"));
			jsonObject.put(SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE, license.getStandardLicenseTemplate().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n"));
			jsonObject.put(SpdxRdfConstants.PROP_LIC_ID_DEPRECATED, this.isDeprecated());
			writer = new OutputStreamWriter(new FileOutputStream(jsonFile), "UTF-8");
			writer.write(jsonObject.toJSONString());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * @return
	 */
	private Object isDeprecated() {
		return this.deprecated;
	}

}
