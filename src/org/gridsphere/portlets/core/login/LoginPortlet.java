/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: LoginPortlet.java 5087 2006-08-18 22:52:23Z novotny $
 */
package org.gridsphere.portlets.core.login;

import com.octo.captcha.service.CaptchaServiceException;
import org.gridsphere.services.core.user.UserManagerService;
import org.gridsphere.services.core.portal.PortalConfigService;
import org.gridsphere.services.core.security.role.RoleManagerService;
import org.gridsphere.services.core.security.role.PortletRole;
import org.gridsphere.services.core.security.password.PasswordManagerService;
import org.gridsphere.services.core.security.password.PasswordEditor;
import org.gridsphere.services.core.messaging.TextMessagingService;
import org.gridsphere.services.core.request.RequestService;
import org.gridsphere.services.core.request.GenericRequest;
import org.gridsphere.services.core.captcha.impl.CaptchaServiceSingleton;
import org.gridsphere.provider.portlet.jsr.ActionPortlet;
import org.gridsphere.provider.event.jsr.RenderFormEvent;
import org.gridsphere.provider.event.jsr.ActionFormEvent;
import org.gridsphere.provider.portletui.beans.MessageBoxBean;
import org.gridsphere.provider.portletui.beans.PasswordBean;
import org.gridsphere.provider.portletui.beans.TextFieldBean;
import org.gridsphere.provider.portletui.beans.HiddenFieldBean;
import org.gridsphere.tmf.TextMessagingException;
import org.gridsphere.tmf.message.MailMessage;
import org.gridsphere.portlet.User;
import org.gridsphere.portlet.jsrimpl.PortletURLImpl;
import org.gridsphere.portlet.impl.SportletProperties;

import javax.portlet.*;
import java.security.cert.X509Certificate;
import java.util.*;

public class LoginPortlet extends ActionPortlet {

    private static String FORGOT_PASSWORD_LABEL = "forgotpassword";
    private static String ACTIVATE_ACCOUNT_LABEL = "activateaccount";
    private static String LOGIN_NUMTRIES = "ACCOUNT_NUMTRIES";
    private static String ADMIN_ACCOUNT_APPROVAL = "ADMIN_ACCOUNT_APPROVAL";
    private static String LOGIN_NAME = "LOGIN_NAME";
    public static String SAVE_PASSWORDS = "SAVE_PASSWORDS";
    public static String REMEMBER_USER = "REMEMBER_USER";
    public static String SUPPORT_X509_AUTH = "SUPPORT_X509_AUTH";
    public static String SEND_USER_FORGET_PASSWORD = "SEND_USER_FORGET_PASSWD";

    private static long REQUEST_LIFETIME = 1000 * 60 * 24 * 3; // 3 days

    public static final String LOGIN_ERROR_FLAG = "LOGIN_FAILED";
    public static final Integer LOGIN_ERROR_UNKNOWN = new Integer(-1);

    public static final String DO_VIEW_USER_EDIT_LOGIN = "login/createaccount.jsp"; //edit user
    public static final String DO_FORGOT_PASSWORD = "login/forgotpassword.jsp";
    public static final String DO_NEW_PASSWORD = "login/newpassword.jsp";

    private UserManagerService userManagerService = null;
    private RoleManagerService roleService = null;
    private PasswordManagerService passwordManagerService = null;
    private PortalConfigService portalConfigService = null;
    private RequestService requestService = null;
    private TextMessagingService tms = null;

    private String  notificationURL = null;
    private String newpasswordURL = null;
    private String activateAccountURL = null;
    private String denyAccountURL = null;

    public void init(PortletConfig config) throws PortletException {

        super.init(config);

        userManagerService = (UserManagerService) createPortletService(UserManagerService.class);
        roleService = (RoleManagerService) createPortletService(RoleManagerService.class);
        passwordManagerService = (PasswordManagerService) createPortletService(PasswordManagerService.class);
        requestService = (RequestService) createPortletService(RequestService.class);
        tms = (TextMessagingService) createPortletService(TextMessagingService.class);
        portalConfigService = (PortalConfigService) createPortletService(PortalConfigService.class);
        DEFAULT_VIEW_PAGE = "doViewUser";
    }

    public void doViewUser(RenderFormEvent event) throws PortletException {
        log.debug("in LoginPortlet: doViewUser");
        PortletRequest request = event.getRenderRequest();
        RenderResponse response = event.getRenderResponse();
        if (notificationURL == null) notificationURL = response.createActionURL().toString();

        if (newpasswordURL == null) {
            PortletURL url = response.createActionURL();
            ((PortletURLImpl)url).setAction("newpassword");
            newpasswordURL = url.toString();
        }

        if (activateAccountURL == null) {
            PortletURL url = response.createActionURL();
            ((PortletURLImpl)url).setAction("approveAccount");
            activateAccountURL = url.toString();
        }
        if (denyAccountURL == null) {
            PortletURL url = response.createActionURL();
            ((PortletURLImpl)url).setAction("denyAccount");
            denyAccountURL = url.toString();
        }
        PasswordBean pass = event.getPasswordBean("password");
        pass.setValue("");

        // Check certificates
        String x509supported = portalConfigService.getProperty("SUPPORT_X509_AUTH");
        if ((x509supported != null) && (x509supported.equalsIgnoreCase("true"))) {
            X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
            if (certs != null && certs.length > 0) {
                request.setAttribute("certificate", certs[0].getSubjectDN().toString());
            }
        }

        String remUser = portalConfigService.getProperty(REMEMBER_USER);
        if ((remUser != null) && (remUser.equalsIgnoreCase("TRUE"))) {
            request.setAttribute("remUser", "true");
        }

        Boolean useSecureLogin = Boolean.valueOf(portalConfigService.getProperty("use.https.login"));

        request.setAttribute("useSecureLogin", useSecureLogin.toString());
        boolean canUserCreateAccount = Boolean.valueOf(portalConfigService.getProperty("CAN_USER_CREATE_ACCOUNT")).booleanValue();
        if (canUserCreateAccount) request.setAttribute("canUserCreateAcct", "true");
        boolean dispUser = Boolean.valueOf(portalConfigService.getProperty(SEND_USER_FORGET_PASSWORD)).booleanValue();
        if (dispUser) request.setAttribute("dispPass", "true");
        setNextState(request, "login/login.jsp");
    }

    public void doCancel(ActionFormEvent event) throws PortletException {
        setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
    }

    public void gs_login(ActionFormEvent event) throws PortletException {
        log.debug("in LoginPortlet: gs_login");
        PortletRequest req = event.getActionRequest();

        String errorMsg = (String) req.getAttribute(LoginPortlet.LOGIN_ERROR_FLAG);

        if (errorMsg != null) {
            Integer numTries = (Integer) req.getPortletSession(true).getAttribute(LoginPortlet.LOGIN_NUMTRIES);
            String loginname = (String) req.getPortletSession(true).getAttribute(LoginPortlet.LOGIN_NAME);
            int i = 1;
            if (numTries != null) {
                i = numTries.intValue();
                i++;
            }
            numTries = new Integer(i);
            req.getPortletSession(true).setAttribute(LoginPortlet.LOGIN_NUMTRIES, numTries);
            req.getPortletSession(true).setAttribute(LoginPortlet.LOGIN_NAME, req.getParameter("username"));
            System.err.println("num tries = " + i);
            // tried one to many times using same name

            int defaultNumTries = Integer.valueOf(portalConfigService.getProperty(LOGIN_NUMTRIES)).intValue();

            if (req.getParameter("username") != null && req.getParameter("username").equals(loginname)) {
                if ((i >= defaultNumTries) && (defaultNumTries != -1)) {
                    disableAccount(event);
                    errorMsg = this.getLocalizedText(req, "LOGIN_TOOMANY_ATTEMPTS");
                    req.getPortletSession(true).removeAttribute(LoginPortlet.LOGIN_NUMTRIES);
                    req.getPortletSession(true).removeAttribute(LoginPortlet.LOGIN_NAME);
                }
            }
            createErrorMessage(event, errorMsg);
            req.removeAttribute(LoginPortlet.LOGIN_ERROR_FLAG);
        }

        setNextState(req, "doViewUser");
    }

    public void disableAccount(ActionFormEvent event) {
        PortletRequest req = event.getActionRequest();
        String loginName = req.getParameter("username");
        User user = userManagerService.getUserByUserName(loginName);
        if (user != null) {
            user.setAttribute(User.DISABLED, "true");
            userManagerService.saveUser(user);

            MailMessage mailToUser = tms.getMailMessage();
            StringBuffer body = new StringBuffer();
            body.append(getLocalizedText(req, "LOGIN_DISABLED_MSG1") + " " + getLocalizedText(req, "LOGIN_DISABLED_MSG2") + "\n\n");
            mailToUser.setBody(body.toString());
            mailToUser.setSubject(getLocalizedText(req, "LOGIN_DISABLED_SUBJECT"));
            mailToUser.setTo(user.getEmailAddress());
            mailToUser.setServiceid("mail");

            MailMessage mailToAdmin = tms.getMailMessage();
            StringBuffer body2 = new StringBuffer();
            body2.append(getLocalizedText(req, "LOGIN_DISABLED_ADMIN_MSG") + " " + user.getUserName());
            mailToAdmin.setBody(body2.toString());
            mailToAdmin.setSubject(getLocalizedText(req, "LOGIN_DISABLED_SUBJECT") + " " + user.getUserName());
            mailToAdmin.setTo(tms.getServiceUserID("mail", "root"));
            mailToUser.setServiceid("mail");


            try {
                tms.send(mailToUser);
                tms.send(mailToAdmin);
            } catch (TextMessagingException e) {
                log.error("Unable to send mail message!", e);
                createErrorMessage(event, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
                return;
            }
        }
    }

    public void doNewUser(ActionFormEvent evt)
            throws PortletException {

        boolean canUserCreateAccount = Boolean.valueOf(portalConfigService.getProperty("CAN_USER_CREATE_ACCOUNT")).booleanValue();
        if (!canUserCreateAccount) return;

        ActionRequest req = evt.getActionRequest();

        MessageBoxBean msg = evt.getMessageBoxBean("msg");

        String savePasswds = portalConfigService.getProperty("SAVE_PASSWORDS");
        if (savePasswds.equals(Boolean.TRUE.toString())) {
            req.setAttribute("savePass", "true");
        }
        String adminApproval = portalConfigService.getProperty("ADMIN_ACCOUNT_APPROVAL");
        if (adminApproval.equals(Boolean.TRUE.toString())) {
            msg.setKey("LOGIN_ACCOUNT_CREATE_APPROVAL");
        } else {
            msg.setKey("LOGIN_CREATE_ACCT");
        }

        setNextState(req, DO_VIEW_USER_EDIT_LOGIN);
        log.debug("in doViewNewUser");
    }

    public void doConfirmEditUser(ActionFormEvent evt)
            throws PortletException {
        PortletRequest req = evt.getActionRequest();

        String savePasswds = portalConfigService.getProperty("SAVE_PASSWORDS");
        if (savePasswds.equals(Boolean.TRUE.toString())) {
            req.setAttribute("savePass", "true");
        }

        boolean canUserCreateAccount = Boolean.valueOf(portalConfigService.getProperty("CAN_USER_CREATE_ACCOUNT")).booleanValue();
        if (!canUserCreateAccount) return;

        try {
            //check if the user is new or not
            validateUser(evt);
            //new and valid user and will save it
            notifyNewUser(evt);

            setNextState(req, "doViewUser");
        } catch (PortletException e) {
            //invalid user, an exception was thrown
            //back to edit
            log.error("Could not creat account: ", e);
            setNextState(req, DO_VIEW_USER_EDIT_LOGIN);
        }
    }

    private void validateUser(ActionFormEvent event)
            throws PortletException {
        log.debug("Entering validateUser()");
        PortletRequest req = event.getActionRequest();
        StringBuffer message = new StringBuffer();

        // Validate user name
        String userName = event.getTextFieldBean("userName").getValue();
        if (userName.equals("")) {
            createErrorMessage(event, this.getLocalizedText(req, "USER_NAME_BLANK") + "<br />");
            throw new PortletException("user name is blank!");
        }

        if (this.userManagerService.existsUserName(userName)) {
            createErrorMessage(event, this.getLocalizedText(req, "USER_EXISTS") + "<br />");
            throw new PortletException("user exists already");
        }

        // Validate full name

        String familyName = event.getTextFieldBean("fullName").getValue();
        if (familyName.equals("")) {
            createErrorMessage(event, this.getLocalizedText(req, "USER_FULLNAME_BLANK") + "<br />");
            throw new PortletException("full name is blank");
        }

        // Validate e-mail
        String eMail = event.getTextFieldBean("emailAddress").getValue();
        if (eMail.equals("")) {
            createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
            throw new PortletException("email is blank");
        } else if ((eMail.indexOf("@") < 0)) {
            createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
            throw new PortletException("email address invalid");
        } else if ((eMail.indexOf(".") < 0)) {
            createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
            throw new PortletException("email address invalid");
        }

        //Validate password

        String savePasswds = portalConfigService.getProperty(SAVE_PASSWORDS);
        if (savePasswds.equals(Boolean.TRUE.toString())) {
            if (isInvalidPassword(event)) throw new PortletException("password no good!");
        }


        //remenber that we need an id to validate!
        String captchaId = req.getPortletSession().getId();
        //retrieve the response
        String response = event.getTextFieldBean("captchaTF").getValue();
        // Call the Service method
        boolean isInvalid = false;
        try {
            isInvalid = CaptchaServiceSingleton.getInstance().validateResponseForID(captchaId, response);
        } catch (CaptchaServiceException e) {
            //should not happen, may be thrown if the id is not valid
        }
        if (!isInvalid) {
            createErrorMessage(event, this.getLocalizedText(req, "USER_CAPTCHA_MISMATCH"));
            throw new PortletException("captcha challenge mismatch!");
        }

        log.debug("Exiting validateUser()");
    }

    private boolean isInvalidPassword(ActionFormEvent event) {
        PortletRequest req = event.getActionRequest();
        // Validate password
        String passwordValue = event.getPasswordBean("password").getValue();
        String confirmPasswordValue = event.getPasswordBean("confirmPassword").getValue();

        if (passwordValue == null) {
            createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_NOTSET"));
            return true;
        }

        // Otherwise, password must match confirmation
        if (!passwordValue.equals(confirmPasswordValue)) {
            createErrorMessage(event, (this.getLocalizedText(req, "USER_PASSWORD_MISMATCH")) + "<br />");
            return true;
            // If they do match, then validate password with our service
        } else {
            passwordValue = passwordValue.trim();
            if (passwordValue.length() == 0) {
                createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_BLANK"));
                return true;
            }
            if (passwordValue.length() < 5) {
                createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_TOOSHORT"));
                return true;
            }
        }
        return false;
    }

    private User saveUser(GenericRequest request) {
        log.debug("Entering saveUser()");
        // Account request

        // Create edit account request

        User newuser = this.userManagerService.createUser();

        // Edit account attributes
        newuser.setUserName(request.getAttribute("userName"));
        newuser.setFullName(request.getAttribute("fullName"));
        newuser.setEmailAddress(request.getAttribute("emailAddress"));
        newuser.setOrganization(request.getAttribute("organization"));

        // Submit changes
        this.userManagerService.saveUser(newuser);

        String savePasswds = portalConfigService.getProperty("SAVE_PASSWORDS");
        if (savePasswds.equals(Boolean.TRUE.toString())) {
            PasswordEditor editor = passwordManagerService.editPassword(newuser);
            String password = request.getAttribute("password");

            editor.setValue(password);
            passwordManagerService.saveHashedPassword(editor);
        }

        // Save user role
        saveUserRole(newuser);
        log.debug("Exiting saveUser()");
        return newuser;
    }

    private void saveUserRole(User user) {
        log.debug("Entering saveUserRole()");
        roleService.addUserToRole(user, PortletRole.USER);
    }




    public void displayForgotPassword(ActionFormEvent event) {
        boolean sendMail = Boolean.valueOf(portalConfigService.getProperty(SEND_USER_FORGET_PASSWORD)).booleanValue();
        if (sendMail) {
            PortletRequest req = event.getActionRequest();
            setNextState(req, DO_FORGOT_PASSWORD);
        }
    }

    public void notifyUser(ActionFormEvent evt) {
        PortletRequest req = evt.getActionRequest();

        User user;
        TextFieldBean emailTF = evt.getTextFieldBean("emailTF");

        if (emailTF.getValue().equals("")) {
            createErrorMessage(evt, this.getLocalizedText(req, "LOGIN_NO_EMAIL"));
            return;
        } else {
            user = userManagerService.getUserByEmail(emailTF.getValue());
        }
        if (user == null) {
            createErrorMessage(evt, this.getLocalizedText(req, "LOGIN_NOEXIST"));
            return;
        }

        // create a request
        GenericRequest request = requestService.createRequest(FORGOT_PASSWORD_LABEL);
        long now = Calendar.getInstance().getTime().getTime();

        request.setLifetime(new Date(now + REQUEST_LIFETIME));
        request.setUserID(user.getID());
        requestService.saveRequest(request);

        org.gridsphere.tmf.message.MailMessage mailToUser = tms.getMailMessage();
        mailToUser.setTo(emailTF.getValue());
        String subjectHeader = portalConfigService.getProperty("LOGIN_FORGOT_SUBJECT");
        if (subjectHeader == null) subjectHeader = getLocalizedText(req, "MAIL_SUBJECT_HEADER");
        mailToUser.setSubject(subjectHeader);
        StringBuffer body = new StringBuffer();

        String forgotMail = portalConfigService.getProperty("LOGIN_FORGOT_BODY");
        if (forgotMail == null) forgotMail = getLocalizedText(req, "LOGIN_FORGOT_MAIL");
        body.append(forgotMail + "\n\n");
        
        body.append(newpasswordURL + "&reqid=" + request.getOid());
        mailToUser.setBody(body.toString());
        mailToUser.setServiceid("mail");

        try {
            tms.send(mailToUser);
        } catch (TextMessagingException e) {
            log.error("Unable to send mail message!", e);
            createErrorMessage(evt, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
            return;
        }
        createSuccessMessage(evt, this.getLocalizedText(req, "LOGIN_SUCCESS_MAIL"));

    }

    public void notifyNewUser(ActionFormEvent evt) throws PortletException {
        PortletRequest req = evt.getActionRequest();

        TextFieldBean emailTF = evt.getTextFieldBean("emailAddress");


        // create a request
        GenericRequest request = requestService.createRequest(ACTIVATE_ACCOUNT_LABEL);
        long now = Calendar.getInstance().getTime().getTime();

        request.setLifetime(new Date(now + REQUEST_LIFETIME));

        // request.setUserID(user.getID());

        request.setAttribute("userName", evt.getTextFieldBean("userName").getValue());
        request.setAttribute("fullName", evt.getTextFieldBean("fullName").getValue());
        request.setAttribute("emailAddress", evt.getTextFieldBean("emailAddress").getValue());
        request.setAttribute("organization", evt.getTextFieldBean("organization").getValue());

        // put hashed pass in request
        String savePasswds = portalConfigService.getProperty("SAVE_PASSWORDS");
        if (savePasswds.equals(Boolean.TRUE.toString())) {
            String pass = evt.getPasswordBean("password").getValue();
            pass = passwordManagerService.getHashedPassword(pass);
            request.setAttribute("password", pass);
        }

        requestService.saveRequest(request);

        MailMessage mailToUser = tms.getMailMessage();
        StringBuffer body = new StringBuffer();

        String activateURL = activateAccountURL + "&reqid=" + request.getOid();

        String denyURL = denyAccountURL + "&reqid=" + request.getOid();

        // check if this account request should be approved by an administrator
        boolean accountApproval = Boolean.valueOf(portalConfigService.getProperty(ADMIN_ACCOUNT_APPROVAL)).booleanValue();
        if (accountApproval) {
            List usersToBeNotified = roleService.getUsersInRole(PortletRole.ADMIN);
            Set admins = new HashSet();
            for (int i = 0; i < usersToBeNotified.size(); i++) {
                User u = (User) usersToBeNotified.get(i);
                admins.add(u.getEmailAddress());
            }
            mailToUser.setTo(admins);
            String mailSubject = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ADMIN_MAILSUBJECT");
            if (mailSubject == null) mailSubject = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ADMIN_MAILSUBJECT");
            mailToUser.setSubject(mailSubject);
            String adminBody = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ADMIN_MAIL");
            if (adminBody == null) adminBody = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ADMIN_MAIL");
            body.append(adminBody).append("\n\n");
            body.append(getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ALLOW")).append("\n\n");
            body.append(activateURL).append("\n\n");
            body.append(getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_DENY")).append("\n\n");
            body.append(denyURL).append("\n\n");
        } else {
            mailToUser.setTo(emailTF.getValue());

            String mailSubjectHeader = portalConfigService.getProperty("LOGIN_ACTIVATE_SUBJECT");
            String loginActivateMail = portalConfigService.getProperty("LOGIN_ACTIVATE_BODY");

            if (mailSubjectHeader == null) mailSubjectHeader = getLocalizedText(req, "LOGIN_ACTIVATE_SUBJECT");
            mailToUser.setSubject(mailSubjectHeader);

            if (loginActivateMail == null) loginActivateMail = getLocalizedText(req, "LOGIN_ACTIVATE_MAIL");
            body.append(loginActivateMail).append("\n\n");
            body.append(activateURL).append("\n\n");
        }

        body.append(getLocalizedText(req, "USERNAME"));
        body.append(evt.getTextFieldBean("userName").getValue()).append("\n");
        body.append(getLocalizedText(req, "FULLNAME"));
        body.append(evt.getTextFieldBean("fullName").getValue()).append("\n");
        body.append(getLocalizedText(req, "EMAILADDRESS"));
        body.append(evt.getTextFieldBean("emailAddress").getValue()).append("\n");
        body.append(getLocalizedText(req, "ORGANIZATION"));
        body.append(evt.getTextFieldBean("organization").getValue()).append("\n");

        mailToUser.setBody(body.toString());
        mailToUser.setServiceid("mail");

        try {
            tms.send(mailToUser);
        } catch (TextMessagingException e) {
            createErrorMessage(evt, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
            throw new PortletException("Unable to send mail message!", e);
        }

        boolean adminRequired = Boolean.valueOf(portalConfigService.getProperty(ADMIN_ACCOUNT_APPROVAL));
        if (adminRequired) {
            createSuccessMessage(evt, this.getLocalizedText(req, "LOGIN_ACCT_ADMIN_MAIL"));
        } else {
            createSuccessMessage(evt, this.getLocalizedText(req, "LOGIN_ACCT_MAIL"));
        }

    }


    public void newpassword(ActionFormEvent evt) {
        PortletRequest req = evt.getActionRequest();
        String id = req.getParameter("reqid");
        GenericRequest request = requestService.getRequest(id, FORGOT_PASSWORD_LABEL);
        if (request != null) {
            HiddenFieldBean reqid = evt.getHiddenFieldBean("reqid");
            reqid.setValue(id);
            setNextState(req, DO_NEW_PASSWORD);
        } else {
            setNextState(req, DEFAULT_VIEW_PAGE);
        }
    }

    private void doEmailAction(ActionFormEvent event, String msg, boolean createAccount) {
        PortletRequest req = event.getActionRequest();
        String id = req.getParameter("reqid");
        User user = null;
        GenericRequest request = requestService.getRequest(id, ACTIVATE_ACCOUNT_LABEL);
        if (request != null) {
            requestService.deleteRequest(request);

            String subject = "";
            String body = "";
            if (createAccount) {
                user = saveUser(request);
                createSuccessMessage(event, msg + " " + user.getUserName());

                // send the user an email
                subject = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
                if (subject == null) subject = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
                body = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED_BODY");
                if (body == null) body = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
            } else {
                createSuccessMessage(event, msg);

                // send the user an email
                subject = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY");
                if (subject == null) subject = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY");
                body = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY_BODY");
                if (body == null) body = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY");
            }

            MailMessage mailToUser = tms.getMailMessage();
            mailToUser.setTo(user.getEmailAddress());

            mailToUser.setSubject(subject);
            StringBuffer msgbody = new StringBuffer();

            msgbody.append(body).append("\n\n");
            msgbody.append(notificationURL);
            mailToUser.setBody(body.toString());
            mailToUser.setServiceid("mail");

            try {
                tms.send(mailToUser);
            } catch (TextMessagingException e) {
                log.error("Error: " + e.getMessage());
                createErrorMessage(event, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
            }

        }
        setNextState(req, "doViewUser");

    }

    public void activate(ActionFormEvent event) {
        PortletRequest req = event.getActionRequest();
        String msg = this.getLocalizedText(req, "USER_NEW_ACCOUNT") +
                "<br>" + this.getLocalizedText(req, "USER_PLEASE_LOGIN");
        doEmailAction(event, msg, true);
    }

    public void approveAccount(ActionFormEvent event) {
        PortletRequest req = event.getActionRequest();
        String msg = this.getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
        doEmailAction(event, msg, true);
    }

    public void denyAccount(ActionFormEvent event) {
        PortletRequest req = event.getActionRequest();
        String msg = this.getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY");
        doEmailAction(event, msg, false);
    }


    public void doSavePass(ActionFormEvent event) {

        PortletRequest req = event.getActionRequest();

        HiddenFieldBean reqid = event.getHiddenFieldBean("reqid");
        String id = reqid.getValue();
        GenericRequest request = requestService.getRequest(id, FORGOT_PASSWORD_LABEL);
        if (request != null) {
            String uid = request.getUserID();
            User user = userManagerService.getUser(uid);

            passwordManagerService.editPassword(user);

            String passwordValue = event.getPasswordBean("password").getValue();
            String confirmPasswordValue = event.getPasswordBean("confirmPassword").getValue();

            if (passwordValue == null) {
                createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_NOTSET"));
                setNextState(req, DO_NEW_PASSWORD);
                return;
            }

            // Otherwise, password must match confirmation
            if (!passwordValue.equals(confirmPasswordValue)) {
                createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_MISMATCH"));
                setNextState(req, DO_NEW_PASSWORD);
                // If they do match, then validate password with our service
            } else {
                if (passwordValue.length() == 0) {
                    createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_BLANK"));
                    setNextState(req, DO_NEW_PASSWORD);
                } else if (passwordValue.length() < 5) {
                    System.err.println("length < 5 password= " + passwordValue);
                    createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_TOOSHORT"));
                    setNextState(req, DO_NEW_PASSWORD);
                } else {
                    // save password
                    //System.err.println("saving password= " + passwordValue);
                    PasswordEditor editPasswd = passwordManagerService.editPassword(user);
                    editPasswd.setValue(passwordValue);
                    editPasswd.setDateLastModified(Calendar.getInstance().getTime());
                    passwordManagerService.savePassword(editPasswd);
                    createSuccessMessage(event, this.getLocalizedText(req, "USER_PASSWORD_SUCCESS"));
                    requestService.deleteRequest(request);
                }
            }
        }
    }
}
