/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.tags.portletui;

import org.gridlab.gridsphere.provider.portletui.beans.ListBoxBean;
import org.gridlab.gridsphere.provider.portletui.beans.BaseComponentBean;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.util.Vector;
import java.util.Iterator;

public class ListBoxTag extends ContainerTag {

    protected ListBoxBean listbox = null;

    protected int size = 1;

    /**
     * Returns the (html) size of the field.
     * @return size of the field
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the (html) size of the field
     * @param size size of the field
     */
    public void setSize(int size) {
        this.size = size;
    }

    public int doStartTag() throws JspException {

        //System.err.println("in TableCellTag:doStartTag");
        list = new Vector();

        //System.err.println("creating new list");

        if (!beanId.equals("")) {
            listbox = (ListBoxBean)pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
            if (listbox == null) {
                listbox = new ListBoxBean();
                this.setBaseComponentBean(listbox);
                listbox.setSize(size);
            } else {
                this.updateBaseComponentBean(listbox);
            }
        } else {
            listbox = new ListBoxBean();
            this.setBaseComponentBean(listbox);
            listbox.setSize(size);
        }

        ContainerTag rowTag = (ContainerTag)getParent();
        if (rowTag == null) return SKIP_BODY;
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {

        if (!beanId.equals("")) {
            if (listbox.getSize() == 0) listbox.setSize(size);
        } else {
            listbox.setSize(size);
        }

        Iterator it = list.iterator();
        while (it.hasNext()) {
            BaseComponentBean bc = (BaseComponentBean)it.next();
            bc.setName(name);
            listbox.addBean(bc);
        }
        //debug();

        Object parentTag = getParent();
        if (parentTag instanceof ContainerTag) {
            ContainerTag containerTag = (ContainerTag)parentTag;
            listbox.setName(containerTag.getName());
            containerTag.addTagBean(listbox);
        } else {
            try {
                JspWriter out = pageContext.getOut();
                out.print(listbox.toString());
            } catch (Exception e) {
                throw new JspException(e.getMessage());
            }
        }
        return EVAL_BODY_INCLUDE;
    }

}
