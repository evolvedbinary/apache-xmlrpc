/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
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
     *
     * @return the connections input stream
     * @throws IOException if an I/O error occurs
     */
    InputStream newInputStream() throws IOException;
    /** Returns the connections output stream.
     *
     * @return the connections output stream
     * @throws IOException if an I/O error occurs
     */
    OutputStream newOutputStream() throws IOException;
    /** Closes the connection, and frees resources.
     * @throws IOException if an I/O error occurs
     */
    void close() throws IOException;
}
