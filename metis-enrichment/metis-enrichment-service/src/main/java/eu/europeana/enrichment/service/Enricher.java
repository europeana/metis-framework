/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.europeana.enrichment.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.utils.InputValue;

/**
 * Tagging (aka semantic enrichment) of records from SOLR with built-in vocabularies.
 * 
 * @author Borys Omelayenko
 * @author Yorgos.Mamakis@ europeana.eu
 */
@Service
public class Enricher {
  private static final Logger LOGGER = LoggerFactory.getLogger(Enricher.class);
  private RedisInternalEnricher redisEnricher;

  @Autowired
  public Enricher(RedisInternalEnricher redisEnricher) {
    this.redisEnricher = redisEnricher;
  }

  /**
   * Main enrichment method
   * 
   * @param values The values to enrich
   * @return The resulting enrichment List
   * @throws IOException
   */
  public List<EntityWrapper> tagExternal(List<InputValue> values) throws IOException {
    return new ArrayList<>(redisEnricher.tag(values));
  }

  public EntityWrapper getByUri(String uri) {
    try {
      return redisEnricher.getByUri(uri);
    } catch (RuntimeException | IOException e) {
      LOGGER.warn("Unable to rerieve entity form uri {}", e);
    }
    return null;
  }
}
