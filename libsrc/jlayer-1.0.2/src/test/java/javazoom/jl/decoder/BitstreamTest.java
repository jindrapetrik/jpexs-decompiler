/*
 * 11/19/2004 : 1.0 moved to LGPL.
 * 01/01/2004 : Initial version by E.B javalayer@javazoom.net
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.logging.Level;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Bitstream unit test.
 * It matches test.mp3 properties to test.mp3.properties expected results.
 * As we don't ship test.mp3, you have to generate your own test.mp3.properties
 * Uncomment out = System.out; in setUp() method to generated it on stdout from
 * your own MP3 file.
 *
 * @since 0.4
 */
public class BitstreamTest {

    private String basefile = null;
    private String name = null;
    private String filename = null;
    private Properties props = null;
    private FileInputStream mp3in = null;
    private Bitstream in = null;

    @BeforeEach
    protected void setUp() throws Exception {
        props = new Properties();
        InputStream pin = getClass().getClassLoader().getResourceAsStream("test.mp3.properties");
        props.load(pin);
        basefile = props.getProperty("basefile");
        name = props.getProperty("filename");
        filename = basefile + name;
        mp3in = new FileInputStream(filename);
        in = new Bitstream(mp3in);
    }

    @AfterEach
    protected void tearDown() throws Exception {
        in.close();
        mp3in.close();
    }

    @Test
    public void testStream() throws Exception {
        InputStream id3in = in.getRawID3v2();
        int size = id3in.available();
        Header header = in.readFrame();
        Debug.println(Level.FINE, "--- " + filename + " ---");
        Debug.println(Level.FINE, "ID3v2Size=" + size);
        Debug.println(Level.FINE, "version=" + header.version());
        Debug.println(Level.FINE, "version_string=" + header.version_string());
        Debug.println(Level.FINE, "layer=" + header.layer());
        Debug.println(Level.FINE, "frequency=" + header.frequency());
        Debug.println(Level.FINE, "frequency_string=" + header.sample_frequency_string());
        Debug.println(Level.FINE, "bitrate=" + header.bitrate());
        Debug.println(Level.FINE, "bitrate_string=" + header.bitrate_string());
        Debug.println(Level.FINE, "mode=" + header.mode());
        Debug.println(Level.FINE, "mode_string=" + header.mode_string());
        Debug.println(Level.FINE, "slots=" + header.slots());
        Debug.println(Level.FINE, "vbr=" + header.vbr());
        Debug.println(Level.FINE, "vbr_scale=" + header.vbr_scale());
        Debug.println(Level.FINE, "max_number_of_frames=" + header.max_number_of_frames(mp3in.available()));
        Debug.println(Level.FINE, "min_number_of_frames=" + header.min_number_of_frames(mp3in.available()));
        Debug.println(Level.FINE, "ms_per_frame=" + header.ms_per_frame());
        Debug.println(Level.FINE, "frames_per_second=" + (float) ((1.0 / (header.ms_per_frame())) * 1000.0));
        Debug.println(Level.FINE, "total_ms=" + header.total_ms(mp3in.available()));
        Debug.println(Level.FINE, "SyncHeader=" + header.getSyncHeader());
        Debug.println(Level.FINE, "checksums=" + header.checksums());
        Debug.println(Level.FINE, "copyright=" + header.copyright());
        Debug.println(Level.FINE, "original=" + header.original());
        Debug.println(Level.FINE, "padding=" + header.padding());
        Debug.println(Level.FINE, "framesize=" + header.calculate_framesize());
        Debug.println(Level.FINE, "number_of_subbands=" + header.number_of_subbands());
        assertEquals(Integer.parseInt(props.getProperty("ID3v2Size")), size, "ID3v2Size");
        assertEquals(Integer.parseInt(props.getProperty("version")), header.version(), "version");
        assertEquals(props.getProperty("version_string"), header.version_string(), "version_string");
        assertEquals(Integer.parseInt(props.getProperty("layer")), header.layer(), "layer");
        assertEquals(Integer.parseInt(props.getProperty("frequency")), header.frequency(), "frequency");
        assertEquals(props.getProperty("frequency_string"), header.sample_frequency_string(), "frequency_string");
        assertEquals(Integer.parseInt(props.getProperty("bitrate")), header.bitrate(), "bitrate");
        assertEquals(props.getProperty("bitrate_string"), header.bitrate_string(), "bitrate_string");
        assertEquals(Integer.parseInt(props.getProperty("mode")), header.mode(), "mode");
        assertEquals(props.getProperty("mode_string"), header.mode_string(), "mode_string");
        assertEquals(Integer.parseInt(props.getProperty("slots")), header.slots(), "slots");
        assertEquals(Boolean.valueOf(props.getProperty("vbr")), header.vbr(), "vbr");
        assertEquals(Integer.parseInt(props.getProperty("vbr_scale")), header.vbr_scale(), "vbr_scale");
        assertEquals(Integer.parseInt(props.getProperty("max_number_of_frames")),
                header.max_number_of_frames(mp3in.available()),
                "max_number_of_frames");
        assertEquals(Integer.parseInt(props.getProperty("min_number_of_frames")),
                header.min_number_of_frames(mp3in.available()),
                "min_number_of_frames");
        assertEquals(Float.parseFloat(props.getProperty("ms_per_frame")), header.ms_per_frame(), "ms_per_frame");
        assertEquals(Float
                .parseFloat(props.getProperty("frames_per_second")), (float) ((1.0 / (header.ms_per_frame())) * 1000.0), "frames_per_second");
        assertEquals(Float.parseFloat(props.getProperty("total_ms")), header.total_ms(mp3in.available()), "total_ms");
        assertEquals(Integer.parseInt(props.getProperty("SyncHeader")), header.getSyncHeader(), "SyncHeader");
        assertEquals(Boolean.valueOf(props.getProperty("checksums")), header.checksums(), "checksums");
        assertEquals(Boolean.valueOf(props.getProperty("copyright")), header.copyright(), "copyright");
        assertEquals(Boolean.valueOf(props.getProperty("original")), header.original(), "original");
        assertEquals(Boolean.valueOf(props.getProperty("padding")), header.padding(), "padding");
        assertEquals(Integer.parseInt(props.getProperty("framesize")), header.calculate_framesize(), "framesize");
        assertEquals(Integer.parseInt(props.getProperty("number_of_subbands")),
                header.number_of_subbands(),
                "number_of_subbands");
        in.closeFrame();
    }
}
