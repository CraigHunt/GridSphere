/*
 * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */

package org.gridlab.gridsphere.portletcontainer.impl.descriptor;

import org.gridlab.gridsphere.core.persistence.castor.descriptor.ConfigParamList;
import org.gridlab.gridsphere.portletcontainer.ApplicationPortletConfig;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * The <code>ApplicationSportletConfig</code> is the implementation of
 * <code>ApplicationPortletConfig</code> using Castor for XML to Java
 * bindings.
 */
public class ApplicationSportletConfig implements ApplicationPortletConfig {

    private String id = new String();
    private String portletName = new String();
    private String servletName = new String();

    // configuration hash exported to clients
    private ConfigParamList configParamList = new ConfigParamList();

    // cache info
    private CacheInfo cacheInfo = new CacheInfo();

    // window states used by Castor
    private AllowsWindowStates allowsWindowStates = new AllowsWindowStates();

    // portlet mode list used by Castor
    private SupportsModes supportsModes = new SupportsModes();

    /**
     *  Constructs an instance of ApplicationSportletConfig
     */
    public ApplicationSportletConfig() {
    }

    /**
     * Returns the portlet application id
     *
     * @return the portlet application id
     */
    public String getApplicationPortletID() {
        return id;
    }

    /**
     * Sets the portlet application id
     *
     * @param id the portlet application id
     */
    public void setApplicationPortletID(String id) {
        this.id = id;
    }

    /**
     * Returns the name of a servlet associated with this portlet defined
     * in ui.xml as <servlet-name>
     *
     * @return the servlet name
     */
    public String getServletName() {
        return servletName;
    }

    /**
     * Sets the servlet name associated with this portlet defined in ui.xml
     * as <servlet-name>
     *
     * @param servletName the servlet name
     */
    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    /**
     * Returns the portlet application name
     *
     * @return the portlet application name
     */
    public String getPortletName() {
        return portletName;
    }

    /**
     * Sets the name of a PortletApplication
     *
     * @param portletName name of a PortletApplication
     */
    public void setPortletName(String portletName) {
        this.portletName = portletName;
    }

    /**
     * Returns the configuration parameter <code>Hashtable</code>
     *
     * @return the configuration parameter <code>Hashtable</code>
     */
    public Hashtable getConfigParams() {
        return configParamList.getConfigParams();
    }

    /**
     * Sets the configuration parameter <code>Hashtable</code>
     *
     * @param configHash the configuration parameter <code>Hashtable</code>
     */
    public void setConfigParams(Hashtable configHash) {
        this.configParamList.setConfigParams(configHash);
    }

    /**
     * Used internally by Castor. Clients should use #getConfigParams
     * Returns the configuration paarameter map
     *
     * @return the <code>List</code> of <code>ConfigParam</code>s
     */
    public List getConfigParamList() {
        return configParamList.getConfigParamList();
    }

    /**
     * Used internally by Castor. Clients should use #setConfigParams
     * Sets the configuration paarameter map
     *
     * @param configList the <code>List</code> of <code>ConfigParam</code>s
     */
    public void setConfigParamList(ArrayList configList) {
        this.configParamList.setConfigParamList(configList);
    }

    /**
     * Used internally by Castor. Not fully implemented yet.
     * Returns the portlet caching information
     *
     * @return the cache info
     */
    public CacheInfo getCacheInfo() {
        return cacheInfo;
    }

    /**
     * Used internally by Castor. Not fully implemented yet.
     * Sets the portlet caching information
     *
     * @param cacheInfo the portlets caching information
     */
    public void setCacheInfo(CacheInfo cacheInfo) {
        this.cacheInfo = cacheInfo;
    }

    /**
     * Returns the allowed window states supported by this portlet
     *
     * @return the <code>List</code> of
     * <code>PortletWindow.State</code> elements allowed for this portlet
     */
    public List getAllowedWindowStates() {
        return allowsWindowStates.getPortletWindowStates();
    }

    /**
     * Internally used by Castor
     * Returns the allowed window states supported by this portlet
     *
     * @return the <code>List</code> of
     * <code>PortletWindow.State</code> elements allowed for this portlet
     */
    public AllowsWindowStates getAllowsWindowStates() {
        return allowsWindowStates;
    }

    /**
     * Internal for use by Castor.
     * Sets the allowed window states supported by this portlet
     *
     * @param windowStates the <code>List</code> of
     * <code>PortletWindow.State</code> elements allowed for this portlet
     */
    public void setAllowsWindowStates(AllowsWindowStates windowStates) {
        this.allowsWindowStates = windowStates;
    }

    /**
     * Returns the supported modes for this portlet
     *
     * @return the supported modes for this portlet
     */
    public List getSupportedModes() {
        return supportsModes.getPortletModes();
    }

    /**
     * Used internally by Castor. Clients should use #getSupportedModes
     * instead.
     *
     * @return the supported modes for this portlet
     */
    public SupportsModes getSupportsModes() {
        return this.supportsModes;
    }

    /**
     * Used internally by Castor. Clients should use #getSupportedModes
     * instead.
     *
     * @param supportsModes the supported modes for this portlet
     */
    public void setSupportsModes(SupportsModes supportsModes) {
        this.supportsModes = supportsModes;
    }
}

