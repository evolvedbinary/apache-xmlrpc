/*
 * Copyright 1999,2006 The Apache Software Foundation.
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.metadata.ReflectiveXmlRpcMetaDataHandler;
import org.apache.xmlrpc.metadata.Util;
import org.apache.xmlrpc.metadata.XmlRpcListableHandlerMapping;
import org.apache.xmlrpc.metadata.XmlRpcMetaDataHandler;


/** Abstract base class of handler mappings, which are
 * using reflection.
 */
public abstract class AbstractReflectiveHandlerMapping
		implements XmlRpcListableHandlerMapping {
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

    /** An object, which is called for initializing the
     * actual handler object.
     */
    public interface InitializationHandler {
        /** Called for initializing the object, which
         * is was returned by {@link ReflectiveXmlRpcHandler#newInstance()}.
         */
        public void init(XmlRpcRequest pRequest, Object pObject)
            throws XmlRpcException;
    }

    protected Map handlerMap = new HashMap();
    private AuthenticationHandler authenticationHandler;
    private InitializationHandler initializationHandler;
    private final boolean instanceIsStateless;

    /** Creates a new instance.
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
     */
    protected AbstractReflectiveHandlerMapping(boolean pInstanceIsStateless) {
        instanceIsStateless = pInstanceIsStateless;
    }
    
    /** Returns the authentication handler, if any, or null.
     */
    public AuthenticationHandler getAuthenticationHandler() {
        return authenticationHandler;
    }

    /** Sets the authentication handler, if any, or null.
     */
    public void setAuthenticationHandler(AuthenticationHandler pAuthenticationHandler) {
        authenticationHandler = pAuthenticationHandler;
    }

    /** Returns the initialization handler, if any, or null.
     */
    public InitializationHandler getInitializationHandler() {
        return initializationHandler;
    }

    /** Sets the initialization handler, if any, or null.
     */
    public void setInitializationHandler(InitializationHandler pInitializationHandler) {
        initializationHandler = pInitializationHandler;
    }

    /** Searches for methods in the given class. For any valid
     * method, it creates an instance of {@link XmlRpcHandler}.
     * Valid methods are defined as follows:
     * <ul>
     *   <li>They must be public.</li>
     *   <li>They must not be static.</li>
     *   <li>The return type must not be void.</li>
     *   <li>The declaring class must not be
     *     {@link java.lang.Object}.</li>
     *   <li>If multiple methods with the same name exist,
     *     which meet the above conditins, then only the
     *     first method is valid.</li>
     * </ul>
     * @param pMap Handler map, in which created handlers are
     * being registered.
     * @param pKey Suffix for building handler names. A dot and
     * the method name are being added.
     * @param pType The class being inspected.
     */
    protected void registerPublicMethods(Map pMap, String pKey,
    		Class pType) throws XmlRpcException {
    	Map map = new HashMap();
        Method[] methods = pType.getMethods();
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
            String name = pKey + "." + method.getName();
            Method[] mArray;
            Method[] oldMArray = (Method[]) map.get(name);
            if (oldMArray == null) {
                mArray = new Method[]{method};
            } else {
                mArray = new Method[oldMArray.length+1];
                System.arraycopy(oldMArray, 0, mArray, 0, oldMArray.length);
                mArray[oldMArray.length] = method;
            }
            map.put(name, mArray);
        }

        for (Iterator iter = map.entrySet().iterator();  iter.hasNext();  ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = (String) entry.getKey();
            Method[] mArray = (Method[]) entry.getValue();
            pMap.put(name, newXmlRpcHandler(pType, mArray));
        }
    }

    /** Creates a new instance of {@link XmlRpcHandler}.
     * @param pClass The class, which was inspected for handler
     * methods. This is used for error messages only. Typically,
     * it is the same than <pre>pInstance.getClass()</pre>.
     * @param pMethods The method being invoked.
     */
    protected XmlRpcHandler newXmlRpcHandler(final Class pClass,
            final Method[] pMethods) throws XmlRpcException {
    	String[][] sig = getSignature(pMethods);
    	String help = getMethodHelp(pClass, pMethods);
    	if (sig == null  ||  help == null) {
    		return new ReflectiveXmlRpcHandler(this, pClass, instanceIsStateless, pMethods);
    	}
    	return new ReflectiveXmlRpcMetaDataHandler(this, pClass, instanceIsStateless,
    			pMethods, sig, help);
    }

    /** Creates a signature for the given method.
     */
    protected String[][] getSignature(Method[] pMethods) {
    	return Util.getSignature(pMethods);
    }

    /** Creates a help string for the given method, when applied
     * to the given class.
     */
    protected String getMethodHelp(Class pClass, Method[] pMethods) {
    	return Util.getMethodHelp(pClass, pMethods);
    }

    /** Returns the {@link XmlRpcHandler} with the given name.
     * @param pHandlerName The handlers name
     * @throws XmlRpcNoSuchHandlerException A handler with the given
     * name is unknown.
     */
    public XmlRpcHandler getHandler(String pHandlerName)
            throws XmlRpcNoSuchHandlerException, XmlRpcException {
        XmlRpcHandler result = (XmlRpcHandler) handlerMap.get(pHandlerName);
        if (result == null) {
            throw new XmlRpcNoSuchHandlerException("No such handler: " + pHandlerName);
        }
        return result;
    }

	public String[] getListMethods() throws XmlRpcException {
		List list = new ArrayList();
		for (Iterator iter = handlerMap.entrySet().iterator();
		     iter.hasNext();  ) {
			Map.Entry entry = (Map.Entry) iter.next();
			if (entry.getValue() instanceof XmlRpcMetaDataHandler) {
				list.add(entry.getKey());
			}
		}
		
		return (String[]) list.toArray(new String[list.size()]);
	}

	public String getMethodHelp(String pHandlerName) throws XmlRpcException {
		XmlRpcHandler h = getHandler(pHandlerName);
		if (h instanceof XmlRpcMetaDataHandler)
			return ((XmlRpcMetaDataHandler)h).getMethodHelp();
		throw new XmlRpcNoSuchHandlerException("No help available for method: "
				+ pHandlerName);
	}

	public String[][] getMethodSignature(String pHandlerName) throws XmlRpcException {
		XmlRpcHandler h = getHandler(pHandlerName);
		if (h instanceof XmlRpcMetaDataHandler)
			return ((XmlRpcMetaDataHandler)h).getSignatures();
		throw new XmlRpcNoSuchHandlerException("No metadata available for method: "
				+ pHandlerName);
	}
}
