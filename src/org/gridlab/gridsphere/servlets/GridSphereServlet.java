/*
* @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
* @version $Id$
*/
package org.gridlab.gridsphere.servlets;


import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
import org.gridlab.gridsphere.core.persistence.hibernate.DBTask;
import org.gridlab.gridsphere.layout.PortletLayoutEngine;
import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.portlet.impl.*;
import org.gridlab.gridsphere.portlet.service.PortletServiceException;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.portletcontainer.impl.GridSphereEventImpl;
import org.gridlab.gridsphere.portletcontainer.impl.SportletMessageManager;
import org.gridlab.gridsphere.portletcontainer.*;
import org.gridlab.gridsphere.services.core.registry.PortletManagerService;
import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
import org.gridlab.gridsphere.services.core.security.auth.AuthorizationException;
import org.gridlab.gridsphere.services.core.security.auth.AuthenticationException;
import org.gridlab.gridsphere.services.core.user.LoginService;
import org.gridlab.gridsphere.services.core.user.UserManagerService;
import org.gridlab.gridsphere.services.core.user.UserSessionManager;
import org.gridlab.gridsphere.services.core.request.RequestService;
import org.gridlab.gridsphere.services.core.request.GenericRequest;
import org.gridlab.gridsphere.services.core.tracker.TrackerService;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.net.SocketException;


/**
 * The <code>GridSphereServlet</code> is the GridSphere portlet container.
 * All portlet requests get proccessed by the GridSphereServlet before they
 * are rendered.
 */
public class GridSphereServlet extends HttpServlet implements ServletContextListener,
        HttpSessionAttributeListener, HttpSessionListener, HttpSessionActivationListener {

    /* GridSphere logger */
    private static PortletLog log = SportletLog.getInstance(GridSphereServlet.class);

    /* GridSphere service factory */
    private static SportletServiceFactory factory = null;

    /* GridSphere Portlet Registry Service */
    private static PortletManagerService portletManager = null;

    /* GridSphere Access Control Service */
    private static AccessControlManagerService aclService = null;

    private static UserManagerService userManagerService = null;

    private static LoginService loginService = null;

    private static TrackerService trackerService = null;

    private PortletMessageManager messageManager = SportletMessageManager.getInstance();

    /* GridSphere Portlet layout Engine handles rendering */
    private static PortletLayoutEngine layoutEngine = null;

    /* Session manager maps users to sessions */
    private UserSessionManager userSessionManager = UserSessionManager.getInstance();

    /* creates cookie requests */
    private RequestService requestService = null;

    private PortletContext context = null;
    private static Boolean firstDoGet = Boolean.TRUE;

    private static PortletSessionManager sessionManager = PortletSessionManager.getInstance();

    //private static PortletRegistry registry = PortletRegistry.getInstance();
    private static final String COOKIE_REQUEST = "cookie-request";
    private int COOKIE_EXPIRATION_TIME = 60 * 60 * 24 * 7;  // 1 week (in secs)

    private PortletGroup coreGroup = null;

    private boolean isTCK = false;

    /**
     * Initializes the GridSphere portlet container
     *
     * @param config the <code>ServletConfig</code>
     * @throws ServletException if an error occurs during initialization
     */
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);

        GridSphereConfig.setServletConfig(config);

        //SportletLog.setConfigureURL(GridSphereConfig.getServletContext().getRealPath("/WEB-INF/classes/log4j.properties"));
        this.context = new SportletContext(config);
        factory = SportletServiceFactory.getInstance();
        factory.init();
        layoutEngine = PortletLayoutEngine.getInstance();
        log.debug("in init of GridSphereServlet");
    }

    public synchronized void initializeServices() throws PortletServiceException {
        requestService = (RequestService) factory.createPortletService(RequestService.class, getServletConfig().getServletContext(), true);
        log.debug("Creating access control manager service");
        aclService = (AccessControlManagerService) factory.createPortletService(AccessControlManagerService.class, getServletConfig().getServletContext(), true);
        // create root user in default group if necessary
        log.debug("Creating user manager service");
        userManagerService = (UserManagerService) factory.createPortletService(UserManagerService.class, getServletConfig().getServletContext(), true);

        loginService = (LoginService) factory.createPortletService(LoginService.class, getServletConfig().getServletContext(), true);
        log.debug("Creating portlet manager service");
        portletManager = (PortletManagerService) factory.createPortletService(PortletManagerService.class, getServletConfig().getServletContext(), true);

        trackerService = (TrackerService) factory.createPortletService(TrackerService.class, getServletConfig().getServletContext(), true);

    }

    /**
     * Processes GridSphere portal framework requests
     *
     * @param req the <code>HttpServletRequest</code>
     * @param res the <code>HttpServletResponse</code>
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        processRequest(req, res);
    }

    public void setHeaders(HttpServletResponse res) {
        res.setContentType("text/html; charset=utf-8"); // Necessary to display UTF-8 encoded characters
        res.setHeader("Cache-Control","no-cache"); //Forces caches to obtain a new copy of the page from the origin server
        res.setHeader("Cache-Control","no-store"); //Directs caches not to store the page under any circumstance
        res.setDateHeader("Expires", 0); //Causes the proxy cache to see the page as "stale"
        res.setHeader("Pragma","no-cache"); //HTTP 1.0 backward compatibility
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        setHeaders(res);

        GridSphereEvent event = new GridSphereEventImpl(context, req, res);
        PortletRequest portletReq = event.getPortletRequest();

        // If first time being called, instantiate all portlets
        if (firstDoGet.equals(Boolean.TRUE)) {

            synchronized (firstDoGet) {
                firstDoGet = Boolean.FALSE;
                log.debug("Testing Database");
                // checking if database setup is correct
                DBTask dt = new DBTask();
                dt.setAction(DBTask.ACTION_CHECKDB);
                dt.setConfigDir(GridSphereConfig.getServletContext().getRealPath(""));
                try {
                    dt.execute();
                } catch (Exception e) {
                    RequestDispatcher rd = req.getRequestDispatcher("/jsp/errors/database_error.jsp");
                    log.error("Check DB failed: ", e);
                    req.setAttribute("error", "DB Error! Please contact your GridSphere/Database Administrator!");
                    rd.forward(req, res);
                    return;
                }

                log.debug("Initializing portlets and services");
                try {
                    // initialize needed services
                    initializeServices();
                    // create a root user if none available
                    userManagerService.initRootUser();
                    // initialize all portlets

                    PortletResponse portletRes = event.getPortletResponse();
                    PortletInvoker.initAllPortlets(portletReq, portletRes);

                    // deep inside a service is used which is why this must follow the factory.init
                    layoutEngine.init();
                } catch (Exception e) {
                    log.error("GridSphere initialization failed!", e);
                    RequestDispatcher rd = req.getRequestDispatcher("/jsp/errors/init_error.jsp");
                    req.setAttribute("error", e);
                    rd.forward(req, res);
                    return;
                }
                coreGroup = aclService.getCoreGroup();
            }
        }

        setUserAndGroups(portletReq);

        String trackme = req.getParameter(TrackerService.TRACK_PARAM);
        if (trackme != null) {
            trackerService.trackURL(trackme, req.getHeader("user-agent"), portletReq.getUser().getUserName());
            String url = req.getParameter(TrackerService.REDIRECT_URL);
            if (url != null) {
                System.err.println("redirect: " + url);
                res.sendRedirect(url);
            }
         }

        checkUserHasCookie(event);

        // Used for TCK tests
        if (isTCK) setTCKUser(portletReq);

        // Handle user login and logout
        if (event.hasAction()) {
            if (event.getAction().getName().equals(SportletProperties.LOGIN)) {
                login(event);
                //event = new GridSphereEventImpl(aclService, context, req, res);
            }
            if (event.getAction().getName().equals(SportletProperties.LOGOUT)) {
                logout(event);
                // since event is now invalidated, must create new one
                event = new GridSphereEventImpl(context, req, res);
            }
        }

        layoutEngine.actionPerformed(event);

        // is this a file download operation?
        downloadFile(event);

        // Handle any outstanding messages
        // This needs work certainly!!!
        Map portletMessageLists = messageManager.retrieveAllMessages();
        if (!portletMessageLists.isEmpty()) {
            Set keys = portletMessageLists.keySet();
            Iterator it = keys.iterator();
            String concPortletID = null;
            List messages = null;
            while (it.hasNext()) {
                concPortletID = (String) it.next();
                messages = (List) portletMessageLists.get(concPortletID);
                Iterator newit = messages.iterator();
                while (newit.hasNext()) {
                    PortletMessage msg = (PortletMessage) newit.next();
                    layoutEngine.messageEvent(concPortletID, msg, event);
                }

            }
            messageManager.removeAllMessages();
        }

        setUserAndGroups(portletReq);
        // Used for TCK tests
        if (isTCK) setTCKUser(portletReq);


        layoutEngine.service(event);

        //log.debug("Session stats");
        //userSessionManager.dumpSessions();

        //log.debug("Portlet service factory stats");
        //factory.logStatistics();

        /*
        log.debug("Portlet page factory stats");
        try {
            PortletPageFactory pageFactory = PortletPageFactory.getInstance();
            pageFactory.logStatistics();
        } catch (Exception e) {
            log.error("Unable to get page factory", e);
        }
        */

    }

    public void setTCKUser(PortletRequest req) {
        //String tck = (String)req.getPortletSession(true).getAttribute("tck");
        String[] portletNames = req.getParameterValues("portletName");
        if ((isTCK) || (portletNames != null)) {
            log.info("Setting a TCK user");
            SportletUserImpl u = new SportletUserImpl();
            u.setUserName("tckuser");
            u.setUserID("tckuser");
            u.setID("500");
            Map l = new HashMap();
            l.put(coreGroup, PortletRole.USER);
            req.setAttribute(SportletProperties.PORTLET_USER, u);
            req.setAttribute(SportletProperties.PORTLETGROUPS, l);
            req.setAttribute(SportletProperties.PORTLET_ROLE, PortletRole.USER);
            isTCK = true;
        }
    }

    public void setUserAndGroups(PortletRequest req) {
        // Retrieve user if there is one
        User user = null;
        if (req.getPortletSession() != null) {
            String uid = (String) req.getPortletSession().getAttribute(SportletProperties.PORTLET_USER);
            if (uid != null) {
                user = userManagerService.getUser(uid);
            }

        }
        HashMap groups = new HashMap();

        PortletRole role = PortletRole.GUEST;
        if (user == null) {
            user = GuestUser.getInstance();
            groups = new HashMap();
            groups.put(coreGroup, PortletRole.GUEST);
        } else {
            List mygroups = aclService.getGroups(user);
            Iterator it = mygroups.iterator();
            while (it.hasNext()) {
                PortletGroup g = (PortletGroup) it.next();
                role = aclService.getRoleInGroup(user, g);
                groups.put(g, role);
            }
        }

        // req.getPortletRole returns the role user has in core gridsphere group
        role = aclService.getRoleInGroup(user, coreGroup);

        // set user, role and groups in request
        req.setAttribute(SportletProperties.PORTLET_GROUP, coreGroup);
        req.setAttribute(SportletProperties.PORTLET_USER, user);
        req.setAttribute(SportletProperties.PORTLETGROUPS, groups);
        req.setAttribute(SportletProperties.PORTLET_ROLE, role);
    }

    protected void checkUserHasCookie(GridSphereEvent event) {
        PortletRequest req = event.getPortletRequest();
        User user = req.getUser();
        if (user instanceof GuestUser) {
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    Cookie c = cookies[i];
                    System.err.println("found a cookie:");
                    System.err.println("name=" + c.getName());
                    System.err.println("value=" + c.getValue());
                    if (c.getName().equals("gsuid")) {

                        String cookieVal = c.getValue();
                        int hashidx = cookieVal.indexOf("#");
                        if (hashidx > 0) {
                            String uid = cookieVal.substring(0, hashidx);

                            System.err.println("uid = " + uid);

                            String reqid = cookieVal.substring(hashidx+1);
                            System.err.println("reqid = " + reqid);

                            GenericRequest genreq = requestService.getRequest(reqid, COOKIE_REQUEST);
                            if (genreq != null) {

                                if (genreq.getUserID().equals(uid)) {
                                    User newuser = userManagerService.getUser(uid);
                                    if (newuser != null) {
                                        setUserSettings(event, newuser);
                                    }
                                }
                            }
                        } 
                    }
                }
            }
        }
    }

    protected void setUserCookie(GridSphereEvent event) {
        PortletRequest req = event.getPortletRequest();
        PortletResponse res = event.getPortletResponse();

        User user = req.getUser();
        GenericRequest request = requestService.createRequest(COOKIE_REQUEST);
        Cookie cookie = new Cookie("gsuid", user.getID() + "#" + request.getOid());
        request.setUserID(user.getID());
        long time = Calendar.getInstance().getTime().getTime() + COOKIE_EXPIRATION_TIME * 1000;
        request.setLifetime(new Date(time));
        requestService.saveRequest(request);

        // COOKIE_EXPIRATION_TIME is specified in secs
        cookie.setMaxAge(COOKIE_EXPIRATION_TIME);
        res.addCookie(cookie);
        System.err.println("adding a  cookie");
    }

    protected void removeUserCookie(GridSphereEvent event) {
        PortletRequest req = event.getPortletRequest();
        PortletResponse res = event.getPortletResponse();
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie c = cookies[i];
                if (c.getName().equals("gsuid")) {
                    int idx = c.getValue().indexOf("#");
                    if (idx > 0) {
                        String reqid = c.getValue().substring(idx+1);
                        System.err.println("reqid= " + reqid);
                        GenericRequest request = requestService.getRequest(reqid, COOKIE_REQUEST);
                        if (request != null) requestService.deleteRequest(request);
                    }
                    c.setMaxAge(0);
                    res.addCookie(c);
                }
            }
        }

    }

    /**
     * Handles login requests
     *
     * @param event a <code>GridSphereEvent</code>
     */
    protected void login(GridSphereEvent event) {
        log.debug("in login of GridSphere Servlet");

        String LOGIN_ERROR_FLAG = "LOGIN_FAILED";
        PortletRequest req = event.getPortletRequest();


        try {
            User user = loginService.login(req);

            setUserSettings(event, user);

            String remme = req.getParameter("remlogin");
            if (remme != null) {
                setUserCookie(event);
            } else {
                removeUserCookie(event);
            }

        } catch (AuthorizationException err) {
            log.debug(err.getMessage());
            req.setAttribute(LOGIN_ERROR_FLAG, err.getMessage());
        } catch (AuthenticationException err) {
            log.debug(err.getMessage());
            req.setAttribute(LOGIN_ERROR_FLAG, err.getMessage());
        }
    }

    public void setUserSettings(GridSphereEvent event, User user) {
        PortletRequest req = event.getPortletRequest();
        PortletSession session = req.getPortletSession(true);

        req.setAttribute(SportletProperties.PORTLET_USER, user);
        session.setAttribute(SportletProperties.PORTLET_USER, user.getID());
        if (user.getAttribute(User.LOCALE) != null) {
            session.setAttribute(User.LOCALE, new Locale((String)user.getAttribute(User.LOCALE), "", ""));
        }
        if (aclService.hasSuperRole(user)) {
            log.debug("User: " + user.getUserName() + " logged in as SUPER");
        }
        setUserAndGroups(req);
        log.debug("Adding User: " + user.getID() + " with session:" + session.getId() + " to usersessionmanager");
        userSessionManager.addSession(user, session);
        layoutEngine.loginPortlets(event);
    }

    /**
     * Handles logout requests
     *
     * @param event a <code>GridSphereEvent</code>
     */
    protected void logout(GridSphereEvent event) {
        log.debug("in logout of GridSphere Servlet");
        PortletRequest req = event.getPortletRequest();
        removeUserCookie(event);
        PortletSession session = req.getPortletSession();
        session.removeAttribute(SportletProperties.PORTLET_USER);
        userSessionManager.removeSessions(req.getUser());
        layoutEngine.logoutPortlets(event);
    }

    /**
     * Method to set the response headers to perform file downloads to a browser
     *
     * @param event the gridsphere event
     * @throws PortletException
     */
    public void downloadFile(GridSphereEvent event) throws PortletException {
        PortletResponse res = event.getPortletResponse();
        PortletRequest req = event.getPortletRequest();
        try {
            String fileName = (String) req.getPortletSession(true).getAttribute(SportletProperties.FILE_DOWNLOAD_NAME);
            String path = (String) req.getPortletSession(true).getAttribute(SportletProperties.FILE_DOWNLOAD_PATH);
            Boolean deleteFile = (Boolean)req.getPortletSession(true).getAttribute(SportletProperties.FILE_DELETE);
            if ((fileName == null) || (path == null)) return;
            log.debug("in downloadFile");
            log.debug("filename: " + fileName + " filepath= " + path);
            File f = new File(path);

            FileDataSource fds = new FileDataSource(f);
            res.setContentType(fds.getContentType());
            res.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            res.setHeader("Content-Length", String.valueOf(f.length()));
            DataHandler handler = new DataHandler(fds);
            handler.writeTo(res.getOutputStream());
            if (deleteFile.booleanValue()) {
                f.delete();
            }
        } catch (FileNotFoundException e) {
            log.error("Unable to find file!", e);
        } catch (SecurityException e) {
            // this gets thrown if a security policy applies to the file. see java.io.File for details.
            log.error("A security exception occured!", e);
        } catch (SocketException e) {
            log.error("Caught SocketException: " + e.getMessage());    
        } catch (IOException e) {
            log.error("Caught IOException", e);
            //response.sendError(HttpServletResponse.SC_INTERNAL_SERVER,e.getMessage());
        } finally {
            req.getPortletSession(true).removeAttribute(SportletProperties.FILE_DOWNLOAD_NAME);
            req.getPortletSession(true).removeAttribute(SportletProperties.FILE_DOWNLOAD_PATH);
            req.getPortletSession(true).removeAttribute(SportletProperties.FILE_DELETE);
        }
    }

    /**
     * @see #doGet
     */
    public final void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doGet(req, res);
    }

    /**
     * Return the servlet info.
     *
     * @return a string with the servlet information.
     */
    public final String getServletInfo() {
        return "GridSphere Servlet 2.0.1";
    }

    /**
     * Shuts down the GridSphere portlet container
     */
    public final void destroy() {
        log.debug("in destroy: Shutting down services");
        userSessionManager.destroy();
        layoutEngine.destroy();
        // Shutdown services
        factory.shutdownServices();
        // shutdown the persistencemanagers
        PersistenceManagerFactory.shutdown();
        System.gc();
    }

    /**
     * Record the fact that a servlet context attribute was added.
     *
     * @param event The session attribute event
     */
    public void attributeAdded(HttpSessionBindingEvent event) {

        log.debug("attributeAdded('" + event.getSession().getId() + "', '" +
                event.getName() + "', '" + event.getValue() + "')");

    }


    /**
     * Record the fact that a servlet context attribute was removed.
     *
     * @param event The session attribute event
     */
    public void attributeRemoved(HttpSessionBindingEvent event) {

        log.debug("attributeRemoved('" + event.getSession().getId() + "', '" +
                event.getName() + "', '" + event.getValue() + "')");

    }


    /**
     * Record the fact that a servlet context attribute was replaced.
     *
     * @param event The session attribute event
     */
    public void attributeReplaced(HttpSessionBindingEvent event) {

        log.debug("attributeReplaced('" + event.getSession().getId() + "', '" +
                event.getName() + "', '" + event.getValue() + "')");

    }


    /**
     * Record the fact that this ui application has been destroyed.
     *
     * @param event The servlet context event
     */
    public void contextDestroyed(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        log.debug("contextDestroyed()");
        log.debug("contextName: " + ctx.getServletContextName());
        log.debug("context path: " + ctx.getRealPath(""));

    }


    /**
     * Record the fact that this ui application has been initialized.
     *
     * @param event The servlet context event
     */
    public void contextInitialized(ServletContextEvent event) {
        System.err.println("contextInitialized()");
        ServletContext ctx = event.getServletContext();
        GridSphereConfig.setServletContext(ctx);        
        log.debug("contextName: " + ctx.getServletContextName());
        log.debug("context path: " + ctx.getRealPath(""));

    }

    /**
     * Record the fact that a session has been created.
     *
     * @param event The session event
     */
    public void sessionCreated(HttpSessionEvent event) {
        log.debug("sessionCreated('" + event.getSession().getId() + "')");
        sessionManager.sessionCreated(event);
    }


    /**
     * Record the fact that a session has been destroyed.
     *
     * @param event The session event
     */
    public void sessionDestroyed(HttpSessionEvent event) {
        sessionManager.sessionDestroyed(event);
        log.debug("sessionDestroyed('" + event.getSession().getId() + "')");
    }

    /**
     * Record the fact that a session has been created.
     *
     * @param event The session event
     */
    public void sessionDidActivate(HttpSessionEvent event) {
        log.debug("sessionDidActivate('" + event.getSession().getId() + "')");
        sessionManager.sessionCreated(event);
    }


    /**
     * Record the fact that a session has been destroyed.
     *
     * @param event The session event
     */
    public void sessionWillPassivate(HttpSessionEvent event) {
        sessionManager.sessionDestroyed(event);
        log.debug("sessionWillPassivate('" + event.getSession().getId() + "')");
    }

}
