package org.apache.xmlrpc.server;

import java.util.Iterator;


/** A handler mapping, which requires explicit registration
 * of handlers.
 */
public class DynamicHandlerMapping extends AbstractReflectiveHandlerMapping {
	/** Adds handlers for the given object to the mapping.
	 * The handlers are build by invoking
	 * {@link #registerPublicMethods(java.util.Map, String, Class, Object)}.
	 * @param pKey The class key, which is passed
	 * to {@link #registerPublicMethods(java.util.Map, String, Class, Object)}.
	 * @param pHandler The object, which is handling requests.
	 */
    public void addHandler(String pKey, Object pHandler) {
        registerPublicMethods(handlerMap, pKey, pHandler.getClass(), pHandler);
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
