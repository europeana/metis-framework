package eu.europeana.enrichment.builder;

import eu.europeana.enrichment.service.Enricher;

public class VocBuilder {

	public static void main (String[] args){
		Enricher enricher = new Enricher();
		try {
			enricher.init("");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
