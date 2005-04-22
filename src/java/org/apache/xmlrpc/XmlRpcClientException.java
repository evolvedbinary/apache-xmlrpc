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


package org.apache.xmlrpc;

/**
 * This is thrown by many of the client classes if an error occured processing
 * and XML-RPC request or response due to client side processing. This exception
 * will wrap a cause exception in the JDK 1.4 style.
 *
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @version $Id$
 * @since 1.2
 */
public class XmlRpcClientException extends XmlRpcException
{
    /**
     * The underlying cause of this exception.
     */
    public Throwable cause;

    /**
     * Create an XmlRpcClientException with the given message and
     * underlying cause exception.
     *
     * @param message the message for this exception.
     * @param cause the cause of the exception.
     */
    public XmlRpcClientException(String message, Throwable cause)
    {
        super(0, message);
        this.cause = cause;
    }

    /**
     * Returns the cause of this throwable or null if the cause is nonexistent
     * or unknown. (The cause is the throwable that caused this throwable to
     * get thrown.)
     * 
     * This implementation returns the cause that was supplied via the constructor,
     * according to the rules specified for a "legacy chained throwable" that
     * predates the addition of chained exceptions to Throwable.
     *
     * See the <a
     * href="http://java.sun.com/j2se/1.4.1/docs/api/java/lang/Throwable.html">JDK
     * 1.4 Throwable documentation</a> for more information.
     */
    public Throwable getCause()
    {
        return cause;
    }
}
