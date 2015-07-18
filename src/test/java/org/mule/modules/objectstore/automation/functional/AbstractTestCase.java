/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation.functional;

import org.mule.modules.objectstore.ObjectStoreConnector;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

abstract class AbstractTestCase extends org.mule.tools.devkit.ctf.junit.AbstractTestCase<ObjectStoreConnector> {

    protected static final String OBJECTSTORE_KEY = "mykey";
    protected static final String OBJECTSTORE_VALUE = "mulesoft";

    public AbstractTestCase() {
        this.setConnectorClass(ObjectStoreConnector.class);
    }

}
