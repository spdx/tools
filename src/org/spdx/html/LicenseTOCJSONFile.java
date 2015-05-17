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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.SpdxListedLicense;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * This class holds a JSON file for a license table of contents
 * @author Kyle E. Mitchell
 *
 */
public class LicenseTOCJSONFile {
	
	public class ListedSpdxLicense {
		private String reference;
		private String refNumber;
		private String licenseId;
		private boolean osiApproved;
		private String licenseName;
		
		public ListedSpdxLicense() {
			reference = null;
			refNumber = null;
			licenseId = null;
			osiApproved = false;
			licenseName = null;
		}
		
		public ListedSpdxLicense(String reference, String refNumber, 
				String licenseId, boolean osiApproved, String licenseName) {
			this.reference = reference;
			this.refNumber = refNumber;
			this.licenseId = licenseId;
			this.osiApproved = osiApproved;
			this.licenseName = licenseName;
		}

		public String getReference() {
			return reference;
		}

		public void setReference(String reference) {
			this.reference = reference;
		}

		public String getRefNumber() {
			return refNumber;
		}

		public void setRefNumber(String refNumber) {
			this.refNumber = refNumber;
		}

		public String getLicenseId() {
			return licenseId;
		}

		public void setLicenseId(String licenseId) {
			this.licenseId = licenseId;
		}

		public boolean getOsiApproved() {
			return osiApproved;
		}

		public void setOsiApproved(boolean osiApproved) {
			this.osiApproved = osiApproved;
		}

		public String getLicenseName() {
			return licenseName;
		}

		public void setLicenseName(String licenseName) {
			this.licenseName = licenseName;
		}
	}
	
	ArrayList<ListedSpdxLicense> listedLicenses = new ArrayList<ListedSpdxLicense>();
	
	private int currentRefNumber = 1;

	String version;
	String releaseDate;

	public LicenseTOCJSONFile(String version, String releaseDate) {
		this.version = version;
		this.releaseDate = releaseDate;
	}

	public void addLicense(SpdxListedLicense license, String licHTMLReference) {
		listedLicenses.add(new ListedSpdxLicense(licHTMLReference, String.valueOf(this.currentRefNumber), 
				license.getLicenseId(), license.isOsiApproved(), license.getName()));
		currentRefNumber++;
	}

	@SuppressWarnings("unchecked")
	public void writeToFile(File jsonFile) throws IOException {
		FileWriter writer = null;
		if (!jsonFile.exists()) {
			if (!jsonFile.createNewFile()) {
				throw(new IOException("Can not create new file "+jsonFile.getName()));
			}
		}
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(SpdxRdfConstants.PROP_LICENSE_LIST_VERSION, version);
			jsonObject.put("releaseDate", releaseDate);
			JSONArray licensesList = new JSONArray();
			for (ListedSpdxLicense license : listedLicenses) {
				JSONObject licenseJSON = new JSONObject();
				licenseJSON.put("reference", license.getReference());
				licenseJSON.put("referenceNumber", license.getRefNumber());
				licenseJSON.put(SpdxRdfConstants.PROP_LICENSE_ID, license.getLicenseId());
				licenseJSON.put(SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED, license.getOsiApproved());
				licenseJSON.put(SpdxRdfConstants.PROP_STD_LICENSE_NAME, license.getLicenseName());
				licensesList.add(licenseJSON);
			}
			jsonObject.put("licenses", licensesList);
			writer = new FileWriter(jsonFile);
			writer.write(jsonObject.toJSONString());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}
