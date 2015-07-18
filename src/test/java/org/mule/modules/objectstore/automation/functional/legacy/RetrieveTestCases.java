/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.functional.legacy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.modules.tests.ConnectorTestCase;

import static org.junit.Assert.assertEquals;

public class RetrieveTestCases extends ConnectorTestCase {

    @Before
    public void setUp() throws Exception {
        initializeTestRunMessage("retrieveTestData");
        runFlowAndGetMessage("store");
    }

    @Test
    public void testRetrieveToVariable() throws Exception {
        String expected = getTestRunMessageValue("value");
        String propertyName = getTestRunMessageValue("propertyName");
        String retrieved = runFlowAndGetInvocationProperty("retrieve", propertyName);
        assertEquals(expected, retrieved);
    }

    @Test
    public void testRetrieveDefaultToVariable() throws Exception {
        String expected = getTestRunMessageValue("default");
        String propertyName = getTestRunMessageValue("propertyName");
        String retrieved = runFlowAndGetInvocationProperty("retrieveDefaultToVariable", propertyName);
        assertEquals(expected, retrieved);
    }

    @After
    public void tearDown() throws Exception {

    }
}
