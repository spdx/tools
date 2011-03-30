<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns="http://www.w3.org/1999/xhtml" 
  xmlns:html="http://www.w3.org/1999/xhtml" 
  exclude-result-prefixes="html">

<!-- Output method XML -->
<xsl:output method="xml" 
  indent="yes"
  omit-xml-declaration="no" 
  encoding="utf-8"  />

  <xsl:template match="/">
    <rdf:RDF  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
              xmlns:ex="http://example.org/test#">

     <rdf:Description rdf:about="">

        <dc:title     xmlns:dc="http://purl.org/dc/elements/1.1/"><xsl:value-of select="/html:html/html:head/html:title"/></dc:title>
      </rdf:Description>

    </rdf:RDF>
  </xsl:template>
</xsl:stylesheet>

