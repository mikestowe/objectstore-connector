/**
 * (c) 2003-2015 MuleSoft, Inc. The software in this package is
 * published under the terms of the CPAL v1.0 license, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */

package org.mule.modules.objectstore;

/**
 * Enum for Mule Property Scopes Used for improving user experience in Studio
 */
public enum MulePropertyScope {
    INVOCATION("invocation"), SESSION("session"), OUTBOUND("outbound"), INBOUND("inbound");

    private String value;

    MulePropertyScope(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return this.value();
    }
}
