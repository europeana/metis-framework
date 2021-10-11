package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.WebResource;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.Concept.Choice;
import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class for extracting items from different structures.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-09-13
 */
public final class ItemExtractorUtils {

  private ItemExtractorUtils() {
  }

  static void setAbout(EnrichmentBase source, AboutType destination) {
    destination.setAbout(source.getAbout());
  }

  static <S, T> List<T> extractItems(List<S> sourceList, Function<S, T> converter) {
    final List<T> result;
    if (sourceList == null) {
      result = new ArrayList<>();
    } else {
      result = sourceList.stream().filter(Objects::nonNull).map(converter)
          .collect(Collectors.toList());
    }
    return result;
  }

  static <T extends LiteralType> List<T> extractLabels(List<Label> sourceList,
      Supplier<T> newInstanceProvider) {
    return extractItems(sourceList, label -> extractLabel(label, newInstanceProvider));
  }

  static <T extends ResourceType> List<T> extractResources(List<? extends WebResource> sourceList,
          Supplier<T> newInstanceProvider) {
    return extractItems(sourceList,
            item -> extractAsResource(item, newInstanceProvider, WebResource::getResourceUri));
  }

  static <T extends ResourceOrLiteralType> List<T> extractLabelResources(
      List<LabelResource> sourceList, Supplier<T> newInstanceProvider) {
    return extractItems(sourceList,
        labelResource -> extractLabelResource(labelResource, newInstanceProvider));
  }

  static <T extends LiteralType> T extractFirstLabel(List<Label> sourceList,
      Supplier<T> newInstanceProvider) {
    final Label firstLabel;
    if (sourceList == null) {
      firstLabel = null;
    } else {
      firstLabel = sourceList.stream().filter(Objects::nonNull).findFirst().orElse(null);
    }
    return firstLabel == null ? null : extractLabel(firstLabel, newInstanceProvider);
  }

  static <T extends LiteralType> T extractLabel(Label label,
      Supplier<T> newInstanceProvider) {
    final T result = newInstanceProvider.get();
    if (label.getLang() != null) {
      final LiteralType.Lang lang = new LiteralType.Lang();
      lang.setLang(label.getLang());
      result.setLang(lang);
    }
    result.setString(label.getValue() == null ? "" : label.getValue());
    return result;
  }

  static <T extends ResourceOrLiteralType> T extractLabelResource(
      LabelResource labelResource, Supplier<T> newInstanceProvider) {
    final T result = newInstanceProvider.get();
    if (labelResource.getLang() != null) {
      final ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
      lang.setLang(labelResource.getLang());
      result.setLang(lang);
    }
    if (labelResource.getResource() != null) {
      ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
      resrc.setResource(labelResource.getResource());
      result.setResource(resrc);
    }
    result.setString(labelResource.getValue() == null ? "" : labelResource.getValue());
    return result;
  }

  static <T extends ResourceOrLiteralType> T extractPart(Part part,
      Supplier<T> newInstanceProvider) {
    final T result = newInstanceProvider.get();
    if (part.getResource() != null) {
      ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
      resrc.setResource(part.getResource());
      result.setResource(resrc);
    }
    result.setString("");
    return result;
  }

  static <S, T extends ResourceType> T extractAsResource(S input,
      Supplier<T> newInstanceProvider, Function<S, String> resourceProvider) {
    final T result = newInstanceProvider.get();
    final String inputString = resourceProvider.apply(input);
    result.setResource(inputString == null ? "" : inputString);
    return result;
  }

  static <T> void toChoices(List<T> inputList, BiConsumer<Choice, T> propertySetter,
      List<Choice> destination) {
    for (T input : inputList) {
      final Choice choice = new Choice();
      propertySetter.accept(choice, input);
      destination.add(choice);
    }
  }

}
