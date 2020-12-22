package eu.europeana.enrichment.rest.client.enrichment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.api.internal.FieldType;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.enrichment.utils.EnrichmentUtils;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.jibx.Contributor;
import eu.europeana.metis.schema.jibx.Coverage;
import eu.europeana.metis.schema.jibx.Created;
import eu.europeana.metis.schema.jibx.Creator;
import eu.europeana.metis.schema.jibx.Date;
import eu.europeana.metis.schema.jibx.EuropeanaAggregationType;
import eu.europeana.metis.schema.jibx.EuropeanaProxy;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.Extent;
import eu.europeana.metis.schema.jibx.Issued;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Lang;
import eu.europeana.metis.schema.jibx.Spatial;
import eu.europeana.metis.schema.jibx.Subject;
import eu.europeana.metis.schema.jibx.Temporal;
import eu.europeana.metis.schema.jibx.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

public class MetisRecordParserTest {

  @Test
  public void testExtractedFieldValuesForEnrichment() {
    RDF rdf = new RDF();
    ProxyType proxy = new ProxyType();
    ArrayList<Choice> choiceList = new ArrayList<>();

    Choice choice1 = new Choice();
    Creator creator = new Creator();
    creator.setString("Creator");
    Lang language1 = new Lang();
    language1.setLang("English");
    creator.setLang(language1);
    choice1.setCreator(creator);
    choiceList.add(choice1);

    Choice choice2 = new Choice();
    Contributor contributor = new Contributor();
    contributor.setString("Contributor");
    Lang language2 = new Lang();
    language2.setLang("Dutch");
    contributor.setLang(language2);
    choice2.setContributor(contributor);
    choiceList.add(choice2);

    Choice choice3 = new Choice();
    Date date = new Date();
    date.setString("Date");
    Lang language3 = new Lang();
    language3.setLang("German");
    date.setLang(language3);
    choice3.setDate(date);
    choiceList.add(choice3);

    Choice choice4 = new Choice();
    Issued issued = new Issued();
    issued.setString("Issued");
    Lang language4 = new Lang();
    language4.setLang("French");
    issued.setLang(language4);
    choice4.setIssued(issued);
    choiceList.add(choice4);

    Choice choice5 = new Choice();
    Created created = new Created();
    created.setString("Created");
    Lang language5 = new Lang();
    language5.setLang("Italian");
    created.setLang(language5);
    choice5.setCreated(created);
    choiceList.add(choice5);

    Choice choice6 = new Choice();
    Coverage coverage = new Coverage();
    coverage.setString("Coverage");
    Lang language6 = new Lang();
    language6.setLang("Spanish");
    coverage.setLang(language6);
    choice6.setCoverage(coverage);
    choiceList.add(choice6);

    Choice choice7 = new Choice();
    Temporal temporal = new Temporal();
    temporal.setString("Temporal");
    Lang language7 = new Lang();
    language7.setLang("Polish");
    temporal.setLang(language7);
    choice7.setTemporal(temporal);
    choiceList.add(choice7);

    Choice choice8 = new Choice();
    Type type = new Type();
    type.setString("Type");
    Lang language8 = new Lang();
    language8.setLang("Romanian");
    type.setLang(language8);
    choice8.setType(type);
    choiceList.add(choice8);

    Choice choice9 = new Choice();
    Spatial spatial = new Spatial();
    spatial.setString("Spatial");
    Lang language9 = new Lang();
    language9.setLang("Greek");
    spatial.setLang(language9);
    choice9.setSpatial(spatial);
    choiceList.add(choice9);

    Choice choice10 = new Choice();
    Subject subject = new Subject();
    subject.setString("Subject");
    Lang language10 = new Lang();
    language10.setLang("Bulgarian");
    subject.setLang(language10);
    choice10.setSubject(subject);
    choiceList.add(choice10);

    // Should be rejected
    Choice choice11 = new Choice();
    Extent extent = new Extent();
    extent.setString("Extent");
    Lang language11 = new Lang();
    language11.setLang("Ukrainian");
    extent.setLang(language11);
    choice11.setExtent(extent);
    choiceList.add(choice11);

    proxy.setChoiceList(choiceList);

    ArrayList<ProxyType> proxyList = new ArrayList<>();
    proxyList.add(proxy);

    // Should be rejected
    ProxyType proxyEuropeana = new ProxyType();
    EuropeanaProxy europeanaProxy = new EuropeanaProxy();
    europeanaProxy.setEuropeanaProxy(true);
    proxyEuropeana.setEuropeanaProxy(europeanaProxy);
    proxyList.add(proxyEuropeana);

    rdf.setProxyList(proxyList);

    Set<SearchTermContext> result = new MetisRecordParser().parseSearchTerms(rdf);

    assertNotNull(result);
    assertEquals(10, result.size());

    ArrayList<String> resultProcessed = new ArrayList<>();
    for (SearchTermContext searchValue : result) {
      resultProcessed.add(searchValue.getTextValue() + "|" + searchValue.getLanguage());
    }

    assertTrue(resultProcessed.contains("Creator|English"));
    assertTrue(resultProcessed.contains("Contributor|Dutch"));
    assertTrue(resultProcessed.contains("Date|German"));
    assertTrue(resultProcessed.contains("Issued|French"));
    assertTrue(resultProcessed.contains("Created|Italian"));

    assertTrue(resultProcessed.contains("Coverage|Spanish"));
    assertTrue(resultProcessed.contains("Temporal|Polish"));
    assertTrue(resultProcessed.contains("Type|Romanian"));
    assertTrue(resultProcessed.contains("Spatial|Greek"));
    assertTrue(resultProcessed.contains("Subject|Bulgarian"));
  }

  @Test
  public void testSetAdditionalData() throws Exception {
    String xml = IOUtils
        .toString(getClass().getClassLoader().getResourceAsStream("sample_completeness.rdf"), "UTF-8");
    RDF rdf = RdfConversionUtils.convertStringToRdf(xml);
    EnrichmentUtils.setAdditionalData(rdf);
    EuropeanaAggregationType europeanaAggregationType = rdf.getEuropeanaAggregationList().stream()
        .findAny().orElse(null);
    assertEquals("6", europeanaAggregationType.getCompleteness().getString());
  }
}
