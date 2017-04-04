/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.mapping.persistence;

import com.mongodb.MongoClient;
import eu.europeana.metis.mapping.validation.Flag;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * A Flag persistence DAO
 * Created by ymamakis on 6/15/16.
 */
public class FlagDao extends BasicDAO<Flag, ObjectId> {
    /**
     * Default constructor
     * @param morphia The Morphia wrapper to use
     * @param mongo The Mongo connection settings
     * @param database The database to connect to
     */
    public FlagDao(Morphia morphia, MongoClient mongo, String database){
        super(mongo,morphia,database);
    }
}
