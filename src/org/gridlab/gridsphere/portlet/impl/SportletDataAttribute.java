/*
 * @author <a href="mailto:wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */


package org.gridlab.gridsphere.portlet.impl;

import org.gridlab.gridsphere.core.persistence.UniqueID;
import org.gridlab.gridsphere.core.persistence.castor.Attribute;

/**
 * @table sdattribute
 * @depends org.gridlab.gridsphere.portlet.impl.SportletData
 */
public class SportletDataAttribute extends Attribute {

    /**
     * @sql-name sportletdata
     */
    private SportletData SportletData = new SportletData();

    public SportletDataAttribute() {
        super();
    }

    public SportletDataAttribute(String k, String v) {
        this.setKey(k);
        this.setValue(v);
        this.setOid(UniqueID.get());
    }

    public SportletData getSportletData() {
        return SportletData;
    }

    public void setSportletData(SportletData sd) {
        SportletData = sd;
    }

}