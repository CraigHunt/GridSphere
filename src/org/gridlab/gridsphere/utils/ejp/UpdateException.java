/*
 * @author <a href="oliver@wehrens.de">Oliver Wehrens</a>
 * @team sonicteam
 * @version $Id$
 *
 * Is thrown when the update of an object failed.
 */

package org.gridlab.gridsphere.utils.ejp;

public class UpdateException extends Exception {
    static org.apache.log4j.Category cat = org.apache.log4j.Category.getInstance(UpdateException.class.getName());

    public UpdateException() {
        super();
    }

    public UpdateException (String msg) {
        super(msg);
    }

}

