/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.services.container.parsing.impl;

import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.portlet.impl.*;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
import org.gridlab.gridsphere.services.container.parsing.ServletParsingService;
import org.gridlab.gridsphere.services.container.registry.PortletRegistryService;
import org.gridlab.gridsphere.portletcontainer.descriptor.PortletApplication;
import org.gridlab.gridsphere.portletcontainer.descriptor.PortletDefinition;
import org.gridlab.gridsphere.portletcontainer.descriptor.ConcretePortletApplication;
import org.gridlab.gridsphere.portletcontainer.RegisteredPortlet;
import org.gridlab.gridsphere.portletcontainer.GridSphereProperties;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import java.util.List;

public class ServletParsingServiceImpl implements PortletServiceProvider, ServletParsingService {

    private static PortletLog log = SportletLog.getInstance(ServletParsingServiceImpl.class);
    private PortletServiceConfig config;

    public void init(PortletServiceConfig config) {
        log.info("in init()");
        this.config = config;
    }

    public void destroy() {
        log.info("in destroy()");
    }

    public PortletRequest getPortletRequest(RegisteredPortlet regPortlet, HttpServletRequest request) {

      /*
        request.setAttribute(GridSphereProperties.PORTLETSETTINGS, portletSettings);
        request.setAttribute(GridSphereProperties.PORTLETWINDOW, portletWindow);
        request.setAttribute(GridSphereProperties.MODEMODIFIER, modeModifier);
        request.setAttribute(GridSphereProperties.PORTLETMODE, mode);
        request.setAttribute(GridSphereProperties.PREVIOUSMODE, previousMode);
        request.setAttribute(GridSphereProperties.CLIENT, client);
        PortletRequest req = new SportletRequest(request);
        return req;
        */
        return null;
    }

    public PortletResponse getPortletResponse(HttpServletResponse res) {
        /*
        SportletResponse sportletResponse = new SportletResponse(res);
        return (PortletResponse) sportletResponse;
        */
        return null;
    }

    public void putPortletRequest(PortletRequest req) {

    }

    public PortletConfig getPortletConfig(ServletConfig config) {
        SportletConfig portletConfig = new SportletConfig(config);
        return portletConfig;
    }

    public PortletContext getPortletContext(ServletConfig config) {
        SportletContext portletContext = new SportletContext(config);
        return portletContext;
    }

}
