package research;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 11 de Abr de 2013
 */
public class MapOfLists<K, O> implements Serializable {

  private static final long serialVersionUID = 1;

  private final Hashtable<K, ArrayList<O>> hashtable;
  private int listInitialCapacity = -1;

  /**
   * Creates a new instance of this class.
   */
  public MapOfLists() {
    hashtable = new Hashtable<>();
  }

  /**
   * @param key
   * @param value
   */
  public void put(K key, O value) {
    ArrayList<O> recs = hashtable.get(key);
    if (recs == null) {
      recs = new ArrayList<>(listInitialCapacity == -1 ? 1 : listInitialCapacity);
      recs.add(value);
      hashtable.put(key, recs);
    } else {
      recs.add(value);
    }
  }

  /**
   * @param key
   * @param values
   */
  public void putAll(K key, Collection<O> values) {
    ArrayList<O> recs = hashtable.get(key);
    if (recs == null) {
      recs = new ArrayList<>(listInitialCapacity == -1 ? 1 : listInitialCapacity);
      recs.addAll(values);
      hashtable.put(key, recs);
    } else {
      recs.addAll(values);
    }
  }

  /**
   * @return bool
   */
  public boolean containsKey(K key) {
    return hashtable.containsKey(key);
  }

  /**
   * @return list
   */
  public List<O> get(K key) {
    return hashtable.get(key);
  }

  /**
   * @return keySet
   */
  public Set<K> keySet() {
    return hashtable.keySet();
  }

  /**
   *
   */
  @SuppressWarnings("unchecked")
  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    @SuppressWarnings("rawtypes")
    ArrayList keys = new ArrayList(hashtable.keySet());
    Collections.sort(keys);
    for (K key : (List<K>) keys) {
      List<O> vals = get(key);
      buffer.append(key.toString()).append("(").append(vals.size()).append(")").append(":\n");
      for (O val : vals) {
        buffer.append("\t").append(val.toString()).append("\n");
      }
    }
    return buffer.toString();
  }
}
