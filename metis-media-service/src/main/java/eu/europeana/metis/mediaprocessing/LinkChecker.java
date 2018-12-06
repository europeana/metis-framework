package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.LinkCheckingException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import java.io.Closeable;

public interface LinkChecker extends Closeable {

  void performLinkChecking(RdfResourceEntry resourceEntry) throws LinkCheckingException;

}
