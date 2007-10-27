package org.apache.xmlrpc.client;

import javax.net.ssl.SSLSocketFactory;


/**
 * Java 1.4 specific factory for the lite HTTP transport,
 * {@link org.apache.xmlrpc.client.XmlRpcLiteHttpTransport}.
 */
public class XmlRpcLite14HttpTransportFactory extends XmlRpcLiteHttpTransportFactory {
    private SSLSocketFactory sslSocketFactory;

    /**
     * Creates a new instance.
     * @param pClient The client, which will invoke the factory.
     */
    public XmlRpcLite14HttpTransportFactory(XmlRpcClient pClient) {
        super(pClient);
    }

    /**
     * Sets the SSL Socket Factory to use for https connections.
     */
    public SSLSocketFactory getSSLSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * Returns the SSL Socket Factory to use for https connections.
     */
    public void setSSLSocketFactory(SSLSocketFactory pSSLSocketFactory) {
        sslSocketFactory = pSSLSocketFactory;
    }

    public XmlRpcTransport getTransport() {
        XmlRpcLite14HttpTransport transport = new XmlRpcLite14HttpTransport(getClient());
        transport.setSSLSocketFactory(sslSocketFactory);
        return transport;
    }
}
