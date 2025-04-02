package com.verr1.controlcraft.foundation.type;


public enum Side {

    /*
    *   SERVER_ONLY: The field will only be used for server side save and load
    * */
    SERVER_ONLY,

    /*
     *   SHARED: The field will be used for server side save and load and used for syncing client field
     * */

    SHARED,


    /*
     *   RUNTIME_SHARED: The field will only be used for syncing client field
     * */
    RUNTIME_SHARED
}
