<?xml version="1.0" encoding="UTF-8"?>

<!-- Document : dbpedia.xsl Created on : October 13, 2014, 11:33 AM Author 
	: gmamakis, cesare Description: Map DBPedia to EDM-Agent -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				version="1.0"
				xmlns:dcterms="http://purl.org/dc/terms/"
				xmlns:skos="http://www.w3.org/2004/02/skos/core#"
				xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
				xmlns:dbpedia-owl="http://dbpedia.org/ontology/"
				xmlns:dbpprop="http://dbpedia.org/property/" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
	<xsl:output indent="yes" />
	<xsl:param name="rdf_about">
		<xsl:text>test</xsl:text>
	</xsl:param>

	<xsl:template match="/">

		<xsl:element name="skos:Concept">

			<xsl:attribute name="rdf:about"
				namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <xsl:value-of select="$rdf_about" />
            </xsl:attribute>

			<xsl:for-each select="rdf:RDF/rdf:Description/rdfs:label">
				<xsl:element name="skos:prefLabel" namespace="http://www.w3.org/2004/02/skos/core#">
					<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			
			<xsl:for-each select="rdf:RDF/rdf:Description/dcterms:subject">
				<xsl:element name="skos:related" namespace="http://www.w3.org/2004/02/skos/core#">    
						<xsl:attribute name="rdf:resource"
							namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <xsl:value-of select="./@rdf:resource" />
                    </xsl:attribute>
                    
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			
			
			<xsl:for-each select="rdf:RDF/rdf:Description/dbpedia-owl:abstract">
				<xsl:element name="skos:note"
					namespace="http://www.w3.org/2004/02/skos/core#">
					<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/rdf:Description/dbpprop:abstract">
				<xsl:element name="skos:note"
					namespace="http://www.w3.org/2004/02/skos/core#">
					<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="rdf:RDF/rdf:Description/owl:sameAs">
				<xsl:element name="skos:exactMatch" namespace="http://www.w3.org/2004/02/skos/core#">
					<xsl:attribute name="rdf:resource"
						namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <xsl:value-of select="./@rdf:resource" />
                    </xsl:attribute>
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="rdf:RDF/rdf:Description/dbpprop:subgenres">
				<xsl:element name="skos:broader" namespace="http://www.w3.org/2004/02/skos/core#">
					<xsl:if test="../@rdf:about">
						<xsl:attribute name="rdf:resource"
							namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                            <xsl:value-of select="../@rdf:about" />
                        </xsl:attribute>
                        
                        <!--xsl:value-of select="../@rdf:about" /-->
					</xsl:if>
					
				</xsl:element>
			</xsl:for-each>
			
			
		</xsl:element>

	</xsl:template>


</xsl:stylesheet>
