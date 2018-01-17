package eu.europeana.metis.utils;

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
import eu.europeana.metis.common.model.Agent;
import eu.europeana.metis.common.model.EnrichmentBase;
import eu.europeana.metis.common.model.Label;
import eu.europeana.metis.common.model.LabelResource;
import eu.europeana.metis.common.model.Part;
import eu.europeana.metis.common.model.Place;
import eu.europeana.metis.common.model.Resource;
import eu.europeana.metis.common.model.Timespan;
import eu.europeana.corelib.definitions.jibx.Concept.Choice;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by erikkonijnenburg on 28/07/2017.
 */
class EntityMergeUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityMergeUtils.class);
  private final static String UTF8= "UTF-8";
  private static IBindingFactory agentFactory;
  private static IBindingFactory conceptFactory;
  private static IBindingFactory placeFactory;
  private static IBindingFactory tsFactory;
  private static IBindingFactory rdfFactory;

  static {
    try {
      agentFactory = BindingDirectory.getFactory(AgentType.class);
      conceptFactory = BindingDirectory.getFactory(Concept.class);
      placeFactory = BindingDirectory.getFactory(PlaceType.class);
      tsFactory = BindingDirectory.getFactory(TimeSpanType.class);
      rdfFactory = BindingDirectory.getFactory(RDF.class);
    } catch (JiBXException e) {
      LOGGER.error("Unable to get BindingFactory", e);
      System.exit(-1);
    }
  }

  public static RDF mergeEntity(String record, String entity) throws JiBXException {
    IUnmarshallingContext rdfCTX = rdfFactory.createUnmarshallingContext();
    RDF rdf = (RDF)rdfCTX.unmarshalDocument(IOUtils.toInputStream(record),UTF8);

    appendEntityInRDF(entity, rdf);

    return rdf;
  }

  public static RDF mergeEntityForEnrichment(String record, String entity, String fieldName) throws JiBXException {
    IUnmarshallingContext rdfCTX = rdfFactory.createUnmarshallingContext();
    RDF rdf = (RDF)rdfCTX.unmarshalDocument(IOUtils.toInputStream(record),UTF8);

    AboutType type =  appendEntityInRDF(entity, rdf);

    if (StringUtils.isNotEmpty(fieldName)) {
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
    IUnmarshallingContext unmarshaller = agentFactory.createUnmarshallingContext();
    AgentType agentType = (AgentType) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);

    if (rdf.getAgentList() == null) {
      rdf.setAgentList(new ArrayList<>());
    }
    rdf.getAgentList().add(agentType);
    return agentType;

  }

  private static Concept appendConceptInRDF(RDF rdf, String entity) throws JiBXException {
    IUnmarshallingContext unmarshaller = conceptFactory.createUnmarshallingContext();
    Concept conceptType = (Concept) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);
    if(rdf.getConceptList() == null) {
      rdf.setConceptList(new ArrayList<>());
    }
    rdf.getConceptList().add(conceptType);
    return conceptType;
  }

  private static PlaceType appendPlaceInRDF(RDF rdf, String entity) throws JiBXException {
    IUnmarshallingContext unmarshaller = placeFactory.createUnmarshallingContext();
    PlaceType placeType = (PlaceType) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);	  
    if (rdf.getPlaceList()==null) {
      rdf.setPlaceList(new ArrayList<>());
    }  
    rdf.getPlaceList().add(placeType);
    return placeType;
  }

  private static TimeSpanType appendTimespanInRDF(RDF rdf, String entity) throws JiBXException {
    IUnmarshallingContext unmarshaller = tsFactory.createUnmarshallingContext();
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
    return null;
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
  
  static RDF mergeEntity(RDF rdf, ArrayList<EnrichmentBase> enrichmentBaseList, String fieldName) {
	  for (EnrichmentBase enrichmentBase : enrichmentBaseList) {			
		  if (enrichmentBase.getClass() == Place.class) {
			  Place place_ = (Place)enrichmentBase;
			  PlaceType placeType = new PlaceType();

			  // about
			  placeType.setAbout(place_.getAbout());

			  // alt					
			  String place_Alt = place_.getAlt();
			  if (place_Alt != null) {
				  Alt alt = new Alt();
				  alt.setAlt(Float.valueOf(place_.getAlt()));
				  placeType.setAlt(alt);
			  }

			  // altLabelList					
			  if (place_.getAltLabelList() != null) {
				  ArrayList<AltLabel> altLabelList = new ArrayList<AltLabel>();
				  for (Label label : place_.getAltLabelList()) {
					  if (label != null) {
						  AltLabel altLabel = new AltLabel();
						  LiteralType.Lang lang = new LiteralType.Lang();
						  lang.setLang(label.getLang());
						  altLabel.setLang(lang);
						  altLabel.setString(label.getValue());
						  altLabelList.add(altLabel);
					  }
				  }	
				  placeType.setAltLabelList(altLabelList);
			  }

			  // hasPartList					
			  if (place_.getHasPartsList() != null) {
				  ArrayList<HasPart> hasPartList = new ArrayList<HasPart>();
				  for (Part part : place_.getHasPartsList()) {
					  if (part != null) {
						  HasPart hasPart = new HasPart();
						  ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
						  resrc.setResource(part.getResource());
						  hasPart.setResource(resrc);
						  hasPartList.add(hasPart);
					  }
				  }	
				  placeType.setHasPartList(hasPartList);
			  }	

			  // isPartOfList
			  if (place_.getIsPartOfList() != null) {
				  ArrayList<IsPartOf> isPartOfList = new ArrayList<IsPartOf>();
				  for (Part part : place_.getIsPartOfList()) {
					  if (part != null) {
						  IsPartOf isPartOf = new IsPartOf();							
						  ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
						  resrc.setResource(part.getResource());
						  isPartOf.setResource(resrc);
						  isPartOf.setString("");
						  isPartOfList.add(isPartOf);
					  }
				  }		
				  placeType.setIsPartOfList(isPartOfList);
			  }

			  // lat	
			  if (place_.getLat() != null) {
				  Lat lat = new Lat();
				  lat.setLat(Float.valueOf(place_.getLat()));
				  placeType.setLat(lat);
			  }

			  // _long
			  if (place_.getLon() != null) {
				  _Long _long = new _Long();
				  _long.setLong(Float.valueOf(place_.getLon()));
				  placeType.setLong(_long);
			  }

			  // noteList					
			  if (place_.getNotes() != null) {
				  ArrayList<Note> noteList = new ArrayList<Note>();
				  for (Label label : place_.getNotes()) {
					  if (label != null) {
						  Note note = new Note();
						  LiteralType.Lang lang = new LiteralType.Lang();
						  lang.setLang(label.getLang());
						  note.setLang(lang);
						  note.setString(label.getValue());
						  noteList.add(note);
					  }
				  }		
				  placeType.setNoteList(noteList);
			  }

			  // prefLabelList
			  if (place_.getPrefLabelList() != null) {
				  ArrayList<PrefLabel> prefLabelList = new ArrayList<PrefLabel>();
				  for (Label label : place_.getPrefLabelList()) {
					  if (label != null) {
						  PrefLabel prefLabel = new PrefLabel();
						  LiteralType.Lang lang = new LiteralType.Lang();
						  lang.setLang(label.getLang());
						  prefLabel.setLang(lang);
						  prefLabel.setString(label.getValue());
						  prefLabelList.add(prefLabel);
					  }
				  }
				  placeType.setPrefLabelList(prefLabelList);
			  }				

			  // sameAsList					
			  if (place_.getSameAs() != null) {
				  ArrayList<SameAs> sameAsList = new ArrayList<SameAs>();
				  for (Part part : place_.getSameAs()) {
					  if (part != null) {
						  SameAs sameAs = new SameAs();						
						  sameAs.setResource(part.getResource());												
						  sameAsList.add(sameAs);
					  }
				  }
				  placeType.setSameAList(sameAsList);
			  }

			  // isNextInSequence: not available
			  
			  if (rdf.getPlaceList() == null)
				  rdf.setPlaceList(new ArrayList<PlaceType>());
			  
			  rdf.getPlaceList().add(placeType);
			  
			  if (StringUtils.isNotEmpty(fieldName)) {
				  AboutType type = placeType;				 
			      return appendToProxy(rdf, type, fieldName);
			    } else {
			    	return rdf;
			    }				
		  }

		  if (enrichmentBase.getClass() == Agent.class) {
			  Agent agent = (Agent)enrichmentBase;
			  AgentType agentType = new AgentType();

			  // about
			  agentType.setAbout(agent.getAbout());

			  // altLabelList
			  ArrayList<AltLabel> altLabelList = new ArrayList<AltLabel>();
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
			  }
			  agentType.setAltLabelList(altLabelList);

			  // begin
			  ArrayList<Label> beginList = (ArrayList<Label>) agent.getBeginList();
			  if (beginList != null && beginList.size()>0) {
				  Begin begin = new Begin();
				  Label label = beginList.get(0);
				  if (label != null) {
					  LiteralType.Lang lang = new LiteralType.Lang();
					  lang.setLang(label.getLang());
					  begin.setLang(lang);
					  begin.setString(label.getValue());
				  }
				  agentType.setBegin(begin);
			  }

			  // biographicalInformation
			  ArrayList<Label> bioInfoList = (ArrayList<Label>) agent.getBiographicaInformation();
			  if (bioInfoList != null && bioInfoList.size()>0) {
				  BiographicalInformation bioInfo = new BiographicalInformation();
				  Label label = bioInfoList.get(0);
				  if (label != null) {
					  LiteralType.Lang lang = new LiteralType.Lang();
					  lang.setLang(label.getLang());
					  bioInfo.setLang(lang);
					  bioInfo.setString(label.getValue());
				  }
				  agentType.setBiographicalInformation(bioInfo);
			  }

			  // dateList
			  ArrayList<Date> dateList_ = new ArrayList<Date>();					
			  ArrayList<Label> dateList = (ArrayList<Label>) agent.getBiographicaInformation();
			  if (dateList != null && dateList.size()>0) {						
				  Label label = dateList.get(0);
				  Date date_ = new Date();
				  if (label != null) {							
					  ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
					  lang.setLang(label.getLang());
					  date_.setLang(lang);
					  date_.setString(label.getValue());
				  }			
				  dateList_.add(date_);
			  }
			  agentType.setDateList(dateList_);

			  // dateOfBirth
			  ArrayList<Label> dateOfBirthList = (ArrayList<Label>) agent.getDateOfBirth();
			  if (dateOfBirthList != null && dateOfBirthList.size()>0) {
				  DateOfBirth dateOfBirth = new DateOfBirth();
				  Label label = dateOfBirthList.get(0);
				  if (label != null) {
					  LiteralType.Lang lang = new LiteralType.Lang();
					  lang.setLang(label.getLang());
					  dateOfBirth.setLang(lang);
					  dateOfBirth.setString(label.getValue());
				  }
				  agentType.setDateOfBirth(dateOfBirth);
			  }

			  // dateofDeath				
			  ArrayList<Label> dateOfDeathList = (ArrayList<Label>) agent.getDateOfDeath();
			  if (dateOfDeathList != null && dateOfDeathList.size()>0) {
				  DateOfDeath dateOfDeath = new DateOfDeath();
				  Label label = dateOfDeathList.get(0);
				  if (label != null) {
					  LiteralType.Lang lang = new LiteralType.Lang();
					  lang.setLang(label.getLang());
					  dateOfDeath.setLang(lang);
					  dateOfDeath.setString(label.getValue());
				  }
				  agentType.setDateOfDeath(dateOfDeath);
			  }

			  // dateOfEstablishment
			  ArrayList<Label> dateOfEstablishmentList = (ArrayList<Label>) agent.getDateOfEstablishment();
			  if (dateOfEstablishmentList != null && dateOfEstablishmentList.size()>0) {
				  DateOfEstablishment dateOfEstablishment = new DateOfEstablishment();
				  Label label = dateOfEstablishmentList.get(0);
				  if (label != null) {
					  LiteralType.Lang lang = new LiteralType.Lang();
					  lang.setLang(label.getLang());
					  dateOfEstablishment.setLang(lang);
					  dateOfEstablishment.setString(label.getValue());
				  }
				  agentType.setDateOfEstablishment(dateOfEstablishment);
			  }

			  // dateofTermination
			  ArrayList<Label> dateOfTerminationList = (ArrayList<Label>) agent.getDateOfTermination();
			  if (dateOfTerminationList != null && dateOfTerminationList.size()>0) {
				  DateOfTermination dateOfTermination = new DateOfTermination();
				  Label label = dateOfTerminationList.get(0);
				  if (label != null) {
					  LiteralType.Lang lang = new LiteralType.Lang();
					  lang.setLang(label.getLang());
					  dateOfTermination.setLang(lang);
					  dateOfTermination.setString(label.getValue());
				  }
				  agentType.setDateOfTermination(dateOfTermination);
			  }

			  // end
			  ArrayList<Label> endList = (ArrayList<Label>) agent.getEndList();
			  if (endList != null && endList.size()>0) {
				  End end = new End();
				  Label label = endList.get(0);
				  if (label != null) {
					  LiteralType.Lang lang = new LiteralType.Lang();
					  lang.setLang(label.getLang());
					  end.setLang(lang);
					  end.setString(label.getValue());
				  }
				  agentType.setEnd(end);
			  }

			  // gender
			  ArrayList<Label> genderList = (ArrayList<Label>) agent.getGender();
			  if (genderList != null && genderList.size()>0) {
				  Gender gender = new Gender();
				  Label label = genderList.get(0);
				  if (label != null) {
					  LiteralType.Lang lang = new LiteralType.Lang();
					  lang.setLang(label.getLang());
					  gender.setLang(lang);
					  gender.setString(label.getValue());
				  }
				  agentType.setGender(gender);
			  }

			  // hasMetList
			  ArrayList<HasMet> hasMetList_ = new ArrayList<HasMet>();					
			  ArrayList<Label> hasMetList = (ArrayList<Label>) agent.getHasMet();
			  if (hasMetList != null && hasMetList.size()>0) {						
				  Label label = hasMetList.get(0);
				  HasMet hasMet_ = new HasMet();
				  if (label != null)
					  hasMet_.setResource(label.getValue());		
				  hasMetList_.add(hasMet_);
			  }
			  agentType.setHasMetList(hasMetList_);

			  // hasPartList: not available

			  // identifierList
			  ArrayList<Identifier> identifierList_ = new ArrayList<Identifier>();					
			  ArrayList<Label> identifierList = (ArrayList<Label>) agent.getIdentifier();
			  if (identifierList != null && identifierList.size()>0) {						
				  Label label = identifierList.get(0);
				  Identifier identifier_ = new Identifier();
				  if (label != null)
				  {
					  LiteralType.Lang lang = new LiteralType.Lang();
					  lang.setLang(label.getLang());
					  identifier_.setLang(lang);
					  identifier_.setString(label.getValue());
				  }
				  identifierList_.add(identifier_);
			  }
			  agentType.setIdentifierList(identifierList_);

			  // isPartOfList: not available

			  // isRelatedToList
			  ArrayList<IsRelatedTo> isRelatedToList_ = new ArrayList<IsRelatedTo>();					
			  ArrayList<LabelResource> isRelatedToList = (ArrayList<LabelResource>) agent.getIsRelatedTo();
			  if (isRelatedToList != null && isRelatedToList.size()>0) {						
				  LabelResource labelResource = isRelatedToList.get(0);
				  IsRelatedTo isRelatedTo_ = new IsRelatedTo();
				  if (labelResource != null)
				  {
					  isRelatedTo_.setString(labelResource.getValue());
					  ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
					  lang.setLang(labelResource.getLang());
					  isRelatedTo_.setLang(lang);	
					  ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
					  resrc.setResource(labelResource.getResource());
					  isRelatedTo_.setResource(resrc);
				  }
				  isRelatedToList_.add(isRelatedTo_);
			  }
			  agentType.setIsRelatedToList(isRelatedToList_);

			  // nameList: not available

			  // noteList
			  ArrayList<Note> noteList_ = new ArrayList<Note>();					
			  ArrayList<Label> noteList = (ArrayList<Label>) agent.getNotes();
			  if (noteList != null && noteList.size()>0) {						
				  Label label = noteList.get(0);
				  Note note_ = new Note();
				  if (label != null)
				  {
					  LiteralType.Lang lang = new LiteralType.Lang();
					  lang.setLang(label.getLang());
					  note_.setLang(lang);
					  note_.setString(label.getValue());
				  }
				  noteList_.add(note_);
			  }
			  agentType.setNoteList(noteList_);				

			  // placeofBirth: not available

			  // placeofDeath: not available

			  // prefLabelList
			  ArrayList<PrefLabel> prefLabelList_ = new ArrayList<PrefLabel>();					
			  ArrayList<Label> prefLabelList = (ArrayList<Label>) agent.getPrefLabelList();
			  if (prefLabelList != null && prefLabelList.size()>0) {						
				  Label label = prefLabelList.get(0);
				  PrefLabel prefLabel_ = new PrefLabel();
				  if (label != null)
				  {
					  LiteralType.Lang lang = new LiteralType.Lang();
					  lang.setLang(label.getLang());
					  prefLabel_.setLang(lang);
					  prefLabel_.setString(label.getValue());
				  }
				  prefLabelList_.add(prefLabel_);
			  }
			  agentType.setPrefLabelList(prefLabelList_);

			  // professionOrOccupationList
			  ArrayList<ProfessionOrOccupation> professionOrOccupationList_ = new ArrayList<ProfessionOrOccupation>();					
			  ArrayList<LabelResource> professionOrOccupationList = (ArrayList<LabelResource>) agent.getProfessionOrOccupation();
			  if (professionOrOccupationList != null && professionOrOccupationList.size()>0) {						
				  LabelResource labelResource = professionOrOccupationList.get(0);
				  ProfessionOrOccupation professionOrOccupation_ = new ProfessionOrOccupation();
				  if (labelResource != null)
				  {
					  professionOrOccupation_.setString(labelResource.getValue());
					  ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
					  lang.setLang(labelResource.getLang());
					  professionOrOccupation_.setLang(lang);	
					  ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
					  resrc.setResource(labelResource.getResource());
					  professionOrOccupation_.setResource(resrc);
				  }
				  professionOrOccupationList_.add(professionOrOccupation_);
			  }
			  agentType.setProfessionOrOccupationList(professionOrOccupationList_);

			  // sameAsList
			  ArrayList<SameAs> sameAsList_ = new ArrayList<SameAs>();					
			  ArrayList<Part> sameAsList = (ArrayList<Part>) agent.getSameAs();
			  if (sameAsList != null && sameAsList.size()>0) {						
				  Part part = sameAsList.get(0);
				  SameAs sameAs_ = new SameAs();
				  if (part != null)
					  sameAs_.setResource(part.getResource());
				  sameAsList_.add(sameAs_);
			  }
			  agentType.setSameAList(sameAsList_);

			  if (rdf.getAgentList() == null)
				  rdf.setAgentList(new ArrayList<AgentType>());
			  
			  rdf.getAgentList().add(agentType);
			  
			  if (StringUtils.isNotEmpty(fieldName)) {
				  AboutType type = agentType;				 
			      return appendToProxy(rdf, type, fieldName);
			    } else {
			    	return rdf;
			    }
		  }

		  if (enrichmentBase.getClass() == eu.europeana.metis.common.model.Concept.class) {
			  eu.europeana.metis.common.model.Concept concept = (eu.europeana.metis.common.model.Concept)enrichmentBase;
			  eu.europeana.corelib.definitions.jibx.Concept concept_ = new eu.europeana.corelib.definitions.jibx.Concept();

			  // about
			  concept_.setAbout(concept.getAbout());

			  // choiceList
			  ArrayList<Choice> choiceList_ = new ArrayList<Choice>();

			  Choice choice_ = new Choice();

			  ArrayList<Label> altLabelList = (ArrayList<Label>) concept.getAltLabelList();
			  AltLabel altLabel_ = new AltLabel();
			  if (altLabelList != null) {
				  Label altLabel = altLabelList.get(0);
				  LiteralType.Lang lang1 = new LiteralType.Lang();
				  lang1.setLang(altLabel.getLang());
				  altLabel_.setLang(lang1);
				  altLabel_.setString(altLabel.getValue());
			  }
			  choice_.setAltLabel(altLabel_);

			  ArrayList<Resource> broadMatch = (ArrayList<Resource>)concept.getBroadMatch();
			  BroadMatch broadMatch_ = new BroadMatch();
			  if (broadMatch != null)
				  broadMatch_.setResource(broadMatch.get(0).getResource());
			  choice_.setBroadMatch(broadMatch_);

			  ArrayList<Resource> broader = (ArrayList<Resource>)concept.getBroader();
			  Broader broader_ = new Broader();
			  if (broader != null)
				  broader_.setResource(broader.get(0).getResource());
			  choice_.setBroader(broader_);

			  ArrayList<Resource> closeMatch = (ArrayList<Resource>)concept.getCloseMatch();
			  CloseMatch closeMatch_ = new CloseMatch();
			  if (closeMatch != null)
				  closeMatch_.setResource(closeMatch.get(0).getResource());
			  choice_.setCloseMatch(closeMatch_);

			  ArrayList<Resource> exactMatch = (ArrayList<Resource>)concept.getExactMatch();
			  ExactMatch exactMatch_ = new ExactMatch();
			  if (exactMatch != null)
				  exactMatch_.setResource(exactMatch.get(0).getResource());
			  choice_.setExactMatch(exactMatch_);

			  ArrayList<Resource> inScheme = (ArrayList<Resource>)concept.getInScheme();
			  InScheme inScheme_ = new InScheme();
			  if (inScheme != null)
				  inScheme_.setResource(inScheme.get(0).getResource());
			  choice_.setInScheme(inScheme_);

			  ArrayList<Resource> narrower = (ArrayList<Resource>)concept.getNarrower();
			  Narrower narrower_ = new Narrower();
			  if (narrower != null)
				  narrower_.setResource(narrower.get(0).getResource());
			  choice_.setNarrower(narrower_);

			  ArrayList<Resource> narrowMatch = (ArrayList<Resource>)concept.getNarrowMatch();
			  NarrowMatch narrowMatch_ = new NarrowMatch();
			  if (narrowMatch != null)
				  narrowMatch_.setResource(narrowMatch.get(0).getResource());
			  choice_.setNarrowMatch(narrowMatch_);

			  ArrayList<Label> notationList = (ArrayList<Label>) concept.getNotation();
			  Notation notation_ = new Notation();
			  if (notationList != null) {
				  Label notation = notationList.get(0);
				  LiteralType.Lang lang2 = new LiteralType.Lang();
				  lang2.setLang(notation.getLang());
				  notation_.setLang(lang2);
				  notation_.setString(notation.getValue());
			  }
			  choice_.setNotation(notation_);

			  ArrayList<Label> noteList = (ArrayList<Label>) concept.getNotes();
			  Note note_ = new Note();
			  if (noteList != null) {
				  Label note = noteList.get(0);
				  LiteralType.Lang lang3 = new LiteralType.Lang();
				  lang3.setLang(note.getLang());
				  note_.setLang(lang3);
				  note_.setString(note.getValue());
			  }
			  choice_.setNote(note_);

			  ArrayList<Label> prefLabelList = (ArrayList<Label>) concept.getPrefLabelList();
			  PrefLabel prefLabel_ = new PrefLabel();
			  if (prefLabelList != null) {
				  Label prefLabel = prefLabelList.get(0);
				  LiteralType.Lang lang4 = new LiteralType.Lang();
				  lang4.setLang(prefLabel.getLang());
				  prefLabel_.setLang(lang4);
				  prefLabel_.setString(prefLabel.getValue());
			  }
			  choice_.setPrefLabel(prefLabel_);

			  ArrayList<Resource> related = (ArrayList<Resource>)concept.getRelated();
			  Related related_ = new Related();
			  if (related != null)
				  related_.setResource(related.get(0).getResource());
			  choice_.setRelated(related_);

			  ArrayList<Resource> relatedMatch = (ArrayList<Resource>)concept.getRelatedMatch();
			  RelatedMatch relatedMatch_ = new RelatedMatch();
			  if (relatedMatch != null)
				  relatedMatch_.setResource(relatedMatch.get(0).getResource());
			  choice_.setRelatedMatch(relatedMatch_);

			  choiceList_.add(choice_);
			  concept_.setChoiceList(choiceList_);
			  ArrayList<eu.europeana.corelib.definitions.jibx.Concept> conceptList = new ArrayList<eu.europeana.corelib.definitions.jibx.Concept>();
			  conceptList.add(concept_);

			  if (rdf.getConceptList() == null)
				  rdf.setConceptList(new ArrayList<Concept>());
			  
			  rdf.setConceptList(conceptList);
			  
			  if (StringUtils.isNotEmpty(fieldName)) {
				  AboutType type = concept_;				 
			      return appendToProxy(rdf, type, fieldName);
			    } else {
			    	return rdf;
			    }
		  }

		  if (enrichmentBase.getClass() == Timespan.class) {
			  Timespan timespan = (Timespan)enrichmentBase;
			  TimeSpanType timeSpanType = new TimeSpanType();

			  // about
			  timeSpanType.setAbout(timespan.getAbout());

			  // altLabelList
			  ArrayList<AltLabel> altLabelList = new ArrayList<AltLabel>();
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
			  }
			  timeSpanType.setAltLabelList(altLabelList);

			  // begin		
			  ArrayList<Label> beginList = (ArrayList<Label>) timespan.getBeginList();
			  Begin begin_ = new Begin();
			  if (beginList != null) {
				  Label begin = beginList.get(0);
				  LiteralType.Lang lang2 = new LiteralType.Lang();
				  lang2.setLang(begin.getLang());
				  begin_.setLang(lang2);
				  begin_.setString(begin.getValue());
			  }
			  timeSpanType.setBegin(begin_);

			  // end		
			  ArrayList<Label> endList = (ArrayList<Label>) timespan.getEndList();
			  End end_ = new End();
			  if (endList != null) {
				  Label end = endList.get(0);
				  LiteralType.Lang lang2 = new LiteralType.Lang();
				  lang2.setLang(end.getLang());
				  end_.setLang(lang2);
				  end_.setString(end.getValue());
			  }
			  timeSpanType.setEnd(end_);

			  // hasPartList
			  ArrayList<HasPart> hasPartList = new ArrayList<HasPart>();
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
			  }
			  timeSpanType.setHasPartList(hasPartList);

			  // isNextInSequence: not available

			  // isPartOfList
			  ArrayList<IsPartOf> isPartOfList = new ArrayList<IsPartOf>();
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
			  }
			  timeSpanType.setIsPartOfList(isPartOfList);

			  // noteList
			  ArrayList<Note> noteList = new ArrayList<Note>();
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
			  }
			  timeSpanType.setNoteList(noteList);

			  // prefLabelList
			  ArrayList<PrefLabel> prefLabelList = new ArrayList<PrefLabel>();
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
			  }
			  timeSpanType.setPrefLabelList(prefLabelList);

			  // sameAsList
			  ArrayList<SameAs> sameAsList = new ArrayList<SameAs>();
			  if (timespan.getSameAs() != null) {
				  for (Part part : timespan.getSameAs()) {
					  if (part != null) {
						  SameAs sameAs = new SameAs();
						  sameAs.setResource(part.getResource());
						  sameAsList.add(sameAs);
					  }
				  }
			  }
			  timeSpanType.setSameAList(sameAsList);

			  if (rdf.getTimeSpanList() == null)
				  rdf.setTimeSpanList(new ArrayList<TimeSpanType>());
			  
			  rdf.getTimeSpanList().add(timeSpanType);
			  
			  if (StringUtils.isNotEmpty(fieldName)) {
				  AboutType type = timeSpanType;				 
			      return appendToProxy(rdf, type, fieldName);
			    } else {
			    	return rdf;
			    }
		  }
	  }

	  return rdf;
	}
}
