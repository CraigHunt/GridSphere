/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.provider.portletui.tags;

import org.gridlab.gridsphere.provider.portletui.beans.TableCellBean;
import org.gridlab.gridsphere.provider.portletui.beans.TableRowBean;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * A <code>TableCellTag</code> represents a table cell element contained by a <code>TableRowTag</code>
 */
public class TableCellTag extends BaseComponentTag {

    protected TableCellBean cellBean = null;
    protected String width = null;
    protected String height = null;
    protected String align = null;
    protected String valign = null;

    /**
     * Sets the table alignment e.g. "left", "center" or "right"
     *
     * @param align the table alignment
     */
    public void setAlign(String align) {
        this.align = align;
    }

    /**
     * Returns the table alignment e.g. "left", "center" or "right"
     *
     * @return the table alignment
     */
    public String getAlign() {
        return align;
    }

    /**
     * Sets the table vertical alignment e.g. "top", "middle", "bottom" or "baseline"
     *
     * @param valign the table vertical alignment
     */
    public void setValign(String valign) {
        this.valign = valign;
    }

    /**
     * Returns the table vertical alignment e.g. "top", "middle", "bottom" or "baseline"
     *
     * @return the table vertical alignment
     */
    public String getValign() {
        return valign;
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

    /**
     * Sets the table cell bean
     *
     * @param cellBean the table cell bean
     */
    public void setCellBean(TableCellBean cellBean) {
        this.cellBean = cellBean;
    }

    /**
     * Returns the table cell bean
     *
     * @return the table cell bean
     */
    public TableCellBean getCellBean() {
        return cellBean;
    }

    public int doStartTag() throws JspException {
        if (!beanId.equals("")) {
            cellBean = (TableCellBean) pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
            if (cellBean == null) cellBean = new TableCellBean();
        } else {
            cellBean = new TableCellBean();
            if (width != null) cellBean.setWidth(width);
            if (height != null) cellBean.setHeight(height);
            if (align != null) cellBean.setAlign(align);
            if (valign != null) cellBean.setValign(align);
        }

        TableRowTag rowTag = (TableRowTag) getParent();
        if (rowTag.getHeader()) {
            cellBean.setCssClass(TableRowBean.TABLE_HEADER_STYLE);
        }
        try {
            JspWriter out = pageContext.getOut();
            out.print(cellBean.toStartString());
        } catch (Exception e) {
            throw new JspException(e.getMessage());
        }

        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {

        try {
            JspWriter out = pageContext.getOut();
            out.print(cellBean.toEndString());
        } catch (Exception e) {
            throw new JspException(e.getMessage());
        }
        return EVAL_PAGE;
    }
}
