/*
 * Copyright 1999,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.xmlrpc.test;

import java.io.IOException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClientConfig;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.common.TypeConverterFactory;
import org.apache.xmlrpc.common.TypeConverterFactoryImpl;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;

import junit.framework.TestCase;


/** Abstract base class for deriving test cases.
 */
public abstract class XmlRpcTestCase extends TestCase {
    protected ClientProvider[] providers;

    protected abstract XmlRpcHandlerMapping getHandlerMapping() throws IOException, XmlRpcException;

    protected XmlRpcClientConfigImpl getConfig(ClientProvider pProvider) throws Exception {
        return pProvider.getConfig();
    }

    protected XmlRpcClientConfig getExConfig(ClientProvider pProvider) throws Exception {
        XmlRpcClientConfigImpl config = getConfig(pProvider);
        config.setEnabledForExtensions(true);
        return config;
    }

    protected XmlRpcHandlerMapping getHandlerMapping(String pResource) throws IOException, XmlRpcException {
        PropertyHandlerMapping mapping = new PropertyHandlerMapping();
        mapping.load(getClass().getClassLoader(), getClass().getResource(pResource));
        mapping.setTypeConverterFactory(getTypeConverterFactory());
        return mapping;
    }

    public void setUp() throws Exception {
        if (providers == null) {
            XmlRpcHandlerMapping mapping = getHandlerMapping();
            providers = new ClientProvider[]{
                new LocalTransportProvider(mapping),
                new LocalStreamTransportProvider(mapping),
                new LiteTransportProvider(mapping, true),
                // new LiteTransportProvider(mapping, false), Doesn't support HTTP/1.1
                new SunHttpTransportProvider(mapping, true),
                new SunHttpTransportProvider(mapping, false),
                new CommonsProvider(mapping),
                new ServletWebServerProvider(mapping, true),
                new ServletWebServerProvider(mapping, false)
            };
        }
    }

    protected TypeConverterFactory getTypeConverterFactory() {
        return new TypeConverterFactoryImpl();
    }
}