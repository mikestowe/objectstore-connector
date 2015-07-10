/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.testrunners;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.mule.modules.objectstore.automation.FullRegressionTests;

@RunWith(Suite.class)
@Categories.IncludeCategory(FullRegressionTests.class)
@Suite.SuiteClasses({
        RegressionTestSuite.class,
        LegacyRegressionTestSuite.class })
public class FullRegressionTestSuite {
}
