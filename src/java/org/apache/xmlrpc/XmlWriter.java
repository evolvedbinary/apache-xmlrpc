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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.apache.xmlrpc.util.DateTool;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.EncoderException;

/**
 * A quick and dirty XML writer.  If you feed it a
 * <code>ByteArrayInputStream</code>, it may be necessary to call
 * <code>writer.flush()</code> before calling
 * <code>buffer.toByteArray()</code> to get the data written to
 * your byte buffer.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author Daniel L. Rall
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
    
    protected static final Base64 base64Codec = new Base64();

    /**
     * Class to delegate type decoding to.
     */
    protected static TypeDecoder typeDecoder;

    /**
     * Mapping between Java encoding names and "real" names used in
     * XML prolog.
     */
    private static Properties encodings = new Properties();

    static
    {
        encodings.put(UTF8, "UTF-8");
        encodings.put(ISO8859_1, "ISO-8859-1");
        typeDecoder = new DefaultTypeDecoder();
    }

    /**
     * Thread-safe wrapper for the <code>DateFormat</code> object used
     * to parse date/time values.
     */
    private static DateTool dateTool = new DateTool();

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
            throw new XmlRpcException
                (0, "null values not supported by XML-RPC");
        }
        else if (obj instanceof String)
        {
            chardata(obj.toString());
        }
        else if (typeDecoder.isXmlRpcI4(obj))
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
        else if (typeDecoder.isXmlRpcDouble(obj))
        {
            startElement("double");
            write(obj.toString());
            endElement("double");
        }
        else if (obj instanceof Date)
        {
            startElement("dateTime.iso8601");
            Date d = (Date) obj;
            write(dateTool.format(d));
            endElement("dateTime.iso8601");
        }
        else if (obj instanceof byte[])
        {
            startElement("base64");
            try
            {
                this.write((byte[]) base64Codec.encode(obj));
            }
            catch (EncoderException e)
            {
                throw new XmlRpcException
                    (0, "Unable to Base 64 encode byte array", e);
            }
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
            throw new XmlRpcException(0, "Unsupported Java type: "
                                       + obj.getClass(), null);
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
        String enc = super.getEncoding();
        boolean isUnicode = UTF8.equals(enc) || "UTF-16".equals(enc);
        // ### TODO: Use a buffer rather than going character by
        // ### character to scale better for large text sizes.
        //char[] buf = new char[32];
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
                if (c < 0x20 || c > 0x7f)
                {
                    // Though the XML-RPC spec allows any ASCII
                    // characters except '<' and '&', the XML spec
                    // does not allow this range of characters,
                    // resulting in a parse error from most XML
                    // parsers.  However, the XML spec does require
                    // XML parsers to support UTF-8 and UTF-16.
                    if (isUnicode)
                    {
                        if (c < 0x20)
                        {
                            // Entity escape the character.
                            write("&#");
                            // ### Do we really need the String conversion?
                            write(String.valueOf((int) c));
                            write(';');
                        }
                        else // c > 0x7f
                        {
                            // Write the character in our encoding.
                            write(new String(String.valueOf(c).getBytes(enc)));
                        }
                    }
                    else
                    {
                        throw new XmlRpcException(0, "Invalid character data "
                                                  + "corresponding to XML "
                                                  + "entity &#"
                                                  + String.valueOf((int) c)
                                                  + ';');
                    }
                }
                else
                {
                    write(c);
                }
            }
        }
    }

    protected static void setTypeDecoder(TypeDecoder newTypeDecoder)
    {
        typeDecoder = newTypeDecoder;
    }
}
