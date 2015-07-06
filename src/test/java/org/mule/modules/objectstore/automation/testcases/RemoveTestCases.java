/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.testcases;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mule.api.store.ObjectDoesNotExistException;
import org.mule.modules.objectstore.ObjectStoreModule;
import org.mule.modules.objectstore.automation.ObjectStoreTestParent;
import org.mule.modules.objectstore.automation.RegressionTests;
import org.mule.transport.NullPayload;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class RemoveTestCases extends ObjectStoreTestParent {

    @Before
    public void setUp() throws Exception {
        ObjectStoreModule module = getModule();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
    }

    @Category({ RegressionTests.class })
    @Test
    public void testRemove() throws Exception {
        ObjectStoreModule module = getModule();
        String value = (String) module.remove(OBJECTSTORE_KEY, false);
        assertEquals(OBJECTSTORE_VALUE, value);
    }

    @Category({ RegressionTests.class })
    @Test(expected = ObjectDoesNotExistException.class)
    public void testRemoveFail() throws Exception {
        ObjectStoreModule module = getModule();
        module.remove("muleion", false);
    }

    @Test
    public void testRemoveNotExists() throws Exception {
        ObjectStoreModule module = getModule();
        Object removedObject = module.remove("muleion", false);
        assertTrue(removedObject instanceof NullPayload);
    }

    @After
    public void tearDown() throws Exception {
        ObjectStoreModule module = getModule();
        module.remove(OBJECTSTORE_KEY, true);
    }
}
