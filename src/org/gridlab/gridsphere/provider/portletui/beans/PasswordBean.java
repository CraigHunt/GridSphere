/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */

package org.gridlab.gridsphere.provider.portletui.beans;

import javax.servlet.http.HttpServletRequest;
import javax.portlet.PortletRequest;

/**
 * A <code>PasswordBean</code> represents a password input element
 */
public class PasswordBean extends TextFieldBean {

    public static final String NAME = "pb";

    /**
     * Constructs a default password bean
     */
    public PasswordBean() {
        super(NAME);
    }

    /**
     * Constructs a password bean using a supplied portlet request and bean identifier
     *
     * @param beanId the bean identifier
     */
    public PasswordBean(String beanId) {
        super(NAME, beanId);
    }

    public String toStartString() {
        this.inputtype = "password";
        return super.toStartString();
    }
}
