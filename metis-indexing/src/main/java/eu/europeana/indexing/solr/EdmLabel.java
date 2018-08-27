package eu.europeana.indexing.solr;

/**
 * Enumeration holding the Solr field definitions
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public enum EdmLabel {

  // GENERAL
  EUROPEANA_COMPLETENESS("europeana_completeness"),
  EUROPEANA_COLLECTIONNAME("europeana_collectionName"), 
  EUROPEANA_ID("europeana_id"), 
  TIMESTAMP_CREATED("timestamp_created"), 
  TIMESTAMP_UPDATED("timestamp_update"), 
  
  // CRF Fields
  CRF_HAS_THUMBNAILS("has_thumbnails"),
  CRF_HAS_LANDING_PAGE("has_landingpage"),
  CRF_HAS_MEDIA("has_media"),
  CRF_IS_FULL_TEXT("is_fulltext"),
  CRF_FILTER_TAGS("filter_tags"),
  CRF_FACET_TAGS("facet_tags"),
  
  // Provider Aggregation Fields
  PROVIDER_AGGREGATION_EDM_DATA_PROVIDER("provider_aggregation_edm_dataProvider"), 
  PROVIDER_AGGREGATION_EDM_HASVIEW("provider_aggregation_edm_hasView"),
  PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY("provider_aggregation_edm_isShownBy"), 
  PROVIDER_AGGREGATION_EDM_IS_SHOWN_AT("provider_aggregation_edm_isShownAt"), 
  PROVIDER_AGGREGATION_EDM_OBJECT("provider_aggregation_edm_object"), 
  PROVIDER_AGGREGATION_EDM_PROVIDER("provider_aggregation_edm_provider"),
  PROVIDER_AGGREGATION_EDM_INTERMEDIATE_PROVIDER("provider_aggregation_edm_intermediateProvider"),
  PROVIDER_AGGREGATION_DC_RIGHTS("provider_aggregation_dc_rights"), 
  PROVIDER_AGGREGATION_EDM_RIGHTS("provider_aggregation_edm_rights"), 
  PROVIDER_AGGREGATION_CC_LICENSE("provider_aggregation_cc_license"),
  PROVIDER_AGGREGATION_ODRL_INHERITED_FROM("provider_aggregation_odrl_inherited_from"),
  PROVIDER_AGGREGATION_CC_DEPRECATED_ON("provider_aggregation_cc_deprecated_on"),
  PREVIEW_NO_DISTRIBUTE("edm_previewNoDistribute"), 
  
  // EUROPEANA AGGREGATION
  EUROPEANA_AGGREGATION_EDM_COUNTRY("europeana_aggregation_edm_country"), 
  EUROPEANA_AGGREGATION_EDM_LANGUAGE("europeana_aggregation_edm_language"),
  
  // WEB RESOURCE FIELDS
  EDM_WEB_RESOURCE("edm_webResource"), 
  WR_DC_RIGHTS("wr_dc_rights"), 
  WR_EDM_RIGHTS("wr_edm_rights"), 
  WR_EDM_IS_NEXT_IN_SEQUENCE("wr_edm_isNextInSequence"),
  WR_CC_LICENSE("wr_cc_license"),
  WR_CC_DEPRECATED_ON("wr_cc_deprecated_on"),
  WR_SVCS_HAS_SERVICE("wr_svcs_hasservice"),
  WR_DCTERMS_ISREFERENCEDBY("wr_dcterms_isReferencedBy"),
  WR_CC_ODRL_INHERITED_FROM("wr_cc_odrl_inherited_from"),

  //SERVICE
  SV_SERVICE("svcs_service"),
  SV_DCTERMS_CONFORMS_TO("sv_dcterms_conformsTo"),

  // PROVIDER PROXY
  PROXY_DC_COVERAGE("proxy_dc_coverage"), 
  PROXY_DC_CONTRIBUTOR("proxy_dc_contributor"), 
  PROXY_DC_DESCRIPTION("proxy_dc_description"), 
  PROXY_DC_CREATOR("proxy_dc_creator"), 
  PROXY_DC_DATE("proxy_dc_date"), 
  PROXY_DC_FORMAT("proxy_dc_format"), 
  PROXY_DC_IDENTIFIER("proxy_dc_identifier"), 
  PROXY_DC_LANGUAGE("proxy_dc_language"), 
  PROXY_DC_PUBLISHER("proxy_dc_publisher"), 
  PROXY_DC_RIGHTS("proxy_dc_rights"),
  PROXY_DC_SOURCE("proxy_dc_source"), 
  PROXY_DC_SUBJECT("proxy_dc_subject"), 
  PROXY_DC_TITLE("proxy_dc_title"), 
  PROXY_DC_TYPE("proxy_dc_type"), 
  PROXY_DCTERMS_ALTERNATIVE("proxy_dcterms_alternative"), 
  PROXY_DCTERMS_CREATED("proxy_dcterms_created"), 
  PROXY_DCTERMS_HAS_PART("proxy_dcterms_hasPart"), 
  PROXY_DCTERMS_IS_PART_OF("proxy_dcterms_isPartOf"), 
  PROXY_DCTERMS_ISSUED("proxy_dcterms_issued"), 
  PROXY_DCTERMS_MEDIUM("proxy_dcterms_medium"), 
  PROXY_DCTERMS_PROVENANCE("proxy_dcterms_provenance"), 
  PROXY_DCTERMS_SPATIAL("proxy_dcterms_spatial"), 
  PROXY_DCTERMS_TEMPORAL("proxy_dcterms_temporal"), 
  EDM_UGC("edm_UGC"),
  PROXY_EDM_CURRENT_LOCATION("proxy_edm_currentLocation"),
  PROXY_EDM_HAS_MET("proxy_edm_hasMet"),
  PROXY_EDM_ISRELATEDTO("proxy_edm_isRelatedTo"), 
  PROXY_EDM_YEAR("proxy_edm_year"),
  PROVIDER_EDM_TYPE("proxy_edm_type"),

  //SKOS_CONCEPT
  SKOS_CONCEPT("skos_concept"), 
  CC_SKOS_PREF_LABEL("cc_skos_prefLabel"), 
  CC_SKOS_ALT_LABEL("cc_skos_altLabel"), 
  
  //PLACE
  EDM_PLACE("edm_place"), 
  PL_SKOS_PREF_LABEL("pl_skos_prefLabel"), 
  PL_SKOS_ALT_LABEL("pl_skos_altLabel"), 
  PL_WGS84_POS_LAT("pl_wgs84_pos_lat"),
  PL_WGS84_POS_LONG("pl_wgs84_pos_long"),
  PL_WGS84_POS_ALT("pl_wgs84_pos_alt"),
  
  //TIMESPAN
  EDM_TIMESPAN("edm_timespan"), 
  TS_SKOS_PREF_LABEL("ts_skos_prefLabel"), 
  TS_SKOS_ALT_LABEL("ts_skos_altLabel"), 
  
  //EDM_AGENT
  EDM_AGENT("edm_agent"),
  AG_SKOS_PREF_LABEL("ag_skos_prefLabel"), 
  AG_SKOS_ALT_LABEL("ag_skos_altLabel"), 
  AG_FOAF_NAME("ag_foaf_name"),
  AG_RDAGR2_DATEOFBIRTH("ag_rdagr2_dateOfBirth"),
  AG_RDAGR2_DATEOFDEATH("ag_rdagr2_dateOfDeath"),
  AG_RDAGR2_PLACEOFBIRTH("ag_rdagr2_placeOfBirth"),
  AG_RDAGR2_PLACEOFDEATH("ag_rdagr2_placeOfDeath"),
  AG_RDAGR2_PROFESSIONOROCCUPATION("ag_rdagr2_professionOrOccupation");
  
  private final String label;

  EdmLabel(String label) {
    this.label = label;
  }

  /**
   * Return the field value
   */
  @Override
  public String toString() {
    return label;
  }
}
