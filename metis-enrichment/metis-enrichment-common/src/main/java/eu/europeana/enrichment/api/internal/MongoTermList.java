/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved by the
 * European Commission; You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at: http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, without warranties or conditions of any kind, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.europeana.enrichment.api.internal;

import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlTransient;
import org.bson.types.ObjectId;
import org.mongojack.DBRef;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;

/**
 * Basic Class linking a number of MongoTerms. This class enables searching by CodeUri for fetching
 * all the relevant MongoTerms while it includes the parent term (skos:broader, dcterms:isPartOf),
 * the className of the entityType for deserialization and a JSON representation of the contextual
 * class
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 * @param <T> AgentImpl, PlaceImpl, ConceptImpl, TimespanImpl, OrganizationImpl
 */

public abstract class MongoTermList<T extends AbstractEdmEntityImpl> implements DatedObject {

  private String parent;
  private String codeUri;
  private String[] owlSameAs;

  private Date created;
  private Date modified;

  @JsonIgnore
  @XmlTransient
  private List<DBRef<? extends MongoTerm, String>> terms;

  @JsonProperty("_id")
  private ObjectId id;

  protected T representation;
  private String entityType;

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getCodeUri() {
    return codeUri;
  }

  public void setCodeUri(String codeUri) {
    this.codeUri = codeUri;
  }

  public List<DBRef<? extends MongoTerm, String>> getTerms() {
    return terms;
  }

  public void setTerms(List<DBRef<? extends MongoTerm, String>> terms) {
    this.terms = terms;
  }

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public abstract T getRepresentation();

  public abstract void setRepresentation(T representation);

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public void setOwlSameAs(String[] owlSameAs) {
    this.owlSameAs = owlSameAs;
  }

  public String[] getOwlSameAs() {
    return owlSameAs;
  }

  @SuppressWarnings("unchecked")
  public static <T extends AbstractEdmEntityImpl, S extends T> MongoTermList<T> cast(
      MongoTermList<S> source) {
    return (MongoTermList<T>) source;
  }

  @Override
  public Date getModified() {
    return modified;
  }

  @Override
  public void setModified(Date modified) {
    this.modified = modified;
  }

  @Override
  public Date getCreated() {
    return created;
  }

  @Override
  public void setCreated(Date created) {
    this.created = created;
  }
}
