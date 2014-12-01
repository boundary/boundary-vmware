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

import com.vmware.common.annotations.After;
import com.vmware.common.annotations.Before;
import com.vmware.common.annotations.Option;
import com.vmware.vim25.*;

import java.net.URL;
import java.util.Map;

/**
 * This simple object shows how to set up a vCenter connection. It is intended as a utility class for use
 * by Samples that will need to connect before they can do anything useful. This is a light weight POJO
 * that should be very easy to make portable.
 */
public interface Connection {
    // getters and setters
    @Option(name = "url", systemProperty = "vimService.url", description = "full url to the vSphere WS SDK service")
    void setUrl(String url);

    String getUrl();

    String getHost();

    Integer getPort();

    @Option(name = "username", systemProperty = "connection.username", description = "username on remote system")
    void setUsername(String username);

    String getUsername();

    @Option(name = "password", systemProperty = "connection.password", description = "password on remote system")
    void setPassword(String password);

    String getPassword();

    VimService getVimService();

    VimPortType getVimPort();

    ServiceContent getServiceContent();

    UserSession getUserSession();

    String getServiceInstanceName();

    @SuppressWarnings("rawtypes")
	Map getHeaders();

    ManagedObjectReference getServiceInstanceReference();

    @Before
    Connection connect();

    boolean isConnected();

    @After
    Connection disconnect();

    URL getURL();
}
