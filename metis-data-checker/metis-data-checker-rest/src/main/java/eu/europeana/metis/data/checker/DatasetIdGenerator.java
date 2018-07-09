package eu.europeana.metis.data.checker;

import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatasetIdGenerator implements Supplier<String> {

  @Autowired
  public DatasetIdGenerator() {}

  @Override
  public String get() {
    return UUID.randomUUID().toString();
  }
}
