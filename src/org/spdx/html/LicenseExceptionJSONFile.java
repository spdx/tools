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


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.LicenseException;

/**
 * This class holds a JSON details file for a specific license
 * @author Gary O'Neall
 *
 */
public class LicenseExceptionJSONFile extends AbstractJsonFile {

	private boolean deprecated;
	private LicenseException exception;

	/**
	 * Create a license JSON file with no license initialized
	 */
	public LicenseExceptionJSONFile() {
	}

	public LicenseExceptionJSONFile(LicenseException exception, boolean deprecated) {
		this.setException(exception, deprecated);
	}

	/**
	 * @param license
	 * @param deprecated True if the license ID is deprecated
	 */
	public void setException(LicenseException exception, boolean deprecated) {
		this.exception = exception;
		this.deprecated = deprecated;
	}

	/**
	 * @param deprecated True if the license ID is deprecated
	 */
	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}

	/**
	 * @return
	 */
	private Object isDeprecated() {
		return this.deprecated;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected JSONObject getJsonObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(SpdxRdfConstants.PROP_LICENSE_EXCEPTION_ID, exception.getLicenseExceptionId());
		jsonObject.put(SpdxRdfConstants.PROP_NAME, exception.getName());
		String[] seeAlsos = exception.getSeeAlso();
		if (seeAlsos != null && seeAlsos.length > 0) {
			JSONArray seeAlsoArray = new JSONArray();
			for (String seeAlso:seeAlsos) {
				seeAlsoArray.add(seeAlso);
			}
			jsonObject.put(SpdxRdfConstants.RDFS_PROP_SEE_ALSO, seeAlsoArray);
		}
		jsonObject.put(SpdxRdfConstants.PROP_LIC_COMMENTS, exception.getComment());
		jsonObject.put(SpdxRdfConstants.PROP_EXCEPTION_TEXT, exception.getLicenseExceptionText().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n"));
		jsonObject.put(SpdxRdfConstants.PROP_EXAMPLE, exception.getExample());
		jsonObject.put(SpdxRdfConstants.PROP_LIC_ID_DEPRECATED, this.isDeprecated());
		return jsonObject;
	}

}
