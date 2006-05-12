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


/** A handler mapping based on a property file. The property file
 * contains a set of properties. The property key is taken as the
 * handler name. The property value is taken as the name of a
 * class being instantiated. For any non-void, non-static, and
 * public method in the class, an entry in the handler map is
 * generated.<br>
 * The following constrains apply to the classes:
 * <ol>
 *   <li>The classes must be stateless. In other words, any
 *     instance of the class must be completely thread safe.</li>
 * </ol>
 * A typical use would be, to specify interface names as the
 * property keys and implementations as the values.
 */
public class PropertyHandlerMapping extends AbstractReflectiveHandlerMapping {
    /** Creates a new instance, loading the property file
     * from the given URL.
     * @param pClassLoader Classloader being used to load the classes.
     * @param pURL The URL, from which the property file is being
     * loaded.
     * @throws IOException Loading the property file failed.
     * @throws XmlRpcException Initializing the handlers failed.
     */
    public PropertyHandlerMapping(ClassLoader pClassLoader, URL pURL)
            throws IOException, XmlRpcException {
        handlerMap = load(pClassLoader, pURL);
    }

    /** Creates a new instance, loading the properties from
     * the given resource.
     * @param pClassLoader Classloader being used to locate
     * the resource.
     * @param pResource Resource being loaded.
     * @throws IOException Loading the property file failed.
     * @throws XmlRpcException Initializing the handlers failed.
     */
    public PropertyHandlerMapping(ClassLoader pClassLoader, String pResource)
            throws IOException, XmlRpcException {
        URL url = pClassLoader.getResource(pResource);
        if (url == null) {
            throw new IOException("Unable to locate resource " + pResource);
        }
        handlerMap = load(pClassLoader, url);
    }

    private Map load(ClassLoader pClassLoader, URL pURL) throws IOException, XmlRpcException {
        Map map = new HashMap();
        Properties props = new Properties();
        props.load(pURL.openStream());
        for (Iterator iter = props.entrySet().iterator();  iter.hasNext();  ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            Object o = newHandlerObject(pClassLoader, value);
            registerPublicMethods(map, key, o.getClass(), o);
        }
        return map;
    }

    protected Object newHandlerObject(ClassLoader pClassLoader, String pClassName)
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
        try {
            return c.newInstance();
        } catch (InstantiationException e) {
            throw new XmlRpcException("Failed to instantiate class " + c.getName(), e);
        } catch (IllegalAccessException e) {
            throw new XmlRpcException("Illegal access when instantiating class " + c.getName(), e);
        }
    }
}
