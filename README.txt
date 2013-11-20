See the SPDX Tools documentation located at http://spdx.org/tools for usage information.

Usage
=====
java -jar spdx-tools-jar-with-dependencies.jar <function> <parameters> 

function                parameter                         example 
----------------------------------------------------------------- 
TagToSpreadsheet        inputFile outputFile              Examples/SPDXTagExample.tag TagToSpreadsheet.xls 
TagToRDF                inputFile outputFile              Examples/SPDXTagExample.tag TagToRDF.rdf 
RdfToTag                inputFile outputFile              TestFiles/SPDXRdfExample.rdf  RdfToTag.tag 
RdfToHtml               inputFile outputFile              TestFiles/SPDXRdfExample.rdf  RdfToHtml.html 
SPDXViewer              inputFile                         TestFiles/SPDXRdfExample.rdf 
PrettyPrinter           inputFile                         TestFiles/SPDXRdfExample.rdf 
CompareMultipleSpdxDocs output.xls doc1 doc2 ... docN 
CompareSpdxDocs         doc1 doc2 [output] 
LicenseRDFAGenerator    licenseSpreadsheet.xls outputDirectory [version] [releasedate] 

License
=========================
See the NOTICE file for licensing information
including info from 3rd Party Software

see LICENSE file for full license text

SPDX-License-Identifier:	Apache-2.0
PackageLicenseDeclared:		Apache-2.0

Update tools data formats
=========================
To update SPDX tools, the following is a very brief checklist:
1. Update the SpdxRdfContants with any new or changed RDF properties and classes
2. Update the Java code representing the RDF model.  
3. Update the properties files in the org.spdx.tag package for any new tag values
4. Update the org.spdx.tag.CommonCode.java for any new or changed tag values.  This will implement
both the rdfToTag and the SPDXViewer applications.
5. Update the org.spdx.tag.BuildDocument to implement changes for the TagToRdf application
6. Update the HTML template (resources/htmlTemplate/SpdxHTMLTemplate.html) and contexts in org.spdx.html to 
implement changes for the SpdxToHtml application
7. Update the related sheets and RdfToSpreadsheet.java file in the package org.spdx.spreadsheet
8. Update the sheets and SpdxComparer/SpdxFileComparer in the org.spdx.compare package
