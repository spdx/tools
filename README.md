[![Build Status](https://travis-ci.org/spdx/tools.svg?branch=master)](https://travis-ci.org/spdx/tools)

 [![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/734/badge)](https://bestpractices.coreinfrastructure.org/projects/734)

# Overview
The Software Package Data Exchange (SPDX) specification is a standard format for communicating the components, licenses and copyrights associated with a software package.

  * [SPDX License List](http://spdx.org/licenses/)
  * [SPDX Vocabulary Specification](http://spdx.org/rdf/terms)

These tools are published by the SPDX Workgroup
see [http://spdx.org/](http://spdx.org/)

See the [https://spdx.org/sites/cpstandard/files/pages/files/spdx_tools-20170327.pdf](SPDX Tools Documentation) for details on how to use the command line tools.

## Contributing
See the file CONTRIBUTING.md for information on making contributions to the SPDX tools.

## Issues
Report any security related issues by sending an email to [spdx-tools-security@lists.spdx.org](mailto:spdx-tools-security@lists.spdx.org)

Non-security related issues should be added to the [SPDX tools issues list](https://github.com/spdx/tools/issues)

## Syntax
The command line interface of the spdx tools can be used like this:

    java -jar spdx-tools-jar-with-dependencies.jar <function> <parameters> 

## SPDX format converters
The following converter tools are provided by the spdx tools:

  * TagToSpreadsheet
  * TagToRDF
  * RdfToTag
  * RdfToHtml
  * RdfToSpreadsheet
  * SpreadsheetToRDF
  * SpreadsheetToTag

Example to convert a SPDX file from tag to rdf format:

    java -jar spdx-tools-jar-with-dependencies.jar TagToRDF Examples/SPDXTagExample.tag TagToRDF.rdf

## Compare utilities
The following  tools can be used to compare one or more SPDX documents:

  * CompareSpdxDocs

    Example to compare two SPDX files provided in rdf format:

        java -jar spdx-tools-jar-with-dependencies.jar CompareSpdxDocs doc1 doc2 [output]

  * CompareMultipleSpdxDocs

    Example to compare multiple SPDX files provided in rdf format and provide a spreadsheet with the results:

        java -jar spdx-tools-jar-with-dependencies.jar CompareMultipleSpdxDocs output.xls doc1 doc2 ... docN

## SPDX Viewer
The following tool can be used to "Pretty Print" an SPDX document.

  * SPDXViewer

Sample usage:

    java -jar spdx-tools-jar-with-dependencies.jar SPDXViewer TestFiles/SPDXRdfExample.rdf

## Verifier
The following tool can be used to verify an SPDX document:

  * Verify

Sample usage:

    java -jar spdx-tools-jar-with-dependencies.jar Verify TestFiles/SPDXRdfExample.rdf

## Generators
The following tool can be used to generate an SPDX verification code from a directory of source files:

  * GenerateVerificationCode sourceDirectory
  
  Sample usage:

        java -jar spdx-tools-jar-with-dependencies.jar GenerateVerificationCode sourceDirectory [ignoredFilesRegex]

# License
See the [NOTICE](NOTICE) file for licensing information
including info from 3rd Party Software

See [LICENSE](LICENSE) file for full license text

    SPDX-License-Identifier:	Apache-2.0
    PackageLicenseDeclared:	Apache-2.0

# Development

## Build
You need [Apache Maven](http://maven.apache.org/) to build the project:

    mvn clean install

## Update tools data formats
To update SPDX tools, the following is a very brief checklist:

  1. Update the SpdxRdfContants with any new or changed RDF properties and classes
  2. Update the Java code representing the RDF model.
  3. Update the properties files in the org.spdx.tag package for any new tag values
  4. Update the org.spdx.tag.CommonCode.java for any new or changed tag values.  This will implement both the rdfToTag and the SPDXViewer applications.
  5. Update the org.spdx.tag.BuildDocument to implement changes for the TagToRdf application
  6. Update the HTML template (resources/htmlTemplate/SpdxHTMLTemplate.html) and contexts in org.spdx.html to implement changes for the SpdxToHtml application
  7. Update the related sheets and RdfToSpreadsheet.java file in the package org.spdx.spreadsheet
  8. Update the sheets and SpdxComparer/SpdxFileComparer in the org.spdx.compare package

## Upgrading to SPDX 2.0
To the users of the tools as a binary, there should not be any need to upgrade.  The tools should be backwards compatible with SPDX 1.0, 1.1, and 1.2.

If, however, you are using this Java code as a library for your own tools read on...

There are a number of changes to the design of the SPDX Parser both due to the extensive changes to the SPEC (e.g. support for multiple SPDX Packages within a document and support for relationships with external SPDX documents) and due to some much needed refactoring.

The starting point remains SPDXDocumentFactory.  To ease the migration, the old 1.2 code and model is still available and simply changing your code to call SPDXDcoumentFactory.createLegacySpdxDocument(...) will probably work.  You'll notice, however, almost everything your application is using is deprecated.  These will be removed once SPDX 2.0 has been released and people have a chance to migrate (likely around Jan 1 2016).

To move over to the new model, simply start with SPDXDocumentFactory and call the createSpdxDocument(...) method to create the new SpdxDocument model code.  
The object returned will be similar to the 1.2 version for SPDXDocument, but with a few key differences.
All new model objects are in the package org.spdx.rdfparser.mode.  The SPDX prefix is either removed or replaced with a more consistent Spdx.  

Accessing the model objects is similar to 1.2, simply call the get/set methods.  The method names have all been changed to be consistent with the specification property names.  As a convenience, many of the old getter method names are still there but deprecated.

The structure has changed with the SpdxPackage being a distinct class from SpdxDocument.  There is also a new class org.spdx.rdfparser.SpdxDocumentContainer which separates out the container functionality from the SpdxDocument leaving the SpdxDocument to represent the SpdxDocument properties.
There are several new classes which are consistent with the SPDX 2.0 Model.  See the JavaDocs and the SPDX 2.0 specification for a description of those classes and properties.

There is one significant class not found in the SPDX 2.0 model - ExternalSpdxElement.  This class represents elements not found within the SPDX Document.  The only valid property for this element is the ID (all other properties including the type are only known in the external document containing the element).
There is a more structured class hierarchy, mostly mirroring the SPDX 2.0 model.  As a user of the library, you likely do not need to understand these internals - but if you are interested, start at RdfModelObject and read the JavaDocs.

If you have any problems, and especially if you have any solutions, email the tech working group for SPDX at spdx-tech@lists.spdx.org.
