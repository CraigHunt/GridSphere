/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: LoginService.java 5032 2006-08-17 18:15:06Z novotny $
 */
package org.gridsphere.services.core.user;

import org.gridsphere.portlet.User;
import org.gridsphere.portlet.PortletRequest;
import org.gridsphere.portlet.service.PortletService;
import org.gridsphere.services.core.security.auth.AuthorizationException;
import org.gridsphere.services.core.security.auth.AuthenticationException;
import org.gridsphere.services.core.security.auth.modules.LoginAuthModule;
import java.util.List;

/**
 * The <code>LoginService</code> is the primary interface that defines the login method used to obtain a
 * <code>User</code> from a username and password. The <code>LoginService</code> is configured
 * dynamically at run-time with login authorization modules. By default the PASSWORD_AUTH_MODULE is
 * selected which uses the GridSphere database to store passwords. Other authorization modules
 * can use external directory servers such as LDAP, etc
 */
public interface LoginService extends PortletService {

    public void loadAuthModules(String authModsPath, ClassLoader classloader);

    public List<LoginAuthModule> getActiveAuthModules();

    public List<LoginAuthModule> getAuthModules();

    public LoginUserModule getActiveLoginModule();

    /**
     * Login a user. Implementation
     * obtains username and password from supplied PortletRequest
     *
     * @param req the portlet request
     *
     * @return User The associated user.
     * @throws org.gridsphere.services.core.security.auth.AuthorizationException
     *          If login unsuccessful
     */
    public User login(PortletRequest req)
            throws AuthenticationException, AuthorizationException;

    public void saveAuthModule(LoginAuthModule authModule);

}
