package org.apache.xmlrpc.server;

import org.apache.xmlrpc.common.ServerStreamConnection;


/** Interface of a {@link ServerStreamConnection} for HTTP
 * response transport.
 */
public interface ServerHttpConnection extends ServerStreamConnection {
    /** Sets a response header.
     */
    void setResponseHeader(String pKey, String pValue);
    /** Sets the content length.
     */
    void setContentLength(int pContentLength);
}
