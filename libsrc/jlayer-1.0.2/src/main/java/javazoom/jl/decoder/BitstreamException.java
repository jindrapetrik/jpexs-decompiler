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

/**
 * Instances of <code>BitstreamException</code> are thrown
 * when operations on a <code>Bitstream</code> fail.
 * <p>
 * The exception provides details of the exception condition
 * in two ways:
 * <ol><li>
 *        as an error-code describing the nature of the error
 * </li><br></br><li>
 *        as the <code>Throwable</code> instance, if any, that was thrown
 *        indicating that an exceptional condition has occurred.
 * </li></ol></p>
 *
 * @author MDM    12/12/99
 * @since 0.0.6
 */
public class BitstreamException extends JavaLayerException
        implements BitstreamErrors {
    private int errorCode = UNKNOWN_ERROR;

    public BitstreamException(String msg, Throwable t) {
        super(msg, t);
    }

    public BitstreamException(int errorCode, Throwable t) {
        this(getErrorString(errorCode), t);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    static public String getErrorString(int errorcode) {
        // REVIEW: use resource bundle to map error codes
        // to locale-sensitive strings.

        return "Bitstream errorCode " + Integer.toHexString(errorcode);
    }
}
