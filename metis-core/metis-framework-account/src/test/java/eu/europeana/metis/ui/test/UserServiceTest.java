package eu.europeana.metis.ui.test;

import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.domain.User;
import eu.europeana.metis.ui.mongo.domain.DBUser;
import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import eu.europeana.metis.ui.mongo.service.UserService;
import eu.europeana.metis.ui.test.config.TestApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

/**
 * Created by ymamakis on 11/25/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration (classes = TestApplication.class)
public class UserServiceTest {
    @Autowired
    private UserService service;
    @Autowired
    private UserDao userDao;

    @Test
    public void testUserUpdate(){
        Mockito.doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                return null;
            }
        }).when(userDao).create(ldapUser(false,false));
        Mockito.when(userDao.findByPrimaryKey("test@test.com")).thenReturn(ldapUser(false,false));
        service.createLdapUser(ldapUser(false,false));
        Assert.assertEquals(ldapUser(false,false),service.getUser("test@test.com").getUser());
    }

    @Test
    public void testLdapCreate(){
        Mockito.doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                return null;
            }
        }).when(userDao).create(ldapUser(false,false));
        Mockito.when(userDao.findByPrimaryKey("test@test.com")).thenReturn(ldapUser(false,false));
        service.createLdapUser(ldapUser(false,false));
        UserDTO userDto = new UserDTO();
        userDto.setDbUser(dbUser());
		service.updateUserFromDTO(userDto);
        UserDTO dto = service.getUser("test@test.com");
        DBUser dbUser = dto.getDbUser();
        dbUser.setSkypeId("newSkypeId");

    }

    @Test
    public void testDBUser(){
        UserDTO userDto = new UserDTO();
        userDto.setDbUser(dbUser());
		service.updateUserFromDTO(userDto);
        UserDTO dto = service.getUser("test@test.com");
        Assert.assertEquals(dto.getDbUser().getCountry(),Country.ALBANIA);
        Assert.assertTrue(dto.getDbUser().getEuropeanaNetworkMember());
        Assert.assertEquals(dto.getDbUser().getNotes(),"test notes");
        Assert.assertEquals(dto.getDbUser().getSkypeId(),"testSkypeId");
    }
    @Test
    public void testDBUserWithRequest(){
        RoleRequest request = roleRequest();
        UserDTO userDto = new UserDTO();
        userDto.setDbUser(dbUser());
		service.updateUserFromDTO(userDto);
        UserDTO dto = service.getUser("test@test.com");
        Assert.assertEquals(dto.getDbUser().getCountry(),Country.ALBANIA);
        Assert.assertTrue(dto.getDbUser().getEuropeanaNetworkMember());
        Assert.assertEquals(dto.getDbUser().getNotes(),"test notes");
        Assert.assertEquals(dto.getDbUser().getSkypeId(),"testSkypeId");
        List<RoleRequest> retRequests = service.getUserRequests("test@test.com");
        Assert.assertEquals(retRequests.size(),1);
        Assert.assertEquals(retRequests.get(0).getOrganizationId(),request.getOrganizationId());
        Assert.assertEquals(retRequests.get(0).getRequestStatus(),request.getRequestStatus());
        Assert.assertEquals(retRequests.get(0).getRequestDate(),request.getRequestDate());
        Assert.assertEquals(retRequests.get(0).getRequestStatus(), request.getRequestStatus());
        Assert.assertEquals(retRequests.get(0).getUserId(),request.getUserId());
        Assert.assertEquals(1,service.getAllRequestsForUser("test@test.com",0,10).size());
        Assert.assertEquals(1,service.getAllPendingRequestsForUser("test@test.com",0,10).size());
        Assert.assertEquals(1,service.getAllRequests(0,10).size());
        Assert.assertEquals(1,service.getAllPendingRequests(0,10).size());
    }

    @Test
    public void approveUser(){
        Mockito.doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                return null;
            }
        }).when(userDao).approve(ldapUser(false,false).getEmail());
        Mockito.when(userDao.findByPrimaryKey("test@test.com")).thenReturn(ldapUser(true,true));
        service.createLdapUser(ldapUser(false,false));
        service.approveUser("test@test.com");
        User user = service.getUser("test@test.com").getUser();
        Assert.assertTrue(user.isActive());
        Assert.assertTrue(user.isApproved());
    }
    @Test
    public void approveUserWithRequests(){
        Mockito.doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                return null;
            }
        }).when(userDao).approve(ldapUser(false,false).getEmail());
        Mockito.when(userDao.findByPrimaryKey("test@test.com")).thenReturn(ldapUser(true,true));
        service.createLdapUser(ldapUser(false,false));
        UserDTO userDto = new UserDTO();
        userDto.setDbUser(dbUser());
		service.updateUserFromDTO(userDto);
        service.approveUser("test@test.com");
        UserDTO userDTO = service.getUser("test@test.com");
        Assert.assertTrue(userDTO.getUser().isActive());
        Assert.assertTrue(userDTO.getUser().isApproved());
        Assert.assertEquals(userDTO.getDbUser().getOrganizationRoles().size(),1);
        Assert.assertEquals(userDTO.getDbUser().getOrganizationRoles().get(0),"orgId");
    }

    @Test
    public void testDeleteUser(){
        Mockito.doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                return null;
            }
        }).when(userDao).delete(ldapUser(true,true));
        Mockito.when(userDao.findByPrimaryKey("test@test.com")).thenReturn(ldapUser(true,false));
        service.createLdapUser(ldapUser(true,true));
        service.deleteUser("test@test.com");
        UserDTO userDTO = service.getUser("test@test.com");
        Assert.assertFalse(userDTO.getUser().isActive());
        Assert.assertTrue(userDTO.getUser().isApproved());
    }
    private User ldapUser(boolean approved, boolean active){
        User user = new User();
        user.setEmail("test@test.com");
        user.setActive(active);
        user.setApproved(approved);
        return user;
    }

    private DBUser dbUser(){
        DBUser dbUser = new DBUser();
        dbUser.setEmail("test@test.com");
        dbUser.setEuropeanaNetworkMember(true);
        dbUser.setCountry(Country.ALBANIA);
        dbUser.setNotes("test notes");
        dbUser.setSkypeId("testSkypeId");
        return dbUser;
    }

    private RoleRequest roleRequest(){
        RoleRequest request = new RoleRequest();
        request.setRequestDate(new Date());
        request.setOrganizationId("orgId");
        request.setRequestStatus("pending");
        request.setUserId("test@test.com");
        return request;
    }
}
