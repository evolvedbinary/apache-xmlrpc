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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfig;
import org.apache.xmlrpc.common.XmlRpcNotAuthorizedException;


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
public class PropertyHandlerMapping implements XmlRpcHandlerMapping {
	/** An object implementing this interface may be used
	 * to validate user names and passwords.
	 */
	public interface AuthenticationHandler {
		/** Returns, whether the user is authenticated and
		 * authorized to perform the request.
		 */
		boolean isAuthorized(XmlRpcRequest pRequest)
			throws XmlRpcException;
	}

	private final Map handlerMap;
	private AuthenticationHandler authenticationHandler;

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

	/** Creates a new instance, loading the given property file.
	 * @param pClassLoader Classloader being used to load the classes.
	 * @param pFile File being loaded.
	 * @throws IOException Loading the property file failed.
	 * @throws XmlRpcException Initializing the handlers failed.
	 */
	public PropertyHandlerMapping(ClassLoader pClassLoader, File pFile)
			throws IOException, XmlRpcException {
		handlerMap = load(pClassLoader, pFile.toURL());
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

	/** Returns the authentication handler, if any, or null.
	 */
	public AuthenticationHandler getAuthenticationHandler() {
		return authenticationHandler;
	}

	/** Sets the authentication handler, if any, or null.
	 */
	public void setAuthenticationHandler(
			AuthenticationHandler pAuthenticationHandler) {
		authenticationHandler = pAuthenticationHandler;
	}

	private Map load(ClassLoader pClassLoader, URL pURL) throws IOException, XmlRpcException {
		Map map = new HashMap();
		Properties props = new Properties();
		props.load(pURL.openStream());
		for (Iterator iter = props.entrySet().iterator();  iter.hasNext();  ) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			final Class c;
			try {
				c = pClassLoader.loadClass(value);
			} catch (ClassNotFoundException e) {
				throw new XmlRpcException("Unable to load class: " + value, e);
			}
			if (c == null) {
				throw new XmlRpcException(0, "Loading class " + value + " returned null.");
			}
			final Object o;
			try {
				o = c.newInstance();
			} catch (InstantiationException e) {
				throw new XmlRpcException("Failed to instantiate class " + c.getName(), e);
			} catch (IllegalAccessException e) {
				throw new XmlRpcException("Illegal access when instantiating class " + c.getName(), e);
			}
			Method[] methods = c.getMethods();
			for (int i = 0;  i < methods.length;  i++) {
				final Method method = methods[i];
				if (!Modifier.isPublic(method.getModifiers())) {
					continue;  // Ignore methods, which aren't public
				}
				if (Modifier.isStatic(method.getModifiers())) {
					continue;  // Ignore methods, which are static
				}
				if (method.getReturnType() == void.class) {
					continue;  // Ignore void methods.
				}
				if (method.getDeclaringClass() == Object.class) {
					continue;  // Ignore methods from Object.class
				}
				String name = key + "." + method.getName();
				if (!map.containsKey(name)) {
					map.put(name, newXmlRpcHandler(c, o, method));
				}
			}
		}
		return map;
	}

	protected XmlRpcHandler newXmlRpcHandler(final Class pClass, final Object pValue, final Method pMethod) {
		return new XmlRpcHandler(){
			public Object execute(XmlRpcRequest pRequest) throws XmlRpcException {
				AuthenticationHandler authHandler = getAuthenticationHandler();
				if (authHandler != null  &&  !authHandler.isAuthorized(pRequest)) {
					throw new XmlRpcNotAuthorizedException("Not authorized");
				}
				Object[] args = new Object[pRequest.getParameterCount()];
				for (int j = 0;  j < args.length;  j++) {
					args[j] = pRequest.getParameter(j);
				}
				try {
					return pMethod.invoke(pValue, args);
				} catch (IllegalAccessException e) {
					throw new XmlRpcException("Illegal access to method "
											  + pMethod.getName() + " in class "
											  + pClass.getName(), e);
				} catch (IllegalArgumentException e) {
					throw new XmlRpcException("Illegal argument for method "
											  + pMethod.getName() + " in class "
											  + pClass.getName(), e);
				} catch (InvocationTargetException e) {
					Throwable t = e.getTargetException();
					throw new XmlRpcException("Failed to invoke method "
											  + pMethod.getName() + " in class "
											  + pClass.getName() + ": "
											  + t.getMessage(), t);
				}
			}
		};
	}

	public XmlRpcHandler getHandler(String handlerName)
			throws XmlRpcNoSuchHandlerException, XmlRpcException {
		XmlRpcHandler result = (XmlRpcHandler) handlerMap.get(handlerName);
		if (result == null) {
			throw new XmlRpcNoSuchHandlerException("No such handler: " + handlerName);
		}
		return result;
	}
}
