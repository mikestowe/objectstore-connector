/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.modules;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Module;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.api.config.MuleProperties;
import org.mule.api.context.MuleContextAware;
import org.mule.api.registry.Registry;
import org.mule.api.store.ListableObjectStore;
import org.mule.api.store.ObjectAlreadyExistsException;
import org.mule.api.store.ObjectDoesNotExistException;
import org.mule.api.store.ObjectStore;
import org.mule.api.store.ObjectStoreException;
import org.mule.api.store.ObjectStoreManager;
import org.mule.api.transport.PropertyScope;
import org.mule.util.StringUtils;

/**
 * Generic module for accessing Object Stores.
 * <p/>
 * Can be used with Mule default implementations or one can be passed via ref. It allows to store,
 * retrieve and remove objects from the store.
 *
 * @author MuleSoft, Inc.
 */
@Module(name = "objectstore", schemaVersion = "1.0", friendlyName = "ObjectStore")
public class ObjectStoreModule {
    /**
     * Name of the partition in the default in-memory or persistent object stores (this argument has no
     * meaning if the object store is passed by ref using objectStore-ref)
     */
    @Configurable
    @Optional
    private String partition;

    /**
     * Specified whenever the required store needs to be persistent or not (this argument has no
     * meaning if the object store is passed by ref using objectStore-ref or no partition name is defined)
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
    private ObjectStore<Serializable> objectStore;

    /**
     * TimeToLive for stored values in milliseconds. MaxEntries and ExpirationInterval are mandatory for using this param.
     */
    @Configurable
    @Optional
    private Integer entryTtl;

    /**
     * Specifies the max number of entries. EntryTTL and ExpirationInterval are mandatory for using this param.
     */
    @Configurable
    @Optional
    private Integer maxEntries;

    /**
     * Specifies the expiration check interval in milliseconds. EntryTTL and MaxEntries are mandatory for using this param.
     */
    @Configurable
    @Optional
    private Integer expirationInterval;

    @Inject
    private Registry registry;

    @Inject
    private ObjectStoreManager objectStoreManager;

    @Inject
    private MuleContext muleContext = null;
    
    private String sharedObjectStoreLockId = null;
    
    @PostConstruct
    public void init() {
        if (objectStore == null) {
            if(StringUtils.isNotEmpty(partition)) {
                if(entryTtl != null && maxEntries != null && expirationInterval != null) {
                    objectStore = objectStoreManager.getObjectStore(partition, persistent, maxEntries, entryTtl, expirationInterval);
                }
                else {
                    objectStore = objectStoreManager.getObjectStore(partition, persistent);
                }
            }

            if(objectStore == null) {
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
    @Inject
    public void dualStore(String key, Serializable value, @Optional @Default("false") boolean overwrite)
            throws ObjectStoreException {

        //For rollback purposes
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
     * Retrieve the given Object with lock.
     * <p/>
     * {@sample.xml ../../../doc/mule-module-objectstore.xml.sample objectstore:retrieve-with-lock}
     *
     * @param key          The identifier of the object to retrieve.
     * @param defaultValue The default value if the key does not exists.
     * @param targetProperty The Mule Message property where the retrieved value will be stored
     * @param targetScope  The Mule Message property scope, only used when targetProperty is specified
     * @param muleMessage  Injected Mule Message
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
    @Inject
    public Object retrieveWithLock(String key, @Optional Object defaultValue, @Optional String targetProperty,
                           @Optional @Default("INVOCATION") MulePropertyScope targetScope,
                           MuleMessage muleMessage) throws ObjectStoreException {

        Lock lock = muleContext.getLockFactory().createLock(sharedObjectStoreLockId);
        lock.lock();

        try {
            return retrieve(key, defaultValue, targetProperty, targetScope, muleMessage);
        } finally {
            lock.unlock();
        }

    }

    /**
     * Retrieve and Store in the same operation.
     * <p/>
     * {@sample.xml ../../../doc/mule-module-objectstore.xml.sample objectstore:retrieve-store}
     *
     * @param key          The identifier of the object to retrieve.
     * @param defaultValue The default value if the key does not exists.
     * @param storeValue   The object to store. If you want this to be the payload then use value-ref="#[payload]".
     * @param targetProperty The Mule Message property where the retrieved value will be stored
     * @param targetScope  The Mule Message property scope, only used when targetProperty is specified
     * @param muleMessage  Injected Mule Message
     * @return The object associated with the given key. If no object for the given key was found
     *         this method returns the defaultValue
     * @throws ObjectStoreException if the given key is <code>null</code>.
     * @throws org.mule.api.store.ObjectStoreNotAvaliableException
     *                              if the store is not  available or any other
     *                              implementation-specific error occured.
     * @throws org.mule.api.store.ObjectDoesNotExistException
     *                              if no value for the given key was previously stored.
     */
    @Processor
    @Inject
    public Object retrieveStore(String key, Object defaultValue, Serializable storeValue, @Optional String targetProperty,
                           @Optional @Default("INVOCATION") MulePropertyScope targetScope,
                           MuleMessage muleMessage) throws ObjectStoreException {

        Lock lock = muleContext.getLockFactory().createLock(sharedObjectStoreLockId);
        lock.lock();
        Object ret = null;

        try {
            ret = retrieve(key, defaultValue, targetProperty, targetScope, muleMessage);
            objectStore.store(key, storeValue);
        } catch (ObjectAlreadyExistsException e) {
            objectStore.remove(key);
            objectStore.store(key, storeValue);
        } finally {
            lock.unlock();
        }

        return ret;
    }

    /**
     * Retrieve the given Object.
     * <p/>
     * {@sample.xml ../../../doc/mule-module-objectstore.xml.sample objectstore:retrieve}
     *
     * @param key          The identifier of the object to retrieve.
     * @param defaultValue The default value if the key does not exists.
     * @param targetProperty The Mule Message property where the retrieved value will be stored
     * @param targetScope  The Mule Message property scope, only used when targetProperty is specified
     * @param muleMessage  Injected Mule Message
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
    @Inject
    public Object retrieve(String key, @Optional Object defaultValue, @Optional String targetProperty,
                           @Optional @Default("INVOCATION") MulePropertyScope targetScope,
                           MuleMessage muleMessage) throws ObjectStoreException {
        Object ret = null;
        try {
            ret = objectStore.retrieve(key);
        } catch (ObjectDoesNotExistException ose) {
            if (defaultValue != null) {
                ret = defaultValue;
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
     * @param key The identifier of the object to remove.
     * @param ignoreNotExists Indicates if the operation will ignore NotExistsException from ObjectStore.
     * @return The object that was previously stored for the given key.  If the key does not exist and
     * ignoreNotExists is true the operation will return a null object.
     * @throws ObjectStoreException if the given key is <code>null</code> or if the store is not
     *                              available or any other implementation-specific error occurred
     * @throws org.mule.api.store.ObjectDoesNotExistException
     *                              if no value for the given key was previously stored.
     */
    @Processor
    @Inject
    public Object remove(String key, @Optional @Default("false") boolean ignoreNotExists)
            throws ObjectStoreException {
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
     * @throws ObjectStoreException if an exception occurred while collecting the list of all keys.
     */
    @Processor
    public List<Serializable> allKeys() throws ObjectStoreException {
        if (objectStore instanceof ListableObjectStore) {
            return ((ListableObjectStore<?>) objectStore).allKeys();
        } else {
            throw new UnsupportedOperationException("The objectStore [" + objectStore.getClass().getName() + "] does not support the operation allKeys");
        }
    }

    /**
     * Returns whether the object store contains the given key or not
     *
     * {@sample.xml ../../../doc/mule-module-objectstore.xml.sample objectstore:contains}
     *
     * @param key The identifier of the object to validate.
     * @return true if the object store contains the key, or false if it doesn't.
     * @throws ObjectStoreException if the provided key is null.
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
        } catch(Exception ex) {
            
        }        
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

    public void setObjectStore(ObjectStore<Serializable> objectStore) {
        this.objectStore = objectStore;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public String getPartition() {
        return partition;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public ObjectStore<Serializable> getObjectStore() {
        return objectStore;
    }

    public Registry getRegistry() {
        return registry;
    }

    public Integer getEntryTtl() {
        return entryTtl;
    }

    public void setEntryTtl(Integer entryTtl) {
        this.entryTtl = entryTtl;
    }

    public Integer getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(Integer maxEntries) {
        this.maxEntries = maxEntries;
    }

    public Integer getExpirationInterval() {
        return expirationInterval;
    }

    public void setExpirationInterval(Integer expirationInterval) {
        this.expirationInterval = expirationInterval;
    }

    public void setMuleContext(MuleContext context) {
        this.muleContext = context;
    }
}
