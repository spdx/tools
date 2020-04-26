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
package org.spdx.tag;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;
import org.spdx.rdfparser.model.DoapProject;
import org.spdx.rdfparser.model.ExternalRef;
import org.spdx.rdfparser.model.ExternalRef.ReferenceCategory;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxFile.FileType;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.rdfparser.model.SpdxSnippet;
import org.spdx.rdfparser.model.UnitTestHelper;
import org.spdx.rdfparser.model.pointer.ByteOffsetPointer;
import org.spdx.rdfparser.model.pointer.LineCharPointer;
import org.spdx.rdfparser.model.pointer.StartEndPointer;
import org.spdx.rdfparser.referencetype.ListedReferenceTypes;
import org.spdx.rdfparser.referencetype.ReferenceType;

import com.google.common.collect.Lists;

/**
 * @author Gary O'Neall
 *
 */
public class TestBuildDocument {
	
	// Document level
	static final String SPDX_VERSION = "SPDX-2.1";
	static final String SPDX_VERSION_TAG = "SPDXVersion: " + SPDX_VERSION;
	static final String DOC_NAMESPACE = "http://spdx.org/spdxdocs/spdx-example-444504E0-4F89-41D3-9A0C-0305E82C3301";
	static final String DOC_NAMESPACE_TAG = "DocumentNamespace: " + DOC_NAMESPACE;
	static final String DOC_NAME = "SPDX-Tools-v2.1";
	static final String DOC_NAME_TAG = "DocumentName: " + DOC_NAME;
	static final String DOC_SPDXID = "SPDXRef-DOCUMENT";
	static final String DOC_SPDXID_TAG = "SPDXID: " + DOC_SPDXID;
	static final String DOC_COMMENT = "Test document\ncomment";
	static final String DOC_COMMENT_TAG = "DocumentComment: <text>" + DOC_COMMENT + "</text>";
	static final String DOC_LEVEL_TAGS = SPDX_VERSION_TAG + "\n" + 
			DOC_NAMESPACE_TAG + "\n" + DOC_NAME_TAG + "\n" + DOC_SPDXID_TAG + "\n" +
			DOC_COMMENT_TAG;
	
	// External document references
	static final String EXTERNAL_DOC_REF_NAME = "DocumentRef-spdx-tool-1.2";
	static final String EXTERNAL_DOC_REF_URI = "http://spdx.org/spdxdocs/spdx-tools-v1.2-3F2504E0-4F89-41D3-9A0C-0305E82C3301";
	static final String EXTERNAL_DOC_REF_SHA1 = "d6a770ba38583ed4bb4525bd96e50461655d2759";
	static final String EXTERNAL_DOC_REF_TAG = "ExternalDocumentRef: " + EXTERNAL_DOC_REF_NAME +
			" " + EXTERNAL_DOC_REF_URI + " SHA1: " + EXTERNAL_DOC_REF_SHA1;
	
	// Creator
	static final String CREATOR_TOOL = "Tool: LicenseFind-1.0";
	static final String CREATOR_TOOL_TAG = "Creator: " + CREATOR_TOOL;
	static final String CREATOR_ORGANIZATION = "Organization: ExampleCodeInspect ()";
	static final String CREATOR_ORGANIZATION_TAG = "Creator: " + CREATOR_ORGANIZATION;
	static final String CREATOR_PERSON = "Person: Jane Doe ()";
	static final String CREATOR_PERSON_TAG = "Creator: " + CREATOR_PERSON;
	static final String CREATED = "2010-01-29T18:30:22Z";
	static final String CREATED_TAG = "Created: " + CREATED;
	static final String CREATOR_COMMENT = "This package has been shipped in source and binary form.\n" +
			"The binaries were created with gcc 4.5.1 and expect to link to\n" +
			"compatible system run time libraries.";
	static final String CREATOR_COMMENT_TAG = "CreatorComment: <text>" + CREATOR_COMMENT + "</text>";
	static final String CREATOR_TAGS = CREATOR_TOOL_TAG + "\n" + CREATOR_ORGANIZATION_TAG + "\n" + 
			CREATOR_PERSON_TAG + "\n" + CREATED_TAG + "\n" + CREATOR_COMMENT;
	
	// Document Annotation
	static final String DOC_ANNOTATOR = "Person: Jane Doe ()";
	static final String DOC_ANNOTATOR_TAG = "Annotator: " + DOC_ANNOTATOR;
	static final String DOC_ANNOTATION_DATE = "2010-01-29T18:30:22Z";
	static final String DOC_ANNOTATION_DATE_TAG = "AnnotationDate: " + DOC_ANNOTATION_DATE;
	static final String DOC_ANNOTATION_COMMENT = "Document level annotation";
	static final String DOC_ANNOTATION_COMMENT_TAG = "AnnotationComment: <text>" + DOC_ANNOTATION_COMMENT + "</text>";
	static final String DOC_ANNOTATION_TYPE = "OTHER";
	static final String DOC_ANNOTATION_TYPE_TAG = "AnnotationType: " + DOC_ANNOTATION_TYPE;
	static final String DOC_ANNOTATION_SPDXID_TAG = "SPDXREF: " + DOC_SPDXID;
	static final String DOC_ANNOTATION_TAGS = DOC_ANNOTATOR_TAG + "\n" + DOC_ANNOTATION_DATE_TAG + "\n" + 
			DOC_ANNOTATION_COMMENT_TAG + "\n" + DOC_ANNOTATION_TYPE_TAG + "\n" + 
			DOC_ANNOTATION_SPDXID_TAG;
	
	// Document Relationships
	static final String COPY_OF_RELATIONSHIP_TYPE = "COPY_OF";
	static final String COPY_OF_RELATIONSHIP = "DocumentRef-spdx-tool-1.2:SPDXRef-ToolsElement";
	static final String COPY_OF_RELATIONSHIP_TAG = "Relationship: " + DOC_SPDXID + " " + COPY_OF_RELATIONSHIP_TYPE + " " + COPY_OF_RELATIONSHIP;
	static final String CONTAINS_RELATIONSHIP_TYPE = "CONTAINS";
	static final String PACKAGE_SPDXID = "SPDXRef-Package";
	static final String CONTAINS_RELATIONSHIP = PACKAGE_SPDXID;
	static final String CONTAINS_RELATIONSHIP_TAG = "Relationship: " + DOC_SPDXID + " " + CONTAINS_RELATIONSHIP_TYPE + " " + CONTAINS_RELATIONSHIP;
	static final String DESCRIBES_RELATIONSHIP_TYPE = "DESCRIBES";
	static final String DESCRIBES_RELATIONSHIP = PACKAGE_SPDXID;
	static final String DESCRIBES_RELATIONSHIP_TAG = "Relationship: " + DOC_SPDXID + " " + DESCRIBES_RELATIONSHIP_TYPE + " " + DESCRIBES_RELATIONSHIP;
	static final String DOC_RELATIONSHIP_TAGS = COPY_OF_RELATIONSHIP_TAG + "\n" + CONTAINS_RELATIONSHIP_TAG + "\n" + DESCRIBES_RELATIONSHIP_TAG;
	
	// Package
	static final String PACKAGE_NAME = "glibc";
	static final String PACKAGE_NAME_TAG = "PackageName: " + PACKAGE_NAME;
	static final String PACKAGE_SPDXID_TAG = "SPDXID: " + PACKAGE_SPDXID;
	static final String PACKAGE_VERSION = "2.11.1";
	static final String PACKAGE_VERSION_TAG = "PackageVersion: " + PACKAGE_VERSION;
	static final String PACKAGE_FILENAME = "glibc-2.11.1.tar.gz";
	static final String PACKAGE_FILENAME_TAG = "PackageFileName: ";
	static final String PACKAGE_SUPPLIER = "Person: Jane Doe (jane.doe@example.com)";
	static final String PACKAGE_SUPPLIER_TAG = "PackageSupplier: " + PACKAGE_SUPPLIER;
	static final String PACKAGE_ORIGINATOR = "Organization: ExampleCodeInspect (contact@example.com)";
	static final String PACKAGE_ORIGINATOR_TAG = "PackageOriginator: " + PACKAGE_ORIGINATOR;
	static final String PACKAGE_DOWNLOAD_LOCATION = "http://ftp.gnu.org/gnu/glibc/glibc-ports-2.15.tar.gz";
	static final String PACKAGE_DOWNLOAD_LOCATION_TAG = "PackageDownloadLocation: " + PACKAGE_DOWNLOAD_LOCATION;
	static final String PACKAGE_VERIFICATION_CODE = "d6a770ba38583ed4bb4525bd96e50461655d2758";
	static final String PACKAGE_VERIFICATION_EXCLUDES = "(excludes: ./package.spdx)";
	static final String PACKAGE_VERIFICATION_TAG = "PackageVerificationCode: " + PACKAGE_VERIFICATION_CODE + " " + 
			PACKAGE_VERIFICATION_EXCLUDES;
	static final String PACKAGE_CHECKSUM_SHA1 = "85ed0817af83a24ad8da68c2b5094de69833983c";
	static final String PACKAGE_CHECKSUM_SHA1_TAG = "PackageChecksum: SHA1: " + PACKAGE_CHECKSUM_SHA1;
	static final String PACKAGE_CHECKSUM_SHA256 = "11b6d3ee554eedf79299905a98f9b9a04e498210b59f15094c916c91d150efcd";
	static final String PACKAGE_CHECKSUM_SHA256_TAG = "PackageChecksum: SHA256: " + PACKAGE_CHECKSUM_SHA256;
	static final String PACKAGE_CHECKSUM_MD5 = "624c1abb3664f4b35547e7c73864ad24";
	static final String PACKAGE_CHECKSUM_MD5_TAG = "PackageChecksum: MD5: " + PACKAGE_CHECKSUM_MD5;
	static final String PACKAGE_HOME_PAGE = "http://ftp.gnu.org/gnu/glibc";
	static final String PACKAGE_HOME_PAGE_TAG = "PackageHomePage: " + PACKAGE_HOME_PAGE;
	static final String PACKAGE_SOURCE_INFO = "uses glibc-2_11-branch from git://sourceware.org/git/glibc.git.";
	static final String PACKAGE_SOURCE_INFO_TAG = "PackageSourceInfo: <text>" + PACKAGE_SOURCE_INFO + "</text>";	
	static final String LICENSE_REF1 = "LicenseRef-1";
	static final String PACKAGE_LICENSE_CONCLUDED = "(" + LICENSE_REF1 + " OR LGPL-2.0-only)";
	static final String PACKAGE_LICENSE_CONCLUDED_TAG = "PackageLicenseConcluded: " + PACKAGE_LICENSE_CONCLUDED;
	static final String PACKAGE_LICENSE_INFO_FROM_FILES1 = "GPL-2.0-only";
	static final String PACKAGE_LICENSE_INFO_FROM_FILES1_TAG = "PackageLicenseInfoFromFiles: " + PACKAGE_LICENSE_INFO_FROM_FILES1;
	static final String PACKAGE_LICENSE_INFO_FROM_FILES2 = LICENSE_REF1;
	static final String PACKAGE_LICENSE_INFO_FROM_FILES2_TAG = "PackageLicenseInfoFromFiles: " + PACKAGE_LICENSE_INFO_FROM_FILES2;
	static final String LICENSE_REF2 = "LicenseRef-2";
	static final String PACKAGE_LICENSE_INFO_FROM_FILES3 = LICENSE_REF2;
	static final String PACKAGE_LICENSE_INFO_FROM_FILES3_TAG = "PackageLicenseInfoFromFiles: " + PACKAGE_LICENSE_INFO_FROM_FILES3;
	static final String PACKAGE_LICENSE_INFO_FROM_FILES_TAGS = PACKAGE_LICENSE_INFO_FROM_FILES1_TAG + "\n" + 
			PACKAGE_LICENSE_INFO_FROM_FILES2_TAG + "\n" + PACKAGE_LICENSE_INFO_FROM_FILES3_TAG;
	static final String PACKAGE_LICENSE_DECLARED = "(" + LICENSE_REF2 + " AND LGPL-2.0-only)";
	static final String PACKAGE_LICENSE_DECLARED_TAG = "PackageLicenseDeclared: " + PACKAGE_LICENSE_DECLARED;
	static final String PACKAGE_LICENSE_COMMENT = "The license for this project changed with the release of version x.y.\n" +
			"The version of the project included here post-dates the license change.";
	static final String PACKAGE_LICENSE_COMMENT_TAG = "PackageLicenseComments: <text>" + PACKAGE_LICENSE_COMMENT + "</text>";
	static final String PACKAGE_COPYRIGHT = "Copyright 2008-2010 John Smith";
	static final String PACKAGE_COPYRIGHT_TAG = "PackageCopyrightText: <text>" + PACKAGE_COPYRIGHT + "</text>";
	static final String PACKAGE_SUMMARY = "GNU C library.";
	static final String PACKAGE_SUMMARY_TAG = "PackageSummary: <text>" + PACKAGE_SUMMARY + "</text>";
	static final String PACKAGE_DESCRIPTION = "The GNU C Library defines functions that are \n" +
			"specified by the ISO C standard, as well as additional features specific to \n" +
			"POSIX and other derivatives of the Unix operating system,\n" +
			" and extensions specific to GNU systems.";
	static final String PACKAGE_DESCRIPTION_TAG = "PackageDescription: <text>" + PACKAGE_DESCRIPTION + "</text>";
	static final String EXTERNAL_REF_SECURITY_CATEGORY = "SECURITY";
	static final String EXTERNAL_REF_SECURITY_TYPE = "cpe23Type";
	static final String EXTERNAL_REF_SECURITY_LOCATOR = "cpe:2.3:a:pivotal_software:spring_framework:4.1.0:*:*:*:*:*:*:*";
	static final String PACKAGE_EXTERNAL_REF_SECURITY_TAG = "ExternalRef: " + EXTERNAL_REF_SECURITY_CATEGORY + " " + 
			EXTERNAL_REF_SECURITY_TYPE + " " + EXTERNAL_REF_SECURITY_LOCATOR;
		static final String PACKAGE_EXTERNAL_REF_SECURITY_COMMENT = "external ref comment for security";
	static final String PACKAGE_EXTERNAL_REF_SECURITY_COMMENT_TAG = "ExternalRefComment: <text>" + 
		PACKAGE_EXTERNAL_REF_SECURITY_COMMENT + "</text>";
	static final String PACKAGE_EXTERNAL_REF_OTHER_CATEGORY = "OTHER";
	static final String PACKAGE_EXTERNAL_REF_OTHER_TYPE = "LocationRefacmeforge";
	static final String PACKAGE_EXTERNAL_REF_OTHER_LOCATOR = "acmecorp/acmenator/4.1.3alpha";
	
	static final String PACKAGE_EXTERNAL_REF_OTHER_TAG = "ExternalRef: " + PACKAGE_EXTERNAL_REF_OTHER_CATEGORY +
			" " + PACKAGE_EXTERNAL_REF_OTHER_TYPE + " " + PACKAGE_EXTERNAL_REF_OTHER_LOCATOR;
	static final String PACKAGE_TAGS = PACKAGE_NAME_TAG + "\n" + PACKAGE_SPDXID_TAG + "\n" + 
			PACKAGE_VERSION_TAG + "\n" + PACKAGE_FILENAME_TAG + "\n" + 
			PACKAGE_SUPPLIER_TAG + "\n" + PACKAGE_ORIGINATOR_TAG + "\n" + 
			PACKAGE_DOWNLOAD_LOCATION_TAG + "\n" + PACKAGE_VERIFICATION_TAG + "\n" + 
			PACKAGE_CHECKSUM_SHA1_TAG + "\n" + PACKAGE_CHECKSUM_SHA256_TAG + "\n" + 
			PACKAGE_CHECKSUM_MD5_TAG + "\n" + PACKAGE_HOME_PAGE_TAG + "\n" + 
			PACKAGE_SOURCE_INFO_TAG + "\n" + PACKAGE_LICENSE_CONCLUDED_TAG + "\n" + 
			PACKAGE_LICENSE_INFO_FROM_FILES_TAGS + "\n" + PACKAGE_LICENSE_DECLARED_TAG + "\n" + 
			PACKAGE_LICENSE_COMMENT_TAG + "\n" + PACKAGE_COPYRIGHT_TAG + "\n" + 
			PACKAGE_SUMMARY_TAG + "\n" + PACKAGE_DESCRIPTION_TAG + "\n" +
			PACKAGE_EXTERNAL_REF_SECURITY_TAG + "\n" + PACKAGE_EXTERNAL_REF_SECURITY_COMMENT_TAG + "\n" +
			PACKAGE_EXTERNAL_REF_OTHER_TAG;
	static final String PACKAGE_NO_FILES_TAG = "FilesAnalyzed: false";
	static final String PACKAGE_TAGS_NO_FILES = PACKAGE_NAME_TAG + "\n" + PACKAGE_SPDXID_TAG + "\n" + 
			PACKAGE_VERSION_TAG + "\n" + PACKAGE_FILENAME_TAG + "\n" + 
			PACKAGE_SUPPLIER_TAG + "\n" + PACKAGE_ORIGINATOR_TAG + "\n" + 
			PACKAGE_DOWNLOAD_LOCATION_TAG + "\n" + 
			PACKAGE_CHECKSUM_SHA1_TAG + "\n" + PACKAGE_CHECKSUM_SHA256_TAG + "\n" + 
			PACKAGE_CHECKSUM_MD5_TAG + "\n" + PACKAGE_HOME_PAGE_TAG + "\n" + 
			PACKAGE_SOURCE_INFO_TAG + "\n" + PACKAGE_LICENSE_CONCLUDED_TAG + "\n" + 
			PACKAGE_LICENSE_INFO_FROM_FILES_TAGS + "\n" + PACKAGE_LICENSE_DECLARED_TAG + "\n" + 
			PACKAGE_LICENSE_COMMENT_TAG + "\n" + PACKAGE_COPYRIGHT_TAG + "\n" + 
			PACKAGE_SUMMARY_TAG + "\n" + PACKAGE_DESCRIPTION_TAG + "\n" +
			PACKAGE_EXTERNAL_REF_SECURITY_TAG + "\n" + PACKAGE_EXTERNAL_REF_SECURITY_COMMENT_TAG + "\n" +
			PACKAGE_EXTERNAL_REF_OTHER_TAG + "\n" + PACKAGE_NO_FILES_TAG;
	
	// File lib-source
	static final String FILE_LIB_FILENAME = "./lib-source/commons-lang3-3.1-sources.jar";
	static final String FILE_LIB_FILENAME_TAG = "FileName: " + FILE_LIB_FILENAME;
	static final String FILE_LIB_SPDXID = "SPDXRef-CommonsLangSrc";
	static final String FILE_LIB_SPDXID_TAG = "SPDXID: " + FILE_LIB_SPDXID;
	static final String FILE_LIB_COMMENT = "This file is used by Jena";
	static final String FILE_LIB_COMMENT_TAG = "FileComment: <text>" + FILE_LIB_COMMENT + "</text>";
	static final String FILE_LIB_FILE_TYPE = "ARCHIVE";
	static final String FILE_LIB_FILE_TYPE_TAG = "FileType: " + FILE_LIB_FILE_TYPE;
	static final String FILE_LIB_CHECKSUM = "c2b4e1c67a2d28fced849ee1bb76e7391b93f125";
	static final String FILE_LIB_CHECKSUM_TAG = "FileChecksum: SHA1: " + FILE_LIB_CHECKSUM;
	static final String FILE_LIB_LICENSE_CONCLUDED = "Apache-2.0";
	static final String FILE_LIB_LICENSE_CONCLUDED_TAG = "LicenseConcluded: " + FILE_LIB_LICENSE_CONCLUDED;
	static final String FILE_LIB_LICENSE_INFO = "Apache-1.1";
	static final String FILE_LIB_LICENSE_INFO_TAG = "LicenseInfoInFile: " + FILE_LIB_LICENSE_INFO;
	static final String FILE_LIB_LICENSE_COMMENT = "License comment for file lib";
	static final String FILE_LIB_LICENSE_COMMENT_TAG = "LicenseComments: " + FILE_LIB_LICENSE_COMMENT;
	static final String FILE_LIB_COPYRIGHT = "Copyright 2001-2011 The Apache Software Foundation";
	static final String FILE_LIB_COPYRIGHT_TAG = "FileCopyrightText: <text>" + FILE_LIB_COPYRIGHT + "</text>";
	static final String FILE_LIB_NOTICE = "Apache Commons Lang\n" + 
			"Copyright 2001-2011 The Apache Software Foundation";
	static final String FILE_LIB_NOTICE_TAG = "FileNotice: <text>" + FILE_LIB_NOTICE + "</text>";
	static final String FILE_LIB_TAGS = FILE_LIB_FILENAME_TAG + "\n" + FILE_LIB_SPDXID_TAG + "\n" + 
			FILE_LIB_COMMENT_TAG + "\n" + FILE_LIB_FILE_TYPE_TAG + "\n" + FILE_LIB_CHECKSUM_TAG + "\n" + 
			FILE_LIB_LICENSE_CONCLUDED_TAG + "\n" + FILE_LIB_LICENSE_INFO_TAG + "\n" + 
			FILE_LIB_LICENSE_COMMENT_TAG + "\n" + FILE_LIB_COPYRIGHT_TAG + "\n" + FILE_LIB_NOTICE_TAG;
	
	// Snippet libSnippet
	static final String SNIPPET_LIB_ID = "SPDXRef-Snippet";
	static final String SNIPPET_LIB_ID_TAG = "SnippetSPDXID: ";
	static final String SNIPPET_LIB_FROM_FILE_TAG = "SnippetFromFileSPDXID: " + FILE_LIB_SPDXID;
	static final int SNIPPET_LIB_BYTE_START = 310;
	static final int SNIPPET_LIB_BYTE_END = 420;
	static final String SNIPPET_LIB_BYTE_RANGE_TAG = "SnippetByteRange: " + String.valueOf(SNIPPET_LIB_BYTE_START) + ":" + 
			String.valueOf(SNIPPET_LIB_BYTE_END);
	static final int SNIPPET_LIB_LINE_START = 5;
	static final int SNIPPET_LIB_LINE_END = 23;
	static final String SNIPPET_LIB_LINE_RANGE_TAG = "SnippetLineRange: " + String.valueOf(SNIPPET_LIB_LINE_START) + ":" + 
			String.valueOf(SNIPPET_LIB_LINE_END);
	static final String SNIPPET_LIB_CONCLUDED_LICENSE = "GPL-2.0-only";
	static final String SNIPPET_LIB_CONCLUDED_LICENSE_TAG = "SnippetLicenseConcluded: " +
			SNIPPET_LIB_CONCLUDED_LICENSE;
	static final String SNIPPET_LIB_LICENSE_COMMENT = "Comment on snippet license";
	static final String SNIPPET_LIB_LICENSE_COMMENT_TAG = "SnippetLicenseComments: " + 
			SNIPPET_LIB_LICENSE_COMMENT;
	static final String SNIPPET_LIB_LICENSE_INFO_IN_SNIPPET = "LGPL-2.0-only";
	static final String SNIPPET_LIB_LICENSE_INFO_IN_SNIPPET_TAG = "LicenseInfoInSnippet: " +
			SNIPPET_LIB_LICENSE_INFO_IN_SNIPPET;
	static final String SNIPPET_LIB_COPYRIGHT = "Copyright 20082010 John Smith";
	static final String SNIPPET_LIB_COPYRIGHT_TAG = "SnippetCopyrightText: " + SNIPPET_LIB_COPYRIGHT;
	static final String SNIPPET_LIB_COMMENT = "This snippet was identified as significant and highlighted in this Apache2.0\n" +
			"file, when a commercial scanner identified it as being derived from file foo.c in package xyz which is\n" +
			"licensed under GPL2.0.";
	static final String SNIPPET_LIB_COMMENT_TAG = "SnippetComment: <text>" + SNIPPET_LIB_COMMENT + "</text>";
	static final String SNIPPET_LIB_NAME = "from linux kernel";
	static final String SNIPPET_LIB_NAME_TAG = "SnippetName: " + SNIPPET_LIB_NAME;
	static final String SNIPPET_LIB_TAGS = SNIPPET_LIB_ID_TAG + SNIPPET_LIB_ID + "\n" + SNIPPET_LIB_FROM_FILE_TAG + "\n" +
			SNIPPET_LIB_BYTE_RANGE_TAG + "\n" + SNIPPET_LIB_LINE_RANGE_TAG + "\n" + SNIPPET_LIB_CONCLUDED_LICENSE_TAG + "\n" +
			SNIPPET_LIB_LICENSE_COMMENT_TAG + "\n" + SNIPPET_LIB_LICENSE_INFO_IN_SNIPPET_TAG + "\n" +
			SNIPPET_LIB_COPYRIGHT_TAG + "\n" + SNIPPET_LIB_COMMENT_TAG + "\n" + SNIPPET_LIB_NAME_TAG;
	
	// License Ref 1
	static final String LICENSE_REF1_LICENSEID_TAG = "LicenseID: " + LICENSE_REF1;
	static final String LICENSE_REF1_EXTRACTED_TEXT = "/*\n" +
			"* (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP\n" +
			"* All rights reserved.";
	static final String LICENSE_REF1_EXTRACTED_TEXT_TAG = "ExtractedText: <text>" + LICENSE_REF1_EXTRACTED_TEXT + "</text>";
	static final String LICENSE_REF1_TAGS = LICENSE_REF1_LICENSEID_TAG + "\n" + LICENSE_REF1_EXTRACTED_TEXT_TAG;
	
	// License Ref 2
		static final String LICENSE_REF2_LICENSEID_TAG = "LicenseID: " + LICENSE_REF2;
		static final String LICENSE_REF2_EXTRACTED_TEXT = "/*This package includes the GRDDL parser developed by Hewlett Pa";
		static final String LICENSE_REF2_EXTRACTED_TEXT_TAG = "ExtractedText: <text>" + LICENSE_REF2_EXTRACTED_TEXT + "</text>";
		static final String LICENSE_REF2_TAGS = LICENSE_REF2_LICENSEID_TAG + "\n" + LICENSE_REF2_EXTRACTED_TEXT_TAG;

	String SIMPLE_TAGDOCUMENT = DOC_LEVEL_TAGS + "\n" + EXTERNAL_DOC_REF_TAG + "\n" + 
			CREATOR_TAGS + "\n" + DOC_ANNOTATION_TAGS + "\n" + DOC_RELATIONSHIP_TAGS + "\n" + 
			PACKAGE_TAGS + "\n" + FILE_LIB_TAGS + "\n" + SNIPPET_LIB_TAGS + "\n" +
			LICENSE_REF1_TAGS + "\n" + LICENSE_REF2_TAGS;
	
	String TAGDOCUMENT_NO_FILES = DOC_LEVEL_TAGS + "\n" + EXTERNAL_DOC_REF_TAG + "\n" + 
			CREATOR_TAGS + "\n" + DOC_ANNOTATION_TAGS + "\n" + DOC_RELATIONSHIP_TAGS + "\n" + 
			PACKAGE_TAGS_NO_FILES + "\n" + LICENSE_REF1_TAGS + "\n" + 
			LICENSE_REF2_TAGS;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBuildSimpleDocument() throws Exception {
		InputStream bais = new ByteArrayInputStream(SIMPLE_TAGDOCUMENT.getBytes());
		HandBuiltParser parser = new HandBuiltParser(new NoCommentInputStream(bais));
		List<String> warnings = Lists.newArrayList();
		Properties constants = CommonCode.getTextFromProperties("org/spdx/tag/SpdxTagValueConstants.properties");
		SpdxDocumentContainer[] result = new SpdxDocumentContainer[1];
		parser.setBehavior(new BuildDocument(result, constants, warnings));
		parser.data();
		assertEquals(0, warnings.size());
		assertEquals(0, result[0].getSpdxDocument().verify().size());
	}
	
	@Test public void testExternalRefs() throws Exception {
		InputStream bais = new ByteArrayInputStream(SIMPLE_TAGDOCUMENT.getBytes());
		HandBuiltParser parser = new HandBuiltParser(new NoCommentInputStream(bais));
		List<String> warnings = Lists.newArrayList();
		Properties constants = CommonCode.getTextFromProperties("org/spdx/tag/SpdxTagValueConstants.properties");
		SpdxDocumentContainer[] result = new SpdxDocumentContainer[1];
		parser.setBehavior(new BuildDocument(result, constants, warnings));
		parser.data();
		SpdxDocument doc = result[0].getSpdxDocument();
		SpdxPackage pkg = (SpdxPackage)doc.getDocumentDescribes()[0];
		ExternalRef[] refs = pkg.getExternalRefs();
		assertEquals(2, refs.length);
		ExternalRef securityRef = new ExternalRef(ReferenceCategory.fromTag(EXTERNAL_REF_SECURITY_CATEGORY),
				new ReferenceType(ListedReferenceTypes.getListedReferenceTypes().getListedReferenceUri(EXTERNAL_REF_SECURITY_TYPE), null, null, null),
				EXTERNAL_REF_SECURITY_LOCATOR, PACKAGE_EXTERNAL_REF_SECURITY_COMMENT);
		ExternalRef otherRef = new ExternalRef(ReferenceCategory.fromTag(PACKAGE_EXTERNAL_REF_OTHER_CATEGORY),
				new ReferenceType(new URI(DOC_NAMESPACE + "#" + PACKAGE_EXTERNAL_REF_OTHER_TYPE), null, null, null),
				PACKAGE_EXTERNAL_REF_OTHER_LOCATOR, null);
		ExternalRef[] expected = new ExternalRef[] {securityRef, otherRef};
		assertTrue(UnitTestHelper.isArraysEquivalent(expected, refs));
	}
	
	@Test 
	public void testNoFilesAnalyzedFiles()  throws Exception {
		InputStream bais = new ByteArrayInputStream(TAGDOCUMENT_NO_FILES.getBytes());
		HandBuiltParser parser = new HandBuiltParser(new NoCommentInputStream(bais));
		List<String> warnings = Lists.newArrayList();
		Properties constants = CommonCode.getTextFromProperties("org/spdx/tag/SpdxTagValueConstants.properties");
		SpdxDocumentContainer[] result = new SpdxDocumentContainer[1];
		parser.setBehavior(new BuildDocument(result, constants, warnings));
		parser.data();
		assertEquals(0, warnings.size());
		assertEquals(0, result[0].getSpdxDocument().verify().size());
		SpdxDocument doc = result[0].getSpdxDocument();
		SpdxPackage pkg = (SpdxPackage)doc.getDocumentDescribes()[0];
		assertFalse(pkg.isFilesAnalyzed());
	}
	
	@Test 
	public void testFile()  throws Exception {
		InputStream bais = new ByteArrayInputStream(SIMPLE_TAGDOCUMENT.getBytes());
		HandBuiltParser parser = new HandBuiltParser(new NoCommentInputStream(bais));
		List<String> warnings = Lists.newArrayList();
		Properties constants = CommonCode.getTextFromProperties("org/spdx/tag/SpdxTagValueConstants.properties");
		SpdxDocumentContainer[] result = new SpdxDocumentContainer[1];
		parser.setBehavior(new BuildDocument(result, constants, warnings));
		parser.data();
		List<SpdxFile> files = result[0].findAllFiles();
		assertEquals(1, files.size());
		SpdxFile expected = new SpdxFile(FILE_LIB_FILENAME, FILE_LIB_COMMENT, 
				new Annotation[0], new Relationship[0], LicenseInfoFactory.parseSPDXLicenseString(FILE_LIB_LICENSE_CONCLUDED, result[0]),
				new AnyLicenseInfo[] {LicenseInfoFactory.parseSPDXLicenseString(FILE_LIB_LICENSE_INFO, result[0])},
				FILE_LIB_COPYRIGHT, FILE_LIB_LICENSE_COMMENT, 
				new FileType[] {FileType.fromTag(FILE_LIB_FILE_TYPE)}, 
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, FILE_LIB_CHECKSUM)},
				new String[0], FILE_LIB_NOTICE, new DoapProject[0]);
		assertTrue(expected.equivalent(files.get(0)));
	}
	
	@Test 
	public void testSnippet()  throws Exception {
		
		InputStream bais = new ByteArrayInputStream(SIMPLE_TAGDOCUMENT.getBytes());
		HandBuiltParser parser = new HandBuiltParser(new NoCommentInputStream(bais));
		List<String> warnings = Lists.newArrayList();
		Properties constants = CommonCode.getTextFromProperties("org/spdx/tag/SpdxTagValueConstants.properties");
		SpdxDocumentContainer[] result = new SpdxDocumentContainer[1];
		parser.setBehavior(new BuildDocument(result, constants, warnings));
		parser.data();
		List<SpdxSnippet> snippets = result[0].findAllSnippets();
		assertEquals(1, snippets.size());
		SpdxFile snippetFromFile = new SpdxFile(FILE_LIB_FILENAME, FILE_LIB_COMMENT, 
				new Annotation[0], new Relationship[0], LicenseInfoFactory.parseSPDXLicenseString(FILE_LIB_LICENSE_CONCLUDED, result[0]),
				new AnyLicenseInfo[] {LicenseInfoFactory.parseSPDXLicenseString(FILE_LIB_LICENSE_INFO, result[0])},
				FILE_LIB_COPYRIGHT, FILE_LIB_LICENSE_COMMENT, 
				new FileType[] {FileType.fromTag(FILE_LIB_FILE_TYPE)}, 
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, FILE_LIB_CHECKSUM)},
				new String[0], FILE_LIB_NOTICE, new DoapProject[0]);
		StartEndPointer byteRange = new StartEndPointer(new ByteOffsetPointer(snippetFromFile, SNIPPET_LIB_BYTE_START), 
					new ByteOffsetPointer(snippetFromFile, SNIPPET_LIB_BYTE_END));
		StartEndPointer lineRange = new StartEndPointer(new LineCharPointer(snippetFromFile, SNIPPET_LIB_LINE_START), 
				new LineCharPointer(snippetFromFile, SNIPPET_LIB_LINE_END));
		SpdxSnippet expected = new SpdxSnippet(SNIPPET_LIB_NAME, SNIPPET_LIB_COMMENT, 
				new Annotation[0], new Relationship[0], LicenseInfoFactory.parseSPDXLicenseString(SNIPPET_LIB_CONCLUDED_LICENSE, result[0]),
				new AnyLicenseInfo[] {LicenseInfoFactory.parseSPDXLicenseString(SNIPPET_LIB_LICENSE_INFO_IN_SNIPPET, result[0])},
				SNIPPET_LIB_COPYRIGHT, SNIPPET_LIB_LICENSE_COMMENT, snippetFromFile, byteRange,
				lineRange);
		assertTrue(expected.equivalent(snippets.get(0)));
	}

}
