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
 * Exception error codes for components of the JavaLayer API.
 */
public interface JavaLayerErrors {

    /**
     * The first bitstream error code. See the {@link DecoderErrors DecoderErrors}
     * interface for other bitstream error codes.
     */
    int BITSTREAM_ERROR = 0x100;

    /**
     * The first decoder error code. See the {@link DecoderErrors DecoderErrors}
     * interface for other decoder error codes.
     */
    int DECODER_ERROR = 0x200;
}
