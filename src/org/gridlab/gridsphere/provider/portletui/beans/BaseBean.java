/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.provider.portletui.beans;

import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.portlet.impl.SportletProperties;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * The abstract <code>BaseBean</code> is an implementation of the <code>TagBean</code> interface.
 * <code>BaseBean</code> provides the basic functionality for all visual beans.
 */
public abstract class BaseBean implements TagBean {

    private transient static PortletLog log = SportletLog.getInstance(BaseBean.class);
    protected String beanId = "";
    protected String vbName = "undefined";
    protected HttpServletRequest request = null;
    protected Locale locale = null;

    /**
     * Constructs default base bean
     */
    public BaseBean() {
        super();
    }

    /**
     * Constructs a base bean for the supplied visual bean type
     *
     * @param vbName a name identifying the type of visual bean
     */
    public BaseBean(String vbName) {
        this.vbName = vbName;
    }

    /**
     * Constructs a base bean for the supplied visual bean type
     *
     * @param vbName a name identifying the type of visual bean
     * @param req the HttpServletRequest
     */
    public BaseBean(String vbName, HttpServletRequest req) {
        this(vbName);
        this.request = req;
        if (request != null) {
            Locale locale = (Locale) request.getSession(true).getAttribute(SportletProperties.LOCALE);
            if (locale == null)
                locale = request.getLocale();
        }
        if (locale == null) locale = Locale.ENGLISH;
    }

    /**
     * Returns the bean identifier
     *
     * @return the bean identifier
     */
    public String getBeanId() {
        return this.beanId;
    }

    /**
     * Sets the bean identifier
     *
     * @param beanId the bean identifier
     */
    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public abstract String toStartString();

    public abstract String toEndString();

    public void store() {
        //System.err.println("storing bean " + getBeanKey());
        if (request != null) request.setAttribute(getBeanKey(), this);
    }

    protected String getBeanKey() {
        String cid = (String) request.getAttribute(SportletProperties.COMPONENT_ID);
        String compId = (String) request.getAttribute(SportletProperties.GP_COMPONENT_ID);
        String beanKey = null;
        if (compId == null) {
            beanKey = beanId + "_" + cid;
        } else {
            beanKey = compId + "%" + beanId + "_" + cid;
        }
        //log.debug("getBeanKey(" + beanId + ") = " + beanKey);
        return beanKey;
    }

    protected String getLocalizedText(String key) {
        return getLocalizedText(key, "gridsphere.resources.Portlet");
    }

    protected String getLocalizedText(String key, String base) {
        ResourceBundle bundle = ResourceBundle.getBundle(base, locale);
        return bundle.getString(key);
    }

}
