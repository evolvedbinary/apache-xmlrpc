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
import org.apache.xmlrpc.common.TypeConverter;
import org.apache.xmlrpc.common.TypeConverterFactory;
import org.apache.xmlrpc.common.XmlRpcNotAuthorizedException;
import org.apache.xmlrpc.metadata.Util;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping.AuthenticationHandler;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping.InitializationHandler;


/** Default implementation of {@link XmlRpcHandler}.
 */
public class ReflectiveXmlRpcHandler implements XmlRpcHandler {
    private static class MethodData {
        final Method method;
        final TypeConverter[] typeConverters;
        MethodData(Method pMethod, TypeConverterFactory pTypeConverterFactory) {
            method = pMethod;
            Class[] paramClasses = method.getParameterTypes();
            typeConverters = new TypeConverter[paramClasses.length];
            for (int i = 0;  i < paramClasses.length;  i++) {
                typeConverters[i] = pTypeConverterFactory.getTypeConverter(paramClasses[i]);
            }
        }
    }
    private final AbstractReflectiveHandlerMapping mapping;
	private final Class clazz;
	private final MethodData[] methods;
    private final Object theInstance;

	/** Creates a new instance.
	 * @param pMapping The mapping, which creates this handler.
	 * @param pClass The class, which has been inspected to create
	 * this handler. Typically, this will be the same as
	 * <pre>pInstance.getClass()</pre>. It is used for diagnostic
	 * messages only.
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
	 * @param pMethods The method, which will be invoked for
	 * executing the handler. 
	 */
	public ReflectiveXmlRpcHandler(AbstractReflectiveHandlerMapping pMapping,
                TypeConverterFactory pTypeConverterFactory,
				Class pClass, boolean pInstanceIsStateless, Method[] pMethods)
            throws XmlRpcException {
		mapping = pMapping;
		clazz = pClass;
		methods = new MethodData[pMethods.length];
        for (int i = 0;  i < methods.length;  i++) {
            methods[i] = new MethodData(pMethods[i], pTypeConverterFactory); 
        }
        theInstance = pInstanceIsStateless ? newInstance() : null;
	}

    private Object getInstance(XmlRpcRequest pRequest) throws XmlRpcException {
        final InitializationHandler ih = mapping.getInitializationHandler();
        if (ih == null) {
            return theInstance == null ? newInstance() : theInstance;
        } else {
            final Object instance = newInstance();
            ih.init(pRequest, instance);
            return instance;
        }
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
	    Object instance = getInstance(pRequest);
	    for (int i = 0;  i < methods.length;  i++) {
            MethodData methodData = methods[i];
            TypeConverter[] converters = methodData.typeConverters;
            if (args.length == converters.length) {
                boolean matching = true;
                for (int j = 0;  j < args.length;  j++) {
                    if (!converters[j].isConvertable(args[i])) {
                        matching = false;
                        break;
                    }
                }
                if (matching) {
                    for (int j = 0;  j < args.length;  j++) {
                        args[i] = converters[i].convert(args[i]);
                    }
                    return invoke(instance, methodData.method, args);
                }
            }
	    }
	    throw new XmlRpcException("No method matching arguments: " + Util.getSignature(args));
    }

    private Object invoke(Object pInstance, Method pMethod, Object[] pArgs) throws XmlRpcException {
        try {
	        return pMethod.invoke(pInstance, pArgs);
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

    protected Object newInstance() throws XmlRpcException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new XmlRpcException("Failed to instantiate class " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new XmlRpcException("Illegal access when instantiating class " + clazz.getName(), e);
        }
    }
}