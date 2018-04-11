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
  PROVIDER_AGGREGATION_ORE_AGGREGATION("provider_aggregation_ore_aggregation"), 
  PROVIDER_AGGREGATION_ORE_AGGREGATES("provider_aggregation_ore_aggregates"), 
  PROVIDER_AGGREGATION_EDM_AGGREGATED_CHO("provider_aggregation_edm_aggregatedCHO"), 
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
  EDM_EUROPEANA_AGGREGATION("edm_europeana_aggregation"), 
  EUROPEANA_AGGREGATION_ORE_AGGREGATEDCHO("europeana_aggregation_edm_aggregatedCHO"),
  EUROPEANA_AGGREGATION_ORE_AGGREGATES("europeana_aggregation_ore_aggregates"), 
  EUROPEANA_AGGREGATION_DC_CREATOR("europeana_aggregation_dc_creator"), 
  EUROPEANA_AGGREGATION_EDM_LANDINGPAGE("europeana_aggregation_edm_landingPage"), 
  EUROPEANA_AGGREGATION_EDM_ISSHOWNBY("europeana_aggregation_edm_isShownBy"), 
  EUROPEANA_AGGREGATION_EDM_HASVIEW("europeana_aggregation_edm_hasView"), 
  EUROPEANA_AGGREGATION_EDM_COUNTRY("europeana_aggregation_edm_country"), 
  EUROPEANA_AGGREGATION_EDM_LANGUAGE("europeana_aggregation_edm_language"), 
  EUROPEANA_AGGREGATION_EDM_PREVIEW("europeana_aggregation_edm_preview"),
  
  // WEB RESOURCE FIELDS
  EDM_WEB_RESOURCE("edm_webResource"), 
  WR_DC_RIGHTS("wr_dc_rights"), 
  WR_EDM_RIGHTS("wr_edm_rights"), 
  WR_DC_DESCRIPTION("wr_dc_description"), 
  WR_DC_FORMAT("wr_dc_format"), 
  WR_DC_SOURCE("wr_dc_source"), 
  WR_DC_CREATOR("wr_dc_creator"),
  WR_DC_TYPE("wr_dc_type"),
  WR_DCTERMS_EXTENT("wr_dcterms_extent"), 
  WR_DCTERMS_ISSUED("wr_dcterms_issued"), 
  WR_DCTERMS_CONFORMSTO("wr_dcterms_conformsTo"), 
  WR_DCTERMS_CREATED("wr_dcterms_created"), 
  WR_DCTERMS_ISFORMATOF("wr_dcterms_isFormatOf"), 
  WR_DCTERMS_HAS_PART("wr_dcterms_hasPart"), 
  WR_EDM_IS_NEXT_IN_SEQUENCE("wr_edm_isNextInSequence"),
  WR_OWL_SAMEAS("wr_owl_sameAs"),
  WR_CC_LICENSE("wr_cc_license"),
  WR_ODRL_INHERITED_FROM("wr_odrl_inherited_from"),
  WR_CC_DEPRECATED_ON("wr_cc_deprecated_on"),
  WR_SVCS_HAS_SERVICE("wr_svcs_hasservice"),
  WR_EDM_PREVIEW("wr_edm_preview"),
  WR_DCTERMS_ISREFERENCEDBY("wr_dcterms_isReferencedBy"),

  //SERVICE
  SV_SERVICE("svcs_service"),
  SV_DCTERMS_CONFORMS_TO("sv_dcterms_conformsTo"),
  SV_DOAP_IMPLEMENTS("sv_doap_implements"),

  // PROVIDER PROXY
  ORE_PROXY("proxy_ore_proxy"), 
  EDM_ISEUROPEANA_PROXY("edm_europeana_proxy"),
  PROXY_OWL_SAMEAS("proxy_owl_sameAs"), 
  PROXY_DC_COVERAGE("proxy_dc_coverage"), 
  PROXY_DC_CONTRIBUTOR("proxy_dc_contributor"), 
  PROXY_DC_DESCRIPTION("proxy_dc_description"), 
  PROXY_DC_CREATOR("proxy_dc_creator"), 
  PROXY_DC_DATE("proxy_dc_date"), 
  PROXY_DC_FORMAT("proxy_dc_format"), 
  PROXY_DC_IDENTIFIER("proxy_dc_identifier"), 
  PROXY_DC_LANGUAGE("proxy_dc_language"), 
  PROXY_DC_PUBLISHER("proxy_dc_publisher"), 
  PROXY_DC_RELATION("proxy_dc_relation"), 
  PROXY_DC_RIGHTS("proxy_dc_rights"), 
  PROXY_EDM_RIGHTS("proxy_edm_rights"), 
  PROXY_DC_SOURCE("proxy_dc_source"), 
  PROXY_DC_SUBJECT("proxy_dc_subject"), 
  PROXY_DC_TITLE("proxy_dc_title"), 
  PROXY_DC_TYPE("proxy_dc_type"), 
  PROXY_DCTERMS_ALTERNATIVE("proxy_dcterms_alternative"), 
  PROXY_DCTERMS_CREATED("proxy_dcterms_created"), 
  PROXY_DCTERMS_CONFORMS_TO("proxy_dcterms_conformsTo"), 
  PROXY_DCTERMS_EXTENT("proxy_dcterms_extent"), 
  PROXY_DCTERMS_HAS_FORMAT("proxy_dcterms_hasFormat"), 
  PROXY_DCTERMS_HAS_PART("proxy_dcterms_hasPart"), 
  PROXY_DCTERMS_HAS_VERSION("proxy_dcterms_hasVersion"), 
  PROXY_DCTERMS_IS_FORMAT_OF("proxy_dcterms_isFormatOf"), 
  PROXY_DCTERMS_IS_PART_OF("proxy_dcterms_isPartOf"), 
  PROXY_DCTERMS_IS_REFERENCED_BY("proxy_dcterms_isReferencedBy"), 
  PROXY_DCTERMS_IS_REPLACED_BY("proxy_dcterms_isReplacedBy"), 
  PROXY_DCTERMS_IS_REQUIRED_BY("proxy_dcterms_isRequiredBy"), 
  PROXY_DCTERMS_ISSUED("proxy_dcterms_issued"), 
  PROXY_DCTERMS_IS_VERSION_OF("proxy_dcterms_isVersionOf"), 
  PROXY_DCTERMS_MEDIUM("proxy_dcterms_medium"), 
  PROXY_DCTERMS_PROVENANCE("proxy_dcterms_provenance"), 
  PROXY_DCTERMS_REFERENCES("proxy_dcterms_references"), 
  PROXY_DCTERMS_REPLACES("proxy_dcterms_replaces"), 
  PROXY_DCTERMS_REQUIRES("proxy_dcterms_requires"), 
  PROXY_DCTERMS_SPATIAL("proxy_dcterms_spatial"), 
  PROXY_DCTERMS_TABLE_OF_CONTENTS("proxy_dcterms_tableOfContents"), 
  PROXY_DCTERMS_TEMPORAL("proxy_dcterms_temporal"), 
  EDM_UGC("edm_UGC"), 
  PROXY_EDM_CURRENT_LOCATION("proxy_edm_currentLocation"),
  PROXY_EDM_IS_NEXT_IN_SEQUENCE("proxy_edm_isNextInSequence"), 
  PROXY_EDM_HAS_TYPE("proxy_edm_hasType"), 
  PROXY_EDM_INCORPORATES("proxy_edm_incorporates"), 
  PROXY_EDM_ISDERIVATIVE_OF("proxy_edm_isDerivativeOf"), 
  PROXY_EDM_ISRELATEDTO("proxy_edm_isRelatedTo"), 
  PROXY_EDM_ISREPRESENTATIONOF("proxy_edm_isRepresentationOf"), 
  PROXY_EDM_ISSIMILARTO("proxy_edm_isSimilarTo"), 
  PROXY_EDM_ISSUCCESSOROF("proxy_edm_isSuccessorOf"), 
  PROXY_EDM_REALIZES("proxy_edm_realizes"), 
  PROXY_EDM_WASPRESENTAT("proxy_edm_wasPresentAt"),
  PROXY_ORE_PROXY_IN("proxy_ore_proxyIn"), 
  PROXY_ORE_PROXY_FOR("proxy_ore_proxyFor"),
  PROXY_EDM_YEAR("proxy_edm_year"),
  PROVIDER_EDM_TYPE("proxy_edm_type"),

  //SKOS_CONCEPT
  SKOS_CONCEPT("skos_concept"), 
  CC_SKOS_PREF_LABEL("cc_skos_prefLabel"), 
  CC_SKOS_ALT_LABEL("cc_skos_altLabel"), 
  CC_SKOS_NOTE("cc_skos_note"), 
  CC_SKOS_BROADER("cc_skos_broader"), 
  CC_SKOS_HIDDEN_LABEL("cc_skos_hiddenLabel"),
  CC_SKOS_NARROWER("cc_skos_narrower"),
  CC_SKOS_RELATED("cc_skos_related"),
  CC_SKOS_BROADMATCH("cc_skos_broadMatch"),
  CC_SKOS_NARROWMATCH("cc_skos_narrowMatch"),
  CC_SKOS_RELATEDMATCH("cc_skos_relatedMatch"),
  CC_SKOS_EXACTMATCH("cc_skos_exactMatch"),
  CC_SKOS_CLOSEMATCH("cc_skos_closeMatch"),
  CC_SKOS_NOTATIONS("cc_skos_notation"),
  CC_SKOS_INSCHEME("cc_skos_inScheme"),
  
  //PLACE
  EDM_PLACE("edm_place"), 
  PL_SKOS_PREF_LABEL("pl_skos_prefLabel"), 
  PL_SKOS_ALT_LABEL("pl_skos_altLabel"), 
  PL_SKOS_NOTE("pl_skos_note"), 
  PL_DCTERMS_ISPART_OF("pl_dcterms_isPartOf"), 
  PL_WGS84_POS_LAT("pl_wgs84_pos_lat"),
  PL_WGS84_POS_LONG("pl_wgs84_pos_long"),
  PL_WGS84_POS_ALT("pl_wgs84_pos_alt"),
  PL_SKOS_HIDDENLABEL("pl_skos_hiddenLabel"),
  PL_DCTERMS_HASPART("pl_dcterms_hasPart"),
  PL_OWL_SAMEAS("pl_owl_sameAs"),
  
  //TIMESPAN
  EDM_TIMESPAN("edm_timespan"), 
  TS_SKOS_PREF_LABEL("ts_skos_prefLabel"), 
  TS_SKOS_ALT_LABEL("ts_skos_altLabel"), 
  TS_SKOS_NOTE("ts_skos_note"), 
  TS_EDM_BEGIN("ts_edm_begin"), 
  TS_EDM_END("ts_edm_end"), 
  TS_DCTERMS_ISPART_OF("ts_dcterms_isPartOf"), 
  TS_SKOS_HIDDENLABEL("ts_skos_hiddenLabel"),
  TS_DCTERMS_HASPART("ts_dcterms_hasPart"),
  
  //EDM_AGENT
  EDM_AGENT("edm_agent"),
  AG_DC_DATE("ag_dc_date"),
  AG_DC_IDENTIFIER("ag_dc_identifier"),
  AG_SKOS_PREF_LABEL("ag_skos_prefLabel"), 
  AG_SKOS_ALT_LABEL("ag_skos_altLabel"), 
  AG_SKOS_NOTE("ag_skos_note"), 
  AG_EDM_BEGIN("ag_edm_begin"),
  AG_EDM_END("ag_edm_end"), 
  AG_EDM_WASPRESENTAT("ag_edm_wasPresentAt"),
  AG_EDM_HASMET("ag_edm_hasMet"),
  AG_EDM_ISRELATEDTO("ag_edm_isRelatedTo"),
  AG_OWL_SAMEAS("ag_owl_sameAs"),
  AG_FOAF_NAME("ag_foaf_name"),
  AG_RDAGR2_DATEOFBIRTH("ag_rdagr2_dateOfBirth"),
  AG_RDAGR2_DATEOFDEATH("ag_rdagr2_dateOfDeath"),
  AG_RDAGR2_PLACEOFBIRTH("ag_rdagr2_placeOfBirth"),
  AG_RDAGR2_PLACEOFDEATH("ag_rdagr2_placeOfDeath"),
  AG_RDAGR2_DATEOFESTABLISHMENT("ag_rdagr2_dateOfEstablishment"),
  AG_RDAGR2_DATEOFTERMINATION("ag_rdagr2_dateOfTermination"),
  AG_RDAGR2_GENDER("ag_rdagr2_gender"),
  AG_RDAGR2_PROFESSIONOROCCUPATION("ag_rdagr2_professionOrOccupation"),
  AG_RDAGR2_BIOGRAPHICALINFORMATION("ag_rdagr2_biographicalInformation");
  
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
