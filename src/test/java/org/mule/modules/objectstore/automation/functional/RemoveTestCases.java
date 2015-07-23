/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.functional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.api.store.ObjectDoesNotExistException;
import org.mule.modules.objectstore.ObjectStoreConnector;

import static junit.framework.Assert.assertEquals;

public class RemoveTestCases extends AbstractTestCase {

    ObjectStoreConnector module;

    @Before
    public void setUp() throws Exception {
        module = this.getConnector();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
    }

    @Test
    public void testRemove() throws Exception {
        String value = (String) module.remove(OBJECTSTORE_KEY, false);
        assertEquals(OBJECTSTORE_VALUE, value);
    }

    @Test(expected = ObjectDoesNotExistException.class)
    public void testRemoveFail() throws Exception {
        module.remove("muleion", false);
    }

    @After
    public void tearDown() throws Exception {
        module.remove(OBJECTSTORE_KEY, true);
    }
}
