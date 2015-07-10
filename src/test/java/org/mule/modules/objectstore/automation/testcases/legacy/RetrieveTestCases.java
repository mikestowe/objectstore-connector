/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.testcases.legacy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mule.modules.objectstore.automation.LegacyRegressionTests;
import org.mule.modules.objectstore.automation.ObjectStoreFunctionalTestParent;

import static org.junit.Assert.assertEquals;

public class RetrieveTestCases extends ObjectStoreFunctionalTestParent {

    @Before
    public void setUp() throws Exception {
        initializeTestRunMessage("retrieveTestData");
        runFlowAndGetMessage("store");
    }

    @Category({ LegacyRegressionTests.class })
    @Test
    public void testRetrieveToVariable() throws Exception {
        String expected = (String) getTestRunMessageValue("value");
        String propertyName = (String) getTestRunMessageValue("propertyName");
        String retrieved = (String) runFlowAndGetInvocationProperty("retrieve", propertyName);
        assertEquals(expected, retrieved);
    }

    @Category({ LegacyRegressionTests.class })
    @Test
    public void testRetrieveDefaultToVariable() throws Exception {
        String expected = (String) getTestRunMessageValue("default");
        String propertyName = (String) getTestRunMessageValue("propertyName");
        String retrieved = (String) runFlowAndGetInvocationProperty("retrieveDefaultToVariable", propertyName);
        assertEquals(expected, retrieved);
    }

    @After
    public void tearDown() throws Exception {

    }
}
