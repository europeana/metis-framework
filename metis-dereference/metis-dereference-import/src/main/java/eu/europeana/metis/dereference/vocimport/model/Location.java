package eu.europeana.metis.dereference.vocimport.model;

import eu.europeana.metis.exception.BadContentException;
import java.io.IOException;
import java.io.InputStream;

public interface Location {

  /**
   * Read a location to an input stream.
   *
   * @return An input stream. The caller is responsible for closing the stream.
   * @throws IOException In case the location could not be read.
   */
  InputStream read() throws IOException;

  /**
   * Resolve a relative location against the given location. The given location can be assumed to be a file (as opposed to a
   * path/directory) so that essentially the relative location is resolved against the parent of the given location.
   *
   * @param relativeLocation The relative location to resolve.
   * @return The resolved location.
   * @throws BadContentException if the resolve did not succeed
   */
  Location resolve(String relativeLocation) throws BadContentException;

  /**
   * @return A human-readable representation of the location.
   */
  String toString();
}
