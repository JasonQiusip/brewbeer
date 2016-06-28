// ILtPushServiceAidlInterface.aidl
package com.ltbrew.brewbeer.service;

// Declare any non-default types here with import statements

interface ILtPushServiceAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void sendBrewSessionCmd(long pack_id);
}
