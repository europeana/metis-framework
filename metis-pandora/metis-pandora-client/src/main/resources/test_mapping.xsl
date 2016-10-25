<?xml version="1.0" encoding="UTF-8"?><xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:odrl="http://www.w3.org/ns/odrl/2/" xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#" xmlns:wdrs="http://www.w3.org/2007/05/powder-s#" xmlns:svcs="http://rdfs.org/sioc/services#" xmlns:edm="http://www.europeana.eu/schemas/edm/" xmlns:repox="http://repox.ist.utl.pt" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:ebucore="http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#" xmlns:adms="http://www.w3.org/ns/adms#" xmlns:pr21="http://www.europeana.eu/schemas/edm/enrichment/" xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:rdaGr2="http://rdvocab.info/ElementsGr2/" xmlns:xml="http://www.w3.org/XML/1998/namespace" xmlns:skos="http://www.w3.org/2004/02/skos/core#" xmlns:sch="http://purl.oclc.org/dsdl/schematron" xmlns:ore="http://www.openarchives.org/ore/terms/" xmlns:dcat="http://www.w3.org/ns/dcat#" xmlns:cc="http://creativecommons.org/ns#" xmlns:foaf="http://xmlns.com/foaf/0.1/"  ><xsl:variable name="edm:ugc"><item>true</item></xsl:variable><xsl:variable name="edm:type"><item>TEXT</item><item>VIDEO</item><item>IMAGE</item><item>SOUND</item><item>3D</item></xsl:variable><xsl:template match="/">
<xsl:apply-templates select="/repox:exportedRecords/repox:record/repox:metadata/record"/>

</xsl:template><xsl:template match="/repox:exportedRecords/repox:record/repox:metadata/record">
<rdf:RDF><xsl:if test="edm:ProvidedCHO"><xsl:for-each select="edm:ProvidedCHO"><xsl:if test="position() = 1"><xsl:element name="edm:ProvidedCHO">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:about"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:if test="owl:sameAs"><xsl:for-each select="owl:sameAs"><xsl:if test="position() = 1"><xsl:element name="owl:sameAs">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:WebResource"><xsl:for-each select="edm:WebResource"><xsl:if test="position() = 1"><xsl:element name="edm:WebResource">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:about"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:if test="dc:creator"><xsl:for-each select="dc:creator"><xsl:if test="position() = 1"><xsl:element name="dc:creator">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:description"><xsl:for-each select="dc:description"><xsl:if test="position() = 1"><xsl:element name="dc:description">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:format"><xsl:for-each select="dc:format"><xsl:if test="position() = 1"><xsl:element name="dc:format">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:rights"><xsl:for-each select="dc:rights"><xsl:if test="position() = 1"><xsl:element name="dc:rights">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:source"><xsl:for-each select="dc:source"><xsl:if test="position() = 1"><xsl:element name="dc:source">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:conformsTo"><xsl:for-each select="dcterms:conformsTo"><xsl:if test="position() = 1"><xsl:element name="dcterms:conformsTo">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:created"><xsl:for-each select="dcterms:created"><xsl:if test="position() = 1"><xsl:element name="dcterms:created">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:extent"><xsl:for-each select="dcterms:extent"><xsl:if test="position() = 1"><xsl:element name="dcterms:extent">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:hasPart"><xsl:for-each select="dcterms:hasPart"><xsl:if test="position() = 1"><xsl:element name="dcterms:hasPart">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isFormatOf"><xsl:for-each select="dcterms:isFormatOf"><xsl:if test="position() = 1"><xsl:element name="dcterms:isFormatOf">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isPartOf"><xsl:for-each select="dcterms:isPartOf"><xsl:if test="position() = 1"><xsl:element name="dcterms:isPartOf">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:issued"><xsl:for-each select="dcterms:issued"><xsl:if test="position() = 1"><xsl:element name="dcterms:issued">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isNextInSequence"><xsl:for-each select="edm:isNextInSequence"><xsl:if test="position() = 1"><xsl:element name="edm:isNextInSequence">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:rights"><xsl:for-each select="edm:rights"><xsl:if test="position() = 1"><xsl:element name="edm:rights">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="owl:sameAs"><xsl:for-each select="owl:sameAs"><xsl:if test="position() = 1"><xsl:element name="owl:sameAs">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="svcs:has_service"><xsl:for-each select="svcs:has_service"><xsl:if test="position() = 1"><xsl:element name="svcs:has_service">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:Agent"><xsl:for-each select="edm:Agent"><xsl:if test="position() = 1"><xsl:element name="edm:Agent">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:about"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:if test="skos:prefLabel"><xsl:for-each select="skos:prefLabel"><xsl:if test="position() = 1"><xsl:element name="skos:prefLabel">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:altLabel"><xsl:for-each select="skos:altLabel"><xsl:if test="position() = 1"><xsl:element name="skos:altLabel">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:note"><xsl:for-each select="skos:note"><xsl:if test="position() = 1"><xsl:element name="skos:note">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:date"><xsl:for-each select="dc:date"><xsl:if test="position() = 1"><xsl:element name="dc:date">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:identifier"><xsl:for-each select="dc:identifier"><xsl:if test="position() = 1"><xsl:element name="dc:identifier">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:hasPart"><xsl:for-each select="dcterms:hasPart"><xsl:if test="position() = 1"><xsl:element name="dcterms:hasPart">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isPartOf"><xsl:for-each select="dcterms:isPartOf"><xsl:if test="position() = 1"><xsl:element name="dcterms:isPartOf">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:begin"><xsl:for-each select="edm:begin"><xsl:if test="position() = 1"><xsl:element name="edm:begin">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:end"><xsl:for-each select="edm:end"><xsl:if test="position() = 1"><xsl:element name="edm:end">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:hasMet"><xsl:for-each select="edm:hasMet"><xsl:if test="position() = 1"><xsl:element name="edm:hasMet">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isRelatedTo"><xsl:for-each select="edm:isRelatedTo"><xsl:if test="position() = 1"><xsl:element name="edm:isRelatedTo">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="foaf:name"><xsl:for-each select="foaf:name"><xsl:if test="position() = 1"><xsl:element name="foaf:name">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="rdaGr2:biographicalInformation"><xsl:for-each select="rdaGr2:biographicalInformation"><xsl:if test="position() = 1"><xsl:element name="rdaGr2:biographicalInformation">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="rdaGr2:dateOfBirth"><xsl:for-each select="rdaGr2:dateOfBirth"><xsl:if test="position() = 1"><xsl:element name="rdaGr2:dateOfBirth">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="rdaGr2:dateOfDeath"><xsl:for-each select="rdaGr2:dateOfDeath"><xsl:if test="position() = 1"><xsl:element name="rdaGr2:dateOfDeath">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="rdaGr2:dateOfEstablishment"><xsl:for-each select="rdaGr2:dateOfEstablishment"><xsl:if test="position() = 1"><xsl:element name="rdaGr2:dateOfEstablishment">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="rdaGr2:dateOfTermination"><xsl:for-each select="rdaGr2:dateOfTermination"><xsl:if test="position() = 1"><xsl:element name="rdaGr2:dateOfTermination">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="rdaGr2:gender"><xsl:for-each select="rdaGr2:gender"><xsl:if test="position() = 1"><xsl:element name="rdaGr2:gender">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="rdaGr2:placeOfBirth"><xsl:for-each select="rdaGr2:placeOfBirth"><xsl:if test="position() = 1"><xsl:element name="rdaGr2:placeOfBirth">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="rdaGr2:placeOfDeath"><xsl:for-each select="rdaGr2:placeOfDeath"><xsl:if test="position() = 1"><xsl:element name="rdaGr2:placeOfDeath">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="rdaGr2:professionOrOccupation"><xsl:for-each select="rdaGr2:professionOrOccupation"><xsl:if test="position() = 1"><xsl:element name="rdaGr2:professionOrOccupation">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="owl:sameAs"><xsl:for-each select="owl:sameAs"><xsl:if test="position() = 1"><xsl:element name="owl:sameAs">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:Place"><xsl:for-each select="edm:Place"><xsl:if test="position() = 1"><xsl:element name="edm:Place">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:about"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:if test="skos:prefLabel"><xsl:for-each select="skos:prefLabel"><xsl:if test="position() = 1"><xsl:element name="skos:prefLabel">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:altLabel"><xsl:for-each select="skos:altLabel"><xsl:if test="position() = 1"><xsl:element name="skos:altLabel">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:note"><xsl:for-each select="skos:note"><xsl:if test="position() = 1"><xsl:element name="skos:note">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:hasPart"><xsl:for-each select="dcterms:hasPart"><xsl:if test="position() = 1"><xsl:element name="dcterms:hasPart">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isPartOf"><xsl:for-each select="dcterms:isPartOf"><xsl:if test="position() = 1"><xsl:element name="dcterms:isPartOf">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isNextInSequence"><xsl:for-each select="edm:isNextInSequence"><xsl:if test="position() = 1"><xsl:element name="edm:isNextInSequence">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="owl:sameAs"><xsl:for-each select="owl:sameAs"><xsl:if test="position() = 1"><xsl:element name="owl:sameAs">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:TimeSpan"><xsl:for-each select="edm:TimeSpan"><xsl:if test="position() = 1"><xsl:element name="edm:TimeSpan">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:about"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:if test="skos:prefLabel"><xsl:for-each select="skos:prefLabel"><xsl:if test="position() = 1"><xsl:element name="skos:prefLabel">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:altLabel"><xsl:for-each select="skos:altLabel"><xsl:if test="position() = 1"><xsl:element name="skos:altLabel">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:note"><xsl:for-each select="skos:note"><xsl:if test="position() = 1"><xsl:element name="skos:note">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:hasPart"><xsl:for-each select="dcterms:hasPart"><xsl:if test="position() = 1"><xsl:element name="dcterms:hasPart">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isPartOf"><xsl:for-each select="dcterms:isPartOf"><xsl:if test="position() = 1"><xsl:element name="dcterms:isPartOf">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:begin"><xsl:for-each select="edm:begin"><xsl:if test="position() = 1"><xsl:element name="edm:begin">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:end"><xsl:for-each select="edm:end"><xsl:if test="position() = 1"><xsl:element name="edm:end">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isNextInSequence"><xsl:for-each select="edm:isNextInSequence"><xsl:if test="position() = 1"><xsl:element name="edm:isNextInSequence">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="owl:sameAs"><xsl:for-each select="owl:sameAs"><xsl:if test="position() = 1"><xsl:element name="owl:sameAs">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:Concept"><xsl:for-each select="skos:Concept"><xsl:if test="position() = 1"><xsl:element name="skos:Concept">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:about"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:if test="skos:prefLabel"><xsl:for-each select="skos:prefLabel"><xsl:if test="position() = 1"><xsl:element name="skos:prefLabel">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:altLabel"><xsl:for-each select="skos:altLabel"><xsl:if test="position() = 1"><xsl:element name="skos:altLabel">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:broader"><xsl:for-each select="skos:broader"><xsl:if test="position() = 1"><xsl:element name="skos:broader">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:narrower"><xsl:for-each select="skos:narrower"><xsl:if test="position() = 1"><xsl:element name="skos:narrower">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:related"><xsl:for-each select="skos:related"><xsl:if test="position() = 1"><xsl:element name="skos:related">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:broadMatch"><xsl:for-each select="skos:broadMatch"><xsl:if test="position() = 1"><xsl:element name="skos:broadMatch">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:narrowMatch"><xsl:for-each select="skos:narrowMatch"><xsl:if test="position() = 1"><xsl:element name="skos:narrowMatch">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:relatedMatch"><xsl:for-each select="skos:relatedMatch"><xsl:if test="position() = 1"><xsl:element name="skos:relatedMatch">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:exactMatch"><xsl:for-each select="skos:exactMatch"><xsl:if test="position() = 1"><xsl:element name="skos:exactMatch">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:closeMatch"><xsl:for-each select="skos:closeMatch"><xsl:if test="position() = 1"><xsl:element name="skos:closeMatch">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:note"><xsl:for-each select="skos:note"><xsl:if test="position() = 1"><xsl:element name="skos:note">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:notation"><xsl:for-each select="skos:notation"><xsl:if test="position() = 1"><xsl:element name="skos:notation">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="skos:inScheme"><xsl:for-each select="skos:inScheme"><xsl:if test="position() = 1"><xsl:element name="skos:inScheme">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="ore:Aggregation"><xsl:for-each select="ore:Aggregation"><xsl:if test="position() = 1"><xsl:element name="ore:Aggregation">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:about"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:if test="edm:aggregatedCHO"><xsl:for-each select="edm:aggregatedCHO"><xsl:if test="position() = 1"><xsl:element name="edm:aggregatedCHO">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:dataProvider"><xsl:for-each select="edm:dataProvider"><xsl:if test="position() = 1"><xsl:element name="edm:dataProvider">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:hasView"><xsl:for-each select="edm:hasView"><xsl:if test="position() = 1"><xsl:element name="edm:hasView">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isShownAt"><xsl:for-each select="edm:isShownAt"><xsl:if test="position() = 1"><xsl:element name="edm:isShownAt">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isShownBy"><xsl:for-each select="edm:isShownBy"><xsl:if test="position() = 1"><xsl:element name="edm:isShownBy">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:object"><xsl:for-each select="edm:object"><xsl:if test="position() = 1"><xsl:element name="edm:object">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:provider"><xsl:for-each select="edm:provider"><xsl:if test="position() = 1"><xsl:element name="edm:provider">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:rights"><xsl:for-each select="dc:rights"><xsl:if test="position() = 1"><xsl:element name="dc:rights">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:rights"><xsl:for-each select="edm:rights"><xsl:if test="position() = 1"><xsl:element name="edm:rights">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:ugc"><xsl:for-each select="edm:ugc"><xsl:if test="position() = 1"><xsl:if test="index-of($edm:ugc/item, replace(.,'^\s*(.+?)\s*$', '$1')) &gt; 0"><xsl:element name="edm:ugc">

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:intermediateProvider"><xsl:for-each select="edm:intermediateProvider"><xsl:if test="position() = 1"><xsl:element name="edm:intermediateProvider">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:ProvidedCHO"><xsl:for-each select="edm:ProvidedCHO"><xsl:if test="position() = 1"><xsl:element name="ore:Proxy">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:about"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:if test="dc:contributor"><xsl:for-each select="dc:contributor"><xsl:if test="position() = 1"><xsl:element name="dc:contributor">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:creator"><xsl:for-each select="dc:creator"><xsl:if test="position() = 1"><xsl:element name="dc:creator">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:date"><xsl:for-each select="dc:date"><xsl:if test="position() = 1"><xsl:element name="dc:date">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:description"><xsl:for-each select="dc:description"><xsl:if test="position() = 1"><xsl:element name="dc:description">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:format"><xsl:for-each select="dc:format"><xsl:if test="position() = 1"><xsl:element name="dc:format">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:identifier"><xsl:for-each select="dc:identifier"><xsl:if test="position() = 1"><xsl:element name="dc:identifier">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:language"><xsl:for-each select="dc:language"><xsl:if test="position() = 1"><xsl:element name="dc:language">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:publisher"><xsl:for-each select="dc:publisher"><xsl:if test="position() = 1"><xsl:element name="dc:publisher">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:relation"><xsl:for-each select="dc:relation"><xsl:if test="position() = 1"><xsl:element name="dc:relation">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:rights"><xsl:for-each select="dc:rights"><xsl:if test="position() = 1"><xsl:element name="dc:rights">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:source"><xsl:for-each select="dc:source"><xsl:if test="position() = 1"><xsl:element name="dc:source">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:subject"><xsl:for-each select="dc:subject"><xsl:if test="position() = 1"><xsl:element name="dc:subject">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:title"><xsl:for-each select="dc:title"><xsl:if test="position() = 1"><xsl:element name="dc:title">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:type"><xsl:for-each select="dc:type"><xsl:if test="position() = 1"><xsl:element name="dc:type">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:alternative"><xsl:for-each select="dcterms:alternative"><xsl:if test="position() = 1"><xsl:element name="dcterms:alternative">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:conformsTo"><xsl:for-each select="dcterms:conformsTo"><xsl:if test="position() = 1"><xsl:element name="dcterms:conformsTo">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:created"><xsl:for-each select="dcterms:created"><xsl:if test="position() = 1"><xsl:element name="dcterms:created">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:extent"><xsl:for-each select="dcterms:extent"><xsl:if test="position() = 1"><xsl:element name="dcterms:extent">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:hasFormat"><xsl:for-each select="dcterms:hasFormat"><xsl:if test="position() = 1"><xsl:element name="dcterms:hasFormat">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:hasPart"><xsl:for-each select="dcterms:hasPart"><xsl:if test="position() = 1"><xsl:element name="dcterms:hasPart">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:hasVersion"><xsl:for-each select="dcterms:hasVersion"><xsl:if test="position() = 1"><xsl:element name="dcterms:hasVersion">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isFormatOf"><xsl:for-each select="dcterms:isFormatOf"><xsl:if test="position() = 1"><xsl:element name="dcterms:isFormatOf">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isPartOf"><xsl:for-each select="dcterms:isPartOf"><xsl:if test="position() = 1"><xsl:element name="dcterms:isPartOf">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isReferencedBy"><xsl:for-each select="dcterms:isReferencedBy"><xsl:if test="position() = 1"><xsl:element name="dcterms:isReferencedBy">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isReplacedBy"><xsl:for-each select="dcterms:isReplacedBy"><xsl:if test="position() = 1"><xsl:element name="dcterms:isReplacedBy">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isRequiredBy"><xsl:for-each select="dcterms:isRequiredBy"><xsl:if test="position() = 1"><xsl:element name="dcterms:isRequiredBy">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:issued"><xsl:for-each select="dcterms:issued"><xsl:if test="position() = 1"><xsl:element name="dcterms:issued">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isVersionOf"><xsl:for-each select="dcterms:isVersionOf"><xsl:if test="position() = 1"><xsl:element name="dcterms:isVersionOf">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:medium"><xsl:for-each select="dcterms:medium"><xsl:if test="position() = 1"><xsl:element name="dcterms:medium">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:provenance"><xsl:for-each select="dcterms:provenance"><xsl:if test="position() = 1"><xsl:element name="dcterms:provenance">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:references"><xsl:for-each select="dcterms:references"><xsl:if test="position() = 1"><xsl:element name="dcterms:references">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:replaces"><xsl:for-each select="dcterms:replaces"><xsl:if test="position() = 1"><xsl:element name="dcterms:replaces">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:requires"><xsl:for-each select="dcterms:requires"><xsl:if test="position() = 1"><xsl:element name="dcterms:requires">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:spatial"><xsl:for-each select="dcterms:spatial"><xsl:if test="position() = 1"><xsl:element name="dcterms:spatial">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:tableOfContents"><xsl:for-each select="dcterms:tableOfContents"><xsl:if test="position() = 1"><xsl:element name="dcterms:tableOfContents">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:temporal"><xsl:for-each select="dcterms:temporal"><xsl:if test="position() = 1"><xsl:element name="dcterms:temporal">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:currentLocation"><xsl:for-each select="edm:currentLocation"><xsl:if test="position() = 1"><xsl:element name="edm:currentLocation">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:hasMet"><xsl:for-each select="edm:hasMet"><xsl:if test="position() = 1"><xsl:element name="edm:hasMet">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:hasType"><xsl:for-each select="edm:hasType"><xsl:if test="position() = 1"><xsl:element name="edm:hasType">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:incorporates"><xsl:for-each select="edm:incorporates"><xsl:if test="position() = 1"><xsl:element name="edm:incorporates">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isDerivativeOf"><xsl:for-each select="edm:isDerivativeOf"><xsl:if test="position() = 1"><xsl:element name="edm:isDerivativeOf">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isNextInSequence"><xsl:for-each select="edm:isNextInSequence"><xsl:if test="position() = 1"><xsl:element name="edm:isNextInSequence">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isRelatedTo"><xsl:for-each select="edm:isRelatedTo"><xsl:if test="position() = 1"><xsl:element name="edm:isRelatedTo">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isRepresentationOf"><xsl:for-each select="edm:isRepresentationOf"><xsl:if test="position() = 1"><xsl:element name="edm:isRepresentationOf">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isSimilarTo"><xsl:for-each select="edm:isSimilarTo"><xsl:if test="position() = 1"><xsl:element name="edm:isSimilarTo">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isSuccessorOf"><xsl:for-each select="edm:isSuccessorOf"><xsl:if test="position() = 1"><xsl:element name="edm:isSuccessorOf">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:realizes"><xsl:for-each select="edm:realizes"><xsl:if test="position() = 1"><xsl:element name="edm:realizes">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="ore:proxyFor"><xsl:for-each select="ore:proxyFor"><xsl:if test="position() = 1"><xsl:element name="ore:proxyFor">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:ProvidedCHO"><xsl:for-each select="edm:ProvidedCHO"><xsl:if test="position() = 1"><xsl:element name="ore:proxyIn">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:type"><xsl:for-each select="edm:type"><xsl:if test="position() = 1"><xsl:if test="index-of($edm:type/item, replace(.,'^\s*(.+?)\s*$', '$1')) &gt; 0"><xsl:element name="edm:type">

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="owl:sameAs"><xsl:for-each select="owl:sameAs"><xsl:if test="position() = 1"><xsl:element name="owl:sameAs">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:ProvidedCHO"><xsl:for-each select="edm:ProvidedCHO"><xsl:if test="position() = 1"><xsl:element name="ore:Proxy">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:about"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:if test="dc:contributor"><xsl:for-each select="dc:contributor"><xsl:if test="position() = 1"><xsl:element name="dc:contributor">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:creator"><xsl:for-each select="dc:creator"><xsl:if test="position() = 1"><xsl:element name="dc:creator">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:date"><xsl:for-each select="dc:date"><xsl:if test="position() = 1"><xsl:element name="dc:date">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:description"><xsl:for-each select="dc:description"><xsl:if test="position() = 1"><xsl:element name="dc:description">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:format"><xsl:for-each select="dc:format"><xsl:if test="position() = 1"><xsl:element name="dc:format">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:identifier"><xsl:for-each select="dc:identifier"><xsl:if test="position() = 1"><xsl:element name="dc:identifier">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:language"><xsl:for-each select="dc:language"><xsl:if test="position() = 1"><xsl:element name="dc:language">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:publisher"><xsl:for-each select="dc:publisher"><xsl:if test="position() = 1"><xsl:element name="dc:publisher">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:relation"><xsl:for-each select="dc:relation"><xsl:if test="position() = 1"><xsl:element name="dc:relation">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:rights"><xsl:for-each select="dc:rights"><xsl:if test="position() = 1"><xsl:element name="dc:rights">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:source"><xsl:for-each select="dc:source"><xsl:if test="position() = 1"><xsl:element name="dc:source">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:subject"><xsl:for-each select="dc:subject"><xsl:if test="position() = 1"><xsl:element name="dc:subject">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:title"><xsl:for-each select="dc:title"><xsl:if test="position() = 1"><xsl:element name="dc:title">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dc:type"><xsl:for-each select="dc:type"><xsl:if test="position() = 1"><xsl:element name="dc:type">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:alternative"><xsl:for-each select="dcterms:alternative"><xsl:if test="position() = 1"><xsl:element name="dcterms:alternative">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:conformsTo"><xsl:for-each select="dcterms:conformsTo"><xsl:if test="position() = 1"><xsl:element name="dcterms:conformsTo">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:created"><xsl:for-each select="dcterms:created"><xsl:if test="position() = 1"><xsl:element name="dcterms:created">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:extent"><xsl:for-each select="dcterms:extent"><xsl:if test="position() = 1"><xsl:element name="dcterms:extent">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:hasFormat"><xsl:for-each select="dcterms:hasFormat"><xsl:if test="position() = 1"><xsl:element name="dcterms:hasFormat">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:hasPart"><xsl:for-each select="dcterms:hasPart"><xsl:if test="position() = 1"><xsl:element name="dcterms:hasPart">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:hasVersion"><xsl:for-each select="dcterms:hasVersion"><xsl:if test="position() = 1"><xsl:element name="dcterms:hasVersion">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isFormatOf"><xsl:for-each select="dcterms:isFormatOf"><xsl:if test="position() = 1"><xsl:element name="dcterms:isFormatOf">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isPartOf"><xsl:for-each select="dcterms:isPartOf"><xsl:if test="position() = 1"><xsl:element name="dcterms:isPartOf">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isReferencedBy"><xsl:for-each select="dcterms:isReferencedBy"><xsl:if test="position() = 1"><xsl:element name="dcterms:isReferencedBy">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isReplacedBy"><xsl:for-each select="dcterms:isReplacedBy"><xsl:if test="position() = 1"><xsl:element name="dcterms:isReplacedBy">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isRequiredBy"><xsl:for-each select="dcterms:isRequiredBy"><xsl:if test="position() = 1"><xsl:element name="dcterms:isRequiredBy">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:issued"><xsl:for-each select="dcterms:issued"><xsl:if test="position() = 1"><xsl:element name="dcterms:issued">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:isVersionOf"><xsl:for-each select="dcterms:isVersionOf"><xsl:if test="position() = 1"><xsl:element name="dcterms:isVersionOf">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:medium"><xsl:for-each select="dcterms:medium"><xsl:if test="position() = 1"><xsl:element name="dcterms:medium">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:provenance"><xsl:for-each select="dcterms:provenance"><xsl:if test="position() = 1"><xsl:element name="dcterms:provenance">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:references"><xsl:for-each select="dcterms:references"><xsl:if test="position() = 1"><xsl:element name="dcterms:references">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:replaces"><xsl:for-each select="dcterms:replaces"><xsl:if test="position() = 1"><xsl:element name="dcterms:replaces">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:requires"><xsl:for-each select="dcterms:requires"><xsl:if test="position() = 1"><xsl:element name="dcterms:requires">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:spatial"><xsl:for-each select="dcterms:spatial"><xsl:if test="position() = 1"><xsl:element name="dcterms:spatial">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:tableOfContents"><xsl:for-each select="dcterms:tableOfContents"><xsl:if test="position() = 1"><xsl:element name="dcterms:tableOfContents">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="dcterms:temporal"><xsl:for-each select="dcterms:temporal"><xsl:if test="position() = 1"><xsl:element name="dcterms:temporal">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:currentLocation"><xsl:for-each select="edm:currentLocation"><xsl:if test="position() = 1"><xsl:element name="edm:currentLocation">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:hasMet"><xsl:for-each select="edm:hasMet"><xsl:if test="position() = 1"><xsl:element name="edm:hasMet">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:hasType"><xsl:for-each select="edm:hasType"><xsl:if test="position() = 1"><xsl:element name="edm:hasType">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:incorporates"><xsl:for-each select="edm:incorporates"><xsl:if test="position() = 1"><xsl:element name="edm:incorporates">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isDerivativeOf"><xsl:for-each select="edm:isDerivativeOf"><xsl:if test="position() = 1"><xsl:element name="edm:isDerivativeOf">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isNextInSequence"><xsl:for-each select="edm:isNextInSequence"><xsl:if test="position() = 1"><xsl:element name="edm:isNextInSequence">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isRelatedTo"><xsl:for-each select="edm:isRelatedTo"><xsl:if test="position() = 1"><xsl:element name="edm:isRelatedTo">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:for-each select="@xml:lang"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isRepresentationOf"><xsl:for-each select="edm:isRepresentationOf"><xsl:if test="position() = 1"><xsl:element name="edm:isRepresentationOf">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isSimilarTo"><xsl:for-each select="edm:isSimilarTo"><xsl:if test="position() = 1"><xsl:element name="edm:isSimilarTo">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:isSuccessorOf"><xsl:for-each select="edm:isSuccessorOf"><xsl:if test="position() = 1"><xsl:element name="edm:isSuccessorOf">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:realizes"><xsl:for-each select="edm:realizes"><xsl:if test="position() = 1"><xsl:element name="edm:realizes">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="ore:proxyFor"><xsl:for-each select="ore:proxyFor"><xsl:if test="position() = 1"><xsl:element name="ore:proxyFor">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:ProvidedCHO"><xsl:for-each select="edm:ProvidedCHO"><xsl:if test="position() = 1"><xsl:element name="ore:proxyIn">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="edm:type"><xsl:for-each select="edm:type"><xsl:if test="position() = 1"><xsl:element name="edm:type">

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="owl:sameAs"><xsl:for-each select="owl:sameAs"><xsl:if test="position() = 1"><xsl:element name="owl:sameAs">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="ore:Aggregation"><xsl:for-each select="ore:Aggregation"><xsl:if test="position() = 1"><xsl:element name="edm:EuropeanaAggregation">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:about"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:element name="dc:creator">

<xsl:text>Europeana</xsl:text>
</xsl:element>
<xsl:if test="edm:aggregatedCHO"><xsl:for-each select="edm:aggregatedCHO"><xsl:if test="position() = 1"><xsl:element name="edm:aggregatedCHO">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:element name="edm:datasetName">

<xsl:text>2029901</xsl:text>
</xsl:element>
<xsl:element name="edm:country">

<xsl:text>Europe</xsl:text>
</xsl:element>
<xsl:element name="edm:language">

<xsl:text>mul</xsl:text>
</xsl:element>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="cc:License"><xsl:for-each select="cc:License"><xsl:if test="position() = 1"><xsl:element name="cc:License">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:about"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:if test="odrl:inheritFrom"><xsl:for-each select="odrl:inheritFrom"><xsl:if test="position() = 1"><xsl:element name="odrl:inheritFrom">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="cc:deprecatedOn"><xsl:for-each select="cc:deprecatedOn"><xsl:if test="position() = 1"><xsl:element name="cc:deprecatedOn">
<xsl:if test="@rdf:datatype"><xsl:attribute name="rdf:datatype"><xsl:for-each select="@rdf:datatype"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
<xsl:if test="svcs:Service"><xsl:for-each select="svcs:Service"><xsl:if test="position() = 1"><xsl:element name="svcs:Service">
<xsl:if test="@rdf:about"><xsl:attribute name="rdf:about"><xsl:for-each select="@rdf:about"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:if test="dcterms:conformsTo"><xsl:for-each select="dcterms:conformsTo"><xsl:if test="position() = 1"><xsl:element name="dcterms:conformsTo">
<xsl:if test="@rdf:resource"><xsl:attribute name="rdf:resource"><xsl:for-each select="@rdf:resource"><xsl:if test="position() = 1"><xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:attribute>
</xsl:if>

<xsl:value-of select="."/>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:if>
</rdf:RDF>

</xsl:template></xsl:stylesheet>