package eu.europeana.enrichment.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Alternative;
import eu.europeana.corelib.definitions.jibx.BitRate;
import eu.europeana.corelib.definitions.jibx.BroadMatch;
import eu.europeana.corelib.definitions.jibx.CloseMatch;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.Concept.Choice;
import eu.europeana.corelib.definitions.jibx.Contributor;
import eu.europeana.corelib.definitions.jibx.Coverage;
import eu.europeana.corelib.definitions.jibx.Created;
import eu.europeana.corelib.definitions.jibx.Creator;
import eu.europeana.corelib.definitions.jibx.CurrentLocation;
import eu.europeana.corelib.definitions.jibx.Date;
import eu.europeana.corelib.definitions.jibx.ExactMatch;
import eu.europeana.corelib.definitions.jibx.Extent;
import eu.europeana.corelib.definitions.jibx.Format;
import eu.europeana.corelib.definitions.jibx.HasFormat;
import eu.europeana.corelib.definitions.jibx.HasMet;
import eu.europeana.corelib.definitions.jibx.HasPart;
import eu.europeana.corelib.definitions.jibx.HasType;
import eu.europeana.corelib.definitions.jibx.HasVersion;
import eu.europeana.corelib.definitions.jibx.Incorporates;
import eu.europeana.corelib.definitions.jibx.IsDerivativeOf;
import eu.europeana.corelib.definitions.jibx.IsFormatOf;
import eu.europeana.corelib.definitions.jibx.IsPartOf;
import eu.europeana.corelib.definitions.jibx.IsReferencedBy;
import eu.europeana.corelib.definitions.jibx.IsRelatedTo;
import eu.europeana.corelib.definitions.jibx.IsRequiredBy;
import eu.europeana.corelib.definitions.jibx.IsSimilarTo;
import eu.europeana.corelib.definitions.jibx.IsSuccessorOf;
import eu.europeana.corelib.definitions.jibx.IsVersionOf;
import eu.europeana.corelib.definitions.jibx.Issued;
import eu.europeana.corelib.definitions.jibx.Medium;
import eu.europeana.corelib.definitions.jibx.NarrowMatch;
import eu.europeana.corelib.definitions.jibx.Note;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.Publisher;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.Realizes;
import eu.europeana.corelib.definitions.jibx.References;
import eu.europeana.corelib.definitions.jibx.Related;
import eu.europeana.corelib.definitions.jibx.RelatedMatch;
import eu.europeana.corelib.definitions.jibx.Relation;
import eu.europeana.corelib.definitions.jibx.Replaces;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.enrichment.utils.DereferenceUtils;
import eu.europeana.corelib.definitions.jibx.SameAs;
import eu.europeana.corelib.definitions.jibx.Source;
import eu.europeana.corelib.definitions.jibx.Spatial;
import eu.europeana.corelib.definitions.jibx.Subject;
import eu.europeana.corelib.definitions.jibx.Temporal;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.jibx.Type;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

public class DereferenceUtilsTest {
	
    @Test
    public void testPlaceListExtractedValues() throws IOException {
    	RDF rdf = new RDF();
        
        PlaceType place = new PlaceType();
        place.setAbout("http://dummy1.dum");
        
        IsPartOf isPartOf = new IsPartOf();      
        ResourceOrLiteralType.Resource resource3 = new ResourceOrLiteralType.Resource();
        resource3.setResource("http://dummy2.dum");
        isPartOf.setResource(resource3);
        ArrayList<IsPartOf> isPartOfList = new ArrayList<IsPartOf>();
        isPartOfList.add(isPartOf);
        place.setIsPartOfList(isPartOfList);
        
        SameAs sameAs = new SameAs();      
        sameAs.setResource("http://dummy3.dum");
        ArrayList<SameAs> sameAsList = new ArrayList<SameAs>();
        sameAsList.add(sameAs);
        place.setSameAList(sameAsList);
        
        HasPart hasPart = new HasPart();
        Resource resource4 = new Resource();
        resource4.setResource("http://dummy4.dum");
        hasPart.setResource(resource4);
        ArrayList<HasPart> hasPartList = new ArrayList<HasPart>();
        hasPartList.add(hasPart);
        place.setHasPartList(hasPartList);
        
        // Should be rejected
        Note note = new Note();      
        note.setString("Note");
        ArrayList<Note> noteList = new ArrayList<Note>();
        noteList.add(note);
        place.setNoteList(noteList);
        
        ArrayList<PlaceType> placeList = new ArrayList<PlaceType>();
        placeList.add(place);
        
        rdf.setPlaceList(placeList);
        
        Set<String> result = DereferenceUtils.extractValuesForDereferencing(rdf);
		
		Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
        
        Assert.assertTrue(result.contains("http://dummy1.dum"));
        Assert.assertTrue(result.contains("http://dummy2.dum"));
        Assert.assertTrue(result.contains("http://dummy3.dum"));
        Assert.assertTrue(result.contains("http://dummy4.dum"));
    }
    
    @Test
    public void testAgentListExtractedValues() throws IOException {
    	RDF rdf = new RDF();
        
        AgentType agent = new AgentType();
        agent.setAbout("http://dummy1.dum");
        
        HasMet hasMet = new HasMet();      
        hasMet.setResource("http://dummy2.dum");
        ArrayList<HasMet> hasMetList = new ArrayList<HasMet>();
        hasMetList.add(hasMet);
        agent.setHasMetList(hasMetList);
        
        IsRelatedTo isRelatedTo = new IsRelatedTo();
        ResourceOrLiteralType.Resource resource3 = new ResourceOrLiteralType.Resource();
        resource3.setResource("http://dummy3.dum");
        isRelatedTo.setResource(resource3);
        ArrayList<IsRelatedTo> isRelatedToList = new ArrayList<IsRelatedTo>();
        isRelatedToList.add(isRelatedTo);
        agent.setIsRelatedToList(isRelatedToList);
        
        // Should be rejected
        Note note = new Note();      
        note.setString("Note");
        ArrayList<Note> noteList = new ArrayList<Note>();
        noteList.add(note);
        agent.setNoteList(noteList);
        
        ArrayList<AgentType> agentList = new ArrayList<AgentType>();
        agentList.add(agent);

        rdf.setAgentList(agentList);
        
        Set<String> result = DereferenceUtils.extractValuesForDereferencing(rdf);
		
		Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());
        
        Assert.assertTrue(result.contains("http://dummy1.dum"));
        Assert.assertTrue(result.contains("http://dummy2.dum"));
        Assert.assertTrue(result.contains("http://dummy3.dum"));
    }
    
    @Test
    public void testConceptListExtractedValues() throws IOException {
    	RDF rdf = new RDF();
        
        Concept concept = new Concept();
        concept.setAbout("http://dummy1.dum");
        
        Choice choice1 = new Choice();              
        BroadMatch broadMatch = new BroadMatch();
        broadMatch.setResource("http://dummy2.dum");
        choice1.setBroadMatch(broadMatch);
        
        Choice choice2 = new Choice();
        CloseMatch closeMatch = new CloseMatch();
        closeMatch.setResource("http://dummy3.dum");
        choice2.setCloseMatch(closeMatch);

        Choice choice3 = new Choice();
        ExactMatch exactMatch = new ExactMatch();
        exactMatch.setResource("http://dummy4.dum");
        choice3.setExactMatch(exactMatch);
        
        Choice choice4 = new Choice();
        NarrowMatch narrowMatch = new NarrowMatch();
        narrowMatch.setResource("http://dummy5.dum");
        choice4.setNarrowMatch(narrowMatch);

        Choice choice5 = new Choice();
        RelatedMatch relatedMatch = new RelatedMatch();
        relatedMatch.setResource("http://dummy6.dum");
        choice5.setRelatedMatch(relatedMatch);
        
        Choice choice6 = new Choice();
        Related related = new Related();
        related.setResource("http://dummy7.dum");
        choice6.setRelated(related);
        
        // Should be rejected
        Choice choice7 = new Choice();
        Note note = new Note();      
        note.setString("Note");
        choice7.setNote(note);

        ArrayList<Choice> choiceList = new ArrayList<Choice>();
        choiceList.add(choice1);
        choiceList.add(choice2);
        choiceList.add(choice3);
        choiceList.add(choice4);
        choiceList.add(choice5);
        choiceList.add(choice6);
        choiceList.add(choice7);
        
        concept.setChoiceList(choiceList);
        
        ArrayList<Concept> conceptList = new ArrayList<Concept>();
        conceptList.add(concept);
        
        rdf.setConceptList(conceptList);
        
        Set<String> result = DereferenceUtils.extractValuesForDereferencing(rdf);
		
		Assert.assertNotNull(result);
        Assert.assertEquals(7, result.size());
        
        Assert.assertTrue(result.contains("http://dummy1.dum"));
        Assert.assertTrue(result.contains("http://dummy2.dum"));
        Assert.assertTrue(result.contains("http://dummy3.dum"));
        Assert.assertTrue(result.contains("http://dummy4.dum"));
        Assert.assertTrue(result.contains("http://dummy5.dum"));
        Assert.assertTrue(result.contains("http://dummy6.dum"));
        Assert.assertTrue(result.contains("http://dummy7.dum"));
    }
    
    @Test
    public void testTimeSpanListExtractedValues() throws IOException {
    	RDF rdf = new RDF();
        
        TimeSpanType timeSpan = new TimeSpanType();
        timeSpan.setAbout("http://dummy1.dum");
        
        HasPart hasPart = new HasPart();
        Resource resource3 = new Resource();
        resource3.setResource("http://dummy2.dum");
        hasPart.setResource(resource3);
        ArrayList<HasPart> hasPartList = new ArrayList<HasPart>();
        hasPartList.add(hasPart);
        timeSpan.setHasPartList(hasPartList);
        
        SameAs sameAs = new SameAs();      
        sameAs.setResource("http://dummy3.dum");
        ArrayList<SameAs> sameAsList = new ArrayList<SameAs>();
        sameAsList.add(sameAs);
        timeSpan.setSameAList(sameAsList);
        
        IsPartOf isPartOf = new IsPartOf();      
        ResourceOrLiteralType.Resource resource4 = new ResourceOrLiteralType.Resource();
        resource4.setResource("http://dummy4.dum");
        isPartOf.setResource(resource4);
        ArrayList<IsPartOf> isPartOfList = new ArrayList<IsPartOf>();
        isPartOfList.add(isPartOf);
        timeSpan.setIsPartOfList(isPartOfList);
        
        // Should be rejected
        Note note = new Note();      
        note.setString("Note");
        ArrayList<Note> noteList = new ArrayList<Note>();
        noteList.add(note);
        timeSpan.setNoteList(noteList);
        
        ArrayList<TimeSpanType> timeSpanList = new ArrayList<TimeSpanType>();
        timeSpanList.add(timeSpan);
        
        rdf.setTimeSpanList(timeSpanList);
        
        Set<String> result = DereferenceUtils.extractValuesForDereferencing(rdf);
		
		Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
        
        Assert.assertTrue(result.contains("http://dummy1.dum"));
        Assert.assertTrue(result.contains("http://dummy2.dum"));
        Assert.assertTrue(result.contains("http://dummy3.dum"));
        Assert.assertTrue(result.contains("http://dummy4.dum"));
    }
    
    @Test
    public void testWebResourceListExtractedValues() throws IOException {
    	RDF rdf = new RDF();
        
        WebResourceType webResource = new WebResourceType();
        
        Created created = new Created();
        Resource resource1 = new Resource();
        resource1.setResource("http://dummy1.dum");
        created.setResource(resource1);
        ArrayList<Created> createdList = new ArrayList<Created>();
        createdList.add(created);
        webResource.setCreatedList(createdList);
        
        Extent extent = new Extent();      
        Resource resource2 = new Resource();
        resource2.setResource("http://dummy2.dum");
        extent.setResource(resource2);
        ArrayList<Extent> extentList = new ArrayList<Extent>();
        extentList.add(extent);
        webResource.setExtentList(extentList);
        
        Format format = new Format();      
        ResourceOrLiteralType.Resource resource3 = new ResourceOrLiteralType.Resource();
        resource3.setResource("http://dummy3.dum");
        format.setResource(resource3);
        ArrayList<Format> formatList = new ArrayList<Format>();
        formatList.add(format);
        webResource.setFormatList(formatList);

        HasPart hasPart = new HasPart();
        Resource resource4 = new Resource();
        resource4.setResource("http://dummy4.dum");
        hasPart.setResource(resource4);
        ArrayList<HasPart> hasPartList = new ArrayList<HasPart>();
        hasPartList.add(hasPart);
        webResource.setHasPartList(hasPartList);
        
        IsFormatOf isFormatOf = new IsFormatOf();
        Resource resource5 = new Resource();
        resource5.setResource("http://dummy5.dum");
        isFormatOf.setResource(resource5);
        ArrayList<IsFormatOf> isFormatOfList = new ArrayList<IsFormatOf>();
        isFormatOfList.add(isFormatOf);
        webResource.setIsFormatOfList(isFormatOfList);

        Issued issued = new Issued();
        Resource resource6 = new Resource();
        resource6.setResource("http://dummy6.dum");
        issued.setResource(resource6);
        ArrayList<Issued> issuedList = new ArrayList<Issued>();
        issuedList.add(issued);
        webResource.setIssuedList(issuedList);
        
        // Should be rejected
        BitRate bitRate = new BitRate();      
        bitRate.setDatatype("Data Type");
        webResource.setBitRate(bitRate);
        
        ArrayList<WebResourceType> webResourceList = new ArrayList<WebResourceType>();
        webResourceList.add(webResource);
        
        rdf.setWebResourceList(webResourceList);
        
        Set<String> result = DereferenceUtils.extractValuesForDereferencing(rdf);
		
		Assert.assertNotNull(result);
        Assert.assertEquals(6, result.size());
        
        Assert.assertTrue(result.contains("http://dummy1.dum"));
        Assert.assertTrue(result.contains("http://dummy2.dum"));
        Assert.assertTrue(result.contains("http://dummy3.dum"));
        Assert.assertTrue(result.contains("http://dummy4.dum"));
        Assert.assertTrue(result.contains("http://dummy5.dum"));
        Assert.assertTrue(result.contains("http://dummy6.dum")); 
    }
    
    @Test
    public void testProxyListExtractedValues() throws IOException {
    	RDF rdf = new RDF();
        
        ProxyType proxy = new ProxyType();
        
        HasMet hasMet = new HasMet();      
        hasMet.setResource("http://dummy1.dum");
        ArrayList<HasMet> hasMetList = new ArrayList<HasMet>();
        hasMetList.add(hasMet);
        proxy.setHasMetList(hasMetList);

        HasType hasType = new HasType();    
        ResourceOrLiteralType.Resource resource2 = new ResourceOrLiteralType.Resource();
        resource2.setResource("http://dummy2.dum");
        hasType.setResource(resource2);
        ArrayList<HasType> hasTypeList = new ArrayList<HasType>();
        hasTypeList.add(hasType);
        proxy.setHasTypeList(hasTypeList);

        Incorporates incorporates = new Incorporates();
        incorporates.setResource("http://dummy3.dum");
        ArrayList<Incorporates> incorporatesList = new ArrayList<Incorporates>();
        incorporatesList.add(incorporates);
        proxy.setIncorporateList(incorporatesList);
        
        IsDerivativeOf isDerivativeOf = new IsDerivativeOf();
        isDerivativeOf.setResource("http://dummy4.dum");
        ArrayList<IsDerivativeOf> isDerivativeOfList = new ArrayList<IsDerivativeOf>();
        isDerivativeOfList.add(isDerivativeOf);
        proxy.setIsDerivativeOfList(isDerivativeOfList);
              
        IsRelatedTo isRelatedTo = new IsRelatedTo();
        ResourceOrLiteralType.Resource resource5 = new ResourceOrLiteralType.Resource();
        resource5.setResource("http://dummy5.dum");
        isRelatedTo.setResource(resource5);
        ArrayList<IsRelatedTo> isRelatedToList = new ArrayList<IsRelatedTo>();
        isRelatedToList.add(isRelatedTo);
        proxy.setIsRelatedToList(isRelatedToList);
        
        IsSimilarTo isSimilarTo = new IsSimilarTo();
        isSimilarTo.setResource("http://dummy6.dum");
        ArrayList<IsSimilarTo> isSimilarToList = new ArrayList<IsSimilarTo>();
        isSimilarToList.add(isSimilarTo);
        proxy.setIsSimilarToList(isSimilarToList);

        IsSuccessorOf isSuccessorOf = new IsSuccessorOf();
        isSuccessorOf.setResource("http://dummy7.dum");
        ArrayList<IsSuccessorOf> isSuccessorOfList = new ArrayList<IsSuccessorOf>();
        isSuccessorOfList.add(isSuccessorOf);
        proxy.setIsSuccessorOfList(isSuccessorOfList);
        
        Realizes realizes = new Realizes();
        realizes.setResource("http://dummy8.dum");
        ArrayList<Realizes> realizesList = new ArrayList<Realizes>();
        realizesList.add(realizes);
        proxy.setRealizeList(realizesList);
        
        CurrentLocation currentLocation = new CurrentLocation();
        ResourceOrLiteralType.Resource resource9 = new ResourceOrLiteralType.Resource();
        resource9.setResource("http://dummy9.dum");
        currentLocation.setResource(resource9);
        proxy.setCurrentLocation(currentLocation);
        
        ProxyType.Choice choice1 = new ProxyType.Choice();
        Subject subject = new Subject();
        ResourceOrLiteralType.Resource resource10 = new ResourceOrLiteralType.Resource();
        resource10.setResource("http://dummy10.dum");
        subject.setResource(resource10);
        choice1.setSubject(subject);
        
        ProxyType.Choice choice2 = new ProxyType.Choice();
        Publisher publisher = new Publisher();
        ResourceOrLiteralType.Resource resource11 = new ResourceOrLiteralType.Resource();
        resource11.setResource("http://dummy11.dum");
        publisher.setResource(resource11);
        choice2.setPublisher(publisher);
        
        ProxyType.Choice choice3 = new ProxyType.Choice();
        References references = new References();
        ResourceOrLiteralType.Resource resource12 = new ResourceOrLiteralType.Resource();
        resource12.setResource("http://dummy12.dum");
        references.setResource(resource12);
        choice3.setReferences(references);
        
        ProxyType.Choice choice4 = new ProxyType.Choice();
        Relation relation = new Relation();
        ResourceOrLiteralType.Resource resource13 = new ResourceOrLiteralType.Resource();
        resource13.setResource("http://dummy13.dum");
        relation.setResource(resource13);
        choice4.setRelation(relation);
        
        ProxyType.Choice choice5 = new ProxyType.Choice();
        Replaces replaces = new Replaces();
        ResourceOrLiteralType.Resource resource14 = new ResourceOrLiteralType.Resource();
        resource14.setResource("http://dummy14.dum");
        replaces.setResource(resource14);
        choice5.setReplaces(replaces);
        
        ProxyType.Choice choice6 = new ProxyType.Choice();
        Contributor contributor = new Contributor();
        ResourceOrLiteralType.Resource resource15 = new ResourceOrLiteralType.Resource();
        resource15.setResource("http://dummy15.dum");
        contributor.setResource(resource15);
        choice6.setContributor(contributor);

        ProxyType.Choice choice7 = new ProxyType.Choice();
        Coverage coverage = new Coverage();
        ResourceOrLiteralType.Resource resource16 = new ResourceOrLiteralType.Resource();
        resource16.setResource("http://dummy16.dum");
        coverage.setResource(resource16);
        choice7.setCoverage(coverage);
        
        ProxyType.Choice choice8 = new ProxyType.Choice();
        Creator creator = new Creator();
        ResourceOrLiteralType.Resource resource17 = new ResourceOrLiteralType.Resource();
        resource17.setResource("http://dummy17.dum");
        creator.setResource(resource17);
        choice8.setCreator(creator);

        ProxyType.Choice choice9 = new ProxyType.Choice();
        Created created = new Created();
        ResourceOrLiteralType.Resource resource18 = new ResourceOrLiteralType.Resource();
        resource18.setResource("http://dummy18.dum");
        created.setResource(resource18);
        choice9.setCreated(created);

        ProxyType.Choice choice10 = new ProxyType.Choice();
        Date date = new Date();
        ResourceOrLiteralType.Resource resource19 = new ResourceOrLiteralType.Resource();
        resource19.setResource("http://dummy19.dum");
        date.setResource(resource19);
        choice10.setDate(date);
        
        ProxyType.Choice choice11 = new ProxyType.Choice();
        Extent extent = new Extent();
        ResourceOrLiteralType.Resource resource20 = new ResourceOrLiteralType.Resource();
        resource20.setResource("http://dummy20.dum");
        extent.setResource(resource20);
        choice11.setExtent(extent);

        ProxyType.Choice choice12 = new ProxyType.Choice();
        Format format = new Format();
        ResourceOrLiteralType.Resource resource21 = new ResourceOrLiteralType.Resource();
        resource21.setResource("http://dummy21.dum");
        format.setResource(resource21);
        choice12.setFormat(format);

        ProxyType.Choice choice13 = new ProxyType.Choice();
        HasFormat hasFormat = new HasFormat();
        ResourceOrLiteralType.Resource resource22 = new ResourceOrLiteralType.Resource();
        resource22.setResource("http://dummy22.dum");
        hasFormat.setResource(resource22);
        choice13.setHasFormat(hasFormat);

        ProxyType.Choice choice14 = new ProxyType.Choice();
        HasPart hasPart = new HasPart();
        ResourceOrLiteralType.Resource resource23 = new ResourceOrLiteralType.Resource();
        resource23.setResource("http://dummy23.dum");
        hasPart.setResource(resource23);
        choice14.setHasPart(hasPart);

        ProxyType.Choice choice15 = new ProxyType.Choice();
        HasVersion hasVersion = new HasVersion();
        ResourceOrLiteralType.Resource resource24 = new ResourceOrLiteralType.Resource();
        resource24.setResource("http://dummy24.dum");
        hasVersion.setResource(resource24);
        choice15.setHasVersion(hasVersion);

        ProxyType.Choice choice16 = new ProxyType.Choice();
        Source source = new Source();
        ResourceOrLiteralType.Resource resource25 = new ResourceOrLiteralType.Resource();
        resource25.setResource("http://dummy25.dum");
        source.setResource(resource25);
        choice16.setSource(source);

        ProxyType.Choice choice17 = new ProxyType.Choice();
        Spatial spatial = new Spatial();
        ResourceOrLiteralType.Resource resource26 = new ResourceOrLiteralType.Resource();
        resource26.setResource("http://dummy26.dum");
        spatial.setResource(resource26);
        choice17.setSpatial(spatial);
        
        ProxyType.Choice choice18 = new ProxyType.Choice();
        Temporal temporal = new Temporal();
        ResourceOrLiteralType.Resource resource27 = new ResourceOrLiteralType.Resource();
        resource27.setResource("http://dummy27.dum");
        temporal.setResource(resource27);
        choice18.setTemporal(temporal);

        ProxyType.Choice choice19 = new ProxyType.Choice();
        IsFormatOf isFormatOf = new IsFormatOf();
        ResourceOrLiteralType.Resource resource28 = new ResourceOrLiteralType.Resource();
        resource28.setResource("http://dummy28.dum");
        isFormatOf.setResource(resource28);
        choice19.setIsFormatOf(isFormatOf);

        ProxyType.Choice choice20 = new ProxyType.Choice();
        IsPartOf isPartOf = new IsPartOf();
        ResourceOrLiteralType.Resource resource29 = new ResourceOrLiteralType.Resource();
        resource29.setResource("http://dummy29.dum");
        isPartOf.setResource(resource29);
        choice20.setIsPartOf(isPartOf);
        
        ProxyType.Choice choice21 = new ProxyType.Choice();
        IsReferencedBy isReferencedBy = new IsReferencedBy();
        ResourceOrLiteralType.Resource resource30 = new ResourceOrLiteralType.Resource();
        resource30.setResource("http://dummy30.dum");
        isReferencedBy.setResource(resource30);
        choice21.setIsReferencedBy(isReferencedBy);

        ProxyType.Choice choice22 = new ProxyType.Choice();
        IsRequiredBy isRequiredBy = new IsRequiredBy();
        ResourceOrLiteralType.Resource resource31 = new ResourceOrLiteralType.Resource();
        resource31.setResource("http://dummy31.dum");
        isRequiredBy.setResource(resource31);
        choice22.setIsRequiredBy(isRequiredBy);

        ProxyType.Choice choice23 = new ProxyType.Choice();
        Issued issued = new Issued();
        ResourceOrLiteralType.Resource resource32 = new ResourceOrLiteralType.Resource();
        resource32.setResource("http://dummy32.dum");
        issued.setResource(resource32);
        choice23.setIssued(issued);

        ProxyType.Choice choice24 = new ProxyType.Choice();
        IsVersionOf isVersionOf = new IsVersionOf();
        ResourceOrLiteralType.Resource resource33 = new ResourceOrLiteralType.Resource();
        resource33.setResource("http://dummy33.dum");
        isVersionOf.setResource(resource33);
        choice24.setIsVersionOf(isVersionOf);

        ProxyType.Choice choice25 = new ProxyType.Choice();
        Medium medium = new Medium();
        ResourceOrLiteralType.Resource resource34 = new ResourceOrLiteralType.Resource();
        resource34.setResource("http://dummy34.dum");
        medium.setResource(resource34);
        choice25.setMedium(medium);
        
        ProxyType.Choice choice26 = new ProxyType.Choice();
        Type type = new Type();
        ResourceOrLiteralType.Resource resource35 = new ResourceOrLiteralType.Resource();
        resource35.setResource("http://dummy35.dum");
        type.setResource(resource35);
        choice26.setType(type);
        
        // Should be rejected
        ProxyType.Choice choice27 = new ProxyType.Choice();
        Alternative alternative = new Alternative();      
        alternative.setString("Alternative");
        choice27.setAlternative(alternative);

        ArrayList<ProxyType.Choice> choiceList = new ArrayList<ProxyType.Choice>();
        
        choiceList.add(choice1);
        choiceList.add(choice2);
        choiceList.add(choice3);
        choiceList.add(choice4);
        choiceList.add(choice5);
        choiceList.add(choice6);
        choiceList.add(choice7);
        choiceList.add(choice8);
        choiceList.add(choice9);
        choiceList.add(choice10);
        choiceList.add(choice11);
        choiceList.add(choice12);
        choiceList.add(choice13);
        choiceList.add(choice14);
        choiceList.add(choice15);
        choiceList.add(choice16);
        choiceList.add(choice17);
        choiceList.add(choice18);
        choiceList.add(choice19);
        choiceList.add(choice20);
        choiceList.add(choice21);
        choiceList.add(choice22);
        choiceList.add(choice23);
        choiceList.add(choice24);
        choiceList.add(choice25);
        choiceList.add(choice26);
        choiceList.add(choice27);
        
        proxy.setChoiceList(choiceList);
        
        ArrayList<ProxyType> proxyList = new ArrayList<ProxyType>();
        proxyList.add(proxy);
        
        rdf.setProxyList(proxyList);
        
        Set<String> result = DereferenceUtils.extractValuesForDereferencing(rdf);
		
		Assert.assertNotNull(result);
        Assert.assertEquals(35, result.size());
        
        Assert.assertTrue(result.contains("http://dummy1.dum"));
        Assert.assertTrue(result.contains("http://dummy2.dum"));
        Assert.assertTrue(result.contains("http://dummy3.dum"));
        Assert.assertTrue(result.contains("http://dummy4.dum"));
        Assert.assertTrue(result.contains("http://dummy5.dum"));
        Assert.assertTrue(result.contains("http://dummy6.dum"));

        Assert.assertTrue(result.contains("http://dummy7.dum"));
        Assert.assertTrue(result.contains("http://dummy8.dum"));
        Assert.assertTrue(result.contains("http://dummy9.dum"));
        Assert.assertTrue(result.contains("http://dummy10.dum"));
        Assert.assertTrue(result.contains("http://dummy11.dum"));
        Assert.assertTrue(result.contains("http://dummy12.dum"));
        
        Assert.assertTrue(result.contains("http://dummy13.dum"));
        Assert.assertTrue(result.contains("http://dummy14.dum"));
        Assert.assertTrue(result.contains("http://dummy15.dum"));
        Assert.assertTrue(result.contains("http://dummy16.dum"));
        Assert.assertTrue(result.contains("http://dummy17.dum"));
        Assert.assertTrue(result.contains("http://dummy18.dum"));
        
        Assert.assertTrue(result.contains("http://dummy19.dum"));
        Assert.assertTrue(result.contains("http://dummy20.dum"));
        Assert.assertTrue(result.contains("http://dummy21.dum"));
        Assert.assertTrue(result.contains("http://dummy22.dum"));
        Assert.assertTrue(result.contains("http://dummy23.dum"));
        Assert.assertTrue(result.contains("http://dummy24.dum")); 

        Assert.assertTrue(result.contains("http://dummy25.dum"));
        Assert.assertTrue(result.contains("http://dummy26.dum"));
        Assert.assertTrue(result.contains("http://dummy27.dum"));
        Assert.assertTrue(result.contains("http://dummy28.dum"));
        Assert.assertTrue(result.contains("http://dummy29.dum"));
        Assert.assertTrue(result.contains("http://dummy30.dum"));
        
        Assert.assertTrue(result.contains("http://dummy31.dum"));
        Assert.assertTrue(result.contains("http://dummy32.dum"));
        Assert.assertTrue(result.contains("http://dummy33.dum"));
        Assert.assertTrue(result.contains("http://dummy34.dum"));
        Assert.assertTrue(result.contains("http://dummy35.dum"));
    }
}
