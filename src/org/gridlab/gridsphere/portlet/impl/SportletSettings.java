/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portlet.impl;

import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.portletcontainer.descriptor.*;
import org.gridlab.gridsphere.services.security.acl.AccessControlService;

import java.util.*;

/**
 * The SportletSettings class provides the portlet with its dynamic configuration.
 * The configuration holds information about the portlet that is valid per concrete portlet for all users,
 * and is maintained by the administrator. The portlet can therefore only read the dynamic configuration.
 * Only when the portlet is in CONFIGURE mode, it has write access to the dynamic configuration data
 */
public class SportletSettings implements PortletSettings {

    protected Hashtable store = new Hashtable();
    protected List langList = new Vector();
    protected PortletDeploymentDescriptor pdd = null;
    protected PortletApplicationSettings applicationSettings = null;
    protected ConcretePortletInfo concretePortletInfo = null;
    protected String concretePortletID = null;
    protected SportletApplicationSettings appSettings = null;
    protected Locale locale = null;

    // Initially don't allow this user to modify sportlet settings
    protected boolean hasConfigurePermission = false;


    /**
     * SportletSettings constructor
     * Create a PortletSettings object from a PortletApplication deployment descriptor object
     *
     * @param portletApp the PortletApplication deployment descriptor information
     * @param knownGroups a list of known groups obtained from the AccessControlService
     * @param knownRoles a list of known roles obtained from the AccessControlService
     */
    public SportletSettings(PortletDeploymentDescriptor pdd,
                            ConcretePortletApplication portletApp,
                            List knownGroups, List knownRoles) {

        this.concretePortletInfo = portletApp.getConcretePortletInfo();

        this.concretePortletID = portletApp.getUID();

        String localeStr = concretePortletInfo.getDefaultLocale();
        locale = new Locale(localeStr, "");
        langList = concretePortletInfo.getLanguageList();

        // Stick <config-param> in store
        Iterator configParamsIt = concretePortletInfo.getConfigParamList().iterator();
        while (configParamsIt.hasNext()) {
            ConfigParam configParam = (ConfigParam)configParamsIt.next();
            store.put(configParam.getParamName(), configParam.getParamValue());
        }
    }

    public void enableConfigurePermission(boolean hasConfigurePermission) {
        this.hasConfigurePermission = hasConfigurePermission;
    }

    /**
     * Returns the value of the attribute with the given name, or null if no such attribute exists.
     *
     * @param name the name of the attribute
     * @return the value of the attribute
     */
    public String getAttribute(String name) {
        return (String) store.get(name);
    }

    /**
     * Returns an enumeration of all available attributes names.
     *
     * @return an enumeration of all available attributes names
     */
    public Enumeration getAttributeNames() {
        return store.keys();
    }

    /**
     * Returns the title of this window for the provided locale, or null if none exists.
     *
     * @param locale the locale-centric title
     * @param client the given client
     * @return the title of the portlet
     */
    public String getTitle(Locale locale, Client client) {
        Iterator it = langList.iterator();
        while (it.hasNext()) {
            LanguageInfo langInfo = (LanguageInfo)it.next();
            if (langInfo.getLocale().equals(locale.toString())) {
                return langInfo.getTitle();
            }
        }
        return null;
    }

    /**
     * Returns the portlet's default locale.
     *
     * @return the portlet's default locale
     */
    public Locale getDefaultLocale() {
        return locale;
    }

    /**
     * Returns the short title of this window for the provided locale, or null if none exists.
     *
     * @param locale the locale-centric title
     * @param client the given client
     * @return the title of the portlet
     */
    public String getTitleShort(Locale locale, Client client) {
        while (langList.iterator().hasNext()) {
            LanguageInfo langInfo = (LanguageInfo)langList.iterator().next();
            if (langInfo.getLocale().equals(locale)) {
                return langInfo.getTitleShort();
            }
        }
        return null;
    }

    /**
     * Returns the description of this window for the provided locale, or null if none exists.
     *
     * @param locale the locale-centric title
     * @param client the given client
     * @return the title of the portlet
     */
    public String getDescription(Locale locale, Client client) {
        while (langList.iterator().hasNext()) {
            LanguageInfo langInfo = (LanguageInfo)langList.iterator().next();
            if (langInfo.getLocale().equals(locale)) {
                return langInfo.getDescription();
            }
        }
        return null;
    }

    /**
     * Returns the keywords of this window for the provided locale, or null if none exists.
     *
     * @param locale the locale-centric title
     * @param client the given client
     * @return the title of the portlet
     */
    public String getKeywords(Locale locale, Client client) {
        while (langList.iterator().hasNext()) {
            LanguageInfo langInfo = (LanguageInfo)langList.iterator().next();
            if (langInfo.getLocale().equals(locale)) {
                return langInfo.getKeywords();
            }
        }
        return null;
    }

    /**
     * Returns this portlets concrete ID. Used internally in Action tags
     * to signal the portlet container which portlet needs to be executed
     * NOTE: THIS IS NOT PART OF THE WPS PORTLET API 4.1
     *
     * @return the concrete portlet ID
     */
    public String getConcretePortletID() {
        return concretePortletID;
    }

    /**
     * Removes the attribute with the given name.
     *
     * @param name the attribute name
     *
     * @throws AccessDeniedException if the caller isn't authorized to access this data object
     */
    public void removeAttribute(String name) throws AccessDeniedException {
        if (!hasConfigurePermission) {
            throw new AccessDeniedException("User is unauthorized to remove portlet settings");
        }
        store.remove(name);
    }

    /**
     * Sets the attribute with the given name and value.
     *
     * @param name the attribute name
     * @param value the attribute value
     *
     * @throws AccessDeniedException if the caller isn't authorized to access this data object
     */
    public void setAttribute(String name, String value) throws AccessDeniedException {
        if (!hasConfigurePermission) {
            throw new AccessDeniedException("User is unauthorized to set portlet settings");
        }
        store.put(name, value);
    }

    /**
     * Stores all attributes.
     *
     * @throws AccessDeniedException if the caller isn't authorized to access this data object
     */
    public void store() throws AccessDeniedException {
        if (!hasConfigurePermission) {
            throw new AccessDeniedException("User is unauthorized to store portlet settings");
        }
        Enumeration enum = store.elements();
        Vector list = new Vector();
        while (enum.hasMoreElements()) {
            String key = (String)enum.nextElement();
            String value = (String)store.get(key);
            ConfigParam parms = new ConfigParam(key, value);
            list.add(parms);
        }
        concretePortletInfo.setConfigParamList(list);
        pdd.save();
    }

    /**
     * Returns the portlet application settings
     *
     * @return the portlet application settings
     */
    public PortletApplicationSettings getApplicationSettings() {
        return applicationSettings;
    }

}
