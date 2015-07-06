/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.testcases;

import static junit.framework.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mule.api.store.ObjectStoreException;
import org.mule.modules.objectstore.MulePropertyScope;
import org.mule.modules.objectstore.ObjectStoreModule;
import org.mule.modules.objectstore.automation.ObjectStoreTestParent;
import org.mule.modules.objectstore.automation.RegressionTests;

public class DualStoreTestCases extends ObjectStoreTestParent {

    @Category({ RegressionTests.class })
    @Test
    public void testDualStore() throws Exception {

        ObjectStoreModule module = getModule();
        module.dualStore(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
        String value = (String) module.retrieve(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
        String key = (String) module.retrieve(OBJECTSTORE_VALUE, null, null, MulePropertyScope.INVOCATION, null);

        assertEquals(OBJECTSTORE_VALUE, value);
        assertEquals(OBJECTSTORE_KEY, key);
    }

    @Category({ RegressionTests.class })
    @Test
    public void testDualStoreOverwrite() throws Exception {

        ObjectStoreModule module = getModule();
        module.store(OBJECTSTORE_KEY, "myKeyValue", true);
        module.store(OBJECTSTORE_VALUE, "myKeyValue2", true);
        module.dualStore(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, true);
        String value = (String) module.retrieve(OBJECTSTORE_KEY, null, null, MulePropertyScope.INVOCATION, null);
        String key = (String) module.retrieve(OBJECTSTORE_VALUE, null, null, MulePropertyScope.INVOCATION, null);

        assertEquals(OBJECTSTORE_VALUE, value);
        assertEquals(OBJECTSTORE_KEY, key);
    }

    @Category({ RegressionTests.class })
    @Test(expected = ObjectStoreException.class)
    public void testDualStoreFailOnOverwrite() throws Exception {

        ObjectStoreModule module = getModule();
        module.dualStore(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
        module.dualStore(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
    }

    @After
    public void tearDown() throws Exception {
        ObjectStoreModule module = getModule();
        module.remove(OBJECTSTORE_KEY, true);
        module.remove(OBJECTSTORE_VALUE, true);
    }
}
