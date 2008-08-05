package org.apache.xmlrpc.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Instances of this class can be used to customize the servers
 * error logging.
 */
public class XmlRpcErrorLogger {
    private static final Log log = LogFactory.getLog(XmlRpcErrorLogger.class);

    /**
     * Called to log the given error.
     */
    public void log(String pMessage, Throwable pThrowable) {
        log.error(pMessage, pThrowable);
    }

    /**
     * Called to log the given error message.
     */
    public void log(String pMessage) {
        log.error(pMessage);
    }
}
