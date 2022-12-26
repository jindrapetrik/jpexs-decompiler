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


/**
 * <i>Work In Progress.</i>
 * <p>
 * An instance of <code>InputStreamSource</code> implements a
 * <code>Source</code> that provides data from an <code>InputStream
 * </code>. Seeking functionality is not supported.
 *
 * @author MDM
 */
public class InputStreamSource implements Source {

    private final InputStream in;

    public InputStreamSource(InputStream in) {
        if (in == null)
            throw new NullPointerException("in");

        this.in = in;
    }

    public int read(byte[] b, int offs, int len)
            throws IOException {
        int read = in.read(b, offs, len);
        return read;
    }

    public boolean willReadBlock() {
        return true;
    }

    public boolean isSeekable() {
        return false;
    }

    public long tell() {
        return -1;
    }

    public long seek(long to) {
        return -1;
    }

    public long length() {
        return -1;
    }
}
