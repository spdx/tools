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
package org.spdx.tag;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.rdfparser.SPDXNoneLicense;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Translates an tag-value file to a an SPDX Document.
 * 
 * @author Rana Rahal, Protecode Inc.
 */

public class BuildDocument implements TagValueBehavior, Serializable {
	private static final long serialVersionUID = -5490491489627686708L;

	private Properties constants;
	private SPDXDocument analysis;
	private DateFormat format = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);

	//When we retrieve a list from the SPDXDocument the order changes, therefore keep track of 
	//the last object that we are looking at so that we can fill in all of it's information
	private SPDXReview lastReviewer = null;
	private SPDXNonStandardLicense lastExtractedLicense = null;
	private SPDXFile lastFile = null;
	private DOAPProject lastProject = null;

	public BuildDocument(Model model, SPDXDocument spdxDocument, Properties constants) {
		this.constants = constants;
		analysis = spdxDocument;
		try {
			analysis.createSpdxAnalysis("http://www.uri.com" + "#SPDXANALYSIS");
			analysis.createSpdxPackage();
		} catch (InvalidSPDXAnalysisException ex) {
			System.out
					.print("Error creating SPDX Analysis: " + ex.getMessage());
			return;
		}
	}

	public void enter() throws Exception {
		// do nothing???
	}

	public void buildDocument(String tag, String value) throws Exception {
		value = trim(value.trim());
		// document
		if (tag.equals(constants.getProperty("PROP_SPDX_VERSION"))) {
			analysis.setSpdxVersion(value);
		} else if (tag.equals(constants.getProperty("PROP_SPDX_DATA_LICENSE"))) {
			analysis.getDataLicense().setName(value);
		} else if (tag.equals(constants.getProperty("PROP_CREATION_CREATOR"))) {
			if (analysis.getCreatorInfo() == null) {
				SPDXCreatorInformation creator = new SPDXCreatorInformation(new String[] { value }, "", "");
				analysis.setCreationInfo(creator);
			} else {
				List<String> creators = new ArrayList<String>(Arrays.asList(analysis.getCreatorInfo().getCreators()));
				creators.add(value);
				analysis.getCreatorInfo().setCreators(creators.toArray(new String[0]));
			}
		} else if (tag.equals(constants.getProperty("PROP_CREATION_CREATED"))) {
			analysis.getCreatorInfo().setCreated(value);
		} else if (tag.equals(constants.getProperty("PROP_CREATION_COMMENT"))) {
			analysis.getCreatorInfo().setComment(value);
		} else if (tag.equals(constants.getProperty("PROP_REVIEW_REVIEWER"))) {
			if (lastReviewer != null) {
				List<SPDXReview> reviewers = new ArrayList<SPDXReview>(Arrays.asList(analysis.getReviewers()));
				reviewers.add(lastReviewer);
				analysis.setReviewers(reviewers.toArray(new SPDXReview[0]));
			}
			lastReviewer = new SPDXReview(value, format.format(new Date()), ""); // update date later
		} else if (tag.equals(constants.getProperty("PROP_REVIEW_DATE"))) {
			lastReviewer.setReviewDate(value);
		} else if (tag.equals(constants.getProperty("PROP_REVIEW_COMMENT"))) {
			lastReviewer.setReviewDate(value);
		} else if (tag.equals(constants.getProperty("PROP_LICENSE_ID"))) {
			if (lastExtractedLicense != null) {
				List<SPDXNonStandardLicense> licenses = new ArrayList<SPDXNonStandardLicense>(Arrays.asList(analysis.getExtractedLicenseInfos()));
				licenses.add(lastExtractedLicense);
				analysis.setExtractedLicenseInfos(licenses.toArray(new SPDXNonStandardLicense[0]));
			}
			lastExtractedLicense = new SPDXNonStandardLicense(value, "WARNING: TEXT IS REQUIRED"); //change text later
		} else if (tag.equals(constants.getProperty("PROP_EXTRACTED_TEXT"))) {
			lastExtractedLicense.setText(value);
		} else {
			SPDXPackage spdxPackage = analysis.getSpdxPackage();
			buildPackage(spdxPackage, tag, value);
		}
	}

	/**
	 * @param spdxPackage
	 * @throws InvalidSPDXAnalysisException
	 */
	private void buildPackage(SPDXPackage pkg, String tag, String value)
			throws Exception {
		if (tag.equals(constants.getProperty("PROP_PACKAGE_DECLARED_NAME"))) {
			pkg.setDeclaredName(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_VERSION_INFO"))) {
			pkg.setVersionInfo(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_DOWNLOAD_URL"))) {
			// TODO can we set analysis.getModel() uri?
			pkg.setDownloadUrl(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_SHORT_DESC"))) {
			pkg.setShortDescription(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_SOURCE_INFO"))) {
			pkg.setSourceInfo(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_FILE_NAME"))) {
			pkg.setFileName(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_SUPPLIER"))) {
			pkg.setSupplier(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_ORIGINATOR"))) {
			pkg.setOriginator(value);
		} else if (constants.getProperty("PROP_PACKAGE_CHECKSUM").startsWith(tag)) { // property contains SHA1:
			pkg.setSha1(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_VERIFICATION_CODE"))) {
			if (value.contains("(")) {
				String[] verification = value.split("(");
				String[] excudedFiles = value.replace(")", "").split(",");
				pkg.setVerificationCode(new SpdxPackageVerificationCode(verification[0].trim(), excudedFiles));
			}
			else {
				pkg.setVerificationCode(new SpdxPackageVerificationCode(value, new String[0]));
			}
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_DESCRIPTION"))) {
			pkg.setDescription(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_DECLARED_COPYRIGHT"))) {
			pkg.setDeclaredCopyright(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_DECLARED_LICENSE"))) {
			SPDXLicenseInfo licenseSet = SPDXLicenseInfoFactory.parseSPDXLicenseString(value);
			//TODO in the case of all licenses do we need to worry about the text? I'm only setting text in the package non-standard licenses
			pkg.setDeclaredLicense(licenseSet);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_CONCLUDED_LICENSE"))) {
			SPDXLicenseInfo licenseSet = SPDXLicenseInfoFactory.parseSPDXLicenseString(value);
			pkg.setConcludedLicenses(licenseSet);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_LICENSE_COMMENT"))) {
			pkg.setLicenseComment(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_LICENSE_INFO_FROM_FILES"))) {
			SPDXLicenseInfo license = SPDXLicenseInfoFactory.parseSPDXLicenseString(value);
			List<SPDXLicenseInfo> licenses = new ArrayList<SPDXLicenseInfo>(Arrays.asList(pkg.getLicenseInfoFromFiles()));
			licenses.add(license);
			pkg.setLicenseInfoFromFiles(licenses.toArray(new SPDXLicenseInfo[0]));
		} else {
			buildFile(pkg, tag, value);
		}
	}

	/**
	 * @param file
	 */
	private void buildFile(SPDXPackage pkg, String tag, String value)
			throws Exception {
		if (tag.equals(constants.getProperty("PROP_FILE_NAME"))) {
			if (lastFile != null) {
				List<SPDXFile> files = new ArrayList<SPDXFile>(Arrays.asList(pkg.getFiles()));
				files.add(lastFile);
				pkg.setFiles(files.toArray(new SPDXFile[0]));
			}
			lastFile = new SPDXFile(value, "", "", new SPDXNoneLicense(),
					new SPDXLicenseInfo[0], "", "", new DOAPProject[0]);
		} else {
			if (tag.equals(constants.getProperty("PROP_FILE_TYPE"))) {
				lastFile.setType(value);
			} else if (constants.getProperty("PROP_FILE_CHECKSUM").startsWith(tag)) {
				lastFile.setSha1(value);
			} else if (tag.equals(constants.getProperty("PROP_FILE_LICENSE"))) {
				SPDXLicenseInfo licenseSet = SPDXLicenseInfoFactory.parseSPDXLicenseString(value);
				lastFile.setConcludedLicenses(licenseSet);
			} else if (tag.equals(constants.getProperty("PROP_FILE_SEEN_LICENSE"))) {
				SPDXLicenseInfo fileLicense = (SPDXLicenseInfoFactory.parseSPDXLicenseString(value));
				List<SPDXLicenseInfo> seenLicenses = new ArrayList<SPDXLicenseInfo>(Arrays.asList(lastFile.getSeenLicenses()));
				seenLicenses.add(fileLicense);
				lastFile.setSeenLicenses(seenLicenses.toArray(new SPDXLicenseInfo[0]));
			} else if (tag.equals(constants.getProperty("PROP_FILE_LIC_COMMENTS"))) {
				lastFile.setLicenseComments(value);
			} else if (tag.equals(constants.getProperty("PROP_FILE_COPYRIGHT"))) {
				lastFile.setCopyright(value);
			} else {
				buildProject(lastFile, tag, value);
			}
		}
	}

	/**
	 * @param doapProject
	 */
	private void buildProject(SPDXFile file, String tag, String value)
			throws Exception {
		if (tag.equals(constants.getProperty("PROP_PROJECT_NAME"))) {
			if (lastProject != null) {
				List<DOAPProject> projects = new ArrayList<DOAPProject>(Arrays.asList(file.getArtifactOf()));
				projects.add(lastProject);
				file.setArtifactOf(projects.toArray(new DOAPProject[0]));
			}
			lastProject = new DOAPProject(value, "");
		} else {
			if (tag.equals(constants.getProperty("PROP_PROJECT_HOMEPAGE"))) {
				lastProject.setHomePage(value);
			} else if (tag.equals(constants.getProperty("PROP_PROJECT_URI"))) {
				lastProject.setUri(value);
			}
		}
	}

	private static String trim(String value) {
		value.trim();
		value = value.replaceAll("<text>", "").replaceAll("</text>", "")
				.replaceAll("SHA1: ", "");
		return value;
	}

	public void exit() throws Exception {
		if (lastProject != null) {
			List<DOAPProject> projects = new ArrayList<DOAPProject>(Arrays.asList(lastFile.getArtifactOf()));
			projects.add(lastProject);
			lastFile.setArtifactOf(projects.toArray(new DOAPProject[0]));
			lastProject = null;
		}
		if (lastFile != null) {
			List<SPDXFile> files = new ArrayList<SPDXFile>(Arrays.asList(analysis.getSpdxPackage().getFiles()));
			files.add(lastFile);
			analysis.getSpdxPackage().setFiles(files.toArray(new SPDXFile[0]));
		}
		if (lastReviewer != null) {
			List<SPDXReview> reviewers = new ArrayList<SPDXReview>(Arrays.asList(analysis.getReviewers()));
			reviewers.add(lastReviewer);
			analysis.setReviewers(reviewers.toArray(new SPDXReview[0]));
		}
		if (lastExtractedLicense != null) {
			List<SPDXNonStandardLicense> licenses = new ArrayList<SPDXNonStandardLicense>(
						Arrays.asList(analysis.getExtractedLicenseInfos()));
			licenses.add(lastExtractedLicense);
			analysis.setExtractedLicenseInfos(licenses.toArray(new SPDXNonStandardLicense[0]));
		}

		ArrayList<String> warningMessages = analysis.verify();
		assertEquals("SPDXDocument", 0, warningMessages);
	}
	
	private static void assertEquals(String name, int expected,
			ArrayList<String> verify) {
		if (verify.size() > expected) {
			System.out.println("The following verifications failed for the " + name + ":");
			for (int x = 0; x < verify.size(); x++) {
				System.out.println("\t" + verify.get(x));
			}
		}
	}
}
