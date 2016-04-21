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
package org.spdx.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.LicenseException;

import com.google.common.collect.Lists;

/**
 * This class holds a JSON file for a license exception table of contents
 * @author Gary O'Neall
 * Copied from LicenseTOCJSONFile by Kyle E. Mitchell
 *
 */
public class ExceptionTOCJSONFile {
	
	public static final String JSON_REFERENCE_FIELD = "detailsUrl";
	
	private static class ListedSpdxException {
		private final String reference;
		private final String refNumber;
		private final String exceptionId;
		private final String name;
		private final String[] seeAlso;
		private boolean deprecated;
		private String excJSONReference;
		
		public ListedSpdxException(String reference, String refNumber, 
				String exceptionId, String name, String[] seeAlso, boolean deprecated, String excJSONReference) {
			this.reference = reference;
			this.refNumber = refNumber;
			this.exceptionId = exceptionId;
			this.name = name;
			this.seeAlso = seeAlso;
			this.deprecated = deprecated;
			this.excJSONReference = excJSONReference;
		}

		/**
		 * @return the licJSONReference
		 */
		public String getExcJSONReference() {
			return excJSONReference;
		}

		/**
		 * @return the deprecated
		 */
		public boolean isDeprecated() {
			return deprecated;
		}

		public String getReference() {
			return reference;
		}

		public String getRefNumber() {
			return refNumber;
		}

		public String getExceptionId() {
			return exceptionId;
		}

		public String getName() {
			return name;
		}
		
		public String[] getSeeAlso() {
			return this.seeAlso;
		}

	}
	
	List<ListedSpdxException> listedExceptions = Lists.newArrayList();
	
	private int currentRefNumber = 1;

	String version;
	String releaseDate;

	public ExceptionTOCJSONFile(String version, String releaseDate) {
		this.version = version;
		this.releaseDate = releaseDate;
	}
	
	/**
	 * Add a license to the JSON table of contents file
	 * @param exception License Exception to be added
	 * @param excHTMLReference file path to the exception file HTML
	 * @param excHTMLReference file path to the exception file JSON detail
	 * @param deprecated true if the exception ID is deprecated
	 */
	public void addException(LicenseException exception, String excHTMLReference,
			String excJSONReference, boolean deprecated) {
		listedExceptions.add(new ListedSpdxException(excHTMLReference, String.valueOf(this.currentRefNumber), 
				exception.getLicenseExceptionId(), exception.getName(), 
				exception.getSeeAlso(),
				deprecated, relativeToAbsolute(excJSONReference)));
		currentRefNumber++;
	}

	/**
	 * Convert a relative file reference to an absolute URL for the spdx.org/licenses web page
	 * @param relativeRef
	 * @return
	 */
	private String relativeToAbsolute(String relativeRef) {
		String retval;
		if (relativeRef.startsWith("./")) {
			retval = relativeRef.substring(2);
		} else {
			retval = relativeRef;
		}
		retval = "http://spdx.org/licenses/" + retval;
		return retval;
	}

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
			jsonObject.put(SpdxRdfConstants.PROP_LICENSE_LIST_VERSION, version);
			jsonObject.put("releaseDate", releaseDate);
			JSONArray exceptionList = new JSONArray();
			for (ListedSpdxException exception : listedExceptions) {
				JSONObject exceptionJSON = new JSONObject();
				exceptionJSON.put("reference", exception.getReference());
				exceptionJSON.put("referenceNumber", exception.getRefNumber());
				exceptionJSON.put(SpdxRdfConstants.PROP_LICENSE_EXCEPTION_ID, exception.getExceptionId());
				exceptionJSON.put(SpdxRdfConstants.PROP_NAME, exception.getName());
				String[] seeAlsos = exception.getSeeAlso();
				if (seeAlsos != null && seeAlsos.length > 0) {
					JSONArray seeAlsoArray = new JSONArray();
					for (String seeAlso:seeAlsos) {
						seeAlsoArray.add(seeAlso);
					}
					exceptionJSON.put(SpdxRdfConstants.RDFS_PROP_SEE_ALSO, seeAlsoArray);
				}
				exceptionJSON.put(SpdxRdfConstants.PROP_LIC_ID_DEPRECATED, exception.isDeprecated());
				exceptionJSON.put(JSON_REFERENCE_FIELD, exception.getExcJSONReference());
				exceptionList.add(exceptionJSON);
			}
			jsonObject.put("exceptions", exceptionList);
			writer = new OutputStreamWriter(new FileOutputStream(jsonFile), "UTF-8");
			writer.write(jsonObject.toJSONString());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}
