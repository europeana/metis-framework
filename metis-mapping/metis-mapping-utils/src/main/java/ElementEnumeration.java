import eu.europeana.metis.mapping.validation.FlagType;
import eu.europeana.metis.mapping.validation.IsBooleanFunction;
import eu.europeana.metis.mapping.validation.IsDateTypeFunction;
import eu.europeana.metis.mapping.validation.IsEnumerationFunction;
import eu.europeana.metis.mapping.validation.IsFloatFunction;
import eu.europeana.metis.mapping.validation.IsLanguageFunction;
import eu.europeana.metis.mapping.validation.IsUriFunction;
import eu.europeana.metis.mapping.validation.IsUrlFunction;
import eu.europeana.metis.mapping.validation.ValidationFunction;
import eu.europeana.metis.mapping.validation.ValidationRule;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by ymamakis on 9/14/16.
 */
public enum ElementEnumeration {


    PCHO("edm:ProvidedCHO") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }

    },
    PCHO_ABOUT("edm:ProvidedCHO/@rdf:about") {
        @Override
        public List<ValueDTO> getSourceFields() {

            return ElementEnumeration.values("edm:ProvidedCHO/@rdf:about", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }
    },
    CC_LICENCE("cc:License") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("cc:License", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }

    },
    CC_LICENCE_ABOUT("cc:License/@rdf:about") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("cc:License/@rdf:about", false);

        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }
    },
    CC_LICENCE_DEPRECATED("cc:License/cc:deprecatedOn") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("cc:License/cc:deprecatedOn", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    CC_LICENCE_DEPRECATED_DATETYPE("cc:License/cc:deprecatedOn/@rdf:datatype") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("cc:License/cc:deprecatedOn/@rdf:datatype", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    CC_LICENCE_INHERITED("cc:License/odrl:inheritFrom") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("cc:License/odrl:inheritFrom", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    CC_LICENCE_INHERITED_RESOURCE("cc:License/odrl:inheritFrom/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("cc:License/odrl:inheritFrom/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUrlRules();
        }
    },
    EDM_AGENT("edm:Agent") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }

    },
    EDM_AGENT_ABOUT("edm:Agent/@rdf:about") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/@rdf:about", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }
    },
    EDM_AGENT_DATE("edm:Agent/dc:date") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/dc:date", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_DATE_RESOURCE("edm:Agent/dc:date/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/dc:date/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }
    },
    EDM_AGENT_DATE_LANG("edm:Agent/dc:date/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/dc:date/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }
    },
    EDM_AGENT_IDENTIFIER("edm:Agent/dc:identifier") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/dc:identifier", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }

    },
    EDM_AGENT_IDENTIFIER_LANG("edm:Agent/dc:identifier/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/dc:identifier/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }
    },
    EDM_AGENT_HASPART("edm:Agent/dcterms:hasPart") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/dcterms:hasPart", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_HASPART_RESOURCE("edm:Agent/dcterms:hasPart/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/dcterms:hasPart/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }
    },
    EDM_AGENT_ISPARTOF("edm:Agent/dcterms:isPartOf") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/dcterms:isPartOf", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_ISPARTOF_RESOURCE("edm:Agent/dcterms:isPartOf/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/dcterms:isPartOf/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }
    },
    EDM_AGENT_BEGIN("edm:Agent/edm:begin") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/edm:begin", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_BEGIN_LANG("edm:Agent/edm:begin/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/edm:begin/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }
    },
    EDM_AGENT_END("edm:Agent/edm:end") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/edm:end", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_END_LANG("edm:Agent/edm:end/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/edm:end/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }
    },
    EDM_AGENT_HASMET("edm:Agent/edm:hasMet") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/edm:hasMet", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_HASMET_RESOURCE("edm:Agent/edm:hasMet/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/edm:hasMet/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }
    },
    EDM_AGENT_ISRELATEDTO("edm:Agent/edm:isRelatedTo") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/edm:isRelatedTo", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_ISRELATEDTO_RESOURCE("edm:Agent/edm:isRelatedTo/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/edm:isRelatedTo/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_AGENT_WASPRESENTAT("edm:Agent/edm:wasPresentAt") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/edm:wasPresentAt", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_WASPRESENTAT_RESOURCE("edm:Agent/edm:wasPresentAt/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/edm:wasPresentAt/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }
    },
    EDM_AGENT_NAME("edm:Agent/foaf:name") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/foaf:name", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_NAME_LANG("edm:Agent/foaf:name/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/foaf:name/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_AGENT_SAMEAS("edm:Agent/owl:sameAs") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/owl:sameAs", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }

    },
    EDM_AGENT_SAMEAS_RESOURCE("edm:Agent/owl:sameAs/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/owl:sameAs/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_AGENT_BIO("edm:Agent/rdaGr2:biographicalInformation") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:biographicalInformation", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }

    },
    EDM_AGENT_BIO_LANG("edm:Agent/rdaGr2:biographicalInformation/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:biographicalInformation/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }


    },
    EDM_AGENT_DOB("edm:Agent/rdaGr2:dateOfBirth") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:dateOfBirth", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_DOB_LANG("edm:Agent/rdaGr2:dateOfBirth/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:dateOfBirth/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_AGENT_DOD("edm:Agent/rdaGr2:dateOfDeath") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:dateOfDeath", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }

    },
    EDM_AGENT_DOD_LANG("edm:Agent/rdaGr2:dateOfDeath/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:dateOfDeath/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_AGENT_DOE("edm:Agent/rdaGr2:dateOfEstablishment") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:dateOfEstablishment", false);
        }


        @Override
        public List<ValidationRule> getRules() {
            return null;
        }


    },
    EDM_AGENT_DOE_LANG("edm:Agent/rdaGr2:dateOfEstablishment/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:dateOfEstablishment/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }
    },
    EDM_AGENT_DOT("edm:Agent/rdaGr2:dateOfTermination") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:dateOfTermination", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_DOT_LANG("edm:Agent/rdaGr2:dateOfTermination/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:dateOfTermination/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_AGENT_GENDER("edm:Agent/rdaGr2:gender") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:gender", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }

    },
    EDM_AGENT_GENDER_LANG("edm:Agent/rdaGr2:gender/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:gender/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }
    },
    EDM_AGENT_POB("edm:Agent/rdaGr2:placeOfBirth") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:placeOfBirth", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_POB_LANG("edm:Agent/rdaGr2:placeOfBirth/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:placeOfBirth/@xml:lang", false);
        }
        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }
    },
    EDM_AGENT_POB_RESOURCE("edm:Agent/rdaGr2:placeOfBirth/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:placeOfBirth/@rdf:resource", false);
        }
        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }
    },
    EDM_AGENT_POD("edm:Agent/rdaGr2:placeOfDeath") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:placeOfDeath", false);
        }
        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_POD_LANG("edm:Agent/rdaGr2:placeOfDeath/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:placeOfDeath/@xml:lang", false);
        }
        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }
    },
    EDM_AGENT_POD_RESOURCE("edm:Agent/rdaGr2:placeOfDeath/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:placeOfDeath/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }
    },
    EDM_AGENT_POO("edm:Agent/rdaGr2:professionOrOccupation") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:professionOrOccupation", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_POO_LANG("edm:Agent/rdaGr2:professionOrOccupation/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:professionOrOccupation/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_AGENT_POO_RESOURCE("edm:Agent/rdaGr2:professionOrOccupation/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/rdaGr2:professionOrOccupation/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }
    },
    EDM_AGENT_ALT("edm:Agent/skos:altLabel") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/skos:altLabel", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_ALT_LANG("edm:Agent/skos:altLabel/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/skos:altLabel/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_AGENT_NOTE("edm:Agent/skos:note") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/skos:note", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_NOTE_LANG("edm:Agent/skos:note/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/skos:note/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_AGENT_PREF("edm:Agent/skos:prefLabel") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/skos:prefLabel", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_AGENT_PREF_LANG("edm:Agent/skos:prefLabel/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Agent/skos:prefLabel/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_PLACE("edm:Place") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_PLACE_ABOUT("edm:Place/@rdf:about") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/@rdf:about", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_PLACE_HASPART("edm:Place/dcterms:hasPart") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/dcterms:hasPart", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_PLACE_HASPART_RESOURCE("edm:Place/dcterms:hasPart/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/dcterms:hasPart/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_PLACE_ISPARTOF("edm:Place/dcterms:isPartOf") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/dcterms:isPartOf", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_PLACE_ISPARTOF_RESOURCE("edm:Place/dcterms:isPartOf/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/dcterms:isPartOf/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_PLACE_ISNEXTINSEQUENCE("edm:Place/edm:isNextInSequence") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/edm:isNextInSequence", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_PLACE_ISNEXTINSEQUENCE_RESOURCE("edm:Place/edm:isNextInSequence/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/edm:isNextInSequence/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_PLACE_SAMEAS("edm:Place/owl:sameAs") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/owl:sameAs", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_PLACE_SAMEAS_RESOURCE("edm:Place/owl:sameAs/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/owl:sameAs/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_PLACE_ALT("edm:Place/skos:altLabel") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/skos:altLabel", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_PLACE_ALT_LANG("edm:Place/skos:altLabel/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/skos:altLabel/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_PLACE_NOTE("edm:Place/skos:note") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/skos:note", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_PLACE_NOTE_LANG("edm:Place/skos:note/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/skos:note/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_PLACE_PREF("edm:Place/skos:prefLabel") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/skos:prefLabel", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_PLACE_PREF_LANG("edm:Place/skos:prefLabel/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/skos:prefLabel/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_PLACE_POS_ALT("edm:Place/wgs84_pos:alt") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/skos:wgs84_pos:alt", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructFloatRules();
        }
    },
    EDM_PLACE_POS_LAT("edm:Place/wgs84_pos:lat") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/skos:wgs84_pos:lat", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructFloatRules();
        }
    },
    EDM_PLACE_POS_LONG("edm:Place/wgs84_pos:long") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:Place/skos:wgs84_pos:long", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructFloatRules();
        }
    },
    EDM_CHO_SAMEAS("edm:ProvidedCHO/owl:sameAs") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/owl:sameAs", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_CHO_SAMEAS_RESOURCE("edm:ProvidedCHO/owl:sameAs/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/owl:sameAs/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_TIMESPAN("edm:TimeSpan") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_TIMESPAN_ABOUT("edm:TimeSpan/@rdf:about") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/@rdf:about", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_TIMESPAN_HASPART("edm:TimeSpan/dcterms:hasPart") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/dcterms:hasPart", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_TIMESPAN_HASPART_RESOURCE("edm:TimeSpan/dcterms:hasPart/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/dcterms:hasPart/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_TIMESPAN_ISPARTOF("edm:TimeSpan/dcterms:isPartOf") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/dcterms:isPartOf", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_TIMESPAN_ISPARTOF_RESOURCE("edm:TimeSpan/dcterms:isPartOf/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/dcterms:isPartOf/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_TIMESPAN_BEGIN("edm:TimeSpan/edm:begin") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/edm:begin", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_TIMESPAN_BEGIN_LANG("edm:TimeSpan/edm:begin/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/edm:begin/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_TIMESPAN_END("edm:TimeSpan/edm:end") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/edm:end", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_TIMESPAN_END_LANG("edm:TimeSpan/edm:end/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/edm:end/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_TIMESPAN_ISNEXTINSEQUENCE("edm:TimeSpan/edm:isNextInSequence") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/edm:isNextInSequence", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_TIMESPAN_ISNEXTINSEQUENCE_RESOURCE("edm:TimeSpan/edm:isNextInSequence/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/edm:isNextInSequence/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_TIMESPAN_SAMEAS("edm:TimeSpan/owl:sameAs") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/owl:sameAs", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_TIMESPAN_SAMEAS_RESOURCE("edm:TimeSpan/owl:sameAs/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/owl:sameAs/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_TIMESPAN_ALT("edm:TimeSpan/skos:altLabel") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/skos:altLabel", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_TIMESPAN_ALT_LANG("edm:TimeSpan/skos:altLabel/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/skos:altLabel/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_TIMESPAN_NOTE("edm:TimeSpan/skos:note") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/skos:note", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_TIMESPAN_NOTE_LANG("edm:TimeSpan/skos:note/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/skos:note/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_TIMESPAN_PREF("edm:TimeSpan/skos:prefLabel") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/skos:prefLabel", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_TIMESPAN_PREF_LANG("edm:TimeSpan/skos:prefLabel/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:TimeSpan/skos:prefLabel/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_WR("edm:WebResource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_ABOUT("edm:WebResource/@rdf:about") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/@rdf:about", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_CREATOR("edm:WebResource/dc:creator") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:creator", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_CREATOR_RESOURCE("edm:WebResource/dc:creator/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:creator/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_CREATOR_LANG("edm:WebResource/dc:creator/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:creator/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_WR_DESCRIPTION("edm:WebResource/dc:description") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:description", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_DESCRIPTION_RESOURCE("edm:WebResource/dc:description/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:description/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_DESCRIPTION_LANG("edm:WebResource/dc:description/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:description/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_WR_FORMAT("edm:WebResource/dc:format") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:format", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_FORMAT_RESOURCE("edm:WebResource/dc:format/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:format/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_FORMAT_LANG("edm:WebResource/dc:format/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:format/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_WR_RIGHTS("edm:WebResource/dc:rights") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:rights", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_RIGHTS_RESOURCE("edm:WebResource/dc:rights/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:rights/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_RIGHTS_LANG("edm:WebResource/dc:rights/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:rights/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_WR_SOURCE("edm:WebResource/dc:source") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:source", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_SOURCE_RESOURCE("edm:WebResource/dc:source/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:source/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_SOURCE_LANG("edm:WebResource/dc:source/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dc:source/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_WR_CONFORMSTO("edm:WebResource/dcterms:conformsTo") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:conformsTo", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_CONFORMSTO_RESOURCE("edm:WebResource/dcterms:conformsTo/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:conformsTo/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_CONFORMSTO_LANG("edm:WebResource/dcterms:conformsTo/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:conformsTo/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_WR_CREATED("edm:WebResource/dcterms:created") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:created", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_CREATED_RESOURCE("edm:WebResource/dcterms:created/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:created/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_CREATED_LANG("edm:WebResource/dcterms:created/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:created/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_WR_EXTENT("edm:WebResource/dcterms:extent") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:extent", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_EXTENT_RESOURCE("edm:WebResource/dcterms:extent/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:extent/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_EXTENT_LANG("edm:WebResource/dcterms:extent/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:extent/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_WR_HASPART("edm:WebResource/dcterms:hasPart") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:hasPart", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_HASPART_RESOURCE("edm:WebResource/dcterms:hasPart/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:hasPart/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },

    EDM_WR_ISFORMATOF("edm:WebResource/dcterms:isFormatOf") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:isFormatOf", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_ISFORMATOF_RESOURCE("edm:WebResource/dcterms:isFormatOf/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:isFormatOf/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_ISFORMATOF_LANG("edm:WebResource/dcterms:isFormatOf/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:isFormatOf/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_WR_ISPARTOF("edm:WebResource/dcterms:isPartOf") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:isPartOf", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_ISPARTOF_RESOURCE("edm:WebResource/dcterms:isPartOf/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:isPartOf/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_ISREFERENCEBY("edm:WebResource/dcterms:isReferencedBy") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:isReferencedBy", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_ISREFERENCEDBY_RESOURCE("edm:WebResource/dcterms:isReferencedBy/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:isReferencedBy/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_ISREFERENCEDBY_LANG("edm:WebResource/dcterms:isReferencedBy/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:isReferencedBy/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_WR_ISSUED("edm:WebResource/dcterms:issued") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:issued", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_ISSUED_RESOURCE("edm:WebResource/dcterms:issued/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:issued/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_ISSUED_LANG("edm:WebResource/dcterms:issued/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/dcterms:issued/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    EDM_WR_ISNEXTINSEQUENCE("edm:WebResource/edm:isNextInSequence") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/edm:isNextInSequence", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_ISNEXTINSEQUENCE_RESOURCE("edm:WebResource/edm:isNextInSequence/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/edm:isNextInSequence/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_EDM_RIGHTS("edm:WebResource/edm:rights") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/edm:rights", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_EDM_RIGHTS_RESOURCE("edm:WebResource/edm:rights/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/edm:rights/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_SERVICE("edm:WebResource/svcs:has_service") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/svcs:has_service", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_SERVICE_RESOURCE("edm:WebResource/svcs:has_service/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/svcs:has_service/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_WR_SAMEAS("edm:WebResource/owl:sameAs") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/owl:sameAs", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_WR_SAMEAS_RESOURCE("edm:WebResource/owl:sameAs/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:WebResource/owl:sameAs/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_EUAGGR("edm:EuropeanaAggregation") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_EUAGGR_ABOUT("edm:EuropeanaAggregation/@rdf:about") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/@rdf:about", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_EUAGGR_CREATOR("edm:EuropeanaAggregation/dc:creator") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("Europeana", true);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_EUAGGR_AGGRCHO("edm:EuropeanaAggregation/edm:aggregatedCHO") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:aggregatedCHO", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_EUAGGR_AGGRCHO_RESOURCE("edm:EuropeanaAggregation/edm:aggregatedCHO/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:aggregatedCHO/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    EDM_EUAGGR_COUNTRY("edm:EuropeanaAggregation/edm:country") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("Europe", true);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_EUAGGR_DATASETNAME("edm:EuropeanaAggregation/edm:datasetName") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("2029901", true);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    EDM_EUAGGR_LANGUAGE("edm:EuropeanaAggregation/edm:language") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("mul", true);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_AGGR("ore:Aggregation") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_AGGR_ABOUT("ore:Aggregation/@rdf:about") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/@rdf:about", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_AGGR_DC_RIGHTS("ore:Aggregation/dc:rights") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/dc:rights", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_AGGR_DC_RIGHTS_RESOURCE("ore:Aggregation/dc:rights/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/dc:rights/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_AGGR_DC_RIGHTS_LANG("ore:Aggregation/dc:rights/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/dc:rights/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_AGGR_AGCHO("ore:Aggregation/edm:aggregatedCHO") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:aggregatedCHO", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_AGGR_AGCHO_RESOURCE("ore:Aggregation/edm:aggregatedCHO/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:aggregatedCHO/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_AGGR_DATAPROVIDER("ore:Aggregation/edm:dataProvider") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:dataProvider", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_AGGR_DATAPROVIDER_RESOURCE("ore:Aggregation/edm:dataProvider/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:dataProvider/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_AGGR_DATAPROVIDER_LANG("ore:Aggregation/edm:dataProvider/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:dataProvider/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_AGGR_HASVIEW("ore:Aggregation/edm:hasView") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:hasView", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_AGGR_HASVIEW_RESOURCE("ore:Aggregation/edm:hasView/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:hasView/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_AGGR_INTERMEDIATEPROVIDER("ore:Aggregation/edm:intermediateProvider") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:intermediateProvider", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_AGGR_INTERMEDIATEPROVIDER_RESOURCE("ore:Aggregation/edm:intermediateProvider/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:intermediateProvider/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_AGGR_INTERMEDIATEPROVIDER_LANG("ore:Aggregation/edm:intermediateProvider/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:intermediateProvider/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_AGGR_ISSHOWNAT("ore:Aggregation/edm:isShownAt") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:isShownAt", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_AGGR_ISSHOWNAT_RESOURCE("ore:Aggregation/edm:isShownAt/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:isShownAt/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_AGGR_ISSHOWNBY("ore:Aggregation/edm:isShownBy") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:isShownBy", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_AGGR_ISSHOWNBY_RESOURCE("ore:Aggregation/edm:isShownBy/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:isShownBy/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_AGGR_OBJECT("ore:Aggregation/edm:object") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:object", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_AGGR_OBJECT_RESOURCE("ore:Aggregation/edm:object/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:object/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_AGGR_PROVIDER("ore:Aggregation/edm:provider") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:provider", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_AGGR_PROVIDER_RESOURCE("ore:Aggregation/edm:provider/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:provider/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_AGGR_PROVIDER_LANG("ore:Aggregation/edm:provider/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:provider/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_AGGR_EDM_RIGHTS("ore:Aggregation/edm:rights") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:rights", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_AGGR_EDM_RIGHTS_RESOURCE("ore:Aggregation/edm:rights/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:rights/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_AGGR_EDM_UGC("ore:Aggregation/edm:ugc") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/edm:ugc", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructBooleanRules();
        }
    },
    ORE_PROXY("ore:Proxy") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ABOUT("ore:Proxy/@rdf:about") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/@rdf:about", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },

    ORE_PROXY_CONTRIBUTOR("ore:Proxy/dc:contributor") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:contributor", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_CONTRIBUTOR_RESOURCE("ore:Proxy/dc:contributor/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:contributor/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_CONTRIBUTOR_LANG("ore:Proxy/dc:contributor/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:contributor/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_CREATOR("ore:Proxy/dc:creator") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:creator", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_CREATOR_RESOURCE("ore:Proxy/dc:creator/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:creator/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_CREATOR_LANG("ore:Proxy/dc:creator/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:creator/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_DATE("ore:Proxy/dc:date") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:date", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_DATE_RESOURCE("ore:Proxy/dc:date/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:date/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_DATE_LANG("ore:Proxy/dc:date/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:date/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_DESCRIPTION("ore:Proxy/dc:description") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:description", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_DESCRIPTION_RESOURCE("ore:Proxy/dc:description/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:description/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_DESCRIPTION_LANG("ore:Proxy/dc:description/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:description/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_FORMAT("ore:Proxy/dc:format") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:format", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_FORMAT_RESOURCE("ore:Proxy/dc:format/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:format/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_FORMAT_LANG("ore:Proxy/dc:format/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:format/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_IDENTIFIER("ore:Proxy/dc:identifier") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:identifier", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_IDENTIFIER_LANG("ore:Proxy/dc:identifier/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:identifier/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_LANGUAGE("ore:Proxy/dc:language") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:language", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_LANGUAGE_LANG("ore:Proxy/dc:language/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:language/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_PUBLISHER("ore:Proxy/dc:publisher") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:publisher", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_PUBLISHER_RESOURCE("ore:Proxy/dc:publisher/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:publisher/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_PUBLISHER_LANG("ore:Proxy/dc:publisher/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:publisher/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_RELATION("ore:Proxy/dc:relation") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:relation", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_RELATION_RESOURCE("ore:Proxy/dc:relation/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:relation/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_RELATION_LANG("ore:Proxy/dc:relation/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:relation/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_RIGHTS("ore:Proxy/dc:rights") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:rights", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_RIGHTS_RESOURCE("ore:Proxy/dc:rights/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:rights/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_RIGHTS_LANG("ore:Proxy/dc:rights/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:rights/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_SOURCE("ore:Proxy/dc:source") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:source", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_SOURCE_RESOURCE("ore:Proxy/dc:source/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:source/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_SOURCE_LANG("ore:Proxy/dc:source/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:source/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_SUBJECT("ore:Proxy/dc:subject") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:subject", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_SUBJECT_RESOURCE("ore:Proxy/dc:subject/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:subject/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_SUBJECT_LANG("ore:Proxy/dc:subject/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:subject/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_TITLE("ore:Proxy/dc:title") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:title", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_TITLE_RESOURCE("ore:Proxy/dc:title/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:title/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_TITLE_LANG("ore:Proxy/dc:title/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:title/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_TYPE("ore:Proxy/dc:type") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:type", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_TYPE_RESOURCE("ore:Proxy/dc:type/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:type/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_TYPE_LANG("ore:Proxy/dc:type/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dc:type/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_ALTERNATIVE("ore:Proxy/dcterms:alternative") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:alternative", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ALTERNATIVE_LANG("ore:Proxy/dcterms:alternative/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:alternative/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_CONFORMSTO("ore:Proxy/dcterms:conformsTo") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:conformsTo", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_CONFORMSTO_RESOURCE("ore:Proxy/dcterms:conformsTo/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:conformsTo/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_CONFORMSTO_LANG("ore:Proxy/dcterms:conformsTo/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:conformsTo/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_CREATED("ore:Proxy/dcterms:created") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:created", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_CREATED_RESOURCE("ore:Proxy/dcterms:created/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:created/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_CREATED_LANG("ore:Proxy/dcterms:created/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:created/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_EXTENT("ore:Proxy/dcterms:extent") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:extent", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_EXTENT_RESOURCE("ore:Proxy/dcterms:extent/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:extent/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_EXTENT_LANG("ore:Proxy/dcterms:extent/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:extent/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_HASFORMAT("ore:Proxy/dcterms:hasFormat") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:hasFormat", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_HASFORMAT_RESOURCE("ore:Proxy/dcterms:hasFormat/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:hasFormat/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_HASFORMAT_LANG("ore:Proxy/dcterms:hasFormat/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:hasFormat/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_HASPART("ore:Proxy/dcterms:hasPart") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:hasPart", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_HASPART_RESOURCE("ore:Proxy/dcterms:hasPart/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:hasPart/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_HASPART_LANG("ore:Proxy/dcterms:hasPart/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:hasPart/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_HASVERSION("ore:Proxy/dcterms:hasVersion") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:hasVersion", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_HASVERSION_RESOURCE("ore:Proxy/dcterms:hasVersion/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:hasVersion/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_HASVERSION_LANG("ore:Proxy/dcterms:hasVersion/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:hasVersion/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_ISFORMATOF("ore:Proxy/dcterms:isFormatOf") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isFormatOf", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ISFORMATOF_RESOURCE("ore:Proxy/dcterms:isFormatOf/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isFormatOf/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_ISFORMATOF_LANG("ore:Proxy/dcterms:isFormatOf/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isFormatOf/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_ISPARTOF("ore:Proxy/dcterms:isPartOf") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isPartOf", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ISPARTOF_RESOURCE("ore:Proxy/dcterms:isPartOf/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isPartOf/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_ISPARTOF_LANG("ore:Proxy/dcterms:isPartOf/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isPartOf/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_ISREFERENCEDBY("ore:Proxy/dcterms:isReferencedBy") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isReferencedBy", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ISREFERENCEDBY_RESOURCE("ore:Proxy/dcterms:isReferencedBy/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isReferencedBy/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_ISREFERENCEDBY_LANG("ore:Proxy/dcterms:isReferencedBy/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isReferencedBy/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_ISREPLACEDBY("ore:Proxy/dcterms:isReplacedBy") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isReplacedBy", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ISREPLACEDBY_RESOURCE("ore:Proxy/dcterms:isReplacedBy/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isReplacedBy/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_ISREPLACEDBY_LANG("ore:Proxy/dcterms:isReplacedBy/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isReplacedBy/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_ISREQUIREDBY("ore:Proxy/dcterms:isRequiredBy") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isRequiredBy", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ISREQUIREDBY_RESOURCE("ore:Proxy/dcterms:isRequiredBy/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isRequiredBy/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_ISREQUIREDBY_LANG("ore:Proxy/dcterms:isRequiredBy/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isRequiredBy/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_ISSUED("ore:Proxy/dcterms:issued") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:issued", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ISSUED_RESOURCE("ore:Proxy/dcterms:issued/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:issued/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_ISSUED_LANG("ore:Proxy/dcterms:issued/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:issued/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_ISVERSIONOF("ore:Proxy/dcterms:isVersionOf") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isVersionOf", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ISVERSIONOF_RESOURCE("ore:Proxy/dcterms:isVersionOf/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isVersionOf/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_ISVERSIONOF_LANG("ore:Proxy/dcterms:isVersionOf/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:isVersionOf/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_MEDIUM("ore:Proxy/dcterms:medium") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:medium", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_MEDIUM_RESOURCE("ore:Proxy/dcterms:medium/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:medium/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_MEDIUM_LANG("ore:Proxy/dcterms:medium/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:medium/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_PROVENANCE("ore:Proxy/dcterms:provenance") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:provenance", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_PROVENANCE_RESOURCE("ore:Proxy/dcterms:provenance/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:provenance/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_PROVENANCE_LANG("ore:Proxy/dcterms:provenance/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:provenance/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_REFERENCES("ore:Proxy/dcterms:references") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:references", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_REFERENCES_RESOURCE("ore:Proxy/dcterms:references/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:references/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_REFERENCES_LANG("ore:Proxy/dcterms:references/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:references/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_REPLACES("ore:Proxy/dcterms:replaces") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:replaces", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_REPLACES_RESOURCE("ore:Proxy/dcterms:replaces/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:replaces/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_REPLACES_LANG("ore:Proxy/dcterms:replaces/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:replaces/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_REQUIRES("ore:Proxy/dcterms:requires") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:requires", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_REQUIRES_RESOURCE("ore:Proxy/dcterms:requires/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:requires/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_REQUIRES_LANG("ore:Proxy/dcterms:requires/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:requires/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_SPATIAL("ore:Proxy/dcterms:spatial") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:spatial", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_SPATIAL_RESOURCE("ore:Proxy/dcterms:spatial/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:spatial/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_SPATIAL_LANG("ore:Proxy/dcterms:spatial/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:spatial/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_TOC("ore:Proxy/dcterms:tableOfContents") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:tableOfContents", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_TOC_RESOURCE("ore:Proxy/dcterms:tableOfContents/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:tableOfContents/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_TOC_LANG("ore:Proxy/dcterms:tableOfContents/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:tableOfContents/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_TEMPORAL("ore:Proxy/dcterms:temporal") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:temporal", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_TEMPORAL_RESOURCE("ore:Proxy/dcterms:temporal/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:temporal/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_TEMPORAL_LANG("ore:Proxy/dcterms:temporal/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/dcterms:temporal/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_CURRENTLOCATION("ore:Proxy/edm:currentLocation") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:currentLocation", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_CURRENTLOCATION_RESOURCE("ore:Proxy/edm:currentLocation/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:currentLocation/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_HASMET("ore:Proxy/edm:hasMet") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:hasMet", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_HASMET_RESOURCE("ore:Proxy/edm:hasMet/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:hasMet/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_HASTYPE("ore:Proxy/edm:hasType") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:hasType", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_HASTYPE_RESOURCE("ore:Proxy/edm:hasType/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:hasType/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_HASTYPE_LANG("ore:Proxy/edm:hasType/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:hasType/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_INCORPORATES("ore:Proxy/edm:incorporates") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:incorporates", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_INCORPORATES_RESOURCE("ore:Proxy/edm:incorporates/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:incorporates/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_ISDERIVATIVEOF("ore:Proxy/edm:isDerivativeOf") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:isDerivativeOf", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ISDERIVATIVEOF_RESOURCE("ore:Proxy/edm:isDerivativeOf/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:isDerivativeOf/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_ISNEXTINSEQUENCE("ore:Proxy/edm:isNextInSequence") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:isNextInSequence", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ISNEXTINSEQUENCE_RESOURCE("ore:Proxy/edm:isNextInSequence/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:isNextInSequence/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_ISRELATEDTO("ore:Proxy/edm:isRelatedTo") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:isRelatedTo", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ISRELATEDTO_RESOURCE("ore:Proxy/edm:isRelatedTo/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:isRelatedTo/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_ISRELATEDTO_LANG("ore:Proxy/edm:isRelatedTo/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:isRelatedTo/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    ORE_PROXY_ISREPRESENTATIONOF("ore:Proxy/edm:isRepresentationOf") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:isRepresentationOf", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ISREPRESENTATIONOF_RESOURCE("ore:Proxy/edm:isRepresentationOf/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:isRepresentationOf/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_ISSIMILARTO("ore:Proxy/edm:isSimilarTo") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:isSimilarTo", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ISSIMILARTO_RESOURCE("ore:Proxy/edm:isSimilarTo/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:isSimilarTo/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_ISSUCCESSOROF("ore:Proxy/edm:isSuccessorOf") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:isSuccessorOf", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_ISSUCCESSOROF_RESOURCE("ore:Proxy/edm:isSuccessorOf/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:isSuccessorOf/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_REALIZES("ore:Proxy/edm:realizes") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:realizes", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_REALIZES_RESOURCE("ore:Proxy/edm:realizes/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:realizes/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_EDM_TYPE("ore:Proxy/edm:type") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:type", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            Set<String> values = new HashSet<>();
            values.add("IMAGE");
            values.add("TEXT");
            values.add("AUDIO");
            values.add("VIDEO");
            values.add("3D");
            return ElementEnumeration.constructEnumerationRules(values);
        }
    },
    ORE_PROXY_WASPRESENTAT("ore:Proxy/edm:wasPresentAt") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:wasPresentAt", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_WASPRESENTAT_RESOURCE("ore:Proxy/edm:wasPresentAt/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/edm:wasPresentAt/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_FOR("ore:Proxy/ore:proxyFor") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/ore:proxyFor", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_FOR_RESOURCE("ore:Proxy/ore:proxyFor/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("ore:Aggregation/@rdf:about", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_IN("ore:Proxy/ore:proxyIn") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_IN_RESOURCE("ore:Proxy/ore:proxyIn/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/@rdf:about", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    ORE_PROXY_SAMEAS("ore:Proxy/owl:sameAs") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/owl:sameAs", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    ORE_PROXY_SAMEAS_RESOURCE("ore:Proxy/owl:sameAs/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("edm:ProvidedCHO/owl:sameAs/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    SKOS_CONCEPT("skos:Concept") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_ABOUT("skos:Concept/@rdf:about") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/@rdf:about", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    SKOS_CONCEPT_ALT("skos:Concept/skos:altLabel") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:altLabel", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_ALT_LANG("skos:Concept/skos:altLabel/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:altLabel/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    SKOS_CONCEPT_BROADER("skos:Concept/skos:broader") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:broader", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_BROADER_RESOURCE("skos:Concept/skos:broader/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:broader/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    SKOS_CONCEPT_BROADMATCH("skos:Concept/skos:broadMatch") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:broadMatch", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_BROADMATCH_RESOURCE("skos:Concept/skos:broadMatch/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:broadMatch/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    SKOS_CONCEPT_CLOSEMATCH("skos:Concept/skos:closeMatch") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:closeMatch", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_CLOSEMATCH_RESOURCE("skos:Concept/skos:closeMatch/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:closeMatch/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    SKOS_CONCEPT_EXACTMATCH("skos:Concept/skos:exactMatch") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:exactMatch", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_EXACTMATCH_RESOURCE("skos:Concept/skos:exactMatch/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:exactMatch/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    SKOS_CONCEPT_INSCHEME("skos:Concept/skos:inScheme") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:inScheme", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_INSCHEME_RESOURCE("skos:Concept/skos:inScheme/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:inScheme/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    SKOS_CONCEPT_NARROWER("skos:Concept/skos:narrower") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:narrower", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_NARROWER_RESOURCE("skos:Concept/skos:narrower/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:narrower/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    SKOS_CONCEPT_NARROWMATCH("skos:Concept/skos:narrowMatch") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:narrowMatch", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_NARROWMATCH_RESOURCE("skos:Concept/skos:narrowMatch/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:narrowMatch/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    SKOS_CONCEPT_NOTATION("skos:Concept/skos:notation") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:notation", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_NOTATION_LANG("skos:Concept/skos:notation/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:notation/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    SKOS_CONCEPT_NOTE("skos:Concept/skos:note") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:note", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_NOTE_LANG("skos:Concept/skos:note/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:note/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    SKOS_CONCEPT_PREFLABEL("skos:Concept/skos:prefLabel") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:prefLabel", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_PREFLABEL_LANG("skos:Concept/skos:prefLabel/@xml:lang") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:prefLabel/@xml:lang", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructLangRules();
        }

    },
    SKOS_CONCEPT_RELATED("skos:Concept/skos:related") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:related", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_RELATED_RESOURCE("skos:Concept/skos:related/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:related/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    SKOS_CONCEPT_RELATEDMATCH("skos:Concept/skos:relatedMatch") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:relatedMatch", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SKOS_CONCEPT_RELATEDMATCH_RESOURCE("skos:Concept/skos:relatedMatch/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("skos:Concept/skos:relatedMatch/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    SVCS_SERVICE("svcs:Service") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("svcs:Service", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SVCS_SERVICE_ABOUT("svcs:Service/@rdf:about") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("svcs:Service/@rdf:about", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    SVCS_SERVICE_CONFORMSTO("svcs:Service/dcterms:conformsTo") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("svcs:Service/dcterms:conformsTo", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SVCS_SERVICE_CONFORMSTO_RESOURCE("svcs:Service/dcterms:conformsTo/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("svcs:Service/dcterms:conformsTo/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    },
    SVCS_SERVICE_IMPLEMENTS("svcs:Service/doap:implements") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("svcs:Service/doap:implements", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return null;
        }
    },
    SVCS_SERVICE_IMPLEMENTS_RESOURCE("svcs:Service/doap:implements/@rdf:resource") {
        @Override
        public List<ValueDTO> getSourceFields() {
            return ElementEnumeration.values("svcs:Service/doap:implements/@rdf:resource", false);
        }

        @Override
        public List<ValidationRule> getRules() {
            return ElementEnumeration.constructUriRules();
        }

    }
    ;

    private String targetField;

    ElementEnumeration(String targetField) {
        this.targetField = targetField;
    }

    public abstract List<ValueDTO> getSourceFields();

    public abstract List<ValidationRule> getRules();

    public static ElementEnumeration find(String targetField) {
        for (ElementEnumeration elementEnumeration : ElementEnumeration.values()) {
            if (StringUtils.equals(elementEnumeration.targetField, targetField)) {
                return elementEnumeration;
            }
        }
        return null;
    }

    private static List<ValueDTO> values(String field, boolean constant) {
        ValueDTO dto = new ValueDTO();
        dto.setConstant(constant);
        dto.setValue(field);
        List<ValueDTO> dtos = new ArrayList<>();
        dtos.add(dto);
        return dtos;
    }

    private static List<ValidationRule> constructUriRules() {
        return constructAbstractRule(new IsUriFunction(), null);
    }

    private static List<ValidationRule> constructUrlRules() {
        return constructAbstractRule(new IsUrlFunction(), null);
    }

    private static List<ValidationRule> constructLangRules() {
        return constructAbstractRule(new IsLanguageFunction(), null);
    }

    private static List<ValidationRule> constructEnumerationRules(Set<String> values) {
        return constructAbstractRule(new IsEnumerationFunction(), values);
    }

    private static List<ValidationRule> constructDateTypeRules() {
        return constructAbstractRule(new IsDateTypeFunction(), null);
    }

    private static List<ValidationRule> constructFloatRules() {
        return constructAbstractRule(new IsFloatFunction(), null);
    }

    private static List<ValidationRule> constructBooleanRules() {
        return constructAbstractRule(new IsBooleanFunction(), null);
    }

    private static List<ValidationRule> constructAbstractRule(ValidationFunction function, Set<String> values) {
        List<ValidationRule> rules = new ArrayList<>();
        ValidationRule rule = new ValidationRule();
        rule.setFlagType(FlagType.BLOCKER);
        if (StringUtils.equals(function.getType(), "isEnumerationFunction")) {
            ((IsEnumerationFunction) function).setValues(values);
        }
        rule.setFunction(function);
        rules.add(rule);
        return rules;
    }
}
