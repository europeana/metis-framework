package eu.europeana.metis.utils;

import eu.europeana.corelib.definitions.jibx.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gmamakis on 8-3-17.
 */
public enum EnrichmentFields {
    DC_CREATOR {
        @Override
        public List<EntityClass> getVocabularies() {
            List<EntityClass> vocs = new ArrayList<>();
            vocs.add(EntityClass.AGENT);
            return vocs;
        }

        @Override
        public List<InputValue> extractFieldValuesForEnrichment(ProxyType proxy) {
            List<InputValue> values = new ArrayList<>();
            List<EuropeanaType.Choice> choices = proxy.getChoiceList();
            for(EuropeanaType.Choice choice:choices){
                if(choice.ifCreator() && choice.getCreator().getString()!=null){
                    Creator creator = choice.getCreator();
                    InputValue value = new InputValue();
                    value.setOriginalField(this.name());
                    value.setValue(creator.getString());
                    value.setVocabularies(getVocabularies());
                    if(creator.getLang()!=null){
                        value.setLanguage(creator.getLang().getLang());
                    }
                    values.add(value);
                }
            }
            return values;
        }

        @Override
        public EuropeanaType.Choice createChoice(String about) {
            EuropeanaType.Choice choice = new EuropeanaType.Choice();
            Creator creator = new Creator();
            ResourceOrLiteralType.Resource resource =  new ResourceOrLiteralType.Resource();
            resource.setResource(about);
            creator.setResource(resource);
            choice.setCreator(creator);
            return choice;
        }
    },DC_CONTRIBUTOR {
        @Override
        public List<EntityClass> getVocabularies() {
            List<EntityClass> vocs = new ArrayList<>();
            vocs.add(EntityClass.AGENT);
            return vocs;
        }

        @Override
        public List<InputValue> extractFieldValuesForEnrichment(ProxyType proxy) {
            List<InputValue> values = new ArrayList<>();
            List<EuropeanaType.Choice> choices = proxy.getChoiceList();
            for(EuropeanaType.Choice choice:choices){
                if(choice.ifContributor() && choice.getContributor().getString()!=null){
                    Contributor contributor = choice.getContributor();
                    InputValue value = new InputValue();
                    value.setOriginalField(this.name());
                    value.setValue(contributor.getString());
                    value.setVocabularies(getVocabularies());
                    if(contributor.getLang()!=null){
                        value.setLanguage(contributor.getLang().getLang());
                    }
                    values.add(value);
                }
            }
            return values;
        }

        @Override
        public EuropeanaType.Choice createChoice(String about) {
            EuropeanaType.Choice choice = new EuropeanaType.Choice();
            Contributor contributor = new Contributor();
            ResourceOrLiteralType.Resource resource =  new ResourceOrLiteralType.Resource();
            resource.setResource(about);
            contributor.setResource(resource);
            choice.setContributor(contributor);
            return choice;
        }
    }, DC_DATE {
        @Override
        public List<EntityClass> getVocabularies() {
            List<EntityClass> vocs = new ArrayList<>();
            vocs.add(EntityClass.TIMESPAN);
            return vocs;
        }

        @Override
        public List<InputValue> extractFieldValuesForEnrichment(ProxyType proxy) {
            List<InputValue> values = new ArrayList<>();
            List<EuropeanaType.Choice> choices = proxy.getChoiceList();
            for(EuropeanaType.Choice choice:choices){
                if(choice.ifDate() && choice.getDate().getString()!=null){
                    Date date = choice.getDate();
                    InputValue value = new InputValue();
                    value.setOriginalField(this.name());
                    value.setValue(date.getString());
                    value.setVocabularies(getVocabularies());
                    if(date.getLang()!=null){
                        value.setLanguage(date.getLang().getLang());
                    }
                    values.add(value);
                }
            }
            return values;
        }

        @Override
        public EuropeanaType.Choice createChoice(String about) {
            EuropeanaType.Choice choice = new EuropeanaType.Choice();
            Date date = new Date();
            ResourceOrLiteralType.Resource resource =  new ResourceOrLiteralType.Resource();
            resource.setResource(about);
            date.setResource(resource);
            choice.setDate(date);
            return choice;
        }
    },DCTERMS_ISSUED {
        @Override
        public List<EntityClass> getVocabularies() {
            List<EntityClass> vocs = new ArrayList<>();
            vocs.add(EntityClass.TIMESPAN);
            return vocs;
        }

        @Override
        public List<InputValue> extractFieldValuesForEnrichment(ProxyType proxy) {
            List<InputValue> values = new ArrayList<>();
            List<EuropeanaType.Choice> choices = proxy.getChoiceList();
            for(EuropeanaType.Choice choice:choices){
                if(choice.ifIssued() && choice.getIssued().getString()!=null){
                    Issued date = choice.getIssued();
                    InputValue value = new InputValue();
                    value.setOriginalField(this.name());
                    value.setValue(date.getString());
                    value.setVocabularies(getVocabularies());
                    if(date.getLang()!=null){
                        value.setLanguage(date.getLang().getLang());
                    }
                    values.add(value);
                }
            }
            return values;
        }

        @Override
        public EuropeanaType.Choice createChoice(String about) {
            EuropeanaType.Choice choice = new EuropeanaType.Choice();
            Issued issued = new Issued();
            ResourceOrLiteralType.Resource resource =  new ResourceOrLiteralType.Resource();
            resource.setResource(about);
            issued.setResource(resource);
            choice.setIssued(issued);
            return choice;
        }
    },DCTERMS_CREATED {
        @Override
        public List<EntityClass> getVocabularies() {
            List<EntityClass> vocs = new ArrayList<>();
            vocs.add(EntityClass.TIMESPAN);
            return vocs;
        }

        @Override
        public List<InputValue> extractFieldValuesForEnrichment(ProxyType proxy) {
            List<InputValue> values = new ArrayList<>();
            List<EuropeanaType.Choice> choices = proxy.getChoiceList();
            for(EuropeanaType.Choice choice:choices){
                if(choice.ifCreated() && choice.getCreated().getString()!=null){
                    Created date = choice.getCreated();
                    InputValue value = new InputValue();
                    value.setOriginalField(this.name());
                    value.setValue(date.getString());
                    value.setVocabularies(getVocabularies());
                    if(date.getLang()!=null){
                        value.setLanguage(date.getLang().getLang());
                    }
                    values.add(value);
                }
            }
            return values;
        }

        @Override
        public EuropeanaType.Choice createChoice(String about) {
            EuropeanaType.Choice choice = new EuropeanaType.Choice();
            Created created = new Created();
            ResourceOrLiteralType.Resource resource =  new ResourceOrLiteralType.Resource();
            resource.setResource(about);
            created.setResource(resource);
            choice.setCreated(created);
            return choice;
        }
    },DC_COVERAGE {
        @Override
        public List<EntityClass> getVocabularies() {
            List<EntityClass> vocs = new ArrayList<>();
            vocs.add(EntityClass.PLACE);
            return vocs;
        }

        @Override
        public List<InputValue> extractFieldValuesForEnrichment(ProxyType proxy) {
            List<InputValue> values = new ArrayList<>();
            List<EuropeanaType.Choice> choices = proxy.getChoiceList();
            for(EuropeanaType.Choice choice:choices){
                if(choice.ifCoverage() && choice.getCoverage().getString()!=null){
                    Coverage coverage = choice.getCoverage();
                    InputValue value = new InputValue();
                    value.setOriginalField(this.name());
                    value.setValue(coverage.getString());
                    value.setVocabularies(getVocabularies());
                    if(coverage.getLang()!=null){
                        value.setLanguage(coverage.getLang().getLang());
                    }
                    values.add(value);
                }
            }
            return values;
        }

        @Override
        public EuropeanaType.Choice createChoice(String about) {
            EuropeanaType.Choice choice = new EuropeanaType.Choice();
            Coverage coverage = new Coverage();
            ResourceOrLiteralType.Resource resource =  new ResourceOrLiteralType.Resource();
            resource.setResource(about);
            coverage.setResource(resource);
            choice.setCoverage(coverage);
            return choice;
        }
    },DCTERMS_TEMPORAL {
        @Override
        public List<EntityClass> getVocabularies() {
            List<EntityClass> vocs = new ArrayList<EntityClass>();
            vocs.add(EntityClass.TIMESPAN);
            return vocs;
        }

        @Override
        public List<InputValue> extractFieldValuesForEnrichment(ProxyType proxy) {
            List<InputValue> values = new ArrayList<>();
            List<EuropeanaType.Choice> choices = proxy.getChoiceList();
            for(EuropeanaType.Choice choice:choices){
                if(choice.ifTemporal() && choice.getTemporal().getString()!=null){
                    Temporal temporal= choice.getTemporal();
                    InputValue value = new InputValue();
                    value.setOriginalField(this.name());
                    value.setValue(temporal.getString());
                    value.setVocabularies(getVocabularies());
                    if(temporal.getLang()!=null){
                        value.setLanguage(temporal.getLang().getLang());
                    }
                    values.add(value);
                }
            }
            return values;
        }

        @Override
        public EuropeanaType.Choice createChoice(String about) {
            EuropeanaType.Choice choice = new EuropeanaType.Choice();
            Temporal temporal = new Temporal();
            ResourceOrLiteralType.Resource resource =  new ResourceOrLiteralType.Resource();
            resource.setResource(about);
            temporal.setResource(resource);
            choice.setTemporal(temporal);
            return choice;
        }
    },DC_TYPE {
        @Override
        public List<EntityClass> getVocabularies() {
            List<EntityClass> vocs = new ArrayList<EntityClass>();
            vocs.add(EntityClass.CONCEPT);
            return vocs;
        }

        @Override
        public List<InputValue> extractFieldValuesForEnrichment(ProxyType proxy) {
            List<InputValue> values = new ArrayList<>();
            List<EuropeanaType.Choice> choices = proxy.getChoiceList();
            for(EuropeanaType.Choice choice:choices){
                if(choice.ifType() && choice.getType().getString()!=null){
                    Type type= choice.getType();
                    InputValue value = new InputValue();
                    value.setOriginalField(this.name());
                    value.setValue(type.getString());
                    value.setVocabularies(getVocabularies());
                    if(type.getLang()!=null){
                        value.setLanguage(type.getLang().getLang());
                    }
                    values.add(value);
                }
            }
            return values;
        }

        @Override
        public EuropeanaType.Choice createChoice(String about) {
            EuropeanaType.Choice choice = new EuropeanaType.Choice();
            Type type = new Type();
            ResourceOrLiteralType.Resource resource =  new ResourceOrLiteralType.Resource();
            resource.setResource(about);
            type.setResource(resource);
            choice.setType(type);
            return choice;
        }
    },
    DCTERMS_SPATIAL {
        @Override
        public List<EntityClass> getVocabularies() {
            List<EntityClass> vocs = new ArrayList<>();
            vocs.add(EntityClass.PLACE);
            return vocs;
        }

        @Override
        public List<InputValue> extractFieldValuesForEnrichment(ProxyType proxy) {
            List<InputValue> values = new ArrayList<>();
            List<EuropeanaType.Choice> choices = proxy.getChoiceList();
            for(EuropeanaType.Choice choice:choices){
                if(choice.ifSpatial() && choice.getSpatial().getString()!=null){
                    Spatial spatial= choice.getSpatial();
                    InputValue value = new InputValue();
                    value.setOriginalField(this.name());
                    value.setValue(spatial.getString());
                    value.setVocabularies(getVocabularies());
                    if(spatial.getLang()!=null){
                        value.setLanguage(spatial.getLang().getLang());
                    }
                    values.add(value);
                }
            }
            return values;
        }

        @Override
        public EuropeanaType.Choice createChoice(String about) {
            EuropeanaType.Choice choice = new EuropeanaType.Choice();
            Spatial spatial = new Spatial();
            ResourceOrLiteralType.Resource resource =  new ResourceOrLiteralType.Resource();
            resource.setResource(about);
            spatial.setResource(resource);
            choice.setSpatial(spatial);
            return choice;
        }
    },DC_SUBJECT {
        @Override
        public List<EntityClass> getVocabularies() {
            List<EntityClass> vocs = new ArrayList<>();
            vocs.add(EntityClass.CONCEPT);
            return vocs;
        }

        @Override
        public List<InputValue> extractFieldValuesForEnrichment(ProxyType proxy) {
            List<InputValue> values = new ArrayList<>();
            List<EuropeanaType.Choice> choices = proxy.getChoiceList();
            for(EuropeanaType.Choice choice:choices){
                if(choice.ifSubject() && choice.getSubject().getString()!=null){
                    Subject subject= choice.getSubject();
                    InputValue value = new InputValue();
                    value.setOriginalField(this.name());
                    value.setValue(subject.getString());
                    value.setVocabularies(getVocabularies());
                    if(subject.getLang()!=null){
                        value.setLanguage(subject.getLang().getLang());
                    }
                    values.add(value);
                }
            }
            return values;
        }

        @Override
        public EuropeanaType.Choice createChoice(String about) {
            EuropeanaType.Choice choice = new EuropeanaType.Choice();
            Subject subject = new Subject();
            ResourceOrLiteralType.Resource resource =  new ResourceOrLiteralType.Resource();
            resource.setResource(about);
            subject.setResource(resource);
            choice.setSubject(subject);
            return choice;
        }
    };

    /**
     * Get the vocabularies for this field
     * @return The vocabularies to enrich with
     */
    public abstract List<EntityClass> getVocabularies();

    /**
     * Extract fields from a Proxy for enrichment
     * @param proxy The proxy to use fro enrichment
     * @return A list ofg values ready for enrichment
     */
    public abstract List<InputValue> extractFieldValuesForEnrichment(ProxyType proxy);

    /**
     * Create a field appendable on a Europeana Proxy during enrichment for semantic linking
     * @param about The rdf:about of the Class to append on the specified field
     * @return The full field to append
     */
    public abstract EuropeanaType.Choice createChoice(String about);
}
