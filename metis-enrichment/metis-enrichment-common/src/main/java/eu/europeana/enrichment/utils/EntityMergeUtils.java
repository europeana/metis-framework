package eu.europeana.enrichment.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.jibx.AboutType;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Alt;
import eu.europeana.corelib.definitions.jibx.AltLabel;
import eu.europeana.corelib.definitions.jibx.Begin;
import eu.europeana.corelib.definitions.jibx.BiographicalInformation;
import eu.europeana.corelib.definitions.jibx.BroadMatch;
import eu.europeana.corelib.definitions.jibx.Broader;
import eu.europeana.corelib.definitions.jibx.CloseMatch;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.Concept.Choice;
import eu.europeana.corelib.definitions.jibx.Date;
import eu.europeana.corelib.definitions.jibx.DateOfBirth;
import eu.europeana.corelib.definitions.jibx.DateOfDeath;
import eu.europeana.corelib.definitions.jibx.DateOfEstablishment;
import eu.europeana.corelib.definitions.jibx.DateOfTermination;
import eu.europeana.corelib.definitions.jibx.End;
import eu.europeana.corelib.definitions.jibx.EuropeanaType;
import eu.europeana.corelib.definitions.jibx.ExactMatch;
import eu.europeana.corelib.definitions.jibx.Gender;
import eu.europeana.corelib.definitions.jibx.HasMet;
import eu.europeana.corelib.definitions.jibx.HasPart;
import eu.europeana.corelib.definitions.jibx.Identifier;
import eu.europeana.corelib.definitions.jibx.InScheme;
import eu.europeana.corelib.definitions.jibx.IsPartOf;
import eu.europeana.corelib.definitions.jibx.IsRelatedTo;
import eu.europeana.corelib.definitions.jibx.Lat;
import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.NarrowMatch;
import eu.europeana.corelib.definitions.jibx.Narrower;
import eu.europeana.corelib.definitions.jibx.Notation;
import eu.europeana.corelib.definitions.jibx.Note;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.PrefLabel;
import eu.europeana.corelib.definitions.jibx.ProfessionOrOccupation;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.Related;
import eu.europeana.corelib.definitions.jibx.RelatedMatch;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.SameAs;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.jibx._Long;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.Timespan;

/**
 * Created by erikkonijnenburg on 28/07/2017.
 */
public class EntityMergeUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityMergeUtils.class);

  private static final String UTF8 = StandardCharsets.UTF_8.name();

  private static final Map<Class<?>, IBindingFactory> BINDING_FACTORIES = new HashMap<>();
  
  private static final Class<?>[] BINDING_FACTORY_TYPES = new Class<?>[] {AgentType.class,
      Concept.class, PlaceType.class, TimeSpanType.class, RDF.class};

  static {
    for (Class<?> type : BINDING_FACTORY_TYPES) {
      try {
        BINDING_FACTORIES.put(type, BindingDirectory.getFactory(type));
      } catch (JiBXException e) {
        LOGGER.error("Unable to create binding factory for type " + type.getName() + ".", e);
      }
    }
  }
  
  private EntityMergeUtils() {}

  private static IBindingFactory getBindingFactory(Class<?> type) {
    final IBindingFactory result = BINDING_FACTORIES.get(type);
    if (result != null) {
      return result;
    }
    throw new IllegalStateException("No binding factory available.");
  }

  public static RDF mergeEntity(String record, String entity) throws JiBXException {
    IUnmarshallingContext rdfCTX = getBindingFactory(RDF.class).createUnmarshallingContext();
    RDF rdf = (RDF)rdfCTX.unmarshalDocument(IOUtils.toInputStream(record),UTF8);

    appendEntityInRDF(entity, rdf);

    return rdf;
  }

  public static RDF mergeEntityForEnrichment(String record, String entity, String fieldName) throws JiBXException {
    IUnmarshallingContext rdfCTX = getBindingFactory(RDF.class).createUnmarshallingContext();
    RDF rdf = (RDF)rdfCTX.unmarshalDocument(IOUtils.toInputStream(record),UTF8);

    AboutType type =  appendEntityInRDF(entity, rdf);

    if (StringUtils.isNotEmpty(fieldName) && type != null) {
      return appendToProxy(rdf, type, fieldName);
    }
    return rdf;
  }

  public static ProxyType getProviderProxy(RDF rdf) {
    for(ProxyType proxyType:rdf.getProxyList()) {
      if(proxyType.getEuropeanaProxy()==null || !proxyType.getEuropeanaProxy().isEuropeanaProxy()) {
        return proxyType;
      }
    }
    return null;
  }

  private static AboutType appendEntityInRDF(String entity, RDF rdf)
      throws JiBXException {

    AboutType type = null;
    if(StringUtils.contains(entity,"skos:Concept")){
      type = appendConceptInRDF(rdf, entity);
    } else if(StringUtils.contains(entity,"edm:Agent")) {
      type = appendAgentInRDF(rdf, entity);
    } else if(StringUtils.contains(entity,"edm:Place")) {
      type = appendPlaceInRDF(rdf, entity);
    } else if(StringUtils.contains(entity,"edm:Timespan")) {
      type =  appendTimespanInRDF(rdf, entity);
    }
    if (type == null) {
      LOGGER.warn("Unknown entity found: {}", entity);
    }

    return type;
  }

  private static AgentType appendAgentInRDF(RDF rdf, String entity) throws JiBXException {
    IUnmarshallingContext unmarshaller = getBindingFactory(AgentType.class).createUnmarshallingContext();
    AgentType agentType = (AgentType) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);

    if (rdf.getAgentList() == null) {
      rdf.setAgentList(new ArrayList<>());
    }
    rdf.getAgentList().add(agentType);
    return agentType;

  }

  private static Concept appendConceptInRDF(RDF rdf, String entity) throws JiBXException {
    IUnmarshallingContext unmarshaller = getBindingFactory(Concept.class).createUnmarshallingContext();
    Concept conceptType = (Concept) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);
    if(rdf.getConceptList() == null) {
      rdf.setConceptList(new ArrayList<>());
    }
    rdf.getConceptList().add(conceptType);
    return conceptType;
  }

  private static PlaceType appendPlaceInRDF(RDF rdf, String entity) throws JiBXException {
    IUnmarshallingContext unmarshaller = getBindingFactory(PlaceType.class).createUnmarshallingContext();
    PlaceType placeType = (PlaceType) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);	  
    if (rdf.getPlaceList()==null) {
      rdf.setPlaceList(new ArrayList<>());
    }  
    rdf.getPlaceList().add(placeType);
    return placeType;
  }

  private static TimeSpanType appendTimespanInRDF(RDF rdf, String entity) throws JiBXException {
    IUnmarshallingContext unmarshaller = getBindingFactory(TimeSpanType.class).createUnmarshallingContext();
    TimeSpanType tsType = (TimeSpanType) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);
    if (rdf.getTimeSpanList()==null) {
      rdf.setTimeSpanList(new ArrayList<>());
    }
    rdf.getTimeSpanList().add(tsType);
    return tsType;
  }

  private static RDF appendToProxy(RDF rdf, AboutType about, String fieldName) {
    ProxyType europeanaProxy = getEuropeanaProxy(rdf);
    appendToProxy(europeanaProxy,EnrichmentFields.valueOf(fieldName), about.getAbout());
    return replaceProxy(rdf,europeanaProxy);
  }

  private static void appendToProxy(ProxyType europeanaProxy, EnrichmentFields enrichmentFields, String about) {
    List<EuropeanaType.Choice> choices = europeanaProxy.getChoiceList();
    choices.add(enrichmentFields.createChoice(about));
    europeanaProxy.setChoiceList(choices);
  }

  private static ProxyType getEuropeanaProxy(RDF rdf){
    for(ProxyType proxyType:rdf.getProxyList()){
      if(proxyType.getEuropeanaProxy()!=null && proxyType.getEuropeanaProxy().isEuropeanaProxy()){
        return proxyType;
      }
    }
    throw new IllegalArgumentException("Could not find Europeana proxy.");
  }

  private static RDF replaceProxy(RDF rdf, ProxyType europeanaProxy) {
    List<ProxyType> proxyTypeList = new ArrayList<>();
    proxyTypeList.add(europeanaProxy);
    for(ProxyType proxyType:rdf.getProxyList()){
      if(!StringUtils.equals(proxyType.getAbout(),europeanaProxy.getAbout())){
        proxyTypeList.add(proxyType);
      }
    }
    rdf.setProxyList(proxyTypeList);
    return rdf;
  }
  
  private static RDF mergePlace(RDF rdf, Place place, String fieldName) {
    
    PlaceType placeType = new PlaceType();

    // about
    if (place.getAbout() != null)
    	placeType.setAbout(place.getAbout());
    else
    	placeType.setAbout("");

    // alt                    
    String placeAlt = place.getAlt();
    Alt alt = new Alt();
    if (placeAlt != null)
        alt.setAlt(Float.valueOf(placeAlt));
    placeType.setAlt(alt);

    // altLabelList
    ArrayList<AltLabel> altLabelList = new ArrayList<>();
    if (place.getAltLabelList() != null) {        
        for (Label label : place.getAltLabelList()) {
            if (label != null) {
                AltLabel altLabel = new AltLabel();
                LiteralType.Lang lang = new LiteralType.Lang();
                lang.setLang(label.getLang());
                altLabel.setLang(lang);
                altLabel.setString(label.getValue());
                altLabelList.add(altLabel);
            }
        }         
    }
    else {
    	AltLabel altLabel = new AltLabel();
    	altLabelList.add(altLabel);
    }
    placeType.setAltLabelList(altLabelList);

    // hasPartList       
    ArrayList<HasPart> hasPartList = new ArrayList<>();
    if (place.getHasPartsList() != null) {        
        for (Part part : place.getHasPartsList()) {
            if (part != null) {
                HasPart hasPart = new HasPart();
                ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
                resrc.setResource(part.getResource());
                hasPart.setResource(resrc);
                hasPartList.add(hasPart);
            }
        }
    } else {
    	HasPart hasPart = new HasPart();
    	hasPartList.add(hasPart);
    }
    placeType.setHasPartList(hasPartList);

    // isPartOfList
    ArrayList<IsPartOf> isPartOfList = new ArrayList<>();
    if (place.getIsPartOfList() != null) {        
        for (Part part : place.getIsPartOfList()) {
            if (part != null) {
                IsPartOf isPartOf = new IsPartOf();                           
                ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
                resrc.setResource(part.getResource());
                isPartOf.setResource(resrc);
                isPartOf.setString("");
                isPartOfList.add(isPartOf);
            }
        }    
    } else {
    	IsPartOf isPartOf = new IsPartOf(); 
    	isPartOfList.add(isPartOf);
    }
    placeType.setIsPartOfList(isPartOfList);

    // lat
    Lat lat = new Lat();
    if (place.getLat() != null)
        lat.setLat(Float.valueOf(place.getLat()));
    placeType.setLat(lat);
    
    // _long
    _Long longitude = new _Long();
    if (place.getLon() != null)
        longitude.setLong(Float.valueOf(place.getLon()));
    placeType.setLong(longitude);
    
    // noteList
    ArrayList<Note> noteList = new ArrayList<>();
    if (place.getNotes() != null) {       
        for (Label label : place.getNotes()) {
            if (label != null) {
                Note note = new Note();
                LiteralType.Lang lang = new LiteralType.Lang();
                lang.setLang(label.getLang());
                note.setLang(lang);
                note.setString(label.getValue());
                noteList.add(note);
            }
        }             
    } else {
    	Note note = new Note();
    	noteList.add(note);
    }
    placeType.setNoteList(noteList);

    // prefLabelList
    ArrayList<PrefLabel> prefLabelList = new ArrayList<>();
    if (place.getPrefLabelList() != null) {        
        for (Label label : place.getPrefLabelList()) {
            if (label != null) {
                PrefLabel prefLabel = new PrefLabel();
                LiteralType.Lang lang = new LiteralType.Lang();
                lang.setLang(label.getLang());
                prefLabel.setLang(lang);
                prefLabel.setString(label.getValue());
                prefLabelList.add(prefLabel);
            }
        }        
    } else {
    	PrefLabel prefLabel = new PrefLabel();
    	prefLabelList.add(prefLabel);
    }
    placeType.setPrefLabelList(prefLabelList);

    // sameAsList                 
    ArrayList<SameAs> sameAsList = new ArrayList<>();
    if (place.getSameAs() != null) {        
        for (Part part : place.getSameAs()) {
            if (part != null) {
                SameAs sameAs = new SameAs();                     
                sameAs.setResource(part.getResource());                                               
                sameAsList.add(sameAs);
            }
        }        
    } else {
    	SameAs sameAs = new SameAs();     
    	sameAsList.add(sameAs);
    }
    placeType.setSameAList(sameAsList);

    // isNextInSequence: not available
    
    if (rdf.getPlaceList() == null)
        rdf.setPlaceList(new ArrayList<PlaceType>());
    
    rdf.getPlaceList().add(placeType);
    
    if (StringUtils.isNotEmpty(fieldName)) {
        return appendToProxy(rdf, placeType, fieldName);
      } else {
          return rdf;
      }               
  }
  
  private static RDF mergeAgent(RDF rdf, Agent agent, String fieldName) {
    
    AgentType agentType = new AgentType();

    // about
    if (agent.getAbout() != null)
    	agentType.setAbout(agent.getAbout());
    else
    	agentType.setAbout("");

    // altLabelList
    ArrayList<AltLabel> altLabelList = new ArrayList<>();
    if (agent.getAltLabelList() != null) {
        for (Label label : agent.getAltLabelList()) {
            if (label != null) {
                AltLabel altLabel = new AltLabel();
                LiteralType.Lang lang = new LiteralType.Lang();
                lang.setLang(label.getLang());
                altLabel.setLang(lang);
                altLabel.setString(label.getValue());
                altLabelList.add(altLabel);
            }
        }
    } else {
    	AltLabel altLabel = new AltLabel();
    	altLabelList.add(altLabel);
    }
    agentType.setAltLabelList(altLabelList);

    // begin
    Begin begin = new Begin();
    ArrayList<Label> beginList = (ArrayList<Label>) agent.getBeginList();
    if (beginList != null && !beginList.isEmpty()) {        
        Label label = beginList.get(0);
        if (label != null) {
            LiteralType.Lang lang = new LiteralType.Lang();
            lang.setLang(label.getLang());
            begin.setLang(lang);
            begin.setString(label.getValue());
        }        
    }
    agentType.setBegin(begin);

    // biographicalInformation
    BiographicalInformation bioInfo = new BiographicalInformation();
    ArrayList<Label> bioInfoList = (ArrayList<Label>) agent.getBiographicaInformation();
    if (bioInfoList != null && !bioInfoList.isEmpty()) {        
        Label label = bioInfoList.get(0);
        if (label != null) {
            LiteralType.Lang lang = new LiteralType.Lang();
            lang.setLang(label.getLang());
            bioInfo.setLang(lang);
            bioInfo.setString(label.getValue());
        }        
    }
    agentType.setBiographicalInformation(bioInfo);

    // dateList    
    ArrayList<Date> dateList = new ArrayList<>();
    Date date = new Date();
    ArrayList<Label> dateLabelList = (ArrayList<Label>) agent.getBiographicaInformation();
    if (dateLabelList != null && !dateLabelList.isEmpty()) {                      
        Label label = dateLabelList.get(0);        
        if (label != null) {                          
            ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
            lang.setLang(label.getLang());
            date.setLang(lang);
            date.setString(label.getValue());
        }             
    }
    dateList.add(date);
    agentType.setDateList(dateList);

    // dateOfBirth
    DateOfBirth dateOfBirth = new DateOfBirth();
    ArrayList<Label> dateOfBirthList = (ArrayList<Label>) agent.getDateOfBirth();
    if (dateOfBirthList != null && !dateOfBirthList.isEmpty()) {        
        Label label = dateOfBirthList.get(0);
        if (label != null) {
            LiteralType.Lang lang = new LiteralType.Lang();
            lang.setLang(label.getLang());
            dateOfBirth.setLang(lang);
            dateOfBirth.setString(label.getValue());
        }        
    }
    agentType.setDateOfBirth(dateOfBirth);

    // dateofDeath                
    DateOfDeath dateOfDeath = new DateOfDeath();
    ArrayList<Label> dateOfDeathList = (ArrayList<Label>) agent.getDateOfDeath();
    if (dateOfDeathList != null && !dateOfDeathList.isEmpty()) {        
        Label label = dateOfDeathList.get(0);
        if (label != null) {
            LiteralType.Lang lang = new LiteralType.Lang();
            lang.setLang(label.getLang());
            dateOfDeath.setLang(lang);
            dateOfDeath.setString(label.getValue());
        }        
    }
    agentType.setDateOfDeath(dateOfDeath);

    // dateOfEstablishment
    DateOfEstablishment dateOfEstablishment = new DateOfEstablishment();
    ArrayList<Label> dateOfEstablishmentList = (ArrayList<Label>) agent.getDateOfEstablishment();
    if (dateOfEstablishmentList != null && !dateOfEstablishmentList.isEmpty()) {        
        Label label = dateOfEstablishmentList.get(0);
        if (label != null) {
            LiteralType.Lang lang = new LiteralType.Lang();
            lang.setLang(label.getLang());
            dateOfEstablishment.setLang(lang);
            dateOfEstablishment.setString(label.getValue());
        }        
    }
    agentType.setDateOfEstablishment(dateOfEstablishment);

    // dateofTermination
    DateOfTermination dateOfTermination = new DateOfTermination();
    ArrayList<Label> dateOfTerminationList = (ArrayList<Label>) agent.getDateOfTermination();
    if (dateOfTerminationList != null && !dateOfTerminationList.isEmpty()) {        
        Label label = dateOfTerminationList.get(0);
        if (label != null) {
            LiteralType.Lang lang = new LiteralType.Lang();
            lang.setLang(label.getLang());
            dateOfTermination.setLang(lang);
            dateOfTermination.setString(label.getValue());
        }        
    }
    agentType.setDateOfTermination(dateOfTermination);

    // end
    End end = new End();
    ArrayList<Label> endList = (ArrayList<Label>) agent.getEndList();
    if (endList != null && !endList.isEmpty()) {        
        Label label = endList.get(0);
        if (label != null) {
            LiteralType.Lang lang = new LiteralType.Lang();
            lang.setLang(label.getLang());
            end.setLang(lang);
            end.setString(label.getValue());
        }        
    }
    agentType.setEnd(end);

    // gender
    Gender gender = new Gender();
    ArrayList<Label> genderList = (ArrayList<Label>) agent.getGender();
    if (genderList != null && !genderList.isEmpty()) {        
        Label label = genderList.get(0);
        if (label != null) {
            LiteralType.Lang lang = new LiteralType.Lang();
            lang.setLang(label.getLang());
            gender.setLang(lang);
            gender.setString(label.getValue());
        }        
    }
    agentType.setGender(gender);

    // hasMetList
    ArrayList<HasMet> hasMetList = new ArrayList<>();
    HasMet hasMet = new HasMet();
    ArrayList<Label> hasMetLabelList = (ArrayList<Label>) agent.getHasMet();
    if (hasMetLabelList != null && !hasMetLabelList.isEmpty()) {                      
        Label label = hasMetLabelList.get(0);        
        if (label != null)
            hasMet.setResource(label.getValue());            
    }
    hasMetList.add(hasMet);
    agentType.setHasMetList(hasMetList);

    // hasPartList: not available

    // identifierList
    ArrayList<Identifier> identifierList = new ArrayList<>();
    Identifier identifier = new Identifier();
    ArrayList<Label> identifierLabelList = (ArrayList<Label>) agent.getIdentifier();
    if (identifierLabelList != null && !identifierLabelList.isEmpty()) {                      
        Label label = identifierLabelList.get(0);        
        if (label != null)
        {
            LiteralType.Lang lang = new LiteralType.Lang();
            lang.setLang(label.getLang());
            identifier.setLang(lang);
            identifier.setString(label.getValue());
        }        
    }
    identifierList.add(identifier);
    agentType.setIdentifierList(identifierList);

    // isPartOfList: not available

    // isRelatedToList
    ArrayList<IsRelatedTo> isRelatedToList = new ArrayList<>(); 
    IsRelatedTo isRelatedTo = new IsRelatedTo();
    ArrayList<LabelResource> isRelatedToLabelResourceList = (ArrayList<LabelResource>) agent.getIsRelatedTo();
    if (isRelatedToLabelResourceList != null && !isRelatedToLabelResourceList.isEmpty()) {                        
        LabelResource labelResource = isRelatedToLabelResourceList.get(0);        
        if (labelResource != null)
        {
            isRelatedTo.setString(labelResource.getValue());
            ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
            lang.setLang(labelResource.getLang());
            isRelatedTo.setLang(lang);    
            ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
            resrc.setResource(labelResource.getResource());
            isRelatedTo.setResource(resrc);
        }        
    }
    isRelatedToList.add(isRelatedTo);
    agentType.setIsRelatedToList(isRelatedToList);

    // nameList: not available

    // noteList
    ArrayList<Note> noteList = new ArrayList<>();
    Note note = new Note();
    ArrayList<Label> noteLabelList = (ArrayList<Label>) agent.getNotes();
    if (noteLabelList != null && !noteLabelList.isEmpty()) {                      
        Label label = noteLabelList.get(0);        
        if (label != null)
        {
            LiteralType.Lang lang = new LiteralType.Lang();
            lang.setLang(label.getLang());
            note.setLang(lang);
            note.setString(label.getValue());
        }        
    }
    noteList.add(note);
    agentType.setNoteList(noteList);              

    // placeofBirth: not available

    // placeofDeath: not available

    // prefLabelList
    ArrayList<PrefLabel> prefLabelList = new ArrayList<>();
    PrefLabel prefLabel = new PrefLabel();
    ArrayList<Label> prefLabelLabelList = (ArrayList<Label>) agent.getPrefLabelList();
    if (prefLabelLabelList != null && !prefLabelLabelList.isEmpty()) {                        
        Label label = prefLabelLabelList.get(0);        
        if (label != null)
        {
            LiteralType.Lang lang = new LiteralType.Lang();
            lang.setLang(label.getLang());
            prefLabel.setLang(lang);
            prefLabel.setString(label.getValue());
        }        
    }
    prefLabelList.add(prefLabel);
    agentType.setPrefLabelList(prefLabelList);

    // professionOrOccupationList
    ArrayList<ProfessionOrOccupation> professionOrOccupationList = new ArrayList<>();
    ProfessionOrOccupation professionOrOccupation = new ProfessionOrOccupation();
    ArrayList<LabelResource> professionOrOccupationLabelResourceList = (ArrayList<LabelResource>) agent.getProfessionOrOccupation();
    if (professionOrOccupationLabelResourceList != null && !professionOrOccupationLabelResourceList.isEmpty()) {                      
        LabelResource labelResource = professionOrOccupationLabelResourceList.get(0);        
        if (labelResource != null)
        {
            professionOrOccupation.setString(labelResource.getValue());
            ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
            lang.setLang(labelResource.getLang());
            professionOrOccupation.setLang(lang); 
            ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
            resrc.setResource(labelResource.getResource());
            professionOrOccupation.setResource(resrc);
        }        
    }
    professionOrOccupationList.add(professionOrOccupation);
    agentType.setProfessionOrOccupationList(professionOrOccupationList);

    // sameAsList
    ArrayList<SameAs> sameAsList = new ArrayList<>();         
    SameAs sameAs = new SameAs();
    ArrayList<Part> sameAsPartList = (ArrayList<Part>) agent.getSameAs();
    if (sameAsPartList != null && !sameAsPartList.isEmpty()) {                        
        Part part = sameAsPartList.get(0);        
        if (part != null)
            sameAs.setResource(part.getResource());        
    }
    sameAsList.add(sameAs);
    agentType.setSameAList(sameAsList);

    if (rdf.getAgentList() == null)
        rdf.setAgentList(new ArrayList<AgentType>());
    
    rdf.getAgentList().add(agentType);
    
    if (StringUtils.isNotEmpty(fieldName)) {
         return appendToProxy(rdf, agentType, fieldName);
      } else {
          return rdf;
      }
  }
  
  private static RDF mergeConcept(RDF rdf, eu.europeana.enrichment.api.external.model.Concept baseConcept, String fieldName) {
    eu.europeana.corelib.definitions.jibx.Concept concept = new eu.europeana.corelib.definitions.jibx.Concept();

    // about
    if (baseConcept.getAbout() != null)
    	concept.setAbout(baseConcept.getAbout());
    else
    	concept.setAbout("");

    // choiceList
    ArrayList<Choice> choiceList = new ArrayList<>();

    Choice choice = new Choice();

    ArrayList<Label> altLabelList = (ArrayList<Label>) baseConcept.getAltLabelList();
    AltLabel altLabel = new AltLabel();
    if (altLabelList != null) {
        Label altLabelLabel = altLabelList.get(0);
        LiteralType.Lang lang1 = new LiteralType.Lang();
        lang1.setLang(altLabelLabel.getLang());
        altLabel.setLang(lang1);
        altLabel.setString(altLabelLabel.getValue());
    }
    choice.setAltLabel(altLabel);

    ArrayList<Resource> broadMatchResource = (ArrayList<Resource>)baseConcept.getBroadMatch();
    BroadMatch broadMatch = new BroadMatch();
    if (broadMatchResource != null)
        broadMatch.setResource(broadMatchResource.get(0).getResource());
    choice.setBroadMatch(broadMatch);

    ArrayList<Resource> broaderResource = (ArrayList<Resource>)baseConcept.getBroader();
    Broader broader = new Broader();
    if (broaderResource != null)
        broader.setResource(broaderResource.get(0).getResource());
    choice.setBroader(broader);

    ArrayList<Resource> closeMatchResource = (ArrayList<Resource>)baseConcept.getCloseMatch();
    CloseMatch closeMatch = new CloseMatch();
    if (closeMatchResource != null)
        closeMatch.setResource(closeMatchResource.get(0).getResource());
    choice.setCloseMatch(closeMatch);

    ArrayList<Resource> exactMatchResource = (ArrayList<Resource>)baseConcept.getExactMatch();
    ExactMatch exactMatch = new ExactMatch();
    if (exactMatchResource != null)
        exactMatch.setResource(exactMatchResource.get(0).getResource());
    choice.setExactMatch(exactMatch);

    ArrayList<Resource> inSchemeResource = (ArrayList<Resource>)baseConcept.getInScheme();
    InScheme inScheme = new InScheme();
    if (inSchemeResource != null)
        inScheme.setResource(inSchemeResource.get(0).getResource());
    choice.setInScheme(inScheme);

    ArrayList<Resource> narrowerResource = (ArrayList<Resource>)baseConcept.getNarrower();
    Narrower narrower = new Narrower();
    if (narrowerResource != null)
        narrower.setResource(narrowerResource.get(0).getResource());
    choice.setNarrower(narrower);

    ArrayList<Resource> narrowMatchResource = (ArrayList<Resource>)baseConcept.getNarrowMatch();
    NarrowMatch narrowMatch = new NarrowMatch();
    if (narrowMatchResource != null)
        narrowMatch.setResource(narrowMatchResource.get(0).getResource());
    choice.setNarrowMatch(narrowMatch);

    ArrayList<Label> notationList = (ArrayList<Label>) baseConcept.getNotation();
    Notation notation = new Notation();
    if (notationList != null) {
        Label notationLabel = notationList.get(0);
        LiteralType.Lang lang2 = new LiteralType.Lang();
        lang2.setLang(notationLabel.getLang());
        notation.setLang(lang2);
        notation.setString(notationLabel.getValue());
    }
    choice.setNotation(notation);

    ArrayList<Label> noteList = (ArrayList<Label>) baseConcept.getNotes();
    Note note = new Note();
    if (noteList != null) {
        Label noteLabel = noteList.get(0);
        LiteralType.Lang lang3 = new LiteralType.Lang();
        lang3.setLang(noteLabel.getLang());
        note.setLang(lang3);
        note.setString(noteLabel.getValue());
    }
    choice.setNote(note);

    ArrayList<Label> prefLabelList = (ArrayList<Label>) baseConcept.getPrefLabelList();
    PrefLabel prefLabel = new PrefLabel();
    if (prefLabelList != null) {
        Label prefLabelLabel = prefLabelList.get(0);
        LiteralType.Lang lang4 = new LiteralType.Lang();
        lang4.setLang(prefLabelLabel.getLang());
        prefLabel.setLang(lang4);
        prefLabel.setString(prefLabelLabel.getValue());
    }
    choice.setPrefLabel(prefLabel);

    ArrayList<Resource> relatedResource = (ArrayList<Resource>)baseConcept.getRelated();
    Related related = new Related();
    if (relatedResource != null)
        related.setResource(relatedResource.get(0).getResource());
    choice.setRelated(related);

    ArrayList<Resource> relatedMatchResource = (ArrayList<Resource>)baseConcept.getRelatedMatch();
    RelatedMatch relatedMatch = new RelatedMatch();
    if (relatedMatchResource != null)
        relatedMatch.setResource(relatedMatchResource.get(0).getResource());
    choice.setRelatedMatch(relatedMatch);

    choiceList.add(choice);
    concept.setChoiceList(choiceList);
    ArrayList<eu.europeana.corelib.definitions.jibx.Concept> conceptList = new ArrayList<>();
    conceptList.add(concept);

    if (rdf.getConceptList() == null)
        rdf.setConceptList(new ArrayList<Concept>());
    
    rdf.setConceptList(conceptList);
    
    if (StringUtils.isNotEmpty(fieldName)) {
        return appendToProxy(rdf, concept, fieldName);
      } else {
          return rdf;
      }
  }
  
  private static RDF mergeTimespan(RDF rdf, Timespan timespan, String fieldName) {
    TimeSpanType timeSpanType = new TimeSpanType();

    // about
    if (timespan.getAbout() != null)
    	timeSpanType.setAbout(timespan.getAbout());
    else
    	timeSpanType.setAbout("");

    // altLabelList
    ArrayList<AltLabel> altLabelList = new ArrayList<>();
    if (timespan.getAltLabelList() != null) {
        for (Label label : timespan.getAltLabelList()) {
            if (label != null) {
                AltLabel altLabel = new AltLabel();
                LiteralType.Lang lang = new LiteralType.Lang();
                lang.setLang(label.getLang());
                altLabel.setLang(lang);
                altLabel.setString(label.getValue());
                altLabelList.add(altLabel);
            }
        }
    } else {
    	AltLabel altLabel = new AltLabel();
    	altLabelList.add(altLabel);
    }
    timeSpanType.setAltLabelList(altLabelList);

    // begin      
    ArrayList<Label> beginList = (ArrayList<Label>) timespan.getBeginList();
    Begin begin = new Begin();
    if (beginList != null) {
        Label beginLabel = beginList.get(0);
        LiteralType.Lang lang2 = new LiteralType.Lang();
        lang2.setLang(beginLabel.getLang());
        begin.setLang(lang2);
        begin.setString(beginLabel.getValue());
    }
    timeSpanType.setBegin(begin);

    // end        
    ArrayList<Label> endList = (ArrayList<Label>) timespan.getEndList();
    End end = new End();
    if (endList != null) {
        Label endLabel = endList.get(0);
        LiteralType.Lang lang2 = new LiteralType.Lang();
        lang2.setLang(endLabel.getLang());
        end.setLang(lang2);
        end.setString(endLabel.getValue());
    }
    timeSpanType.setEnd(end);

    // hasPartList
    ArrayList<HasPart> hasPartList = new ArrayList<>();
    if (timespan.getHasPartsList() != null) {
        for (Part part : timespan.getHasPartsList()) {
            if (part != null) {
                HasPart hasPart = new HasPart();
                ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
                resrc.setResource(part.getResource());
                hasPart.setResource(resrc);
                hasPartList.add(hasPart);
            }
        }
    } else {
    	HasPart hasPart = new HasPart();
    	hasPartList.add(hasPart);
    }
    timeSpanType.setHasPartList(hasPartList);

    // isNextInSequence: not available

    // isPartOfList
    ArrayList<IsPartOf> isPartOfList = new ArrayList<>();
    if (timespan.getIsPartOfList() != null) {
        for (Part part : timespan.getIsPartOfList()) {
            if (part != null) {
                IsPartOf isPartOf = new IsPartOf();
                ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
                resrc.setResource(part.getResource());
                isPartOf.setResource(resrc);
                isPartOfList.add(isPartOf);
            }
        }
    } else {
    	IsPartOf isPartOf = new IsPartOf();
    	isPartOfList.add(isPartOf);
    }
    timeSpanType.setIsPartOfList(isPartOfList);

    // noteList
    ArrayList<Note> noteList = new ArrayList<>();
    if (timespan.getNotes() != null) {
        for (Label label : timespan.getNotes()) {
            if (label != null) {
                Note note = new Note();
                LiteralType.Lang lang = new LiteralType.Lang();
                lang.setLang(label.getLang());
                note.setLang(lang);
                note.setString(label.getValue());                             
                noteList.add(note);
            }
        }
    } else {
    	Note note = new Note();
    	noteList.add(note);
    }
    timeSpanType.setNoteList(noteList);

    // prefLabelList
    ArrayList<PrefLabel> prefLabelList = new ArrayList<>();
    if (timespan.getPrefLabelList() != null) {
        for (Label label : timespan.getPrefLabelList()) {
            if (label != null) {
                PrefLabel prefLabel = new PrefLabel();
                LiteralType.Lang lang = new LiteralType.Lang();
                lang.setLang(label.getLang());
                prefLabel.setLang(lang);
                prefLabel.setString(label.getValue());
                prefLabelList.add(prefLabel);
            }
        }
    } else {
    	PrefLabel prefLabel = new PrefLabel();
    	prefLabelList.add(prefLabel);
    }
    timeSpanType.setPrefLabelList(prefLabelList);

    // sameAsList
    ArrayList<SameAs> sameAsList = new ArrayList<>();
    if (timespan.getSameAs() != null) {
        for (Part part : timespan.getSameAs()) {
            if (part != null) {
                SameAs sameAs = new SameAs();
                sameAs.setResource(part.getResource());
                sameAsList.add(sameAs);
            }
        }
    } else {
    	SameAs sameAs = new SameAs();
    	sameAsList.add(sameAs);
    }
    timeSpanType.setSameAList(sameAsList);

    if (rdf.getTimeSpanList() == null)
        rdf.setTimeSpanList(new ArrayList<TimeSpanType>());
    
    rdf.getTimeSpanList().add(timeSpanType);
    
    if (StringUtils.isNotEmpty(fieldName)) {
        return appendToProxy(rdf, timeSpanType, fieldName);
      } else {
          return rdf;
      }
  }

  public static RDF mergeEntity(RDF rdf, List<EnrichmentBase> enrichmentBaseList,
      String fieldName) {
    for (EnrichmentBase enrichmentBase : enrichmentBaseList) {
      if (enrichmentBase.getClass() == Place.class) {
        return mergePlace(rdf, (Place) enrichmentBase, fieldName);
      }
      if (enrichmentBase.getClass() == Agent.class) {
        return mergeAgent(rdf, (Agent) enrichmentBase, fieldName);
      }
      if (enrichmentBase.getClass() == eu.europeana.enrichment.api.external.model.Concept.class) {
        return mergeConcept(rdf,
            (eu.europeana.enrichment.api.external.model.Concept) enrichmentBase, fieldName);
      }
      if (enrichmentBase.getClass() == Timespan.class) {
        return mergeTimespan(rdf, (Timespan) enrichmentBase, fieldName);
      }
    }
    return rdf;
  }
}
