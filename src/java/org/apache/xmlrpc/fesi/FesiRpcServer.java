package org.apache.xmlrpc.fesi;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 *        Apache Software Foundation (http://www.apache.org/)."
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
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import FESI.Data.ESObject;
import FESI.Data.ESValue;
import FESI.Data.ObjectPrototype;
import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcHandler;

/**
 * An ESObject that makes its properties (sub-objects) callable via XML-RPC.
 * For example, if Server is an instance of FesiRpcServer, the following would make the
 * functions defined for someObject available to XML-RPC clients:
 * <pre>
 * Server.someObject = new SomeObject ();
 * </pre>
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @version $Id$
 */
public class FesiRpcServer extends ObjectPrototype
{

    // This is public (for now) to be able to set access restrictions from the
    // outside.
    public WebServer srv;
    Evaluator evaluator;

    /**
     * Create an XML-RPC server with an already existing WebServer.
     */
    public FesiRpcServer(WebServer srv, ESObject op, Evaluator eval)
            throws IOException, EcmaScriptException
    {
        super(op, eval);
        this.evaluator = eval;
        this.srv = srv;
    }

    /**
     * Create an XML-RPC server listening on a specific port.
     */
    public FesiRpcServer(int port, ESObject op, Evaluator eval)
            throws IOException, EcmaScriptException
    {
        super(op, eval);
        this.evaluator = eval;
        srv = new WebServer(port);
    }

    /**
     *
     */
    public void putProperty(String propertyName, ESValue propertyValue,
            int hash)
            throws EcmaScriptException
    {
        if (propertyValue instanceof ESObject)
        {
            srv.addHandler(propertyName,
                    new FesiInvoker((ESObject) propertyValue));
        }
        super.putProperty(propertyName, propertyValue, hash);
    }

    /**
     *
     */
    public boolean deleteProperty(String propertyName, int hash)
            throws EcmaScriptException
    {
        srv.removeHandler(propertyName);
        super.deleteProperty(propertyName, hash);
        return true;
    }

    /**
     *
     */
    class FesiInvoker implements XmlRpcHandler
    {
        ESObject target;

        /**
         *
         */
        public FesiInvoker(ESObject target)
        {
            this.target = target;
        }

        /**
         *
         */
        public Object execute(String method, Vector argvec) throws Exception
        {
            // convert arguments
            int l = argvec.size();

            ESObject callTarget = target;
            if (method.indexOf (".") > -1)
            {
                StringTokenizer st = new StringTokenizer(method, ".");
                int cnt = st.countTokens();
                for (int i = 1; i < cnt; i++)
                {
                    String next = st.nextToken();
                    try
                    {
                        callTarget = (ESObject) callTarget.getProperty(
                                next, next.hashCode());
                    }
                    catch (Exception x)
                    {
                        throw new EcmaScriptException("The property \"" + next
                                + "\" is not defined in the remote object.");
                    }
                }
                method = st.nextToken();
            }

            ESValue args[] = new ESValue[l];
            for (int i = 0; i < l; i++)
            {
                args[i] = FesiRpcUtil.convertJ2E(argvec.elementAt(i),
                        evaluator);
            }
            Object retval = FesiRpcUtil.convertE2J(
                    callTarget.doIndirectCall(evaluator, callTarget,
                    method, args));
            return retval;
        }
    }
}
