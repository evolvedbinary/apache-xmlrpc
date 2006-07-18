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
package org.apache.xmlrpc.server;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.TypeConverterFactory;


/**
 * A handler mapping based on a property file. The property file
 * contains a set of properties. The property key is taken as the
 * handler name. The property value is taken as the name of a
 * class being instantiated. For any non-void, non-static, and
 * public method in the class, an entry in the handler map is
 * generated. A typical use would be, to specify interface names
 * as the property keys and implementations as the values.
 */
public class PropertyHandlerMapping extends AbstractReflectiveHandlerMapping {
    /** Creates a new instance, loading the property file
     * from the given URL.
     * @param pClassLoader Classloader being used to load the classes.
     * @param pURL The URL, from which the property file is being
     * loaded.
     * @param pInstanceIsStateless The handler
     * can operate in either of two operation modes:
     * <ol>
     *   <li>The object, which is actually performing the requests,
     *     is initialized at startup. In other words, there is only
     *     one object, which is performing all the requests.
     *     Obviously, this is the faster operation mode. On the
     *     other hand, it has the disadvantage, that the object
     *     must be stateless.</li>
     *   <li>A new object is created for any request. This is slower,
     *     because the object needs to be initialized. On the other
     *     hand, it allows for stateful objects, which may take
     *     request specific configuration like the clients IP address,
     *     and the like.</li>
     * </ol>
     * @throws IOException Loading the property file failed.
     * @throws XmlRpcException Initializing the handlers failed.
     */
    public PropertyHandlerMapping(ClassLoader pClassLoader, URL pURL,
                TypeConverterFactory pTypeConverterFactory,
                boolean pInstanceIsStateless)
            throws IOException, XmlRpcException {
        super(pTypeConverterFactory, pInstanceIsStateless);
        handlerMap = load(pClassLoader, pURL);
    }

    /** Creates a new instance, loading the properties from
     * the given resource.
     * @param pClassLoader Classloader being used to locate
     * the resource.
     * @param pResource Resource being loaded.
     * @param pInstanceIsStateless The handler
     * can operate in either of two operation modes:
     * <ol>
     *   <li>The object, which is actually performing the requests,
     *     is initialized at startup. In other words, there is only
     *     one object, which is performing all the requests.
     *     Obviously, this is the faster operation mode. On the
     *     other hand, it has the disadvantage, that the object
     *     must be stateless.</li>
     *   <li>A new object is created for any request. This is slower,
     *     because the object needs to be initialized. On the other
     *     hand, it allows for stateful objects, which may take
     *     request specific configuration like the clients IP address,
     *     and the like.</li>
     * </ol>
     * @throws IOException Loading the property file failed.
     * @throws XmlRpcException Initializing the handlers failed.
     */
    public PropertyHandlerMapping(ClassLoader pClassLoader, String pResource,
                TypeConverterFactory pTypeConverterFactory,
                boolean pInstanceIsStateless)
            throws IOException, XmlRpcException {
        this(pClassLoader, asURL(pClassLoader, pResource), pTypeConverterFactory,
                pInstanceIsStateless);
    }

    private static URL asURL(ClassLoader pClassLoader, String pResource) throws IOException {
        URL url = pClassLoader.getResource(pResource);
        if (url == null) {
            throw new IOException("Unable to locate resource " + pResource);
        }
        return url;
    }
    
    private Map load(ClassLoader pClassLoader, URL pURL) throws IOException, XmlRpcException {
        Map map = new HashMap();
        Properties props = new Properties();
        props.load(pURL.openStream());
        for (Iterator iter = props.entrySet().iterator();  iter.hasNext();  ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            Class c = newHandlerClass(pClassLoader, value);
            registerPublicMethods(map, key, c);
        }
        return map;
    }

    protected Class newHandlerClass(ClassLoader pClassLoader, String pClassName)
            throws XmlRpcException {
        final Class c;
        try {
            c = pClassLoader.loadClass(pClassName);
        } catch (ClassNotFoundException e) {
            throw new XmlRpcException("Unable to load class: " + pClassName, e);
        }
        if (c == null) {
            throw new XmlRpcException(0, "Loading class " + pClassName + " returned null.");
        }
        return c;
    }
}
