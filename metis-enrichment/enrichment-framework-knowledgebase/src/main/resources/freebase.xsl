<?xml version="1.0" encoding="UTF-8"?>

<!-- Document : dbpedia.xsl Created on : October 13, 2014, 11:33 AM Author 
	: gmamakis Description: Map DBPedia to EDM-Agent -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				version="1.0" xmlns:edm="http://www.europeana.eu/schemas/edm/"
				xmlns:dc="http://purl.org/dc/elements/1.1/"
				xmlns:skos="http://www.w3.org/2004/02/skos/core#"
				xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
				xmlns:rdaGr2="http://rdvocab.info/ElementsGr2/" xmlns:dbpedia-owl="http://dbpedia.org/ontology/"
				xmlns:dbpprop="http://dbpedia.org/property/" xmlns:j.0="http://rdf.freebase.com/ns/"
>
	<xsl:output indent="yes" />
	<xsl:param name="rdf_about">
		<xsl:text>test</xsl:text>
	</xsl:param>

	<xsl:template match="/">

		<xsl:element name="edm:Agent" namespace="http://www.europeana.eu/schemas/edm/">

			<xsl:attribute name="rdf:about"
				namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <xsl:value-of select="$rdf_about" />
            </xsl:attribute>
			<xsl:for-each select="rdf:RDF/*/j.0:type.object.name">
				<xsl:element name="skos:prefLabel" namespace="http://www.w3.org/2004/02/skos/core#">
					<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/*/j.0:common.topic.alias">
				<xsl:element name="skos:altLabel" namespace="http://www.w3.org/2004/02/skos/core#">
					<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/rdf:Description/dbpprop:alternativeNames">
				<xsl:element name="skos:altLabel" namespace="http://www.w3.org/2004/02/skos/core#">
					<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/*/j.0:common.topic.description">
				<xsl:element name="rdaGr2:biographicalInformation"
					namespace="http://rdvocab.info/ElementsGr2/">
					<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/rdf:Description/dbpprop:abstract">
				<xsl:element name="rdaGr2:biographicalInformation"
					namespace="http://rdvocab.info/ElementsGr2/">
					<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="rdf:RDF/*/j.0:people.person.date_of_birth">
				<xsl:element name="rdaGr2:dateOfBirth" namespace="http://rdvocab.info/ElementsGr2/">
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/rdf:Description/dbpprop:birthDate">
				<xsl:element name="rdaGr2:dateOfBirth" namespace="http://rdvocab.info/ElementsGr2/">
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="rdf:RDF/*/j.0:people.deceased_person.date_of_death">
				<xsl:element name="rdaGr2:dateOfDeath" namespace="http://rdvocab.info/ElementsGr2/">
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/rdf:Description/dbpprop:deathDate">
				<xsl:element name="rdaGr2:dateOfDeath" namespace="http://rdvocab.info/ElementsGr2/">
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/*/j.0:people.person.place_of_birth">
				<xsl:element name="rdaGr2:placeOfBirth" namespace="http://rdvocab.info/ElementsGr2/">
					<xsl:if test="./@rdf:resource">
						<xsl:attribute name="rdf:resource"
							namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <xsl:value-of select="./@rdf:resource" />
                    </xsl:attribute>
					</xsl:if>
					<xsl:if test=".!=''">
						<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
						<xsl:value-of select="." />
					</xsl:if>
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/rdf:Description/dbpprop:birthPlace">
				<xsl:element name="rdaGr2:placeOfBirth" namespace="http://rdvocab.info/ElementsGr2/">
					<xsl:if test="./@rdf:resource">
						<xsl:attribute name="rdf:resource"
							namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <xsl:value-of select="./@rdf:resource" />
                    </xsl:attribute>
					</xsl:if>
					<xsl:if test=".!=''">
						<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
						<xsl:value-of select="." />
					</xsl:if>
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/*/j.0:people.deceased_person.place_of_death">
				<xsl:element name="rdaGr2:placeOfDeath" namespace="http://rdvocab.info/ElementsGr2/">
					<xsl:if test="./@rdf:resource">
						<xsl:attribute name="rdf:resource"
							namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <xsl:value-of select="./@rdf:resource" />
                    </xsl:attribute>
					</xsl:if>
					<xsl:if test=".!=''">
						<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
						<xsl:value-of select="." />
					</xsl:if>
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/rdf:Description/dbpprop:deathPlace">


				<xsl:element name="rdaGr2:placeOfDeath" namespace="http://rdvocab.info/ElementsGr2/">
					<xsl:if test="./@rdf:resource">
						<xsl:attribute name="rdf:resource"
							namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <xsl:value-of select="./@rdf:resource" />
                    </xsl:attribute>
					</xsl:if>
					<xsl:if test=".!=''">
						<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
						<xsl:value-of select="." />
					</xsl:if>
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/*/j.0:people.person.profession">
				<xsl:element name="rdaGr2:professionOrOccupation"
					namespace="http://rdvocab.info/ElementsGr2/">
					<xsl:if test="./@rdf:resource">
						<xsl:attribute name="rdf:resource"
							namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <xsl:value-of select="./@rdf:resource" />
                    </xsl:attribute>
					</xsl:if>
					<xsl:if test=".!=''">
						<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
						<xsl:value-of select="." />
					</xsl:if>
				</xsl:element>
			</xsl:for-each>
			
			<xsl:for-each select="rdf:RDF/*/j.0:people.person.date_of_birth">
				<xsl:element name="edm:begin" namespace="http://www.europeana.eu/schemas/edm/">
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/*/j.0:people.deceased_person.date_of_death">
				<xsl:element name="edm:end" namespace="http://www.europeana.eu/schemas/edm/">
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/rdf:Description/dbpedia-owl:viaf">
				<xsl:element name="dc:identifier" namespace="http://purl.org/dc/elements/1.1/">
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/rdf:Description/dbpprop:viaf">
				<xsl:element name="dc:identifier" namespace="http://purl.org/dc/elements/1.1/">
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/*/j.0:common.topic.topic_equivalent_webpage">
				<xsl:element name="owl:sameAs" namespace="http://www.w3.org/2002/07/owl#">
					<xsl:attribute name="rdf:resource"
						namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <xsl:value-of select="./@rdf:resource" />
                    </xsl:attribute>
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:for-each>

			<xsl:for-each select="rdf:RDF/*/j.0:influence.influence_node">
				<xsl:element name="edm:isRelatedTo" namespace="http://www.europeana.eu/schemas/edm/">
					<xsl:if test="./@rdf:resource">
						<xsl:attribute name="rdf:resource"
							namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <xsl:value-of select="./@rdf:resource" />
                    </xsl:attribute>
					</xsl:if>
					<xsl:if test=".!=''">
						<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
						<xsl:value-of select="." />
					</xsl:if>
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/*/j.0:influence.influence_node.influenced_by">
				<xsl:element name="edm:isRelatedTo" namespace="http://www.europeana.eu/schemas/edm/">
					<xsl:if test="./@rdf:resource">
						<xsl:attribute name="rdf:resource"
							namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <xsl:value-of select="./@rdf:resource" />
                    </xsl:attribute>
					</xsl:if>
					<xsl:if test=".!=''">
						<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
						<xsl:value-of select="." />
					</xsl:if>
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/*/j.0:influence.influence_node.influenced_by">
				<xsl:element name="edm:isRelatedTo" namespace="http://www.europeana.eu/schemas/edm/">
					<xsl:if test="./@rdf:resource">
						<xsl:attribute name="rdf:resource"
							namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <xsl:value-of select="./@rdf:resource" />
                    </xsl:attribute>
					</xsl:if>
					<xsl:if test=".!=''">
						<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
						<xsl:value-of select="." />
					</xsl:if>
				</xsl:element>
			</xsl:for-each>
			<xsl:for-each select="rdf:RDF/rdf:Description/dbpprop:influencedBy">
				<xsl:element name="edm:isRelatedTo" namespace="http://www.europeana.eu/schemas/edm/">
					<xsl:if test="./@rdf:resource">
						<xsl:attribute name="rdf:resource"
							namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <xsl:value-of select="./@rdf:resource" />
                    </xsl:attribute>
					</xsl:if>
					<xsl:if test=".!=''">
						<xsl:attribute name="xml:lang">
                        <xsl:value-of select="./@xml:lang" />
                    </xsl:attribute>
						<xsl:value-of select="." />
					</xsl:if>
				</xsl:element>
			</xsl:for-each>
		</xsl:element>

	</xsl:template>


</xsl:stylesheet>
