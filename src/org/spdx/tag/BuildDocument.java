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

	private static final String DEFAULT_SHA1 = "0000000000000000000000000000000000000000";
	
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
		tag = tag.trim()+" ";
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
			if (analysis.getCreatorInfo() == null) {
				SPDXCreatorInformation creator = new SPDXCreatorInformation(new String[] {  }, "", "");
				analysis.setCreationInfo(creator);
			}
			analysis.getCreatorInfo().setCreated(value);
		} else if (tag.equals(constants.getProperty("PROP_CREATION_COMMENT"))) {
			if (analysis.getCreatorInfo() == null) {
				SPDXCreatorInformation creator = new SPDXCreatorInformation(new String[] { value }, "", "");
				analysis.setCreationInfo(creator);
			}
			analysis.getCreatorInfo().setComment(value);
		} else if (tag.equals(constants.getProperty("PROP_SPDX_COMMENT"))) {
			analysis.setDocumentComment(value);
		} else if (tag.equals(constants.getProperty("PROP_REVIEW_REVIEWER"))) {
			lastReviewer = new SPDXReview(value, format.format(new Date()), ""); // update date later
			List<SPDXReview> reviewers = new ArrayList<SPDXReview>(Arrays.asList(analysis.getReviewers()));
			reviewers.add(lastReviewer);
			analysis.setReviewers(reviewers.toArray(new SPDXReview[0]));
		} else if (tag.equals(constants.getProperty("PROP_REVIEW_DATE"))) {
			if (lastReviewer == null) {
				throw(new InvalidSpdxTagFileException("Missing Reviewer - A reviewer must be provided before a review date"));
			}
			lastReviewer.setReviewDate(value);
		} else if (tag.equals(constants.getProperty("PROP_REVIEW_COMMENT"))) {
			if (lastReviewer == null) {
				throw(new InvalidSpdxTagFileException("Missing Reviewer - A reviewer must be provided before a review comment"));
			}
			lastReviewer.setComment(value);
		} else if (tag.equals(constants.getProperty("PROP_LICENSE_ID"))) {
			lastExtractedLicense = new SPDXNonStandardLicense(value, "WARNING: TEXT IS REQUIRED", null, null, null); //change text later
			SPDXNonStandardLicense[] currentNonStdLicenses = analysis.getExtractedLicenseInfos();
			List<SPDXNonStandardLicense> licenses = new ArrayList<SPDXNonStandardLicense>(Arrays.asList(currentNonStdLicenses));
			licenses.add(lastExtractedLicense);
			analysis.setExtractedLicenseInfos(licenses.toArray(new SPDXNonStandardLicense[0]));
		} else if (tag.equals(constants.getProperty("PROP_EXTRACTED_TEXT"))) {
			if (lastExtractedLicense == null) {
				throw(new InvalidSpdxTagFileException("Missing Extracted License - An  extracted license ID must be provided before the license text"));
			}
			lastExtractedLicense.setText(value);
		} else if (tag.equals(constants.getProperty("PROP_LICENSE_NAME"))) {
			if (lastExtractedLicense == null) {
				throw(new InvalidSpdxTagFileException("Missing Extracted License - An  extracted license ID must be provided before the license name"));
			}
			lastExtractedLicense.setLicenseName(value);
		} else if (tag.equals(constants.getProperty("PROP_SOURCE_URLS"))) {
			if (lastExtractedLicense == null) {
				throw(new InvalidSpdxTagFileException("Missing Extracted License - An  extracted license ID must be provided before the license URL"));
			}
			String[] values = value.split(",");
			for (int i = 0; i < values.length; i++) {
				values[i] = values[i].trim();
			}
			lastExtractedLicense.setSourceUrls(values);
		} else if (tag.equals(constants.getProperty("PROP_LICENSE_COMMENT"))) {
			if (lastExtractedLicense == null) {
				throw(new InvalidSpdxTagFileException("Missing Extracted License - An  extracted license ID must be provided before the license comment"));
			}
			lastExtractedLicense.setComment(value);
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
				String[] verification = value.split("\\(");
				String[] excludedFiles = verification[1].replace(")", "").split(",");
				for (int i = 0; i < excludedFiles.length; i++) {
					excludedFiles[i] = excludedFiles[i].trim();
				}
				pkg.setVerificationCode(new SpdxPackageVerificationCode(verification[0].trim(), excludedFiles));
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
			lastFile = new SPDXFile(value, SpdxRdfConstants.FILE_TYPE_OTHER, DEFAULT_SHA1, new SPDXNoneLicense(),
					new SPDXLicenseInfo[0], "", "", new DOAPProject[0]);
			pkg.addFile(lastFile);
		} else {
			if (lastFile == null) {
				if (tag.equals(constants.getProperty("PROP_FILE_TYPE")) || constants.getProperty("PROP_FILE_CHECKSUM").startsWith(tag) ||
						tag.equals(constants.getProperty("PROP_FILE_LICENSE")) || tag.equals(constants.getProperty("PROP_FILE_LIC_COMMENTS")) ||
						tag.equals(constants.getProperty("PROP_FILE_COPYRIGHT")) || tag.equals(constants.getProperty("PROP_FILE_COMMENT"))) {
					throw(new InvalidSpdxTagFileException("Missing File Name - A file name must be specified before the file properties"));
				} else {
					throw(new InvalidSpdxTagFileException("Unrecognized SPDX Tag: "+tag));
				}
			}
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
			} else if (tag.equals(constants.getProperty("PROP_FILE_COMMENT"))) {
				lastFile.setComment(value);
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
			lastProject = new DOAPProject(value, null);
			List<DOAPProject> projects = new ArrayList<DOAPProject>(Arrays.asList(file.getArtifactOf()));
			projects.add(lastProject);
			file.setArtifactOf(projects.toArray(new DOAPProject[0]));			
		} else {
			if (tag.equals(constants.getProperty("PROP_PROJECT_HOMEPAGE"))) {
				if (lastProject == null) {
					throw(new InvalidSpdxTagFileException("Missing Project Name - A project name must be provided before the project properties"));
				}
				lastProject.setHomePage(value);
			} else if (tag.equals(constants.getProperty("PROP_PROJECT_URI"))) {
				if (lastProject == null) {
					throw(new InvalidSpdxTagFileException("Missing Project Name - A project name must be provided before the project properties"));
				}
				// can not set the URI since it is already created, we need to replace DOAP project
				DOAPProject[] existingProjects = file.getArtifactOf();
				int i = 0;
				while (i < existingProjects.length && existingProjects[i] != lastProject) {
					i++;
				}
				if (i >= existingProjects.length) {
					existingProjects = Arrays.copyOf(existingProjects, existingProjects.length+1);
				}
				existingProjects[i] = new DOAPProject(lastProject.getName(), lastProject.getHomePage());
				existingProjects[i].setUri(value);
				file.setArtifactOf(existingProjects);
				lastProject = existingProjects[i];
			} else {
				throw(new InvalidSpdxTagFileException("Unrecognized tag: "+tag));
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
