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

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests our Base64 encoding/decoding implementation.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @since 1.2
 */
public class Base64Test
    extends TestCase 
{
    private static final String[] TEST_DATA =
    {
        "foobar",
        "foo bar",
        "foobar   \t\r",
        "foo bar\nbaz"
    };

    private static final String UNENCODED =
        "This module provides functions to encode and decode\n" +
        "strings into the Base64 encoding specified in RFC 2045 -\n" +
        "MIME (Multipurpose Internet Mail Extensions). The Base64\n" +
        "encoding is designed to represent arbitrary sequences of\n" +
        "octets in a form that need not be humanly readable. A\n" +
        "65-character subset ([A-Za-z0-9+/=]) of US-ASCII is used,\n" +
        "enabling 6 bits to be represented per printable character.";

    /**
     * The string <code>UNENCODED</code> after being encoded by Perl's
     * MIME::Base64 module.
     */
    private static final String ENCODED =
        "VGhpcyBtb2R1bGUgcHJvdmlkZXMgZnVuY3Rpb25zIHRvIGVuY29kZSBhbmQgZGVjb2RlCnN0cmlu\n" +
        "Z3MgaW50byB0aGUgQmFzZTY0IGVuY29kaW5nIHNwZWNpZmllZCBpbiBSRkMgMjA0NSAtCk1JTUUg\n" +
        "KE11bHRpcHVycG9zZSBJbnRlcm5ldCBNYWlsIEV4dGVuc2lvbnMpLiBUaGUgQmFzZTY0CmVuY29k\n" +
        "aW5nIGlzIGRlc2lnbmVkIHRvIHJlcHJlc2VudCBhcmJpdHJhcnkgc2VxdWVuY2VzIG9mCm9jdGV0\n" +
        "cyBpbiBhIGZvcm0gdGhhdCBuZWVkIG5vdCBiZSBodW1hbmx5IHJlYWRhYmxlLiBBCjY1LWNoYXJh\n" +
        "Y3RlciBzdWJzZXQgKFtBLVphLXowLTkrLz1dKSBvZiBVUy1BU0NJSSBpcyB1c2VkLAplbmFibGlu\n" +
        "ZyA2IGJpdHMgdG8gYmUgcmVwcmVzZW50ZWQgcGVyIHByaW50YWJsZSBjaGFyYWN0ZXIu";


    /**
     * Constructor
     */
    public Base64Test(String testName) 
    {
        super(testName);
    }

    /**
     * Return the Test
     */
    public static Test suite() 
    {
        return new TestSuite(Base64Test.class);
    }

    public void testBase64()
        throws Exception
    {
        try
        {
            for (int i = 0; i < TEST_DATA.length; i++)
            {
                System.out.println("Input data: '" + TEST_DATA[i] + "'");
                byte[] raw = TEST_DATA[i].getBytes();
                byte[] encoded = Base64.encode(raw);
                byte[] decoded = Base64.decode(encoded);
                System.out.println("Encoded data: '" + new String(encoded) +
                                   "'");
                assertEquals(raw, decoded);
                assertEquals(TEST_DATA[i], new String(decoded));
            }

            assertEquals(Base64.encode(UNENCODED.getBytes()),
                         ENCODED.getBytes());
            assertEquals(UNENCODED.getBytes(),
                         Base64.decode(ENCODED.getBytes()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Byte by byte test for equality.
     */
    private void assertEquals(byte[] a, byte[] b)
    {
        if (a.length != b.length)
        {
            fail("Byte arrays have different lengths (" + a.length + " != " +
                 b.length + ")", a, b);
        }
        for (int i = 0; i < a.length; i++)
        {
            if (a[i] != b[i])
            {
                fail("Byte arrays not equal (" + a[i] + " != " + b[i] +
                     " at position + " + i + ")", a, b);
            }
        }
    }

    /**
     * Throws an <code>AssertionFailedError</code> using the two
     * supplied byte arrays formatted into the error message.
     */
    private void fail(String msg, byte[] a, byte[] b)
        throws AssertionFailedError
    {
        StringBuffer buf = new StringBuffer();
        writeToBuffer(buf, a);
        buf.append(" not equal to ");
        writeToBuffer(buf, b);
        fail(msg + ": " + buf);
    }

    /**
     * Writes <code>array</code> to <code>buf</code>.
     */
    private void writeToBuffer(StringBuffer buf, byte[] array)
    {
        buf.append("{ ");
        for (int i = 0; i < array.length; i++)
        {
            if (i > 0)
            {
                buf.append(", ");
            }
            buf.append(array[i]);
        }
        buf.append(" }");
    }
}
