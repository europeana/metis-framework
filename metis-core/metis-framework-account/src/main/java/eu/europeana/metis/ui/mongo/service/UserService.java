package eu.europeana.metis.ui.mongo.service;

import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.domain.User;
import eu.europeana.metis.ui.mongo.dao.DBUserDao;
import eu.europeana.metis.ui.mongo.dao.RoleRequestDao;
import eu.europeana.metis.ui.mongo.domain.DBUser;
import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ymamakis on 11/24/16.
 */
@Service
public class UserService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private DBUserDao dbUserDao;
    @Autowired
    private RoleRequestDao roleRequestDao;

    public void updateUserFromDTO(UserDTO userDto){
        if(userDto.getUser()!=null) {
           updateUserInLdap(userDto.getUser());
        }
        if(userDto.getDbUser()!=null){
            updateUserInMongo(userDto.getDbUser());
        }
    }

    public void deleteUser(String email){
        userDao.disable(email);
    }

    public void createLdapUser(User user){
        userDao.create(user);
    }

    public void createDBUser(DBUser dbUser, RoleRequest... requests){
        dbUserDao.save(dbUser);
        if(requests!=null){
            for (RoleRequest request:requests) {
                createRequest(request);
            }
        }
    }

    public void createRequest(RoleRequest request){
        roleRequestDao.save(request);
    }

    public List<RoleRequest> getUserRequests(String email){
        Query<RoleRequest> requestQuery = roleRequestDao.createQuery();
        requestQuery.filter("userId",email);
        return roleRequestDao.find(requestQuery).asList();
    }

    public void approveRequest(RoleRequest request){
        UpdateOperations<RoleRequest> ops = roleRequestDao.createUpdateOperations();
        ops.set("requestStatus","approved");
        Query<RoleRequest> query = roleRequestDao.createQuery();
        query.filter("id",request.getId());
        roleRequestDao.update(query,ops);
        DBUser user = dbUserDao.findOne("userId",request.getUserId());
        if(user!=null) {
            List<String> orgIds = user.getOrganizations();
            orgIds.add(request.getOrganizationId());
            user.setOrganizations(orgIds);
            updateUserInMongo(user);
        }

    }

    public void approveUser(String email){
        userDao.approve(email);
        List<RoleRequest> requests = getUserRequests(email);
        List<String> orgIds = new ArrayList<>();
        for(RoleRequest request:requests){
            approveRequest(request);
        }

    }

    private void updateUserInMongo(DBUser user){
        UpdateOperations<DBUser> ops = dbUserDao.createUpdateOperations();
        Query<DBUser> query = dbUserDao.createQuery();
        query.filter("id", user.getId());
        if(user.getCountry()!=null) {
            ops.set("country", user.getCountry());
        } else {
            ops.unset("country");
        }
        ops.set("modified",new Date());
        if(user.getEuropeanaNetworkMember()!=null) {
            ops.set("europeanaNetworkMember", user.getEuropeanaNetworkMember());
        } else {
            ops.unset("europeanaNetworkMember");
        }
        if(user.getNotes()!=null) {
            ops.set("notes", user.getNotes());
        } else {
            ops.unset("notes");
        }
        if(user.getSkypeId()!=null) {
            ops.set("skypeId", user.getSkypeId());
        } else {
            ops.unset("skypeId");
        }
        if(user.getOrganizations()!=null) {
            ops.set("organizations", user.getOrganizations());
        } else {
            ops.unset("organizations");
        }
        dbUserDao.update(query,ops);
    }

    private void updateUserInLdap(User user){
        userDao.update(user);
    }

    public UserDTO getUser(String email){
        UserDTO userDto = new UserDTO();
        userDto.setUser(userDao.findByPrimaryKey(email));
        userDto.setDbUser(dbUserDao.findOne("email",email));
        return userDto;
    }

    public List<RoleRequest>getAllRequests(int from, int to){
        Query<RoleRequest> requestQuery = roleRequestDao.createQuery();
        requestQuery.offset(from);
        requestQuery.limit(to-from);
        return roleRequestDao.find(requestQuery).asList();
    }

    public List<RoleRequest> getAllPendingRequests(int from, int to){
        Query<RoleRequest> requestQuery = roleRequestDao.createQuery();
        requestQuery.offset(from);
        requestQuery.limit(to-from);
        requestQuery.filter("requestStatus","pending");
        return roleRequestDao.find(requestQuery).asList();
    }

    public List<RoleRequest> getAllPendingRequestsForUser(String email, int from, int to){
        Query<RoleRequest> requestQuery = roleRequestDao.createQuery();
        requestQuery.offset(from);
        requestQuery.limit(to-from);
        requestQuery.filter("email",email);
        requestQuery.filter("requestStatus","pending");
        return roleRequestDao.find(requestQuery).asList();
    }

    public List<RoleRequest> getAllRequestsForUser(String email){
        Query<RoleRequest> requestQuery = roleRequestDao.createQuery();
        requestQuery.filter("email",email);
        requestQuery.filter("requestStatus","pending");
        return roleRequestDao.find(requestQuery).asList();
    }


}
