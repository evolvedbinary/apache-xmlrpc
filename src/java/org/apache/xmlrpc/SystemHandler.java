package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright(c) 2001,2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation(http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "XML-RPC" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES(INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.*;

/**
 * Implements the XML-RPC standard system.* methods (such as
 * <code>system.multicall</code>.
 *
 * @author <a href="mailto:adam@megacz.com">Adam Megacz</a>
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @since 1.2
 */
public class SystemHandler
{
    private XmlRpcHandlerMapping handlerMapping = null;

    /**
     * Creates a new instance that delegates calls via the
     * specified {@link org.apache.xmlrpc.XmlRpcHandlerMapping}
     */
    public SystemHandler(XmlRpcHandlerMapping handlerMapping)
    {
        this.handlerMapping = handlerMapping;
    }

    /**
     * Creates a new instance that delegates its multicalls via
     * the mapping used by the specified {@link org.apache.xmlrpc.XmlRpcServer}.
     *
     * @param server The server to retrieve the XmlRpcHandlerMapping from.
     */
    protected SystemHandler(XmlRpcServer server)
    {
        this(server.getHandlerMapping());
    }

    /**
     * The <code>system.multicall</code> handler performs several RPC
     * calls at a time.
     *
     * @param requests The request containing multiple RPC calls.
     * @return The RPC response.
     */
    public Vector multicall(Vector requests)
    {
        Vector response = new Vector();
        XmlRpcRequest request;
        for (int i = 0; i < requests.size(); i++)
        {
            try
            {
                Hashtable call = (Hashtable) requests.elementAt(i);
                request = new XmlRpcRequest((String) call.get("methodName"),
                                            (Vector) call.get("params"),
                                            null, null);
                Object handler = handlerMapping.getHandler(request.getMethodName());
                Vector v = new Vector();
                v.addElement(XmlRpcWorker.invokeHandler(handler, request));
                response.addElement(v);
            }
            catch (Exception x)
            {
                String message = x.toString();
                int code = (x instanceof XmlRpcException ?
                            ((XmlRpcException) x).code : 0);
                Hashtable h = new Hashtable();
                h.put("faultString", message);
                h.put("faultCode", new Integer(code));
                response.addElement(h);
            }
        }
        return response;
    }
}
