<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:gn="http://www.geonames.org/ontology#" xmlns:skos="http://www.w3.org/2004/02/skos/core#"
	xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#" xmlns:edm="http://www.europeana.eu/schemas/edm/"
	xmlns:dc="http://purl.org/dc/terms/">
	<xsl:param name="targetId"></xsl:param>
	<xsl:output indent="yes" encoding="UTF-8"></xsl:output>
	<xsl:template match="/rdf:RDF">
		<!-- Parent mapping: gn:Feature -> edm:Place -->
		<xsl:for-each select="./gn:Feature">
			<xsl:if test="@rdf:about=$targetId">
				<edm:Place>
					<!-- Attribute mapping: rdf:about -> rdf:about -->
					<xsl:if test="@rdf:about">
						<xsl:attribute name="rdf:about"><xsl:value-of
							select="@rdf:about"></xsl:value-of></xsl:attribute>
					</xsl:if>
					<!-- Tag mapping: gn:wikipediaArticle -> skos:note -->
					<xsl:for-each select="./gn:wikipediaArticle">
						<skos:note>
							<!-- Text content mapping (only content with non-space characters) -->
							<xsl:for-each select="text()[normalize-space()]">
								<xsl:if test="position() &gt; 1">
									<xsl:text> </xsl:text>
								</xsl:if>
								<xsl:value-of select="normalize-space(.)"></xsl:value-of>
							</xsl:for-each>
						</skos:note>
					</xsl:for-each>
					<!-- Tag mapping: wgs84_pos:long -> wgs84_pos:long -->
					<xsl:for-each select="./wgs84_pos:long">
						<wgs84_pos:long>
							<!-- Text content mapping (only content with non-space characters) -->
							<xsl:for-each select="text()[normalize-space()]">
								<xsl:if test="position() &gt; 1">
									<xsl:text> </xsl:text>
								</xsl:if>
								<xsl:value-of select="normalize-space(.)"></xsl:value-of>
							</xsl:for-each>
						</wgs84_pos:long>
					</xsl:for-each>
					<!-- Tag mapping: gn:name -> skos:prefLabel -->
					<xsl:for-each select="./gn:name">
						<skos:prefLabel>
							<!-- Text content mapping (only content with non-space characters) -->
							<xsl:for-each select="text()[normalize-space()]">
								<xsl:if test="position() &gt; 1">
									<xsl:text> </xsl:text>
								</xsl:if>
								<xsl:value-of select="normalize-space(.)"></xsl:value-of>
							</xsl:for-each>
						</skos:prefLabel>
					</xsl:for-each>
					<!-- Tag mapping: gn:officialName -> skos:prefLabel -->
					<xsl:for-each select="./gn:officialName">
						<skos:prefLabel>
							<!-- Attribute mapping: xml:lang -> xml:lang -->
							<xsl:if test="@xml:lang">
								<xsl:attribute name="xml:lang"><xsl:value-of
									select="@xml:lang"></xsl:value-of></xsl:attribute>
							</xsl:if>
							<!-- Text content mapping (only content with non-space characters) -->
							<xsl:for-each select="text()[normalize-space()]">
								<xsl:if test="position() &gt; 1">
									<xsl:text> </xsl:text>
								</xsl:if>
								<xsl:value-of select="normalize-space(.)"></xsl:value-of>
							</xsl:for-each>
						</skos:prefLabel>
					</xsl:for-each>
					<!-- Tag mapping: gn:alternateName -> skos:altLabel -->
					<xsl:for-each select="./gn:alternateName">
						<skos:altLabel>
							<!-- Attribute mapping: xml:lang -> xml:lang -->
							<xsl:if test="@xml:lang">
								<xsl:attribute name="xml:lang"><xsl:value-of
									select="@xml:lang"></xsl:value-of></xsl:attribute>
							</xsl:if>
							<!-- Text content mapping (only content with non-space characters) -->
							<xsl:for-each select="text()[normalize-space()]">
								<xsl:if test="position() &gt; 1">
									<xsl:text> </xsl:text>
								</xsl:if>
								<xsl:value-of select="normalize-space(.)"></xsl:value-of>
							</xsl:for-each>
						</skos:altLabel>
					</xsl:for-each>
					<!-- Tag mapping: wgs84_pos:lat -> wgs84_pos:lat -->
					<xsl:for-each select="./wgs84_pos:lat">
						<wgs84_pos:lat>
							<!-- Text content mapping (only content with non-space characters) -->
							<xsl:for-each select="text()[normalize-space()]">
								<xsl:if test="position() &gt; 1">
									<xsl:text> </xsl:text>
								</xsl:if>
								<xsl:value-of select="normalize-space(.)"></xsl:value-of>
							</xsl:for-each>
						</wgs84_pos:lat>
					</xsl:for-each>
					<!-- Tag mapping: gn:parentCountry -> dc:isPartOf -->
					<xsl:for-each select="./gn:parentCountry">
						<dc:isPartOf>
							<!-- Attribute mapping: rdf:resource -> rdf:resource -->
							<xsl:if test="@rdf:resource">
								<xsl:attribute name="rdf:resource">
									<xsl:value-of select="@rdf:resource" />
								</xsl:attribute>
							</xsl:if>
						</dc:isPartOf>
					</xsl:for-each>
				</edm:Place>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>