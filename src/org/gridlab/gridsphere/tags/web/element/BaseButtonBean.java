/**
 * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */

package org.gridlab.gridsphere.tags.web.element;

public class BaseButtonBean extends BaseNameValueBean implements Button {

    protected String type = new String();

    public BaseButtonBean() {
        super();
    }

    public BaseButtonBean(String name, String value) {
        super(name, value, false);
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public String toString() {
        return "<input type='" + type + "' name='" + getTagName() + name + "' value='" + value + "'/>";
    }
}
