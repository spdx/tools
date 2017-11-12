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
import java.util.Map;

import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet.DeprecatedLicenseInfo;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This class holds a formatted HTML file for a license table of contents
 * @author Gary O'Neall
 *
 */
public class LicenseTOCHTMLFile {
	
	static final String TEMPLATE_CLASS_PATH = "resources" + "/" + "htmlTemplate";
	static final String TEMPLATE_ROOT_PATH = "resources" + File.separator + "htmlTemplate";
	static final String HTML_TEMPLATE = "TocHTMLTemplate.html";

	public static class DeprecatedLicense {
		private String reference;
		private String refNumber;
		private String licenseId;
		private String licenseName;
		private String deprecatedVersion;
		
		public DeprecatedLicense(String reference, String refNumber, 
				String licenseId, String licenseName, String deprecatedVersion) {
			this.reference = reference;
			this.refNumber = refNumber;
			this.licenseId = licenseId;
			this.licenseName = licenseName;
			this.deprecatedVersion = deprecatedVersion;
		}

		/**
		 * @return the reference
		 */
		public String getReference() {
			return reference;
		}

		/**
		 * @param reference the reference to set
		 */
		public void setReference(String reference) {
			this.reference = reference;
		}

		/**
		 * @return the refNumber
		 */
		public String getRefNumber() {
			return refNumber;
		}

		/**
		 * @param refNumber the refNumber to set
		 */
		public void setRefNumber(String refNumber) {
			this.refNumber = refNumber;
		}

		/**
		 * @return the licenseId
		 */
		public String getLicenseId() {
			return licenseId;
		}

		/**
		 * @param licenseId the licenseId to set
		 */
		public void setLicenseId(String licenseId) {
			this.licenseId = licenseId;
		}

		/**
		 * @return the licenseName
		 */
		public String getLicenseName() {
			return licenseName;
		}

		/**
		 * @param licenseName the licenseName to set
		 */
		public void setLicenseName(String licenseName) {
			this.licenseName = licenseName;
		}

		/**
		 * @return the deprecatedVersion
		 */
		public String getDeprecatedVersion() {
			return deprecatedVersion;
		}

		/**
		 * @param deprecatedVersion the deprecatedVersion to set
		 */
		public void setDeprecatedVersion(String deprecatedVersion) {
			this.deprecatedVersion = deprecatedVersion;
		}
	}
	
	public static class ListedSpdxLicense {
		private String reference;
		private String refNumber;
		private String licenseId;
		private String osiApproved;
		private String fsfLibre;
		private String licenseName;
		
		public ListedSpdxLicense() {
			reference = null;
			refNumber = null;
			licenseId = null;
			osiApproved = null;
			licenseName = null;
			fsfLibre = null;
		}
		
		public ListedSpdxLicense(String reference, String refNumber, 
				String licenseId, boolean isOsiApproved, Boolean fsfLibre, String licenseName) {
			this.reference = reference;
			this.refNumber = refNumber;
			this.licenseId = licenseId;
			if (isOsiApproved) {
				this.osiApproved = "Y";
			} else {
				this.osiApproved = "";
			}
			if (fsfLibre != null && fsfLibre) {
				this.fsfLibre = "Y";
			} else {
				this.fsfLibre = "";
			}
			this.licenseName = licenseName;
		}

		/**
		 * @return the reference
		 */
		public String getReference() {
			return reference;
		}

		/**
		 * @param reference the reference to set
		 */
		public void setReference(String reference) {
			this.reference = reference;
		}

		/**
		 * @return the refNumber
		 */
		public String getRefNumber() {
			return refNumber;
		}

		/**
		 * @param refNumber the refNumber to set
		 */
		public void setRefNumber(String refNumber) {
			this.refNumber = refNumber;
		}

		/**
		 * @return the licenseId
		 */
		public String getLicenseId() {
			return licenseId;
		}

		/**
		 * @param licenseId the licenseId to set
		 */
		public void setLicenseId(String licenseId) {
			this.licenseId = licenseId;
		}

		/**
		 * @return the osiApproved
		 */
		public String getOsiApproved() {
			return osiApproved;
		}
		
		public String getFsfLibre() {
			return fsfLibre;
		}

		/**
		 * @param osiApproved the osiApproved to set
		 */
		public void setOsiApproved(String osiApproved) {
			this.osiApproved = osiApproved;
		}

		/**
		 * @return the licenseName
		 */
		public String getLicenseName() {
			return licenseName;
		}

		/**
		 * @param licenseName the licenseName to set
		 */
		public void setLicenseName(String licenseName) {
			this.licenseName = licenseName;
		}
	}
	
	List<ListedSpdxLicense> listedLicenses = Lists.newArrayList();
	List<DeprecatedLicense> deprecatedLicenses = Lists.newArrayList();
	
      private int currentRefNumber = 1;
      
      String version;
      String releaseDate;
      
      private String generateVersionString(String version, String releaseDate) {
    	  if (version == null || version.trim().isEmpty()) {
    		  return "";
    	  }
    	  String retval = version.trim();
    	  if (releaseDate != null && !releaseDate.trim().isEmpty()) {
    		  retval = retval + " "+ releaseDate.trim();
    	  }
    	  return retval;
      }
      public LicenseTOCHTMLFile(String version, String releaseDate) {
    	  this.version = version;
    	  this.releaseDate = releaseDate;
      }
      
	public void addLicense(SpdxListedLicense license, String licHTMLReference) {
		listedLicenses.add(new ListedSpdxLicense(licHTMLReference, String.valueOf(this.currentRefNumber), 
				license.getLicenseId(), license.isOsiApproved(), license.getFsfLibre(), license.getName()));
		currentRefNumber++;
	}

	public void writeToFile(File htmlFile) throws IOException, MustacheException {
		FileOutputStream stream = null;
		OutputStreamWriter writer = null;
		if (!htmlFile.exists()) {
			if (!htmlFile.createNewFile()) {
				throw(new IOException("Can not create new file "+htmlFile.getName()));
			}
		}
		String templateDirName = TEMPLATE_ROOT_PATH;
		File templateDirectoryRoot = new File(templateDirName);
		if (!(templateDirectoryRoot.exists() && templateDirectoryRoot.isDirectory())) {
			templateDirName = TEMPLATE_CLASS_PATH;
		}
		try {
			stream = new FileOutputStream(htmlFile);
			writer = new OutputStreamWriter(stream, "UTF-8");
			DefaultMustacheFactory builder = new DefaultMustacheFactory(templateDirName);
	        Map<String, Object> mustacheMap = buildMustachMap();
	        Mustache mustache = builder.compile(HTML_TEMPLATE);
	        mustache.execute(writer, mustacheMap);
		} finally {
			if (writer != null) {
				writer.close();
			}
			if (stream != null) {
				stream.close();
			}
		}
	}
	/**
	 * Build the a hash map to map the variables in the template to the values
	 * @return
	 */
	private Map<String, Object> buildMustachMap() {
		Map<String, Object> retval = Maps.newHashMap();
		retval.put("version", generateVersionString(version, releaseDate));
		retval.put("listedLicenses", this.listedLicenses);
		retval.put("deprecatedLicenses", this.deprecatedLicenses);
		return retval;
	}
	/**
	 * @param deprecatedLicense
	 * @param licHTMLReference
	 */
	public void addDeprecatedLicense(DeprecatedLicenseInfo deprecatedLicense,
			String licHTMLReference) {
		deprecatedLicenses.add(new DeprecatedLicense(licHTMLReference, String.valueOf(this.currentRefNumber), 
				deprecatedLicense.getLicense().getLicenseId(), 
				deprecatedLicense.getLicense().getName(),
				deprecatedLicense.getDeprecatedVersion()));
		currentRefNumber++;
	}
	

}
