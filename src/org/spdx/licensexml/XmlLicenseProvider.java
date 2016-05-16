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
package org.spdx.licensexml;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.ISpdxListedLicenseProvider;
import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.LicenseRestrictionException;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.license.SpdxListedLicenseException;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet.DeprecatedLicenseInfo;
import org.spdx.spdxspreadsheet.SpreadsheetException;

import com.google.common.io.Files;

/**
 * Provide license information from XML files
 * @author Gary O'Neall
 *
 */
public class XmlLicenseProvider implements ISpdxListedLicenseProvider {
	
	Logger logger = Logger.getLogger(XmlLicenseProvider.class.getName());
	
	class XmlLicenseIterator implements Iterator<SpdxListedLicense> {
		
		private int xmlFileIndex = 0;
		private SpdxListedLicense nextListedLicense = null;
		
		public XmlLicenseIterator() {
			findNextItem();
		}

		private void findNextItem() {
			nextListedLicense = null;
			while (xmlFileIndex < xmlFiles.length && nextListedLicense == null) {
				try {
					LicenseXmlDocument licDoc = new LicenseXmlDocument(xmlFiles[xmlFileIndex]);
					if (licDoc.isListedLicense() && !licDoc.isDeprecated()) {
						try {
							this.nextListedLicense = licDoc.getListedLicense();
						} catch (InvalidSPDXAnalysisException e) {
							logger.error("Invalid SPDX license in XML document "+xmlFiles[xmlFileIndex].getName(),e);
							this.nextListedLicense = null;	// continue to look for the next valid license
						}
					}
				} catch(LicenseXmlException e) {
					logger.warn(e.getMessage() + ", Skipping file "+xmlFiles[xmlFileIndex].getName());
				}
				xmlFileIndex++;
			}
		}
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return this.nextListedLicense != null;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public SpdxListedLicense next() {
			SpdxListedLicense retval = this.nextListedLicense;
			this.findNextItem();
			return retval;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			// Not implemented
		}
	}
	
	class XmlExceptionIterator implements Iterator<LicenseException> {
		
		private int xmlFileIndex = 0;
		private LicenseException nextLicenseException = null;
		
		public XmlExceptionIterator() {
			findNextItem();
		}

		private void findNextItem() {
			nextLicenseException = null;
			while (xmlFileIndex < xmlFiles.length && nextLicenseException == null) {
				try {
					LicenseXmlDocument licDoc = new LicenseXmlDocument(xmlFiles[xmlFileIndex]);
					if (licDoc.isLicenseException() && !licDoc.isDeprecated()) {
						this.nextLicenseException = licDoc.getLicenseException();
					}
				} catch(LicenseXmlException e) {
					logger.warn(e.getMessage() + ", Skipping file "+xmlFiles[xmlFileIndex].getName());
				}
				xmlFileIndex++;
			}
		}
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return this.nextLicenseException != null;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public LicenseException next() {
			LicenseException retval = this.nextLicenseException;
			this.findNextItem();
			return retval;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			// Not implemented
		}
	}

	class XmlDeprecatedLicenseIterator implements Iterator<DeprecatedLicenseInfo> {
		
		private int xmlFileIndex = 0;
		private DeprecatedLicenseInfo nextDeprecatedLicense = null;
		
		public XmlDeprecatedLicenseIterator() {
			findNextItem();
		}

		private void findNextItem() {
			nextDeprecatedLicense = null;
			while (xmlFileIndex < xmlFiles.length && nextDeprecatedLicense == null) {
				try {
					LicenseXmlDocument licDoc = new LicenseXmlDocument(xmlFiles[xmlFileIndex]);
					if (licDoc.isListedLicense() && licDoc.isDeprecated()) {
						this.nextDeprecatedLicense = licDoc.getDeprecatedLicenseInfo();
					}
				} catch(LicenseXmlException e) {
					logger.warn(e.getMessage() + ", Skipping file "+xmlFiles[xmlFileIndex].getName());
				}
				xmlFileIndex++;
			}
		}
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return this.nextDeprecatedLicense != null;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public DeprecatedLicenseInfo next() {
			DeprecatedLicenseInfo retval = this.nextDeprecatedLicense;
			this.findNextItem();
			return retval;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			// Not implemented
		}
	}
	
	private File[] xmlFiles = new File[0];

	/**
	 * @param xmlFileDirectory directory of XML files
	 * @throws SpdxListedLicenseException 
	 */
	public XmlLicenseProvider(File xmlFileDirectory) throws SpdxListedLicenseException {
		if (!xmlFileDirectory.isDirectory()) {
			throw(new SpdxListedLicenseException("XML File Directory is not a directory"));
		}
		this.xmlFiles = xmlFileDirectory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && "xml".equals(Files.getFileExtension(pathname.getName().toLowerCase()));
			}
			
		});
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.ISpdxListedLicenseProvider#getLicenseIterator()
	 */
	@Override
	public Iterator<SpdxListedLicense> getLicenseIterator()
			throws SpdxListedLicenseException {
		return new XmlLicenseIterator();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.ISpdxListedLicenseProvider#getExceptionIterator()
	 */
	@Override
	public Iterator<LicenseException> getExceptionIterator()
			throws LicenseRestrictionException, SpreadsheetException {
		return new XmlExceptionIterator();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.ISpdxListedLicenseProvider#getDeprecatedLicenseIterator()
	 */
	@Override
	public Iterator<DeprecatedLicenseInfo> getDeprecatedLicenseIterator() {
		return new XmlDeprecatedLicenseIterator();
	}

}
