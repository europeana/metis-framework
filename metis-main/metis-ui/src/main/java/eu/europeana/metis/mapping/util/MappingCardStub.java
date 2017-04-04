package eu.europeana.metis.mapping.util;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.MappingType;
import eu.europeana.metis.mapping.model.SimpleMapping;
import eu.europeana.metis.mapping.organisms.pandora.Mapping_card;
import eu.europeana.metis.mapping.statistics.Statistics;
import eu.europeana.metis.mapping.statistics.StatisticsValue;

public class MappingCardStub {
	
	public static Mapping_card buildMappingCardModel() {
		String xpath = "edm:ProvidedCHO";
		
		//build element
		Element element = new Element();
		element.setPrefix("edm");
		element.setName("ProvidedCHO");
		ObjectId id = new ObjectId();
		element.setId(id);
		List<SimpleMapping> simpleMappings = new ArrayList<SimpleMapping>();
		SimpleMapping simpleMapping = new SimpleMapping();
		simpleMapping.setId(id);
		simpleMapping.setType(MappingType.XPATH);
		simpleMapping.setSourceField(xpath);
		element.setMappings(simpleMappings);
		
		//build statistics
		Statistics statistics = new Statistics();
		statistics.setXpath(xpath);
		List<StatisticsValue> values = new ArrayList<>();
		StatisticsValue value0 = new StatisticsValue();
		value0.setId(id);
		value0.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value0.setOccurence(126);
		values.add(value0);		
		StatisticsValue value1 = new StatisticsValue();
		value1.setId(id);
		value1.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value1.setOccurence(12);
		values.add(value1);
		StatisticsValue value2 = new StatisticsValue();
		value2.setId(id);
		value2.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value2.setOccurence(3450);
		values.add(value2);
		StatisticsValue value3 = new StatisticsValue();
		value3.setId(id);
		value3.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value3.setOccurence(7);
		values.add(value3);
		StatisticsValue value4 = new StatisticsValue();
		value4.setId(id);
		value4.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value4.setOccurence(24);
		values.add(value4);
		StatisticsValue value5 = new StatisticsValue();
		value5.setId(id);
		value5.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value5.setOccurence(126);
		values.add(value5);
		StatisticsValue value6 = new StatisticsValue();
		value6.setId(id);
		value6.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value6.setOccurence(625);
		values.add(value6);
		StatisticsValue value7 = new StatisticsValue();
		value7.setId(id);
		value7.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value7.setOccurence(38);
		values.add(value7);
		StatisticsValue value8 = new StatisticsValue();
		value8.setId(id);
		value8.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value8.setOccurence(29);
		values.add(value8);
		StatisticsValue value9 = new StatisticsValue();
		value9.setId(id);
		value9.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value9.setOccurence(2);
		values.add(value9);
		StatisticsValue value10 = new StatisticsValue();
		value10.setId(id);
		value10.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value10.setOccurence(156);
		values.add(value10);
		StatisticsValue value11 = new StatisticsValue();
		value11.setId(id);
		value11.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value11.setOccurence(10567);
		values.add(value11);
		statistics.setValues(values);
		
		//build a child element for the root element;
		Element child = new Element();
		child.setPrefix("@rdf");
		child.setName("about");
		ObjectId childId = new ObjectId();
		child.setId(childId);
		
		List<SimpleMapping> simpleMappingsChild = new ArrayList<SimpleMapping>();
		SimpleMapping simpleMappingChild = new SimpleMapping();
		simpleMappingChild.setId(id);
		simpleMappingChild.setType(MappingType.XPATH);
		simpleMappingChild.setSourceField("@rdf:about");
		simpleMappingsChild.add(simpleMappingChild);
		child.setMappings(simpleMappingsChild);

		Statistics childStatistics = new Statistics();
		childStatistics.setXpath("rdf:about");
		childStatistics.setValues(values);
		
		List<Element> childElements = new ArrayList<>();
		childElements.add(child);
		element.setElements(childElements);

		Mapping_card childCard = new Mapping_card(child, childStatistics, 0, 10, null);
		Mapping_card mapping_card = new Mapping_card(element, statistics,  0, 10, null);
		return mapping_card;
	}
	
	
}
