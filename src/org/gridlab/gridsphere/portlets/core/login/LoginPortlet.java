/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portlets.core.login;

import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.portlet.service.PortletServiceException;
import org.gridlab.gridsphere.provider.event.FormEvent;
import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
import org.gridlab.gridsphere.provider.portletui.beans.CheckBoxBean;
import org.gridlab.gridsphere.provider.portletui.beans.FrameBean;
import org.gridlab.gridsphere.provider.portletui.beans.TextFieldBean;
import org.gridlab.gridsphere.provider.portletui.beans.HiddenFieldBean;
import org.gridlab.gridsphere.services.core.security.auth.modules.LoginAuthModule;
import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
import org.gridlab.gridsphere.services.core.security.acl.GroupRequest;
import org.gridlab.gridsphere.services.core.security.password.InvalidPasswordException;
import org.gridlab.gridsphere.services.core.security.password.PasswordManagerService;
import org.gridlab.gridsphere.services.core.user.LoginService;
import org.gridlab.gridsphere.services.core.user.UserManagerService;
import org.gridlab.gridsphere.services.core.user.AccountRequest;

import javax.servlet.UnavailableException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LoginPortlet extends ActionPortlet {

    public static final String LOGIN_ERROR_FLAG = "LOGIN_FAILED";
    public static final Integer LOGIN_ERROR_UNKNOWN = new Integer(-1);

    public static final String DO_VIEW_USER_EDIT_LOGIN = "login/createaccount.jsp"; //edit user
    public static final String DO_CONFIGURE = "login/config.jsp"; //configure login 

    private boolean canUserCreateAccount = true;

    private UserManagerService userManagerService = null;
    private AccessControlManagerService aclService = null;
    private PasswordManagerService passwordManagerService = null;

    public void init(PortletConfig config) throws UnavailableException {
        super.init(config);
        try {
            userManagerService = (UserManagerService)getPortletConfig().getContext().getService(UserManagerService.class);
            aclService = (AccessControlManagerService)getPortletConfig().getContext().getService(AccessControlManagerService.class);
            passwordManagerService = (PasswordManagerService)getPortletConfig().getContext().getService(PasswordManagerService.class);
            String createAllowed = config.getInitParameter("canUserCreateAccount");
            if (createAllowed != null) {
                    if (createAllowed.equalsIgnoreCase("false")) {
                            canUserCreateAccount = false;
                    }
                    if (createAllowed.equalsIgnoreCase("true")) {
                            canUserCreateAccount = true;
                    }
            }
        } catch (PortletServiceException e) {
            throw new UnavailableException("Unable to initialize services");
        }
        DEFAULT_VIEW_PAGE = "doViewUser";
        DEFAULT_CONFIGURE_PAGE = "showConfigure";
    }

    public void initConcrete(PortletSettings settings) throws UnavailableException {
        super.initConcrete(settings);
    }

    public void doViewUser(FormEvent event) throws PortletException {
        log.debug("in LoginPortlet: doViewUser");
        PortletRequest request = event.getPortletRequest();
        User user = request.getUser();
        request.setAttribute("user", user);
        if (user instanceof GuestUser) {
            request.setAttribute("canUserCreateAcct", new Boolean(canUserCreateAccount));
            setNextState(request, "login/login.jsp");
        } else {
            showConfigure(event);
        }
    }


    public void doTitle(PortletRequest request, PortletResponse response) throws PortletException, IOException {
        User user = request.getUser();
        PrintWriter out = response.getWriter();

        if (user instanceof GuestUser) {
            out.println(getNextTitle(request));
        } else {
            out.println(getLocalizedText(request, "LOGIN_CONFIGURE"));
        }
    }

    public void gs_login(FormEvent event) throws PortletException {
        log.debug("in LoginPortlet: gs_login");
        PortletRequest req = event.getPortletRequest();

        String errorKey = (String)req.getAttribute(LoginPortlet.LOGIN_ERROR_FLAG);

        if (errorKey != null) {
            FrameBean frame = event.getFrameBean("errorFrame");
            frame.setKey(LoginPortlet.LOGIN_ERROR_FLAG);
            frame.setStyle("error");
        }
        setNextState(req, "doViewUser");
    }

    public void doNewUser(FormEvent evt)
            throws PortletException {
        if (!canUserCreateAccount) return;

        PortletRequest req = evt.getPortletRequest();
        setNextState(req, DO_VIEW_USER_EDIT_LOGIN);
        log.debug("in doViewNewUser");
    }

    public void doConfirmEditUser(FormEvent evt)
            throws PortletException {
        PortletRequest req = evt.getPortletRequest();
        //User user = loadUser(evt);
        if (!canUserCreateAccount) return;

        try {
            //check if the user is new or not
            validateUser(evt);
            //new and valid user and will save it
            User user = saveUser(evt);
            //show the data of this user
            FrameBean frame = evt.getFrameBean("errorFrame");
            frame.setValue(this.getLocalizedText(req, "USER_NEW_ACCOUNT") +
                    "<br>" + this.getLocalizedText(req, "USER_PLEASE_LOGIN") +
                    " " + user.getUserName());
            frame.setStyle("success");
            setNextState(req, "doViewUser");
        } catch (PortletException e) {
            //invalid user, an exception was thrown
            FrameBean err = evt.getFrameBean("errorFrame");
            err.setValue(e.getMessage());
            err.setStyle("error");
            //back to edit
            setNextState(req, DO_VIEW_USER_EDIT_LOGIN);
        }
    }

    public void doCancelEditUser(FormEvent evt)
            throws PortletException {
        PortletRequest req = evt.getPortletRequest();
        setNextState(req, "doViewUser");
    }

    private void validateUser(FormEvent event)
            throws PortletException {
        log.debug("Entering validateUser()");
        PortletRequest req = event.getPortletRequest();
        StringBuffer message = new StringBuffer();
        boolean isInvalid = false;
        // Validate user name
        String userName = event.getTextFieldBean("userName").getValue();
        if (userName.equals("")) {
            message.append(this.getLocalizedText(req, "USER_NAME_BLANK") + "<br>");
            isInvalid = true;
        }

        if (this.userManagerService.existsUserName(userName)) {
            message.append(this.getLocalizedText(req, "USER_EXISTS") + "<br>");
            isInvalid = true;
        }


        // Validate family name
        String familyName = event.getTextFieldBean("familyName").getValue();
        if (familyName.equals("")) {
            message.append(this.getLocalizedText(req, "USER_FAMILYNAME_BLANK") + "<br>");
            isInvalid = true;
        }
        // Validate given name
        String givenName = event.getTextFieldBean("givenName").getValue();
        if (givenName.equals("")) {
            message.append(this.getLocalizedText(req, "USER_GIVENNAME_BLANK") + "<br>");
            isInvalid = true;
        }

        // Validate e-mail
        String eMail = event.getTextFieldBean("emailAddress").getValue();
        if (eMail.equals("")) {
            message.append(this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br>");
            isInvalid = true;
        } else if ((eMail.indexOf("@") < 0)) {
            message.append(this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br>");
            isInvalid = true;
        } else if ((eMail.indexOf(".") < 0)) {
            message.append(this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br>");
            isInvalid = true;
        }

        //Validate password
        if (!isInvalid) {
            isInvalid = isInvalidPassword(event, message);
        }
        // Throw exception if error was found
        if (isInvalid){
            throw new PortletException(message.toString());
        }
        log.debug("Exiting validateUser()");
    }

    private boolean isInvalidPassword(FormEvent event, StringBuffer message)
    {
        PortletRequest req = event.getPortletRequest();
        // Validate password
        String passwordValue = event.getPasswordBean("password").getValue();
        String confirmPasswordValue = event.getPasswordBean("confirmPassword").getValue();

        // If user already exists and password unchanged, no problem
        if (passwordValue.length() == 0 ||
                confirmPasswordValue.length() == 0) {
            message.append(this.getLocalizedText(req, "USER_PASSWORD_BLANK") + "<br>");
            return true;
        }
        // Otherwise, password must match confirmation
        if (!passwordValue.equals(confirmPasswordValue)) {
            message.append(this.getLocalizedText(req, "USER_PASSWORD_MISMATCH") + "<br>");
            return true;
            // If they do match, then validate password with our service
        } else {
            try {
                this.passwordManagerService.validatePassword(passwordValue);
            } catch (InvalidPasswordException e) {
                message.append(e.getMessage());
                return true;
            }
        }
        return false;
    }

    private User saveUser(FormEvent event)
            throws PortletException {
        log.debug("Entering saveUser()");
        // Account request
        AccountRequest accountRequest = null;

        // Create edit account request

        accountRequest = this.userManagerService.createAccountRequest();


        // Edit account attributes
        editAccountRequest(event, accountRequest);
        // Submit changes
        this.userManagerService.submitAccountRequest(accountRequest);
        User newuser = this.userManagerService.approveAccountRequest(accountRequest);
        // Save user role
        saveUserRole(newuser);
        log.debug("Exiting saveUser()");
        return newuser;
    }

    private void editAccountRequest(FormEvent event, AccountRequest accountRequest) {
        log.debug("Entering editAccountRequest()");
        accountRequest.setUserName(event.getTextFieldBean("userName").getValue());
        accountRequest.setFamilyName(event.getTextFieldBean("familyName").getValue());
        accountRequest.setGivenName(event.getTextFieldBean("givenName").getValue());
        accountRequest.setFullName(event.getTextFieldBean("fullName").getValue());
        accountRequest.setEmailAddress(event.getTextFieldBean("emailAddress").getValue());
        accountRequest.setOrganization(event.getTextFieldBean("organization").getValue());
        String passwordValue = event.getPasswordBean("password").getValue();
        // Save password parameters if password was altered
        if (passwordValue.length() > 0) {
            accountRequest.setPasswordValue(passwordValue);
        }
        log.debug("Exiting editAccountRequest()");
    }

    private void saveUserRole(User user)
            throws PortletException {
        log.debug("Entering saveUserRole()");

        // Revoke super role (in case they had it)
        //this.aclService.revokeSuperRole(user);
        // Create appropriate access request
        GroupRequest groupRequest = this.aclService.createGroupRequest();
        groupRequest.setUser(user);
        groupRequest.setGroup(PortletGroupFactory.GRIDSPHERE_GROUP);
        groupRequest.setRole(PortletRole.USER);

        // Submit changes
        this.aclService.submitGroupRequest(groupRequest);
        this.aclService.approveGroupRequest(groupRequest);
    }

    public void showConfigure(FormEvent event) {
        PortletRequest req = event.getPortletRequest();
        CheckBoxBean acctCB = event.getCheckBoxBean("acctCB");
        acctCB.setSelected(canUserCreateAccount);
        setNextState(req, DO_CONFIGURE);
    }

    public void setUserCreateAccount(FormEvent event) {
        CheckBoxBean acctCB = event.getCheckBoxBean("acctCB");
        String useracct = acctCB.getSelectedValue();
        if (useracct != null) {
            canUserCreateAccount = true;
        } else {
            canUserCreateAccount = false;
        }
        showConfigure(event);
    }
}
