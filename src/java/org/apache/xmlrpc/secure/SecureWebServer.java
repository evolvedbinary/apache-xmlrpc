package org.apache.xmlrpc.secure;

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

import com.sun.net.ssl.KeyManager;
import com.sun.net.ssl.KeyManagerFactory;
import com.sun.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import org.apache.xmlrpc.AuthDemo;
import org.apache.xmlrpc.Echo;
import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpc;

/**
 * A minimal web server that exclusively handles XML-RPC requests
 * over a secure channel.
 *
 * Standard security properties must be set before the SecureWebserver
 * can be used. The SecurityTool takes care of retrieving these
 * values, but the parent application must set the necessary
 * values before anything will work.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id$
 */
public class SecureWebServer 
    extends WebServer
    implements SecurityConstants
{
    /**
      * Creates a secure web server at the specified port number.
      *
      * @param int port number of secure web server.
      */
    public SecureWebServer (int port) throws IOException
    {
        this(port, null);
    }

    /**
      * Creates a secure web server at the specified port 
      * number and IP address.
      *
      * @param int port number of the secure web server
      * @param InetAddress ip address to bind to.
      */
    public SecureWebServer (int port, InetAddress add) throws IOException
    {
        super(port,add);
    }

    public void setupServerSocket(int port, int backlog, InetAddress add)
        throws Exception
    {
        SecurityTool.setup();
    
        SSLContext context = SSLContext.getInstance(SecurityTool.getSecurityProtocol());
          
        KeyManagerFactory keyManagerFactory = 
            KeyManagerFactory.getInstance(SecurityTool.getKeyManagerType());
            
        KeyStore keyStore = KeyStore.getInstance(SecurityTool.getKeyStoreType());
            
        keyStore.load(new FileInputStream(SecurityTool.getKeyStore()), 
            SecurityTool.getKeyStorePassword().toCharArray());
            
        keyManagerFactory.init(keyStore, SecurityTool.getKeyStorePassword().toCharArray());
            
        context.init(keyManagerFactory.getKeyManagers(), null, null);
        SSLServerSocketFactory sslSrvFact = context.getServerSocketFactory();
        serverSocket = (SSLServerSocket) sslSrvFact.createServerSocket(port);
    }

    /**
     * This <em>can</em> be called from command line, but you'll have to 
     * edit and recompile to change the server port or handler objects. 
     * By default, it sets up the following responders:
     * 
     * <ul><li> A java.lang.String object
     * <li> The java.lang.Math class (making its static methods callable via XML-RPC)
     * <li> An Echo handler that returns the argument array
     * </ul>
     */
    public static void main (String args[])
    {
        System.err.println ("Usage: java org.apache.xmlrpc.SecureWebServer [port]");
        
        int p = 10000;
        
        if (args.length > 0)
        {
            try
            {
                p = Integer.parseInt (args[0]);
            }
            catch (NumberFormatException nfx)
            {
                System.err.println ("Error parsing port number: "+args[0]);
            }
        }            

        XmlRpc.setKeepAlive (true);

        try
        {
            SecureWebServer webserver = new SecureWebServer (p);
            webserver.addHandler ("string", "Welcome to XML-RPC!");
            webserver.addHandler ("math", Math.class);
            webserver.addHandler ("auth", new AuthDemo());
            webserver.addHandler ("$default", new Echo());

            System.err.println ("started web server on port "+p);
        }
        catch (IOException x)
        {
            System.err.println ("Error creating web server: "+x);
        }
    }
}
