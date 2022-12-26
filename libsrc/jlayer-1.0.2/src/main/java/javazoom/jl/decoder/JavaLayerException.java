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

import java.io.PrintStream;


/**
 * The JavaLayerException is the base class for all API-level
 * exceptions thrown by JavaLayer. To facilitate conversion and
 * common handling of exceptions from other domains, the class
 * can delegate some functionality to a contained Throwable instance.
 * <p>
 *
 * @author MDM
 */
public class JavaLayerException extends Exception {

    private Throwable exception;

    public JavaLayerException() {
    }

    public JavaLayerException(String msg) {
        super(msg);
    }

    public JavaLayerException(String msg, Throwable t) {
        super(msg);
        exception = t;
    }

    public Throwable getException() {
        return exception;
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintStream ps) {
        if (this.exception == null) {
            super.printStackTrace(ps);
        } else {
            exception.printStackTrace();
        }
    }
}
