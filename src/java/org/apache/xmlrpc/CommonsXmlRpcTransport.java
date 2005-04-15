package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Implementor of the XmlRpcTransport interface using the Apache
 * Commons HttpClient library v2.0 available at
 * http://jakarta.apache.org/commons/httpclient/
 *
 * Note: <b>Currently this transport is not thread safe</b>
 *
 * @author <a href="mailto:rhoegg@isisnetworks.net">Ryan Hoegg</a>
 * @version $Id$
 * @since 1.2
 */
public class CommonsXmlRpcTransport implements XmlRpcTransport 
{
    
    protected PostMethod method;

    /** Creates a new instance of CommonsXmlRpcTransport */
    public CommonsXmlRpcTransport(URL url, HttpClient client) 
    {
        this.url = url;
        if (client == null) 
        {
            HttpClient newClient = new HttpClient();
            this.client = newClient;
        } 
        else 
        {
            this.client = client;
        }
    }
    
    public CommonsXmlRpcTransport(URL url) 
    {
        this(url, null);
    }
    
    private URL url;
    private HttpClient client;
    private final Header userAgentHeader = new Header("User-Agent", XmlRpc.version);
    private boolean http11 = false; // defaults to HTTP 1.0
    private boolean gzip = false;
    private boolean rgzip = false;
    private Credentials creds;
    
    public InputStream sendXmlRpc(byte[] request) throws IOException, XmlRpcClientException 
    {
        method = new PostMethod(url.toString());
        method.setHttp11(http11);
        method.setRequestHeader(new Header("Content-Type", "text/xml"));
        
        if (rgzip)
        	method.setRequestHeader(new Header("Content-Encoding", "gzip"));
        
        if (gzip)
        	method.setRequestHeader(new Header("Accept-Encoding", "gzip"));
                
        method.setRequestHeader(userAgentHeader);

        if (rgzip)
        {
        	ByteArrayOutputStream lBo = new ByteArrayOutputStream();
        	GZIPOutputStream lGzo = new GZIPOutputStream(lBo);
        	lGzo.write(request);
        	lGzo.finish();        	
        	lGzo.close();        	
        	byte[] lArray = lBo.toByteArray();
        	method.setRequestBody(new ByteArrayInputStream(lArray));
        	method.setRequestContentLength(-1);
        }
        else
        	method.setRequestBody(new ByteArrayInputStream(request));
        
        URI hostURI = new URI(url.toString());
        HostConfiguration hostConfig = new HostConfiguration();
        hostConfig.setHost(hostURI);
        client.executeMethod(hostConfig, method);

        boolean lgzipo = false;
        
        Header lHeader = method.getResponseHeader( "Content-Encoding" );
        if ( lHeader != null ) {
            String lValue = lHeader.getValue();
            if ( lValue != null )
        		lgzipo = (lValue.indexOf( "gzip" ) >= 0);
        }

        if (lgzipo)
        	return( new GZIPInputStream( method.getResponseBodyAsStream() ) );
        else
        	return method.getResponseBodyAsStream();
    }
    
    /**
     * Make use of HTTP 1.1 
     * 
     * @param http11 HTTP 1.1 will be used if http11 is true
     */
    public void setHttp11(boolean http11) 
    {
        this.http11 = http11;
    }
    
    /**
     * Transport make use of the 'Accept-Encoding: gzip', so compliant HTTP servers
     * could return HTTP reply compressed with gzip 
     *   
     * @param gzip  Gzip compression will be used if gzip is true
     */
    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }
    
    /**
     * Transport make use of the 'Content-Encoding: gzip' and send HTTP request
     * compressed with gzip : works only with some compliant HTTP servers like Apache 2.x
     * using SetInputFilter DEFLATE.
     *   
     * @param gzip  Compress request with gzip if gzip is true
     */
    public void setRGzip(boolean gzip) {
        this.rgzip = gzip;
    }
    
    /**
     * Set the UserAgent for this client
     * 
     * @param userAgent
     */
    public void setUserAgent(String userAgent) 
    {
        userAgentHeader.setValue(userAgent);
    }

    /**
     * Sets Authentication for this client, very basic for now user/password
     * 
     * @param user
     * @param password
     */
    public void setBasicAuthentication(String user, String password)
    {
        creds = new UsernamePasswordCredentials(user, password);
        client.getState().setCredentials(null, null, creds);
    }

    public void endClientRequest()
    throws XmlRpcClientException
    {
        // Rlease connection resources
        method.releaseConnection();
    }
}
