/*
 * 11/19/04 1.0 moved to LGPL.
 * 12/12/99 Initial implementation.        mdm@techie.com.
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
 * A Type-safe representation of the supported output channel
 * constants.
 * <p>
 * This class is immutable and, hence, is thread safe.
 *
 * @author Mat McGowan 12/12/99
 * @since 0.0.7
 */
public class OutputChannels {
    /**
     * Flag to indicate output should include both channels.
     */
    public static final int BOTH_CHANNELS = 0;

    /**
     * Flag to indicate output should include the left channel only.
     */
    public static final int LEFT_CHANNEL = 1;

    /**
     * Flag to indicate output should include the right channel only.
     */
    public static final int RIGHT_CHANNEL = 2;

    /**
     * Flag to indicate output is mono.
     */
    public static final int DOWNMIX_CHANNELS = 3;

    public static final OutputChannels LEFT = new OutputChannels(LEFT_CHANNEL);
    public static final OutputChannels RIGHT = new OutputChannels(RIGHT_CHANNEL);
    public static final OutputChannels BOTH = new OutputChannels(BOTH_CHANNELS);
    public static final OutputChannels DOWNMIX = new OutputChannels(DOWNMIX_CHANNELS);

    private int outputChannels;

    /**
     * Creates an <code>OutputChannels</code> instance
     * corresponding to the given channel code.
     *
     * @param code one of the OutputChannels channel code constants.
     * @throws IllegalArgumentException if code is not a valid
     *                                  channel code.
     */
    static public OutputChannels fromInt(int code) {
        switch (code) {
        case LEFT_CHANNEL:
            return LEFT;
        case RIGHT_CHANNEL:
            return RIGHT;
        case BOTH_CHANNELS:
            return BOTH;
        case DOWNMIX_CHANNELS:
            return DOWNMIX;
        default:
            throw new IllegalArgumentException("Invalid channel code: " + code);
        }
    }

    private OutputChannels(int channels) {
        outputChannels = channels;

        if (channels < 0 || channels > 3)
            throw new IllegalArgumentException("channels");
    }

    /**
     * Retrieves the code representing the desired output channels.
     * Will be one of LEFT_CHANNEL, RIGHT_CHANNEL, BOTH_CHANNELS
     * or DOWNMIX_CHANNELS.
     *
     * @return the channel code represented by this instance.
     */
    public int getChannelsOutputCode() {
        return outputChannels;
    }

    /**
     * Retrieves the number of output channels represented
     * by this channel output type.
     *
     * @return The number of output channels for this channel output
     * type. This will be 2 for BOTH_CHANNELS only, and 1
     * for all other types.
     */
    public int getChannelCount() {
        int count = (outputChannels == BOTH_CHANNELS) ? 2 : 1;
        return count;
    }


    public boolean equals(Object o) {
        boolean equals = false;

        if (o instanceof OutputChannels) {
            OutputChannels oc = (OutputChannels) o;
            equals = (oc.outputChannels == outputChannels);
        }

        return equals;
    }

    public int hashCode() {
        return outputChannels;
    }

}
