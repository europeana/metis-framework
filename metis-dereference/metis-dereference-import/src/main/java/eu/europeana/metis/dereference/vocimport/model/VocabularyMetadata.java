package eu.europeana.metis.dereference.vocimport.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This class represents the metadata of a vocabulary. It is meant to reflect the structure of the
 * source of the data and changes should therefore not be made to this class without also changing
 * the format of the source.
 */
public class VocabularyMetadata {

  public enum Type {AGENT, CONCEPT, PLACE, TIMESTAMP}

  private String name;
  private Type type;
  private List<String> paths;
  private Integer parentIterations;
  private String suffix;
  private List<String> examples;
  private List<String> counterExamples;

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public List<String> getPaths() {
    return Optional.ofNullable(paths).map(Collections::unmodifiableList)
            .orElseGet(Collections::emptyList);
  }

  public Integer getParentIterations() {
    return parentIterations;
  }

  public String getSuffix() {
    return suffix;
  }

  public List<String> getExamples() {
    return Optional.ofNullable(examples).map(Collections::unmodifiableList)
            .orElseGet(Collections::emptyList);
  }

  public List<String> getCounterExamples() {
    return Optional.ofNullable(counterExamples).map(Collections::unmodifiableList)
            .orElseGet(Collections::emptyList);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public void setPaths(List<String> paths) {
    this.paths = new ArrayList<>(paths);
  }

  public void setParentIterations(Integer parentIterations) {
    this.parentIterations = parentIterations;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public void setExamples(List<String> examples) {
    this.examples = new ArrayList<>(examples);
  }

  public void setCounterExamples(List<String> counterExamples) {
    this.counterExamples = new ArrayList<>(counterExamples);
  }
}
