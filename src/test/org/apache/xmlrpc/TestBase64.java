package org.apache.xmlrpc;

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

import java.util.*;
import java.io.IOException;

public class TestBase64 
    implements Runnable
{

    XmlRpcClient client;
    static String url;
    static int clients = 1; // 6;
    static int loops = 1; //00;

    int gCalls = 0, gErrors = 0;

    byte[] data;

    public TestBase64 () throws Exception
    {
        client = new XmlRpcClientLite (url);

        Vector args = new Vector ();
        // Some JITs (Symantec, IBM) have problems with several Threads
        // starting all at the same time.
        // This initial XML-RPC call seems to pacify them.
        args.addElement (new Integer (123));
        client.execute ("math.abs", args);

        data = new byte[20000];
        for (int j = 0; j < data.length; j++)
            data[j] = (byte) j;

        for (int i = 0; i < clients; i++)
            new Thread (this).start ();
    }

    public void run ()
    {
        int errors = 0;
        int calls = 0;
        long start = System.currentTimeMillis ();
        try
        {
            int val = (int)(-100 * Math.random ());
            Vector args = new Vector ();

            args.addElement (data);

            for (int i = 0; i < loops; i++)
            {

                Vector v = (Vector) client.execute ("echo", args);
                byte[] d = (byte[]) v.elementAt (0);
                for (int j = 0; j < d.length; j++)
                    if (d[j] != (byte) j)
                        errors += 1;
                calls += 1;
            }
        }
        catch (IOException x)
        {
            System.err.println ("Exception in client: "+x);
            x.printStackTrace ();
        }
        catch (XmlRpcException x)
        {
            System.err.println ("Server reported error: "+x);
        }
        catch (Exception other)
        {
            System.err.println ("Exception in Benchmark client: "+other);
        }
        int millis = (int)(System.currentTimeMillis () - start);
        checkout (calls, errors, millis);
    }

    private synchronized void checkout (int calls, int errors, int millis)
    {
        clients--;
        gCalls += calls;
        gErrors += errors;
        System.err.println ("Benchmark thread finished: "+calls + " calls, "+
                errors + " errors in "+millis + " milliseconds.");
        if (clients == 0)
        {
            System.err.println ("");
            System.err.println ("Benchmark result: "+
                    (1000 * gCalls / millis) + " calls per second.");
        }
    }

    public static void main (String args[]) throws Exception
    {
        if (args.length > 0 && args.length < 3)
        {
            url = args[0];
            XmlRpc.setKeepAlive (true);
            // XmlRpc.setDebug (true);
            if (args.length == 2)
                XmlRpc.setDriver (args[1]);
            new TestBase64 ();
        }
        else
        {
            System.err.println ("Usage: java org.apache.xmlrpc.Benchmark URL [SAXDriver]");
        }
    }

}
