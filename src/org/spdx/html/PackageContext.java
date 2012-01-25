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

import java.util.ArrayList;
import java.util.List;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SpdxPackageVerificationCode;

/**
 * Context for SPDX Package
 * @author Gary O'Neall
 *
 */
public class PackageContext {
	
	private SPDXPackage pkg = null;

	/**
	 * @param doc
	 * @throws InvalidSPDXAnalysisException 
	 */
	public PackageContext(SPDXDocument doc) throws InvalidSPDXAnalysisException {
		pkg = doc.getSpdxPackage();
	}
	
	public String name() {
		if (pkg != null) {
			try {
				return pkg.getDeclaredName();
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package name: "+e.getMessage();
			}
		} else {
			return null;
		}
	}

	public String versionInfo() {
		if (pkg != null) {
			try {
				return pkg.getVersionInfo();
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package version: "+e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public String downloadLocation() {
		if (pkg != null) {
			try {
				return pkg.getDownloadUrl();
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package download URL: "+e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public String summary() {
		if (pkg != null) {
			try {
				return pkg.getShortDescription();
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package summary: "+e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public String sourceInfo() {
		if (pkg != null) {
			try {
				return pkg.getSourceInfo();
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package source info: "+e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public String packageFileName() {
		if (pkg != null) {
			try {
				return pkg.getFileName();
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package file name: "+e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public String supplier() {
		if (pkg != null) {
			try {
				return pkg.getSupplier();
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package supplier: "+e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public String originator() {
		if (pkg != null) {
			try {
				return pkg.getOriginator();
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package originator: "+e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public String description() {
		if (pkg != null) {
			try {
				return pkg.getDescription();
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package description: "+e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public VerificationCodeContext packageVerificationCode() {
		if (pkg == null) {
			return null;
		}
		SpdxPackageVerificationCode verificationCode;
		try {
			verificationCode = pkg.getVerificationCode();
			if (verificationCode == null) {
				return null;
			}
			return new VerificationCodeContext(verificationCode);
		} catch (InvalidSPDXAnalysisException e) {
			return new VerificationCodeContext(e);
		}

	}
	
	public String checksum() {
		if (pkg != null) {
			try {
				return "SHA1: "+pkg.getSha1();
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package checksum: "+e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public String copyrightText() {
		if (pkg != null) {
			try {
				return pkg.getDeclaredCopyright();
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package copyright: "+e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public String licenseDeclared() {
		if (pkg != null) {
			try {
				SPDXLicenseInfo info = pkg.getDeclaredLicense();
				if (info != null) {
					return info.toString();
				} else {
					return null;
				}
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package copyright: "+e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public String licenseConcluded() {
		if (pkg != null) {
			try {
				SPDXLicenseInfo info = pkg.getConcludedLicenses();
				if (info != null) {
					return info.toString();
				} else {
					return null;
				}
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package concluded license: "+e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public String licenseComments() {
		if (pkg != null) {
			try {
				return pkg.getLicenseComment();
			} catch (InvalidSPDXAnalysisException e) {
				return "Error getting SPDX Package license comments: "+e.getMessage();
			}
		} else {
			return null;
		}
	}
	
	public List<String> licenseInfoFromFiles() {
		ArrayList<String> retval = new ArrayList<String>();
		if (pkg != null) {
			SPDXLicenseInfo[] licenseInfos = null;
			try {
				licenseInfos = pkg.getLicenseInfoFromFiles();
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Error geting license information from files: "+e.getMessage());
			}
			if (licenseInfos != null) {
				for (int i = 0; i < licenseInfos.length; i++) {
					retval.add(licenseInfos[i].toString());
				}
			}
		}
		return retval;
	}
	
	public List<FileContext> hasFile() {
		ArrayList<FileContext> retval = new ArrayList<FileContext>();
		if (pkg != null) {
			SPDXFile[] files;
			try {
				files = pkg.getFiles();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						retval.add(new FileContext(files[i]));
					}
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add(new FileContext(e));
			}
		}
		return retval;
	}
	
}
