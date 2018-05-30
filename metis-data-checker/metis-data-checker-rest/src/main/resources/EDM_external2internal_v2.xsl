<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="xml wgs84_pos" version="2.0"
                xmlns:adms="http://www.w3.org/ns/adms#" xmlns:cc="http://creativecommons.org/ns#"
                xmlns:crm="http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#"
                xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:dcterms="http://purl.org/dc/terms/" xmlns:doap="http://usefulinc.com/ns/doap#"
                xmlns:ebucore="http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#"
                xmlns:edm="http://www.europeana.eu/schemas/edm/" xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:odrl="http://www.w3.org/ns/odrl/2/" xmlns:ore="http://www.openarchives.org/ore/terms/"
                xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:rdaGr2="http://rdvocab.info/ElementsGr2/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:repox="http://repox.ist.utl.pt"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#" xmlns:svcs="http://rdfs.org/sioc/services#"
                xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#"
                xmlns:xalan="http://xml.apache.org/xalan" xmlns:xml="http://www.w3.org/XML/1998/namespace"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output encoding="UTF-8" indent="yes"/>


    <!--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX-->
    <!-- ++++++++++   VARIABLES   +++++++++++++++++ -->
    <!-- XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX-->

    <!-- FIXED VARIABLES -->
    <xsl:variable name="country" select="'Europe'"/>
    <xsl:variable name="language" select="'mul'"/>


    <!-- DYNAMIC VARIABLES -->
    <!-- IDs -->
    <xsl:variable name="id_PCHO"
                  select="/rdf:RDF/edm:ProvidedCHO/@rdf:about"/>
    <xsl:variable name="id_aggregation"
                  select="/rdf:RDF/ore:Aggregation/@rdf:about"/>


    <!--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX-->
    <!-- ++++++++++   MAPPING   +++++++++++++++++ -->
    <!-- XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX-->

    <!-- ROOT MATCH -->
    <xsl:template match="/">
        <xsl:apply-templates select="."/>
        <!--<xsl:apply-templates select="." mode="main"/>-->
    </xsl:template>


    <!-- MAIN TEMPLATE -->
    <xsl:template match="/">

        <!-- rdf:RDF, id: 0 -->
        <xsl:element name="rdf:RDF">
            <xsl:namespace name="adms">http://www.w3.org/ns/adms#</xsl:namespace>
            <xsl:namespace name="cc">http://creativecommons.org/ns#</xsl:namespace>
            <xsl:namespace name="crm"
            >http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#
            </xsl:namespace>
            <xsl:namespace name="dc">http://purl.org/dc/elements/1.1/</xsl:namespace>
            <xsl:namespace name="dcat">http://www.w3.org/ns/dcat#</xsl:namespace>
            <xsl:namespace name="dcterms">http://purl.org/dc/terms/</xsl:namespace>
            <xsl:namespace name="doap">http://usefulinc.com/ns/doap#</xsl:namespace>
            <xsl:namespace name="dv">http://dfg-viewer.de/</xsl:namespace>
            <xsl:namespace name="dwork"
            >http://www.ub.uni-heidelberg.de/__NO_SUCH_SCHEMA__
            </xsl:namespace>
            <xsl:namespace name="ebucore"
            >http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#
            </xsl:namespace>
            <xsl:namespace name="edm">http://www.europeana.eu/schemas/edm/</xsl:namespace>
            <xsl:namespace name="foaf">http://xmlns.com/foaf/0.1/</xsl:namespace>
            <xsl:namespace name="mets">http://www.loc.gov/METS/</xsl:namespace>
            <xsl:namespace name="mods1">http://www.loc.gov/mods/v3</xsl:namespace>
            <xsl:namespace name="odrl">http://www.w3.org/ns/odrl/2/</xsl:namespace>
            <xsl:namespace name="oai">http://www.openarchives.org/OAI/2.0/</xsl:namespace>
            <xsl:namespace name="ore">http://www.openarchives.org/ore/terms/</xsl:namespace>
            <xsl:namespace name="owl">http://www.w3.org/2002/07/owl#</xsl:namespace>
            <xsl:namespace name="rdaGr2">http://rdvocab.info/ElementsGr2/</xsl:namespace>
            <xsl:namespace name="rdf">http://www.w3.org/1999/02/22-rdf-syntax-ns#</xsl:namespace>
            <xsl:namespace name="rdfs">http://www.w3.org/2000/01/rdf-schema#</xsl:namespace>
            <xsl:namespace name="repox">http://repox.ist.utl.pt</xsl:namespace>
            <xsl:namespace name="sch">http://purl.oclc.org/dsdl/schematron</xsl:namespace>
            <xsl:namespace name="skos">http://www.w3.org/2004/02/skos/core#</xsl:namespace>
            <xsl:namespace name="svcs">http://rdfs.org/sioc/services#</xsl:namespace>
            <xsl:namespace name="wdrs">http://www.w3.org/2007/05/powder-s#</xsl:namespace>
            <xsl:namespace name="wgs84">http://www.w3.org/2003/01/geo/wgs84_pos#</xsl:namespace>
            <xsl:namespace name="wgs84_pos">http://www.w3.org/2003/01/geo/wgs84_pos#</xsl:namespace>
            <xsl:namespace name="xalan">http://xml.apache.org/xalan</xsl:namespace>
            <xsl:namespace name="xlink">http://www.w3.org/1999/xlink</xsl:namespace>
            <xsl:namespace name="xml">http://www.w3.org/XML/1998/namespace</xsl:namespace>
            <xsl:namespace name="xsi">http://www.w3.org/2001/XMLSchema-instance</xsl:namespace>

            <!-- edm:ProvidedCHO -->
            <xsl:element name="edm:ProvidedCHO">
                <xsl:attribute name="rdf:about">
                    <xsl:value-of select="$id_PCHO"/>
                </xsl:attribute>
            </xsl:element>

            <!-- edm:WebResource -->
            <xsl:copy-of select="rdf:RDF/edm:WebResource"/>

            <!-- edm:Agent -->
            <xsl:copy-of select="rdf:RDF/edm:Agent"/>

            <!-- edm:Place -->
            <xsl:copy-of select="rdf:RDF/edm:Place"/>

            <!-- edm:TimeSpan -->
            <xsl:copy-of select="rdf:RDF/edm:TimeSpan"/>

            <!-- skos:Concept -->
            <xsl:copy-of select="rdf:RDF/skos:Concept"/>

            <!-- ore:Aggregation -->
            <xsl:copy-of select="rdf:RDF/ore:Aggregation"/>

            <!-- ore:Proxy -->
            <xsl:element name="ore:Proxy">
                <xsl:attribute name="rdf:about">
                    <xsl:value-of select="$id_PCHO"/>
                </xsl:attribute>
                <xsl:copy-of select="rdf:RDF/edm:ProvidedCHO/*[not(self::owl:sameAs | self::edm:type)]"/>

                <xsl:element name="ore:proxyFor">
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="$id_PCHO"/>
                    </xsl:attribute>
                </xsl:element>

                <xsl:element name="ore:proxyIn">
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="$id_aggregation"/>
                    </xsl:attribute>
                </xsl:element>

                <xsl:copy-of select="rdf:RDF/edm:ProvidedCHO/edm:type"/>
                <xsl:copy-of select="rdf:RDF/edm:ProvidedCHO/owl:sameAs"/>

            </xsl:element>

            <!-- edm:EuropeanaAggregation -->
            <xsl:element name="edm:EuropeanaAggregation">
                <xsl:attribute name="rdf:about">
                    <xsl:value-of select="$id_aggregation"/>
                </xsl:attribute>

                <xsl:element name="edm:aggregatedCHO">
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="$id_PCHO"/>
                    </xsl:attribute>
                </xsl:element>
                <xsl:element name="edm:country">
                    <xsl:value-of select="$country"/>
                </xsl:element>
                <xsl:element name="edm:language">
                    <xsl:value-of select="$language"/>
                </xsl:element>

            </xsl:element>

            <!-- cc:License -->
            <xsl:copy-of select="rdf:RDF/cc:License"/>

            <!-- svcs:Service -->
            <xsl:copy-of select="rdf:RDF/svcs:Service"/>

            <!-- ./rdf:RDF -->
        </xsl:element>

    </xsl:template>

</xsl:stylesheet>