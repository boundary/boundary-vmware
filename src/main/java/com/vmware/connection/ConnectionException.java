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
 * ConnectionException is the base exception thrown by connection classes,
 * making this a runtime exception means that catching it is optional preventing clutter,
 * basing all connection related exceptions on this class means
 * that you may decide to catch ConnectionException to deal with any issues underneath
 * the connection infrastructure. Basing all connection classes' exceptions
 * on ConnectionException means that all new exceptions originating in the connection
 * related utilities are decoupled from any other subsystem.
 */
public class ConnectionException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public ConnectionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
