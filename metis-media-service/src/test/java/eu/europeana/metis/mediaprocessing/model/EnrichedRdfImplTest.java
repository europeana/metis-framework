package eu.europeana.metis.mediaprocessing.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EnrichedRdfImplTest {

  private static ResourceMetadata createResourceMetadata(String resourceUrl,
      Set<String> thumbnailNames) {
    final ResourceMetadata metadata = mock(ResourceMetadata.class);
    doReturn(resourceUrl).when(metadata).getResourceUrl();
    doReturn(thumbnailNames).when(metadata).getThumbnailTargetNames();
    final AbstractResourceMetadata source = mock(AbstractResourceMetadata.class);
    doReturn(source).when(metadata).getMetaData();
    return metadata;
  }

  @Test
  void testEnrichResource() {

    // Create objects. Empty RDF.
    final RDF rdf = spy(new RDF());
    final EnrichedRdfImpl enrichedRdf = new EnrichedRdfImpl(rdf);
    rdf.setWebResourceList(null);

    // Add first resource: need to create new list and new resource in RDF.
    reset(rdf);
    final String url1 = "url1";
    final Set<String> names1 = new HashSet<>(Arrays.asList("name1", "name2"));
    final ResourceMetadata resource1 = createResourceMetadata(url1, names1);
    enrichedRdf.enrichResource(resource1);

    // Verify adding the first resource.
    verify(rdf, times(1)).setWebResourceList(notNull());
    assertEquals(1, rdf.getWebResourceList().size());
    assertEquals(url1, rdf.getWebResourceList().get(0).getAbout());
    assertEquals(names1, enrichedRdf.getThumbnailTargetNames(url1));
    verify(resource1.getMetaData(), times(1)).updateResource(any());

    // Add the second resource: need to create new resource in RDF.
    reset(rdf);
    final String url2 = "url2";
    final Set<String> names2 = new HashSet<>(Arrays.asList("name3", "name4"));
    final ResourceMetadata resource2 = createResourceMetadata(url2, names2);
    enrichedRdf.enrichResource(resource2);

    // Verify adding the second resource.
    verify(rdf, never()).setWebResourceList(notNull());
    assertEquals(2, rdf.getWebResourceList().size());
    assertEquals(url2, rdf.getWebResourceList().get(1).getAbout());
    assertEquals(names2, enrichedRdf.getThumbnailTargetNames(url2));
    verify(resource2.getMetaData(), times(1)).updateResource(any());

    // Add the third resource: make sure it already exists in RDF first.
    reset(rdf);
    final String url3 = "url3";
    final Set<String> names3 = new HashSet<>(Arrays.asList("name5", "name6"));
    final ResourceMetadata resource3 = createResourceMetadata(url3, names3);
    final WebResourceType webResourceType3 = new WebResourceType();
    webResourceType3.setAbout(url3);
    rdf.getWebResourceList().add(webResourceType3);
    enrichedRdf.enrichResource(resource3);

    // Verify adding the second resource.
    verify(rdf, never()).setWebResourceList(notNull());
    assertEquals(3, rdf.getWebResourceList().size());
    assertEquals(url3, rdf.getWebResourceList().get(2).getAbout());
    assertEquals(names3, enrichedRdf.getThumbnailTargetNames(url3));
    verify(resource3.getMetaData(), times(1)).updateResource(any());
  }
}
