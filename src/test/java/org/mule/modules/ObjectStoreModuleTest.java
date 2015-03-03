/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.modules;

import java.util.List;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.api.MuleEvent;
import org.mule.api.store.ObjectDoesNotExistException;
import org.mule.api.store.ObjectStoreException;
import org.mule.api.transport.PropertyScope;
import org.mule.construct.Flow;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.transport.NullPayload;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class ObjectStoreModuleTest extends FunctionalTestCase
{

    private static final String OBJECTSTORE_KEY = "mykey";
    private static final String OBJECTSTORE_VALUE = "mulesoft";
    private static final String OBJECTSTORE_PARTITION = "test";
    private static final String OBJECTSTORE_CONFIG_NAME = "test-partition";

    @Override
    protected String getConfigResources()
    {
        return "mule-config.xml";
    }

    @Rule
    public ExpectedException thrownException = ExpectedException.none();

    @Test
    public void testStore() throws Exception
    {
        runFlowWithPayload("store", "mulesoft2");
        runFlowWithPayload("store", OBJECTSTORE_VALUE);

        ObjectStoreModule module = muleContext.getRegistry().lookupObject(OBJECTSTORE_CONFIG_NAME);
        String value = (String)module.getObjectStoreManager().getObjectStore(OBJECTSTORE_PARTITION).retrieve(OBJECTSTORE_KEY);
        assertEquals(OBJECTSTORE_VALUE, value);
    }

    @Test
    public void testDualStore() throws Exception
    {
        runFlowWithPayload("dualStore", OBJECTSTORE_VALUE);

        ObjectStoreModule module = muleContext.getRegistry().lookupObject(OBJECTSTORE_CONFIG_NAME);
        String value1 = (String)module.getObjectStoreManager().getObjectStore(OBJECTSTORE_PARTITION).retrieve(OBJECTSTORE_KEY);
        String value2 = (String)module.getObjectStoreManager().getObjectStore(OBJECTSTORE_PARTITION).retrieve(OBJECTSTORE_VALUE);

        assertEquals(OBJECTSTORE_VALUE, value1);
        assertEquals(OBJECTSTORE_KEY, value2);
    }

    @Test
    public void testDualStoreOverwrite() throws Exception
    {
        ObjectStoreModule module = muleContext.getRegistry().lookupObject(OBJECTSTORE_CONFIG_NAME);
        module.store(OBJECTSTORE_KEY, "myKeyValue", true);
        module.store(OBJECTSTORE_VALUE, "myKeyValue2", true);
        runFlowWithPayload("dualStoreOverwrite", OBJECTSTORE_VALUE);

        String value1 = (String)module.getObjectStoreManager().getObjectStore(OBJECTSTORE_PARTITION).retrieve(OBJECTSTORE_KEY);
        String value2 = (String)module.getObjectStoreManager().getObjectStore(OBJECTSTORE_PARTITION).retrieve(OBJECTSTORE_VALUE);

        assertEquals(OBJECTSTORE_VALUE, value1);
        assertEquals(OBJECTSTORE_KEY, value2);
    }

    @Test
    public void testRetrieveWithLock() throws Exception
    {
        runFlowWithPayload("store", OBJECTSTORE_VALUE);

        assertEquals(OBJECTSTORE_VALUE, runFlow("retrieve-with-lock"));
    }

    @Test
    public void testRetrieveStore() throws Exception
    {
        runFlowWithPayload("store", OBJECTSTORE_VALUE);

        assertEquals(OBJECTSTORE_VALUE, runFlow("retrieve-store"));
    }

    @Test
    public void testRetrieve() throws Exception
    {
        runFlowWithPayload("store", OBJECTSTORE_VALUE);

        assertEquals(OBJECTSTORE_VALUE, runFlow("retrieve"));
    }

    @Test
    public void testRetrieveDefaultValue() throws Exception
    {
        assertEquals("muleion", runFlow("retrieveDefaultValue"));
    }

    @Test
    public void testRetrieveToInvocationVariable() throws Exception
    {
        runFlowWithPayload("store", OBJECTSTORE_VALUE);
        Flow flow = lookupFlowConstruct("retrieveVariable");
        MuleEvent event = getTestEvent(null);
        MuleEvent responseEvent = flow.process(event);

        assertEquals(OBJECTSTORE_VALUE, responseEvent.getMessage().getProperty("targetProperty", PropertyScope.SESSION));
    }

    @Test
    public void testRemove() throws Exception
    {
        runFlowWithPayload("store", OBJECTSTORE_VALUE);
        Object removedObject = runFlow("remove");

        assertEquals(OBJECTSTORE_VALUE, removedObject);
    }

    @Test
    public void testRemoveNotExists() throws Exception
    {
        Object removedObject = runFlow("removeNotExists");

        assertTrue(removedObject instanceof NullPayload);
    }

    @Test
    public void testAllKeys() throws Exception
    {
        runFlowWithPayload("store", OBJECTSTORE_VALUE);
        Object keys = runFlow("allKeys");
        
        assertTrue(keys instanceof List);
        assertEquals(1, ((List<?>) keys).size());
        assertEquals(OBJECTSTORE_KEY, ((List<?>) keys).get(0));
        
        // Remove keys from persistent store for next tests!
        runFlow("remove");
    }

    @Test
    public void testRetrieveExpirable() throws Exception
    {
        runFlowWithPayload("testStoreExpirable", OBJECTSTORE_VALUE);
        Thread.sleep(2000L);

        ObjectStoreModule module = muleContext.getRegistry().lookupObject("test-partition-expirable");
        thrownException.expect(org.mule.api.store.ObjectDoesNotExistException.class);
        module.getObjectStoreManager().getObjectStore(OBJECTSTORE_PARTITION).retrieve(OBJECTSTORE_KEY);
    }

    @Test
    public void testContains() throws Exception {
        runFlowWithPayload("store", OBJECTSTORE_VALUE);
        Boolean response = (Boolean) runFlowWithPayload("contains", OBJECTSTORE_KEY);

        assertTrue(response);
    }

    @Test
    public void testNotContain() throws Exception {
        Boolean response = (Boolean) runFlowWithPayload("contains", OBJECTSTORE_KEY);

        assertFalse(response);
    }

    @After
    public void tearDown() throws ObjectStoreException {
        //clean object store
        ObjectStoreModule module = muleContext.getRegistry().lookupObject(OBJECTSTORE_CONFIG_NAME);
        removeFromObjectStore(module, OBJECTSTORE_KEY, OBJECTSTORE_VALUE);
    }
    
    /**
    * Run the flow specified by name
    *
    * @param flowName The name of the flow to run
    */
    protected <T> Object runFlowWithPayload(String flowName, T payload) throws Exception
    {
        Flow flow = lookupFlowConstruct(flowName);
        MuleEvent event = getTestEvent(payload);
        MuleEvent responseEvent = flow.process(event);
        return responseEvent.getMessage().getPayload();
    }

    /**
    * Run the flow specified by name
    *
    * @param flowName The name of the flow to run
    */
    protected Object runFlow(String flowName) throws Exception
    {
        Flow flow = lookupFlowConstruct(flowName);
        MuleEvent event = getTestEvent(null);
        MuleEvent responseEvent = flow.process(event);

        return responseEvent.getMessage().getPayload();
    }

    /**
     * Retrieve a flow by name from the registry
     *
     * @param name Name of the flow to retrieve
     */
    protected Flow lookupFlowConstruct(String name) throws Exception {
        return (Flow) getTestService().getMuleContext().getRegistry().lookupFlowConstruct(name);
    }

    /**
     * Removes the specified keys from the Object Store, using the test partition
     * @param module Object Store module
     * @param keys list of keys to be removed
     */
    private void removeFromObjectStore(ObjectStoreModule module, String ... keys) throws ObjectStoreException {
        for (String key : keys) {
            try {
                module.getObjectStoreManager().getObjectStore(OBJECTSTORE_PARTITION).remove(key);
            } catch (ObjectDoesNotExistException e) {
                //Do nothing if the key doesn't exist
            }
        }
    }
}
