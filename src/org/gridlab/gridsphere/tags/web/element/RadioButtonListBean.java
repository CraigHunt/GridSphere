/*
 * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */

package org.gridlab.gridsphere.tags.web.element;

public class RadioButtonListBean extends CheckBoxListBean {

    public RadioButtonListBean(String name) {
        super(name);
    }

    /**
     * Adds an radiobutton to the list.
     *
     * @param value value of the radiobutton
     * @param label label of the radiobuton
     * @param selected true of the radiobutton should be selected else false
     */
    public void add(String value, String label, boolean selected) {
        RadioButtonBean cbb = new RadioButtonBean(this.name, value, selected, false);
        cbb.setLabel(label);
        list.addElement(cbb);
    }

}
