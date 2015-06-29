/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */

package org.mule.modules.objectstore;

import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.annotations.Category;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Module;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.api.config.MuleProperties;
import org.mule.api.registry.Registry;
import org.mule.api.store.*;
import org.mule.api.transport.PropertyScope;
import org.mule.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;

/**
 * Generic module for accessing Object Stores.
 * <p/>
 * Can be used with Mule default implementations or one can be passed via ref. It allows to store, retrieve and remove objects from the store.
 *
 * @author MuleSoft, Inc.
 */
@Module(name = "objectstore", schemaVersion = "1.0", friendlyName = "ObjectStore")
@Category(name = "org.mule.tooling.category.transformers", description = "Transformers")
public class ObjectStoreModule {

    @Config
    private ObjectStoreConfiguration configuration;

    @Inject
    private Registry registry;

    @Inject
    private ObjectStoreManager objectStoreManager;

    @Inject
    private MuleContext muleContext = null;

    private String sharedObjectStoreLockId = null;
    private String partition;
    private boolean persistent;
    private ObjectStore<Serializable> objectStore;
    private Integer entryTtl;
    private Integer maxEntries;
    private Integer expirationInterval;

    @PostConstruct
    public void init() {
        if (objectStore == null) {
            if (StringUtils.isNotEmpty(partition)) {
                if (entryTtl != null && maxEntries != null && expirationInterval != null) {
                    objectStore = objectStoreManager.getObjectStore(partition, persistent, maxEntries, entryTtl, expirationInterval);
                } else {
                    objectStore = objectStoreManager.getObjectStore(partition, persistent);
                }
            }

            if (objectStore == null) {
                objectStore = registry.lookupObject(MuleProperties.DEFAULT_USER_OBJECT_STORE_NAME);
            }

            if (objectStore == null) {
                throw new IllegalArgumentException("Unable to acquire an object store.");
            }
        }

        if (sharedObjectStoreLockId == null) {
            sharedObjectStoreLockId = new Random().nextInt(1000) + "-" + System.currentTimeMillis() + "-lock";
        }
    }

    /**
     * Store object
     * <p/>
     * {@sample.xml ../../../doc/mule-module-objectstore.xml.sample objectstore:store}
     *
     * @param key
     *            The identifier of the object to store
     * @param value
     *            The object to store. If you want this to be the payload then use value-ref="#[payload]".
     * @param overwrite
     *            True if you want to overwrite the existing object.
     * @throws org.mule.api.store.ObjectStoreException
     *             if the given key cannot be stored or is <code>null</code>.
     * @throws org.mule.api.store.ObjectStoreNotAvaliableException
     *             if the store is not available or any other implementation-specific error occured.
     * @throws org.mule.api.store.ObjectAlreadyExistsException
     *             if an attempt is made to store an object for a key that already has an object associated. Only thrown if overwrite is false.
     */
    @Processor
    public void store(String key, Serializable value, @Default("false") boolean overwrite) throws ObjectStoreException {
        Lock lock = muleContext.getLockFactory().createLock(sharedObjectStoreLockId);
        lock.lock();
        try {
            objectStore.store(key, value);
        } catch (ObjectAlreadyExistsException e) {
            if (overwrite) {
                objectStore.remove(key);
                objectStore.store(key, value);
            } else {
                throw e;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Store value using key, and also store key using value. If an exception is thrown rolls back both operations.
     * <p/>
     * {@sample.xml ../../../doc/mule-module-objectstore.xml.sample objectstore:dual-store}
     *
     * @param key
     *            The identifier of the object to store
     * @param value
     *            The object to store. If you want this to be the payload then use value-ref="#[payload]".
     * @param overwrite
     *            True if you want to overwrite the existing object.
     * @throws org.mule.api.store.ObjectStoreException
     *             if the given key cannot be stored or is <code>null</code>.
     * @throws org.mule.api.store.ObjectStoreNotAvaliableException
     *             if the store is not available or any other implementation-specific error occured.
     * @throws org.mule.api.store.ObjectAlreadyExistsException
     *             if an attempt is made to store an object for a key that already has an object associated. Only thrown if overwrite is false.
     */
    @Processor
    public void dualStore(String key, Serializable value, @Default("false") boolean overwrite) throws ObjectStoreException {

        // For rollback purposes
        Serializable previousValue = null;
        Lock lock = muleContext.getLockFactory().createLock(sharedObjectStoreLockId);
        lock.lock();
        try {
            try {
                objectStore.store(key, value);
            } catch (ObjectAlreadyExistsException e) {
                if (overwrite) {
                    previousValue = objectStore.retrieve(key);
                    objectStore.remove(key);
                    objectStore.store(key, value);
                } else {
                    throw e;
                }
            }

            try {
                objectStore.store(value, key);
            } catch (ObjectAlreadyExistsException e) {
                if (overwrite) {
                    objectStore.remove(value);
                    objectStore.store(value, key);
                } else {
                    rollbackDualStore(key, value, previousValue);
                    throw e;
                }
            } catch (Exception e) {
                rollbackDualStore(key, value, previousValue);
                throw new ObjectStoreException(e);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieve the given Object.
     * <p/>
     * {@sample.xml ../../../doc/mule-module-objectstore.xml.sample objectstore:retrieve}
     *
     * @param key
     *            The identifier of the object to retrieve.
     * @param defaultValue
     *            The default value if the key does not exists.
     * @param targetProperty
     *            The Mule Message property where the retrieved value will be stored
     * @param targetScope
     *            The Mule Message property scope, only used when targetProperty is specified
     * @param muleMessage
     *            Injected Mule Message
     * @return The object associated with the given key. If no object for the given key was found this method throws an {@link org.mule.api.store.ObjectDoesNotExistException}.
     * @throws ObjectStoreException
     *             if the given key is <code>null</code>.
     * @throws org.mule.api.store.ObjectStoreNotAvaliableException
     *             if the store is not available or any other implementation-specific error occured.
     * @throws org.mule.api.store.ObjectDoesNotExistException
     *             if no value for the given key was previously stored.
     */
    @Processor
    public Object retrieve(String key, @Optional Object defaultValue, @Optional String targetProperty, @Default("INVOCATION") MulePropertyScope targetScope,
            MuleMessage muleMessage) throws ObjectStoreException {
        Object ret = null;
        try {
            ret = objectStore.retrieve(key);
        } catch (ObjectDoesNotExistException ose) {
            if (defaultValue != null) {
                return defaultValue;
            } else {
                throw ose;
            }
        }

        if (targetProperty != null) {
            muleMessage.setProperty(targetProperty, ret, PropertyScope.get(targetScope.value()));
        }

        return ret;
    }

    /**
     * Remove the object with key.
     * <p/>
     * {@sample.xml ../../../doc/mule-module-objectstore.xml.sample objectstore:remove}
     *
     * @param key
     *            The identifier of the object to remove.
     * @param ignoreNotExists
     *            Indicates if the operation will ignore NotExistsException from ObjectStore.
     * @return The object that was previously stored for the given key. If the key does not exist and ignoreNotExists is true the operation will return a null object.
     * @throws ObjectStoreException
     *             if the given key is <code>null</code> or if the store is not available or any other implementation-specific error occurred
     * @throws org.mule.api.store.ObjectDoesNotExistException
     *             if no value for the given key was previously stored.
     */
    @Processor
    public Object remove(String key, @Default("false") boolean ignoreNotExists) throws ObjectStoreException {
        Lock lock = muleContext.getLockFactory().createLock(sharedObjectStoreLockId);
        lock.lock();
        try {
            return objectStore.remove(key);
        } catch (ObjectDoesNotExistException e) {
            if (ignoreNotExists) {
                return null;
            } else {
                throw e;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns a list of all the keys in the store.
     * <p/>
     * <i><b>IMPORTANT:</b> Not all stores support this method. If the method is not supported a java.lang.UnsupportedOperationException is thrown</i>
     * <p/>
     * {@sample.xml ../../../doc/mule-module-objectstore.xml.sample objectstore:all-keys}
     *
     * @return a java.util.List with all the keys in the store.
     * @throws ObjectStoreException
     *             if an exception occurred while collecting the list of all keys.
     */
    @Processor
    public List<String> allKeys() throws ObjectStoreException {
        if (objectStore instanceof ListableObjectStore) {
            List<Serializable> allkeys = ((ListableObjectStore<?>) objectStore).allKeys();
            List<String> list = new ArrayList<String>();
            for(Serializable key: allkeys){
                if(key instanceof String) {
                    list.add((String) key);
                }
                else{
                    throw new UnsupportedOperationException("The objectStore [" + objectStore.getClass().getName() +
                            "] supports only keys of type: " + String.class.getName());
                }
            }
            return list;
        } else {
            throw new UnsupportedOperationException("The objectStore [" + objectStore.getClass().getName() +
                    "] does not support the operation allKeys");
        }
    }

    /**
     * Returns whether the object store contains the given key or not
     *
     * {@sample.xml ../../../doc/mule-module-objectstore.xml.sample objectstore:contains}
     *
     * @param key
     *            The identifier of the object to validate.
     * @return true if the object store contains the key, or false if it doesn't.
     * @throws ObjectStoreException
     *             if the provided key is null.
     */
    @Processor
    public boolean contains(String key) throws ObjectStoreException {
        return objectStore.contains(key);
    }

    /*
     * This method is executed inside a lock
     */
    private void rollbackDualStore(String key, Serializable value, Serializable previousValue) throws ObjectStoreException {
        silentlyDelete(key);
        if (previousValue != null) {
            objectStore.store(key, previousValue);
        }
    }

    /*
     * This method is executed inside a lock
     */
    private void silentlyDelete(String key) {
        try {
            objectStore.remove(key);
        } catch (Exception ex) {

        }
    }

    public ObjectStoreManager getObjectStoreManager() {
        return objectStoreManager;
    }

    public void setObjectStoreManager(ObjectStoreManager objectStoreManager) {
        this.objectStoreManager = objectStoreManager;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setMuleContext(MuleContext context) {
        this.muleContext = context;
    }

    public ObjectStoreConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ObjectStoreConfiguration configuration) {
        this.configuration = configuration;
        this.partition = configuration.getPartition();
        this.persistent = configuration.getPersistent();
        this.objectStore = configuration.getObjectStore();
        this.entryTtl = configuration.getEntryTtl();
        this.maxEntries = configuration.getMaxEntries();
        this.expirationInterval = configuration.getExpirationInterval();
    }
}
