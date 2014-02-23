# Overview
The Software Package Data Exchange (SPDX) specification is a standard format for communicating the components, licenses and copyrights associated with a software package.

  * [SPDX License List](http://spdx.org/licenses/)
  * [SPDX Vocabulary Specification](http://spdx.org/rdf/terms)

These tools are published by the SPDX Workgroup
see [http://spdx.org/](http://spdx.org/)

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

  * CompareSpdxDocs

    Example to compare two SPDX files provided in rdf format:

        java -jar spdx-tools-jar-with-dependencies.jar CompareSpdxDocs doc1 doc2 [output]

  * CompareMultipleSpdxDocs

    Example to compare multiple SPDX files provided in rdf format and provide a spreadsheet with the results:

        java -jar spdx-tools-jar-with-dependencies.jar CompareMultipleSpdxDocs output.xls doc1 doc2 ... docN

## SPDX Viewer and PrettyPrinter
  * SPDXViewer
  * PrettyPrinter

Sample usage:

    java -jar spdx-tools-jar-with-dependencies.jar PrettyPrinter TestFiles/SPDXRdfExample.rdf

## Generators
  * LicenseRDFAGenerator

        java -jar spdx-tools-jar-with-dependencies.jar LicenseRDFAGenerator licenseSpreadsheet.xls outputDirectory [version] [releasedate]

  * GenerateVerificationCode sourceDirectory

        java -jar spdx-tools-jar-with-dependencies.jar GenerateVerificationCode sourceDirectory [ignoredFilesRegex]

# License
See the [NOTICE](NOTICE) file for licensing information
including info from 3rd Party Software

See [LICENSE](LICENSE) file for full license text

    SPDX-License-Identifier:	Apache-2.0
    PackageLicenseDeclared:		Apache-2.0

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

