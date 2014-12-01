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

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VimPortType;

/**
 * This is a keep-alive utility class. It will keep an instance of a connection alive by polling the "currentTime"
 * method on the remote Host or vCenter that the supplied connection & VimPortType were talking to.
 * @see com.vmware.vim25.VimPortType
 */
public class KeepAlive implements Runnable {
    public static final Long DEFAULT_INTERVAL = 300000l;
    private boolean verbose = Boolean.parseBoolean(System.getProperty("keep-alive.verbose", "false"));
    private Boolean running;
    private final Long interval;
    private final VimPortType vimPort;
    private final ManagedObjectReference serviceInstanceReference;

    /**
     * this class is immutable and acts on the supplied vimPort and serviceInstanceReference the default
     * interval is set to 300000 milliseconds
     * @param vimPort
     * @param serviceInstanceReference
     */
    public KeepAlive(final VimPortType vimPort, final ManagedObjectReference serviceInstanceReference) {
        this(vimPort,serviceInstanceReference,DEFAULT_INTERVAL);
    }

    /**
     * builds an instance of this object
     * @param vimPort
     * @param serviceInstanceReference
     * @param interval
     */
    public KeepAlive(final VimPortType vimPort, final ManagedObjectReference serviceInstanceReference, final Long interval) {
        this.vimPort = vimPort;
        this.serviceInstanceReference = serviceInstanceReference;
        this.interval = interval;
        this.running = Boolean.TRUE;
    }

    /**
     * kicks off a thread that will call the "keep alive" method on the connection instance
     */
    public void keepAlive() {
        try {
            run(vimPort, serviceInstanceReference);
        } catch (RuntimeFaultFaultMsg runtimeFaultFaultMsg) {
            runtimeFaultFaultMsg.printStackTrace();
        } catch (Exception e) {
            stop();
        }
    }

    /**
     * calls "currentTime" against the supplied objects
     * @param vimPort
     * @param serviceInstanceRef
     * @throws com.vmware.vim25.RuntimeFaultFaultMsg
     */
    public static void run(final VimPortType vimPort, final ManagedObjectReference serviceInstanceRef) throws RuntimeFaultFaultMsg {
        vimPort.currentTime(serviceInstanceRef);
    }

    /**
     * @return true if the embedded thread is running
     */
    public boolean isRunning() {
        final Boolean val;
        synchronized (running) {
            val = running;
        }
        return val;
    }

    /**
     * signals the embedded thread to stop
     */
    public void stop() {
        synchronized (running) {
            if (verbose) {
                System.out.println("keep alive stopped");
            }
            running = false;
        }
    }

    /**
     * starts a keep-alive thread which will call keepAlive then sleep for the interval
     */
    @Override
    public void run() {
        synchronized (running) {
            running = true;
        }
        try {
            while (isRunning()) {
                if (verbose) {
                    System.out.println("keep alive");
                }
                keepAlive();
                Thread.sleep(interval);
            }
        } catch (Throwable t) {
            stop();
        }
    }

    /**
     * Returns a thread you can start to run a keep alive on your connection. You supply it with your copy of
     * the vimPort and serviceInstanceRef to ping. Call start on the thread when you need to start the keep-alive.
     *
     * @param vimPort
     * @param serviceInstanceRef
     * @return
     */
    public static Thread keepAlive(VimPortType vimPort, ManagedObjectReference serviceInstanceRef) {
        return keepAlive(vimPort, serviceInstanceRef, DEFAULT_INTERVAL);
    }

    /**
     * constructs a new embedded thread to keep alive
     * @param vimPort
     * @param serviceInstanceRef
     * @param interval
     * @return
     */
    public static Thread keepAlive(VimPortType vimPort, ManagedObjectReference serviceInstanceRef, Long interval) {
        Thread thread = new Thread(new KeepAlive(vimPort, serviceInstanceRef, interval));
        return thread;
    }
}
