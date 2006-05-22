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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcNotAuthorizedException;
import org.apache.xmlrpc.metadata.Util;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping.AuthenticationHandler;


/** Default implementation of {@link XmlRpcHandler}.
 */
public class ReflectiveXmlRpcHandler implements XmlRpcHandler {
	private final AbstractReflectiveHandlerMapping mapping;
	private final Class clazz;
	private final Object instance;
	private final Method[] methods;

	/** Creates a new instance.
	 * @param pMapping The mapping, which creates this handler.
	 * @param pClass The class, which has been inspected to create
	 * this handler. Typically, this will be the same as
	 * <pre>pInstance.getClass()</pre>. It is used for diagnostic
	 * messages only.
	 * @param pInstance The instance, which will be invoked for
	 * executing the handler.
	 * @param pMethods The method, which will be invoked for
	 * executing the handler. 
	 */
	public ReflectiveXmlRpcHandler(AbstractReflectiveHandlerMapping pMapping,
				Class pClass, Object pInstance, Method[] pMethods) {
		mapping = pMapping;
		clazz = pClass;
		instance = pInstance;
		methods = pMethods;
	}

	public Object execute(XmlRpcRequest pRequest) throws XmlRpcException {
	    AuthenticationHandler authHandler = mapping.getAuthenticationHandler();
	    if (authHandler != null  &&  !authHandler.isAuthorized(pRequest)) {
	        throw new XmlRpcNotAuthorizedException("Not authorized");
	    }
	    Object[] args = new Object[pRequest.getParameterCount()];
	    for (int j = 0;  j < args.length;  j++) {
	        args[j] = pRequest.getParameter(j);
	    }
	    if (methods.length == 1) {
            return invoke(methods[0], args);
        } else {
            for (int i = 0;  i < methods.length;  i++) {
                if (Util.isMatching(methods[i], args)) {
                    return invoke(methods[i], args);
                }
            }
            throw new XmlRpcException("No method matching arguments: " + Util.getSignature(args));
        }
    }

    private Object invoke(Method pMethod, Object[] pArgs) throws XmlRpcException {
        try {
	        return pMethod.invoke(instance, pArgs);
	    } catch (IllegalAccessException e) {
	        throw new XmlRpcException("Illegal access to method "
	                                  + pMethod.getName() + " in class "
	                                  + clazz.getName(), e);
	    } catch (IllegalArgumentException e) {
	        throw new XmlRpcException("Illegal argument for method "
	                                  + pMethod.getName() + " in class "
	                                  + clazz.getName(), e);
	    } catch (InvocationTargetException e) {
	        Throwable t = e.getTargetException();
	        throw new XmlRpcException("Failed to invoke method "
	                                  + pMethod.getName() + " in class "
	                                  + clazz.getName() + ": "
	                                  + t.getMessage(), t);
	    }
	}
}