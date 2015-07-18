/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.functional;

import org.junit.After;
import org.junit.Test;
import org.mule.api.store.ObjectStoreException;
import org.mule.modules.objectstore.MulePropertyScope;
import org.mule.modules.objectstore.ObjectStoreConnector;

import static junit.framework.Assert.assertEquals;

public class StoreTestCases extends AbstractTestCase {

    @Test
    public void testStore() throws Exception {

        ObjectStoreConnector module = this.getConnector();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
        String value = (String) module.retrieve(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
        assertEquals(OBJECTSTORE_VALUE, value);
    }

    @Test
    public void testStoreOverwrite() throws Exception {

        ObjectStoreConnector module = this.getConnector();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, true);
        String value = (String) module.retrieve(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
        assertEquals(OBJECTSTORE_VALUE, value);
    }

    @Test(expected = ObjectStoreException.class)
    public void testStoreFailOnOverwrite() throws Exception {

        ObjectStoreConnector module = this.getConnector();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
    }

    @After
    public void tearDown() throws Exception {
        ObjectStoreConnector module = this.getConnector();
        module.remove(OBJECTSTORE_KEY, true);
    }
}
