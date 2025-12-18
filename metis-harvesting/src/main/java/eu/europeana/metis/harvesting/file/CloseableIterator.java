package eu.europeana.metis.harvesting.file;

import java.io.Closeable;
import java.util.Iterator;

/**
 * Represents an iterator that provides the ability to traverse elements and also supports explicit resource management. This
 * interface combines the functionalities of {@link Iterator} for element iteration and {@link AutoCloseable} for resource
 * closure.
 *
 * @param <T> the type of elements returned by this iterator
 */
public interface CloseableIterator<T> extends Iterator<T>, Closeable {

}
