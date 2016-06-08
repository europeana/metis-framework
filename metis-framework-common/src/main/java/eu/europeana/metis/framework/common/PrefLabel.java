package eu.europeana.metis.framework.common;

import org.mongodb.morphia.annotations.Entity;

/**
 * Created by ymamakis on 4/4/16.
 */
@Entity(value="prefLabel")
public class PrefLabel extends Label<String> {
}
