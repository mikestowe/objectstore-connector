/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package org.mule.modules.objectstore.automation;

import org.junit.Before;
import org.mule.modules.objectstore.ObjectStoreModule;
import org.mule.tools.devkit.ctf.mockup.ConnectorDispatcher;
import org.mule.tools.devkit.ctf.mockup.ConnectorTestContext;

public class ObjectStoreTestParent {

    protected static final String OBJECTSTORE_KEY = "mykey";
    protected static final String OBJECTSTORE_VALUE = "mulesoft";

    private ObjectStoreModule module;
    private ConnectorDispatcher<ObjectStoreModule> dispatcher;

    @Before
    public void init() throws Exception {
        ConnectorTestContext<ObjectStoreModule> context = ConnectorTestContext.getInstance(ObjectStoreModule.class);
        dispatcher = context.getConnectorDispatcher();
        module = dispatcher.createMockup();
    }

    protected ObjectStoreModule getModule() {
        return module;
    }

    protected ConnectorDispatcher<ObjectStoreModule> getDispatcher() {
        return dispatcher;
    }
}
