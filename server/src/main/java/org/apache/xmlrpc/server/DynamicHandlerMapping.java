package org.apache.xmlrpc.server;

import java.util.Iterator;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.TypeConverterFactory;


/** A handler mapping, which requires explicit registration
 * of handlers.
 */
public class DynamicHandlerMapping extends AbstractReflectiveHandlerMapping {
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
    public DynamicHandlerMapping(TypeConverterFactory pTypeConverterFactory,
                boolean pInstanceIsStateless) {
        super(pTypeConverterFactory, pInstanceIsStateless);
    }

	/** Adds handlers for the given object to the mapping.
	 * The handlers are build by invoking
	 * {@link #registerPublicMethods(java.util.Map, String, Class)}.
	 * @param pKey The class key, which is passed
	 * to {@link #registerPublicMethods(java.util.Map, String, Class)}.
	 * @param pClass Class, which is responsible for handling the request.
	 */
    public void addHandler(String pKey, Class pClass) throws XmlRpcException {
        registerPublicMethods(handlerMap, pKey, pClass);
    }

    /** Removes all handlers with the given class key.
     */
    public void removeHandler(String pKey) {
        for (Iterator i = handlerMap.keySet().iterator(); i.hasNext();) {
            String k = (String)i.next();
            if (k.startsWith(pKey)) i.remove();
        }
    }
}
