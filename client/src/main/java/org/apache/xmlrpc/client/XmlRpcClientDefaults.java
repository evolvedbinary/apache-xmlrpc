package org.apache.xmlrpc.client;

import org.apache.xmlrpc.serializer.DefaultXMLWriterFactory;
import org.apache.xmlrpc.serializer.XmlWriterFactory;


/**
 * This class is responsible to provide default settings.
 */
public class XmlRpcClientDefaults {
    private static final XmlWriterFactory xmlWriterFactory = new DefaultXMLWriterFactory();

    /**
     * Creates a new transport factory for the given client.
     */
    public static XmlRpcTransportFactory newTransportFactory(XmlRpcClient pClient) {
        try {
            return new XmlRpcSun15HttpTransportFactory(pClient);
        } catch (Throwable t1) {
            try {
                return new XmlRpcSun14HttpTransportFactory(pClient);
            } catch (Throwable t2) {
                return new XmlRpcSunHttpTransportFactory(pClient);
            }
        }
    }

    /**
     * Creates a new instance of {@link XmlRpcClientConfig}.
     */
    public static XmlRpcClientConfig newXmlRpcClientConfig() {
        return new XmlRpcClientConfigImpl();
    }
    
    /**
     * Creates a new {@link XmlWriterFactory}.
     */
    public static XmlWriterFactory newXmlWriterFactory() {
        return xmlWriterFactory;
    }
}
