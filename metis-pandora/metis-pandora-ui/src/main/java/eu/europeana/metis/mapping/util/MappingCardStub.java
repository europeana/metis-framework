package eu.europeana.metis.mapping.util;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.organisms.pandora.Mapping_card;
import eu.europeana.metis.mapping.statistics.Statistics;
import eu.europeana.metis.mapping.statistics.StatisticsValue;

public class MappingCardStub {

	public static Mapping_card buildMappingCardModel() {
		Element element = new Element();
		element.setPrefix("@rdf");
		element.setName("about");
		
		Statistics statistics = new Statistics();
		List<StatisticsValue> values = new ArrayList<>();
		
		StatisticsValue value0 = new StatisticsValue();
		value0.setId(new ObjectId());
		value0.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value0.setOccurence(126);
		values.add(value0);
		
		StatisticsValue value1 = new StatisticsValue();
		value1.setId(new ObjectId());
		value1.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value1.setOccurence(12);
		values.add(value1);
		
		StatisticsValue value2 = new StatisticsValue();
		value2.setId(new ObjectId());
		value2.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value2.setOccurence(3450);
		values.add(value2);
		
		StatisticsValue value3 = new StatisticsValue();
		value3.setId(new ObjectId());
		value3.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value3.setOccurence(7);
		values.add(value3);
		
		StatisticsValue value4 = new StatisticsValue();
		value4.setId(new ObjectId());
		value4.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value4.setOccurence(24);
		values.add(value4);
		
		StatisticsValue value5 = new StatisticsValue();
		value5.setId(new ObjectId());
		value5.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value5.setOccurence(126);
		values.add(value5);
		
		StatisticsValue value6 = new StatisticsValue();
		value6.setId(new ObjectId());
		value6.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value6.setOccurence(625);
		values.add(value6);
		
		StatisticsValue value7 = new StatisticsValue();
		value7.setId(new ObjectId());
		value7.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value7.setOccurence(38);
		values.add(value7);
		
		StatisticsValue value8 = new StatisticsValue();
		value8.setId(new ObjectId());
		value8.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value8.setOccurence(29);
		values.add(value8);
		
		StatisticsValue value9 = new StatisticsValue();
		value9.setId(new ObjectId());
		value9.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value9.setOccurence(2);
		values.add(value9);
		
		StatisticsValue value10 = new StatisticsValue();
		value10.setId(new ObjectId());
		value10.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value10.setOccurence(156);
		values.add(value10);
		
		StatisticsValue value11 = new StatisticsValue();
		value11.setId(new ObjectId());
		value11.setValue("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF 3319/II/28");
		value11.setOccurence(10567);
		values.add(value11);
		
		statistics.setValues(values);
		element.setStatistics(statistics);
		//add one child element to the root element;
//		element.setElements(Arrays.asList(element));
		return new Mapping_card(element, 0, 10);
	}
}
