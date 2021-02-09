package eu.europeana.metis.harvesting.http;

import java.util.Arrays;

enum CompressedFileExtension {

  ZIP("zip"),
  GZIP("gz"),
  TGZIP("tgz");

  private final String extension;

  CompressedFileExtension(String extension) {
    this.extension = extension;
  }

  public final String getExtension() {
    return extension;
  }

  public static CompressedFileExtension forExtension(String fileExtension) {
    return Arrays.stream(values())
            .filter(extension -> extension.getExtension().equals(fileExtension)).findAny()
            .orElse(null);
  }

  public static boolean contains(String fileExtension) {
    return forExtension(fileExtension) != null;
  }

  public static String[] getExtensionValues() {
    return Arrays.stream(values())
            .map(CompressedFileExtension::getExtension)
            .toArray(String[]::new);
  }
}
