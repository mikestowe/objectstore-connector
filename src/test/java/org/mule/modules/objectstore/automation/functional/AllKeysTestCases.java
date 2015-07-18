/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.functional;

import org.junit.After;
import org.junit.Test;
import org.mule.modules.objectstore.ObjectStoreConnector;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class AllKeysTestCases extends AbstractTestClass {

    @Test
    public void testAllKeys() throws Exception {
        ObjectStoreConnector module = this.getConnector();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, true);
        module.store("testkey", "testdata", true);
        List<String> keys = module.allKeys();

        assertTrue(keys instanceof List);
        assertEquals(2, keys.size());
        assertTrue(keys.contains(OBJECTSTORE_KEY));
        assertTrue(keys.contains("testkey"));
    }

    @Test
    public void testAllKeysNoKeys() throws Exception {
        ObjectStoreConnector module = this.getConnector();
        List<String> keys = module.allKeys();

        assertTrue(keys instanceof List);
        assertEquals(0, keys.size());
    }

    @After
    public void tearDown() throws Exception {
        ObjectStoreConnector module = this.getConnector();
        module.remove(OBJECTSTORE_KEY, true);
        module.remove("testkey", true);
    }

}
