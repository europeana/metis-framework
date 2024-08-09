package eu.europeana.enrichment.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.AltLabel;
import eu.europeana.metis.schema.jibx.Concept.Choice;
import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.LiteralType.Lang;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ItemExtractorUtilsTest {

  @Test
  void testSetAbout() {

    EnrichmentBase enrichmentBase = new Agent();
    AboutType aboutType = new AboutType();

    enrichmentBase.setAbout("About");

    ItemExtractorUtils.setAbout(enrichmentBase, aboutType);

    assertEquals("About", aboutType.getAbout());
  }

  @Test
  void testExtractItems() {
    List<Integer> inputList = new ArrayList<>();
    Integer integer1 = 1;
    Integer integer2 = 2;
    Integer integer3 = 3;
    inputList.add(integer1);
    inputList.add(integer2);
    inputList.add(integer3);

    List<Double> output = ItemExtractorUtils.extractItems(inputList, Double::valueOf);

    for (Integer elem : inputList) {
      assertTrue(output.contains(Double.valueOf(elem)));
    }
  }

  @Test
  void testExtractItemsReturnEmpty() {
    List<String> output = ItemExtractorUtils.extractItems(null, String::valueOf);
    assertTrue(output.isEmpty());
  }

  @Test
  void testExtractLabels() {
    List<Label> labels = new ArrayList<>();
    Label label1 = new Label("lang1", "value1");
    Label label2 = new Label("lang2", "value2");
    Label label3 = new Label("lang3", "value3");
    labels.add(label1);
    labels.add(label2);
    labels.add(label3);

    List<LiteralType> output = ItemExtractorUtils.extractLabels(labels, LiteralType::new);

    for (Label label : labels) {
      List<LiteralType> result = output.stream().filter(x -> x.getString().equals(label.getValue())).toList();

      assertEquals(1, result.size());
      assertEquals(label.getLang(), result.getFirst().getLang().getLang());
    }
  }

  @Test
  void testExtractResources() {
    List<Resource> resources = new ArrayList<>();
    Resource resource1 = new Resource("resource1");
    Resource resource2 = new Resource("resource2");
    Resource resource3 = new Resource("resource3");
    resources.add(resource1);
    resources.add(resource2);
    resources.add(resource3);

    List<ResourceType> output = ItemExtractorUtils.extractResources(resources, ResourceType::new);

    for (Resource resource : resources) {
      List<ResourceType> result = output
          .stream()
          .filter(x -> x.getResource().equals(resource.getResource()))
          .toList();

      assertEquals(1, result.size());
    }
  }

  @Test
  void extractLabelResources() {
    List<LabelResource> labelResources = new ArrayList<>();
    LabelResource labelResource1 = new LabelResource("resource1");
    LabelResource labelResource2 = new LabelResource("resource2");
    LabelResource labelResource3 = new LabelResource("resource3");
    labelResources.add(labelResource1);
    labelResources.add(labelResource2);
    labelResources.add(labelResource3);

    List<ResourceOrLiteralType> output =
        ItemExtractorUtils.extractLabelResources(labelResources, ResourceOrLiteralType::new);

    for (LabelResource labelResource : labelResources) {
      List<ResourceOrLiteralType> result = output
          .stream()
          .filter(x -> x.getResource().getResource().equals(labelResource.getResource()))
          .toList();

      assertEquals(1, result.size());
    }
  }

  @Test
  void testExtractFirstLabel() {
    List<Label> labels = new ArrayList<>();
    Label label1 = new Label("lang1", "value1");
    Label label2 = new Label("lang2", "value2");
    Label label3 = new Label("lang3", "value3");
    labels.add(label1);
    labels.add(label2);
    labels.add(label3);

    LiteralType output =
        ItemExtractorUtils.extractFirstLabel(labels, LiteralType::new);

    assertNotNull(output);
    assertEquals("value1", output.getString());
    assertEquals("lang1", output.getLang().getLang());
  }

  @Test
  void testExtractFirstLabelReturnNextNonNull() {
    List<Label> labels = new ArrayList<>();
    Label label2 = new Label("lang2", "value2");
    Label label3 = new Label("lang3", "value3");
    labels.add(null);
    labels.add(label2);
    labels.add(label3);

    LiteralType output =
        ItemExtractorUtils.extractFirstLabel(labels, LiteralType::new);

    assertNotNull(output);
    assertEquals("value2", output.getString());
    assertEquals("lang2", output.getLang().getLang());
  }

  @Test
  void testExtractFirstLabelReturnNull() {
    LiteralType output = ItemExtractorUtils.extractFirstLabel(null, LiteralType::new);
    assertNull(output);
  }

  @Test
  void testExtractLabel() {
    Label label = new Label("lang1", "value1");
    LiteralType output = ItemExtractorUtils.extractLabel(label, LiteralType::new);

    assertNotNull(output);
    assertEquals("value1", output.getString());
    assertEquals("lang1", output.getLang().getLang());
  }

  @Test
  void testExtractLabelReturnEmpty() {
    Label label = new Label(null);
    LiteralType output = ItemExtractorUtils.extractLabel(label, LiteralType::new);

    assertNotNull(output);
    assertEquals("", output.getString());
    assertNull(output.getLang());
  }

  @Test
  void testExtractLabelToResourceOrLiteralEmpty() {
    Label label = new Label(null);
    LiteralType output = ItemExtractorUtils.extractLabel(label, LiteralType::new);

    assertNotNull(output);
    assertEquals("", output.getString());
    assertNull(output.getLang());
  }

  @Test
  void testExtractLabelResource() {
    LabelResource label = new LabelResource("lang1", "value1");
    label.setResource("resource1");
    ResourceOrLiteralType output =
        ItemExtractorUtils.extractLabelResource(label, ResourceOrLiteralType::new);

    assertNotNull(output);
    assertEquals("value1", output.getString());
    assertEquals("lang1", output.getLang().getLang());
    assertEquals("resource1", output.getResource().getResource());
  }

  @Test
  void testExtractLabelResourceWithoutResource() {
    LabelResource label = new LabelResource("lang1", "value1");
    ResourceOrLiteralType output =
        ItemExtractorUtils.extractLabelResource(label, ResourceOrLiteralType::new);

    assertNotNull(output);
    assertEquals("value1", output.getString());
    assertEquals("lang1", output.getLang().getLang());
    assertNull(output.getResource());
  }

  @Test
  void testExtractLabelResourceWithoutLanguage() {
    LabelResource label = new LabelResource(null, "value1");
    label.setResource("resource1");
    ResourceOrLiteralType output =
        ItemExtractorUtils.extractLabelResource(label, ResourceOrLiteralType::new);

    assertNotNull(output);
    assertEquals("value1", output.getString());
    assertNull(output.getLang());
    assertEquals("resource1", output.getResource().getResource());
  }

  @Test
  void testExtractLabelResourceWithoutValue() {
    LabelResource label = new LabelResource("lang1", null);
    label.setResource("resource1");
    ResourceOrLiteralType output =
        ItemExtractorUtils.extractLabelResource(label, ResourceOrLiteralType::new);

    assertNotNull(output);
    assertEquals("", output.getString());
    assertEquals("lang1", output.getLang().getLang());
    assertEquals("resource1", output.getResource().getResource());
  }

  @Test
  void testExtractLabelResourceReturnNull() {
    LabelResource label = new LabelResource(null);
    ResourceOrLiteralType output =
        ItemExtractorUtils.extractLabelResource(label, ResourceOrLiteralType::new);

    assertNotNull(output);
    assertEquals("", output.getString());
    assertNull(output.getLang());
    assertNull(output.getResource());
  }

  @Test
  void testExtractPart() {
    Part part = new Part("resource");
    ResourceOrLiteralType output = ItemExtractorUtils.extractPart(part, ResourceOrLiteralType::new);

    assertNotNull(output);
    assertEquals("", output.getString());
    assertEquals("resource", output.getResource().getResource());
  }

  @Test
  void testExtractPartNullResource() {
    Part part = new Part(null);
    ResourceOrLiteralType output = ItemExtractorUtils.extractPart(part, ResourceOrLiteralType::new);

    assertNotNull(output);
    assertEquals("", output.getString());
    assertNull(output.getResource());
  }

  @Test
  void testExtractAsResource() {
    String string = "resource";
    ResourceType output =
        ItemExtractorUtils.extractAsResource(string, ResourceType::new, String::toUpperCase);

    assertNotNull(output);
    assertEquals(string.toUpperCase(), output.getResource());
  }

  @Test
  void testExtractAsResourceNullValue() {
    ResourceType output = ItemExtractorUtils.extractAsResource(null, ResourceType::new, x -> null);

    assertNotNull(output);
    assertEquals("", output.getResource());
  }

  @Test
  void testToChoices() {
    List<Choice> choices = new ArrayList<>();

    List<AltLabel> altLabels = new ArrayList<>();
    AltLabel label1 = new AltLabel();
    AltLabel label2 = new AltLabel();
    AltLabel label3 = new AltLabel();

    Lang lang1 = new Lang();
    lang1.setLang("lang1");
    Lang lang2 = new Lang();
    lang2.setLang("lang2");
    Lang lang3 = new Lang();
    lang3.setLang("lang3");

    label1.setString("value1");
    label1.setLang(lang1);
    label2.setString("value2");
    label1.setLang(lang2);
    label3.setString("value3");
    label1.setLang(lang3);

    altLabels.add(label1);
    altLabels.add(label3);
    altLabels.add(label3);

    ItemExtractorUtils.toChoices(altLabels, Choice::setAltLabel, choices);

    assertTrue(choices.size() > 0);

    for (AltLabel label : altLabels) {
      List<Choice> result = choices.stream().filter(x -> x.getAltLabel().equals(label)).collect(
          Collectors.toList());

      assertTrue(result.size() > 0);
    }
  }
}
