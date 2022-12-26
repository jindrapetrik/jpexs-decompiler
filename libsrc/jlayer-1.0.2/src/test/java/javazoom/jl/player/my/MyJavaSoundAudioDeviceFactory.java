/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package javazoom.jl.player.my;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.AudioDeviceFactory;


/**
 * factory for MyJavaSoundAudioDevice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/10/03 nsano initial version <br>
 * @see "META-INF/services/javazoom.jl.player.AudioDeviceFactory"
 */
public class MyJavaSoundAudioDeviceFactory extends AudioDeviceFactory {

    private boolean tested = false;

    @Override
    public synchronized AudioDevice createAudioDevice()
            throws JavaLayerException {

        if (!tested) {
            testAudioDevice();
            tested = true;
        }

        return new MyJavaSoundAudioDevice();
    }

    private void testAudioDevice() throws JavaLayerException {
        MyJavaSoundAudioDevice dev = new MyJavaSoundAudioDevice();
        dev.test();
    }
}
