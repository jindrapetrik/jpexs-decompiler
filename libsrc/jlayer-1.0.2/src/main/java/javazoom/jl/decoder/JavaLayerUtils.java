/*
 * 11/19/04        1.0 moved to LGPL.
 * 12/12/99        Initial version.    mdm@techie.com
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package javazoom.jl.decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;


/**
 * The JavaLayerUtils class is not strictly part of the JavaLayer API.
 * It serves to provide useful methods and system-wide hooks.
 *
 * @author MDM
 */
public class JavaLayerUtils {
    static private JavaLayerHook hook = null;

    /**
     * Deserializes the object contained in the given input stream.
     *
     * @param in  The input stream to deserialize an object from.
     * @param cls The expected class of the deserialized object.
     */
    static public Object deserialize(InputStream in, Class<?> cls)
            throws IOException {
        if (cls == null)
            throw new NullPointerException("cls");

        Object obj = deserialize(in, cls);
        if (!cls.isInstance(obj)) {
            throw new InvalidObjectException("type of deserialized instance not of required class.");
        }

        return obj;
    }

    /**
     * Deserializes an object from the given <code>InputStream</code>.
     * The deserialization is delegated to an <code>
     * ObjectInputStream</code> instance.
     *
     * @param in The <code>InputStream</code> to deserialize an object
     *           from.
     * @return The object deserialized from the stream.
     * @throws IOException is thrown if there was a problem reading
     *                     the underlying stream, or an object could not be deserialized
     *                     from the stream.
     * @see java.io.ObjectInputStream
     */
    static public Object deserialize(InputStream in)
            throws IOException {
        if (in == null)
            throw new NullPointerException("in");

        ObjectInputStream objIn = new ObjectInputStream(in);

        Object obj;

        try {
            obj = objIn.readObject();
        } catch (ClassNotFoundException ex) {
            throw new InvalidClassException(ex.toString());
        }

        return obj;
    }

    /**
     * Deserializes an array from a given <code>InputStream</code>.
     *
     * @param in       The <code>InputStream</code> to
     *                 deserialize an object from.
     * @param elemType The class denoting the type of the array
     *                 elements.
     * @param length   The expected length of the array, or -1 if
     *                 any length is expected.
     */
    static public Object deserializeArray(InputStream in, Class<?> elemType, int length)
            throws IOException {
        if (elemType == null)
            throw new NullPointerException("elemType");

        if (length < -1)
            throw new IllegalArgumentException("length");

        Object obj = deserialize(in);

        Class<?> cls = obj.getClass();


        if (!cls.isArray())
            throw new InvalidObjectException("object is not an array");

        Class<?> arrayElemType = cls.getComponentType();
        if (arrayElemType != elemType)
            throw new InvalidObjectException("unexpected array component type");

        if (length != -1) {
            int arrayLength = Array.getLength(obj);
            if (arrayLength != length)
                throw new InvalidObjectException("array length mismatch");
        }

        return obj;
    }

    static public Object deserializeArrayResource(String name, Class<?> elemType, int length)
            throws IOException {
        InputStream str = getResourceAsStream(name);
        if (str == null)
            throw new IOException("unable to load resource '" + name + "'");

        Object obj = deserializeArray(str, elemType, length);

        return obj;
    }

    static public void serialize(OutputStream out, Object obj)
            throws IOException {
        if (out == null)
            throw new NullPointerException("out");

        if (obj == null)
            throw new NullPointerException("obj");

        ObjectOutputStream objOut = new ObjectOutputStream(out);
        objOut.writeObject(obj);
    }

    /**
     * Sets the system-wide JavaLayer hook.
     */
    static synchronized public void setHook(JavaLayerHook hook0) {
        hook = hook0;
    }

    static synchronized public JavaLayerHook getHook() {
        return hook;
    }

    /**
     * Retrieves an InputStream for a named resource.
     *
     * @param name The name of the resource. This must be a simple
     *             name, and not a qualified package name.
     * @return The InputStream for the named resource, or null if
     * the resource has not been found. If a hook has been
     * provided, its getResourceAsStream() method is called
     * to retrieve the resource.
     */
    static synchronized public InputStream getResourceAsStream(String name) {
        InputStream is = null;

        if (hook != null) {
            is = hook.getResourceAsStream(name);
        } else {
            Class<?> cls = JavaLayerUtils.class;
            is = cls.getResourceAsStream(name);
        }

        return is;
    }
}
