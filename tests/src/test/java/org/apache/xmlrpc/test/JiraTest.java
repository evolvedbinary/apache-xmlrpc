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
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.util.ClientFactory;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;


/**
 * Test case for various jira issues.
 */ 
public class JiraTest extends XmlRpcTestCase {
    /** Interface of the handler for {@link JiraTest#testXMLRPC89()}
     */
    public interface XMLRPC89Handler {
        /**
         * Returns the reversed vector.
         */
        Vector reverse(Vector pVector);
        /**
         * Returns the same hashtable, but doubles the
         * values.
         */
        Hashtable doubledValues(Hashtable pMap);
        /**
         * Returns the same properties, but doubles the
         * values.
         */
        Properties doubledPropertyValues(Properties pMap);
    }

    /**
     * Handler for {@link JiraTest#testXMLRPC89()}
     */ 
    public static class XMLRPC89HandlerImpl implements XMLRPC89Handler {
        public Vector reverse(Vector pVector) {
            Vector result = new Vector(pVector.size());
            result.addAll(pVector);
            Collections.reverse(result);
            return result;
        }
        public Hashtable doubledValues(Hashtable pMap) {
            final Hashtable result;
            if (pMap instanceof Properties) {
                result = new Properties();
            } else {
                result = new Hashtable();
            }
            result.putAll(pMap);
            for (Iterator iter = result.entrySet().iterator();  iter.hasNext();  ) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object value = entry.getValue();
                final Integer i;
                if (pMap instanceof Properties) {
                    i = Integer.valueOf((String) value);
                } else {
                    i = (Integer) value;
                }
                Integer iDoubled = new Integer(i.intValue()*2);
                if (pMap instanceof Properties) {
                    entry.setValue(iDoubled.toString());
                } else {
                    entry.setValue(iDoubled);
                }
            }
            return result;
        }
        public Properties doubledPropertyValues(Properties pProperties) {
            return (Properties) doubledValues(pProperties);
        }
    }

    protected XmlRpcHandlerMapping getHandlerMapping() throws IOException,
            XmlRpcException {
        return new PropertyHandlerMapping(getClass().getClassLoader(),
                getClass().getResource("JiraTest.properties"),
                getTypeConverterFactory(),
                true);
    }

    /**
     * Test case for <a href="http://issues.apache.org/jira/browse/XMLRPC-89">
     * XMLRPC-89</a>
     */
    public void testXMLRPC89() throws Exception {
        for (int i = 0;  i < providers.length;  i++) {
            testXMLRPC89Vector(providers[i]);
            testXMLRPC89Hashtable(providers[i]);
            testXMLRPC89Properties(providers[i]);
        }
    }

    private void testXMLRPC89Vector(ClientProvider pProvider) throws Exception {
        Vector values = new Vector();
        for (int i = 0;  i < 3;  i++) {
            values.add(new Integer(i));
        }
        Vector params = new Vector();
        params.add(values);
        XmlRpcClient client = pProvider.getClient();
        client.setConfig(getConfig(pProvider));
        Object res = client.execute(XMLRPC89Handler.class.getName() + ".reverse", params);
        Object[] result = (Object[]) res;
        assertNotNull(result);
        assertEquals(3, result.length);
        for (int i = 0;  i < 3;  i++) {
            assertEquals(new Integer(2-i), result[i]);
        }

        ClientFactory factory = new ClientFactory(client);
        XMLRPC89Handler handler = (XMLRPC89Handler) factory.newInstance(XMLRPC89Handler.class);
        Vector resultVector = handler.reverse(values);
        assertNotNull(resultVector);
        assertEquals(3, resultVector.size());
        for (int i = 0;  i < 3;  i++) {
            assertEquals(new Integer(2-i), resultVector.get(i));
        }
    }

    private void verifyXMLRPC89Hashtable(Map pMap) {
        assertNotNull(pMap);
        assertEquals(3, pMap.size());
        for (int i = 0;  i < 3;  i++) {
            Integer j = (Integer) pMap.get(String.valueOf(i));
            assertEquals(i*2, j.intValue());
        }
    }

    private void testXMLRPC89Hashtable(ClientProvider pProvider) throws Exception {
        Hashtable values = new Hashtable();
        for (int i = 0;  i < 3;  i++) {
            values.put(String.valueOf(i), new Integer(i));
        }
        XmlRpcClient client = pProvider.getClient();
        client.setConfig(getConfig(pProvider));
        Object res = client.execute(XMLRPC89Handler.class.getName() + ".doubledValues", new Object[]{values});
        verifyXMLRPC89Hashtable((Map) res);

        ClientFactory factory = new ClientFactory(client);
        XMLRPC89Handler handler = (XMLRPC89Handler) factory.newInstance(XMLRPC89Handler.class);
        Hashtable result = handler.doubledValues(values);
        verifyXMLRPC89Hashtable(result);
    }

    private void verifyXMLRPC89Properties(Map pMap) {
        assertNotNull(pMap);
        assertEquals(3, pMap.size());
        for (int i = 0;  i < 3;  i++) {
            String j = (String) pMap.get(String.valueOf(i));
            assertEquals(i*2, Integer.parseInt(j));
        }
    }

    private void testXMLRPC89Properties(ClientProvider pProvider) throws Exception {
        Properties values = new Properties();
        for (int i = 0;  i < 3;  i++) {
            values.put(String.valueOf(i), String.valueOf(i));
        }
        XmlRpcClient client = pProvider.getClient();
        client.setConfig(getConfig(pProvider));
        Object res = client.execute(XMLRPC89Handler.class.getName() + ".doubledPropertyValues", new Object[]{values});
        verifyXMLRPC89Properties((Map) res);

        ClientFactory factory = new ClientFactory(client);
        XMLRPC89Handler handler = (XMLRPC89Handler) factory.newInstance(XMLRPC89Handler.class);
        Properties result = handler.doubledPropertyValues(values);
        verifyXMLRPC89Properties(result);
    }

    /** Handler for XMLRPC-96
     */
    public static class XMLRPC96Handler {
        /** Returns the "Hello, world!" string.
         */
        public String getHelloWorld() {
            return "Hello, world!";
        }
    }

    /**
     * Test case for <a href="http://issues.apache.org/jira/browse/XMLRPC-96">
     * XMLRPC-96</a>
     */
    public void testXMLRPC96() throws Exception {
        for (int i = 0;  i < providers.length;  i++) {
            testXMLRPC96(providers[i]);
        }
    }

    private void testXMLRPC96(ClientProvider pProvider) throws Exception {
        XmlRpcClient client = pProvider.getClient();
        client.setConfig(getConfig(pProvider));
        String s = (String) client.execute(XMLRPC96Handler.class.getName() + ".getHelloWorld", new Object[0]);
        assertEquals("Hello, world!", s);
        s = (String) client.execute(XMLRPC96Handler.class.getName() + ".getHelloWorld", (Object[]) null);
        assertEquals("Hello, world!", s);
    }
}
