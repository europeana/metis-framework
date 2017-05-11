package eu.europeana.metis.core.service;

import eu.europeana.metis.core.api.MetisKey;
import eu.europeana.metis.core.dao.AuthorizationDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by gmamakis on 7-2-17.
 */
public class MetisAuthorizationService {
    @Autowired
    private AuthorizationDao dao;

    public MetisKey getKeyFromId(String id){
        return dao.getById(id);
    }
}
