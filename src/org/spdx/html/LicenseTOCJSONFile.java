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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.SpdxListedLicense;

import com.google.common.collect.Lists;

/**
 * This class holds a JSON file for a license table of contents
 * @author Kyle E. Mitchell
 *
 */
public class LicenseTOCJSONFile extends AbstractJsonFile {
	
	public static final String JSON_REFERENCE_FIELD = "detailsUrl";
	
	private static class ListedSpdxLicense {
		private final String reference;
		private final String refNumber;
		private final String licenseId;
		private final boolean osiApproved;
		private final Boolean fsfLibre;
		private final String licenseName;
		private final String[] seeAlso;
		private boolean deprecated;
		private String licJSONReference;
		
		public ListedSpdxLicense(String reference, String refNumber, 
				String licenseId, boolean osiApproved, Boolean fsfLibre, String licenseName, String[] seeAlso, 
				boolean deprecated, String licJSONReference) {
			this.reference = reference;
			this.refNumber = refNumber;
			this.licenseId = licenseId;
			this.osiApproved = osiApproved;
			this.fsfLibre = fsfLibre;
			this.licenseName = licenseName;
			this.seeAlso = seeAlso;
			this.deprecated = deprecated;
			this.licJSONReference = licJSONReference;
		}

		/**
		 * @return the licJSONReference
		 */
		public String getLicJSONReference() {
			return licJSONReference;
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

		public String getLicenseId() {
			return licenseId;
		}

		public boolean getOsiApproved() {
			return osiApproved;
		}
		
		public Boolean getFsfLibre() {
			return fsfLibre;
		}

		public String getLicenseName() {
			return licenseName;
		}
		
		public String[] getSeeAlso() {
			return this.seeAlso;
		}

	}
	
	List<ListedSpdxLicense> listedLicenses = Lists.newArrayList();
	
	private int currentRefNumber = 1;

	String version;
	String releaseDate;

	public LicenseTOCJSONFile(String version, String releaseDate) {
		this.version = version;
		this.releaseDate = releaseDate;
	}
	
	/**
	 * Add a license to the JSON table of contents file
	 * @param license License to be added
	 * @param licHTMLReference file path to the license file HTML
	 * @param licHTMLReference file path to the license file JSON detail
	 * @param deprecated true if the license ID is deprecated
	 */
	public void addLicense(SpdxListedLicense license, String licHTMLReference,
			String licJSONReference, boolean deprecated) {
		listedLicenses.add(new ListedSpdxLicense(licHTMLReference, String.valueOf(this.currentRefNumber), 
				license.getLicenseId(), license.isOsiApproved(), license.getFsfLibre(), license.getName(), 
				license.getSeeAlso(), deprecated, relativeToAbsolute(licJSONReference)));
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

	@Override
	@SuppressWarnings("unchecked")
	protected JSONObject getJsonObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(SpdxRdfConstants.PROP_LICENSE_LIST_VERSION, version);
		jsonObject.put("releaseDate", releaseDate);
		JSONArray licensesList = new JSONArray();
		Collections.sort(listedLicenses, new Comparator<ListedSpdxLicense>() {
			@Override
			public int compare(ListedSpdxLicense o1, ListedSpdxLicense o2) {
				if (o1 == null) {
					if (o2 == null) {
						return 0;
					} else {
						return -1;
					}
				} else if (o2 == null) {
					return 1;
				} else {
					return o1.getLicenseId().compareTo(o2.getLicenseId());
				}
			}
		});
		for (ListedSpdxLicense license : listedLicenses) {
			JSONObject licenseJSON = new JSONObject();
			licenseJSON.put("reference", license.getReference());
			licenseJSON.put("referenceNumber", license.getRefNumber());
			licenseJSON.put(SpdxRdfConstants.PROP_LICENSE_ID, license.getLicenseId());
			licenseJSON.put(SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED, license.getOsiApproved());
			if (license.getFsfLibre() != null) {
				licenseJSON.put(SpdxRdfConstants.PROP_STD_LICENSE_FSF_LIBRE, license.getFsfLibre());
			}
			licenseJSON.put(SpdxRdfConstants.PROP_STD_LICENSE_NAME, license.getLicenseName());
			String[] seeAlsos = license.getSeeAlso();
			if (seeAlsos != null && seeAlsos.length > 0) {
				JSONArray seeAlsoArray = new JSONArray();
				for (String seeAlso:seeAlsos) {
					seeAlsoArray.add(seeAlso);
				}
				licenseJSON.put(SpdxRdfConstants.RDFS_PROP_SEE_ALSO, seeAlsoArray);
			}
			licenseJSON.put(SpdxRdfConstants.PROP_LIC_ID_DEPRECATED, license.isDeprecated());
			licenseJSON.put(JSON_REFERENCE_FIELD, license.getLicJSONReference());
			licensesList.add(licenseJSON);
		}
		jsonObject.put("licenses", licensesList);
		return jsonObject;
	}
}
