/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.provider.portletui.tags.gs;

import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.provider.portletui.beans.ImageBean;
import org.gridlab.gridsphere.provider.portletui.tags.gs.BaseComponentTagImpl;
import org.gridlab.gridsphere.provider.portletui.tags.ActionLinkTag;
import org.gridlab.gridsphere.provider.portletui.tags.ImageTag;
import org.gridlab.gridsphere.provider.portletui.tags.gs.BaseComponentTagImpl;
import org.gridlab.gridsphere.provider.portletui.tags.gs.BaseComponentTagImpl;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

/**
 * The <code>ImageTag</code> represents an href img element
 */
public class ImageTagImpl extends BaseComponentTagImpl implements ImageTag {
    private static PortletLog log = SportletLog.getInstance(ImageTagImpl.class);

    protected ImageBean urlImageBean = null;
    protected String src = null;
    protected String border = null;
    protected String title = null;
    protected String alt = null;
    protected String width = null;
    protected String height = null;
    protected String align = null;

    /**
     * Returns the location of the image
     *
     * @return src the location of the image
     */
    public String getSrc() {
        return src;
    }

    /**
     * Sets the location of the image
     *
     * @param src the location of the image
     */
    public void setSrc(String src) {
        this.src = src;
    }

    /**
     * Return the image title border
     *
     * @return the image title border
     */
    public String getBorder() {
        return border;
    }

    /**
     * Sets the image title border
     *
     * @param border the image title border
     */
    public void setBorder(String border) {
        this.border = border;
    }

    /**
     * Return the image title
     *
     * @return the image title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the image title
     *
     * @param title the image title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the associated alt attribute
     *
     * @return the alt tag
     */
    public String getAlt() {
        return alt;
    }

    /**
     * Sets an alt tag to display
     *
     * @param alt the alt tag
     */
    public void setAlt(String alt) {
        this.alt = alt;
    }

    /**
     * Sets the table alignment e.g. "left", "top", "bottom" or "right"
     *
     * @param align the table alignment
     */
    public void setAlign(String align) {
        this.align = align;
    }

    /**
     * Returns the table alignment e.g. "left", "top", "bottom" or "right"
     *
     * @return the table alignment
     */
    public String getAlign() {
        return align;
    }

    /**
     * Sets the table cell width
     *
     * @param width the table cell width
     */
    public void setWidth(String width) {
        this.width = width;
    }

    /**
     * Returns the table cell width
     *
     * @return the table cell width
     */
    public String getWidth() {
        return width;
    }

    /**
     * Sets the table cell height
     *
     * @param height the table cell height
     */
    public void setHeight(String height) {
        this.height = height;
    }

    /**
     * Returns the table cell height
     *
     * @return the table cell height
     */
    public String getHeight() {
        return height;
    }

    private void setImageBean(ImageBean urlImageBean) {
        if (alt != null) urlImageBean.setAlt(alt);
        if (title != null) urlImageBean.setTitle(title);
        if (src != null) urlImageBean.setSrc(src);
        if (border != null) urlImageBean.setBorder(border);
        if (width != null) urlImageBean.setWidth(width);
        if (height != null) urlImageBean.setHeight(height);
        if (align != null) urlImageBean.setAlign(align);
    }

    public int doStartTag() throws JspException {
        if (!beanId.equals("")) {
            urlImageBean = (ImageBean) pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
            if (urlImageBean == null) {
                urlImageBean = new ImageBean();
                setImageBean(urlImageBean);
            }
            setImageBean(urlImageBean);
        } else {
            urlImageBean = new ImageBean();
            setImageBean(urlImageBean);
        }

        Tag parent = getParent();
        if (parent instanceof ActionLinkTag) {
            ActionLinkTag actionTag = (ActionLinkTag) parent;
            actionTag.setImageBean(urlImageBean);
        } else {
            try {
                JspWriter out = pageContext.getOut();
                out.print(urlImageBean.toStartString());
            } catch (Exception e) {
                throw new JspException(e.getMessage());
            }
        }
        return SKIP_BODY;
    }

}



