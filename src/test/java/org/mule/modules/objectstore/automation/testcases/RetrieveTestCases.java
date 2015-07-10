/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.testcases;

import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mule.api.store.ObjectDoesNotExistException;
import org.mule.api.store.ObjectStoreException;
import org.mule.modules.objectstore.MulePropertyScope;
import org.mule.modules.objectstore.ObjectStoreModule;
import org.mule.modules.objectstore.automation.ObjectStoreTestParent;
import org.mule.modules.objectstore.automation.RegressionTests;

import static junit.framework.Assert.assertEquals;

public class RetrieveTestCases extends ObjectStoreTestParent {

    @Category({ RegressionTests.class })
    @Test
    public void testRetrieve() throws Exception {

        ObjectStoreModule module = getModule();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
        String value = (String) module.retrieve(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
        assertEquals(OBJECTSTORE_VALUE, value);
    }

    @Category({ RegressionTests.class })
    @Test
    public void testRetrieveWithLock() throws Exception {

        ObjectStoreModule module = getModule();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
        String value = (String) module.retrieveWithLock(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
        assertEquals(OBJECTSTORE_VALUE, value);
    }

    @Category({ RegressionTests.class })
    @Test
    public void testRetrieveStore() throws Exception {

        ObjectStoreModule module = getModule();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
        module.retrieveStore(OBJECTSTORE_KEY, null, OBJECTSTORE_VALUE, null, null, null);
        String value = (String) module.retrieve(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
        assertEquals(OBJECTSTORE_VALUE, value);
    }

    @Category({ RegressionTests.class })
    @Test
    public void testRetrieveDefaultValue() throws Exception {

        ObjectStoreModule module = getModule();
        String value = (String) module.retrieve("muleion", "muleion", null, MulePropertyScope.INVOCATION, null);
        assertEquals("muleion", value);
    }

    @Category({ RegressionTests.class })
    @Test(expected = ObjectDoesNotExistException.class)
    public void testRetrieveFail() throws Exception {

        ObjectStoreModule module = getModule();
        module.retrieve("muleion", null, null, MulePropertyScope.INVOCATION, null);
    }

    @Category({ RegressionTests.class })
    @Test(expected = ObjectStoreException.class)
    public void testRetrieveExpired() throws Exception {

        ObjectStoreModule module = getModule();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, true);
        Thread.sleep(2000);

        module.retrieve(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
    }

    @Category({ RegressionTests.class })
    @Test
    public void testRetrieveNotExpired() throws Exception {

        ObjectStoreModule module = getModule();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, true);
        Thread.sleep(50);

        String value = (String) module.retrieve(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
        assertEquals(OBJECTSTORE_VALUE, value);
    }

    @Category({ RegressionTests.class })
    @Test
    public void testRetrieveExpiredDefaultValue() throws Exception {

        ObjectStoreModule module = getModule();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, true);
        Thread.sleep(2000L);

        String defaultValue = "default value";
        String value = (String) module.retrieve(OBJECTSTORE_KEY, defaultValue, null, MulePropertyScope.INVOCATION, null);
        assertEquals(defaultValue, value);
    }

    @After
    public void tearDown() throws Exception {
        ObjectStoreModule module = getModule();
        module.remove(OBJECTSTORE_KEY, true);
    }
}
