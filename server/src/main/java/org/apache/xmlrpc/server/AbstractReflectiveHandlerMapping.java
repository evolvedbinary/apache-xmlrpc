package org.apache.xmlrpc.server;

import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.common.XmlRpcNotAuthorizedException;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;


/** Abstract base class of handler mappings, which are
 * using reflection.
 */
public abstract class AbstractReflectiveHandlerMapping implements XmlRpcHandlerMapping {
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

    protected Map handlerMap = new HashMap();
    private AuthenticationHandler authenticationHandler;

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
     * @param pInstance The object being invoked. Note, that this
     * object must be stateless: Multiple threads can run on it
     * at the same time.
     */
    protected void registerPublicMethods(Map pMap, String pKey,
    		Class pType, Object pInstance) {
    	if (pInstance == null) {
    		throw new NullPointerException("The object instance must not be null.");
    	}
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
            if (!pMap.containsKey(name)) {
                pMap.put(name, newXmlRpcHandler(pType, pInstance, method));
            }
        }
    }

    /** Creates a new instance of {@link XmlRpcHandler}.
     * @param pClass The class, which was inspected for handler
     * methods. This is used for error messages only. Typically,
     * it is the same than <pre>pInstance.getClass()</pre>.
     * @param pInstance The object, which is being invoked by
     * the created handler. Typically an instance of
     * <code>pClass</code>.
     * @param pMethod The method being invoked.
     */
    protected XmlRpcHandler newXmlRpcHandler(final Class pClass,
    		final Object pInstance, final Method pMethod) {
    	if (pInstance == null) {
    		throw new NullPointerException("The object instance must not be null.");
    	}
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
                    return pMethod.invoke(pInstance, args);
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
}
