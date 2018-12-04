package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaprocessing.exception.MediaException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.ResourceProcessingResult;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaservice.EdmObject;
import eu.europeana.metis.mediaservice.MediaProcessor;
import eu.europeana.metis.mediaprocessing.UrlType;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

@Deprecated
public class TemporaryMediaService extends TemporaryMediaHandler implements Closeable {

  private final MediaProcessor mediaProcessor;

  public TemporaryMediaService() throws MediaProcessorException {
    try {
      mediaProcessor = new MediaProcessor();
    } catch (MediaException e) {
      throw new MediaProcessorException(e);
    }
  }

  // This method is probably thread-safe.
  // The files are done sequentially: the next one begins only if the previous one is completed.
  public <I extends DownloadedResource> Pair<RDF, List<Thumbnail>> performMediaProcessing(
      RDF incomingRdf, List<I> sources, MediaProcessingListener<I> listener) {
    final List<ResourceProcessingResult> processedResources = performResourceProcessing(incomingRdf,
        sources, listener);
    return mergeProcessedResources(incomingRdf, processedResources);
  }

  // This method is probably thread-safe.
  // The files are done sequentially: the next one begins only if the previous one is completed.
  public <I extends DownloadedResource> List<ResourceProcessingResult> performResourceProcessing(
      RDF incomingRdf, List<I> sources, MediaProcessingListener<I> listener) {
    final List<ResourceProcessingResult> result = new ArrayList<>();
    for (I source : sources) {
      try {
        listener.beforeStartingFile(source);
        final File file = source.getContentPath() == null ? null : source.getContentPath().toFile();
        final ResourceProcessingResult resourceResult = mediaProcessor
            .processResource(source.getResourceUrl(), source.getUrlTypes(), source.getMimeType(),
                file);
        if (resourceResult != null) {
          result.add(resourceResult);
        }
      } catch (Exception e) {
        final boolean stopProcessing;
        if (e instanceof MediaException) {
          stopProcessing = listener.handleMediaException(source, (MediaException) e);
        } else {
          stopProcessing = listener.handleOtherException(source, e);
        }
        if (stopProcessing) {
          break;
        }
      } finally {
        listener.afterCompletingFile(source);
      }
    }
    return result;
  }

  // This method is probably thread-safe.
  public Pair<RDF, List<Thumbnail>> mergeProcessedResources(RDF incomingRdf,
      List<ResourceProcessingResult> processedResources) {

    // Aggregate the results and update the RDF.
    final EdmObject edm = new EdmObject(incomingRdf);
    final List<Thumbnail> thumbnails = processedResources.stream()
        .map(ResourceProcessingResult::getThumbnails).filter(Objects::nonNull).flatMap(List::stream)
        .collect(Collectors.toList());
    processedResources.stream().map(ResourceProcessingResult::getMetadata).filter(Objects::nonNull)
        .forEach(metadata -> metadata.updateRdf(edm));

    // Set the preview based on the new data
    updateEdmPreview(edm, thumbnails);

    // Done
    return new ImmutablePair<>(edm.getRdf(), thumbnails);
  }

  private void updateEdmPreview(EdmObject edm, List<Thumbnail> thumbnails) {
    Set<String> objectUrls = edm.getResourceUrls(Arrays.asList(UrlType.OBJECT)).keySet();
    Set<String> otherUrls =
        edm.getResourceUrls(Arrays.asList(UrlType.IS_SHOWN_BY, UrlType.HAS_VIEW)).keySet();

    if (!objectUrls.isEmpty()) {
      edm.updateEdmPreview(objectUrls.iterator().next());
    } else {
      Optional<Thumbnail> thumbnail = thumbnails.stream().filter(
          t -> t.getTargetName().contains("-LARGE") && otherUrls.contains(t.getResourceUrl()))
          .findFirst();
      if (thumbnail.isPresent()) {
        String url = String.format("%s", thumbnail.get().getTargetName());
        edm.updateEdmPreview(url);
      }
    }
  }

  @Override
  public void close() {
    mediaProcessor.close();
  }

  public interface MediaProcessingListener<I extends DownloadedResource> {

    void beforeStartingFile(I source) throws MediaException;

    // If this returns true, we will stop processing.
    boolean handleMediaException(I source, MediaException exception);

    // If this returns true, we will stop processing.
    boolean handleOtherException(I source, Exception exception);

    void afterCompletingFile(I source);

  }
}
