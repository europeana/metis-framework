package eu.europeana.enrichment.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

public class ItemExtractorUtilsTest {


  @Test
  void testSetAbout(){

    EnrichmentBase enrichmentBase = new Agent();
    AboutType aboutType = new AboutType();

    enrichmentBase.setAbout("About");

    ItemExtractorUtils.setAbout(enrichmentBase, aboutType);

    assertEquals("About", aboutType.getAbout());

  }

  @Test
  void testExtractItems(){
    List<Integer> inputList = new ArrayList<>();
    Integer integer1  = 1;
    Integer integer2  = 2;
    Integer integer3  = 3;
    inputList.add(integer1);
    inputList.add(integer2);
    inputList.add(integer3);

    List<Double> output = ItemExtractorUtils.extractItems(inputList, Double::valueOf);

    for(Integer elem: inputList){
      assertTrue(output.contains(Double.valueOf(elem)));
    }

  }

  @Test
  void testExtractLabels(){

    List<Label> labels = new ArrayList<>();
    Label label1 = new Label("lang1", "value1");
    Label label2 = new Label("lang2", "value2");
    Label label3 = new Label("lang3", "value3");
    labels.add(label1);
    labels.add(label2);
    labels.add(label3);

    List<LiteralType> output = ItemExtractorUtils.extractLabels(labels, LiteralType::new);

    for(Label label: labels){
      List<LiteralType> result = output.stream().filter(x -> x.getString().equals(label.getValue())).collect(
          Collectors.toList());

      assertEquals(1, result.size());
      assertEquals(label.getLang(), result.get(0).getLang().getLang());
    }

  }

  @Test
  void testExtractLabelsToResourceOrLiteralList(){

    List<Label> labels = new ArrayList<>();
    Label label1 = new Label("lang1", "value1");
    Label label2 = new Label("lang2", "value2");
    Label label3 = new Label("lang3", "value3");
    labels.add(label1);
    labels.add(label2);
    labels.add(label3);

    List<ResourceOrLiteralType> output =
        ItemExtractorUtils.extractLabelsToResourceOrLiteralList(labels, ResourceOrLiteralType::new);

    for(Label label: labels){
      List<ResourceOrLiteralType> result = output.stream().filter(x -> x.getString().equals(label.getValue())).collect(
          Collectors.toList());

      assertEquals(1, result.size());
      assertEquals(label.getLang(), result.get(0).getLang().getLang());
      assertNull(result.get(0).getResource());
    }

  }

  @Test
  void testExtractParts(){

    List<Part> parts = new ArrayList<>();
    Part part1 = new Part("resource1");
    Part part2 = new Part("resource2");
    Part part3 = new Part("resource3");
    parts.add(part1);
    parts.add(part2);
    parts.add(part3);

    List<ResourceOrLiteralType> output =
        ItemExtractorUtils.extractParts(parts, ResourceOrLiteralType::new);

    for(Part part: parts){
      List<ResourceOrLiteralType> result = output
          .stream()
          .filter(x -> x.getResource().getResource().equals(part.getResource()))
          .collect(Collectors.toList());

      assertEquals(1, result.size());
      assertNull(result.get(0).getLang());
      assertTrue(StringUtils.isBlank(result.get(0).getString()));
    }

  }

  @Test
  void testExtractAsResources(){

  }

  @Test
  void testExtractResources(){

  }

  @Test
  void extractLabelResources(){

  }

}
