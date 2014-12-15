/*
 * ******************************************************
 * Copyright VMware, Inc. 2010-2012.  All Rights Reserved.
 * ******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS # OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY # DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY # QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.vmware.connection;

/**
 * Holds the logic for how to construct a connection class. We allow the
 * specific implementation of the connection class to change underneath
 * the Connection interface. The specific implementation
 */

public class ConnectionFactory {
    /**
     * Constructs a connection class based on the system property
     * com.vmware.connection.Connection=some.class.name     * @return instance of a class that implements the Connection interface
     * @throws ClassNotFoundException thrown when jvm cannot find a class to load
     * @throws IllegalAccessException thrown when an instance attempts to create an instance or invoke a method
     * @throws InstantiationException thrown when unable to create an instance of a class
     */
	public static Connection newConnection() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String connectionClassName = System.getProperty(
                Connection.class.getCanonicalName(),
                // makes the default the SsoConnection class
                SsoConnection.class.getCanonicalName()
        );
        Class<?> connectionClass = Class.forName(connectionClassName);
        return (Connection) connectionClass.newInstance();
    }
}
