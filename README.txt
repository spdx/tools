See the SPDX Tools documentation located at http://spdx.org/tools for usage information.

See the NOTICE file for licensing information.

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