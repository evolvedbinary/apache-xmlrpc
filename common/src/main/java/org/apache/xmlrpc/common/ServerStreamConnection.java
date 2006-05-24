package org.apache.xmlrpc.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/** Interface of an object, which is able to provide
 * an XML stream, containing an XML-RPC request.
 * Additionally, the object may also be used to
 * write the response as an XML stream.
 */
public interface ServerStreamConnection {
    /** Returns the connections input stream.
     */
    InputStream newInputStream() throws IOException;
    /** Returns the connections output stream.
     */
    OutputStream newOutputStream() throws IOException;
    /** Closes the connection, and frees resources.
     */
    void close() throws IOException;
}
