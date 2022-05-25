<?xml version="1.0" encoding="UTF-8"?>

<!--
  Document   : wikidata.xsl
  Author     : hmanguinhas
  Created on : October 13, 2019
  Updated on : March 16, 2021

  https://github.com/europeana/metis-vocabularies/blob/develop/src/main/resources/vocabularies/wikidata/wikidata.xsl
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
  xmlns:rdaGr2="http://rdvocab.info/ElementsGr2/"
  xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#"

  xmlns:fn="http://www.w3.org/2005/xpath-functions"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"

  xmlns:wdt="http://www.wikidata.org/prop/direct/"
  xmlns:p="http://www.wikidata.org/prop/"
  xmlns:ps="http://www.wikidata.org/prop/statement/"
  xmlns:schema="http://schema.org/"

  xmlns:lib="http://example.org/lib"

  exclude-result-prefixes="wdt schema lib xs fn">

  <xsl:output indent="yes" encoding="UTF-8"/>

  <xsl:param name="targetId"/>
  <xsl:param name="coref" select="true()"/>

  <xsl:variable name="namespace" select="'http://www.wikidata.org/prop/direct/'"/>
  <xsl:variable name="wiki"      select="'https://en.wikipedia.org/wiki/'"/>
  <xsl:variable name="dbp"       select="'http://dbpedia.org/resource/'"/>

  <!-- Portal languages (27) -->
  <xsl:variable name="langs">en,pl,de,nl,fr,it,da,sv,el,fi,hu,cs,sl,et,pt,es,lt,lv,bg,ro,sk,hr,ga,mt,no,ca,ru</xsl:variable>

  <!-- Co-reference mapping table -->
  <xsl:variable name="map">
    <entry key="P214">http://viaf.org/viaf/$1</entry>
    <entry key="P227">http://d-nb.info/gnd/$1</entry>
    <entry key="P244">http://id.loc.gov/authorities/names/$1</entry>
    <entry key="P245">http://vocab.getty.edu/ulan/$1</entry>
    <entry key="P268">http://data.bnf.fr/ark:/12148/cb$1</entry>
    <entry key="P269">http://www.idref.fr/$1/id</entry>
    <entry key="P349">http://id.ndl.go.jp/auth/ndlna/$1</entry>
    <entry key="P486">http://id.nlm.nih.gov/mesh/$1</entry>
    <entry key="P508">http://purl.org/bncf/tid/$1</entry>
    <entry key="P646">https://www.freebase.com$1</entry>
    <entry key="P646">https://g.co/kg$1</entry>
    <entry key="P648">http://openlibrary.org/works/$1</entry>
    <entry key="P672">http://id.nlm.nih.gov/mesh/$1</entry>
    <entry key="P906">http://libris.kb.se/resource/auth/$1</entry>
    <entry key="P950">http://datos.bne.es/resource/$1</entry>
    <entry key="P1006">http://data.bibliotheken.nl/id/thes/p$1</entry>
    <entry key="P1014">http://vocab.getty.edu/aat/$1</entry>
    <entry key="P1015">https://livedata.bibsys.no/authority/$1</entry>
    <entry key="P1036">http://dewey.info/class/$1/</entry>
    <entry key="P1256">http://iconclass.org/$1</entry>
    <entry key="P1260">http://kulturarvsdata.se/$1</entry>
    <entry key="P1422">http://ta.sandrart.net/-person-$1</entry>
    <entry key="P1566">https://sws.geonames.org/$1/</entry>
    <entry key="P1584">http://pleiades.stoa.org/places/$1/rdf</entry>
    <entry key="P1667">http://vocab.getty.edu/tgn/$1</entry>
    <entry key="P1802">urn:uuid:$1</entry>
    <entry key="P1936">http://dare.ht.lu.se/places/$1</entry>
    <entry key="P2163">http://id.worldcat.org/fast/$1</entry>
    <entry key="P2347">http://www.yso.fi/onto/yso/p$1</entry>
    <entry key="P2452">http://www.geonames.org/ontology#$1</entry>
    <entry key="P2581">http://babelnet.org/rdf/s$1</entry>
    <entry key="P2671">http://g.co/kg$1</entry>
    <entry key="P2799">http://data.cervantesvirtual.com/person/$1</entry>
    <entry key="P2950">http://nomisma.org/id/$1</entry>
    <entry key="P3120">http://data.ordnancesurvey.co.uk/id/$1</entry>
    <entry key="P3348">http://nlg.okfn.gr/resource/authority/record$1</entry>
    <entry key="P3832">http://thesaurus.europeanafashion.eu/thesaurus/$1</entry>
    <entry key="P3911">http://zbw.eu/stw/descriptor/$1</entry>
    <entry key="P3916">http://vocabularies.unesco.org/thesaurus/$1</entry>
    <entry key="P4104">http://data.carnegiehall.org/names/$1</entry>
    <entry key="P4307">https://id.erfgoed.net/thesauri/erfgoedtypes/$1</entry>
    <entry key="P4953">http://id.loc.gov/authorities/genreForms/$1</entry>
    <entry key="P5034">http://lod.nl.go.kr/resource/$1</entry>
    <entry key="P5429">http://cv.iptc.org/newscodes/$1</entry>
    <entry key="P5587">https://libris.kb.se/$1</entry>
    <entry key="P5748">http://uri.gbv.de/terminology/bk/$1</entry>
    <entry key="P6293">http://www.yso.fi/onto/ysa/$1</entry>
  </xsl:variable>

  <xsl:variable name="articles"
    select="/rdf:RDF/rdf:Description[rdf:type/@rdf:resource='http://schema.org/Article']"/>

  <xsl:variable name="statements"
    select="/rdf:RDF/rdf:Description[rdf:type/@rdf:resource='http://wikiba.se/ontology#Statement']"/>



  <!--+++++++++++++++++++++++++++++++ RULES +++++++++++++++++++++++++++++++-->

  <xsl:template match="/">
    <xsl:apply-templates select="rdf:RDF"/>
  </xsl:template>

  <xsl:template match="rdf:RDF">

    <xsl:variable name="props" select="rdf:Description[@rdf:about=$targetId]/*"/>

    <xsl:if test="$props">
      <xsl:variable name="entity">
        <xsl:choose>
          <!--
          <xsl:when test="$props[name()='wdt:P31']/@rdf:resource = 'http://www.wikidata.org/entity/Q17362920'">
          </xsl:when>
           -->
          <xsl:when test="$props[name()='wdt:P31']/@rdf:resource = 'http://www.wikidata.org/entity/Q22808320'">
          </xsl:when>
          <xsl:when test="$props[name()='wdt:P31']/@rdf:resource = 'http://www.wikidata.org/entity/Q4167410'">
          </xsl:when>
          <xsl:when test="$props[name()!='owl:sameAs']">
            <rdf:Description>
              <xsl:attribute name="rdf:about" select="$targetId"/>
              <xsl:copy-of select="$props"/>
            </rdf:Description>
          </xsl:when>
          <xsl:otherwise>
            <!-- Redirection case -->
            <xsl:variable name="newId" select="$props[name()='owl:sameAs']/@rdf:resource"/>
            <rdf:Description>
              <xsl:attribute name="rdf:about" select="$newId"/>
              <xsl:copy-of select="rdf:Description[@rdf:about=$newId]/*"/>
              <xsl:element name="owl:sameAs">
                <xsl:attribute name="rdf:resource"><xsl:value-of select="$targetId"/></xsl:attribute>
              </xsl:element>
            </rdf:Description>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:for-each select="$entity/rdf:Description">
        <xsl:call-template name="Entity"/>
      </xsl:for-each>
    </xsl:if>

  </xsl:template>

  <xsl:template name="Entity">

    <!--  for convenience, readability and performance -->
    <!--  also includes statement assertions (ie. crucial for "Ancient Rome" entity) -->
    <!--  could be improved to use a set of strings and containment functions -->
    <xsl:variable name="instanceOf" select="wdt:P31 | $statements/ps:P31"/>

    <xsl:choose>

      <!-- To avoid mapping Wikidata Properties by default to Concepts -->

      <xsl:when test="count(*[namespace-uri()!='http://www.wikidata.org/prop/'
                                    and name()!='rdf:type'])=0">
      </xsl:when>

      <!-- Concepts (explicit conditions) -->

      <!-- instance of: music genre (Q188451), ancient civilisation (Q28171280) -->
      <xsl:when test="$instanceOf[@rdf:resource='http://www.wikidata.org/entity/Q188451'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q28171280']">
        <xsl:call-template name="Concept"/>
      </xsl:when>

      <!-- Places -->

      <!-- instance of: human settlement (Q486972), country (Q6256)
                      , historical country (Q3024240), City (Q515)
                      , third-level administrative country subdivision (Q13221722)
                      , municipal district (Q2198484), amt (Q478847), castle (Q23413)
                      , monument (Q4989906), royal palace (Q53536964), château (Q751876)
                      , fortress (Q57831), fortification (Q57821)
                      , abbey (Q160742), excavation (Q959782)
                      , memorial (Q6642119), war memorial (Q575759)
                      , archaeological site (Q839954) -->
      <xsl:when test="$instanceOf[@rdf:resource='http://www.wikidata.org/entity/Q486972'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q6256'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q3024240'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q515'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q13221722'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q2198484'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q478847'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q23413'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q4989906'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q53536964'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q751876'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q57831'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q57821'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q160742'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q959782'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q6642119'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q575759'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q839954'
                                     ]">
        <xsl:call-template name="Place"/>
      </xsl:when>

      <!-- containing property: P1082 (population), P2046 (area)
                              , P4777 (has boundary), capital of (P1376) -->
      <xsl:when test="wdt:P1082 | wdt:P2046 | wdt:P4777 | wdt:P1376">
        <xsl:call-template name="Place"/>
      </xsl:when>

      <!-- Agents: Individuals (real and fictional) -->

      <!-- instance of: Human (Q5), Fictional Human (Q15632617)
                      , Fictional Character (Q95074), Comics character (Q1114461)
                      , Pen name (Q127843), Heteronym (Q1136342)
                      , literary character (Q3658341) -->
      <xsl:when test="$instanceOf[@rdf:resource='http://www.wikidata.org/entity/Q5'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q15632617'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q95074'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q1114461'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q127843'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q1136342'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q3658341'
                                     ]">
        <xsl:call-template name="AgentIndividual"/>
      </xsl:when>

      <!-- containing property: P463 (member of)
                              applies to Agent and Place
                               -->
      <xsl:when test="wdt:P463">
        <xsl:call-template name="AgentIndividual"/>
      </xsl:when>


      <!-- Agents: Organisations/Groups -->

      <!-- containing property: P159 (headquarters location)
                              , P749 (parent organisation), P1128 (employees) -->
      <xsl:when test="wdt:P159 | wdt:P749 | wdt:P1128">
        <xsl:call-template name="AgentOrganization"/>
      </xsl:when>

      <!-- instance of: Fashion label (Q1618899), Fashion House (Q3661311)
                      , Musical Group (Q215380), Musical Duo (Q9212979)
                      , Group of Humans (Q16334295), Duo (Q10648343)
                      , Musical Ensemble (Q2088357), brass ensemble (Q883323)
                      , quartet (Q1135557), octet (Q99252497)
                      , group (Q16887380), art group (Q4502119)
                      , sibling group (Q16979650), sibling duo (Q14073567)
                      , girl group (Q641066)
                      , vocal group (Q120544), rock group (Q5741069), heavy metal band (Q56816954)
                      , family name (Q101352), musical family (Q78425721)
                      , family of artists (Q1292111), brand (Q431289)
                      , string quartet (Q207338), family (Q8436), noble family (Q13417114)
                       -->
      <xsl:when test="$instanceOf[@rdf:resource='http://www.wikidata.org/entity/Q1618899'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q3661311'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q215380'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q9212979'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q16334295'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q10648343'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q2088357'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q883323'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q1135557'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q99252497'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q16887380'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q4502119'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q16979650'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q14073567'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q641066'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q120544'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q5741069'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q56816954'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q101352'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q78425721'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q431289'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q1292111'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q207338'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q8436'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q13417114'
                                     ]">
        <xsl:call-template name="AgentOrganization"/>
      </xsl:when>

      <!-- Agents: Organisations -->

      <!-- instance of: art gallery (Q1007870), art museum (Q207694), museum (Q33506)
                      , library (Q7075), academic library (Q856234), archive (Q166118)
                      , organisation (Q43229)
                      , non-profit organisation (Q163740)
                      , foundation (Q157031), art collective (Q1400264)
                      , art colony (Q726483), arts association (Q281644)
                      , art project (Q21076217), photographic studio (Q672070)
                      , university (Q3918), school of thought (Q1387659)
                      , collective (Q13473501), house name (Q19913602)
                      , business (Q4830453), bell museum (Q26944983)
                      , cooperative (Q4539), film organization (Q104649845)
                       -->
      <xsl:when test="$instanceOf[@rdf:resource='http://www.wikidata.org/entity/Q1007870'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q207694'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q33506'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q7075'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q856234'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q166118'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q43229'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q163740'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q157031'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q1400264'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q726483'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q281644'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q21076217'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q672070'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q3918'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q1387659'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q13473501'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q19913602'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q4830453'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q26944983'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q4539'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q104649845'
                                     ]">
        <xsl:call-template name="AgentOrganization"/>
      </xsl:when>

      <!-- Agents: conditions applicable to both individuals and groups -->

      <!--  field of work (P101) and genre (P136) is applicable for agents and topics -->
      <!-- containing property: occupation (P106),
                              , work period (P2031)
                               -->
      <xsl:when test="wdt:P106 | wdt:P2031">
        <xsl:choose>
          <xsl:when test="wdt:P571 | wdt:P576">
            <xsl:call-template name="AgentOrganization"/>
          </xsl:when>
          <xsl:when test="wdt:P569 | wdt:P570 | wdt:P21 | wdt:P19 | wdt:P20">
            <xsl:call-template name="AgentIndividual"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="AgentIndividual"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>


      <!-- TimeSpan -->

      <!-- instance of: century, millennium, archaeological period, time interval, era -->
      <!--
      <xsl:when test="$instanceOf[@rdf:resource='http://www.wikidata.org/entity/Q578'
                               or @rdf:resource='http://www.wikidata.org/entity/Q36507'
                               or @rdf:resource='http://www.wikidata.org/entity/Q15401633'
                               or @rdf:resource='http://www.wikidata.org/entity/Q186081'
                               or @rdf:resource='http://www.wikidata.org/entity/Q6428674']">
       -->
      <xsl:when test="$instanceOf[@rdf:resource='http://www.wikidata.org/entity/Q578'
                                     or @rdf:resource='http://www.wikidata.org/entity/Q36507']">
        <xsl:call-template name="TimeSpan"/>
      </xsl:when>

      <!-- instance of: historical period BUT WITH EXCEPTIONS -->
      <!--
      <xsl:when test="$instanceOf[@rdf:resource='http://www.wikidata.org/entity/Q11514315'] and not
                      ($instanceOf[@rdf:resource='http://www.wikidata.org/entity/Q968159'])">
          <xsl:call-template name="TimeSpan"/>
      </xsl:when>
       -->


      <!-- Places (less strong conditions) -->

      <!-- containing property: P1566 (GeoNames ID)
                              , P625 (coordinate location) -->
      <xsl:when test="wdt:P1566 | wdt:P625">
        <xsl:call-template name="Place"/>
      </xsl:when>

      <!-- instance of: building (Q7736711) -->
      <xsl:when test="$instanceOf[@rdf:resource='http://www.wikidata.org/entity/Q7736711'
                                     ]">
        <xsl:call-template name="Place"/>
      </xsl:when>

      <!-- Concepts -->

      <!-- everything else is mapped to concept -->
      <xsl:otherwise>
        <xsl:call-template name="Concept"/>
      </xsl:otherwise>

    </xsl:choose>

  </xsl:template>


  <!-- MAPPINGS FOR EACH CONTEXTUAL ENTITY -->

  <xsl:template name="AgentOrganization">
    <edm:Agent>
      <xsl:copy-of select="@rdf:about"/>

      <!-- labels -->
      <xsl:call-template name="labels">
        <xsl:with-param name="alt" select="skos:altLabel"/>
      </xsl:call-template>

      <!-- descriptions -->
      <xsl:for-each select="schema:description">
        <xsl:if test="lib:isAcceptableLang(@xml:lang) and lib:isAcceptableLabel(.)">
          <xsl:call-template name="LangLiteral">
            <xsl:with-param name="prop" select="'skos:note'"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:for-each>

      <!-- dates -->
      <xsl:for-each select="wdt:P571">
        <xsl:call-template name="Literal">
          <xsl:with-param name="prop" select="'rdaGr2:dateOfEstablishment'"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="wdt:P576">
        <xsl:call-template name="Literal">
          <xsl:with-param name="prop" select="'rdaGr2:dateOfTermination'"/>
        </xsl:call-template>
      </xsl:for-each>

      <!-- relationships -->
      <xsl:for-each select="wdt:P361 | wdt:P463">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'dcterms:isPartOf'"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="wdt:P527">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'dcterms:hasPart'"/>
        </xsl:call-template>
      </xsl:for-each>

      <!-- Co-referencing -->
      <xsl:if test="$coref">
        <xsl:for-each select="owl:sameAs | wdt:P1709 | wdt:P2888">
          <xsl:element name="owl:sameAs">
            <xsl:copy-of select="@rdf:resource"/>
          </xsl:element>
        </xsl:for-each>
        <xsl:call-template name="coref">
          <xsl:with-param name="current" select="."/>
          <xsl:with-param name="target" select="'owl:sameAs'"/>
        </xsl:call-template>
      </xsl:if>

      <xsl:call-template name="DBpedia">
        <xsl:with-param name="uri" select="@rdf:about"/>
      </xsl:call-template>

    </edm:Agent>
  </xsl:template>

  <xsl:template name="AgentIndividual">
    <edm:Agent>
      <xsl:copy-of select="@rdf:about"/>

      <!-- labels -->
      <xsl:call-template name="labels">
        <xsl:with-param name="alt" select="skos:altLabel | wdt:P742
                                                 | wdt:P1477 | wdt:P2562"/>
      </xsl:call-template>

      <!-- description -->
      <xsl:for-each select="schema:description">
        <xsl:if test="lib:isAcceptableLang(@xml:lang) and lib:isAcceptableLabel(.)">
          <xsl:call-template name="LangLiteral">
            <xsl:with-param name="prop" select="'skos:note'"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:for-each>

      <!-- gender -->
      <!--
                  <xsl:for-each select="wdt:P21">
                      <xsl:choose>
                          <xsl:when test="@rdf:resource='http://www.wikidata.org/entity/Q6581097'">
                              <xsl:element name="rdaGr2:gender" xml:lang="en">Male</xsl:element>
                          </xsl:when>
                          <xsl:when test="@rdf:resource='http://www.wikidata.org/entity/Q6581072'">
                              <xsl:element name="rdaGr2:gender" xml:lang="en">Female</xsl:element>
                          </xsl:when>
                          <xsl:when test="@rdf:resource='http://www.wikidata.org/entity/Q1052281'">
                              <xsl:element name="rdaGr2:gender" xml:lang="en">Transgender woman</xsl:element>
                          </xsl:when>
                          <xsl:when test="@rdf:resource='http://www.wikidata.org/entity/Q1097630'">
                              <xsl:element name="rdaGr2:gender" xml:lang="en">Intersex</xsl:element>
                          </xsl:when>
                      </xsl:choose>
                  </xsl:for-each>
       -->

      <!-- places -->
      <xsl:for-each select="wdt:P19">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'rdaGr2:placeOfBirth'"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="wdt:P20">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'rdaGr2:placeOfDeath'"/>
        </xsl:call-template>
      </xsl:for-each>

      <!-- dates -->

      <xsl:for-each select="wdt:P569">
        <xsl:call-template name="Literal">
          <xsl:with-param name="prop" select="'rdaGr2:dateOfBirth'"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="wdt:P570">
        <xsl:call-template name="Literal">
          <xsl:with-param name="prop" select="'rdaGr2:dateOfDeath'"/>
        </xsl:call-template>
      </xsl:for-each>

      <!-- professions or occupations -->

      <xsl:for-each select="wdt:P39 | wdt:P97 | wdt:P101 | wdt:P106">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'rdaGr2:professionOrOccupation'"/>
        </xsl:call-template>
      </xsl:for-each>

      <!-- family relationships -->

      <xsl:for-each select="wdt:P7 | wdt:P9 | wdt:P22 | wdt:P25 | wdt:P26
                                | wdt:P40 | wdt:P451 | wdt:P3373">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'edm:isRelatedTo'"/>
        </xsl:call-template>
      </xsl:for-each>

      <!-- part relationships -->
      <xsl:for-each select="wdt:P361 | wdt:P463">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'dcterms:isPartOf'"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="wdt:P527">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'dcterms:hasPart'"/>
        </xsl:call-template>
      </xsl:for-each>

      <!-- relationships with other agents -->

      <xsl:for-each select="wdt:P737 | wdt:P1327 | wdt:P1775 | wdt:P1780">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'edm:isRelatedTo'"/>
        </xsl:call-template>
      </xsl:for-each>

      <!-- relationships with concepts -->

      <xsl:for-each select="wdt:P135 | wdt:P136 | wdt:P1066 | wdt:P607
                                | wdt:P1303 | wdt:P641 | wdt:P2416 | wdt:P2650">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'edm:hasMet'"/>
        </xsl:call-template>
      </xsl:for-each>

      <!-- Co-referencing -->

      <xsl:if test="$coref">
        <xsl:for-each select="owl:sameAs | wdt:P1709 | wdt:P2888">
          <xsl:element name="owl:sameAs">
            <xsl:copy-of select="@rdf:resource"/>
          </xsl:element>
        </xsl:for-each>

        <xsl:call-template name="coref">
          <xsl:with-param name="current" select="."/>
          <xsl:with-param name="target" select="'owl:sameAs'"/>
        </xsl:call-template>
      </xsl:if>

      <xsl:call-template name="DBpedia">
        <xsl:with-param name="uri" select="@rdf:about"/>
      </xsl:call-template>

    </edm:Agent>
  </xsl:template>

  <xsl:template name="Place">
    <edm:Place>
      <xsl:copy-of select="@rdf:about"/>

      <!-- labels -->
      <xsl:call-template name="labels">
        <xsl:with-param name="alt" select="skos:altLabel"/>
      </xsl:call-template>

      <!-- descriptions -->
      <xsl:for-each select="schema:description">
        <xsl:if test="lib:isAcceptableLang(@xml:lang) and lib:isAcceptableLabel(.)">
          <xsl:call-template name="LangLiteral">
            <xsl:with-param name="prop" select="'skos:note'"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:for-each>

      <!-- coordinate location -->
      <xsl:for-each select="wdt:P625">
        <xsl:variable name="dt"    select="@rdf:datatype"/>
        <xsl:if test="$dt='http://www.opengis.net/ont/geosparql#wktLiteral'">
          <xsl:variable name="coord" select="string(text())"/>
          <xsl:variable name="long"  select="lib:geo2coord($coord,1)"/>
          <xsl:variable name="lat"   select="lib:geo2coord($coord,2)"/>
          <xsl:if test="$lat and $long">
            <xsl:element name="wgs84_pos:long">
              <xsl:value-of select="$long"/>
            </xsl:element>
            <xsl:element name="wgs84_pos:lat">
              <xsl:value-of select="$lat"/>
            </xsl:element>
          </xsl:if>
        </xsl:if>
      </xsl:for-each>

      <xsl:for-each select="wdt:P2044">
        <xsl:call-template name="Literal">
          <xsl:with-param name="prop" select="'wgs84_pos:alt'"/>
        </xsl:call-template>
      </xsl:for-each>

      <!-- Part relations -->

      <xsl:choose>
        <xsl:when test="wdt:P361">
          <xsl:for-each select="wdt:P361">
            <xsl:call-template name="Reference">
              <xsl:with-param name="prop" select="'dcterms:isPartOf'"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:when test="wdt:P17">
          <xsl:for-each select="wdt:P17">
            <xsl:call-template name="Reference">
              <xsl:with-param name="prop" select="'dcterms:isPartOf'"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:when test="wdt:P30">
          <xsl:for-each select="wdt:P30">
            <xsl:call-template name="Reference">
              <xsl:with-param name="prop" select="'dcterms:isPartOf'"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
      </xsl:choose>

      <!-- Co-referencing -->

      <xsl:if test="$coref">
        <xsl:for-each select="owl:sameAs | wdt:P1709 | wdt:P2888">
          <xsl:element name="owl:sameAs">
            <xsl:copy-of select="@rdf:resource"/>
          </xsl:element>
        </xsl:for-each>

        <xsl:call-template name="coref">
          <xsl:with-param name="current" select="."/>
          <xsl:with-param name="target" select="'owl:sameAs'"/>
        </xsl:call-template>
      </xsl:if>

      <xsl:call-template name="DBpedia">
        <xsl:with-param name="uri" select="@rdf:about"/>
      </xsl:call-template>

    </edm:Place>
  </xsl:template>

  <xsl:template name="TimeSpan">

    <edm:TimeSpan>

      <xsl:copy-of select="@rdf:about"/>

      <!-- labels -->
      <xsl:call-template name="labels">
        <xsl:with-param name="alt" select="skos:altLabel | wdt:P2561"/>
      </xsl:call-template>

      <!-- dates -->

      <xsl:if test="wdt:P580">
        <xsl:element name="edm:begin">
          <xsl:value-of select="wdt:P580[1]"/>
        </xsl:element>
      </xsl:if>
      <xsl:if test="wdt:P582">
        <xsl:element name="edm:end">
          <xsl:value-of select="wdt:P582[1]"/>
        </xsl:element>
      </xsl:if>

      <!-- relations -->

      <xsl:for-each select="wdt:P361">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'dcterms:isPartOf'"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="wdt:P155">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'edm:isNextInSequence'"/>
        </xsl:call-template>
      </xsl:for-each>

      <!-- Co-referencing -->

      <xsl:if test="$coref">
        <xsl:for-each select="owl:sameAs | wdt:P1709 | wdt:P2888">
          <xsl:element name="owl:sameAs">
            <xsl:copy-of select="@rdf:resource"/>
          </xsl:element>
        </xsl:for-each>

        <xsl:call-template name="coref">
          <xsl:with-param name="current" select="."/>
          <xsl:with-param name="target" select="'owl:sameAs'"/>
        </xsl:call-template>
      </xsl:if>

      <xsl:call-template name="DBpedia">
        <xsl:with-param name="uri" select="@rdf:about"/>
      </xsl:call-template>

    </edm:TimeSpan>
  </xsl:template>

  <xsl:template name="Concept">

    <skos:Concept>

      <xsl:copy-of select="@rdf:about"/>

      <!-- labels -->
      <xsl:call-template name="labels">
        <xsl:with-param name="alt" select="skos:altLabel"/>
      </xsl:call-template>

      <xsl:for-each select="schema:description">
        <xsl:if test="lib:isAcceptableLang(@xml:lang) and lib:isAcceptableLabel(.)">
          <xsl:call-template name="LangLiteral">
            <xsl:with-param name="prop" select="'skos:note'"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:for-each>

      <xsl:for-each select="wdt:P31 | wdt:P279 | wdt:P2445">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'skos:broader'"/>
        </xsl:call-template>
      </xsl:for-each>

      <!-- Relationships with other concepts -->

      <xsl:for-each select="wdt:PP135 | wdt:P136 | wdt:P144 | wdt:P155
                                | wdt:P156 | wdt:P361 | wdt:P527 | wdt:P737
                                | wdt:P1535 | wdt:P1557 | wdt:P2283 | wdt:P2579
                                | wdt:P2670 | wdt:P101 | wdt:P3095">
        <xsl:call-template name="Reference">
          <xsl:with-param name="prop" select="'skos:related'"/>
        </xsl:call-template>
      </xsl:for-each>

      <!-- Co-referencing -->

      <xsl:if test="$coref">
        <xsl:for-each select="owl:sameAs | wdt:P1709 | wdt:P2888">
          <xsl:element name="skos:exactMatch">
            <xsl:copy-of select="@rdf:resource"/>
          </xsl:element>
        </xsl:for-each>


        <xsl:call-template name="coref">
          <xsl:with-param name="current" select="."/>
          <xsl:with-param name="target" select="'skos:exactMatch'"/>
        </xsl:call-template>
      </xsl:if>

      <xsl:call-template name="DBpedia">
        <xsl:with-param name="uri" select="@rdf:about"/>
      </xsl:call-template>

    </skos:Concept>

  </xsl:template>


  <!--+++++++++++++++++++++++++++ FUNCTIONS +++++++++++++++++++++++++++++++-->

  <xsl:template name="Literal">
    <xsl:param name="prop"/>

    <xsl:if test="text()[string-length(.) &gt; 0]">
      <xsl:element name="{$prop}">
        <xsl:value-of select="."/>
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <xsl:template name="LangLiteral">
    <xsl:param name="prop"/>

    <xsl:if test="text()[string-length(.) &gt; 0]">
      <xsl:element name="{$prop}">
        <xsl:copy-of select="@xml:lang"/>
        <xsl:value-of select="."/>
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <xsl:template name="Reference">
    <xsl:param name="prop"/>

    <xsl:if test="@rdf:resource">
      <xsl:element name="{$prop}">
        <xsl:copy-of select="@rdf:resource"/>
      </xsl:element>
    </xsl:if>
  </xsl:template>


  <!--+++++++++++++++++++++++++++++ LABELS ++++++++++++++++++++++++++++++++-->

  <xsl:template name="labels">
    <xsl:param name="alt"/>

    <xsl:variable name="labels"
      select="rdfs:label[lib:isAcceptableLang(@xml:lang)
                                     and lib:isAcceptableLabel(text())]"/>

    <xsl:for-each select="$labels">
      <xsl:element name="skos:prefLabel">
        <xsl:copy-of select="@xml:lang"/>
        <xsl:value-of select="."/>
      </xsl:element>
    </xsl:for-each>

    <xsl:for-each select="$alt">
      <xsl:variable name="literal" select="text()"/>
      <xsl:variable name="lang"    select="@xml:lang"/>
      <xsl:if test="lib:isAcceptableLang($lang)
                   and lib:isAcceptableLabel($literal)
                   and not($labels[text()=$literal and @xml:lang=$lang])">
        <xsl:element name="skos:altLabel">
          <xsl:copy-of select="@xml:lang"/>
          <xsl:value-of select="$literal" />
        </xsl:element>
      </xsl:if>
    </xsl:for-each>

  </xsl:template>

  <xsl:function name="lib:isAcceptableLang" as="xs:boolean">
    <xsl:param name="string"/>

    <xsl:sequence select="$string!='' and contains($langs,lower-case($string))"/>
  </xsl:function>

  <xsl:function name="lib:isAcceptableLabel" as="xs:boolean">
    <xsl:param name="string"/>

    <xsl:sequence select="matches($string,'[\p{L}\p{N}]')"/>
  </xsl:function>


  <!--++++++++++++++++++++++++ GEO COORDINATES ++++++++++++++++++++++++++++-->

  <xsl:function name="lib:geo2coord" as="xs:string">
    <xsl:param    name="value"/>
    <xsl:param    name="index"/>

    <xsl:variable name="replace">
      <xsl:choose>
        <xsl:when test="$index=1">
          <xsl:value-of select="'$1'"/>
        </xsl:when>
        <xsl:when test="$index=2">
          <xsl:value-of select="'$3'"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>


    <xsl:value-of select="replace($value, '\s*Point[(]\s*([-+]?\d+([.]\d+)?)\s+([-+]?\d+([.]\d+)?)\s*[)]\s*', $replace)"/>

  </xsl:function>


  <!--++++++++++++++++++++++++++ COREFERENCING ++++++++++++++++++++++++++++-->

  <xsl:template name="coref">
    <xsl:param name="current"/>
    <xsl:param name="target"/>
    <xsl:for-each select="$map/entry">
      <xsl:variable name="property" select="string(@key)"/>
      <xsl:variable name="pattern"  select="string(text())"/>
      <xsl:for-each select="$current/*[local-name()=$property and namespace-uri()=$namespace]">
        <xsl:element name="{$target}">
          <xsl:attribute name="rdf:resource">
            <xsl:value-of select="lib:mapLD($pattern,string(text()))"/>
          </xsl:attribute>
        </xsl:element>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:function name="lib:mapLD" as="xs:string">
    <xsl:param    name="pattern"/>
    <xsl:param    name="value"/>

    <xsl:variable name="base"  select="substring-before($pattern, '$1')"/>

    <xsl:choose>
      <xsl:when test="starts-with($value,$base)">
        <xsl:value-of select="$value"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="fn:replace($pattern,'[$]1',$value)"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:function>

  <xsl:template name="DBpedia">
    <xsl:param name="uri"/>

    <xsl:for-each select="$articles[schema:about/@rdf:resource=$uri
                                    and contains(@rdf:about,$wiki)]">
      <xsl:element name="owl:sameAs">
        <xsl:attribute name="rdf:resource">
          <xsl:value-of select="replace(@rdf:about,$wiki,$dbp)"/>
        </xsl:attribute>
      </xsl:element>
    </xsl:for-each>

  </xsl:template>

</xsl:stylesheet>
