/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.modules;

import java.util.List;

import org.junit.Test;
import org.mule.api.MuleEvent;
import org.mule.construct.Flow;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.tck.FunctionalTestCase;

public class ObjectStoreModuleTest extends FunctionalTestCase
{
    @Override
    protected String getConfigResources()
    {
        return "mule-config.xml";
    }

    @Test
    public void testStore() throws Exception
    {
        runFlowWithPayload("store", "mulesoft2");
        runFlowWithPayload("store", "mulesoft");

        ObjectStoreModule module = muleContext.getRegistry().lookupObject(ObjectStoreModule.class);
        String value = (String)module.getObjectStoreManager().getObjectStore("test").retrieve("mykey");

        assertEquals("mulesoft", value);
    }

    @Test
    public void testDualStore() throws Exception
    {
        runFlowWithPayload("dualStore", "mulesoft");

        ObjectStoreModule module = muleContext.getRegistry().lookupObject(ObjectStoreModule.class);
        String value1 = (String)module.getObjectStoreManager().getObjectStore("test").retrieve("mykey");
        String value2 = (String)module.getObjectStoreManager().getObjectStore("test").retrieve("mulesoft");

        assertEquals("mulesoft", value1);
        assertEquals("mykey", value2);
    }

    @Test
    public void testRetrieve() throws Exception
    {
        runFlowWithPayload("store", "mulesoft");

        assertEquals("mulesoft", runFlow("retrieve"));
    }

    @Test
    public void testRetrieveDefaultValue() throws Exception
    {
        assertEquals("muleion", runFlow("retrieveDefaultValue"));
    }

    @Test
    public void testRemove() throws Exception
    {
        runFlowWithPayload("store", "mulesoft");
        runFlow("remove");
    }

    @Test
    public void testAllKeys() throws Exception
    {
        runFlowWithPayload("store", "mulesoft");
        Object keys = runFlow("allKeys");
        
        assertTrue(keys instanceof List);
        assertEquals(1, ((List<?>) keys).size());
        assertEquals("mykey", ((List<?>) keys).get(0));
        
        // Remove keys from persistent store for next tests!
        runFlow("remove");
    }
    
    /**
    * Run the flow specified by name
    *
    * @param flowName The name of the flow to run
    */
    protected <T> void runFlowWithPayload(String flowName, T payload) throws Exception
    {
        Flow flow = lookupFlowConstruct(flowName);
        MuleEvent event = AbstractMuleTestCase.getTestEvent(payload);
        MuleEvent responseEvent = flow.process(event);
    }

    /**
    * Run the flow specified by name
    *
    * @param flowName The name of the flow to run
    */
    protected Object runFlow(String flowName) throws Exception
    {
        Flow flow = lookupFlowConstruct(flowName);
        MuleEvent event = AbstractMuleTestCase.getTestEvent(null);
        MuleEvent responseEvent = flow.process(event);

        return responseEvent.getMessage().getPayload();
    }

    /**
     * Retrieve a flow by name from the registry
     *
     * @param name Name of the flow to retrieve
     */
    protected Flow lookupFlowConstruct(String name)
    {
        return (Flow) AbstractMuleTestCase.muleContext.getRegistry().lookupFlowConstruct(name);
    }
}
