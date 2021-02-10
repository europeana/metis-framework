package eu.europeana.metis.harvesting.http;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

enum CompressedFileExtension {

  ZIP(".zip"),
  GZIP(".gz"),
  TAR_GZ(".tar.gz"),
  TGZIP(".tgz");

  private final String extension;

  CompressedFileExtension(String extension) {
    this.extension = extension;
  }

  public final String getExtension() {
    return extension;
  }

  public static CompressedFileExtension forPath(Path file) {
    return Arrays.stream(values())
            .sorted(Comparator.comparing(candidate -> -candidate.getExtension().length()))
            .filter(extension -> extension.hasExtension(file.toString()))
            .findFirst()
            .orElse(null);
  }

  public boolean hasExtension(String fileName) {
    return fileName.endsWith(getExtension());
  }

  public static Path removeExtension(Path file) {
    final CompressedFileExtension extension = forPath(file);
    if (extension == null) {
      throw new IllegalArgumentException("File " + file + " is not a recognised compressed file.");
    }
    final String fileName = file.getFileName().toString();
    final String newFileName = fileName
            .substring(0, fileName.length() - extension.getExtension().length());
    return Optional.ofNullable(file.getParent()).map(parent -> parent.resolve(newFileName))
            .orElse(Path.of(newFileName));
  }

  public static boolean hasCompressedFileExtension(Path file) {
    return hasCompressedFileExtension(file.getFileName().toString());
  }

  public static boolean hasCompressedFileExtension(String fileName) {
    return Stream.of(values()).anyMatch(extension -> extension.hasExtension(fileName));
  }
}
