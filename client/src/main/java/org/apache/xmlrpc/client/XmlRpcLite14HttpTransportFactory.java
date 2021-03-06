/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
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
     * @return the SSL Socket Factory
     */
    public SSLSocketFactory getSSLSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * Returns the SSL Socket Factory to use for https connections.
     * @param pSSLSocketFactory the SSL Socket Factory
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
