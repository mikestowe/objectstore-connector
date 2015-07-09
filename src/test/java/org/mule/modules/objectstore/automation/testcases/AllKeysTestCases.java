/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.testcases;

import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mule.modules.objectstore.ObjectStoreModule;
import org.mule.modules.objectstore.automation.ObjectStoreTestParent;
import org.mule.modules.objectstore.automation.RegressionTests;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class AllKeysTestCases extends ObjectStoreTestParent {

    @Category({ RegressionTests.class })
    @Test
    public void testAllKeys() throws Exception {
        ObjectStoreModule module = getModule();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, true);
        module.store("testkey", "testdata", true);
        List<String> keys = module.allKeys();

        assertTrue(keys instanceof List);
        assertEquals(2, ((List<?>) keys).size());
        assertTrue( keys.contains(OBJECTSTORE_KEY));
        assertTrue(keys.contains("testkey"));
    }

    @Category({ RegressionTests.class })
    @Test
    public void testAllKeysNoKeys() throws Exception {
        ObjectStoreModule module = getModule();
        List<String> keys = module.allKeys();

        assertTrue(keys instanceof List);
        assertEquals(0, ((List<?>) keys).size());
    }

    @After
    public void tearDown() throws Exception {
        ObjectStoreModule module = getModule();
        module.remove(OBJECTSTORE_KEY, true);
        module.remove("testkey", true);
    }

}