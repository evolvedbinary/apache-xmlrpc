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

import org.apache.xmlrpc.*;

import FESI.Interpreter.*;
import FESI.Exceptions.*;
import FESI.Extensions.*;
import FESI.Data.*;

import java.io.*;
import java.util.*;
import java.net.*;

/**
 * An extension to transparently call and serve XML-RPC from the
 * <a href=http://home.worldcom.ch/jmlugrin/fesi/>FESI EcmaScript</a> interpreter.
 * The extension adds constructors for XML-RPC clients and servers to the Global Object.
 * For more information on how to use this please look at the files <tt>server.es</tt> and
 * <tt>client.es</tt> in the src/fesi directory of the distribution.
 *
 * All argument conversion is done automatically. Currently the following argument and return
 * types are supported:
 * <ul>
 * <li> plain objects (with all properties returned by ESObject.getProperties ())
 * <li> arrays
 * <li> strings
 * <li> date objects
 * <li> booleans
 * <li> integer and float numbers (long values are not supported!)
 * </ul>
 * 
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 *
 */
public class FesiRpcExtension 
    extends Extension
{

    Evaluator evaluator;
    ESObject op;

    public void initializeExtension (Evaluator evaluator)
            throws EcmaScriptException
    {
        // XmlRpc.setDebug (true);
        this.evaluator = evaluator;
        GlobalObject go = evaluator.getGlobalObject();
        FunctionPrototype fp =
                (FunctionPrototype) evaluator.getFunctionPrototype();

        op = evaluator.getObjectPrototype();

        go.putHiddenProperty ("Remote",
                new GlobalObjectRemote ("Remote", evaluator, fp)); // the Remote constructor
                go.putHiddenProperty ("RemoteServer",
                new GlobalObjectRemoteServer ("RemoteServer",
                evaluator, fp)); // the RemoteServer constructor

                }


    class GlobalObjectRemote extends BuiltinFunctionObject
    {

        GlobalObjectRemote (String name, Evaluator evaluator,
                FunctionPrototype fp)
        {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject,
                ESValue[] arguments) throws EcmaScriptException
        {
            return doConstruct(thisObject, arguments);
        }

        public ESObject doConstruct(ESObject thisObject,
                ESValue[] arguments) throws EcmaScriptException
        {
            ESObject remote = null;
            String url = null;
            String robj = null;
            if (arguments.length >= 1)
                url = arguments[0].toString ();
            if (arguments.length >= 2)
                robj = arguments[1].toString ();
            try
            {
                remote = new ESRemote (op, this.evaluator, url, robj);
            }
            catch (MalformedURLException x)
            {
                throw new EcmaScriptException (x.toString ());
            }
            return remote;
        }
    }

    class GlobalObjectRemoteServer extends BuiltinFunctionObject
    {



        GlobalObjectRemoteServer (String name, Evaluator evaluator,
                FunctionPrototype fp)
        {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject,
                ESValue[] arguments) throws EcmaScriptException
        {
            return doConstruct(thisObject, arguments);
        }

        public ESObject doConstruct(ESObject thisObject,
                ESValue[] arguments) throws EcmaScriptException
        {
            ESObject remotesrv = null;
            String globalname = null;
            if (arguments.length < 1 || arguments.length > 2)
                throw new EcmaScriptException ("Wrong number of arguments for constructor RemoteServer");
            int port = arguments[0].toInt32 ();
            if (arguments.length == 2)
                globalname = arguments[1].toString ();
            try
            {
                remotesrv = new FesiRpcServer (port, op, this.evaluator);
                if (globalname != null)
                    this.evaluator.getGlobalObject ().putProperty (
                            globalname, remotesrv, globalname.hashCode ());
            }
            catch (IOException x)
            {
                throw new EcmaScriptException (x.toString ());
            }
            return remotesrv;
        }
    }

    class ESRemote 
        extends ObjectPrototype
    {

        URL url;
        String remoteObject;

        public ESRemote (ESObject prototype, Evaluator evaluator,
                String urlstring, String robj) throws MalformedURLException
        {
            super (prototype, evaluator);
            this.url = new URL (urlstring);
            remoteObject = robj;
        }

        public ESRemote (ESObject prototype, Evaluator evaluator,
                URL url, String robj)
        {
            super (prototype, evaluator);
            this.url = url;
            remoteObject = robj;
        }

        public ESValue doIndirectCall(Evaluator evaluator,
                ESObject target, String functionName,
                ESValue arguments[]) throws EcmaScriptException,
        NoSuchMethodException
        {
            // System.out.println ("doIndirectCall called with "+functionName);
            XmlRpcClient client = new XmlRpcClient (url);
            long now = System.currentTimeMillis ();
            Object retval = null;
            int l = arguments.length;
            Vector v = new Vector ();
            for (int i = 0; i < l; i++)
            {
                Object arg = FesiRpcUtil.convertE2J (arguments[i]);
                // System.out.println ("converted to J: "+arg.getClass ());
                v.addElement (arg);
            }
            // System.out.println ("spent "+(System.currentTimeMillis ()-now)+" millis in argument conversion");
            ESObject esretval = ObjectObject.createObject (evaluator);
            try
            {
                String method = remoteObject == null ? functionName :
                        remoteObject + "."+functionName;
                retval = client.execute (method, v);
                esretval.putProperty ("error", ESNull.theNull,
                        "error".hashCode());
                esretval.putProperty ("result",
                        FesiRpcUtil.convertJ2E (retval,
                        this.evaluator), "result".hashCode());
            }
            catch (Exception x)
            {
                String msg = x.getMessage();
                if (msg == null || msg.length() == 0)
                    msg = x.toString ();
                esretval.putProperty ("error", new ESString(msg),
                        "error".hashCode());
                esretval.putProperty ("result", ESNull.theNull,
                        "result".hashCode());
            }
            return esretval;
        }

        public ESValue getProperty (String name, int hash) 
            throws EcmaScriptException
        {
            ESValue sprop = super.getProperty (name, hash);
            if (sprop != ESUndefined.theUndefined &&
                    sprop != ESNull.theNull)
                return sprop;
            String newRemoteObject =
                    remoteObject == null ? name : remoteObject + "."+name;
            return new ESRemote (op, this.evaluator, url, newRemoteObject);
        }
    }
}
