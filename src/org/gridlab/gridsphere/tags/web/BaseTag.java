/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.tags.web;

import org.gridlab.gridsphere.tags.web.element.ElementBean;
import org.gridlab.gridsphere.tags.web.validator.NoValidation;
import org.gridlab.gridsphere.tags.web.validator.Validator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class BaseTag extends TagSupport {

    protected String type;
    protected String value = "";
    protected String name = "name";
    protected boolean isChecked = false;
    protected boolean isDisabled = false;
    protected boolean isReadonly = false;
    protected Validator validator = new NoValidation();
    protected int size = 20;
    protected int maxLength = 20;
    protected ElementBean htmlelement;
    protected String bean = new String();
    protected int rows = 10;
    protected int cols = 40;

    public static final String TEXT = "text";
    public static final String PASSWORD = "password";
    public static final String CHECKBOX = "checkbox";
    public static final String RADIO = "radio";
    public static final String SUBMIT = "submit";
    public static final String RESET = "reset";
    public static final String FILE = "file";
    public static final String BUTTON = "button";

    /**
     * Possible attributes of HTML 4.0 <input> tag
     *
     * TYPE=[ text | password | checkbox | radio | submit | reset | file | hidden | image | button ] (type of input)
     * NAME=CDATA (key in submitted form)
     * VALUE=CDATA (value of input)
     * CHECKED (check radio button or checkbox)
     * SIZE=CDATA (suggested number of characters for text input)
     * MAXLENGTH=Number (maximum number of characters for text input)
     * SRC=URI (source for image)
     * ALT=CDATA (alternate text for image input)
     * USEMAP=URI (client-side image map)
     * ALIGN=[ top | middle | bottom | left | right ] (alignment of image input)
     * DISABLED (disable element)
     * READONLY (prevent changes)
     * ACCEPT=ContentTypes (media types for file upload)
     * ACCESSKEY=Character (shortcut key)
     * TABINDEX=Number (position in tabbing order)
     * ONFOCUS=Script (element received focus)
     * ONBLUR=Script (element lost focus)
     * ONSELECT=Script (element text selected)
     * ONCHANGE=Script (element value changed)
     */

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean getChecked() {
        return isChecked;
    }

    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public boolean getDisabled() {
        return isDisabled;
    }

    public void setReadonly(boolean isReadonly) {
        this.isReadonly = isReadonly;
    }

    public boolean getReadonly() {
        return isReadonly;
    }

    public void setMaxlength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxlength() {
        return maxLength;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public String getBean() {
        return bean;
    }

    public void setBean(String bean) {
        this.bean = bean;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public void setValidator(String validatorClass) {
        try {
            validator = (Validator) Class.forName(validatorClass).newInstance();
        } catch (Exception e) {
        } // so what? use default novalidator currently or should it return false??
    }

    public Validator getValidator() {
        return validator;
    }

    public int doStartTag() throws JspException {
        if (!bean.equals("")) {
            this.htmlelement = (ElementBean) pageContext.getRequest().getAttribute(bean);
        }
        try {
            JspWriter out = pageContext.getOut();
            out.print(htmlelement.toString());
        } catch (Exception e) {
            throw new JspTagException(e.getMessage());
        }
        return EVAL_BODY_INCLUDE;
    }


}
