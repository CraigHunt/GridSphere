/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portletcontainer;

import java.util.Hashtable;
import java.util.List;

/**
 * The <code>ApplicationPortletConfig</code> represents the application portlet
 * configuration information.
 */
public interface ApplicationPortletConfig {

    /**
     * Returns the portlet application id
     *
     * @return the portlet application id
     */
    public String getApplicationPortletID();

    /**
     * Sets the  portlet application id
     *
     * @param id the portlet application id
     */
    public void setApplicationPortletID(String id);

    /**
     * Returns the name of a servlet associated with this portlet defined in ui.xml as <servlet-name>
     *
     * @return the servlet name
     */
    public String getServletName();

    /**
     * Sets the servlet name associated with this portlet defined in ui.xml as <servlet-name>
     *
     * @param servletName the servlet name
     */
    public void setServletName(String servletName);

    /**
     * Returns the portlet application name
     *
     * @return name portlet application name
     */
    public String getPortletName();

    /**
     * Sets the portlet application name
     *
     * @param portletName the portlet application name
     */
    public void setPortletName(String portletName);

    /**
     * Returns the configuration parameter <code>Hashtable</code>
     *
     * @return the configuration parameter <code>Hashtable</code>
     */
    public Hashtable getConfigParams();

    /**
     * Returns the allowed window states supported by this portlet
     *
     * @return the <code>List</code> of
     * <code>PortletWindow.State</code> elements allowed for this portlet
     */
    public List getAllowedWindowStates();

    /**
     * Returns the supported modes for this portlet
     *
     * @return the supported modes for this portlet
     */
    public List getSupportedModes();
}
