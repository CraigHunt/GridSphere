/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portlet.service.spi.impl;

import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;

import javax.servlet.ServletConfig;
import java.util.Enumeration;
import java.util.Properties;

/**
 * The <code>SportletServiceConfig</code> provides an implementation
 * of the <code>PortletServiceConfig</code> interface through which
 * portlet services access the configuration settings from the services
 * descriptor file.
 */
public class SportletServiceConfig implements PortletServiceConfig {

    private Class service;
    private Properties configProperties;
    private ServletConfig servletConfig;

    /**
     * Constructor disallows non-argument instantiation
     */
    private SportletServiceConfig() {
    }

    /**
     * Constructs an instance of SportletServiceConfig using the supplied
     * service class, the configuration properties and the  servlet configuration
     *
     * @param service the service class
     * @param configProperties the service configuration properties
     * @param servletConfig the <code>ServletConfig</code>
     */
    public SportletServiceConfig(Class service,
                                 Properties configProperties,
                                 ServletConfig servletConfig) {
        this.service = service;
        this.configProperties = configProperties;
        this.servletConfig = servletConfig;
    }

    /**
     * Returns the init parameter with the given name.
     *
     * @param name the name of the requested init parameter.
     * @return the init parameter
     */
    public String getInitParameter(String name) {
        return configProperties.getProperty(name);
    }

    /**
     * Returns the init parameter with the given name.
     *
     * @param name the name of the requested init parameter.
     * @param value the value of the init parameter
     */
    public void setInitParameter(String name, String value) {
        configProperties.setProperty(name, value);
    }

    /**
     * Returns the init parameter with the given name. It returns the given default
     * value if the parameter is not found.
     *
     * @param name the name of the requested init parameter.
     * @param defaultValue the default value to return.
     * @return the init parameter value if exists, otherwise defaultValue
     */
    public String getInitParameter(String name, String defaultValue) {
        return configProperties.getProperty(name, defaultValue);
    }

    /**
     * Returns an enumeration with the names of all init parameters provided in the portlet service configuration.
     *
     * @return an enumeration of the init parameters
     */
    public Enumeration getInitParameterNames() {
        return configProperties.keys();
    }

    /**
     * Returns the servlet configuration
     *
     * @return the servlet configuration
     */
    public ServletConfig getServletConfig() {
        return servletConfig;
    }

}
