/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.core.persistence.castor.descriptor;

import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.core.persistence.castor.descriptor.DescriptorException;
import org.gridlab.gridsphere.core.persistence.castor.PersistenceManagerXml;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;

import java.io.IOException;

public abstract class Descriptor {

    private static PortletLog log = SportletLog.getInstance(Descriptor.class);

    public Descriptor() {}

    /**
     * Unmarshalls an object given a descriptor xml file and a corresponding mapping xml file
     *
     * @param descriptorPath the location of the descriptor xml file
     * @param mappingPath the location of the mapping xml file
     * @return Object the unmarshalled object
     * @throws DescriptorException if the Descriptor cannot be created
     */
    protected static Object load(String descriptorPath, String mappingPath) throws IOException, DescriptorException  {

        Object object = null;
        PersistenceManagerXml pmx = PersistenceManagerXml.createPersistenceManager(descriptorPath, mappingPath);

        log.info("Using " + descriptorPath + " " +  mappingPath);
        // try to get it
        try {
             object = pmx.restoreObject();
        } catch (PersistenceManagerException e) {
            log.error("PersistenceManagerException ("+pmx.getMappingURL()+", "+pmx.getDescriptorURL()+") ", e);
            throw new DescriptorException("Unable to load descriptor: "+ e.getMessage());
        }
        return object;
    }

    /**
     * Save the layout deployment descriptor to layout.xml
     * <b>not implemented yet</b>
     */
    protected static void save(String descriptorPath, String mappingPath, Object object) throws IOException, DescriptorException {

        PersistenceManagerXml pmx = PersistenceManagerXml.createPersistenceManager(descriptorPath, mappingPath);

        // try to get it
        try {
             pmx.update(object);
        } catch (PersistenceManagerException e) {
            log.error("PersistenceManagerException: ("+pmx.getMappingURL()+", "+pmx.getDescriptorURL()+") ", e);
            throw new DescriptorException("Unable to save: "+e.getMessage());
        }
    }
}
