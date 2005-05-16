package org.apache.xmlrpc;

/* ====================================================================
 * The Apache Software License, Version 1.1
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
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests XmlWriter.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id$
 */
public class XmlWriterTest
    extends TestCase 
{
    private ByteArrayOutputStream buffer;
    private XmlWriter writer;

    /**
     * Constructor
     */
    public XmlWriterTest(String testName) 
    {
        super(testName);
    }

    /**
     * Return the Test
     */
    public static Test suite() 
    {
        return new TestSuite(XmlWriterTest.class);
    }

    /**
     * Setup the test.
     */
    public void setUp() 
    {
        XmlRpc.setDebug(true);
        buffer = new ByteArrayOutputStream();
    }
   
    /**
     * Tear down the test.
     */
    public void tearDown() 
    {
        XmlRpc.setDebug(false);
    }

    public void testForceAlternateEncoding()
        throws Exception
    {
        writer = new XmlWriter(buffer, null);
        assertEquals("null should be forced to UTF-8",
                     XmlWriter.UTF8, writer.getEncoding());

        writer = new XmlWriter(buffer, XmlWriter.ISO8859_1);
        assertEquals(XmlWriter.ISO8859_1 + " should be forced to " +
                     XmlWriter.UTF8, XmlWriter.UTF8, writer.getEncoding());

        writer = new XmlWriter(buffer, "ISO8859_15");
        assertEquals("ISO8859_15 should be forced to " + XmlWriter.UTF8,
                     XmlWriter.UTF8, writer.getEncoding());

        writer = new XmlWriter(buffer, "EUC_JP");
        assertEquals("EUC_JP should be forced to " + XmlWriter.UTF8,
                     XmlWriter.UTF8, writer.getEncoding());

        writer = new XmlWriter(buffer, XmlWriter.UTF16);
        assertEquals(XmlWriter.UTF16 + " should remain " + XmlWriter.UTF16,
                     XmlWriter.UTF16, writer.getEncoding());
    }

    public void testProlog()
        throws IOException
    {
        final String EXPECTED_PROLOG =
            XmlWriter.PROLOG_START + XmlWriter.PROLOG_END;

        writer = new XmlWriter(buffer, XmlWriter.UTF8);
        writer.write(new char[0], 0, 0);
        writer.flush();
        assertEquals("Unexpected or missing XML prolog when writing char[]",
                     EXPECTED_PROLOG, buffer.toString());
        // Append a space using an overload, and assure non-duplication.
        writer.write(' ');
        writer.flush();
        assertEquals("Unexpected or missing XML prolog when writing char",
                     EXPECTED_PROLOG + ' ', buffer.toString());

        buffer = new ByteArrayOutputStream();
        writer = new XmlWriter(buffer, XmlWriter.UTF8);
        writer.write("");
        writer.flush();
        assertEquals("Unexpected or missing XML prolog when writing String",
                     EXPECTED_PROLOG, buffer.toString());
        // Try again to assure it's not duplicated in the output.
        writer.write("");
        writer.flush();
        assertEquals("Unexpected or missing XML prolog when writing String",
                     EXPECTED_PROLOG, buffer.toString());
        
    }

    public void testBasicResults()
        throws Exception
    {
        try
        {
            writer = new XmlWriter(buffer, XmlWriter.UTF8);

            String foobar = "foobar";
            writer.writeObject(foobar);
            writer.flush();
            String postProlog = "<value>" + foobar + "</value>";
            assertTrue("Unexpected results from writing of String",
                       buffer.toString().endsWith(postProlog));

            Integer thirtySeven = new Integer(37);
            writer.writeObject(thirtySeven);
            writer.flush();
            postProlog += "<value><int>" + thirtySeven + "</int></value>";
            assertTrue(buffer.toString().endsWith(postProlog));

            Boolean flag = Boolean.TRUE;
            writer.writeObject(flag);
            writer.flush();
            postProlog += "<value><boolean>1</boolean></value>";
            assertTrue("Unexpected results from writing of Boolean",
                       buffer.toString().endsWith(postProlog));

            Object[] array = { foobar, thirtySeven };
            writer.writeObject(array);
            writer.flush();
            postProlog += "<value><array><data>";
            postProlog += "<value>" + foobar + "</value>";
            postProlog += "<value><int>" + thirtySeven + "</int></value>";
            postProlog += "</data></array></value>";
            assertTrue("Unexpected results from writing of Object[]",
                       buffer.toString().endsWith(postProlog));

            Hashtable map = new Hashtable();
            map.put(foobar, thirtySeven);
            writer.writeObject(map);
            writer.flush();
            postProlog += "<value><struct><member>";
            postProlog += "<name>" + foobar + "</name>";
            postProlog += "<value><int>" + thirtySeven + "</int></value>";
            postProlog += "</member></struct></value>";
            assertTrue("Unexpected results from writing of Hashtable",
                       buffer.toString().endsWith(postProlog));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testWriteCharacterReference()
        throws Exception
    {
        writer = new XmlWriter(buffer, null);
        writer.hasWrittenProlog = true;
        writer.writeObject(String.valueOf((char) 0x80));
        writer.flush();
        String postProlog = "<value>&#128;</value>";
        assertTrue("Character reference not created as expected",
                   buffer.toString().endsWith(postProlog));
    }
}
