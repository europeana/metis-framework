<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:iso="http://purl.oclc.org/dsdl/schematron"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:schold="http://www.ascc.net/xml/schematron"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:edm="http://www.europeana.eu/schemas/edm/"
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:ebucore="http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#"
  xmlns:rdaGr2="http://rdvocab.info/ElementsGr2/"
  xmlns:ore="http://www.openarchives.org/ore/terms/"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:owl="http://www.w3.org/2002/07/owl#"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
  xmlns:crm="http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#"
  xmlns:skos="http://www.w3.org/2004/02/skos/core#"
  xmlns:cc="http://creativecommons.org/ns#"
  xmlns:odrl="http://www.w3.org/ns/odrl/2/"
  xmlns:dct="http://purl.org/dc/terms/"
  xmlns:fn="http://www.w3.org/2005/xpath-functions"
  xmlns:lib="http://localhost.com"
  version="1.0"><!--Implementers: please note that overriding process-prolog or process-root is
    the preferred method for meta-stylesheets to use where possible. -->
  <xsl:param name="archiveDirParameter"/>
  <xsl:param name="archiveNameParameter"/>
  <xsl:param name="fileNameParameter"/>
  <xsl:param name="fileDirParameter"/>
  <xsl:variable name="document-uri">
    <xsl:value-of select="document-uri(/)"/>
  </xsl:variable>
  <xsl:variable name="license_patterns">
    <!--
    CC VERSION 1.0 LICENSES
    <http://creativecommons.org/licenses/[PERMISSIONS]/1.0/[PORT]/>
    PERMISSIONS: by | by-sa | by-nd | by-nc | by-nc-sa | by-nc-nd | by-nd-nc
    PORT: generic (no port) | fi | il | nl
    -->
    <pattern
      value="^http[:]//creativecommons[.]org/licenses/(by|by-sa|by-nd|by-nc|by-nc-sa|by-nc-nd|by-nd-nc)/1[.]0/((fi|il|nl)/)?$"/>

    <!--
    CC VERSION 2.0 LICENSES
    <http://creativecommons.org/licenses/[PERMISSIONS]/2.0/[PORT]/>
    PERMISSIONS: by | by-sa | by-nd | by-nc | by-nc-sa | by-nc-nd
    PORT: generic (no port) | au | at | be | br | ca | cl | hr | uk | fr | de | it | jp | nl | pl | kr | es | tw
    -->
    <pattern
      value="^http[:]//creativecommons[.]org/licenses/(by|by-sa|by-nd|by-nc|by-nc-sa|by-nc-nd)/2[.]0/((au|at|be|br|ca|cl|hr|uk|fr|de|it|jp|nl|pl|kr|es|tw)/)?$"/>

    <!--
    CC VERSION 2.1 LICENSES
    <http://creativecommons.org/licenses/[PERMISSIONS]/2.1/[PORT]/>
    PERMISSIONS: by | by-sa | by-nd | by-nc | by-nc-sa | by-nc-nd
    PORT: au | es | jp
    -->
    <pattern
      value="^http[:]//creativecommons[.]org/licenses/(by|by-sa|by-nd|by-nc|by-nc-sa|by-nc-nd)/2[.]1/((au|es|jp)/)$"/>

    <!--
    CC VERSION 2.5 LICENSES
    <http://creativecommons.org/licenses/[PERMISSIONS]/2.5/[PORT]/>
    PERMISSIONS: by | by-sa | by-nd | by-nc | by-nc-sa | by-nc-nd
    PORT: generic (no port) | ar | au | br | bg | ca | cn | co | hr | dk | hu | in | il | it | mk | my | mt | mx | nl | pe | pl | pt | scotland | si | za | es | se | ch | tw
    -->
    <pattern
      value="^http[:]//creativecommons[.]org/licenses/(by|by-sa|by-nd|by-nc|by-nc-sa|by-nc-nd)/2[.]5/((ar|au|br|bg|ca|cn|co|hr|dk|hu|in|il|it|mk|my|mt|mx|nl|pe|pl|pt|scotland|si|za|es|se|ch|tw)/)?$"/>

    <!--
    CC VERSION 3.0 LICENSES
    <http://creativecommons.org/licenses/[PERMISSIONS]/3.0/[PORT]/>
    PERMISSIONS: by | by-sa | by-nd |  by-nc | by-nc-sa | by-nc-nd
    PORT: generic (no port) | au | at | br | cl | cn | cr | hr | cz | ec | eg | ee | fr | de | gr | gt | hk | igo | ie | it | lu | nl | nz | no | ph | pl | pt | pr | ro | rs | sg | za | es | ch | tw | th | ug | us | ve | vn
    -->
    <pattern
      value="^http[:]//creativecommons[.]org/licenses/(by|by-sa|by-nd|by-nc|by-nc-sa|by-nc-nd)/3[.]0/((au|at|br|cl|cn|cr|hr|cz|ec|eg|ee|fr|de|gr|gt|hk|igo|ie|it|lu|nl|nz|no|ph|pl|pt|pr|ro|rs|sg|za|es|ch|tw|th|ug|us|ve|vn)/)?$"/>

    <!--
    CC VERSION 4.0 LICENSES
    <http://creativecommons.org/licenses/[PERMISSIONS]/4.0/>
    PERMISSIONS: by | by-sa | by-nd | by-nc | by-nc-sa | by-nc-nd
    -->
    <pattern
      value="^http[:]//creativecommons[.]org/licenses/(by|by-sa|by-nd|by-nc|by-nc-sa|by-nc-nd)/4[.]0/$"/>

    <!--
    CC PUBLIC DOMAIN TOOLS
    <http://creativecommons.org/publicdomain/[PUBLIC DOMAIN TOOL]/1.0/
    PUBLIC DOMAIN TOOL: zero | mark
    -->
    <pattern value="^http[:]//creativecommons[.]org/publicdomain/(zero|mark)/1[.]0/$"/>

    <!--
    RIGHTSSTATEMENTS.ORG
    <http://rightsstatements.org/vocab/[PERMISSIONS]/1.0/>
    PERMISSIONS: NoC-NC | NoC-OKLR | InC | InC-EDU | InC-OW-EU | CNE
    -->
    <pattern
      value="^http[:]//rightsstatements[.]org/vocab/(NoC-NC|NoC-OKLR|InC|InC-EDU|InC-OW-EU|CNE)/1[.]0/$"/>
  </xsl:variable>
  <xsl:variable name="cc_licenses" select="//cc:License" />

  <!--PHASES-->


  <!--PROLOG-->
  <xsl:output xmlns:svrl="http://purl.oclc.org/dsdl/svrl" method="xml"
    omit-xml-declaration="no"
    standalone="yes"
    indent="yes"/>

  <!--XSD TYPES FOR XSLT2-->


  <!--KEYS AND FUNCTIONS-->


  <!--DEFAULT RULES-->


  <!--MODE: SCHEMATRON-SELECT-FULL-PATH-->
  <!--This mode can be used to generate an ugly though full XPath for locators-->
  <xsl:template match="*" mode="schematron-select-full-path">
    <xsl:apply-templates select="." mode="schematron-get-full-path"/>
  </xsl:template>

  <!--MODE: SCHEMATRON-FULL-PATH-->
  <!--This mode can be used to generate an ugly though full XPath for locators-->
  <xsl:template match="*" mode="schematron-get-full-path">
    <xsl:apply-templates select="parent::*" mode="schematron-get-full-path"/>
    <xsl:text>/</xsl:text>
    <xsl:choose>
      <xsl:when test="namespace-uri()=''">
        <xsl:value-of select="name()"/>
        <xsl:variable name="p_1"
          select="1+    count(preceding-sibling::*[name()=name(current())])"/>
        <xsl:if test="$p_1&gt;1 or following-sibling::*[name()=name(current())]">[<xsl:value-of
          select="$p_1"/>]
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>*[local-name()='</xsl:text>
        <xsl:value-of select="local-name()"/>
        <xsl:text>']</xsl:text>
        <xsl:variable name="p_2"
          select="1+   count(preceding-sibling::*[local-name()=local-name(current())])"/>
        <xsl:if test="$p_2&gt;1 or following-sibling::*[local-name()=local-name(current())]">
          [<xsl:value-of select="$p_2"/>]
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="@*" mode="schematron-get-full-path">
    <xsl:text>/</xsl:text>
    <xsl:choose>
      <xsl:when test="namespace-uri()=''">@<xsl:value-of select="name()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>@*[local-name()='</xsl:text>
        <xsl:value-of select="local-name()"/>
        <xsl:text>' and namespace-uri()='</xsl:text>
        <xsl:value-of select="namespace-uri()"/>
        <xsl:text>']</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--MODE: SCHEMATRON-FULL-PATH-2-->
  <!--This mode can be used to generate prefixed XPath for humans-->
  <xsl:template match="node() | @*" mode="schematron-get-full-path-2">
    <xsl:for-each select="ancestor-or-self::*">
      <xsl:text>/</xsl:text>
      <xsl:value-of select="name(.)"/>
      <xsl:if test="preceding-sibling::*[name(.)=name(current())]">
        <xsl:text>[</xsl:text>
        <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/>
        <xsl:text>]</xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:if test="not(self::*)">
      <xsl:text/>/@<xsl:value-of select="name(.)"/>
    </xsl:if>
  </xsl:template>
  <!--MODE: SCHEMATRON-FULL-PATH-3-->
  <!--This mode can be used to generate prefixed XPath for humans
      (Top-level element has index)-->
  <xsl:template match="node() | @*" mode="schematron-get-full-path-3">
    <xsl:for-each select="ancestor-or-self::*">
      <xsl:text>/</xsl:text>
      <xsl:value-of select="name(.)"/>
      <xsl:if test="parent::*">
        <xsl:text>[</xsl:text>
        <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/>
        <xsl:text>]</xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:if test="not(self::*)">
      <xsl:text/>/@<xsl:value-of select="name(.)"/>
    </xsl:if>
  </xsl:template>

  <!--MODE: GENERATE-ID-FROM-PATH -->
  <xsl:template match="/" mode="generate-id-from-path"/>
  <xsl:template match="text()" mode="generate-id-from-path">
    <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
    <xsl:value-of select="concat('.text-', 1+count(preceding-sibling::text()), '-')"/>
  </xsl:template>
  <xsl:template match="comment()" mode="generate-id-from-path">
    <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
    <xsl:value-of select="concat('.comment-', 1+count(preceding-sibling::comment()), '-')"/>
  </xsl:template>
  <xsl:template match="processing-instruction()" mode="generate-id-from-path">
    <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
    <xsl:value-of
      select="concat('.processing-instruction-', 1+count(preceding-sibling::processing-instruction()), '-')"/>
  </xsl:template>
  <xsl:template match="@*" mode="generate-id-from-path">
    <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
    <xsl:value-of select="concat('.@', name())"/>
  </xsl:template>
  <xsl:template match="*" mode="generate-id-from-path" priority="-0.5">
    <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
    <xsl:text>.</xsl:text>
    <xsl:value-of
      select="concat('.',name(),'-',1+count(preceding-sibling::*[name()=name(current())]),'-')"/>
  </xsl:template>

  <!--MODE: GENERATE-ID-2 -->
  <xsl:template match="/" mode="generate-id-2">U</xsl:template>
  <xsl:template match="*" mode="generate-id-2" priority="2">
    <xsl:text>U</xsl:text>
    <xsl:number level="multiple" count="*"/>
  </xsl:template>
  <xsl:template match="node()" mode="generate-id-2">
    <xsl:text>U.</xsl:text>
    <xsl:number level="multiple" count="*"/>
    <xsl:text>n</xsl:text>
    <xsl:number count="node()"/>
  </xsl:template>
  <xsl:template match="@*" mode="generate-id-2">
    <xsl:text>U.</xsl:text>
    <xsl:number level="multiple" count="*"/>
    <xsl:text>_</xsl:text>
    <xsl:value-of select="string-length(local-name(.))"/>
    <xsl:text>_</xsl:text>
    <xsl:value-of select="translate(name(),':','.')"/>
  </xsl:template>
  <!--Strip characters-->
  <xsl:template match="text()" priority="-1"/>

  <!--SCHEMA SETUP-->
  <xsl:template match="/">
    <svrl:schematron-output xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
      title="Schematron validation"
      schemaVersion="">
      <xsl:comment>
        <xsl:value-of select="$archiveDirParameter"/>
        <xsl:value-of select="$archiveNameParameter"/>
        <xsl:value-of select="$fileNameParameter"/>
        <xsl:value-of select="$fileDirParameter"/>
      </xsl:comment>
      <svrl:ns-prefix-in-attribute-values uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.europeana.eu/schemas/edm/" prefix="edm"/>
      <svrl:ns-prefix-in-attribute-values uri="http://xmlns.com/foaf/0.1/" prefix="foaf"/>
      <svrl:ns-prefix-in-attribute-values
        uri="http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#" prefix="ebucore"/>
      <svrl:ns-prefix-in-attribute-values uri="http://rdvocab.info/ElementsGr2/" prefix="rdaGr2"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.openarchives.org/ore/terms/"
        prefix="ore"/>
      <svrl:ns-prefix-in-attribute-values uri="http://purl.org/dc/terms/" prefix="dcterms"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/2000/01/rdf-schema#"
        prefix="rdfs"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/2002/07/owl#" prefix="owl"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        prefix="rdf"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/2003/01/geo/wgs84_pos#"
        prefix="wgs84"/>
      <svrl:ns-prefix-in-attribute-values
        uri="http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#"
        prefix="crm"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/2004/02/skos/core#" prefix="skos"/>
      <svrl:ns-prefix-in-attribute-values uri="http://creativecommons.org/ns#" prefix="cc"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/ns/odrl/2/" prefix="odrl"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M15"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M16"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M17"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M18"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M19"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M20"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M21"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M22"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M23"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M24"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M25"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M26"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M27"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.europeana.eu/schemas/edm/" prefix="edm"/>
      <svrl:ns-prefix-in-attribute-values uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>
      <svrl:ns-prefix-in-attribute-values uri="http://purl.org/dc/terms/" prefix="dct"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.openarchives.org/ore/terms/"
        prefix="ore"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/2002/07/owl#" prefix="owl"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        prefix="rdf"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/2000/01/rdf-schema#"
        prefix="rdfs"/>
      <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/2004/02/skos/core#" prefix="skos"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M35"/>
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </svrl:active-pattern>
      <xsl:apply-templates select="/" mode="M45"/>
    </svrl:schematron-output>
  </xsl:template>

  <!--SCHEMATRON PATTERNS-->


  <!--PATTERN -->


  <!--RULE -->
  <xsl:template match="*" priority="1000" mode="M15">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="*"/>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="not(@rdf:resource = '')"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="not(@rdf:resource = '')">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <svrl:text>
            Empty rdf:resource attribute is not allowed for <xsl:text/><xsl:value-of select="name(.)"/><xsl:text/> element.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M15"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M15"/>
  <xsl:template match="@*|node()" priority="-2" mode="M15">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M15"/>
  </xsl:template>

  <!--PATTERN -->


  <!--RULE -->
  <xsl:template match="*" priority="1000" mode="M16">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="*"/>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="not(@rdf:resource and text())"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="not(@rdf:resource and text())">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <svrl:text>
            Element <xsl:text/><xsl:value-of select="name(.)"/><xsl:text/> should not have both rdf:resource attribute and text value populated.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M16"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M16"/>
  <xsl:template match="@*|node()" priority="-2" mode="M16">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M16"/>
  </xsl:template>

  <!--PATTERN -->


  <!--RULE -->
  <xsl:template match="ore:Proxy" priority="1000" mode="M17">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="ore:Proxy"/>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="dc:subject or dc:type or dct:temporal or dct:spatial"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="dc:subject or dc:type or dct:temporal or dct:spatial">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            A Proxy must have a dc:subject or dc:type or dct:temporal or dct:spatial.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when
        test="((dc:subject and (exists(dc:subject/@rdf:resource) or normalize-space(dc:subject)!='')) or (dc:type and (exists(dc:type/@rdf:resource) or         normalize-space(dc:type)!='')) or (dct:temporal and          (exists(dct:temporal/@rdf:resource) or normalize-space(dct:temporal)!=''))  or (dct:spatial and (exists(dct:spatial/@rdf:resource) or normalize-space       (dct:spatial)!='')))"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="((dc:subject and (exists(dc:subject/@rdf:resource) or normalize-space(dc:subject)!='')) or (dc:type and (exists(dc:type/@rdf:resource) or normalize-space(dc:type)!='')) or (dct:temporal and (exists(dct:temporal/@rdf:resource) or normalize-space(dct:temporal)!='')) or (dct:spatial and (exists(dct:spatial/@rdf:resource) or normalize-space (dct:spatial)!='')))">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            A Proxy must have a non empty dc:subject or dc:type or dct:temporal or dct:spatial.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="dc:title or dc:description"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="dc:title or dc:description">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            A Proxy must have a dc:title or dc:description.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when
        test="(dc:title and normalize-space(dc:title)!='') or (dc:description and (exists(dc:description/@rdf:resource) or normalize-space(dc:decription)!=''))"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="(dc:title and normalize-space(dc:title)!='') or (dc:description and (exists(dc:description/@rdf:resource) or normalize-space(dc:decription)!=''))">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            A Proxy must have a non empty dc:title or a non empty dc:description
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="not(edm:type='TEXT') or (edm:type='TEXT' and exists(dc:language))"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="not(edm:type='TEXT') or (edm:type='TEXT' and exists(dc:language))">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            Within a Proxy context, dc:language is mandatory when dc:language has the value 'TEXT'.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="edm:type or (not(edm:type) and edm:europeanaProxy)"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="edm:type or (not(edm:type) and edm:europeanaProxy)">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            edm:type should be present in an ore:Proxy context.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="not(edm:type and edm:europeanaProxy)"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="not(edm:type and edm:europeanaProxy)">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            edm:type should not be present in an Europeana Proxy context (when the
            edm:europeanaProxy value is present).
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M17"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M17"/>
  <xsl:template match="@*|node()" priority="-2" mode="M17">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M17"/>
  </xsl:template>

  <!--PATTERN -->


  <!--RULE -->
  <xsl:template match="ore:Aggregation" priority="1000" mode="M18">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="ore:Aggregation"/>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when
        test="(edm:isShownAt and exists(edm:isShownAt/@rdf:resource)) or (edm:isShownBy and exists(edm:isShownBy/@rdf:resource))"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="(edm:isShownAt and exists(edm:isShownAt/@rdf:resource)) or (edm:isShownBy and exists(edm:isShownBy/@rdf:resource))">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            An ore:Aggregation must have either edm:isShownAt or edm:isShownBy
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M18"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M18"/>
  <xsl:template match="@*|node()" priority="-2" mode="M18">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M18"/>
  </xsl:template>

  <!--PATTERN -->


  <!--RULE -->
  <xsl:template match="ore:Aggregation" priority="1000" mode="M19">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="ore:Aggregation"/>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="edm:dataProvider"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="edm:dataProvider">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            An ore:Aggregation must have at least one instance of edm:dataProvider
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M19"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M19"/>
  <xsl:template match="@*|node()" priority="-2" mode="M19">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M19"/>
  </xsl:template>

  <!--PATTERN -->


  <!--RULE -->
  <xsl:template match="ore:Aggregation" priority="1000" mode="M20">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="ore:Aggregation"/>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="edm:provider"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="edm:provider">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            An ore:Aggregation must have at least one instance of edm:provider
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M20"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M20"/>
  <xsl:template match="@*|node()" priority="-2" mode="M20">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M20"/>
  </xsl:template>

  <!--PATTERN -->


  <!--RULE -->
  <xsl:template match="ore:Aggregation" priority="1000" mode="M21">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="ore:Aggregation"/>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="edm:rights and exists(edm:rights/@rdf:resource)"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="edm:rights and exists(edm:rights/@rdf:resource)">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            An ore:Aggregation must have at least one instance of edm:rights
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M21"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M21"/>
  <xsl:template match="@*|node()" priority="-2" mode="M21">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M21"/>
  </xsl:template>

  <!--PATTERN -->


  <!--RULE -->
  <xsl:template match="ore:Aggregation/edm:provider" priority="1000" mode="M22">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
      context="ore:Aggregation/edm:provider"/>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="exists(./@rdf:resource) or normalize-space(.)!=''"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="exists(./@rdf:resource) or normalize-space(.)!=''">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <svrl:text>An ore:Aggregation must have a non empty edm:provider</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M22"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M22"/>
  <xsl:template match="@*|node()" priority="-2" mode="M22">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M22"/>
  </xsl:template>

  <!--PATTERN -->


  <!--RULE -->
  <xsl:template match="ore:Aggregation/edm:dataProvider" priority="1000" mode="M23">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
      context="ore:Aggregation/edm:dataProvider"/>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="exists(./@rdf:resource) or normalize-space(.)!=''"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="exists(./@rdf:resource) or normalize-space(.)!=''">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <svrl:text>An ore:Aggregation must have a non empty edm:dataProvider</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M23"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M23"/>
  <xsl:template match="@*|node()" priority="-2" mode="M23">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M23"/>
  </xsl:template>

  <!--PATTERN -->


  <!--RULE -->
  <xsl:template match="ore:Aggregation/edm:rights" priority="1000" mode="M24">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
      context="ore:Aggregation/edm:rights"/>
    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="lib:isValidRightsField(@rdf:resource)"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="lib:isValidRightsField(@rdf:resource)">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            Invalid Rights Statements
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M24"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M24"/>
  <xsl:template match="@*|node()" priority="-2" mode="M24">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M24"/>
  </xsl:template>
  <svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">Schematron validation</svrl:text>

  <!--PATTERN -->


  <!--RULE -->
  <xsl:template match="*" priority="1000" mode="M25">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="*"/>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="not(@xml:lang = '')"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="not(@xml:lang = '')">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <svrl:text>
            Empty xml:lang attribute is not allowed for
            <xsl:text/><xsl:value-of select="name(.)"/><xsl:text/> element.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M25"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M25"/>
  <xsl:template match="@*|node()" priority="-2" mode="M25">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M25"/>
  </xsl:template>

  <xsl:function name="lib:isValidRS" as="xs:boolean">
    <xsl:param name="uri"/>
    <xsl:sequence select="some $x in $license_patterns/pattern/@value satisfies fn:matches($uri,$x)"/>
  </xsl:function>

  <xsl:function name="lib:isValidRightsField" as="xs:boolean">
    <xsl:param name="uri"/>
    <xsl:sequence select="lib:isValidRS($uri) or lib:isValidRS($cc_licenses[@rdf:about=$uri]/odrl:inheritFrom/@rdf:resource)"/>
  </xsl:function>

  <xsl:template match="edm:WebResource/edm:rights" priority="1000" mode="M26">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="edm:WebResource/edm:rights"/>
    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="lib:isValidRightsField(@rdf:resource)"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="lib:isValidRightsField(@rdf:resource)">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            Invalid Rights Statements
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M26"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M26"/>
  <xsl:template match="@*|node()" priority="-2" mode="M26">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M26"/>
  </xsl:template>
  <svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">Schematron validation</svrl:text>

  <!--RULE -->
  <xsl:template match="edm:WebResource" priority="1000" mode="M27">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="edm:WebResource"/>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="not(dct:hasPart[text()])"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="not(dct:hasPart[text()])">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            The element dcterms:isPartOf should not have a literal value in the edm:WebResource context with this id. Use an rdf:resource instead.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M27"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M27"/>
  <xsl:template match="@*|node()" priority="-2" mode="M27">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M27"/>
  </xsl:template>
  <svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">Schematron validation</svrl:text>

  <!--PATTERN -->


  <!--RULE -->
  <xsl:template match="edm:ProvidedCHO" priority="1000" mode="M35">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="edm:ProvidedCHO"/>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="dc:subject or dc:type or dct:temporal or dct:spatial"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="dc:subject or dc:type or dct:temporal or dct:spatial">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            A ProvidedCHO must have a dc:subject or dc:type or dct:temporal or dct:spatial.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:choose>
      <xsl:when
        test="dc:subject[@rdf:resource] or dc:subject[normalize-space(text())!=''] or dc:type[@rdf:resource] or dc:type[normalize-space(text())!=''] or dct:temporal[@rdf:resource] or dct:temporal[normalize-space(text())!=''] or dct:spatial[@rdf:resource] or dct:spatial[normalize-space(text())!='']">
      </xsl:when>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <svrl:text>
            A ProvidedCHO must have at least one of dc:subject or dc:type or dct:temporal or dct:spatial, that has an rdf:resource attribute or have non empty text value.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when
        test="(dc:title and dc:title[normalize-space(text())!='']) or (dc:description[@rdf:resource] or dc:description[normalize-space(text())!=''])"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            A ProvidedCHO must have a non empty dc:title or a non empty dc:description
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

    <!--ASSERT -->
    <xsl:choose>
      <xsl:when test="not(edm:type='TEXT') or (edm:type='TEXT' and dc:language)"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
          test="not(edm:type='TEXT') or (edm:type='TEXT' and exists(dc:language))">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <xsl:attribute name="nodeId">
            <xsl:value-of select="@rdf:about"/>
          </xsl:attribute>
          <svrl:text>
            Within a ProvidedCHO context, dc:language is mandatory when edm:type has the value 'TEXT'.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M35"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M35"/>
  <xsl:template match="@*|node()" priority="-2" mode="M35">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M35"/>
  </xsl:template>

  <!--PATTERN -->


  <!--RULE -->
  <xsl:template match="rdf:RDF" priority="1000" mode="M45">
    <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="rdf:RDF"/>

    <!--ASSERT -->
    <!-- NOTE: this check is not captured in the XSD schematron annotations. It applies only to
         Metis validation and does not constitute an official requirement on EDM documents. -->
    <xsl:choose>
      <xsl:when test="count(edm:ProvidedCHO) = 1"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                test="count(edm:ProvidedCHO) = 1">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <svrl:text>
            Within a RDF context, there must be exactly one edm:ProvidedCHO.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

    <!--ASSERT -->
    <!-- NOTE: this check is not captured in the XSD schematron annotations. It applies only to
         Metis validation and does not constitute an official requirement on EDM documents. -->
    <xsl:choose>
      <xsl:when test="count(ore:Aggregation) = 1"/>
      <xsl:otherwise>
        <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                test="count(ore:Aggregation) = 1">
          <xsl:attribute name="location">
            <xsl:apply-templates select="." mode="schematron-select-full-path"/>
          </xsl:attribute>
          <svrl:text>
            Within a RDF context, there must be exactly one ore:Aggregation.
          </svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M45"/>
  </xsl:template>
  <xsl:template match="text()" priority="-1" mode="M45"/>
  <xsl:template match="@*|node()" priority="-2" mode="M45">
    <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M45"/>
  </xsl:template>

</xsl:stylesheet>
