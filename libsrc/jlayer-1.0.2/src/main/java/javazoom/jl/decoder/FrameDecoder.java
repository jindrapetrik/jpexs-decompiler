/*
 * 09/26/08     throw exception on subband alloc error: Christopher G. Jennings (cjennings@acm.org)
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
 * Implementations of FrameDecoder are responsible for decoding
 * an MPEG audio frame.
 * <p>
 * REVIEW: the interface currently is too thin. There should be
 * methods to specify the output buffer, the synthesis filters and
 * possibly other objects used by the decoder.
 */
public interface FrameDecoder {

    /**
     * Decodes one frame of MPEG audio.
     */
    void decodeFrame() throws DecoderException;
}
