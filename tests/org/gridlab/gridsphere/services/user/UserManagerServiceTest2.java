package org.gridlab.gridsphere.services.user;

import org.gridlab.gridsphere.portlet.service.spi.ServiceTest;
import org.gridlab.gridsphere.portlet.service.PortletServiceException;
import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.PortletGroup;
import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.portlet.impl.SportletUserImpl;
import org.gridlab.gridsphere.portlet.impl.SportletRole;
import org.gridlab.gridsphere.portlet.impl.SportletGroup;
import org.gridlab.gridsphere.services.security.acl.AccessControlService;
import org.gridlab.gridsphere.services.security.acl.AccessControlManagerService;
import org.gridlab.gridsphere.services.security.acl.impl2.UserACL;
import org.gridlab.gridsphere.core.persistence.castor.PersistenceManagerRdbms;
import org.gridlab.gridsphere.core.persistence.ConfigurationException;
import org.gridlab.gridsphere.core.persistence.CreateException;
import org.gridlab.gridsphere.core.persistence.PersistenceException;
import org.gridlab.gridsphere.core.persistence.DeleteException;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestResult;

import java.util.List;

/*
 * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
 * @team sonicteam
 * @version $Id$
 */
public class UserManagerServiceTest2 extends ServiceTest {

    private static PortletLog log = SportletLog.getInstance(UserManagerServiceTest.class);
    private static AccessControlService aclService = null;
    private static AccessControlManagerService aclManagerService = null;
    private static UserManagerService userManager = null;
    private PersistenceManagerRdbms pm = null;
    private PortletGroup portal, triana, cactus;
    private User superuser;


    public UserManagerServiceTest2(String name) {
        super(name);
    }

    public static void main (String[] args) throws Exception {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite ( ) {
        return new TestSuite(UserManagerServiceTest2.class);
    }

    protected void setUp() {
        super.setUp();
        log.info(" =====================================  setup");
        // Create both services using mock ServletConfig
        try {
            aclService = (AccessControlService)factory.createPortletService(AccessControlService.class, props, null, true);
            aclManagerService = (AccessControlManagerService)factory.createPortletService(AccessControlManagerService.class, props, null, true);
            userManager = (UserManagerService)factory.createPortletService(UserManagerService.class, props, null, true);
        } catch (Exception e) {
            log.error("Unable to initialize services: ", e);
        }
        pm = new PersistenceManagerRdbms();

        try {
            pm.deleteList("select u from org.gridlab.gridsphere.services.user.impl.AccountRequestImpl u");
            pm.deleteList("select u from org.gridlab.gridsphere.portlet.impl.SportletUserImpl u");
            pm.deleteList("select u from org.gridlab.gridsphere.portlet.impl.SportletGroup u");
            pm.deleteList("select u from org.gridlab.gridsphere.services.security.acl.impl2.UserACL u");
        } catch (PersistenceException e) {
            log.error("Exception " + e);
        }


        setupSuperUser();
        setupGroups();
        setupUsers();


    }

    protected void tearDown() {

        log.info(" ===================================== tear down");
    }

    // === helper

    public User getSuperUser() {
        List result = null;
        try {
            result = aclService.getSuperUsers();
        } catch (PortletServiceException e) {
            fail("Should be able to get superusers!");
            log.error("Exception " + e);
        }
        return (User)result.get(0);
    }


    // === tests

    public void testremoveUser() {
        log.info("+ testremoveUser");
        User jason = userManager.loginUser("jason");
        try {
            userManager.removeUser(jason, "michael");
            fail("Should fail here!");
        } catch (PermissionDeniedException e) {}

        try {
            userManager.removeUser(getSuperUser(), "michael");
        } catch (PermissionDeniedException e) {
            fail("should NOT fail here");
        }

        List users = userManager.getAllUsers();
        assertEquals(2, users.size());

    }

    public void testdenyGroupRequest() {
        log.info("+ testdenygrouprequest");
        User jason = userManager.loginUser("jason");
        try {
            userManager.denyGroupRequest(superuser, jason,  portal, null);
        } catch (PermissionDeniedException e) {
            log.error("Exception " + e);
            fail("Should NOT fail");
        }

        boolean isInGroup = false;
        try {
            isInGroup = aclService.isUserInGroup(jason, portal);
        } catch (PortletServiceException e) {
            log.error("Exception "+e);
            fail("should not fail here");
        }

        assertTrue(!isInGroup);
    }

    public void testApproveGroup() {
        log.info("+ testapprovegroup");
        User jason = userManager.loginUser("jason");
        try {
            userManager.approveGroupRequest(superuser, jason,  portal, null);
        } catch (PermissionDeniedException e) {
            log.error("Exception " + e);
            fail("Should NOT fail");
        }

        User michael = userManager.loginUser("michael");

        try {
            userManager.approveGroupRequest(jason, michael, triana, null);
            fail("This should fail!");
        } catch (PermissionDeniedException e) {}

        try {
            userManager.approveGroupRequest(jason, michael, portal, null);
        } catch (PermissionDeniedException e) {
            log.error("Exception " + e);
            fail("This should not fail");
        }

        List result = null;

        try {
            result = aclService.getGroups(michael);
        } catch (PortletServiceException e) {
            log.error("Exception " + e);
            fail("should not fail");
        }

        assertEquals(1, result.size());

    }

    public void testremoveGroup() {
        log.info("+ testremovegroup");
        try {
            aclManagerService.removeGroup(portal);
        } catch (PortletServiceException e) {
            log.error("Exception " + e);
            fail("should not fail");
        }

        List result = null;
        try {
            result = aclService.getAllGroups();
        } catch (PortletServiceException e) {
            log.error("Exception " + e);
            fail("Should not fail here");
        }

        assertEquals(2, result.size());
    }


    //@todo test which tries to create a new user with existing login
    //@todo all that useracl stuff

    public void testmodifyExistingUser() {
        log.info("+ testmodifyExistingUser");
        User jason = userManager.loginUser("jason");
        String jasonid = jason.getID();
        AccountRequest req = userManager.changeAccountRequest(jason);
        req.setFamilyName("Novotny");
        try {
            userManager.submitAccountRequest(req);
        } catch (PortletServiceException e) {
            log.error("Exception " + e);
            fail("Should not fail at submit accountrequest");
        }

        User root = getSuperUser();
        List result = userManager.getAccountRequests();
        // now simply approve all
        for (int i=0;i<result.size();i++) {
            try {
                userManager.approveAccountRequest(root,(AccountRequest)result.get(i), null);
            } catch (PermissionDeniedException e) {
                log.error("Exception " + e);
                fail("Should not fail here");
            }
        }

        // now we should have 4+1 users
        result = userManager.getAllUsers();
        assertEquals(5, result.size());

        User newjason = userManager.loginUser("jason");
        assertEquals("Novotny", newjason.getFamilyName());
        assertEquals(jasonid, newjason.getID());

    }

    public void testExistsUser() {
        log.info("+ testExsistUser");
        assertTrue(userManager.existsUser("jason"));
    }

    public void testDenyAccountRequest() {
        log.info("+ testdenyAccountRequest");
        User jason = userManager.loginUser("jason");
        List result = userManager.getAccountRequests();
        try {
            userManager.denyAccountRequest(jason,(AccountRequest)result.get(0), null);
            fail("should raise Permission denied Exception");
        } catch (PermissionDeniedException e) {
        }

        try {
            userManager.denyAccountRequest(getSuperUser(),(AccountRequest)result.get(0), null);
        } catch (PermissionDeniedException e) {
           fail("should NOT raise Permission denied Exception");
        }
    }

    public void testApproveAccount() {
        log.info("+ testApproveAccount");
        User jason = userManager.loginUser("jason");
        List result = userManager.getAccountRequests();
        try {
            userManager.approveAccountRequest(jason,(AccountRequest)result.get(0), null);
            fail("should raise Permission denied Exception");
        } catch (PermissionDeniedException e) {
        }

        try {
            userManager.approveAccountRequest(getSuperUser(),(AccountRequest)result.get(0), null);
        } catch (PermissionDeniedException e) {
           fail("should NOT raise Permission denied Exception");
        }

    }

    public void testUsers() {
        List result = userManager.getAllUsers();
        assertEquals(3,result.size());
    }

    public void testloginUser() {
        log.info("+ testLoginuser");
        User u = userManager.loginUser("jason");

        assertEquals("Jason", u.getGivenName());
    }


    public void testgetSuperUser() {
        log.info("+ testSuperUser");
        List result = null;
        try {
            result = aclService.getSuperUsers();
        } catch (PortletServiceException e) {
            fail("Should be able to get superusers!");
            log.error("Exception " + e);
        }
        assertEquals(1,result.size());
        assertEquals(((SportletUserImpl)result.get(0)).getFamilyName(),"Wehrens");
    }

    public void testGroups() {
        log.info("+ testGroups");
        List result = null;
        try {
            result = aclService.getAllGroups();
        } catch (PortletServiceException e) {
            log.error("Exception " + e);
            fail("could not get Groups");
        }
        assertEquals(3,result.size());

        String groupid = ((PortletGroup)result.get(0)).getID();
        String groupname = ((PortletGroup)result.get(0)).getName();

        try {
            aclManagerService.renameGroup((PortletGroup)result.get(0), "globus");
        } catch (PortletServiceException e) {
            log.error("Exception " + e);
            fail("grouprenaming failed");
        }
        // get them again..yeah codeshareing
        try {
            result = aclService.getAllGroups();
        } catch (PortletServiceException e) {
            log.error("Exception " + e);
            fail("could not get Groups");
        }
        assertEquals(3,result.size());

        for (int i=0; i<result.size();i++) {
            if (((PortletGroup)result.get(i)).getID().equals(groupid)){
                assertTrue(!((PortletGroup)result.get(i)).getName().equals(groupname));
            }
        }
    }



    public void testAccountRequestImpl() {
        log.info("+ testAccountRequestImpl");
        List result = null;

        result = userManager.getAccountRequests();
        assertEquals(2,result.size());

    }

    // === setup

    public void setupSuperUser() {
        log.info("- setup superuser");
        // create a superuser - faked
        SportletUserImpl root = new SportletUserImpl();
        root.setFamilyName("Wehrens");
        root.setGivenName("Oliver");
        root.setUserID("wehrens");

        UserACL rootacl = new UserACL();
        rootacl.setUserID(root.getOid());
        rootacl.setRoleID(SportletRole.SUPER);
        rootacl.setGroupID(SportletGroup.getSuperGroup().getID());
        rootacl.setStatus(UserACL.STATUS_APPROVED);

        try {
            pm.create(root);
            pm.create(rootacl);
        } catch (PersistenceException e) {
            log.error("Exception " + e);
        }

        superuser = root;
    }

    public void setupUsers() {
        log.info("- setup users");
        // create real Users

        // jason, michael should be users
        // oliver was denied
        // ian should be still in accountrequest status

        AccountRequest req1 = userManager.createAccountRequest();
        req1.setUserID("jason");req1.setGivenName("Jason");
        req1.addToGroup(portal, SportletRole.getAdminRole());
        req1.addToGroup(cactus, SportletRole.getUserRole());

        AccountRequest req2 = userManager.createAccountRequest();
        req2.setUserID("michael");req2.setGivenName("Michael");
        req2.addToGroup(portal, SportletRole.getUserRole());
        req2.addToGroup(triana, SportletRole.getUserRole());

        AccountRequest req3 = userManager.createAccountRequest();
        req3.setUserID("ian");req3.setGivenName("Ian");

        AccountRequest req4 = userManager.createAccountRequest();
        req4.setUserID("oliver");req4.setGivenName("Oliver");

        User root = getSuperUser();
        try {
            userManager.submitAccountRequest(req1);
            userManager.submitAccountRequest(req2);
            userManager.submitAccountRequest(req3);
            userManager.submitAccountRequest(req4);
            userManager.approveAccountRequest(root, req1, null);
            userManager.approveAccountRequest(root, req2, null);
        } catch (PortletServiceException e) {
            log.error("Exception " + e);
            fail("failed to generate AccountRequests");
        } catch (PermissionDeniedException e) {
                log.error("Exception " + e);
                fail("No permissions!");
        }

    }

    public void setupGroups(){
        log.info("- setup groups");
        try {
            aclManagerService.createNewGroup("portal");
            aclManagerService.createNewGroup("triana");
            aclManagerService.createNewGroup("cactus");
        } catch (PortletServiceException e) {
            log.error("Exception " + e);
            fail("Could not create groups ");
        }
        List groups = null;
        try {
            groups = aclService.getAllGroups();
        } catch (PortletServiceException e) {
            fail("shouldnot fail here");
            log.error("Exception " + e);
        }

        for (int i=0; i<groups.size();i++) {
            if (((PortletGroup)groups.get(i)).getName().equals("cactus")) {
                cactus = (PortletGroup)groups.get(i);
            }
            if (((PortletGroup)groups.get(i)).getName().equals("triana")) {
                triana = (PortletGroup)groups.get(i);
            }
            if (((PortletGroup)groups.get(i)).getName().equals("portal")) {
                portal = (PortletGroup)groups.get(i);
            }
        }
    }
}
