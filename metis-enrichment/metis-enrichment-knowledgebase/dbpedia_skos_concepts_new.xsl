<?xml version="1.0" encoding="UTF-8"?>

<!-- Document : dbpedia.xsl Created on : October 13, 2014, 11:33 AM Author 
	: gmamakis, cesare Description: Map DBPedia to EDM-Agent -->

<xsl:stylesheet version="1.0"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:dcterms="http://purl.org/dc/terms/"
				xmlns:skos="http://www.w3.org/2004/02/skos/core#"
				xmlns:owl="http://www.w3.org/2002/07/owl#"
				xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
				xmlns:dbpedia-owl="http://dbpedia.org/ontology/"
				xmlns:dbpprop="http://dbpedia.org/property/"
				xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
>
	<xsl:output indent="no"/>

	<xsl:param name="rdf_about">
		<xsl:text>test</xsl:text>
	</xsl:param>

	<xsl:template match="/">

		<xsl:variable name="subgenres" select="/rdf:RDF/rdf:Description/dbpprop:subgenres"/>

		<xsl:for-each select="rdf:RDF/rdf:Description">

			<xsl:element name="skos:Concept">

				<xsl:variable name="about" select="@rdf:about"/>

				<xsl:copy-of select="@rdf:about"/>

				<xsl:for-each select="rdfs:label">
					<xsl:element name="skos:prefLabel">
						<xsl:copy-of select="@xml:lang"/>
						<xsl:value-of select="."/>
					</xsl:element>
				</xsl:for-each>

				<xsl:for-each select="dcterms:subject">
					<xsl:element name="skos:related">
						<xsl:copy-of select="@rdf:resource"/>
					</xsl:element>
				</xsl:for-each>

				<xsl:for-each select="dbpedia-owl:abstract | dbpprop:abstract">
					<xsl:element name="skos:note">
						<xsl:copy-of select="@xml:lang"/>
						<xsl:value-of select="." />
					</xsl:element>
				</xsl:for-each>
	
				<xsl:for-each select="owl:sameAs">
					<xsl:element name="skos:exactMatch">
						<xsl:copy-of select="@rdf:resource"/>
					</xsl:element>
				</xsl:for-each>

				<xsl:for-each select="$subgenres[@rdf:resource=$about]">
					<xsl:element name="skos:broader">
						<xsl:attribute name="rdf:resource">
                            				<xsl:value-of select="../@rdf:about"/>
                        			</xsl:attribute>
					</xsl:element>
				</xsl:for-each>


			</xsl:element>

		</xsl:for-each>

	</xsl:template>


</xsl:stylesheet>
