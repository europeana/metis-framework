package eu.europeana.enrichment.service;

public class EntityGenerator {

	public static void main(String[] args) {
		Enricher enricher = new Enricher("/home/ymamakis/git/tools/europeana-enrichment-framework/enrichment/enrichment-framework-service/converters/vocabularies");
		try {
			enricher.init("Europeana", "localhost","27017");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
