/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.tags.web;

import org.gridlab.gridsphere.tags.web.element.PasswordBean;
import org.gridlab.gridsphere.tags.web.element.FileInputBean;

import javax.servlet.jsp.JspException;

public class FileInputTag extends BaseTag {

    public int doStartTag() throws JspException {
        if (tagBean.equals("")) {
            this.htmlelement = new FileInputBean(name, value, isDisabled, isReadonly, size, maxLength);

        }
        return super.doStartTag();
    }

}
