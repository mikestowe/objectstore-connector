/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */

package org.mule.modules.objectstore;

import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.api.store.ObjectStore;

import java.io.Serializable;

@Configuration(configElementName = "config", friendlyName = "Configuration")
public class Config {

    /**
     * Name of the partition in the default in-memory or persistent object stores (this argument has no meaning if the object store is passed by ref using objectStore-ref)
     */
    @Configurable
    @Optional
    private String partition;

    /**
     * Specified whenever the required store needs to be persistent or not (this argument has no meaning if the object store is passed by ref using objectStore-ref or no partition
     * name is defined)
     */
    @Configurable
    @Default("false")
    private boolean persistent;

    /**
     * Reference to an Object Store bean. This is optional and if it is not specified then the default in-memory or persistent store will be used.
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

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public boolean getPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public ObjectStore<Serializable> getObjectStore() {
        return objectStore;
    }

    public void setObjectStore(ObjectStore<Serializable> objectStore) {
        this.objectStore = objectStore;
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
}
