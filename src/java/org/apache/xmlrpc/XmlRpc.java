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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import uk.co.wilson.xml.MinML;

/**
 * This abstract base class provides basic capabilities for XML-RPC,
 * like parsing of parameters or encoding Java objects into XML-RPC format.
 * Any XML parser with a <a href=http://www.megginson.com/SAX/> SAX</a> interface can
 * be used.<p>
 * XmlRpcServer and XmlRpcClient are the classes that actually implement an
 * XML-RCP server and client.
 *
 * @see XmlRpcServer
 * @see XmlRpcClient
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id$
 */
public abstract class XmlRpc extends HandlerBase
{
    /**
     * The version string used in HTTP communication.
     */
    public static final String version = "Apache XML-RPC 1.0";

    /**
     * The default parser to use (MinML).
     */
    private static final String DEFAULT_PARSER = MinML.class.getName();

    /**
     * The maximum number of threads which can be used concurrently.
     */
    private static int maxThreads = 100;

    String methodName;

    /**
     * The class name of SAX parser to use.
     */
    private static Class parserClass;
    private static Hashtable saxDrivers = new Hashtable (8);

    static
    {
        // A mapping of short identifiers to the fully qualified class
        // names of common SAX parsers.  If more mappings are added
        // here, increase the size of the saxDrivers Map used to store
        // them.
        saxDrivers.put("xerces", "org.apache.xerces.parsers.SAXParser");
        saxDrivers.put("xp", "com.jclark.xml.sax.Driver");
        saxDrivers.put("ibm1", "com.ibm.xml.parser.SAXDriver");
        saxDrivers.put("ibm2", "com.ibm.xml.parsers.SAXParser");
        saxDrivers.put("aelfred", "com.microstar.xml.SAXDriver");
        saxDrivers.put("oracle1", "oracle.xml.parser.XMLParser");
        saxDrivers.put("oracle2", "oracle.xml.parser.v2.SAXParser");
        saxDrivers.put("openxml", "org.openxml.parser.XMLSAXParser");
    }

    // the stack we're parsing our values into.
    Stack values;
    Value currentValue;

    /**
     * Thread-safe wrapper for the <code>DateFormat</code> object used
     * to format and parse date/time values.
     */
    static Formatter dateformat = new Formatter ();

    /**
     * Used to collect character data (<code>CDATA</code>) of
     * parameter values.
     */
    StringBuffer cdata;
    boolean readCdata;

    // XML RPC parameter types used for dataMode
    static final int STRING = 0;
    static final int INTEGER = 1;
    static final int BOOLEAN = 2;
    static final int DOUBLE = 3;
    static final int DATE = 4;
    static final int BASE64 = 5;
    static final int STRUCT = 6;
    static final int ARRAY = 7;

    // Error level + message
    int errorLevel;
    String errorMsg;

    static final int NONE = 0;
    static final int RECOVERABLE = 1;
    static final int FATAL = 2;

    /**
     * Wheter to use HTTP Keep-Alive headers.
     */
    static boolean keepalive = false;

    /**
     * Whether to log debugging output.
     */
    public static boolean debug = false;

    /**
     * The list of valid XML elements used for RPC.
     */
    final static String types[] =
    {
        "String",
        "Integer",
        "Boolean",
        "Double",
        "Date",
        "Base64",
        "Struct",
        "Array"
    };

    /**
     * Java's name for the encoding we're using.
     */
    static String encoding = "ISO8859_1";

    /**
     * Mapping between Java encoding names and "real" names used in
     * XML prolog.
     */
    static Properties encodings = new Properties ();

    static
    {
        encodings.put ("UTF8", "UTF-8");
        encodings.put ("ISO8859_1", "ISO-8859-1");
    }

    /**
     * Set the SAX Parser to be used. The argument can either be the
     * full class name or a user friendly shortcut if the parser is
     * known to this class. The parsers that can currently be set by
     * shortcut are listed in the main documentation page. If you are
     * using another parser please send me the name of the SAX driver
     * and I'll include it in a future release.  If setDriver() is
     * never called then the System property "sax.driver" is
     * consulted. If that is not defined the driver defaults to
     * OpenXML.
     */
    public static void setDriver(String driver) throws ClassNotFoundException
    {
        String parserClassName = null;
        try
        {
            parserClassName = (String) saxDrivers.get(driver);
            if (parserClassName == null)
            {
                // Identifier lookup failed, assuming we were provided
                // with the fully qualified class name.
                parserClassName = driver;
            }
            parserClass = Class.forName(parserClassName);
        }
        catch (ClassNotFoundException x)
        {
            throw new ClassNotFoundException ("SAX driver not found: "
                    + parserClassName);
        }
    }

    /**
     * Set the SAX Parser to be used by directly passing the Class object.
     */
    public static void setDriver(Class driver)
    {
        parserClass = driver;
    }

    /**
     * Set the encoding of the XML. This should be the name of a Java encoding
     * contained in the encodings Hashtable.
     */
    public static void setEncoding(String enc)
    {
        encoding = enc;
    }

    /**
     * Return the encoding, transforming to the canonical name if possible.
     */
    public String getEncoding ()
    {
        return encodings.getProperty(encoding, encoding);
    }

    /**
     * Gets the maximum number of threads used at any given moment.
     */
    public static int getMaxThreads()
    {
        return maxThreads;
    }

    /**
     * Sets the maximum number of threads used at any given moment.
     */
    public static void setMaxThreads(int maxThreads)
    {
        XmlRpc.maxThreads = maxThreads;
    }

    /**
     * Switch debugging output on/off.
     */
    public static void setDebug(boolean val)
    {
        debug = val;
    }

    /**
     * Switch HTTP keepalive on/off.
     */
    public static void setKeepAlive(boolean val)
    {
        keepalive = val;
    }

    /**
     * get current HTTP keepalive mode.
     */
    public static boolean getKeepAlive()
    {
        return keepalive;
    }

    /**
     * Parse the input stream. For each root level object, method
     * <code>objectParsed</code> is called.
     */
    synchronized void parse(InputStream is) throws Exception
    {
        // reset values (XmlRpc objects are reusable)
        errorLevel = NONE;
        errorMsg = null;
        values = new Stack ();
        if (cdata == null)
        {
            cdata = new StringBuffer(128);
        }
        else
        {
            cdata.setLength(0);
        }
        readCdata = false;
        currentValue = null;

        long now = System.currentTimeMillis();
        if (parserClass == null)
        {
            // try to get the name of the SAX driver from the System properties
            String driver;
            try
            {
                driver = System.getProperty("sax.driver", DEFAULT_PARSER);
            }
            catch (SecurityException e)
            {
                // An unsigned applet may not access system properties.
                driver = DEFAULT_PARSER;
            }
            setDriver(driver);
        }

        Parser parser = null;
        try
        {
            parser = (Parser) parserClass.newInstance();
        }
        catch (NoSuchMethodError nsm)
        {
            // This is thrown if no constructor exists for the parser class
            // and is transformed into a regular exception.
            throw new Exception("Can't create Parser: " + parserClass);
        }

        parser.setDocumentHandler(this);
        parser.setErrorHandler(this);

        if (debug)
        {
            System.err.println("Beginning parsing XML input stream");
        }
        parser.parse(new InputSource (is));
        if (debug)
        {
            System.err.println ("Spent " + (System.currentTimeMillis() - now)
                    + " millis parsing");
        }
    }

    /**
     *  This method is called when a root level object has been parsed.
     */
    abstract void objectParsed(Object what);

    ////////////////////////////////////////////////////////////////
    // methods called by XML parser

    /**
     * Method called by SAX driver.
     */
    public void characters(char ch[], int start, int length)
            throws SAXException
    {
        if (readCdata)
        {
            cdata.append(ch, start, length);
        }
    }

    /**
     * Method called by SAX driver.
     */
    public void endElement(String name) throws SAXException
    {

        if (debug)
        {
            System.err.println("endElement: " + name);
        }

        // finalize character data, if appropriate
        if (currentValue != null && readCdata)
        {
            currentValue.characterData(cdata.toString());
            cdata.setLength(0);
            readCdata = false;
        }

        if ("value".equals(name))
        {
            // Only handle top level objects or objects contained in
            // arrays here.  For objects contained in structs, wait
            // for </member> (see code below).
            int depth = values.size ();
            if (depth < 2 || values.elementAt(depth - 2).hashCode() != STRUCT)
            {
                Value v = currentValue;
                values.pop();
                if (depth < 2)
                {
                    // This is a top-level object
                    objectParsed(v.value);
                    currentValue = null;
                }
                else
                {
                    // Add object to sub-array; if current container
                    // is a struct, add later (at </member>).
                    currentValue = (Value) values.peek();
                    currentValue.endElement(v);
                }
            }
        }

        // Handle objects contained in structs.
        if ("member".equals(name))
        {
            Value v = currentValue;
            values.pop();
            currentValue = (Value) values.peek();
            currentValue.endElement(v);
        }

        else if ("methodName".equals(name))
        {
            methodName = cdata.toString();
            cdata.setLength(0);
            readCdata = false;
        }
    }

    /**
     * Method called by SAX driver.
     */
    public void startElement(String name, AttributeList atts)
            throws SAXException
    {
        if (debug)
        {
            System.err.println("startElement: " + name);
        }

        if ("value".equals(name))
        {
            // System.err.println ("starting value");
            Value v = new Value();
            values.push(v);
            currentValue = v;
            // cdata object is reused
            cdata.setLength(0);
            readCdata = true;
        }
        else if ("methodName".equals(name))
        {
            cdata.setLength(0);
            readCdata = true;
        }
        else if ("name".equals(name))
        {
            cdata.setLength(0);
            readCdata = true;
        }
        else if ("string".equals(name))
        {
            // currentValue.setType (STRING);
            cdata.setLength(0);
            readCdata = true;
        }
        else if ("i4".equals(name) || "int".equals(name))
        {
            currentValue.setType(INTEGER);
            cdata.setLength(0);
            readCdata = true;
        }
        else if ("boolean".equals(name))
        {
            currentValue.setType(BOOLEAN);
            cdata.setLength(0);
            readCdata = true;
        }
        else if ("double".equals(name))
        {
            currentValue.setType(DOUBLE);
            cdata.setLength(0);
            readCdata = true;
        }
        else if ("dateTime.iso8601".equals(name))
        {
            currentValue.setType(DATE);
            cdata.setLength(0);
            readCdata = true;
        }
        else if ("base64".equals(name))
        {
            currentValue.setType(BASE64);
            cdata.setLength(0);
            readCdata = true;
        }
        else if ("struct".equals(name))
        {
            currentValue.setType(STRUCT);
        }
        else if ("array".equals(name))
        {
            currentValue.setType(ARRAY);
        }
    }

    /**
     *
     * @param e
     * @throws SAXException
     */
    public void error(SAXParseException e) throws SAXException
    {
        System.err.println("Error parsing XML: " + e);
        errorLevel = RECOVERABLE;
        errorMsg = e.toString();
    }

    /**
     *
     * @param e
     * @throws SAXException
     */
    public void fatalError(SAXParseException e) throws SAXException
    {
        System.err.println("Fatal error parsing XML: " + e);
        errorLevel = FATAL;
        errorMsg = e.toString();
    }

    /**
     * This represents a XML-RPC value parsed from the request.
     */
    class Value
    {
        int type;
        Object value;
        // the name to use for the next member of struct values
        String nextMemberName;

        Hashtable struct;
        Vector array;

        /**
         * Constructor.
         */
        public Value()
        {
            this.type = STRING;
        }

        /**
         * Notification that a new child element has been parsed.
         */
        public void endElement(Value child)
        {
            switch (type)
            {
                case ARRAY:
                    array.addElement(child.value);
                    break;
                case STRUCT:
                    struct.put(nextMemberName, child.value);
            }
        }

        /**
         * Set the type of this value. If it's a container, create the
         * corresponding java container.
         */
        public void setType(int type)
        {
            // System.err.println ("setting type to "+types[type]);
            this.type = type;
            switch (type)
            {
                case ARRAY:
                    value = array = new Vector ();
                    break;
                case STRUCT:
                    value = struct = new Hashtable ();
                    break;
            }
        }

        /**
         * Set the character data for the element and interpret it
         * according to the element type.
         */
        public void characterData(String cdata)
        {
            switch (type)
            {
                case INTEGER:
                    value = new Integer(cdata.trim ());
                    break;
                case BOOLEAN:
                    value = ("1".equals(cdata.trim ())
                            ? Boolean.TRUE : Boolean.FALSE);
                    break;
                case DOUBLE:
                    value = new Double(cdata.trim ());
                    break;
                case DATE:
                    try
                    {
                        value = dateformat.parse(cdata.trim());
                    }
                    catch (ParseException p)
                    {
                        throw new RuntimeException(p.getMessage());
                    }
                    break;
                case BASE64:
                    value = Base64.decode(cdata.getBytes());
                    break;
                case STRING:
                    value = cdata;
                    break;
                case STRUCT:
                    // this is the name to use for the next member of this struct
                    nextMemberName = cdata;
                    break;
            }
        }

        /**
         * This is a performance hack to get the type of a value
         * without casting the Object.  It breaks the contract of
         * method hashCode, but it doesn't matter since Value objects
         * are never used as keys in Hashtables.
         */
        public int hashCode()
        {
            return type;
        }

        /**
         *
         * @return
         */
        public String toString()
        {
            return (types[type] + " element " + value);
        }
    }

    /**
     * A quick and dirty XML writer.  If you feed it a
     * <code>ByteArrayInputStream</code>, it may be necessary to call
     * <code>writer.flush()</code> before calling
     * <code>buffer.toByteArray()</code> to get the data written to
     * your byte buffer.
     */
    class XmlWriter extends OutputStreamWriter
    {
        protected static final String PROLOG_START
                = "<?xml version=\"1.0\" encoding=\"";
        protected static final String PROLOG_END = "\"?>";
        protected static final String CLOSING_TAG_START = "</";
        protected static final String SINGLE_TAG_END = "/>";
        protected static final String LESS_THAN_ENTITY = "&lt;";
        protected static final String GREATER_THAN_ENTITY = "&gt;";
        protected static final String AMPERSAND_ENTITY = "&amp;";

        /**
         *
         * @param out
         * @throws UnsupportedEncodingException
         * @throws IOException
         */
        public XmlWriter(OutputStream out)
                throws UnsupportedEncodingException, IOException
        {
            // The default encoding used for XML-RPC is ISO-8859-1.
            this(out, encoding);
        }

        /**
         *
         * @param out
         * @param enc
         * @throws UnsupportedEncodingException
         * @throws IOException
         */
        public XmlWriter(OutputStream out, String enc)
                throws UnsupportedEncodingException, IOException
        {
            super(out, enc);

            // Add the XML prolog (which includes the encoding)
            write(PROLOG_START);
            write(encodings.getProperty(enc, enc));
            write(PROLOG_END);
        }

        /**
         * Writes the XML representation of a supported Java object type.
         *
         * @param obj The <code>Object</code> to write.
         * @exception IOException Problem writing data.
         * @throws IllegalArgumentException If a <code>null</code>
         * parameter is passed to this method (not supported by the <a
         * href="http://xml-rpc.com/spec">XML-RPC specification</a>).
         */
        public void writeObject(Object obj) throws IOException
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
                write(dateformat.format(d));
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
                    String nextkey = (String) e.nextElement();
                    Object nextval = struct.get(nextkey);
                    startElement("member");
                    startElement("name");
                    write(nextkey);
                    endElement("name");
                    writeObject(nextval);
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
         *
         * @param text
         * @throws IOException
         */
        protected void chardata(String text) throws IOException
        {
            int l = text.length ();
            for (int i = 0; i < l; i++)
            {
                char c = text.charAt (i);
                switch (c)
                {
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
                    write(c);
                }
            }
        }
    }
}

/**
 * Wraps a <code>DateFormat</code> instance to provide thread safety.
 */
class Formatter
{
    private DateFormat f;

    /**
     * Uses the <code>DateFormat</code> string
     * <code>yyyyMMdd'T'HH:mm:ss</code>.
     */
    public Formatter()
    {
        f = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
    }

    /**
     * @param d The date to format.
     * @return the formatted date.
     */
    public synchronized String format(Date d)
    {
        return f.format(d);
    }

    /**
     * @param s The text to parse a date from.
     * @return The parsed date.
     * @throws ParseException If the date could not be parsed.
     */
    public synchronized Date parse(String s) throws ParseException
    {
        return f.parse(s);
    }
}
