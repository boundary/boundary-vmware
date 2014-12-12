package com.boundary.metrics.vmware.poller;

import com.boundary.metrics.vmware.util.TimeUtils;
import com.google.common.base.Throwables;
import com.vmware.connection.Connection;
import com.vmware.vim25.*;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.Map;

/**
 * VMware Connection with no SSL validation
 */
public class VMwareClient implements Connection {

    private static final Logger LOG = LoggerFactory.getLogger(VMwareClient.class);

    private URI uri;
    private String username;
    private String password;
	private String name;
    private VimService vimService;
    private VimPortType vimPort;
    private UserSession userSession;
    private ServiceContent serviceContent;
    private ManagedObjectReference SVC_INST_REF;
    @SuppressWarnings("rawtypes")
    private Map headers;



    public VMwareClient(URI url, String username, String password,String name) {
        this.uri = url;
        this.username = username;
        this.password = password;
        this.name = name;
    }

    @Override
    public void setUrl(String url) {
        this.uri = URI.create(url);
    }

    @Override
    public String getUrl() {
        return uri.toASCIIString();
    }

    @Override
    public String getHost() {
        return uri.getHost();
    }

    @Override
    public Integer getPort() {
        return uri.getPort();
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getName() {
    	return this.name;
    }

    @Override
    public VimService getVimService() {
        return vimService;
    }

    @Override
    public VimPortType getVimPort() {
        return vimPort;
    }

    @Override
    public ServiceContent getServiceContent() {
        return serviceContent;
    }

    @Override
    public UserSession getUserSession() {
        return userSession;
    }

    @Override
    public String getServiceInstanceName() {
        return "ServiceInstance";
    }

    @Override
    public Map getHeaders() {
        return headers;
    }

    @Override
    public ManagedObjectReference getServiceInstanceReference() {
        return SVC_INST_REF;
    }

    @Override
    public Connection connect() {
        if (!isConnected()) {
            try {
                // Variables of the following types for access to the API methods
                // and to the vSphere inventory.
                // -- ManagedObjectReference for the ServiceInstance on the Server
                // -- VimService for access to the vSphere Web service
                // -- VimPortType for access to methods
                // -- ServiceContent for access to managed object services
                SVC_INST_REF = new ManagedObjectReference();

                // Declare a host name verifier that will automatically enable
                // the connection. The host name verifier is invoked during
                // the SSL handshake.
                HostnameVerifier hv = new HostnameVerifier() {
                    public boolean verify(String urlHostName, SSLSession session) {
                        return true;
                    }
                };

                // Create the trust manager.
                TrustManager[] trustAllCerts = new TrustManager[]{new TrustAllTrustManager()};

                // Create the SSL context
                SSLContext sc = SSLContext.getInstance("SSL");

                // Create the session context
                SSLSessionContext sslsc = sc.getServerSessionContext();

                // Initialize the contexts; the session context takes the trust manager.
                sslsc.setSessionTimeout(0);
                sc.init(null, trustAllCerts, null);

                // Use the default socket factory to create the socket for the secure connection
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                // Set the default host name verifier to enable the connection.
                HttpsURLConnection.setDefaultHostnameVerifier(hv);

                // Set up the manufactured managed object reference for the ServiceInstance
                SVC_INST_REF.setType("ServiceInstance");
                SVC_INST_REF.setValue("ServiceInstance");

                // Create a VimService object to obtain a VimPort binding provider.
                // The BindingProvider provides access to the protocol fields
                // in request/response messages. Retrieve the request context
                // which will be used for processing message requests.
                vimService = new VimService();
                vimPort = vimService.getVimPort();
                Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();

                // Store the Server URL in the request context and specify true
                // to maintain the connection between the client and server.
                // The client API will include the Server's HTTP cookie in its
                // requests to maintain the session. If you do not set this to true,
                // the Server will start a new session with each request.
                ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, uri.toASCIIString());
                ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

                // Retrieve the ServiceContent object and login
                serviceContent = vimPort.retrieveServiceContent(SVC_INST_REF);
                headers = (Map) ((BindingProvider) vimPort).getResponseContext().get(
                                MessageContext.HTTP_RESPONSE_HEADERS);
                userSession = vimPort.login(serviceContent.getSessionManager(),
                        username,
                        password,
                        null);

                // Display summary information about the product name, server type, and product version
                AboutInfo about = serviceContent.getAbout();
                LOG.info("Successfully connected to {} ({}) using API version {} (of type {})",
                        getHost(), about.getFullName(), about.getApiVersion(), about.getApiType());
            } catch (Exception e) {
                LOG.error("Unable to connect to " + getHost(), e);
            }
        }
        return this;
    }

    @Override
    public boolean isConnected() {
        if (userSession == null) {
            LOG.info("UserSession is null, disconnected");
            return false;
        }
        DateTime startTime = TimeUtils.toDateTime(userSession.getLastActiveTime());
        return startTime.plusMinutes(30).isBeforeNow();
    }

    @Override
    public Connection disconnect() {
        try {
            vimPort.logout(serviceContent.getSessionManager());
        } catch (RuntimeFaultFaultMsg e) {
            throw Throwables.propagate(e);
        } finally {
            // A connection is very memory intensive, I'm helping the garbage collector here
            userSession = null;
            serviceContent = null;
            vimPort = null;
            vimService = null;
        }
        return this;
    }

    @Override
    public URL getURL() {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw Throwables.propagate(e);
        }
    }

    // Authentication is handled by using a TrustManager and supplying
    // a host name verifier method. (The host name verifier is declared
    // in the main function.)
    private static class TrustAllTrustManager implements TrustManager, X509TrustManager {

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                                       String authType) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                                       String authType) throws CertificateException {
        }
    }
}