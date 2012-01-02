/**
 * Mule Object Store Module
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.modules;

import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Module;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.api.store.ObjectAlreadyExistsException;
import org.mule.api.store.ObjectStore;
import org.mule.api.store.ObjectStoreException;
import org.mule.api.store.ObjectStoreManager;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * Generic module for accessing Object Stores.
 * <p/>
 * Can be used with Mule default implementations or one can be passed via ref. It allows to store,
 * retrieve and remove objects from the store.
 *
 * @author MuleSoft, Inc.
 */
@Module(name = "objectstore", schemaVersion = "1.0")
public class ObjectStoreModule {
    /**
     * Name of the partition in the default in-memory or persistent object stores. This argument has no
     * meaning if the object store is passed by ref using objectStore-ref.
     */
    @Configurable
    @Optional
    private String partition;

    /**
     * Specified whenever the required store needs to be persistent or not. This argument has no
     * meaning if the object store is passed by ref using objectStore-ref.
     */
    @Configurable
    @Optional
    @Default("false")
    private boolean persistent;

    /**
     * Reference to an Object Store bean. This is optional and if it is not specified then the default
     * in-memory or persistent store will be used.
     */
    @Configurable
    @Optional
    private ObjectStore objectStore;

    @Inject
    private ObjectStoreManager objectStoreManager;

    @PostConstruct
    public void init() {
        if (objectStore == null) {
            objectStore = objectStoreManager.getObjectStore(partition, persistent);
            if (objectStore == null) {
                throw new IllegalArgumentException("Unable to acquire an object store.");
            }
        }
    }

    /**
     * Store object
     * <p/>
     * {@sample.xml ../../../doc/mule-module-objectstore.xml.sample objectstore:store}
     *
     * @param key       The identifier of the object to store
     * @param value     The object to store. If you want this to be the payload then use value-ref="#[payload]".
     * @param overwrite True if you want to overwrite the existing object.
     * @throws org.mule.api.store.ObjectStoreException
     *          if the given key cannot be stored or is <code>null</code>.
     * @throws org.mule.api.store.ObjectStoreNotAvaliableException
     *          if the store is not available or any other
     *          implementation-specific error occured.
     * @throws org.mule.api.store.ObjectAlreadyExistsException
     *          if an attempt is made to store an object for a key
     *          that already has an object associated. Only thrown if overwrite is false.
     */
    @Processor
    public void store(String key, Serializable value, @Optional @Default("false") boolean overwrite) throws ObjectStoreException {
        try {
            objectStore.store(key, value);
        } catch (ObjectAlreadyExistsException e) {
            if (overwrite) {
                objectStore.remove(key);
                objectStore.store(key, value);
            } else {
                throw e;
            }
        }
    }

    /**
     * Retrieve the given Object.
     * <p/>
     * {@sample.xml ../../../doc/mule-module-objectstore.xml.sample objectstore:retrieve}
     *
     * @param key The identifier of the object to retrieve.
     * @return The object associated with the given key. If no object for the given key was found
     *         this method throws an {@link org.mule.api.store.ObjectDoesNotExistException}.
     * @throws ObjectStoreException if the given key is <code>null</code>.
     * @throws org.mule.api.store.ObjectStoreNotAvaliableException
     *                              if the store is not  available or any other
     *                              implementation-specific error occured.
     * @throws org.mule.api.store.ObjectDoesNotExistException
     *                              if no value for the given key was previously stored.
     */
    @Processor
    public Object retrieve(String key) throws ObjectStoreException {
        return objectStore.retrieve(key);
    }

    /**
     * Remove the object with key.
     * <p/>
     * {@sample.xml ../../../doc/mule-module-objectstore.xml.sample objectstore:remove}
     *
     * @param key The identifier of the object to remove.
     * @return The object that was previously stored for the given key
     * @throws ObjectStoreException if the given key is <code>null</code> or if the store is not
     *                              available or any other implementation-specific error occurred
     * @throws org.mule.api.store.ObjectDoesNotExistException
     *                              if no value for the given key was previously stored.
     */
    @Processor
    public Object remove(String key) throws ObjectStoreException {
        return objectStore.remove(key);
    }

    public ObjectStoreManager getObjectStoreManager() {
        return objectStoreManager;
    }

    public void setObjectStoreManager(ObjectStoreManager objectStoreManager) {
        this.objectStoreManager = objectStoreManager;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public void setObjectStore(ObjectStore objectStore) {
        this.objectStore = objectStore;
    }
}
