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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * A quick and dirty XML writer.  If you feed it a
 * <code>ByteArrayInputStream</code>, it may be necessary to call
 * <code>writer.flush()</code> before calling
 * <code>buffer.toByteArray()</code> to get the data written to
 * your byte buffer.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 */
class XmlWriter extends OutputStreamWriter
{
    // Various XML pieces.
    protected static final String PROLOG_START =
        "<?xml version=\"1.0\" encoding=\"";
    protected static final String PROLOG_END = "\"?>";
    protected static final String CLOSING_TAG_START = "</";
    protected static final String SINGLE_TAG_END = "/>";
    protected static final String LESS_THAN_ENTITY = "&lt;";
    protected static final String GREATER_THAN_ENTITY = "&gt;";
    protected static final String AMPERSAND_ENTITY = "&amp;";

    /**
     * Java's name for the the ISO8859_1 encoding.
     */
    protected static final String ISO8859_1 = "ISO8859_1";

    /**
     * Java's name for the the UTF8 encoding.
     */
    protected static final String UTF8 = "UTF8";

    /**
     * Mapping between Java encoding names and "real" names used in
     * XML prolog.
     */
    private static Properties encodings = new Properties();

    static
    {
        encodings.put(UTF8, "UTF-8");
        encodings.put(ISO8859_1, "ISO-8859-1");
    }

    /**
     * Creates a new instance.
     *
     * @param out The stream to write output to.
     * @param enc The encoding to using for outputing XML.
     * @throws UnsupportedEncodingException Encoding unrecognized.
     * @throws IOException Problem writing.
     */
    public XmlWriter(OutputStream out, String enc)
        throws UnsupportedEncodingException, IOException
    {
        // Super-class wants the Java form of the encoding.
        super(out, enc);

        // Add the XML prolog (including the encoding in XML form).
        write(PROLOG_START);
        write(canonicalizeEncoding(enc));
        write(PROLOG_END);
    }

    /**
     * Tranforms a Java encoding to the canonical XML form (if a
     * mapping is available).
     *
     * @param javaEncoding The name of the encoding as known by Java.
     * @return The XML encoding (if a mapping is available);
     * otherwise, the encoding as provided.
     */
    protected static String canonicalizeEncoding(String javaEncoding)
    {
        return encodings.getProperty(javaEncoding, javaEncoding);
    }

    /**
     * Writes the XML representation of a supported Java object type.
     *
     * @param obj The <code>Object</code> to write.
     * @exception XmlRpcException Unsupported character data found.
     * @exception IOException Problem writing data.
     * @throws IllegalArgumentException If a <code>null</code>
     * parameter is passed to this method (not supported by the <a
     * href="http://xml-rpc.com/spec">XML-RPC specification</a>).
     */
    public void writeObject(Object obj)
        throws XmlRpcException, IOException
    {
        startElement("value");
        if (obj == null)
        {
            throw new IllegalArgumentException
                ("null values not supported by XML-RPC");
        }
        else if (obj instanceof String)
        {
            chardata(obj.toString());
        }
        else if (obj instanceof Integer)
        {
            startElement("int");
            write(obj.toString());
            endElement("int");
        }
        else if (obj instanceof Boolean)
        {
            startElement("boolean");
            write(((Boolean) obj).booleanValue() ? "1" : "0");
            endElement("boolean");
        }
        else if (obj instanceof Double || obj instanceof Float)
        {
            startElement("double");
            write(obj.toString());
            endElement("double");
        }
        else if (obj instanceof Date)
        {
            startElement("dateTime.iso8601");
            Date d = (Date) obj;
            // TODO: Stop using package private variable from XmlRpc
            write(XmlRpc.dateformat.format(d));
            endElement("dateTime.iso8601");
        }
        else if (obj instanceof byte[])
        {
            startElement("base64");
            this.write(Base64.encode((byte[]) obj));
            endElement("base64");
        }
        else if (obj instanceof Object[])
        {
            startElement("array");
            startElement("data");
            Object[] array = (Object []) obj;
            for (int i = 0; i < array.length; i++)
            {
                writeObject(array[i]);
            }
            endElement("data");
            endElement("array");
        }
        else if (obj instanceof Vector)
        {
            startElement("array");
            startElement("data");
            Vector array = (Vector) obj;
            int size = array.size();
            for (int i = 0; i < size; i++)
            {
                writeObject(array.elementAt(i));
            }
            endElement("data");
            endElement("array");
        }
        else if (obj instanceof Hashtable)
        {
            startElement("struct");
            Hashtable struct = (Hashtable) obj;
            for (Enumeration e = struct.keys(); e.hasMoreElements(); )
            {
                String key = (String) e.nextElement();
                Object value = struct.get(key);
                startElement("member");
                startElement("name");
                chardata(key);
                endElement("name");
                writeObject(value);
                endElement("member");
            }
            endElement("struct");
        }
        else
        {
            throw new RuntimeException("unsupported Java type: "
                                       + obj.getClass());
        }
        endElement("value");
    }

    /**
     * This is used to write out the Base64 output...
     */
    protected void write(byte[] byteData) throws IOException
    {
        for (int i = 0; i < byteData.length; i++)
        {
            write(byteData[i]);
        }
    }

    /**
     *
     * @param elem
     * @throws IOException
     */
    protected void startElement(String elem) throws IOException
    {
        write('<');
        write(elem);
        write('>');
    }

    /**
     *
     * @param elem
     * @throws IOException
     */
    protected void endElement(String elem) throws IOException
    {
        write(CLOSING_TAG_START);
        write(elem);
        write('>');
    }

    /**
     *
     * @param elem
     * @throws IOException
     */
    protected void emptyElement(String elem) throws IOException
    {
        write('<');
        write(elem);
        write(SINGLE_TAG_END);
    }

    /**
     * Writes text as <code>PCDATA</code>.
     *
     * @param text The data to write.
     * @exception XmlRpcException Unsupported character data found.
     * @exception IOException Problem writing data.
     */
    protected void chardata(String text)
        throws XmlRpcException, IOException
    {
        int l = text.length ();
        for (int i = 0; i < l; i++)
        {
            char c = text.charAt (i);
            switch (c)
            {
            case '\t':
            case '\r':
            case '\n':
                write(c);
                break;
            case '<':
                write(LESS_THAN_ENTITY);
                break;
            case '>':
                write(GREATER_THAN_ENTITY);
                break;
            case '&':
                write(AMPERSAND_ENTITY);
                break;
            default:
                if (c < 0x20 || c > 0xff)
                {
                    // Though the XML-RPC spec allows any ASCII
                    // characters except '<' and '&', the XML spec
                    // does not allow this range of characters,
                    // resulting in a parse error from most XML
                    // parsers.
                    throw new XmlRpcException(0, "Invalid character data " +
                                              "corresponding to XML entity &#" +
                                              String.valueOf((int) c) + ';');
                }
                else
                {
                    write(c);
                }
            }
        }
    }
}
