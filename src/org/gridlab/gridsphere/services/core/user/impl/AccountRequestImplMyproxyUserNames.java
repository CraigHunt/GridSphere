/*
 * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
 * @team sonicteam
 * @version $Id$
 */

package org.gridlab.gridsphere.services.core.user.impl;

import org.gridlab.gridsphere.core.persistence.castor.StringVector;

/**
 * @table arimyproxyusernames
 * @depends AccountRequestImpl
 */
public class AccountRequestImplMyproxyUserNames extends StringVector {

    /**
     * @sql-name reference
     */
    private org.gridlab.gridsphere.services.core.user.impl.AccountRequestImpl Reference;

}

