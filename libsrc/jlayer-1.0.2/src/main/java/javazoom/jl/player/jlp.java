/*
 * 11/19/04        1.0 moved to LGPL.
 *
 * 06/04/01        Streaming support added. javalayer@javazoom.net
 *
 * 29/01/00        Initial version. mdm@techie.com
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

package javazoom.jl.player;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import javazoom.jl.decoder.JavaLayerException;


/**
 * The <code>jlp</code> class implements a simple command-line
 * player for MPEG audio files.
 *
 * @author Mat McGowan (mdm@techie.com)
 */
public class jlp {

    private static final Logger logger = Logger.getLogger(jlp.class.getName());

    private String fFilename = null;
    private boolean remote = false;
    private AudioDevice audioDevice;
    private Player player;

    public static void main(String[] args) {
        int retval = 0;
        try {
            jlp player = createInstance(args);
            if (player != null)
                player.play();
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace(System.err);
            retval = 1;
        }
        System.exit(retval);
    }

    static public jlp createInstance(String[] args) {
        jlp player = new jlp();
        if (!player.parseArgs(args))
            player = null;
        return player;
    }

    private jlp() {
    }

    public jlp(String filename) {
        init(filename);
    }

    protected void init(String filename) {
        fFilename = filename;
    }

    protected boolean parseArgs(String[] args) {
        boolean parsed = false;
        if (args.length == 1) {
            init(args[0]);
            parsed = true;
            remote = false;
        } else if (args.length == 2) {
            if (!(args[0].equals("-url"))) {
                showUsage();
            } else {
                init(args[1]);
                parsed = true;
                remote = true;
            }
        } else {
            showUsage();
        }
        return parsed;
    }

    public void showUsage() {
        System.out.println("Usage: jlp [-url] <filename>");
        System.out.println();
        System.out.println(" e.g. : java javazoom.jl.player.jlp localfile.mp3");
        System.out.println("        java javazoom.jl.player.jlp -url http://www.server.com/remotefile.mp3");
        System.out.println("        java javazoom.jl.player.jlp -url http://www.shoutcastserver.com:8000");
    }

    public void play()
            throws JavaLayerException {
        try {
            System.out.println("playing " + fFilename + "...");
            InputStream in;
            if (remote) in = getURLInputStream();
            else in = getInputStream();
            AudioDevice dev = setAudioDevice();
            logger.fine("audioDevice: " + dev);
            player = new Player(in, dev);
            player.play();
        } catch (Exception ex) {
            throw new JavaLayerException("Problem playing file " + fFilename, ex);
        }
    }

    /** @since 1.0.2 */
    public void stop() {
        player.close();
    }

    /**
     * Playing file from URL (Streaming).
     */
    protected InputStream getURLInputStream()
            throws Exception {

        URL url = new URL(fFilename);
        InputStream fin = url.openStream();
        BufferedInputStream bin = new BufferedInputStream(fin);
        return bin;
    }

    /**
     * Playing file from FileInputStream.
     */
    protected InputStream getInputStream()
            throws IOException {
        FileInputStream fin = new FileInputStream(fFilename);
        BufferedInputStream bin = new BufferedInputStream(fin);
        return bin;
    }

    /**
     * Use when you want to use a specific audio device.
     * @since 1.0.2
     */
    public void setAudioDevice(AudioDevice audioDevice) {
        this.audioDevice = audioDevice;
    }

    /**
     * if you don't use {@link #setAudioDevice},
     * default audio device depends on service loader.
     * @since 1.0.2 default audio device depends on service loader
     */
    public AudioDevice setAudioDevice()
            throws JavaLayerException {
        if (audioDevice == null) {
            audioDevice = FactoryRegistry.systemRegistry().createAudioDevice();
        }
        return audioDevice;
    }
}
