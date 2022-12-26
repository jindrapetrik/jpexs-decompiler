/*
 * 11/19/04        1.0 moved to LGPL.
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

import java.applet.Applet;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javazoom.jl.decoder.JavaLayerException;


/**
 * A simple applet that plays an MPEG audio file.
 * The URL (relative to the document base)
 * is passed as the "audioURL" parameter.
 *
 * @author Mat McGowan
 * @since 0.0.8
 */
public class PlayerApplet extends Applet implements Runnable {

    static public final String AUDIO_PARAMETER = "audioURL";

    /**
     * The Player used to play the MPEG audio file.
     */
    private Player player = null;

    /**
     * The thread that runs the player.
     */
    private Thread playerThread = null;

    private String fileName = null;

    /**
     * Retrieves the <code>AudioDevice</code> instance that will
     * be used to sound the audio data.
     *
     * @return an audio device instance that will be used to
     * sound the audio stream.
     */
    protected AudioDevice getAudioDevice() throws JavaLayerException {
        return FactoryRegistry.systemRegistry().createAudioDevice();
    }

    /**
     * Retrieves the InputStream that provides the MPEG audio
     * stream data.
     *
     * @return an InputStream from which the MPEG audio data
     * is read, or null if an error occurs.
     */
    protected InputStream getAudioStream() {
        InputStream in = null;

        try {
            URL url = getAudioURL();
            if (url != null)
                in = url.openStream();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return in;
    }

    protected String getAudioFileName() {
        String urlString = fileName;
        if (urlString == null) {
            urlString = getParameter(AUDIO_PARAMETER);
        }
        return urlString;
    }

    protected URL getAudioURL() {
        String urlString = getAudioFileName();
        URL url = null;
        if (urlString != null) {
            try {
                url = new URL(getDocumentBase(), urlString);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return url;
    }

    /**
     * Sets the URL of the audio stream to play.
     */
    public void setFileName(String name) {
        fileName = name;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Stops the audio player. If the player is already stopped
     * this method is a no-op.
     */
    protected void stopPlayer() throws JavaLayerException {
        if (player != null) {
            player.close();
            player = null;
            playerThread = null;
        }
    }

    /**
     * Decompresses audio data from an InputStream and plays it
     * back through an AudioDevice. The playback is run on a newly
     * created thread.
     *
     * @param in  The InputStream that provides the MPEG audio data.
     * @param dev The AudioDevice to use to sound the decompressed data.
     * @throws JavaLayerException if there was a problem decoding
     *                            or playing the audio data.
     */
    protected void play(InputStream in, AudioDevice dev) throws JavaLayerException {
        stopPlayer();

        if (in != null && dev != null) {
            player = new Player(in, dev);
            playerThread = createPlayerThread();
            playerThread.start();
        }
    }

    /**
     * Creates a new thread used to run the audio player.
     *
     * @return A new Thread that, once started, runs the audio player.
     */
    protected Thread createPlayerThread() {
        return new Thread(this, "Audio player thread");
    }

    /**
     * Initializes this applet.
     */
    public void init() {
    }

    /**
     * Starts this applet. An input stream and audio device
     * are created and passed to the play() method.
     */
    public void start() {
        String name = getAudioFileName();
        try {
            InputStream in = getAudioStream();
            AudioDevice dev = getAudioDevice();
            play(in, dev);
        } catch (JavaLayerException ex) {
            synchronized (System.err) {
                System.err.println("Unable to play " + name);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Stops this applet. If audio is currently playing, it is
     * stopped.
     */
    public void stop() {
        try {
            stopPlayer();
        } catch (JavaLayerException ex) {
            ex.printStackTrace();
        }
    }

    public void destroy() {
    }

    /**
     * The run method for the audio player thread. Simply calls
     * play() on the player to play the entire stream.
     */
    public void run() {
        if (player != null) {
            try {
                player.play();
            } catch (JavaLayerException ex) {
                System.err.println("Problem playing audio: " + ex);
                ex.printStackTrace();
            }
        }
    }
}
