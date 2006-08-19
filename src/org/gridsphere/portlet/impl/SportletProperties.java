/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: SportletProperties.java 5089 2006-08-18 22:54:05Z novotny $
 */
package org.gridsphere.portlet.impl;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * <code>SportletProperties</code> conatins all the "hidden" variable names
 * that get transmitted between the portlet container and the portlets to
 * request a particular portlet lifecycle action.
 * <p/>
 * SportletProperties comtains three kinds of attributes
 * <ul>
 * <li>Lifecycle atttributes specify the lifecycle method to invoke on a
 * portlet</li>
 * <li>Portlet object attributes contain the actual portlet object e.g.
 * PortletConfig, PortletSettings which must be transferred in the
 * servlet request.</li>
 * <li>Portlet event objects</li>
 * </ul>
 */
public class SportletProperties {

    protected static Properties props = null;

    private static SportletProperties instance = new SportletProperties();

    /**
     * Specifes GS context path for use in creating urls
     */
    public static final String CONTEXT_PATH = "org.gridsphere.CONTEXT_PATH";

    /**
     * Specifes GS servlet path for use in creating urls
     */
    public static final String SERVLET_PATH = "org.gridsphere.SERVLET_PATH";

    /**
     * Determines which lifecycle command to invoke
     */
    public static final String PORTLET_LIFECYCLE_METHOD = "org.gridsphere.portlet.portletLifecycleMethod";

    // Portlet Lifecyle methods
    /**
     * Command to perform the init method on a portlet
     */
    public static final String INIT = "org.gridsphere.portlet.lifecycle.init";

    /**
     * Command to perform the destroy method on a portlet
     */
    public static final String DESTROY = "org.gridsphere.portlet.lifecycle.destroy";

    /**
     * Command to perform the init concrete method on a portlet
     */
    public static final String INIT_CONCRETE = "org.gridsphere.portlet.lifecycle.initConcrete";

    /**
     * Command to perform the destroy concrete method on a portlet
     */
    public static final String DESTROY_CONCRETE = "org.gridsphere.portlet.lifecycle.destroyConcrete";

    /**
     * Command to perform the login method on a portlet
     */
    public static final String LOGIN = "gs_login";

    /**
     * Command to perform the logout method on a portlet
     */
    public static final String LOGOUT = "gs_logout";

    /**
     * Command to perform the service method on a portlet
     */
    public static final String SERVICE = "org.gridsphere.portlet.lifecycle.service";

    // Portlet obects
    /**
     * The variable name of the PortletApplication object
     */
    public static final String PORTLET_APPLICATION = "org.gridsphere.portletcontainer.PortletApplication";

    /**
     * The variable name of the PortletSettings object
     */
    public static final String PORTLET_SETTINGS = "org.gridsphere.portlet.PortletSettings";

    // Portlet events
    /**
     * The variable name of the ActionEvent object
     */
    public static final String ACTION_EVENT = "org.gridsphere.event.ActionEvent";

    /**
     * The variable name of the MessageEvent object
     */
    public static final String MESSAGE_EVENT = "org.gridsphere.event.MessageEvent";

    /**
     * The variable name of the WindowEvent object
     */
    public static final String WINDOW_EVENT = "org.gridsphere.event.WindowEvent";

    /**
     * Determines which event listener to notify
     */
    public static final String PORTLET_ACTION_METHOD = "org.gridsphere.portlet.lifecycle.portletActionMethod";

    /**
     * Command to perform the actionPerformed method on a portlet
     */
    public static final String ACTION_PERFORMED = "org.gridsphere.portlet.lifecycle.actionPerformed";

    /**
     * Command to perform the messageReceived method on a portlet
     */
    public static final String MESSAGE_RECEIVED = "org.gridsphere.portlet.lifecycle.messageReceived";

    /**
     * Command to perform the windowDetached method on a portlet
     */
    public static final String WINDOW_DETACHED = "org.gridsphere.portlet.lifecycle.windowDetached";

    /**
     * Command to perform the windowMinimized method on a portlet
     */
    public static final String WINDOW_MINIMIZED = "org.gridsphere.portlet.lifecycle.windowMinimized";

    /**
     * Command to perform the windowMaximized method on a portlet
     */
    public static final String WINDOW_MAXIMIZED = "org.gridsphere.portlet.lifecycle.windowMaximized";

    /**
     * Command to perform the windowClosing method on a portlet
     * <p/>
     * *NOT IMPLEMENTED*
     */
    public static final String WINDOW_CLOSING = "org.gridsphere.portlet.lifecycle.windowClosing";

    /**
     * Command to perform the windowRestored method on a portlet
     */
    public static final String WINDOW_RESTORED = "org.gridsphere.portlet.lifecycle.windowRestored";

    public static final String INIT_PAGE = "org.gridsphere.layout.INIT_PAGE";
    /**
     * Command to perform the doTitle method on a portlet
     */
    public static final String DO_TITLE = "org.gridsphere.portlet.lifecycle.doTitle";

    public static final String COMPONENT_ID = "cid";

    public static final String COMPONENT_ID_VAR = "org.gridsphere.layout.COMPONENT_ID_VAR";

    public static final String LAYOUT_EDIT_MODE = "org.gridsphere.layout.LAYOUT_EDIT_MODE";

    public static final String LAYOUT_PAGE = "org.gridsphere.layout.PAGE";

    public static final String LAYOUT_THEME = "org.gridsphere.layout.THEME";

    public static final String LAYOUT_RENDERKIT = "org.gridsphere.layout.RENDERKIT";

    public static final String LAYOUT_PAGE_PARAM = "gs_PageLayout";


    // Used for "action component model" in grid portlets currently
    public static final String GP_COMPONENT_ID = "gpcompid";

    public static final String DEFAULT_PORTLET_ACTION = "gs_action";

    public static final String DEFAULT_PORTLET_MESSAGE = "message";

    public static final String PORTLETID = "pid";

    public static final String ERROR = "org.gridsphere.portlet.error";
    // Portlet API objects

    public static final String CLIENT = "org.gridsphere.portlet.Client";

    public static final String PORTLET_MODE = "gs_mode";

    public static final String PORTLET_TITLE = "title";

    public static final String PREVIOUS_MODE = "org.gridsphere.portlet.PreviousMode";

    public static final String MODEMODIFIER = "org.gridsphere.portlet.ModeModifier";

    public static final String PORTLET_WINDOW = "gs_state";

    public static final String PORTLET_WINDOW_ID = "org.gridsphere.layout.WINDOW_ID";

    public static final String PORTLET_DATA_MANAGER = "org.gridsphere.portletcontainer.PortletDataManager";

    public static final String PORTLETERROR = "org.gridsphere.portlet.PortletError";

    public static final String PREFIX = "up";

    public static final String PORTLET_USER = "org.gridsphere.portlet.User";

    /**
     * The variable name of the PortletConfig object
     */
    public static final String PORTLET_CONFIG = "javax.portlet.config";

    public static final String PORTAL_CONTEXT = "javax.portlet.context";

    public static final String PORTLET_PREFERENCES = "javax.portlet.preferences";

    public static final String PORTLET_PREFERENCES_MANAGER = "org.gridsphere.portlet.jsrimpl.PortletPreferencesManager";

    public static final String PORTLET_GROUP = "org.gridsphere.portlet.PortletGroup";

    public static final String PORTLET_ROLE = "org.gridsphere.portlet.PortletRole";

    public static final String RENDER_REQUEST = "javax.portlet.request";

    public static final String RENDER_RESPONSE = "javax.portlet.response";

    public static final String PORTLETGROUPS = "org.gridsphere.portlet.groups";

    public static final String LOCALE = "org.gridsphere.portlet.Locale";

    public static final String PORTLET_SERVLET = "org.gridsphere.portlets.PortletServlet";

    public static final String FILE_DOWNLOAD_NAME = "org.gridsphere.portletcontainer.FILE_DOWNLOAD_NAME";

    public static final String FILE_DOWNLOAD_PATH = "org.gridsphere.portletcontainer.FILE_DOWNLOAD_PATH";

    public static final String FILE_DOWNLOAD_BINARY = "org.gridsphere.portletcontainer.FILE_DOWNLOAD_BINARY";

    public static final String FILE_DELETE = "org.gridsphere.portletcontainer.FILE_DELETE";

    public static final String FILE_DOWNLOAD_ERROR = "org.gridsphere.portletcontainer.FILE_DOWNLOAD_ERROR";

    public static final String PORTAL_PROPERTIES = "org.gridsphere.PORTAL_PROPERTIES";

    public static final String ALLOWED_MODES = "org.gridsphere.ALLOWED_MODES";

    public static final String MIME_TYPES = "org.gridsphere.MIME_TYPES";

    public static final String RESPONSE_COMMITTED = "org.gridsphere.RESPONSE_COMMITTED";

    public static final String RENDER_PARAM_PREFIX = "rp_";

    public static final String RENDER_OUTPUT = "org.gridsphere.layout.RENDER_OUTPUT.";

    public static final String FLOAT_STATE = "org.gridsphere.portlet.FLOAT_STATE";

    public static final String SSL_REQUIRED = "javax.portlet.SSL_REQUIRED";

    public static final String PORTLET_USER_PRINCIPAL = "org.gridsphere.portlet.UserPrincipal";

    public static final String EXTRA_QUERY_INFO = "org.gridsphere.layout.EXTRA_QUERY_INFO";

    private SportletProperties() {
        if (props == null) {
            InputStream propsStream = getClass().getResourceAsStream("/org/gridsphere/portlet/impl/portlet.properties");
            props = new Properties();
            try {
                props.load(propsStream);
            } catch (IOException e) {
                System.err.println("Unable to load gridsphere.properties");
                e.printStackTrace();
            }
        }
    }

    public static SportletProperties getInstance() {
        return instance;
    }

    public String getProperty(String key) {
        if (key == null) throw new IllegalArgumentException("property key cannot be null!");
        return props.getProperty(key);
    }

}

