/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.testrunners;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.mule.modules.objectstore.ObjectStoreModule;
import org.mule.modules.objectstore.automation.RegressionTests;
import org.mule.modules.objectstore.automation.testcases.*;
import org.mule.tools.devkit.ctf.mockup.ConnectorTestContext;

@RunWith(Categories.class)
@Categories.IncludeCategory(RegressionTests.class)
@SuiteClasses({
        StoreTestCases.class,
        DualStoreTestCases.class,
        RetrieveTestCases.class,
        RemoveTestCases.class,
        AllKeysTestCases.class,
        ContainsTestCases.class })
public class RegressionTestSuite {

    @BeforeClass
    public static void initialiseSuite() throws Exception {
        ConnectorTestContext.initialize(ObjectStoreModule.class);
    }

    @AfterClass
    public static void shutdownSuite() throws Exception {
        ConnectorTestContext.shutDown();
    }

}
