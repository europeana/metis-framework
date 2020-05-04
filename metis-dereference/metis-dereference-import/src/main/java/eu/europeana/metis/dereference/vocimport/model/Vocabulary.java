package eu.europeana.metis.dereference.vocimport.model;

import eu.europeana.metis.dereference.vocimport.model.VocabularyMetadata.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is an immutable class that represents one vocabulary.
 */
public class Vocabulary {

  private final String name;
  private final Type type;
  private final List<String> paths;
  private final int parentIterations;
  private final String suffix;
  private final List<String> examples;
  private final List<String> counterExamples;
  private final String transformation;

  private Vocabulary(Builder builder) {
    this.name = builder.name;
    this.type = builder.type;
    this.paths = builder.paths;
    this.parentIterations = builder.parentIterations;
    this.suffix = builder.suffix;
    this.examples = builder.examples;
    this.counterExamples = builder.counterExamples;
    this.transformation = builder.transformation;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
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

  public static Builder builder() {
    return new Builder();
  }

  /**
   * This class is a builder for the vocabulary.
   */
  public static class Builder {

    private String name;
    private Type type;
    private List<String> paths;
    private int parentIterations;
    private String suffix;
    private List<String> examples;
    private List<String> counterExamples;
    private String transformation;

    private Builder() {
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setType(Type type) {
      this.type = type;
      return this;
    }

    public Builder setPaths(List<String> paths) {
      this.paths = new ArrayList<>(paths);
      return this;
    }

    public Builder setParentIterations(int parentIterations) {
      this.parentIterations = parentIterations;
      return this;
    }

    public Builder setSuffix(String suffix) {
      this.suffix = suffix;
      return this;
    }

    public Builder setExamples(List<String> examples) {
      this.examples = new ArrayList<>(examples);
      return this;
    }

    public Builder setCounterExamples(List<String> counterExamples) {
      this.counterExamples = new ArrayList<>(counterExamples);
      return this;
    }

    public Builder setTransformation(String transformation) {
      this.transformation = transformation;
      return this;
    }

    public Vocabulary build() {
      return new Vocabulary(this);
    }
  }
}
