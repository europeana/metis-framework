<?xml version="1.0" encoding="UTF-8"?>

<!--
  Document : dbpedia.xsl Created on : October 13, 2014, 11:33 AM Author 
           : gmamakis, cesare Description: Map DBPedia to EDM-Agent
           : hmanguinhas Updated on: June, 15
-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:edm="http://www.europeana.eu/schemas/edm/"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:dcterms="http://purl.org/dc/terms/"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:skos="http://www.w3.org/2004/02/skos/core#"
	xmlns:owl="http://www.w3.org/2002/07/owl#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:rdaGr2="http://rdvocab.info/ElementsGr2/"
	xmlns:dbpedia-owl="http://dbpedia.org/ontology/"
	xmlns:dbpprop="http://dbpedia.org/property/"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	exclude-result-prefixes="rdfs dcterms foaf dbpedia-owl dbpprop">

	<xsl:output indent="yes" encoding="UTF-8"/>

	<xsl:param name="rdf_about">
		<xsl:text>test</xsl:text>
	</xsl:param>

	<xsl:template match="/">
		<xsl:apply-templates select="rdf:RDF/rdf:Description"/>
	</xsl:template>

	<xsl:template match="/rdf:RDF/rdf:Description">

		<edm:Agent>

			<xsl:copy-of select="@rdf:about"/>

			<xsl:variable name="labels" select="rdfs:label"/>

			<xsl:for-each select="$labels">
				<xsl:element name="skos:prefLabel">
					<xsl:copy-of select="@xml:lang"/>
					<xsl:value-of select="."/>
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="foaf:name | dbpedia-owl:alternativeNames | dbpprop:alternativeNames">
				<xsl:variable name="literal" select="text()"/>
				<xsl:variable name="lang"    select="@xml:lang"/>
				<xsl:if test="not($labels[text()=$literal and @xml:lang=$lang])">
					<xsl:element name="skos:altLabel">
						<xsl:copy-of select="@xml:lang"/>
						<xsl:value-of select="$literal" />
					</xsl:element>
				</xsl:if>
			</xsl:for-each>

			<xsl:for-each select="dbpedia-owl:abstract | dbpprop:abstract">
				<xsl:element name="rdaGr2:biographicalInformation">
					<xsl:copy-of select="@xml:lang"/>
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="dbpedia-owl:birthDate | dbpprop:birthDate">
				<xsl:element name="rdaGr2:dateOfBirth">
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="dbpedia-owl:deathDate | dbpprop:deathDate">
				<xsl:element name="rdaGr2:dateOfDeath">
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="dbpedia-owl:birthPlace | dbpprop:birthPlace">
				<xsl:element name="rdaGr2:placeOfBirth">
					<xsl:copy-of select="@rdf:resource"/>
					<xsl:copy-of select="@xml:lang"/>
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>


			<xsl:for-each select="dbpedia-owl:deathPlace | dbpprop:deathPlace">
				<xsl:element name="rdaGr2:placeOfDeath">
					<xsl:copy-of select="@rdf:resource"/>
					<xsl:copy-of select="@xml:lang"/>
					<xsl:value-of select="."/>
				</xsl:element>
			</xsl:for-each>


			<xsl:for-each select="dbpedia-owl:occupation | dbpprop:occupation">
				<xsl:element name="rdaGr2:professionOrOccupation">
					<xsl:call-template name="object_or_literal"/>
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="dbpedia-owl:birthDate | dbpprop:birthDate">
				<xsl:element name="edm:begin">
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="dbpedia-owl:deathDate | dbpprop:deathDate">
				<xsl:element name="edm:end">
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="dbpedia-owl:viaf | dbpprop:viaf">
				<xsl:element name="dc:identifier">
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="owl:sameAs">
				<xsl:element name="owl:sameAs">
					<xsl:copy-of select="@rdf:resource"/>
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="dbpedia-owl:influenced | dbpprop:influenced | dbpedia-owl:influencedBy | dbpprop:influencedBy">
				<xsl:if test="@rdf:resource">
					<xsl:element name="edm:isRelatedTo">
						<xsl:copy-of select="@rdf:resource"/>
					</xsl:element>
				</xsl:if>
			</xsl:for-each>

		</edm:Agent>

	</xsl:template>

	<xsl:template name="object_or_literal">
		<xsl:choose>
			<xsl:when test="@rdf:resource">
				<xsl:copy-of select="@rdf:resource"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="@xml:lang"/>
				<xsl:value-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>