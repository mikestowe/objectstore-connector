/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.functional;

import org.junit.After;
import org.junit.Test;
import org.mule.api.store.ObjectDoesNotExistException;
import org.mule.api.store.ObjectStoreException;
import org.mule.modules.objectstore.MulePropertyScope;
import org.mule.modules.objectstore.ObjectStoreConnector;

import static junit.framework.Assert.assertEquals;

public class RetrieveTestCases extends AbstractTestCase {

    @Test
    public void testRetrieve() throws Exception {

        ObjectStoreConnector module = this.getConnector();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
        String value = (String) module.retrieve(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
        assertEquals(OBJECTSTORE_VALUE, value);
    }

    @Test
    public void testRetrieveWithLock() throws Exception {

        ObjectStoreConnector module = this.getConnector();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
        String value = (String) module.retrieveWithLock(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
        assertEquals(OBJECTSTORE_VALUE, value);
    }

    @Test
    public void testRetrieveStore() throws Exception {

        ObjectStoreConnector module = this.getConnector();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
        module.retrieveStore(OBJECTSTORE_KEY, null, OBJECTSTORE_VALUE, null, null, null);
        String value = (String) module.retrieve(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
        assertEquals(OBJECTSTORE_VALUE, value);
    }

    @Test
    public void testRetrieveDefaultValue() throws Exception {

        ObjectStoreConnector module = this.getConnector();
        String value = (String) module.retrieve("muleion", "muleion", null, MulePropertyScope.INVOCATION, null);
        assertEquals("muleion", value);
    }

    @Test(expected = ObjectDoesNotExistException.class)
    public void testRetrieveFail() throws Exception {

        ObjectStoreConnector module = this.getConnector();
        module.retrieve("muleion", null, null, MulePropertyScope.INVOCATION, null);
    }

    @Test(expected = ObjectStoreException.class)
    public void testRetrieveExpired() throws Exception {

        ObjectStoreConnector module = this.getConnector();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, true);
        Thread.sleep(2000);

        module.retrieve(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
    }

    @Test
    public void testRetrieveNotExpired() throws Exception {

        ObjectStoreConnector module = this.getConnector();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, true);
        Thread.sleep(50);

        String value = (String) module.retrieve(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
        assertEquals(OBJECTSTORE_VALUE, value);
    }

    @Test
    public void testRetrieveExpiredDefaultValue() throws Exception {

        ObjectStoreConnector module = this.getConnector();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, true);
        Thread.sleep(2000L);

        String defaultValue = "default value";
        String value = (String) module.retrieve(OBJECTSTORE_KEY, defaultValue, null, MulePropertyScope.INVOCATION, null);
        assertEquals(defaultValue, value);
    }

    @After
    public void tearDown() throws Exception {
        ObjectStoreConnector module = this.getConnector();
        module.remove(OBJECTSTORE_KEY, true);
    }
}
