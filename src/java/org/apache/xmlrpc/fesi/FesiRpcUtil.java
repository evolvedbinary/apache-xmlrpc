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
import FESI.Exceptions.*;
import FESI.Data.*;
import FESI.Interpreter.*;
import java.util.*;

/**
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 */
public class FesiRpcUtil
{
    // convert a generic Java object to a JavaScript Object.
    public static ESValue convertJ2E (Object what, Evaluator evaluator) 
        throws Exception
    {
        if (what == null)
            return ESNull.theNull;
        if (what instanceof Vector)
        {
            Vector v = (Vector) what;
            ArrayPrototype retval = new ArrayPrototype (
                    evaluator.getArrayPrototype (), evaluator);
            int l = v.size ();
            for (int i = 0; i < l; i++)
                retval.putProperty (i,
                        convertJ2E (v.elementAt (i), evaluator));
            return retval;
        }
        if (what instanceof Hashtable)
        {
            Hashtable t = (Hashtable) what;
            ESObject retval = new ObjectPrototype (
                    evaluator.getObjectPrototype (), evaluator);
            for (Enumeration e = t.keys(); e.hasMoreElements();)
            {
                String next = (String) e.nextElement ();
                retval.putProperty (next,
                        convertJ2E (t.get (next), evaluator),
                        next.hashCode ());
            }
            return retval;
        }
        
        if (what instanceof String)
        {
            return new ESString (what.toString ());
        }            
        if (what instanceof Number)
        {
            return new ESNumber (new Double (what.toString ()).doubleValue ());
        }            
        if (what instanceof Boolean)
        {
            return ESBoolean.makeBoolean (((Boolean) what).booleanValue ());
        }            
        if (what instanceof Date)
        {
            return new DatePrototype (evaluator, (Date) what);
        }            
        return ESLoader.normalizeValue (what, evaluator);
    }


    // convert a JavaScript Object object to a generic Java.
    public static Object convertE2J (ESValue what)
        throws EcmaScriptException
    {
        if (XmlRpc.debug)
        {
            System.out.println ("converting e-2-j: "+what.getClass ());
        }            
        if (what instanceof ESNull)
        {
            return null;
        }            
        if (what instanceof ArrayPrototype)
        {
            ArrayPrototype a = (ArrayPrototype) what;
            int l = a.size ();
            Vector v = new Vector ();
            for (int i = 0; i < l; i++)
            {
                Object nj = convertE2J (a.getProperty (i));
                v.addElement (nj);
            }
            return v;
        }
        if (what instanceof ObjectPrototype)
        {
            ObjectPrototype o = (ObjectPrototype) what;
            Hashtable t = new Hashtable ();
            for (Enumeration e = o.getProperties (); e.hasMoreElements ();)
            {
                String next = (String) e.nextElement ();
                if (XmlRpc.debug)
                {
                    System.out.println ("converting object member "+next);
                }                    
                Object nj = convertE2J (
                        o.getProperty (next, next.hashCode ()));
                if (nj != null)// can't put null as value in hashtable
                {
                    t.put (next, nj);
                }                    
            }
            return t;
        }
        if (what instanceof ESUndefined || what instanceof ESNull)
        {
            return null;
        }            
        Object jval = what.toJavaObject ();
        if (jval instanceof Byte || jval instanceof Short)
        {
            jval = new Integer (jval.toString ());
        }            
        return jval;
    }
}
