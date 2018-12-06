package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaprocessing.exception.MediaException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdfImpl;
import eu.europeana.metis.mediaprocessing.model.ResourceProcessingResult;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaservice.MediaProcessor;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

@Deprecated
public class TemporaryMediaService implements Closeable {

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
    final List<ResourceProcessingResult> processedResources = performResourceProcessing(sources, listener);
    return mergeProcessedResources(incomingRdf, processedResources);
  }

  // This method is probably thread-safe.
  // The files are done sequentially: the next one begins only if the previous one is completed.
  public <I extends DownloadedResource> List<ResourceProcessingResult> performResourceProcessing(
      List<I> sources, MediaProcessingListener<I> listener) {
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
    final EnrichedRdf enrichedRdf = new EnrichedRdfImpl(incomingRdf);
    processedResources.stream().map(ResourceProcessingResult::getMetadata)
        .forEach(enrichedRdf::enrichResource);
    final List<Thumbnail> thumbnails = processedResources.stream()
        .map(ResourceProcessingResult::getThumbnails).filter(Objects::nonNull).flatMap(List::stream)
        .collect(Collectors.toList());
    return new ImmutablePair<>(enrichedRdf.finalizeRdf(), thumbnails);
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
