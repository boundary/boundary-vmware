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

import com.google.common.base.Throwables;
import com.vmware.common.Main;
import com.vmware.sso.client.soaphandlers.*;
import com.vmware.sso.client.utils.SecurityUtil;
import com.vmware.sso.client.utils.Utils;
import com.vmware.vim25.*;
import com.vmware.vsphere.soaphandlers.HeaderCookieExtractionHandler;

import org.w3c.dom.Element;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;

import static com.vmware.sso.client.samples.AcquireHoKTokenByUserCredentialSample.getToken;

/**
 * Demonstrates SSO with vCenter's SSO service. This only demonstrates simple BearerToken logins.
 * @see com.vmware.sso.client.samples.AcquireHoKTokenByUserCredentialSample
 */
public class SsoConnection implements Connection {
    public final static String SSO_URL = "sso.url";

    private VimService vimService;
    private VimPortType vimPort;
    private ServiceContent serviceContent;
    private UserSession userSession;
    private ManagedObjectReference svcInstRef;

    URL url;
    private URL ssoUrl = null;
    private String username;
    private String password;
    @SuppressWarnings("rawtypes")
	private Map headers;
    private PrivateKey privateKey;
    private X509Certificate certificate;

    /**
     * You may optionally specify the system property sso.pkey.file=/fully/qualified/path
     * to load your SSL private key from a file.
     * @see com.vmware.sso.client.utils.SecurityUtil
     */
    public final String pkeyFileName = System.getProperty("sso.pkey.file");

    /**
     * You may optionally specify the system property sso.cert.file=/fully/qualified/path
     * to load your SSL cert from a file.
     * @see com.vmware.sso.client.utils.SecurityUtil
     */
    public final String certFileName = System.getProperty("sso.cert.file");

    /**
     * Will attempt to return the SSO URL you set from the command line, if you forgot or didn't set one it
     * will call getDefaultSsoUrl to attempt to calculate what the URL should have been.
     *
     * @return the URL for the SSO services
     * @throws java.net.MalformedURLException
     */
    public URL getSsoUrl() throws MalformedURLException {
        if (ssoUrl != null) {
            return ssoUrl;
        }
        String ssoUrlString = System.getProperty(SSO_URL, getDefaultSsoUrl());
        ssoUrl = new URL(ssoUrlString);
        return ssoUrl;
    }

    /**
     * Generates a default SSO URL to use if none was supplied on the command line. This will
     * attempt to use the system properties <code>sso.host</code> <code>sso.port</code> and
     * <code>sso.path</code> to construct a URL for the SSO server. These properties are all optional.
     * <p>
     * If no value is set <em>sso.host</em> will default to the url of the WS server (assuming SSO and WS are hosted
     * on the same IP)
     * </p>
     * <p>
     * If no value is set <em>sso.port</em> will default to 7444
     * </p>
     * <p>
     * If no value is set, <em>sso.path</em> will default to <code>/ims/STSService</code> which <i>may not</i> be correct.
     * </p>
     *
     * @return the URL to the SSO server to try
     */
    public String getDefaultSsoUrl() {
        String host = System.getProperty("sso.host", url.getHost());
        String port = System.getProperty("sso.port", "7444");
        String path = System.getProperty("sso.path", "/ims/STSService");
        return String.format("https://%s:%s%s", host, port, path);
    }

    @Override
    public void setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public String getUrl() {
        return this.url.toString();
    }

    @Override
    public String getHost() {
        return url.getHost();
    }

    @Override
    public Integer getPort() {
        return url.getPort();
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public VimService getVimService() {
        return this.vimService;
    }

    @Override
    public VimPortType getVimPort() {
        return this.vimPort;
    }

    @Override
    public ServiceContent getServiceContent() {
        return this.serviceContent;
    }

    @Override
    public UserSession getUserSession() {
        return this.userSession;
    }

    @Override
    public String getServiceInstanceName() {
        return "ServiceInstance"; // Theoretically this could change but it never does in these samples.
    }

    /**
     * the cached headers gleaned from the last connection atttempt
     * @return
     */
    @SuppressWarnings("rawtypes")
	@Override
    public Map getHeaders() {
        return this.headers;
    }

    /**
     * A service instance reference used to boot strap the client
     * <p/>
     * @return the top level ServiceInstanceReference
     */
    @Override
    public ManagedObjectReference getServiceInstanceReference() {
        if (svcInstRef == null) {
            ManagedObjectReference ref = new ManagedObjectReference();
            ref.setType(this.getServiceInstanceName());
            ref.setValue(this.getServiceInstanceName());
            svcInstRef = ref;
        }
        return this.svcInstRef;
    }

    /**
     * returns the token used for login to SSO
     * @return token to use for Single Sign On security
     */
    public Element login() {
        Element token = null;
        try {
            String[] args = {getSsoUrl().toString(), username, password};
            token = getToken(args, privateKey, certificate);
        } catch (Exception e) {
            throw new SSOLoginException("login fault", (e.getCause() != null)?e.getCause():e);
        }
        return token;
    }

    /**
     * sets up a VIM service registering handlers and handler resolvers. Takes an arbitrary number of handlers.
     * @see com.vmware.vim25.VimService
     * @param token - service token
     * @param handlers - arbitrary number of handlers
     * @return a properly configured VimService object
     */
    public VimService setupVimService(Element token, SSOHeaderHandler... handlers) {
        VimService vimSvc = new VimService();
        HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver();
        handlerResolver.addHandler(new TimeStampHandler());
        handlerResolver.addHandler(new SamlTokenHandler(token));
        handlerResolver.addHandler(new WsSecuritySignatureAssertionHandler(
                privateKey, certificate, Utils
                .getNodeProperty(token, "ID")));
        for (SSOHeaderHandler handler : handlers) {
            handlerResolver.addHandler(handler);
        }
        vimSvc.setHandlerResolver(handlerResolver);
        return vimSvc;
    }

    @Override
    public Connection connect() {
        if (!isConnected()) {
            try {
                _connection();
            } catch (Exception e) {
                Throwable cause = (e.getCause() != null)?e.getCause():e;
                throw new SSOLoginException(
                        "could not connect: " + e.getMessage() + " : " + cause.getMessage(), cause
                );
            }
        }
        return this;
    }

    /**
     * This method is here to separate out the exception handling from the logic of actually forming a connection.
     * The method forms the connection but does not handle any exceptions.
     * <p/>
     * @throws com.vmware.vim25.RuntimeFaultFaultMsg
     * @throws com.vmware.vim25.InvalidLocaleFaultMsg
     * @throws com.vmware.vim25.InvalidLoginFaultMsg
     */
    @SuppressWarnings("rawtypes")
	private void _connection() throws RuntimeFaultFaultMsg, InvalidLocaleFaultMsg, InvalidLoginFaultMsg {
        loadUserCert();

        Element token = login();
        HeaderCookieExtractionHandler cookieExtracter = new HeaderCookieExtractionHandler();
        vimService = setupVimService(token, cookieExtracter);
        vimPort = vimService.getVimPort();
        Map<String, Object> ctxt =
                ((BindingProvider) vimPort).getRequestContext();

        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url.toString());
        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);


        serviceContent = vimPort.retrieveServiceContent(this.getServiceInstanceReference());
        headers =
                (Map) ((BindingProvider) vimPort).getResponseContext().get(
                        MessageContext.HTTP_RESPONSE_HEADERS);
        userSession = vimPort.loginByToken(serviceContent.getSessionManager(), null);
    }

    /**
     * Load a cached key & cert from the file system if no X509Certificate is present on the
     * file system, calls "generate"
     */
    public void loadUserCert() {
        if (privateKey != null && certificate != null) {
            return;
        }

        if(pkeyFileName != null && certFileName != null) {
            SecurityUtil securityUtil = SecurityUtil.loadFromFiles(pkeyFileName,certFileName);
            privateKey = securityUtil.getPrivateKey();
            certificate = securityUtil.getUserCert();
        }

        if (privateKey == null || certificate == null) {
            generate();
        }
        return;
    }

    /**
     * generates a new key & cert and caches them for next time.
     */
    public void generate() {
        SecurityUtil userCert = SecurityUtil.generateKeyCertPair();
        privateKey = userCert.getPrivateKey();
        certificate = userCert.getUserCert();
        return;
    }

    /**
     * returns true if the connection is open, and hasn't timed out. Connections time-out every 30 minutes.
     * @return true if a good, non-stale connection
     */
    @Override
    public boolean isConnected() {
        if (userSession == null) {
            return false;
        }
        long startTime = userSession.getLastActiveTime().toGregorianCalendar().getTime().getTime();

        // 30 minutes in milliseconds = 30 minutes * 60 seconds * 1000 milliseconds
        return new Date().getTime() < startTime + 30 * 60 * 1000;
    }

    /**
     * properly disconnect the connection and set stale objects to "null" to help the garbage collector
     * in resource constrained environments.
     * @return
     */
    @Override
    public Connection disconnect() {
        if (this.isConnected()) {
            try {
                vimPort.logout(serviceContent.getSessionManager());
            } catch (RuntimeFaultFaultMsg runtimeFaultFaultMsg) {
                throw new SSOLogoutException(
                        "failure while logging out: " + runtimeFaultFaultMsg.getMessage(),
                        runtimeFaultFaultMsg.getCause()
                );
            } finally {
                // A connection is very memory intensive, I'm helping the garbage collector here
                userSession = null;
                serviceContent = null;
                vimPort = null;
                vimService = null;
            }
        }
        return this;
    }

    /**
     * gets the URL used for this connection
     * @return
     */
    @Override
    public URL getURL() {
        return this.url;
    }

    /**
     * thrown when a login has failed for a reason
     */
    public class SSOLoginException extends ConnectionException {
		private static final long serialVersionUID = 1L;
        public SSOLoginException(String s, Throwable t) {
            super(s, t);
        }
    }

    /**
     * thrown on logout when something fails
     */
    public class SSOLogoutException extends ConnectionException {
		private static final long serialVersionUID = 1L;
        public SSOLogoutException(String s, Throwable t) {
            super(s, t);
        }
    }

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}
