package eu.europeana.metis.preview;

import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CollectionIdGenerator implements Supplier<String> {

  @Autowired
  public CollectionIdGenerator() {}

  @Override
  public String get() {
    return UUID.randomUUID().toString();
  }
}
