package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright(c) 2001 The Apache Software Foundation.  All rights
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

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * A multithreaded, reusable XML-RPC server object. The name may be misleading 
 * because this does not open any server sockets. Instead it is fed by passing 
 * an XML-RPC input stream to the execute method. If you want to open a 
 * HTTP listener, use the WebServer class instead.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 */
public class XmlRpcServer
{
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private Hashtable handlers;
    private Stack pool;
    private int workers;

    /**
     * Construct a new XML-RPC server. You have to register handlers
     * to make it do something useful.
     */
    public XmlRpcServer()
    {
        handlers = new Hashtable();
        pool = new Stack();
        workers = 0;
    }

    /**
     * Register a handler object with this name. Methods of this
     * objects will be callable over XML-RPC as
     * "handlername.methodname". For more information about XML-RPC
     * handlers see the <a href="../index.html#1a">main documentation
     * page</a>.
     *
     * @param handlername The name to identify the handler by.
     * @param handler The handler itself.
     */
    public void addHandler(String handlername, Object handler)
    {
        if (handler instanceof XmlRpcHandler ||
                handler instanceof AuthenticatedXmlRpcHandler)
        {
            handlers.put(handlername, handler);
        }
        else if (handler != null)
        {
            handlers.put(handlername, new Invoker(handler));
        }
    }

    /**
     * Remove a handler object that was previously registered with
     * this server.
     *
     * @param handlername The name identifying the handler to remove.
     */
    public void removeHandler(String handlername)
    {
        handlers.remove(handlername);
    }

    /**
     * Parse the request and execute the handler method, if one is
     * found. Returns the result as XML.  The calling Java code
     * doesn't need to know whether the call was successful or not
     * since this is all packed into the response.
     */
    public byte[] execute(InputStream is)
    {
        return execute(is, null, null);
    }

    /**
     * Parse the request and execute the handler method, if one is
     * found. If the invoked handler is AuthenticatedXmlRpcHandler,
     * use the credentials to authenticate the user.
     */
    public byte[] execute(InputStream is, String user, String password)
    {
        Worker worker = getWorker();
        byte[] retval = worker.execute(is, user, password);
        pool.push(worker);
        return retval;
    }

    private final Worker getWorker()
    {
        try
        {
            return(Worker) pool.pop();
        }
        catch(EmptyStackException x)
        {
            int maxThreads = XmlRpc.getMaxThreads();
            if (workers < maxThreads)
            {
                workers += 1;
                if (workers >= maxThreads * .95)
                {
                    System.err.println("95% of XML-RPC server threads in use");
                }
                return new Worker();
            }
            throw new RuntimeException("System overload");
        }
    }

    /**
     * Performs streaming, parsing, and handler execution.
     * Implementation is not thread-safe.
     */
    class Worker extends XmlRpc
    {
        private Vector inParams;
        private ByteArrayOutputStream buffer;
        private XmlWriter writer;

        /**
         * Creates a new instance.
         */
        protected Worker()
        {
            inParams = new Vector();
            buffer = new ByteArrayOutputStream();
        }

        /**
         * Given a request for the server, generates a response.
         */
        public byte[] execute(InputStream is, String user, String password)
        {
            try
            {
                // Do the work
                return executeInternal(is, user, password);
            }
            finally
            {
                // Release most of our resources
                buffer.reset();
                inParams.removeAllElements();
            }
        }

        private byte[] executeInternal(InputStream is, String user,
                                        String password)
        {
            byte[] result;
            long now = 0;
    
            if (XmlRpc.debug)
            {
                now = System.currentTimeMillis();
            }
            try
            {
                parse(is);
                if (XmlRpc.debug)
                {
                    System.err.println("method name: " + methodName);
                    System.err.println("inparams: " + inParams);
                }
                // check for errors from the XML parser
                if (errorLevel > NONE)
                {
                    throw new Exception(errorMsg);
                }
                Object handler = null;

                String handlerName = null;
                int dot = methodName.lastIndexOf('.');
                if (dot > -1)
                {
                    handlerName = methodName.substring(0, dot);
                    handler = handlers.get(handlerName);
                    if (handler != null)
                    {
                        methodName = methodName.substring(dot + 1);
                    }
                }

                if (handler == null)
                {
                    handler = handlers.get("$default");
                }

                if (handler == null)
                {
                    if (dot > -1)
                    {
                        throw new Exception("RPC handler object \""+
                                handlerName + "\" not found and no default handler registered.");
                    }
                    else
                    {
                        throw new Exception("RPC handler object not found for \""+
                                methodName + "\": no default handler registered.");
                    }
                }

                Object outParam;
                if (handler instanceof AuthenticatedXmlRpcHandler)
                {
                    outParam =((AuthenticatedXmlRpcHandler) handler).
                            execute(methodName, inParams, user, password);
                }
                else
                {
                    outParam =((XmlRpcHandler) handler).execute(
                            methodName, inParams);
                }
                if (XmlRpc.debug)
                {
                    System.err.println("outparam = "+outParam);
                }
                writer = new XmlWriter(buffer);
                writeResponse(outParam, writer);
                writer.flush();
                result = buffer.toByteArray();
            }
            catch(Exception x)
            {
                if (XmlRpc.debug)
                {
                    x.printStackTrace();
                }
                // Ensure that if there is anything in the buffer, it
                // is cleared before continuing with the writing of exceptions.
                // It is possible that something is in the buffer
                // if there were an exception during the writeResponse()
                // call above.
                buffer.reset();

                writer = null;
                try
                {
                    writer = new XmlWriter(buffer);
                }
                catch(UnsupportedEncodingException encx)
                {
                    System.err.println("XmlRpcServer attempted to use " +
                                       "unsupported encoding: " + encx);
                    // NOTE: If we weren't already using the default
                    // encoding, we could try it here.
                }
                catch(IOException iox)
                {
                    System.err.println("XmlRpcServer experienced I/O error " +
                                       "writing error response: " + iox);
                }

                String message = x.toString();
                // Retrieve XmlRpcException error code(if possible).
                int code = x instanceof XmlRpcException ?
                       ((XmlRpcException) x).code : 0;
                try
                {
                    writeError(code, message, writer);
                    writer.flush();
                }
                catch(Exception e)
                {
                    // Unlikely to occur, as we just sent a struct
                    // with an int and a string.
                    System.err.println("Unable to send error response to " +
                                       "client: " + e);
                }

                // If we were able to create a XmlWriter, we should
                // have a response.
                if (writer != null)
                {
                    result = buffer.toByteArray();
                }
                else
                {
                    result = EMPTY_BYTE_ARRAY;
                }
            }
            finally
            {
                if (writer != null)
                {
                    try
                    {
                        writer.close();
                    }
                    catch(IOException iox)
                    {
                        // This is non-fatal, but worth logging a
                        // warning for.
                        System.err.println("Exception closing output stream: " +
                                           iox);
                    }
                }
            }
            if (XmlRpc.debug)
            {
                System.err.println("Spent "+
                       (System.currentTimeMillis() - now) + " millis in request");
            }
            return result;
        }

        /**
         * Called when an object to be added to the argument list has
         * been parsed.
         */
        void objectParsed(Object what)
        {
            inParams.addElement(what);
        }

        /**
          * Writes an XML-RPC response to the XML writer.
          */
        void writeResponse(Object param, XmlWriter writer)
            throws XmlRpcException, IOException
        {
            writer.startElement("methodResponse");
            // if (param == null) param = ""; // workaround for Frontier bug
            writer.startElement("params");
            writer.startElement("param");
            writer.writeObject(param);
            writer.endElement("param");
            writer.endElement("params");
            writer.endElement("methodResponse");
        }

        /**
         * Writes an XML-RPC error response to the XML writer.
         */
        void writeError(int code, String message, XmlWriter writer)
            throws XmlRpcException, IOException
        {
            // System.err.println("error: "+message);
            Hashtable h = new Hashtable();
            h.put("faultCode", new Integer(code));
            h.put("faultString", message);
            writer.startElement("methodResponse");
            writer.startElement("fault");
            writer.writeObject(h);
            writer.endElement("fault");
            writer.endElement("methodResponse");
        }

    } // end of inner class Worker

} // XmlRpcServer

/**
 * Introspects handlers using Java Reflection to call methods matching
 * a XML-RPC call.
 */
class Invoker implements XmlRpcHandler
{
    private Object invokeTarget;
    private Class targetClass;

    public Invoker(Object target)
    {
        invokeTarget = target;
        targetClass = (invokeTarget instanceof Class) ? (Class) invokeTarget :
                invokeTarget.getClass();
        if (XmlRpc.debug)
        {
            System.err.println("Target object is " + targetClass);
        }
    }

    // main method, sucht methode in object, wenn gefunden dann aufrufen.
    public Object execute(String methodName,
            Vector params) throws Exception
    {
        // Array mit Classtype bilden, ObjectAry mit Values bilden
        Class[] argClasses = null;
        Object[] argValues = null;
        if (params != null)
        {
            argClasses = new Class[params.size()];
            argValues = new Object[params.size()];
            for (int i = 0; i < params.size(); i++)
            {
                argValues[i] = params.elementAt(i);
                if (argValues[i] instanceof Integer)
                {
                    argClasses[i] = Integer.TYPE;
                }
                else if (argValues[i] instanceof Double)
                {
                    argClasses[i] = Double.TYPE;
                }
                else if (argValues[i] instanceof Boolean)
                {
                    argClasses[i] = Boolean.TYPE;
                }
                else
                {
                    argClasses[i] = argValues[i].getClass();
                }
            }
        }

        // Methode da ?
        Method method = null;

        if (XmlRpc.debug)
        {
            System.err.println("Searching for method: " + methodName);
            for (int i = 0; i < argClasses.length; i++)
                System.err.println("Parameter " + i + ": " +
                        argClasses[i] + " = " + argValues[i]);
        }

        try
        {
            method = targetClass.getMethod(methodName, argClasses);
        }
        // Wenn nicht da dann entsprechende Exception returnen
        catch(NoSuchMethodException nsm_e)
        {
            throw nsm_e;
        }
        catch(SecurityException s_e)
        {
            throw s_e;
        }

        // Our policy is to make all public methods callable except
        // the ones defined in java.lang.Object.
        if (method.getDeclaringClass() == Object.class)
        {
            throw new XmlRpcException(0, "Invoker can't call methods " +
                                      "defined in java.lang.Object");
        }

        // invoke
        Object returnValue = null;
        try
        {
            returnValue = method.invoke(invokeTarget, argValues);
        }
        catch(IllegalAccessException iacc_e)
        {
            throw iacc_e;
        }
        catch(IllegalArgumentException iarg_e)
        {
            throw iarg_e;
        }
        catch(InvocationTargetException it_e)
        {
            if (XmlRpc.debug)
            {
                it_e.getTargetException().printStackTrace();
            }
            // check whether the thrown exception is XmlRpcException
            Throwable t = it_e.getTargetException();
            if (t instanceof XmlRpcException)
            {
                throw (XmlRpcException) t;
            }
            // It is some other exception
            throw new Exception(t.toString());
        }
        return returnValue;
    }
}
