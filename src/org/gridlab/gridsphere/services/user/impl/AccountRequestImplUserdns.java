/*
 * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
 * @team sonicteam
 * @version $Id$
 */

package org.gridlab.gridsphere.services.user.impl;

import org.gridlab.gridsphere.core.persistence.castor.StringVector;

/**
 * @table ariuserdns
 * @depends org.gridlab.gridsphere.services.user.impl.AccountRequestImpl
 */
public class AccountRequestImplUserdns extends StringVector {

    /**
     * @field-type org.gridlab.gridsphere.services.user.impl.AccountRequestImpl
     * @sql-name reference
     */
    private org.gridlab.gridsphere.services.user.impl.AccountRequestImpl Reference;


}

