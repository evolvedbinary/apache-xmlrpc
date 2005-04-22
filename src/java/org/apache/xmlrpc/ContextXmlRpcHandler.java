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


import java.util.Vector;

/**
 * An XML-RPC handler that also handles user authentication.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @see org.apache.xmlrpc.AuthenticationFailed
 * @version $Id$
 * @since 1.2
 */
public interface ContextXmlRpcHandler
{
    /**
     * Return the result, or throw an Exception if something went wrong.
     *
     * @throws AuthenticationFailed If authentication fails, an
     * exception of this type must be thrown.
     * @see org.apache.xmlrpc.AuthenticationFailed
     */
    public Object execute(String method, Vector params, XmlRpcContext context)
            throws Exception;
}
