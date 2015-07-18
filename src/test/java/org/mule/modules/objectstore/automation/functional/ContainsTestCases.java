/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.functional;

import org.junit.Test;
import org.mule.modules.objectstore.ObjectStoreConnector;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class ContainsTestCases extends AbstractTestCase {

    @Test
    public void testContains() throws Exception {
        ObjectStoreConnector module = this.getConnector();
        module.store(OBJECTSTORE_KEY, OBJECTSTORE_VALUE, false);
        Boolean response = module.contains(OBJECTSTORE_KEY);

        assertTrue(response);
    }

    @Test
    public void testNotContain() throws Exception {
        ObjectStoreConnector module = this.getConnector();
        Boolean response = module.contains("testkey");

        assertFalse(response);
    }
}
