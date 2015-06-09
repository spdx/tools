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
package org.spdx.rdfparser;

import java.util.regex.Pattern;


/**
 * Constants which map to the SPDX specifications found at http://spdx.org/rdf/terms
 * @author Gary O'Neall
 *
 */
public interface SpdxRdfConstants {

	// Namespaces
	public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDFS_NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String SPDX_NAMESPACE = "http://spdx.org/rdf/terms#";
	public static final String DOAP_NAMESPACE = "http://usefulinc.com/ns/doap#";
	public static final String OWL_NAMESPACE = "http://www.w3.org/2002/07/owl#";
	
	// RDF Properties
	public static final String RDF_PROP_TYPE = "type";
	public static final String RDF_PROP_RESOURCE = "resource";
	
	// OWL Properties
	public static final String PROP_OWL_SAME_AS = "sameAs";

	
	// RDFS Properties
	public static final String RDFS_PROP_COMMENT = "comment";
	public static final String RDFS_PROP_LABEL = "label";
	public static final String RDFS_PROP_SEE_ALSO = "seeAlso";
	
	// DOAP Class Names
	public static final String CLASS_DOAP_PROJECT = "Project";
	
	// DOAP Project Property Names
	public static final String PROP_PROJECT_NAME = "name";
	public static final String PROP_PROJECT_HOMEPAGE = "homepage";
	
	// SPDX Class Names
	public static final String CLASS_SPDX_DOCUMENT = "SpdxDocument";
	public static final String CLASS_SPDX_PACKAGE = "Package";
	public static final String CLASS_SPDX_CREATION_INFO = "CreationInfo";
	public static final String CLASS_SPDX_CHECKSUM = "Checksum";
	public static final String CLASS_SPDX_ANY_LICENSE_INFO = "AnyLicenseInfo";
	public static final String CLASS_SPDX_SIMPLE_LICENSE_INFO = "SimpleLicenseInfo";
	public static final String CLASS_SPDX_CONJUNCTIVE_LICENSE_SET = "ConjunctiveLicenseSet";
	public static final String CLASS_SPDX_DISJUNCTIVE_LICENSE_SET = "DisjunctiveLicenseSet";
	public static final String CLASS_SPDX_EXTRACTED_LICENSING_INFO = "ExtractedLicensingInfo";
	public static final String CLASS_SPDX_LICENSE = "License";
	public static final String CLASS_SPDX_LICENSE_EXCEPTION = "LicenseException";
	public static final String CLASS_OR_LATER_OPERATOR = "OrLaterOperator";
	public static final String CLASS_WITH_EXCEPTION_OPERATOR = "WithExceptionOperator";
	public static final String CLASS_SPDX_FILE = "File";
	public static final String CLASS_SPDX_REVIEW = "Review";
	public static final String CLASS_SPDX_VERIFICATIONCODE = "PackageVerificationCode";
	public static final String CLASS_ANNOTATION = "Annotation";
	public static final String CLASS_RELATIONSHIP = "Relationship";
	public static final String CLASS_SPDX_ITEM = "SpdxItem";
	public static final String CLASS_SPDX_ELEMENT = "SpdxElement";
	public static final String CLASS_EXTERNAL_DOC_REF = "ExternalDocumentRef";
	
	// General SPDX Properties
	public static final String PROP_VALUE_NONE = "none";
	public static final String URI_VALUE_NONE = SPDX_NAMESPACE  + PROP_VALUE_NONE;
	public static final String PROP_VALUE_NOASSERTION = "noassertion";
	public static final String URI_VALUE_NOASSERTION = SPDX_NAMESPACE + PROP_VALUE_NOASSERTION;
	
	// SPDX Document Properties
	// The comment property is the RDFS_PROP_COMMENT property in the rdfs namespace
	public static final String PROP_SPDX_REVIEWED_BY = "reviewed";
	public static final String PROP_SPDX_EXTRACTED_LICENSES = "hasExtractedLicensingInfo";
	public static final String PROP_SPDX_VERSION = "specVersion";
	public static final String PROP_SPDX_CREATION_INFO = "creationInfo";
	public static final String PROP_SPDX_PACKAGE = "describesPackage";
	@Deprecated		// since 2.0  Planned to be removed in next major spec revision
	public static final String PROP_SPDX_FILE_REFERENCE = "referencesFile";
	public static final String PROP_SPDX_DATA_LICENSE = "dataLicense";
	public static final String PROP_SPDX_EXTERNAL_DOC_REF = "externalDocumentRef";
	public static final String SPDX_DOCUMENT_ID = "SPDXRef-DOCUMENT";
	
	// SPDX CreationInfo Properties
	// The comment property is the RDFS_PROP_COMMENT property in the rdfs namespace
	public static final String PROP_CREATION_CREATOR = "creator";
	public static final String PROP_CREATION_CREATED = "created"; // creation timestamp
	public static final String PROP_LICENSE_LIST_VERSION = "licenseListVersion";
	public static final String CREATOR_PREFIX_PERSON = "Person:";
	public static final String CREATOR_PREFIX_ORGANIZATION = "Organization:";
	public static final String CREATOR_PREFIX_TOOL = "Tool:";
	
	// SPDX Checksum Properties
	public static final String PROP_CHECKSUM_ALGORITHM = "algorithm";
	public static final String PROP_CHECKSUM_VALUE = "checksumValue";
	public static final String ALGORITHM_SHA1 = "SHA1";
	public static final String PROP_CHECKSUM_ALGORITHM_SHA1 = "checksumAlgorithm_sha1";
	
	// SPDX PackageVerificationCode Properties
	public static final String PROP_VERIFICATIONCODE_IGNORED_FILES = "packageVerificationCodeExcludedFile";
	public static final String PROP_VERIFICATIONCODE_VALUE = "packageVerificationCodeValue";

	// SPDX Element Properties 
	public static final String PROP_ANNOTATION = "annotation";
	public static final String PROP_RELATIONSHIP = "relationship";
	
	// SPDX Item Properties 
	public static final String PROP_LICENSE_CONCLUDED = "licenseConcluded";
	public static final String PROP_COPYRIGHT_TEXT = "copyrightText";	
	public static final String PROP_LIC_COMMENTS = "licenseComments";
	public static final String PROP_LICENSE_DECLARED = "licenseDeclared";
	
	// SPDX Package Properties
	public static final String PROP_PACKAGE_DECLARED_NAME = "name";
	public static final String PROP_PACKAGE_FILE_NAME = "packageFileName";
	public static final String PROP_PACKAGE_CHECKSUM = "checksum";
	public static final String PROP_PACKAGE_DOWNLOAD_URL = "downloadLocation";
	public static final String PROP_PACKAGE_SOURCE_INFO = "sourceInfo";
	public static final String PROP_PACKAGE_DECLARED_LICENSE = "licenseDeclared";
	public static final String PROP_PACKAGE_CONCLUDED_LICENSE = PROP_LICENSE_CONCLUDED;
	public static final String PROP_PACKAGE_DECLARED_COPYRIGHT = PROP_COPYRIGHT_TEXT;
	public static final String PROP_PACKAGE_SHORT_DESC = "summary";
	public static final String PROP_PACKAGE_DESCRIPTION = "description";
	public static final String PROP_PACKAGE_FILE = "hasFile";
	public static final String PROP_PACKAGE_VERIFICATION_CODE = "packageVerificationCode";
	public static final String PROP_PACKAGE_LICENSE_INFO_FROM_FILES = "licenseInfoFromFiles";
	public static final String PROP_PACKAGE_LICENSE_COMMENT = "licenseComments";
	public static final String PROP_PACKAGE_VERSION_INFO = "versionInfo";
	public static final String PROP_PACKAGE_ORIGINATOR = "originator";
	public static final String PROP_PACKAGE_SUPPLIER = "supplier";
	
	// SPDX License Properties
	// The comment property is the RDFS_PROP_COMMENT property in the rdfs namespace
	// the seeAlso property is in the RDFS_PROP_SEE_ALSO property in the rdfs namespace
	public static final String PROP_LICENSE_ID = "licenseId";
	public static final String PROP_LICENSE_TEXT = "licenseText";
	public static final String PROP_EXTRACTED_TEXT = "extractedText";
	public static final String PROP_LICENSE_NAME = "licenseName";
	public static final String PROP_STD_LICENSE_NAME_VERSION_1 = "licenseName";	// old property name (pre 1.1 spec)
	public static final String PROP_STD_LICENSE_NAME = "name";
	public static final String PROP_STD_LICENSE_URL_VERSION_1 = "licenseSourceUrl";	// This has been replaced with the rdfs:seeAlso property
	public static final String PROP_STD_LICENSE_NOTES_VERSION_1 = "licenseNotes";	// old property name (pre 1.1 spec)
	public static final String PROP_STD_LICENSE_HEADER_VERSION_1 = "licenseHeader";	// old property name (pre 1.1 spec)
	public static final String PROP_STD_LICENSE_NOTICE = "standardLicenseHeader";	
	public static final String PROP_STD_LICENSE_TEMPLATE_VERSION_1 = "licenseTemplate";		// old property name (pre 1.2 spec)
	public static final String PROP_STD_LICENSE_TEMPLATE = "standardLicenseTemplate";
	public static final String PROP_STD_LICENSE_OSI_APPROVED = "isOsiApproved";
	public static final String PROP_STD_LICENSE_OSI_APPROVED_VERSION_1 = "licenseOsiApproved";	// old property name (pre 1.1 spec)
	public static final String PROP_LICENSE_SET_MEMEBER = "member";
	public static final String TERM_LICENSE_NOASSERTION = PROP_VALUE_NOASSERTION;
	public static final String TERM_LICENSE_NONE = PROP_VALUE_NONE;
	public static final String PROP_LICENSE_EXCEPTION_ID = "licenseExceptionId";
	public static final String PROP_EXAMPLE = "example";
	public static final String PROP_EXCEPTION_TEXT = "licenseExceptionText";
	public static final String PROP_LICENSE_EXCEPTION = "licenseException";
	
	// SpdxElement Properties
	public static final String PROP_NAME = "name";
	
	// SPDX File Properties
	// The comment property is the RDFS_PROP_COMMENT property in the rdfs namespace
	public static final String PROP_FILE_NAME = "fileName";
	public static final String PROP_FILE_TYPE = "fileType";
	public static final String PROP_FILE_LICENSE = PROP_LICENSE_CONCLUDED;
	public static final String PROP_FILE_COPYRIGHT = PROP_COPYRIGHT_TEXT;
	public static final String PROP_FILE_CHECKSUM = "checksum";
	public static final String PROP_FILE_SEEN_LICENSE = "licenseInfoInFile";	
	public static final String PROP_FILE_LIC_COMMENTS = PROP_LIC_COMMENTS;
	public static final String PROP_FILE_ARTIFACTOF = "artifactOf";
	public static final String PROP_FILE_FILE_DEPENDENCY = "fileDependency"; 
	public static final String PROP_FILE_CONTRIBUTOR = "fileContributor";
	public static final String PROP_FILE_NOTICE = "noticeText";
	
	// SPDX File Type Properties
	public static final String PROP_FILE_TYPE_SOURCE = "fileType_source";
	public static final String PROP_FILE_TYPE_ARCHIVE = "fileType_archive";
	public static final String PROP_FILE_TYPE_BINARY = "fileType_binary";
	public static final String PROP_FILE_TYPE_OTHER = "fileType_other";
	
	public static final String FILE_TYPE_SOURCE = "SOURCE";
	public static final String FILE_TYPE_ARCHIVE = "ARCHIVE";
	public static final String FILE_TYPE_BINARY = "BINARY";
	public static final String FILE_TYPE_OTHER = "OTHER";
	
	// SPDX Annotation Properties
	public static final String PROP_ANNOTATOR = "annotator";
	public static final String PROP_ANNOTATION_DATE = "annotationDate";
	public static final String PROP_ANNOTATION_TYPE = "annotationType";
	
	// SPDX Relationship Properties
	public static final String PROP_RELATED_SPDX_ELEMENT = "relatedSpdxElement";
	public static final String PROP_RELATIONSHIP_TYPE = "relationshipType";
	
	// ExternalDocumentRef properties
	public static final String PROP_EXTERNAL_DOC_CHECKSUM = "checksum";
	public static final String PROP_EXTERNAL_SPDX_DOCUMENT = "spdxDocument";
	public static final String PROP_EXTERNAL_DOC_ID = "externalDocumentId";
	
	// SPDX Review Properties
	// NOTE: These have all been deprecated as of SPDX 2.0
	// The comment property is the RDFS_PROP_COMMENT property in the rdfs namespace
	@Deprecated
	public static final String PROP_REVIEW_REVIEWER = "reviewer";
	@Deprecated
	public static final String PROP_REVIEW_DATE = "reviewDate";
	
	// Date format
	public static final String SPDX_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String STANDARD_LICENSE_URL = "http://spdx.org/licenses";
	
	// license ID format
	public static String NON_STD_LICENSE_ID_PRENUM = "LicenseRef-";
	public static Pattern LICENSE_ID_PATTERN_NUMERIC = 
			Pattern.compile(NON_STD_LICENSE_ID_PRENUM+"(\\d+)$");	// Pattern for numeric only license IDs
	static Pattern LICENSE_ID_PATTERN = Pattern.compile(NON_STD_LICENSE_ID_PRENUM+"([0-9a-zA-Z\\.\\-\\+]+)$");
	
	// SPDX Element Reference format
	public static String SPDX_ELEMENT_REF_PRENUM = "SPDXRef-";
	public static Pattern SPDX_ELEMENT_REF_PATTERN = Pattern.compile(SPDX_ELEMENT_REF_PRENUM+"([0-9a-zA-Z\\.\\-\\+]+)$");
	
	// External Document ID format
	public static String EXTERNAL_DOC_REF_PRENUM = "DocumentRef-";
	public static Pattern EXTERNAL_DOC_REF_PATTERN = Pattern.compile(EXTERNAL_DOC_REF_PRENUM+"([0-9a-zA-Z\\.\\-\\+]+)$");
	public static Pattern EXTERNAL_ELEMENT_REF_PATTERN = Pattern.compile("("+EXTERNAL_DOC_REF_PRENUM+"[0-9a-zA-Z\\.\\-\\+]+):("+SPDX_ELEMENT_REF_PRENUM+"[0-9a-zA-Z\\.\\-\\+]+)$");	
	
	public static Pattern SPDX_VERSION_PATTERN = Pattern.compile("^SPDX-(\\d+)\\.(\\d+)$");

	// Standard value strings
	public static String NONE_VALUE = "NONE";
	public static String NOASSERTION_VALUE = "NOASSERTION";
	
	// data license ID
	public static final String SPDX_DATA_LICENSE_ID_VERSION_1_0 = "PDDL-1.0";
	public static final String SPDX_DATA_LICENSE_ID = "CC0-1.0";
}