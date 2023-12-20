package eu.europeana.metis.dereference.vocimport.model;

import eu.europeana.metis.dereference.vocimport.model.VocabularyMetadata.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * This is an immutable class that represents one vocabulary.
 */
public class Vocabulary {

  private final String name;
  private final Set<Type> types;
  private final List<String> paths;
  private final int parentIterations;
  private final String suffix;
  private final List<String> examples;
  private final List<String> counterExamples;
  private final String transformation;
  private final String readableMetadataLocation;
  private final String readableMappingLocation;

  private Vocabulary(Builder builder) {
    this.name = builder.name;
    this.types = Optional.ofNullable(builder.types).orElseGet(Collections::emptySet);
    this.paths = Optional.ofNullable(builder.paths).orElseGet(Collections::emptyList);
    this.parentIterations = builder.parentIterations;
    this.suffix = builder.suffix;
    this.examples = Optional.ofNullable(builder.examples).orElseGet(Collections::emptyList);
    this.counterExamples = Optional.ofNullable(builder.counterExamples)
            .orElseGet(Collections::emptyList);
    this.transformation = builder.transformation;
    this.readableMetadataLocation = builder.readableMetadataLocation;
    this.readableMappingLocation = builder.readableMappingLocation;
  }

  public String getName() {
    return name;
  }

  public Set<Type> getTypes() {
    return Collections.unmodifiableSet(types);
  }

  public List<String> getPaths() {
    return Collections.unmodifiableList(paths);
  }

  public int getParentIterations() {
    return parentIterations;
  }

  public String getSuffix() {
    return suffix;
  }

  public List<String> getExamples() {
    return Collections.unmodifiableList(examples);
  }

  public List<String> getCounterExamples() {
    return Collections.unmodifiableList(counterExamples);
  }

  public String getTransformation() {
    return transformation;
  }

  public String getReadableMetadataLocation() {
    return readableMetadataLocation;
  }

  public String getReadableMappingLocation() {
    return readableMappingLocation;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * This class is a builder for the vocabulary.
   */
  public static class Builder {

    protected String name;
    protected Set<Type> types;
    protected List<String> paths;
    protected int parentIterations;
    protected String suffix;
    protected List<String> examples;
    protected List<String> counterExamples;
    protected String transformation;
    protected String readableMetadataLocation;
    protected String readableMappingLocation;

    private Builder() {
    }

    public Builder setName(String name) {
      this.name = normalizeString(name);
      return this;
    }

    public Builder setTypes(Collection<Type> types) {
      this.types = EnumSet.noneOf(Type.class);
      this.types.addAll(types);
      return this;
    }

    public Builder setPaths(List<String> paths) {
      this.paths = normalizeStringList(paths);
      return this;
    }

    public Builder setParentIterations(Integer parentIterations) {
      this.parentIterations = Objects.requireNonNullElse(parentIterations, 0);
      return this;
    }

    public Builder setSuffix(String suffix) {
      this.suffix = normalizeString(suffix);
      return this;
    }

    public Builder setExamples(List<String> examples) {
      this.examples = normalizeStringList(examples);
      return this;
    }

    public Builder setCounterExamples(List<String> counterExamples) {
      this.counterExamples = normalizeStringList(counterExamples);
      return this;
    }

    public Builder setTransformation(String transformation) {
      this.transformation = normalizeString(transformation);
      return this;
    }

    public Builder setReadableMetadataLocation(String readableMetadataLocation) {
      this.readableMetadataLocation = readableMetadataLocation;
      return this;
    }

    public Builder setReadableMappingLocation(String readableMappingLocation) {
      this.readableMappingLocation = readableMappingLocation;
      return this;
    }

    private static String normalizeString(String input) {
      return Optional.ofNullable(input).filter(StringUtils::isNotBlank).map(String::trim)
              .orElse(null);
    }

    private static List<String> normalizeStringList(List<String> input) {
      return input.stream().filter(StringUtils::isNotBlank).map(String::trim)
                  .toList();
    }

    public Vocabulary build() {
      return new Vocabulary(this);
    }
  }
}
