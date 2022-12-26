/*
 * 11/19/04        1.0 moved to LGPL.
 * 01/12/99        Initial version.    mdm@techie.com
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
 * The <code>DecoderException</code> represents the class of
 * errors that can occur when decoding MPEG audio.
 *
 * @author MDM
 */
public class DecoderException extends JavaLayerException
        implements DecoderErrors {

    private int errorCode = UNKNOWN_ERROR;

    public DecoderException(String msg, Throwable t) {
        super(msg, t);
    }

    public DecoderException(int errorCode, Throwable t) {
        this(getErrorString(errorCode), t);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    static public String getErrorString(int errorcode) {
        // REVIEW: use resource file to map error codes
        // to locale-sensitive strings.

        return "Decoder errorCode " + Integer.toHexString(errorcode);
    }
}

