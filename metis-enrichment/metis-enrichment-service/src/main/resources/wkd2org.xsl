<?xml version="1.0" encoding="UTF-8"?>
<!--
  Document   : wkd2org.xsl
  Author     : hmanguinhas
  Created on : March 17, 2018
  Updated on : May 30, 2018
-->
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:edm="http://www.europeana.eu/schemas/edm/"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:foaf="http://xmlns.com/foaf/0.1/"
    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    xmlns:owl="http://www.w3.org/2002/07/owl#"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#"
    xmlns:vcard="http://www.w3.org/2006/vcard/ns#"

    xmlns:wdt="http://www.wikidata.org/prop/direct/"
    xmlns:schema="http://schema.org/"

    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:lib="http://localhost.com"

    exclude-result-prefixes="xalan fn xs lib wdt schema dcterms">

    <xsl:output indent="yes" encoding="UTF-8"/>

    <xsl:param name="rdf_about"/>
    <xsl:param name="deref"    />
    <xsl:param name="address"  select="true()"/>
    <xsl:param name="dbpedia"  />

    <xsl:variable name="langs">bg,ca,cs,da,de,el,en,es,et,eu,fi,fr,ga,gd,he,hr,hu,ie,is,it,ka,lt,lv,mk,mt,mul,nl,no,pl,pt,ro,ru,sk,sl,sr,sv,tr,uk,yi,cy,sq,hy,az,be,bs,gl,ja,ar,ko,zh,hi</xsl:variable>

    <xsl:template match="/">
        <xsl:apply-templates select="rdf:RDF"/>
    </xsl:template>

    <xsl:template match="rdf:RDF">
        <rdf:RDF>
            <xsl:choose>
                <xsl:when test="$rdf_about">
                    <xsl:apply-templates select="rdf:Description[@rdf:about=$rdf_about and rdfs:label]"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="rdf:Description"/>
                </xsl:otherwise>
            </xsl:choose>
        </rdf:RDF>
    </xsl:template>

    <xsl:template match="rdf:Description">
        <xsl:variable name="wkdURI" select="string(@rdf:about)"/>

        <foaf:Organization>

            <xsl:copy-of select="@rdf:about"/>

            <xsl:for-each select="rdfs:label">
                <xsl:call-template name="label">
                    <xsl:with-param name="property" select="'skos:prefLabel'"/>
                </xsl:call-template>
            </xsl:for-each>

            <xsl:for-each select="skos:altLabel">
                <xsl:call-template name="label">
                    <xsl:with-param name="property" select="'skos:altLabel'"/>
                </xsl:call-template>
            </xsl:for-each>

            <xsl:for-each select="schema:description">
                <xsl:if test="contains($langs,@xml:lang)">
                    <xsl:element name="dc:description">
                        <xsl:copy-of select="@xml:lang"/>
                        <xsl:value-of select="."/>
                    </xsl:element>
                </xsl:if>
            </xsl:for-each>

            <xsl:for-each select="wdt:P856">
                <xsl:element name="foaf:homepage">
                    <xsl:copy-of select="@rdf:resource"/>
                </xsl:element>
            </xsl:for-each>

            <xsl:for-each select="wdt:P154">
                <xsl:element name="foaf:logo">
                    <xsl:copy-of select="@rdf:resource"/>
                </xsl:element>
            </xsl:for-each>

            <xsl:for-each select="wdt:P968">
                <xsl:element name="foaf:mbox">
                    <xsl:value-of select="replace(@rdf:resource,'mailto:','')"/>
                </xsl:element>
            </xsl:for-each>

            <xsl:for-each select="wdt:P1329">
                <xsl:element name="foaf:phone">
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:for-each>

            <xsl:variable name="country" select="lib:toISO639_2(wdt:P17[1]/@rdf:resource)"/>

            <xsl:if test="$country">
                <xsl:element name="edm:country">
                    <xsl:value-of select="$country"/>
                </xsl:element>
            </xsl:if>

            <!-- Address -->
            <xsl:if test="$address and (($deref and wdt:P669) or wdt:P969 or wdt:P281 or wdt:P2918)">
                <xsl:variable name="countryName"   select="lib:toCountryName(wdt:P17[1]/@rdf:resource)"/>
<!--            <xsl:variable name="locality"      select="wdt:P276[1]/@rdf:resource"/>  -->
                <xsl:variable name="streetAddress" select="wdt:P969[1]/text()"/>
                <xsl:variable name="street"        select="wdt:P669[1]/@rdf:resource"/>
                <xsl:variable name="postal"        select="wdt:P281[1]/text()"/>
                <xsl:variable name="pobox"         select="wdt:P2918[1]/text()"/>

                <xsl:element name="vcard:hasAddress">

                    <xsl:element name="vcard:Address">
                        <xsl:attribute name="rdf:about"><xsl:value-of select="$wkdURI"/>#address</xsl:attribute>

                        <xsl:if test="$countryName">
                            <xsl:element name="vcard:country-name">
                                <xsl:value-of select="$countryName"/>
                            </xsl:element>
                        </xsl:if>

<!-- 
                        <xsl:if test="$deref and $locality">
                            <xsl:variable name="localName" select="lib:getLabel($locality)"/>
                            <xsl:element name="vcard:locality">
                                <xsl:value-of select="$localName"/>
                            </xsl:element>
                        </xsl:if>
 -->

                        <xsl:choose>
                            <xsl:when test="$streetAddress">
                                <xsl:element name="vcard:street-address">
                                    <xsl:value-of select="$streetAddress"/>
                                </xsl:element>
                            </xsl:when>
                            <xsl:when test="$deref and $street">
                                <xsl:variable name="localName" select="lib:getLabel($street)"/>
                                <xsl:element name="vcard:street-address">
                                    <xsl:value-of select="$localName"/>
                                </xsl:element>
                            </xsl:when>
                        </xsl:choose>

                        <xsl:if test="$postal">
                            <xsl:element name="vcard:postal-code">
                                <xsl:value-of select="$postal"/>
                            </xsl:element>
                        </xsl:if>

                        <xsl:if test="$pobox">
                            <xsl:element name="vcard:post-office-box">
                                <xsl:value-of select="$pobox"/>
                            </xsl:element>
                        </xsl:if>

                    </xsl:element>
                </xsl:element>
            </xsl:if>

        <!-- Co-referencing -->

            <!-- ISNI -->
            <xsl:for-each select="wdt:P213">
                <xsl:element name="owl:sameAs">
                    <xsl:attribute name="rdf:resource">http://isni.org/isni/<xsl:value-of select="translate(text(),' ','')"/></xsl:attribute>
                </xsl:element>
            </xsl:for-each>

            <!-- VIAF ID -->
            <xsl:for-each select="wdt:P214">
                <xsl:element name="owl:sameAs">
                    <xsl:attribute name="rdf:resource">http://viaf.org/viaf/<xsl:value-of select="text()"/></xsl:attribute>
                </xsl:element>
            </xsl:for-each>

            <!-- Freebase ID -->
            <xsl:for-each select="wdt:P646">
                <xsl:element name="owl:sameAs">
                    <xsl:attribute name="rdf:resource">https://www.freebase.com<xsl:value-of select="text()"/></xsl:attribute>
                </xsl:element>
                <xsl:element name="owl:sameAs">
                    <xsl:attribute name="rdf:resource">https://g.co/kg<xsl:value-of select="text()"/></xsl:attribute>
                </xsl:element>
            </xsl:for-each>

            <!-- GND ID -->
            <xsl:for-each select="wdt:P227">
                <xsl:element name="owl:sameAs">
                    <xsl:attribute name="rdf:resource">http://d-nb.info/gnd/<xsl:value-of select="text()"/></xsl:attribute>
                </xsl:element>
            </xsl:for-each>

            <!-- BNF Identitier -->
            <xsl:for-each select="wdt:P268">
                <xsl:element name="owl:sameAs">
                    <xsl:attribute name="rdf:resource">http://data.bnf.fr/ark:/12148/cb<xsl:value-of select="text()"/></xsl:attribute>
                </xsl:element>
            </xsl:for-each>

            <!-- LCAuth identifier -->
            <xsl:for-each select="wdt:P244">
                <xsl:element name="owl:sameAs">
                    <xsl:attribute name="rdf:resource">http://id.loc.gov/authorities/names/<xsl:value-of select="text()"/></xsl:attribute>
                </xsl:element>
            </xsl:for-each>

            <!-- ULAN identifier -->
            <xsl:for-each select="wdt:P245">
                <xsl:element name="owl:sameAs">
                    <xsl:attribute name="rdf:resource">http://vocab.getty.edu/ulan/<xsl:value-of select="text()"/></xsl:attribute>
                </xsl:element>
            </xsl:for-each>

            <!-- Geonames ID -->
            <xsl:for-each select="wdt:P1566">
                <xsl:element name="owl:sameAs">
                    <xsl:attribute name="rdf:resource">http://sws.geonames.org/<xsl:value-of select="text()"/>/</xsl:attribute>
                </xsl:element>
            </xsl:for-each>

            <!-- BabelNet ID -->
            <xsl:for-each select="wdt:P2581">
                <xsl:element name="owl:sameAs">
                    <xsl:attribute name="rdf:resource">http://babelnet.org/rdf/s<xsl:value-of select="text()"/></xsl:attribute>
                </xsl:element>
            </xsl:for-each>

            <!-- BNE ID -->
            <xsl:for-each select="wdt:P950">
                <xsl:element name="owl:sameAs">
                    <xsl:attribute name="rdf:resource">http://datos.bne.es/resource/<xsl:value-of select="text()"/></xsl:attribute>
                </xsl:element>
            </xsl:for-each>

            <!-- Nomisma ID -->
            <xsl:for-each select="wdt:P2950">
                <xsl:element name="owl:sameAs">
                    <xsl:attribute name="rdf:resource">http://nomisma.org/id/<xsl:value-of select="text()"/></xsl:attribute>
                </xsl:element>
            </xsl:for-each>

            <!-- DBpedia Language Editions -->

            <xsl:if test="$dbpedia">
                <xsl:for-each select="/rdf:RDF/rdf:Description[schema:about/@rdf:resource=$wkdURI]">
                    <xsl:if test="fn:matches(@rdf:about, 'https:[/][/]([a-z-]+).wikipedia.org[/]wiki[/](.*)')">
                        <xsl:variable name="suffix" select="substring-after(@rdf:about,'.wikipedia.org/wiki/')"/>
                        <xsl:variable name="prefix" select="replace(replace(substring-before(@rdf:about,'wikipedia.org/wiki/'),'en.',''),'https','http')"/>
                        <xsl:variable name="iri"    select="concat($prefix,'dbpedia.org/resource/',replace($suffix,'%20+','_'))"/>
                        <xsl:element name="owl:sameAs">
                            <xsl:attribute name="rdf:resource"><xsl:value-of select="$iri"/></xsl:attribute>
                        </xsl:element>
                    </xsl:if>
                </xsl:for-each>
            </xsl:if>
 
        </foaf:Organization>

    </xsl:template>

    <xsl:template name="label">
        <xsl:param name="property"/>

        <xsl:choose>
            <xsl:when test="not(contains($langs,@xml:lang))"/>
            <xsl:when test="fn:matches(string(.),'^\s*([A-Z]+[.]*)+\s*$')">
                <xsl:element name="edm:acronym">
                    <xsl:copy-of select="@xml:lang"/>
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="{$property}">
                    <xsl:copy-of select="@xml:lang"/>
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:function name="lib:getLabel" as="xs:string">
        <xsl:param name="uri"/>

        <xsl:variable name="name" select="substring-after($uri,'http://www.wikidata.org/entity/')"/>
        <xsl:variable name="url">https://www.wikidata.org/wiki/Special:EntityData/<xsl:value-of select="$name"/>.rdf</xsl:variable>

        <xsl:variable name="doc"      select="document($url)"/>
        <xsl:variable name="labels"   select="$doc/rdf:RDF/rdf:Description[@rdf:about=$uri]/rdfs:label"/>
        <xsl:variable name="label_en" select="$labels[@xml:lang='en']"/>

        <xsl:choose>
            <xsl:when test="$label_en">
                <xsl:value-of select="$label_en/text()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$labels[1]/text()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!-- Country Codes -->
    <xsl:function name="lib:toISO639_2" as="xs:string">
        <xsl:param name="uri"/>
        <xsl:choose>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q37'">LT</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q222'">AL</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q750'">BO</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q184'">BY</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q2895'">BY</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q16'">CA</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q39'">CH</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q148'">CN</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q29'">ES</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q145'">GB</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q230'">GE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q27'">IE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q189'">IS</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q221'">MK</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q96'">MX</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q36'">PL</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1050'">SZ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q212'">UA</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q228'">AD</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q878'">AE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q889'">AF</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q781'">AG</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q25227'">AN</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q227'">AZ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q225'">BA</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q244'">BB</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q398'">BH</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q974'">CD</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1009'">CM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q736'">EC</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q734'">GY</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q783'">HN</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q232'">KZ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q819'">LA</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1028'">MA</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q236'">ME</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q912'">ML</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q13353'">MS</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q826'">MV</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1029'">MZ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q811'">NI</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q697'">NR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q419'">PE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q928'">PH</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1183'">PR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1042'">SC</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1049'">SD</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q34'">SE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q858'">SY</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q657'">TD</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q865'">TW</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q237'">VA</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q686'">VU</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q805'">YE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q916'">AO</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q414'">AR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q40'">AT</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q800'">CR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q191'">EE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q79'">EG</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q986'">ER</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q33'">FI</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q3769'">GF</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q41'">GR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q8646'">HK</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1019'">MG</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q14773'">MO</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q833'">MY</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1030'">NA</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q31057'">NF</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q804'">PA</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q35672'">PN</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q218'">RO</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q159'">RU</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q215'">SI</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1041'">SN</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q678'">TO</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q43'">TR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q672'">TV</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q16645'">UM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q35555'">WF</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q21590062'">AQ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q16641'">AS</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q902'">BD</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q967'">BI</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q23635'">BM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q155'">BR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q917'">BT</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q242'">BZ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q298'">CL</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q784'">DM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q17012'">GP</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q983'">GQ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q16635'">GU</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q423'">KP</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q347'">LI</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q37'">LT</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q32'">LU</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q709'">MH</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q20'">NO</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q691'">PG</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q408'">AU</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q929'">CF</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q971'">CG</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1008'">CI</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q241'">CU</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1011'">CV</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q668'">IN</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q794'">IR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q785'">JE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q17'">JP</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q854'">LK</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1013'">LS</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q16644'">MP</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q34020'">NU</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q334'">SG</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q265'">UZ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1246'">XK</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q262'">DZ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1410'">GI</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q945'">TG</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q23408'">BV</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q83286'">YU</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q838261'">YU</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q192184'">SH</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q863'">TJ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q30'">US</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q16957'">DD</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q790'">HT</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q695'">PW</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1036'">UG</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q219060'">PS</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q407199'">PS</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q766'">JM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q842829'">SJ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q5785'">KY</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q252'">ID</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q817'">KW</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q18221'">TC</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q25230'">GG</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q965'">BF</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q712'">FJ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q28'">HU</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1014'">LR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1025'">MR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1033'">NG</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q55'">NL</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q29999'">NL</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q27561'">BQ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q165783'">BQ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q31063'">CX</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q846'">QA</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q30971'">PF</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q17063'">YT</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q38'">IT</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q115'">ET</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q117'">GH</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q733'">PY</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q954'">ZW</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q33788'">NC</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q796'">IQ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q683'">WS</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q17070'">RE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q11703'">VI</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q739'">CO</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q810'">JO</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q114'">KE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q884'">KR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1045'">SO</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q36823'">TK</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q574'">TL</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q757'">VC</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q25305'">VG</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q921'">BN</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q37024'">CS</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q213'">CZ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q702'">FM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q43448'">IO</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q760'">LC</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q711'">MN</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q233'">MT</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q664'">NZ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q874'">TM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q717'">VE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q778'">BS</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q822'">LB</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q235'">MC</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q219'">BG</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q45'">PT</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q851'">SA</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q142'">FR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q801'">IL</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q126125'">MF</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q258'">ZA</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q962'">BJ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q786'">DO</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q958'">SS</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q792'">SV</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q9648'">FK</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1249802'">FK</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1027'">MU</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q948'">TN</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q953'">ZM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q211'">LV</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q685'">SB</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q25362'">BL</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q25279'">CW</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1005'">GM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q9676'">IM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q403'">RS</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q214'">SK</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1044'">SL</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q35'">DK</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q756617'">DK</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q217'">MD</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q924'">TZ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q183'">DE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q869'">TH</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q763'">KN</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1006'">GN</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q131198'">HM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q730'">SR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1000'">GA</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q223'">GL</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q35086'">GS</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q881'">VN</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q21203'">AW</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1016'">LY</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q843'">PK</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q129003'">TF</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1007'">GW</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q34617'">PM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q399'">AM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q710'">KI</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q26273'">SX</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q229'">CY</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q970'">KM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q769'">GD</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q36004'">CC</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q754'">TT</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q424'">KH</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q25228'">AI</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q5689'">AX</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1032'">NE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q6250'">EH</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1039'">ST</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q224'">HR</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1020'">MW</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q17054'">MQ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q813'">KG</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q26988'">CK</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q4628'">FO</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q842'">OM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q977'">DJ</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q836'">MM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q774'">GT</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q31'">BE</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q238'">SM</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q77'">UY</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q837'">NP</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q963'">BW</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1037'">RW</xsl:when>
            <xsl:otherwise><xsl:value-of select="''"/></xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!-- Country Names -->
    <xsl:function name="lib:toCountryName" as="xs:string">
        <xsl:param name="uri"/>
        <xsl:choose>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q17'">Japan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q16'">Canada</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q31'">Belgium</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q20'">Norway</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q28'">Hungary</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q35'">Denmark</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q27'">Ireland</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q29'">Spain</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q45'">Portugal</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q77'">Uruguay</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q114'">Kenya</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q32'">Luxembourg</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q30'">United States of America</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q33'">Finland</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q38'">Italy</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q183'">Germany</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q36'">Poland</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q142'">France</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q55'">Netherlands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q37'">Lithuania</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q213'">Czech Republic</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q34'">Sweden</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q211'">Latvia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q40'">Austria</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q39'">Switzerland</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q214'">Slovakia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q41'">Greece</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q117'">Ghana</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q43'">Turkey</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q115'">Ethiopia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q219'">Bulgaria</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q217'">Moldova</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q241'">Cuba</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q223'">Greenland</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q79'">Egypt</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q96'">Mexico</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q252'">Indonesia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q224'">Croatia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q262'">Algeria</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q145'">United Kingdom</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q265'">Uzbekistan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q148'">People's Republic of China</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q229'">Cyprus</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q233'">Malta</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q155'">Brazil</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q334'">Singapore</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q159'">Russia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q235'">Monaco</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q408'">Australia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q238'">San Marino</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q668'">India</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q184'">Belarus</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q683'">Samoa</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q258'">South Africa</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q189'">Iceland</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q695'">Palau</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q191'">Estonia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q712'">Fiji</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q399'">Armenia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q733'">Paraguay</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q403'">Serbia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q212'">Ukraine</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q766'">Jamaica</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q424'">Cambodia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q785'">Jersey</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q215'">Slovenia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q574'">East Timor</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q790'">Haiti</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q664'">New Zealand</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q218'">Romania</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q794'">Iran</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q685'">Solomon Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q702'">Federated States of Micronesia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q221'">Republic of Macedonia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q710'">Kiribati</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q796'">Iraq</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q711'">Mongolia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q222'">Albania</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q817'">Kuwait</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q225'">Bosnia and Herzegovina</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q846'">Qatar</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q717'">Venezuela</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q227'">Azerbaijan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q730'">Suriname</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q228'">Andorra</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q854'">Sri Lanka</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q739'">Colombia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q230'">Georgia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q754'">Trinidad and Tobago</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q757'">Saint Vincent and the Grenadines</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q863'">Tajikistan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q232'">Kazakhstan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q236'">Montenegro</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q763'">Saint Kitts and Nevis</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q760'">Saint Lucia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q929'">Central African Republic</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q237'">Vatican City</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q769'">Grenada</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q954'">Zimbabwe</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q242'">Belize</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q945'">Togo</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q778'">The Bahamas</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q298'">Chile</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q774'">Guatemala</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q244'">Barbados</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q965'">Burkina Faso</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1008'">Ivory Coast</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q347'">Liechtenstein</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q398'">Bahrain</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q792'">El Salvador</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q419'">Peru</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q810'">Jordan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1011'">Cape Verde</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q971'">Republic of the Congo</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1013'">Lesotho</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q801'">Israel</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q786'">Dominican Republic</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q813'">Kyrgyzstan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q414'">Argentina</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q837'">Nepal</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q842'">Oman</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q836'">Myanmar</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q843'">Pakistan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q822'">Lebanon</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q672'">Tuvalu</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1033'">Nigeria</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1410'">Gibraltar</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q686'">Vanuatu</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q423'">North Korea</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q678'">Tonga</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q691'">Papua New Guinea</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1014'">Liberia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q851'">Saudi Arabia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q16957'">East Germany</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1036'">Uganda</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1246'">Kosovo</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1025'">Mauritania</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q869'">Thailand</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q657'">Chad</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q16644'">Northern Mariana Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q734'">Guyana</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q709'">Marshall Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q697'">Nauru</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q18221'">Turks and Caicos Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q881'">Vietnam</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q5785'">Cayman Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q736'">Ecuador</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q781'">Antigua and Barbuda</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q874'">Turkmenistan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q783'">Honduras</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q948'">Tunisia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q17063'">Mayotte</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q804'">Panama</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q921'">Brunei</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q784'">Dominica</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q750'">Bolivia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q953'">Zambia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q25230'">Guernsey</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q31063'">Christmas Island</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q27561'">Caribbean Netherlands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q30971'">French Polynesia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q33788'">New Caledonia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q884'">South Korea</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q805'">Yemen</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q29999'">Kingdom of the Netherlands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q819'">Laos</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q34020'">Niue</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q800'">Costa Rica</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q83286'">Socialist Federal Republic of Yugoslavia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q165783'">British Antarctic Territory</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q963'">Botswana</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q924'">Tanzania</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q23408'">Bouvet Island</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q811'">Nicaragua</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q958'">South Sudan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q962'">Benin</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q842829'">Svalbard and Jan Mayen</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q826'">Maldives</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q407199'">Palestinian territories</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q838261'">Federal Republic of Yugoslavia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q970'">Comoros</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q192184'">"Saint Helena, Ascension and Tristan da Cunha"</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1000'">Gabon</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q865'">Taiwan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q219060'">State of Palestine</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1005'">The Gambia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q858'">Syria</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q833'">Malaysia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1006'">Guinea</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q977'">Djibouti</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q889'">Afghanistan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q878'">United Arab Emirates</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1007'">Guinea-Bissau</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1016'">Libya</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q902'">Bangladesh</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q916'">Angola</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q912'">Mali</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1027'">Mauritius</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1020'">Malawi</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q917'">Bhutan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1032'">Niger</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q928'">Philippines</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1037'">Rwanda</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q967'">Burundi</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1039'">São Tomé and Príncipe</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1044'">Sierra Leone</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q974'">Democratic Republic of the Congo</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1045'">Somalia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q983'">Equatorial Guinea</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q986'">Eritrea</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q4628'">Faroe Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q5689'">Åland Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1009'">Cameroon</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q6250'">Western Sahara</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1019'">Madagascar</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q9648'">Falkland Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1028'">Morocco</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q9676'">Isle of Man</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q11703'">United States Virgin Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1029'">Mozambique</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1030'">Namibia</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q17054'">Martinique</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q17070'">Réunion</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1041'">Senegal</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1042'">Seychelles</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q21203'">Aruba</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1049'">Sudan</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q25279'">Curaçao</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1050'">Swaziland</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q25305'">British Virgin Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q25228'">Anguilla</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q25362'">Saint-Barthélemy</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q26273'">Sint Maarten</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1183'">Puerto Rico</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q26988'">Cook Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q3769'">French Guiana</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q2895'">Byelorussian Soviet Socialist Republic</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q34617'">Saint Pierre and Miquelon</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q35086'">South Georgia and the South Sandwich Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q36004'">Cocos (Keeling) Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q8646'">Hong Kong</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q36823'">Tokelau</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q37024'">Serbia and Montenegro</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q13353'">Montserrat</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q14773'">Macau</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q43448'">British Indian Ocean Territory</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q126125'">Saint Martin</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q129003'">French Southern and Antarctic Lands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q16635'">Guam</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q131198'">Heard Island and McDonald Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q756617'">Kingdom of Denmark</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q1249802'">Falkland Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q16641'">American Samoa</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q17012'">Guadeloupe</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q16645'">United States Minor Outlying Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q23635'">Bermuda</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q25227'">Netherlands Antilles</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q31057'">Norfolk Island</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q35555'">Wallis and Futuna</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q35672'">Pitcairn Islands</xsl:when>
            <xsl:when test="$uri='http://www.wikidata.org/entity/Q21590062'">Antarctic Treaty area</xsl:when>
            <xsl:otherwise><xsl:value-of select="''"/></xsl:otherwise>
        </xsl:choose>
    </xsl:function>

</xsl:stylesheet>