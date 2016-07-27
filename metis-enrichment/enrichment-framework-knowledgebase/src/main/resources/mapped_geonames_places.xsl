<?xml version="1.0" encoding="UTF-8"?>

<!-- Document : dbpedia.xsl Created on : October 13, 2014, 11:33 AM Author 
	: gmamakis, cesare Description: Map DBPedia to EDM-Agent -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#"
                xmlns:edm="http://www.europeana.eu/schemas/edm/"
>
    <xsl:output indent="yes"/>
    <xsl:param name="rdf_about">
        <xsl:text>test</xsl:text>
    </xsl:param>

    <!--
    <rdf:Description rdf:about="http://sws.geonames.org/6612802/">
    <dcterms:isPartOf rdf:resource="http://sws.geonames.org/2969678/"/>
    <dcterms:isPartOf rdf:resource="http://sws.geonames.org/3017382/"/>
    <europeana:country>FR</europeana:country>
    <europeana:division rdf:resource="http://www.geonames.org/ontology#A.ADM4"/>
    <europeana:population>1403</europeana:population>
    <lat xmlns="http://www.w3.org/2003/01/geo/wgs84_pos#">48.73639</lat>
    <long xmlns="http://www.w3.org/2003/01/geo/wgs84_pos#">2.09111</long>
    <skos:altLabel xml:lang="kk">Шатофор</skos:altLabel>
    <skos:altLabel xml:lang="sr">Шатофор</skos:altLabel>
    <skos:altLabel xml:lang="uk">Шатофор</skos:altLabel>
    <skos:prefLabel>Châteaufort</skos:prefLabel>
</rdf:Description>
    -->


    <xsl:template match="/">

        <xsl:element name="edm:Place">

            <xsl:attribute name="rdf:about"
                           namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <xsl:value-of select="rdf:RDF/rdf:Description/@rdf:about"/>
            </xsl:attribute>

            <xsl:for-each select="rdf:RDF/rdf:Description/skos:prefLabel">
                <xsl:element name="skos:prefLabel" namespace="http://www.w3.org/2004/02/skos/core#">
                    <xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang"/>
                    </xsl:attribute>
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:for-each>

            <xsl:for-each select="rdf:RDF/rdf:Description/skos:altLabel">
                <xsl:choose>
                    <xsl:when test="./@xml:lang!=''">
                        <xsl:element name="skos:prefLabel" namespace="http://www.w3.org/2004/02/skos/core#">
                            <xsl:attribute name="xml:lang">
                                <xsl:value-of select="./@xml:lang"/>
                            </xsl:attribute>

                            <xsl:value-of select="."/>
                        </xsl:element>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="skos:altLabel" namespace="http://www.w3.org/2004/02/skos/core#">
                            <xsl:value-of select="."/>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>


            <xsl:for-each select="rdf:RDF/rdf:Description/wgs84_pos:lat">
                <xsl:element name="wgs84_pos:lat"
                             namespace="http://www.w3.org/2003/01/geo/wgs84_pos#">
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="rdf:RDF/rdf:Description/wgs84_pos:long">
                <xsl:element name="wgs84_pos:long"
                             namespace="http://www.w3.org/2003/01/geo/wgs84_pos#">
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:for-each>

            <xsl:for-each select="rdf:RDF/rdf:Description/dcterms:isPartOf">
                <xsl:element name="dcterms:isPartOf" namespace="http://purl.org/dc/terms/">
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="./@rdf:resource"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:for-each>


        </xsl:element>

    </xsl:template>


</xsl:stylesheet>
