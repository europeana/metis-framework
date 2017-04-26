<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:edm="http://www.europeana.eu/schemas/edm/"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:gn="http://www.geonames.org/ontology#"
                xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#"

                >
    <xsl:output indent="yes" encoding="UTF-8"/>
    <xsl:template match="/rdf:RDF/gn:Feature">

        <edm:Place>
            <xsl:copy-of select="@rdf:about"/>
            <xsl:for-each select="gn:alternateName">
                <xsl:element name="skos:altLabel">
                    <xsl:copy-of select="@xml:lang"/>
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="gn:colloquialName">
                <xsl:element name="skos:altLabel">
                    <xsl:copy-of select="@xml:lang"/>
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="gn:historicalName">
                <xsl:element name="skos:altLabel">
                    <xsl:copy-of select="@xml:lang"/>
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="gn:name">
                <xsl:element name="skos:prefLabel">
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="gn:officialName">
                <xsl:element name="skos:prefLabel">
                    <xsl:copy-of select="@xml:lang"/>
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="gn:shortName">
                <xsl:element name="skos:altLabel">
                    <xsl:copy-of select="@xml:lang"/>
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="gn:childrenFeatures">
                <xsl:element name="dcterms:hasPart">
                    <xsl:copy-of select="@rdf:resource"/>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="gn:nearby">
                <xsl:element name="skos:related">
                    <xsl:copy-of select="@rdf:resource"/>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="gn:parentCountry">
                <xsl:element name="dcterms:isPartOf">
                    <xsl:copy-of select="@rdf:resource"/>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="gn:parentFeature">
                <xsl:element name="dcterms:isPartOf">
                    <xsl:copy-of select="@rdf:resource"/>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="gn:wikipediaArticle">
                <xsl:element name="skos:note">
                    <xsl:copy-of select="@rdf:resource"/>
                </xsl:element>
            </xsl:for-each>
            <xsl:copy-of select="wgs84_pos:long"/>
            <xsl:copy-of select="wgs84_pos:lat"/>
            <xsl:for-each select="owl:sameAs">
                <xsl:copy-of select="."/>
                    </xsl:for-each>
        </edm:Place>

    </xsl:template>


</xsl:stylesheet>