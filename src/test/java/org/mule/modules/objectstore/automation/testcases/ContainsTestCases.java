/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.testcases;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mule.modules.objectstore.ObjectStoreModule;
import org.mule.modules.objectstore.automation.ObjectStoreTestParent;
import org.mule.modules.objectstore.automation.RegressionTests;

public class ContainsTestCases extends ObjectStoreTestParent {

    @Category({ RegressionTests.class })
    @Test
    public void testContains() throws Exception {
        ObjectStoreModule module = getModule();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
        Boolean response = module.contains(OBJECTSTORE_KEY);

        assertTrue(response);
    }

    @Category({ RegressionTests.class })
    @Test
    public void testNotContain() throws Exception {
        ObjectStoreModule module = getModule();
        Boolean response = module.contains("testkey");

        assertFalse(response);
    }
}
